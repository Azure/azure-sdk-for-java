// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.ingestion.implementation;

import com.azure.core.util.logging.ClientLogger;
import com.azure.monitor.ingestion.models.LogsUploadOptions;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPOutputStream;

public final class Utils {
    public static final long MAX_REQUEST_PAYLOAD_SIZE = 1024 * 1024; // 1 MB
    public static final String GZIP = "gzip";

    private static final ClientLogger LOGGER = new ClientLogger(Utils.class);
    // similarly to Schedulers.DEFAULT_BOUNDED_ELASTIC_SIZE, just puts a limit depending on logical processors count.
    private static final int MAX_CONCURRENCY = 10 * Runtime.getRuntime().availableProcessors();

    private Utils() {
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
     *
     * @return {@link ExecutorService} instance.
     */
    public static ExecutorService createThreadPool() {
        return new ThreadPoolExecutor(0, MAX_CONCURRENCY, 60L, TimeUnit.SECONDS, new SynchronousQueue<>());
    }

    /**
     * Registers {@link ExecutorService} shutdown hook which will be called when JVM terminates.
     * First, stops accepting new tasks, then awaits their completion for
     * half of timeout, cancels remaining tasks and waits another half of timeout for them to get cancelled.
     *
     * @param threadPool Thread pool to shut down.
     * @param timeoutSec Timeout in seconds to wait for tasks to complete or terminate after JVM starting to shut down.
     * @return hook thread instance that can be used to unregister hook.
     */
    public static Thread registerShutdownHook(ExecutorService threadPool, int timeoutSec) {
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
