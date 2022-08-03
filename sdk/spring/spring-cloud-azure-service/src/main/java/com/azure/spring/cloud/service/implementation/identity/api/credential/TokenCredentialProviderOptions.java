// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.identity.api.credential;

import com.azure.core.util.Configuration;
import com.azure.identity.AzureAuthorityHosts;
import com.azure.spring.cloud.service.implementation.identity.api.AuthProperty;

public class TokenCredentialProviderOptions {

    private String authorityHost = AzureAuthorityHosts.AZURE_PUBLIC_CLOUD;
    private String tenantId;
    private String clientId;
    private String clientSecret;
    private String clientCertificatePath;
    private String clientCertificatePassword;
    private String username;
    private String password;
    private boolean managedIdentityEnabled;
    private String tokenCredentialProviderClassName;
    private String tokenCredentialBeanName;
    private boolean cachedEnabled;

    public TokenCredentialProviderOptions() {

    }

    public TokenCredentialProviderOptions(Configuration configuration) {
        this.tenantId = AuthProperty.TENANT_ID.get(configuration);
        this.clientId = AuthProperty.CLIENT_ID.get(configuration);
        this.clientSecret = AuthProperty.CLIENT_SECRET.get(configuration);
        this.clientCertificatePath = AuthProperty.CLIENT_CERTIFICATE_PATH.get(configuration);
        this.clientCertificatePassword = AuthProperty.CLIENT_CERTIFICATE_PASSWORD.get(configuration);
        this.username = AuthProperty.USERNAME.get(configuration);
        this.password = AuthProperty.PASSWORD.get(configuration);
        this.managedIdentityEnabled = Boolean.TRUE.equals(AuthProperty.MANAGED_IDENTITY_ENABLED.getBoolean(configuration));
        this.tokenCredentialProviderClassName = AuthProperty.TOKEN_CREDENTIAL_PROVIDER_CLASS_NAME.get(configuration);
        this.tokenCredentialBeanName = AuthProperty.TOKEN_CREDENTIAL_BEAN_NAME.get(configuration);
        this.cachedEnabled = Boolean.TRUE.equals(AuthProperty.CACHE_ENABLED.getBoolean(configuration));
        this.authorityHost = AuthProperty.AUTHORITY_HOST.get(configuration);
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
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

    public String getClientCertificatePath() {
        return clientCertificatePath;
    }

    public void setClientCertificatePath(String clientCertificatePath) {
        this.clientCertificatePath = clientCertificatePath;
    }

    public String getClientCertificatePassword() {
        return clientCertificatePassword;
    }

    public void setClientCertificatePassword(String clientCertificatePassword) {
        this.clientCertificatePassword = clientCertificatePassword;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isManagedIdentityEnabled() {
        return managedIdentityEnabled;
    }

    public void setManagedIdentityEnabled(boolean managedIdentityEnabled) {
        this.managedIdentityEnabled = managedIdentityEnabled;
    }

    public String getTokenCredentialProviderClassName() {
        return tokenCredentialProviderClassName;
    }

    public void setTokenCredentialProviderClassName(String tokenCredentialProviderClassName) {
        this.tokenCredentialProviderClassName = tokenCredentialProviderClassName;
    }

    public String getTokenCredentialBeanName() {
        return tokenCredentialBeanName;
    }

    public void setTokenCredentialBeanName(String tokenCredentialBeanName) {
        this.tokenCredentialBeanName = tokenCredentialBeanName;
    }

    public boolean isCachedEnabled() {
        return cachedEnabled;
    }

    public void setCachedEnabled(boolean cachedEnabled) {
        this.cachedEnabled = cachedEnabled;
    }

    public String getAuthorityHost() {
        return authorityHost;
    }

    public void setAuthorityHost(String authorityHost) {
        this.authorityHost = authorityHost;
    }

}
