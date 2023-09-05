// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.clienttelemetry;

import com.azure.core.util.Context;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosDiagnosticsContext;
import com.azure.cosmos.CosmosDiagnosticsHandler;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.ConnectionPolicy;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public final class ClientMetricsDiagnosticsHandler implements CosmosDiagnosticsHandler {
    private static final Logger logger = LoggerFactory.getLogger(ClientMetricsDiagnosticsHandler.class);
    private static final int OPERATION_RECORDING_EXECUTOR_THREAD_MIN_COUNT = 1;
    private static final int OPERATION_RECORDING_EXECUTOR_THREAD_MAX_COUNT = Configs.getCPUCnt();
    private static final int OPERATION_RECORDING_EXECUTOR_THREAD_KEEP_ALIVE_TIME_IN_MINUTES = 5;
    private static final int OPERATION_RECORDING_QUEUE_SIZE = 50000;

    private static final ImplementationBridgeHelpers.CosmosClientTelemetryConfigHelper.CosmosClientTelemetryConfigAccessor clientTelemetryConfigAccessor =
        ImplementationBridgeHelpers.CosmosClientTelemetryConfigHelper.getCosmosClientTelemetryConfigAccessor();

    private final CosmosAsyncClient client;
    private final ExecutorService executorService;

    public ClientMetricsDiagnosticsHandler(CosmosAsyncClient client, ConnectionPolicy connectionPolicy) {

        checkNotNull(client, "Argument 'client' must not be null.");
        checkNotNull(connectionPolicy, "Argument 'connectionPolicy' must not be null.");

        this.client = client;

        this.executorService = new ThreadPoolExecutor(
            OPERATION_RECORDING_EXECUTOR_THREAD_MIN_COUNT,
            OPERATION_RECORDING_EXECUTOR_THREAD_MAX_COUNT,
            OPERATION_RECORDING_EXECUTOR_THREAD_KEEP_ALIVE_TIME_IN_MINUTES,
            TimeUnit.MINUTES,
            new LinkedBlockingQueue<>(OPERATION_RECORDING_QUEUE_SIZE),
            (r, executor) -> {
                if (!executor.isShutdown()) {
                    Runnable task = executor.getQueue().poll();
                    executor.execute(r);
                    // TODO: maybe use debug log level
                    logger.warn(
                        "ClientMetricsRecordingQueue is full, dropping metrics reporting for operation {}",
                        task.toString());
                }
            }); // if the task queue being full, then discard the oldest task
    }

    @Override
    public void handleDiagnostics(CosmosDiagnosticsContext diagnosticsContext, Context traceContext) {
        checkNotNull(traceContext, "Argument 'traceContext' must not be null.");

        try {
            this.executorService.submit(new ClientMetricsOperationRecordingTask(this.client, diagnosticsContext));
        } catch (Exception e) {
            logger.error("Record metrics failed for operation {} with exception {}", diagnosticsContext.toString(), e);
        }
    }

    private static class ClientMetricsOperationRecordingTask implements Runnable {
        private final CosmosAsyncClient client;
        private final CosmosDiagnosticsContext diagnosticsContext;

        public ClientMetricsOperationRecordingTask(
            CosmosAsyncClient client,
            CosmosDiagnosticsContext diagnosticsContext) {
            checkNotNull(client, "Argument 'client' must not be null.");
            checkNotNull(diagnosticsContext, "Argument 'diagnosticsContext' must not be null.");

            this.client = client;
            this.diagnosticsContext = diagnosticsContext;
        }

        @Override
        public void run() {
            ClientTelemetryMetrics.recordOperation(this.client, this.diagnosticsContext);
        }

        @Override
        public String toString() {
            return "ClientMetricsOperationRecordingTask{" +
                "diagnosticsContext=" + diagnosticsContext +
                '}';
        }
    }
}

