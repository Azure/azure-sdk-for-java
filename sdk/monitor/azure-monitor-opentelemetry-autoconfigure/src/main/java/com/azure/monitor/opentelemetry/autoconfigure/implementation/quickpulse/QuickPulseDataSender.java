// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse;

import com.azure.core.http.rest.Response;

import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LogLevel;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.filtering.FilteringConfiguration;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.LiveMetricsRestAPIsForClientSDKs;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.CollectionConfigurationInfo;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.MonitoringDataPoint;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.PublishHeaders;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.utils.IKeyMasker;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.utils.Strings;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;
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

    private final AtomicReference<FilteringConfiguration> configuration;

    QuickPulseDataSender(LiveMetricsRestAPIsForClientSDKs liveMetricsRestAPIsForClientSDKs,
        ArrayBlockingQueue<MonitoringDataPoint> sendQueue, Supplier<URL> endpointUrl,
        Supplier<String> instrumentationKey, AtomicReference<FilteringConfiguration> configuration) {
        this.sendQueue = sendQueue;
        this.liveMetricsRestAPIsForClientSDKs = liveMetricsRestAPIsForClientSDKs;
        this.endpointUrl = endpointUrl;
        this.qpStatus = QuickPulseStatus.QP_IS_OFF;
        this.instrumentationKey = instrumentationKey;
        this.configuration = configuration;
        logger.verbose("QuickPulseDataSender initialized with endpointUrl: {}, instrumentationKey: {}",
            Objects.toString(endpointUrl.get()), Objects.toString(IKeyMasker.mask(instrumentationKey.get())));
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
            // should not include "QuickPulseService.svc/"
            String endpointPrefix
                = Strings.isNullOrEmpty(redirectEndpointPrefix) ? getQuickPulseEndpoint() : redirectEndpointPrefix;
            // TODO (harskaur): for a future PR revisit caching & retry mechanism for failed post requests (shouldn't retry), send "cached" data points in the next post
            List<MonitoringDataPoint> dataPointList = new ArrayList<>();
            dataPointList.add(point);
            Date currentDate = new Date();
            long transmissionTimeInTicks = currentDate.getTime() * 10000 + TICKS_AT_EPOCH;
            String etag = configuration.get().getETag();

            if (logger.canLogAtLevel(LogLevel.VERBOSE)) {
                logger.verbose("Attempting to send data points to quickpulse with etag {}: {}", etag,
                    printListOfMonitoringPoints(dataPointList));
            }

            try {
                // the swagger will add on the QuickPulseService.svc/ when creating the request.
                logger.verbose("About to publish to quickpulse with the endpoint prefix: {}", endpointPrefix);
                Response<CollectionConfigurationInfo> responseMono = liveMetricsRestAPIsForClientSDKs
                    .publishNoCustomHeadersWithResponseAsync(endpointPrefix, instrumentationKey.get(), etag,
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
                CollectionConfigurationInfo body = responseMono.getValue();
                if (body != null && !etag.equals(body.getETag())) {
                    configuration.set(new FilteringConfiguration(body));
                    try {
                        logger.verbose("Received a new live metrics filtering configuration from post response: {}",
                            body.toJsonString());
                    } catch (IOException e) {
                        logger.verbose(e.getMessage());
                    }
                }

            } catch (RuntimeException e) { // this includes ServiceErrorException & RuntimeException thrown from quickpulse post api
                onPostError(sendTime);
                logger.error("QuickPulseDataSender received an error while attempting to send data to quickpulse {}",
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

    private String printListOfMonitoringPoints(List<MonitoringDataPoint> points) {
        StringBuilder dataPointsPrint = new StringBuilder("[");
        for (MonitoringDataPoint p : points) {
            try {
                dataPointsPrint.append(p.toJsonString());
                dataPointsPrint.append("\n");
            } catch (IOException e) {
                logger.verbose(e.getMessage());
            }
        }
        dataPointsPrint.append("]");
        return dataPointsPrint.toString();
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
