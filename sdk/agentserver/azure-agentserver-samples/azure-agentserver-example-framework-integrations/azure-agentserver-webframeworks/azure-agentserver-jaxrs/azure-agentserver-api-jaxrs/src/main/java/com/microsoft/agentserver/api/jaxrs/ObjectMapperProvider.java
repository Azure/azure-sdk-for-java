// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.agentserver.api.jaxrs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.agentserver.api.serialization.ObjectMapperFactory;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Provider;

/**
 * JAX-RS {@link ContextResolver} that provides the pre-configured {@link ObjectMapper}
 * from {@link ObjectMapperFactory} to the JAX-RS runtime.
 */
@Provider
public class ObjectMapperProvider implements ContextResolver<ObjectMapper> {

    @Override
    public ObjectMapper getContext(Class<?> type) {
        return ObjectMapperFactory.getObjectMapper();
    }
}

