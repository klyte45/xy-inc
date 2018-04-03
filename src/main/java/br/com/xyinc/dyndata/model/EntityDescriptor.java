package br.com.xyinc.dyndata.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static br.com.xyinc.dyndata.service.EntityManagementService.DEFAULT_COLLECTION_PREFIX;

@JsonSerialize
public class EntityDescriptor {
    @JsonProperty
    private String uriName;
    @JsonProperty
    private String entityName;
    @JsonProperty
    private String sequenceField;

    @JsonProperty
    private List<FieldDescriptor> fields = new ArrayList<>();

    @JsonProperty
    private List<String> keys = new ArrayList<>();

    public String getUriName() {
        return uriName;
    }

    public void setUriName(String uriName) {
        this.uriName = uriName;
    }

    public String getCollectionName() {
        return DEFAULT_COLLECTION_PREFIX + uriName;
    }

    public List<FieldDescriptor> getFields() {
        return fields;
    }

    public void setFields(List<FieldDescriptor> fields) {
        this.fields = fields;
    }

    public List<String> getKeys() {
        return keys;
    }

    public void setKeys(List<String> keys) {
        this.keys = keys;
    }

    public String getSequenceField() {
        return sequenceField;
    }

    public FieldDescriptor getFieldByName(String fieldName) {
        Optional<FieldDescriptor> fieldFound = fields.stream().filter(x -> x.getFieldName().equals(fieldName)).findFirst();
        return fieldFound.orElse(null);
    }

    public void setSequenceField(String sequenceField) {
        this.sequenceField = sequenceField;
    }

    public String getEntityName() {
        return entityName;
    }
}
