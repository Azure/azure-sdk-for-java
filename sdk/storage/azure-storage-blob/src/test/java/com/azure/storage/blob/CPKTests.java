// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.http.rest.Response;
import com.azure.core.test.annotation.LiveOnly;
import com.azure.core.test.utils.TestUtils;
import com.azure.storage.blob.models.AppendBlobItem;
import com.azure.storage.blob.models.BlobDownloadResponse;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.models.BlockBlobItem;
import com.azure.storage.blob.models.CustomerProvidedKey;
import com.azure.storage.blob.models.PageBlobItem;
import com.azure.storage.blob.models.PageRange;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.azure.storage.blob.specialized.AppendBlobClient;
import com.azure.storage.blob.specialized.BlobClientBase;
import com.azure.storage.blob.specialized.BlockBlobClient;
import com.azure.storage.blob.specialized.PageBlobClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@LiveOnly
public class CPKTests extends BlobTestBase {
    // LiveOnly because "x-ms-encryption-key-sha256 cannot be stored in recordings"
    private CustomerProvidedKey key;
    private BlobContainerClient cpkContainer;
    private BlockBlobClient cpkBlockBlob;
    private PageBlobClient cpkPageBlob;
    private AppendBlobClient cpkAppendBlob;
    private BlobClientBase cpkExistingBlob;

    @BeforeEach
    public void setup() {
        key = new CustomerProvidedKey(getRandomKey());
        BlobContainerClientBuilder builder = instrument(new BlobContainerClientBuilder()
            .endpoint(cc.getBlobContainerUrl())
            .customerProvidedKey(key)
            .credential(ENVIRONMENT.getPrimaryAccount().getCredential()));

        cpkContainer = builder.buildClient();
        cpkBlockBlob = cpkContainer.getBlobClient(generateBlobName()).getBlockBlobClient();
        cpkPageBlob = cpkContainer.getBlobClient(generateBlobName()).getPageBlobClient();
        cpkAppendBlob = cpkContainer.getBlobClient(generateBlobName()).getAppendBlobClient();

        BlockBlobClient existingBlobSetup = cpkContainer.getBlobClient(generateBlobName()).getBlockBlobClient();
        existingBlobSetup.upload(DATA.getDefaultInputStream(), DATA.getDefaultDataSize());
        cpkExistingBlob = existingBlobSetup;

    }

    @Test
    public void putBlobWithCPK() {
        Response<BlockBlobItem> response = cpkBlockBlob.uploadWithResponse(DATA.getDefaultInputStream(),
            DATA.getDefaultDataSize(), null, null, null, null, null, null, null);
        assertResponseStatusCode(response, 201);
        assertTrue(response.getValue().isServerEncrypted());
        assertEquals(key.getKeySha256(), response.getValue().getEncryptionKeySha256());
    }

    @SuppressWarnings("deprecation")
    @Test
    public void getBlobWithCPK() {
        cpkBlockBlob.upload(DATA.getDefaultInputStream(), DATA.getDefaultDataSize());
        ByteArrayOutputStream dataStream = new ByteArrayOutputStream();

        BlobDownloadResponse response = cpkBlockBlob.downloadWithResponse(dataStream, null, null, null, false, null,
            null);

        assertResponseStatusCode(response, 200);
        TestUtils.assertArraysEqual(dataStream.toByteArray(), DATA.getDefaultBytes());
    }

    @Test
    public void putBlockWithCPK() {
        Response<Void> response = cpkBlockBlob.stageBlockWithResponse(getBlockID(), DATA.getDefaultInputStream(),
            DATA.getDefaultDataSize(), null, null, null, null);

        assertResponseStatusCode(response, 201);
        assertTrue(Boolean.parseBoolean(response.getHeaders().getValue(X_MS_REQUEST_SERVER_ENCRYPTED)));
    }

    @Test
    public void putBlockFromURLWithCPK() {
        String blobName = generateBlobName();
        BlockBlobClient sourceBlob = cc.getBlobClient(blobName).getBlockBlobClient();
        sourceBlob.upload(DATA.getDefaultInputStream(), DATA.getDefaultDataSize());
        String sas = cc.generateSas(new BlobServiceSasSignatureValues(testResourceNamer.now().plusHours(1),
            new BlobSasPermission().setReadPermission(true)));

        Response<Void> response = cpkBlockBlob.stageBlockFromUrlWithResponse(getBlockID(),
            sourceBlob.getBlobUrl() + "?" + sas, null, null, null, null, null, null);

        assertResponseStatusCode(response, 201);
        assertTrue(Boolean.parseBoolean(response.getHeaders().getValue(X_MS_REQUEST_SERVER_ENCRYPTED)));
    }

