// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.management.implementation.polling;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

/**
 * The type to store the data associated with Azure-AsyncOperation header based polling.
 */
final class AzureAsyncOperationData implements JsonSerializable<AzureAsyncOperationData> {
    @JsonIgnore
    private static final ClientLogger LOGGER = new ClientLogger(AzureAsyncOperationData.class);

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
     * @param lroRequestMethod the http verb used to initiate the long-running operation
     * @param lroOperationUri the endpoint called to initiate the long-running operation
     * @param pollUrl the value of the Azure-AsyncOperation header
     * @param locationUrl the value of the Location header, if exists
     */
    AzureAsyncOperationData(HttpMethod lroRequestMethod, URL lroOperationUri, URL pollUrl, URL locationUrl) {
        this.lroRequestMethod = Objects.requireNonNull(lroRequestMethod, "'lroRequestMethod' cannot be null.");
        this.lroOperationUri = Objects.requireNonNull(lroOperationUri, "'lroOperationUri' cannot be null.");
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

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeStringField("lroRequestMethod", Objects.toString(lroRequestMethod, null))
            .writeStringField("lroOperationUri", Objects.toString(lroOperationUri, null))
            .writeStringField("pollUrl", Objects.toString(pollUrl, null))
            .writeStringField("locationUrl", Objects.toString(locationUrl, null))
            .writeStringField("provisioningState", provisioningState)
            .writeJsonField("pollError", pollError)
            .writeJsonField("finalResult", finalResult)
            .writeEndObject();
    }

