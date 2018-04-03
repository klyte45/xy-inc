package br.com.xyinc.dyndata.service;

import br.com.xyinc.dyndata.exception.*;
import br.com.xyinc.dyndata.model.EntityDescriptor;
import br.com.xyinc.dyndata.model.FieldDescriptor;
import br.com.xyinc.dyndata.model.FieldType;
import org.apache.commons.lang3.ArrayUtils;
import org.bson.*;
import org.bson.types.Decimal128;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Serviço para tratar dos tipos de dados nas entidades e conversão JSON &lt;=&gt; BSON.
 */
@Service
public class FieldTypeService {
    private static final SimpleDateFormat       defaultDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    private static final Map<String, FieldType> registeredTypes       = new HashMap<>();

    static {
        registeredTypes.put(DefaultFieldTypes.STRING, new FieldType(String.class));
        registeredTypes.put(DefaultFieldTypes.LONG, new FieldType(Long.class));
        registeredTypes.put(DefaultFieldTypes.INTEGER, new FieldType(Integer.class));
        registeredTypes.put(DefaultFieldTypes.TIMESTAMP, new FieldType(Timestamp.class));
        registeredTypes.put(DefaultFieldTypes.DECIMAL, new FieldType(BigDecimal.class));
        registeredTypes.put(DefaultFieldTypes.BOOLEAN, new FieldType(Boolean.class));
        registeredTypes.put(DefaultFieldTypes.DOCUMENT, new FieldType(Map.class));

        registeredTypes.put(DefaultFieldTypes.STRING_ARR, new FieldType(String[].class));
        registeredTypes.put(DefaultFieldTypes.LONG_ARR, new FieldType(Long[].class));
        registeredTypes.put(DefaultFieldTypes.INTEGER_ARR, new FieldType(Integer[].class));
        registeredTypes.put(DefaultFieldTypes.TIMESTAMP_ARR, new FieldType(Timestamp[].class));
        registeredTypes.put(DefaultFieldTypes.DECIMAL_ARR, new FieldType(BigDecimal[].class));
        registeredTypes.put(DefaultFieldTypes.BOOLEAN_ARR, new FieldType(Boolean[].class));
        registeredTypes.put(DefaultFieldTypes.DOCUMENT_ARR, new FieldType(Map[].class));
    }

    @Autowired
    private MongoService mongoService;

    /**
     * Obtém o descritor de tipo a partir do nome dele.
     *
     * @param typeName Nome do tipo
     * @return FieldType que descreve o tipo
     */
    public FieldType getType(String typeName) {
        return registeredTypes.get(typeName);
    }

    /**
     * Obtém todas os nomes de tipos registrados
     *
     * @return Nomes dos tipos registrados, em Set&lt;String&gt;
     */
    public Set<String> getAllowedTypes() {
        return registeredTypes.keySet();
    }

    /**
     * Transforma um descritor de dados JSON em um documento BSON pronto para inserção
     *
     * @param descriptor Descritor da entidade
     * @param values     Map com valores a serem inseridos
     * @return Documento BSON com os dados enviados
     */
    public Document toDocument(EntityDescriptor descriptor, Map<String, Object> values) {
        List<String> tracker = new ArrayList<>();
        try {
            return toDocument(descriptor, values, tracker);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new DocumentParseException("[" + String.join("=>", ArrayUtils.toStringArray(tracker.toArray())) + "] " + e.getMessage(), e);
        }
    }

    private Document toDocument(EntityDescriptor descriptor, Map<String, Object> values, List<String> tracking) {
        List<FieldDescriptor> fields = descriptor.getFields();
        Document              result = new Document();
        if (fields != null && !fields.isEmpty()) {
            iterateFields(fields, values, tracking, result, descriptor);
        }
        return result;
    }

    private BsonDocument toBsonDocument(List<FieldDescriptor> fields, Map<String, Object> values, List<String> tracking) {
        BsonDocument result = new BsonDocument();
        if (fields != null && !fields.isEmpty()) {
            iterateFields(fields, values, tracking, result);
        }
        return result;
    }

    void iterateFields(List<FieldDescriptor> fields, Map<String, Object> values, List<String> tracking, final Map result) {
        iterateFields(fields, values, tracking, result, null);
    }

    @SuppressWarnings("unchecked")
    void iterateFields(List<FieldDescriptor> fields, Map<String, Object> values, List<String> tracking, final Map result, EntityDescriptor descriptor) {
        String       seqField  = null;
        List<String> keyFields = new ArrayList<>();
        if (descriptor != null) {
            seqField = descriptor.getSequenceField();
            keyFields = descriptor.getKeys();
        }

        for (FieldDescriptor fieldDescriptor : fields) {
            tracking.add(fieldDescriptor.getFieldName());
            Object targetValue = values.get(fieldDescriptor.getFieldName());
            if (!registeredTypes.containsKey(fieldDescriptor.getFieldType())) {
                throw new EntityDescriptorException("Tipo de campo não registrado! (" + fieldDescriptor.getFieldType() + ")");
            }
            FieldType type = registeredTypes.get(fieldDescriptor.getFieldType());

            BsonValue bsonValue = toBson(targetValue, fieldDescriptor.getDocumentFields(), type, tracking);

            if (bsonValue.getClass() == BsonNull.class) {
                bsonValue = doNullValidation(tracking, descriptor, seqField, keyFields, fieldDescriptor, type, bsonValue);
            } else if (type.isNumeric()) {
                doNumericValidation(fieldDescriptor, bsonValue);
            } else if (type.isString() || type.isArray()) {
                doStringAndArrayValidation(fieldDescriptor, type, bsonValue);
            }

            result.put(fieldDescriptor.getFieldName(), bsonValue);
            tracking.remove(fieldDescriptor.getFieldName());
        }
    }

