// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.secrets;

import com.azure.identity.AuthorizationCodeCredential;
import com.azure.identity.AuthorizationCodeCredentialBuilder;
import com.azure.identity.AzureCliCredential;
import com.azure.identity.AzureCliCredentialBuilder;
import com.azure.identity.ChainedTokenCredential;
import com.azure.identity.ChainedTokenCredentialBuilder;
import com.azure.identity.ClientCertificateCredential;
import com.azure.identity.ClientCertificateCredentialBuilder;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.identity.DeviceCodeCredential;
import com.azure.identity.DeviceCodeCredentialBuilder;
import com.azure.identity.IntelliJCredential;
import com.azure.identity.IntelliJCredentialBuilder;
import com.azure.identity.InteractiveBrowserCredential;
import com.azure.identity.InteractiveBrowserCredentialBuilder;
import com.azure.identity.ManagedIdentityCredential;
import com.azure.identity.ManagedIdentityCredentialBuilder;
import com.azure.identity.UsernamePasswordCredential;
import com.azure.identity.UsernamePasswordCredentialBuilder;
import com.azure.identity.VisualStudioCodeCredential;
import com.azure.identity.VisualStudioCodeCredentialBuilder;

/**
 * WARNING: MODIFYING THIS FILE WILL REQUIRE CORRESPONDING UPDATES TO README.md FILE. LINE NUMBERS
 * ARE USED TO EXTRACT APPROPRIATE CODE SEGMENTS FROM THIS FILE. ADD NEW CODE AT THE BOTTOM TO AVOID CHANGING
 * LINE NUMBERS OF EXISTING CODE SAMPLES.
 *
 * Code snippet for creating various credentials that will be used in README.md.
 */
public class IdentityReadmeSamples {

    /**
     * The default credential first checks environment variables for configuration.
     * If environment configuration is incomplete, it will try managed identity.
     */
    public void createDefaultAzureCredential() {
        DefaultAzureCredential defaultCredential = new DefaultAzureCredentialBuilder().build();

        // Azure SDK client builders accept the credential as a parameter
        SecretClient client = new SecretClientBuilder()
            .vaultUrl("https://{YOUR_VAULT_NAME}.vault.azure.net")
            .credential(defaultCredential)
            .buildClient();
    }

    /**
     *  Authenticate with a client secret.
     */
    public void createClientSecretCredential() {
        ClientSecretCredential clientSecretCredential = new ClientSecretCredentialBuilder()
            .clientId("<YOUR_CLIENT_ID>")
            .clientSecret("<YOUR_CLIENT_SECRET>")
            .tenantId("<YOUR_TENANT_ID>")
            .build();

        // Azure SDK client builders accept the credential as a parameter
        SecretClient client = new SecretClientBuilder()
            .vaultUrl("https://{YOUR_VAULT_NAME}.vault.azure.net")
            .credential(clientSecretCredential)
            .buildClient();
    }

    /**
     *  Authenticate with a client certificate.
     */
    public void createClientCertificateCredential() {
        ClientCertificateCredential clientCertificateCredential = new ClientCertificateCredentialBuilder()
            .clientId("<YOUR_CLIENT_ID>")
            .pemCertificate("<PATH TO PEM CERTIFICATE>")
            // choose between either a PEM certificate or a PFX certificate
            //.pfxCertificate("<PATH TO PFX CERTIFICATE>", "PFX CERTIFICATE PASSWORD")
            .tenantId("<YOUR_TENANT_ID>")
            .build();

        // Azure SDK client builders accept the credential as a parameter
        SecretClient client = new SecretClientBuilder()
            .vaultUrl("https://{YOUR_VAULT_NAME}.vault.azure.net")
            .credential(clientCertificateCredential)
            .buildClient();
    }

    /**
     * Authenticate with device code credential.
     */
    public void createDeviceCodeCredential() {
        DeviceCodeCredential deviceCodeCredential = new DeviceCodeCredentialBuilder()
            .challengeConsumer(challenge -> {
                // lets user know of the challenge
                System.out.println(challenge.getMessage());
            })
            .build();

        // Azure SDK client builders accept the credential as a parameter
        SecretClient client = new SecretClientBuilder()
            .vaultUrl("https://{YOUR_VAULT_NAME}.vault.azure.net")
            .credential(deviceCodeCredential)
            .buildClient();
    }

    /**
     * Authenticate with username, password.
     */
    public void createUserNamePasswordCredential() {
        UsernamePasswordCredential usernamePasswordCredential = new UsernamePasswordCredentialBuilder()
            .clientId("<YOUR_CLIENT_ID>")
            .username("<YOUR_USERNAME>")
            .password("<YOUR_PASSWORD>")
            .build();

        // Azure SDK client builders accept the credential as a parameter
        SecretClient client = new SecretClientBuilder()
            .vaultUrl("https://{YOUR_VAULT_NAME}.vault.azure.net")
            .credential(usernamePasswordCredential)
            .buildClient();
    }

    /**
     * Authenticate interactively in the browser.
     */
    public void createInteractiveBrowserCredential() {
        InteractiveBrowserCredential interactiveBrowserCredential = new InteractiveBrowserCredentialBuilder()
            .clientId("<YOUR CLIENT ID>")
            .port(8765)
            .build();

        // Azure SDK client builders accept the credential as a parameter
        SecretClient client = new SecretClientBuilder()
            .vaultUrl("https://{YOUR_VAULT_NAME}.vault.azure.net")
            .credential(interactiveBrowserCredential)
            .buildClient();
    }

