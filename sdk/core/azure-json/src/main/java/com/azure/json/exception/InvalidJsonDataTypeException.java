package com.azure.json.exception;

public class InvalidJsonDataTypeException extends RuntimeException {
    public InvalidJsonDataTypeException() {
        super();
    }

    public InvalidJsonDataTypeException(String message) {
        super(message);
    }

    public InvalidJsonDataTypeException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidJsonDataTypeException(Throwable cause) {
        super(cause);
    }
}
