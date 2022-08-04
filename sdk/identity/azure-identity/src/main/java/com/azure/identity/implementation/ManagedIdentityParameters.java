package com.azure.identity.implementation;

public class ManagedIdentityParameters {
    private String identityEndpoint;
    private String identityHeader;
    private String msiEndpoint;
    private String msiSecret;
    private String identityServerThumbprint;

    public ManagedIdentityParameters() { }


    public String getIdentityEndpoint() {
        return identityEndpoint;
    }

    public ManagedIdentityParameters setIdentityEndpoint(String identityEndpoint) {
        this.identityEndpoint = identityEndpoint;
        return this;
    }

    public String getIdentityHeader() {
        return identityHeader;
    }

    public ManagedIdentityParameters setIdentityHeader(String identityHeader) {
        this.identityHeader = identityHeader;
        return this;
    }

    public String getMsiEndpoint() {
        return msiEndpoint;
    }

    public ManagedIdentityParameters setMsiEndpoint(String msiEndpoint) {
        this.msiEndpoint = msiEndpoint;
        return this;
    }

    public String getMsiSecret() {
        return msiSecret;
    }

    public ManagedIdentityParameters setMsiSecret(String msiSecret) {
        this.msiSecret = msiSecret;
        return this;
    }

    public String getIdentityServerThumbprint() {
        return identityServerThumbprint;
    }

    public ManagedIdentityParameters setIdentityServerThumbprint(String identityServerThumbprint) {
        this.identityServerThumbprint = identityServerThumbprint;
        return this;
    }
}
