/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config;

import static com.microsoft.azure.spring.cloud.config.TestConstants.TEST_CONN_STRING;
import static com.microsoft.azure.spring.cloud.config.TestConstants.TEST_ETAG;
import static com.microsoft.azure.spring.cloud.config.TestConstants.TEST_STORE_NAME;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.cloud.endpoint.event.RefreshEvent;
import org.springframework.context.ApplicationEventPublisher;

import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.microsoft.azure.spring.cloud.config.properties.AppConfigurationProperties;
import com.microsoft.azure.spring.cloud.config.properties.AppConfigurationStoreMonitoring;
import com.microsoft.azure.spring.cloud.config.properties.AppConfigurationStoreTrigger;
import com.microsoft.azure.spring.cloud.config.properties.ConfigStore;
import com.microsoft.azure.spring.cloud.config.stores.ClientStore;

public class AppConfigurationRefreshTest {

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private AppConfigurationProperties properties;

    private ArrayList<ConfigurationSetting> keys;

    @Mock
    private Map<String, List<String>> contextsMap;

    AppConfigurationRefresh configRefresh;

    private AppConfigurationStoreTrigger trigger;

    private AppConfigurationStoreMonitoring monitoring;

    @Mock
    private Date date;

    @Mock
    private ClientStore clientStoreMock;

    private static final String WATCHED_KEYS = "/application/*";

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        ConfigStore store = new ConfigStore();
        store.setEndpoint(TEST_STORE_NAME);
        store.setConnectionString(TEST_CONN_STRING);

