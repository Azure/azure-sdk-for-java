// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse;

import com.azure.core.http.rest.Response;

import com.azure.core.util.logging.ClientLogger;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.LiveMetricsRestAPIsForClientSDKs;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.CollectionConfigurationInfo;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.MonitoringDataPoint;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.PublishHeaders;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.utils.Strings;

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.function.Supplier;

class QuickPulseDataSender implements Runnable {

    private static final ClientLogger logger = new ClientLogger(QuickPulseCoordinator.class);

    private long lastValidRequestTimeNs = 0;

    private final ArrayBlockingQueue<MonitoringDataPoint> sendQueue;

    private final LiveMetricsRestAPIsForClientSDKs liveMetricsRestAPIsForClientSDKs;

    private final Supplier<URL> endpointUrl;

    private String redirectEndpointPrefix;

    private QuickPulseStatus qpStatus;

    private final Supplier<String> instrumentationKey;

    private static final long TICKS_AT_EPOCH = 621355968000000000L;

    QuickPulseDataSender(LiveMetricsRestAPIsForClientSDKs liveMetricsRestAPIsForClientSDKs,
        ArrayBlockingQueue<MonitoringDataPoint> sendQueue, Supplier<URL> endpointUrl,
        Supplier<String> instrumentationKey) {
        this.sendQueue = sendQueue;
        this.liveMetricsRestAPIsForClientSDKs = liveMetricsRestAPIsForClientSDKs;
        this.endpointUrl = endpointUrl;
        this.qpStatus = QuickPulseStatus.QP_IS_OFF;
        this.instrumentationKey = instrumentationKey;
    }

    @Override
    public void run() {
        while (true) {
            MonitoringDataPoint point;
            try {
                point = sendQueue.take();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("QuickPulseDataSender was interrupted while waiting for a request", e);
                return;
            }
            if (qpStatus != QuickPulseStatus.QP_IS_ON) {
                logger.verbose("QuickPulseDataSender is not sending data because QP is " + qpStatus);
                continue;
            }

            long sendTime = System.nanoTime();
            String endpointPrefix
                = Strings.isNullOrEmpty(redirectEndpointPrefix) ? getQuickPulseEndpoint() : redirectEndpointPrefix;
            // TODO (harskaur): for a future PR revisit caching & retry mechanism for failed post requests (shouldn't retry), send "cached" data points in the next post
            List<MonitoringDataPoint> dataPointList = new ArrayList<>();
            dataPointList.add(point);
            Date currentDate = new Date();
            long transmissionTimeInTicks = currentDate.getTime() * 10000 + TICKS_AT_EPOCH;
            try {
                //TODO (harskaur): for a future PR populate the saved etag here for filtering
                Response<CollectionConfigurationInfo> responseMono = liveMetricsRestAPIsForClientSDKs
                    .publishNoCustomHeadersWithResponseAsync(endpointPrefix, instrumentationKey.get(), "",
                        transmissionTimeInTicks, dataPointList)
                    .block();
                if (responseMono == null) {
                    // this shouldn't happen, the mono should complete with a response or a failure
                    throw new AssertionError("http response mono returned empty");
                }
                // If we reach this point the api returned http 200
                PublishHeaders headers = new PublishHeaders(responseMono.getHeaders());
                String isSubscribed = headers.getXMsQpsSubscribed();

                // it is unlikely that we would get a null/empty subscribed header on an http 200
                // but treating that like a not subscribed just in case
                if (Strings.isNullOrEmpty(isSubscribed) || isSubscribed.equalsIgnoreCase("false")) {
                    this.qpStatus = QuickPulseStatus.QP_IS_OFF;
                } else {
                    this.qpStatus = QuickPulseStatus.QP_IS_ON;
                }

                lastValidRequestTimeNs = sendTime;
                //TODO (harskaur): for a future PR parse the response body here for filtering

            } catch (RuntimeException e) { // this includes ServiceErrorException & RuntimeException thrown from quickpulse post api
                onPostError(sendTime);
                logger.error(
                    "QuickPulseDataSender received a service error while attempting to send data to quickpulse {}",
                    e.getMessage());
            }

        }
    }

    void startSending() {
        qpStatus = QuickPulseStatus.QP_IS_ON;
    }

    private void onPostError(long sendTime) {
        double timeFromlastValidRequestTimeNs = (sendTime - lastValidRequestTimeNs) / 1000000000.0;
        if (timeFromlastValidRequestTimeNs >= 20.0) {
            qpStatus = QuickPulseStatus.ERROR;
        }
    }

    public void setRedirectEndpointPrefix(String endpointPrefix) {
        this.redirectEndpointPrefix = endpointPrefix;
    }

    private String getQuickPulseEndpoint() {
        return endpointUrl.get().toString();
    }

    public QuickPulseStatus getQuickPulseStatus() {
        return this.qpStatus;
    }

    public void resetLastValidRequestTimeNs(long lastValidPingTrasmission) {
        this.lastValidRequestTimeNs = lastValidPingTrasmission;
    }

    public long getLastValidPostRequestTimeNs() {
        return this.lastValidRequestTimeNs;
    }
}
