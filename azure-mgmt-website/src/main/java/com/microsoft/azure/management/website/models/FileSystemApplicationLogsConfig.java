/**
 * Object]
 */

package com.microsoft.azure.management.website.models;


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
    public LogLevel getLevel() {
        return this.level;
    }

    /**
     * Set the level value.
     *
     * @param level the level value to set
     */
    public void setLevel(LogLevel level) {
        this.level = level;
    }

}
