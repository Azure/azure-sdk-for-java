// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.implementation.jackson;

import com.typespec.core.implementation.util.BinaryDataContent;
import com.typespec.core.implementation.util.BinaryDataHelper;
import com.typespec.core.util.BinaryData;
import com.typespec.core.util.logging.ClientLogger;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.IOException;

/**
 * Custom serializer for serializing {@link BinaryData}.
 */
final class BinaryDataSerializer extends JsonSerializer<BinaryData> {
    private static final ClientLogger LOGGER = new ClientLogger(BinaryDataSerializer.class);

    /**
     * Gets a module wrapping this serializer as an adapter for the Jackson ObjectMapper.
     *
     * @return a simple module to be plugged onto Jackson ObjectMapper.
     */
    public static SimpleModule getModule() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(BinaryData.class, new BinaryDataSerializer());
        return module;
    }

    @Override
    public void serialize(BinaryData value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null) {
            return;
        }

        BinaryDataContent content = BinaryDataHelper.getContent(value);
        switch (content.getContentType()) {
            case BINARY:
                gen.writeBinary(content.toBytes());
                break;

            case OBJECT:
                gen.writeRawValue(content.toString());
                break;

            case TEXT:
                gen.writeString(content.toString());
                break;

            default:
                throw LOGGER.logExceptionAsError(new IllegalStateException(
                    "Unsupported BinaryData content type: " + content.getClass().getName()));
        }
    }
}
