// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation;

import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.EMPTY_LABEL;
import static com.azure.spring.cloud.appconfiguration.config.implementation.TestConstants.TEST_CONN_STRING;
import static com.azure.spring.cloud.appconfiguration.config.implementation.TestConstants.TEST_CONN_STRING_2;
import static com.azure.spring.cloud.appconfiguration.config.implementation.TestConstants.TEST_STORE_NAME;
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
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.MockitoSession;
import org.mockito.quality.Strictness;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

import com.azure.data.appconfiguration.models.FeatureFlagConfigurationSetting;
import com.azure.spring.cloud.appconfiguration.config.implementation.autofailover.ReplicaLookUp;
import com.azure.spring.cloud.appconfiguration.config.implementation.feature.entity.Feature;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.AppConfigurationKeyValueSelector;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.AppConfigurationProperties;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.AppConfigurationProviderProperties;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.AppConfigurationStoreMonitoring;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.AppConfigurationStoreTrigger;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.ConfigStore;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.FeatureFlagStore;

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
    private ConfigStore configStoreMockError;

    @Mock
    private AppConfigurationProviderProperties appPropertiesMock;

    @Mock
    private ReplicaLookUp replicaLookUpMock;

    @Mock
    private FeatureFlagClient featureFlagClientMock;

    @Mock
    private ConfigStore configStoreMock;

    private AppConfigurationPropertySourceLocator locator;

    private AppConfigurationProperties properties;

    private AppConfigurationProviderProperties appProperties;

    private List<ConfigStore> stores;

    private AppConfigurationStoreMonitoring monitoring;

    private MutablePropertySources sources = new MutablePropertySources();

    private MockitoSession session;

    @BeforeEach
    public void setup() {
        session = Mockito.mockitoSession().initMocks(this).strictness(Strictness.STRICT_STUBS).startMocking();
        MockitoAnnotations.openMocks(this);

        sources.addFirst(new PropertySource<String>("refreshArgs") {

            @Override
            public Object getProperty(String name) {
                return null;
            }
        });

        properties = new AppConfigurationProperties();
        properties.setEnabled(true);
        properties.setRefreshInterval(null);

        TestUtils.addStore(properties, TEST_STORE_NAME, TEST_CONN_STRING, KEY_FILTER);

        monitoring = new AppConfigurationStoreMonitoring();
        monitoring.setEnabled(false);
        AppConfigurationStoreTrigger trigger = new AppConfigurationStoreTrigger();
        trigger.setKey("test");

        monitoring.setTriggers(List.of(trigger));

        appProperties = new AppConfigurationProviderProperties();
        appProperties.setVersion("1.0");
        appProperties.setMaxRetries(12);
        appProperties.setMaxRetryTime(0);
        appProperties.setDefaultMaxBackoff((long) 600);
        appProperties.setDefaultMinBackoff((long) 30);

        properties.getStores().get(0).setFeatureFlags(featureFlagStoreMock);
        properties.getStores().get(0).setMonitoring(monitoring);
        stores = properties.getStores();
    }

    @AfterEach
    public void cleanup() throws Exception {
        MockitoAnnotations.openMocks(this).close();
        session.finishMocking();
        AppConfigurationPropertySourceLocator.STARTUP.set(true);
    }

    @Test
    public void compositeSourceIsCreated() {
        setupEmptyEnvironment();
        when(featureFlagClientMock.getProperties()).thenReturn(Map.of());

        locator = new AppConfigurationPropertySourceLocator(appProperties, clientFactoryMock,
            keyVaultClientFactory, null, stores, replicaLookUpMock, featureFlagClientMock);

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
        setupEmptyEnvironment();
        when(featureFlagClientMock.getProperties()).thenReturn(Map.of());

        String watchKey = "wk1";
        AppConfigurationStoreMonitoring monitoring = new AppConfigurationStoreMonitoring();
        monitoring.setEnabled(true);

        AppConfigurationStoreTrigger trigger = new AppConfigurationStoreTrigger();
        trigger.setKey(watchKey);
        trigger.setLabel(EMPTY_LABEL);
        monitoring.setTriggers(List.of(trigger));

        properties.getStores().get(0).setMonitoring(monitoring);

        when(replicaClientMock.getWatchKey(Mockito.eq(watchKey), Mockito.anyString()))
            .thenReturn(TestUtils.createItem("", watchKey, "0", EMPTY_LABEL, ""));

        locator = new AppConfigurationPropertySourceLocator(appProperties, clientFactoryMock, keyVaultClientFactory,
            null, stores, replicaLookUpMock, featureFlagClientMock);

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
    public void compositeSourceIsCreatedWithMonitoringWatchKeyDoesNotExist() {
        // The listed Watch Key doesn't have a value in app config. When one is added will cause a refresh.
        setupEmptyEnvironment();
        when(featureFlagClientMock.getProperties()).thenReturn(Map.of());

        String watchKey = "wk1";
        AppConfigurationStoreMonitoring monitoring = new AppConfigurationStoreMonitoring();
        monitoring.setEnabled(true);

        AppConfigurationStoreTrigger trigger = new AppConfigurationStoreTrigger();
        trigger.setKey(watchKey);
        trigger.setLabel(EMPTY_LABEL);
        monitoring.setTriggers(List.of(trigger));

        properties.getStores().get(0).setMonitoring(monitoring);

        when(replicaClientMock.getWatchKey(Mockito.eq(watchKey), Mockito.anyString()))
            .thenReturn(null);

        locator = new AppConfigurationPropertySourceLocator(appProperties, clientFactoryMock, keyVaultClientFactory,
            null, stores, replicaLookUpMock, featureFlagClientMock);

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
        when(devEnvironment.getActiveProfiles()).thenReturn(new String[] { PROFILE_NAME_1 });
        when(devEnvironment.getPropertySources()).thenReturn(sources);
        when(clientFactoryMock.getAvailableClients(Mockito.anyString(), Mockito.eq(true))).thenReturn(Arrays.asList(replicaClientMock));
        when(replicaClientMock.listSettings(Mockito.any())).thenReturn(List.of());
        when(featureFlagClientMock.getProperties()).thenReturn(Map.of());

        locator = new AppConfigurationPropertySourceLocator(appProperties, clientFactoryMock, keyVaultClientFactory,
            null, stores, replicaLookUpMock, featureFlagClientMock);

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
        when(multiEnvironment.getActiveProfiles()).thenReturn(new String[] { PROFILE_NAME_1, PROFILE_NAME_2 });
        when(multiEnvironment.getPropertySources()).thenReturn(sources);
        when(clientFactoryMock.getAvailableClients(Mockito.anyString(), Mockito.eq(true))).thenReturn(Arrays.asList(replicaClientMock));
        when(replicaClientMock.listSettings(Mockito.any())).thenReturn(List.of());
        when(featureFlagClientMock.getProperties()).thenReturn(Map.of());

        locator = new AppConfigurationPropertySourceLocator(appProperties, clientFactoryMock, keyVaultClientFactory,
            null, stores, replicaLookUpMock, featureFlagClientMock);

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
        setupEmptyEnvironment();
        when(featureFlagClientMock.getProperties()).thenReturn(Map.of("fake_features", new Feature()));

        FeatureFlagStore featureFlagStore = new FeatureFlagStore();
        featureFlagStore.setEnabled(true);
        featureFlagStore.validateAndInit();

        FeatureFlagConfigurationSetting featureFlag = new FeatureFlagConfigurationSetting("Alpha", false);
        featureFlag.setValue("");

        properties.getStores().get(0).setFeatureFlags(featureFlagStore);

        when(replicaClientMock.listSettings(Mockito.any())).thenReturn(List.of(featureFlag));

        locator = new AppConfigurationPropertySourceLocator(appProperties, clientFactoryMock, keyVaultClientFactory,
            null, stores, replicaLookUpMock, featureFlagClientMock);

        try (MockedStatic<StateHolder> stateHolderMock = Mockito.mockStatic(StateHolder.class)) {
            stateHolderMock.when(() -> StateHolder.updateState(Mockito.any())).thenReturn(null);
            PropertySource<?> source = locator.locate(emptyEnvironment);
            assertTrue(source instanceof CompositePropertySource);

            Collection<PropertySource<?>> sources = ((CompositePropertySource) source).getPropertySources();
            String[] expectedSourceNames = new String[] {
                KEY_FILTER + "store1/\0",
                "feature_management"
            };
            assertEquals(expectedSourceNames.length, sources.size());
            assertArrayEquals((Object[]) expectedSourceNames, sources.stream().map(PropertySource::getName).toArray());
        }
    }

    @Test
    public void storeCreatedWithFeatureFlagsWithMonitoring() {
        setupEmptyEnvironment();
        when(featureFlagClientMock.getProperties()).thenReturn(Map.of("fake_features", new Feature()));

        AppConfigurationStoreMonitoring monitoring = new AppConfigurationStoreMonitoring();
        monitoring.setEnabled(true);
        FeatureFlagStore featureFlagStore = new FeatureFlagStore();
        featureFlagStore.setEnabled(true);
        featureFlagStore.validateAndInit();

        FeatureFlagConfigurationSetting featureFlag = new FeatureFlagConfigurationSetting("Alpha", false);
        featureFlag.setValue("");

        properties.getStores().get(0).setFeatureFlags(featureFlagStore);
        properties.getStores().get(0).setMonitoring(monitoring);

        when(replicaClientMock.listSettings(Mockito.any())).thenReturn(List.of(featureFlag));

        locator = new AppConfigurationPropertySourceLocator(appProperties, clientFactoryMock, keyVaultClientFactory,
            null, stores, replicaLookUpMock, featureFlagClientMock);

        try (MockedStatic<StateHolder> stateHolderMock = Mockito.mockStatic(StateHolder.class)) {
            stateHolderMock.when(() -> StateHolder.updateState(Mockito.any())).thenReturn(null);
            PropertySource<?> source = locator.locate(emptyEnvironment);
            assertTrue(source instanceof CompositePropertySource);

            Collection<PropertySource<?>> sources = ((CompositePropertySource) source).getPropertySources();
            String[] expectedSourceNames = new String[] {
                KEY_FILTER + "store1/\0",
                "feature_management"
            };
            assertEquals(expectedSourceNames.length, sources.size());
            assertArrayEquals((Object[]) expectedSourceNames, sources.stream().map(PropertySource::getName).toArray());
        }
    }

    @Test
    public void watchedKeyCheck() {
        setupEmptyEnvironment();
        when(featureFlagClientMock.getProperties()).thenReturn(Map.of());

        locator = new AppConfigurationPropertySourceLocator(appProperties, clientFactoryMock, keyVaultClientFactory,
            null, stores, replicaLookUpMock, featureFlagClientMock);

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
    public void defaultFailFastThrowException() {
        when(emptyEnvironment.getActiveProfiles()).thenReturn(new String[] {});
        when(emptyEnvironment.getPropertySources()).thenReturn(sources);
        when(clientFactoryMock.getAvailableClients(Mockito.anyString(), Mockito.eq(true))).thenReturn(Arrays.asList(replicaClientMock));
        when(configStoreMock.getEndpoint()).thenReturn(TEST_STORE_NAME);
        when(configStoreMock.isEnabled()).thenReturn(true);
        when(configStoreMock.getSelects()).thenReturn(List.of());
        when(configStoreMock.getFeatureFlags()).thenReturn(featureFlagStoreMock);
        when(configStoreMock.isFailFast()).thenReturn(true);

        AppConfigurationStoreTrigger trigger = new AppConfigurationStoreTrigger();
        AppConfigurationStoreMonitoring monitor = new AppConfigurationStoreMonitoring();
        monitor.setEnabled(true);
        monitor.setTriggers(List.of(trigger));

        when(configStoreMock.getMonitoring()).thenReturn(monitor);

        locator = new AppConfigurationPropertySourceLocator(appProperties, clientFactoryMock, keyVaultClientFactory,
            null, List.of(configStoreMock), replicaLookUpMock, featureFlagClientMock);

        when(replicaClientMock.getWatchKey(Mockito.any(), Mockito.anyString())).thenThrow(new RuntimeException());
        RuntimeException e = assertThrows(RuntimeException.class, () -> locator.locate(emptyEnvironment));
        assertEquals("Failed to generate property sources for " + TEST_STORE_NAME, e.getMessage());
        verify(configStoreMock, times(1)).isFailFast();
    }

    @Test
    public void refreshThrowException() throws IllegalArgumentException {
        setupEmptyEnvironment();
        when(replicaClientMock.listSettings(any())).thenThrow(new RuntimeException());

        AppConfigurationPropertySourceLocator.STARTUP.set(false);

        locator = new AppConfigurationPropertySourceLocator(appProperties, clientFactoryMock, keyVaultClientFactory,
            null, stores, replicaLookUpMock, featureFlagClientMock);

        try (MockedStatic<StateHolder> stateHolderMock = Mockito.mockStatic(StateHolder.class)) {
            stateHolderMock.when(() -> StateHolder.getLoadState(Mockito.anyString())).thenReturn(true);
            RuntimeException e = assertThrows(RuntimeException.class, () -> locator.locate(emptyEnvironment));
            assertEquals("Failed to generate property sources for store1", e.getMessage());
        }
    }

    @Test
    public void notFailFastShouldPass() {
        when(featureFlagClientMock.getProperties()).thenReturn(Map.of());
        when(emptyEnvironment.getActiveProfiles()).thenReturn(new String[] {});
        when(emptyEnvironment.getPropertySources()).thenReturn(sources);
        when(clientFactoryMock.getAvailableClients(Mockito.anyString(), Mockito.eq(true))).thenReturn(Arrays.asList(replicaClientMock));
        when(configStoreMock.getEndpoint()).thenReturn(TEST_STORE_NAME);
        when(configStoreMock.isEnabled()).thenReturn(true);
        when(configStoreMock.getSelects()).thenReturn(List.of());
        when(configStoreMock.getFeatureFlags()).thenReturn(featureFlagStoreMock);
        when(configStoreMock.isFailFast()).thenReturn(false);

        locator = new AppConfigurationPropertySourceLocator(appProperties, clientFactoryMock, keyVaultClientFactory,
            null, List.of(configStoreMock), replicaLookUpMock, featureFlagClientMock);

        properties.getStores().get(0).setFailFast(false);

        try (MockedStatic<StateHolder> stateHolderMock = Mockito.mockStatic(StateHolder.class)) {
            stateHolderMock.when(() -> StateHolder.updateState(Mockito.any())).thenReturn(null);
            PropertySource<?> source = locator.locate(emptyEnvironment);
            assertTrue(source instanceof CompositePropertySource);

            // Once a store fails it should stop attempting to load
            verify(configStoreMock, times(3)).isFailFast();
        }
    }

    @Test
    public void multiplePropertySourcesExistForMultiStores() {
        setupEmptyEnvironment();
        when(featureFlagClientMock.getProperties()).thenReturn(Map.of());
        TestUtils.addStore(properties, TEST_STORE_NAME_2, TEST_CONN_STRING_2, KEY_FILTER);

        locator = new AppConfigurationPropertySourceLocator(appProperties,
            clientFactoryMock, keyVaultClientFactory, null, properties.getStores(), replicaLookUpMock,
            featureFlagClientMock);

        try (MockedStatic<StateHolder> stateHolderMock = Mockito.mockStatic(StateHolder.class)) {
            stateHolderMock.when(() -> StateHolder.updateState(Mockito.any())).thenReturn(null);
            PropertySource<?> source = locator.locate(emptyEnvironment);
            assertTrue(source instanceof CompositePropertySource);

            Collection<PropertySource<?>> sources = ((CompositePropertySource) source).getPropertySources();
            String[] expectedSourceNames = new String[] { KEY_FILTER + TEST_STORE_NAME_2 + "/\0",
                KEY_FILTER + TEST_STORE_NAME + "/\0" };
            assertEquals(2, sources.size());
            assertArrayEquals((Object[]) expectedSourceNames, sources.stream().map(PropertySource::getName).toArray());
        }
    }

    @Test
    public void awaitOnError() {
        when(clientFactoryMock.getAvailableClients(Mockito.anyString(), Mockito.eq(true))).thenReturn(Arrays.asList(replicaClientMock));
        when(replicaClientMock.listSettings(Mockito.any())).thenReturn(List.of());
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

        when(replicaClientMock.listSettings(Mockito.any())).thenThrow(new NullPointerException(""));
        when(appPropertiesMock.getPrekillTime()).thenReturn(-60);
        when(appPropertiesMock.getStartDate()).thenReturn(Instant.now());

        locator = new AppConfigurationPropertySourceLocator(appPropertiesMock, clientFactoryMock, keyVaultClientFactory,
            null, List.of(configStoreMockError), replicaLookUpMock, featureFlagClientMock);

        assertThrows(RuntimeException.class, () -> locator.locate(env));
        verify(appPropertiesMock, times(1)).getPrekillTime();
    }

    @Test
    public void storeDisabled() {
        when(emptyEnvironment.getActiveProfiles()).thenReturn(new String[] {});
        when(emptyEnvironment.getPropertySources()).thenReturn(sources);
        properties.getStores().get(0).setEnabled(false);
        properties.getStores().get(0).setMonitoring(monitoring);

        locator = new AppConfigurationPropertySourceLocator(appProperties, clientFactoryMock, keyVaultClientFactory,
            null, stores, replicaLookUpMock, featureFlagClientMock);
        try (MockedStatic<StateHolder> stateHolderMock = Mockito.mockStatic(StateHolder.class)) {
            stateHolderMock.when(() -> StateHolder.updateState(Mockito.any())).thenReturn(null);
            PropertySource<?> source = locator.locate(emptyEnvironment);
            assertTrue(source instanceof CompositePropertySource);

            Collection<PropertySource<?>> sources = ((CompositePropertySource) source).getPropertySources();

            assertEquals(0, sources.size());
        }
    }

    private void setupEmptyEnvironment() {
        when(emptyEnvironment.getActiveProfiles()).thenReturn(new String[] {});
        when(emptyEnvironment.getPropertySources()).thenReturn(sources);
        when(clientFactoryMock.getAvailableClients(Mockito.anyString(), Mockito.eq(true))).thenReturn(Arrays.asList(replicaClientMock));
        when(replicaClientMock.listSettings(Mockito.any())).thenReturn(List.of());
    }
}
