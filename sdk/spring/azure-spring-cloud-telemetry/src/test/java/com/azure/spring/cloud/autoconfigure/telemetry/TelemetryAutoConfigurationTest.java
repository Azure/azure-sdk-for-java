// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.telemetry;

import static org.assertj.core.api.Assertions.assertThat;

import com.azure.spring.cloud.telemetry.TelemetryProperties;
import com.azure.spring.cloud.telemetry.TelemetrySender;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;


public class TelemetryAutoConfigurationTest {

    private ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(TelemetryAutoConfiguration.class));

    @Test
    public void testTelemetryPropertiesConfigured() {
        this.contextRunner.withPropertyValues("telemetry.instrumentationKey=012345678901234567890123456789012345")
            .run(context -> {
                assertThat(context).hasSingleBean(TelemetryProperties.class);
                assertThat(context.getBean(TelemetryProperties.class).getInstrumentationKey())
                    .isEqualTo("012345678901234567890123456789012345");
            });
    }

    @Test
    public void testAzurePropertiesTelemetryConfigured() {
        this.contextRunner.withPropertyValues("telemetry.instrumentationKey=012345678901234567890123456789012345")
            .run(context -> assertThat(context.getBean(TelemetrySender.class)).isNotNull());
    }

    @Test
    public void testAzureTelemetryDisabled() {
        this.contextRunner.withPropertyValues("spring.cloud.azure.telemetry.enabled=false")
            .run(context -> assertThat(context).doesNotHaveBean(TelemetrySender.class));
    }

    @Test
    public void testWithoutTelemetryKey() {
        this.contextRunner.run(context -> assertThat(context).doesNotHaveBean(TelemetryProperties.class));
    }
}
