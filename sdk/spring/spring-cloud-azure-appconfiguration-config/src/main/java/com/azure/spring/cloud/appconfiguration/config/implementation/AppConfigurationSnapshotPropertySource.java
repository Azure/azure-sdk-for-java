// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation;

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * Azure App Configuration PropertySource unique per Store Label(Profile) combo.
 *
 * <p>
 * i.e. If connecting to 2 stores and have 2 labels set 4 AppConfigurationPropertySources need to be
 * created.
 * </p>
 */
final class AppConfigurationSnapshotPropertySource extends AppConfigurationPropertySource {

    private final AppConfigurationKeyVaultClientFactory keyVaultClientFactory;

    private final String snapshotName;

    AppConfigurationSnapshotPropertySource(String originEndpoint, AppConfigurationReplicaClient replicaClient,
        AppConfigurationKeyVaultClientFactory keyVaultClientFactory, String snapshotName, int maxRetryTime) {
        // The context alone does not uniquely define a PropertySource, append storeName
        // and label to uniquely define a PropertySource
        super(snapshotName + originEndpoint + "/", replicaClient, maxRetryTime);
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
        processConfigurationSettingsSnapshot(replicaClient.listSettingSnapshot(snapshotName), trim, keyVaultClientFactory);
    }
}
