package com.azure.storage.blob;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.rest.Response;
import com.azure.storage.blob.models.*;
import com.azure.storage.blob.options.BlobCopyFromUrlOptions;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.azure.storage.blob.specialized.*;
import com.azure.storage.common.test.shared.extensions.RequiredServiceVersion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;

import java.io.ByteArrayInputStream;
import java.time.OffsetDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

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

        assertThrows(BlobStorageException.class, () -> cpknAppendBlob.create());
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
        assertFalse(items.hasNext());
        assertEquals(scope2, blob.getProperties().getEncryptionScope());
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
        assertFalse(items.hasNext());
        assertEquals(scope2, blob.getProperties().getEncryptionScope());
    }

    @Test
    public void appendBlobCreate() {
        Response<AppendBlobItem> response = cpknAppendBlob.createWithResponse(null, null, null, null, null);

        assertResponseStatusCode(response, 201);
        assertTrue(response.getValue().isServerEncrypted());
        assertEquals(scope1, response.getValue().getEncryptionScope());
    }

    @Test
    public void appendBlobAppendBlock() {
        cpknAppendBlob.create();

        Response<AppendBlobItem> response = cpknAppendBlob.appendBlockWithResponse(DATA.getDefaultInputStream(),
            DATA.getDefaultDataSize(), null, null, null, null);

        assertResponseStatusCode(response, 201);
        assertTrue(response.getValue().isServerEncrypted());
        assertEquals(scope1, response.getValue().getEncryptionScope());
    }

    @Test
    public void appendBlobAppendBlockFromURL() {
        cpknAppendBlob.create();
        String blobName = generateBlobName();
        BlockBlobClient sourceBlob = cc.getBlobClient(blobName).getBlockBlobClient();
        sourceBlob.upload(DATA.getDefaultInputStream(), DATA.getDefaultDataSize());

        String sas2 = new BlobServiceSasSignatureValues()
            .setExpiryTime(OffsetDateTime.now().plusHours(1))
            .setPermissions(new BlobSasPermission().setReadPermission(true))
            .setContainerName(cc.getBlobContainerName())
            .setBlobName(blobName)
            .generateSasQueryParameters(ENVIRONMENT.getPrimaryAccount().getCredential())
            .encode();

        String sas = cc.generateSas(new BlobServiceSasSignatureValues(
            OffsetDateTime.now().plusHours(1),
            new BlobSasPermission().setReadPermission(true)))
            ;
        Response<AppendBlobItem> response = cpknAppendBlob.appendBlockFromUrlWithResponse(
            sourceBlob.getBlobUrl() + "?" + sas, null, null, null, null, null, null);

        assertResponseStatusCode(response, 201);
        assertTrue(response.getValue().isServerEncrypted());
        assertEquals(scope1, response.getValue().getEncryptionScope());
    }

    @Test
    public void pageBlobCreate() {
        Response<PageBlobItem> response = cpknPageBlob.createWithResponse(1024, null, null, null, null, null, null);

        assertResponseStatusCode(response, 201);
        assertTrue(response.getValue().isServerEncrypted());
        assertEquals(scope1, response.getValue().getEncryptionScope());
    }

    @Test
    public void pageBlobPutPage() {
        cpknPageBlob.create(PageBlobClient.PAGE_BYTES);

        Response<PageBlobItem> response = cpknPageBlob.uploadPagesWithResponse(
            new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1),
            new ByteArrayInputStream(getRandomByteArray(PageBlobClient.PAGE_BYTES)),
            null, null, null, null);

        assertResponseStatusCode(response, 201);
        assertTrue(response.getValue().isServerEncrypted());
        assertEquals(scope1, response.getValue().getEncryptionScope());
    }

    @Test
    public void pageBlobPutPageFromURL() {
        String blobName = generateBlobName();
        PageBlobClient sourceBlob = cc.getBlobClient(blobName).getPageBlobClient();
        sourceBlob.create(PageBlobClient.PAGE_BYTES);
        sourceBlob.uploadPagesWithResponse(new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1),
            new ByteArrayInputStream(getRandomByteArray(PageBlobClient.PAGE_BYTES)), null, null, null, null);

        cpknPageBlob.create(PageBlobClient.PAGE_BYTES);
        String sas = cc.generateSas(new BlobServiceSasSignatureValues(
            OffsetDateTime.now().plusHours(1),
            new BlobSasPermission().setReadPermission(true)));

        Response<PageBlobItem> response = cpknPageBlob.uploadPagesFromUrlWithResponse(
            new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1), sourceBlob.getBlobUrl() + "?" + sas,
            null, null, null, null, null, null);

        assertResponseStatusCode(response, 201);
        assertTrue(response.getValue().isServerEncrypted());
        assertEquals(scope1, response.getValue().getEncryptionScope());
    }

    @Test
    public void pageBlobPutMultiplePages() {
        cpknPageBlob.create(PageBlobClient.PAGE_BYTES * 2);

        Response<PageBlobItem> response = cpknPageBlob.uploadPagesWithResponse(new PageRange().setStart(0)
                .setEnd(PageBlobClient.PAGE_BYTES * 2 - 1),
            new ByteArrayInputStream(getRandomByteArray(PageBlobClient.PAGE_BYTES * 2)), null,
            null, null, null);

        assertResponseStatusCode(response, 201);
        assertTrue(response.getValue().isServerEncrypted());
        assertEquals(scope1, response.getValue().getEncryptionScope());
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
        assertTrue(response.getValue().isServerEncrypted());
        assertEquals(scope1, response.getValue().getEncryptionScope());
    }

    @Test
    public void blockBlobStageBlock() {
        cpknBlockBlob.upload(DATA.getDefaultInputStream(), DATA.getDefaultDataSize());
        Response<Void> response = cpknBlockBlob.stageBlockWithResponse(getBlockID(), DATA.getDefaultInputStream(),
            DATA.getDefaultDataSize(), null, null, null, null);
        HttpHeaders headers = response.getHeaders();

        assertResponseStatusCode(response, 201);
        assertTrue(Boolean.parseBoolean(headers.getValue(X_MS_REQUEST_SERVER_ENCRYPTED)));
        assertEquals(scope1, headers.getValue(X_MS_ENCRYPTION_SCOPE));
    }

    @Test
    public void blockBlobCommitBlockList() {
        String blockID = getBlockID();
        cpknBlockBlob.stageBlock(blockID, DATA.getDefaultInputStream(), DATA.getDefaultDataSize());
        List<String> ids = Collections.singletonList(blockID);

        Response<BlockBlobItem> response = cpknBlockBlob.commitBlockListWithResponse(ids, null, null, null, null, null,
            null);

        assertResponseStatusCode(response, 201);
        assertTrue(response.getValue().isServerEncrypted());
        assertEquals(scope1, response.getValue().getEncryptionScope());
    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20201206ServiceVersion")
    @Test
    public void syncCopyEncryptionScope() {
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null);
        BlobClient blobSource = cc.getBlobClient(generateBlobName());
        blobSource.upload(DATA.getDefaultBinaryData());

        cpknBlockBlob.copyFromUrlWithResponse(new BlobCopyFromUrlOptions(blobSource.getBlobUrl()), null, null);

        assertEquals(scope1, cpknBlockBlob.getProperties().getEncryptionScope());
    }

    @Test
    public void serviceClientBuilderCheck() {
        assertThrows(IllegalArgumentException.class, () -> new BlobServiceClientBuilder()
            .encryptionScope(es)
            .customerProvidedKey(new CustomerProvidedKey(getRandomKey()))
            .buildClient());
    }

    @Test
    public void containerClientBuilderCheck() {
        assertThrows(IllegalArgumentException.class, () -> new BlobContainerClientBuilder()
            .encryptionScope(es)
            .customerProvidedKey(new CustomerProvidedKey(getRandomKey()))
            .buildClient());
    }

    @Test
    public void blobClientBuilderCheck() {
        assertThrows(IllegalArgumentException.class, () -> new BlobClientBuilder()
            .encryptionScope(es)
            .customerProvidedKey(new CustomerProvidedKey(getRandomKey()))
            .endpoint(cc.getBlobContainerUrl())
            .blobName(generateBlobName())
            .buildClient());
    }

    @Test
    public void appendBlobClientBuilderCheck() {
        assertThrows(IllegalArgumentException.class, () -> new SpecializedBlobClientBuilder()
            .encryptionScope(es)
            .customerProvidedKey(new CustomerProvidedKey(getRandomKey()))
            .endpoint(cc.getBlobContainerUrl())
            .blobName(generateBlobName())
            .buildAppendBlobClient());
    }

    @Test
    public void blockBlobClientBuilderCheck() {
        assertThrows(IllegalArgumentException.class, () -> new SpecializedBlobClientBuilder()
            .encryptionScope(es)
            .customerProvidedKey(new CustomerProvidedKey(getRandomKey()))
            .endpoint(cc.getBlobContainerUrl())
            .blobName(generateBlobName())
            .buildBlockBlobClient());
    }

    @Test
    public void pageBlobClientBuilderCheck() {
        assertThrows(IllegalArgumentException.class, () -> new SpecializedBlobClientBuilder()
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
        assertInstanceOf(AppendBlobClient.class, newCpknAppendBlob);
        assertNotEquals(cpknAppendBlob.getEncryptionScope(), newCpknAppendBlob.getEncryptionScope());

        // when: "BlockBlob"
        BlockBlobClient newCpknBlockBlob = cpknBlockBlob.getEncryptionScopeClient(newEncryptionScope);
        assertInstanceOf(BlockBlobClient.class, newCpknBlockBlob);
        assertNotEquals(cpknBlockBlob.getEncryptionScope(), newCpknBlockBlob.getEncryptionScope());

        // when: "PageBlob"
        PageBlobClient newCpknPageBlob = cpknPageBlob.getEncryptionScopeClient(newEncryptionScope);
        assertInstanceOf(PageBlobClient.class, newCpknPageBlob);
        assertNotEquals(cpknPageBlob.getEncryptionScope(), newCpknPageBlob.getEncryptionScope());

        // when: "BlobClient"
        BlobClient cpkBlobClient = cpknContainer.getBlobClient(generateBlobName()); // Inherits container's CPK
        BlobClient newCpknBlobClient = cpkBlobClient.getEncryptionScopeClient(newEncryptionScope);
        assertInstanceOf(BlobClient.class, newCpknBlobClient);
        assertNotEquals(cpkBlobClient.getEncryptionScope(), newCpknBlobClient.getEncryptionScope());

        // when: "BlobClientBase"
        BlobClientBase newCpknBlobClientBase = ((BlobClientBase) cpkBlobClient)
            .getEncryptionScopeClient(newEncryptionScope);
        assertInstanceOf(BlobClientBase.class, newCpknBlobClientBase);
        assertNotEquals(cpkBlobClient.getEncryptionScope(), newCpknBlobClientBase.getEncryptionScope());
    }

}
