// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.quickpulse;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

class QuickPulseDataSender implements Runnable {

    private final QuickPulseNetworkHelper networkHelper = new QuickPulseNetworkHelper();
    private QuickPulseConfiguration quickPulseConfiguration = QuickPulseConfiguration.getInstance();
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
                            String etagValue = networkHelper.getEtagHeaderValue(response);
                            if (!Objects.equals(etagValue, quickPulseConfiguration.getEtag())) {
                                ConcurrentHashMap<String, QuickPulseConfiguration.OpenTelMetricInfo> otelMetrics = quickPulseConfiguration.parseMetrics(response);
                                quickPulseConfiguration.updateConfig(etagValue, otelMetrics);
                            }
                            break;

                        case ERROR:
                            onPostError(sendTime);
                            break;
                    }
                }

//                String etagValue = networkHelper.getEtagHeaderValue(response);
//                ArrayList<QuickPulseConfiguration.OpenTelMetricInfo> otelMetrics = quickPulseConfiguration.parseMetrics(response);
//                quickPulseConfiguration.updateConfig(etagValue, otelMetrics);



            }
            System.out.println("POST*********************");
            System.out.println("ETAG: " + quickPulseConfiguration.getEtag());
            System.out.println("METRICS: " + quickPulseConfiguration.getMetrics());
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
