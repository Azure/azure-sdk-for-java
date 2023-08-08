// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.ingestion;

import com.azure.core.exception.HttpResponseException;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.monitor.ingestion.models.LogsUploadOptions;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * This sample demonstrates uploading logs to Azure Monitor and configure an error handler to handle any failures
 * when uploading logs to the service.
 */
public class UploadLogsErrorHandlingSample {
    /**
     * Main method to run the sample.
     * @param args ignore args.
     */
    public static void main(String[] args) {
        LogsIngestionClient client = new LogsIngestionClientBuilder()
                .endpoint("<data-collection-endpoint>")
                .credential(new DefaultAzureCredentialBuilder().build())
                .buildClient();

        List<Object> dataList = getLogs();

        // Configure the error handler to inspect HTTP request failure and the logs associated with the failed
        // request. A single client.upload() call can be broken down by the client into smaller HTTP requests, so,
        // this error handler can be called multiple times if there are multiple HTTP request failures.
        LogsUploadOptions logsUploadOptions = new LogsUploadOptions()
                .setLogsUploadErrorConsumer(uploadLogsError -> {
                    HttpResponseException responseException = uploadLogsError.getResponseException();
                    System.out.println(responseException.getMessage());
                    System.out.println("Failed logs count " + uploadLogsError.getFailedLogs().size());
                });
        client.upload("<data-collection-rule-id>", "<stream-name>", dataList, logsUploadOptions);
    }

    private static List<Object> getLogs() {
        List<Object> logs = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            CustomLogData e = new CustomLogData()
                    .setTime(OffsetDateTime.now())
                    .setExtendedColumn("extend column data" + i)
                    .setAdditionalContext("more logs context");
            logs.add(e);
        }
        return logs;
    }
}
