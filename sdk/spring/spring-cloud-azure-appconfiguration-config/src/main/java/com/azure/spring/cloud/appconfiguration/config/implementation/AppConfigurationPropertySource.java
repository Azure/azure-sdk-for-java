// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.source.InvalidConfigurationPropertyValueException;
import org.springframework.core.env.EnumerablePropertySource;

import com.azure.core.util.Context;

/**
 * Abstract base class for Azure App Configuration PropertySource implementations.
 * 
 * <p>
 * Each PropertySource is unique per Store-Label(Profile) combination. For example, if connecting to 2 stores with 2
 * labels each, 4 AppConfigurationPropertySources need to be created.
 * </p>
 */
abstract class AppConfigurationPropertySource extends EnumerablePropertySource<AppConfigurationReplicaClient> {

    /**
     * Cache for storing configuration properties retrieved from Azure App Configuration.
     */
    protected final Map<String, Object> properties = new LinkedHashMap<>();

    /**
     * Client for communicating with Azure App Configuration service.
     */
    protected final AppConfigurationReplicaClient replicaClient;

    /**
     * Creates a new AppConfigurationPropertySource.
     * 
     * @param name the name of this property source, should be unique to identify the store-label combination
     * @param replicaClient the client for communicating with Azure App Configuration
     */
    AppConfigurationPropertySource(String name, AppConfigurationReplicaClient replicaClient) {
        // The context alone does not uniquely define a PropertySource, append storeName
        // and label to uniquely define a PropertySource
        super(name, replicaClient);
        this.replicaClient = replicaClient;
    }

    /**
     * Returns the names of all properties in this property source.
     * 
     * @return array of property names
     */
    @Override
    public String[] getPropertyNames() {
        return properties.keySet().toArray(String[]::new);
    }

    /**
     * Returns the value of the specified property.
     * 
     * @param name the name of the property to retrieve
     * @return the value of the property, or null if not found
     */
    @Override
    public Object getProperty(String name) {
        return properties.get(name);
    }

    /**
     * Creates a comma-separated string from the given label filters.
     * 
     * @param labelFilters array of label filters, may be null
     * @return comma-separated string of labels, or empty string if null/empty
     */
    protected static String getLabelName(String[] labelFilters) {
        if (labelFilters == null || labelFilters.length == 0) {
            return "";
        }
        return String.join(",", labelFilters);
    }

    /**
     * Initializes the properties for this property source by loading them from Azure App Configuration.
     * 
     * @param trim list of key prefixes to trim from configuration keys
     * @param context the context for loading properties, may contain additional metadata
     * @throws InvalidConfigurationPropertyValueException if there are issues with the configuration properties
     */
    protected abstract void initProperties(List<String> trim, Context context)
        throws InvalidConfigurationPropertyValueException;
}
