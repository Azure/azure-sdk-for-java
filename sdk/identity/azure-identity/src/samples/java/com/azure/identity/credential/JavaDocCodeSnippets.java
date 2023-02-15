// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.credential;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.ProxyOptions;
import com.azure.core.http.ProxyOptions.Type;
import com.azure.identity.*;

import java.net.InetSocketAddress;

/**
 * This class contains code samples for generating javadocs through doclets for azure-identity.
 */
public final class JavaDocCodeSnippets {

    private String tenantId = System.getenv("AZURE_TENANT_ID");
    private String clientId = System.getenv("AZURE_CLIENT_ID");
    private String clientSecret = System.getenv("AZURE_CLIENT_SECRET");
    private String fakeUsernamePlaceholder = "fakeUsernamePlaceholder";
    private String fakePasswordPlaceholder = "fakePasswordPlaceholder";

    /**
     * Method to insert code snippets for {@link ClientSecretCredential}
     */
    public void clientSecretCredentialCodeSnippets() {
        // BEGIN: com.azure.identity.credential.clientsecretcredential.construct
        TokenCredential clientSecretCredential = new ClientSecretCredentialBuilder()
            .tenantId(tenantId)
            .clientId(clientId)
            .clientSecret(clientSecret)
            .build();
        // END: com.azure.identity.credential.clientsecretcredential.construct

        // BEGIN: com.azure.identity.credential.clientsecretcredential.constructwithproxy
        TokenCredential secretCredential = new ClientSecretCredentialBuilder()
            .tenantId(tenantId)
            .clientId(clientId)
            .clientSecret(clientSecret)
            .proxyOptions(new ProxyOptions(Type.HTTP, new InetSocketAddress("10.21.32.43", 5465)))
            .build();
        // END: com.azure.identity.credential.clientsecretcredential.constructwithproxy
    }

    /**
     * Method to insert code snippets for {@link ClientCertificateCredential}
     */
    public void clientCertificateCredentialCodeSnippets() {
        // BEGIN: com.azure.identity.credential.clientcertificatecredential.construct
        TokenCredential clientCertificateCredential = new ClientCertificateCredentialBuilder()
            .tenantId(tenantId)
            .clientId(clientId)
            .pemCertificate("<PATH-TO-PEM-CERTIFICATE>")
            .build();
        // END: com.azure.identity.credential.clientcertificatecredential.construct

        // BEGIN: com.azure.identity.credential.clientcertificatecredential.constructwithproxy
        TokenCredential certificateCredential = new ClientCertificateCredentialBuilder()
            .tenantId(tenantId)
            .clientId(clientId)
            .pfxCertificate("<PATH-TO-PFX-CERTIFICATE>", "P@s$w0rd")
            .proxyOptions(new ProxyOptions(Type.HTTP, new InetSocketAddress("10.21.32.43", 5465)))
            .build();
        // END: com.azure.identity.credential.clientcertificatecredential.constructwithproxy
    }

    /**
     * Method to insert code snippets for {@link ChainedTokenCredential}
     */
    public void chainedTokenCredentialCodeSnippets() {
        // BEGIN: com.azure.identity.credential.chainedtokencredential.construct
        TokenCredential usernamePasswordCredential = new UsernamePasswordCredentialBuilder()
            .clientId(clientId)
            .username(fakeUsernamePlaceholder)
            .password(fakePasswordPlaceholder)
            .build();
        TokenCredential interactiveBrowserCredential = new InteractiveBrowserCredentialBuilder()
            .clientId(clientId)
            .port(8765)
            .build();
        TokenCredential credential = new ChainedTokenCredentialBuilder()
            .addLast(usernamePasswordCredential)
            .addLast(interactiveBrowserCredential)
            .build();
        // END: com.azure.identity.credential.chainedtokencredential.construct
    }

    /**
     * Method to insert code snippets for {@link DefaultAzureCredential}
     */
    public void defaultAzureCredentialCodeSnippets() {
        // BEGIN: com.azure.identity.credential.defaultazurecredential.construct
        TokenCredential defaultAzureCredential = new DefaultAzureCredentialBuilder()
            .build();
        // END: com.azure.identity.credential.defaultazurecredential.construct

        // BEGIN: com.azure.identity.credential.defaultazurecredential.constructwithuserassignedmanagedidentity
        TokenCredential dacWithUserAssignedManagedIdentity = new DefaultAzureCredentialBuilder()
            .managedIdentityClientId("<Managed-Identity-Client-Id")
            .build();
        // END: com.azure.identity.credential.defaultazurecredential.constructwithuserassignedmanagedidentity
    }

    /**
     * Method to insert code snippets for {@link InteractiveBrowserCredential}
     */
    public void interactiveBrowserCredentialsCodeSnippets() {
        // BEGIN: com.azure.identity.credential.interactivebrowsercredential.construct
        TokenCredential interactiveBrowserCredential = new InteractiveBrowserCredentialBuilder()
            .clientId(clientId)
            .redirectUrl("http://localhost:8765")
            .build();
        // END: com.azure.identity.credential.interactivebrowsercredential.construct
    }

    /**
     * Method to insert code snippets for {@link ManagedIdentityCredential}
     */
    public void managedIdentityCredentialsCodeSnippets() {
        // BEGIN: com.azure.identity.credential.managedidentitycredential.construct
        TokenCredential managedIdentityCredential = new ManagedIdentityCredentialBuilder()
            .clientId(clientId) // specify client id only if targeting a user-assigned managed identity.
            .build();
        // END: com.azure.identity.credential.managedidentitycredential.construct
    }


    /**
     * Method to insert code snippets for {@link AzureCliCredential}
     */
    public void azureCliCredentialsCodeSnippets() {
        // BEGIN: com.azure.identity.credential.azureclicredential.construct
        TokenCredential azureCliCredential = new AzureCliCredentialBuilder()
            .build();
        // END: com.azure.identity.credential.azureclicredential.construct
    }

    /**
     * Method to insert code snippets for {@link IntelliJCredential}
     */
    public void intelliJCredentialsCodeSnippets() {
        // BEGIN: com.azure.identity.credential.intellijcredential.construct
        TokenCredential intelliJCredential = new IntelliJCredentialBuilder()
            .build();
        // END: com.azure.identity.credential.intellijcredential.construct
    }

}
