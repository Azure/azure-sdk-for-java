// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.secrets.perf;

import com.azure.perf.test.core.PerfStressOptions;
import com.azure.security.keyvault.secrets.perf.core.SecretsTest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public class ListSecretsTest extends SecretsTest<PerfStressOptions> {
    private String[] _secretNames;

    public ListSecretsTest(PerfStressOptions options) {
        super(options);
    }

    @Override
    public Mono<Void> globalSetupAsync() {
        super.globalSetupAsync().block();

        // Validate that vault contains 0 secrets (including soft-deleted secrets), since additional secrets
        // (including soft-deleted) impact performance.
        if (secretClient.listPropertiesOfSecrets().iterator().hasNext() ||
            secretClient.listDeletedSecrets().iterator().hasNext()) {

            throw new RuntimeException("KeyVault " + secretClient.getVaultUrl() + "must contain 0 " +
                "secrets (including soft-deleted) before starting perf test");
        }

        _secretNames = new String[options.getCount()];

        return Flux.range(0, options.getCount())
                .map(i -> {
                    String name = "listSecretsPerfTest-" + UUID.randomUUID();
                    _secretNames[i] = name;
                    return name;
                })
                .flatMap(b -> secretAsyncClient.setSecret(b, b))
                .then();
    }

    @Override
    public Mono<Void> globalCleanupAsync() {
        return deleteSecretsAsync(_secretNames).then(super.globalCleanupAsync());
    }

    @Override
    public void run() {
        secretClient.listPropertiesOfSecrets().forEach(b -> {
        });
    }

    @Override
    public Mono<Void> runAsync() {
        return secretAsyncClient.listPropertiesOfSecrets()
            .then();
    }
}
