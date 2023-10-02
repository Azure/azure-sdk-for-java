// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.test.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Keeps track of transport layer recording options to send to proxy.
 */
public class TestProxyRecordingOptions {

    @JsonProperty("HandleRedirects")
    private boolean autoRedirect = false;
    @JsonProperty("Transport")
    private ProxyTransport proxyTransportOptions;

    /**
     * Model for proxy transport options
     */
    public static class ProxyTransport {
        @JsonProperty("Certificates")
        private List<Certificate> certificates;
        @JsonProperty("TLSValidationCert")
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
    }

    /**
     * Model representing the certificate item object
     */
    public static class Certificate {
        @JsonProperty("PemValue")
        private String pemValue;
        @JsonProperty("PemKey")
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
}
