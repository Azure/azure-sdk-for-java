/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation.api;


/**
 * Application logs to file system configuration.
 */
public class FileSystemApplicationLogsConfig {
    /**
     * Log level. Possible values include: 'Off', 'Verbose', 'Information',
     * 'Warning', 'Error'.
     */
    private LogLevel level;

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
     * @return the FileSystemApplicationLogsConfig object itself.
     */
    public FileSystemApplicationLogsConfig withLevel(LogLevel level) {
        this.level = level;
        return this;
    }

}
