// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.credential;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.http.ProxyOptions;
import com.azure.core.http.ProxyOptions.Type;

import com.azure.identity.AuthenticationRecord;
import com.azure.identity.AzureCliCredential;
import com.azure.identity.AzureCliCredentialBuilder;
import com.azure.identity.AzureDeveloperCliCredential;
import com.azure.identity.AzureDeveloperCliCredentialBuilder;
import com.azure.identity.AuthorizationCodeCredential;
import com.azure.identity.AuthorizationCodeCredentialBuilder;
import com.azure.identity.AzurePipelinesCredential;
import com.azure.identity.AzurePipelinesCredentialBuilder;
import com.azure.identity.AzurePowerShellCredential;
import com.azure.identity.AzurePowerShellCredentialBuilder;
import com.azure.identity.ClientAssertionCredential;
import com.azure.identity.ClientAssertionCredentialBuilder;
import com.azure.identity.ClientCertificateCredential;
import com.azure.identity.ClientCertificateCredentialBuilder;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.identity.ChainedTokenCredential;
import com.azure.identity.ChainedTokenCredentialBuilder;
import com.azure.identity.DeviceCodeCredential;
import com.azure.identity.DeviceCodeCredentialBuilder;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.identity.EnvironmentCredential;
import com.azure.identity.EnvironmentCredentialBuilder;
import com.azure.identity.InteractiveBrowserCredential;
import com.azure.identity.InteractiveBrowserCredentialBuilder;
import com.azure.identity.IntelliJCredential;
import com.azure.identity.IntelliJCredentialBuilder;
import com.azure.identity.ManagedIdentityCredential;
import com.azure.identity.ManagedIdentityCredentialBuilder;
import com.azure.identity.OnBehalfOfCredential;
import com.azure.identity.OnBehalfOfCredentialBuilder;
import com.azure.identity.UsernamePasswordCredential;
import com.azure.identity.UsernamePasswordCredentialBuilder;
import com.azure.identity.WorkloadIdentityCredential;
import com.azure.identity.WorkloadIdentityCredentialBuilder;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;

/**
    * This class contains code samples for generating javadocs through doclets for azure-identity.
    */
public final class JavaDocCodeSnippets {
    private String tenantId = System.getenv("AZURE_TENANT_ID");

    private String clientId = System.getenv("AZURE_CLIENT_ID");
    private String clientSecret = System.getenv("AZURE_CLIENT_SECRET");
    private String serviceConnectionId = System.getenv("SERVICE_CONNECTION_ID");


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

        byte[] certificateBytes = new byte[0];

        // BEGIN: com.azure.identity.credential.clientcertificatecredential.constructWithStream
        ByteArrayInputStream certificateStream = new ByteArrayInputStream(certificateBytes);
        TokenCredential certificateCredentialWithStream = new ClientCertificateCredentialBuilder()
            .tenantId(tenantId)
            .clientId(clientId)
            .pemCertificate(certificateStream)
            .build();
        // END: com.azure.identity.credential.clientcertificatecredential.constructWithStream

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
     * Method to insert code snippets for {@link ClientAssertionCredential}
     */
    public void clientAssertionCredentialCodeSnippets() {
        // BEGIN: com.azure.identity.credential.clientassertioncredential.construct
        TokenCredential clientAssertionCredential = new ClientAssertionCredentialBuilder()
            .tenantId(tenantId)
            .clientId(clientId)
            .clientAssertion(() -> "<Client-Assertion>")
            .build();
        // END: com.azure.identity.credential.clientassertioncredential.construct

        // BEGIN: com.azure.identity.credential.clientassertioncredential.constructwithproxy
        TokenCredential assertionCredential = new ClientAssertionCredentialBuilder()
            .tenantId(tenantId)
            .clientId(clientId)
            .clientAssertion(() -> "<Client-Assertion>")
            .proxyOptions(new ProxyOptions(Type.HTTP, new InetSocketAddress("10.21.32.43", 5465)))
            .build();
        // END: com.azure.identity.credential.clientassertioncredential.constructwithproxy
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
            .redirectUrl("http://localhost:8765")
            .build();
        // END: com.azure.identity.credential.interactivebrowsercredential.construct
    }

    /**
     * Method to insert code snippets for {@link ManagedIdentityCredential}
     */
    public void managedIdentityCredentialsCodeSnippets() {
        // BEGIN: com.azure.identity.credential.managedidentitycredential.userassigned.construct
        TokenCredential managedIdentityCredentialUserAssigned = new ManagedIdentityCredentialBuilder()
            .clientId(clientId) // specify client id of user-assigned managed identity.
            .build();
        // END: com.azure.identity.credential.managedidentitycredential.userassigned.construct

        // BEGIN: com.azure.identity.credential.managedidentitycredential.construct
        TokenCredential managedIdentityCredential = new ManagedIdentityCredentialBuilder()
            .build();
        // END: com.azure.identity.credential.managedidentitycredential.construct
    }

