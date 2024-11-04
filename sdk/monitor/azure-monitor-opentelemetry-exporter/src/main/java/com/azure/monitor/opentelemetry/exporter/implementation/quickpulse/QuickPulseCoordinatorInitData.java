// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.quickpulse;

final class QuickPulseCoordinatorInitData {
    final QuickPulsePingSender pingSender;
    final QuickPulseDataFetcher dataFetcher;
    final QuickPulseDataSender dataSender;
    final QuickPulseDataCollector collector;

    final long waitBetweenPingsInMillis;
    final long waitBetweenPostsInMillis;
    final long waitOnErrorInMillis;

    QuickPulseCoordinatorInitData(QuickPulsePingSender pingSender, QuickPulseDataFetcher dataFetcher,
        QuickPulseDataSender dataSender, QuickPulseDataCollector collector, long waitBetweenPingsInMillis,
        long waitBetweenPostsInMillis, long waitOnErrorInMillis) {

        this.pingSender = pingSender;
        this.dataFetcher = dataFetcher;
        this.dataSender = dataSender;
        this.collector = collector;
        this.waitBetweenPingsInMillis = waitBetweenPingsInMillis;
        this.waitBetweenPostsInMillis = waitBetweenPostsInMillis;
        this.waitOnErrorInMillis = waitOnErrorInMillis;
    }
}
