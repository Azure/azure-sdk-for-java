// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.ingestion;

import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.util.Context;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.monitor.ingestion.models.LogsUploadOptions;

import java.util.List;

/**
 * Class to include all the README.md code samples.
 */
public final class ReadmeSamples {

    /**
     * Sample to demonstrate creation of a synchronous client.
     */
    public void createClient() {
        // BEGIN: readme-sample-createLogsIngestionClient
        DefaultAzureCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

        LogsIngestionClient client = new LogsIngestionClientBuilder()
                .endpoint("<data-collection-endpoint>")
                .credential(tokenCredential)
                .buildClient();
        // END: readme-sample-createLogsIngestionClient
    }

    /**
     * Sample to demonstrate creation of an asynchronous client.
     */
    public void createAsyncClient() {
        // BEGIN: readme-sample-createLogsIngestionAsyncClient
        DefaultAzureCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

        LogsIngestionAsyncClient asyncClient = new LogsIngestionClientBuilder()
                .endpoint("<data-collection-endpoint>")
                .credential(tokenCredential)
                .buildAsyncClient();
        // END: readme-sample-createLogsIngestionAsyncClient
    }

    /**
     * Sample to demonstrate uploading logs to Azure Monitor.
     */
    public void uploadLogs() {
        // BEGIN: readme-sample-uploadLogs
        DefaultAzureCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

        LogsIngestionClient client = new LogsIngestionClientBuilder()
                .endpoint("<data-collection-endpoint")
                .credential(tokenCredential)
                .buildClient();

        List<Object> logs = getLogs();
        client.upload("<data-collection-rule-id>", "<stream-name>", logs);
        System.out.println("Logs uploaded successfully");
        // END: readme-sample-uploadLogs
    }

    /**
     * Sample to demonstrate uploading logs to Azure Monitor.
     */
    public void uploadLogsWithMaxConcurrency() {
        // BEGIN: readme-sample-uploadLogsWithMaxConcurrency
        DefaultAzureCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

        LogsIngestionClient client = new LogsIngestionClientBuilder()
                .endpoint("<data-collection-endpoint")
                .credential(tokenCredential)
                .buildClient();

        List<Object> logs = getLogs();
        LogsUploadOptions logsUploadOptions = new LogsUploadOptions()
                .setMaxConcurrency(3);
        client.upload("<data-collection-rule-id>", "<stream-name>", logs, logsUploadOptions,
                Context.NONE);
        System.out.println("Logs uploaded successfully");
        // END: readme-sample-uploadLogsWithMaxConcurrency
    }


    /**
     * Sample to demonstrate uploading logs to Azure Monitor.
     */
    public void uploadLogsWithErrorHandler() {
        // BEGIN: readme-sample-uploadLogs-error-handler
        DefaultAzureCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

        LogsIngestionClient client = new LogsIngestionClientBuilder()
                .endpoint("<data-collection-endpoint")
                .credential(tokenCredential)
                .buildClient();

        List<Object> logs = getLogs();

        LogsUploadOptions logsUploadOptions = new LogsUploadOptions()
                .setLogsUploadErrorConsumer(uploadLogsError -> {
                    System.out.println("Error message " + uploadLogsError.getResponseException().getMessage());
                    System.out.println("Total logs failed to upload = " + uploadLogsError.getFailedLogs().size());

                    // throw the exception here to abort uploading remaining logs
                    // throw uploadLogsError.getResponseException();
                });
        client.upload("<data-collection-rule-id>", "<stream-name>", logs, logsUploadOptions,
                Context.NONE);
        // END: readme-sample-uploadLogs-error-handler
    }

    /**
     * Enable HTTP request and response logging.
     */
    public void tsgEnableHttpLogging() {
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();

        // BEGIN: readme-sample-enablehttplogging
        LogsIngestionClient logsIngestionClient = new LogsIngestionClientBuilder()
            .credential(credential)
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .buildClient();
        // END: readme-sample-enablehttplogging
    }



    private List<Object> getLogs() {
        return null;
    }
}
