// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.ingestion.models;

import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

/**
 * The audience indicating the authorization scope of log ingestion clients.
 */
public class LogsIngestionAudience extends ExpandableStringEnum<LogsIngestionAudience> {

    /**
     * Static value for Azure Public Cloud.
     */
    public static final LogsIngestionAudience AZURE_PUBLIC_CLOUD = fromString("https://monitor.azure.com//.default");

    /**
     * Static value for Azure US Government.
     */
    public static final LogsIngestionAudience AZURE_GOVERNMENT = fromString("https://monitor.azure.us//.default");

    /**
     * Static value for Azure China.
     */
    public static final LogsIngestionAudience AZURE_CHINA = fromString("https://monitor.azure.cn//.default");

    /**
     * @deprecated Creates an instance of LogsIngestionAudience.
     */
    @Deprecated
    LogsIngestionAudience() {
    }

    /**
     * Creates an instance of LogsIngestionAudience.
     *
     * @param name the string value.
     * @return the LogsIngestionAudience.
     */
    public static LogsIngestionAudience fromString(String name) {
        return fromString(name, LogsIngestionAudience.class);
    }

    /**
     * Get the collection of LogsIngestionAudience values.
     *
     * @return the collection of LogsIngestionAudience values.
     */
    public static Collection<LogsIngestionAudience> values() {
        return values(LogsIngestionAudience.class);
    }

}
