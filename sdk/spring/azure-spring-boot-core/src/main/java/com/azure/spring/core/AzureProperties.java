// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core;

/**
 * Unified properties for Azure SDK clients.
 */
public class AzureProperties {


    private CredentialProperties credentialProperties;

    private EnvironmentProperties environment;

    public CredentialProperties getCredentialProperties() {
        return credentialProperties;
    }

    public void setCredentialProperties(CredentialProperties credentialProperties) {
        this.credentialProperties = credentialProperties;
    }

    public EnvironmentProperties getEnvironment() {
        return environment;
    }

    public void setEnvironment(EnvironmentProperties environment) {
        this.environment = environment;
    }
}
