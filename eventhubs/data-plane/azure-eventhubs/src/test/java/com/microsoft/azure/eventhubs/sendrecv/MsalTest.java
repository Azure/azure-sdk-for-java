// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs.sendrecv;

import com.microsoft.aad.adal4j.AuthenticationContext;
import com.microsoft.aad.adal4j.AuthenticationResult;
import com.microsoft.aad.adal4j.ClientCredential;
import com.microsoft.aad.msal4j.ClientCredentialParameters;
import com.microsoft.aad.msal4j.ClientSecret;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.aad.msal4j.IAuthenticationResult;
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

import java.net.MalformedURLException;
import java.net.URI;
import java.text.ParseException;
import java.time.Duration;
import java.util.Base64;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class MsalTest extends ApiTestBase {
	static URI endpoint;
	static String eventHubName;
	
	final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(8);
	final private String authority = "https://login.windows.net/72f988bf-86f1-41af-91ab-2d7cd011db47";
	//final private String authority = AzureActiveDirectoryTokenProvider.COMMON_AUTHORITY;
	final private String clientId = "51ce7943-a1aa-4bb1-b708-8f66656a9242";
	final private String clientSecret = "sMm5YoVVcmb4fUtm=dQa*nY-P-YEkx35";
	
    @BeforeClass
    public static void initializeEventHub() throws Exception {
        ConnectionStringBuilder csb = TestContext.getConnectionString();
        MsalTest.endpoint = csb.getEndpoint();
        MsalTest.eventHubName = csb.getEventHubName();
    }

    @AfterClass()
    public static void cleanup() throws EventHubException {
    }
	
	@Test
	public void runSendReceiveWithAuthCallbackTest() throws Exception {
		final AuthCallback callback = new AuthCallback();
		final EventHubClient ehc = EventHubClient.createWithAzureActiveDirectory(MsalTest.endpoint, MsalTest.eventHubName,
				callback, this.executorService, null).get();

		innerTest(ehc);
	}
	
	@Test
	public void runSendReceiveWithAuthCallbackWithAuthorityTest() throws Exception {
		final AuthCallback callback = new AuthCallback();
		final EventHubClient ehc = EventHubClient.createWithAzureActiveDirectory(MsalTest.endpoint, MsalTest.eventHubName,
				callback, this.authority, this.executorService, null).get();

		innerTest(ehc);
	}
	
	private void innerTest(final EventHubClient ehc) throws EventHubException {
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
	
	@Test
	public void runSendReceiveWithAADTokenProvider() throws Exception {
		final AuthCallback callback = new AuthCallback();
		final AzureActiveDirectoryTokenProvider aadTokenProvider =
				new AzureActiveDirectoryTokenProvider(callback, this.authority, null);
		final EventHubClient ehc = EventHubClient.createWithTokenProvider(MsalTest.endpoint, MsalTest.eventHubName, aadTokenProvider,
				this.executorService, null).get();
		
		innerTest(ehc);
	}
	
	@Test
	public void runSendReceiveWithCustomTokenProvider() throws Exception {
		final CustomTokenProvider tokenProvider = new CustomTokenProvider();
		final EventHubClient ehc = EventHubClient.createWithTokenProvider(MsalTest.endpoint, MsalTest.eventHubName, tokenProvider,
				this.executorService, null).get();
		
		innerTest(ehc);
	}
	
	class CustomTokenProvider implements ITokenProvider, Supplier<String> {
		@Override
		public CompletableFuture<SecurityToken> getToken(String resource, Duration timeout) {
			return CompletableFuture.supplyAsync(this).thenApply((rawToken) -> {
	    		try {
					return new JsonSecurityToken(rawToken, resource);
				} catch (ParseException e) {
					throw new CompletionException(e);
				}
	    	});
		}

		@Override
		public String get() {
			String retval = "";
			
			try {
				retval = MsalTest.this.tokenGet(MsalTest.this.authority, MsalTest.this.clientId, MsalTest.this.clientSecret, ClientConstants.EVENTHUBS_AUDIENCE, ".default");
			}
			catch (Exception e) {
				throw new CompletionException(e);
			}
			
			return retval;
		}
	}
	
	class AuthCallback implements AzureActiveDirectoryTokenProvider.AuthenticationCallback, Supplier<String> {
		private String audience;
		private String callbackAuthority;
		
		@Override
		public CompletableFuture<String> acquireToken(String audience, String authority, Object state) {
			this.audience = audience;
			this.callbackAuthority = authority;
			return CompletableFuture.supplyAsync(this);
		}

		@Override
		public String get() {
			String retval = "";
			
			try {
				retval = MsalTest.this.tokenGet(this.callbackAuthority, MsalTest.this.clientId, MsalTest.this.clientSecret, this.audience, ".default");
			}
			catch (Exception e) {
				throw new CompletionException(e);
			}
			
			return retval;
		}
	}

	private String tokenGet(final String authority, final String clientId, final String clientSecret, final String audience, final String extra)
			throws MalformedURLException, InterruptedException, ExecutionException {
		return adalGet(authority, clientId, clientSecret, audience);
		//return msalGet(authority, clientId, clientSecret, audience + extra);
	}
	
	private String adalGet(final String authority, final String clientId, final String clientSecret, final String audience)
			throws MalformedURLException, InterruptedException, ExecutionException {
		AuthenticationContext context = new AuthenticationContext(authority, true, MsalTest.this.executorService);
		ClientCredential creds = new ClientCredential(clientId, clientSecret);
		AuthenticationResult result = context.acquireToken(audience, creds, null).get();
		System.out.println(decodeToken(result.getAccessToken()));
		return result.getAccessToken();
	}
	
	private String msalGet(final String authority, final String clientId, final String clientSecret, final String audience)
			throws MalformedURLException, InterruptedException, ExecutionException {
		ConfidentialClientApplication app = ConfidentialClientApplication.builder(clientId, new ClientSecret(clientSecret))
				.authority(authority)
				.build();
		
		ClientCredentialParameters parameters = ClientCredentialParameters.builder(Collections.singleton(audience)).build();

		IAuthenticationResult result = app.acquireToken(parameters).get();
		
		System.out.println(result.accessToken());
		System.out.println(decodeToken(result.accessToken()));
		return result.accessToken();
	}
	
	private String decodeToken(final String rawToken) {
		String parts[] = rawToken.split("\\.");
		String output = new String(Base64.getDecoder().decode(parts[0]));
		output += '.';
		output += new String(Base64.getDecoder().decode(parts[1]));
		return output;
	}
}
