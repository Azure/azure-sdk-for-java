// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.perf.test.core;

import com.beust.jcommander.Parameter;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Represents the command line configurable options for a performance test.
 */
@JsonPropertyOrder(alphabetic = true)
public class PerfStressOptions {
    @Parameter(names = { "-d", "--duration" }, description = "duration of test in seconds")
    private int duration = 10;

    @Parameter(names = { "--host" }, description = "Host to redirect HTTP requests")
    private String host;

    @Parameter(names = { "--insecure" }, description = "Allow untrusted SSL server certs")
    private boolean insecure = false;

    @Parameter(names = { "-i", "--iterations" }, description = "Number of iterations of main test loop")
    private int iterations = 1;

    @Parameter(names = { "--no-cleanup" }, description = "Disables test cleanup")
    private boolean noCleanup = false;

    @Parameter(names = { "-p", "--parallel" }, description = "Number of operations to execute in parallel")
    private int parallel = 1;

    @Parameter(names = { "--port" }, description = "port to redirect HTTP requests")
    private int port = -1;

    @Parameter(names = { "-w", "--warmup" }, description = "duration of warmup in seconds")
    private int warmup = 10;

    @Parameter(names = { "--sync" }, description = "Runs sync version of test")
    private boolean sync = false;

    /**
     * Get the configured duration for performance test.
     * @return The duration.
     */
    public int getDuration() {
        return duration;
    }

    /**
     * Get the configured host for performance test.
     * @return The host.
     */
    public String getHost() {
        return host;
    }

    /**
     * Get the host security status for performance test.
     * @return The insecure status.
     */
    public boolean isInsecure() {
        return insecure;
    }

    /**
     * Get the configured iterations for performance test.
     * @return The iterations.
     */
    public int getIterations() {
        return iterations;
    }

    /**
     * Get the cleanup configuration for performance test.
     * @return The cleanup status.
     */
    public boolean isNoCleanup() {
        return noCleanup;
    }

    /**
     * Get the configured parallelism for performance test.
     * @return The parallel threads.
     */
    public int getParallel() {
        return parallel;
    }

    /**
     * Get the configured port for performance test.
     * @return The port.
     */
    public int getPort() {
        return port;
    }

    /**
     * Get the configured warmup for performance test.
     * @return The warm up.
     */
    public int getWarmup() {
        return warmup;
    }

    /**
     * Get the configured synchronous status for performance test.
     * @return The synchronous status.
     */
    public boolean isSync() {
        return sync;
    }
}
