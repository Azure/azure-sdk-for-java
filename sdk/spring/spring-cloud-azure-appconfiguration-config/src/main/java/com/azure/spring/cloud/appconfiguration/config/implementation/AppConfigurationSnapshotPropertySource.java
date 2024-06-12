// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.source.InvalidConfigurationPropertyValueException;

import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.FeatureFlagConfigurationSetting;
import com.azure.spring.cloud.appconfiguration.config.implementation.feature.FeatureFlags;

/**
 * Azure App Configuration PropertySource unique per Store Label(Profile) combo.
 *
 * <p>
 * i.e. If connecting to 2 stores and have 2 labels set 4 AppConfigurationPropertySources need to be created.
 * </p>
 */
final class AppConfigurationSnapshotPropertySource extends AppConfigurationApplicationSettingPropertySource {

    private final String snapshotName;

    private final FeatureFlagClient featureFlagClient;

    private List<ConfigurationSetting> featureFlagsList = new ArrayList<>();

    AppConfigurationSnapshotPropertySource(String name, AppConfigurationReplicaClient replicaClient,
        AppConfigurationKeyVaultClientFactory keyVaultClientFactory, String snapshotName,
        FeatureFlagClient featureFlagClient) {
        // The context alone does not uniquely define a PropertySource, append storeName
        // and label to uniquely define a PropertySource
        super(name, replicaClient, keyVaultClientFactory, null, null);
        this.snapshotName = snapshotName;
        this.featureFlagClient = featureFlagClient;
    }

    /**
     * <p>
     * Gets settings from Azure/Cache to set as configurations. Updates the cache.
     * </p>
     *
     * @param trim prefix to trim
     * @throws InvalidConfigurationPropertyValueException thrown if fails to parse Json content type
     */
    public void initProperties(List<String> trim) throws InvalidConfigurationPropertyValueException {
        processConfigurationSettings(replicaClient.listSettingSnapshot(snapshotName), null, trim);

        FeatureFlags featureFlags = new FeatureFlags(null, featureFlagsList);
        featureFlagClient.proccessFeatureFlags(featureFlags, replicaClient.getEndpoint());
    }

    @Override
    void handleFeatureFlag(String key, FeatureFlagConfigurationSetting setting, List<String> trimStrings) {
        // Feature Flags are only part of this if they come from a snapshot
        featureFlagsList.add(setting);
    }
}
