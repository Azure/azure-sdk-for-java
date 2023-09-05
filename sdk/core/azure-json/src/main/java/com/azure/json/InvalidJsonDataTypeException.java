package com.azure.json;

public class InvalidJsonDataTypeException extends RuntimeException {
    public InvalidJsonDataTypeException() {
        super();
    }
    public InvalidJsonDataTypeException(String message) {
        super(message);
    }
}
