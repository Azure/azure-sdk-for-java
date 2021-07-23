// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.keyvault;

import com.azure.spring.autoconfigure.unity.PreLegacyPropertyEnvironmentPostProcessor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static com.azure.spring.keyvault.KeyVaultEnvironmentPostProcessorHelper.AZURE_KEYVAULT_PROPERTYSOURCE_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.core.env.StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME;

public class PostLegacyPropertyProcessorTest {

    private PostLegacyPropertyEnvironmentPostProcessor processor = new PostLegacyPropertyEnvironmentPostProcessor();
    private AutoCloseable closeable;

    @Mock
    private KeyVaultOperation keyVaultOperationOne;

    @Mock
    private KeyVaultOperation keyVaultOperationTwo;

    @BeforeEach
    public void setup() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    public void close() throws Exception {
        closeable.close();
    }

    @Test
    public void postProcessorHasConfiguredOrder() {
        assertEquals(processor.getOrder(), PostLegacyPropertyEnvironmentPostProcessor.DEFAULT_ORDER);
    }

    @Test
    public void testMappingSingleKvSources() {
        when(keyVaultOperationOne.getProperty("azure.cosmos.uri")).thenReturn("one");
        when(keyVaultOperationOne.getProperty("spring.cloud.azure.cosmos.uri")).thenReturn(null);

        List<PropertySource> sourceList = new ArrayList<PropertySource>();
        buildNonKvPropertySource(sourceList);
        buildKvPropertySource(sourceList);

        ConfigurableEnvironment environment = getEnvironment(sourceList, processor);
        MutablePropertySources propertySources = environment.getPropertySources();
        PropertySource<?> kvPropertySource = propertySources.get("first");
        PropertySource<?> postPropertySource = propertySources.get(processor.getClass().getName());

        assertNotNull(kvPropertySource);
        assertNotNull(postPropertySource);
        assertEquals("one", environment.getProperty("spring.cloud.azure.cosmos.uri"));
        assertTrue(propertySources.precedenceOf(kvPropertySource) > propertySources.precedenceOf(postPropertySource));
    }

    @Test
    public void testMappingSingleKvSourcesFail() {
        when(keyVaultOperationOne.getProperty("azure.cosmos.uri")).thenReturn("one");
        when(keyVaultOperationOne.getProperty("spring.cloud.azure.cosmos.uri")).thenReturn("two");

        List<PropertySource> sourceList = new ArrayList<PropertySource>();
        buildNonKvPropertySource(sourceList);
        buildKvPropertySource(sourceList);

        ConfigurableEnvironment environment = getEnvironment(sourceList, processor);
        assertFalse(environment.getPropertySources().contains(processor.getClass().getName()));
        assertNotEquals("one", environment.getProperty("spring.cloud.azure.cosmos.uri"));
    }

    @Test
    public void testNoKvSources() {
        List<PropertySource> sourceList = new ArrayList<PropertySource>();
        buildNonKvPropertySource(sourceList);

        ConfigurableEnvironment environment = getEnvironment(sourceList, processor);
        assertFalse(environment.getPropertySources().contains(processor.getClass().getName()));
    }

    @Test
    public void testMappingMultipleKvSources() {
        when(keyVaultOperationOne.getProperty("azure.cosmos.uri")).thenReturn("one");
        when(keyVaultOperationTwo.getProperty("azure.cosmos.uri")).thenReturn("two");
        when(keyVaultOperationTwo.getProperty("spring.cloud.azure.cosmos.uri")).thenReturn("two");

        List<PropertySource> sourceList = new ArrayList<PropertySource>();
        buildNonKvPropertySource(sourceList);
        buildKvPropertySource(sourceList);

        ConfigurableEnvironment environment = getEnvironment(sourceList, processor);
        assertEquals("one", environment.getProperty("spring.cloud.azure.cosmos.uri"));
        assertEquals("two", environment.getPropertySources().get("second")
                                       .getProperty("spring.cloud.azure.cosmos.uri"));
    }

    @Test
    public void testMappingMultipleKvSourcesFail() {
        when(keyVaultOperationOne.getProperty("azure.cosmos.uri")).thenReturn("one");
        when(keyVaultOperationOne.getProperty("spring.cloud.azure.cosmos.uri")).thenReturn("three");
        when(keyVaultOperationTwo.getProperty("azure.cosmos.uri")).thenReturn("two");
        when(keyVaultOperationTwo.getProperty("spring.cloud.azure.cosmos.uri")).thenReturn("four");

        List<PropertySource> sourceList = new ArrayList<PropertySource>();
        buildNonKvPropertySource(sourceList);
        buildKvPropertySource(sourceList);

        ConfigurableEnvironment environment = getEnvironment(sourceList, processor);
        assertFalse(environment.getPropertySources().contains(processor.getClass().getName()));
        assertEquals("three", environment.getProperty("spring.cloud.azure.cosmos.uri"));
    }

    private List<PropertySource> buildNonKvPropertySource(List<PropertySource> sourceList) {
        Properties test = new Properties();
        test.setProperty("spring.cloud.azure.keyvault.order", "first, second");
        test.setProperty("azure.cosmos.key", "fake");
        sourceList.add(new PropertiesPropertySource("test", test));
        return sourceList;
    }

