// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation;

public class IntelliJAuthMethodDetails {

    private String accountEmail;
    private String credFilePath;
    private String authMethod;
    private String azureEnv;

    public String getAccountEmail() {
        return accountEmail;
    }

    public String getCredFilePath() {
        return credFilePath;
    }

    public String getAuthMethod() {
        return authMethod;
    }

    public String getAzureEnv() {
        return azureEnv;
    }
}
