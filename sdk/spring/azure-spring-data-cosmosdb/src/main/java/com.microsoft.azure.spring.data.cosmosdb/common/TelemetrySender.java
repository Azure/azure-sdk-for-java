// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.spring.data.cosmosdb.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON;

/**
 * Class for telemetry sender to send request and event data
 */
public class TelemetrySender {

    private static final Logger LOGGER = LoggerFactory.getLogger(TelemetrySender.class);

    private static final String PROPERTY_INSTALLATION_ID = "installationId";

    private static final String PROPERTY_VERSION = "version";

    private static final String PROPERTY_SERVICE_NAME = "serviceName";

    private static final String PROJECT_INFO = "spring-data-cosmosdb/"
                                                    + PropertyLoader.getProjectVersion();

    private static final String TELEMETRY_TARGET_URL = "https://dc.services.visualstudio.com/v2/track";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final int RETRY_LIMIT = 3; // Align the retry times with sdk

    private ResponseEntity<String> executeRequest(final TelemetryEventData eventData) {
        final HttpHeaders headers = new HttpHeaders();

        headers.add(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON.toString());

        try {
            final RestTemplate restTemplate = new RestTemplate();
            final HttpEntity<String> body = new HttpEntity<>(MAPPER.writeValueAsString(eventData), headers);

            return restTemplate.exchange(TELEMETRY_TARGET_URL, HttpMethod.POST, body, String.class);
        } catch (JsonProcessingException | HttpClientErrorException ignore) {
            LOGGER.warn("Failed to exchange telemetry request, {}.", ignore.getMessage());
        }

        return null;
    }

    private void sendTelemetryData(@NonNull TelemetryEventData eventData) {
        ResponseEntity<String> response = null;

        for (int i = 0; i < RETRY_LIMIT; i++) {
            response = executeRequest(eventData);

            if (response != null
                    && response.getStatusCode() == HttpStatus.OK) {
                return;
            }
        }

        if (response != null
                && response.getStatusCode() != HttpStatus.OK) {
            LOGGER.warn("Failed to send telemetry data, response status code {}.", response.getStatusCode().toString());
        }
    }

    /**
     * Send telemetry data according to event name
     *
     * @param name event name
     */
    public void send(String name) {
        Assert.hasText(name, "Event name should contain text.");

        sendTelemetryData(new TelemetryEventData(name, getProperties()));
    }

    private Map<String, String> getProperties() {
        final Map<String, String> properties = new HashMap<>();

        properties.put(PROPERTY_VERSION, PROJECT_INFO);
        properties.put(PROPERTY_SERVICE_NAME, "cosmosdb");
        properties.put(PROPERTY_INSTALLATION_ID, MacAddress.getHashMac());

        return properties;
    }
}

