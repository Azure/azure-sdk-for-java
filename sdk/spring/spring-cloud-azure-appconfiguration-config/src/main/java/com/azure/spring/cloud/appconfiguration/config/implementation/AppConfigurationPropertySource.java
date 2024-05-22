// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.boot.context.properties.source.InvalidConfigurationPropertyValueException;
import org.springframework.core.env.EnumerablePropertySource;

import com.azure.data.appconfiguration.ConfigurationClient;
import com.azure.data.appconfiguration.models.ConfigurationSetting;

/**
 * Azure App Configuration PropertySource unique per Store Label(Profile) combo.
 *
 * <p>
 * i.e. If connecting to 2 stores and have 2 labels set 4 AppConfigurationPropertySources need to be
 * created.
 * </p>
 */
abstract class AppConfigurationPropertySource extends EnumerablePropertySource<ConfigurationClient> {

    protected final Map<String, Object> properties = new LinkedHashMap<>();

    protected final List<ConfigurationSetting> featureConfigurationSettings = new ArrayList<>();

    protected final AppConfigurationReplicaClient replicaClient;

    AppConfigurationPropertySource(String name, AppConfigurationReplicaClient replicaClient) {
        // The context alone does not uniquely define a PropertySource, append storeName
        // and label to uniquely define a PropertySource
        super(name);
        this.replicaClient = replicaClient;
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

    protected static String getLabelName(String[] labelFilters) {
        if (labelFilters == null) {
            return "";
        }
        return String.join(",", labelFilters);
    }

    protected abstract void initProperties(List<String> trim) throws InvalidConfigurationPropertyValueException;
}
