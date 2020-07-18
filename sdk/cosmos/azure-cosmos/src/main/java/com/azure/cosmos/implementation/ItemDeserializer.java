// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.implementation.encryption.EncryptionProcessor;
import com.azure.cosmos.implementation.encryption.api.DataEncryptionKeyProvider;
import com.fasterxml.jackson.databind.node.ObjectNode;


public interface ItemDeserializer {
    <T> T parseFrom(Class<T> classType, byte[] bytes);
    <T> T convert(Class<T> classType, ObjectNode objectNode);


    class JsonDeserializer implements ItemDeserializer {
        public <T> T parseFrom(Class<T> classType, byte[] bytes) {
            if (bytes == null) {
                return null;
            }

            // TODO: does this handdle jackson ObjectNode?
            return Utils.parse(bytes, classType);
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> T convert(Class<T> classType, ObjectNode objectNode) {
            if (classType == ObjectNode.class) {
                return (T) objectNode;
            }

            return Utils.getSimpleObjectMapper().convertValue(objectNode, classType);
        }
    }

    class EncryptionDeserializer implements ItemDeserializer {
        private final DataEncryptionKeyProvider dataEncryptionKeyProvider;
        private final JsonDeserializer jsonDeserializer;

        public EncryptionDeserializer(DataEncryptionKeyProvider dataEncryptionKeyProvider, JsonDeserializer jsonDeserializer) {
            this.dataEncryptionKeyProvider = dataEncryptionKeyProvider;
            this.jsonDeserializer = jsonDeserializer;
        }

        @Override
        public <T> T parseFrom(Class<T> classType, byte[] bytes) {
            if (bytes == null) {
                return null;
            }

            ObjectNode objectNode = jsonDeserializer.parseFrom(ObjectNode.class, bytes);
            return convert(classType, objectNode);
        }

        @Override
        public <T> T convert(Class<T> classType, ObjectNode objectNode) {
            if (objectNode == null) {
                return null;
            }

            EncryptionProcessor encryptionProcessor = new EncryptionProcessor();
            ObjectNode objectNodeWithSensitiveDataDecrypted = encryptionProcessor.decryptAsync(objectNode, dataEncryptionKeyProvider);

            return Utils.getSimpleObjectMapper().convertValue(objectNodeWithSensitiveDataDecrypted, classType);
        }
    }
}
