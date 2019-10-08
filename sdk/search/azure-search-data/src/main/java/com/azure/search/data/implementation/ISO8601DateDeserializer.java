// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.data.implementation;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonTokenId;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.UntypedObjectDeserializer;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Custom deserializer to deserialize strings as instances of {@link OffsetDateTime}
 */
final class ISO8601DateDeserializer extends UntypedObjectDeserializer {

    private final UntypedObjectDeserializer defaultDeserializer;

    /**
     * Constructor
     * @param defaultDeserializer the deserializer to use when an OffsetDateTime match is not found
     */
    ISO8601DateDeserializer(UntypedObjectDeserializer defaultDeserializer) {
        super(null, null);
        this.defaultDeserializer = defaultDeserializer;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        if (jp.getCurrentTokenId() == JsonTokenId.ID_STRING) {
            String value = jp.getText();
            return parseOffsetDateTime(value);
        } else if (jp.getCurrentTokenId() == JsonTokenId.ID_START_ARRAY) {
            List<?> list = (List) defaultDeserializer.deserialize(jp, ctxt);
            return list.stream()
                .map(this::parseOffsetDateTime)
                .collect(Collectors.toList());
        } else {
            return defaultDeserializer.deserialize(jp, ctxt);
        }
    }

    /**
     * Converts an object to an OffsetDateTime if it matches the ISO8601 format.
     * @param obj the object to parse
     * @return an instance of {@link OffsetDateTime} if valid ISO8601, otherwise obj.
     */
    private Object parseOffsetDateTime(Object obj) {
        if (obj != null && obj.getClass() == String.class) {
            try {
                return OffsetDateTime.parse((String) obj, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            } catch (Exception e) {
                return obj;
            }
        } else {
            return obj;
        }
    }
}
