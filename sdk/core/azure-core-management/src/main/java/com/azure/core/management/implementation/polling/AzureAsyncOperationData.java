// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.management.implementation.polling;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

/**
 * The type to store the data associated with Azure-AsyncOperation header based polling.
 */
final class AzureAsyncOperationData {
    @JsonIgnore
    private final ClientLogger logger = new ClientLogger(AzureAsyncOperationData.class);

    @JsonProperty(value = "lroRequestMethod", required = true)
    private HttpMethod lroRequestMethod;
    @JsonProperty(value = "lroOperationUri", required = true)
    private URL lroOperationUri;
    @JsonProperty(value = "pollUrl", required = true)
    private URL pollUrl;
    @JsonProperty(value = "locationUrl")
    private URL locationUrl;
    @JsonProperty(value = "provisioningState", required = true)
    private String provisioningState;
    @JsonProperty(value = "pollError")
    private Error pollError;
    @JsonProperty(value = "finalResult")
    private FinalResult finalResult;

    AzureAsyncOperationData() {
    }

    /**
     * Creates AzureAsyncOperationData.
     *
     * @param lroRequestMethod the http verb used to initiate the long running operation
     * @param lroOperationUri the endpoint called to initiate the long running operation
     * @param pollUrl the value of the Azure-AsyncOperation header
     * @param locationUrl the value of the Location header, if exists
     */
    AzureAsyncOperationData(HttpMethod lroRequestMethod,
                                    URL lroOperationUri,
                                    URL pollUrl,
                                    URL locationUrl) {
        this.lroRequestMethod = Objects.requireNonNull(lroRequestMethod,
            "'lroRequestMethod' cannot be null.");
        this.lroOperationUri = Objects.requireNonNull(lroOperationUri,
            "'lroOperationUri' cannot be null.");
        this.pollUrl = Objects.requireNonNull(pollUrl, "'pollUrl' cannot be null.");
        this.locationUrl = locationUrl;
        this.provisioningState = ProvisioningState.IN_PROGRESS;
    }

    /**
     * @return the current state of the long-running-operation.
     */
    String getProvisioningState() {
        return this.provisioningState;
    }

    /**
     * @return the poll url retrieved from Azure-AsyncOperation header.
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
     * @param adapter the adapter to decode poll response body
     */
    void update(int pollResponseStatusCode,
                HttpHeaders pollResponseHeaders,
                String pollResponseBody,
                SerializerAdapter adapter) {
        if (pollResponseStatusCode != 200 && pollResponseStatusCode != 201 && pollResponseStatusCode != 202) {
            this.provisioningState = ProvisioningState.FAILED;
            this.pollError = new Error("Polling failed with status code:" + pollResponseStatusCode,
                pollResponseStatusCode,
                pollResponseHeaders.toMap(),
                pollResponseBody);
        } else {
            AsyncOperationResource resource = tryParseAsyncOperationResource(pollResponseBody, adapter);
            if (resource == null || resource.getProvisioningState() == null) {
                this.provisioningState = ProvisioningState.FAILED;
                this.pollError = new Error("Polling response does not contain a valid body.",
                    pollResponseStatusCode,
                    pollResponseHeaders.toMap(),
                    pollResponseBody);
            } else {
                this.provisioningState = resource.getProvisioningState();
                if (ProvisioningState.FAILED.equalsIgnoreCase(this.provisioningState)
                    || ProvisioningState.CANCELED.equalsIgnoreCase(this.provisioningState)) {
                    this.pollError = new Error("Long running operation is Failed or Cancelled.",
                        pollResponseStatusCode,
                        pollResponseHeaders.toMap(),
                        pollResponseBody);
                } else {
                    if (ProvisioningState.SUCCEEDED.equalsIgnoreCase(this.provisioningState)) {
                        if (this.lroRequestMethod == HttpMethod.POST
                            || this.lroRequestMethod == HttpMethod.DELETE) {
                            if (this.locationUrl != null) {
                                this.finalResult = new FinalResult(this.locationUrl, null);
                            }
                        } else if (this.lroRequestMethod == HttpMethod.PUT
                            || this.lroRequestMethod == HttpMethod.PATCH) {
                            this.finalResult = new FinalResult(this.lroOperationUri, null);
                        }
                    } else {
                        this.updateUrls(pollResponseHeaders);
                    }
                }
            }
        }
    }

    /**
     * Update pollUrl and locationUrl from the poll response headers.
     *
     * @param pollResponseHeaders the poll response headers
     */
    private void updateUrls(HttpHeaders pollResponseHeaders) {
        final URL azAsyncOpUrl = Util.getAzureAsyncOperationUrl(pollResponseHeaders, logger);
        if (azAsyncOpUrl != null) {
            this.pollUrl = azAsyncOpUrl;
        }
        final URL locationUrl = Util.getLocationUrl(pollResponseHeaders, logger);
        if (locationUrl != null) {
            this.locationUrl = locationUrl;
        }
    }

    /**
     * Parse (deserialize) the given value to AsyncOperationResource.
     *
     * @param value the value
     * @return AsyncOperationResource or null if value cannot be parsed
     */
    private static AsyncOperationResource tryParseAsyncOperationResource(String value, SerializerAdapter adapter) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return adapter.deserialize(value, AsyncOperationResource.class, SerializerEncoding.JSON);
        } catch (IOException ignored) {
            return null;
        }
    }

    /**
     * The schema of Azure-AzureOperation poll response.
     */
    private static class AsyncOperationResource {
        @JsonProperty(value = "status")
        private String provisioningState;

        private String getProvisioningState() {
            return provisioningState;
        }
    }
}

