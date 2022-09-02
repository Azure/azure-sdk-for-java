// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.quickpulse;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;

import java.util.concurrent.ArrayBlockingQueue;

class QuickPulseDataSender implements Runnable {

    private final QuickPulseNetworkHelper networkHelper = new QuickPulseNetworkHelper();
    private final HttpPipeline httpPipeline;
    private volatile QuickPulseHeaderInfo quickPulseHeaderInfo;
    private long lastValidTransmission = 0;

    private final ArrayBlockingQueue<HttpRequest> sendQueue;

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
