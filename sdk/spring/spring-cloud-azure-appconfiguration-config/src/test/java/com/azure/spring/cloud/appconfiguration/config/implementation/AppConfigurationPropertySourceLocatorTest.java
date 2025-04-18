// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation;

import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.EMPTY_LABEL;
import static com.azure.spring.cloud.appconfiguration.config.implementation.TestConstants.TEST_CONN_STRING;
import static com.azure.spring.cloud.appconfiguration.config.implementation.TestConstants.TEST_CONN_STRING_2;
import static com.azure.spring.cloud.appconfiguration.config.implementation.TestConstants.TEST_STORE_NAME;
import static com.azure.spring.cloud.appconfiguration.config.implementation.TestConstants.TEST_STORE_NAME_1;
import static com.azure.spring.cloud.appconfiguration.config.implementation.TestConstants.TEST_STORE_NAME_2;
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
import org.junit.jupiter.api.Disabled;
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
import com.azure.data.appconfiguration.models.FeatureFlagConfigurationSetting;
import com.azure.spring.cloud.appconfiguration.config.implementation.feature.entity.Feature;
import com.azure.spring.cloud.appconfiguration.config.implementation.http.policy.TracingInfo;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.AppConfigurationKeyValueSelector;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.AppConfigurationProperties;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.AppConfigurationProviderProperties;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.AppConfigurationStoreMonitoring;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.AppConfigurationStoreTrigger;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.ConfigStore;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.FeatureFlagStore;

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
    private AppConfigurationKeyVaultClientFactory keyVaultClientFactory;

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
    private List<ConfigurationSetting> watchKeyListMock;

    private AppConfigurationPropertySourceLocator locator;

    private AppConfigurationProviderProperties appProperties;

    private List<ConfigStore> stores;

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

        stores = new ArrayList<>();
        stores.add(configStoreMock);

        AppConfigurationStoreMonitoring monitoring = new AppConfigurationStoreMonitoring();
        monitoring.setEnabled(false);
        AppConfigurationStoreTrigger trigger = new AppConfigurationStoreTrigger();
        trigger.setKey("sentinel");
        trigger.setKey("test");
        ArrayList<AppConfigurationStoreTrigger> triggers = new ArrayList<>();
        triggers.add(trigger);
        monitoring.setTriggers(triggers);
        when(configStoreMock.getMonitoring()).thenReturn(monitoring);

        when(configClientMock.listConfigurationSettings(Mockito.any())).thenReturn(settingsMock);

        when(iterableMock.iterator()).thenReturn(iteratorMock);
        when(iteratorMock.hasNext()).thenReturn(true).thenReturn(false);
        when(iteratorMock.next()).thenReturn(pagedMock);

        when(watchKeyListMock.iterator()).thenReturn(Collections.emptyIterator());

        when(clientFactoryMock.getAvailableClients(Mockito.anyString(), Mockito.eq(true)))
            .thenReturn(Arrays.asList(replicaClientMock));
        when(replicaClientMock.listSettings(Mockito.any())).thenReturn(watchKeyListMock)
            .thenReturn(watchKeyListMock).thenReturn(watchKeyListMock);
        when(replicaClientMock.getEndpoint()).thenReturn(TEST_STORE_NAME);

        appProperties = new AppConfigurationProviderProperties();
        appProperties.setVersion("1.0");
        appProperties.setMaxRetries(12);
        appProperties.setMaxRetryTime(0);
        appProperties.setDefaultMaxBackoff((long) 600);
        appProperties.setDefaultMinBackoff((long) 30);

        AppConfigurationKeyValueSelector selectedKeys = new AppConfigurationKeyValueSelector().setKeyFilter(KEY_FILTER);
        List<AppConfigurationKeyValueSelector> selects = new ArrayList<>();
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

        locator = new AppConfigurationPropertySourceLocator(appProperties, clientFactoryMock,
            keyVaultClientFactory, null, stores);

        try (MockedStatic<StateHolder> stateHolderMock = Mockito.mockStatic(StateHolder.class)) {
            stateHolderMock.when(() -> StateHolder.updateState(Mockito.any())).thenReturn(null);
            PropertySource<?> source = locator.locate(emptyEnvironment);

            assertTrue(source instanceof CompositePropertySource);

            Collection<PropertySource<?>> sources = ((CompositePropertySource) source).getPropertySources();

            String[] expectedSourceNames = new String[] {
                KEY_FILTER + "store1/\0"
            };
            assertEquals(expectedSourceNames.length, sources.size());
            assertArrayEquals((Object[]) expectedSourceNames, sources.stream().map(PropertySource::getName).toArray());
        }
    }

    @Test
    public void compositeSourceIsCreatedWithMonitoring() {
        String watchKey = "wk1";
        String watchValue = "0";
        String watchLabel = EMPTY_LABEL;
        AppConfigurationStoreMonitoring monitoring = new AppConfigurationStoreMonitoring();
        monitoring.setEnabled(true);
        List<AppConfigurationStoreTrigger> watchKeys = new ArrayList<>();
        AppConfigurationStoreTrigger trigger = new AppConfigurationStoreTrigger();
        trigger.setKey(watchKey);
        trigger.setLabel(watchLabel);
        watchKeys.add(trigger);
        monitoring.setTriggers(watchKeys);

        when(configStoreMock.getMonitoring()).thenReturn(monitoring);
        when(configStoreMock.getFeatureFlags()).thenReturn(featureFlagStoreMock);
        when(replicaClientMock.getWatchKey(Mockito.eq(watchKey), Mockito.anyString()))
            .thenReturn(TestUtils.createItem("", watchKey, watchValue, watchLabel, ""));

        locator = new AppConfigurationPropertySourceLocator(appProperties, clientFactoryMock, keyVaultClientFactory,
            null, stores);

        try (MockedStatic<StateHolder> stateHolderMock = Mockito.mockStatic(StateHolder.class)) {
            stateHolderMock.when(() -> StateHolder.updateState(Mockito.any())).thenReturn(null);
            PropertySource<?> source = locator.locate(emptyEnvironment);

            assertTrue(source instanceof CompositePropertySource);

            Collection<PropertySource<?>> sources = ((CompositePropertySource) source).getPropertySources();

            String[] expectedSourceNames = new String[] {
                KEY_FILTER + "store1/\0"
            };
            assertEquals(expectedSourceNames.length, sources.size());
            assertArrayEquals((Object[]) expectedSourceNames, sources.stream().map(PropertySource::getName).toArray());
            verify(replicaClientMock, times(1)).getWatchKey(Mockito.eq(watchKey), Mockito.anyString());
        }
    }

    @Test
    public void compositeSourceIsCreatedWithMonitoringNoWatchKey() {
        String watchKey = "wk1";
        String watchLabel = EMPTY_LABEL;
        AppConfigurationStoreMonitoring monitoring = new AppConfigurationStoreMonitoring();
        monitoring.setEnabled(true);
        List<AppConfigurationStoreTrigger> watchKeys = new ArrayList<>();
        AppConfigurationStoreTrigger trigger = new AppConfigurationStoreTrigger();
        trigger.setKey(watchKey);
        trigger.setLabel(watchLabel);
        watchKeys.add(trigger);
        monitoring.setTriggers(watchKeys);

        when(configStoreMock.getMonitoring()).thenReturn(monitoring);
        when(configStoreMock.getFeatureFlags()).thenReturn(featureFlagStoreMock);
        when(replicaClientMock.getWatchKey(Mockito.eq(watchKey), Mockito.anyString()))
            .thenReturn(null);

        locator = new AppConfigurationPropertySourceLocator(appProperties, clientFactoryMock, keyVaultClientFactory,
            null, stores);

        try (MockedStatic<StateHolder> stateHolderMock = Mockito.mockStatic(StateHolder.class)) {
            stateHolderMock.when(() -> StateHolder.updateState(Mockito.any())).thenReturn(null);
            PropertySource<?> source = locator.locate(emptyEnvironment);

            assertTrue(source instanceof CompositePropertySource);

            Collection<PropertySource<?>> sources = ((CompositePropertySource) source).getPropertySources();

            String[] expectedSourceNames = new String[] {
                KEY_FILTER + "store1/\0"
            };
            assertEquals(expectedSourceNames.length, sources.size());
            assertArrayEquals((Object[]) expectedSourceNames, sources.stream().map(PropertySource::getName).toArray());
            verify(replicaClientMock, times(1)).getWatchKey(Mockito.eq(watchKey), Mockito.anyString());
        }
    }

    @Test
    public void devSourceIsCreated() {
        when(configStoreMock.getFeatureFlags()).thenReturn(featureFlagStoreMock);

        locator = new AppConfigurationPropertySourceLocator(appProperties, clientFactoryMock, keyVaultClientFactory,
            null, stores);

        try (MockedStatic<StateHolder> stateHolderMock = Mockito.mockStatic(StateHolder.class)) {
            stateHolderMock.when(() -> StateHolder.updateState(Mockito.any())).thenReturn(null);
            PropertySource<?> source = locator.locate(devEnvironment);
            assertTrue(source instanceof CompositePropertySource);

            Collection<PropertySource<?>> sources = ((CompositePropertySource) source).getPropertySources();

            String[] expectedSourceNames = new String[] {
                KEY_FILTER + "store1/dev"
            };
            assertEquals(expectedSourceNames.length, sources.size());
            assertArrayEquals((Object[]) expectedSourceNames, sources.stream().map(PropertySource::getName).toArray());
        }
    }

    @Test
    public void multiSourceIsCreated() {
        when(configStoreMock.getFeatureFlags()).thenReturn(featureFlagStoreMock);

        locator = new AppConfigurationPropertySourceLocator(appProperties, clientFactoryMock, keyVaultClientFactory,
            null, stores);

        try (MockedStatic<StateHolder> stateHolderMock = Mockito.mockStatic(StateHolder.class)) {
            stateHolderMock.when(() -> StateHolder.updateState(Mockito.any())).thenReturn(null);
            PropertySource<?> source = locator.locate(multiEnvironment);
            assertTrue(source instanceof CompositePropertySource);

            Collection<PropertySource<?>> sources = ((CompositePropertySource) source).getPropertySources();

            String[] expectedSourceNames = new String[] {
                KEY_FILTER + "store1/prod,dev"
            };
            assertEquals(expectedSourceNames.length, sources.size());
            assertArrayEquals((Object[]) expectedSourceNames, sources.stream().map(PropertySource::getName).toArray());
        }
    }

    @Test
    public void storeCreatedWithFeatureFlags() {
        FeatureFlagStore featureFlagStore = new FeatureFlagStore();
        featureFlagStore.setEnabled(true);
        featureFlagStore.validateAndInit();

        List<ConfigurationSetting> featureList = new ArrayList<>();
        FeatureFlagConfigurationSetting featureFlag = new FeatureFlagConfigurationSetting("Alpha", false);
        featureFlag.setValue("");
        featureList.add(featureFlag);

        when(configStoreMock.getFeatureFlags()).thenReturn(featureFlagStore);
        when(replicaClientMock.listSettings(Mockito.any())).thenReturn(featureList);

        locator = new AppConfigurationPropertySourceLocator(appProperties, clientFactoryMock, keyVaultClientFactory,
            null, stores);

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
                "FM_store1/ ",
                KEY_FILTER + "store1/\0"
            };
            assertEquals(expectedSourceNames.length, sources.size());
            assertArrayEquals((Object[]) expectedSourceNames, sources.stream().map(PropertySource::getName).toArray());
        }
    }

    @Test
    public void storeCreatedWithFeatureFlagsRequireAll() {
        FeatureFlagStore featureFlagStore = new FeatureFlagStore();
        featureFlagStore.setEnabled(true);
        featureFlagStore.validateAndInit();

        List<ConfigurationSetting> featureList = new ArrayList<>();
        FeatureFlagConfigurationSetting featureFlag = new FeatureFlagConfigurationSetting("Alpha", true);
        featureFlag.setValue("{\"id\":null,\"description\":null,\"display_name\":null,\"enabled\":true,\"conditions\":{\"requirement_type\":\"All\", \"client_filters\":[{\"name\":\"AlwaysOn\",\"parameters\":{}}]}}");
        featureList.add(featureFlag);

        when(configStoreMock.getFeatureFlags()).thenReturn(featureFlagStore);
        when(replicaClientMock.listSettings(Mockito.any())).thenReturn(featureList);
        when(replicaClientMock.getTracingInfo()).thenReturn(new TracingInfo(false, false, 0, null));

        locator = new AppConfigurationPropertySourceLocator(appProperties, clientFactoryMock, keyVaultClientFactory,
            null, stores);

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
                "FM_store1/ ",
                KEY_FILTER + "store1/\0"
            };
            assertEquals(expectedSourceNames.length, sources.size());
            Object[] propertySources = sources.stream().map(c -> c.getProperty("feature-management.Alpha")).toArray();
            Feature alpha = (Feature) propertySources[0];
            assertEquals("All", alpha.getRequirementType());
            assertArrayEquals((Object[]) expectedSourceNames, sources.stream().map(PropertySource::getName).toArray());

        }
    }

    @Test
    public void storeCreatedWithFeatureFlagsWithMonitoring() {
        AppConfigurationStoreMonitoring monitoring = new AppConfigurationStoreMonitoring();
        monitoring.setEnabled(true);
        FeatureFlagStore featureFlagStore = new FeatureFlagStore();
        featureFlagStore.setEnabled(true);
        featureFlagStore.validateAndInit();

        List<ConfigurationSetting> featureList = new ArrayList<>();
        FeatureFlagConfigurationSetting featureFlag = new FeatureFlagConfigurationSetting("Alpha", false);
        featureFlag.setValue("");
        featureList.add(featureFlag);

        when(configStoreMock.getFeatureFlags()).thenReturn(featureFlagStore);
        when(configStoreMock.getMonitoring()).thenReturn(monitoring);
        when(replicaClientMock.listSettings(Mockito.any())).thenReturn(featureList);

        locator = new AppConfigurationPropertySourceLocator(appProperties, clientFactoryMock, keyVaultClientFactory,
            null, stores);

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
                "FM_store1/ ",
                KEY_FILTER + "store1/\0"
            };
            assertEquals(expectedSourceNames.length, sources.size());
            assertArrayEquals((Object[]) expectedSourceNames, sources.stream().map(PropertySource::getName).toArray());
        }
    }

    @Test
    public void watchedKeyCheck() {
        when(configStoreMock.getFeatureFlags()).thenReturn(featureFlagStoreMock);

        locator = new AppConfigurationPropertySourceLocator(appProperties, clientFactoryMock, keyVaultClientFactory,
            null, stores);

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
            assertArrayEquals((Object[]) expectedSourceNames, sources.stream().map(PropertySource::getName).toArray());
        }
    }

    @Test
    public void defaultFailFastThrowException() {
        AppConfigurationStoreTrigger trigger = new AppConfigurationStoreTrigger();
        List<AppConfigurationStoreTrigger> triggers = new ArrayList<>();
        triggers.add(trigger);
        AppConfigurationStoreMonitoring monitor = new AppConfigurationStoreMonitoring();
        monitor.setEnabled(true);
        monitor.setTriggers(triggers);

        when(configStoreMock.getFeatureFlags()).thenReturn(featureFlagStoreMock);
        when(configStoreMock.isFailFast()).thenReturn(true);
        when(configStoreMock.getMonitoring()).thenReturn(monitor);

        locator = new AppConfigurationPropertySourceLocator(appProperties, clientFactoryMock, keyVaultClientFactory,
            null, stores);

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

        locator = new AppConfigurationPropertySourceLocator(appProperties, clientFactoryMock, keyVaultClientFactory,
            null, stores);

        when(clientFactoryMock.getAvailableClients(Mockito.anyString())).thenReturn(Arrays.asList(replicaClientMock));
        when(replicaClientMock.getWatchKey(Mockito.any(), Mockito.anyString())).thenThrow(new RuntimeException());
        when(replicaClientMock.listSettings(any())).thenThrow(new RuntimeException());

        try (MockedStatic<StateHolder> stateHolderMock = Mockito.mockStatic(StateHolder.class)) {
            stateHolderMock.when(() -> StateHolder.getLoadState(Mockito.anyString())).thenReturn(true);
            RuntimeException e = assertThrows(RuntimeException.class, () -> locator.locate(emptyEnvironment));
            assertEquals("Failed to generate property sources for store1", e.getMessage());
        }
    }

    @Test
    @Disabled
    public void notFailFastShouldPass() {
        when(configStoreMock.isFailFast()).thenReturn(false);
        locator = new AppConfigurationPropertySourceLocator(appProperties, clientFactoryMock, keyVaultClientFactory,
            null, stores);

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

        locator = new AppConfigurationPropertySourceLocator(appProperties,
            clientFactoryMock, keyVaultClientFactory, null, properties.getStores());

        try (MockedStatic<StateHolder> stateHolderMock = Mockito.mockStatic(StateHolder.class)) {
            stateHolderMock.when(() -> StateHolder.updateState(Mockito.any())).thenReturn(null);
            PropertySource<?> source = locator.locate(emptyEnvironment);
            assertTrue(source instanceof CompositePropertySource);

            Collection<PropertySource<?>> sources = ((CompositePropertySource) source).getPropertySources();
            String[] expectedSourceNames = new String[] { KEY_FILTER + TEST_STORE_NAME_2 + "/\0",
                KEY_FILTER + TEST_STORE_NAME_1 + "/\0" };
            assertEquals(2, sources.size());
            assertArrayEquals((Object[]) expectedSourceNames, sources.stream().map(PropertySource::getName).toArray());
        }
    }

    @Test
    public void awaitOnError() {
        List<ConfigStore> configStores = new ArrayList<>();
        configStores.add(configStoreMockError);

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
        AppConfigurationKeyValueSelector selectedKeys = new AppConfigurationKeyValueSelector()
            .setKeyFilter("/application/");
        List<AppConfigurationKeyValueSelector> selects = new ArrayList<>();
        selects.add(selectedKeys);
        when(configStoreMockError.getSelects()).thenReturn(selects);
        when(configStoreMockError.isEnabled()).thenReturn(true);
        when(configStoreMockError.isFailFast()).thenReturn(true);
        when(configStoreMockError.getEndpoint()).thenReturn("");

        when(configStoreMockError.getFeatureFlags()).thenReturn(featureFlagStoreMock);

        when(clientFactoryMock.getAvailableClients(Mockito.anyString())).thenReturn(Arrays.asList(replicaClientMock));
        when(replicaClientMock.listSettings(Mockito.any())).thenThrow(new NullPointerException(""));
        when(appPropertiesMock.getPrekillTime()).thenReturn(-60);
        when(appPropertiesMock.getStartDate()).thenReturn(Instant.now());

        locator = new AppConfigurationPropertySourceLocator(appPropertiesMock, clientFactoryMock, keyVaultClientFactory,
            null, configStores);

        assertThrows(RuntimeException.class, () -> locator.locate(env));
        verify(appPropertiesMock, times(1)).getPrekillTime();
    }

    @Test
    public void storeDisabled() {
        when(configStoreMock.isEnabled()).thenReturn(false);

        locator = new AppConfigurationPropertySourceLocator(appProperties, clientFactoryMock, keyVaultClientFactory,
            null, stores);
        try (MockedStatic<StateHolder> stateHolderMock = Mockito.mockStatic(StateHolder.class)) {
            stateHolderMock.when(() -> StateHolder.updateState(Mockito.any())).thenReturn(null);
            PropertySource<?> source = locator.locate(emptyEnvironment);
            assertTrue(source instanceof CompositePropertySource);

            Collection<PropertySource<?>> sources = ((CompositePropertySource) source).getPropertySources();

            assertEquals(0, sources.size());
        }
    }
}
