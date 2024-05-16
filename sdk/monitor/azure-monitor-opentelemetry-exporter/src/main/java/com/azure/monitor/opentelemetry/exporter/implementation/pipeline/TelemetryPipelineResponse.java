// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.pipeline;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Collections.singleton;

public class TelemetryPipelineResponse {

    private static final String INVALID_INSTRUMENTATION_KEY = "Invalid instrumentation key"; // 400 status code
    private final int statusCode;
    private final String body;

    TelemetryPipelineResponse(int statusCode, String body) {
        this.statusCode = statusCode;
        this.body = body;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getBody() {
        return body;
    }

    public Set<String> getErrors() {
        return parseErrors(body); // parseErrors on demand
    }

    public boolean isInvalidInstrumentationKey() {
        Set<String> errors = parseErrors(body);
        return errors != null && errors.contains(INVALID_INSTRUMENTATION_KEY);
    }

    private static Set<String> parseErrors(String body) {
        JsonNode jsonNode;
        try {
            jsonNode = new ObjectMapper().readTree(body);
        } catch (JsonProcessingException e) {
            // fallback to generic message
            return singleton("Could not parse response");
        }
        List<JsonNode> errorNodes = new ArrayList<>();
        jsonNode.get("errors").forEach(errorNodes::add);
        return errorNodes.stream()
            .map(errorNode -> errorNode.get("message").asText())
            .filter(s -> !s.equals("Telemetry sampled out."))
            .collect(Collectors.toSet());
    }
}
