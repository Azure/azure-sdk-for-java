// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.jca;

/**
 * The JsonConverterFactory.
 *
 * @author Manfred Riem (manfred.riem@microsoft.com)
 */
class JsonConverterFactory {

    /**
     * Constructor.
     */
    private JsonConverterFactory() {
    }

    /**
     * Static helper method to create a JsonConverter.
     *
     * @return the JsonConverter.
     */
    static JsonConverter createJsonConverter() {
        return new JacksonJsonConverter();
    }
}
