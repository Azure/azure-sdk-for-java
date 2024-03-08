// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.http.rest.Response;
import com.azure.core.test.utils.TestUtils;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.polling.PollerFlux;
import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.AppendBlobItem;
import com.azure.storage.blob.models.BlobCopyInfo;
import com.azure.storage.blob.models.BlobListDetails;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.models.BlockBlobItem;
import com.azure.storage.blob.models.ListBlobsOptions;
import com.azure.storage.blob.models.PageBlobItem;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.azure.storage.common.sas.AccountSasPermission;
import com.azure.storage.common.sas.AccountSasResourceType;
import com.azure.storage.common.sas.AccountSasService;
import com.azure.storage.common.sas.AccountSasSignatureValues;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class VersioningAsyncTests extends BlobTestBase {

    private BlobContainerAsyncClient blobContainerClient;
    private BlobAsyncClient blobClient;
    private final String contentV1 = "contentV1";
    private final String contentV2 = "contentV2";

    @BeforeEach
    public void setup() {
        String blobName = generateBlobName();
        String containerName = generateContainerName();
        blobContainerClient = versionedBlobServiceAsyncClient.createBlobContainer(containerName).block();
        blobClient = blobContainerClient.getBlobAsyncClient(blobName);
    }

    @AfterEach
    public void cleanup() {
        blobContainerClient.delete().block();
    }

    @Test
    public void createBlockBlobWithVersion() {
        BlockBlobItem blobItemV1 = blobClient.getBlockBlobAsyncClient().upload(DATA.getDefaultFlux(),
            DATA.getDefaultDataSize()).block();
        BlockBlobItem blobItemV2 = blobClient.getBlockBlobAsyncClient().upload(DATA.getDefaultFlux(),
            DATA.getDefaultDataSize(), true).block();

        assertNotNull(blobItemV1.getVersionId());
        assertNotNull(blobItemV2.getVersionId());
        assertFalse(StringUtils.equals(blobItemV1.getVersionId(), blobItemV2.getVersionId()));
    }

    @Test
    public void createPageBlobWithVersion() {
        PageBlobItem blobItemV1 = blobClient.getPageBlobAsyncClient().create(512).block();
        PageBlobItem blobItemV2 = blobClient.getPageBlobAsyncClient().create(512, true).block();

        assertNotNull(blobItemV1.getVersionId());
        assertNotNull(blobItemV2.getVersionId());
        assertFalse(StringUtils.equals(blobItemV1.getVersionId(), blobItemV2.getVersionId()));
    }

    @Test
    public void createAppendBlobWithVersion() {
        AppendBlobItem blobItemV1 = blobClient.getAppendBlobAsyncClient().create().block();
        AppendBlobItem blobItemV2 = blobClient.getAppendBlobAsyncClient().create(true).block();

        assertNotNull(blobItemV1.getVersionId());
        assertNotNull(blobItemV2.getVersionId());
        assertFalse(StringUtils.equals(blobItemV1.getVersionId(), blobItemV2.getVersionId()));
    }

    @Test
    public void downloadBlobByVersion() {
        Flux<ByteBuffer> inputV1 = Flux.just(ByteBuffer.wrap(contentV1.getBytes(StandardCharsets.UTF_8)));
        Flux<ByteBuffer> inputV2 = Flux.just(ByteBuffer.wrap(contentV2.getBytes(StandardCharsets.UTF_8)));
        BlockBlobItem blobItemV1 = blobClient.getBlockBlobAsyncClient().upload(inputV1, contentV1.length()).block();
        BlockBlobItem blobItemV2 = blobClient.getBlockBlobAsyncClient().upload(inputV2, contentV2.length(), true).block();

        StepVerifier.create(FluxUtil.collectBytesInByteBufferStream(blobClient
            .getVersionClient(blobItemV1.getVersionId()).download()))
            .assertNext(r -> TestUtils.assertArraysEqual(r, contentV1.getBytes(StandardCharsets.UTF_8)))
            .verifyComplete();

        StepVerifier.create(FluxUtil.collectBytesInByteBufferStream(blobClient
            .getVersionClient(blobItemV2.getVersionId()).download()))
            .assertNext(r -> TestUtils.assertArraysEqual(r, contentV2.getBytes(StandardCharsets.UTF_8)))
            .verifyComplete();
    }

    @Test
    public void downloadBlobByVersionStreaming() {
        Flux<ByteBuffer> inputV1 = Flux.just(ByteBuffer.wrap(contentV1.getBytes(StandardCharsets.UTF_8)));
        Flux<ByteBuffer> inputV2 = Flux.just(ByteBuffer.wrap(contentV2.getBytes(StandardCharsets.UTF_8)));
        BlockBlobItem blobItemV1 = blobClient.getBlockBlobAsyncClient().upload(inputV1, contentV1.length()).block();
        BlockBlobItem blobItemV2 = blobClient.getBlockBlobAsyncClient().upload(inputV2, contentV2.length(), true).block();

        StepVerifier.create(FluxUtil.collectBytesInByteBufferStream(blobClient
            .getVersionClient(blobItemV1.getVersionId()).downloadStream()))
            .assertNext(r -> TestUtils.assertArraysEqual(r, contentV1.getBytes(StandardCharsets.UTF_8)))
            .verifyComplete();

        StepVerifier.create(FluxUtil.collectBytesInByteBufferStream(blobClient
            .getVersionClient(blobItemV2.getVersionId()).downloadStream()))
            .assertNext(r -> TestUtils.assertArraysEqual(r, contentV2.getBytes(StandardCharsets.UTF_8)))
            .verifyComplete();
    }

    @Test
    public void downloadBlobByVersionBinaryData() {
        Flux<ByteBuffer> inputV1 = Flux.just(ByteBuffer.wrap(contentV1.getBytes(StandardCharsets.UTF_8)));
        Flux<ByteBuffer> inputV2 = Flux.just(ByteBuffer.wrap(contentV2.getBytes(StandardCharsets.UTF_8)));
        BlockBlobItem blobItemV1 = blobClient.getBlockBlobAsyncClient().upload(inputV1, contentV1.length()).block();
        BlockBlobItem blobItemV2 = blobClient.getBlockBlobAsyncClient().upload(inputV2, contentV2.length(), true).block();

        StepVerifier.create(blobClient.getVersionClient(blobItemV1.getVersionId()).downloadContent())
            .assertNext(r -> assertEquals(r.toString(), contentV1))
            .verifyComplete();

        StepVerifier.create(blobClient.getVersionClient(blobItemV2.getVersionId()).downloadContent())
            .assertNext(r -> assertEquals(r.toString(), contentV2))
            .verifyComplete();
    }

    @Test
    public void deleteBlobByVersion() {
        Flux<ByteBuffer> inputV1 = Flux.just(ByteBuffer.wrap(contentV1.getBytes(StandardCharsets.UTF_8)));
        Flux<ByteBuffer> inputV2 = Flux.just(ByteBuffer.wrap(contentV2.getBytes(StandardCharsets.UTF_8)));
        BlockBlobItem blobItemV1 = blobClient.getBlockBlobAsyncClient().upload(inputV1, contentV1.length()).block();
        BlockBlobItem blobItemV2 = blobClient.getBlockBlobAsyncClient().upload(inputV2, contentV2.length(), true).block();

        blobClient.getVersionClient(blobItemV1.getVersionId()).delete().block();

        StepVerifier.create(blobClient.getVersionClient(blobItemV1.getVersionId()).exists())
            .expectNext(false)
            .verifyComplete();

        StepVerifier.create(blobClient.getVersionClient(blobItemV2.getVersionId()).exists())
            .expectNext(true)
            .verifyComplete();
    }

    @Test
    public void deleteBlobByVersionUsingSASToken() {
        Flux<ByteBuffer> inputV1 = Flux.just(ByteBuffer.wrap(contentV1.getBytes(StandardCharsets.UTF_8)));
        Flux<ByteBuffer> inputV2 = Flux.just(ByteBuffer.wrap(contentV2.getBytes(StandardCharsets.UTF_8)));
        BlockBlobItem blobItemV1 = blobClient.getBlockBlobAsyncClient().upload(inputV1, contentV1.length()).block();
        BlockBlobItem blobItemV2 = blobClient.getBlockBlobAsyncClient().upload(inputV2, contentV2.length(), true).block();

        BlobSasPermission permission = new BlobSasPermission()
            .setDeleteVersionPermission(true);
        String sasToken = blobClient.getVersionClient(blobItemV1.getVersionId())
            .generateSas(new BlobServiceSasSignatureValues(testResourceNamer.now().plusDays(1), permission));

        BlobAsyncClient sasClient = getBlobAsyncClient(blobClient.getVersionClient(blobItemV1.getVersionId()).getBlobUrl(),
            sasToken);

        sasClient.delete().block();

        StepVerifier.create(blobClient.getVersionClient(blobItemV1.getVersionId()).exists())
            .expectNext(false)
            .verifyComplete();

        StepVerifier.create(blobClient.getVersionClient(blobItemV2.getVersionId()).exists())
            .expectNext(true)
            .verifyComplete();
    }

    @Test
    public void deleteBlobByVersionUsingContainerSASToken() {
        Flux<ByteBuffer> inputV1 = Flux.just(ByteBuffer.wrap(contentV1.getBytes(StandardCharsets.UTF_8)));
        Flux<ByteBuffer> inputV2 = Flux.just(ByteBuffer.wrap(contentV2.getBytes(StandardCharsets.UTF_8)));
        BlockBlobItem blobItemV1 = blobClient.getBlockBlobAsyncClient().upload(inputV1, contentV1.length()).block();
        BlockBlobItem blobItemV2 = blobClient.getBlockBlobAsyncClient().upload(inputV2, contentV2.length(), true).block();

        BlobSasPermission permission = new BlobSasPermission()
            .setDeleteVersionPermission(true);
        String sasToken = blobContainerClient.generateSas(
            new BlobServiceSasSignatureValues(testResourceNamer.now().plusDays(1), permission));

        BlobAsyncClient sasClient = getBlobAsyncClient(blobClient.getVersionClient(blobItemV1.getVersionId()).getBlobUrl(),
            sasToken);

        sasClient.delete().block();

        StepVerifier.create(blobClient.getVersionClient(blobItemV1.getVersionId()).exists())
            .expectNext(false)
            .verifyComplete();

        StepVerifier.create(blobClient.getVersionClient(blobItemV2.getVersionId()).exists())
            .expectNext(true)
            .verifyComplete();
    }

    @Test
    public void deleteBlobByVersionUsingAccountSASToken() {
        Flux<ByteBuffer> inputV1 = Flux.just(ByteBuffer.wrap(contentV1.getBytes(StandardCharsets.UTF_8)));
        Flux<ByteBuffer> inputV2 = Flux.just(ByteBuffer.wrap(contentV2.getBytes(StandardCharsets.UTF_8)));
        BlockBlobItem blobItemV1 = blobClient.getBlockBlobAsyncClient().upload(inputV1, contentV1.length()).block();
        BlockBlobItem blobItemV2 = blobClient.getBlockBlobAsyncClient().upload(inputV2, contentV2.length(), true).block();

        AccountSasPermission permission = new AccountSasPermission()
            .setDeleteVersionPermission(true);
        String sasToken = versionedBlobServiceAsyncClient.generateAccountSas(new AccountSasSignatureValues(
            testResourceNamer.now().plusDays(1), permission, new AccountSasService().setBlobAccess(true),
            new AccountSasResourceType().setObject(true)));

        BlobAsyncClient sasClient = getBlobAsyncClient(blobClient.getVersionClient(blobItemV1.getVersionId()).getBlobUrl(),
            sasToken);

        sasClient.delete().block();

        StepVerifier.create(blobClient.getVersionClient(blobItemV1.getVersionId()).exists())
            .expectNext(false)
            .verifyComplete();

        StepVerifier.create(blobClient.getVersionClient(blobItemV2.getVersionId()).exists())
            .expectNext(true)
            .verifyComplete();
    }

    @Test
    public void getBlobPropertiesByVersion() {
        String key = "key";
        String valV2 = "val2";
        String valV3 = "val3";
        BlockBlobItem blobItemV1 = blobClient.getBlockBlobAsyncClient().upload(DATA.getDefaultFlux(),
            DATA.getDefaultDataSize()).block();
        Response<Void> responseV2 = blobClient.getBlockBlobAsyncClient()
            .setMetadataWithResponse(Collections.singletonMap(key, valV2), null).block();
        Response<Void> responseV3 = blobClient.getBlockBlobAsyncClient()
            .setMetadataWithResponse(Collections.singletonMap(key, valV3), null).block();
        String versionId1 = blobItemV1.getVersionId();
        String versionId2 = responseV2.getHeaders().getValue(X_MS_VERSION_ID);
        String versionId3 = responseV3.getHeaders().getValue(X_MS_VERSION_ID);

        String receivedValV1 = blobClient.getVersionClient(versionId1).getProperties().block().getMetadata().get(key);
        String receivedValV2 = blobClient.getVersionClient(versionId2).getProperties().block().getMetadata().get(key);
        String receivedValV3 = blobClient.getVersionClient(versionId3).getProperties().block().getMetadata().get(key);

        assertNull(receivedValV1);
        assertEquals(valV2, receivedValV2);
        assertEquals(valV3, receivedValV3);
    }

    @Test
    public void listBlobsWithVersion() {
        BlockBlobItem blobItemV1 = blobClient.getBlockBlobAsyncClient().upload(DATA.getDefaultFlux(),
            DATA.getDefaultDataSize()).block();
        BlockBlobItem blobItemV2 = blobClient.getBlockBlobAsyncClient().upload(DATA.getDefaultFlux(),
            DATA.getDefaultDataSize(), true).block();
        BlockBlobItem blobItemV3 = blobClient.getBlockBlobAsyncClient().upload(DATA.getDefaultFlux(),
            DATA.getDefaultDataSize(), true).block();

        StepVerifier.create(blobContainerClient.listBlobs(
            new ListBlobsOptions().setDetails(new BlobListDetails().setRetrieveVersions(true)), null))
            .assertNext(r -> {
                assertEquals(blobItemV1.getVersionId(), r.getVersionId());
                assertNull(r.isCurrentVersion());
            })
            .assertNext(r -> {
                assertEquals(blobItemV2.getVersionId(), r.getVersionId());
                assertNull(r.isCurrentVersion());
            })
            .assertNext(r -> {
                assertEquals(blobItemV3.getVersionId(), r.getVersionId());
                assertTrue(r.isCurrentVersion());
            })
            .verifyComplete();
    }

    @Test
    public void listBlobsWithoutVersion() {
        blobClient.getBlockBlobAsyncClient().upload(DATA.getDefaultFlux(), DATA.getDefaultDataSize()).block();
        blobClient.getBlockBlobAsyncClient().upload(DATA.getDefaultFlux(), DATA.getDefaultDataSize(), true).block();
        BlockBlobItem blobItemV3 = blobClient.getBlockBlobAsyncClient().upload(DATA.getDefaultFlux(),
            DATA.getDefaultDataSize(), true).block();

        StepVerifier.create(blobContainerClient.listBlobs(
            new ListBlobsOptions().setDetails(new BlobListDetails().setRetrieveVersions(false)), null))
            .assertNext(r -> assertEquals(blobItemV3.getVersionId(), r.getVersionId()))
            .verifyComplete();
    }

    @Test
    public void beginCopyBlobsWithVersion() {
        BlockBlobItem blobItemV1 = blobClient.getBlockBlobAsyncClient().upload(DATA.getDefaultFlux(),
            DATA.getDefaultDataSize()).block();
        BlobAsyncClient sourceBlob = blobContainerClient.getBlobAsyncClient(generateBlobName());
        sourceBlob.getBlockBlobAsyncClient().upload(DATA.getDefaultFlux(), DATA.getDefaultDataSize()).block();

        PollerFlux<BlobCopyInfo, Void> poller = setPlaybackPollerFluxPollInterval(blobClient.beginCopy(
            sourceBlob.getBlobUrl(), null));
        BlobCopyInfo copyInfo = poller.blockLast().getValue();

        assertNotNull(copyInfo.getVersionId());
        assertNotEquals(blobItemV1.getVersionId(), copyInfo.getVersionId());
    }

    @Test
    public void copyFromUrlBlobsWithVersion() {
        BlockBlobItem blobItemV1 = blobClient.getBlockBlobAsyncClient().upload(DATA.getDefaultFlux(),
            DATA.getDefaultDataSize()).block();
        BlobAsyncClient sourceBlob = blobContainerClient.getBlobAsyncClient(generateBlobName());
        sourceBlob.getBlockBlobAsyncClient().upload(DATA.getDefaultFlux(), DATA.getDefaultDataSize()).block();

        String sas = sourceBlob.generateSas(new BlobServiceSasSignatureValues(testResourceNamer.now().plusDays(1),
            new BlobSasPermission().setTagsPermission(true).setReadPermission(true)));
        StepVerifier.create(blobClient.copyFromUrlWithResponse(sourceBlob.getBlobUrl() + "?" + sas,
            null, null, null, null))
            .assertNext(r -> {
                assertNotNull(r.getHeaders().getValue(X_MS_VERSION_ID));
                assertNotEquals(blobItemV1.getVersionId(), r.getHeaders().getValue(X_MS_VERSION_ID));
            })
            .verifyComplete();
    }

    @Test
    public void setTierWithVersion() {
        Flux<ByteBuffer> inputV1 = Flux.just(ByteBuffer.wrap(contentV1.getBytes(StandardCharsets.UTF_8)));
        Flux<ByteBuffer> inputV2 = Flux.just(ByteBuffer.wrap(contentV2.getBytes(StandardCharsets.UTF_8)));
        BlockBlobItem blobItemV1 = blobClient.getBlockBlobAsyncClient().upload(inputV1, contentV1.length()).block();
        blobClient.getBlockBlobAsyncClient().upload(inputV2, contentV2.length(), true).block();

        blobClient.getVersionClient(blobItemV1.getVersionId()).setAccessTier(AccessTier.COOL).block();

        StepVerifier.create(blobClient.getVersionClient(blobItemV1.getVersionId()).getProperties())
            .assertNext(r -> assertEquals(AccessTier.COOL, r.getAccessTier()))
            .verifyComplete();

        StepVerifier.create(blobClient.getProperties())
            .assertNext(r -> assertNotEquals(AccessTier.COOL, r.getAccessTier()))
            .verifyComplete();
    }

    @Test
    public void setTierWithVersionError() {
        Flux<ByteBuffer> inputV1 = Flux.just(ByteBuffer.wrap(contentV1.getBytes(StandardCharsets.UTF_8)));
        blobClient.getBlockBlobAsyncClient().upload(inputV1, contentV1.length());
        String fakeVersion = "2020-04-17T20:37:16.5129130Z";

        StepVerifier.create(blobClient.getVersionClient(fakeVersion).setAccessTier(AccessTier.COOL))
            .verifyError(BlobStorageException.class);
    }

    @Test
    public void blobPropertiesShouldContainVersionInformation() {
        BlockBlobItem blobItemV1 = blobClient.getBlockBlobAsyncClient().upload(DATA.getDefaultFlux(),
            DATA.getDefaultDataSize()).block();
        BlockBlobItem blobItemV2 = blobClient.getBlockBlobAsyncClient().upload(DATA.getDefaultFlux(),
            DATA.getDefaultDataSize(), true).block();

        StepVerifier.create(blobClient.getVersionClient(blobItemV1.getVersionId()).getProperties())
            .assertNext(r -> {
                assertEquals(r.getVersionId(), blobItemV1.getVersionId());
                assertNull(r.isCurrentVersion());
            })
            .verifyComplete();

        StepVerifier.create(blobClient.getVersionClient(blobItemV2.getVersionId()).getProperties())
            .assertNext(r -> {
                assertEquals(r.getVersionId(), blobItemV2.getVersionId());
                assertTrue(r.isCurrentVersion());
            })
            .verifyComplete();
    }

    @Test
    public void doNotLookForSnapshotOfVersion() {
        blobClient.getBlockBlobAsyncClient().upload(DATA.getDefaultFlux(), DATA.getDefaultDataSize()).block();

        assertThrows(IllegalArgumentException.class, () -> blobClient.getVersionClient("a").getSnapshotClient("b"));
    }

    @Test
    public void doNotLookForVersionOfSnapshot() {
        blobClient.getBlockBlobAsyncClient().upload(DATA.getDefaultFlux(), DATA.getDefaultDataSize()).block();

        assertThrows(IllegalArgumentException.class, () -> blobClient.getSnapshotClient("a").getVersionClient("b"));
    }

    @Test
    public void snapshotCreatesNewVersion() {
        BlockBlobItem blobItemV1 = blobClient.getBlockBlobAsyncClient().upload(DATA.getDefaultFlux(),
            DATA.getDefaultDataSize()).block();

        StepVerifier.create(blobClient.createSnapshotWithResponse(null, null))
            .assertNext(r -> {
                assertNotNull(r.getHeaders().getValue(X_MS_VERSION_ID));
                assertNotEquals(blobItemV1.getVersionId(), r.getHeaders().getValue(X_MS_VERSION_ID));
            })
            .verifyComplete();
    }

    @Test
    public void versionedBlobURLContainsVersion() {
        BlockBlobItem blobItem = blobClient.getBlockBlobAsyncClient().upload(DATA.getDefaultFlux(),
            DATA.getDefaultDataSize()).block();

        String blobUrl = blobClient.getVersionClient(blobItem.getVersionId()).getBlobUrl();

        assertTrue(blobUrl.contains(blobItem.getVersionId()));
    }
}
