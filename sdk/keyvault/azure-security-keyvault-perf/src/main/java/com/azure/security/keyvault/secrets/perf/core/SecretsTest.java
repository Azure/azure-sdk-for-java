// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.secrets.perf.core;

import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.polling.AsyncPollResponse;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.perf.test.core.PerfStressOptions;
import com.azure.perf.test.core.PerfStressTest;
import com.azure.security.keyvault.secrets.SecretAsyncClient;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.security.keyvault.secrets.models.DeletedSecret;
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
        SecretClientBuilder builder = new SecretClientBuilder().vaultUrl(vaultUrl)
            .credential(new DefaultAzureCredentialBuilder().build());

        configureClientBuilder(builder);

        secretClient = builder.buildClient();
        secretAsyncClient = builder.buildAsyncClient();
    }

    protected Mono<Void> deleteAndPurgeSecretsAsync(String... names) {
        return Flux.fromArray(names)
            .flatMap(name -> secretAsyncClient.beginDeleteSecret(name).last())
            .map(AsyncPollResponse::getValue)
            .flatMap(deletedSecret -> {
                String recoveryId = deletedSecret.getRecoveryId();
                if (recoveryId != null && !recoveryId.isEmpty()) {
                    return secretAsyncClient.purgeDeletedSecret(deletedSecret.getName());
                } else {
                    return Mono.empty();
                }
            })
            .then();
    }

    protected void deleteAndPurgeSecrets(String... names) {
        for (String name : names) {
            DeletedSecret deletedSecret = secretClient.beginDeleteSecret(name).waitForCompletion().getValue();
            String recoveryId = deletedSecret.getRecoveryId();
            if (recoveryId != null && !recoveryId.isEmpty()) {
                secretClient.purgeDeletedSecret(deletedSecret.getName());
            }
        }
    }
}
