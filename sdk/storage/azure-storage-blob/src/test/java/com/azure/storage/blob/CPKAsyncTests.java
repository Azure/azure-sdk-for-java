// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.test.annotation.LiveOnly;
import com.azure.core.test.utils.TestUtils;
import com.azure.core.util.FluxUtil;
import com.azure.storage.blob.models.CustomerProvidedKey;
import com.azure.storage.blob.models.PageRange;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.azure.storage.blob.specialized.AppendBlobAsyncClient;
import com.azure.storage.blob.specialized.BlobAsyncClientBase;
import com.azure.storage.blob.specialized.BlockBlobAsyncClient;
import com.azure.storage.blob.specialized.PageBlobAsyncClient;
import com.azure.storage.blob.specialized.PageBlobClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@LiveOnly
public class CPKAsyncTests extends BlobTestBase {
    // LiveOnly because "x-ms-encryption-key-sha256 cannot be stored in recordings"
    private CustomerProvidedKey key;
    private BlobContainerAsyncClient cpkContainer;
    private BlockBlobAsyncClient cpkBlockBlob;
    private PageBlobAsyncClient cpkPageBlob;
    private AppendBlobAsyncClient cpkAppendBlob;
    private BlobAsyncClientBase cpkExistingBlob;

    @BeforeEach
    public void setup() {
        key = new CustomerProvidedKey(getRandomKey());
        BlobContainerClientBuilder builder = instrument(new BlobContainerClientBuilder()
            .endpoint(ccAsync.getBlobContainerUrl().toString())
            .customerProvidedKey(key)
            .credential(ENVIRONMENT.getPrimaryAccount().getCredential()));

        cpkContainer = builder.buildAsyncClient();
        cpkBlockBlob = cpkContainer.getBlobAsyncClient(generateBlobName()).getBlockBlobAsyncClient();
        cpkPageBlob = cpkContainer.getBlobAsyncClient(generateBlobName()).getPageBlobAsyncClient();
        cpkAppendBlob = cpkContainer.getBlobAsyncClient(generateBlobName()).getAppendBlobAsyncClient();

        BlockBlobAsyncClient existingBlobSetup = cpkContainer.getBlobAsyncClient(generateBlobName())
            .getBlockBlobAsyncClient();
        existingBlobSetup.upload(DATA.getDefaultFlux(), DATA.getDefaultDataSize()).block();
        cpkExistingBlob = existingBlobSetup;
    }

    @Test
    public void putBlobWithCPK() {
        StepVerifier.create(cpkBlockBlob.uploadWithResponse(DATA.getDefaultFlux(),
            DATA.getDefaultDataSize(), null, null, null, null, null))
            .assertNext(r -> {
                assertResponseStatusCode(r, 201);
                assertTrue(r.getValue().isServerEncrypted());
                assertEquals(key.getKeySha256(), r.getValue().getEncryptionKeySha256());
            })
            .verifyComplete();
    }

    @Test
    public void getBlobWithCPK() {
        cpkBlockBlob.upload(DATA.getDefaultFlux(), DATA.getDefaultDataSize()).block();

        StepVerifier.create(cpkBlockBlob.downloadWithResponse(null, null, null,
            false)
            .flatMap(r -> {
                assertResponseStatusCode(r, 200);
                return FluxUtil.collectBytesInByteBufferStream(r.getValue());
            }))
            .assertNext(r -> TestUtils.assertArraysEqual(r, DATA.getDefaultBytes()))
            .verifyComplete();
    }

    @Test
    public void putBlockWithCPK() {
        StepVerifier.create(cpkBlockBlob.stageBlockWithResponse(getBlockID(), DATA.getDefaultFlux(),
            DATA.getDefaultDataSize(), null, null))
            .assertNext(r -> {
                assertResponseStatusCode(r, 201);
                assertTrue(Boolean.parseBoolean(r.getHeaders().getValue(X_MS_REQUEST_SERVER_ENCRYPTED)));
            })
            .verifyComplete();
    }

    @Test
    public void putBlockFromURLWithCPK() {
        String blobName = generateBlobName();
        BlockBlobAsyncClient sourceBlob = ccAsync.getBlobAsyncClient(blobName).getBlockBlobAsyncClient();
        sourceBlob.upload(DATA.getDefaultFlux(), DATA.getDefaultDataSize()).block();
        String sas = ccAsync.generateSas(new BlobServiceSasSignatureValues(testResourceNamer.now().plusHours(1),
            new BlobSasPermission().setReadPermission(true)));

        StepVerifier.create(cpkBlockBlob.stageBlockFromUrlWithResponse(getBlockID(),
            sourceBlob.getBlobUrl() + "?" + sas, null, null, null,
            null))
            .assertNext(r -> {
                assertResponseStatusCode(r, 201);
                assertTrue(Boolean.parseBoolean(r.getHeaders().getValue(X_MS_REQUEST_SERVER_ENCRYPTED)));
            })
            .verifyComplete();
    }

