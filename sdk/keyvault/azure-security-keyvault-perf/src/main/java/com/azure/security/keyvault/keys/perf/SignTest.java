// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.keys.perf;

import com.azure.perf.test.core.PerfStressOptions;
import com.azure.security.keyvault.keys.cryptography.models.SignatureAlgorithm;
import com.azure.security.keyvault.keys.perf.core.CryptographyTest;
import reactor.core.publisher.Mono;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class SignTest extends CryptographyTest<PerfStressOptions> {
    private final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.RS256;
    private final byte[] digest;

    public SignTest(PerfStressOptions options) throws NoSuchAlgorithmException {
        super(options);

        byte[] plaintext = new byte[100];
        new SecureRandom().nextBytes(plaintext);
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(plaintext);
        digest = md.digest();
    }

    @Override
    public void run() {
        cryptographyClient.sign(signatureAlgorithm, digest);
    }

    @Override
    public Mono<Void> runAsync() {
        return cryptographyAsyncClient.sign(signatureAlgorithm, digest)
            .then();
    }
}
