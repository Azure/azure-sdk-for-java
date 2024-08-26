// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.file.datalake;

import com.azure.core.http.rest.Response;
import com.azure.core.test.annotation.LiveOnly;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.file.datalake.models.CustomerProvidedKey;
import com.azure.storage.file.datalake.models.PathInfo;
import com.azure.storage.file.datalake.models.PathProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@LiveOnly
public class CpkTests extends DataLakeTestBase {
    // LiveOnly because "x-ms-encryption-key-sha256 should not be stored in recordings"
    private CustomerProvidedKey key;
    private DataLakeFileClient cpkFile;
    private DataLakeDirectoryClient cpkDirectory;

    @BeforeEach
    public void setup() {
        key = new CustomerProvidedKey(getRandomKey());
        DataLakeFileSystemClientBuilder builder = instrument(new DataLakeFileSystemClientBuilder())
            .endpoint(dataLakeFileSystemClient.getFileSystemUrl())
            .customerProvidedKey(key)
            .credential(ENVIRONMENT.getDataLakeAccount().getCredential());

        DataLakeFileSystemClient cpkFileSystem = builder.buildClient();
        cpkDirectory = cpkFileSystem.getDirectoryClient(generatePathName());
        cpkFile = cpkFileSystem.getFileClient(generatePathName());
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
        Response<PathInfo> response = cpkDirectory.createWithResponse(null, null, null, null, null, null, null);

        assertEquals(201, response.getStatusCode());
        assertTrue(response.getValue().isServerEncrypted());
        assertEquals(key.getKeySha256(), response.getValue().getEncryptionKeySha256());
    }

    @Test
    public void pathGetProperties() {
        cpkFile.create();

        Response<PathProperties> response = cpkFile.getPropertiesWithResponse(null, null, null);

        assertEquals(200, response.getStatusCode());
        assertTrue(response.getValue().isServerEncrypted());
        assertEquals(key.getKeySha256(), response.getValue().getEncryptionKeySha256());
    }

    @Test
    public void pathSetMetadata() {
        Map<String, String> metadata = Collections.singletonMap("foo", "bar");
        cpkFile.create();

        Response<Void> response = cpkFile.setMetadataWithResponse(metadata, null, null, null);

        assertEquals(200, response.getStatusCode());
        assertTrue(Boolean.parseBoolean(response.getHeaders().getValue(X_MS_REQUEST_SERVER_ENCRYPTED)));
        assertEquals(key.getKeySha256(),
            response.getHeaders().getValue(Constants.HeaderConstants.ENCRYPTION_KEY_SHA256_HEADER_NAME));
    }

    @Test
    public void fileRead() {
        cpkFile.create();

        assertDoesNotThrow(() ->
            cpkFile.readWithResponse(new ByteArrayOutputStream(), null, null, null, false, null, null));
    }

    @Test
    public void fileAppend() {
        cpkFile.create();

        Response<Void> response = cpkFile.appendWithResponse(DATA.getDefaultInputStream(), 0,
            DATA.getDefaultDataSizeLong(), null, null, null, null);

        assertTrue(Boolean.parseBoolean(response.getHeaders().getValue(X_MS_REQUEST_SERVER_ENCRYPTED)));
        assertEquals(key.getKeySha256(),
            response.getHeaders().getValue(Constants.HeaderConstants.ENCRYPTION_KEY_SHA256_HEADER_NAME));
    }

    @Test
    public void fileFlush() {
        cpkFile.create();
        cpkFile.append(DATA.getDefaultBinaryData(), 0);

        Response<PathInfo> response = cpkFile.flushWithResponse(DATA.getDefaultDataSizeLong(), true, true, null, null,
            null, null);

        assertTrue(response.getValue().isServerEncrypted());
        assertEquals(key.getKeySha256(), response.getValue().getEncryptionKeySha256());
    }

    @Test
    public void directoryCreateSupDir() {
        cpkDirectory.create();

        Response<DataLakeDirectoryClient> response = cpkDirectory.createSubdirectoryWithResponse(generatePathName(),
            null, null, null, null, null, null, null);

        assertEquals(key.getKeySha256(), response.getValue().getCustomerProvidedKey().getKeySha256());
        assertTrue(Boolean.parseBoolean(response.getHeaders().getValue(X_MS_REQUEST_SERVER_ENCRYPTED)));
        assertEquals(key.getKeySha256(),
            response.getHeaders().getValue(Constants.HeaderConstants.ENCRYPTION_KEY_SHA256_HEADER_NAME));
    }

    @Test
    public void getCustomerProvidedKeyClient() {
        CustomerProvidedKey newCpk = new CustomerProvidedKey(getRandomKey());

        DataLakeFileClient newCpkFileClient = cpkFile.getCustomerProvidedKeyClient(newCpk);

        assertNotEquals(cpkFile.getCustomerProvidedKey(), newCpkFileClient.getCustomerProvidedKey());

        DataLakeDirectoryClient newCpkDirectoryClient = cpkDirectory.getCustomerProvidedKeyClient(newCpk);

        assertNotEquals(cpkDirectory.getCustomerProvidedKey(), newCpkDirectoryClient.getCustomerProvidedKey());

        DataLakePathClient newCpkPathClient = ((DataLakePathClient) cpkFile).getCustomerProvidedKeyClient(newCpk);

        assertNotEquals(cpkFile.getCustomerProvidedKey(), newCpkPathClient.getCustomerProvidedKey());
    }

    @Test
    public void existsWithoutCpk() {
        cpkFile.create();

        assertTrue(cpkFile.getCustomerProvidedKeyClient(null).exists());
    }
}
