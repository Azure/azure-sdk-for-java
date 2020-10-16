// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.autoconfigure.cosmos;


import org.junit.Test;
import org.springframework.boot.context.properties.ConfigurationPropertiesBindException;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.bind.validation.BindValidationException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.ObjectError;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class CosmosPropertiesTest {
    @Test
    public void canSetAllProperties() {
        PropertySettingUtil.setProperties();

        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.register(Config.class);
            context.refresh();
            final CosmosProperties properties = context.getBean(CosmosProperties.class);

            assertThat(properties.getUri()).isEqualTo(PropertySettingUtil.URI);
            assertThat(properties.getKey()).isEqualTo(PropertySettingUtil.KEY);
            assertThat(properties.getConsistencyLevel()).isEqualTo(PropertySettingUtil.CONSISTENCY_LEVEL);
            assertThat(properties.isAllowTelemetry()).isEqualTo(PropertySettingUtil.ALLOW_TELEMETRY_TRUE);
            assertThat(properties.isPopulateQueryMetrics()).isEqualTo(PropertySettingUtil.POPULATE_QUERY_METRICS);
            assertThat(properties.getConnectionMode()).isEqualTo(PropertySettingUtil.CONNECTION_MODE);
        }

        PropertySettingUtil.unsetProperties();
    }

    @Test
    public void canSetAllowTelemetryFalse() {
        PropertySettingUtil.setAllowTelemetryFalse();

        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.register(Config.class);
            context.refresh();
            final CosmosProperties properties = context.getBean(CosmosProperties.class);

            assertThat(properties.isAllowTelemetry()).isEqualTo(PropertySettingUtil.ALLOW_TELEMETRY_FALSE);
        }

        PropertySettingUtil.unsetProperties();
    }

    @Test
    public void emptySettingNotAllowed() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            Exception exception = null;

            context.register(Config.class);

            try {
                context.refresh();
            } catch (Exception e) {
                exception = e;
            }

            assertThat(exception).isNotNull();
            assertThat(exception).isExactlyInstanceOf(ConfigurationPropertiesBindException.class);

            final BindValidationException bindException = (BindValidationException) exception.getCause().getCause();
            final List<ObjectError> errors = bindException.getValidationErrors().getAllErrors();
            final List<String> errorStrings = errors.stream().map(e -> e.toString()).collect(Collectors.toList());

            Collections.sort(errorStrings);

            final List<String> errorStringsExpected = Arrays.asList(
                    "Field error in object 'azure.cosmos' on field 'database': rejected value [null];",
                    "Field error in object 'azure.cosmos' on field 'key': rejected value [null];",
                    "Field error in object 'azure.cosmos' on field 'uri': rejected value [null];"
            );

            assertThat(errorStrings.size()).isEqualTo(errorStringsExpected.size());

            for (int i = 0; i < errorStrings.size(); i++) {
                assertThat(errorStrings.get(i)).contains(errorStringsExpected.get(i));
            }
        }
    }

    @Configuration
    @EnableConfigurationProperties(CosmosProperties.class)
    static class Config {
    }
}

