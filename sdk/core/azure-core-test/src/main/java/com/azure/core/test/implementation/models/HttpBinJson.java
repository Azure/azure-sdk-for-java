// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.test.implementation.models;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Maps to the JSON return values from http://httpbin.org.
 */
public final class HttpBinJson implements JsonSerializable<HttpBinJson> {
    private String url;
    private Map<String, List<String>> headers;
    private String data;

    /**
     * Creates an instance of HttpBinJson.
     */
    public HttpBinJson() {
    }

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
    public void url(String url) {
        this.url = url;
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
     * Gets the value of the header with the provided name.
     *
     * @param name The name of the header.
     * @return The value of the header with the provided name.
     */
    public String getHeaderValue(String name) {
        return headers == null ? null : headers.containsKey(name) ? headers.get(name).get(0) : null;
    }

    /**
     * Sets the response headers.
     *
     * @param headers The response headers.
     */
    public void headers(Map<String, List<String>> headers) {
        this.headers = headers;
    }

    /**
     * Gets the response body.
     *
     * @return The response body.
     */
    public String data() {
        return data;
    }

    /**
     * Sets the response body.
     *
     * @param data The response body.
     */
    public void data(String data) {
        this.data = data;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeStringField("url", url)
            .writeMapField("headers", headers, (writer, list) -> writer.writeArray(list, JsonWriter::writeString))
            .writeStringField("data", data)
            .writeEndObject();
    }

    /**
     * Deserializes an instance of HttpBinJson from the input JSON.
     *
     * @param jsonReader The JSON reader to read from.
     * @return An instance of HttpBinJson deserialized from the input JSON.
     * @throws IOException If an error occurs while reading from the JSON.
     */
    public static HttpBinJson fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            HttpBinJson headers = new HttpBinJson();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("url".equals(fieldName)) {
                    headers.url = reader.getString();
                } else if ("headers".equals(fieldName)) {
                    headers.headers = reader.readMap(listReader -> listReader.readArray(JsonReader::getString));
                } else if ("data".equals(fieldName)) {
                    headers.data = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }

            return headers;
        });
    }
}
