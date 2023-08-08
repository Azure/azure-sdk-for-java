// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.ingestion;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.monitor.ingestion.models.LogsUploadException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Sample to demonstrate uploading logs to Azure Monitor using the Async client.
 */
public class UploadLogsAsyncClientSample {

    private static final Duration TIMEOUT = Duration.ofSeconds(10);
    /**
     * Main method to run the sample.
     * @param args ignore args.
     */
    public static void main(String[] args) throws InterruptedException {
        UploadLogsAsyncClientSample sample = new UploadLogsAsyncClientSample();
        sample.run();
    }

    private void run() throws InterruptedException {
        LogsIngestionAsyncClient client = new LogsIngestionClientBuilder()
            .endpoint("<data-collection-endpoint>")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildAsyncClient();

        CountDownLatch countdownLatch = new CountDownLatch(1);
        List<Object> dataList = getLogs();
        // More details on Mono<> can be found in the project reactor documentation at :
        // https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Mono.html
        Mono<Void> resultMono = client.upload("<data-collection-rule-id>",
                "<stream-name>", dataList);

        resultMono.subscribe(
                ignored -> {
                    // returns void
                },
                error -> {
                    // If any exceptions are thrown, they are handled here.
                    if (error instanceof LogsUploadException) {
                        LogsUploadException ex = (LogsUploadException) error;
                        System.out.println("Failed to upload " + ex.getFailedLogsCount() + "logs.");
                    }
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