    @Test
    public void putBlockListWithCPK() {
        List<String> blockIDList = Arrays.asList(getBlockID(), getBlockID());
        for (String blockId : blockIDList) {
            cpkBlockBlob.stageBlock(blockId, DATA.getDefaultInputStream(), DATA.getDefaultDataSize());
        }

        Response<BlockBlobItem> response = cpkBlockBlob.commitBlockListWithResponse(blockIDList, null, null, null, null,
            null, null);

        assertResponseStatusCode(response, 201);
        assertTrue(response.getValue().isServerEncrypted());
        assertEquals(key.getKeySha256(), response.getValue().getEncryptionKeySha256());
    }

    @Test
    public void putPageWithCPK() {
        cpkPageBlob.create(PageBlobClient.PAGE_BYTES);

        Response<PageBlobItem> response = cpkPageBlob.uploadPagesWithResponse(
            new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1),
            new ByteArrayInputStream(getRandomByteArray(PageBlobClient.PAGE_BYTES)), null, null, null, null);

        assertResponseStatusCode(response, 201);
        assertTrue(response.getValue().isServerEncrypted());
        assertEquals(key.getKeySha256(), response.getValue().getEncryptionKeySha256());
    }

    @Test
    public void putPageFromURLWithCPK() {
        String blobName = generateBlobName();
        PageBlobClient sourceBlob = cc.getBlobClient(blobName).getPageBlobClient();
        sourceBlob.create(PageBlobClient.PAGE_BYTES);
        sourceBlob.uploadPagesWithResponse(new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1),
            new ByteArrayInputStream(getRandomByteArray(PageBlobClient.PAGE_BYTES)), null, null, null, null);

        cpkPageBlob.create(PageBlobClient.PAGE_BYTES);
        String sas = cc.generateSas(new BlobServiceSasSignatureValues(testResourceNamer.now().plusHours(1),
            new BlobSasPermission().setReadPermission(true)));

        Response<PageBlobItem> response = cpkPageBlob.uploadPagesFromUrlWithResponse(
            new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1),
            sourceBlob.getBlobUrl() + "?" + sas, null, null, null, null, null, null);

        assertResponseStatusCode(response, 201);
        assertTrue(response.getValue().isServerEncrypted());
        assertEquals(key.getKeySha256(), response.getValue().getEncryptionKeySha256());
    }

    @Test
    public void putMultiplePagesWithCPK() {
        cpkPageBlob.create(PageBlobClient.PAGE_BYTES * 2);

        Response<PageBlobItem> response = cpkPageBlob.uploadPagesWithResponse(
            new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES * 2 - 1),
            new ByteArrayInputStream(getRandomByteArray(PageBlobClient.PAGE_BYTES * 2)), null, null, null, null);

        assertResponseStatusCode(response, 201);
        assertTrue(response.getValue().isServerEncrypted());
        assertEquals(key.getKeySha256(), response.getValue().getEncryptionKeySha256());
    }

    @Test
    public void appendBlockWithCPK() {
        cpkAppendBlob.create();

        Response<AppendBlobItem> response = cpkAppendBlob.appendBlockWithResponse(DATA.getDefaultInputStream(),
            DATA.getDefaultDataSize(), null, null, null, null);

        assertResponseStatusCode(response, 201);
        assertTrue(response.getValue().isServerEncrypted());
        assertEquals(key.getKeySha256(), response.getValue().getEncryptionKeySha256());
    }

    @Test
    public void appendBlockFromURLWithCPK() {
        cpkAppendBlob.create();
        String blobName = generateBlobName();
        BlockBlobClient sourceBlob = cc.getBlobClient(blobName).getBlockBlobClient();
        sourceBlob.upload(DATA.getDefaultInputStream(), DATA.getDefaultDataSize());

        String sas = cc.generateSas(new BlobServiceSasSignatureValues(testResourceNamer.now().plusHours(1),
            new BlobSasPermission().setReadPermission(true)));

        Response<AppendBlobItem> response = cpkAppendBlob.appendBlockFromUrlWithResponse(
            sourceBlob.getBlobUrl() + "?" + sas, null, null, null, null, null, null);

        assertResponseStatusCode(response, 201);
        assertTrue(response.getValue().isServerEncrypted());
        assertEquals(key.getKeySha256(), response.getValue().getEncryptionKeySha256());
    }

    @Test
    public void setBlobMetadataWithCPK() {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("foo", "bar");

        Response<Void> response = cpkExistingBlob.setMetadataWithResponse(metadata, null, null, null);

        assertResponseStatusCode(response, 200);
        assertTrue(Boolean.parseBoolean(response.getHeaders().getValue(X_MS_REQUEST_SERVER_ENCRYPTED)));
        assertEquals(key.getKeySha256(), response.getHeaders().getValue(X_MS_ENCRYPTION_KEY_SHA256));
    }

    @Test
    public void getBlobPropertiesAndMetadataWithCPK() {
        Response<BlobProperties> response = cpkExistingBlob.getPropertiesWithResponse(null, null, null);

        assertResponseStatusCode(response, 200);
        assertTrue(Boolean.parseBoolean(response.getHeaders().getValue(X_MS_SERVER_ENCRYPTED)));
        assertEquals(key.getKeySha256(), response.getHeaders().getValue(X_MS_ENCRYPTION_KEY_SHA256));
    }

