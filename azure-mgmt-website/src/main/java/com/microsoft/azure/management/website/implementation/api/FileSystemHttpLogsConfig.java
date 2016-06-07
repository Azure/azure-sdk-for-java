/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation.api;


/**
 * Http logs to file system configuration.
 */
public class FileSystemHttpLogsConfig {
    /**
     * Maximum size in megabytes that http log files can use.
     * When reached old log files will be removed to make space
     * for new ones.
     * Value can range between 25 and 100.
     */
    private Integer retentionInMb;

    /**
     * Retention in days.
     * Remove files older than X days.
     * 0 or lower means no retention.
     */
    private Integer retentionInDays;

    /**
     * Enabled.
     */
    private Boolean enabled;

    /**
     * Get the retentionInMb value.
     *
     * @return the retentionInMb value
     */
    public Integer retentionInMb() {
        return this.retentionInMb;
    }

    /**
     * Set the retentionInMb value.
     *
     * @param retentionInMb the retentionInMb value to set
     * @return the FileSystemHttpLogsConfig object itself.
     */
    public FileSystemHttpLogsConfig withRetentionInMb(Integer retentionInMb) {
        this.retentionInMb = retentionInMb;
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
     * @return the FileSystemHttpLogsConfig object itself.
     */
    public FileSystemHttpLogsConfig withRetentionInDays(Integer retentionInDays) {
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
     * @return the FileSystemHttpLogsConfig object itself.
     */
    public FileSystemHttpLogsConfig withEnabled(Boolean enabled) {
        this.enabled = enabled;
        return this;
    }

}
