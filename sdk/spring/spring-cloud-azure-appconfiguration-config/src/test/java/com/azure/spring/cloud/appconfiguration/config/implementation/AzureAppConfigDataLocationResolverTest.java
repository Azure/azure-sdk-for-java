// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.context.config.ConfigDataLocation;
import org.springframework.boot.context.config.ConfigDataLocationNotFoundException;
import org.springframework.boot.context.config.ConfigDataLocationResolverContext;
import org.springframework.boot.context.config.Profiles;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Binder;

import com.azure.spring.cloud.appconfiguration.config.implementation.properties.AppConfigurationProperties;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.ConfigStore;

/**
 * Test class for AzureAppConfigDataLocationResolver.
 */
@ExtendWith(MockitoExtension.class)
class AzureAppConfigDataLocationResolverTest {

    private final AzureAppConfigDataLocationResolver resolver = new AzureAppConfigDataLocationResolver();

    @Mock
    private ConfigDataLocationResolverContext mockContext;

    @Mock
    private Binder mockBinder;

    @Mock
    BindResult<String> emptyResult;

    @Mock
    BindResult<String> validResult;

    private static final String PREFIX = "azureAppConfiguration:";
    private static final String INVALID_PREFIX = "someOtherPrefix:";

    private static class TestableResolver extends AzureAppConfigDataLocationResolver {
        private final AppConfigurationProperties testProperties;

        TestableResolver(AppConfigurationProperties properties) {
            this.testProperties = properties;
        }

        @Override
        protected AppConfigurationProperties loadProperties(ConfigDataLocationResolverContext context) {
            if (testProperties != null) {
                return testProperties;
            }
            return super.loadProperties(context);
        }
    }

    @Test
    void testIsResolvableWithIncorrectPrefix() {
        ConfigDataLocation location = ConfigDataLocation.of(INVALID_PREFIX);

        boolean result = resolver.isResolvable(mockContext, location);

        assertFalse(result, "Resolver should reject locations with incorrect prefix");
    }

    @Test
    void testIsResolvableWithCorrectPrefix() {
        ConfigDataLocation location = ConfigDataLocation.of(PREFIX);

        when(mockContext.getBinder()).thenReturn(mockBinder);
        when(mockBinder.bind("spring.cloud.azure.appconfiguration.stores[0].endpoint", String.class))
            .thenReturn(validResult);
        when(validResult.orElse("")).thenReturn("https://test.config.io");

        boolean result = resolver.isResolvable(mockContext, location);
        
        assertTrue(location.hasPrefix(PREFIX), "Location should have correct prefix");
        assertTrue(result, "Resolver should accept locations with valid endpoint configuration");
    }

    @Test
    void testIsResolvableWithValidConnectionStringConfiguration() {
        ConfigDataLocation location = ConfigDataLocation.of("azureAppConfiguration:");
        
        when(mockContext.getBinder()).thenReturn(mockBinder);
        when(mockBinder.bind("spring.cloud.azure.appconfiguration.stores[0].endpoint", String.class))
            .thenReturn(emptyResult);
        when(emptyResult.orElse("")).thenReturn("");
        
        when(mockBinder.bind("spring.cloud.azure.appconfiguration.stores[0].connection-string", String.class))
            .thenReturn(validResult);
        when(validResult.orElse("")).thenReturn("Endpoint=https://test.config.io;Id=test;Secret=secret");

        boolean result = resolver.isResolvable(mockContext, location);

        assertTrue(result, "Resolver should accept locations with valid connection-string configuration");
    }

    @Test
    void testIsResolvableWithValidEndpointsConfiguration() {
        ConfigDataLocation location = ConfigDataLocation.of("azureAppConfiguration:");
        
        when(mockContext.getBinder()).thenReturn(mockBinder);
        when(mockBinder.bind("spring.cloud.azure.appconfiguration.stores[0].endpoint", String.class))
            .thenReturn(emptyResult);
        when(emptyResult.orElse("")).thenReturn("");
        
        when(mockBinder.bind("spring.cloud.azure.appconfiguration.stores[0].connection-string", String.class))
            .thenReturn(emptyResult);
        
        when(mockBinder.bind("spring.cloud.azure.appconfiguration.stores[0].endpoints", String.class))
            .thenReturn(validResult);
        when(validResult.orElse("")).thenReturn("https://store1.config.io,https://store2.config.io");

        boolean result = resolver.isResolvable(mockContext, location);

        assertTrue(result, "Resolver should accept locations with valid endpoints configuration");
    }

