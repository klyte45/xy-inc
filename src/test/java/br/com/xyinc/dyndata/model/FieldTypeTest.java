package br.com.xyinc.dyndata.model;

import org.bson.BsonString;
import org.junit.Test;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class FieldTypeTest {

    @Test(expected = IllegalArgumentException.class)
    public void constructor_NullTarget() {
        new FieldType<>(null);
    }

    @Test
    public void toBson_Null() {
        assert new FieldType<>(Boolean.class).toBson(null) == null;
    }

    @Test
    public void toBson_String_Success() {
        FieldType type = new FieldType<>(String.class);
        String value = "Teste";
        assert value.equals(type.getValue(type.toBson(value)));
    }

    @Test
    public void toBson_String_Cast() {
        FieldType type = new FieldType<>(String.class);
        assert "1231".equals(type.getValue(type.toBson(1231)));
    }

    @Test
    public void toBson_Long_Success() {
        FieldType<Long> type = new FieldType<>(Long.class);
        assert type.getValue(type.toBson(2015L)) == 2015L;
    }

    @Test(expected = NumberFormatException.class)
    public void toBson_Long_FormatError() {
        FieldType<Long> type = new FieldType<>(Long.class);
        type.toBson("xxx");
    }

    @Test
    public void toBson_Integer_Success() {
        FieldType<Integer> type = new FieldType<>(Integer.class);
        assert type.getValue(type.toBson(2015)) == 2015;
    }

    @Test(expected = NumberFormatException.class)
    public void toBson_Integer_FormatError() {
        FieldType<Integer> type = new FieldType<>(Integer.class);
        type.toBson("xxx");
    }

    @Test
    public void toBson_BigDecimal_Success() {
        FieldType<BigDecimal> type = new FieldType<>(BigDecimal.class);
        assert type.getValue(type.toBson(2015)).compareTo(BigDecimal.valueOf(2015)) == 0;
    }

    @Test
    public void toBson_BigDecimal_Success_BigDecimal() {
        FieldType<BigDecimal> type = new FieldType<>(BigDecimal.class);
        BigDecimal value = BigDecimal.valueOf(50);
        assert type.getValue(type.toBson(value)).compareTo(value) == 0;
    }

    @Test(expected = NumberFormatException.class)
    public void toBson_BigDecimal_FormatError() {
        FieldType<BigDecimal> type = new FieldType<>(BigDecimal.class);
        type.toBson("xxx");
    }

    @Test
    public void toBson_Bool_Success() {
        FieldType<Boolean> type = new FieldType<>(Boolean.class);
        assert type.getValue(type.toBson(true));
        assert !type.getValue(type.toBson(false));
    }

    @Test(expected = ClassCastException.class)
    public void toBson_Bool_CastException() {
        FieldType<Boolean> type = new FieldType<>(Boolean.class);
        type.toBson(24234231);
    }

    @Test
    public void toBson_Timestamp_Success() {
        FieldType<Timestamp> type = new FieldType<>(Timestamp.class);
        Timestamp t = new Timestamp(1565746);
        assert type.getValue(type.toBson(t)).equals(t);
    }

    @Test
    public void toBson_Timestamp_FromTimeMillis() {
        FieldType<Timestamp> type = new FieldType<>(Timestamp.class);
        Timestamp t = new Timestamp(1565746);
        assert type.getValue(type.toBson(1565746)).equals(t);
    }

    @Test
    public void toBson_Timestamp_FromTimeString() {
        FieldType<Timestamp> type = new FieldType<>(Timestamp.class);
        Timestamp t = new Timestamp(1522615135000L);
        assert type.getValue(type.toBson("2018-04-01T20:38:55.000+0000")).equals(t);
        assert type.getValue(type.toBson("2018-04-01T17:38:55.000-0300")).equals(t);
    }

    @Test(expected = IllegalArgumentException.class)
    public void toBson_Timestamp_FromInvalidTimeString() {
        FieldType<Timestamp> type = new FieldType<>(Timestamp.class);
        type.toBson("2018-04-01T20:38:55");
    }

    @Test(expected = IllegalArgumentException.class)
    public void toBson_Timestamp_FromInvalidObject() {
        FieldType<Timestamp> type = new FieldType<>(Timestamp.class);
        type.toBson(type);
    }

    @Test(expected = NotImplementedException.class)
    public void toBson_NotImplemented() {
        FieldType<FieldType> type = new FieldType<>(FieldType.class);
        type.toBson(0);
    }

    @Test(expected = NotImplementedException.class)
    public void getValue_NotImplemented() {
        FieldType<FieldType> type = new FieldType<>(FieldType.class);
        type.getValue(new BsonString(""));
    }

}