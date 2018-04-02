package br.com.xyinc.dyndata.model;

public class FieldType {

    private final Class<?> targetClass;
    private final boolean  isArray;

    public FieldType(Class<?> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("Classe da implementação não pode ser nula!");
        }
        targetClass = clazz;
        isArray = clazz.isArray();
    }

    public boolean isNumeric() {
        return !isArray && Number.class.isAssignableFrom(targetClass);
    }

    public boolean isString() {
        return !isArray && String.class.isAssignableFrom(targetClass);
    }

    public boolean isArray() {
        return isArray;
    }

    public Class<?> getTargetClass() {
        return targetClass;
    }
}