    @Test
    void testIsResolvableWithValidConnectionStringsConfiguration() {
        ConfigDataLocation location = ConfigDataLocation.of("azureAppConfiguration:");
        
        when(mockContext.getBinder()).thenReturn(mockBinder);
        when(mockBinder.bind("spring.cloud.azure.appconfiguration.stores[0].endpoint", String.class))
            .thenReturn(emptyResult);
        when(mockBinder.bind("spring.cloud.azure.appconfiguration.stores[0].connection-string", String.class))
            .thenReturn(emptyResult);
        when(mockBinder.bind("spring.cloud.azure.appconfiguration.stores[0].endpoints", String.class))
            .thenReturn(emptyResult);
        when(emptyResult.orElse("")).thenReturn("");
        
        when(mockBinder.bind("spring.cloud.azure.appconfiguration.stores[0].connection-strings", String.class))
            .thenReturn(validResult);
        when(validResult.orElse("")).thenReturn("Endpoint=https://store1.config.io;Id=test;Secret=secret,Endpoint=https://store2.config.io;Id=test;Secret=secret");

        boolean result = resolver.isResolvable(mockContext, location);

        assertTrue(result, "Resolver should accept locations with valid connection-strings configuration");
    }

    @Test
    void testIsResolvableWithNoValidConfiguration() {
        ConfigDataLocation location = ConfigDataLocation.of("azureAppConfiguration:");
        
        when(mockContext.getBinder()).thenReturn(mockBinder);
        when(mockBinder.bind("spring.cloud.azure.appconfiguration.stores[0].endpoint", String.class))
            .thenReturn(emptyResult);
        when(mockBinder.bind("spring.cloud.azure.appconfiguration.stores[0].connection-string", String.class))
            .thenReturn(emptyResult);
        when(mockBinder.bind("spring.cloud.azure.appconfiguration.stores[0].endpoints", String.class))
            .thenReturn(emptyResult);
        when(mockBinder.bind("spring.cloud.azure.appconfiguration.stores[0].connection-strings", String.class))
            .thenReturn(emptyResult);
        when(emptyResult.orElse("")).thenReturn("");

        boolean result = resolver.isResolvable(mockContext, location);

        assertFalse(result, "Resolver should reject locations with no valid configuration");
    }

    @Test
    void testIsResolvableWithEmptyLocation() {
        ConfigDataLocation location = ConfigDataLocation.of("someother:");

        boolean result = resolver.isResolvable(mockContext, location);

        assertFalse(result, "Resolver should reject locations with different prefix");
    }

