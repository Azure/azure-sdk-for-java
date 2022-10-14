// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.cosmos.repository;

import com.azure.spring.data.cosmos.Constants;
import com.azure.spring.data.cosmos.core.mapping.EnableCosmosAuditing;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.springframework.context.annotation.Bean;

@EnableCosmosAuditing(dateTimeProviderRef = "auditingDateTimeProvider")
public class AuditableConfig {


    @Bean(name = "auditingDateTimeProvider")
    public StubDateTimeProvider stubDateTimeProvider() {
        return new StubDateTimeProvider();
    }

    @Bean
    public StubAuditorProvider auditorProvider() {
        return new StubAuditorProvider();
    }

    @Bean(Constants.OBJECT_MAPPER_BEAN_NAME)
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new ParameterNamesModule())
            .registerModule(new Jdk8Module())
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objectMapper;
    }

}
