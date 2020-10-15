// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.gremlin.telemetry;

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

import java.util.HashMap;
import java.util.Map;

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON;

public class TelemetrySender {

    private static final Logger LOGGER = LoggerFactory.getLogger(TelemetrySender.class);

    private static final String PROPERTY_INSTALLATION_ID = "installationId";

    private static final String PROPERTY_VERSION = "version";

    private static final String PROPERTY_SERVICE_NAME = "serviceName";

    private static final String PROJECT_INFO = "spring-data-gremlin/" + PropertyLoader.getProjectVersion();

    private static final String TELEMETRY_TARGET_URL = "https://dc.services.visualstudio.com/v2/track";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final int RETRY_LIMIT = 3; // Align the retry times with sdk

    private static final RestTemplate REST_TEMPLATE = new RestTemplate();

    private static final HttpHeaders HEADERS = new HttpHeaders();

    static {
        HEADERS.add(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON.toString());
    }

    private ResponseEntity<String> executeRequest(final TelemetryEventData eventData) {
        try {
            final HttpEntity<String> body = new HttpEntity<>(MAPPER.writeValueAsString(eventData), HEADERS);

            return REST_TEMPLATE.exchange(TELEMETRY_TARGET_URL, HttpMethod.POST, body, String.class);
        } catch (RestClientException | JsonProcessingException e) {
            LOGGER.trace("Failed to exchange telemetry request, {}.", e.getMessage());
        }

        return null;
    }

    private void sendTelemetryData(@NonNull TelemetryEventData eventData) {
        Assert.notNull(eventData, "eventData should not be null");
        ResponseEntity<String> response = null;

        for (int i = 0; i < RETRY_LIMIT; i++) {
            response = executeRequest(eventData);

            if (response != null && response.getStatusCode() == HttpStatus.OK) {
                return;
            }
        }

        if (response != null && response.getStatusCode() != HttpStatus.OK) {
            LOGGER.trace("Failed to send telemetry data, response status code {}.",
                response.getStatusCode().toString());
        }
    }

    public void send(String name) {
        Assert.hasText(name, "Event name should contain text.");

        sendTelemetryData(new TelemetryEventData(name, getProperties()));
    }

    private Map<String, String> getProperties() {
        final Map<String, String> properties = new HashMap<>();

        properties.put(PROPERTY_VERSION, PROJECT_INFO);
        properties.put(PROPERTY_SERVICE_NAME, "gremlin");
        properties.put(PROPERTY_INSTALLATION_ID, MacAddress.getHashMac());

        return properties;
    }
}

