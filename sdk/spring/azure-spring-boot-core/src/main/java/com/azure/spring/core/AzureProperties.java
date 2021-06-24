// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core;


import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.util.logging.LogLevel;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Azure related properties.
 */
@Validated
@ConfigurationProperties(AzureProperties.PREFIX)
public class AzureProperties {

    public static final String PREFIX = "spring.cloud.azure";

    /**
     * Disables telemetry collection.
     */
    private boolean allowTelemetry;

    /**
     * Disables tracing.
     */
    private boolean tracingDisabled;

    /**
     * The Azure Active Directory endpoint to connect to.
     */
    private String authorityHost;

    /**
     * Name of the Azure cloud to connect to.
     */
    private String environment = "Azure";

    /**
     * Client id to use when performing service principal authentication with Azure.
     */
    private String clientId;

    /**
     * Client secret to use when performing service principal authentication with Azure.
     */
    private String clientSecret;

    /**
     * Path of a PEM certificate file to use when performing service principal authentication with Azure.
     */
    private String clientCertificatePath;

    /**
     * Flag to enable MSI.
     */
    private boolean msiEnabled = false;

    /**
     * Endpoint to connect to when using Azure Active Directory managed service identity (MSI).
     */
    private String msiEndpoint;

    /**
     * Secret when connecting to Azure Active Directory using managed service identity (MSI).
     */
    private String msiSecret;

    /**
     * Name of the Azure resource group.
     */
    private String resourceGroup;

    /**
     * Subscription id to use when connecting to Azure resources.
     */
    private String subscriptionId;

    /**
     * Tenant id for the Azure resources.
     */
    private String tenantId;

    /**
     * Username to use when performing username/password authentication with Azure.
     */
    private String username;

    /**
     * Password to use when performing username/password authentication with Azure.
     */
    private String password;

    /**
     * Enables logging by setting a log level.
     */
    private LogLevel logLevel;

    /**
     * Enables HTTP request/response logging by setting an HTTP log detail level.
     */
    private HttpLogDetailLevel httpLogDetailLevel;

    /**
     * URL of the proxy for HTTP connections.
     */
    private String httpProxy;

    /**
     * URL of the proxy for HTTPS connections.
     */
    private String httpsProxy;

    /**
     * A list of hosts or CIDR to not use proxy HTTP/HTTPS connections through, separated by comma.
     */
    private String noProxy;

    /**
     * Flag to disable the CP1 client capabilities in Azure Identity Token credentials.
     */
    private boolean identityDisableCP1;

    /**
     * Endpoint to connect to when using Azure Active Directory managed service identity (MSI).
     */
    private String identityEndpoint;

    /**
     * Header when connecting to Azure Active Directory using managed service identity (MSI).
     */
    private String identityHeader;

    public boolean getAllowTelemetry() {
        return allowTelemetry;
    }

    public void setAllowTelemetry(boolean allowTelemetry) {
        this.allowTelemetry = allowTelemetry;
    }

    public String getAuthorityHost() {
        return authorityHost;
    }

    public void setAuthorityHost(String authorityHost) {
        this.authorityHost = authorityHost;
    }

    public String getClientCertificatePath() {
        return clientCertificatePath;
    }

    public void setClientCertificatePath(String clientCertificatePath) {
        this.clientCertificatePath = clientCertificatePath;
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

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public HttpLogDetailLevel getHttpLogDetailLevel() {
        return httpLogDetailLevel;
    }

    public void setHttpLogDetailLevel(HttpLogDetailLevel httpLogDetailLevel) {
        this.httpLogDetailLevel = httpLogDetailLevel;
    }

    public String getHttpProxy() {
        return httpProxy;
    }

    public void setHttpProxy(String httpProxy) {
        this.httpProxy = httpProxy;
    }

    public String getHttpsProxy() {
        return httpsProxy;
    }

    public void setHttpsProxy(String httpsProxy) {
        this.httpsProxy = httpsProxy;
    }

    public String getIdentityEndpoint() {
        return identityEndpoint;
    }

    public void setIdentityEndpoint(String identityEndpoint) {
        this.identityEndpoint = identityEndpoint;
    }

    public String getIdentityHeader() {
        return identityHeader;
    }

    public void setIdentityHeader(String identityHeader) {
        this.identityHeader = identityHeader;
    }

    public LogLevel getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(LogLevel logLevel) {
        this.logLevel = logLevel;
    }

    public String getMsiEndpoint() {
        return msiEndpoint;
    }

    public void setMsiEndpoint(String msiEndpoint) {
        this.msiEndpoint = msiEndpoint;
    }

    public String getMsiSecret() {
        return msiSecret;
    }

    public void setMsiSecret(String msiSecret) {
        this.msiSecret = msiSecret;
    }

    public String getNoProxy() {
        return noProxy;
    }

    public void setNoProxy(String noProxy) {
        this.noProxy = noProxy;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getResourceGroup() {
        return resourceGroup;
    }

    public void setResourceGroup(String resourceGroup) {
        this.resourceGroup = resourceGroup;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isIdentityDisableCP1() {
        return identityDisableCP1;
    }

    public void setIdentityDisableCP1(boolean identityDisableCP1) {
        this.identityDisableCP1 = identityDisableCP1;
    }

    public boolean isMsiEnabled() {
        return msiEnabled;
    }

    public void setMsiEnabled(boolean msiEnabled) {
        this.msiEnabled = msiEnabled;
    }

    public boolean isTracingDisabled() {
        return tracingDisabled;
    }

    public void setTracingDisabled(boolean tracingDisabled) {
        this.tracingDisabled = tracingDisabled;
    }
}
