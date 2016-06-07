/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation.api;


/**
 * Http logs to azure blob storage configuration.
 */
public class AzureBlobStorageHttpLogsConfig {
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
     * Enabled.
     */
    private Boolean enabled;

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
     * @return the AzureBlobStorageHttpLogsConfig object itself.
     */
    public AzureBlobStorageHttpLogsConfig withSasUrl(String sasUrl) {
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
     * @return the AzureBlobStorageHttpLogsConfig object itself.
     */
    public AzureBlobStorageHttpLogsConfig withRetentionInDays(Integer retentionInDays) {
        this.retentionInDays = retentionInDays;
        return this;
    }

    /**
     * Get the enabled value.
     *
     * @return the enabled value
     */
    public Boolean enabled() {
        return this.enabled;
    }

    /**
     * Set the enabled value.
     *
     * @param enabled the enabled value to set
     * @return the AzureBlobStorageHttpLogsConfig object itself.
     */
    public AzureBlobStorageHttpLogsConfig withEnabled(Boolean enabled) {
        this.enabled = enabled;
        return this;
    }

}
