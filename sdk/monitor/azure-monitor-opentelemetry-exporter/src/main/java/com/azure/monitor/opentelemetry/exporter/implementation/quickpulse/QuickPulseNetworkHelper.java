// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.quickpulse;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.monitor.opentelemetry.exporter.implementation.utils.Strings;

import java.util.Date;

final class QuickPulseNetworkHelper {
    private static final long TICKS_AT_EPOCH = 621355968000000000L;
    private static final HttpHeaderName HEADER_TRANSMISSION_TIME
        = HttpHeaderName.fromString("x-ms-qps-transmission-time");
    private static final HttpHeaderName QPS_STATUS_HEADER = HttpHeaderName.fromString("x-ms-qps-subscribed");
    private static final HttpHeaderName QPS_SERVICE_POLLING_INTERVAL_HINT
        = HttpHeaderName.fromString("x-ms-qps-service-polling-interval-hint");
    private static final HttpHeaderName QPS_SERVICE_ENDPOINT_REDIRECT
        = HttpHeaderName.fromString("x-ms-qps-service-endpoint-redirect-v2");
    private static final HttpHeaderName QPS_ROLE_NAME = HttpHeaderName.fromString("x-ms-qps-role-name");
    private static final HttpHeaderName QPS_MACHINE_NAME = HttpHeaderName.fromString("x-ms-qps-machine-name");
    private static final HttpHeaderName QPS_STREAM_ID = HttpHeaderName.fromString("x-ms-qps-stream-id");
    private static final HttpHeaderName QPS_INSTANCE_NAME = HttpHeaderName.fromString("x-ms-qps-instance-name");
    private static final HttpHeaderName QPS_INVARIANT_VERSION = HttpHeaderName.fromString("x-ms-qps-invariant-version");

    HttpRequest buildPingRequest(Date currentDate, String address, String quickPulseId, String machineName,
        String roleName, String instanceName) {

        HttpRequest request = buildRequest(currentDate, address);
        request.setHeader(QPS_ROLE_NAME, roleName);
        request.setHeader(QPS_MACHINE_NAME, machineName);
        request.setHeader(QPS_STREAM_ID, quickPulseId);
        request.setHeader(QPS_INSTANCE_NAME, instanceName);
        request.setHeader(QPS_INVARIANT_VERSION, Integer.toString(QuickPulse.QP_INVARIANT_VERSION));
        return request;
    }

    HttpRequest buildRequest(Date currentDate, String address) {
        long ticks = currentDate.getTime() * 10000 + TICKS_AT_EPOCH;

        HttpRequest request = new HttpRequest(HttpMethod.POST, address);
        request.setHeader(HEADER_TRANSMISSION_TIME, String.valueOf(ticks));
        return request;
    }

    boolean isSuccess(HttpResponse response) {
        return response.getStatusCode() == 200;
    }

    QuickPulseHeaderInfo getQuickPulseHeaderInfo(HttpResponse response) {
        HttpHeaders headers = response.getHeaders();
        QuickPulseStatus status = QuickPulseStatus.ERROR;
        long servicePollingIntervalHint = -1;

        String qpStatus = headers.getValue(QPS_STATUS_HEADER);
        if (qpStatus != null) {
            if ("true".equalsIgnoreCase(qpStatus)) {
                status = QuickPulseStatus.QP_IS_ON;
            } else {
                status = QuickPulseStatus.QP_IS_OFF;
            }
        }

        String servicePollingIntervalHintHeaderValue = headers.getValue(QPS_SERVICE_POLLING_INTERVAL_HINT);
        if (!Strings.isNullOrEmpty(servicePollingIntervalHintHeaderValue)) {
            servicePollingIntervalHint = Long.parseLong(servicePollingIntervalHintHeaderValue);
        }

        return new QuickPulseHeaderInfo(status, headers.getValue(QPS_SERVICE_ENDPOINT_REDIRECT),
            servicePollingIntervalHint);
    }
}
