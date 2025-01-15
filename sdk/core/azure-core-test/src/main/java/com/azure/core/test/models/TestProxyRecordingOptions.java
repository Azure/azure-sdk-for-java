// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test.models;

import com.azure.core.test.implementation.TestingHelpers;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonWriter;

import java.io.IOException;
import java.util.List;

/**
 * Keeps track of transport layer recording options to send to proxy.
 */
public class TestProxyRecordingOptions implements JsonSerializable<TestProxyRecordingOptions> {
    private boolean autoRedirect = false;
    private ProxyTransport proxyTransportOptions;

    /**
     * Model for proxy transport options
     */
    public static class ProxyTransport implements JsonSerializable<ProxyTransport> {
        private List<Certificate> certificates;
        private String tLSValidationCert;

        /**
         * Get allowed certificates for the recording.
         * @return the list of allowed certificates for the recording.
         */
        public List<Certificate> getCertificates() {
            return certificates;
        }

        /**
         * Set allowed certificates for the recording.
         * @param certificates the list of allowed certificates for the recording.
         * @return the updated {@link ProxyTransport} object.
         */
        public ProxyTransport setCertificates(List<Certificate> certificates) {
            this.certificates = certificates;
            return this;
        }

        /**
         * Get the TLS/SSL Certificate
         * @return the TLS/SSL Certificate
         */
        public String gettLSValidationCert() {
            return tLSValidationCert;
        }

        /**
         * Set the TLS/SSL Certificate
         * @param tLSValidationCert the TLS/SSL Certificate to set
         * @return the updated {@link ProxyTransport} object.
         */
        public ProxyTransport settLSValidationCert(String tLSValidationCert) {
            this.tLSValidationCert = tLSValidationCert;
            return this;
        }

        @Override
        public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
            return jsonWriter.writeStartObject()
                .writeArrayField("Certificates", certificates, JsonWriter::writeJson)
                .writeStringField("TLSValidationCert", tLSValidationCert)
                .writeEndObject();
        }

        /**
         * Deserializes an instance of ProxyTransport from the input JSON.
         *
         * @param jsonReader The JSON reader to deserialize the data from.
         * @return An instance of ProxyTransport deserialized from the JSON.
         * @throws IOException If the JSON reader encounters an error while reading the JSON.
         */
        public static ProxyTransport fromJson(JsonReader jsonReader) throws IOException {
            return TestingHelpers.readObject(jsonReader, ProxyTransport::new, (proxyTransport, fieldName, reader) -> {
                if ("Certificates".equals(fieldName)) {
                    proxyTransport.certificates = reader.readArray(Certificate::fromJson);
                } else if ("TLSValidationCert".equals(fieldName)) {
                    proxyTransport.tLSValidationCert = reader.getString();
                } else {
                    reader.skipChildren();
                }
            });
        }
    }

    /**
     * Model representing the certificate item object
     */
    public static class Certificate implements JsonSerializable<Certificate> {
        private String pemValue;
        private String pemKey;

        /**
         * Get the cert pem value
         * @return the cert pem value
         */
        public String getPemValue() {
            return pemValue;
        }

        /**
         * Set the cert pem value
         * @param pemValue the cert pem value
         * @return the {@link Certificate} object
         */
        public Certificate setPemValue(String pemValue) {
            this.pemValue = pemValue;
            return this;
        }

        /**
         * Get the cert pem key
         * @return the cert pem key
         */
        public String getPemKey() {
            return pemKey;
        }

        /**
         * Get the cert pem key
         * @param pemKey the cert pem key
         * @return the {@link Certificate} object
         */
        public Certificate setPemKey(String pemKey) {
            this.pemKey = pemKey;
            return this;
        }

        @Override
        public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
            return jsonWriter.writeStartObject()
                .writeStringField("PemValue", pemValue)
                .writeStringField("PemKey", pemKey)
                .writeEndObject();
        }

        /**
         * Deserializes an instance of Certificate from the input JSON.
         *
         * @param jsonReader The JSON reader to deserialize the data from.
         * @return An instance of Certificate deserialized from the JSON.
         * @throws IOException If the JSON reader encounters an error while reading the JSON.
         */
        public static Certificate fromJson(JsonReader jsonReader) throws IOException {
            return TestingHelpers.readObject(jsonReader, Certificate::new, (certificate, fieldName, reader) -> {
                if ("PemValue".equals(fieldName)) {
                    certificate.pemValue = reader.getString();
                } else if ("PemKey".equals(fieldName)) {
                    certificate.pemKey = reader.getString();
                } else {
                    reader.skipChildren();
                }
            });
        }
    }

    /**
     * Get if auto redirecting is allowed. Default value is set to true.
     * @return the boolean value indicating if auto redirect is allowed.
     */
    public boolean isAutoRedirect() {
        return autoRedirect;
    }

    /**
     * Set the boolean value indicating if auto redirect is allowed.
     * @param autoRedirect the boolean value indicating if auto redirect is allowed.
     * @return the {@link TestProxyRecordingOptions} object.
     */
    public TestProxyRecordingOptions setAutoRedirect(boolean autoRedirect) {
        this.autoRedirect = autoRedirect;
        return this;
    }

    /**
     * Get test proxy transport options for recording.
     * @return the {@link ProxyTransport} options.
     */
    public ProxyTransport getTransportOptions() {
        return proxyTransportOptions;
    }

    /**
     * Set test proxy transport options for recording.
     * @param proxyTransportOptions the test proxy transport options for recording
     * @return the {@link ProxyTransport} options.
     */
    public TestProxyRecordingOptions setTransportOptions(ProxyTransport proxyTransportOptions) {
        this.proxyTransportOptions = proxyTransportOptions;
        return this;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeBooleanField("HandleRedirects", autoRedirect)
            .writeJsonField("Transport", proxyTransportOptions)
            .writeEndObject();
    }

    /**
     * Deserializes an instance of TestProxyRecordingOptions from the input JSON.
     *
     * @param jsonReader The JSON reader to deserialize the data from.
     * @return An instance of TestProxyRecordingOptions deserialized from the JSON.
     * @throws IOException If the JSON reader encounters an error while reading the JSON.
     */
    public static TestProxyRecordingOptions fromJson(JsonReader jsonReader) throws IOException {
        return TestingHelpers.readObject(jsonReader, TestProxyRecordingOptions::new,
            (testProxyRecordingOptions, fieldName, reader) -> {
                if ("HandleRedirects".equals(fieldName)) {
                    testProxyRecordingOptions.autoRedirect = reader.getBoolean();
                } else if ("Transport".equals(fieldName)) {
                    testProxyRecordingOptions.proxyTransportOptions = ProxyTransport.fromJson(reader);
                } else {
                    reader.skipChildren();
                }
            });
    }
}
