// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.implementation.encryption.EncryptionProcessor;
import com.azure.cosmos.implementation.encryption.api.DataEncryptionKeyProvider;
import com.azure.cosmos.implementation.encryption.api.EncryptionOptions;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.nio.ByteBuffer;

public interface ItemSerializer {
    <T> ByteBuffer serializeTo(T item);

    class JsonSerializer implements ItemSerializer {
        @Override
        public <T> ByteBuffer serializeTo(T item) {
            return Utils.serializeJsonToByteBuffer(Utils.getSimpleObjectMapper(), item);
        }
    }

    class CosmosSerializer implements ItemSerializer {
        private final JsonSerializer jsonSerializer = new JsonSerializer();
        private final DataEncryptionKeyProvider dataEncryptionKeyProvider;
        private final EncryptionOptions encryptionOptions;

        public CosmosSerializer(DataEncryptionKeyProvider dataEncryptionKeyProvider, EncryptionOptions encryptionOptions) {
            this.dataEncryptionKeyProvider = dataEncryptionKeyProvider;
            this.encryptionOptions = encryptionOptions;
        }

        @Override
        public <T> ByteBuffer serializeTo(T item) {
            if (dataEncryptionKeyProvider == null
                || encryptionOptions == null
                || encryptionOptions.getPathsToEncrypt() == null
                || encryptionOptions.getPathsToEncrypt().isEmpty()
                || item instanceof Document
                || item instanceof InternalObjectNode) {
                return jsonSerializer.serializeTo(item);
            } else {
                return new EncryptionSerializer(dataEncryptionKeyProvider, jsonSerializer, encryptionOptions).serializeTo(item);
            }
        }

    }

    class EncryptionSerializer implements ItemSerializer {
        private final EncryptionOptions encryptionOptions;
        private final DataEncryptionKeyProvider dataEncryptionKeyProvider;
        private final JsonSerializer jsonSerializer;

        public EncryptionSerializer(DataEncryptionKeyProvider dataEncryptionKeyProvider, JsonSerializer jsonSerializer, EncryptionOptions encryptionOptions) {
            this.encryptionOptions = encryptionOptions;
            this.dataEncryptionKeyProvider = dataEncryptionKeyProvider;
            this.jsonSerializer = jsonSerializer;
        }

        @Override
        public <T> ByteBuffer serializeTo(T item) {
            ObjectNode objectNode = Utils.getSimpleObjectMapper().convertValue(item, ObjectNode.class);
            EncryptionProcessor encryptionProcessor = new EncryptionProcessor();
            ObjectNode result = encryptionProcessor.encryptAsync(objectNode, encryptionOptions, dataEncryptionKeyProvider);
            return Utils.serializeJsonToByteBuffer(Utils.getSimpleObjectMapper(), result);
        }
    }
}
