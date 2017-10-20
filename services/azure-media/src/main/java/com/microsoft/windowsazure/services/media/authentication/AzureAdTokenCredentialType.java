package com.microsoft.windowsazure.services.media.authentication;

/**
 * Enumerates the different types of credentials available
 *
 */
public enum AzureAdTokenCredentialType {

    /**
     * User Credential by prompting user for user name and password.
     */
    UserCredential,

    /**
     * User Secret Credential by providing user name and password via configuration.
     */
    UserSecretCredential,

    /**
     * Service Principal with the symmetric key credential.
     */
    ServicePrincipalWithClientSymmetricKey,

    /**
     * Service Principal with the certificate credential.
     */
    ServicePrincipalWithClientCertificate
}
