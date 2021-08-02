package com.azure.identity;

import com.azure.core.util.ExpandableStringEnum;
import com.fasterxml.jackson.annotation.JsonCreator;

public class TokenCredentialType extends ExpandableStringEnum<TokenCredentialType> {

    public static final TokenCredentialType AUTHORIZATION_CODE_CREDENTIAL = fromString("authorizationCodeCredential");

    public static final TokenCredentialType DEVICE_CODE_CREDENTIAL = fromString("deviceCodeCredential");


    public static final TokenCredentialType INTERACTIVE_BROWSER_CREDENTIAL = fromString("interactiveBrowserCredential");


    public static final TokenCredentialType AZURE_POWERSHELL_CREDENTIAL = fromString("azurePowerShellCredential");


    public static final TokenCredentialType AZURE_CLI_CREDENTIAL = fromString("azureCliCredential");


    public static final TokenCredentialType MANAGED_IDENTITY_CREDENTIAL = fromString("managedIdentityCredential");

    public static final TokenCredentialType USERNAME_PASSWORD_CREDENTIAL = fromString("usernamePasswordCredential");

    /**
     * Static value Client Secret Credential for TokenCredentialType.
     */
    public static final TokenCredentialType CLIENT_SECRET_CREDENTIAL = fromString("clientSecretCredential");


    public static final TokenCredentialType CLIENT_CERTIFICATE_CREDENTIAL = fromString("clientCertificateCredential");

    /**
     * Creates or finds a TokenCredentialType from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding KeyOperation.
     */
    @JsonCreator
    public static TokenCredentialType fromString(String name) {
        return fromString(name, TokenCredentialType.class);
    }
}

