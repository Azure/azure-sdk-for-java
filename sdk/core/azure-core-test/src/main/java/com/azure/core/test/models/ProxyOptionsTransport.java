// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test.models;

import java.util.List;

/**
 * Keeps track of transport layer recording options to send to proxy.
 */
public class ProxyOptionsTransport {

    boolean allowsAutoRedirect;
    String TLSValidationCertPath;
    List<ProxyOptionsTransportCertificatesItem> certificates;

    public boolean isAutoRedirectAllowed() {
        return allowsAutoRedirect;
    }

    public ProxyOptionsTransport setAllowsAutoRedirect(boolean allowsAutoRedirect) {
        this.allowsAutoRedirect = allowsAutoRedirect;
        return this;
    }

    public String getTLSValidationCertPath() {
        return TLSValidationCertPath;
    }

    public ProxyOptionsTransport setTLSValidationCertPath(String TLSValidationCertPath) {
        this.TLSValidationCertPath = TLSValidationCertPath;
        return this;
    }

    public List<ProxyOptionsTransportCertificatesItem> getCertificates() {
        return certificates;
    }

    public ProxyOptionsTransport setCertificates(
        List<ProxyOptionsTransportCertificatesItem> certificates) {
        this.certificates = certificates;
        return this;
    }
}
