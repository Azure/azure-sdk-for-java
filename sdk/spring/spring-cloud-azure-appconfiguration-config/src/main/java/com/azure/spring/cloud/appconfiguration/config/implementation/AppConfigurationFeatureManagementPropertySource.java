// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation;

import org.springframework.core.env.EnumerablePropertySource;

/**
 * Azure App Configuration PropertySource unique per Store Label(Profile) combo.
 *
 * <p>
 * i.e. If connecting to 2 stores and have 2 labels set 4 AppConfigurationPropertySources need to be created.
 * </p>
 */
class AppConfigurationFeatureManagementPropertySource extends EnumerablePropertySource<FeatureFlagClient> {

    private final FeatureFlagClient featureFlagLoader;

    private static final String FEATURE_MANAGEMENT_KEY = "feature_management";

    private static final String FEATURE_FLAG_KEY = FEATURE_MANAGEMENT_KEY + ".feature_flags";

    AppConfigurationFeatureManagementPropertySource(FeatureFlagClient featureFlagLoader) {
        super(FEATURE_MANAGEMENT_KEY, featureFlagLoader);
        this.featureFlagLoader = featureFlagLoader;
    }

    @Override
    public String[] getPropertyNames() {
        String[] names = { FEATURE_FLAG_KEY };
        return names;
    }

    @Override
    public Object getProperty(String name) {
        if (FEATURE_FLAG_KEY.equals(name)) {
            return featureFlagLoader.getProperties();
        }
        return null;
    }
}
