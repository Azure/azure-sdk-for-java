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
    protected ScheduledExecutorService executor;
    
    protected final EventHubClientOptions options;
    
    EventHubClientFactory(final RetryPolicy retryPolicy) {
        this((new EventHubClientOptions()).setRetryPolicy(retryPolicy));
    }
    
    EventHubClientFactory(final EventHubClientOptions options) {
        this.options = options;
    }
    
    public void setExecutor(ScheduledExecutorService executor) {
        this.executor = executor;
    }
    
    abstract CompletableFuture<EventHubClient> createEventHubClient() throws EventHubException, IOException;
    
    static class EHCFWithConnectionString extends EventHubClientFactory {
        private final String eventHubConnectionString;
        
        EHCFWithConnectionString(final String eventHubConnectionString,
                final RetryPolicy retryPolicy) {
            super(retryPolicy);
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
        
        EHCFWithAuthCallback(final URI endpoint,
                final String eventHubPath,
                final AzureActiveDirectoryTokenProvider.AuthenticationCallback authCallback,
                final String authority,
                final EventHubClientOptions options) {
            super(options);
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
        
        EHCFWithTokenProvider(final URI endpoint,
                final String eventHubPath,
                final ITokenProvider tokenProvider,
                final EventHubClientOptions options) {
            super(options);
            this.endpoint = endpoint;
            this.eventHubPath = eventHubPath;
            this.tokenProvider = tokenProvider;
        }
        
        public CompletableFuture<EventHubClient> createEventHubClient() throws EventHubException, IOException {
            return EventHubClient.createWithTokenProvider(this.endpoint, this.eventHubPath, this.tokenProvider, this.executor, this.options);
        }
    }
}
