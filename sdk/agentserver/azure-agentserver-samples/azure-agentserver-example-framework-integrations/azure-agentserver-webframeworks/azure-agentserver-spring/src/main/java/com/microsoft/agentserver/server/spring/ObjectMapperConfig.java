// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.agentserver.server.spring;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.agentserver.api.serialization.ObjectMapperFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

/**
 * Spring configuration that provides the pre-configured {@link ObjectMapper}
 * from {@link ObjectMapperFactory} to the Spring MVC stack.
 */
@Configuration
public class ObjectMapperConfig {

    /**
     * Provides the shared {@link ObjectMapper} as a Spring bean.
     */
    @Bean
    public ObjectMapper objectMapper() {
        return ObjectMapperFactory.getObjectMapper();
    }

    /**
     * Configures Spring's HTTP message converter to use the shared {@link ObjectMapper}.
     */
    @Bean
    public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter(ObjectMapper objectMapper) {
        return new MappingJackson2HttpMessageConverter(objectMapper);
    }
}