    @Test
    void testIsResolvableWithNullBinder() {
        ConfigDataLocation location = ConfigDataLocation.of("azureAppConfiguration:");
        
        when(mockContext.getBinder()).thenReturn(null);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            resolver.isResolvable(mockContext, location);
        }, "Resolver should handle null binder gracefully");
        
        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("Binder must not be null"), "Exception message should indicate null binder");
    }

    @Test
    void testResolveAlwaysReturnsEmptyList() {
        ConfigDataLocation location = ConfigDataLocation.of("azureAppConfiguration:");

        List<AzureAppConfigDataResource> result = resolver.resolve(mockContext, location);

        assertNotNull(result, "Resolve should never return null");
        assertTrue(result.isEmpty(), "Resolve should always return empty list as per design");
    }

    @Test
    void testResolveProfileSpecificWithNoStores() {
        AppConfigurationProperties properties = new AppConfigurationProperties();
        properties.setStores(null);
        
        TestableResolver testResolver = new TestableResolver(properties);
        ConfigDataLocation location = ConfigDataLocation.of("azureAppConfiguration:");
        Profiles mockProfiles = mock(Profiles.class);

        ConfigDataLocationNotFoundException exception = assertThrows(
            ConfigDataLocationNotFoundException.class,
            () -> testResolver.resolveProfileSpecific(mockContext, location, mockProfiles)
        );
        
        assertTrue(exception.getMessage().contains("No Azure App Configuration stores are configured"));
    }

    @Test
    void testResolveProfileSpecificWithValidStores() {
        AppConfigurationProperties properties = new AppConfigurationProperties();
        properties.setEnabled(true);
        
        ConfigStore store1 = new ConfigStore();
        store1.setEndpoint("https://store1.config.io");
        store1.setEnabled(true);
        
        ConfigStore store2 = new ConfigStore();
        store2.setEndpoint("https://store2.config.io");
        store2.setEnabled(true);
        
        properties.setStores(List.of(store1, store2));
        
        TestableResolver testResolver = new TestableResolver(properties);
        ConfigDataLocation location = ConfigDataLocation.of("azureAppConfiguration:");
        Profiles mockProfiles = mock(Profiles.class);

        List<AzureAppConfigDataResource> result = testResolver.resolveProfileSpecific(mockContext, location, mockProfiles);

        assertNotNull(result);
        assertEquals(2, result.size());
        
        assertEquals("https://store1.config.io", result.get(0).getEndpoint());
        assertEquals("https://store2.config.io", result.get(1).getEndpoint());
        assertTrue(result.get(0).isConfigStoreEnabled());
        assertTrue(result.get(1).isConfigStoreEnabled());
        assertEquals(mockProfiles, result.get(0).getProfiles());
        assertEquals(mockProfiles, result.get(1).getProfiles());
    }

    @Test
    void testResolveProfileSpecificWithDisabledAppConfig() {
        AppConfigurationProperties properties = new AppConfigurationProperties();
        properties.setEnabled(false);
        
        ConfigStore store = new ConfigStore();
        store.setEndpoint("https://store.config.io");
        store.setEnabled(true);
        
        properties.setStores(List.of(store));
        
        TestableResolver testResolver = new TestableResolver(properties);
        ConfigDataLocation location = ConfigDataLocation.of("azureAppConfiguration:");
        Profiles mockProfiles = mock(Profiles.class);

        List<AzureAppConfigDataResource> result = testResolver.resolveProfileSpecific(mockContext, location, mockProfiles);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertFalse(result.get(0).isConfigStoreEnabled());
    }

    @Test
    void testResolveProfileSpecificWithDisabledStore() {
        AppConfigurationProperties properties = new AppConfigurationProperties();
        properties.setEnabled(true);
        
        ConfigStore store = new ConfigStore();
        store.setEndpoint("https://store.config.io");
        store.setEnabled(false); // Disable the store
        
        properties.setStores(List.of(store));
        
        TestableResolver testResolver = new TestableResolver(properties);
        ConfigDataLocation location = ConfigDataLocation.of("azureAppConfiguration:");
        Profiles mockProfiles = mock(Profiles.class);

        List<AzureAppConfigDataResource> result = testResolver.resolveProfileSpecific(mockContext, location, mockProfiles);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertFalse(result.get(0).isConfigStoreEnabled());
    }



    @Test
    void testResolveProfileSpecificWithCustomRefreshInterval() {
        AppConfigurationProperties properties = createValidProperties();
        Duration customInterval = Duration.ofMinutes(10);
        properties.setRefreshInterval(customInterval);
        
        TestableResolver testResolver = new TestableResolver(properties);
        ConfigDataLocation location = ConfigDataLocation.of("azureAppConfiguration:");
        Profiles mockProfiles = mock(Profiles.class);

        List<AzureAppConfigDataResource> result = testResolver.resolveProfileSpecific(mockContext, location, mockProfiles);

        assertNotNull(result, "Result should not be null");
        assertEquals(2, result.size(), "Should return resources for both stores");
        assertEquals(customInterval, result.get(0).getRefreshInterval(), "Store 1 should have custom refresh interval");
        assertEquals(customInterval, result.get(1).getRefreshInterval(), "Store 2 should have custom refresh interval");
    }

    @Test
    void testResolveProfileSpecificWithEmptyStoresList() {
        AppConfigurationProperties properties = new AppConfigurationProperties();
        properties.setStores(List.of());
        
        TestableResolver testResolver = new TestableResolver(properties);
        ConfigDataLocation location = ConfigDataLocation.of("azureAppConfiguration:");
        Profiles mockProfiles = mock(Profiles.class);

        ConfigDataLocationNotFoundException exception = assertThrows(
            ConfigDataLocationNotFoundException.class,
            () -> testResolver.resolveProfileSpecific(mockContext, location, mockProfiles)
        );
        
        assertTrue(exception.getMessage().contains("No Azure App Configuration stores are configured"));
    }

    @Test
    void testResolveProfileSpecificWithMixedStoreStates() {
        AppConfigurationProperties properties = new AppConfigurationProperties();
        properties.setEnabled(true);
        
        ConfigStore enabledStore = new ConfigStore();
        enabledStore.setEndpoint("https://enabled.config.io");
        enabledStore.setEnabled(true);
        
        ConfigStore disabledStore = new ConfigStore();
        disabledStore.setEndpoint("https://disabled.config.io");
        disabledStore.setEnabled(false);
        
        properties.setStores(List.of(enabledStore, disabledStore));
        
        TestableResolver testResolver = new TestableResolver(properties);
        ConfigDataLocation location = ConfigDataLocation.of("azureAppConfiguration:");
        Profiles mockProfiles = mock(Profiles.class);

        List<AzureAppConfigDataResource> result = testResolver.resolveProfileSpecific(mockContext, location, mockProfiles);

        assertNotNull(result);
        assertEquals(2, result.size());
        
        assertEquals("https://enabled.config.io", result.get(0).getEndpoint());
        assertTrue(result.get(0).isConfigStoreEnabled());
        
        assertEquals("https://disabled.config.io", result.get(1).getEndpoint());
        assertFalse(result.get(1).isConfigStoreEnabled());
    }

    @Test
    void testResolveProfileSpecificWithSingleStore() {
        AppConfigurationProperties properties = new AppConfigurationProperties();
        properties.setEnabled(true);
        properties.setRefreshInterval(Duration.ofMinutes(5));
        
        ConfigStore store = new ConfigStore();
        store.setEndpoint("https://single.config.io");
        store.setEnabled(true);
        
        properties.setStores(List.of(store));
        
        TestableResolver testResolver = new TestableResolver(properties);
        ConfigDataLocation location = ConfigDataLocation.of("azureAppConfiguration:");
        Profiles mockProfiles = mock(Profiles.class);

        List<AzureAppConfigDataResource> result = testResolver.resolveProfileSpecific(mockContext, location, mockProfiles);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("https://single.config.io", result.get(0).getEndpoint());
        assertTrue(result.get(0).isConfigStoreEnabled());
        assertEquals(Duration.ofMinutes(5), result.get(0).getRefreshInterval());
        assertEquals(mockProfiles, result.get(0).getProfiles());
    }

    @Test
    void testResolveProfileSpecificWithNullRefreshInterval() {
        AppConfigurationProperties properties = new AppConfigurationProperties();
        properties.setEnabled(true);
        properties.setRefreshInterval(null);
        
        ConfigStore store = new ConfigStore();
        store.setEndpoint("https://store.config.io");
        store.setEnabled(true);
        
        properties.setStores(List.of(store));
        
        TestableResolver testResolver = new TestableResolver(properties);
        ConfigDataLocation location = ConfigDataLocation.of("azureAppConfiguration:");
        Profiles mockProfiles = mock(Profiles.class);

        List<AzureAppConfigDataResource> result = testResolver.resolveProfileSpecific(mockContext, location, mockProfiles);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(null, result.get(0).getRefreshInterval());
    }

    @Test
    void testPrefixConstant() {
        assertEquals("azureAppConfiguration", AzureAppConfigDataLocationResolver.PREFIX);
    }

    private AppConfigurationProperties createValidProperties() {
        AppConfigurationProperties properties = new AppConfigurationProperties();
        properties.setEnabled(true);
        properties.setRefreshInterval(Duration.ofMinutes(30));
        
        List<ConfigStore> stores = new ArrayList<>();
        
        ConfigStore store1 = new ConfigStore();
        store1.setEndpoint("https://store1.config.io");
        store1.setEnabled(true);
        stores.add(store1);
        
        ConfigStore store2 = new ConfigStore();
        store2.setEndpoint("https://store2.config.io");
        store2.setEnabled(true);
        stores.add(store2);
        
        properties.setStores(stores);
        return properties;
    }
}
