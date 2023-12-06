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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.ConfigurableBootstrapContext;
import org.springframework.boot.context.config.ConfigData;
import org.springframework.boot.context.config.ConfigDataLoaderContext;
import org.springframework.boot.context.config.ConfigDataResourceNotFoundException;
import org.springframework.boot.context.config.Profiles;
import org.springframework.boot.logging.DeferredLogFactory;
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

@TestMethodOrder(MethodOrderer.MethodName.class)
public class AppConfigDataLoaderTest {

    private static final String DEV_PROFILE = "dev";

    private static final String PROD_PROFILE = "prod";

    private static final String KEY_FILTER = "/foo/";

    private List<String> devProfiles = new ArrayList<>();

    private List<String> multiProfiles = new ArrayList<>();

    @Mock
    private AppConfigurationReplicaClientFactory clientFactoryMock;

    @Mock
    private AppConfigurationKeyVaultClientFactory keyVaultClientFactory;

    @Mock
    private AppConfigurationReplicaClient replicaClientMock;

    @Mock
    private ConfigurationAsyncClient configClientMock;

    @Mock
    private PagedFlux<ConfigurationSetting> settingsMock;

    @Mock
    private Iterable<PagedResponse<ConfigurationSetting>> iterableMock;

    @Mock
    private Iterator<PagedResponse<ConfigurationSetting>> iteratorMock;

    @Mock
    private PagedResponse<ConfigurationSetting> pagedMock;

    private ConfigStore configStore;

    @Mock
    private AppConfigurationProviderProperties appPropertiesMock;

    @Mock
    private List<ConfigurationSetting> watchKeyListMock;

    @Mock
    private DeferredLogFactory deferredLogMock;

    @Mock
    private Log logMock;

    @Mock
    private ConfigDataLoaderContext contextMock;

    @Mock
    private ConfigurableBootstrapContext bootContextMock;

    @Mock
    private Profiles profilesMock;

    private AppConfigDataResource appConfigDataResource;

    private AppConfigDataLoader loader;

    private AppConfigurationProviderProperties appProperties;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        devProfiles.add(DEV_PROFILE);
        multiProfiles.add(DEV_PROFILE);
        multiProfiles.add(PROD_PROFILE);

        configStore = new ConfigStore();
        configStore.setConnectionString(TEST_CONN_STRING);
        configStore.setEndpoint(TEST_STORE_NAME);
        configStore.setEnabled(true);

        AppConfigurationStoreMonitoring monitoring = new AppConfigurationStoreMonitoring();
        monitoring.setEnabled(false);
        AppConfigurationStoreTrigger trigger = new AppConfigurationStoreTrigger();
        trigger.setKey("sentinel");
        trigger.setKey("test");
        ArrayList<AppConfigurationStoreTrigger> triggers = new ArrayList<>();
        triggers.add(trigger);
        monitoring.setTriggers(triggers);
        configStore.setMonitoring(monitoring);

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
        configStore.setSelects(selects);

        when(deferredLogMock.getLog(Mockito.eq(AppConfigDataLoader.class))).thenReturn(logMock);

