package br.com.xyinc.dyndata.service;

import br.com.xyinc.dyndata.exception.*;
import br.com.xyinc.dyndata.model.EntityDescriptor;
import br.com.xyinc.dyndata.model.FieldDescriptor;
import br.com.xyinc.dyndata.model.FieldType;
import org.bson.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public class FieldTypeServiceTest {

    @Mock
    private MongoService     mongoService;
    @InjectMocks
    @Resource
    private FieldTypeService fieldTypeService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(mongoService.getNextSequence(any())).thenReturn(35L);
    }

    @Test
    public void getType_checkBasicRegistered() {
        assert fieldTypeService.getType(FieldTypeService.DefaultFieldTypes.STRING) != null;
        assert fieldTypeService.getType(FieldTypeService.DefaultFieldTypes.BOOLEAN) != null;
        assert fieldTypeService.getType(FieldTypeService.DefaultFieldTypes.DECIMAL) != null;
        assert fieldTypeService.getType(FieldTypeService.DefaultFieldTypes.INTEGER) != null;
        assert fieldTypeService.getType(FieldTypeService.DefaultFieldTypes.LONG) != null;
        assert fieldTypeService.getType(FieldTypeService.DefaultFieldTypes.TIMESTAMP) != null;
        assert fieldTypeService.getType(FieldTypeService.DefaultFieldTypes.DOCUMENT) != null;
        assert fieldTypeService.getType(FieldTypeService.DefaultFieldTypes.STRING_ARR) != null;
        assert fieldTypeService.getType(FieldTypeService.DefaultFieldTypes.BOOLEAN_ARR) != null;
        assert fieldTypeService.getType(FieldTypeService.DefaultFieldTypes.DECIMAL_ARR) != null;
        assert fieldTypeService.getType(FieldTypeService.DefaultFieldTypes.INTEGER_ARR) != null;
        assert fieldTypeService.getType(FieldTypeService.DefaultFieldTypes.LONG_ARR) != null;
        assert fieldTypeService.getType(FieldTypeService.DefaultFieldTypes.TIMESTAMP_ARR) != null;
        assert fieldTypeService.getType(FieldTypeService.DefaultFieldTypes.DOCUMENT_ARR) != null;
        assert fieldTypeService.getAllowedTypes().size() == 14;
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor_NullTarget() {
        new FieldType(null);
    }

    @Test
    public void toBson_Null() {
        assert fieldTypeService.toBson(null, null, new FieldType(Boolean.class), new ArrayList<>()).getClass() == BsonNull.class;
    }

    @Test
    public void toBson_String_Success() {
        FieldType type  = new FieldType(String.class);
        String    value = "Teste";
        assert fieldTypeService.toBson(value, null, type, new ArrayList<>()).asString().getValue().equals(value);
    }

    @Test
    public void toBson_String_Cast() {
        FieldType type = new FieldType(String.class);
        assert "1231".equals(fieldTypeService.toBson(1231, null, type, new ArrayList<>()).asString().getValue());
    }

    @Test
    public void toBson_Long_Success() {
        FieldType type = new FieldType(Long.class);
        assert fieldTypeService.toBson(1231000000000L, null, type, new ArrayList<>()).asNumber().longValue() == 1231000000000L;
    }

    @Test(expected = NumberFormatException.class)
    public void toBson_Long_FormatError() {
        FieldType type = new FieldType(Long.class);
        fieldTypeService.toBson("XXX", null, type, new ArrayList<>());
    }

    @Test
    public void toBson_Integer_Success() {
        FieldType type = new FieldType(Integer.class);
        assert fieldTypeService.toBson(2015, null, type, new ArrayList<>()).asNumber().longValue() == 2015;
    }

    @Test(expected = NumberFormatException.class)
    public void toBson_Integer_FormatError() {
        FieldType type = new FieldType(Integer.class);
        fieldTypeService.toBson("XXX", null, type, new ArrayList<>());
    }

    @Test
    public void toBson_BigDecimal_Success() {
        FieldType  type      = new FieldType(BigDecimal.class);
        BigDecimal value     = BigDecimal.valueOf(2015);
        BsonValue  bsonValue = fieldTypeService.toBson(2015, null, type, new ArrayList<>());
        assert value.compareTo(bsonValue.asDecimal128().decimal128Value().bigDecimalValue()) == 0;
    }

    @Test
    public void toBson_BigDecimal_Success_BigDecimal() {
        FieldType  type      = new FieldType(BigDecimal.class);
        BigDecimal value     = BigDecimal.valueOf(2015);
        BsonValue  bsonValue = fieldTypeService.toBson(value, null, type, new ArrayList<>());
        assert value.compareTo(bsonValue.asDecimal128().decimal128Value().bigDecimalValue()) == 0;
    }

    @Test(expected = NumberFormatException.class)
    public void toBson_BigDecimal_FormatError() {
        FieldType type = new FieldType(BigDecimal.class);
        fieldTypeService.toBson("XXX", null, type, new ArrayList<>());
    }

    @Test
    public void toBson_Bool_Success() {
        FieldType type = new FieldType(Boolean.class);
        assert fieldTypeService.toBson(true, null, type, new ArrayList<>()).asBoolean().getValue();
        assert !fieldTypeService.toBson(false, null, type, new ArrayList<>()).asBoolean().getValue();
    }

    @Test
    public void toBson_Bool_SuccessString() {
        FieldType type = new FieldType(Boolean.class);
        assert fieldTypeService.toBson("true", null, type, new ArrayList<>()).asBoolean().getValue();
        assert !fieldTypeService.toBson("TRUE", null, type, new ArrayList<>()).asBoolean().getValue();
    }

    @Test(expected = ClassCastException.class)
    public void toBson_Bool_CastException() {
        FieldType type = new FieldType(Boolean.class);
        fieldTypeService.toBson(21245, null, type, new ArrayList<>());
    }

    @Test
    public void toBson_Timestamp_Success() {
        FieldType type   = new FieldType(Timestamp.class);
        Timestamp t      = new Timestamp(1565746);
        BsonValue result = fieldTypeService.toBson(t, null, type, new ArrayList<>());
        Assert.assertEquals(t.getTime(), result.asTimestamp().getValue());
    }

    @Test
    public void toBson_Timestamp_FromTimeMillis() {
        FieldType type   = new FieldType(Timestamp.class);
        long      t      = 1565746455L;
        BsonValue result = fieldTypeService.toBson(t, null, type, new ArrayList<>());
        Assert.assertEquals(t, result.asTimestamp().getValue());
    }

    @Test
    public void toBson_Timestamp_FromTimeString() {
        FieldType type = new FieldType(Timestamp.class);
        Timestamp t    = new Timestamp(1522615135000L);
        BsonValue gmt  = fieldTypeService.toBson("2018-04-01T20:38:55.000+0000", null, type, new ArrayList<>());
        BsonValue bsb  = fieldTypeService.toBson("2018-04-01T17:38:55.000-0300", null, type, new ArrayList<>());
        Assert.assertEquals(t.getTime(), gmt.asTimestamp().getValue());
        Assert.assertEquals(t.getTime(), bsb.asTimestamp().getValue());
    }

    @Test(expected = TimestampFormatException.class)
    public void toBson_Timestamp_FromInvalidTimeString() {
        FieldType type = new FieldType(Timestamp.class);
        fieldTypeService.toBson("2018-04-01T20:38:55", null, type, new ArrayList<>());
    }

    @Test(expected = TimestampFormatException.class)
    public void toBson_Timestamp_FromInvalidObject() {
        FieldType type = new FieldType(Timestamp.class);
        fieldTypeService.toBson(type, null, type, new ArrayList<>());
    }

    @Test(expected = NotImplementedException.class)
    public void toBson_NotImplemented() {
        FieldType type = new FieldType(FieldType.class);
        fieldTypeService.toBson(0, null, type, new ArrayList<>());
    }

    @Test
    public void toBson_BsonValue() {
        FieldType type = new FieldType(FieldType.class);
        BsonValue val  = new BsonNull();
        assert fieldTypeService.toBson(val, null, type, new ArrayList<>()) == val;
    }

    @Test
    public void toBson_ArrayValue() {
        FieldType     type        = new FieldType(Boolean[].class);
        List<Boolean> val         = Arrays.asList(true, false, true);
        BsonValue     result      = fieldTypeService.toBson(val, null, type, new ArrayList<>());
        BsonArray     resultArray = result.asArray();
        assert resultArray.size() == val.size();
        for (int i = 0; i < val.size(); i++) {
            assert val.get(i) == resultArray.get(i).asBoolean().getValue();
        }
    }

    @Test(expected = FieldValidationException.class)
    public void toBson_ArrayValue_NotArrayParam() {
        FieldType type = new FieldType(Boolean[].class);
        Boolean   val  = false;
        fieldTypeService.toBson(val, null, type, new ArrayList<>());
    }

    @Test
    public void toBson_DocumentValue() {
        List<FieldDescriptor> fieldsDocument = Collections.singletonList(new FieldDescriptor("teste", FieldTypeService.DefaultFieldTypes.STRING, true));
        FieldType             type           = new FieldType(Map.class);
        Map<String, Object>   val            = new HashMap<>();
        val.put("teste", "teste");
        val.put("esteNãoDeveEstarLá", "teste");

        BsonValue    result   = fieldTypeService.toBson(val, fieldsDocument, type, new ArrayList<>());
        BsonDocument document = result.asDocument();
        assertResultsMapString(fieldsDocument, val, document);
    }

    @Test(expected = DocumentParseException.class)
    public void toBson_DocumentValue_NonMap() {
        List<FieldDescriptor> fieldsDocument = Collections.singletonList(new FieldDescriptor("teste", FieldTypeService.DefaultFieldTypes.STRING, true));
        FieldType             type           = new FieldType(Map.class);
        String                val            = "";
        fieldTypeService.toBson(val, fieldsDocument, type, new ArrayList<>());
    }

    @Test(expected = EntityDescriptorException.class)
    public void iterateFields_InvalidValueType() {
        List<FieldDescriptor> fieldsDocument = Collections.singletonList(new FieldDescriptor("teste", "!!!!!", true));
        BsonDocument          result         = new BsonDocument();
        Map<String, Object>   val            = new HashMap<>();
        fieldTypeService.iterateFields(fieldsDocument, val, new ArrayList<>(), result);
    }

    @Test(expected = FieldValidationException.class)
    public void iterateFields_NotNull_SendNull() {
        List<FieldDescriptor> fieldsDocument = getFieldDescriptorInt();
        Map<String, Object>   val            = new HashMap<>();
        BsonDocument          result         = new BsonDocument();

        fieldTypeService.iterateFields(fieldsDocument, val, new ArrayList<>(), result);
        assertResultsMapNumber(fieldsDocument, val, result);
    }

    @Test
    public void iterateFields_NotNull_WithDefault() {
        List<FieldDescriptor> fieldsDocument = getFieldDescriptorInt();
        fieldsDocument.get(0).setDefaultValue("0");
        Map<String, Object> val    = new HashMap<>();
        BsonDocument        result = new BsonDocument();

        fieldTypeService.iterateFields(fieldsDocument, val, new ArrayList<>(), result);
        val.put("teste", 0);
        assertResultsMapNumber(fieldsDocument, val, result);
    }

    @Test(expected = EntityKeyException.class)
    public void iterateFields_Null_IsKey() {
        List<FieldDescriptor> fieldsDocument = getFieldDescriptorInt();
        EntityDescriptor      descriptor     = new EntityDescriptor();
        descriptor.setFields(fieldsDocument);
        descriptor.setKeys(Collections.singletonList("teste"));
        Map<String, Object> val    = new HashMap<>();
        BsonDocument        result = new BsonDocument();

        fieldTypeService.iterateFields(fieldsDocument, val, new ArrayList<>(), result, descriptor);
    }

    @Test
    public void iterateFields_NotNull_IsSequence() {
        List<FieldDescriptor> fieldsDocument = getFieldDescriptorInt();

        EntityDescriptor ed = new EntityDescriptor();
        ed.setFields(fieldsDocument);
        ed.setSequenceField("teste");

        Map<String, Object> val    = new HashMap<>();
        BsonDocument        result = new BsonDocument();

        fieldTypeService.iterateFields(fieldsDocument, val, new ArrayList<>(), result, ed);
        val.put("teste", 35L);
        assertResultsMapNumber(fieldsDocument, val, result);
    }


    @Test
    public void iterateFields_MinMaxValidation_OK() {
        List<FieldDescriptor> fieldsDocument = getFieldDescriptorInt();
        Map<String, Object>   val            = new HashMap<>();
        val.put("teste", 15);
        val.put("esteNãoDeveEstarLá", "teste");
        BsonDocument result = new BsonDocument();

        fieldTypeService.iterateFields(fieldsDocument, val, new ArrayList<>(), result);
        assertResultsMapNumber(fieldsDocument, val, result);
    }


    @Test(expected = FieldValidationException.class)
    public void iterateFields_MinMaxValidation_BelowMin() {
        List<FieldDescriptor> fieldsDocument = getFieldDescriptorInt();
        Map<String, Object>   val            = new HashMap<>();
        val.put("teste", 5);
        val.put("esteNãoDeveEstarLá", "teste");
        BsonDocument result = new BsonDocument();

        fieldTypeService.iterateFields(fieldsDocument, val, new ArrayList<>(), result);
    }

    @Test(expected = FieldValidationException.class)
    public void iterateFields_MinMaxValidation_AboveMax() {
        List<FieldDescriptor> fieldsDocument = getFieldDescriptorInt();
        Map<String, Object>   val            = new HashMap<>();
        val.put("teste", 25);
        val.put("esteNãoDeveEstarLá", "teste");
        BsonDocument result = new BsonDocument();

        fieldTypeService.iterateFields(fieldsDocument, val, new ArrayList<>(), result);
    }

    @Test
    public void iterateFields_LengthValidation_String_OK() {
        List<FieldDescriptor> fieldsDocument = getFieldDescriptorString();
        Map<String, Object>   val            = new HashMap<>();
        val.put("teste", "12345678901");
        val.put("esteNãoDeveEstarLá", "teste");
        BsonDocument result = new BsonDocument();

        fieldTypeService.iterateFields(fieldsDocument, val, new ArrayList<>(), result);
        assertResultsMapString(fieldsDocument, val, result);
    }

    @Test(expected = FieldValidationException.class)
    public void iterateFields_LengthValidation_String_Short() {
        List<FieldDescriptor> fieldsDocument = getFieldDescriptorString();
        Map<String, Object>   val            = new HashMap<>();
        val.put("teste", "123456");
        val.put("esteNãoDeveEstarLá", "teste");
        BsonDocument result = new BsonDocument();

        fieldTypeService.iterateFields(fieldsDocument, val, new ArrayList<>(), result);
    }

    @Test(expected = FieldValidationException.class)
    public void iterateFields_LengthValidation_String_Long() {
        List<FieldDescriptor> fieldsDocument = getFieldDescriptorString();
        Map<String, Object>   val            = new HashMap<>();
        val.put("teste", "12345678901234567890123456789");
        val.put("esteNãoDeveEstarLá", "teste");
        BsonDocument result = new BsonDocument();

        fieldTypeService.iterateFields(fieldsDocument, val, new ArrayList<>(), result);
    }

    @Test(expected = FieldValidationException.class)
    public void iterateFields_LengthValidation_Array_Short() {
        List<FieldDescriptor> fieldsDocument = getFieldDescriptorStringArray();
        Map<String, Object>   val            = new HashMap<>();
        val.put("teste", Collections.singletonList("x"));
        val.put("esteNãoDeveEstarLá", "teste");
        BsonDocument result = new BsonDocument();

        fieldTypeService.iterateFields(fieldsDocument, val, new ArrayList<>(), result);
    }

    @Test(expected = FieldValidationException.class)
    public void iterateFields_LengthValidation_Array_Long() {
        List<FieldDescriptor> fieldsDocument = getFieldDescriptorStringArray();
        Map<String, Object>   val            = new HashMap<>();
        val.put("teste", Arrays.asList("x", "a", "a", "as", "da"));
        val.put("esteNãoDeveEstarLá", "teste");
        BsonDocument result = new BsonDocument();

        fieldTypeService.iterateFields(fieldsDocument, val, new ArrayList<>(), result);
    }

    @Test
    public void iterateFields_OptionsString_OK() {
        List<FieldDescriptor> fieldsDocument = getFieldDescriptorStringOptions();
        Map<String, Object>   val            = new HashMap<>();
        val.put("teste", "A");
        val.put("esteNãoDeveEstarLá", "teste");
        BsonDocument result = new BsonDocument();

        fieldTypeService.iterateFields(fieldsDocument, val, new ArrayList<>(), result);
        assertResultsMapString(fieldsDocument, val, result);
    }

    @Test(expected = FieldValidationException.class)
    public void iterateFields_OptionsString_NOK() {
        List<FieldDescriptor> fieldsDocument = getFieldDescriptorStringOptions();
        Map<String, Object>   val            = new HashMap<>();
        val.put("teste", "K");
        val.put("esteNãoDeveEstarLá", "teste");
        BsonDocument result = new BsonDocument();

        fieldTypeService.iterateFields(fieldsDocument, val, new ArrayList<>(), result);
        assertResultsMapString(fieldsDocument, val, result);
    }


    @Test
    public void toDocument_EmptyDescriptor() {
        EntityDescriptor    descriptor = new EntityDescriptor();
        Map<String, Object> val        = new HashMap<>();
        val.put("teste", "K");
        val.put("esteNãoDeveEstarLá", "teste");
        Document document = fieldTypeService.toDocument(descriptor, val);

        assert document.isEmpty();
    }

    @Test(expected = DocumentParseException.class)
    public void toDocument_FieldError() {
        EntityDescriptor descriptor = new EntityDescriptor();
        descriptor.setFields(getFieldDescriptorStringOptions());
        Map<String, Object> val = new HashMap<>();
        val.put("teste", "K");
        val.put("esteNãoDeveEstarLá", "teste");
        fieldTypeService.toDocument(descriptor, val);
    }

    private List<FieldDescriptor> getFieldDescriptorInt() {
        FieldDescriptor intFieldDescriptor = new FieldDescriptor("teste", FieldTypeService.DefaultFieldTypes.INTEGER, false);
        intFieldDescriptor.setMin(BigDecimal.valueOf(10));
        intFieldDescriptor.setMax(BigDecimal.valueOf(20));
        return Collections.singletonList(intFieldDescriptor);
    }

    private List<FieldDescriptor> getFieldDescriptorString() {
        FieldDescriptor strFieldDescriptor = new FieldDescriptor("teste", FieldTypeService.DefaultFieldTypes.STRING, false);
        strFieldDescriptor.setMinLength(10);
        strFieldDescriptor.setMaxLength(20);
        return Collections.singletonList(strFieldDescriptor);
    }

    private List<FieldDescriptor> getFieldDescriptorStringOptions() {
        FieldDescriptor strFieldDescriptor = new FieldDescriptor("teste", FieldTypeService.DefaultFieldTypes.STRING, false);
        strFieldDescriptor.setAllowedValues(Arrays.asList("A", "B", "C"));
        return Collections.singletonList(strFieldDescriptor);
    }


    private List<FieldDescriptor> getFieldDescriptorStringArray() {
        FieldDescriptor strFieldDescriptor = new FieldDescriptor("teste", FieldTypeService.DefaultFieldTypes.STRING_ARR, false);
        strFieldDescriptor.setMinLength(2);
        strFieldDescriptor.setMaxLength(4);
        return Collections.singletonList(strFieldDescriptor);
    }

    private void assertResultsMapNumber(List<FieldDescriptor> fieldsDocument, Map<String, Object> val, BsonDocument result) {
        List<String> allowedFields = fieldsDocument.stream().map(FieldDescriptor::getFieldName).collect(Collectors.toList());
        for (Map.Entry<String, BsonValue> entry : result.entrySet()) {
            assert allowedFields.contains(entry.getKey());
            assert ((Number) val.get(entry.getKey())).doubleValue() == entry.getValue().asNumber().doubleValue();
        }
    }

    private void assertResultsMapString(List<FieldDescriptor> fieldsDocument, Map<String, Object> val, BsonDocument result) {
        List<String> allowedFields = fieldsDocument.stream().map(FieldDescriptor::getFieldName).collect(Collectors.toList());
        for (Map.Entry<String, BsonValue> entry : result.entrySet()) {
            assert allowedFields.contains(entry.getKey());
            assert val.get(entry.getKey()).equals(entry.getValue().asString().getValue());
        }
    }
}