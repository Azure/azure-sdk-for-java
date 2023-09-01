// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation;

import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.FEATURE_FLAG_PREFIX;
import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.FEATURE_MANAGEMENT_KEY;

import java.util.List;

import com.azure.data.appconfiguration.models.FeatureFlagConfigurationSetting;
import com.azure.spring.cloud.appconfiguration.config.implementation.http.policy.TracingInfo;
import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * Azure App Configuration PropertySource unique per Store Label(Profile) combo.
 *
 * <p>
 * i.e. If connecting to 2 stores and have 2 labels set 4 AppConfigurationPropertySources need to be created.
 * </p>
 */
final class AppConfigurationSnapshotPropertySource extends AppConfigurationApplicationSettingPropertySource {

    private final AppConfigurationKeyVaultClientFactory keyVaultClientFactory;

    private final String snapshotName;

    AppConfigurationSnapshotPropertySource(String originEndpoint, AppConfigurationReplicaClient replicaClient,
        AppConfigurationKeyVaultClientFactory keyVaultClientFactory, String snapshotName, int maxRetryTime) {
        // The context alone does not uniquely define a PropertySource, append storeName
        // and label to uniquely define a PropertySource
        // super(snapshotName + originEndpoint + "/", replicaClient, maxRetryTime);
        super(originEndpoint, replicaClient, keyVaultClientFactory, null, null, maxRetryTime);
        this.keyVaultClientFactory = keyVaultClientFactory;
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
    public void initProperties(List<String> trim) throws JsonProcessingException {
        processConfigurationSettings(replicaClient.listSettingSnapshot(snapshotName), null, trim,
            keyVaultClientFactory);
    }

    @Override
    void handleFeatureFlag(String key, FeatureFlagConfigurationSetting setting, List<String> trimStrings)
        throws JsonProcessingException {
        // Feature Flags are only part of this if they come from a snapshot
        TracingInfo tracing = replicaClient.getTracingInfo();
        featureConfigurationSettings.add(setting);
        FeatureFlagConfigurationSetting featureFlag = setting;

        String configName = FEATURE_MANAGEMENT_KEY
            + setting.getKey().trim().substring(FEATURE_FLAG_PREFIX.length());

        updateTelemetry(featureFlag, tracing);

        properties.put(configName, createFeature(featureFlag));

    }
}
