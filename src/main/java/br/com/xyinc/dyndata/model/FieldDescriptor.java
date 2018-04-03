package br.com.xyinc.dyndata.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.math.BigDecimal;
import java.util.List;

@JsonSerialize
public class FieldDescriptor {
    @JsonProperty
    private String                fieldName;
    @JsonProperty
    private Boolean               nullable;
    @JsonProperty
    private String                fieldType;
    @JsonProperty
    private List<String>          allowedValues;
    @JsonProperty
    private BigDecimal            min;
    @JsonProperty
    private BigDecimal            max;
    @JsonProperty
    private Integer               minLength;
    @JsonProperty
    private Integer               maxLength;
    @JsonProperty
    private String                defaultValue;
    @JsonProperty
    private List<FieldDescriptor> documentFields;

    private FieldDescriptor() {
    }

    public FieldDescriptor(String fieldName, String fieldType, boolean nullable) {
        this();
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

    public boolean getNullable() {
        return nullable == null || nullable;
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

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }
}
