// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs.sendrecv;

import java.net.MalformedURLException;
import java.net.URI;
import java.text.ParseException;
import java.time.Duration;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;

import org.junit.Assert;
import org.junit.BeforeClass;

import com.microsoft.azure.eventhubs.AzureActiveDirectoryTokenProvider;
import com.microsoft.azure.eventhubs.ConnectionStringBuilder;
import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.EventHubException;
import com.microsoft.azure.eventhubs.EventHubRuntimeInformation;
import com.microsoft.azure.eventhubs.ITokenProvider;
import com.microsoft.azure.eventhubs.JsonSecurityToken;
import com.microsoft.azure.eventhubs.PartitionReceiver;
import com.microsoft.azure.eventhubs.PartitionSender;
import com.microsoft.azure.eventhubs.SecurityToken;
import com.microsoft.azure.eventhubs.impl.ClientConstants;
import com.microsoft.azure.eventhubs.impl.EventPositionImpl;
import com.microsoft.azure.eventhubs.lib.ApiTestBase;
import com.microsoft.azure.eventhubs.lib.TestContext;

/**
 * Because of the way JUnit is structured, the individual test classes for AAD libraries have to implement
 * the actual @Test methods. Each test method tests a different way of creating an EventHubClient, then calls
 * innerTest() to perform a common set of data-plane operations which the token is expected to allow. 
 * 
 * Each test class implements tokenGet() to do the actual token-getting via that library's APIs. All the layers
 * above that (callback, ITokenProvider, etc.) are just different plumbing to get the same token to where it
 * needs to go, so those parts are implemented here in the common base class.
 * 
 * decodeToken() provides a convenient way to get the token contents as a printable string, allowing the tester
 * to verify token contents if things are not working as expected. 
 */
public abstract class AadBase extends ApiTestBase {
    protected static URI endpoint;
    protected static String eventHubName;
    
    protected final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(8);

    @BeforeClass
    public static void initializeEventHub() throws Exception {
        ConnectionStringBuilder csb = TestContext.getConnectionString();
        AadBase.endpoint = csb.getEndpoint();
        AadBase.eventHubName = csb.getEventHubName();
    }

    protected void innerTest(final EventHubClient ehc) throws EventHubException, InterruptedException, ExecutionException {
        // Try some runtime operations. We don't care about the result, just that it doesn't throw.
        EventHubRuntimeInformation ehri = ehc.getRuntimeInformation().get();
        ehc.getPartitionRuntimeInformation(ehri.getPartitionIds()[ehri.getPartitionCount() - 1]).get();
        
        // Open a receiver
        final PartitionReceiver pReceiver = ehc.createReceiverSync("$Default", "0", EventPositionImpl.fromEndOfStream());

        // Open a sender and do a send.
        final PartitionSender pSender = ehc.createPartitionSenderSync("0");
        final String testMessage = "somedata test";
        pSender.send(EventData.create(testMessage.getBytes()));

        // Do some receives.
        int scanned = 0;
        boolean found = false;
        while ((scanned < 10000) && !found) {
            System.out.println("Scanned " + scanned);
            final Iterable<EventData> events = pReceiver.receiveSync(100);
            for (EventData ed : events) {
                scanned++;
                if ((new String(ed.getBytes())).equals(testMessage)) {
                    found = true;
                    break;
                }
            }
        }

        // Close everything.
        pSender.closeSync();
        pReceiver.closeSync();
        ehc.closeSync();
        
        Assert.assertTrue(found);
    }

    abstract String tokenGet(String authority, String clientId, String clientSecret, String audience, String extra)
            throws MalformedURLException, InterruptedException, ExecutionException;
    
    protected class AuthCallback implements AzureActiveDirectoryTokenProvider.AuthenticationCallback {
        private final String clientId;
        private final String clientSecret;
        
        public AuthCallback(final String clientId, final String clientSecret) {
            this.clientId = clientId;
            this.clientSecret = clientSecret;
        }
        
        @Override
        public CompletableFuture<String> acquireToken(String audience, String authority, Object state) {
            //(new Throwable()).printStackTrace();
            return CompletableFuture.supplyAsync(new TestTokenSupplier(authority, this.clientId, this.clientSecret, audience));
        }
    }

    protected class CustomTokenProvider implements ITokenProvider {
        private final TestTokenSupplier supplier;
        
        public CustomTokenProvider(final String authority, final String clientId, final String clientSecret) {
            this.supplier = new TestTokenSupplier(authority, clientId, clientSecret, ClientConstants.EVENTHUBS_AUDIENCE);
        }
        
        @Override
        public CompletableFuture<SecurityToken> getToken(String resource, Duration timeout) {
            return CompletableFuture.supplyAsync(this.supplier).thenApply((rawToken) -> {
                try {
                    return new JsonSecurityToken(rawToken, resource);
                } catch (ParseException e) {
                    throw new CompletionException(e);
                }
            });
        }
    }
    
    protected class TestTokenSupplier implements Supplier<String> {
        private final String authority;
        private final String clientId;
        private final String clientSecret;
        private final String audience;
        
        public TestTokenSupplier(final String authority, final String clientId, final String clientSecret, final String audience) {
            this.authority = authority;
            this.clientId = clientId;
            this.clientSecret = clientSecret;
            this.audience = audience;
        }

        @Override
        public String get() {
            String retval = "";
            
            try {
                retval = tokenGet(this.authority, this.clientId, this.clientSecret, this.audience, ".default");
            } catch (Exception e) {
                throw new CompletionException(e);
            }
            
            return retval;
        }
    }
    
    protected String decodeToken(final String rawToken) {
        String[] parts = rawToken.split("\\.");
        String output = new String(Base64.getDecoder().decode(parts[0]));
        output += '.';
        output += new String(Base64.getDecoder().decode(parts[1]));
        return output;
    }
}
