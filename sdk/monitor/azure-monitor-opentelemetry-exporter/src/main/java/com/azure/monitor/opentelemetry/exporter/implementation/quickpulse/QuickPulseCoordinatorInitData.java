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

final class QuickPulseCoordinatorInitData {
    final QuickPulsePingSender pingSender;
    final QuickPulseDataFetcher dataFetcher;
    final QuickPulseDataSender dataSender;
    final QuickPulseDataCollector collector;

    final long waitBetweenPingsInMillis;
    final long waitBetweenPostsInMillis;
    final long waitOnErrorInMillis;

    QuickPulseCoordinatorInitData(
        QuickPulsePingSender pingSender,
        QuickPulseDataFetcher dataFetcher,
        QuickPulseDataSender dataSender,
        QuickPulseDataCollector collector,
        long waitBetweenPingsInMillis,
        long waitBetweenPostsInMillis,
        long waitOnErrorInMillis) {

        this.pingSender = pingSender;
        this.dataFetcher = dataFetcher;
        this.dataSender = dataSender;
        this.collector = collector;
        this.waitBetweenPingsInMillis = waitBetweenPingsInMillis;
        this.waitBetweenPostsInMillis = waitBetweenPostsInMillis;
        this.waitOnErrorInMillis = waitOnErrorInMillis;
    }
}