    private void doStringAndArrayValidation(FieldDescriptor fieldDescriptor, FieldType type, BsonValue bsonValue) {
        if (type.isString() && fieldDescriptor.getAllowedValues() != null && fieldDescriptor.getAllowedValues().size() > 0) {
            if (!fieldDescriptor.getAllowedValues().contains(bsonValue.asString().getValue())) {
                throw new FieldValidationException(String.format("Valor inválido para o campo '%s'. Deve ser um dos seguintes valores: ['%s']", fieldDescriptor.getFieldName(), String.join("','", fieldDescriptor.getAllowedValues())));
            }
        } else {
            if (fieldDescriptor.getMinLength() != null) {
                if (type.isString() && bsonValue.asString().getValue().length() < fieldDescriptor.getMinLength()) {
                    throw new FieldValidationException(String.format("Campo '%s' deve conter no mínimo %s caracteres!", fieldDescriptor.getFieldName(), fieldDescriptor.getMinLength()));
                }
                if (type.isArray() && bsonValue.asArray().size() < fieldDescriptor.getMinLength()) {
                    throw new FieldValidationException(String.format("A lista do campo '%s' deve conter no mínimo %s itens!", fieldDescriptor.getFieldName(), fieldDescriptor.getMinLength()));
                }
            }
            if (fieldDescriptor.getMaxLength() != null) {
                if (type.isString() && bsonValue.asString().getValue().length() > fieldDescriptor.getMaxLength()) {
                    throw new FieldValidationException(String.format("Campo '%s' deve conter no máximo %s caracteres!", fieldDescriptor.getFieldName(), fieldDescriptor.getMaxLength()));
                }
                if (type.isArray() && bsonValue.asArray().size() > fieldDescriptor.getMaxLength()) {
                    throw new FieldValidationException(String.format("A lista do campo '%s' deve conter no máximo %s itens!", fieldDescriptor.getFieldName(), fieldDescriptor.getMaxLength()));
                }
            }
        }
    }

    private void doNumericValidation(FieldDescriptor fieldDescriptor, BsonValue bsonValue) {
        if (fieldDescriptor.getMin() != null) {
            if (bsonValue.asNumber().doubleValue() < fieldDescriptor.getMin().doubleValue()) {
                throw new FieldValidationException(String.format("Valor do campo '%s' deve ser maior ou igual a %s !", fieldDescriptor.getFieldName(), fieldDescriptor.getMin()));
            }
        }
        if (fieldDescriptor.getMax() != null) {
            if (bsonValue.asNumber().doubleValue() > fieldDescriptor.getMax().doubleValue()) {
                throw new FieldValidationException(String.format("Valor do campo '%s' deve ser menor ou igual a %s !", fieldDescriptor.getFieldName(), fieldDescriptor.getMin()));
            }
        }
    }

    private BsonValue doNullValidation(List<String> tracking, EntityDescriptor descriptor, String seqField, List<String> keyFields, FieldDescriptor fieldDescriptor, FieldType type, BsonValue bsonValue) {
        if (fieldDescriptor.getFieldName().equals(seqField)) {
            bsonValue = toBson(mongoService.getNextSequence(descriptor), fieldDescriptor.getDocumentFields(), type, tracking);
        } else if (fieldDescriptor.getDefaultValue() != null) {
            bsonValue = toBson(fieldDescriptor.getDefaultValue(), fieldDescriptor.getDocumentFields(), type, tracking);
        } else if (keyFields.contains(fieldDescriptor.getFieldName())) {
            throw new EntityKeyException(String.format("Campo '%s' é chave da entidade e não pode ser nulo!", fieldDescriptor.getFieldName()));
        } else if (!fieldDescriptor.getNullable()) {
            throw new FieldValidationException(String.format("Valor do campo '%s' não pode ser nulo!", fieldDescriptor.getFieldName()));
        }
        return bsonValue;
    }

