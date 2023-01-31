// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.ingestion;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.monitor.ingestion.models.UploadLogsResult;
import reactor.core.Disposable;
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
        try {

            // More details on Mono<> can be found in the project reactor documentation at :
            // https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Mono.html

            Mono<UploadLogsResult> resultMono = client.upload("<data-collection-rule-id>",
                "<stream-name>", dataList);

            Disposable subscription = resultMono.subscribe(
                uploadLogsResult -> {
                    // Upload operation has finished and the result object is populated.
                    if (uploadLogsResult == null) {
                        throw new RuntimeException();
                    }
                    System.out.println(uploadLogsResult.getStatus());
                },
                error -> {
                    // If any exceptions are throw, they are handled here.
                    throw new RuntimeException("Unexpected error calling upload.", error);
                });

            // Subscribe is not a blocking call, so we wait here so the program does not terminate.
            countdownLatch.await(TIMEOUT.getSeconds(), TimeUnit.SECONDS);

            // Disposing of the subscription will cancel the upload() operation.
            subscription.dispose();
            
        } catch (RuntimeException runtimeException) {
            // RuntimeException can be thrown by calling Mono.block() if an error occurs or if the operation times out.
            // Handling operation timeout, logging and so on would go here.
        }
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
