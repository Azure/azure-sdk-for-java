// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.springframework.boot.context.config.Profiles;

import com.azure.spring.cloud.appconfiguration.config.implementation.properties.AppConfigurationKeyValueSelector;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.AppConfigurationStoreMonitoring;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.ConfigStore;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.FeatureFlagKeyValueSelector;

/**
 * Test class for AzureAppConfigDataResource focusing on enabled state functionality.
 */
class AzureAppConfigDataResourceTest {

    @Mock
    private Profiles mockProfiles;

    private static final String TEST_ENDPOINT = "https://test.azconfig.io";
    private static final Duration TEST_REFRESH_INTERVAL = Duration.ofSeconds(30);
    
    private ConfigStore configStore;
    private AppConfigurationStoreMonitoring monitoring;

    @BeforeEach
    public void init() {
        configStore = new ConfigStore();
        monitoring = new AppConfigurationStoreMonitoring();
        
        configStore.setEndpoint(TEST_ENDPOINT);
        configStore.setMonitoring(monitoring);
    }

    @ParameterizedTest
    @CsvSource({
        "true,  true,  true,  'Both appConfig and configStore enabled'",
        "true,  false, false, 'AppConfig enabled but configStore disabled'", 
        "false, true,  false, 'AppConfig disabled regardless of configStore state'",
        "false, false, false, 'Both appConfig and configStore disabled'"
    })
    void testConfigStoreEnabledState(boolean appConfigEnabled, boolean configStoreEnabled, 
                                   boolean expectedEnabled, String description) {
        configStore.setEnabled(configStoreEnabled);

        AzureAppConfigDataResource resource = new AzureAppConfigDataResource(
            appConfigEnabled, configStore, mockProfiles, false, TEST_REFRESH_INTERVAL);

        assertEquals(expectedEnabled, resource.isConfigStoreEnabled(), description);
    }

    @ParameterizedTest
    @CsvSource({
        "true,  'refresh scenario'",
        "false, 'startup scenario'"
    })
    void testEnabledStateWithRefreshScenarios(boolean isRefresh, String scenarioDescription) {
        configStore.setEnabled(true);

        AzureAppConfigDataResource resource = new AzureAppConfigDataResource(
            true, configStore, mockProfiles, isRefresh, TEST_REFRESH_INTERVAL);

        assertTrue(resource.isConfigStoreEnabled(), 
            "Config store should be enabled in " + scenarioDescription + " when conditions are met");
        assertEquals(isRefresh, resource.isRefresh(), 
            "Should correctly report refresh state for " + scenarioDescription);
    }

    @Test
    void testAllPropertiesSetCorrectlyRegardlessOfEnabledState() {
        List<String> trimKeyPrefixes = List.of("/application/", "/config/");
        List<AppConfigurationKeyValueSelector> selects = new ArrayList<>();
        List<FeatureFlagKeyValueSelector> featureFlagSelects = new ArrayList<>();

        configStore.setTrimKeyPrefix(trimKeyPrefixes);
        configStore.setSelects(selects);

        configStore.setEnabled(true);
        AzureAppConfigDataResource enabledResource = new AzureAppConfigDataResource(
            true, configStore, mockProfiles, false, TEST_REFRESH_INTERVAL);

        assertTrue(enabledResource.isConfigStoreEnabled());
        assertAllPropertiesCorrect(enabledResource, trimKeyPrefixes, selects, featureFlagSelects, false);

        configStore.setEnabled(false);
        AzureAppConfigDataResource disabledResource = new AzureAppConfigDataResource(
            true, configStore, mockProfiles, true, TEST_REFRESH_INTERVAL);

        assertFalse(disabledResource.isConfigStoreEnabled());
        assertAllPropertiesCorrect(disabledResource, trimKeyPrefixes, selects, featureFlagSelects, true);
    }

    private void assertAllPropertiesCorrect(AzureAppConfigDataResource resource,
                                          List<String> expectedTrimKeyPrefixes,
                                          List<AppConfigurationKeyValueSelector> expectedSelects,
                                          List<FeatureFlagKeyValueSelector> expectedFeatureFlagSelects,
                                          boolean expectedIsRefresh) {
        assertEquals(TEST_ENDPOINT, resource.getEndpoint());
        assertEquals(expectedTrimKeyPrefixes, resource.getTrimKeyPrefix());
        assertEquals(expectedSelects, resource.getSelects());
        assertEquals(expectedFeatureFlagSelects, resource.getFeatureFlagSelects());
        assertEquals(monitoring, resource.getMonitoring());
        assertEquals(mockProfiles, resource.getProfiles());
        assertEquals(TEST_REFRESH_INTERVAL, resource.getRefreshInterval());
        assertEquals(expectedIsRefresh, resource.isRefresh());
    }

    @Test
    void testNullRefreshIntervalHandling() {
        configStore.setEnabled(true);
        AzureAppConfigDataResource enabledResource = new AzureAppConfigDataResource(
            true, configStore, mockProfiles, false, null);
        assertTrue(enabledResource.isConfigStoreEnabled());
        assertNull(enabledResource.getRefreshInterval());

        configStore.setEnabled(false);
        AzureAppConfigDataResource disabledResource = new AzureAppConfigDataResource(
            false, configStore, mockProfiles, true, null);
        assertFalse(disabledResource.isConfigStoreEnabled());
        assertNull(disabledResource.getRefreshInterval());
        assertTrue(disabledResource.isRefresh());
    }

}
