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
import com.microsoft.azure.eventhubs.ITokenProvider;
import com.microsoft.azure.eventhubs.JsonSecurityToken;
import com.microsoft.azure.eventhubs.PartitionReceiver;
import com.microsoft.azure.eventhubs.PartitionSender;
import com.microsoft.azure.eventhubs.SecurityToken;
import com.microsoft.azure.eventhubs.impl.ClientConstants;
import com.microsoft.azure.eventhubs.impl.EventPositionImpl;
import com.microsoft.azure.eventhubs.lib.ApiTestBase;
import com.microsoft.azure.eventhubs.lib.TestContext;

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

	protected void innerTest(final EventHubClient ehc) throws EventHubException {
        final PartitionReceiver pReceiver = ehc.createReceiverSync("$Default", "0", EventPositionImpl.fromEndOfStream());

        final PartitionSender pSender = ehc.createPartitionSenderSync("0");
        final String testMessage = "somedata test";
        pSender.send(EventData.create(testMessage.getBytes()));

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

        pSender.closeSync();
        pReceiver.closeSync();;
        ehc.closeSync();
        
        Assert.assertTrue(found);
	}

	abstract String tokenGet(final String authority, final String clientId, final String clientSecret, final String audience, final String extra)
			throws MalformedURLException, InterruptedException, ExecutionException;
	
	protected class AuthCallback implements AzureActiveDirectoryTokenProvider.AuthenticationCallback {
		final private String clientId;
		final private String clientSecret;
		
		public AuthCallback(final String clientId, final String clientSecret) {
			this.clientId = clientId;
			this.clientSecret = clientSecret;
		}
		
		@Override
		public CompletableFuture<String> acquireToken(String audience, String authority, Object state) {
			return CompletableFuture.supplyAsync(new TestTokenSupplier(authority, this.clientId, this.clientSecret, audience));
		}
	}

	protected class CustomTokenProvider implements ITokenProvider {
		final private TestTokenSupplier supplier;
		
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
		final private String authority;
		final private String clientId;
		final private String clientSecret;
		final private String audience;
		
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
			}
			catch (Exception e) {
				throw new CompletionException(e);
			}
			
			return retval;
		}
	}
	
	
	protected String decodeToken(final String rawToken) {
		String parts[] = rawToken.split("\\.");
		String output = new String(Base64.getDecoder().decode(parts[0]));
		output += '.';
		output += new String(Base64.getDecoder().decode(parts[1]));
		return output;
	}
}
