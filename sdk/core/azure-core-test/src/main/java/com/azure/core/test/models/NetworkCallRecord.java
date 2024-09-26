// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test.models;

import com.azure.core.test.implementation.TestingHelpers;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonWriter;

import java.io.IOException;
import java.util.Map;

/**
 * Keeps track of network call records from each unit test session.
 */
public class NetworkCallRecord implements JsonSerializable<NetworkCallRecord> {
    private String method;
    private String uri;
    private Map<String, String> headers;
    private Map<String, String> response;
    private NetworkCallError exception;

    /**
     * Gets the HTTP method for with this network call
     *
     * @return The HTTP method.
     */
    public String getMethod() {
        return method;
    }

    /**
     * Sets the HTTP method for with this network call
     *
     * @param method HTTP method for this network call.
     */
    public void setMethod(String method) {
        this.method = method;
    }

    /**
     * Gets the URL for this network call.
     *
     * @return The URL for this network call.
     */
    public String getUri() {
        return uri;
    }

    /**
     * Sets the URL for this network call.
     *
     * @param uri The URL for this network call.
     */
    public void setUri(String uri) {
        this.uri = uri;
    }

    /**
     * Gets the HTTP headers for the network call.
     *
     * @return The HTTP headers for the network call.
     */
    public Map<String, String> getHeaders() {
        return headers;
    }

    /**
     * Sets the HTTP headers for the network call.
     *
     * @param headers The HTTP headers for the network call.
     */
    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    /**
     * Gets the contents of the HTTP response as a map of its HTTP headers and response body. The HTTP response body is
     * mapped under key "Body".
     *
     * @return Contents of the HTTP response.
     */
    public Map<String, String> getResponse() {
        return response;
    }

    /**
     * Sets the contents of the HTTP response as a map of its HTTP headers and response body. The HTTP response body is
     * mapped under key "body".
     *
     * @param response Contents of the HTTP response.
     */
    public void setResponse(Map<String, String> response) {
        this.response = response;
    }

    /**
     * Gets the throwable thrown during evaluation of the network call.
     *
     * @return Throwable thrown during the network call.
     */
    public NetworkCallError getException() {
        return exception;
    }

    /**
     * Sets the throwable thrown during evaluation of the network call.
     *
     * @param exception Throwable thrown during the network call.
     */
    public void setException(NetworkCallError exception) {
        this.exception = exception;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeStringField("Method", method)
            .writeStringField("Uri", uri)
            .writeMapField("Headers", headers, JsonWriter::writeString)
            .writeMapField("Response", response, JsonWriter::writeString)
            .writeJsonField("Exception", exception)
            .writeEndObject();
    }

    /**
     * Deserializes an instance of NetworkCallRecord from the input JSON.
     *
     * @param jsonReader The JSON reader to deserialize the data from.
     * @return An instance of NetworkCallRecord deserialized from the JSON.
     * @throws IOException If the JSON reader encounters an error while reading the JSON.
     */
    public static NetworkCallRecord fromJson(JsonReader jsonReader) throws IOException {
        return TestingHelpers.readObject(jsonReader, NetworkCallRecord::new, (callRecord, fieldName, reader) -> {
            if ("Method".equals(fieldName)) {
                callRecord.method = reader.getString();
            } else if ("Uri".equals(fieldName)) {
                callRecord.uri = reader.getString();
            } else if ("Headers".equals(fieldName)) {
                callRecord.headers = reader.readMap(JsonReader::getString);
            } else if ("Response".equals(fieldName)) {
                callRecord.response = reader.readMap(JsonReader::getString);
            } else if ("Exception".equals(fieldName)) {
                callRecord.exception = NetworkCallError.fromJson(reader);
            } else {
                reader.skipChildren();
            }
        });
    }
}
