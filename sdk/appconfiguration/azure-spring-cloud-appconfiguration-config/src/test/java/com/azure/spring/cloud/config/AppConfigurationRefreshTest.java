// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config;

import static com.azure.spring.cloud.config.TestConstants.TEST_CONN_STRING;
import static com.azure.spring.cloud.config.TestConstants.TEST_ETAG;
import static com.azure.spring.cloud.config.TestConstants.TEST_STORE_NAME;
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
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.cloud.endpoint.event.RefreshEvent;
import org.springframework.context.ApplicationEventPublisher;

import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.spring.cloud.config.health.AppConfigurationStoreHealth;
import com.azure.spring.cloud.config.properties.AppConfigurationProperties;
import com.azure.spring.cloud.config.properties.AppConfigurationStoreMonitoring;
import com.azure.spring.cloud.config.properties.AppConfigurationStoreTrigger;
import com.azure.spring.cloud.config.properties.ConfigStore;
import com.azure.spring.cloud.config.properties.FeatureFlagStore;
import com.azure.spring.cloud.config.stores.ClientStore;

@TestMethodOrder(MethodOrderer.MethodName.class)
public class AppConfigurationRefreshTest {

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

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        ConfigStore store = new ConfigStore();
        store.setEndpoint(TEST_STORE_NAME);
        store.setConnectionString(TEST_CONN_STRING);

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
        contextsMap.put(TEST_STORE_NAME, Arrays.asList(TEST_ETAG));
        keys = new ArrayList<ConfigurationSetting>();
        ConfigurationSetting kvi = new ConfigurationSetting();
        kvi.setKey("fake-etag/application/test.key");
        kvi.setValue("TestValue");
        keys.add(kvi);

        ConfigurationSetting item = new ConfigurationSetting();
        item.setKey("fake-etag/application/test.key");
        item.setETag("fake-etag");
        configRefresh = new AppConfigurationRefresh(properties, clientStoreMock);
        StateHolder.setLoadState(TEST_STORE_NAME, true);
        StateHolder.setLoadStateFeatureFlag(TEST_STORE_NAME, true);
        
