// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.ingestion;

import com.azure.core.exception.HttpResponseException;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.monitor.ingestion.models.LogsUploadOptions;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * This sample demonstrates uploading logs to Azure Monitor using the async client and configure an error
 * handler to handle any failures when uploading logs to the service.
 */
public class UploadLogsAsyncClientErrorHandlingSample {
    private static final Duration TIMEOUT = Duration.ofSeconds(10);
    /**
     * Main method to run the sample.
     * @param args ignore args.
     */
    public static void main(String[] args) throws InterruptedException {
        UploadLogsAsyncClientErrorHandlingSample sample = new UploadLogsAsyncClientErrorHandlingSample();
        sample.run();
    }

    private void run() throws InterruptedException {
        LogsIngestionAsyncClient client = new LogsIngestionClientBuilder()
                .endpoint("<data-collection-endpoint>")
                .credential(new DefaultAzureCredentialBuilder().build())
                .buildAsyncClient();

        CountDownLatch countdownLatch = new CountDownLatch(1);
        List<Object> dataList = getLogs();

        // Configure the error handler to inspect HTTP request failure and the logs associated with the failed
        // request. A single client.upload() call can be broken down by the client into smaller HTTP requests, so,
        // this error handler can be called multiple times if there are multiple HTTP request failures.
        LogsUploadOptions uploadLogsOptions = new LogsUploadOptions()
                .setLogsUploadErrorConsumer(uploadLogsError -> {
                    HttpResponseException responseException = uploadLogsError.getResponseException();
                    System.out.println(responseException.getMessage());
                    System.out.println("Failed logs count " + uploadLogsError.getFailedLogs().size());
                });

        // More details on Mono<> can be found in the project reactor documentation at :
        // https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Mono.html
        Mono<Void> resultMono = client.upload("<data-collection-rule-id>",
                "<stream-name>", dataList, uploadLogsOptions);

        resultMono.subscribe(
                ignored -> {
                    // returns void
                },
                error -> {
                    // service errors are handled by the error consumer but if the error consumer throws
                    // an exception or if a required param like data collection rule id is null, upload operation will
                    // abort and the exception will be handled here
                    System.out.println(error.getMessage());
                },
                countdownLatch::countDown);

        // Subscribe is not a blocking call, so we wait here so the program does not terminate.
        countdownLatch.await(TIMEOUT.getSeconds(), TimeUnit.SECONDS);
    }

    private static List<Object> getLogs() {
        List<Object> logs = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            CustomLogData e = new CustomLogData()
                    .setTime(OffsetDateTime.now())
                    .setExtendedColumn("extend column data" + i);
            logs.add(e);
        }
        return logs;
    }
}
