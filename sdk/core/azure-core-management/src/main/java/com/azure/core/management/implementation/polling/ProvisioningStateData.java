// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.management.implementation.polling;

import com.azure.core.http.HttpHeaders;
import com.azure.core.util.CoreUtils;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

/**
 * The type to store the data associated with polling based on resource provisioning state.
 */
final class ProvisioningStateData implements JsonSerializable<ProvisioningStateData> {
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
        this.provisioningState = Objects.requireNonNull(provisioningState, "'provisioningState' cannot be null.");
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
     */
    void update(int pollResponseStatusCode, HttpHeaders pollResponseHeaders, String pollResponseBody) {
        if (pollResponseStatusCode != 200) {
            this.provisioningState = ProvisioningState.FAILED;
            this.pollError = new Error("Polling failed with status code:" + pollResponseStatusCode,
                pollResponseStatusCode, pollResponseHeaders.toMap(), pollResponseBody);

            return;
        }

        String value = tryParseProvisioningState(pollResponseBody);
        if (value == null) {
            this.provisioningState = ProvisioningState.FAILED;
            this.pollError = new Error("Polling response does not contain a valid body.", pollResponseStatusCode,
                pollResponseHeaders.toMap(), pollResponseBody);

            return;
        }

        this.provisioningState = value;
        if (ProvisioningState.FAILED.equalsIgnoreCase(this.provisioningState)
            || ProvisioningState.CANCELED.equalsIgnoreCase(this.provisioningState)) {
            this.pollError = new Error("Long running operation failed or cancelled.", pollResponseStatusCode,
                pollResponseHeaders.toMap(), pollResponseBody);
        } else if (ProvisioningState.SUCCEEDED.equalsIgnoreCase(this.provisioningState)) {
            this.finalResult = new FinalResult(null, pollResponseBody);
        }
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeStringField("pollUrl", Objects.toString(pollUrl, null))
            .writeStringField("provisioningState", provisioningState)
            .writeJsonField("pollError", pollError)
            .writeJsonField("finalResult", finalResult)
            .writeEndObject();
    }

    /**
     * Reads a JSON stream into a {@link ProvisioningStateData}.
     *
     * @param jsonReader The {@link JsonReader} being read.
     * @return The {@link ProvisioningStateData} that the JSON stream represented, may return null.
     * @throws IOException If a {@link ProvisioningStateData} fails to be read from the {@code jsonReader}.
     */
    @SuppressWarnings("deprecation")
    public static ProvisioningStateData fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            ProvisioningStateData provisioningStateData = new ProvisioningStateData();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("pollUrl".equals(fieldName)) {
                    provisioningStateData.pollUrl = reader.getNullable(nonNullReader -> new URL(reader.getString()));
                } else if ("provisioningState".equals(fieldName)) {
                    provisioningStateData.provisioningState = reader.getString();
                } else if ("pollError".equals(fieldName)) {
                    provisioningStateData.pollError = Error.fromJson(reader);
                } else if ("finalResult".equals(fieldName)) {
                    provisioningStateData.finalResult = FinalResult.fromJson(reader);
                } else {
                    reader.skipChildren();
                }
            }

            return provisioningStateData;
        });
    }

    /**
     * Parse the given value and extract the ProvisioningState from it.
     *
     * @param value the value
     * @return provisioning state or null if value cannot be parsed
     */
    static String tryParseProvisioningState(String value) {
        if (CoreUtils.isNullOrEmpty(value)) {
            return null;
        }
        try (JsonReader jsonReader = JsonProviders.createReader(value)) {
            ResourceWithProvisioningState resource = ResourceWithProvisioningState.fromJson(jsonReader);

            return resource != null ? resource.getProvisioningState() : null;
        } catch (IOException ignored) {
            return null;
        }
    }

    /**
     * Schema of an azure resource with provisioningState property.
     */
    private static class ResourceWithProvisioningState implements JsonSerializable<ResourceWithProvisioningState> {
        @JsonProperty(value = "properties")
        private Properties properties;

        private String getProvisioningState() {
            if (this.properties != null) {
                return this.properties.provisioningState;
            } else {
                return null;
            }
        }

        @Override
        public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
            return jsonWriter.writeStartObject().writeJsonField("properties", properties).writeEndObject();
        }

        /**
         * Reads a JSON stream into a {@link ResourceWithProvisioningState}.
         *
         * @param jsonReader The {@link JsonReader} being read.
         * @return The {@link ResourceWithProvisioningState} that the JSON stream represented, may return null.
         * @throws IOException If a {@link ResourceWithProvisioningState} fails to be read from the {@code jsonReader}.
         */
        public static ResourceWithProvisioningState fromJson(JsonReader jsonReader) throws IOException {
            return jsonReader.readObject(reader -> {
                ResourceWithProvisioningState resourceWithProvisioningState = new ResourceWithProvisioningState();

                while (reader.nextToken() != JsonToken.END_OBJECT) {
                    String fieldName = reader.getFieldName();
                    reader.nextToken();

                    if ("properties".equals(fieldName)) {
                        resourceWithProvisioningState.properties = Properties.fromJson(reader);
                    } else {
                        reader.skipChildren();
                    }
                }

                return resourceWithProvisioningState;
            });
        }

        private static class Properties implements JsonSerializable<Properties> {
            @JsonProperty(value = "provisioningState")
            private String provisioningState;

            @Override
            public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
                return jsonWriter.writeStartObject()
                    .writeStringField("provisioningState", provisioningState)
                    .writeEndObject();
            }

            /**
             * Reads a JSON stream into a {@link Properties}.
             *
             * @param jsonReader The {@link JsonReader} being read.
             * @return The {@link Properties} that the JSON stream represented, may return null.
             * @throws IOException If a {@link Properties} fails to be read from the {@code jsonReader}.
             */
            public static Properties fromJson(JsonReader jsonReader) throws IOException {
                return jsonReader.readObject(reader -> {
                    Properties properties = new Properties();

                    while (reader.nextToken() != JsonToken.END_OBJECT) {
                        String fieldName = reader.getFieldName();
                        reader.nextToken();

                        if ("provisioningState".equals(fieldName)) {
                            properties.provisioningState = reader.getString();
                        } else {
                            reader.skipChildren();
                        }
                    }

                    return properties;
                });
            }
        }
    }
}