        when(contextMock.getBootstrapContext()).thenReturn(bootContextMock);
        when(bootContextMock.get(Mockito.eq(AppConfigurationReplicaClientFactory.class))).thenReturn(clientFactoryMock);
        when(bootContextMock.get(Mockito.eq(AppConfigurationKeyVaultClientFactory.class)))
            .thenReturn(keyVaultClientFactory);
    }

    @AfterEach
    public void cleanup() throws Exception {
        MockitoAnnotations.openMocks(this).close();
    }

    @Test
    public void compositeSourceIsCreated() throws ConfigDataResourceNotFoundException, IOException {
        appConfigDataResource = new AppConfigDataResource(configStore, profilesMock, appProperties);

        loader = new AppConfigDataLoader(deferredLogMock);

        try (MockedStatic<StateHolder> stateHolderMock = Mockito.mockStatic(StateHolder.class)) {
            stateHolderMock.when(() -> StateHolder.updateState(Mockito.any())).thenReturn(null);
            ConfigData source = loader.load(contextMock, appConfigDataResource);

            Collection<PropertySource<?>> sources = source.getPropertySources();

            String[] expectedSourceNames = new String[] {
                KEY_FILTER + "store1/\0"
            };
            assertEquals(expectedSourceNames.length, sources.size());
            assertArrayEquals((Object[]) expectedSourceNames, sources.stream().map(PropertySource::getName).toArray());
        }
    }

    @Test
    public void compositeSourceIsCreatedWithMonitoring() throws ConfigDataResourceNotFoundException, IOException {
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

        configStore.setMonitoring(monitoring);

        appConfigDataResource = new AppConfigDataResource(configStore, profilesMock, appProperties);

        when(replicaClientMock.getWatchKey(Mockito.eq(watchKey), Mockito.anyString()))
            .thenReturn(TestUtils.createItem("", watchKey, watchValue, watchLabel, ""));

        loader = new AppConfigDataLoader(deferredLogMock);

        try (MockedStatic<StateHolder> stateHolderMock = Mockito.mockStatic(StateHolder.class)) {
            stateHolderMock.when(() -> StateHolder.updateState(Mockito.any())).thenReturn(null);
            ConfigData source = loader.load(contextMock, appConfigDataResource);

            Collection<PropertySource<?>> sources = source.getPropertySources();

            String[] expectedSourceNames = new String[] {
                KEY_FILTER + "store1/\0"
            };
            assertEquals(expectedSourceNames.length, sources.size());
            assertArrayEquals((Object[]) expectedSourceNames, sources.stream().map(PropertySource::getName).toArray());
            verify(replicaClientMock, times(1)).getWatchKey(Mockito.eq(watchKey), Mockito.anyString());
        }
    }

    @Test
    public void compositeSourceIsCreatedWithMonitoringNoWatchKey()
        throws ConfigDataResourceNotFoundException, IOException {
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

        configStore.setMonitoring(monitoring);

        appConfigDataResource = new AppConfigDataResource(configStore, profilesMock, appProperties);
        when(replicaClientMock.getWatchKey(Mockito.eq(watchKey), Mockito.anyString()))
            .thenReturn(null);

        loader = new AppConfigDataLoader(deferredLogMock);

        try (MockedStatic<StateHolder> stateHolderMock = Mockito.mockStatic(StateHolder.class)) {
            stateHolderMock.when(() -> StateHolder.updateState(Mockito.any())).thenReturn(null);
            ConfigData source = loader.load(contextMock, appConfigDataResource);

            Collection<PropertySource<?>> sources = source.getPropertySources();

            String[] expectedSourceNames = new String[] {
                KEY_FILTER + "store1/\0"
            };
            assertEquals(expectedSourceNames.length, sources.size());
            assertArrayEquals((Object[]) expectedSourceNames, sources.stream().map(PropertySource::getName).toArray());
            verify(replicaClientMock, times(1)).getWatchKey(Mockito.eq(watchKey), Mockito.anyString());
        }
    }

    @Test
    public void devSourceIsCreated() throws ConfigDataResourceNotFoundException, IOException {
        when(profilesMock.getActive()).thenReturn(devProfiles);

        appConfigDataResource = new AppConfigDataResource(configStore, profilesMock, appProperties);

        loader = new AppConfigDataLoader(deferredLogMock);

        try (MockedStatic<StateHolder> stateHolderMock = Mockito.mockStatic(StateHolder.class)) {
            stateHolderMock.when(() -> StateHolder.updateState(Mockito.any())).thenReturn(null);
            ConfigData source = loader.load(contextMock, appConfigDataResource);

            Collection<PropertySource<?>> sources = source.getPropertySources();

            String[] expectedSourceNames = new String[] {
                KEY_FILTER + "store1/dev"
            };
            assertEquals(expectedSourceNames.length, sources.size());
            assertArrayEquals((Object[]) expectedSourceNames, sources.stream().map(PropertySource::getName).toArray());
        }
    }

    @Test
    public void multiSourceIsCreated() throws ConfigDataResourceNotFoundException, IOException {
        when(profilesMock.getActive()).thenReturn(multiProfiles);
        appConfigDataResource = new AppConfigDataResource(configStore, profilesMock, appProperties);

        loader = new AppConfigDataLoader(deferredLogMock);

        try (MockedStatic<StateHolder> stateHolderMock = Mockito.mockStatic(StateHolder.class)) {
            stateHolderMock.when(() -> StateHolder.updateState(Mockito.any())).thenReturn(null);
            ConfigData source = loader.load(contextMock, appConfigDataResource);

            Collection<PropertySource<?>> sources = source.getPropertySources();

            String[] expectedSourceNames = new String[] {
                KEY_FILTER + "store1/prod,dev"
            };
            assertEquals(expectedSourceNames.length, sources.size());
            assertArrayEquals((Object[]) expectedSourceNames, sources.stream().map(PropertySource::getName).toArray());
        }
    }

    @Test
    public void storeCreatedWithFeatureFlags() throws ConfigDataResourceNotFoundException, IOException {
        FeatureFlagStore featureFlagStore = new FeatureFlagStore();
        featureFlagStore.setEnabled(true);
        featureFlagStore.validateAndInit();

        List<ConfigurationSetting> featureList = new ArrayList<>();
        FeatureFlagConfigurationSetting featureFlag = new FeatureFlagConfigurationSetting("Alpha", false);
        featureFlag.setValue("");
        featureList.add(featureFlag);

        configStore.setFeatureFlags(featureFlagStore);
        appConfigDataResource = new AppConfigDataResource(configStore, profilesMock, appProperties);
        when(replicaClientMock.listSettings(Mockito.any())).thenReturn(featureList);

        loader = new AppConfigDataLoader(deferredLogMock);

        try (MockedStatic<StateHolder> stateHolderMock = Mockito.mockStatic(StateHolder.class)) {
            stateHolderMock.when(() -> StateHolder.updateState(Mockito.any())).thenReturn(null);
            ConfigData source = loader.load(contextMock, appConfigDataResource);

            Collection<PropertySource<?>> sources = source.getPropertySources();
            String[] expectedSourceNames = new String[] {
                "FM_store1/ ",
                KEY_FILTER + "store1/\0"
            };
            assertEquals(expectedSourceNames.length, sources.size());
            assertArrayEquals((Object[]) expectedSourceNames, sources.stream().map(PropertySource::getName).toArray());
        }
    }

    @Test
    public void storeCreatedWithFeatureFlagsRequireAll() throws ConfigDataResourceNotFoundException, IOException {
        FeatureFlagStore featureFlagStore = new FeatureFlagStore();
        featureFlagStore.setEnabled(true);
        featureFlagStore.validateAndInit();

        List<ConfigurationSetting> featureList = new ArrayList<>();
        FeatureFlagConfigurationSetting featureFlag = new FeatureFlagConfigurationSetting("Alpha", true);
        featureFlag.setValue(
            "{\"id\":null,\"description\":null,\"display_name\":null,\"enabled\":true,\"conditions\":{\"requirement_type\":\"All\", \"client_filters\":[{\"name\":\"AlwaysOn\",\"parameters\":{}}]}}");
        featureList.add(featureFlag);

        configStore.setFeatureFlags(featureFlagStore);
        appConfigDataResource = new AppConfigDataResource(configStore, profilesMock, appProperties);
        when(replicaClientMock.listSettings(Mockito.any())).thenReturn(featureList);
        when(replicaClientMock.getTracingInfo()).thenReturn(new TracingInfo(false, false, 0, null));

        loader = new AppConfigDataLoader(deferredLogMock);

        try (MockedStatic<StateHolder> stateHolderMock = Mockito.mockStatic(StateHolder.class)) {
            stateHolderMock.when(() -> StateHolder.updateState(Mockito.any())).thenReturn(null);
            ConfigData source = loader.load(contextMock, appConfigDataResource);

            Collection<PropertySource<?>> sources = source.getPropertySources();
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
    public void storeCreatedWithFeatureFlagsWithMonitoring() throws ConfigDataResourceNotFoundException, IOException {
        AppConfigurationStoreMonitoring monitoring = new AppConfigurationStoreMonitoring();
        monitoring.setEnabled(true);
        FeatureFlagStore featureFlagStore = new FeatureFlagStore();
        featureFlagStore.setEnabled(true);
        featureFlagStore.validateAndInit();

        List<ConfigurationSetting> featureList = new ArrayList<>();
        FeatureFlagConfigurationSetting featureFlag = new FeatureFlagConfigurationSetting("Alpha", false);
        featureFlag.setValue("");
        featureList.add(featureFlag);

        configStore.setFeatureFlags(featureFlagStore);
        configStore.setMonitoring(monitoring);
        appConfigDataResource = new AppConfigDataResource(configStore, profilesMock, appProperties);
        when(replicaClientMock.listSettings(Mockito.any())).thenReturn(featureList);

        loader = new AppConfigDataLoader(deferredLogMock);

        try (MockedStatic<StateHolder> stateHolderMock = Mockito.mockStatic(StateHolder.class)) {
            stateHolderMock.when(() -> StateHolder.updateState(Mockito.any())).thenReturn(null);
            ConfigData source = loader.load(contextMock, appConfigDataResource);

            Collection<PropertySource<?>> sources = source.getPropertySources();
            String[] expectedSourceNames = new String[] {
                "FM_store1/ ",
                KEY_FILTER + "store1/\0"
            };
            assertEquals(expectedSourceNames.length, sources.size());
            assertArrayEquals((Object[]) expectedSourceNames, sources.stream().map(PropertySource::getName).toArray());
        }
    }

    @Test
    public void watchedKeyCheck() throws ConfigDataResourceNotFoundException, IOException {
        appConfigDataResource = new AppConfigDataResource(configStore, profilesMock, appProperties);

        loader = new AppConfigDataLoader(deferredLogMock);

        try (MockedStatic<StateHolder> stateHolderMock = Mockito.mockStatic(StateHolder.class)) {
            stateHolderMock.when(() -> StateHolder.updateState(Mockito.any())).thenReturn(null);
            ConfigData source = loader.load(contextMock, appConfigDataResource);

            Collection<PropertySource<?>> sources = source.getPropertySources();
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

        configStore.setMonitoring(monitor);
        appConfigDataResource = new AppConfigDataResource(configStore, profilesMock, appProperties);
        loader = new AppConfigDataLoader(deferredLogMock);

        when(clientFactoryMock.getAvailableClients(Mockito.anyString())).thenReturn(Arrays.asList(replicaClientMock));
        when(replicaClientMock.getWatchKey(Mockito.any(), Mockito.anyString())).thenThrow(new RuntimeException());
        RuntimeException e = assertThrows(RuntimeException.class,
            () -> loader.load(contextMock, appConfigDataResource));
        assertEquals("Failed to generate property sources for " + TEST_STORE_NAME, e.getMessage());
    }

    @Test
    public void refreshThrowException() throws IllegalArgumentException {
        appConfigDataResource = new AppConfigDataResource(configStore, profilesMock, appProperties);

        loader = new AppConfigDataLoader(deferredLogMock);

        when(clientFactoryMock.getAvailableClients(Mockito.anyString())).thenReturn(Arrays.asList(replicaClientMock));
        when(replicaClientMock.getWatchKey(Mockito.any(), Mockito.anyString())).thenThrow(new RuntimeException());
        when(replicaClientMock.listSettings(any())).thenThrow(new RuntimeException());

        try (MockedStatic<StateHolder> stateHolderMock = Mockito.mockStatic(StateHolder.class)) {
            stateHolderMock.when(() -> StateHolder.getLoadState(Mockito.anyString())).thenReturn(true);
            RuntimeException e = assertThrows(RuntimeException.class,
                () -> loader.load(contextMock, appConfigDataResource));
            assertEquals("Failed to generate property sources for store1", e.getMessage());
        }
    }

    @Test
    public void multipleProfilePropertySources() throws ConfigDataResourceNotFoundException, IOException {
        configStore.setEndpoint(TEST_STORE_NAME);
        when(profilesMock.getActive()).thenReturn(multiProfiles);
        appConfigDataResource = new AppConfigDataResource(configStore, profilesMock, appProperties);

        AppConfigurationProperties properties = new AppConfigurationProperties();
        TestUtils.addStore(properties, TEST_STORE_NAME_1, TEST_CONN_STRING, KEY_FILTER);
        TestUtils.addStore(properties, TEST_STORE_NAME_2, TEST_CONN_STRING_2, KEY_FILTER);

        loader = new AppConfigDataLoader(deferredLogMock);

        try (MockedStatic<StateHolder> stateHolderMock = Mockito.mockStatic(StateHolder.class)) {
            stateHolderMock.when(() -> StateHolder.updateState(Mockito.any())).thenReturn(null);
            ConfigData source = loader.load(contextMock, appConfigDataResource);

            Collection<PropertySource<?>> sources = source.getPropertySources();
            String[] expectedSourceNames = new String[] { KEY_FILTER + TEST_STORE_NAME + "/prod,dev" };
            assertEquals(1, sources.size());
            assertArrayEquals((Object[]) expectedSourceNames, sources.stream().map(PropertySource::getName).toArray());
        }
    }

    @Test
    public void awaitOnError() {
        appConfigDataResource = new AppConfigDataResource(configStore, profilesMock, appPropertiesMock);
        List<ConfigStore> configStores = new ArrayList<>();
        configStores.add(configStore);

        when(appPropertiesMock.getPrekillTime()).thenReturn(5);

        AppConfigurationKeyValueSelector selectedKeys = new AppConfigurationKeyValueSelector()
            .setKeyFilter("/application/");
        List<AppConfigurationKeyValueSelector> selects = new ArrayList<>();
        selects.add(selectedKeys);
        configStore.setSelects(selects);
        configStore.setEnabled(true);
        configStore.setEndpoint("");

        when(clientFactoryMock.getAvailableClients(Mockito.anyString())).thenReturn(Arrays.asList(replicaClientMock));
        when(replicaClientMock.listSettings(Mockito.any())).thenThrow(new NullPointerException(""));
        when(appPropertiesMock.getPrekillTime()).thenReturn(-60);
        when(appPropertiesMock.getStartDate()).thenReturn(Instant.now());

        loader = new AppConfigDataLoader(deferredLogMock);

        assertThrows(RuntimeException.class,
            () -> loader.load(contextMock, appConfigDataResource));
        verify(appPropertiesMock, times(1)).getPrekillTime();
    }

    @Test
    public void storeDisabled() throws ConfigDataResourceNotFoundException, IOException {
        configStore.setEnabled(false);
        appConfigDataResource = new AppConfigDataResource(configStore, profilesMock, appProperties);

        loader = new AppConfigDataLoader(deferredLogMock);
        try (MockedStatic<StateHolder> stateHolderMock = Mockito.mockStatic(StateHolder.class)) {
            stateHolderMock.when(() -> StateHolder.updateState(Mockito.any())).thenReturn(null);
            ConfigData source = loader.load(contextMock, appConfigDataResource);

            Collection<PropertySource<?>> sources = source.getPropertySources();

            assertEquals(0, sources.size());
        }
    }
}
