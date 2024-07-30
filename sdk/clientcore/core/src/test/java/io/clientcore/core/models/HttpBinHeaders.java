// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.models;

import io.clientcore.core.implementation.util.DateTimeRfc1123;
import io.clientcore.core.json.JsonReader;
import io.clientcore.core.json.JsonSerializable;
import io.clientcore.core.json.JsonToken;
import io.clientcore.core.json.JsonWriter;

import java.io.IOException;

/**
 * Defines headers for httpbin.org operations.
 */
public class HttpBinHeaders implements JsonSerializable<HttpBinHeaders> {
    private DateTimeRfc1123 date;

    private String via;

    private String connection;

    /**
     * Gets the date of the response.
     *
     * @return The date of the response.
     */
    public DateTimeRfc1123 date() {
        return date;
    }

    /**
     * Sets the date of the response.
     *
     * @param date The date of the response.
     */
    public HttpBinHeaders date(DateTimeRfc1123 date) {
        this.date = date;
        return this;
    }

    /**
     * Gets any proxy information.
     * https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Via
     *
     * @return Gets any proxy information.
     */
    public String via() {
        return via;
    }

    /**
     * Sets any proxy information.
     * https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Via
     *
     * @param via Proxy information associated with this response.
     */
    public HttpBinHeaders via(String via) {
        this.via = via;
        return this;
    }

    /**
     * Gets information about the connection status after this message is received.
     * https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Connection
     *
     * @return Information whether to keep network connection open or not.
     */
    public String connection() {
        return connection;
    }

    /**
     * Sets information about the connection status after this message is received.
     * https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Connection
     *
     * @param connection Information whether to keep network connection open or not.
     */
    public HttpBinHeaders connection(String connection) {
        this.connection = connection;
        return this;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("Date", date == null ? null : date.toString());
        jsonWriter.writeStringField("Via", via);
        jsonWriter.writeStringField("Connection", connection);
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
    public static HttpBinHeaders fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            String date = null;
            String via = null;
            String connection = null;

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                // Example of case-insensitive names and where serialization named don't match field names.
                if ("Date".equalsIgnoreCase(fieldName)) {
                    date = reader.getString();
                } else if ("Via".equalsIgnoreCase(fieldName)) {
                    via = reader.getString();
                } else if ("Connection".equalsIgnoreCase(fieldName)) {
                    connection = reader.getString();
                }
            }

            return new HttpBinHeaders().connection(connection).date(new DateTimeRfc1123(date)).via(via);
        });
    }
}
