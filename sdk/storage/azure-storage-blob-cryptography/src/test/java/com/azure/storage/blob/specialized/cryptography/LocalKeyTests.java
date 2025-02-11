// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized.cryptography;

import com.azure.core.cryptography.AsyncKeyEncryptionKey;
import com.azure.security.keyvault.keys.cryptography.CryptographyServiceVersion;
import com.azure.security.keyvault.keys.cryptography.KeyEncryptionKeyClientBuilder;
import com.azure.security.keyvault.keys.models.JsonWebKey;
import com.azure.security.keyvault.keys.models.KeyOperation;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.common.implementation.Constants;
import org.junit.jupiter.api.Test;

import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Arrays;

import static com.azure.core.test.utils.TestUtils.assertArraysEqual;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class LocalKeyTests extends BlobCryptographyTestBase {
    private static final CryptographyServiceVersion CRYPTOGRAPHY_SERVICE_VERSION = CryptographyServiceVersion.V7_2;
    private BlobContainerClient cc;
    private EncryptedBlobClient bec; // encrypted client for download

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

        cc = getServiceClientBuilder(ENV.getPrimaryAccount()).buildClient()
            .getBlobContainerClient(generateContainerName());
        cc.create();

        bec = getEncryptedClientBuilder(akek, null, ENV.getPrimaryAccount().getCredential(), cc.getBlobContainerUrl())
            .blobName(generateBlobName())
            .buildEncryptedBlobClient();
    }

    @Test
    public void uploadDownload() {
        byte[] inputArray = getRandomByteArray(Constants.KB);
        InputStream stream = new ByteArrayInputStream(inputArray);
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        bec.upload(stream, Constants.KB);
        bec.download(os);

        assertArraysEqual(inputArray, os.toByteArray());
    }

    @Test
    public void encryptionNotANoop() {
        byte[] inputArray = getRandomByteArray(Constants.KB);
        InputStream stream = new ByteArrayInputStream(inputArray);
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        bec.upload(stream, Constants.KB);
        cc.getBlobClient(bec.getBlobName()).download(os);

        assertFalse(Arrays.equals(inputArray, os.toByteArray()));
    }
}
