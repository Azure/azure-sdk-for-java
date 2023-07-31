package com.azure.core.test.models;

/**
 * Proxy layer Certificate item to send to proxy.
 */
public class ProxyOptionsTransportCertificatesItem {

    final String pemValue;
    final String pemKey;

    /**
     * Initializes a new instance of the ProxyOptionsTransportCertificatesItem class.
     * @param pemValue pem value of the certificate
     * @param pemKey pem key of the certificate
     */
    public ProxyOptionsTransportCertificatesItem(String pemValue, String pemKey) {
        this.pemValue = pemValue;
        this.pemKey = pemKey;
    }
}