    @Test
    public void putBlockListWithCPK() {
        List<String> blockIDList = Arrays.asList(getBlockID(), getBlockID());
        for (String blockId : blockIDList) {
            cpkBlockBlob.stageBlock(blockId, DATA.getDefaultFlux(), DATA.getDefaultDataSize()).block();
        }

        StepVerifier.create(cpkBlockBlob.commitBlockListWithResponse(blockIDList, null, null, null,
            null))
            .assertNext(r -> {
                assertResponseStatusCode(r, 201);
                assertTrue(r.getValue().isServerEncrypted());
                assertEquals(key.getKeySha256(), r.getValue().getEncryptionKeySha256());
            })
            .verifyComplete();
    }

    @Test
    public void putPageWithCPK() {
        cpkPageBlob.create(PageBlobClient.PAGE_BYTES).block();

        StepVerifier.create(cpkPageBlob.uploadPagesWithResponse(
            new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1),
            Flux.just(ByteBuffer.wrap(getRandomByteArray(PageBlobClient.PAGE_BYTES))), null,
            null))
            .assertNext(r -> {
                assertResponseStatusCode(r, 201);
                assertTrue(r.getValue().isServerEncrypted());
                assertEquals(key.getKeySha256(), r.getValue().getEncryptionKeySha256());
            })
            .verifyComplete();
    }

    @Test
    public void putPageFromURLWithCPK() {
        String blobName = generateBlobName();
        PageBlobAsyncClient sourceBlob = ccAsync.getBlobAsyncClient(blobName).getPageBlobAsyncClient();
        sourceBlob.create(PageBlobClient.PAGE_BYTES).block();
        sourceBlob.uploadPagesWithResponse(new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1),
            Flux.just(ByteBuffer.wrap(getRandomByteArray(PageBlobClient.PAGE_BYTES))), null,
            null).block();

        cpkPageBlob.create(PageBlobClient.PAGE_BYTES).block();
        String sas = ccAsync.generateSas(new BlobServiceSasSignatureValues(testResourceNamer.now().plusHours(1),
            new BlobSasPermission().setReadPermission(true)));

        StepVerifier.create(cpkPageBlob.uploadPagesFromUrlWithResponse(
            new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1),
            sourceBlob.getBlobUrl() + "?" + sas, null, null, null,
            null))
            .assertNext(r -> {
                assertResponseStatusCode(r, 201);
                assertTrue(r.getValue().isServerEncrypted());
                assertEquals(key.getKeySha256(), r.getValue().getEncryptionKeySha256());
            })
            .verifyComplete();
    }

    @Test
    public void putMultiplePagesWithCPK() {
        cpkPageBlob.create(PageBlobClient.PAGE_BYTES * 2).block();

        StepVerifier.create(cpkPageBlob.uploadPagesWithResponse(
            new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES * 2 - 1),
            Flux.just(ByteBuffer.wrap(getRandomByteArray(PageBlobClient.PAGE_BYTES * 2))), null,
            null))
            .assertNext(r -> {
                assertResponseStatusCode(r, 201);
                assertTrue(r.getValue().isServerEncrypted());
                assertEquals(key.getKeySha256(), r.getValue().getEncryptionKeySha256());
            })
            .verifyComplete();
    }

    @Test
    public void appendBlockWithCPK() {
        cpkAppendBlob.create().block();

        StepVerifier.create(cpkAppendBlob.appendBlockWithResponse(DATA.getDefaultFlux(),
            DATA.getDefaultDataSize(), null, null))
            .assertNext(r -> {
                assertResponseStatusCode(r, 201);
                assertTrue(r.getValue().isServerEncrypted());
                assertEquals(key.getKeySha256(), r.getValue().getEncryptionKeySha256());
            })
            .verifyComplete();
    }

    @Test
    public void appendBlockFromURLWithCPK() {
        cpkAppendBlob.create().block();
        String blobName = generateBlobName();
        BlockBlobAsyncClient sourceBlob = ccAsync.getBlobAsyncClient(blobName).getBlockBlobAsyncClient();
        sourceBlob.upload(DATA.getDefaultFlux(), DATA.getDefaultDataSize()).block();

        String sas = ccAsync.generateSas(new BlobServiceSasSignatureValues(testResourceNamer.now().plusHours(1),
            new BlobSasPermission().setReadPermission(true)));

        StepVerifier.create(cpkAppendBlob.appendBlockFromUrlWithResponse(
            sourceBlob.getBlobUrl() + "?" + sas, null, null, null,
            null))
            .assertNext(r -> {
                assertResponseStatusCode(r, 201);
                assertTrue(r.getValue().isServerEncrypted());
                assertEquals(key.getKeySha256(), r.getValue().getEncryptionKeySha256());
            })
            .verifyComplete();
    }

    @Test
    public void setBlobMetadataWithCPK() {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("foo", "bar");

        StepVerifier.create(cpkExistingBlob.setMetadataWithResponse(metadata, null))
            .assertNext(r -> {
                assertResponseStatusCode(r, 200);
                assertTrue(Boolean.parseBoolean(r.getHeaders().getValue(X_MS_REQUEST_SERVER_ENCRYPTED)));
                assertEquals(key.getKeySha256(), r.getHeaders().getValue(X_MS_ENCRYPTION_KEY_SHA256));
            })
            .verifyComplete();
    }

    @Test
    public void getBlobPropertiesAndMetadataWithCPK() {
        StepVerifier.create(cpkExistingBlob.getPropertiesWithResponse(null))
            .assertNext(r -> {
                assertResponseStatusCode(r, 200);
                assertTrue(Boolean.parseBoolean(r.getHeaders().getValue(X_MS_SERVER_ENCRYPTED)));
                assertEquals(key.getKeySha256(), r.getHeaders().getValue(X_MS_ENCRYPTION_KEY_SHA256));
            })
            .verifyComplete();
    }

    @Test
    public void snapshotBlobWithCPK() {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("foo", "bar");
        assertAsyncResponseStatusCode(cpkExistingBlob.createSnapshotWithResponse(metadata, null),
            201);
    }

    @Test
    public void getCustomerProvidedKeyClient() {
        CustomerProvidedKey newCpk = new CustomerProvidedKey(getRandomKey());

        // when: "AppendBlob"
        AppendBlobAsyncClient newCpkAppendBlob = cpkAppendBlob.getCustomerProvidedKeyAsyncClient(newCpk);

        assertInstanceOf(AppendBlobAsyncClient.class, newCpkAppendBlob);
        assertNotEquals(newCpkAppendBlob.getCustomerProvidedKey(), cpkAppendBlob.getCustomerProvidedKey());

        // when: "BlockBlob"
        BlockBlobAsyncClient newCpkBlockBlob = cpkBlockBlob.getCustomerProvidedKeyAsyncClient(newCpk);

        assertInstanceOf(BlockBlobAsyncClient.class, newCpkBlockBlob);
        assertNotEquals(newCpkBlockBlob.getCustomerProvidedKey(), cpkBlockBlob.getCustomerProvidedKey());

        // when: "PageBlob"
        PageBlobAsyncClient newCpkPageBlob = cpkPageBlob.getCustomerProvidedKeyAsyncClient(newCpk);

        assertInstanceOf(PageBlobAsyncClient.class, newCpkPageBlob);
        assertNotEquals(newCpkPageBlob.getCustomerProvidedKey(), cpkPageBlob.getCustomerProvidedKey());

        // when: "BlobClientBase"
        BlobAsyncClientBase newCpkBlobClientBase = cpkExistingBlob.getCustomerProvidedKeyAsyncClient(newCpk);

        assertInstanceOf(BlobAsyncClientBase.class, newCpkBlobClientBase);
        assertNotEquals(newCpkBlobClientBase.getCustomerProvidedKey(), cpkExistingBlob.getCustomerProvidedKey());

        // when: "BlobClient"
        BlobAsyncClient cpkBlobClient = cpkContainer.getBlobAsyncClient(generateBlobName()); // Inherits container's CPK
        BlobAsyncClient newCpkBlobClient = cpkBlobClient.getCustomerProvidedKeyAsyncClient(newCpk);

        assertInstanceOf(BlobAsyncClient.class, newCpkBlobClient);
        assertNotEquals(newCpkBlobClient.getCustomerProvidedKey(), cpkBlobClient.getCustomerProvidedKey());
    }

    @Test
    public void existsWithoutCPK() {
        BlobAsyncClientBase clientWithoutCpk = cpkExistingBlob.getCustomerProvidedKeyAsyncClient(null);
        StepVerifier.create(clientWithoutCpk.exists())
            .expectNext(true)
            .verifyComplete();
    }

}
