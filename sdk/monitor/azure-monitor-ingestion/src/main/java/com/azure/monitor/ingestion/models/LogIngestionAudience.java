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
     * Creates an instance of LogIngestionAudience.
     * @param audience The audience for the log ingestion.
     */
    LogIngestionAudience(String audience) {
        this.audience = audience;
    }

    /**
     * Creates an instance of LogIngestionAudience for Azure Public Cloud.
     * @return LogIngestionAudience for Azure Public Cloud.
     */
    public static LogIngestionAudience azurePublic() {
        return new LogIngestionAudience(AZURE_PUBLIC);
    }

    /**
     * Creates an instance of LogIngestionAudience for Azure US Government Cloud.
     * @return LogIngestionAudience for Azure US Government Cloud.
     */
    public static LogIngestionAudience azureUSGovernment() {
        return new LogIngestionAudience(AZURE_US_GOV);
    }

    /**
     * Creates an instance of LogIngestionAudience for Azure China Cloud.
     * @return LogIngestionAudience for Azure China Cloud.
     */
    public static LogIngestionAudience azureChina() {
        return new LogIngestionAudience(AZURE_CHINA);
    }

    /**
     * Gets the audience for the log ingestion.
     * @return The audience for the log ingestion.
     */
    public String[] getAudience() {
        return new String[]{audience};
    }

}
