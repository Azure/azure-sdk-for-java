// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.secrets.perf.core;

import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.perf.test.core.PerfStressOptions;
import com.azure.perf.test.core.PerfStressTest;
import com.azure.security.keyvault.secrets.SecretAsyncClient;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public abstract class SecretsTest<TOptions extends PerfStressOptions> extends PerfStressTest<TOptions> {
    protected final SecretClient secretClient;
    protected final SecretAsyncClient secretAsyncClient;
    private final Configuration configuration;

    /**
     * Creates an instance of performance test.
     *
     * @param options the options configured for the test.
     */
    public SecretsTest(TOptions options) {
        super(options);

        configuration = Configuration.getGlobalConfiguration().clone();
        String vaultUrl = configuration.get("AZURE_KEYVAULT_URL");

        if (CoreUtils.isNullOrEmpty(vaultUrl)) {
            throw new IllegalStateException("Environment variable AZURE_KEYVAULT_URL must be set");
        }

        // Setup the service client
        SecretClientBuilder builder = new SecretClientBuilder()
            .vaultUrl(vaultUrl)
            .credential(new DefaultAzureCredentialBuilder().build());

        configureClientBuilder(builder);

        secretClient = builder.buildClient();
        secretAsyncClient = builder.buildAsyncClient();
    }

    protected Mono<Void> deleteAndPurgeSecretsAsync(String ... names) {
        return Flux
            .fromArray(names)
            .flatMap(name -> secretAsyncClient
                .beginDeleteSecret(name)
                .doOnError(err -> System.err.println("Secret '" + name + "' not found for delete"))
                .onErrorResume(ResourceNotFoundException.class, e -> Mono.empty())
                .then(secretAsyncClient
                    .purgeDeletedSecret(name)
                    .doOnError(err -> System.err.println("Secret '" + name + "' not found for purge"))
                    .onErrorResume(ResourceNotFoundException.class, e -> Mono.empty())))
            .then();
    }
}
