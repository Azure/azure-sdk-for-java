// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.quickpulse;

//import com.azure.core.http.HttpPipeline;
//import com.azure.core.http.HttpRequest;
//import com.azure.core.http.HttpResponse;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.rest.Response;
//import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.monitor.opentelemetry.exporter.implementation.quickpulse.model.swagger.LiveMetricsRestAPIsForClientSDKs;
import com.azure.monitor.opentelemetry.exporter.implementation.quickpulse.model.swagger.models.CollectionConfigurationInfo;
import com.azure.monitor.opentelemetry.exporter.implementation.quickpulse.model.swagger.models.IsSubscribedHeaders;
import com.azure.monitor.opentelemetry.exporter.implementation.quickpulse.model.swagger.models.MonitoringDataPoint;
import com.azure.monitor.opentelemetry.exporter.implementation.quickpulse.model.swagger.models.PublishHeaders;
import com.azure.monitor.opentelemetry.exporter.implementation.utils.Strings;
import reactor.core.publisher.Mono;

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.function.Supplier;

class QuickPulseDataSender implements Runnable {

    private static final ClientLogger logger = new ClientLogger(QuickPulseCoordinator.class);

    //private final QuickPulseNetworkHelper networkHelper = new QuickPulseNetworkHelper();
    //private final HttpPipeline httpPipeline; // TODO: remove if not needed
    private volatile PublishHeaders postResponseHeaders;
    private long lastValidTransmission = 0;

    private final ArrayBlockingQueue<MonitoringDataPoint> sendQueue;

    private final LiveMetricsRestAPIsForClientSDKs liveMetricsRestAPIsForClientSDKs;

    private Supplier<URL> endpointUrl;

    private String redirectEndpointPrefix;

    private QuickPulseStatus qpStatus;

    private Supplier<String> instrumentationKey;

    private static final long TICKS_AT_EPOCH = 621355968000000000L;

    QuickPulseDataSender(LiveMetricsRestAPIsForClientSDKs liveMetricsRestAPIsForClientSDKs,  ArrayBlockingQueue<MonitoringDataPoint> sendQueue, Supplier<URL> endpointUrl, Supplier<String> instrumentationKey) {
        this.sendQueue = sendQueue;
        this.liveMetricsRestAPIsForClientSDKs = liveMetricsRestAPIsForClientSDKs;
        this.endpointUrl = endpointUrl;
        this.qpStatus = QuickPulseStatus.QP_IS_OFF;
        this.instrumentationKey = instrumentationKey;
    }

    @Override
    public void run() {
        while (true) {
            //HttpRequest post;
            MonitoringDataPoint point;
            try {
                point = sendQueue.take();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("QuickPulseDataSender was interrupted while waiting for a request", e);
                return;
            }
            if (qpStatus != QuickPulseStatus.QP_IS_ON) {
                logger.verbose("QuickPulseDataSender is not sending data because QP is "
                    + qpStatus);
                continue;
            }

            long sendTime = System.nanoTime();
            String endpointPrefix = Strings.isNullOrEmpty(redirectEndpointPrefix) ? getQuickPulseEndpoint() : redirectEndpointPrefix;
            List<MonitoringDataPoint> dataPointList = new ArrayList<>();
            dataPointList.add(point);
            Date currentDate = new Date();
            long transmissionTimeInTicks = currentDate.getTime() * 10000 + TICKS_AT_EPOCH;
            try {
                //TODO: populate the saved etag here for filtering
                Mono<Response<CollectionConfigurationInfo>> responseMono =
                    liveMetricsRestAPIsForClientSDKs.publishNoCustomHeadersWithResponseAsync(endpointPrefix, instrumentationKey.get(), "", transmissionTimeInTicks, dataPointList);
                if (responseMono == null) {
                    // this shouldn't happen, the mono should complete with a response or a failure
                    throw new AssertionError("http response mono returned empty");
                }
                responseMono.doOnNext(response -> { //do on Success or do on Next??
                    PublishHeaders headers = new PublishHeaders(response.getHeaders());
                    String isSubscribed = headers.getXMsQpsSubscribed();
                    this.qpStatus = getQuickPulseStatusFromHeader(isSubscribed);
                    switch(this.qpStatus) {
                        case QP_IS_OFF:
                        case QP_IS_ON:
                            lastValidTransmission = sendTime;
                            //TODO: parse the response body here for filtering
                            break;

                        case ERROR:
                            onPostError(sendTime); // see if this part is a bug in tha main code
                            break;
                    }
                    this.postResponseHeaders = headers;
                });

            } catch (Exception e) {
                logger.error("QuickPulseDataSender failed to send a request", e.getMessage());
            }
            /*try (HttpResponse response = httpPipeline.sendSync(post, Context.NONE)) {
                if (response == null) {
                    // this shouldn't happen, the mono should complete with a response or a failure
                    throw new AssertionError("http response mono returned empty");
                }

                if (networkHelper.isSuccess(response)) {
                    QuickPulseHeaderInfo quickPulseHeaderInfo = networkHelper.getQuickPulseHeaderInfo(response);
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
            } catch (Throwable t) {
                logger.error("QuickPulseDataSender failed to send a request", t);
            }*/
        }
    }

    void startSending() {
        qpStatus = QuickPulseStatus.QP_IS_ON;
    }

    PublishHeaders getPostResponseHeaders() {
        return postResponseHeaders;
    }

    private void onPostError(long sendTime) {
        double timeFromLastValidTransmission = (sendTime - lastValidTransmission) / 1000000000.0;
        if (timeFromLastValidTransmission >= 20.0) {
            qpStatus = QuickPulseStatus.ERROR;
            postResponseHeaders = new PublishHeaders(new HttpHeaders());
        }
    }

    public void setRedirectEndpointPrefix(String endpointPrefix) {
        this.redirectEndpointPrefix = endpointPrefix;
    }

    private String getQuickPulseEndpoint() {
        return endpointUrl.get().toString() + "QuickPulseService.svc";
    }

    private QuickPulseStatus getQuickPulseStatusFromHeader(String headerValue) {
        if (!Strings.isNullOrEmpty(headerValue)) {
            return QuickPulseStatus.ERROR;
        } else if (headerValue.equalsIgnoreCase("true")) {
            return QuickPulseStatus.QP_IS_ON;
        } else {
            return QuickPulseStatus.QP_IS_OFF;
        }
    }
}
