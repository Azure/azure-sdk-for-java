// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.credential;

import com.azure.core.http.ProxyOptions;
import com.azure.core.http.ProxyOptions.Type;
import com.azure.identity.ChainedTokenCredential;
import com.azure.identity.ChainedTokenCredentialBuilder;
import com.azure.identity.ClientCertificateCredential;
import com.azure.identity.ClientCertificateCredentialBuilder;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.identity.InteractiveBrowserCredential;
import com.azure.identity.InteractiveBrowserCredentialBuilder;
import com.azure.identity.UsernamePasswordCredential;
import com.azure.identity.UsernamePasswordCredentialBuilder;

import java.net.InetSocketAddress;

/**
 * This class contains code samples for generating javadocs through doclets for azure-identity.
 */
public final class JavaDocCodeSnippets {

    private String tenantId = System.getenv("AZURE_TENANT_ID");
    private String clientId = System.getenv("AZURE_CLIENT_ID");
    private String clientSecret = System.getenv("AZURE_CLIENT_SECRET");
    private String username = "sampleuser";
    private String password = "Samp1eP@ssw0rd";

    /**
     * Method to insert code snippets for {@link ClientSecretCredential}
     */
    public void clientSecretCredentialCodeSnippets() {
        // BEGIN: com.azure.identity.credential.clientsecretcredential.construct
        ClientSecretCredential credential1 = new ClientSecretCredentialBuilder()
            .tenantId(tenantId)
            .clientId(clientId)
            .clientSecret(clientSecret)
            .build();
        // END: com.azure.identity.credential.clientsecretcredential.construct

        // BEGIN: com.azure.identity.credential.clientsecretcredential.constructwithproxy
        ClientSecretCredential credential2 = new ClientSecretCredentialBuilder()
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
        ClientCertificateCredential credential1 = new ClientCertificateCredentialBuilder()
            .tenantId(tenantId)
            .clientId(clientId)
            .pemCertificate("<PATH-TO-PEM-CERTIFICATE>")
            .build();
        // END: com.azure.identity.credential.clientcertificatecredential.construct

        // BEGIN: com.azure.identity.credential.clientcertificatecredential.constructwithproxy
        ClientCertificateCredential credential2 = new ClientCertificateCredentialBuilder()
            .tenantId(tenantId)
            .clientId(clientId)
            .pfxCertificate("<PATH-TO-PFX-CERTIFICATE>", "P@s$w0rd")
            .proxyOptions(new ProxyOptions(Type.HTTP, new InetSocketAddress("10.21.32.43", 5465)))
            .build();
        // END: com.azure.identity.credential.clientcertificatecredential.constructwithproxy
    }

    /**
     * Method to insert code snippets for {@link ClientCertificateCredential}
     */
    public void chainedTokenCredentialCodeSnippets() {
        // BEGIN: com.azure.identity.credential.chainedtokencredential.construct
        UsernamePasswordCredential usernamePasswordCredential = new UsernamePasswordCredentialBuilder()
            .clientId(clientId)
            .username(username)
            .password(password)
            .build();
        InteractiveBrowserCredential interactiveBrowserCredential = new InteractiveBrowserCredentialBuilder()
            .clientId(clientId)
            .port(8765)
            .build();
        ChainedTokenCredential credential = new ChainedTokenCredentialBuilder()
            .addLast(usernamePasswordCredential)
            .addLast(interactiveBrowserCredential)
            .build();
        // END: com.azure.identity.credential.chainedtokencredential.construct
    }
}