        monitoring = new AppConfigurationStoreMonitoring();
        trigger = new AppConfigurationStoreTrigger();
        trigger.setKey(WATCHED_KEYS);
        trigger.setLabel("\0");
        List<AppConfigurationStoreTrigger> triggers = new ArrayList<AppConfigurationStoreTrigger>();
        triggers.add(trigger);
        monitoring.setTriggers(triggers);
        monitoring.setCacheExpiration(Duration.ofMinutes(-60));
        monitoring.setEnabled(true);
        store.setMonitoring(monitoring);

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
    }

    @After
    public void cleanupMethod() {
        StateHolder.setState(TEST_STORE_NAME, new ArrayList<ConfigurationSetting>(), monitoring);
    }

    @Test
    public void nonUpdatedEtagShouldntPublishEvent() throws Exception {
        List<ConfigurationSetting> watchKeys = new ArrayList<ConfigurationSetting>();
        watchKeys.add(initialResponse());
        StateHolder.setState(TEST_STORE_NAME, watchKeys, monitoring);

        configRefresh.setApplicationEventPublisher(eventPublisher);

        when(clientStoreMock.getRevison(Mockito.any(), Mockito.anyString())).thenReturn(initialResponse());

        configRefresh.refreshConfigurations().get();
        verify(eventPublisher, times(0)).publishEvent(any(RefreshEvent.class));
    }

    @Test
    public void updatedEtagShouldPublishEvent() throws Exception {
        List<ConfigurationSetting> watchKeys = new ArrayList<ConfigurationSetting>();
        watchKeys.add(initialResponse());
        StateHolder.setState(TEST_STORE_NAME, watchKeys, monitoring);

        when(clientStoreMock.getRevison(Mockito.any(), Mockito.anyString())).thenReturn(initialResponse());
        configRefresh.setApplicationEventPublisher(eventPublisher);

        // The first time an action happens it can't update
        assertFalse(configRefresh.refreshConfigurations().get());
        verify(eventPublisher, times(0)).publishEvent(any(RefreshEvent.class));

        when(clientStoreMock.getRevison(Mockito.any(), Mockito.anyString())).thenReturn(updatedResponse());

        // If there is a change it should update
        assertTrue(configRefresh.refreshConfigurations().get());
        verify(eventPublisher, times(1)).publishEvent(any(RefreshEvent.class));

        watchKeys = new ArrayList<ConfigurationSetting>();
        watchKeys.add(updatedResponse());
        StateHolder.setState(TEST_STORE_NAME, watchKeys, monitoring);

        HashMap<String, String> map = new HashMap<String, String>();
        map.put("store1_configuration", "fake-etag-updated");
        map.put("store1_feature", "fake-etag-updated");

        ConfigurationSetting updated = new ConfigurationSetting();
        updated.setETag("fake-etag-updated");
        watchKeys = new ArrayList<ConfigurationSetting>();
        watchKeys.add(updated);
        StateHolder.setState(TEST_STORE_NAME, watchKeys, monitoring);

        // If there is no change it shouldn't update
        assertFalse(configRefresh.refreshConfigurations().get());
        verify(eventPublisher, times(1)).publishEvent(any(RefreshEvent.class));
    }

    @Test
    public void noEtagReturned() throws Exception {
        List<ConfigurationSetting> watchKeys = new ArrayList<ConfigurationSetting>();
        watchKeys.add(initialResponse());
        StateHolder.setState(TEST_STORE_NAME, watchKeys, monitoring);

        when(clientStoreMock.getRevison(Mockito.any(), Mockito.anyString()))
                .thenReturn(null);
        configRefresh.setApplicationEventPublisher(eventPublisher);

        // The first time an action happens it can't update
        assertFalse(configRefresh.refreshConfigurations().get());
        verify(eventPublisher, times(0)).publishEvent(any(RefreshEvent.class));
    }

    @Test
    public void nullItemsReturned() throws Exception {
        List<ConfigurationSetting> watchKeys = new ArrayList<ConfigurationSetting>();
        watchKeys.add(initialResponse());
        StateHolder.setState(TEST_STORE_NAME, watchKeys, monitoring);

        when(clientStoreMock.getRevison(Mockito.any(), Mockito.anyString())).thenReturn(null);
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
        List<AppConfigurationStoreTrigger> triggers = new ArrayList<AppConfigurationStoreTrigger>();
        triggers.add(trigger);
        monitoring.setTriggers(triggers);
        monitoring.setCacheExpiration(Duration.ofMinutes(-60));
        store.setMonitoring(monitoring);

        AppConfigurationProperties propertiesLost = new AppConfigurationProperties();
        propertiesLost.setStores(Arrays.asList(store));

        AppConfigurationRefresh configRefreshLost = new AppConfigurationRefresh(propertiesLost,
                clientStoreMock);
        StateHolder.setLoadState(TEST_STORE_NAME, true);
        when(clientStoreMock.getRevison(Mockito.any(), Mockito.anyString())).thenReturn(null);
        configRefreshLost.setApplicationEventPublisher(eventPublisher);

        // The first time an action happens it can't update
        assertFalse(configRefreshLost.refreshConfigurations().get());
        verify(eventPublisher, times(0)).publishEvent(any(RefreshEvent.class));
    }

    @Test
    public void notRefreshTime() throws Exception {
        List<ConfigurationSetting> watchKeys = new ArrayList<ConfigurationSetting>();
        watchKeys.add(initialResponse());
        StateHolder.setState(TEST_STORE_NAME, watchKeys, monitoring);

        ConfigStore store = new ConfigStore();
        store.setEndpoint(TEST_STORE_NAME);
        store.setConnectionString(TEST_CONN_STRING);

        AppConfigurationStoreMonitoring monitoring = new AppConfigurationStoreMonitoring();
        monitoring.setEnabled(true);
        trigger = new AppConfigurationStoreTrigger();
        trigger.setKey(WATCHED_KEYS);
        trigger.setLabel("\0");
        List<AppConfigurationStoreTrigger> triggers = new ArrayList<AppConfigurationStoreTrigger>();
        triggers.add(trigger);
        monitoring.setTriggers(triggers);
        monitoring.setCacheExpiration(Duration.ofMinutes(60));
        store.setMonitoring(monitoring);

        AppConfigurationProperties properties = new AppConfigurationProperties();
        properties.setStores(Arrays.asList(store));

        AppConfigurationRefresh watchLargeDelay = new AppConfigurationRefresh(properties, clientStoreMock);

        watchLargeDelay.setApplicationEventPublisher(eventPublisher);
        watchLargeDelay.refreshConfigurations().get();

        // The first time an action happens it can update
        verify(eventPublisher, times(0)).publishEvent(any(RefreshEvent.class));
    }

    private ConfigurationSetting initialResponse() {
        return new ConfigurationSetting().setETag("fake-etag");
    }

    private ConfigurationSetting updatedResponse() {
        return new ConfigurationSetting().setETag("fake-etag-updated");
    }

}
