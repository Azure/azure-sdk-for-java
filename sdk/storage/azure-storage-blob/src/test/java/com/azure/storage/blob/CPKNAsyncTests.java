// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.rest.Response;
import com.azure.storage.blob.models.AppendBlobItem;
import com.azure.storage.blob.models.BlobContainerEncryptionScope;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.models.CustomerProvidedKey;
import com.azure.storage.blob.models.PageBlobItem;
import com.azure.storage.blob.models.PageRange;
import com.azure.storage.blob.options.BlobCopyFromUrlOptions;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.azure.storage.blob.specialized.AppendBlobAsyncClient;
import com.azure.storage.blob.specialized.BlobAsyncClientBase;
import com.azure.storage.blob.specialized.BlobBaseTestHelper;
import com.azure.storage.blob.specialized.BlockBlobAsyncClient;
import com.azure.storage.blob.specialized.PageBlobAsyncClient;
import com.azure.storage.blob.specialized.PageBlobClient;
import com.azure.storage.blob.specialized.SpecializedBlobClientBuilder;
import com.azure.storage.common.test.shared.extensions.RequiredServiceVersion;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CPKNAsyncTests extends BlobTestBase {

    private final String scope1 = "testscope1";
    private final String scope2 = "testscope2";
    private String es;
    private BlobContainerEncryptionScope ces;
    private BlobContainerClientBuilder builder;

    private BlobContainerAsyncClient cpknContainer;
    private BlockBlobAsyncClient cpknBlockBlob;
    private PageBlobAsyncClient cpknPageBlob;
    private AppendBlobAsyncClient cpknAppendBlob;

    @BeforeEach
    public void setup() {
        es = scope1;
        ces = new BlobContainerEncryptionScope().setDefaultEncryptionScope(scope2)
            .setEncryptionScopeOverridePrevented(true);

        builder = getContainerClientBuilder(ccAsync.getBlobContainerUrl())
            .credential(ENVIRONMENT.getPrimaryAccount().getCredential());

        cpknContainer = builder.encryptionScope(es).buildAsyncClient();

        cpknBlockBlob = cpknContainer.getBlobAsyncClient(generateBlobName()).getBlockBlobAsyncClient();
        cpknPageBlob = cpknContainer.getBlobAsyncClient(generateBlobName()).getPageBlobAsyncClient();
        cpknAppendBlob = cpknContainer.getBlobAsyncClient(generateBlobName()).getAppendBlobAsyncClient();
    }

    @Test
    public void containerCreate() {
        BlobContainerAsyncClient cpknCesContainer = builder.blobContainerEncryptionScope(ces).encryptionScope(null)
            .containerName(generateContainerName()).buildAsyncClient();

        assertAsyncResponseStatusCode(cpknCesContainer.createWithResponse(null, null),
            201);
    }

    @Test
    public void containerDenyEncryptionScopeOverride() {
        BlobContainerAsyncClient cpknCesContainer = builder.blobContainerEncryptionScope(ces)
            .containerName(generateContainerName()).buildAsyncClient();

        cpknAppendBlob = builder.encryptionScope(es)
            .containerName(cpknCesContainer.getBlobContainerName())
            .buildAsyncClient()
            .getBlobAsyncClient(generateBlobName())
            .getAppendBlobAsyncClient();

        StepVerifier.create(cpknCesContainer.create().then(cpknAppendBlob.create()))
            .verifyError(BlobStorageException.class);
    }

    @Test
    public void containerListBlobsFlat() {
        BlobContainerAsyncClient cpkncesContainer = builder
            .blobContainerEncryptionScope(ces)
            .encryptionScope(null)
            .containerName(generateContainerName())
            .buildAsyncClient();
        AppendBlobAsyncClient cpknAppendBlob = cpkncesContainer.getBlobAsyncClient(generateBlobName())
            .getAppendBlobAsyncClient();

        StepVerifier.create(cpkncesContainer.create().then(cpknAppendBlob.create()).thenMany(cpkncesContainer.listBlobs()))
            .assertNext(r -> assertEquals(scope2, r.getProperties().getEncryptionScope()))
            .verifyComplete();
    }

    @Test
    public void containerListBlobsHierarchical() {
        BlobContainerAsyncClient cpkncesContainer = builder
            .blobContainerEncryptionScope(ces)
            .encryptionScope(null)
            .containerName(generateContainerName())
            .buildAsyncClient();
        AppendBlobAsyncClient cpknAppendBlob = cpkncesContainer.getBlobAsyncClient(generateBlobName())
            .getAppendBlobAsyncClient();

        StepVerifier.create(cpkncesContainer.create().then(cpknAppendBlob.create()).thenMany(cpkncesContainer.listBlobsByHierarchy("")))
            .assertNext(r -> assertEquals(scope2, r.getProperties().getEncryptionScope()))
            .verifyComplete();
    }

    @Test
    public void appendBlobCreate() {
        StepVerifier.create(cpknAppendBlob.createWithResponse(null, null, null))
            .assertNext(r -> {
                assertResponseStatusCode(r, 201);
                assertTrue(r.getValue().isServerEncrypted());
                assertEquals(scope1, r.getValue().getEncryptionScope());
            })
            .verifyComplete();
    }

    @Test
    public void appendBlobAppendBlock() {
        StepVerifier.create(cpknAppendBlob.create().then(cpknAppendBlob.appendBlockWithResponse(DATA.getDefaultFlux(),
            DATA.getDefaultDataSize(), null, null)))
            .assertNext(r -> {
                assertResponseStatusCode(r, 201);
                assertTrue(r.getValue().isServerEncrypted());
                assertEquals(scope1, r.getValue().getEncryptionScope());
            })
            .verifyComplete();
    }

    @Test
    public void appendBlobAppendBlockFromURL() {
        String blobName = generateBlobName();
        BlockBlobAsyncClient sourceBlob = ccAsync.getBlobAsyncClient(blobName).getBlockBlobAsyncClient();
        String sas = ccAsync.generateSas(new BlobServiceSasSignatureValues(testResourceNamer.now().plusHours(1),
            new BlobSasPermission().setReadPermission(true)));

        Mono<Response<AppendBlobItem>> response = cpknAppendBlob.create()
            .then(sourceBlob.upload(DATA.getDefaultFlux(), DATA.getDefaultDataSize()))
            .then(cpknAppendBlob.appendBlockFromUrlWithResponse(
                sourceBlob.getBlobUrl() + "?" + sas, null, null, null,
                null));

        StepVerifier.create(response)
            .assertNext(r -> {
                assertResponseStatusCode(r, 201);
                assertTrue(r.getValue().isServerEncrypted());
                assertEquals(scope1, r.getValue().getEncryptionScope());
            })
            .verifyComplete();
    }

    @Test
    public void pageBlobCreate() {
        StepVerifier.create(cpknPageBlob.createWithResponse(1024, null, null, null,
            null))
            .assertNext(r -> {
                assertResponseStatusCode(r, 201);
                assertTrue(r.getValue().isServerEncrypted());
                assertEquals(scope1, r.getValue().getEncryptionScope());
            })
            .verifyComplete();
    }

    @Test
    public void pageBlobPutPage() {
        StepVerifier.create(cpknPageBlob.create(PageBlobClient.PAGE_BYTES).then(cpknPageBlob.uploadPagesWithResponse(
            new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1),
            Flux.just(ByteBuffer.wrap(getRandomByteArray(PageBlobClient.PAGE_BYTES))), null,
            null)))
            .assertNext(r -> {
                assertResponseStatusCode(r, 201);
                assertTrue(r.getValue().isServerEncrypted());
                assertEquals(scope1, r.getValue().getEncryptionScope());
            })
            .verifyComplete();
    }

    @Test
    public void pageBlobPutPageFromURL() {
        String blobName = generateBlobName();
        PageBlobAsyncClient sourceBlob = ccAsync.getBlobAsyncClient(blobName).getPageBlobAsyncClient();
        String sas = ccAsync.generateSas(new BlobServiceSasSignatureValues(testResourceNamer.now().plusHours(1),
            new BlobSasPermission().setReadPermission(true)));

        Mono<Response<PageBlobItem>> response = sourceBlob.create(PageBlobClient.PAGE_BYTES)
            .then(sourceBlob.uploadPagesWithResponse(new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1),
                Flux.just(ByteBuffer.wrap(getRandomByteArray(PageBlobClient.PAGE_BYTES))), null,
                null))
            .then(cpknPageBlob.create(PageBlobClient.PAGE_BYTES))
            .then(cpknPageBlob.uploadPagesFromUrlWithResponse(
                new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1), sourceBlob.getBlobUrl() + "?" + sas,
                null, null, null, null));

        StepVerifier.create(response)
            .assertNext(r -> {
                assertResponseStatusCode(r, 201);
                assertTrue(r.getValue().isServerEncrypted());
                assertEquals(scope1, r.getValue().getEncryptionScope());
            })
            .verifyComplete();
    }

    @Test
    public void pageBlobPutMultiplePages() {
        StepVerifier.create(cpknPageBlob.create(PageBlobClient.PAGE_BYTES * 2).then(cpknPageBlob.uploadPagesWithResponse(new PageRange().setStart(0)
            .setEnd(PageBlobClient.PAGE_BYTES * 2 - 1),
            Flux.just(ByteBuffer.wrap(getRandomByteArray(PageBlobClient.PAGE_BYTES * 2))), null,
            null)))
            .assertNext(r -> {
                assertResponseStatusCode(r, 201);
                Assertions.assertTrue(r.getValue().isServerEncrypted());
                Assertions.assertEquals(scope1, r.getValue().getEncryptionScope());
            })
            .verifyComplete();
    }

    @Test
    public void pageBlobClearPage() {
        Mono<Response<PageBlobItem>> response = cpknPageBlob.create(PageBlobClient.PAGE_BYTES * 2)
            .then(cpknPageBlob.uploadPagesWithResponse(new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1),
                Flux.just(ByteBuffer.wrap(getRandomByteArray(PageBlobClient.PAGE_BYTES))), null,
                null))
            .then(cpknPageBlob.clearPagesWithResponse(new PageRange().setStart(0)
                .setEnd(PageBlobClient.PAGE_BYTES - 1), null));

        assertAsyncResponseStatusCode(response, 201);
    }

    @Test
    public void pageBlobResize() {
        Mono<Response<PageBlobItem>> response = cpknPageBlob.create(PageBlobClient.PAGE_BYTES * 2)
            .then(cpknPageBlob.resizeWithResponse(PageBlobClient.PAGE_BYTES * 2,
                null));

        assertAsyncResponseStatusCode(response, 200);
    }

    @Test
    public void blockBlobUpload() {
        StepVerifier.create(cpknBlockBlob.uploadWithResponse(DATA.getDefaultFlux(),
            DATA.getDefaultDataSize(), null, null, null, null, null))
            .assertNext(r -> {
                assertResponseStatusCode(r, 201);
                Assertions.assertTrue(r.getValue().isServerEncrypted());
                Assertions.assertEquals(scope1, r.getValue().getEncryptionScope());
            })
            .verifyComplete();
    }

    @Test
    public void blockBlobStageBlock() {
        StepVerifier.create(cpknBlockBlob.upload(DATA.getDefaultFlux(), DATA.getDefaultDataSize()).then(cpknBlockBlob.stageBlockWithResponse(getBlockID(), DATA.getDefaultFlux(),
            DATA.getDefaultDataSize(), null, null)))
            .assertNext(r -> {
                HttpHeaders headers = r.getHeaders();

                assertResponseStatusCode(r, 201);
                Assertions.assertTrue(Boolean.parseBoolean(headers.getValue(X_MS_REQUEST_SERVER_ENCRYPTED)));
                Assertions.assertEquals(scope1, headers.getValue(X_MS_ENCRYPTION_SCOPE));
            })
            .verifyComplete();
    }

    @Test
    public void blockBlobCommitBlockList() {
        String blockID = getBlockID();
        List<String> ids = Collections.singletonList(blockID);

        StepVerifier.create(cpknBlockBlob.stageBlock(blockID, DATA.getDefaultFlux(), DATA.getDefaultDataSize()).then(cpknBlockBlob.commitBlockListWithResponse(ids, null, null, null,
            null)))
            .assertNext(r -> {
                assertResponseStatusCode(r, 201);
                Assertions.assertTrue(r.getValue().isServerEncrypted());
                Assertions.assertEquals(scope1, r.getValue().getEncryptionScope());
            })
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2020-12-06")
    @Test
    public void asyncCopyEncryptionScope() {
        BlobAsyncClient blobSource = ccAsync.getBlobAsyncClient(generateBlobName());
        String sas = blobSource.generateSas(new BlobServiceSasSignatureValues(testResourceNamer.now().plusDays(1),
            new BlobSasPermission().setTagsPermission(true).setReadPermission(true)));

        Mono<BlobProperties> response = blobSource.upload(DATA.getDefaultBinaryData())
            .then(cpknBlockBlob.copyFromUrlWithResponse(new BlobCopyFromUrlOptions(blobSource.getBlobUrl() + "?" + sas)))
            .then(cpknBlockBlob.getProperties());

        StepVerifier.create(response)
            .assertNext(r -> assertEquals(scope1, r.getEncryptionScope()))
            .verifyComplete();
    }

    @Test
    public void serviceClientBuilderCheck() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new BlobServiceClientBuilder()
            .encryptionScope(es)
            .customerProvidedKey(new CustomerProvidedKey(getRandomKey()))
            .buildAsyncClient());
    }

    @Test
    public void containerClientBuilderCheck() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new BlobContainerClientBuilder()
            .encryptionScope(es)
            .customerProvidedKey(new CustomerProvidedKey(getRandomKey()))
            .buildAsyncClient());
    }

    @Test
    public void blobClientBuilderCheck() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new BlobClientBuilder()
            .encryptionScope(es)
            .customerProvidedKey(new CustomerProvidedKey(getRandomKey()))
            .endpoint(ccAsync.getBlobContainerUrl())
            .blobName(generateBlobName())
            .buildAsyncClient());
    }

    @Test
    public void appendBlobClientBuilderCheck() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new SpecializedBlobClientBuilder()
            .encryptionScope(es)
            .customerProvidedKey(new CustomerProvidedKey(getRandomKey()))
            .endpoint(ccAsync.getBlobContainerUrl())
            .blobName(generateBlobName())
            .buildAppendBlobAsyncClient());
    }

    @Test
    public void blockBlobClientBuilderCheck() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new SpecializedBlobClientBuilder()
            .encryptionScope(es)
            .customerProvidedKey(new CustomerProvidedKey(getRandomKey()))
            .endpoint(ccAsync.getBlobContainerUrl())
            .blobName(generateBlobName())
            .buildBlockBlobAsyncClient());
    }

    @Test
    public void pageBlobClientBuilderCheck() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new SpecializedBlobClientBuilder()
            .encryptionScope(es)
            .customerProvidedKey(new CustomerProvidedKey(getRandomKey()))
            .endpoint(ccAsync.getBlobContainerUrl())
            .blobName(generateBlobName())
            .buildPageBlobAsyncClient());
    }

    @Test
    public void getEncryptionScopeClient() {
        String newEncryptionScope = "newtestscope";

        // when: "AppendBlob"
        AppendBlobAsyncClient newCpknAppendBlob = cpknAppendBlob.getEncryptionScopeAsyncClient(newEncryptionScope);
        assertInstanceOf(AppendBlobAsyncClient.class, newCpknAppendBlob);
        assertNotEquals(BlobBaseTestHelper.getEncryptionScope(cpknAppendBlob),
            BlobBaseTestHelper.getEncryptionScope(newCpknAppendBlob));

        // when: "BlockBlob"
        BlockBlobAsyncClient newCpknBlockBlob = cpknBlockBlob.getEncryptionScopeAsyncClient(newEncryptionScope);
        assertInstanceOf(BlockBlobAsyncClient.class, newCpknBlockBlob);
        assertNotEquals(BlobBaseTestHelper.getEncryptionScope(cpknBlockBlob),
            BlobBaseTestHelper.getEncryptionScope(newCpknBlockBlob));

        // when: "PageBlob"
        PageBlobAsyncClient newCpknPageBlob = cpknPageBlob.getEncryptionScopeAsyncClient(newEncryptionScope);
        assertInstanceOf(PageBlobAsyncClient.class, newCpknPageBlob);
        assertNotEquals(BlobBaseTestHelper.getEncryptionScope(cpknPageBlob),
            BlobBaseTestHelper.getEncryptionScope(newCpknPageBlob));

        // when: "BlobClient"
        BlobAsyncClient cpkBlobClient = cpknContainer.getBlobAsyncClient(generateBlobName()); // Inherits container's CPK
        BlobAsyncClient newCpknBlobClient = cpkBlobClient.getEncryptionScopeAsyncClient(newEncryptionScope);
        assertInstanceOf(BlobAsyncClient.class, newCpknBlobClient);
        assertNotEquals(BlobBaseTestHelper.getEncryptionScope(cpkBlobClient),
            BlobBaseTestHelper.getEncryptionScope(newCpknBlobClient));

        // when: "BlobClientBase"
        BlobAsyncClientBase newCpknBlobClientBase = ((BlobAsyncClientBase) cpkBlobClient)
            .getEncryptionScopeAsyncClient(newEncryptionScope);
        assertInstanceOf(BlobAsyncClientBase.class, newCpknBlobClientBase);
        assertNotEquals(BlobBaseTestHelper.getEncryptionScope(cpkBlobClient),
            BlobBaseTestHelper.getEncryptionScope(newCpknBlobClientBase));
    }
}
