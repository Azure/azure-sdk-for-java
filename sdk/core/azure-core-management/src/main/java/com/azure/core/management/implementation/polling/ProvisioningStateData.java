// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.management.implementation.polling;

import com.azure.core.http.HttpHeaders;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

/**
 * The type to store the data associated with polling based on resource provisioning state.
 */
final class ProvisioningStateData {
    @JsonProperty(value = "pollUrl", required = true)
    private URL pollUrl;
    @JsonProperty(value = "provisioningState", required = true)
    private String provisioningState;
    @JsonProperty(value = "pollError")
    private Error pollError;
    @JsonProperty(value = "finalResult")
    private FinalResult finalResult;

    ProvisioningStateData() {
    }

    /**
     * Creates ProvisioningStateData.
     *
     * @param pollUrl the poll url
     * @param provisioningState the initial provisioning state
     */
    ProvisioningStateData(URL pollUrl, String provisioningState) {
        this.pollUrl = Objects.requireNonNull(pollUrl, "'pollUrl' cannot be null.");
        this.provisioningState
            = Objects.requireNonNull(provisioningState, "'provisioningState' cannot be null.");
    }

    /**
     * @return the current state of the long-running-operation.
     */
    String getProvisioningState() {
        return this.provisioningState;
    }

    /**
     * @return the url to retrieve latest resource representation with provisioningState property.
     */
    URL getPollUrl() {
        return this.pollUrl;
    }

    /**
     * @return the error describing the reason for 'Failed' ProvisioningState.
     */
    Error getPollError() {
        return this.pollError;
    }

    /**
     * @return FinalResult object to access final result of long-running-operation when
     * ProvisioningState is 'Succeeded'.
     */
    FinalResult getFinalResult() {
        return this.finalResult;
    }

    /**
     * Update the state from the given poll response.
     *
     * @param pollResponseStatusCode the poll response status code
     * @param pollResponseHeaders the poll response headers
     * @param pollResponseBody the poll response body
     * @param adapter the serializer adapter to decode the poll response body
     */
    void update(int pollResponseStatusCode,
                HttpHeaders pollResponseHeaders,
                String pollResponseBody,
                SerializerAdapter adapter) {
        if (pollResponseStatusCode != 200) {
            this.provisioningState = ProvisioningState.FAILED;
            this.pollError = new Error("Polling failed with status code:" + pollResponseStatusCode,
                pollResponseStatusCode,
                pollResponseHeaders.toMap(),
                pollResponseBody);
        } else {
            String value = tryParseProvisioningState(pollResponseBody, adapter);
            if (value == null) {
                this.provisioningState = ProvisioningState.FAILED;
                this.pollError = new Error("Polling response does not contain a valid body.",
                    pollResponseStatusCode,
                    pollResponseHeaders.toMap(),
                    pollResponseBody);
            } else {
                this.provisioningState = value;
                if (ProvisioningState.FAILED.equalsIgnoreCase(this.provisioningState)
                    || ProvisioningState.CANCELED.equalsIgnoreCase(this.provisioningState)) {
                    this.pollError = new Error("Long running operation failed or cancelled.",
                        pollResponseStatusCode,
                        pollResponseHeaders.toMap(),
                        pollResponseBody);
                } else if (ProvisioningState.SUCCEEDED.equalsIgnoreCase(this.provisioningState)) {
                    this.finalResult = new FinalResult(null, pollResponseBody);
                }
            }
        }
    }

    /**
     * Parse the given value and extract the ProvisioningState from it.
     *
     * @param value the value
     * @return provisioning state or null if value cannot be parsed
     */
    static String tryParseProvisioningState(String value, SerializerAdapter adapter) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            ResourceWithProvisioningState resource = adapter.deserialize(value,
                ResourceWithProvisioningState.class,
                SerializerEncoding.JSON);
            return resource != null
                ? resource.getProvisioningState()
                : null;
        } catch (IOException | RuntimeException ignored) {
            return null;
        }
    }
    /**
     * Schema of an azure resource with provisioningState property.
     */
    private static class ResourceWithProvisioningState {
        @JsonProperty(value = "properties")
        private Properties properties;

        private String getProvisioningState() {
            if (this.properties != null) {
                return this.properties.provisioningState;
            } else {
                return null;
            }
        }

        private static class Properties {
            @JsonProperty(value = "provisioningState")
            private String provisioningState;
        }
    }
}

