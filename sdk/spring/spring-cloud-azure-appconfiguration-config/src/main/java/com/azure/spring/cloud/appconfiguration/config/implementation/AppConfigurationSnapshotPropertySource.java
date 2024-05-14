// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation;

import java.util.List;

import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * Azure App Configuration PropertySource unique per Store Label(Profile) combo.
 *
 * <p>
 * i.e. If connecting to 2 stores and have 2 labels set 4 AppConfigurationPropertySources need to be created.
 * </p>
 */
final class AppConfigurationSnapshotPropertySource extends AppConfigurationApplicationSettingPropertySource {

    private final String snapshotName;

    AppConfigurationSnapshotPropertySource(String name, AppConfigurationReplicaClient replicaClient,
        AppConfigurationKeyVaultClientFactory keyVaultClientFactory, String snapshotName) {
        // The context alone does not uniquely define a PropertySource, append storeName
        // and label to uniquely define a PropertySource
        // super(snapshotName + originEndpoint + "/", replicaClient, maxRetryTime);
        super(name, replicaClient, keyVaultClientFactory, null, null);
        this.snapshotName = snapshotName;
    }

    /**
     * <p>
     * Gets settings from Azure/Cache to set as configurations. Updates the cache.
     * </p>
     *
     * @param trim prefix to trim
     * @throws JsonProcessingException thrown if fails to parse Json content type
     */
    public void initProperties(List<String> trim, FeatureFlagLoader featureFlagLoader) throws JsonProcessingException {
        List<ConfigurationSetting> settings = replicaClient.listSettingSnapshot(snapshotName);
        processConfigurationSettings(settings, null, trim);
        
        FeatureFlags featureFlags = new FeatureFlags(null, settings);
        featureFlagLoader.proccessFeatureFlags(featureFlags, replicaClient.getEndpoint());
    }
}
