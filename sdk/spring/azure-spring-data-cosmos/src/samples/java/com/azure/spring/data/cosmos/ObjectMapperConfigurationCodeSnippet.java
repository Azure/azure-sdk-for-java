// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ObjectMapperConfigurationCodeSnippet {

    // BEGIN: readme-sample-objectMapper
    @Bean(name = "cosmosObjectMapper")
    public ObjectMapper objectMapper() {
        return new ObjectMapper(); // Do configuration to the ObjectMapper if required
    }
    // END: readme-sample-objectMapper

}
