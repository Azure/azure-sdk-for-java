// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.serializer;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Maps to the JSON return values from <a href="http://httpbin.org">http://httpbin.org</a>.
 */
public class HttpBinJSON implements JsonSerializable<HttpBinJSON> {
    private String url;
    private Map<String, String> headers;
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
    public void url(String url) {
        this.url = url;
    }

    /**
     * Gets the response headers.
     *
     * @return The response headers.
     */
    public Map<String, String> headers() {
        return headers;
    }

    /**
     * Sets the response headers.
     *
     * @param headers The response headers.
     */
    public void headers(Map<String, String> headers) {
        this.headers = headers;
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
    public void data(Object data) {
        this.data = data;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) {
        jsonWriter.writeStartObject()
            .writeStringField("url", url, false);

        if (headers != null) {
            jsonWriter.writeStartObject("headers");

            headers.forEach(jsonWriter::writeStringField);

            jsonWriter.writeEndObject();
        }

        if (data != null) {
            jsonWriter.writeUntypedField("data", data);
        }

        return jsonWriter.writeEndObject().flush();
    }

    public static HttpBinJSON fromJson(JsonReader jsonReader) {
        return jsonReader.readObject(reader -> {
            String url = null;
            Map<String, String> headers = null;
            Object data = null;

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("url".equals(fieldName)) {
                    url = reader.getStringValue();
                } else if ("headers".equals(fieldName) && reader.currentToken() == JsonToken.START_OBJECT) {
                    if (headers == null) {
                        headers = new LinkedHashMap<>();
                    }

                    while (reader.nextToken() != JsonToken.END_OBJECT) {
                        fieldName = reader.getFieldName();
                        reader.nextToken();

                        headers.put(fieldName, reader.getStringValue());
                    }
                } else if ("data".equals(fieldName)) {
                    data = reader.readUntyped();
                } else {
                    reader.skipChildren();
                }
            }

            HttpBinJSON json = new HttpBinJSON();
            json.url(url);
            json.headers(headers);
            json.data(data);

            return json;
        });
    }
}
