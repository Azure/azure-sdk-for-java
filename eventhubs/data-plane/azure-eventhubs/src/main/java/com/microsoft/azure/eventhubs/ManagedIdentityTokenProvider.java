package com.microsoft.azure.eventhubs;

import java.io.IOException;
import java.text.ParseException;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import com.microsoft.azure.credentials.MSICredentials;
import com.microsoft.azure.eventhubs.impl.ClientConstants;

public class ManagedIdentityTokenProvider implements ITokenProvider {
	final static MSICredentials credentials = new MSICredentials();
	
    @Override
    public CompletableFuture<SecurityToken> getToken(final String resource, final Duration timeout) {
    	return CompletableFuture.supplyAsync(() -> {
			try {
				String rawToken = ManagedIdentityTokenProvider.credentials.getToken(ClientConstants.EVENTHUBS_AUDIENCE);
				return new JsonSecurityToken(rawToken, resource);
			} catch (IOException | ParseException e) {
				throw new CompletionException(e);
			}
		});
    }
}
