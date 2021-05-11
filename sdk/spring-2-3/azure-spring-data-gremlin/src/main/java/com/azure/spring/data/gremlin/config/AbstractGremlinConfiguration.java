// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.gremlin.config;

import com.azure.spring.data.gremlin.common.GremlinConfig;
import com.azure.spring.data.gremlin.common.GremlinFactory;
import com.azure.spring.data.gremlin.conversion.MappingGremlinConverter;
import com.azure.spring.data.gremlin.query.GremlinTemplate;
import org.springframework.context.annotation.Bean;

public abstract class AbstractGremlinConfiguration extends GremlinConfigurationSupport {

    public abstract GremlinConfig getGremlinConfig();

    @Bean
    public GremlinFactory gremlinFactory() {
        return new GremlinFactory(getGremlinConfig());
    }

    /**
     * Create bean for {@link MappingGremlinConverter}.
     *
     * @return The {@link MappingGremlinConverter} bean.
     * @throws ClassNotFoundException If ClassNotFoundException has occurred.
     */
    @Bean
    public MappingGremlinConverter mappingGremlinConverter() throws ClassNotFoundException {
        return new MappingGremlinConverter(gremlinMappingContext());
    }

    /**
     * Create bean for {@link GremlinTemplate}.
     *
     * @param factory The gremlin factory.
     * @return The {@link GremlinTemplate} bean.
     * @throws ClassNotFoundException If ClassNotFoundException has occurred.
     */
    @Bean
    public GremlinTemplate gremlinTemplate(GremlinFactory factory) throws ClassNotFoundException {
        return new GremlinTemplate(factory, mappingGremlinConverter());
    }
}
