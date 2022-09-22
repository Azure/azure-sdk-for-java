// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config.implementation;

import static com.azure.spring.cloud.config.TestConstants.TEST_CONN_STRING;
import static com.azure.spring.cloud.config.TestConstants.TEST_CONN_STRING_2;
import static com.azure.spring.cloud.config.TestConstants.TEST_STORE_NAME;
import static com.azure.spring.cloud.config.TestConstants.TEST_STORE_NAME_1;
import static com.azure.spring.cloud.config.TestConstants.TEST_STORE_NAME_2;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedResponse;
import com.azure.data.appconfiguration.ConfigurationAsyncClient;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.spring.cloud.config.KeyVaultCredentialProvider;
import com.azure.spring.cloud.config.properties.AppConfigurationProperties;
import com.azure.spring.cloud.config.properties.AppConfigurationProviderProperties;
import com.azure.spring.cloud.config.properties.AppConfigurationStoreMonitoring;
import com.azure.spring.cloud.config.properties.AppConfigurationStoreSelects;
import com.azure.spring.cloud.config.properties.AppConfigurationStoreTrigger;
import com.azure.spring.cloud.config.properties.ConfigStore;
import com.azure.spring.cloud.config.properties.FeatureFlagStore;

import reactor.core.publisher.Flux;

@TestMethodOrder(MethodOrderer.MethodName.class)
public class AppConfigurationPropertySourceLocatorTest {

    private static final String PROFILE_NAME_1 = "dev";

    private static final String PROFILE_NAME_2 = "prod";

    private static final String KEY_FILTER = "/foo/";

    @Mock
    private ConfigurableEnvironment emptyEnvironment;

    @Mock
    private ConfigurableEnvironment devEnvironment;

    @Mock
    private ConfigurableEnvironment multiEnvironment;

    @Mock
    private AppConfigurationReplicaClientFactory clientFactoryMock;

    @Mock
    private AppConfigurationReplicaClient replicaClientMock;

    @Mock
    private FeatureFlagStore featureFlagStoreMock;

    @Mock
    private ConfigurationAsyncClient configClientMock;

    @Mock
    private PagedFlux<ConfigurationSetting> settingsMock;

    @Mock
    private Iterable<PagedResponse<ConfigurationSetting>> iterableMock;

    @Mock
    private Iterator<PagedResponse<ConfigurationSetting>> iteratorMock;

    @Mock
    private Flux<PagedResponse<ConfigurationSetting>> pageMock;

    @Mock
    private PagedResponse<ConfigurationSetting> pagedMock;

    @Mock
    private ConfigStore configStoreMock;

    @Mock
    private ConfigStore configStoreMockError;

    @Mock
    private AppConfigurationProviderProperties appPropertiesMock;

    private AppConfigurationProperties properties;

    @Mock
    private List<ConfigurationSetting> configurationListMock;

    private AppConfigurationPropertySourceLocator locator;

    private AppConfigurationProviderProperties appProperties;

    private KeyVaultCredentialProvider tokenCredentialProvider = null;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        when(emptyEnvironment.getActiveProfiles()).thenReturn(new String[] {});
        when(devEnvironment.getActiveProfiles()).thenReturn(new String[] { PROFILE_NAME_1 });
        when(multiEnvironment.getActiveProfiles()).thenReturn(new String[] { PROFILE_NAME_1, PROFILE_NAME_2 });
        MutablePropertySources sources = new MutablePropertySources();

        sources.addFirst(new PropertySource<String>("refreshArgs") {

            @Override
            public Object getProperty(String name) {
                return null;
            }
        });

        when(emptyEnvironment.getPropertySources()).thenReturn(sources);
        when(devEnvironment.getPropertySources()).thenReturn(sources);
        when(multiEnvironment.getPropertySources()).thenReturn(sources);

        properties = new AppConfigurationProperties();
        properties.setEnabled(true);
        properties.setRefreshInterval(null);

