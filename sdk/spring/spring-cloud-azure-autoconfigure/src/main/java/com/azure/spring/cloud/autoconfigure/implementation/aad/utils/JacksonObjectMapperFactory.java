// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.aad.utils;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * factory class of JacksonObjectMapper
 */
public final class JacksonObjectMapperFactory {

    private JacksonObjectMapperFactory() {
    }

    /**
     * Gets the singleton instance of ObjectMapper.
     *
     * @return the ObjectMapper instance
     */
    public static ObjectMapper getInstance() {
        return SingletonHelper.INSTANCE;
    }

    private static class SingletonHelper {
        private static final ObjectMapper INSTANCE = new ObjectMapper();
    }
}
