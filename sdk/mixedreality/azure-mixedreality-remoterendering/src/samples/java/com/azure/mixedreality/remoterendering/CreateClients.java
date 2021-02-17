// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.mixedreality.remoterendering;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.identity.DeviceCodeCredentialBuilder;
import com.azure.identity.DeviceCodeInfo;
import com.azure.identity.implementation.IdentityClientOptions;

import java.time.OffsetDateTime;

/**
 * Sample class demonstrating the different ways to authenticate and create a client instance.
 * createClientWithAccountKey() is used in all samples.
 */
public class CreateClients {

    private SampleEnvironment environment = new SampleEnvironment();

    /**
     * Obtains a client with an AzureKeyCredential.
     *
     * @return the RemoteRenderingClient.
     */
    public RemoteRenderingClient createClientWithAccountKey()
    {
        RemoteRenderingClientBuilder builder = new RemoteRenderingClientBuilder();
        builder.accountId(environment.getAccountId());
        builder.accountDomain(environment.getAccountDomain());
        builder.credential(new AzureKeyCredential(environment.getAccountKey()));
        builder.endpoint(environment.getServiceEndpoint());
        return builder.buildClient();
    }

    public RemoteRenderingClient createClientWithAAD()
    {
        RemoteRenderingClientBuilder builder = new RemoteRenderingClientBuilder();
        builder.accountId(environment.getAccountId());
        builder.accountDomain(environment.getAccountDomain());
        builder.credential(new ClientSecretCredentialBuilder()
            .tenantId(environment.getTenantId())
            .clientId(environment.getClientId())
            .clientSecret(environment.getClientSecret())
            .authorityHost("https://login.microsoftonline.com/" + environment.getTenantId())
            .build()
        );
        builder.endpoint(environment.getServiceEndpoint());
        return builder.buildClient();
    }

    public RemoteRenderingClient createClientWithDeviceCode()
    {
        RemoteRenderingClientBuilder builder = new RemoteRenderingClientBuilder();
        builder.accountId(environment.getAccountId());
        builder.accountDomain(environment.getAccountDomain());
        builder.endpoint(environment.getServiceEndpoint());

        builder.credential(new DeviceCodeCredentialBuilder()
            .challengeConsumer((DeviceCodeInfo deviceCodeInfo) -> { System.out.println(deviceCodeInfo.getMessage()); })
            .clientId(environment.getClientId())
            .tenantId(environment.getTenantId())
            .authorityHost("https://login.microsoftonline.com/" + environment.getTenantId())
            .build()
        );

        return builder.buildClient();
    }

    public RemoteRenderingClient createClientWithDefaultAzureCredential()
    {
        RemoteRenderingClientBuilder builder = new RemoteRenderingClientBuilder();
        builder.accountId(environment.getAccountId());
        builder.accountDomain(environment.getAccountDomain());
        builder.endpoint(environment.getServiceEndpoint());
        builder.credential(new DefaultAzureCredentialBuilder().build());
        return builder.buildClient();
    }

    /**
     * getMixedRealityAccessTokenFromWebService is a hypothetical method that retrieves
     * a Mixed Reality access token from a web service. The web service would use the
     * MixedRealityStsClient and credentials to obtain an access token to be returned
     * to the client.
     *
     * @return returns the AccessToken.
     */
    private AccessToken getMixedRealityAccessTokenFromWebService()
    {
        return new AccessToken("TokenObtainedFromStsClientRunningInWebservice", OffsetDateTime.MAX);
    }

    public RemoteRenderingClient createClientWithStaticAccessToken()
    {
        RemoteRenderingClientBuilder builder = new RemoteRenderingClientBuilder();
        builder.accountId(environment.getAccountId());
        builder.accountDomain(environment.getAccountDomain());
        builder.endpoint(environment.getServiceEndpoint());
        builder.accessToken(getMixedRealityAccessTokenFromWebService());
        return builder.buildClient();
    }
}
