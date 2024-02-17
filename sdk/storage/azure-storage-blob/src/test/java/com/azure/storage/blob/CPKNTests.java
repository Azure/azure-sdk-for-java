// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.rest.Response;
import com.azure.storage.blob.models.AppendBlobItem;
import com.azure.storage.blob.models.BlobContainerEncryptionScope;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.models.BlockBlobItem;
import com.azure.storage.blob.models.CustomerProvidedKey;
import com.azure.storage.blob.models.PageBlobItem;
import com.azure.storage.blob.models.PageRange;
import com.azure.storage.blob.models.PublicAccessType;
import com.azure.storage.blob.options.BlobCopyFromUrlOptions;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.azure.storage.blob.specialized.AppendBlobClient;
import com.azure.storage.blob.specialized.BlobClientBase;
import com.azure.storage.blob.specialized.BlockBlobClient;
import com.azure.storage.blob.specialized.PageBlobClient;
import com.azure.storage.blob.specialized.SpecializedBlobClientBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;

import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class CPKNTests extends BlobTestBase {

    private final String scope1 = "testscope1";
    private final String scope2 = "testscope2";
    private String es;
    private BlobContainerEncryptionScope ces;
    private BlobContainerClientBuilder builder;

    private BlobContainerClient cpknContainer;
    private BlockBlobClient cpknBlockBlob;
    private PageBlobClient cpknPageBlob;
    private AppendBlobClient cpknAppendBlob;

    @BeforeEach
    public void setup() {
        es = scope1;
        ces = new BlobContainerEncryptionScope().setDefaultEncryptionScope(scope2)
            .setEncryptionScopeOverridePrevented(true);

        builder = getContainerClientBuilder(cc.getBlobContainerUrl())
            .credential(ENVIRONMENT.getPrimaryAccount().getCredential());

        cpknContainer = builder.encryptionScope(es).buildClient();

        cpknBlockBlob = cpknContainer.getBlobClient(generateBlobName()).getBlockBlobClient();
        cpknPageBlob = cpknContainer.getBlobClient(generateBlobName()).getPageBlobClient();
        cpknAppendBlob = cpknContainer.getBlobClient(generateBlobName()).getAppendBlobClient();
    }

    @Test
    public void containerCreate() {
        BlobContainerClient cpknCesContainer = builder.blobContainerEncryptionScope(ces).encryptionScope(null)
            .containerName(generateContainerName()).buildClient();
        Response<Void> response = cpknCesContainer.createWithResponse(null, null, null, null);
        assertResponseStatusCode(response, 201);
    }

    @Test
    public void containerDenyEncryptionScopeOverride() {
        BlobContainerClient cpknCesContainer = builder.blobContainerEncryptionScope(ces)
            .containerName(generateContainerName()).buildClient();
        cpknCesContainer.create();

        cpknAppendBlob = builder.encryptionScope(es)
            .containerName(cpknCesContainer.getBlobContainerName())
            .buildClient()
            .getBlobClient(generateBlobName())
            .getAppendBlobClient();

        Assertions.assertThrows(BlobStorageException.class, () -> cpknAppendBlob.create());
    }

    @Test
    public void containerListBlobsFlat() {
        BlobContainerClient cpkncesContainer = builder
            .blobContainerEncryptionScope(ces)
            .encryptionScope(null)
            .containerName(generateContainerName())
            .buildClient();
        cpkncesContainer.create();
        AppendBlobClient cpknAppendBlob = cpkncesContainer.getBlobClient(generateBlobName()).getAppendBlobClient();
        cpknAppendBlob.create();

        Iterator<BlobItem> items = cpkncesContainer.listBlobs().iterator();

        BlobItem blob = items.next();
        Assertions.assertFalse(items.hasNext());
        Assertions.assertEquals(scope2, blob.getProperties().getEncryptionScope());
    }

    @Test
    public void containerListBlobsHierarchical() {
        BlobContainerClient cpkncesContainer = builder
            .blobContainerEncryptionScope(ces)
            .encryptionScope(null)
            .containerName(generateContainerName())
            .buildClient();
        cpkncesContainer.create();
        AppendBlobClient cpknAppendBlob = cpkncesContainer.getBlobClient(generateBlobName()).getAppendBlobClient();
        cpknAppendBlob.create();

        Iterator<BlobItem> items = cpkncesContainer.listBlobsByHierarchy("").iterator();

        BlobItem blob = items.next();
        Assertions.assertFalse(items.hasNext());
        Assertions.assertEquals(scope2, blob.getProperties().getEncryptionScope());
    }

    @Test
    public void appendBlobCreate() {
        Response<AppendBlobItem> response = cpknAppendBlob.createWithResponse(null, null, null, null, null);

        assertResponseStatusCode(response, 201);
        Assertions.assertTrue(response.getValue().isServerEncrypted());
        Assertions.assertEquals(scope1, response.getValue().getEncryptionScope());
    }

    @Test
    public void appendBlobAppendBlock() {
        cpknAppendBlob.create();

        Response<AppendBlobItem> response = cpknAppendBlob.appendBlockWithResponse(DATA.getDefaultInputStream(),
            DATA.getDefaultDataSize(), null, null, null, null);

        assertResponseStatusCode(response, 201);
        Assertions.assertTrue(response.getValue().isServerEncrypted());
        Assertions.assertEquals(scope1, response.getValue().getEncryptionScope());
    }

    @Test
    public void appendBlobAppendBlockFromURL() {
        cpknAppendBlob.create();
        String blobName = generateBlobName();
        BlockBlobClient sourceBlob = cc.getBlobClient(blobName).getBlockBlobClient();
        sourceBlob.upload(DATA.getDefaultInputStream(), DATA.getDefaultDataSize());

        String sas = cc.generateSas(new BlobServiceSasSignatureValues(testResourceNamer.now().plusHours(1),
            new BlobSasPermission().setReadPermission(true)));
        Response<AppendBlobItem> response = cpknAppendBlob.appendBlockFromUrlWithResponse(
            sourceBlob.getBlobUrl() + "?" + sas, null, null, null, null, null, null);

        assertResponseStatusCode(response, 201);
        Assertions.assertTrue(response.getValue().isServerEncrypted());
        Assertions.assertEquals(scope1, response.getValue().getEncryptionScope());
    }

    @Test
    public void pageBlobCreate() {
        Response<PageBlobItem> response = cpknPageBlob.createWithResponse(1024, null, null, null, null, null, null);

        assertResponseStatusCode(response, 201);
        Assertions.assertTrue(response.getValue().isServerEncrypted());
        Assertions.assertEquals(scope1, response.getValue().getEncryptionScope());
    }

    @Test
    public void pageBlobPutPage() {
        cpknPageBlob.create(PageBlobClient.PAGE_BYTES);

        Response<PageBlobItem> response = cpknPageBlob.uploadPagesWithResponse(
            new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1),
            new ByteArrayInputStream(getRandomByteArray(PageBlobClient.PAGE_BYTES)), null, null, null, null);

        assertResponseStatusCode(response, 201);
        Assertions.assertTrue(response.getValue().isServerEncrypted());
        Assertions.assertEquals(scope1, response.getValue().getEncryptionScope());
    }

    @Test
    public void pageBlobPutPageFromURL() {
        String blobName = generateBlobName();
        PageBlobClient sourceBlob = cc.getBlobClient(blobName).getPageBlobClient();
        sourceBlob.create(PageBlobClient.PAGE_BYTES);
        sourceBlob.uploadPagesWithResponse(new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1),
            new ByteArrayInputStream(getRandomByteArray(PageBlobClient.PAGE_BYTES)), null, null, null, null);

        cpknPageBlob.create(PageBlobClient.PAGE_BYTES);
        String sas = cc.generateSas(new BlobServiceSasSignatureValues(testResourceNamer.now().plusHours(1),
            new BlobSasPermission().setReadPermission(true)));

        Response<PageBlobItem> response = cpknPageBlob.uploadPagesFromUrlWithResponse(
            new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1), sourceBlob.getBlobUrl() + "?" + sas,
            null, null, null, null, null, null);

        assertResponseStatusCode(response, 201);
        Assertions.assertTrue(response.getValue().isServerEncrypted());
        Assertions.assertEquals(scope1, response.getValue().getEncryptionScope());
    }

    @Test
    public void pageBlobPutMultiplePages() {
        cpknPageBlob.create(PageBlobClient.PAGE_BYTES * 2);

        Response<PageBlobItem> response = cpknPageBlob.uploadPagesWithResponse(new PageRange().setStart(0)
                .setEnd(PageBlobClient.PAGE_BYTES * 2 - 1),
            new ByteArrayInputStream(getRandomByteArray(PageBlobClient.PAGE_BYTES * 2)), null, null, null, null);

        assertResponseStatusCode(response, 201);
        Assertions.assertTrue(response.getValue().isServerEncrypted());
        Assertions.assertEquals(scope1, response.getValue().getEncryptionScope());
    }

    @Test
    public void pageBlobClearPage() {
        cpknPageBlob.create(PageBlobClient.PAGE_BYTES * 2);
        cpknPageBlob.uploadPagesWithResponse(new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1),
            new ByteArrayInputStream(getRandomByteArray(PageBlobClient.PAGE_BYTES)), null, null, null, null);

        Response<PageBlobItem> response = cpknPageBlob.clearPagesWithResponse(new PageRange().setStart(0)
                .setEnd(PageBlobClient.PAGE_BYTES - 1), null, null, null);

        assertResponseStatusCode(response, 201);
    }

    @Test
    public void pageBlobResize() {
        cpknPageBlob.create(PageBlobClient.PAGE_BYTES * 2);
        Response<PageBlobItem> response = cpknPageBlob.resizeWithResponse(PageBlobClient.PAGE_BYTES * 2, null, null,
            null);

        assertResponseStatusCode(response, 200);
    }

    @Test
    public void blockBlobUpload() {
        Response<BlockBlobItem> response = cpknBlockBlob.uploadWithResponse(DATA.getDefaultInputStream(),
            DATA.getDefaultDataSize(), null, null, null, null, null, null, null);

        assertResponseStatusCode(response, 201);
        Assertions.assertTrue(response.getValue().isServerEncrypted());
        Assertions.assertEquals(scope1, response.getValue().getEncryptionScope());
    }

    @Test
    public void blockBlobStageBlock() {
        cpknBlockBlob.upload(DATA.getDefaultInputStream(), DATA.getDefaultDataSize());
        Response<Void> response = cpknBlockBlob.stageBlockWithResponse(getBlockID(), DATA.getDefaultInputStream(),
            DATA.getDefaultDataSize(), null, null, null, null);
        HttpHeaders headers = response.getHeaders();

        assertResponseStatusCode(response, 201);
        Assertions.assertTrue(Boolean.parseBoolean(headers.getValue(X_MS_REQUEST_SERVER_ENCRYPTED)));
        Assertions.assertEquals(scope1, headers.getValue(X_MS_ENCRYPTION_SCOPE));
    }

    @Test
    public void blockBlobCommitBlockList() {
        String blockID = getBlockID();
        cpknBlockBlob.stageBlock(blockID, DATA.getDefaultInputStream(), DATA.getDefaultDataSize());
        List<String> ids = Collections.singletonList(blockID);

        Response<BlockBlobItem> response = cpknBlockBlob.commitBlockListWithResponse(ids, null, null, null, null, null,
            null);

        assertResponseStatusCode(response, 201);
        Assertions.assertTrue(response.getValue().isServerEncrypted());
        Assertions.assertEquals(scope1, response.getValue().getEncryptionScope());
    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20201206ServiceVersion")
    @Test
    public void syncCopyEncryptionScope() {
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null);
        BlobClient blobSource = cc.getBlobClient(generateBlobName());
        blobSource.upload(DATA.getDefaultBinaryData());

        cpknBlockBlob.copyFromUrlWithResponse(new BlobCopyFromUrlOptions(blobSource.getBlobUrl()), null, null);

        Assertions.assertEquals(scope1, cpknBlockBlob.getProperties().getEncryptionScope());
    }

    @Test
    public void serviceClientBuilderCheck() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new BlobServiceClientBuilder()
            .encryptionScope(es)
            .customerProvidedKey(new CustomerProvidedKey(getRandomKey()))
            .buildClient());
    }

    @Test
    public void containerClientBuilderCheck() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new BlobContainerClientBuilder()
            .encryptionScope(es)
            .customerProvidedKey(new CustomerProvidedKey(getRandomKey()))
            .buildClient());
    }

    @Test
    public void blobClientBuilderCheck() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new BlobClientBuilder()
            .encryptionScope(es)
            .customerProvidedKey(new CustomerProvidedKey(getRandomKey()))
            .endpoint(cc.getBlobContainerUrl())
            .blobName(generateBlobName())
            .buildClient());
    }

    @Test
    public void appendBlobClientBuilderCheck() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new SpecializedBlobClientBuilder()
            .encryptionScope(es)
            .customerProvidedKey(new CustomerProvidedKey(getRandomKey()))
            .endpoint(cc.getBlobContainerUrl())
            .blobName(generateBlobName())
            .buildAppendBlobClient());
    }

    @Test
    public void blockBlobClientBuilderCheck() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new SpecializedBlobClientBuilder()
            .encryptionScope(es)
            .customerProvidedKey(new CustomerProvidedKey(getRandomKey()))
            .endpoint(cc.getBlobContainerUrl())
            .blobName(generateBlobName())
            .buildBlockBlobClient());
    }

    @Test
    public void pageBlobClientBuilderCheck() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new SpecializedBlobClientBuilder()
            .encryptionScope(es)
            .customerProvidedKey(new CustomerProvidedKey(getRandomKey()))
            .endpoint(cc.getBlobContainerUrl())
            .blobName(generateBlobName())
            .buildPageBlobClient());
    }

    @Test
    public void getEncryptionScopeClient() {
        String newEncryptionScope = "newtestscope";

        // when: "AppendBlob"
        AppendBlobClient newCpknAppendBlob = cpknAppendBlob.getEncryptionScopeClient(newEncryptionScope);
        Assertions.assertInstanceOf(AppendBlobClient.class, newCpknAppendBlob);
        Assertions.assertNotEquals(cpknAppendBlob.getEncryptionScope(), newCpknAppendBlob.getEncryptionScope());

        // when: "BlockBlob"
        BlockBlobClient newCpknBlockBlob = cpknBlockBlob.getEncryptionScopeClient(newEncryptionScope);
        Assertions.assertInstanceOf(BlockBlobClient.class, newCpknBlockBlob);
        Assertions.assertNotEquals(cpknBlockBlob.getEncryptionScope(), newCpknBlockBlob.getEncryptionScope());

        // when: "PageBlob"
        PageBlobClient newCpknPageBlob = cpknPageBlob.getEncryptionScopeClient(newEncryptionScope);
        Assertions.assertInstanceOf(PageBlobClient.class, newCpknPageBlob);
        Assertions.assertNotEquals(cpknPageBlob.getEncryptionScope(), newCpknPageBlob.getEncryptionScope());

        // when: "BlobClient"
        BlobClient cpkBlobClient = cpknContainer.getBlobClient(generateBlobName()); // Inherits container's CPK
        BlobClient newCpknBlobClient = cpkBlobClient.getEncryptionScopeClient(newEncryptionScope);
        Assertions.assertInstanceOf(BlobClient.class, newCpknBlobClient);
        Assertions.assertNotEquals(cpkBlobClient.getEncryptionScope(), newCpknBlobClient.getEncryptionScope());

        // when: "BlobClientBase"
        BlobClientBase newCpknBlobClientBase = ((BlobClientBase) cpkBlobClient)
            .getEncryptionScopeClient(newEncryptionScope);
        Assertions.assertInstanceOf(BlobClientBase.class, newCpknBlobClientBase);
        Assertions.assertNotEquals(cpkBlobClient.getEncryptionScope(), newCpknBlobClientBase.getEncryptionScope());
    }

}
