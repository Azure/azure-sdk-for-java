// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.ingestion;

import com.azure.core.util.Context;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.monitor.ingestion.models.UploadLogsOptions;
import com.azure.monitor.ingestion.models.UploadLogsResult;

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
        UploadLogsResult result = client.upload("<data-collection-rule-id>", "<stream-name>", logs);
        System.out.println("Logs upload result status " + result.getStatus());
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
        UploadLogsOptions uploadLogsOptions = new UploadLogsOptions()
                .setMaxConcurrency(3);
        UploadLogsResult result = client.upload("<data-collection-rule-id>", "<stream-name>", logs, uploadLogsOptions,
                Context.NONE);
        System.out.println("Logs upload result status " + result.getStatus());
        // END: readme-sample-uploadLogsWithMaxConcurrency
    }

    private List<Object> getLogs() {
        return null;
    }
}
