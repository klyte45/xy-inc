package br.com.xyinc.dyndata.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.math.BigDecimal;
import java.util.List;

@JsonSerialize
public class FieldDescriptor {
    @JsonProperty
    private final String                fieldName;
    @JsonProperty
    private       boolean               nullable;
    @JsonProperty
    private       String                fieldType;
    @JsonProperty
    private       List<String>          allowedValues;
    @JsonProperty
    private       BigDecimal            min;
    @JsonProperty
    private       BigDecimal            max;
    @JsonProperty
    private       Integer               minLength;
    @JsonProperty
    private       Integer               maxLength;
    @JsonProperty
    private       List<FieldDescriptor> documentFields;

    public FieldDescriptor(String fieldName, String fieldType, boolean nullable) {
        this.fieldName = fieldName;
        this.fieldType = fieldType;
        this.nullable = nullable;
    }

    public FieldDescriptor(String fieldName, String fieldType, boolean nullable, List<FieldDescriptor> documentFields) {
        this(fieldName, fieldType, nullable);
        this.documentFields = documentFields;
    }

    public FieldDescriptor(String fieldName, String fieldType, boolean nullable, List<FieldDescriptor> documentFields, List<String> allowedValues) {
        this(fieldName, fieldType, nullable, documentFields);
        this.allowedValues = allowedValues;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getFieldType() {
        return fieldType;
    }

    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }

    public Boolean getNullable() {
        return nullable;
    }

    public void setNullable(Boolean nullable) {
        this.nullable = nullable;
    }

    public BigDecimal getMin() {
        return min;
    }

    public void setMin(BigDecimal min) {
        this.min = min;
    }

    public BigDecimal getMax() {
        return max;
    }

    public void setMax(BigDecimal max) {
        this.max = max;
    }

    public Integer getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(Integer maxLength) {
        this.maxLength = maxLength;
    }

    public Integer getMinLength() {
        return minLength;
    }

    public void setMinLength(Integer minLength) {
        this.minLength = minLength;
    }

    public List<FieldDescriptor> getDocumentFields() {
        return documentFields;
    }

    public void setDocumentFields(List<FieldDescriptor> documentFields) {
        this.documentFields = documentFields;
    }

    public List<String> getAllowedValues() {
        return allowedValues;
    }

    public void setAllowedValues(List<String> allowedValues) {
        this.allowedValues = allowedValues;
    }
}
