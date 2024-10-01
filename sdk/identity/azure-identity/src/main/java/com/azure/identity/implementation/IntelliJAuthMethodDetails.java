// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation;

/**
 * Represents Azure Tools for IntelliJ IDE Plugin's authentication method details.
 */
public class IntelliJAuthMethodDetails {

    private String accountEmail;
    private String credFilePath;
    private String authMethod;
    private String azureEnv;

    public IntelliJAuthMethodDetails(String accountEmail, String credFilePath, String authMethod, String azureEnv) {
        this.accountEmail = accountEmail;
        this.credFilePath = credFilePath;
        this.authMethod = authMethod;
        this.azureEnv = azureEnv;
    }

    /**
     * Get the account email.
     *
     * @return the account email.
     */
    public String getAccountEmail() {
        return accountEmail;
    }

    /**
     * Get the Service Principal cred file path.
     * @return the cred file path.
     */
    public String getCredFilePath() {
        return credFilePath;
    }

    /**
     * Get the auth method used by Azure Tools for IntelliJ plugin.
     *
     * @return the auth method used.
     */
    public String getAuthMethod() {
        return authMethod;
    }

    /**
     * Get the Azure env used.
     *
     * @return the Azure env used.
     */
    public String getAzureEnv() {
        return azureEnv;
    }
}
