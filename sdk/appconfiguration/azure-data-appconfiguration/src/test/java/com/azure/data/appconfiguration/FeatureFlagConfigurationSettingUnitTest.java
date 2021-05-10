// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration;

import com.azure.data.appconfiguration.models.FeatureFlagConfigurationSetting;
import com.azure.data.appconfiguration.models.FeatureFlagFilter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static com.azure.data.appconfiguration.models.FeatureFlagConfigurationSetting.KEY_PREFIX;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link FeatureFlagConfigurationSetting}.
 */
public class FeatureFlagConfigurationSettingUnitTest {
    private static final String FEATURE_ID = "featureId";
    private static final String FEATURE_FLAG_CONTENT_TYPE = "application/vnd.microsoft.appconfig.ff+json;charset=utf-8";


    @Test
    public void notNullButEmptyFlagFiltersTest() {
        final FeatureFlagConfigurationSetting setting = new FeatureFlagConfigurationSetting(FEATURE_ID, true);
        Assertions.assertNotNull(setting.getClientFilters());
        assertEquals(Collections.emptyList(), setting.getClientFilters());

        setting.getFeatureId();
    }

    @Test
    public void addFlagFilterTest() {
        final FeatureFlagConfigurationSetting setting = new FeatureFlagConfigurationSetting(FEATURE_ID, true);
        final FeatureFlagFilter filterName = new FeatureFlagFilter("filterName");

        assertEquals(0, setting.getClientFilters().size());
        setting.addClientFilter(filterName);
        assertEquals(1, setting.getClientFilters().size());
    }

    @Test
    public void settingKeyWithPrefixValueTest() {
        final FeatureFlagConfigurationSetting setting = new FeatureFlagConfigurationSetting(FEATURE_ID, true);
        final String key = setting.getKey();
        assertTrue(key.startsWith(KEY_PREFIX));
        assertEquals(KEY_PREFIX + FEATURE_ID, key);
    }

    @Test
    public void featureIdTest() {
        final FeatureFlagConfigurationSetting setting = new FeatureFlagConfigurationSetting(FEATURE_ID, true);
        assertEquals(FEATURE_ID, setting.getFeatureId());
    }

    @Test
    public void contentTypePropertyTest() {
        final FeatureFlagConfigurationSetting setting = new FeatureFlagConfigurationSetting(FEATURE_ID, true);
        assertEquals(FEATURE_FLAG_CONTENT_TYPE, setting.getContentType());
    }

    @Test
    public void nullDescriptionPropertyTest() {
        final FeatureFlagConfigurationSetting setting = new FeatureFlagConfigurationSetting(FEATURE_ID, true);
        assertNull(setting.getDescription());
    }

    @Test
    public void nullDisplayNameTest() {
        final FeatureFlagConfigurationSetting setting = new FeatureFlagConfigurationSetting(FEATURE_ID, true);
        assertNull(setting.getDisplayName());
    }


}
