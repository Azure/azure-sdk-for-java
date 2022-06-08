// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.models;

import com.azure.core.util.CoreUtils;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * Serializes an {@link IndexAction}.
 */
public final class IndexActionSerializer extends JsonSerializer<IndexAction> {
    @Override
    public void serialize(IndexAction value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value != null) {
            gen.writeStartObject();
            gen.writeStringField("@search.action", value.getActionType().toString());

            String rawDocument = value.getRawDocument();
            if (!CoreUtils.isNullOrEmpty(rawDocument)) {
                gen.writeRaw(',');
                gen.writeRaw(rawDocument);
            }

            gen.writeEndObject();
        }
    }
}
