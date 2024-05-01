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
class AppConfigurationFeatureManagementPropertySource extends EnumerablePropertySource<FeatureFlagLoader> {
    
    private final FeatureFlagLoader featureFlagLoader;

    AppConfigurationFeatureManagementPropertySource(FeatureFlagLoader featureFlagLoader) {
       super("feature_management", featureFlagLoader);
       this.featureFlagLoader = featureFlagLoader;
    }


    @Override
    public String[] getPropertyNames() {
        String[] names = {"feature_management.feature_flags"};
        return names;
    }

    @Override
    public Object getProperty(String name) {
        return featureFlagLoader.getProperties();
    }
}
