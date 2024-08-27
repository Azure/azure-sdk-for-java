// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake;

import com.azure.core.http.rest.Response;
import com.azure.core.test.annotation.LiveOnly;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.file.datalake.models.CustomerProvidedKey;
import com.azure.storage.file.datalake.models.FileReadAsyncResponse;
import com.azure.storage.file.datalake.models.PathInfo;
import com.azure.storage.file.datalake.models.PathProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@LiveOnly
public class CpkAsyncTests extends DataLakeTestBase {
    // LiveOnly because "x-ms-encryption-key-sha256 should not be stored in recordings"
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
        Mono<Response<PathProperties>> response = cpkFile.create()
            .then(cpkFile.getPropertiesWithResponse(null));

        StepVerifier.create(response)
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

        Mono<Response<Void>> response = cpkFile.create()
            .then(cpkFile.setMetadataWithResponse(metadata, null));

        StepVerifier.create(response)
            .assertNext(r -> {
                assertEquals(200, r.getStatusCode());
                assertTrue(Boolean.parseBoolean(r.getHeaders().getValue(X_MS_REQUEST_SERVER_ENCRYPTED)));
                assertEquals(key.getKeySha256(),
                    r.getHeaders().getValue(Constants.HeaderConstants.ENCRYPTION_KEY_SHA256_HEADER_NAME));
            })
            .verifyComplete();
    }

    @Test
    public void fileRead() {
        Mono<FileReadAsyncResponse> response = cpkFile.create()
            .then(cpkFile.readWithResponse(null, null, null, false));

        StepVerifier.create(response)
            .expectNextCount(1)
            .verifyComplete();
    }

    @Test
    public void fileAppend() {
        Mono<Response<Void>> response = cpkFile.create()
            .then(cpkFile.appendWithResponse(DATA.getDefaultFlux(), 0L, DATA.getDefaultDataSizeLong(),
                (byte[]) null, null));

        StepVerifier.create(response)
            .assertNext(r -> {
                assertTrue(Boolean.parseBoolean(r.getHeaders().getValue(X_MS_REQUEST_SERVER_ENCRYPTED)));
                assertEquals(key.getKeySha256(),
                    r.getHeaders().getValue(Constants.HeaderConstants.ENCRYPTION_KEY_SHA256_HEADER_NAME));
            })
            .verifyComplete();
    }

    @Test
    public void fileFlush() {
        Mono<Response<PathInfo>> response = cpkFile.create()
            .then(cpkFile.append(DATA.getDefaultBinaryData(), 0))
            .then(cpkFile.flushWithResponse(DATA.getDefaultDataSizeLong(), true, true,
                null, null));

        StepVerifier.create(response)
            .assertNext(r -> {
                assertTrue(r.getValue().isServerEncrypted());
                assertEquals(key.getKeySha256(), r.getValue().getEncryptionKeySha256());
            })
            .verifyComplete();
    }

    @Test
    public void directoryCreateSupDir() {
        Mono<Response<DataLakeDirectoryAsyncClient>> response = cpkDirectory.create()
            .then(cpkDirectory.createSubdirectoryWithResponse(generatePathName(), null, null,
                null, null, null));

        StepVerifier.create(response)
            .assertNext(r -> {
                assertEquals(key.getKeySha256(), r.getValue().getCustomerProvidedKey().getKeySha256());
                assertTrue(Boolean.parseBoolean(r.getHeaders().getValue(X_MS_REQUEST_SERVER_ENCRYPTED)));
                assertEquals(key.getKeySha256(),
                    r.getHeaders().getValue(Constants.HeaderConstants.ENCRYPTION_KEY_SHA256_HEADER_NAME));
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
        Mono<Boolean> response = cpkFile.create()
            .then(cpkFile.getCustomerProvidedKeyAsyncClient(null).exists());

        StepVerifier.create(response)
            .expectNext(true)
            .verifyComplete();
    }
}
