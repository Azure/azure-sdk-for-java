// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs.sendrecv;

import com.microsoft.aad.msal4j.ClientCredentialParameters;
import com.microsoft.aad.msal4j.ClientSecret;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.azure.eventhubs.AzureActiveDirectoryTokenProvider;
import com.microsoft.azure.eventhubs.EventHubClient;

import java.net.MalformedURLException;
import java.util.Collections;
import java.util.concurrent.ExecutionException;

//import org.junit.Test;

/**
 * These JUnit test cases are all commented out by default because they can only be run with special setup. 
 * They extract the namespace (endpoint) and event hub name from the connection string in the environment variable
 * which all test cases use, but they assume that the namespace (or event hub) has been set up with special permissions.
 * Within the AAD directory indicated by "authority", there is a registered application with id "clientId" and a secret
 * "clientSecret". This application has been granted the "Azure Event Hubs Data Owner" role on the namespace or
 * event hub.
 */
public class MsalTest extends AadBase {
    private final String authority = "https://login.windows.net/replaceWithTenantIdGuid";
    private final String clientId = "replaceWithClientIdGuid";
    private final String clientSecret = "replaceWithClientSecret";
    
    //@Test
    public void runSendReceiveWithAuthCallbackTest() throws Exception {
        final AuthCallback callback = new AuthCallback(this.clientId, this.clientSecret);
        final EventHubClient ehc = EventHubClient.createWithAzureActiveDirectory(MsalTest.endpoint, MsalTest.eventHubName,
                callback, this.authority, this.executorService, null).get();

        innerTest(ehc);
    }
    
    //@Test
    public void runSendReceiveWithAADTokenProvider() throws Exception {
        final AuthCallback callback = new AuthCallback(this.clientId, this.clientSecret);
        final AzureActiveDirectoryTokenProvider aadTokenProvider =
                new AzureActiveDirectoryTokenProvider(callback, this.authority, null);
        final EventHubClient ehc = EventHubClient.createWithTokenProvider(MsalTest.endpoint, MsalTest.eventHubName, aadTokenProvider,
                this.executorService, null).get();
        
        innerTest(ehc);
    }
    
    //@Test
    public void runSendReceiveWithCustomTokenProvider() throws Exception {
        final CustomTokenProvider tokenProvider = new CustomTokenProvider(this.authority, this.clientId, this.clientSecret);
        final EventHubClient ehc = EventHubClient.createWithTokenProvider(MsalTest.endpoint, MsalTest.eventHubName, tokenProvider,
                this.executorService, null).get();
        
        innerTest(ehc);
    }
    
    @Override
    String tokenGet(final String authority, final String clientId, final String clientSecret, final String audience, final String extra)
            throws MalformedURLException, InterruptedException, ExecutionException {
        ConfidentialClientApplication app = ConfidentialClientApplication.builder(clientId, new ClientSecret(clientSecret))
                .authority(authority)
                .build();
        
        ClientCredentialParameters parameters = ClientCredentialParameters.builder(Collections.singleton(audience + extra)).build();

        IAuthenticationResult result = app.acquireToken(parameters).get();

        return result.accessToken();
    }
}
