// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.secrets.perf;

import com.azure.perf.test.core.PerfStressOptions;
import com.azure.security.keyvault.secrets.perf.core.SecretsTest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public class ListSecretsTest extends SecretsTest<PerfStressOptions> {
    public ListSecretsTest(PerfStressOptions options) {
        super(options);
    }

    public Mono<Void> globalSetupAsync() {
        return super.globalSetupAsync()
            .then(Flux.range(0, options.getCount())
                .map(i -> "listSecretsPerfTest-" + UUID.randomUUID())
                .flatMap(b -> secretAsyncClient.setSecret(b, b))
                .then());
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
