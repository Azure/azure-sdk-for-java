// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.perf.test.core;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.converters.IParameterSplitter;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Represents the command line configurable options for a performance test.
 */
@JsonPropertyOrder(alphabetic = true)
public class PerfStressOptions {
    @Parameter(names = { "-d", "--duration" }, description = "duration of test in seconds")
    private int duration = 15;

    @Parameter(names = { "--insecure" }, description = "Allow untrusted SSL server certs")
    private boolean insecure = false;

    @Parameter(names = { "-x", "--test-proxies" }, splitter = SemiColonSplitter.class, description = "URIs of TestProxy Servers (separated by ';')")
    private List<URI> testProxies;

    @Parameter(names = { "-i", "--iterations" }, description = "Number of iterations of main test loop")
    private int iterations = 1;

    @Parameter(names = { "--no-cleanup" }, description = "Disables test cleanup")
    private boolean noCleanup = false;

    @Parameter(names = { "-p", "--parallel" }, description = "Number of operations to execute in parallel")
    private int parallel = 1;

    @Parameter(names = { "-w", "--warmup" }, description = "duration of warmup in seconds")
    private int warmup = 15;

    @Parameter(names = { "--sync" }, description = "Runs sync version of test")
    private boolean sync = false;

    @Parameter(names = { "-s", "--size" }, description = "Size of payload (in bytes)")
    private long size = 10 * 1024;

    @Parameter(names = { "-c", "--count" }, description = "Number of items")
    private int count = 10;

    @Parameter(names = { "--http-client" }, description = "The http client to use. Can be netty, okhttp.")
    private HttpClientType httpClient = HttpClientType.NETTY;

    /**
     * Get the configured count for performance test.
     * @return The count.
     */
    public int getCount() {
        return count;
    }

    /**
     * Get the configured size option for performance test.
     * @return The size.
     */
    public long getSize() {
        return size;
    }

    /**
     * Get the configured duration for performance test.
     * @return The duration.
     */
    public int getDuration() {
        return duration;
    }

    /**
     * Get the host security status for performance test.
     * @return The insecure status.
     */
    public boolean isInsecure() {
        return insecure;
    }

    /**
     * Get the configured test proxy for performance test.
     * @return The configured test proxy.
     */
    public List<URI> getTestProxies() {
        return testProxies;
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

    /**
     * The http client to use. Can be netty, okhttp.
     * @return The http client to use.
     */
    public HttpClientType getHttpClient() {
        return httpClient;
    }

    private static class SemiColonSplitter implements IParameterSplitter {
        public List<String> split(String value) {
            return Arrays.asList(value.split(";"));
        }
    }

    public enum HttpClientType {
        NETTY, OKHTTP
    }
}
