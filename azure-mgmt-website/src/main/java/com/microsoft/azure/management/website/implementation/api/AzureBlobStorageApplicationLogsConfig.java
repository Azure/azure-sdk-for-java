/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation.api;


/**
 * Application logs azure blob storage configuration.
 */
public class AzureBlobStorageApplicationLogsConfig {
    /**
     * Log level. Possible values include: 'Off', 'Verbose', 'Information',
     * 'Warning', 'Error'.
     */
    private LogLevel level;

    /**
     * SAS url to a azure blob container with read/write/list/delete
     * permissions.
     */
    private String sasUrl;

    /**
     * Retention in days.
     * Remove blobs older than X days.
     * 0 or lower means no retention.
     */
    private Integer retentionInDays;

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
     * @return the AzureBlobStorageApplicationLogsConfig object itself.
     */
    public AzureBlobStorageApplicationLogsConfig withLevel(LogLevel level) {
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
     * @return the AzureBlobStorageApplicationLogsConfig object itself.
     */
    public AzureBlobStorageApplicationLogsConfig withSasUrl(String sasUrl) {
        this.sasUrl = sasUrl;
        return this;
    }

    /**
     * Get the retentionInDays value.
     *
     * @return the retentionInDays value
     */
    public Integer retentionInDays() {
        return this.retentionInDays;
    }

    /**
     * Set the retentionInDays value.
     *
     * @param retentionInDays the retentionInDays value to set
     * @return the AzureBlobStorageApplicationLogsConfig object itself.
     */
    public AzureBlobStorageApplicationLogsConfig withRetentionInDays(Integer retentionInDays) {
        this.retentionInDays = retentionInDays;
        return this;
    }

}
