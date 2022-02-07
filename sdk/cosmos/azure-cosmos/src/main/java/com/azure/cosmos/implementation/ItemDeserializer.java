// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;


public interface ItemDeserializer {
    <T> T parseFrom(Class<T> classType, byte[] bytes);
    <T> T convert(Class<T> classType, JsonNode objectNode);


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
        public <T> T convert(Class<T> classType, JsonNode jsonNode) {
            if (classType == ObjectNode.class) {
                return (T) jsonNode;
            }

            return Utils.getSimpleObjectMapper().convertValue(jsonNode, classType);
        }
    }
}
