// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.ingestion.models;

/**
 * The audience indicating the authorization scope of log ingestion clients.
 */
public class LogsIngestionAudience {

    private final String audience;

    private static final String AZURE_PUBLIC_CLOUD = "https://monitor.azure.com//.default";
    private static final String AZURE_GOVERNMENT = "https://monitor.azure.us//.default";
    private static final String AZURE_CHINA = "https://monitor.azure.cn//.default";

    /**
     * Creates an instance of LogsIngestionAudience.
     * @param audience The audience for the log ingestion.
     */
    LogsIngestionAudience(String audience) {
        this.audience = audience;
    }

    /**
     * Creates an instance of LogsIngestionAudience for Azure Public Cloud.
     * @return LogsIngestionAudience for Azure Public Cloud.
     */
    public static LogsIngestionAudience azurePublic() {
        return new LogsIngestionAudience(AZURE_PUBLIC_CLOUD);
    }

    /**
     * Creates an instance of LogsIngestionAudience for Azure US Government Cloud.
     * @return LogsIngestionAudience for Azure US Government Cloud.
     */
    public static LogsIngestionAudience azureGovernment() {
        return new LogsIngestionAudience(AZURE_GOVERNMENT);
    }

    /**
     * Creates an instance of LogsIngestionAudience for Azure China Cloud.
     * @return LogsIngestionAudience for Azure China Cloud.
     */
    public static LogsIngestionAudience azureChina() {
        return new LogsIngestionAudience(AZURE_CHINA);
    }

    /**
     * Gets the audience for the log ingestion.
     * @return The audience for the log ingestion.
     */
    public String[] getAudience() {
        return new String[]{audience};
    }

}
