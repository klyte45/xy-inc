package br.com.xyinc.dyndata.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.ArrayList;
import java.util.List;

@JsonSerialize
public class EntityDescriptor {
    @JsonProperty
    private String collectionName;
    @JsonProperty
    private String uriName;
    @JsonProperty
    private String entityName;

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
        return collectionName;
    }

    public void setCollectionName(String collectionEffectiveName) {
        this.collectionName = collectionEffectiveName;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
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
}
