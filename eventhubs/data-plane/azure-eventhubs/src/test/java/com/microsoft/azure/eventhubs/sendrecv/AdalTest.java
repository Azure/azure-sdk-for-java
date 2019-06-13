package com.microsoft.azure.eventhubs.sendrecv;

import java.net.MalformedURLException;
import java.util.concurrent.ExecutionException;

import org.junit.Test;

import com.microsoft.aad.adal4j.AuthenticationContext;
import com.microsoft.aad.adal4j.AuthenticationResult;
import com.microsoft.aad.adal4j.ClientCredential;
import com.microsoft.azure.eventhubs.AzureActiveDirectoryTokenProvider;
import com.microsoft.azure.eventhubs.EventHubClient;

public class AdalTest extends AadBase {
	final private String authority = "https://login.windows.net/72f988bf-86f1-41af-91ab-2d7cd011db47";
	//final private String authority = AzureActiveDirectoryTokenProvider.COMMON_AUTHORITY;
	final private String clientId = "51ce7943-a1aa-4bb1-b708-8f66656a9242";
	final private String clientSecret = "sMm5YoVVcmb4fUtm=dQa*nY-P-YEkx35";
	
	@Test
	public void runSendReceiveWithAuthCallbackTest() throws Exception {
		final AuthCallback callback = new AuthCallback(this.clientId, this.clientSecret);
		final EventHubClient ehc = EventHubClient.createWithAzureActiveDirectory(MsalTest.endpoint, MsalTest.eventHubName,
				callback, this.executorService, null).get();

		innerTest(ehc);
	}
	
	@Test
	public void runSendReceiveWithAuthCallbackWithAuthorityTest() throws Exception {
		final AuthCallback callback = new AuthCallback(this.clientId, this.clientSecret);
		final EventHubClient ehc = EventHubClient.createWithAzureActiveDirectory(MsalTest.endpoint, MsalTest.eventHubName,
				callback, this.authority, this.executorService, null).get();

		innerTest(ehc);
	}
	
	@Test
	public void runSendReceiveWithAADTokenProvider() throws Exception {
		final AuthCallback callback = new AuthCallback(this.clientId, this.clientSecret);
		final AzureActiveDirectoryTokenProvider aadTokenProvider =
				new AzureActiveDirectoryTokenProvider(callback, this.authority, null);
		final EventHubClient ehc = EventHubClient.createWithTokenProvider(MsalTest.endpoint, MsalTest.eventHubName, aadTokenProvider,
				this.executorService, null).get();
		
		innerTest(ehc);
	}
	
	@Test
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
		System.out.println(decodeToken(result.getAccessToken()));
		return result.getAccessToken();
	}
}
