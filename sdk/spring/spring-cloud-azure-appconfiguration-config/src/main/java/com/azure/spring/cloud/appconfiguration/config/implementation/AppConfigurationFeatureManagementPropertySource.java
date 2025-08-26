// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation;

import java.util.List;

import org.springframework.core.env.EnumerablePropertySource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

/**
 * Azure App Configuration PropertySource unique per Store Label(Profile) combo.
 *
 * <p>
 * i.e. If connecting to 2 stores and have 2 labels set 4 AppConfigurationPropertySources need to be created.
 * </p>
 */
class AppConfigurationFeatureManagementPropertySource extends EnumerablePropertySource<FeatureFlagClient> {
    private final FeatureFlagClient featureFlagLoader;

    private static final String FEATURE_MANAGEMENT_KEY = "feature-management";

    private static final ObjectMapper MAPPER = JsonMapper.builder().build();

    private static final String FEATURE_FLAG_KEY = FEATURE_MANAGEMENT_KEY + ".feature-flags";

    AppConfigurationFeatureManagementPropertySource(FeatureFlagClient featureFlagLoader) {
        super(FEATURE_MANAGEMENT_KEY, featureFlagLoader);
        this.featureFlagLoader = featureFlagLoader;
    }

    @Override
    public String[] getPropertyNames() {
        if (featureFlagLoader != null && featureFlagLoader.getFeatureFlags() != null 
            && !featureFlagLoader.getFeatureFlags().isEmpty()) {
            return new String[] { FEATURE_FLAG_KEY };
        }
        return new String[0];
    }

    @Override
    public Object getProperty(String name) {
        if (FEATURE_FLAG_KEY.equals(name)) {
            return MAPPER.convertValue(featureFlagLoader.getFeatureFlags(), List.class);
        }
        return null;
    }
}
