// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.json.jackson.implementation;

import com.azure.core.implementation.util.BinaryDataContent;
import com.azure.core.implementation.util.BinaryDataHelper;
import com.azure.core.implementation.util.ByteArrayContent;
import com.azure.core.implementation.util.FileContent;
import com.azure.core.implementation.util.FluxByteBufferContent;
import com.azure.core.implementation.util.InputStreamContent;
import com.azure.core.implementation.util.SerializableContent;
import com.azure.core.implementation.util.StringContent;
import com.azure.core.util.BinaryData;
import com.azure.core.util.logging.ClientLogger;
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
        if (content instanceof ByteArrayContent
            || content instanceof FileContent
            || content instanceof FluxByteBufferContent
            || content instanceof InputStreamContent) {
            gen.writeBinary(content.toBytes());
        } else if (content instanceof SerializableContent) {
            gen.writeRawValue(content.toString());
        } else if (content instanceof StringContent) {
            gen.writeString(content.toString());
        } else {
            throw LOGGER.logExceptionAsError(
                new IllegalStateException("Unsupported BinaryData content type: " + content.getClass().getName()));
        }
    }
}
