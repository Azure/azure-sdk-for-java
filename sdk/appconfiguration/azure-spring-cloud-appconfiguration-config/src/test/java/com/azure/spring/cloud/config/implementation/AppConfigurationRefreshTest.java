// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config.implementation;

import static com.azure.spring.cloud.config.TestConstants.TEST_CONN_STRING;
import static com.azure.spring.cloud.config.TestConstants.TEST_ETAG;
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
import com.azure.spring.cloud.config.AppConfigurationRefresh;
import com.azure.spring.cloud.config.properties.AppConfigurationProperties;
import com.azure.spring.cloud.config.properties.AppConfigurationProviderProperties;
import com.azure.spring.cloud.config.properties.AppConfigurationStoreMonitoring;
import com.azure.spring.cloud.config.properties.AppConfigurationStoreTrigger;
import com.azure.spring.cloud.config.properties.ConfigStore;
import com.azure.spring.cloud.config.properties.FeatureFlagStore;

import net.jcip.annotations.NotThreadSafe;

@NotThreadSafe
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
    private ClientFactory clientFactoryMock;

    @Mock
    private ConfigurationClientWrapper clientWrapperMock;

    @Mock
    private PagedIterable<ConfigurationSetting> pagedFluxMock;
    
    private AppConfigurationProviderProperties libraryProperties;

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
        properties.setRefreshInterval(null);

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
        
        libraryProperties = new AppConfigurationProviderProperties();
        libraryProperties.setDefaultMinBackoff((long) 0);
        
        configRefresh = new AppConfigurationPullRefresh(properties, libraryProperties, clientFactoryMock);

        StateHolder state = new StateHolder();

        state.setLoadState(TEST_STORE_NAME + testInfo.getDisplayName(), true);
        state.setLoadStateFeatureFlag(TEST_STORE_NAME + testInfo.getDisplayName(), true);

        List<ConfigurationSetting> watchKeys = new ArrayList<>();
        watchKeys.add(initialResponse());
        state.setState(TEST_STORE_NAME + testInfo.getDisplayName(), watchKeys, monitoring.getRefreshInterval());
        state.setStateFeatureFlag(TEST_STORE_NAME + testInfo.getDisplayName(), watchKeys,
            monitoring.getFeatureFlagRefreshInterval());

        StateHolder.updateState(state);
    }

    @AfterEach
    public void cleanupMethod(TestInfo testInfo) throws Exception {
        MockitoAnnotations.openMocks(this).close();
        StateHolder.updateState(new StateHolder());
    }

    @Test
    public void nonUpdatedEtagShouldntPublishEvent(TestInfo testInfo) throws Exception {
        List<ConfigurationSetting> watchKeys = new ArrayList<ConfigurationSetting>();
        watchKeys.add(initialResponse());

        StateHolder state = new StateHolder();
        state.setState(TEST_STORE_NAME + testInfo.getDisplayName(), watchKeys, monitoring.getRefreshInterval());
        StateHolder.updateState(state);

        configRefresh.setApplicationEventPublisher(eventPublisher);

        when(clientFactoryMock.getAvailableClients(Mockito.anyString())).thenReturn(Arrays.asList(clientWrapperMock));
        when(clientWrapperMock.getWatchKey(Mockito.anyString(), Mockito.anyString())).thenReturn(initialResponse());

        configRefresh.refreshConfigurations().get();
        verify(eventPublisher, times(0)).publishEvent(any(RefreshEvent.class));
    }

    @Test
    public void updatedEtagShouldPublishEvent(TestInfo testInfo) throws Exception {
        LOGGER.error("=====updatedEtagShouldPublishEvent=====\n"
            + StateHolder.getLoadState(TEST_STORE_NAME + testInfo.getDisplayName()));

        List<ConfigurationSetting> watchKeys = new ArrayList<ConfigurationSetting>();
        watchKeys.add(initialResponse());

        StateHolder state = new StateHolder();
        state.setState(TEST_STORE_NAME + testInfo.getDisplayName(), watchKeys, monitoring.getRefreshInterval());
        StateHolder.updateState(state);

        when(clientFactoryMock.getAvailableClients(Mockito.anyString())).thenReturn(Arrays.asList(clientWrapperMock));
        when(clientWrapperMock.getWatchKey(Mockito.anyString(), Mockito.anyString())).thenReturn(initialResponse());
        configRefresh.setApplicationEventPublisher(eventPublisher);

        // The first time an action happens it can't update
        assertFalse(configRefresh.refreshConfigurations().get());
        verify(eventPublisher, times(0)).publishEvent(any(RefreshEvent.class));

        when(clientFactoryMock.getAvailableClients(Mockito.anyString())).thenReturn(Arrays.asList(clientWrapperMock));
        when(clientWrapperMock.getWatchKey(Mockito.any(), Mockito.any())).thenReturn(updatedResponse());

        // If there is a change it should update
        assertTrue(configRefresh.refreshConfigurations().get());
        verify(eventPublisher, times(1)).publishEvent(any(RefreshEvent.class));

        watchKeys = new ArrayList<>();
        watchKeys.add(updatedResponse());

        state = new StateHolder();

        state.setState(TEST_STORE_NAME + testInfo.getDisplayName(), watchKeys, monitoring.getRefreshInterval());

        HashMap<String, String> map = new HashMap<>();
        map.put("store1_configuration", "fake-etag-updated");
        map.put("store1_feature", "fake-etag-updated");

        ConfigurationSetting updated = new ConfigurationSetting();
        updated.setETag("fake-etag-updated");
        watchKeys = new ArrayList<>();
        watchKeys.add(updated);
        state.setState(TEST_STORE_NAME + testInfo.getDisplayName(), watchKeys, monitoring.getRefreshInterval());

        StateHolder.updateState(state);

        // If there is no change it shouldn't update
        assertFalse(configRefresh.refreshConfigurations().get());
        verify(eventPublisher, times(1)).publishEvent(any(RefreshEvent.class));
    }

    @Test
    public void updatedFeatureFlagEtagShouldPublishEvent(TestInfo testInfo) throws Exception {
        LOGGER
            .error("=====updatedFeatureFlagEtagShouldPublishEvent=====\n"
                + StateHolder.getLoadState(TEST_STORE_NAME + testInfo.getDisplayName()));
        monitoring.setEnabled(false);
        featureFlagStore.setEnabled(true);
        List<ConfigurationSetting> watchKeys = new ArrayList<ConfigurationSetting>();
        watchKeys.add(initialResponse());

        StateHolder state = new StateHolder();

        state.setStateFeatureFlag(TEST_STORE_NAME + testInfo.getDisplayName(), watchKeys,
            monitoring.getRefreshInterval());

        StateHolder.updateState(state);

        when(pagedFluxMock.iterator()).thenReturn(Arrays.asList(initialResponse()).iterator())
            .thenReturn(Arrays.asList(updatedResponse()).iterator());

        when(clientFactoryMock.getAvailableClients(Mockito.anyString())).thenReturn(Arrays.asList(clientWrapperMock));
        when(clientWrapperMock.listSettings(Mockito.any())).thenReturn(pagedFluxMock);
        configRefresh.setApplicationEventPublisher(eventPublisher);

        // The first time an action happens it can't update
        assertFalse(configRefresh.refreshConfigurations().get());
        verify(eventPublisher, times(0)).publishEvent(any(RefreshEvent.class));

        // If there is a change it should update
        assertTrue(configRefresh.refreshConfigurations().get());
        verify(eventPublisher, times(1)).publishEvent(any(RefreshEvent.class));

        watchKeys = new ArrayList<>();
        watchKeys.add(updatedResponse());
        state.setStateFeatureFlag(TEST_STORE_NAME + testInfo.getDisplayName(), watchKeys,
            monitoring.getRefreshInterval());

        HashMap<String, String> map = new HashMap<>();
        map.put("store1_configuration", "fake-etag-updated");
        map.put("store1_feature", "fake-etag-updated");

        ConfigurationSetting updated = new ConfigurationSetting();
        updated.setETag("fake-etag-updated").setKey("fake-key");
        watchKeys = new ArrayList<>();
        watchKeys.add(updated);
        state.setStateFeatureFlag(TEST_STORE_NAME + testInfo.getDisplayName(), watchKeys,
            monitoring.getRefreshInterval());

        StateHolder.updateState(state);

        when(clientFactoryMock.getAvailableClients(Mockito.anyString())).thenReturn(Arrays.asList(clientWrapperMock));
        when(clientWrapperMock.listSettings(Mockito.any())).thenReturn(pagedFluxMock);
        when(pagedFluxMock.iterator()).thenReturn(Arrays.asList(updatedResponse()).iterator());

        // If there is no change it shouldn't update
        assertFalse(configRefresh.refreshConfigurations().get());
        verify(eventPublisher, times(1)).publishEvent(any(RefreshEvent.class));
    }

    @Test
    public void noEtagReturned(TestInfo testInfo) throws Exception {
        List<ConfigurationSetting> watchKeys = new ArrayList<ConfigurationSetting>();
        watchKeys.add(initialResponse());
        StateHolder state = new StateHolder();
        state.setState(TEST_STORE_NAME + testInfo.getDisplayName(), watchKeys, monitoring.getRefreshInterval());
        StateHolder.updateState(state);

        when(clientFactoryMock.getAvailableClients(Mockito.anyString())).thenReturn(Arrays.asList(clientWrapperMock));
        when(clientWrapperMock.getWatchKey(Mockito.anyString(), Mockito.anyString())).thenReturn(null);
        configRefresh.setApplicationEventPublisher(eventPublisher);

        // The first time an action happens it can't update
        assertFalse(configRefresh.refreshConfigurations().get());
        verify(eventPublisher, times(0)).publishEvent(any(RefreshEvent.class));
    }

    @Test
    public void watchKeyThrowError(TestInfo testInfo) throws Exception {
        LOGGER.error(
            "=====watchKeyThrowError=====\n" + StateHolder.getLoadState(TEST_STORE_NAME + testInfo.getDisplayName()));
        when(clientFactoryMock.getAvailableClients(Mockito.anyString())).thenReturn(Arrays.asList(clientWrapperMock));
        when(clientWrapperMock.getWatchKey(Mockito.anyString(), Mockito.anyString())).thenThrow(new RuntimeException(
            "This would be an IO Exception. An existing connection was forcibly closed by the remote host. Test"));
        configRefresh.setApplicationEventPublisher(eventPublisher);

        // The first time an action happens it can't update
        Boolean sawError = false;
        try {
            assertFalse(configRefresh.refreshConfigurations().get());
        } catch (RuntimeException e) {
            sawError = true;
            verify(eventPublisher, times(0)).publishEvent(any(RefreshEvent.class));
        }

        assertTrue(sawError);
    }

    @Test
    public void nullItemsReturned(TestInfo testInfo) throws Exception {
        List<ConfigurationSetting> watchKeys = new ArrayList<ConfigurationSetting>();
        watchKeys.add(initialResponse());
        StateHolder state = new StateHolder();
        state.setState(TEST_STORE_NAME + testInfo.getDisplayName(), watchKeys, monitoring.getRefreshInterval());
        StateHolder.updateState(state);
        when(clientFactoryMock.getAvailableClients(Mockito.anyString())).thenReturn(Arrays.asList(clientWrapperMock));
        when(clientWrapperMock.getWatchKey(Mockito.anyString(), Mockito.anyString())).thenReturn(null);
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

        AppConfigurationRefresh configRefreshLost = new AppConfigurationPullRefresh(propertiesLost, libraryProperties,
            clientFactoryMock);
        when(clientFactoryMock.getAvailableClients(Mockito.anyString())).thenReturn(Arrays.asList(clientWrapperMock));
        when(clientWrapperMock.getWatchKey(Mockito.anyString(), Mockito.anyString())).thenReturn(null);
        configRefreshLost.setApplicationEventPublisher(eventPublisher);

        // The first time an action happens it can't update
        assertFalse(configRefreshLost.refreshConfigurations().get());
        verify(eventPublisher, times(0)).publishEvent(any(RefreshEvent.class));
    }

    @Test
    public void notRefreshTime(TestInfo testInfo) throws Exception {
        List<ConfigurationSetting> watchKeys = new ArrayList<ConfigurationSetting>();
        watchKeys.add(initialResponse());
        StateHolder state = new StateHolder();
        state.setState(TEST_STORE_NAME + testInfo.getDisplayName(), watchKeys, monitoring.getRefreshInterval());
        StateHolder.updateState(state);

        ConfigStore store = new ConfigStore();
        store.setEndpoint(TEST_STORE_NAME + testInfo.getDisplayName());
        store.setConnectionString(TEST_CONN_STRING);

        monitoring.setRefreshInterval(Duration.ofMinutes(60));

        store.setMonitoring(monitoring);

        AppConfigurationProperties properties = new AppConfigurationProperties();
        properties.setStores(Arrays.asList(store));

        AppConfigurationRefresh watchLargeDelay = new AppConfigurationPullRefresh(properties, libraryProperties, clientFactoryMock);

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

        AppConfigurationRefresh refresh = new AppConfigurationPullRefresh(properties, libraryProperties, clientFactoryMock);

        refresh.setApplicationEventPublisher(eventPublisher);
        refresh.refreshConfigurations().get();

        // The first time an action happens it can update
        verify(eventPublisher, times(0)).publishEvent(any(RefreshEvent.class));
    }

    private ConfigurationSetting initialResponse() {
        return new ConfigurationSetting().setETag("fake-etag").setKey("fake-key").setLabel("\0");
    }

    private ConfigurationSetting updatedResponse() {
        return new ConfigurationSetting().setETag("fake-etag-updated").setKey("fake-key").setLabel("\0");
    }

}