    /**
     * Method to insert code snippets for {@link EnvironmentCredential}
     */
    public void environmentCredentialsCodeSnippets() {
        // BEGIN: com.azure.identity.credential.environmentcredential.construct
        TokenCredential environmentCredential = new EnvironmentCredentialBuilder()
            .build();
        // END: com.azure.identity.credential.environmentcredential.construct
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

    /**
     * Method to insert code snippets for {@link DeviceCodeCredential}
     */
    public void deviceCodeCredentialsCodeSnippets() {
        // BEGIN: com.azure.identity.credential.devicecodecredential.construct
        TokenCredential deviceCodeCredential = new DeviceCodeCredentialBuilder()
            .build();
        // END: com.azure.identity.credential.devicecodecredential.construct
    }

    /**
     * Method to insert code snippets for {@link UsernamePasswordCredential}
     */
    public void usernamePasswordCredentialsCodeSnippets() {
        // BEGIN: com.azure.identity.credential.usernamepasswordcredential.construct
        TokenCredential usernamePasswordCredential = new UsernamePasswordCredentialBuilder()
            .clientId("<your app client ID>")
            .username("<your username>")
            .password("<your password>")
            .build();
        // END: com.azure.identity.credential.usernamepasswordcredential.construct
    }

    /**
     * Method to insert code snippets for {@link AzurePowerShellCredential}
     */
    public void azurePowershellCredentialsCodeSnippets() {
        // BEGIN: com.azure.identity.credential.azurepowershellcredential.construct
        TokenCredential powerShellCredential = new AzurePowerShellCredentialBuilder()
            .build();
        // END: com.azure.identity.credential.azurepowershellcredential.construct
    }

    /**
     * Method to insert code snippets for {@link AuthorizationCodeCredential}
     */
    public void authorizationCodeCredentialsCodeSnippets() {
        // BEGIN: com.azure.identity.credential.authorizationcodecredential.construct
        TokenCredential authorizationCodeCredential = new AuthorizationCodeCredentialBuilder()
            .authorizationCode("{authorization-code-received-at-redirectURL}")
            .redirectUrl("{redirectUrl-where-authorization-code-is-received}")
            .clientId("{clientId-of-application-being-authenticated")
            .build();
        // END: com.azure.identity.credential.authorizationcodecredential.construct
    }

    /**
     * Method to insert code snippets for {@link OnBehalfOfCredential}
     */
    public void oboCredentialsCodeSnippets() {
        // BEGIN: com.azure.identity.credential.obocredential.construct
        TokenCredential onBehalfOfCredential = new OnBehalfOfCredentialBuilder()
            .clientId("<app-client-ID>")
            .clientSecret("<app-Client-Secret>")
            .tenantId("<app-tenant-ID>")
            .userAssertion("<user-assertion>")
            .build();
        // END: com.azure.identity.credential.obocredential.construct
    }

    /**
     * Method to insert code snippets for {@link WorkloadIdentityCredential}
     */
    public void workloadIdentityCredentialCodeSnippets() {
        // BEGIN: com.azure.identity.credential.workloadidentitycredential.construct
        TokenCredential workloadIdentityCredential = new WorkloadIdentityCredentialBuilder()
            .clientId("<clientID>")
            .tenantId("<tenantID>")
            .tokenFilePath("<token-file-path>")
            .build();
        // END: com.azure.identity.credential.workloadidentitycredential.construct
    }

    /**
     * Method to insert code snippets for {@link AzureDeveloperCliCredential}
     */
    public void azureDeveloperCliCredentialCodeSnippets() {
        // BEGIN: com.azure.identity.credential.azuredeveloperclicredential.construct
        TokenCredential azureDevCliCredential = new AzureDeveloperCliCredentialBuilder()
            .build();
        // END: com.azure.identity.credential.azuredeveloperclicredential.construct
    }

    public void azurePipelinesCredentialCodeSnippets() {

        // BEGIN: com.azure.identity.credential.azurepipelinescredential.construct
        // serviceConnectionId is retrieved from the portal.
        // systemAccessToken is retrieved from the pipeline environment as shown.
        // You may choose another name for this variable.

        String systemAccessToken = System.getenv("SYSTEM_ACCESSTOKEN");
        AzurePipelinesCredential credential = new AzurePipelinesCredentialBuilder()
            .clientId(clientId)
            .tenantId(tenantId)
            .serviceConnectionId(serviceConnectionId)
            .systemAccessToken(systemAccessToken)
            .build();
        // END: com.azure.identity.credential.azurepipelinescredential.construct
    }

    public void silentAuthenticationSnippets() {
        // BEGIN: com.azure.identity.silentauthentication
        String authenticationRecordPath = "path/to/authentication-record.json";
        AuthenticationRecord authenticationRecord = null;
        try {
            // If we have an existing record, deserialize it.
            if (Files.exists(new File(authenticationRecordPath).toPath())) {
                 authenticationRecord = AuthenticationRecord.deserialize(new FileInputStream(authenticationRecordPath));
            }
        } catch (FileNotFoundException e) {
            // Handle error as appropriate.
        }

        DeviceCodeCredentialBuilder builder = new DeviceCodeCredentialBuilder()
            .clientId(clientId)
            .tenantId(tenantId);
        if (authenticationRecord != null) {
            // As we have a record, configure the builder to use it.
            builder.authenticationRecord(authenticationRecord);
        }
        DeviceCodeCredential credential = builder.build();
        TokenRequestContext trc = new TokenRequestContext().addScopes("your-appropriate-scope");
        if (authenticationRecord == null) {
            // We don't have a record, so we get one and store it. The next authentication will use it.
            credential.authenticate(trc).flatMap(record -> {
                try {
                    return record.serializeAsync(new FileOutputStream(authenticationRecordPath));
                } catch (FileNotFoundException e) {
                    return Mono.error(e);
                }
            }).subscribe();
        }

        // Now the credential can be passed to another service client or used directly.
        AccessToken token = credential.getTokenSync(trc);

        // END: com.azure.identity.silentauthentication
    }
}
