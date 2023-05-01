package com.azure.data.schemaregistry.jsonschemavalidator.models;

import com.azure.core.models.MessageContent;

public class SerializationResult<T extends MessageContent> {
    private final T value;
    private final Iterable<ValidationError> errors;

    public SerializationResult(T value, Iterable<ValidationError> errors) {
        this.value = value;
        this.errors = errors;
    }

    public Iterable<ValidationError> getValidationErrors() {
        return errors;
    }

    public T getValue() {
        return value;
    }
}
