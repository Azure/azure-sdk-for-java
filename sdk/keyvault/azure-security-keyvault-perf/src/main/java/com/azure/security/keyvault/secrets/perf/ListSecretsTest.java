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
        for (int i=0; i < _secretNames.length; i++) {
            _secretNames[i] = "listSecretsPerfTest-" + UUID.randomUUID();
        }

        return Flux.fromArray(_secretNames)
                .flatMap(secretName -> secretAsyncClient.setSecret(secretName, secretName))
                .then();
    }

    @Override
    public Mono<Void> globalCleanupAsync() {
        if (_secretNames != null) {
            return deleteAndPurgeSecretsAsync(_secretNames).then(super.globalCleanupAsync());
        }
        else {
            return super.globalCleanupAsync();
        }
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
