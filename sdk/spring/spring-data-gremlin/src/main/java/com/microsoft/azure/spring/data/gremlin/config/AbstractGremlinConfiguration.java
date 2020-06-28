// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.data.gremlin.config;

import com.microsoft.azure.spring.data.gremlin.common.GremlinConfig;
import com.microsoft.azure.spring.data.gremlin.common.GremlinFactory;
import com.microsoft.azure.spring.data.gremlin.conversion.MappingGremlinConverter;
import com.microsoft.azure.spring.data.gremlin.query.GremlinTemplate;
import org.springframework.context.annotation.Bean;

public abstract class AbstractGremlinConfiguration extends GremlinConfigurationSupport {

    public abstract GremlinConfig getGremlinConfig();

    @Bean
    public GremlinFactory gremlinFactory() {
        return new GremlinFactory(getGremlinConfig());
    }

    @Bean
    public MappingGremlinConverter mappingGremlinConverter() throws ClassNotFoundException {
        return new MappingGremlinConverter(gremlinMappingContext());
    }

    @Bean
    public GremlinTemplate gremlinTemplate(GremlinFactory factory) throws ClassNotFoundException {
        return new GremlinTemplate(factory, mappingGremlinConverter());
    }
}
