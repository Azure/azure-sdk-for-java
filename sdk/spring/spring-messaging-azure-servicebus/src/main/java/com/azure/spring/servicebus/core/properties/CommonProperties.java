// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.servicebus.core.properties;

import com.azure.spring.core.connectionstring.implementation.ServiceBusConnectionString;
import com.azure.spring.core.properties.client.ClientProperties;
import com.azure.spring.core.properties.credential.TokenCredentialProperties;
import com.azure.spring.core.properties.profile.AzureProfile;
import com.azure.spring.core.properties.proxy.ProxyProperties;
import com.azure.spring.core.properties.retry.RetryProperties;
import com.azure.spring.core.properties.AzureProperties;
import com.azure.spring.service.servicebus.properties.ServiceBusCommonDescriptor;

/**
 * Common properties shared by Service Bus namespace, a producer, and a consumer.
 */
public class CommonProperties implements ServiceBusCommonDescriptor, AzureProperties {
    //TODO(yiliu6): extends AzureSdkProperties instead of implementing AzureProperties after xiada's pr getting merged
    private ClientProperties client = new ClientProperties();
    private ProxyProperties proxy = new ProxyProperties();
    private RetryProperties retry = new RetryProperties();
    private TokenCredentialProperties credential = new TokenCredentialProperties();
    private AzureProfile profile = new AzureProfile();

    private String domainName = "servicebus.windows.net";
    private String namespace;
    private String connectionString;

    @Override
    public ClientProperties getClient() {
        return client;
    }

    public void setClient(ClientProperties client) {
        this.client = client;
    }

    @Override
    public ProxyProperties getProxy() {
        return proxy;
    }

    public void setProxy(ProxyProperties proxy) {
        this.proxy = proxy;
    }

    @Override
    public RetryProperties getRetry() {
        return retry;
    }

    public void setRetry(RetryProperties retry) {
        this.retry = retry;
    }

    @Override
    public TokenCredentialProperties getCredential() {
        return credential;
    }

    public void setCredential(TokenCredentialProperties credential) {
        this.credential = credential;
    }

    @Override
    public AzureProfile getProfile() {
        return profile;
    }

    public void setProfile(AzureProfile profile) {
        this.profile = profile;
    }

    private String extractFqdnFromConnectionString() {
        if (this.connectionString == null) {
            return null;
        }
        return new ServiceBusConnectionString(this.connectionString).getFullyQualifiedNamespace();
    }

    @Override
    public String getFQDN() {
        return this.namespace == null ? extractFqdnFromConnectionString() : (this.namespace + "." + domainName);
    }

    @Override
    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    @Override
    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    @Override
    public String getConnectionString() {
        return connectionString;
    }

    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }

}
