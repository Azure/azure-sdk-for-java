// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core;


import com.google.common.base.Strings;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;

import javax.annotation.PostConstruct;

/**
 * Azure related properties.
 */
@Validated
@ConfigurationProperties(AzureProperties.PREFIX)
public class AzureProperties {

    public static final String PREFIX = "spring.cloud.azure";

    /**
     * Client id to use when performing service principal authentication with Azure.
     */
    private String clientId;

    /**
     * Client secret to use when performing service principal authentication with Azure.
     */
    private String clientSecret;

    /**
     * Tenant id for the Azure resources.
     */
    private String tenantId;

    /**
     * Name of the Azure resource group.
     */
    private String resourceGroup;

    /**
     * Name of the Azure cloud to connect to.
     */
    private AzureCloud cloud = AzureCloud.Azure;

    /**
     * The Azure Active Directory endpoint to connect to.
     */
    private String authorityHost;

    /**
     * Name of the region where resources would be automatically created.
     */
    private String region;

    /**
     * Flag to automatically create resources.
     */
    private boolean autoCreateResources = false;

    /**
     * Flag to enable MSI.
     */
    private boolean msiEnabled = false;

    /**
     * Subscription id to use when connecting to Azure resources.
     */
    private String subscriptionId;

    /**
     * URL of the proxy for HTTP connections.
     */
    private String httpProxy;

    /**
     * URL of the proxy for HTTPS connections.
     */
    private String httpsProxy;

    /**
     * Endpoint to connect to when using Azure Active Directory managed service identity (MSI).
     */
    private String identityEndpoint;

    /**
     * Header when connecting to Azure Active Directory using managed service identity (MSI).
     */
    private String identityHeader;

    /**
     * A list of hosts or CIDR to not use proxy HTTP/HTTPS connections through, separated by comma.
     */
    private String noProxy;

    /**
     * Endpoint to connect to when using Azure Active Directory managed service identity (MSI).
     */
    private String msiEndpoint;

    /**
     * Secret when connecting to Azure Active Directory using managed service identity (MSI).
     */
    private String msiSecret;

    /**
     * Username to use when performing username/password authentication with Azure.
     */
    private String username;

    /**
     * Password to use when performing username/password authentication with Azure.
     */
    private String password;

    /**
     * Path of a PEM certificate file to use when performing service principal authentication with Azure.
     */
    private String clientCertificatePath;

    /**
     * Flag to disable the CP1 client capabilities in Azure Identity Token credentials.
     */
    private boolean identityDisableCP1;

    /**
     * Disables telemetry collection.
     */
    private boolean telemetryDisabled;

    /**
     * Enables logging by setting a log level.
     */
    private String logLevel;

    /**
     * Enables HTTP request/response logging by setting an HTTP log detail level.
     */
    private String httpLogDetailLevel;

    /**
     * Disables tracing.
     */
    private boolean tracingDisabled;


    @PostConstruct
    private void validate() {
        if (autoCreateResources) {
            Assert.hasText(this.region,
                "When auto create resources is enabled, spring.cloud.azure.region must be provided");
        }

        if (msiEnabled && Strings.isNullOrEmpty(subscriptionId)) {
            Assert.hasText(this.subscriptionId, "When msi is enabled, "
                + "spring.cloud.azure.subscription-id must be provided");
        }
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

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getResourceGroup() {
        return resourceGroup;
    }

    public void setResourceGroup(String resourceGroup) {
        this.resourceGroup = resourceGroup;
    }

    public AzureCloud getCloud() {
        return cloud;
    }

    public void setCloud(AzureCloud cloud) {
        this.cloud = cloud;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public boolean isAutoCreateResources() {
        return autoCreateResources;
    }

    public void setAutoCreateResources(boolean autoCreateResources) {
        this.autoCreateResources = autoCreateResources;
    }

    public boolean isMsiEnabled() {
        return msiEnabled;
    }

    public void setMsiEnabled(boolean msiEnabled) {
        this.msiEnabled = msiEnabled;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public String getAuthorityHost() {
        return authorityHost;
    }

    public void setAuthorityHost(String authorityHost) {
        this.authorityHost = authorityHost;
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

    public String getNoProxy() {
        return noProxy;
    }

    public void setNoProxy(String noProxy) {
        this.noProxy = noProxy;
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

    public String getClientCertificatePath() {
        return clientCertificatePath;
    }

    public void setClientCertificatePath(String clientCertificatePath) {
        this.clientCertificatePath = clientCertificatePath;
    }

    public boolean isIdentityDisableCP1() {
        return identityDisableCP1;
    }

    public void setIdentityDisableCP1(boolean identityDisableCP1) {
        this.identityDisableCP1 = identityDisableCP1;
    }

    public boolean isTelemetryDisabled() {
        return telemetryDisabled;
    }

    public void setTelemetryDisabled(boolean telemetryDisabled) {
        this.telemetryDisabled = telemetryDisabled;
    }

    public String getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }

    public String getHttpLogDetailLevel() {
        return httpLogDetailLevel;
    }

    public void setHttpLogDetailLevel(String httpLogDetailLevel) {
        this.httpLogDetailLevel = httpLogDetailLevel;
    }

    public boolean isTracingDisabled() {
        return tracingDisabled;
    }

    public void setTracingDisabled(boolean tracingDisabled) {
        this.tracingDisabled = tracingDisabled;
    }
}
