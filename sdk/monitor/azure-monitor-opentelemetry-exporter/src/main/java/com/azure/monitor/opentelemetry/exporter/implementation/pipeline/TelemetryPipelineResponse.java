// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.pipeline;

import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.monitor.opentelemetry.exporter.implementation.models.Response;
import com.azure.monitor.opentelemetry.exporter.implementation.models.ResponseError;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Collections.emptySet;
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

    public Set<ResponseError> getErrors() {
        try {
            return parseErrors(body); // parseErrors on demand
        } catch (IllegalStateException e) {
            return emptySet();
        }
    }

    public Set<String> getErrorMessages() {
        Set<ResponseError> responseErrors;
        try {
            responseErrors = parseErrors(body);
        } catch (IllegalStateException e) {
            return singleton("Could not parse response");
        }
        return responseErrors.stream().map(ResponseError::getMessage)
            .filter(message -> !message.equals("Telemetry sampled out."))
            .collect(Collectors.toSet());
    }

    public boolean isInvalidInstrumentationKey() {
        Set<String> errors = getErrorMessages();
        return errors != null && errors.contains(INVALID_INSTRUMENTATION_KEY);
    }

    static Set<ResponseError> parseErrors(String body) {
        try (JsonReader reader = JsonProviders.createReader(body)) {
            Response response = Response.fromJson(reader);
            return new HashSet<>(response.getErrors());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to parse response body", e);
        }
    }
}
