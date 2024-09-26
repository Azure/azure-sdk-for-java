// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.test.utils.TestUtils;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.polling.SyncPoller;
import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.AppendBlobItem;
import com.azure.storage.blob.models.BlobCopyInfo;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlobListDetails;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.models.BlockBlobItem;
import com.azure.storage.blob.models.ListBlobsOptions;
import com.azure.storage.blob.models.PageBlobItem;
import com.azure.storage.blob.sas.BlobContainerSasPermission;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.azure.storage.common.sas.AccountSasPermission;
import com.azure.storage.common.sas.AccountSasResourceType;
import com.azure.storage.common.sas.AccountSasService;
import com.azure.storage.common.sas.AccountSasSignatureValues;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class VersioningTests extends BlobTestBase {
    private BlobContainerClient blobContainerClient;
    private BlobClient blobClient;
    private final String contentV1 = "contentV1";
    private final String contentV2 = "contentV2";

    @BeforeEach
    public void setup() {
        String blobName = generateBlobName();
        String containerName = generateContainerName();
        blobContainerClient = versionedBlobServiceClient.createBlobContainer(containerName);
        blobClient = blobContainerClient.getBlobClient(blobName);
    }

    @AfterEach
    public void cleanup() {
        blobContainerClient.delete();
    }

    @Test
    public void createBlockBlobWithVersion() {
        BlockBlobItem blobItemV1 = blobClient.getBlockBlobClient().upload(DATA.getDefaultInputStream(),
            DATA.getDefaultDataSize());
        BlockBlobItem blobItemV2 = blobClient.getBlockBlobClient().upload(DATA.getDefaultInputStream(),
            DATA.getDefaultDataSize(), true);

        assertNotNull(blobItemV1.getVersionId());
        assertNotNull(blobItemV2.getVersionId());
        assertNotEquals(blobItemV1.getVersionId(), blobItemV2.getVersionId());
    }

    @Test
    public void createPageBlobWithVersion() {
        PageBlobItem blobItemV1 = blobClient.getPageBlobClient().create(512);
        PageBlobItem blobItemV2 = blobClient.getPageBlobClient().create(512, true);

        assertNotNull(blobItemV1.getVersionId());
        assertNotNull(blobItemV2.getVersionId());
        assertNotEquals(blobItemV1.getVersionId(), blobItemV2.getVersionId());
    }

    @Test
    public void createAppendBlobWithVersion() {
        AppendBlobItem blobItemV1 = blobClient.getAppendBlobClient().create();
        AppendBlobItem blobItemV2 = blobClient.getAppendBlobClient().create(true);

        assertNotNull(blobItemV1.getVersionId());
        assertNotNull(blobItemV2.getVersionId());
        assertNotEquals(blobItemV1.getVersionId(), blobItemV2.getVersionId());
    }

    @Test
    public void downloadBlobByVersion() {
        ByteArrayInputStream inputV1 = new ByteArrayInputStream(contentV1.getBytes(StandardCharsets.UTF_8));
        ByteArrayInputStream inputV2 = new ByteArrayInputStream(contentV2.getBytes(StandardCharsets.UTF_8));
        BlockBlobItem blobItemV1 = blobClient.getBlockBlobClient().upload(inputV1, inputV1.available());
        BlockBlobItem blobItemV2 = blobClient.getBlockBlobClient().upload(inputV2, inputV2.available(), true);

        ByteArrayOutputStream outputV1 = new ByteArrayOutputStream();
        ByteArrayOutputStream outputV2 = new ByteArrayOutputStream();
        blobClient.getVersionClient(blobItemV1.getVersionId()).download(outputV1);
        blobClient.getVersionClient(blobItemV2.getVersionId()).download(outputV2);

        TestUtils.assertArraysEqual(outputV1.toByteArray(), contentV1.getBytes(StandardCharsets.UTF_8));
        TestUtils.assertArraysEqual(outputV2.toByteArray(), contentV2.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    public void downloadBlobByVersionStreaming() {
        ByteArrayInputStream inputV1 = new ByteArrayInputStream(contentV1.getBytes(StandardCharsets.UTF_8));
        ByteArrayInputStream inputV2 = new ByteArrayInputStream(contentV2.getBytes(StandardCharsets.UTF_8));
        BlockBlobItem blobItemV1 = blobClient.getBlockBlobClient().upload(inputV1, inputV1.available());
        BlockBlobItem blobItemV2 = blobClient.getBlockBlobClient().upload(inputV2, inputV2.available(), true);

        ByteArrayOutputStream outputV1 = new ByteArrayOutputStream();
        ByteArrayOutputStream outputV2 = new ByteArrayOutputStream();
        blobClient.getVersionClient(blobItemV1.getVersionId()).downloadStream(outputV1);
        blobClient.getVersionClient(blobItemV2.getVersionId()).downloadStream(outputV2);

        TestUtils.assertArraysEqual(outputV1.toByteArray(), contentV1.getBytes(StandardCharsets.UTF_8));
        TestUtils.assertArraysEqual(outputV2.toByteArray(), contentV2.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    public void downloadBlobByVersionBinaryData() {
        ByteArrayInputStream inputV1 = new ByteArrayInputStream(contentV1.getBytes(StandardCharsets.UTF_8));
        ByteArrayInputStream inputV2 = new ByteArrayInputStream(contentV2.getBytes(StandardCharsets.UTF_8));
        BlockBlobItem blobItemV1 = blobClient.getBlockBlobClient().upload(inputV1, inputV1.available());
        BlockBlobItem blobItemV2 = blobClient.getBlockBlobClient().upload(inputV2, inputV2.available(), true);

        BinaryData outputV1 = blobClient.getVersionClient(blobItemV1.getVersionId()).downloadContent();
        BinaryData outputV2 = blobClient.getVersionClient(blobItemV2.getVersionId()).downloadContent();

        assertEquals(outputV1.toString(), contentV1);
        assertEquals(outputV2.toString(), contentV2);
    }

    @Test
    public void deleteBlobByVersion() {
        ByteArrayInputStream inputV1 = new ByteArrayInputStream(contentV1.getBytes(StandardCharsets.UTF_8));
        ByteArrayInputStream inputV2 = new ByteArrayInputStream(contentV2.getBytes(StandardCharsets.UTF_8));
        BlockBlobItem blobItemV1 = blobClient.getBlockBlobClient().upload(inputV1, inputV1.available());
        BlockBlobItem blobItemV2 = blobClient.getBlockBlobClient().upload(inputV2, inputV2.available(), true);

        blobClient.getVersionClient(blobItemV1.getVersionId()).delete();

        assertFalse(blobClient.getVersionClient(blobItemV1.getVersionId()).exists());
        assertTrue(blobClient.getVersionClient(blobItemV2.getVersionId()).exists());
    }

    @Test
    public void deleteBlobByVersionUsingSASToken() {
        ByteArrayInputStream inputV1 = new ByteArrayInputStream(contentV1.getBytes(StandardCharsets.UTF_8));
        ByteArrayInputStream inputV2 = new ByteArrayInputStream(contentV2.getBytes(StandardCharsets.UTF_8));
        BlockBlobItem blobItemV1 = blobClient.getBlockBlobClient().upload(inputV1, inputV1.available());
        BlockBlobItem blobItemV2 = blobClient.getBlockBlobClient().upload(inputV2, inputV2.available(), true);

        BlobSasPermission permission = new BlobSasPermission()
            .setDeleteVersionPermission(true);
        String sasToken = blobClient.getVersionClient(blobItemV1.getVersionId())
            .generateSas(new BlobServiceSasSignatureValues(testResourceNamer.now().plusDays(1), permission));

        BlobClient sasClient = getBlobClient(blobClient.getVersionClient(blobItemV1.getVersionId()).getBlobUrl(),
            sasToken);

        sasClient.delete();

        assertFalse(blobClient.getVersionClient(blobItemV1.getVersionId()).exists());
        assertTrue(blobClient.getVersionClient(blobItemV2.getVersionId()).exists());
    }

    @Test
    public void deleteBlobByVersionUsingContainerSASToken() {
        ByteArrayInputStream inputV1 = new ByteArrayInputStream(contentV1.getBytes(StandardCharsets.UTF_8));
        ByteArrayInputStream inputV2 = new ByteArrayInputStream(contentV2.getBytes(StandardCharsets.UTF_8));
        BlockBlobItem blobItemV1 = blobClient.getBlockBlobClient().upload(inputV1, inputV1.available());
        BlockBlobItem blobItemV2 = blobClient.getBlockBlobClient().upload(inputV2, inputV2.available(), true);

        BlobSasPermission permission = new BlobSasPermission()
            .setDeleteVersionPermission(true);
        String sasToken = blobContainerClient.generateSas(
            new BlobServiceSasSignatureValues(testResourceNamer.now().plusDays(1), permission));

        BlobClient sasClient = getBlobClient(blobClient.getVersionClient(blobItemV1.getVersionId()).getBlobUrl(),
            sasToken);

        sasClient.delete();

        assertFalse(blobClient.getVersionClient(blobItemV1.getVersionId()).exists());
        assertTrue(blobClient.getVersionClient(blobItemV2.getVersionId()).exists());
    }

    @Test
    public void deleteBlobByVersionUsingAccountSASToken() {
        ByteArrayInputStream inputV1 = new ByteArrayInputStream(contentV1.getBytes(StandardCharsets.UTF_8));
        ByteArrayInputStream inputV2 = new ByteArrayInputStream(contentV2.getBytes(StandardCharsets.UTF_8));
        BlockBlobItem blobItemV1 = blobClient.getBlockBlobClient().upload(inputV1, inputV1.available());
        BlockBlobItem blobItemV2 = blobClient.getBlockBlobClient().upload(inputV2, inputV2.available(), true);

        AccountSasPermission permission = new AccountSasPermission()
            .setDeleteVersionPermission(true);
        String sasToken = versionedBlobServiceClient.generateAccountSas(new AccountSasSignatureValues(
            testResourceNamer.now().plusDays(1), permission, new AccountSasService().setBlobAccess(true),
            new AccountSasResourceType().setObject(true)));

        BlobClient sasClient = getBlobClient(blobClient.getVersionClient(blobItemV1.getVersionId()).getBlobUrl(),
            sasToken);

        sasClient.delete();

        assertFalse(blobClient.getVersionClient(blobItemV1.getVersionId()).exists());
        assertTrue(blobClient.getVersionClient(blobItemV2.getVersionId()).exists());
    }

    @Test
    public void getBlobPropertiesByVersion() {
        String key = "key";
        String valV2 = "val2";
        String valV3 = "val3";
        BlockBlobItem blobItemV1 = blobClient.getBlockBlobClient().upload(DATA.getDefaultInputStream(),
            DATA.getDefaultDataSize());
        Response<Void> responseV2 = blobClient.getBlockBlobClient()
            .setMetadataWithResponse(Collections.singletonMap(key, valV2), null, null, Context.NONE);
        Response<Void> responseV3 = blobClient.getBlockBlobClient()
            .setMetadataWithResponse(Collections.singletonMap(key, valV3), null, null, Context.NONE);
        String versionId1 = blobItemV1.getVersionId();
        String versionId2 = responseV2.getHeaders().getValue(X_MS_VERSION_ID);
        String versionId3 = responseV3.getHeaders().getValue(X_MS_VERSION_ID);

        String receivedValV1 = blobClient.getVersionClient(versionId1).getProperties().getMetadata().get(key);
        String receivedValV2 = blobClient.getVersionClient(versionId2).getProperties().getMetadata().get(key);
        String receivedValV3 = blobClient.getVersionClient(versionId3).getProperties().getMetadata().get(key);

        assertNull(receivedValV1);
        assertEquals(valV2, receivedValV2);
        assertEquals(valV3, receivedValV3);
    }

    @Test
    public void listBlobsWithVersion() {
        BlockBlobItem blobItemV1 = blobClient.getBlockBlobClient().upload(DATA.getDefaultInputStream(),
            DATA.getDefaultDataSize());
        BlockBlobItem blobItemV2 = blobClient.getBlockBlobClient().upload(DATA.getDefaultInputStream(),
            DATA.getDefaultDataSize(), true);
        BlockBlobItem blobItemV3 = blobClient.getBlockBlobClient().upload(DATA.getDefaultInputStream(),
            DATA.getDefaultDataSize(), true);

        PagedIterable<BlobItem> blobs = blobContainerClient.listBlobs(
            new ListBlobsOptions().setDetails(new BlobListDetails().setRetrieveVersions(true)), null);

        Iterator<BlobItem> iterator = blobs.stream().iterator();
        assertEquals(3, blobs.stream().count());
        BlobItem resultingBlobItem1 = iterator.next();
        assertEquals(blobItemV1.getVersionId(), resultingBlobItem1.getVersionId());
        assertNull(resultingBlobItem1.isCurrentVersion());
        BlobItem resultingBlobItem2 = iterator.next();
        assertEquals(blobItemV2.getVersionId(), resultingBlobItem2.getVersionId());
        assertNull(resultingBlobItem2.isCurrentVersion());
        BlobItem resultingBlobItem3 = iterator.next();
        assertEquals(blobItemV3.getVersionId(), resultingBlobItem3.getVersionId());
        assertTrue(resultingBlobItem3.isCurrentVersion());
    }

    @Test
    public void listBlobsWithoutVersion() {
        blobClient.getBlockBlobClient().upload(DATA.getDefaultInputStream(), DATA.getDefaultDataSize());
        blobClient.getBlockBlobClient().upload(DATA.getDefaultInputStream(), DATA.getDefaultDataSize(), true);
        BlockBlobItem blobItemV3 = blobClient.getBlockBlobClient().upload(DATA.getDefaultInputStream(),
            DATA.getDefaultDataSize(), true);

        PagedIterable<BlobItem> blobs = blobContainerClient.listBlobs(
            new ListBlobsOptions().setDetails(new BlobListDetails().setRetrieveVersions(false)), null);

        assertEquals(1, blobs.stream().count());
        assertEquals(blobItemV3.getVersionId(), blobs.stream().iterator().next().getVersionId());
    }

    @Test
    public void beginCopyBlobsWithVersion() {
        BlockBlobItem blobItemV1 = blobClient.getBlockBlobClient().upload(DATA.getDefaultInputStream(),
            DATA.getDefaultDataSize());
        BlobClient sourceBlob = blobContainerClient.getBlobClient(generateBlobName());
        sourceBlob.getBlockBlobClient().upload(DATA.getDefaultInputStream(), DATA.getDefaultDataSize());

        SyncPoller<BlobCopyInfo, Void> poller = setPlaybackSyncPollerPollInterval(
            blobClient.beginCopy(sourceBlob.getBlobUrl(), null));
        BlobCopyInfo copyInfo = poller.waitForCompletion().getValue();

        assertNotNull(copyInfo.getVersionId());
        assertNotEquals(blobItemV1.getVersionId(), copyInfo.getVersionId());
    }

    @Test
    public void copyFromUrlBlobsWithVersion() {
        BlockBlobItem blobItemV1 = blobClient.getBlockBlobClient().upload(DATA.getDefaultInputStream(),
            DATA.getDefaultDataSize());
        BlobClient sourceBlob = blobContainerClient.getBlobClient(generateBlobName());
        sourceBlob.getBlockBlobClient().upload(DATA.getDefaultInputStream(), DATA.getDefaultDataSize());

        String sas = sourceBlob.generateSas(new BlobServiceSasSignatureValues(testResourceNamer.now().plusDays(1),
            new BlobContainerSasPermission().setReadPermission(true)));
        Response<String> response = blobClient.copyFromUrlWithResponse(sourceBlob.getBlobUrl() + "?" + sas,
            null, null, null, null, null, Context.NONE);
        String versionIdAfterCopy = response.getHeaders().getValue(X_MS_VERSION_ID);

        assertNotNull(versionIdAfterCopy);
        assertNotEquals(blobItemV1.getVersionId(), versionIdAfterCopy);
    }

    @Test
    public void setTierWithVersion() {
        ByteArrayInputStream inputV1 = new ByteArrayInputStream(contentV1.getBytes(StandardCharsets.UTF_8));
        ByteArrayInputStream inputV2 = new ByteArrayInputStream(contentV2.getBytes(StandardCharsets.UTF_8));
        BlockBlobItem blobItemV1 = blobClient.getBlockBlobClient().upload(inputV1, inputV1.available());
        blobClient.getBlockBlobClient().upload(inputV2, inputV2.available(), true);

        blobClient.getVersionClient(blobItemV1.getVersionId()).setAccessTier(AccessTier.COOL);

        assertEquals(AccessTier.COOL, blobClient.getVersionClient(blobItemV1.getVersionId()).getProperties()
            .getAccessTier());
        assertNotEquals(AccessTier.COOL, blobClient.getProperties().getAccessTier());
    }

    @Test
    public void setTierWithVersionError() {
        ByteArrayInputStream inputV1 = new ByteArrayInputStream(contentV1.getBytes(StandardCharsets.UTF_8));
        blobClient.getBlockBlobClient().upload(inputV1, inputV1.available());
        String fakeVersion = "2020-04-17T20:37:16.5129130Z";

        assertThrows(BlobStorageException.class, () ->
                blobClient.getVersionClient(fakeVersion).setAccessTier(AccessTier.COOL));
    }

    @Test
    public void blobPropertiesShouldContainVersionInformation() {
        BlockBlobItem blobItemV1 = blobClient.getBlockBlobClient().upload(DATA.getDefaultInputStream(),
            DATA.getDefaultDataSize());
        BlockBlobItem blobItemV2 = blobClient.getBlockBlobClient().upload(DATA.getDefaultInputStream(),
            DATA.getDefaultDataSize(), true);

        BlobProperties propertiesV1 = blobClient.getVersionClient(blobItemV1.getVersionId()).getProperties();
        BlobProperties propertiesV2 = blobClient.getVersionClient(blobItemV2.getVersionId()).getProperties();

        assertEquals(propertiesV1.getVersionId(), blobItemV1.getVersionId());
        assertEquals(propertiesV2.getVersionId(), blobItemV2.getVersionId());
        assertNull(propertiesV1.isCurrentVersion());
        assertTrue(propertiesV2.isCurrentVersion());
    }

    @Test
    public void doNotLookForSnapshotOfVersion() {
        blobClient.getBlockBlobClient().upload(DATA.getDefaultInputStream(), DATA.getDefaultDataSize());

        assertThrows(IllegalArgumentException.class, () -> blobClient.getVersionClient("a").getSnapshotClient("b"));
    }

    @Test
    public void doNotLookForVersionOfSnapshot() {
        blobClient.getBlockBlobClient().upload(DATA.getDefaultInputStream(), DATA.getDefaultDataSize());

        assertThrows(IllegalArgumentException.class, () -> blobClient.getSnapshotClient("a").getVersionClient("b"));
    }

    @Test
    public void snapshotCreatesNewVersion() {
        BlockBlobItem blobItemV1 = blobClient.getBlockBlobClient().upload(DATA.getDefaultInputStream(),
            DATA.getDefaultDataSize());

        String versionIdAfterSnapshot = blobClient.createSnapshotWithResponse(null, null,
            null, Context.NONE).getHeaders().getValue(X_MS_VERSION_ID);

        assertNotNull(versionIdAfterSnapshot);
        assertNotEquals(blobItemV1.getVersionId(), versionIdAfterSnapshot);
    }

    @Test
    public void versionedBlobURLContainsVersion() {
        BlockBlobItem blobItem = blobClient.getBlockBlobClient().upload(DATA.getDefaultInputStream(),
            DATA.getDefaultDataSize());

        String blobUrl = blobClient.getVersionClient(blobItem.getVersionId()).getBlobUrl();

        assertTrue(blobUrl.contains(blobItem.getVersionId()));
    }
}