//    @Test
//    public void setBlobTierWithCPK() {
//        Response<Void> response = cpkExistingBlob.setAccessTierWithResponse(AccessTier.COOL, null, null, null, null);
//
//        assertResponseStatusCode(response, 200);
//        assertTrue(Boolean.parseBoolean(response.getHeaders().getValue(X_MS_SERVER_ENCRYPTED)));
//        assertEquals(key.getKeySha256(), response.getHeaders().getValue(X_MS_ENCRYPTION_KEY_SHA256));
//    }

    @Test
    public void snapshotBlobWithCPK() {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("foo", "bar");
        Response<BlobClientBase> response = cpkExistingBlob.createSnapshotWithResponse(metadata, null, null, null);
        assertResponseStatusCode(response, 201);
    }

    @Test
    public void getCustomerProvidedKeyClient() {
        CustomerProvidedKey newCpk = new CustomerProvidedKey(getRandomKey());

        // when: "AppendBlob"
        AppendBlobClient newCpkAppendBlob = cpkAppendBlob.getCustomerProvidedKeyClient(newCpk);

        assertInstanceOf(AppendBlobClient.class, newCpkAppendBlob);
        assertNotEquals(newCpkAppendBlob.getCustomerProvidedKey(), cpkAppendBlob.getCustomerProvidedKey());

        // when: "BlockBlob"
        BlockBlobClient newCpkBlockBlob = cpkBlockBlob.getCustomerProvidedKeyClient(newCpk);

        assertInstanceOf(BlockBlobClient.class, newCpkBlockBlob);
        assertNotEquals(newCpkBlockBlob.getCustomerProvidedKey(), cpkBlockBlob.getCustomerProvidedKey());

        // when: "PageBlob"
        PageBlobClient newCpkPageBlob = cpkPageBlob.getCustomerProvidedKeyClient(newCpk);

        assertInstanceOf(PageBlobClient.class, newCpkPageBlob);
        assertNotEquals(newCpkPageBlob.getCustomerProvidedKey(), cpkPageBlob.getCustomerProvidedKey());

        // when: "BlobClientBase"
        BlobClientBase newCpkBlobClientBase = cpkExistingBlob.getCustomerProvidedKeyClient(newCpk);

        assertInstanceOf(BlobClientBase.class, newCpkBlobClientBase);
        assertNotEquals(newCpkBlobClientBase.getCustomerProvidedKey(), cpkExistingBlob.getCustomerProvidedKey());

        // when: "BlobClient"
        BlobClient cpkBlobClient = cpkContainer.getBlobClient(generateBlobName()); // Inherits container's CPK
        BlobClient newCpkBlobClient = cpkBlobClient.getCustomerProvidedKeyClient(newCpk);

        assertInstanceOf(BlobClient.class, newCpkBlobClient);
        assertNotEquals(newCpkBlobClient.getCustomerProvidedKey(), cpkBlobClient.getCustomerProvidedKey());
    }

    @Test
    public void existsWithoutCPK() {
        BlobClientBase clientWithoutCpk = cpkExistingBlob.getCustomerProvidedKeyClient(null);
        assertTrue(clientWithoutCpk.exists());
    }
}