        when(configStoreMock.getConnectionString()).thenReturn(TEST_CONN_STRING);
        when(configStoreMock.getEndpoint()).thenReturn(TEST_STORE_NAME);
        when(configStoreMock.isEnabled()).thenReturn(true);

        List<ConfigStore> stores = new ArrayList<>();
        stores.add(configStoreMock);

        properties.setStores(stores);

        AppConfigurationStoreMonitoring monitoring = new AppConfigurationStoreMonitoring();
        monitoring.setEnabled(false);
        AppConfigurationStoreTrigger trigger = new AppConfigurationStoreTrigger();
        trigger.setKey("sentinal");
        trigger.setKey("test");
        ArrayList<AppConfigurationStoreTrigger> triggers = new ArrayList<>();
        triggers.add(trigger);
        monitoring.setTriggers(triggers);
        when(configStoreMock.getMonitoring()).thenReturn(monitoring);

        when(configClientMock.listConfigurationSettings(Mockito.any())).thenReturn(settingsMock);
        when(settingsMock.byPage()).thenReturn(pageMock);
        when(iterableMock.iterator()).thenReturn(iteratorMock);
        when(iteratorMock.hasNext()).thenReturn(true).thenReturn(false);
        when(iteratorMock.next()).thenReturn(pagedMock);
        when(pagedMock.getItems()).thenReturn(new ArrayList<ConfigurationSetting>());

        when(configurationListMock.iterator()).thenReturn(Collections.emptyIterator());

        when(clientFactoryMock.getAvailableClients(Mockito.anyString(), Mockito.eq(true)))
            .thenReturn(Arrays.asList(replicaClientMock));
        when(replicaClientMock.listConfigurationSettings(Mockito.any())).thenReturn(configurationListMock)
            .thenReturn(configurationListMock).thenReturn(configurationListMock);
        when(replicaClientMock.getEndpoint()).thenReturn(TEST_STORE_NAME);
        

        when(appPropertiesMock.getDefaultMinBackoff()).thenReturn((long) 30);
        when(appPropertiesMock.getDefaultMaxBackoff()).thenReturn((long) 600);

        appProperties = new AppConfigurationProviderProperties();
        appProperties.setVersion("1.0");
        appProperties.setMaxRetries(12);
        appProperties.setMaxRetryTime(0);
        appProperties.setDefaultMaxBackoff((long) 600);
        appProperties.setDefaultMinBackoff((long) 30);

