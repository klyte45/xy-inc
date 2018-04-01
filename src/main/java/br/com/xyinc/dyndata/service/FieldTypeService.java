package br.com.xyinc.dyndata.service;

import br.com.xyinc.dyndata.model.FieldType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

@Service
public class FieldTypeService {
    private static final Map<String, FieldType> registeredTypes = new HashMap<>();

    static {
        registeredTypes.put(DefaultFieldTypes.STRING, new FieldType<>(String.class));
        registeredTypes.put(DefaultFieldTypes.LONG, new FieldType<>(Long.class));
        registeredTypes.put(DefaultFieldTypes.INTEGER, new FieldType<>(Integer.class));
        registeredTypes.put(DefaultFieldTypes.TIMESTAMP, new FieldType<>(Timestamp.class));
        registeredTypes.put(DefaultFieldTypes.DECIMAL, new FieldType<>(BigDecimal.class));
        registeredTypes.put(DefaultFieldTypes.BOOLEAN, new FieldType<>(Boolean.class));
    }

    public void registerType(String name, FieldType fieldType) {
        registeredTypes.put(name, fieldType);
    }

    public FieldType getType(String typeName) {
        return registeredTypes.get(typeName);
    }

    public interface DefaultFieldTypes {
        String STRING = "String";
        String LONG = "Long";
        String INTEGER = "Integer";
        String TIMESTAMP = "Timestamp";
        String DECIMAL = "Decimal";
        String BOOLEAN = "Bool";
    }


}