    /**
     * Reads a JSON stream into an {@link AzureAsyncOperationData}.
     *
     * @param jsonReader The {@link JsonReader} being read.
     * @return The {@link AzureAsyncOperationData} that the JSON stream represented, may return null.
     * @throws IOException If an {@link AzureAsyncOperationData} fails to be read from the {@code jsonReader}.
     */
    @SuppressWarnings("deprecation")
    public static AzureAsyncOperationData fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            AzureAsyncOperationData azureAsyncOperationData = new AzureAsyncOperationData();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("lroRequestMethod".equals(fieldName)) {
                    azureAsyncOperationData.lroRequestMethod = HttpMethod.valueOf(reader.getString());
                } else if ("lroOperationUri".equals(fieldName)) {
                    azureAsyncOperationData.lroOperationUri
                        = reader.getNullable(nonNullReader -> new URL(nonNullReader.getString()));
                } else if ("pollUrl".equals(fieldName)) {
                    azureAsyncOperationData.pollUrl
                        = reader.getNullable(nonNullReader -> new URL(nonNullReader.getString()));
                } else if ("locationUrl".equals(fieldName)) {
                    azureAsyncOperationData.locationUrl
                        = reader.getNullable(nonNullReader -> new URL(nonNullReader.getString()));
                } else if ("provisioningState".equals(fieldName)) {
                    azureAsyncOperationData.provisioningState = reader.getString();
                } else if ("pollError".equals(fieldName)) {
                    azureAsyncOperationData.pollError = Error.fromJson(reader);
                } else if ("finalResult".equals(fieldName)) {
                    azureAsyncOperationData.finalResult = FinalResult.fromJson(reader);
                } else {
                    reader.skipChildren();
                }
            }

            return azureAsyncOperationData;
        });
    }

    /**
     * Update the data from the given poll response.
     *
     * @param pollResponseStatusCode the poll response status code
     * @param pollResponseHeaders the poll response headers
     * @param pollResponseBody the poll response body
     */
    void update(int pollResponseStatusCode, HttpHeaders pollResponseHeaders, String pollResponseBody) {
        if (pollResponseStatusCode != 200 && pollResponseStatusCode != 201 && pollResponseStatusCode != 202) {
            this.provisioningState = ProvisioningState.FAILED;
            this.pollError = new Error("Polling failed with status code:" + pollResponseStatusCode,
                pollResponseStatusCode, pollResponseHeaders.toMap(), pollResponseBody);

            return;
        }

        AsyncOperationResource resource = tryParseAsyncOperationResource(pollResponseBody);
        if (resource == null || resource.getProvisioningState() == null) {
            this.provisioningState = ProvisioningState.FAILED;
            this.pollError = new Error("Polling response does not contain a valid body.", pollResponseStatusCode,
                pollResponseHeaders.toMap(), pollResponseBody);

            return;
        }

        this.provisioningState = resource.getProvisioningState();
        if (ProvisioningState.FAILED.equalsIgnoreCase(this.provisioningState)
            || ProvisioningState.CANCELED.equalsIgnoreCase(this.provisioningState)) {
            this.pollError = new Error("Long running operation is Failed or Cancelled.", pollResponseStatusCode,
                pollResponseHeaders.toMap(), pollResponseBody);

            return;
        }

        if (ProvisioningState.SUCCEEDED.equalsIgnoreCase(this.provisioningState)) {
            if (this.lroRequestMethod == HttpMethod.POST || this.lroRequestMethod == HttpMethod.DELETE) {
                if (this.locationUrl != null) {
                    this.finalResult = new FinalResult(this.locationUrl, null);
                }
            } else if (this.lroRequestMethod == HttpMethod.PUT || this.lroRequestMethod == HttpMethod.PATCH) {
                this.finalResult = new FinalResult(this.lroOperationUri, null);
            }
        } else {
            try {
                this.updateUrls(pollResponseHeaders);
            } catch (Util.MalformedUrlException mue) {
                this.provisioningState = ProvisioningState.FAILED;
                this.pollError = new Error("Long running operation contains a malformed Azure-AsyncOperation header.",
                    pollResponseStatusCode, pollResponseHeaders.toMap(), pollResponseBody);
            }
        }
    }

    /**
     * Update pollUrl and locationUrl from the poll response headers.
     *
     * @param pollResponseHeaders the poll response headers
     */
    private void updateUrls(HttpHeaders pollResponseHeaders) {
        final URL azAsyncOpUrl = Util.getAzureAsyncOperationUrl(pollResponseHeaders, LOGGER);
        if (azAsyncOpUrl != null) {
            this.pollUrl = azAsyncOpUrl;
        }
        final URL locationUrl = Util.getLocationUrl(pollResponseHeaders, LOGGER, true);
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
    private static AsyncOperationResource tryParseAsyncOperationResource(String value) {
        if (CoreUtils.isNullOrEmpty(value)) {
            return null;
        }

        try (JsonReader jsonReader = JsonProviders.createReader(value)) {
            return AsyncOperationResource.fromJson(jsonReader);
        } catch (IOException ignored) {
            return null;
        }
    }

    /**
     * The schema of Azure-AzureOperation poll response.
     */
    private static class AsyncOperationResource implements JsonSerializable<AsyncOperationResource> {
        @JsonProperty(value = "status")
        private String provisioningState;

        private String getProvisioningState() {
            return provisioningState;
        }

        @Override
        public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
            return jsonWriter.writeStartObject().writeStringField("status", provisioningState).writeEndObject();
        }

        /**
         * Reads a JSON stream into an {@link AsyncOperationResource}.
         *
         * @param jsonReader The {@link JsonReader} being read.
         * @return The {@link AsyncOperationResource} that the JSON stream represented, may return null.
         * @throws IOException If an {@link AsyncOperationResource} fails to be read from the {@code jsonReader}.
         */
        public static AsyncOperationResource fromJson(JsonReader jsonReader) throws IOException {
            return jsonReader.readObject(reader -> {
                AsyncOperationResource asyncOperationResource = new AsyncOperationResource();

                while (reader.nextToken() != JsonToken.END_OBJECT) {
                    String fieldName = reader.getFieldName();
                    reader.nextToken();

                    if ("status".equals(fieldName)) {
                        asyncOperationResource.provisioningState = reader.getString();
                    } else {
                        reader.skipChildren();
                    }
                }

                return asyncOperationResource;
            });
        }
    }
}