        List<ConfigurationSetting> watchKeys = new ArrayList<>();
        watchKeys.add(initialResponse());
        StateHolder.setState(TEST_STORE_NAME, watchKeys, monitoring.getRefreshInterval());
        StateHolder.setStateFeatureFlag(TEST_STORE_NAME, watchKeys, monitoring.getFeatureFlagRefreshInterval());
    }

    @AfterEach
    public void cleanupMethod() throws Exception {
        MockitoAnnotations.openMocks(this).close();
        StateHolder.setState(TEST_STORE_NAME, new ArrayList<>(), monitoring.getRefreshInterval());
        StateHolder.setStateFeatureFlag(TEST_STORE_NAME, new ArrayList<>(), monitoring.getFeatureFlagRefreshInterval());
    }

    @Test
    public void nonUpdatedEtagShouldntPublishEvent() throws Exception {
        List<ConfigurationSetting> watchKeys = new ArrayList<ConfigurationSetting>();
        watchKeys.add(initialResponse());
        StateHolder.setState(TEST_STORE_NAME, watchKeys, monitoring.getRefreshInterval());

        configRefresh.setApplicationEventPublisher(eventPublisher);

        when(clientStoreMock.getWatchKey(Mockito.any(), Mockito.anyString())).thenReturn(initialResponse());

        configRefresh.refreshConfigurations().get();
        verify(eventPublisher, times(0)).publishEvent(any(RefreshEvent.class));
        assertEquals(AppConfigurationStoreHealth.UP, configRefresh.getAppConfigurationStoresHealth().get(TEST_STORE_NAME));
    }

    @Test
    public void updatedEtagShouldPublishEvent() throws Exception {
        List<ConfigurationSetting> watchKeys = new ArrayList<ConfigurationSetting>();
        watchKeys.add(initialResponse());
        StateHolder.setState(TEST_STORE_NAME, watchKeys, monitoring.getRefreshInterval());

        when(clientStoreMock.getWatchKey(Mockito.any(), Mockito.anyString())).thenReturn(initialResponse());
        configRefresh.setApplicationEventPublisher(eventPublisher);

        // The first time an action happens it can't update
        assertFalse(configRefresh.refreshConfigurations().get());
        verify(eventPublisher, times(0)).publishEvent(any(RefreshEvent.class));

        when(clientStoreMock.getWatchKey(Mockito.any(), Mockito.anyString())).thenReturn(updatedResponse());

        // If there is a change it should update
        assertTrue(configRefresh.refreshConfigurations().get());
        verify(eventPublisher, times(1)).publishEvent(any(RefreshEvent.class));

        watchKeys = new ArrayList<>();
        watchKeys.add(updatedResponse());
        StateHolder.setState(TEST_STORE_NAME, watchKeys, monitoring.getRefreshInterval());

        HashMap<String, String> map = new HashMap<>();
        map.put("store1_configuration", "fake-etag-updated");
        map.put("store1_feature", "fake-etag-updated");

        ConfigurationSetting updated = new ConfigurationSetting();
        updated.setETag("fake-etag-updated");
        watchKeys = new ArrayList<>();
        watchKeys.add(updated);
        StateHolder.setState(TEST_STORE_NAME, watchKeys, monitoring.getRefreshInterval());

        // If there is no change it shouldn't update
        assertFalse(configRefresh.refreshConfigurations().get());
        verify(eventPublisher, times(1)).publishEvent(any(RefreshEvent.class));
    }
    
    @Test
    public void updatedFeatureFlagEtagShouldPublishEvent() throws Exception {
        monitoring.setEnabled(false);
        featureFlagStore.setEnabled(true);
        List<ConfigurationSetting> watchKeys = new ArrayList<ConfigurationSetting>();
        watchKeys.add(initialResponse());
        StateHolder.setStateFeatureFlag(TEST_STORE_NAME, watchKeys, monitoring.getRefreshInterval());

        when(clientStoreMock.getWatchKey(Mockito.any(), Mockito.anyString())).thenReturn(initialResponse());
        configRefresh.setApplicationEventPublisher(eventPublisher);

        // The first time an action happens it can't update
        assertFalse(configRefresh.refreshConfigurations().get());
        verify(eventPublisher, times(0)).publishEvent(any(RefreshEvent.class));

        when(clientStoreMock.getWatchKey(Mockito.any(), Mockito.anyString())).thenReturn(updatedResponse());

        // If there is a change it should update
        assertTrue(configRefresh.refreshConfigurations().get());
        verify(eventPublisher, times(1)).publishEvent(any(RefreshEvent.class));

        watchKeys = new ArrayList<>();
        watchKeys.add(updatedResponse());
        StateHolder.setStateFeatureFlag(TEST_STORE_NAME, watchKeys, monitoring.getRefreshInterval());

        HashMap<String, String> map = new HashMap<>();
        map.put("store1_configuration", "fake-etag-updated");
        map.put("store1_feature", "fake-etag-updated");

        ConfigurationSetting updated = new ConfigurationSetting();
        updated.setETag("fake-etag-updated");
        watchKeys = new ArrayList<>();
        watchKeys.add(updated);
        StateHolder.setStateFeatureFlag(TEST_STORE_NAME, watchKeys, monitoring.getRefreshInterval());

        // If there is no change it shouldn't update
        assertFalse(configRefresh.refreshConfigurations().get());
        verify(eventPublisher, times(1)).publishEvent(any(RefreshEvent.class));
    }

    @Test
    public void noEtagReturned() throws Exception {
        List<ConfigurationSetting> watchKeys = new ArrayList<ConfigurationSetting>();
        watchKeys.add(initialResponse());
        StateHolder.setState(TEST_STORE_NAME, watchKeys, monitoring.getRefreshInterval());

        when(clientStoreMock.getWatchKey(Mockito.any(), Mockito.anyString()))
            .thenReturn(null);
        configRefresh.setApplicationEventPublisher(eventPublisher);

        // The first time an action happens it can't update
        assertFalse(configRefresh.refreshConfigurations().get());
        verify(eventPublisher, times(0)).publishEvent(any(RefreshEvent.class));
    }
    
    @Test
    public void watchKeyThrowError() throws Exception {
        when(clientStoreMock.getWatchKey(Mockito.any(), Mockito.anyString()))
            .thenThrow(new RuntimeException("This would be an IO Exception. An existing connection was forcibly closed by the remote host. Test"));
        configRefresh.setApplicationEventPublisher(eventPublisher);

        // The first time an action happens it can't update
        Boolean sawError = false;
        try {
            assertFalse(configRefresh.refreshConfigurations().get());
        } catch (RuntimeException e) {
            sawError = true;
            verify(eventPublisher, times(0)).publishEvent(any(RefreshEvent.class));
            assertEquals(AppConfigurationStoreHealth.DOWN, configRefresh.getAppConfigurationStoresHealth().get(TEST_STORE_NAME));
        }
        
        assertTrue(sawError);
    }

    @Test
    public void nullItemsReturned() throws Exception {
        List<ConfigurationSetting> watchKeys = new ArrayList<ConfigurationSetting>();
        watchKeys.add(initialResponse());
        StateHolder.setState(TEST_STORE_NAME, watchKeys, monitoring.getRefreshInterval());

        when(clientStoreMock.getWatchKey(Mockito.any(), Mockito.anyString())).thenReturn(null);
        configRefresh.setApplicationEventPublisher(eventPublisher);

        // The first time an action happens it can't update
        assertFalse(configRefresh.refreshConfigurations().get());
        verify(eventPublisher, times(0)).publishEvent(any(RefreshEvent.class));
    }

    @Test
    public void noInitialStateNoEtag() throws Exception {
        ConfigStore store = new ConfigStore();
        store.setEndpoint(TEST_STORE_NAME);
        store.setConnectionString(TEST_CONN_STRING);

        AppConfigurationStoreMonitoring monitoring = new AppConfigurationStoreMonitoring();
        monitoring.setEnabled(true);
        trigger = new AppConfigurationStoreTrigger();
        trigger.setKey(WATCHED_KEYS);
        trigger.setLabel("\0");
        List<AppConfigurationStoreTrigger> triggers = new ArrayList<>();
        triggers.add(trigger);
        monitoring.setTriggers(triggers);
        monitoring.setRefreshInterval(Duration.ofMinutes(-60));
        store.setMonitoring(monitoring);

        AppConfigurationProperties propertiesLost = new AppConfigurationProperties();
        propertiesLost.setStores(Arrays.asList(store));

        AppConfigurationRefresh configRefreshLost = new AppConfigurationRefresh(propertiesLost,
            clientStoreMock);
        StateHolder.setLoadState(TEST_STORE_NAME, true);
        when(clientStoreMock.getWatchKey(Mockito.any(), Mockito.anyString())).thenReturn(null);
        configRefreshLost.setApplicationEventPublisher(eventPublisher);

        // The first time an action happens it can't update
        assertFalse(configRefreshLost.refreshConfigurations().get());
        verify(eventPublisher, times(0)).publishEvent(any(RefreshEvent.class));
    }

    @Test
    public void notRefreshTime() throws Exception {
        List<ConfigurationSetting> watchKeys = new ArrayList<ConfigurationSetting>();
        watchKeys.add(initialResponse());
        StateHolder.setState(TEST_STORE_NAME, watchKeys, monitoring.getRefreshInterval());

        ConfigStore store = new ConfigStore();
        store.setEndpoint(TEST_STORE_NAME);
        store.setConnectionString(TEST_CONN_STRING);

        AppConfigurationStoreMonitoring monitoring = new AppConfigurationStoreMonitoring();
        monitoring.setEnabled(true);
        trigger = new AppConfigurationStoreTrigger();
        trigger.setKey(WATCHED_KEYS);
        trigger.setLabel("\0");
        List<AppConfigurationStoreTrigger> triggers = new ArrayList<>();
        triggers.add(trigger);
        monitoring.setTriggers(triggers);
        monitoring.setRefreshInterval(Duration.ofMinutes(60));
        store.setMonitoring(monitoring);

        AppConfigurationProperties properties = new AppConfigurationProperties();
        properties.setStores(Arrays.asList(store));

        AppConfigurationRefresh watchLargeDelay = new AppConfigurationRefresh(properties, clientStoreMock);

        watchLargeDelay.setApplicationEventPublisher(eventPublisher);
        watchLargeDelay.refreshConfigurations().get();

        // The first time an action happens it can update
        verify(eventPublisher, times(0)).publishEvent(any(RefreshEvent.class));
    }
    
    @Test
    public void storeDisabled() throws Exception {
        ConfigStore store = new ConfigStore();
        store.setEndpoint(TEST_STORE_NAME);
        store.setConnectionString(TEST_CONN_STRING);
        store.setEnabled(false);

        AppConfigurationStoreMonitoring monitoring = new AppConfigurationStoreMonitoring();
        monitoring.setEnabled(true);
        trigger = new AppConfigurationStoreTrigger();
        trigger.setKey(WATCHED_KEYS);
        trigger.setLabel("\0");
        List<AppConfigurationStoreTrigger> triggers = new ArrayList<>();
        triggers.add(trigger);
        monitoring.setTriggers(triggers);
        monitoring.setRefreshInterval(Duration.ofMinutes(60));
        store.setMonitoring(monitoring);

        AppConfigurationProperties properties = new AppConfigurationProperties();
        properties.setStores(Arrays.asList(store));

        AppConfigurationRefresh refresh = new AppConfigurationRefresh(properties, clientStoreMock);

        refresh.setApplicationEventPublisher(eventPublisher);
        refresh.refreshConfigurations().get();

        // The first time an action happens it can update
        verify(eventPublisher, times(0)).publishEvent(any(RefreshEvent.class));
        assertEquals(AppConfigurationStoreHealth.NOT_LOADED, refresh.getAppConfigurationStoresHealth().get(TEST_STORE_NAME));
    }

    private ConfigurationSetting initialResponse() {
        return new ConfigurationSetting().setETag("fake-etag");
    }

    private ConfigurationSetting updatedResponse() {
        return new ConfigurationSetting().setETag("fake-etag-updated");
    }

}
