/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package com.azure.data.cosmos.internal.directconnectivity.rntbd;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.RatioGauge;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.common.base.Stopwatch;

import java.time.Duration;

@JsonPropertyOrder({
    "lifetime", "requests", "responses", "errorResponses", "responseRate", "completionRate", "throughput"
})
public final class RntbdMetrics implements AutoCloseable {

    // region Fields

    private static final MetricRegistry registry = new MetricRegistry();

    private final Gauge<Double> completionRate;
    private final Meter errorResponses;
    private final Stopwatch lifetime;
    private final String prefix;
    private final Meter requests;
    private final Gauge<Double> responseRate;
    private final Meter responses;

    // endregion

    // region Constructors

    public RntbdMetrics(final String name) {

        this.lifetime = Stopwatch.createStarted();
        this.prefix = name + '.';

        this.requests = registry.register(this.prefix + "requests", new Meter());
        this.responses = registry.register(this.prefix + "responses", new Meter());
        this.errorResponses = registry.register(this.prefix + "errorResponses", new Meter());
        this.responseRate = registry.register(this.prefix + "responseRate", new ResponseRate(this));
        this.completionRate = registry.register(this.prefix + "completionRate", new CompletionRate(this));
    }

    // endregion

    // region Accessors

    public double getCompletionRate() {
        return this.completionRate.getValue();
    }

    public long getErrorResponses() {
        return this.errorResponses.getCount();
    }

    public double getLifetime() {
        final Duration elapsed = this.lifetime.elapsed();
        return elapsed.getSeconds() + (1E-9D * elapsed.getNano());
    }

    public long getRequests() {
        return this.requests.getCount();
    }

    public double getResponseRate() {
        return this.responseRate.getValue();
    }

    public long getResponses() {
        return this.responses.getCount();
    }

    public double getThroughput() {
        return this.responses.getMeanRate();
    }

    // endregion

    // region Methods

    @Override
    public void close() {
        registry.removeMatching(MetricFilter.startsWith(this.prefix));
    }

    public final void incrementErrorResponseCount() {
        this.errorResponses.mark();
    }

    public final void incrementRequestCount() {
        this.requests.mark();
    }

    public final void incrementResponseCount() {
        this.responses.mark();
    }

    @Override
    public String toString() {
        return RntbdObjectMapper.toJson(this);
    }

    // endregion

    private static final class CompletionRate extends RatioGauge {

        private final RntbdMetrics metrics;

        private CompletionRate(RntbdMetrics metrics) {
            this.metrics = metrics;
        }

        @Override
        protected Ratio getRatio() {
            return Ratio.of(this.metrics.responses.getCount() - this.metrics.errorResponses.getCount(),
                this.metrics.requests.getCount());
        }
    }

    private static final class ResponseRate extends RatioGauge {

        private final RntbdMetrics metrics;

        private ResponseRate(RntbdMetrics metrics) {
            this.metrics = metrics;
        }

        @Override
        protected Ratio getRatio() {
            return Ratio.of(this.metrics.responses.getCount(), this.metrics.requests.getCount());
        }
    }
}
