// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.keys.perf;

import com.azure.perf.test.core.PerfStressOptions;
import com.azure.security.keyvault.keys.models.KeyType;
import com.azure.security.keyvault.keys.perf.core.KeysTest;
import reactor.core.publisher.Mono;

import java.util.UUID;

public class GetKeyTest extends KeysTest<PerfStressOptions> {
    private final String keyName;

    public GetKeyTest(PerfStressOptions options) {
        super(options);

        keyName = "getKeyPerfTest-" + UUID.randomUUID();
    }

    public Mono<Void> globalSetupAsync() {
        return super.globalSetupAsync()
            .then(keyAsyncClient.createKey(keyName, KeyType.RSA))
            .then();
    }

    @Override
    public void run() {
        keyClient.getKey(keyName);
    }

    @Override
    public Mono<Void> runAsync() {
        return keyAsyncClient.getKey(keyName).then();
    }
}
