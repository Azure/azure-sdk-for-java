// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.autoconfigure.aad;

import com.fasterxml.jackson.databind.ObjectMapper;

public final class JacksonObjectMapperFactory {

    private JacksonObjectMapperFactory() {
    }

    public static ObjectMapper getInstance() {
        return SingletonHelper.INSTANCE;
    }

    private static class SingletonHelper {
        private static final ObjectMapper INSTANCE = new ObjectMapper();
    }
}
