package com.azure.perf.test.core;

import com.azure.core.util.ExpandableStringEnum;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.converters.IParameterSplitter;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Represents the command line configurable options for a performance test.
 */
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

    @Parameter(names = { "--http-client" }, description = "The http client to use. Can be netty, okhttp, jdk, vertx or a full name of HttpClientProvider implementation class.")
    private String httpClient = HttpClientType.NETTY.toString();

    @Parameter(names = { "--completeablefuture" }, help = true, description = "Runs the performance test asynchronously as a CompletableFuture.")
    private boolean completeablefuture = false;

    @Parameter(names = { "--executorservice" }, help = true, description = "Runs the performance test asynchronously with an ExecutorService.")
    private boolean executorservice = false;

    @Parameter(names = { "--virtualthread" }, help = true, description = "Runs the performance test asynchronously with a virtual thread.")
    private boolean virtualthread = false;

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
     * Get the configured CompletableFuture status for performance test.
     * @return The CompletableFuture status.
     */
    public boolean isCompletableFuture() {
        return completeablefuture;
    }

    /**
     * Get the configured ExecutorService status for performance test.
     * @return The ExecutorService status.
     */
    public boolean isExecutorService() {
        return executorservice;
    }

    /**
     * Get the configured VirtualThread status for performance test.
     * @return The VirtualThread status.
     */
    public boolean isVirtualThread() {
        return virtualthread;
    }

    /**
     * The http client to use. Can be netty, okhttp.
     * @return The http client to use.
     */
    public HttpClientType getHttpClient() {
        return HttpClientType.fromString(httpClient);
    }

    private static class SemiColonSplitter implements IParameterSplitter {
        public List<String> split(String value) {
            return Arrays.asList(value.split(";"));
        }
    }

    public static class HttpClientType extends ExpandableStringEnum<HttpClientType> {
        public static final HttpClientType NETTY = fromString("netty", HttpClientType.class);
        public static final HttpClientType OKHTTP = fromString("okhttp", HttpClientType.class);
        public static final HttpClientType JDK = fromString("jdk", HttpClientType.class);
        public static final HttpClientType VERTX = fromString("vertx", HttpClientType.class);

        public static HttpClientType fromString(String name) {
            return fromString(name, HttpClientType.class);
        }

        public static Collection<HttpClientType> values() {
            return values(HttpClientType.class);
        }

        @Deprecated
        public HttpClientType() {
        }
    }
}
