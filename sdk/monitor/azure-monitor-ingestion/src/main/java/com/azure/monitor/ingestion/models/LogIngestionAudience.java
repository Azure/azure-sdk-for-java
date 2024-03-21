// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.ingestion.models;

public class LogIngestionAudience {

    private final String audience;

    private static final String AZURE_PUBLIC = "https://monitor.azure.com//.default";
    private static final String AZURE_US_GOV = "https://monitor.azure.us//.default";
    private static final String AZURE_CHINA = "https://monitor.azure.cn//.default";

    /**
     * Creates an instance of LogIngestionAudience.
     * @param audience The audience for the log ingestion.
     */
    LogIngestionAudience(String audience) {
        this.audience = audience;
    }

    public static LogIngestionAudience AzurePublic() {
        return new LogIngestionAudience(AZURE_PUBLIC);
    }

    public static LogIngestionAudience AzureUSGovernment() {
        return new LogIngestionAudience(AZURE_US_GOV);
    }

    public static LogIngestionAudience AzureChina() {
        return new LogIngestionAudience(AZURE_CHINA);
    }

    public String[] getAudience() {
        return new String[]{audience};
    }

}
