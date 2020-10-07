// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.mediaservices;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;

@Validated
@ConfigurationProperties("azure.mediaservices")
public class MediaServicesProperties {
    @NotEmpty(message = "azure.mediaservices.tenant property must be configured.")
    private String tenant;

    /**
     * Media service Azure Active Directory client-id(application id).
     */
    @NotEmpty(message = "azure.mediaservices.client-id property must be configured.")
    private String clientId;

    /**
     * Media service Azure Active Directory client secret.
     */
    @NotEmpty(message = "azure.mediaservices.client-secret property must be configured.")
    private String clientSecret;

    /**
     * Media service REST API endpoint.
     */
    @NotEmpty(message = "azure.mediaservices.rest-api-endpoint property must be configured.")
    private String restApiEndpoint;

    /**
     * Proxy host if to use proxy.
     */
    private String proxyHost;

    /**
     * Proxy port if to use proxy.
     */
    private Integer proxyPort;

    /**
     * Proxy scheme if to use proxy. Default is http.
     */
    private String proxyScheme = "http";

    /**
     * Socket connect timeout
     */
    private Integer connectTimeout;

    /**
     * Socket read timeout
     */
    private Integer readTimeout;

    public String getTenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getRestApiEndpoint() {
        return restApiEndpoint;
    }

    public void setRestApiEndpoint(String restApiEndpoint) {
        this.restApiEndpoint = restApiEndpoint;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }

    public Integer getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(Integer proxyPort) {
        this.proxyPort = proxyPort;
    }

    public String getProxyScheme() {
        return proxyScheme;
    }

    public void setProxyScheme(String proxyScheme) {
        this.proxyScheme = proxyScheme;
    }

    public Integer getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Integer connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public Integer getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(Integer readTimeout) {
        this.readTimeout = readTimeout;
    }
}
