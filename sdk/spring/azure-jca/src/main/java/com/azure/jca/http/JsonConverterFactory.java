// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.jca.http;

/**
 * The JsonConverterFactory.
 */
public final class JsonConverterFactory {

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
    public static JsonConverter createJsonConverter() {
        return new JacksonJsonConverter();
    }
}
