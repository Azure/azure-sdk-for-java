// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.models;

import com.generic.json.JsonReader;
import com.generic.json.JsonSerializable;
import com.generic.json.JsonToken;
import com.generic.json.JsonWriter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Maps to the JSON return values from http://httpbin.org.
 */
public class HttpBinJSON implements JsonSerializable<HttpBinJSON> {
    private String url;

    private Map<String, List<String>> headers;

    private Object data;

    /**
     * Gets the URL associated with this request.
     *
     * @return he URL associated with the request.
     */
    public String url() {
        return url;
    }

    /**
     * Sets the URL associated with this request.
     *
     * @param url The URL associated with the request.
     */
    public HttpBinJSON url(String url) {
        this.url = url;
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

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        HttpBinJSON binJson = (HttpBinJSON) other;

        return Objects.equals(url, binJson.url) && Objects.equals(headers, ((HttpBinJSON) other).headers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, headers);
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("url", url);
        jsonWriter.writeMapField("headers", headers, (mapEntryWriter, list)
            -> mapEntryWriter.writeArray(list, JsonWriter::writeString));
        jsonWriter.writeStartObject("data");
        jsonWriter.writeEndObject();

        return jsonWriter;
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
            String url = null;
            Map<String, List<String>> headers = null;
            Object data = null;

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                // Example of case-insensitive names and where serialization named don't match field names.
                if ("url".equalsIgnoreCase(fieldName)) {
                    url = reader.getString();
                } else if ("headers".equalsIgnoreCase(fieldName)) {
                    // Pass the JsonReader to another JsonSerializable to read the inner object.
                    headers =
                        reader.readMap(
                            reader1 -> reader1.readArray(Object::toString));
                } else if ("data".equalsIgnoreCase(fieldName)) {
                    data = reader.readUntyped();
                } else {
                    reader.skipChildren();
                }
            }

            return new HttpBinJSON().url(url).headers(headers).data(data);
        });
    }

    public String getHeaderValue(String name) {
        return headers == null ? null : headers.containsKey(name) ? headers.get(name).get(0) : null;
    }
}
