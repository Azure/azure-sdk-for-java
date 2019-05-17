package com.microsoft.azure.eventhubs;

import java.io.IOException;
import java.text.ParseException;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import com.microsoft.azure.credentials.MSICredentials;

public class ManagedIdentityTokenProvider implements ITokenProvider {
    @Override
    public CompletableFuture<SecurityToken> getToken(final String resource, final Duration timeout) {
    	final MSICredentials credentials = new MSICredentials();

    	return CompletableFuture.supplyAsync(() -> {
			try {
				String rawToken = credentials.getToken(resource);
				return new JsonSecurityToken(rawToken, resource);
			} catch (IOException | ParseException e) {
				throw new CompletionException(e);
			}
		});
    }
}
