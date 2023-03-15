// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.ingestion.implementation;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.models.ResponseError;
import com.azure.core.util.logging.ClientLogger;
import com.azure.monitor.ingestion.models.LogsUploadOptions;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.LinkedHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPOutputStream;

public final class Utils {
    private static final ClientLogger LOGGER = new ClientLogger(Utils.class);
    public static final long MAX_REQUEST_PAYLOAD_SIZE = 1024 * 1024; // 1 MB
    public static final String CONTENT_ENCODING = "Content-Encoding";
    public static final String GZIP = "gzip";
    private Utils() {
    }

    /**
     * Method to map the exception to {@link ResponseError}.
     * @param ex the {@link HttpResponseException}.
     * @return the mapped {@link ResponseError}.
     */
    public static ResponseError mapToResponseError(HttpResponseException ex) {
        ResponseError responseError = null;
        // with DPG clients, the error object is just a LinkedHashMap and should map to the standard error
        // response structure
        // https://github.com/Azure/azure-rest-api-specs/blob/main/specification/common-types/data-plane/v1/types.json#L46
        if (ex.getValue() instanceof LinkedHashMap<?, ?>) {
            @SuppressWarnings("unchecked")
            LinkedHashMap<String, Object> errorMap = (LinkedHashMap<String, Object>) ex.getValue();
            if (errorMap.containsKey("error")) {
                Object error = errorMap.get("error");
                if (error instanceof LinkedHashMap<?, ?>) {
                    @SuppressWarnings("unchecked")
                    LinkedHashMap<String, String> errorDetails = (LinkedHashMap<String, String>) error;
                    if (errorDetails.containsKey("code") && errorDetails.containsKey("message")) {
                        responseError = new ResponseError(errorDetails.get("code"), errorDetails.get("message"));
                    }
                }
            }
        }
        return responseError;
    }

    /**
     * Gzips the input byte array.
     * @param bytes The input byte array.
     * @return gzipped byte array.
     */
    public static byte[] gzipRequest(byte[] bytes) {
        // This should be moved to azure-core and should be enabled when the client library requests for gzipping the
        // request body content.
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (GZIPOutputStream zip = new GZIPOutputStream(byteArrayOutputStream)) {
            zip.write(bytes);
        } catch (IOException exception) {
            throw LOGGER.logExceptionAsError(new UncheckedIOException(exception));
        }
        return byteArrayOutputStream.toByteArray();
    }

    public static int getConcurrency(LogsUploadOptions options) {
        if (options != null && options.getMaxConcurrency() != null) {
            return options.getMaxConcurrency();
        }

        return  1;
    }

    /**
     * Creates cached (that supports scaling) thread pool with shutdown hook to do best-effort graceful termination within timeout.
     * @param timeoutSec Timeout in seconds to wait for tasks to complete or terminate.
     * @return {@link ExecutorService} instance.
     */
    public static ExecutorService getThreadPoolWithShutDownHook(int timeoutSec) {
        ExecutorService threadPool = Executors.newCachedThreadPool();
        registerShutdownHook(threadPool, timeoutSec);
        return threadPool;
    }

    /**
     * Registers {@link ExecutorService} shutdown. First, stops accepting new tasks, then awaits their completion for
     * half of timeout, cancels remaining tasks and waits another half of timeout for them to get cancelled.
     *
     * @param threadPool Thread pool to shut down.
     * @param timeoutSec Timeout in seconds to wait for tasks to complete or terminate.
     * @return hook thread instance that can be used to unregister hook.
     */
    static Thread registerShutdownHook(ExecutorService threadPool, int timeoutSec) {
        // based on https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/ExecutorService.html
        long halfTimeoutNanos = TimeUnit.SECONDS.toNanos(timeoutSec) / 2;
        Thread hook = new Thread(() -> {
            try {
                threadPool.shutdown();
                if (!threadPool.awaitTermination(halfTimeoutNanos, TimeUnit.NANOSECONDS)) {
                    threadPool.shutdownNow();
                    threadPool.awaitTermination(halfTimeoutNanos, TimeUnit.NANOSECONDS);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                threadPool.shutdownNow();
            }
        });
        Runtime.getRuntime().addShutdownHook(hook);
        return hook;
    }
}
