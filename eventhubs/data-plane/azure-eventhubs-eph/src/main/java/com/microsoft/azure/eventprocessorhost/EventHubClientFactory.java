// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventprocessorhost;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;

import com.microsoft.azure.eventhubs.AzureActiveDirectoryTokenProvider;
import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.EventHubClientOptions;
import com.microsoft.azure.eventhubs.EventHubException;
import com.microsoft.azure.eventhubs.ITokenProvider;
import com.microsoft.azure.eventhubs.RetryPolicy;

abstract class EventHubClientFactory {
	protected final ScheduledExecutorService executor;
	
	protected final EventHubClientOptions options;
	
	public EventHubClientFactory(final ScheduledExecutorService executor, final RetryPolicy retryPolicy) {
		this(executor, (new EventHubClientOptions()).setRetryPolicy(retryPolicy));
	}
	
	public EventHubClientFactory(final ScheduledExecutorService executor,
			final EventHubClientOptions options) {
		this.executor = executor;
		this.options = options;
	}
	
	abstract CompletableFuture<EventHubClient> createEventHubClient() throws EventHubException, IOException;
	
	static class EHCFWithConnectionString extends EventHubClientFactory {
		private final String eventHubConnectionString;
		
		public EHCFWithConnectionString(final String eventHubConnectionString,
				final RetryPolicy retryPolicy,
				final ScheduledExecutorService executor) {
			super(executor, retryPolicy);
			this.eventHubConnectionString = eventHubConnectionString;
		}
		
		public CompletableFuture<EventHubClient> createEventHubClient() throws EventHubException, IOException {
			return EventHubClient.createFromConnectionString(this.eventHubConnectionString, this.options.getRetryPolicy(), this.executor);
		}
	}
	
	static class EHCFWithAuthCallback extends EventHubClientFactory {
		private final URI endpoint;
		private final String eventHubPath;
		private final AzureActiveDirectoryTokenProvider.AuthenticationCallback authCallback;
		private final String authority;
		
		public EHCFWithAuthCallback(final URI endpoint,
				final String eventHubPath,
				final AzureActiveDirectoryTokenProvider.AuthenticationCallback authCallback,
				final String authority,
				final EventHubClientOptions options,
				final ScheduledExecutorService executor) {
			super(executor, options);
			this.endpoint = endpoint;
			this.eventHubPath = eventHubPath;
			this.authCallback = authCallback;
			this.authority = authority;
		}
		
		public CompletableFuture<EventHubClient> createEventHubClient() throws EventHubException, IOException {
			return EventHubClient.createWithAzureActiveDirectory(this.endpoint,
					this.eventHubPath, this.authCallback, this.authority, this.executor, this.options);
		}
	}
	
	static class EHCFWithTokenProvider extends EventHubClientFactory {
		private final URI endpoint;
		private final String eventHubPath;
		private final ITokenProvider tokenProvider;
		
		public EHCFWithTokenProvider(final URI endpoint,
				final String eventHubPath,
				final ITokenProvider tokenProvider,
				final EventHubClientOptions options,
				final ScheduledExecutorService executor) {
			super(executor, options);
			this.endpoint = endpoint;
			this.eventHubPath = eventHubPath;
			this.tokenProvider = tokenProvider;
		}
		
		public CompletableFuture<EventHubClient> createEventHubClient() throws EventHubException, IOException {
			return EventHubClient.createWithTokenProvider(this.endpoint, this.eventHubPath, this.tokenProvider, this.executor, this.options);
		}
	}
}
