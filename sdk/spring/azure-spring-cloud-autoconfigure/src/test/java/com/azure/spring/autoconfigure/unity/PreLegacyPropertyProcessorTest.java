// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.autoconfigure.unity;

import com.azure.cosmos.ConnectionMode;
import com.azure.spring.cloud.autoconfigure.context.PreLegacyPropertyEnvironmentPostProcessor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;

import java.util.Properties;

import static com.azure.spring.cloud.autoconfigure.context.PreLegacyPropertyEnvironmentPostProcessor.toLogString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Execution(ExecutionMode.SAME_THREAD)
@ExtendWith(OutputCaptureExtension.class)
public class PreLegacyPropertyProcessorTest {

    private PreLegacyPropertyEnvironmentPostProcessor processor = new PreLegacyPropertyEnvironmentPostProcessor();

    @Test
    public void postProcessorHasConfiguredOrder() {
        assertEquals(processor.getOrder(), PreLegacyPropertyEnvironmentPostProcessor.DEFAULT_ORDER);
    }

    @Test
    public void testMapLegacyToCurrent(CapturedOutput output) {
        Properties properties = new Properties();
        properties.setProperty("azure.storage.account-key", "fakekey");
        properties.setProperty("azure.keyvault.uri", "fakeuri");
        properties.setProperty("spring.cloud.azure.keyvault.uri", "trueuri");
        PropertiesPropertySource propertySource = new PropertiesPropertySource("test", properties);
        ConfigurableEnvironment environment = getEnvironment(propertySource, processor);

        assertTrue(environment.getPropertySources().contains(processor.getClass().getName()));
        assertEquals("fakekey", environment.getProperty("spring.cloud.azure.storage.account-key"));
        assertEquals("trueuri", environment.getProperty("spring.cloud.azure.keyvault.uri"));
        assertTrue(output.getOut().contains(
            toLogString("azure.storage.account-key", "spring.cloud.azure.storage.account-key")));
        assertFalse(output.getOut().contains(
            toLogString("azure.keyvault.uri", "spring.cloud.azure.keyvault.uri")));
    }

    @Test
    public void testRelaxBinding(CapturedOutput output) {
        Properties properties = new Properties();
        properties.setProperty("azure.storage.accountKey", "fakekey");
        properties.put("azure.cosmos.connection-mode", ConnectionMode.DIRECT);
        properties.put("azure.cosmos.allow_telemetry", false);
        properties.put("azure.keyvault.REFRESH_INTERVAL", 1000L);
        PropertiesPropertySource propertySource = new PropertiesPropertySource("test", properties);
        ConfigurableEnvironment environment = getEnvironment(propertySource, processor);

        assertTrue(environment.getPropertySources().contains(processor.getClass().getName()));
        assertEquals("fakekey", environment.getProperty("spring.cloud.azure.storage.account-key"));
        assertEquals(ConnectionMode.DIRECT, environment.getProperty("spring.cloud.azure.cosmos.connection-mode", ConnectionMode.class));
        assertEquals(false, environment.getProperty("spring.cloud.azure.cosmos.allow-telemetry", Boolean.class));
        assertEquals(1000L, environment.getProperty("spring.cloud.azure.keyvault.refresh-interval", Long.class));

        assertTrue(output.getOut().contains(
            toLogString("azure.storage.account-key", "spring.cloud.azure.storage.account-key")));
    }


    @Test
    public void testMultipleKeyVaults(CapturedOutput output) {
        Properties properties = new Properties();
        properties.setProperty("azure.keyvault.order", "one, two");
        properties.setProperty("spring.cloud.azure.keyvault.order", "three, four");
        properties.setProperty("azure.keyvault.one.uri", "uri");
        properties.setProperty("azure.keyvault.two.client-id", "id");
        properties.setProperty("azure.keyvault.three.client-key", "key");
        properties.setProperty("azure.keyvault.four.authority-host", "host");
        properties.setProperty("azure.keyvault.five.enabled", "true");

        PropertiesPropertySource propertySource = new PropertiesPropertySource("test", properties);
        ConfigurableEnvironment environment = getEnvironment(propertySource, processor);

        assertTrue(environment.getPropertySources().contains(processor.getClass().getName()));
        assertNull(environment.getProperty("spring.cloud.azure.keyvault.one.uri"));
        assertNull(environment.getProperty("spring.cloud.azure.keyvault.two.credential.client-id"));
        assertEquals("key", environment.getProperty("spring.cloud.azure.keyvault.three.credential.client-secret"));
        assertEquals("host", environment.getProperty("spring.cloud.azure.keyvault.four.environment.authority-host"));
        assertNull(environment.getProperty("spring.cloud.azure.keyvault.five.enabled"));
        assertFalse(output.getOut().contains(
            toLogString("azure.keyvault.one.uri", "spring.cloud.azure.keyvault.one.uri")));
        assertTrue(output.getOut().contains(
            toLogString("azure.keyvault.three.client-key", "spring.cloud.azure.keyvault.three.credential.client-secret")));
    }

    private ConfigurableEnvironment getEnvironment(PropertiesPropertySource propertiesPropertySource,
                                                   EnvironmentPostProcessor environmentPostProcessor) {
        SpringApplication springApplication = getSpringApplication(environmentPostProcessor.getClass());

        ConfigurableApplicationContext context = springApplication.run();
        ConfigurableEnvironment configurableEnvironment = context.getEnvironment();
        configurableEnvironment.getPropertySources().addFirst(propertiesPropertySource);
        environmentPostProcessor.postProcessEnvironment(configurableEnvironment, springApplication);
        context.close();

        return configurableEnvironment;
    }

    private SpringApplication getSpringApplication(Class<?>... sources) {
        return new SpringApplicationBuilder().sources(sources).web(WebApplicationType.NONE).build();
    }

//    public static String toLogString(String legacyPropertyName, String currentPropertyName) {
//        return String.format("Deprecated property %s detected! Use %s instead!", legacyPropertyName, currentPropertyName);
//    }
}
