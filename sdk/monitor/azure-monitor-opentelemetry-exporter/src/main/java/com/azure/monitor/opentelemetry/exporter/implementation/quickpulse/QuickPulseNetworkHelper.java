// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.quickpulse;

import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.HttpHeaderName;
import com.azure.monitor.opentelemetry.exporter.implementation.utils.Strings;
import com.fasterxml.jackson.databind.ObjectMapper;


import java.util.Date;

final class QuickPulseNetworkHelper {
    private static final long TICKS_AT_EPOCH = 621355968000000000L;
    private static final String HEADER_TRANSMISSION_TIME = "x-ms-qps-transmission-time";
    private static final String QPS_STATUS_HEADER = "x-ms-qps-subscribed";
    private static final String QPS_SERVICE_POLLING_INTERVAL_HINT =
        "x-ms-qps-service-polling-interval-hint";
    private static final String QPS_SERVICE_ENDPOINT_REDIRECT =
        "x-ms-qps-service-endpoint-redirect-v2";
    private static final String QPS_INSTANCE_NAME_HEADER = "x-ms-qps-instance-name";
    private static final String QPS_STREAM_ID_HEADER = "x-ms-qps-stream-id";
    private static final String QPS_MACHINE_NAME_HEADER = "x-ms-qps-machine-name";
    private static final String QPS_ROLE_NAME_HEADER = "x-ms-qps-role-name";
    private static final String QPS_INVARIANT_VERSION_HEADER = "x-ms-qps-invariant-version";
    private static final String QPS_CONFIGURATION_ETAG_HEADER = "x-ms-qps-configuration-etag";
    private static final HttpHeaderName QPS_ROLE_NAME_HEADER_NAME = HttpHeaderName.fromString(QPS_ROLE_NAME_HEADER);
    private static final HttpHeaderName QPS_MACHINE_NAME_HEADER_NAME = HttpHeaderName.fromString(QPS_MACHINE_NAME_HEADER);
    private static final HttpHeaderName QPS_STREAM_ID_HEADER_NAME = HttpHeaderName.fromString(QPS_STREAM_ID_HEADER);
    private static final HttpHeaderName QPS_INSTANCE_NAME_HEADER_NAME = HttpHeaderName.fromString(QPS_INSTANCE_NAME_HEADER);
    private static final HttpHeaderName QPS_INVARIANT_VERSION_HEADER_NAME = HttpHeaderName.fromString(QPS_INVARIANT_VERSION_HEADER);
    private static final HttpHeaderName QPS_CONFIGURATION_ETAG_HEADER_NAME = HttpHeaderName.fromString(QPS_CONFIGURATION_ETAG_HEADER);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    HttpRequest buildPingRequest(
        Date currentDate,
        String address,
        String quickPulseId,
        String machineName,
        String roleName,
        String instanceName) {

        HttpRequest request = buildRequest(currentDate, address, "");
        request.setHeader(QPS_ROLE_NAME_HEADER_NAME, roleName);
        request.setHeader(QPS_MACHINE_NAME_HEADER_NAME, machineName);
        request.setHeader(QPS_STREAM_ID_HEADER_NAME, quickPulseId);
        request.setHeader(QPS_INSTANCE_NAME_HEADER_NAME, instanceName);
        request.setHeader(QPS_INVARIANT_VERSION_HEADER_NAME, Integer.toString(QuickPulse.QP_INVARIANT_VERSION));
        return request;
    }

    HttpRequest buildRequest(Date currentDate, String address, String etag) {
        long ticks = currentDate.getTime() * 10000 + TICKS_AT_EPOCH;

        HttpRequest request = new HttpRequest(HttpMethod.POST, address);
        request.setHeader(HttpHeaderName.fromString(HEADER_TRANSMISSION_TIME), String.valueOf(ticks));
        request.setHeader(QPS_CONFIGURATION_ETAG_HEADER_NAME, etag);
        return request;
    }

    boolean isSuccess(HttpResponse response) {
        return response.getStatusCode() == 200;
    }

    QuickPulseHeaderInfo getQuickPulseHeaderInfo(HttpResponse response) {
        HttpHeaders headers = response.getHeaders();
        QuickPulseStatus status = QuickPulseStatus.ERROR;
        long servicePollingIntervalHint = -1;
        String serviceEndpointRedirect = null;

        for (HttpHeader header : headers) {
            if (QPS_STATUS_HEADER.equalsIgnoreCase(header.getName())) {
                String qpStatus = header.getValue();
                if ("true".equalsIgnoreCase(qpStatus)) {
                    status = QuickPulseStatus.QP_IS_ON;
                } else {
                    status = QuickPulseStatus.QP_IS_OFF;
                }
            } else if (QPS_SERVICE_POLLING_INTERVAL_HINT.equalsIgnoreCase(header.getName())) {
                String servicePollingIntervalHintHeaderValue = header.getValue();
                if (!Strings.isNullOrEmpty(servicePollingIntervalHintHeaderValue)) {
                    servicePollingIntervalHint = Long.parseLong(servicePollingIntervalHintHeaderValue);
                }
            } else if (QPS_SERVICE_ENDPOINT_REDIRECT.equalsIgnoreCase(header.getName())) {
                serviceEndpointRedirect = header.getValue();
            }
        }
        return new QuickPulseHeaderInfo(status, serviceEndpointRedirect, servicePollingIntervalHint);
    }

    String getEtagHeaderValue(HttpResponse response) {
        HttpHeaders headers = response.getHeaders();
        HttpHeader etagHeader = headers.get(QPS_CONFIGURATION_ETAG_HEADER_NAME);
        return etagHeader != null ? etagHeader.getValue() : null;
    }

}
