package com.azure.spring.cloud.config.implementation;

import static com.azure.spring.cloud.config.AppConfigurationConstants.EMPTY_LABEL;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.lang.reflect.Array;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.FeatureFlagConfigurationSetting;
import com.azure.data.appconfiguration.models.SettingSelector;
import com.azure.spring.cloud.config.properties.ConfigStore;
import com.azure.spring.cloud.config.properties.FeatureFlagStore;

import reactor.core.publisher.Mono;

public class AppConfigurationRefreshUtilTest {

    @Mock
    private ConfigurationClientWrapper clientMock;

    @Mock
    private ClientFactory clientFactoryMock;
    
    @Mock
    private PagedIterable<ConfigurationSetting> flagsPagedIterableMock;

    private ConfigStore configStore;

    @BeforeEach
    public void setup(TestInfo testInfo) {
        MockitoAnnotations.openMocks(this);

        configStore = new ConfigStore();

        FeatureFlagStore ffStore = new FeatureFlagStore();
        ffStore.setEnabled(true);
        configStore.setFeatureFlags(ffStore);
    }

    @Test
    public void refreshWithoutTimeWatchKey() {
        String endpoint = "refreshWithoutTimeWatchKey.azconfig.io";
        when(clientMock.getEndpoint()).thenReturn(endpoint);

        // Config Store isn't Loaded
        assertFalse(
            AppConfigurationRefreshUtil.checkStoreAfterRefreshFailed(configStore, clientMock, clientFactoryMock));
        StateHolder newState = new StateHolder();
        newState.setLoadState(endpoint, true);

        List<ConfigurationSetting> watchKeys = new ArrayList<>();

        ConfigurationSetting currentWatchKey = new ConfigurationSetting().setKey("/application/*").setLabel("unit")
            .setETag("current");

        watchKeys.add(currentWatchKey);

        newState.setState(endpoint, watchKeys, Duration.ofSeconds(1));
        StateHolder.updateState(newState);

        // Config Store doesn't return a watch key change.
        when(clientMock.getWatchKey(Mockito.eq("/application/*"), Mockito.eq("unit"))).thenReturn(currentWatchKey);
        assertFalse(
            AppConfigurationRefreshUtil.checkStoreAfterRefreshFailed(configStore, clientMock, clientFactoryMock));

        ConfigurationSetting updatedWatchKey = new ConfigurationSetting().setKey("/application/*").setLabel("unit")
            .setETag("updated");

        // Config Store does return a watch key change.
        when(clientMock.getWatchKey(Mockito.eq("/application/*"), Mockito.eq("unit"))).thenReturn(updatedWatchKey);
        assertTrue(
            AppConfigurationRefreshUtil.checkStoreAfterRefreshFailed(configStore, clientMock, clientFactoryMock));

        // Config Store doesn't return a value
        when(clientMock.getWatchKey(Mockito.eq("/application/*"), Mockito.eq("unit"))).thenReturn(null);
        assertFalse(
            AppConfigurationRefreshUtil.checkStoreAfterRefreshFailed(configStore, clientMock, clientFactoryMock));
    }

    @Test
    public void refreshWithoutTimeFeatureFlag() {
        String endpoint = "refreshWithoutTimeFeatureFlag.azconfig.io";
        when(clientMock.getEndpoint()).thenReturn(endpoint);

        StateHolder newState = new StateHolder();
        newState.setLoadStateFeatureFlag(endpoint, true);

        List<ConfigurationSetting> watchKeys = new ArrayList<>();

        FeatureFlagConfigurationSetting currentWatchKey = new FeatureFlagConfigurationSetting("Alpha", false)
            .setETag("current");
        watchKeys.add(currentWatchKey);

        newState.setStateFeatureFlag(endpoint, watchKeys, Duration.ofSeconds(1));
        StateHolder.updateState(newState);

        List<ConfigurationSetting> f = new ArrayList<>();
        f.add(currentWatchKey);

        // Config Store doesn't return a watch key change.
        when(clientMock.listSettings(Mockito.any(SettingSelector.class))).thenReturn(flagsPagedIterableMock);
        when(flagsPagedIterableMock.iterator()).thenReturn(f.iterator());
        
        
        assertFalse(
            AppConfigurationRefreshUtil.checkStoreAfterRefreshFailed(configStore, clientMock, clientFactoryMock));

        ConfigurationSetting updatedWatchKey = new FeatureFlagConfigurationSetting("Alpha", false)
            .setETag("updated");
        
        f = new ArrayList<>();
        f.add(updatedWatchKey);

        // Config Store does return a watch key change.
        when(clientMock.listSettings(Mockito.any(SettingSelector.class))).thenReturn(flagsPagedIterableMock);
        when(flagsPagedIterableMock.iterator()).thenReturn(f.iterator());
        assertTrue(
            AppConfigurationRefreshUtil.checkStoreAfterRefreshFailed(configStore, clientMock, clientFactoryMock));

        // Config Store doesn't return a value, Feature Flag was deleted
        when(clientMock.listSettings(Mockito.any(SettingSelector.class))).thenReturn(flagsPagedIterableMock);
        when(flagsPagedIterableMock.iterator()).thenReturn(new ArrayList<ConfigurationSetting>().iterator());
        assertTrue(
            AppConfigurationRefreshUtil.checkStoreAfterRefreshFailed(configStore, clientMock, clientFactoryMock));
        
        ConfigurationSetting extraFeatureFlag = new FeatureFlagConfigurationSetting("Beta", false)
            .setETag("new");
        
        f = new ArrayList<>();
        f.add(currentWatchKey);
        f.add(extraFeatureFlag);

        // Config Store returns an new feature flag
        when(clientMock.listSettings(Mockito.any(SettingSelector.class))).thenReturn(flagsPagedIterableMock);
        when(flagsPagedIterableMock.iterator()).thenReturn(f.iterator());
        assertTrue(
            AppConfigurationRefreshUtil.checkStoreAfterRefreshFailed(configStore, clientMock, clientFactoryMock));
    }
}
