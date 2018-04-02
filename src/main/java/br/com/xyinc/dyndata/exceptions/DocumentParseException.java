package br.com.xyinc.dyndata.exceptions;

public class DocumentParseException extends IllegalArgumentException {
    public DocumentParseException(String message, Exception parent) {
        super(message, parent);
    }
}
