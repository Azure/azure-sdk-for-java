// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.rntbd;

import com.azure.cosmos.implementation.ConsoleLoggingRegistryFactory;
import com.azure.cosmos.implementation.directconnectivity.RntbdTransportClient;
import com.azure.cosmos.implementation.guava25.net.PercentEscaper;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import io.micrometer.core.instrument.distribution.HistogramSnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("UnstableApiUsage")
@JsonPropertyOrder({
    "tags", "concurrentRequests", "requests", "responseErrors", "responseSuccesses", "completionRate", "responseRate",
    "requestSize", "responseSize", "channelsAcquired", "channelsAvailable", "requestQueueLength", "usedDirectMemory",
    "usedHeapMemory"
})
public final class RntbdMetrics implements RntbdMetricsCompletionRecorder {

    // region Fields

    private static final PercentEscaper PERCENT_ESCAPER = new PercentEscaper("_-", false);

    private static final Logger logger = LoggerFactory.getLogger(RntbdMetrics.class);
    private static final CompositeMeterRegistry registry = new CompositeMeterRegistry();

    private static boolean hasAnyRegistry = false;

    static {
        try {
            int step = Integer.getInteger("azure.cosmos.monitoring.consoleLogging.step", 0);
            if (step > 0) {
                RntbdMetrics.add(ConsoleLoggingRegistryFactory.create(step));
            }
        } catch (Throwable throwable) {
            logger.error("failed to initialize console logging registry due to ", throwable);
            if (throwable instanceof Error) {
                throw (Error) throwable;
            }
        }
    }

    private final RntbdEndpoint endpoint;

    private final DistributionSummary requestSize;
    private final Timer requests;
    private final Timer responseErrors;
    private final DistributionSummary responseSize;
    private final Timer responseSuccesses;
    private final Tags tags;
    private final RntbdTransportClient transportClient;

    public static boolean isEmpty() {
        return !hasAnyRegistry;
    }

    // endregion

    // region Constructors

    private RntbdMetrics(RntbdTransportClient client, RntbdEndpoint endpoint) {

        this.transportClient = client;
        this.endpoint = endpoint;

        this.tags = Tags.of(client.tag(), endpoint.tag());

        this.requests = registry.timer(nameOf("requests"), tags);
        this.responseErrors = registry.timer(nameOf("responseErrors"), tags);
        this.responseSuccesses = registry.timer(nameOf("responseSuccesses"), tags);

        Gauge.builder(nameOf("endpoints"), client, RntbdTransportClient::endpointCount)
             .description("endpoint count")
             .tag(client.tag().getKey(), client.tag().getValue())
             .register(registry);

        Gauge.builder(nameOf("endpointsEvicted"), client, RntbdTransportClient::endpointEvictionCount)
             .description("endpoint eviction count")
             .tag(client.tag().getKey(), client.tag().getValue())
             .register(registry);

        Gauge.builder(nameOf("concurrentRequests"), endpoint, RntbdEndpoint::concurrentRequests)
             .description("executing or queued request count")
             .tags(this.tags)
             .register(registry);

        Gauge.builder(nameOf("requestQueueLength"), endpoint, RntbdEndpoint::requestQueueLength)
            .description("queued request count")
            .tags(this.tags)
            .register(registry);

        Gauge.builder(nameOf("channelsAcquired"), endpoint, RntbdEndpoint::channelsAcquiredMetric)
             .description("acquired channel count")
             .tags(this.tags)
             .register(registry);

        Gauge.builder(nameOf("channelsAvailable"), endpoint, RntbdEndpoint::channelsAvailableMetric)
             .description("available channel count")
             .tags(this.tags)
             .register(registry);

        Gauge.builder(nameOf("usedDirectMemory"), endpoint, x -> x.usedDirectMemory())
             .description("Java direct memory usage (MiB)")
             .baseUnit("bytes")
             .tags(this.tags)
             .register(registry);

        Gauge.builder(nameOf("usedHeapMemory"), endpoint, x -> x.usedHeapMemory())
             .description("Java heap memory usage (MiB)")
             .baseUnit("MiB")
             .tags(this.tags)
             .register(registry);

        this.requestSize = DistributionSummary.builder(nameOf("requestSize"))
            .description("Request size (bytes)")
            .baseUnit("bytes")
            .tags(this.tags)
            .register(registry);

        this.responseSize = DistributionSummary.builder(nameOf("responseSize"))
            .description("Response size (bytes)")
            .baseUnit("bytes")
            .tags(this.tags)
            .register(registry);
    }

    static RntbdMetricsCompletionRecorder create(RntbdTransportClient client, RntbdEndpoint endpoint) {
        return new RntbdMetrics(client, endpoint);
    }

    // endregion

    // region Accessors

    public static void add(MeterRegistry registry) {

        hasAnyRegistry = true;
        RntbdMetrics.registry.add(registry);
    }

    @JsonProperty
    public int channelsAcquired() {
        return this.endpoint.channelsAcquiredMetric();
    }

    @JsonProperty
    public int channelsAvailable() {
        return this.endpoint.channelsAvailableMetric();
    }

    /***
     * Computes the number of successful (non-error) responses received divided by the number of completed requests.
     *
     * @return number of successful (non-error) responses received divided by the number of completed requests.
     */
    @JsonProperty
    public double completionRate() {
        return this.responseSuccesses.count() / (double) this.requests.count();
    }

    @JsonProperty
    public long concurrentRequests() {
        return this.endpoint.concurrentRequests();
    }

    @JsonProperty
    public int endpoints() {
        return this.transportClient.endpointCount();
    }

    @JsonProperty
    public int requestQueueLength() {
        return this.endpoint.requestQueueLength();
    }

    @JsonProperty
    public HistogramSnapshot requestSize() {
        return this.requestSize.takeSnapshot();
    }

    @JsonProperty
    public HistogramSnapshot requests() {
        return this.requests.takeSnapshot();
    }

    @JsonProperty
    public HistogramSnapshot responseErrors() {
        return this.responseErrors.takeSnapshot();
    }

    /***
     * Computes the number of successful (non-error) responses received divided by the number of requests sent
     *
     * @return The number of successful (non-error) responses received divided by the number of requests sent
     */
    @JsonProperty
    public double responseRate() {
        return this.responseSuccesses.count() / (double) (this.requests.count() + this.endpoint.concurrentRequests());
    }

    @JsonProperty
    public HistogramSnapshot responseSize() {
        return this.responseSize.takeSnapshot();
    }

    @JsonProperty
    public HistogramSnapshot responseSuccesses() {
        return this.responseSuccesses.takeSnapshot();
    }

    @JsonProperty
    public Iterable<Tag> tags() {
        return this.tags;
    }

    @JsonProperty
    public long usedDirectMemory() {
        return this.endpoint.usedDirectMemory();
    }

    @JsonProperty
    public long usedHeapMemory() {
        return this.endpoint.usedHeapMemory();
    }

    // endregion

    // region Methods

    public void markComplete(RntbdRequestRecord requestRecord) {
        requestRecord.stop(this.requests, requestRecord.isCompletedExceptionally()
            ? this.responseErrors
            : this.responseSuccesses);

        if (!requestRecord.isCancelled()) {
            this.requestSize.record(requestRecord.requestLength());
            this.responseSize.record(requestRecord.responseLength());
        }
    }

    @Override
    public String toString() {
        return RntbdObjectMapper.toString(this);
    }

    // endregion

    // region Private

    static String escape(String value) {
        return PERCENT_ESCAPER.escape(value);
    }

    private static String nameOf(final String member) {
        return "azure.cosmos.directTcp." + member;
    }

    // endregion
}
