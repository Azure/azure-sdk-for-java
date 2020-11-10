// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad.implementation;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

@ConfigurationProperties("azure.activedirectory")
public class AzureActiveDirectoryProperties {

    private String uri;
    private String tenantId;
    private String clientId;
    private String clientSecret;

    private Map<String, AuthorizationProperties> authorization = new HashMap<>();

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getUri() {
        return uri;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setAuthorization(Map<String, AuthorizationProperties> authorization) {
        this.authorization = authorization;
    }

    public Map<String, AuthorizationProperties> getAuthorization() {
        return authorization;
    }
}
