// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.ingestion.codesnippets;

import com.azure.core.credential.TokenCredential;
import com.azure.core.util.Context;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.monitor.ingestion.LogsIngestionAsyncClient;
import com.azure.monitor.ingestion.LogsIngestionClient;
import com.azure.monitor.ingestion.LogsIngestionClientBuilder;
import com.azure.monitor.ingestion.models.UploadLogsOptions;

import java.util.List;

/**
 * Class containing javadoc code snippets for {@link LogsIngestionClient}.
 */
public class LogsIngestionJavadocCodeSnippets {

    /**
     * Code snippet for creating a {@link LogsIngestionClient}.
     */
    public void instantiation() {
        TokenCredential tokenCredential = new DefaultAzureCredentialBuilder().build();
        // BEGIN: com.azure.monitor.ingestion.LogsIngestionClient.instantiation
        LogsIngestionClient logsIngestionClient = new LogsIngestionClientBuilder()
                .credential(tokenCredential)
                .endpoint("<data-collection-endpoint>")
                .buildClient();
        // END: com.azure.monitor.ingestion.LogsIngestionClient.instantiation

        // BEGIN: com.azure.monitor.ingestion.LogsIngestionAsyncClient.instantiation
        LogsIngestionAsyncClient logsIngestionAsyncClient = new LogsIngestionClientBuilder()
                .credential(tokenCredential)
                .endpoint("<data-collection-endpoint>")
                .buildAsyncClient();
        // END: com.azure.monitor.ingestion.LogsIngestionAsyncClient.instantiation
    }

    public void logsUploadSample() {
        TokenCredential tokenCredential = new DefaultAzureCredentialBuilder().build();
        LogsIngestionClient logsIngestionClient = new LogsIngestionClientBuilder()
                .credential(tokenCredential)
                .endpoint("<data-collection-endpoint>")
                .buildClient();
        // BEGIN: com.azure.monitor.ingestion.LogsIngestionClient.upload
        List<Object> logs = getLogs();
        logsIngestionClient.upload("<data-collection-rule-id>", "<stream-name>", logs);
        System.out.println("Logs uploaded successfully");
        // END: com.azure.monitor.ingestion.LogsIngestionClient.upload
    }

    public void logsUploadWithConcurrencySample() {
        TokenCredential tokenCredential = new DefaultAzureCredentialBuilder().build();
        LogsIngestionClient logsIngestionClient = new LogsIngestionClientBuilder()
                .credential(tokenCredential)
                .endpoint("<data-collection-endpoint>")
                .buildClient();
        // BEGIN: com.azure.monitor.ingestion.LogsIngestionClient.uploadWithConcurrency
        List<Object> logs = getLogs();
        UploadLogsOptions uploadLogsOptions = new UploadLogsOptions().setMaxConcurrency(4);
        logsIngestionClient.upload("<data-collection-rule-id>", "<stream-name>", logs,
                uploadLogsOptions, Context.NONE);
        System.out.println("Logs uploaded successfully");
        // END: com.azure.monitor.ingestion.LogsIngestionClient.uploadWithConcurrency
    }

    public void logsUploadAsyncSample() {

        TokenCredential tokenCredential = new DefaultAzureCredentialBuilder().build();
        LogsIngestionAsyncClient logsIngestionAsyncClient = new LogsIngestionClientBuilder()
                .credential(tokenCredential)
                .endpoint("<data-collection-endpoint>")
                .buildAsyncClient();
        // BEGIN: com.azure.monitor.ingestion.LogsIngestionAsyncClient.upload
        List<Object> logs = getLogs();
        logsIngestionAsyncClient.upload("<data-collection-rule-id>", "<stream-name>", logs)
                .subscribe();
        // END: com.azure.monitor.ingestion.LogsIngestionAsyncClient.upload
    }

    public void logsUploadWithConcurrencyAsyncSample() {
        TokenCredential tokenCredential = new DefaultAzureCredentialBuilder().build();
        LogsIngestionAsyncClient logsIngestionAsyncClient = new LogsIngestionClientBuilder()
                .credential(tokenCredential)
                .endpoint("<data-collection-endpoint>")
                .buildAsyncClient();
        // BEGIN: com.azure.monitor.ingestion.LogsIngestionAsyncClient.uploadWithConcurrency
        List<Object> logs = getLogs();
        UploadLogsOptions uploadLogsOptions = new UploadLogsOptions().setMaxConcurrency(4);
        logsIngestionAsyncClient.upload("<data-collection-rule-id>", "<stream-name>", logs, uploadLogsOptions)
                .subscribe();
        // END: com.azure.monitor.ingestion.LogsIngestionAsyncClient.uploadWithConcurrency
    }

    private List<Object> getLogs() {
        return null;
    }
}
