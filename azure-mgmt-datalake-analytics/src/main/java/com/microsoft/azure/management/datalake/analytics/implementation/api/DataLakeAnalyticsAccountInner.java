/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.datalake.analytics.implementation.api;

import java.util.Map;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A Data Lake Analytics account object, containing all information associated
 * with the named Data Lake Analytics account.
 */
public class DataLakeAnalyticsAccountInner {
    /**
     * Gets or sets the account regional location.
     */
    private String location;

    /**
     * Gets or sets the account name.
     */
    private String name;

    /**
     * Gets or sets the namespace and type of the account.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String type;

    /**
     * Gets or sets the account subscription ID.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String id;

    /**
     * Gets or sets the value of custom properties.
     */
    private Map<String, String> tags;

    /**
     * Gets or sets the properties defined by Data Lake Analytics all
     * properties are specific to each resource provider.
     */
    private DataLakeAnalyticsAccountProperties properties;

    /**
     * Get the location value.
     *
     * @return the location value
     */
    public String location() {
        return this.location;
    }

    /**
     * Set the location value.
     *
     * @param location the location value to set
     * @return the DataLakeAnalyticsAccountInner object itself.
     */
    public DataLakeAnalyticsAccountInner withLocation(String location) {
        this.location = location;
        return this;
    }

    /**
     * Get the name value.
     *
     * @return the name value
     */
    public String name() {
        return this.name;
    }

    /**
     * Set the name value.
     *
     * @param name the name value to set
     * @return the DataLakeAnalyticsAccountInner object itself.
     */
    public DataLakeAnalyticsAccountInner withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Get the type value.
     *
     * @return the type value
     */
    public String type() {
        return this.type;
    }

    /**
     * Get the id value.
     *
     * @return the id value
     */
    public String id() {
        return this.id;
    }

    /**
     * Get the tags value.
     *
     * @return the tags value
     */
    public Map<String, String> tags() {
        return this.tags;
    }

    /**
     * Set the tags value.
     *
     * @param tags the tags value to set
     * @return the DataLakeAnalyticsAccountInner object itself.
     */
    public DataLakeAnalyticsAccountInner withTags(Map<String, String> tags) {
        this.tags = tags;
        return this;
    }

    /**
     * Get the properties value.
     *
     * @return the properties value
     */
    public DataLakeAnalyticsAccountProperties properties() {
        return this.properties;
    }

    /**
     * Set the properties value.
     *
     * @param properties the properties value to set
     * @return the DataLakeAnalyticsAccountInner object itself.
     */
    public DataLakeAnalyticsAccountInner withProperties(DataLakeAnalyticsAccountProperties properties) {
        this.properties = properties;
        return this;
    }

}
