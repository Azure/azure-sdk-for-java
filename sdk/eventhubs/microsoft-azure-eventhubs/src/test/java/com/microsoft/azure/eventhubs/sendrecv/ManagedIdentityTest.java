// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs.sendrecv;

import java.net.MalformedURLException;
import java.util.concurrent.ExecutionException;

//import org.junit.Test;

import com.microsoft.azure.eventhubs.ConnectionStringBuilder;
import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.ManagedIdentityTokenProvider;
import com.microsoft.azure.eventhubs.lib.TestContext;

/**
 * These JUnit test cases are all commented out by default because they can only be run with special setup. 
 * They assume that they are running in a VM which has been set up with a managed identity, and that the namespace
 * (or event hub) has been set up with that managed identity granted the "Azure Event Hubs Data Owner" role on the
 * namespace or event hub.
 */
public class ManagedIdentityTest extends AadBase {
    //@Test
    public void runSendReceiveWithMITokenProvider() throws Exception {
        final ManagedIdentityTokenProvider aadTokenProvider = new ManagedIdentityTokenProvider();
        final EventHubClient ehc = EventHubClient.createWithTokenProvider(ManagedIdentityTest.endpoint,
                ManagedIdentityTest.eventHubName, aadTokenProvider,
                this.executorService, null).get();
        
        innerTest(ehc);
    }

    //@Test
    public void runSendReceiveWithMIConnectionString() throws Exception {
        final ConnectionStringBuilder csb = TestContext.getConnectionString();
        // Remove SAS info and replace with "Authentication=Managed Identity"
        csb.setSasKey(null);
        csb.setSasKeyName(null);
        csb.setAuthentication(ConnectionStringBuilder.MANAGED_IDENTITY_AUTHENTICATION);
        final EventHubClient ehc = EventHubClient.createFromConnectionString(csb.toString(), this.executorService).get();

        innerTest(ehc);
    }
    
    @Override
    String tokenGet(String authority, String clientId, String clientSecret, String audience, String extra)
            throws MalformedURLException, InterruptedException, ExecutionException {
        // Not used for these cases but required by AadBase
        return null;
    }
}
