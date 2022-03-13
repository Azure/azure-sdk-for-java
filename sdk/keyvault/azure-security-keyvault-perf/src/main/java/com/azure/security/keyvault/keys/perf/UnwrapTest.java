// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.keys.perf;

import com.azure.perf.test.core.PerfStressOptions;
import com.azure.security.keyvault.keys.cryptography.models.KeyWrapAlgorithm;
import com.azure.security.keyvault.keys.cryptography.models.WrapResult;
import com.azure.security.keyvault.keys.perf.core.CryptographyTest;
import reactor.core.publisher.Mono;

import java.security.SecureRandom;

public class UnwrapTest extends CryptographyTest<PerfStressOptions> {
    private final KeyWrapAlgorithm keyWrapAlgorithm = KeyWrapAlgorithm.RSA_OAEP;
    private final WrapResult wrapResult;

    public UnwrapTest(PerfStressOptions options) {
        super(options);

        byte[] plaintext = new byte[100];
        new SecureRandom().nextBytes(plaintext);

        wrapResult = cryptographyClient.wrapKey(keyWrapAlgorithm, plaintext);
    }

    @Override
    public void run() {
        cryptographyClient.unwrapKey(keyWrapAlgorithm, wrapResult.getEncryptedKey());
    }

    @Override
    public Mono<Void> runAsync() {
        return cryptographyAsyncClient.unwrapKey(keyWrapAlgorithm, wrapResult.getEncryptedKey())
            .then();
    }
}
