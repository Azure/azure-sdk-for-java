// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.spring.autoconfigure.gremlin;

import com.microsoft.azure.telemetry.TelemetrySender;
import com.microsoft.spring.data.gremlin.common.GremlinConfig;
import com.microsoft.spring.data.gremlin.common.GremlinFactory;
import com.microsoft.spring.data.gremlin.conversion.MappingGremlinConverter;
import com.microsoft.spring.data.gremlin.mapping.GremlinMappingContext;
import com.microsoft.spring.data.gremlin.query.GremlinTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnResource;
import org.springframework.boot.autoconfigure.domain.EntityScanner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.annotation.Persistent;
import org.springframework.lang.NonNull;
import org.springframework.util.ClassUtils;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

import static com.microsoft.azure.telemetry.TelemetryData.SERVICE_NAME;
import static com.microsoft.azure.telemetry.TelemetryData.getClassPackageSimpleName;

/**
 * To create Gremlin factory and template for auto-configure Gremlin properties.
 */
@Configuration
@ConditionalOnClass({GremlinFactory.class, GremlinTemplate.class, MappingGremlinConverter.class})
@ConditionalOnResource(resources = "classpath:gremlin.enable.config")
@ConditionalOnProperty(prefix = "gremlin", value = {"endpoint", "port", "username", "password"})
@EnableConfigurationProperties(GremlinProperties.class)
public class GremlinAutoConfiguration {

    private final GremlinProperties properties;

    private final ApplicationContext applicationContext;

    public GremlinAutoConfiguration(@NonNull GremlinProperties properties, @NonNull ApplicationContext context) {
        this.properties = properties;
        this.applicationContext = context;
    }

    @PostConstruct
    private void sendTelemetry() {
        if (properties.isTelemetryAllowed()) {
            final Map<String, String> events = new HashMap<>();
            final TelemetrySender sender = new TelemetrySender();

            events.put(SERVICE_NAME, getClassPackageSimpleName(GremlinAutoConfiguration.class));

            sender.send(ClassUtils.getUserClass(getClass()).getSimpleName(), events);
        }
    }

    @Bean
    @ConditionalOnMissingBean
    public GremlinConfig getGremlinConfig() {
        return GremlinConfig.builder(properties.getEndpoint(), properties.getUsername(), properties.getPassword())
                .port(properties.getPort())
                .sslEnabled(properties.isSslEnabled())
                .telemetryAllowed(properties.isTelemetryAllowed())
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    public GremlinFactory gremlinFactory() {
        return new GremlinFactory(getGremlinConfig());
    }

    @Bean
    @ConditionalOnMissingBean
    public GremlinTemplate gremlinTemplate(GremlinFactory factory, MappingGremlinConverter converter) {
        return new GremlinTemplate(factory, converter);
    }

    @Bean
    @ConditionalOnMissingBean
    public GremlinMappingContext gremlinMappingContext() {
        try {
            final GremlinMappingContext context = new GremlinMappingContext();

            context.setInitialEntitySet(new EntityScanner(this.applicationContext).scan(Persistent.class));

            return context;
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    @Bean
    @ConditionalOnMissingBean
    public MappingGremlinConverter mappingGremlinConverter(GremlinMappingContext context) {
        return new MappingGremlinConverter(context);
    }
}