    @SuppressWarnings("unchecked")
    BsonValue toBson(Object valOrig, List<FieldDescriptor> docDescriptor, FieldType fieldType, List<String> tracking) {
        if (valOrig == null) {
            return new BsonNull();
        }
        boolean isArray = fieldType.isArray();

        if (isArray != List.class.isAssignableFrom(valOrig.getClass())) {
            throw new FieldValidationException("Objeto " + (isArray ? "DEVE" : "NÃO DEVE") + " ser um array!");
        }

        if (isArray) {
            BsonArray result        = new BsonArray();
            Class<?>  componentType = fieldType.getTargetClass().getComponentType();
            for (Object val : (List) valOrig) {
                result.add(getBsonValueForItem(val, docDescriptor, componentType, tracking));
            }
            return result;
        } else {
            return getBsonValueForItem(valOrig, docDescriptor, fieldType.getTargetClass(), tracking);
        }
    }

    private BsonValue getBsonValueForItem(Object val, List<FieldDescriptor> docDescriptor, Class<?> targetClass, List<String> tracking) {
        if (val instanceof BsonValue) {
            return (BsonValue) val;
        } else if (Map.class.isAssignableFrom(targetClass)) {
            return parseBsonDocument(val, docDescriptor, tracking);
        } else if (String.class.isAssignableFrom(targetClass)) {
            return new BsonString(val.toString());
        } else if (Long.class.isAssignableFrom(targetClass)) {
            return parseBsonLong(val, targetClass);
        } else if (Integer.class.isAssignableFrom(targetClass)) {
            return parseBsonInt(val, targetClass);
        } else if (BigDecimal.class.isAssignableFrom(targetClass)) {
            return parseBsonDecimal(val, targetClass);
        } else if (Boolean.class.isAssignableFrom(targetClass)) {
            return parseBsonBoolean(val);
        } else if (Timestamp.class.isAssignableFrom(targetClass)) {
            return parseBsonTimestamp(val, targetClass);
        } else {
            throw new NotImplementedException();
        }
    }

    private BsonValue parseBsonTimestamp(Object val, Class<?> targetClass) {
        boolean   isAssignable = targetClass.isAssignableFrom(val.getClass());
        Timestamp result;
        if (isAssignable) {
            result = (Timestamp) val;
        } else if (String.class.isAssignableFrom(val.getClass())) {
            try {
                result = new Timestamp(defaultDateTimeFormat.parse(val.toString()).getTime());
            } catch (ParseException e) {
                throw new TimestampFormatException("Campos tipo Timestamp devem ser enviados no formato ISO");
            }
        } else {
            Long timestampMillis;
            if (Number.class.isAssignableFrom(val.getClass())) {
                timestampMillis = ((Number) val).longValue();
            } else {
                throw new TimestampFormatException("Valor inválido para o campo Timestamp: " + val);
            }
            result = new Timestamp(timestampMillis);
        }
        return new BsonTimestamp(result.getTime());
    }

    private BsonValue parseBsonBoolean(Object val) {
        if (String.class.isAssignableFrom(val.getClass())) {
            return new BsonBoolean(val.equals("true"));
        } else {
            return new BsonBoolean((Boolean) val);
        }
    }

    private BsonValue parseBsonDecimal(Object val, Class<?> targetClass) {
        boolean    isAssignable = targetClass.isAssignableFrom(val.getClass());
        BigDecimal targetValue;
        if (isAssignable) {
            targetValue = (BigDecimal) val;
        } else {
            targetValue = new BigDecimal(val.toString());
        }
        return new BsonDecimal128(new Decimal128(targetValue));
    }

    private BsonValue parseBsonInt(Object val, Class<?> targetClass) {
        boolean isAssignable = targetClass.isAssignableFrom(val.getClass());
        Integer targetValue;
        if (isAssignable) {
            targetValue = (Integer) val;
        } else {
            targetValue = Integer.parseInt(val.toString());
        }
        return new BsonInt32(targetValue);
    }

    private BsonValue parseBsonLong(Object val, Class<?> targetClass) {
        boolean isAssignable = targetClass.isAssignableFrom(val.getClass());
        Long    targetValue;
        if (isAssignable) {
            targetValue = (Long) val;
        } else {
            targetValue = Long.parseLong(val.toString());
        }
        return new BsonInt64(targetValue);
    }

    @SuppressWarnings("unchecked")
    private BsonValue parseBsonDocument(Object val, List<FieldDescriptor> docDescriptor, List<String> tracking) {
        Map<String, Object> hashmap;
        try {
            hashmap = (Map<String, Object>) val;
        } catch (ClassCastException e) {
            throw new DocumentParseException("Elemento deveria ser um mapa de valores! Encontrado: " + val.getClass(), e);
        }
        return toBsonDocument(docDescriptor, hashmap, tracking);
    }

    public interface DefaultFieldTypes {
        String STRING        = "String";
        String LONG          = "Long";
        String INTEGER       = "Integer";
        String TIMESTAMP     = "Timestamp";
        String DECIMAL       = "Decimal";
        String BOOLEAN       = "Bool";
        String DOCUMENT      = "Document";
        String STRING_ARR    = "String[]";
        String LONG_ARR      = "Long[]";
        String INTEGER_ARR   = "Integer[]";
        String TIMESTAMP_ARR = "Timestamp[]";
        String DECIMAL_ARR   = "Decimal[]";
        String BOOLEAN_ARR   = "Bool[]";
        String DOCUMENT_ARR  = "Document[]";
    }


}
