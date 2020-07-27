/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.telemetry;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON;

public class TelemetryRestClient {

    private static final Logger log = LoggerFactory.getLogger(TelemetryRestClient.class);

    private static final String TELEMETRY_TARGET_URL = "https://dc.services.visualstudio.com/v2/track";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final HttpHeaders HEADERS = new HttpHeaders();

    private static final RestTemplate REST_TEMPLATE = new RestTemplate();

    private static final int RETRY_LIMIT = 3; // Align the retry times with sdk

    private final String instrumentKey;

    static {
        HEADERS.add(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON.toString());
    }

    public TelemetryRestClient(String instrumentKey) {
        Assert.hasText(instrumentKey, "Instrument key should have text.");

        this.instrumentKey = instrumentKey;
    }

    private boolean sendTelemetryData(final TelemetryEventData eventData) {
        try {
            HttpEntity<String> body = new HttpEntity<>(MAPPER.writeValueAsString(eventData), HEADERS);
            ResponseEntity<String> response = REST_TEMPLATE.exchange(
                    TELEMETRY_TARGET_URL, HttpMethod.POST, body, String.class);

            return response.getStatusCode() == HttpStatus.OK;
        } catch (Exception ignore) {
            log.warn("Failed to exchange telemetry request, {}.", ignore.getMessage());
        }

        return false;
    }

    public void send(@NonNull String eventName, @NonNull Map<String, String> properties) {
        Assert.hasText(eventName, "Event name should have text.");

        for (int i = 0; i < RETRY_LIMIT; i++) {
            if (sendTelemetryData(new TelemetryEventData(eventName, properties, instrumentKey))) {
                return;
            }
        }

        log.warn("Failed to send telemetry data");
    }
}
