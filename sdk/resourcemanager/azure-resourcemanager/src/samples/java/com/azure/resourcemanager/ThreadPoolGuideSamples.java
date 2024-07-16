// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.DefaultAzureCredentialBuilder;
import reactor.netty.resources.ConnectionProvider;
import reactor.netty.resources.LoopResources;

import java.time.Duration;
import java.util.concurrent.Executors;

/**
 * Code samples for the THREAD_POOL_AND_CONNECTION_POOL.md
 */
public class ThreadPoolGuideSamples {

    public AzureResourceManager azureClientThreadPoolUsingReactorNetty(String subscriptionId) {
        // BEGIN: readme-sample-azureClientConnectionPoolReactorNetty
        NettyAsyncHttpClientBuilder singletonHttpClientBuilder = new NettyAsyncHttpClientBuilder();
        singletonHttpClientBuilder
            // Connection pool configuration.
            .connectionProvider(
                ConnectionProvider.builder("connection-pool")
                    // By default, HttpClient uses a "fixed" connection pool with 500 as the maximum number of active channels
                    // and 1000 as the maximum number of further channel acquisition attempts allowed to be kept in a pending state.
                    .maxConnections(500)
                    // When the maximum number of channels in the pool is reached, up to specified new attempts to
                    // acquire a channel are delayed (pending) until a channel is returned to the pool again, and further attempts are declined with an error.
                    .pendingAcquireMaxCount(1000)
                    .maxIdleTime(Duration.ofSeconds(20)) // Configures the maximum time for a connection to stay idle to 20 seconds.
                    .maxLifeTime(Duration.ofSeconds(60)) // Configures the maximum time for a connection to stay alive to 60 seconds.
                    .pendingAcquireTimeout(Duration.ofSeconds(60)) // Configures the maximum time for the pending acquire operation to 60 seconds.
                    .evictInBackground(Duration.ofSeconds(120)) // Every two minutes, the connection pool is regularly checked for connections that are applicable for removal.
                    .build());
        // END: readme-sample-azureClientConnectionPoolReactorNetty

        // BEGIN: readme-sample-azureClientThreadPoolReactorNetty
        // Thread pool configuration.
        singletonHttpClientBuilder
            .eventLoopGroup(LoopResources
                .create(
                    "client-thread-pool", // thread pool name
                    Runtime.getRuntime().availableProcessors() * 2, // thread pool size
                    true)
                // we use our custom event loop here, disable the native one
                .onClient(false))
            .build();
        // END: readme-sample-azureClientThreadPoolReactorNetty

        HttpClient singletonHttpClient = singletonHttpClientBuilder.build();

        // BEGIN: readme-sample-azureIdentityThreadpool
        // Use the singleton httpClient and a dedicated ExecutorService for Azure Identity
        final AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);
        final TokenCredential credential = new DefaultAzureCredentialBuilder()
            .authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
            .executorService(Executors.newCachedThreadPool()) // use a dedicated `ExecutorService` for the `TokenCredential`
            .httpClient(singletonHttpClient) // use the singleton HttpClient
            .build();
        // END: readme-sample-azureIdentityThreadpool

        // BEGIN: readme-sample-azureClientHttpClient
        // Use the singleton httpClient for your Azure client
        AzureResourceManager azureResourceManager = AzureResourceManager
            .configure()
            .withLogLevel(HttpLogDetailLevel.BASIC)
            .withHttpClient(singletonHttpClient)
            .authenticate(credential, profile)
            .withSubscription(subscriptionId); // your subscription ID, can be different for different Azure clients
        // END: readme-sample-azureClientHttpClient

        return azureResourceManager;
    }
}
