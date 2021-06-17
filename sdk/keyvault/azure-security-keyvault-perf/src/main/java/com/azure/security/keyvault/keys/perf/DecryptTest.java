// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.keys.perf;

import com.azure.perf.test.core.PerfStressOptions;
import com.azure.security.keyvault.keys.cryptography.models.EncryptionAlgorithm;
import com.azure.security.keyvault.keys.perf.core.CryptographyTest;
import reactor.core.publisher.Mono;

import java.security.SecureRandom;

public class DecryptTest extends CryptographyTest<PerfStressOptions> {
    private final EncryptionAlgorithm encryptionAlgorithm = EncryptionAlgorithm.RSA_OAEP;
    private final byte[] plaintext;

    public DecryptTest(PerfStressOptions options) {
        super(options);

        plaintext = new byte[100];
        new SecureRandom().nextBytes(plaintext);
    }

    @Override
    public void run() {
        cryptographyClient.decrypt(encryptionAlgorithm, plaintext);
    }

    @Override
    public Mono<Void> runAsync() {
        return cryptographyAsyncClient.decrypt(encryptionAlgorithm, plaintext)
            .then();
    }
}
