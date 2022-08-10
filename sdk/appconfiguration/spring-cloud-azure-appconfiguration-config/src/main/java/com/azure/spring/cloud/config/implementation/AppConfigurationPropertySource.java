// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config.implementation;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.core.env.EnumerablePropertySource;

import com.azure.data.appconfiguration.ConfigurationClient;

/**
 * Azure App Configuration PropertySource unique per Store Label(Profile) combo.
 *
 * <p>
 * i.e. If connecting to 2 stores and have 2 labels set 4 AppConfigurationPropertySources need to be created.
 * </p>
 */
abstract class AppConfigurationPropertySource extends EnumerablePropertySource<ConfigurationClient> {

    protected final String keyFilter;

    protected final String[] labelFilter;

    protected final Map<String, Object> properties = new LinkedHashMap<>();

    protected final AppConfigurationReplicaClient replicaClient;

    AppConfigurationPropertySource(String originEndpoint, AppConfigurationReplicaClient replicaClient, String keyFilter,
        String[] labelFilter) {
        // The context alone does not uniquely define a PropertySource, append storeName
        // and label to uniquely define a PropertySource
        super(
            keyFilter + originEndpoint + "/" + getLabelName(labelFilter));
        this.replicaClient = replicaClient;
        this.keyFilter = keyFilter;
        this.labelFilter = labelFilter;
    }

    @Override
    public String[] getPropertyNames() {
        Set<String> keySet = properties.keySet();
        return keySet.toArray(new String[keySet.size()]);
    }

    @Override
    public Object getProperty(String name) {
        return properties.get(name);
    }

    private static String getLabelName(String[] labelFilter) {
        StringBuilder labelName = new StringBuilder();
        for (String label : labelFilter) {

            labelName.append((labelName.length() == 0) ? label : "," + label);
        }
        return labelName.toString();
    }
}
