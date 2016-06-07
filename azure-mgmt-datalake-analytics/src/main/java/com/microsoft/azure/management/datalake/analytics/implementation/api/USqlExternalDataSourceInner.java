/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.datalake.analytics.implementation.api;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A Data Lake Analytics catalog U-SQL external datasource item.
 */
public class USqlExternalDataSourceInner extends CatalogItem {
    /**
     * Gets or sets the name of the database.
     */
    private String databaseName;

    /**
     * Gets or sets the name of the external data source.
     */
    @JsonProperty(value = "externalDataSourceName")
    private String name;

    /**
     * Gets or sets the name of the provider for the external data source.
     */
    private String provider;

    /**
     * Gets or sets the name of the provider string for the external data
     * source.
     */
    private String providerString;

    /**
     * Gets or sets the list of types to push down from the external data
     * source.
     */
    private List<String> pushdownTypes;

    /**
     * Get the databaseName value.
     *
     * @return the databaseName value
     */
    public String databaseName() {
        return this.databaseName;
    }

    /**
     * Set the databaseName value.
     *
     * @param databaseName the databaseName value to set
     * @return the USqlExternalDataSourceInner object itself.
     */
    public USqlExternalDataSourceInner withDatabaseName(String databaseName) {
        this.databaseName = databaseName;
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
     * @return the USqlExternalDataSourceInner object itself.
     */
    public USqlExternalDataSourceInner withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Get the provider value.
     *
     * @return the provider value
     */
    public String provider() {
        return this.provider;
    }

    /**
     * Set the provider value.
     *
     * @param provider the provider value to set
     * @return the USqlExternalDataSourceInner object itself.
     */
    public USqlExternalDataSourceInner withProvider(String provider) {
        this.provider = provider;
        return this;
    }

    /**
     * Get the providerString value.
     *
     * @return the providerString value
     */
    public String providerString() {
        return this.providerString;
    }

    /**
     * Set the providerString value.
     *
     * @param providerString the providerString value to set
     * @return the USqlExternalDataSourceInner object itself.
     */
    public USqlExternalDataSourceInner withProviderString(String providerString) {
        this.providerString = providerString;
        return this;
    }

    /**
     * Get the pushdownTypes value.
     *
     * @return the pushdownTypes value
     */
    public List<String> pushdownTypes() {
        return this.pushdownTypes;
    }

    /**
     * Set the pushdownTypes value.
     *
     * @param pushdownTypes the pushdownTypes value to set
     * @return the USqlExternalDataSourceInner object itself.
     */
    public USqlExternalDataSourceInner withPushdownTypes(List<String> pushdownTypes) {
        this.pushdownTypes = pushdownTypes;
        return this;
    }

}
