// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonTokenId;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.UntypedObjectDeserializer;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;

class Iso8601DateDeserializer extends UntypedObjectDeserializer {
    private static final long serialVersionUID = 1L;
    private final UntypedObjectDeserializer defaultDeserializer;
    private static final String ISO8601_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    protected Iso8601DateDeserializer(final UntypedObjectDeserializer defaultDeserializer) {
        super(null, null);
        this.defaultDeserializer = defaultDeserializer;
    }

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
            return new SimpleDateFormat(ISO8601_FORMAT).parse((String) obj);
        } catch (ParseException e) {
            return obj;
        }
    }
}
