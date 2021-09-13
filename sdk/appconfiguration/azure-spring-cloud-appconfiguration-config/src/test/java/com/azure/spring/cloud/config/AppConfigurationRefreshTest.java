// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config;

import static com.azure.spring.cloud.config.TestConstants.TEST_CONN_STRING;
import static com.azure.spring.cloud.config.TestConstants.TEST_ETAG;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.endpoint.event.RefreshEvent;
import org.springframework.context.ApplicationEventPublisher;

import com.azure.core.http.rest.PagedIterable;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.spring.cloud.config.health.AppConfigurationStoreHealth;
import com.azure.spring.cloud.config.properties.AppConfigurationProperties;
import com.azure.spring.cloud.config.properties.AppConfigurationStoreMonitoring;
import com.azure.spring.cloud.config.properties.AppConfigurationStoreTrigger;
import com.azure.spring.cloud.config.properties.ConfigStore;
import com.azure.spring.cloud.config.properties.FeatureFlagStore;
import com.azure.spring.cloud.config.stores.ClientStore;

public class AppConfigurationRefreshTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppConfigurationRefreshTest.class);

    private static final String TEST_STORE_NAME = "store1Refresh";

    private static final String WATCHED_KEYS = "/application/*";

    AppConfigurationRefresh configRefresh;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private AppConfigurationProperties properties;

    private ArrayList<ConfigurationSetting> keys;

    @Mock
    private Map<String, List<String>> contextsMap;

    private AppConfigurationStoreTrigger trigger;

    private AppConfigurationStoreMonitoring monitoring;

    private FeatureFlagStore featureFlagStore;

    @Mock
    private Date date;

    @Mock
    private ClientStore clientStoreMock;

    @Mock
    private PagedIterable<ConfigurationSetting> pagedFluxMock;

    @BeforeEach
    public void setup(TestInfo testInfo) {
        MockitoAnnotations.openMocks(this);

        ConfigStore store = new ConfigStore();
        store.setEndpoint(TEST_STORE_NAME + testInfo.getDisplayName());
        store.setConnectionString(TEST_CONN_STRING);
        store.setEnabled(true);

        monitoring = new AppConfigurationStoreMonitoring();
        trigger = new AppConfigurationStoreTrigger();
        trigger.setKey(WATCHED_KEYS);
        trigger.setLabel("\0");
        List<AppConfigurationStoreTrigger> triggers = new ArrayList<>();
        triggers.add(trigger);
        monitoring.setTriggers(triggers);
        monitoring.setRefreshInterval(Duration.ofMinutes(-60));
        monitoring.setFeatureFlagRefreshInterval(Duration.ofMinutes(-60));
        monitoring.setEnabled(true);
        store.setMonitoring(monitoring);

        featureFlagStore = new FeatureFlagStore();
        store.setFeatureFlags(featureFlagStore);

        properties = new AppConfigurationProperties();
        properties.setStores(Arrays.asList(store));

        contextsMap = new ConcurrentHashMap<>();
        contextsMap.put(TEST_STORE_NAME + testInfo.getDisplayName(), Arrays.asList(TEST_ETAG));
        keys = new ArrayList<ConfigurationSetting>();
        ConfigurationSetting kvi = new ConfigurationSetting();
        kvi.setKey("fake-etag/application/test.key");
        kvi.setValue("TestValue");
        keys.add(kvi);

        ConfigurationSetting item = new ConfigurationSetting();
        item.setKey("fake-etag/application/test.key");
        item.setETag("fake-etag");
        configRefresh = new AppConfigurationRefresh(properties, clientStoreMock, testInfo.getDisplayName());
        StateHolder.setLoadState(TEST_STORE_NAME + testInfo.getDisplayName(), true);
        StateHolder.setLoadStateFeatureFlag(TEST_STORE_NAME + testInfo.getDisplayName(), true);

        List<ConfigurationSetting> watchKeys = new ArrayList<>();
        watchKeys.add(initialResponse());
        StateHolder.setState(TEST_STORE_NAME + testInfo.getDisplayName(), watchKeys, monitoring.getRefreshInterval());
        StateHolder.setStateFeatureFlag(TEST_STORE_NAME + testInfo.getDisplayName(), watchKeys, monitoring.getFeatureFlagRefreshInterval());
    }

    @AfterEach
    public void cleanupMethod(TestInfo testInfo) throws Exception {
        MockitoAnnotations.openMocks(this).close();
        StateHolder.setState(TEST_STORE_NAME + testInfo.getDisplayName(), new ArrayList<>(), monitoring.getRefreshInterval());
        StateHolder.setStateFeatureFlag(TEST_STORE_NAME + testInfo.getDisplayName(), new ArrayList<>(), monitoring.getFeatureFlagRefreshInterval());
    }

    @Test
    public void nonUpdatedEtagShouldntPublishEvent(TestInfo testInfo) throws Exception {
        List<ConfigurationSetting> watchKeys = new ArrayList<ConfigurationSetting>();
        watchKeys.add(initialResponse());
        StateHolder.setState(TEST_STORE_NAME + testInfo.getDisplayName(), watchKeys, monitoring.getRefreshInterval());

        configRefresh.setApplicationEventPublisher(eventPublisher);

        when(clientStoreMock.getWatchKey(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
            .thenReturn(initialResponse());

        configRefresh.refreshConfigurations().get();
        verify(eventPublisher, times(0)).publishEvent(any(RefreshEvent.class));
        assertEquals(AppConfigurationStoreHealth.UP,
            configRefresh.getAppConfigurationStoresHealth().get(TEST_STORE_NAME + testInfo.getDisplayName()));
    }

    @Test
    public void updatedEtagShouldPublishEvent(TestInfo testInfo) throws Exception {
        LOGGER.error("=====updatedEtagShouldPublishEvent=====\n" + StateHolder.getLoadState(TEST_STORE_NAME + testInfo.getDisplayName()));

        List<ConfigurationSetting> watchKeys = new ArrayList<ConfigurationSetting>();
        watchKeys.add(initialResponse());
        StateHolder.setState(TEST_STORE_NAME + testInfo.getDisplayName(), watchKeys, monitoring.getRefreshInterval());

        when(clientStoreMock.getWatchKey(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
            .thenReturn(initialResponse());
        configRefresh.setApplicationEventPublisher(eventPublisher);

        // The first time an action happens it can't update
        assertFalse(configRefresh.refreshConfigurations().get());
        verify(eventPublisher, times(0)).publishEvent(any(RefreshEvent.class));

        when(clientStoreMock.getWatchKey(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(updatedResponse());

        // If there is a change it should update
        assertTrue(configRefresh.refreshConfigurations().get());
        verify(eventPublisher, times(1)).publishEvent(any(RefreshEvent.class));

        watchKeys = new ArrayList<>();
        watchKeys.add(updatedResponse());
        StateHolder.setState(TEST_STORE_NAME + testInfo.getDisplayName(), watchKeys, monitoring.getRefreshInterval());

        HashMap<String, String> map = new HashMap<>();
        map.put("store1_configuration", "fake-etag-updated");
        map.put("store1_feature", "fake-etag-updated");

        ConfigurationSetting updated = new ConfigurationSetting();
        updated.setETag("fake-etag-updated");
        watchKeys = new ArrayList<>();
        watchKeys.add(updated);
        StateHolder.setState(TEST_STORE_NAME + testInfo.getDisplayName(), watchKeys, monitoring.getRefreshInterval());

        // If there is no change it shouldn't update
        assertFalse(configRefresh.refreshConfigurations().get());
        verify(eventPublisher, times(1)).publishEvent(any(RefreshEvent.class));
    }

    @Test
    public void updatedFeatureFlagEtagShouldPublishEvent(TestInfo testInfo) throws Exception {
        LOGGER
            .error("=====updatedFeatureFlagEtagShouldPublishEvent=====\n" + StateHolder.getLoadState(TEST_STORE_NAME + testInfo.getDisplayName()));
        monitoring.setEnabled(false);
        featureFlagStore.setEnabled(true);
        List<ConfigurationSetting> watchKeys = new ArrayList<ConfigurationSetting>();
        watchKeys.add(initialResponse());
        StateHolder.setStateFeatureFlag(TEST_STORE_NAME + testInfo.getDisplayName(), watchKeys, monitoring.getRefreshInterval());

        when(pagedFluxMock.iterator()).thenReturn(Arrays.asList(initialResponse()).iterator())
            .thenReturn(Arrays.asList(updatedResponse()).iterator());

        when(clientStoreMock.getFeatureFlagWatchKey(Mockito.any(), Mockito.anyString())).thenReturn(pagedFluxMock);
        configRefresh.setApplicationEventPublisher(eventPublisher);

        // The first time an action happens it can't update
        assertFalse(configRefresh.refreshConfigurations().get());
        verify(eventPublisher, times(0)).publishEvent(any(RefreshEvent.class));

        // If there is a change it should update
        assertTrue(configRefresh.refreshConfigurations().get());
        verify(eventPublisher, times(1)).publishEvent(any(RefreshEvent.class));

        watchKeys = new ArrayList<>();
        watchKeys.add(updatedResponse());
        StateHolder.setStateFeatureFlag(TEST_STORE_NAME + testInfo.getDisplayName(), watchKeys, monitoring.getRefreshInterval());

        HashMap<String, String> map = new HashMap<>();
        map.put("store1_configuration", "fake-etag-updated");
        map.put("store1_feature", "fake-etag-updated");

        ConfigurationSetting updated = new ConfigurationSetting();
        updated.setETag("fake-etag-updated").setKey("fake-key");
        watchKeys = new ArrayList<>();
        watchKeys.add(updated);
        StateHolder.setStateFeatureFlag(TEST_STORE_NAME + testInfo.getDisplayName(), watchKeys, monitoring.getRefreshInterval());

        when(clientStoreMock.getFeatureFlagWatchKey(Mockito.any(), Mockito.anyString())).thenReturn(pagedFluxMock);
        when(pagedFluxMock.iterator()).thenReturn(Arrays.asList(updatedResponse()).iterator());

        // If there is no change it shouldn't update
        assertFalse(configRefresh.refreshConfigurations().get());
        verify(eventPublisher, times(1)).publishEvent(any(RefreshEvent.class));
    }

    @Test
    public void noEtagReturned(TestInfo testInfo) throws Exception {
        List<ConfigurationSetting> watchKeys = new ArrayList<ConfigurationSetting>();
        watchKeys.add(initialResponse());
        StateHolder.setState(TEST_STORE_NAME + testInfo.getDisplayName(), watchKeys, monitoring.getRefreshInterval());

        when(clientStoreMock.getWatchKey(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
            .thenReturn(null);
        configRefresh.setApplicationEventPublisher(eventPublisher);

        // The first time an action happens it can't update
        assertFalse(configRefresh.refreshConfigurations().get());
        verify(eventPublisher, times(0)).publishEvent(any(RefreshEvent.class));
    }

    @Test
    public void watchKeyThrowError(TestInfo testInfo) throws Exception {
        LOGGER.error("=====watchKeyThrowError=====\n" + StateHolder.getLoadState(TEST_STORE_NAME + testInfo.getDisplayName()));
        when(clientStoreMock.getWatchKey(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
            .thenThrow(new RuntimeException(
                "This would be an IO Exception. An existing connection was forcibly closed by the remote host. Test"));
        configRefresh.setApplicationEventPublisher(eventPublisher);

        // The first time an action happens it can't update
        Boolean sawError = false;
        try {
            assertFalse(configRefresh.refreshConfigurations().get());
        } catch (RuntimeException e) {
            sawError = true;
            verify(eventPublisher, times(0)).publishEvent(any(RefreshEvent.class));
            assertEquals(AppConfigurationStoreHealth.DOWN,
                configRefresh.getAppConfigurationStoresHealth().get(TEST_STORE_NAME + testInfo.getDisplayName()));
        }

        assertTrue(sawError);
    }

    @Test
    public void nullItemsReturned(TestInfo testInfo) throws Exception {
        List<ConfigurationSetting> watchKeys = new ArrayList<ConfigurationSetting>();
        watchKeys.add(initialResponse());
        StateHolder.setState(TEST_STORE_NAME + testInfo.getDisplayName(), watchKeys, monitoring.getRefreshInterval());

        when(clientStoreMock.getWatchKey(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
            .thenReturn(null);
        configRefresh.setApplicationEventPublisher(eventPublisher);

        // The first time an action happens it can't update
        assertFalse(configRefresh.refreshConfigurations().get());
        verify(eventPublisher, times(0)).publishEvent(any(RefreshEvent.class));
    }

    @Test
    public void noInitialStateNoEtag(TestInfo testInfo) throws Exception {
        ConfigStore store = new ConfigStore();
        store.setEndpoint(TEST_STORE_NAME + testInfo.getDisplayName());
        store.setConnectionString(TEST_CONN_STRING);

        store.setMonitoring(monitoring);

        AppConfigurationProperties propertiesLost = new AppConfigurationProperties();
        propertiesLost.setStores(Arrays.asList(store));

        AppConfigurationRefresh configRefreshLost = new AppConfigurationRefresh(propertiesLost,
            clientStoreMock, "noInitialStateNoEtag");
        when(clientStoreMock.getWatchKey(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
            .thenReturn(null);
        configRefreshLost.setApplicationEventPublisher(eventPublisher);

        // The first time an action happens it can't update
        assertFalse(configRefreshLost.refreshConfigurations().get());
        verify(eventPublisher, times(0)).publishEvent(any(RefreshEvent.class));
    }

    @Test
    public void notRefreshTime(TestInfo testInfo) throws Exception {
        List<ConfigurationSetting> watchKeys = new ArrayList<ConfigurationSetting>();
        watchKeys.add(initialResponse());
        StateHolder.setState(TEST_STORE_NAME + testInfo.getDisplayName(), watchKeys, monitoring.getRefreshInterval());

        ConfigStore store = new ConfigStore();
        store.setEndpoint(TEST_STORE_NAME + testInfo.getDisplayName());
        store.setConnectionString(TEST_CONN_STRING);

        monitoring.setRefreshInterval(Duration.ofMinutes(60));

        store.setMonitoring(monitoring);

        AppConfigurationProperties properties = new AppConfigurationProperties();
        properties.setStores(Arrays.asList(store));

        AppConfigurationRefresh watchLargeDelay = new AppConfigurationRefresh(properties, clientStoreMock, "notRefreshTime");

        watchLargeDelay.setApplicationEventPublisher(eventPublisher);
        watchLargeDelay.refreshConfigurations().get();

        // The first time an action happens it can update
        verify(eventPublisher, times(0)).publishEvent(any(RefreshEvent.class));
    }

    @Test
    public void storeDisabled(TestInfo testInfo) throws Exception {
        ConfigStore store = new ConfigStore();
        store.setEndpoint(TEST_STORE_NAME + testInfo.getDisplayName());
        store.setConnectionString(TEST_CONN_STRING);
        store.setEnabled(false);

        monitoring.setRefreshInterval(Duration.ofMinutes(60));

        store.setMonitoring(monitoring);

        AppConfigurationProperties properties = new AppConfigurationProperties();
        properties.setStores(Arrays.asList(store));

        AppConfigurationRefresh refresh = new AppConfigurationRefresh(properties, clientStoreMock, "storeDisabled");

        refresh.setApplicationEventPublisher(eventPublisher);
        refresh.refreshConfigurations().get();

        // The first time an action happens it can update
        verify(eventPublisher, times(0)).publishEvent(any(RefreshEvent.class));
        assertEquals(AppConfigurationStoreHealth.NOT_LOADED,
            refresh.getAppConfigurationStoresHealth().get(TEST_STORE_NAME + testInfo.getDisplayName()));
    }

    private ConfigurationSetting initialResponse() {
        return new ConfigurationSetting().setETag("fake-etag").setKey("fake-key").setLabel("\0");
    }

    private ConfigurationSetting updatedResponse() {
        return new ConfigurationSetting().setETag("fake-etag-updated").setKey("fake-key").setLabel("\0");
    }

}
