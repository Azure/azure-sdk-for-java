// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized.cryptography;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.BlobErrorCode;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.common.implementation.Constants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class EncryptedBlobOutputStreamTests extends BlobCryptographyTestBase {
    private static final String KEY_ID = "keyId";
    private EncryptedBlobClient bec; // encrypted client
    private EncryptedBlobAsyncClient beac; // encrypted async client
    private BlobContainerClient cc;

    @Override
    protected void beforeTest() {
        super.beforeTest();
        FakeKey fakeKey = new FakeKey(KEY_ID, getRandomByteArray(256));

        cc = getServiceClientBuilder(ENV.getPrimaryAccount())
            .buildClient()
            .getBlobContainerClient(generateContainerName());
        cc.create();

        String blobName = generateBlobName();

        beac = getEncryptedClientBuilder(fakeKey, null, ENV.getPrimaryAccount().getCredential(),
            cc.getBlobContainerUrl())
            .blobName(blobName)
            .buildEncryptedBlobAsyncClient();

        bec = getEncryptedClientBuilder(fakeKey, null, ENV.getPrimaryAccount().getCredential(),
            cc.getBlobContainerUrl())
            .blobName(blobName)
            .buildEncryptedBlobClient();
    }

    @EnabledIf("com.azure.storage.blob.specialized.cryptography.BlobCryptographyTestBase#liveOnly")
    @Test
    public void encryptedBlobOutputStreamNotANoop() throws IOException {
        byte[] data = getRandomByteArray(10 * Constants.MB);
        ByteArrayOutputStream os = new ByteArrayOutputStream(10 * Constants.MB);

        try (OutputStream outputStream = bec.getBlobOutputStream()) {
            outputStream.write(data);
        }

        cc.getBlobClient(bec.getBlobName()).download(os);

        assertFalse(Arrays.equals(data, os.toByteArray()));
    }

    @EnabledIf("com.azure.storage.blob.specialized.cryptography.BlobCryptographyTestBase#liveOnly")
    @Test
    public void encryptedBlobOutputStream() throws IOException {
        byte[] data = getRandomByteArray(10 * Constants.MB);

        try (OutputStream outputStream = bec.getBlobOutputStream()) {
            outputStream.write(data);
        }

        assertArrayEquals(data, convertInputStreamToByteArray(bec.openInputStream(), 10 * Constants.MB));
    }

    @EnabledIf("com.azure.storage.blob.specialized.cryptography.BlobCryptographyTestBase#liveOnly")
    @Test
    public void encryptedBlobOutputStreamDefaultNoOverwrite() throws IOException {
        byte[] data = getRandomByteArray(10 * Constants.MB);

        try (OutputStream outputStream1 = bec.getBlobOutputStream()) {
            outputStream1.write(data);
        }

        assertThrows(IllegalArgumentException.class, bec::getBlobOutputStream);
    }

    @EnabledIf("com.azure.storage.blob.specialized.cryptography.BlobCryptographyTestBase#liveOnly")
    @Test
    public void encryptedBlobOutputStreamDefaultNoOverwriteInterrupted() throws IOException {
        byte[] data = getRandomByteArray(10 * Constants.MB);

        OutputStream outputStream1 = bec.getBlobOutputStream();
        try (OutputStream outputStream2 = bec.getBlobOutputStream()) {
            outputStream2.write(data);
        }

        IOException e = assertThrows(IOException.class, () -> {
            outputStream1.write(data);
            outputStream1.close();
        });
        assertInstanceOf(BlobStorageException.class, e.getCause());
        assertEquals(BlobErrorCode.BLOB_ALREADY_EXISTS, ((BlobStorageException) e.getCause()).getErrorCode());
    }

    @EnabledIf("com.azure.storage.blob.specialized.cryptography.BlobCryptographyTestBase#liveOnly")
    @Test
    public void encryptedBlobOutputStreamOverwrite() throws IOException {
        byte[] randomData = getRandomByteArray(10 * Constants.MB);
        beac.upload(DATA.getDefaultFlux(), null).block();

        try (OutputStream outputStream = bec.getBlobOutputStream(true)) {
            outputStream.write(randomData);
        }

        assertArrayEquals(randomData, convertInputStreamToByteArray(bec.openInputStream(), 10 * Constants.MB));
    }

    static byte[] convertInputStreamToByteArray(InputStream inputStream, int sizeHint) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(sizeHint);
        byte[] buffer = new byte[8192];
        int count;

        while ((count = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, count);
        }

        return outputStream.toByteArray();
    }
}
