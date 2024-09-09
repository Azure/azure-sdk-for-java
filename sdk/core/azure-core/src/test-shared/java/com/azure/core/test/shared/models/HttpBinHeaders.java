// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.test.shared.models;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.util.DateTimeRfc1123;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;
import java.util.Objects;

/**
 * Defines headers for httpbin.org operations.
 */
public final class HttpBinHeaders implements JsonSerializable<HttpBinHeaders> {
    private static final HttpHeaderName X_PROCESSED_TIME = HttpHeaderName.fromString("X-Processed-Time");
    private static final HttpHeaderName ACCESS_CONTROL_ALLOW_CREDENTIALS
        = HttpHeaderName.fromString("Access-Control-Allow-Credentials");

    private DateTimeRfc1123 date;
    private String via;
    private String connection;
    private double xProcessedTime;
    private boolean accessControlAllowCredentials;

    /**
     * Creates an instance of HttpBinHeaders.
     */
    public HttpBinHeaders() {
    }

    /**
     * Creates an instance of HttpBinHeaders.
     *
     * @param headers The headers to use for initialization.
     */
    public HttpBinHeaders(HttpHeaders headers) {
        String dateHeader = headers.getValue(HttpHeaderName.DATE);
        if (dateHeader != null) {
            this.date = new DateTimeRfc1123(dateHeader);
        }
        this.via = headers.getValue(HttpHeaderName.VIA);
        this.connection = headers.getValue(HttpHeaderName.CONNECTION);

        String xProcessedTimeHeader = headers.getValue(X_PROCESSED_TIME);
        if (xProcessedTimeHeader != null) {
            this.xProcessedTime = Double.parseDouble(xProcessedTimeHeader);
        }

        String accessControlAllowCredentialsHeader = headers.getValue(ACCESS_CONTROL_ALLOW_CREDENTIALS);
        if (accessControlAllowCredentialsHeader != null) {
            this.accessControlAllowCredentials = Boolean.parseBoolean(accessControlAllowCredentialsHeader);
        }
    }

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
    public void date(DateTimeRfc1123 date) {
        this.date = date;
    }

    /**
     * Gets any proxy information.
     *
     * @return Gets any proxy information.
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Via">Via</a>
     */
    public String via() {
        return via;
    }

    /**
     * Sets any proxy information.
     *
     * @param via Proxy information associated with this response.
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Via">Via</a>
     */
    public void via(String via) {
        this.via = via;
    }

    /**
     * Gets information about the connection status after this message is received.
     *
     * @return Information whether to keep network connection open or not.
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Connection">Connection</a>
     */
    public String connection() {
        return connection;
    }

    /**
     * Sets information about the connection status after this message is received.
     *
     * @param connection Information whether to keep network connection open or not.
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Connection">Connection</a>
     */
    public void connection(String connection) {
        this.connection = connection;
    }

    /**
     * Gets the time it took to process this request.
     *
     * @return Time to process this request in seconds.
     * @see <a href="https://github.com/kennethreitz/flask-common/blob/master/flask_common.py#L129">X-Processed-Time</a>
     */
    public double xProcessedTime() {
        return xProcessedTime;
    }

    /**
     * Indicates whether to expose response to frontend JS code.
     *
     * @return True to expose response to frontend JS code and false otherwise.
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Access-Control-Allow-Credentials">Access-Control-Allow-Credentials</a>
     */
    public boolean accessControlAllowCredentials() {
        return accessControlAllowCredentials;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeStringField("Date", Objects.toString(date, null))
            .writeStringField("Via", via)
            .writeStringField("Connection", connection)
            .writeDoubleField("X-Processed-Time", xProcessedTime)
            .writeBooleanField("Access-Control-Allow-Credentials", accessControlAllowCredentials)
            .writeEndObject();
    }

    /**
     * Deserializes an instance of HttpBinHeaders from the input JSON.
     *
     * @param jsonReader The JSON reader to read from.
     * @return An instance of HttpBinHeaders deserialized from the input JSON.
     * @throws IOException If an error occurs while reading from the JSON.
     */
    public static HttpBinHeaders fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            HttpBinHeaders headers = new HttpBinHeaders();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("Date".equals(fieldName)) {
                    headers.date = reader.getNullable(nonNullReader -> new DateTimeRfc1123(nonNullReader.getString()));
                } else if ("Via".equals(fieldName)) {
                    headers.via = reader.getString();
                } else if ("Connection".equals(fieldName)) {
                    headers.connection = reader.getString();
                } else if ("X-Processed-Time".equals(fieldName)) {
                    headers.xProcessedTime = reader.getDouble();
                } else if ("Access-Control-Allow-Credentials".equals(fieldName)) {
                    headers.accessControlAllowCredentials = reader.getBoolean();
                } else {
                    reader.skipChildren();
                }
            }

            return headers;
        });
    }
}
