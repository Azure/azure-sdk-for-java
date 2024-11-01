// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized.cryptography;

import com.azure.core.cryptography.AsyncKeyEncryptionKey;
import com.azure.core.util.FluxUtil;
import com.azure.security.keyvault.keys.cryptography.CryptographyServiceVersion;
import com.azure.security.keyvault.keys.cryptography.KeyEncryptionKeyClientBuilder;
import com.azure.security.keyvault.keys.models.JsonWebKey;
import com.azure.security.keyvault.keys.models.KeyOperation;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.common.implementation.Constants;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.util.Arrays;

import static com.azure.core.test.utils.TestUtils.assertArraysEqual;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class LocalKeyAsyncTests extends BlobCryptographyTestBase {
    private static final CryptographyServiceVersion CRYPTOGRAPHY_SERVICE_VERSION = CryptographyServiceVersion.V7_2;
    private BlobContainerAsyncClient cca;
    private EncryptedBlobAsyncClient beac; // encrypted client for download

    @Override
    protected void beforeTest() {
        super.beforeTest();
        /* Insecurely generate a local key*/
        JsonWebKey localKey
            = JsonWebKey
                .fromAes(new SecretKeySpec(getRandomByteArray(256), "AES"),
                    Arrays.asList(KeyOperation.WRAP_KEY, KeyOperation.UNWRAP_KEY))
                .setId("local");
        AsyncKeyEncryptionKey akek = new KeyEncryptionKeyClientBuilder().serviceVersion(CRYPTOGRAPHY_SERVICE_VERSION)
            .buildAsyncKeyEncryptionKey(localKey)
            .block();

        cca = getServiceClientBuilder(ENV.getPrimaryAccount()).buildAsyncClient()
            .getBlobContainerAsyncClient(generateContainerName());
        cca.create().block();

        beac = getEncryptedClientBuilder(akek, null, ENV.getPrimaryAccount().getCredential(), cca.getBlobContainerUrl())
            .blobName(generateBlobName())
            .buildEncryptedBlobAsyncClient();
    }

    @Test
    public void uploadDownload() {
        byte[] inputArray = getRandomByteArray(Constants.KB);

        StepVerifier
            .create(beac.upload(Flux.just(ByteBuffer.wrap(inputArray)), null)
                .then(FluxUtil.collectBytesInByteBufferStream(beac.downloadStream())))
            .assertNext(r -> assertArraysEqual(inputArray, r))
            .verifyComplete();
    }

    @Test
    public void encryptionNotANoop() {
        byte[] inputArray = getRandomByteArray(Constants.KB);

        StepVerifier
            .create(beac.upload(Flux.just(ByteBuffer.wrap(inputArray)), null)
                .then(FluxUtil
                    .collectBytesInByteBufferStream(cca.getBlobAsyncClient(beac.getBlobName()).downloadStream())))
            .assertNext(r -> assertFalse(Arrays.equals(inputArray, r)))
            .verifyComplete();
    }
}
