// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.function.Function;

public interface ItemDeserializer {
    <T> T convert(Class<T> classType, JsonNode objectNode);

    class JsonDeserializer implements ItemDeserializer {
        private final Function<JsonNode, ?> factoryMethod;
        public JsonDeserializer(Function<JsonNode, ?> factoryMethod) {
            this.factoryMethod = factoryMethod;
        }

        public JsonDeserializer() {
            this.factoryMethod = null;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> T convert(Class<T> classType, JsonNode jsonNode) {
            if (jsonNode == null) {
                return null;
            }

            if (this.factoryMethod != null) {
                return (T) this.factoryMethod.apply(jsonNode);
            }

            try {
                return Utils.getSimpleObjectMapper().treeToValue(jsonNode, classType);
            } catch (IOException e) {
                throw new IllegalStateException(String.format("Unable to parse JSON %s", jsonNode), e);
            }
        }
    }
}
