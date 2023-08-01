// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs.sendrecv;

import java.net.MalformedURLException;
import java.util.concurrent.ExecutionException;

//import org.junit.Test;

import com.microsoft.aad.adal4j.AuthenticationContext;
import com.microsoft.aad.adal4j.AuthenticationResult;
import com.microsoft.aad.adal4j.ClientCredential;
import com.microsoft.azure.eventhubs.AzureActiveDirectoryTokenProvider;
import com.microsoft.azure.eventhubs.EventHubClient;

/**
 * These JUnit test cases are all commented out by default because they can only be run with special setup. 
 * They extract the namespace (endpoint) and event hub name from the connection string in the environment variable
 * which all test cases use, but they assume that the namespace (or event hub) has been set up with special permissions.
 * Within the AAD directory indicated by "authority", there is a registered application with id "clientId" and a secret
 * "clientSecret". This application has been granted the "Azure Event Hubs Data Owner" role on the namespace or
 * event hub.
 */
public class AdalTest extends AadBase {
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
        AuthenticationContext context = new AuthenticationContext(authority, true, this.executorService);
        ClientCredential creds = new ClientCredential(clientId, clientSecret);
        AuthenticationResult result = context.acquireToken(audience, creds, null).get();
        return result.getAccessToken();
    }
}
