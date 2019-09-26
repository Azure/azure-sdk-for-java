// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.tracing.opencensus;

import com.azure.storage.queue.QueueAsyncClient;
import com.azure.storage.queue.QueueClientBuilder;
import io.opencensus.common.Scope;
import io.opencensus.exporter.trace.zipkin.ZipkinTraceExporter;
import io.opencensus.trace.Tracer;
import io.opencensus.trace.Tracing;
import io.opencensus.trace.config.TraceConfig;
import io.opencensus.trace.config.TraceParams;
import io.opencensus.trace.samplers.Samplers;
import reactor.util.context.Context;

import java.util.concurrent.Semaphore;

import static com.azure.core.util.tracing.Tracer.OPENCENSUS_SPAN_KEY;

/*
 *  This example shows tracing support in azure-storage-queue SDK using azure-core-tracing plugin package.
 */
public class HelloWorldAsync {
    private static final String ACCOUNT_NAME = System.getenv("AZURE_STORAGE_ACCOUNT_NAME");
    private static final String QUEUE_NAME = "queue_name";

    /**
     * The main method shows how we do the basic operations of enqueueing and dequeueing messages on async queue client
     * with tracing enabled support. This example relies on user the having started a Zipkin exporter localhost on port
     * 9411.
     *
     * Please refer to the <a href=https://zipkin.io/pages/quickstart>Quickstart Zipkin</a> for more documentation on
     * using a Zipkin exporter.
     *
     * @param args No args needed for main method.
     * @throws InterruptedException when the thread is interrupted in sleep mode.
     */
    public static void main(String[] args) throws InterruptedException {
        ZipkinTraceExporter.createAndRegister("http://localhost:9411/api/v2/spans", "tracing-to-zipkin-service");

        TraceConfig traceConfig = Tracing.getTraceConfig();
        TraceParams activeTraceParams = traceConfig.getActiveTraceParams();
        traceConfig.updateActiveTraceParams(activeTraceParams.toBuilder().setSampler(Samplers.alwaysSample()).build());

        Tracer tracer = Tracing.getTracer();
        Semaphore semaphore = new Semaphore(1);
        Scope scope = tracer.spanBuilder("user-parent-span").startScopedSpan();

        semaphore.acquire();
        String queueURL = String.format("https://%s.queue.core.windows.net/%s%s", ACCOUNT_NAME, QUEUE_NAME, "<GENERATED_SAS_TOKEN>");
        QueueAsyncClient queueAsyncClient = new QueueClientBuilder().endpoint(queueURL).buildAsyncClient();

        // Create a queue, enqueue two messages.
        queueAsyncClient.create()
            .doOnSuccess(response -> queueAsyncClient.enqueueMessage("This is message 1"))
            .then(queueAsyncClient.enqueueMessage("This is message 2"))
            .subscriberContext(Context.of(OPENCENSUS_SPAN_KEY, tracer.getCurrentSpan()))
            .subscribe(
                response -> System.out.printf("Message successfully enqueued by queueAsyncClient. Message id: %s%n",
                    response.getMessageId()),
                err -> {
                    System.out.printf("Error thrown when enqueue the message. Error message: %s%n",
                        err.getMessage());
                    scope.close();
                    semaphore.release();
                },
                () -> {
                    scope.close();
                    semaphore.release();
                    System.out.println("The enqueue has been completed.");
                    Tracing.getExportComponent().shutdown();
                });
    }
}
