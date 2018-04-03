package br.com.xyinc.dyndata.exception;

public class FieldValidationException extends IllegalArgumentException {
    public FieldValidationException(String message) {
        super(message);
    }
}
