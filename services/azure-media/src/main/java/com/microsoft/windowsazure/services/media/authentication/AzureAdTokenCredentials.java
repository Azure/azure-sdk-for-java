package com.microsoft.windowsazure.services.media.authentication;

import com.microsoft.aad.adal4j.AsymmetricKeyCredential;
import com.microsoft.aad.adal4j.ClientCredential;

/**
 * Represents an Azure AD Credential for a specific resource
 */
public class AzureAdTokenCredentials {

    private String tenant;

    private ClientCredential clientKey;

    private AsymmetricKeyCredential asymmetricKeyCredential;

    private AzureAdClientUsernamePassword azureAdClientUsernamePassword;

    private AzureAdTokenCredentialType credentialType;

    private AzureEnvironment azureEnvironment;

    /**
     * Gets the tenant.
     * @return the tenant.
     */
    public String getTenant() {
        return this.tenant;
    }

    /**
     * Gets the client username password
     * @return the client symmetric key.
     */
    public AzureAdClientUsernamePassword getAzureAdClientUsernamePassword() {
        return this.azureAdClientUsernamePassword;
    }

    /**
     * Gets the client symmetric key credential.
     * @return the ClientCredential
     */
    public ClientCredential getClientKey() {
        return this.clientKey;
    }

    /**
     * Gets the AsymmetricKeyCredential
     * @return the AsymmetricKeyCredential
     */
    public AsymmetricKeyCredential getAsymmetricKeyCredential() {
        return this.asymmetricKeyCredential;
    }

    /**
     * Gets the credential type.
     * @return the credential type.
     */
    public AzureAdTokenCredentialType getCredentialType() {
        return this.credentialType;
    }

    /**
     * Gets the environment.
     * @return the environment.
     */
    public AzureEnvironment getAzureEnvironment() {
        return this.azureEnvironment;
    }

    /**
     * Initializes a new instance of the AzureAdTokenCredentials class.
     * @param tenant The tenant
     * @param azureAdClientUsernamePassword the user credentials
     * @param azureEnvironment The environment
     */
    public AzureAdTokenCredentials(String tenant, AzureAdClientUsernamePassword azureAdClientUsernamePassword, AzureEnvironment azureEnvironment) {
        if (tenant == null || tenant.trim().isEmpty()) {
            throw new IllegalArgumentException("tenant");
        }

        if (azureAdClientUsernamePassword == null) {
            throw new NullPointerException("azureAdClientUsernamePassword");
        }

        if (azureEnvironment == null) {
            throw new NullPointerException("azureEnvironment");
        }

        this.tenant = tenant;
        this.azureAdClientUsernamePassword = azureAdClientUsernamePassword;
        this.azureEnvironment = azureEnvironment;
        this.credentialType = AzureAdTokenCredentialType.UserSecretCredential;
    }

    /**
     * Initializes a new instance of the AzureAdTokenCredentials class.
     * @param tenant The tenant
     * @param azureEnvironment The environment
     */
    public AzureAdTokenCredentials(String tenant, AzureEnvironment azureEnvironment) {
        if (tenant == null || tenant.trim().isEmpty()) {
            throw new IllegalArgumentException("tenant");
        }

        if (azureEnvironment == null) {
            throw new NullPointerException("azureEnvironment");
        }

        this.tenant = tenant;
        this.azureEnvironment = azureEnvironment;
        this.credentialType = AzureAdTokenCredentialType.UserSecretCredential;
    }

    /**
     * Initializes a new instance of the AzureAdTokenCredentials class.
     * @param tenant The tenant
     * @param azureAdClientSymmetricKey an instance of AzureAdClientSymmetricKey
     * @param azureEnvironment The environment
     */
    public AzureAdTokenCredentials(String tenant, AzureAdClientSymmetricKey azureAdClientSymmetricKey, AzureEnvironment azureEnvironment) {
        if (tenant == null || tenant.trim().isEmpty()) {
            throw new IllegalArgumentException("tenant");
        }

        if (azureAdClientSymmetricKey == null) {
            throw new NullPointerException("azureAdClientSymmetricKey");
        }

        if (azureEnvironment == null) {
            throw new NullPointerException("azureEnvironment");
        }

        this.tenant = tenant;
        this.azureEnvironment = azureEnvironment;
        this.clientKey = new ClientCredential(azureAdClientSymmetricKey.getClientId(), azureAdClientSymmetricKey.getClientKey());
        this.credentialType = AzureAdTokenCredentialType.ServicePrincipalWithClientSymmetricKey;
    }


    public AzureAdTokenCredentials(String tenant, AsymmetricKeyCredential asymmetricKeyCredential, AzureEnvironment azureEnvironment) {
        if (tenant == null || tenant.trim().isEmpty()) {
            throw new IllegalArgumentException("tenant");
        }

        if (asymmetricKeyCredential == null) {
            throw new NullPointerException("asymmetricKeyCredential");
        }

        if (azureEnvironment == null) {
            throw new NullPointerException("azureEnvironment");
        }

        this.tenant = tenant;
        this.azureEnvironment = azureEnvironment;
        this.asymmetricKeyCredential = asymmetricKeyCredential;

        this.credentialType = AzureAdTokenCredentialType.ServicePrincipalWithClientCertificate;
    }
}