        AppConfigurationStoreSelects selectedKeys = new AppConfigurationStoreSelects().setKeyFilter(KEY_FILTER);
        List<AppConfigurationStoreSelects> selects = new ArrayList<>();
        selects.add(selectedKeys);
        when(configStoreMock.getSelects()).thenReturn(selects);
    }

    @AfterEach
    public void cleanup() throws Exception {
        MockitoAnnotations.openMocks(this).close();
        AppConfigurationPropertySourceLocator.STARTUP.set(true);
    }

    @Test
    public void compositeSourceIsCreated() {
        when(configStoreMock.getFeatureFlags()).thenReturn(featureFlagStoreMock);

        locator = new AppConfigurationPropertySourceLocator(properties, appProperties, clientFactoryMock,
            tokenCredentialProvider, null, null);

        try (MockedStatic<StateHolder> stateHolderMock = Mockito.mockStatic(StateHolder.class)) {
            stateHolderMock.when(() -> StateHolder.updateState(Mockito.any())).thenReturn(null);
            PropertySource<?> source = locator.locate(emptyEnvironment);

            assertTrue(source instanceof CompositePropertySource);

            Collection<PropertySource<?>> sources = ((CompositePropertySource) source).getPropertySources();

            String[] expectedSourceNames = new String[] {
                KEY_FILTER + "store1/\0"
            };
            assertEquals(expectedSourceNames.length, sources.size());
            assertArrayEquals((Object[]) expectedSourceNames, sources.stream().map(s -> s.getName()).toArray());
        }
    }

    @Test
    public void devSourceIsCreated() {
        when(configStoreMock.getFeatureFlags()).thenReturn(featureFlagStoreMock);

        locator = new AppConfigurationPropertySourceLocator(properties, appProperties, clientFactoryMock,
            tokenCredentialProvider, null, null);

        try (MockedStatic<StateHolder> stateHolderMock = Mockito.mockStatic(StateHolder.class)) {
            stateHolderMock.when(() -> StateHolder.updateState(Mockito.any())).thenReturn(null);
            PropertySource<?> source = locator.locate(devEnvironment);
            assertTrue(source instanceof CompositePropertySource);

            Collection<PropertySource<?>> sources = ((CompositePropertySource) source).getPropertySources();

            String[] expectedSourceNames = new String[] {
                KEY_FILTER + "store1/dev"
            };
            assertEquals(expectedSourceNames.length, sources.size());
            assertArrayEquals((Object[]) expectedSourceNames, sources.stream().map(s -> s.getName()).toArray());
        }
    }

    @Test
    public void multiSourceIsCreated() {
        when(configStoreMock.getFeatureFlags()).thenReturn(featureFlagStoreMock);

        locator = new AppConfigurationPropertySourceLocator(properties, appProperties, clientFactoryMock,
            tokenCredentialProvider, null, null);

        try (MockedStatic<StateHolder> stateHolderMock = Mockito.mockStatic(StateHolder.class)) {
            stateHolderMock.when(() -> StateHolder.updateState(Mockito.any())).thenReturn(null);
            PropertySource<?> source = locator.locate(multiEnvironment);
            assertTrue(source instanceof CompositePropertySource);

            Collection<PropertySource<?>> sources = ((CompositePropertySource) source).getPropertySources();

            String[] expectedSourceNames = new String[] {
                KEY_FILTER + "store1/prod,dev"
            };
            assertEquals(expectedSourceNames.length, sources.size());
            assertArrayEquals((Object[]) expectedSourceNames, sources.stream().map(s -> s.getName()).toArray());
        }
    }

    @Test
    public void storeCreatedWithFeatureFlags() {
        FeatureFlagStore featureFlagStore = new FeatureFlagStore();
        featureFlagStore.setEnabled(true);

        when(configStoreMock.getFeatureFlags()).thenReturn(featureFlagStore);

        locator = new AppConfigurationPropertySourceLocator(properties, appProperties, clientFactoryMock,
            tokenCredentialProvider, null, null);

        try (MockedStatic<StateHolder> stateHolderMock = Mockito.mockStatic(StateHolder.class)) {
            stateHolderMock.when(() -> StateHolder.updateState(Mockito.any())).thenReturn(null);
            PropertySource<?> source = locator.locate(emptyEnvironment);
            assertTrue(source instanceof CompositePropertySource);

            Collection<PropertySource<?>> sources = ((CompositePropertySource) source).getPropertySources();
            // Application name: foo and active profile: dev,prod, should construct below
            // composite Property Source:
            // [/foo_prod/, /foo_dev/, /foo/, /application_prod/, /application_dev/,
            // /application/]
            String[] expectedSourceNames = new String[] {
                KEY_FILTER + "store1/\0"
            };
            assertEquals(expectedSourceNames.length, sources.size());
            assertArrayEquals((Object[]) expectedSourceNames, sources.stream().map(s -> s.getName()).toArray());
        }
    }

    @Test
    public void watchedKeyCheck() {
        when(configStoreMock.getFeatureFlags()).thenReturn(featureFlagStoreMock);

        locator = new AppConfigurationPropertySourceLocator(properties, appProperties, clientFactoryMock,
            tokenCredentialProvider, null, null);

        try (MockedStatic<StateHolder> stateHolderMock = Mockito.mockStatic(StateHolder.class)) {
            stateHolderMock.when(() -> StateHolder.updateState(Mockito.any())).thenReturn(null);
            PropertySource<?> source = locator.locate(emptyEnvironment);
            assertTrue(source instanceof CompositePropertySource);

            Collection<PropertySource<?>> sources = ((CompositePropertySource) source).getPropertySources();
            // Application name: foo and active profile: dev,prod, should construct below
            // composite Property Source:
            // [/foo_prod/, /foo_dev/, /foo/, /application_prod/, /application_dev/,
            // /application/]
            String[] expectedSourceNames = new String[] {
                KEY_FILTER + "store1/\0"
            };
            assertEquals(expectedSourceNames.length, sources.size());
            assertArrayEquals((Object[]) expectedSourceNames, sources.stream().map(s -> s.getName()).toArray());
        }
    }

    @Test
    public void defaultFailFastThrowException() {
        when(configStoreMock.getFeatureFlags()).thenReturn(featureFlagStoreMock);
        when(configStoreMock.isFailFast()).thenReturn(true);

        locator = new AppConfigurationPropertySourceLocator(properties, appProperties,
            clientFactoryMock, tokenCredentialProvider, null, null);

        when(clientFactoryMock.getAvailableClients(Mockito.anyString())).thenReturn(Arrays.asList(replicaClientMock));
        when(replicaClientMock.getWatchKey(Mockito.any(), Mockito.anyString())).thenThrow(new RuntimeException());
        RuntimeException e = assertThrows(RuntimeException.class, () -> locator.locate(emptyEnvironment));
        assertEquals("Failed to generate property sources for " + TEST_STORE_NAME, e.getMessage());
        verify(configStoreMock, times(1)).isFailFast();
    }

    @Test
    public void refreshThrowException() throws IllegalArgumentException {
        when(configStoreMock.getFeatureFlags()).thenReturn(featureFlagStoreMock);
        AppConfigurationPropertySourceLocator.STARTUP.set(false);

        locator = new AppConfigurationPropertySourceLocator(properties, appProperties,
            clientFactoryMock, tokenCredentialProvider, null, null);

        when(clientFactoryMock.getAvailableClients(Mockito.anyString())).thenReturn(Arrays.asList(replicaClientMock));
        when(replicaClientMock.getWatchKey(Mockito.any(), Mockito.anyString())).thenThrow(new RuntimeException());
        when(replicaClientMock.listConfigurationSettings(any())).thenThrow(new RuntimeException());

        try (MockedStatic<StateHolder> stateHolderMock = Mockito.mockStatic(StateHolder.class)) {
            stateHolderMock.when(() -> StateHolder.getLoadState(Mockito.anyString())).thenReturn(true);
            RuntimeException e = assertThrows(RuntimeException.class, () -> locator.locate(emptyEnvironment));
            assertEquals("Failed to generate property sources for store1", e.getMessage());
        }
    }

    @Test
    public void notFailFastShouldPass() {
        when(configStoreMock.isFailFast()).thenReturn(false);
        locator = new AppConfigurationPropertySourceLocator(properties, appProperties,
            clientFactoryMock, tokenCredentialProvider, null, null);

        when(configStoreMock.isFailFast()).thenReturn(false);
        when(configStoreMock.getEndpoint()).thenReturn(TEST_STORE_NAME);

        try (MockedStatic<StateHolder> stateHolderMock = Mockito.mockStatic(StateHolder.class)) {
            stateHolderMock.when(() -> StateHolder.updateState(Mockito.any())).thenReturn(null);
            PropertySource<?> source = locator.locate(emptyEnvironment);
            assertTrue(source instanceof CompositePropertySource);

            // Once a store fails it should stop attempting to load
            verify(configStoreMock, times(2)).isFailFast();
        }
    }

    @Test
    public void multiplePropertySourcesExistForMultiStores() {
        when(configStoreMock.getEndpoint()).thenReturn(TEST_STORE_NAME);

        properties = new AppConfigurationProperties();
        TestUtils.addStore(properties, TEST_STORE_NAME_1, TEST_CONN_STRING, KEY_FILTER);
        TestUtils.addStore(properties, TEST_STORE_NAME_2, TEST_CONN_STRING_2, KEY_FILTER);

        locator = new AppConfigurationPropertySourceLocator(properties, appProperties,
            clientFactoryMock, tokenCredentialProvider, null, null);

        try (MockedStatic<StateHolder> stateHolderMock = Mockito.mockStatic(StateHolder.class)) {
            stateHolderMock.when(() -> StateHolder.updateState(Mockito.any())).thenReturn(null);
            PropertySource<?> source = locator.locate(emptyEnvironment);
            assertTrue(source instanceof CompositePropertySource);

            Collection<PropertySource<?>> sources = ((CompositePropertySource) source).getPropertySources();
            String[] expectedSourceNames = new String[] { KEY_FILTER + TEST_STORE_NAME_2 + "/\0",
                KEY_FILTER + TEST_STORE_NAME_1 + "/\0" };
            assertEquals(2, sources.size());
            assertArrayEquals((Object[]) expectedSourceNames, sources.stream().map(s -> s.getName()).toArray());
        }
    }

    @Test
    public void awaitOnError() {
        List<ConfigStore> configStores = new ArrayList<>();
        configStores.add(configStoreMockError);
        AppConfigurationProperties properties = new AppConfigurationProperties();
        properties.setStores(configStores);

        when(appPropertiesMock.getPrekillTime()).thenReturn(5);

        ConfigurableEnvironment env = Mockito.mock(ConfigurableEnvironment.class);
        MutablePropertySources sources = new MutablePropertySources();

        sources.addFirst(new PropertySource<String>("refreshArgs") {

            @Override
            public Object getProperty(String name) {
                return null;
            }
        });

        when(env.getPropertySources()).thenReturn(sources);

        String[] array = {};
        when(env.getActiveProfiles()).thenReturn(array);
        AppConfigurationStoreSelects selectedKeys = new AppConfigurationStoreSelects().setKeyFilter("/application/");
        List<AppConfigurationStoreSelects> selects = new ArrayList<>();
        selects.add(selectedKeys);
        when(configStoreMockError.getSelects()).thenReturn(selects);
        when(configStoreMockError.isEnabled()).thenReturn(true);
        when(configStoreMockError.isFailFast()).thenReturn(true);
        when(configStoreMockError.getEndpoint()).thenReturn("");

        when(configStoreMockError.getFeatureFlags()).thenReturn(featureFlagStoreMock);

        when(clientFactoryMock.getAvailableClients(Mockito.anyString())).thenReturn(Arrays.asList(replicaClientMock));
        when(replicaClientMock.listConfigurationSettings(Mockito.any())).thenThrow(new NullPointerException(""));
        when(appPropertiesMock.getPrekillTime()).thenReturn(-60);
        when(appPropertiesMock.getStartDate()).thenReturn(Instant.now());

        locator = new AppConfigurationPropertySourceLocator(properties, appPropertiesMock, clientFactoryMock,
            tokenCredentialProvider, null, null);

        assertThrows(RuntimeException.class, () -> locator.locate(env));
        verify(appPropertiesMock, times(1)).getPrekillTime();
    }

    @Test
    public void storeDisabled() {
        when(configStoreMock.isEnabled()).thenReturn(false);

        locator = new AppConfigurationPropertySourceLocator(properties, appProperties, clientFactoryMock,
            tokenCredentialProvider, null, null);
        try (MockedStatic<StateHolder> stateHolderMock = Mockito.mockStatic(StateHolder.class)) {
            stateHolderMock.when(() -> StateHolder.updateState(Mockito.any())).thenReturn(null);
            PropertySource<?> source = locator.locate(emptyEnvironment);
            assertTrue(source instanceof CompositePropertySource);

            Collection<PropertySource<?>> sources = ((CompositePropertySource) source).getPropertySources();

            assertEquals(0, sources.size());
        }
    }
}
