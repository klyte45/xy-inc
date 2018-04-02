package br.com.xyinc.dyndata.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize
public class ErrorModel {

    @JsonProperty
    private String message;

    public ErrorModel(String message) {
        this.message = message;
    }
}
