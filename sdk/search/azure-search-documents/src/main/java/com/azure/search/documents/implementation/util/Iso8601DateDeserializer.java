// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonTokenId;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.UntypedObjectDeserializer;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

final class Iso8601DateDeserializer extends UntypedObjectDeserializer {
    private static final long serialVersionUID = 1L;

    private final UntypedObjectDeserializer defaultDeserializer;

    Iso8601DateDeserializer(final UntypedObjectDeserializer defaultDeserializer) {
        super(null, null);
        this.defaultDeserializer = defaultDeserializer;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Object deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        Object obj = defaultDeserializer.deserialize(jp, ctxt);
        if (jp.currentTokenId() == JsonTokenId.ID_START_OBJECT) {
            return parseDateType(obj);
        } else if (jp.currentTokenId() == JsonTokenId.ID_START_ARRAY) {
            List<?> list = (List) obj;
            return list.stream()
                .map(this::parseDateType)
                .collect(Collectors.toList());
        } else {
            return obj;
        }

    }

    private Object parseDateType(Object obj) {
        try {
            return DateTimeFormatter.ISO_INSTANT.parse((String) obj);
        } catch (DateTimeParseException e) {
            return obj;
        }
    }
}
