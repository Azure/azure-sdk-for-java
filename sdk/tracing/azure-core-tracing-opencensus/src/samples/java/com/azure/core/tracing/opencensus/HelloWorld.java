// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.tracing.opencensus;

import com.azure.core.util.Context;
import com.azure.storage.queue.QueueClient;
import com.azure.storage.queue.QueueClientBuilder;
import io.opencensus.common.Scope;
import io.opencensus.exporter.trace.zipkin.ZipkinTraceExporter;
import io.opencensus.trace.Tracer;
import io.opencensus.trace.Tracing;
import io.opencensus.trace.config.TraceConfig;
import io.opencensus.trace.config.TraceParams;
import io.opencensus.trace.samplers.Samplers;

import static com.azure.core.util.tracing.Tracer.OPENCENSUS_SPAN_KEY;

/*
 *  This example shows tracing support in azure-storage-queue sdk using azure-core-tracing plugin package.
 */
public class HelloWorld {
    private static final String ACCOUNT_NAME = System.getenv("AZURE_STORAGE_ACCOUNT_NAME");
    private static final String SAS_TOKEN = System.getenv("PRIMARY_SAS_TOKEN");
    private static final String QUEUE_NAME = "queue_name";

    /**
     * The main method shows how we do the basic operations of enqueueing and dequeueing messages on async queue client
     * with tracing enabled support. This example relies on user the having started a zipkin exporter localhost on port
     * 9411.
     *
     * Please refer to the  <a href=https://zipkin.io/pages/quickstart>Quickstart Zipkin</a> for more documentation on
     * using a zipkin exporter.
     *
     * @param args No args needed for main method.
     */
    public static void main(String[] args) {
        ZipkinTraceExporter.createAndRegister("http://localhost:9411/api/v2/spans", "tracing-to-zipkin-service");

        TraceConfig traceConfig = Tracing.getTraceConfig();
        TraceParams activeTraceParams = traceConfig.getActiveTraceParams();
        traceConfig.updateActiveTraceParams(activeTraceParams.toBuilder().setSampler(Samplers.alwaysSample()).build());

        Tracer tracer = Tracing.getTracer();

        Scope scope = tracer.spanBuilder("user-parent-span").startScopedSpan();
        try {
            QueueClient queueClient = new QueueClientBuilder()
                .endpoint(String.format("https://%s.queue.core.windows.net/%s%s", ACCOUNT_NAME, QUEUE_NAME, SAS_TOKEN))
                .buildClient();

            // Create a queue, enqueue two messages.
            Context tracingContext = new Context(OPENCENSUS_SPAN_KEY, tracer.getCurrentSpan());
            queueClient.createWithResponse(null, null, tracingContext);
            queueClient.enqueueMessageWithResponse("This is message 1", null, null, null, tracingContext);
            queueClient.enqueueMessageWithResponse("This is message 2", null, null, null, tracingContext);
            System.out.println("Enqueuing of messages has completed!");
        } finally {
            scope.close();
        }

        Tracing.getExportComponent().shutdown();
    }
}
