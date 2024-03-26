// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.ingestion.models;

import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

/**
 * The audience indicating the authorization scope of log ingestion clients.
 */
public class LogsIngestionAudience extends ExpandableStringEnum<LogsIngestionAudience> {

    public static final LogsIngestionAudience AZURE_PUBLIC_CLOUD = fromString("https://monitor.azure.com//.default");
    private static final LogsIngestionAudience AZURE_GOVERNMENT = fromString("https://monitor.azure.us//.default");
    private static final LogsIngestionAudience AZURE_CHINA = fromString("https://monitor.azure.cn//.default");

    /**
     * Creates an instance of LogsIngestionAudience.
     */
    @Deprecated
    LogsIngestionAudience() {
    }

    public static LogsIngestionAudience fromString(String name) {
        return fromString(name, LogsIngestionAudience.class);
    }
    public static Collection<LogsIngestionAudience> values() {
        return values(LogsIngestionAudience.class);
    }

}