    /**
     * Authenticate with authorization code.
     */
    public void createAuthCodeCredential() {
        AuthorizationCodeCredential authCodeCredential = new AuthorizationCodeCredentialBuilder()
            .clientId("<YOUR CLIENT ID>")
            .authorizationCode("<AUTH CODE FROM QUERY PARAMETERS")
            .redirectUrl("<THE REDIRECT URL>")
            .build();
        // Azure SDK client builders accept the credential as a parameter
        SecretClient client = new SecretClientBuilder()
            .vaultUrl("https://{YOUR_VAULT_NAME}.vault.azure.net")
            .credential(authCodeCredential)
            .buildClient();
    }

    /**
     * Authenticate with Azure CLI.
     */
    public void createAzureCliCredential() {
        AzureCliCredential cliCredential = new AzureCliCredentialBuilder().build();

        // Azure SDK client builders accept the credential as a parameter
        SecretClient client = new SecretClientBuilder()
            .vaultUrl("https://{YOUR_VAULT_NAME}.vault.azure.net")
            .credential(cliCredential)
            .buildClient();
    }

    /**
     * Authenticate with IntelliJ IDEA.
     */
    public void createIntelliJCredential() {
        IntelliJCredential intelliJCredential = new IntelliJCredentialBuilder()
            // KeePass configuration required only for Windows. No configuration needed for Linux / Mac
            .keePassDatabasePath("C:\\Users\\user\\AppData\\Roaming\\JetBrains\\IdeaIC2020.1\\c.kdbx")
            .build();

        // Azure SDK client builders accept the credential as a parameter
        SecretClient client = new SecretClientBuilder()
            .vaultUrl("https://{YOUR_VAULT_NAME}.vault.azure.net")
            .credential(intelliJCredential)
            .buildClient();
    }

    /**
     * Authenticate with Visual Studio Code.
     */
    public void createVisualStudioCodeCredential() {
        VisualStudioCodeCredential visualStudioCodeCredential = new VisualStudioCodeCredentialBuilder().build();

        // Azure SDK client builders accept the credential as a parameter
        SecretClient client = new SecretClientBuilder()
            .vaultUrl("https://{YOUR_VAULT_NAME}.vault.azure.net")
            .credential(visualStudioCodeCredential)
            .buildClient();
    }

    /**
     * Authenticate with a managed identity.
     */
    public void createManagedIdentityCredential() {
        ManagedIdentityCredential managedIdentityCredential = new ManagedIdentityCredentialBuilder()
            .clientId("<USER ASSIGNED MANAGED IDENTITY CLIENT ID>") // only required for user assigned
            .build();

        // Azure SDK client builders accept the credential as a parameter
        SecretClient client = new SecretClientBuilder()
            .vaultUrl("https://{YOUR_VAULT_NAME}.vault.azure.net")
            .credential(managedIdentityCredential)
            .buildClient();
    }

    /**
     * Authenticate with chained credentials.
     */
    public void createChainedCredential() {
        ManagedIdentityCredential managedIdentityCredential = new ManagedIdentityCredentialBuilder()
            .clientId("<YOUR_CLIENT_ID>")
            .build();

        ClientSecretCredential secondServicePrincipal = new ClientSecretCredentialBuilder()
            .clientId("<YOUR_CLIENT_ID>")
            .clientSecret("<YOUR_CLIENT_SECRET>")
            .tenantId("<YOUR_TENANT_ID>")
            .build();

        // when an access token is requested, the chain will try each
        // credential in order, stopping when one provides a token
        ChainedTokenCredential credentialChain = new ChainedTokenCredentialBuilder()
            .addLast(managedIdentityCredential)
            .addLast(secondServicePrincipal)
            .build();

        // Azure SDK client builders accept the credential as a parameter
        SecretClient client = new SecretClientBuilder()
            .vaultUrl("https://{YOUR_VAULT_NAME}.vault.azure.net")
            .credential(credentialChain)
            .buildClient();
    }

    /**
     * The default credential will use the user assigned managed identity with the specified client ID.
     */
    public void createDefaultAzureCredentialForUserAssignedManagedIdentity() {
        DefaultAzureCredential defaultCredential = new DefaultAzureCredentialBuilder()
            .managedIdentityClientId("<MANAGED_IDENTITY_CLIENT_ID>")
            .build();

        // Azure SDK client builders accept the credential as a parameter
        SecretClient client = new SecretClientBuilder()
            .vaultUrl("https://{YOUR_VAULT_NAME}.vault.azure.net")
            .credential(defaultCredential)
            .buildClient();
    }

    /**
     * The default credential will use the KeePass database path to find the user account in IntelliJ on Windows.
     */
    public void createDefaultAzureCredentialForIntelliJ() {
        DefaultAzureCredential defaultCredential = new DefaultAzureCredentialBuilder()
            // KeePass configuration required only for Windows. No configuration needed for Linux / Mac
            .intelliJKeePassDatabasePath("C:\\Users\\user\\AppData\\Roaming\\JetBrains\\IdeaIC2020.1\\c.kdbx")
            .build();

        // Azure SDK client builders accept the credential as a parameter
        SecretClient client = new SecretClientBuilder()
            .vaultUrl("https://{YOUR_VAULT_NAME}.vault.azure.net")
            .credential(defaultCredential)
            .buildClient();
    }
}
