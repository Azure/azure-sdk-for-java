// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.azure.data.appconfiguration.models.SettingSelector;
import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * Azure App Configuration PropertySource unique per Store Label(Profile) combo.
 *
 * <p>
 * i.e. If connecting to 2 stores and have 2 labels set 4 AppConfigurationPropertySources need to be
 * created.
 * </p>
 */
final class AppConfigurationApplicationSettingPropertySource extends AppConfigurationPropertySource {

    private final AppConfigurationKeyVaultClientFactory keyVaultClientFactory;
    private final String keyFilter;

    private final String[] labelFilter;

    AppConfigurationApplicationSettingPropertySource(String originEndpoint, AppConfigurationReplicaClient replicaClient,
        AppConfigurationKeyVaultClientFactory keyVaultClientFactory, String keyFilter, String[] labelFilter,
        int maxRetryTime) {
        // The context alone does not uniquely define a PropertySource, append storeName
        // and label to uniquely define a PropertySource
        super(keyFilter + originEndpoint + "/" + getLabelName(labelFilter), replicaClient, maxRetryTime);
        this.keyVaultClientFactory = keyVaultClientFactory;
        this.keyFilter = keyFilter;
        this.labelFilter = labelFilter;
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

        List<String> labels = Arrays.asList(labelFilter);
        Collections.reverse(labels);

        for (String label : labels) {
            SettingSelector settingSelector = new SettingSelector().setKeyFilter(keyFilter + "*").setLabelFilter(label);

            // * for wildcard match
            processConfigurationSettings(replicaClient.listSettings(settingSelector), settingSelector.getKeyFilter(),
                trim, keyVaultClientFactory);
        }

    }
}
