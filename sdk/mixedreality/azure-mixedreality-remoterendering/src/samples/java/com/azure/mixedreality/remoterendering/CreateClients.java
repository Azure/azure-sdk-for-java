// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.mixedreality.remoterendering;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.identity.DeviceCodeCredential;
import com.azure.identity.DeviceCodeCredentialBuilder;
import com.azure.identity.DeviceCodeInfo;

import java.time.OffsetDateTime;

/**
 * Sample class demonstrating the different ways to authenticate and create a client instance.
 * createClientWithAccountKey() is used in all samples.
 */
public class CreateClients {

    final ClientLogger logger = new ClientLogger(CreateClients.class);

    private SampleEnvironment environment = new SampleEnvironment();

    /**
     * Obtains a client with an AzureKeyCredential.
     *
     * @return the RemoteRenderingClient.
     */
    public RemoteRenderingClient createClientWithAccountKey()
    {
        return new RemoteRenderingClientBuilder()
            .accountId(environment.getAccountId())
            .accountDomain(environment.getAccountDomain())
            .endpoint(environment.getServiceEndpoint())
            .credential(new AzureKeyCredential(environment.getAccountKey()))
            .buildClient();
    }

    public RemoteRenderingClient createClientWithAAD()
    {
        ClientSecretCredential credential = new ClientSecretCredentialBuilder()
            .tenantId(environment.getTenantId())
            .clientId(environment.getClientId())
            .clientSecret(environment.getClientSecret())
            .authorityHost("https://login.microsoftonline.com/" + environment.getTenantId())
            .build();

        return new RemoteRenderingClientBuilder()
            .accountId(environment.getAccountId())
            .accountDomain(environment.getAccountDomain())
            .endpoint(environment.getServiceEndpoint())
            .credential(credential)
            .buildClient();
    }

    public RemoteRenderingClient createClientWithDeviceCode()
    {
        DeviceCodeCredential credential = new DeviceCodeCredentialBuilder()
            .challengeConsumer((DeviceCodeInfo deviceCodeInfo) -> { logger.info(deviceCodeInfo.getMessage()); })
            .clientId(environment.getClientId())
            .tenantId(environment.getTenantId())
            .authorityHost("https://login.microsoftonline.com/" + environment.getTenantId())
            .build();

        return new RemoteRenderingClientBuilder()
            .accountId(environment.getAccountId())
            .accountDomain(environment.getAccountDomain())
            .endpoint(environment.getServiceEndpoint())
            .credential(credential)
            .buildClient();
    }

    public RemoteRenderingClient createClientWithDefaultAzureCredential()
    {
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();

        return new RemoteRenderingClientBuilder()
            .accountId(environment.getAccountId())
            .accountDomain(environment.getAccountDomain())
            .endpoint(environment.getServiceEndpoint())
            .credential(credential)
            .buildClient();
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
        // GetMixedRealityAccessTokenFromWebService is a hypothetical method that retrieves
        // a Mixed Reality access token from a web service. The web service would use the
        // MixedRealityStsClient and credentials to obtain an access token to be returned
        // to the client.
        AccessToken accessToken = getMixedRealityAccessTokenFromWebService();

        return new RemoteRenderingClientBuilder()
            .accountId(environment.getAccountId())
            .accountDomain(environment.getAccountDomain())
            .endpoint(environment.getServiceEndpoint())
            .accessToken(accessToken)
            .buildClient();
    }
}
