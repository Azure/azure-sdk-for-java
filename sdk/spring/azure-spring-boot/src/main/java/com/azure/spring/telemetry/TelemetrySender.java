// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.telemetry;

import com.azure.spring.support.GetHashMac;
import com.azure.spring.utils.PropertyLoader;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON;

/**
 * Client used for sending telemetry info.
 */
public class TelemetrySender {

    private static final Logger LOGGER = LoggerFactory.getLogger(TelemetrySender.class);

    private static final String TELEMETRY_TARGET_URL = "https://dc.services.visualstudio.com/v2/track";

    private static final String PROJECT_INFO = "spring-boot-starter/" + PropertyLoader.getProjectVersion();

    private static final int RETRY_LIMIT = 3; // Align the retry times with sdk

    private static final RestTemplate REST_TEMPLATE = new RestTemplate();

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final HttpHeaders HEADERS = new HttpHeaders();

    static {
        HEADERS.add(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON.toString());
    }

    private ResponseEntity<String> executeRequest(final TelemetryEventData eventData) {
        try {
            final HttpEntity<String> body = new HttpEntity<>(MAPPER.writeValueAsString(eventData), HEADERS);

            return REST_TEMPLATE.exchange(TELEMETRY_TARGET_URL, HttpMethod.POST, body, String.class);
        } catch (RestClientException | JsonProcessingException e) {
            LOGGER.warn("Failed to exchange telemetry request, {}.", e.getMessage());
        }

        return null;
    }

    private void sendTelemetryData(@NonNull TelemetryEventData eventData) {
        ResponseEntity<String> response = null;

        for (int i = 0; i < RETRY_LIMIT; i++) {
            response = executeRequest(eventData);

            if (response != null && response.getStatusCode() == HttpStatus.OK) {
                return;
            }
        }

        if (response != null && response.getStatusCode() != HttpStatus.OK) {
            LOGGER.warn("Failed to send telemetry data, response status code {}.", response.getStatusCode().toString());
        }
    }

    public void send(String name, @NonNull Map<String, String> properties) {
        Assert.hasText(name, "Event name should contain text.");

        properties.putIfAbsent(TelemetryData.INSTALLATION_ID, GetHashMac.getHashMac());
        properties.putIfAbsent(TelemetryData.PROJECT_VERSION, PROJECT_INFO);

        sendTelemetryData(new TelemetryEventData(name, properties));
    }
}

