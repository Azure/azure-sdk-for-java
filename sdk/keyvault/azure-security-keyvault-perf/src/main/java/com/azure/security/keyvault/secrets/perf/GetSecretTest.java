// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.secrets.perf;

import com.azure.perf.test.core.PerfStressOptions;
import com.azure.security.keyvault.secrets.perf.core.SecretsTest;
import reactor.core.publisher.Mono;

import java.util.UUID;

public class GetSecretTest extends SecretsTest<PerfStressOptions> {
    private final String secretName;

    public GetSecretTest(PerfStressOptions options) {
        super(options);

        secretName = "getSecretPerfTest-" + UUID.randomUUID();
    }

    public Mono<Void> globalSetupAsync() {
        return super.globalSetupAsync()
            .then(secretAsyncClient.setSecret(secretName, secretName))
            .then();
    }

    @Override
    public void run() {
        secretClient.getSecret(secretName);
    }

    @Override
    public Mono<Void> runAsync() {
        return secretAsyncClient.getSecret(secretName).then();
    }
}
