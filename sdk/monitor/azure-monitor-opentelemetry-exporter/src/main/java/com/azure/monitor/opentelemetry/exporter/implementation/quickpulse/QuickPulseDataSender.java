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

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;

import java.util.concurrent.ArrayBlockingQueue;

class QuickPulseDataSender implements Runnable {

    private final QuickPulseNetworkHelper networkHelper = new QuickPulseNetworkHelper();
    private final HttpPipeline httpPipeline;
    private final ArrayBlockingQueue<HttpRequest> sendQueue;
    private volatile QuickPulseHeaderInfo quickPulseHeaderInfo;
    private long lastValidTransmission = 0;

    QuickPulseDataSender(HttpPipeline httpPipeline, ArrayBlockingQueue<HttpRequest> sendQueue) {
        this.httpPipeline = httpPipeline;
        this.sendQueue = sendQueue;
    }

    @Override
    public void run() {
        while (true) {
            HttpRequest post;
            try {
                post = sendQueue.take();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            if (quickPulseHeaderInfo.getQuickPulseStatus() != QuickPulseStatus.QP_IS_ON) {
                continue;
            }

            long sendTime = System.nanoTime();
            try (HttpResponse response = httpPipeline.send(post).block()) {
                if (response == null) {
                    // this shouldn't happen, the mono should complete with a response or a failure
                    throw new AssertionError("http response mono returned empty");
                }

                if (networkHelper.isSuccess(response)) {
                    QuickPulseHeaderInfo quickPulseHeaderInfo =
                        networkHelper.getQuickPulseHeaderInfo(response);
                    switch (quickPulseHeaderInfo.getQuickPulseStatus()) {
                        case QP_IS_OFF:
                        case QP_IS_ON:
                            lastValidTransmission = sendTime;
                            this.quickPulseHeaderInfo = quickPulseHeaderInfo;
                            break;

                        case ERROR:
                            onPostError(sendTime);
                            break;
                    }
                }
            }
        }
    }

    void startSending() {
        quickPulseHeaderInfo = new QuickPulseHeaderInfo(QuickPulseStatus.QP_IS_ON);
    }

    QuickPulseHeaderInfo getQuickPulseHeaderInfo() {
        return quickPulseHeaderInfo;
    }

    private void onPostError(long sendTime) {
        double timeFromLastValidTransmission = (sendTime - lastValidTransmission) / 1000000000.0;
        if (timeFromLastValidTransmission >= 20.0) {
            quickPulseHeaderInfo = new QuickPulseHeaderInfo(QuickPulseStatus.ERROR);
        }
    }
}
