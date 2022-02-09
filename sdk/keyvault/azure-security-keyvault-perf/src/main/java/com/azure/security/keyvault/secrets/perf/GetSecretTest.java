// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.secrets.perf;

import com.azure.perf.test.core.PerfStressOptions;
import com.azure.security.keyvault.secrets.perf.core.SecretsTest;
import reactor.core.publisher.Mono;

import java.util.UUID;

public class GetSecretTest extends SecretsTest<PerfStressOptions> {
    private static final String SECRET_NAME = "getSecretPerfTest-" + UUID.randomUUID();

    public GetSecretTest(PerfStressOptions options) {
        super(options);
    }

    @Override
    public Mono<Void> globalSetupAsync() {
        return super.globalSetupAsync()
            .then(secretAsyncClient.setSecret(SECRET_NAME, SECRET_NAME))
            .then();
    }

    @Override
    public Mono<Void> globalCleanupAsync() {
        return deleteAndPurgeSecretsAsync(SECRET_NAME)
            .then(super.globalCleanupAsync());
    }

    @Override
    public void run() {
        secretClient.getSecret(SECRET_NAME);
    }

    @Override
    public Mono<Void> runAsync() {
        return secretAsyncClient.getSecret(SECRET_NAME).then();
    }
}
