package com.azure.messaging.eventgrid.events;

import com.azure.core.experimental.serializer.ObjectSerializer;

import java.time.OffsetDateTime;

public class CloudEventBuilder {


    public CloudEventBuilder() {
        // TODO: implement method
    }

    public CloudEvent build() {
        // TODO: implement method
        return null;
    }

    public CloudEventBuilder id(String id) {
        // TODO: implement method
        return this;
    }

    public CloudEventBuilder source(String source) {
        // TODO: implement method
        return this;
    }

    public CloudEventBuilder data(Object data) {
        // TODO: implement method
        return this;
    }

    public CloudEventBuilder data(Object data, String dataContentType) {
        // TODO: implement method
        return this;
    }

    public CloudEventBuilder data(Object data, ObjectSerializer serializer, String dataContentType) {
        // TODO: implement method
        return this;
    }

    public CloudEventBuilder data(byte[] data, String dataContentType) {
        // TODO: implement method
        return this;
    }

    public CloudEventBuilder type(String type) {
        // TODO: implement method
        return this;
    }

    public CloudEventBuilder time(OffsetDateTime time) {
        // TODO: implement method
        return this;
    }

    public CloudEventBuilder subject(String subject) {
        // TODO: implement method
        return this;
    }

    public CloudEventBuilder dataSchema(String dataSchema) {
        // TODO: implement method
        return this;
    }

    public CloudEventBuilder addProperty(String name, Object value) {
        // TODO: implement method
        return this;
    }
}
