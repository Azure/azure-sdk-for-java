// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake;

import com.azure.storage.file.datalake.models.CustomerProvidedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CpkAsyncTests extends DataLakeTestBase {
    private CustomerProvidedKey key;
    private DataLakeFileAsyncClient cpkFile;
    private DataLakeDirectoryAsyncClient cpkDirectory;
    @BeforeEach
    public void setup() {
        key = new CustomerProvidedKey(getRandomKey());
        DataLakeFileSystemClientBuilder builder = instrument(new DataLakeFileSystemClientBuilder())
            .endpoint(dataLakeFileSystemAsyncClient.getFileSystemUrl())
            .customerProvidedKey(key)
            .credential(ENVIRONMENT.getDataLakeAccount().getCredential());

        DataLakeFileSystemAsyncClient cpkFileSystem = builder.buildAsyncClient();
        cpkDirectory = cpkFileSystem.getDirectoryAsyncClient(generatePathName());
        cpkFile = cpkFileSystem.getFileAsyncClient(generatePathName());
    }

    /**
     * Insecurely and quickly generates a random AES256 key for the purpose of unit tests. No one should ever make a
     * real key this way.
     */
    private byte[] getRandomKey() {
        return getRandomByteArray(32);
    }

    @Test
    public void pathCreate() {
        StepVerifier.create(cpkDirectory.createWithResponse(null, null, null, null,
            null))
            .assertNext(r -> {
                assertEquals(201, r.getStatusCode());
                assertTrue(r.getValue().isServerEncrypted());
                assertEquals(key.getKeySha256(), r.getValue().getEncryptionKeySha256());
            })
            .verifyComplete();
    }

    @Test
    public void pathGetProperties() {
        cpkFile.create().block();

        StepVerifier.create(cpkFile.getPropertiesWithResponse(null))
            .assertNext(r -> {
                assertEquals(200, r.getStatusCode());
                assertTrue(r.getValue().isServerEncrypted());
                assertEquals(key.getKeySha256(), r.getValue().getEncryptionKeySha256());
            })
            .verifyComplete();
    }

    @Test
    public void pathSetMetadata() {
        Map<String, String> metadata = Collections.singletonMap("foo", "bar");
        cpkFile.create().block();

        StepVerifier.create(cpkFile.setMetadataWithResponse(metadata, null))
            .assertNext(r -> {
                assertEquals(200, r.getStatusCode());
                assertTrue(Boolean.parseBoolean(r.getHeaders().getValue(X_MS_REQUEST_SERVER_ENCRYPTED)));
                assertEquals(key.getKeySha256(), r.getHeaders().getValue(X_MS_ENCRYPTION_KEY_SHA256));
            })
            .verifyComplete();
    }

    @Test
    public void fileRead() {
        cpkFile.create().block();

        StepVerifier.create(cpkFile.readWithResponse(null, null, null, false))
            .expectNextCount(1)
            .verifyComplete();
    }

    @Test
    public void fileAppend() {
        cpkFile.create().block();

        StepVerifier.create(cpkFile.appendWithResponse(DATA.getDefaultFlux(), 0L, DATA.getDefaultDataSizeLong(),
            (byte[]) null, null))
            .assertNext(r -> {
                assertTrue(Boolean.parseBoolean(r.getHeaders().getValue(X_MS_REQUEST_SERVER_ENCRYPTED)));
                assertEquals(key.getKeySha256(), r.getHeaders().getValue(X_MS_ENCRYPTION_KEY_SHA256));
            })
            .verifyComplete();
    }

    @Test
    public void fileFlush() {
        cpkFile.create().block();
        cpkFile.append(DATA.getDefaultBinaryData(), 0).block();

        StepVerifier.create(cpkFile.flushWithResponse(DATA.getDefaultDataSizeLong(), true, true,
            null, null))
            .assertNext(r -> {
                assertTrue(r.getValue().isServerEncrypted());
                assertEquals(key.getKeySha256(), r.getValue().getEncryptionKeySha256());
            })
            .verifyComplete();
    }

    @Test
    public void directoryCreateSupDir() {
        cpkDirectory.create().block();

        StepVerifier.create(cpkDirectory.createSubdirectoryWithResponse(generatePathName(), null, null,
            null, null, null))
            .assertNext(r -> {
                assertEquals(key.getKeySha256(), r.getValue().getCustomerProvidedKey().getKeySha256());
                assertTrue(Boolean.parseBoolean(r.getHeaders().getValue(X_MS_REQUEST_SERVER_ENCRYPTED)));
                assertEquals(key.getKeySha256(), r.getHeaders().getValue(X_MS_ENCRYPTION_KEY_SHA256));
            })
            .verifyComplete();
    }

    @Test
    public void getCustomerProvidedKeyClient() {
        CustomerProvidedKey newCpk = new CustomerProvidedKey(getRandomKey());

        DataLakeFileAsyncClient newCpkFileClient = cpkFile.getCustomerProvidedKeyAsyncClient(newCpk);

        assertNotEquals(cpkFile.getCustomerProvidedKey(), newCpkFileClient.getCustomerProvidedKey());

        DataLakeDirectoryAsyncClient newCpkDirectoryClient = cpkDirectory.getCustomerProvidedKeyAsyncClient(newCpk);

        assertNotEquals(cpkDirectory.getCustomerProvidedKey(), newCpkDirectoryClient.getCustomerProvidedKey());

        DataLakePathAsyncClient newCpkPathClient = ((DataLakePathAsyncClient) cpkFile).getCustomerProvidedKeyAsyncClient(newCpk);

        assertNotEquals(cpkFile.getCustomerProvidedKey(), newCpkPathClient.getCustomerProvidedKey());
    }

    @Test
    public void existsWithoutCpk() {
        cpkFile.create().block();

        StepVerifier.create(cpkFile.getCustomerProvidedKeyAsyncClient(null).exists())
            .expectNext(true)
            .verifyComplete();
    }
}
