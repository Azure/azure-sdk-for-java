package com.azure.identity.implementation;

public class AuthMethodDetails {

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
