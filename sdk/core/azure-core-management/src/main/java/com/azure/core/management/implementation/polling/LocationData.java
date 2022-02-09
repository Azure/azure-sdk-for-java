// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.management.implementation.polling;

import com.azure.core.http.HttpHeaders;
import com.azure.core.util.logging.ClientLogger;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URL;
import java.util.Objects;

/**
 * The type to store the data associated with location header based polling.
 */
final class LocationData {
    @JsonIgnore
    private final ClientLogger logger = new ClientLogger(LocationData.class);

    @JsonProperty(value = "pollUrl", required = true)
    private URL pollUrl;
    @JsonProperty(value = "provisioningState", required = true)
    private String provisioningState;
    @JsonProperty(value = "pollError")
    private Error pollError;
    @JsonProperty(value = "finalResult")
    private FinalResult finalResult;

    LocationData() {
    }

    /**
     * Creates LocationData.
     *
     * @param pollUrl the poll url
     */
    LocationData(URL pollUrl) {
        this.pollUrl = Objects.requireNonNull(pollUrl, "'pollUrl' cannot be null.");
        this.provisioningState = ProvisioningState.IN_PROGRESS;
    }

    /**
     * @return the current state of the long-running-operation.
     */
    String getProvisioningState() {
        return this.provisioningState;
    }

    /**
     * @return the url to retrieve latest status of long-running-operation.
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
     * Update the data from the given poll response.
     *
     * @param pollResponseStatusCode the poll response status code
     * @param pollResponseHeaders the poll response headers
     * @param pollResponseBody the poll response body
     */
    void update(int pollResponseStatusCode, HttpHeaders pollResponseHeaders, String pollResponseBody) {
        if (pollResponseStatusCode == 202) {
            try {
                this.provisioningState = ProvisioningState.IN_PROGRESS;
                final URL locationUrl = Util.getLocationUrl(pollResponseHeaders, logger);
                if (locationUrl != null) {
                    this.pollUrl = locationUrl;
                }
            } catch (Util.MalformedUrlException mue) {
                this.provisioningState = ProvisioningState.FAILED;
                this.pollError = new Error(
                    "Long running operation contains a malformed Location header.",
                    pollResponseStatusCode,
                    pollResponseHeaders.toMap(),
                    pollResponseBody);
            }
        } else if (pollResponseStatusCode == 200 || pollResponseStatusCode == 201 || pollResponseStatusCode == 204) {
            this.provisioningState = ProvisioningState.SUCCEEDED;
            if (pollResponseBody != null) {
                this.finalResult = new FinalResult(null, pollResponseBody);
            }
        } else {
            this.provisioningState = ProvisioningState.FAILED;
            this.pollError = new Error("Polling failed with status code:" + pollResponseStatusCode,
                pollResponseStatusCode,
                pollResponseHeaders.toMap(),
                pollResponseBody);
        }
    }
}
