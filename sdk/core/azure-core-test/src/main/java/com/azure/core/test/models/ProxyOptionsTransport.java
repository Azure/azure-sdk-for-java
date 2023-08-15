// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Keeps track of transport layer recording options to send to proxy.
 */
public class ProxyOptionsTransport {

    @JsonProperty("HandleRedirects")
    private Boolean autoRedirect;
    @JsonProperty("Transport")
    private Transport transportOptions;

    public static class Transport{
        @JsonProperty("Certificates")
        private List<Certificate> certificates;
        @JsonProperty("TLSValidationCert")
        private String tLSValidationCert;

        public List<Certificate> getCertificates() {
            return certificates;
        }

        public Transport setCertificates(List<Certificate> certificates) {
            this.certificates = certificates;
            return this;
        }

        public String gettLSValidationCert() {
            return tLSValidationCert;
        }

        public Transport settLSValidationCert(String tLSValidationCert) {
            this.tLSValidationCert = tLSValidationCert;
            return this;
        }
    }

    public static class Certificate{
        @JsonProperty("PemValue")
        private String pemValue;
        @JsonProperty("PemKey")
        private String pemKey;

        public String getPemValue() {
            return pemValue;
        }

        public void setPemValue(String pemValue) {
            this.pemValue = pemValue;
        }

        public String getPemKey() {
            return pemKey;
        }

        public void setPemKey(String pemKey) {
            this.pemKey = pemKey;
        }
    }

    public Boolean isAutoRedirect() {
        return autoRedirect;
    }

    public ProxyOptionsTransport setAutoRedirect(Boolean autoRedirect) {
        this.autoRedirect = autoRedirect;
        return this;
    }

    public Transport getTransportOptions() {
        return transportOptions;
    }

    public ProxyOptionsTransport setTransportOptions(Transport transportOptions) {
        this.transportOptions = transportOptions;
        return this;
    }
}