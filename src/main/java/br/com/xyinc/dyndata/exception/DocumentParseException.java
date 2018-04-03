package br.com.xyinc.dyndata.exception;

public class DocumentParseException extends IllegalArgumentException {
    public DocumentParseException(String message, Exception parent) {
        super(message, parent);
    }
}
