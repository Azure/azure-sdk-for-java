// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.perf.core;

import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.perf.test.core.PerfStressOptions;
import com.azure.perf.test.core.PerfStressTest;
import com.azure.storage.blob.BlobServiceAsyncClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

public abstract class ServiceTest<TOptions extends PerfStressOptions> extends PerfStressTest<TOptions> {

    protected final BlobServiceClient blobServiceClient;
    protected final BlobServiceAsyncClient blobServiceAsyncClient;
    private final Configuration configuration;
    protected String connectionString;

    public ServiceTest(TOptions options) {
        super(options);
        configuration = Configuration.getGlobalConfiguration().clone();
        connectionString = configuration.get("STORAGE_CONNECTION_STRING");

        if (CoreUtils.isNullOrEmpty(connectionString)) {
            throw new IllegalStateException("Environment variable STORAGE_CONNECTION_STRING must be set");
        }

        // Setup the service client
        BlobServiceClientBuilder builder = new BlobServiceClientBuilder()
            .connectionString(connectionString);

        configureClientBuilder(builder);

        blobServiceClient = builder.buildClient();
        blobServiceAsyncClient = builder.buildAsyncClient();
    }

    @Override
    public Mono<Void> globalSetupAsync() {
        // Arbitrarily run 1000 service get properties calls to warm up the connection pool used by the HttpClient.
        // This helps guard against an edge case seen in Reactor Netty where only one IO thread could end up owning all
        // connections in the connection pool. This results in drastically less CPU usage and throughput, there is
        // ongoing discussions with Reactor Netty on what causes this edge case, whether we had a design flaw in the
        // performance tests, or if there is a configuration change needed in Reactor Netty.
        return super.globalSetupAsync().then(Flux.range(0, 1000)
            .parallel(options.getParallel())
            .runOn(Schedulers.boundedElastic())
            .flatMap(ignored -> blobServiceAsyncClient.getProperties(), false,
                Math.min(options.getParallel(), 1000 / options.getParallel()), 1)
            .then());
    }
}
