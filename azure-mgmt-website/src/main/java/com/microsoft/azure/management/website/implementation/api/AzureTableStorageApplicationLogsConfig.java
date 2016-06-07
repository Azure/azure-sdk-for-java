/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation.api;


/**
 * Application logs to azure table storage configuration.
 */
public class AzureTableStorageApplicationLogsConfig {
    /**
     * Log level. Possible values include: 'Off', 'Verbose', 'Information',
     * 'Warning', 'Error'.
     */
    private LogLevel level;

    /**
     * SAS url to an azure table with add/query/delete permissions.
     */
    private String sasUrl;

    /**
     * Get the level value.
     *
     * @return the level value
     */
    public LogLevel level() {
        return this.level;
    }

    /**
     * Set the level value.
     *
     * @param level the level value to set
     * @return the AzureTableStorageApplicationLogsConfig object itself.
     */
    public AzureTableStorageApplicationLogsConfig withLevel(LogLevel level) {
        this.level = level;
        return this;
    }

    /**
     * Get the sasUrl value.
     *
     * @return the sasUrl value
     */
    public String sasUrl() {
        return this.sasUrl;
    }

    /**
     * Set the sasUrl value.
     *
     * @param sasUrl the sasUrl value to set
     * @return the AzureTableStorageApplicationLogsConfig object itself.
     */
    public AzureTableStorageApplicationLogsConfig withSasUrl(String sasUrl) {
        this.sasUrl = sasUrl;
        return this;
    }

}
