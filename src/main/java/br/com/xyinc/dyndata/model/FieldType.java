package br.com.xyinc.dyndata.model;

import org.bson.*;
import org.bson.types.Decimal128;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class FieldType<T> {

    private final static SimpleDateFormat defaultDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    private Class<T> targetClass;

    public FieldType(Class<T> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("Classe da implementação não pode ser nula!");
        }
        targetClass = clazz;
    }

    @SuppressWarnings("unchecked")
    public BsonValue toBson(Object val) {
        if (val == null) {
            return null;
        }
        boolean isAssignable = targetClass.isAssignableFrom(val.getClass());

        if (String.class.isAssignableFrom(targetClass)) {
            return new BsonString(val.toString());
        } else if (Long.class.isAssignableFrom(targetClass)) {
            Long targetValue;
            if (isAssignable) {
                targetValue = (Long) val;
            } else {
                targetValue = Long.parseLong(val.toString());
            }
            return new BsonInt64(targetValue);
        } else if (Integer.class.isAssignableFrom(targetClass)) {
            Integer targetValue;
            if (isAssignable) {
                targetValue = (Integer) val;
            } else {
                targetValue = Integer.parseInt(val.toString());
            }
            return new BsonInt32(targetValue);
        } else if (BigDecimal.class.isAssignableFrom(targetClass)) {
            BigDecimal targetValue;
            if (isAssignable) {
                targetValue = (BigDecimal) val;
            } else {
                targetValue = new BigDecimal(val.toString());
            }
            return new BsonDecimal128(new Decimal128(targetValue));
        } else if (Boolean.class.isAssignableFrom(targetClass)) {
            return new BsonBoolean((Boolean) val);
        } else if (Timestamp.class.isAssignableFrom(targetClass)) {
            Timestamp result;
            if (isAssignable) {
                result = (Timestamp) val;
            } else if (String.class.isAssignableFrom(val.getClass())) {
                try {
                    result = new Timestamp(defaultDateTimeFormat.parse(val.toString()).getTime());
                } catch (ParseException e) {
                    throw new IllegalArgumentException("Campos tipo Timestamp devem ser enviados no formato ISO");
                }
            } else {
                Long timestampMillis;
                if (Number.class.isAssignableFrom(val.getClass())) {
                    timestampMillis = ((Number) val).longValue();
                } else {
                    throw new IllegalArgumentException("Valor inválido para o campo Timestamp: " + val);
                }
                result = new Timestamp(timestampMillis);
            }
            return new BsonDateTime(result.getTime());
        } else {
            throw new NotImplementedException();
        }
    }

    @SuppressWarnings("unchecked")
    public T getValue(BsonValue val) {
        if (val == null) return null;
        if (String.class.isAssignableFrom(targetClass)) {
            return (T) val.asString().getValue();
        } else if (Long.class.isAssignableFrom(targetClass)) {
            return (T) (Long) val.asInt64().getValue();
        } else if (Integer.class.isAssignableFrom(targetClass)) {
            return (T) (Integer) val.asInt32().getValue();
        } else if (BigDecimal.class.isAssignableFrom(targetClass)) {
            return (T) val.asDecimal128().getValue().bigDecimalValue();
        } else if (Boolean.class.isAssignableFrom(targetClass)) {
            return (T) (Boolean) val.asBoolean().getValue();
        } else if (Timestamp.class.isAssignableFrom(targetClass)) {
            return (T) new Timestamp(val.asDateTime().getValue());
        } else {
            throw new NotImplementedException();
        }
    }
}
