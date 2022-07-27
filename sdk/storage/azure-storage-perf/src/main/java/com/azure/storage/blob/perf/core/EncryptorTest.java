// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.perf.core;

import com.azure.storage.blob.perf.BlobPerfStressOptions;
import com.azure.storage.blob.specialized.cryptography.EncryptionVersion;
import com.azure.storage.common.implementation.Constants;
import reactor.core.publisher.Mono;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import java.security.GeneralSecurityException;

import static com.azure.perf.test.core.TestDataCreationHelper.createRandomByteBufferFlux;
import static com.azure.perf.test.core.TestDataCreationHelper.generateRandomString;

public class EncryptorTest extends BlobTestBase<BlobPerfStressOptions> {

    private static final int SIZE = 10 * Constants.MB;

    private static String randomData;

    public EncryptorTest(BlobPerfStressOptions options, String blobName) {
        super(options, blobName);
    }

    @Override
    public void run()  {

        KeyGenerator keyGen = null;
        try {
            keyGen = KeyGenerator.getInstance("AES");

            keyGen.init(256);
            SecretKey aesKey = keyGen.generateKey();
            Encryptor encryptor;

            if (options.getClientEncryption().equals("1.0")) {
                encryptor = Encryptor.getEncryptor(EncryptionVersion.V1, aesKey);
            } else if (options.getClientEncryption().equals("2.0")) {
                encryptor = Encryptor.getEncryptor(EncryptionVersion.V2, aesKey);
            } else {
                throw new RuntimeException();
            }

            encryptor.encrypt(createRandomByteBufferFlux(SIZE)).blockLast();
        } catch (GeneralSecurityException e) {
            throw new RuntimeException();
        }
    }

    @Override
    public Mono<Void> globalSetupAsync() {
        return super.globalSetupAsync()
            .then(Mono.fromCallable(() -> {
                randomData = generateRandomString(SIZE);
                return randomData;
            }))
            .then();
    }

    // Need a branch for v1/v2. And then another test that is EncryptionMultiUpdateTest.
    // Run both of those with both versions on 10mb and compare 4 ways

    @Override
    public Mono<Void> runAsync() {
        return null;
    }
}
