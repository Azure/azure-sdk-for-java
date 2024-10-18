// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.shared;

import io.clientcore.core.json.JsonReader;
import io.clientcore.core.json.JsonSerializable;
import io.clientcore.core.json.JsonToken;
import io.clientcore.core.json.JsonWriter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Maps to the JSON return values from http://httpbin.org.
 */
public class HttpBinJSON implements JsonSerializable<HttpBinJSON> {
    private String uri;
    private Map<String, List<String>> headers;
    private Object data;
    private Map<String, List<String>> queryParams;

    /**
     * Gets the URI associated with this request.
     *
     * @return he URI associated with the request.
     */
    public String uri() {
        return uri;
    }

    /**
     * Sets the URI associated with this request.
     *
     * @param uri The URI associated with the request.
     */
    public HttpBinJSON uri(String uri) {
        this.uri = uri;

        return this;
    }

    /**
     * Gets the response headers.
     *
     * @return The response headers.
     */
    public Map<String, List<String>> headers() {
        return headers;
    }

    /**
     * Sets the response headers.
     *
     * @param headers The response headers.
     */
    public HttpBinJSON headers(Map<String, List<String>> headers) {
        this.headers = headers;

        return this;
    }

    /**
     * Gets the response body.
     *
     * @return The response body.
     */
    public Object data() {
        return data;
    }

    /**
     * Sets the response body.
     *
     * @param data The response body.
     */
    public HttpBinJSON data(Object data) {
        this.data = data;

        return this;
    }

    /**
     * Gets the response headers.
     *
     * @return The response headers.
     */
    public Map<String, List<String>> queryParams() {
        return queryParams;
    }

    /**
     * Sets the response headers.
     *
     * @param queryParams The response headers.
     */
    public HttpBinJSON queryParams(Map<String, List<String>> queryParams) {
        this.queryParams = queryParams;

        return this;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        HttpBinJSON binJson = (HttpBinJSON) other;

        return Objects.equals(uri, binJson.uri) && Objects.equals(headers, ((HttpBinJSON) other).headers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uri, headers);
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("uri", uri);
        jsonWriter.writeMapField("headers", headers,
            (headerWriter, headerList) -> headerWriter.writeArray(headerList, JsonWriter::writeString));
        jsonWriter.writeUntypedField("data", data);
        jsonWriter.writeMapField("queryParams", queryParams,
            (paramWriter, paramList) -> paramWriter.writeArray(paramList, JsonWriter::writeString));

        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of HttpBinJSON from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of HttpBinJSON if the JsonReader was pointing to an instance of it, or null if it was
     * pointing to JSON null.
     * @throws IOException If an error occurs while reading the HttpBinJSON.
     * @throws IllegalStateException If any of the required properties to create HttpBinJSON aren't found.
     */
    public static HttpBinJSON fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            HttpBinJSON httpBinJSON = new HttpBinJSON();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                // Example of case-insensitive names and where serialization named don't match field names.
                if ("uri".equalsIgnoreCase(fieldName)) {
                    httpBinJSON.uri = reader.getString();
                } else if ("headers".equalsIgnoreCase(fieldName)) {
                    // Pass the JsonReader to another JsonSerializable to read the inner object.
                    httpBinJSON.headers = reader.readMap(headerReader -> headerReader.readArray(JsonReader::getString));
                } else if ("data".equalsIgnoreCase(fieldName)) {
                    httpBinJSON.data = reader.readUntyped();
                } else if ("queryParams".equalsIgnoreCase(fieldName)) {
                    // Pass the JsonReader to another JsonSerializable to read the inner object.
                    httpBinJSON.queryParams = reader.readMap(
                        paramReader -> paramReader.readArray(JsonReader::getString));
                } else {
                    reader.skipChildren();
                }
            }

            return httpBinJSON;
        });
    }

    public String getHeaderValue(String name) {
        return headers == null ? null : headers.containsKey(name) ? headers.get(name).get(0) : null;
    }
}