    private List<PropertySource> buildKvPropertySource(List<PropertySource> sourceList) {
        KeyVaultPropertySource kvSourceOne = new KeyVaultPropertySource("first", keyVaultOperationOne);

        KeyVaultPropertySource kvSourceTwo = new KeyVaultPropertySource("second", keyVaultOperationTwo);
        sourceList.add(kvSourceTwo);
        sourceList.add(kvSourceOne);

        return sourceList;
    }

    private ConfigurableEnvironment getEnvironment(List<PropertySource> sourceList,
                                                   EnvironmentPostProcessor environmentPostProcessor) {
        SpringApplication springApplication = getSpringApplication(environmentPostProcessor.getClass());
        ConfigurableApplicationContext context = springApplication.run();
        ConfigurableEnvironment configurableEnvironment = context.getEnvironment();
        sourceList.stream().forEach(source ->
            configurableEnvironment.getPropertySources().addAfter(SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME, source));
        environmentPostProcessor.postProcessEnvironment(configurableEnvironment, springApplication);
        context.close();

        return configurableEnvironment;
    }

    protected SpringApplication getSpringApplication(Class<?>... sources) {
        return new SpringApplicationBuilder().sources(sources).web(WebApplicationType.NONE).build();
    }

    @Test
    public void testBothProcessorsConfigured() {
        when(keyVaultOperationOne.getProperty("azure.storage.account-key")).thenReturn("LegacyKey");
        when(keyVaultOperationOne.getProperty("azure.cosmos.uri")).thenReturn("LegacyUri");
        when(keyVaultOperationOne.getProperty("spring.cloud.azure.storage.account-key")).thenReturn("CurrentKey");
        when(keyVaultOperationOne.getProperty("spring.cloud.azure.keyvault.credential.client-id")).thenReturn("CurrentId");

        Properties properties = new Properties();
        properties.setProperty("azure.storage.account-key", "fakekey");
        properties.setProperty("azure.cosmos.uri", "fakeuri");
        properties.setProperty("azure.keyvault.client-id", "fakeid");
        properties.setProperty("azure.keyvault.client-key", "fakesecret");
        PropertiesPropertySource propertySource = new PropertiesPropertySource("test", properties);
        ConfigurableEnvironment environment = getEnvironment(propertySource,
            new PreLegacyPropertyEnvironmentPostProcessor(), processor);

        MutablePropertySources propertySources = environment.getPropertySources();
        PropertySource<?> prePropertySource =
            propertySources.get(PreLegacyPropertyEnvironmentPostProcessor.class.getName());
        PropertySource<?> postPropertySource = propertySources.get(processor.getClass().getName());

        assertNotNull(prePropertySource);
        assertNotNull(postPropertySource);
        assertTrue(propertySources.precedenceOf(prePropertySource) > propertySources.precedenceOf(postPropertySource));

        assertEquals("fakekey", prePropertySource.getProperty("spring.cloud.azure.storage.account-key"));
        assertEquals("fakeuri", prePropertySource.getProperty("spring.cloud.azure.cosmos.uri"));
        assertEquals("fakeid", prePropertySource.getProperty("spring.cloud.azure.keyvault.credential.client-id"));
        assertEquals("fakesecret", prePropertySource.getProperty("spring.cloud.azure.keyvault.credential.client-secret"));

        assertNull(postPropertySource.getProperty("spring.cloud.azure.storage.account-key"));
        assertEquals("LegacyUri", postPropertySource.getProperty("spring.cloud.azure.cosmos.uri"));
        assertNull(postPropertySource.getProperty("spring.cloud.azure.keyvault.credential.client-id"));
        assertNull(postPropertySource.getProperty("spring.cloud.azure.keyvault.credential.client-secret"));

        assertEquals("CurrentKey", environment.getProperty("spring.cloud.azure.storage.account-key"));
        assertEquals("LegacyUri", environment.getProperty("spring.cloud.azure.cosmos.uri"));
        assertEquals("CurrentId", environment.getProperty("spring.cloud.azure.keyvault.credential.client-id"));
        assertEquals("fakesecret", environment.getProperty("spring.cloud.azure.keyvault.credential.client-secret"));
    }

    private ConfigurableEnvironment getEnvironment(PropertiesPropertySource propertiesPropertySource,
                                                   PreLegacyPropertyEnvironmentPostProcessor preProcessor,
                                                   PostLegacyPropertyEnvironmentPostProcessor postProcessor) {
        SpringApplication springApplication = getSpringApplication(postProcessor.getClass(), preProcessor.getClass());

        ConfigurableApplicationContext context = springApplication.run();
        ConfigurableEnvironment configurableEnvironment = context.getEnvironment();
        configurableEnvironment.getPropertySources().addLast(propertiesPropertySource);
        preProcessor.postProcessEnvironment(configurableEnvironment, springApplication);

        configurableEnvironment.getPropertySources().addAfter(SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME,
            new KeyVaultPropertySource(AZURE_KEYVAULT_PROPERTYSOURCE_NAME, keyVaultOperationOne));
        postProcessor.postProcessEnvironment(configurableEnvironment, springApplication);
        context.close();

        return configurableEnvironment;
    }
}
