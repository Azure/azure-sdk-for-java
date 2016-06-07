/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation.api;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Database connection string value to type pair.
 */
public class ConnStringValueTypePair {
    /**
     * Value of pair.
     */
    private String value;

    /**
     * Type of database. Possible values include: 'MySql', 'SQLServer',
     * 'SQLAzure', 'Custom'.
     */
    @JsonProperty(required = true)
    private DatabaseServerType type;

    /**
     * Get the value value.
     *
     * @return the value value
     */
    public String value() {
        return this.value;
    }

    /**
     * Set the value value.
     *
     * @param value the value value to set
     * @return the ConnStringValueTypePair object itself.
     */
    public ConnStringValueTypePair withValue(String value) {
        this.value = value;
        return this;
    }

    /**
     * Get the type value.
     *
     * @return the type value
     */
    public DatabaseServerType type() {
        return this.type;
    }

    /**
     * Set the type value.
     *
     * @param type the type value to set
     * @return the ConnStringValueTypePair object itself.
     */
    public ConnStringValueTypePair withType(DatabaseServerType type) {
        this.type = type;
        return this;
    }

}
