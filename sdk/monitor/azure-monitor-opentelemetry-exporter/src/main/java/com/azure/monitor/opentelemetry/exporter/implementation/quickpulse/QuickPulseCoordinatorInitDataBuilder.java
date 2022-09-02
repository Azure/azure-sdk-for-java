/*
 * ApplicationInsights-Java
 * Copyright (c) Microsoft Corporation
 * All rights reserved.
 *
 * MIT License
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the ""Software""), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify, merge,
 * publish, distribute, sublicense, and/or sell copies of the Software, and to permit
 * persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED *AS IS*, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package com.azure.monitor.opentelemetry.exporter.implementation.quickpulse;

final class QuickPulseCoordinatorInitDataBuilder {
    private static final long DEFAULT_WAIT_BETWEEN_PING_IN_MILLIS = 5000;
    private static final long DEFAULT_WAIT_BETWEEN_POSTS_IN_MILLIS = 1000;
    private static final long DEFAULT_WAIT_BETWEEN_PINGS_AFTER_ERROR_IN_MILLIS = 60000;

    private QuickPulsePingSender pingSender;
    private QuickPulseDataSender dataSender;
    private QuickPulseDataFetcher dataFetcher;
    private QuickPulseDataCollector collector;

    private long waitBetweenPingsInMillis = DEFAULT_WAIT_BETWEEN_PING_IN_MILLIS;
    private long waitBetweenPostsInMillis = DEFAULT_WAIT_BETWEEN_POSTS_IN_MILLIS;
    private long waitOnErrorInMillis = DEFAULT_WAIT_BETWEEN_PINGS_AFTER_ERROR_IN_MILLIS;

    QuickPulseCoordinatorInitDataBuilder withWaitBetweenPingsInMillis(long waitBetweenPingsInMillis) {
        this.waitBetweenPingsInMillis = waitBetweenPingsInMillis;
        return this;
    }

    QuickPulseCoordinatorInitDataBuilder withWaitBetweenPostsInMillis(long waitBetweenPostsInMillis) {
        this.waitBetweenPostsInMillis = waitBetweenPostsInMillis;
        return this;
    }

    QuickPulseCoordinatorInitDataBuilder withWaitOnErrorInMillis(long waitOnErrorInMillis) {
        this.waitOnErrorInMillis = waitOnErrorInMillis;
        return this;
    }

    QuickPulseCoordinatorInitDataBuilder withPingSender(QuickPulsePingSender pingSender) {
        this.pingSender = pingSender;
        return this;
    }

    QuickPulseCoordinatorInitDataBuilder withDataSender(QuickPulseDataSender dataSender) {
        this.dataSender = dataSender;
        return this;
    }

    QuickPulseCoordinatorInitDataBuilder withDataFetcher(QuickPulseDataFetcher dataFetcher) {
        this.dataFetcher = dataFetcher;
        return this;
    }

    QuickPulseCoordinatorInitDataBuilder withCollector(QuickPulseDataCollector collector) {
        this.collector = collector;
        return this;
    }

    QuickPulseCoordinatorInitData build() {
        if (pingSender == null) {
            throw new NullPointerException("ping sender should not be null");
        }
        if (dataFetcher == null) {
            throw new NullPointerException("data fetcher should not be null");
        }
        if (dataSender == null) {
            throw new NullPointerException("data sender should not be null");
        }

        return new QuickPulseCoordinatorInitData(
            pingSender,
            dataFetcher,
            dataSender,
            collector,
            waitBetweenPingsInMillis,
            waitBetweenPostsInMillis,
            waitOnErrorInMillis);
    }
}
