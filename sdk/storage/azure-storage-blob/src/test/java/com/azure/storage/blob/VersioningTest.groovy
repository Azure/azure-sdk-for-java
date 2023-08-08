// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob

import com.azure.core.util.BinaryData
import com.azure.core.util.Context
import com.azure.storage.blob.models.AccessTier
import com.azure.storage.blob.models.BlobListDetails
import com.azure.storage.blob.models.BlobStorageException
import com.azure.storage.blob.models.ListBlobsOptions
import com.azure.storage.blob.models.PublicAccessType
import com.azure.storage.blob.sas.BlobSasPermission
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues
import com.azure.storage.common.sas.AccountSasPermission
import com.azure.storage.common.sas.AccountSasResourceType
import com.azure.storage.common.sas.AccountSasService
import com.azure.storage.common.sas.AccountSasSignatureValues
import com.azure.storage.common.test.shared.extensions.RequiredServiceVersion
import org.apache.commons.lang3.StringUtils

import java.nio.charset.StandardCharsets

@RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "V2019_12_12")
class VersioningTest extends APISpec {

    BlobContainerClient blobContainerClient
    BlobClient blobClient
    String blobName
    String containerName
    String contentV1 = "contentV1"
    String contentV2 = "contentV2"

    def setup() {
        blobName = generateBlobName()
        containerName = generateContainerName()
        blobContainerClient = versionedBlobServiceClient.createBlobContainer(containerName)
        blobClient = blobContainerClient.getBlobClient(blobName)
    }

    def cleanup() {
        blobContainerClient.delete()
    }

    def "Create Block Blob with Version"() {
        when:
        def blobItemV1 = blobClient.getBlockBlobClient().upload(data.defaultInputStream, data.defaultDataSize)
        def blobItemV2 = blobClient.getBlockBlobClient().upload(data.defaultInputStream, data.defaultDataSize, true)

        then:
        blobItemV1.getVersionId() != null
        blobItemV2.getVersionId() != null
        !StringUtils.equals(blobItemV1.getVersionId(), blobItemV2.getVersionId())
    }

    def "Create Page Blob with Version"() {
        when:
        def blobItemV1 = blobClient.getPageBlobClient().create(512)
        def blobItemV2 = blobClient.getPageBlobClient().create(512, true)

        then:
        blobItemV1.getVersionId() != null
        blobItemV2.getVersionId() != null
        !StringUtils.equals(blobItemV1.getVersionId(), blobItemV2.getVersionId())
    }

    def "Create Append Blob with Version"() {
        when:
        def blobItemV1 = blobClient.getAppendBlobClient().create()
        def blobItemV2 = blobClient.getAppendBlobClient().create(true)

        then:
        blobItemV1.getVersionId() != null
        blobItemV2.getVersionId() != null
        !StringUtils.equals(blobItemV1.getVersionId(), blobItemV2.getVersionId())
    }

    def "Download Blob by Version"() {
        given:
        def inputV1 = new ByteArrayInputStream(contentV1.getBytes(StandardCharsets.UTF_8))
        def inputV2 = new ByteArrayInputStream(contentV2.getBytes(StandardCharsets.UTF_8))
        def blobItemV1 = blobClient.getBlockBlobClient().upload(inputV1, inputV1.available())
        def blobItemV2 = blobClient.getBlockBlobClient().upload(inputV2, inputV2.available(), true)

        when:
        def outputV1 = new ByteArrayOutputStream()
        def outputV2 = new ByteArrayOutputStream()
        blobClient.getVersionClient(blobItemV1.getVersionId()).download(outputV1)
        blobClient.getVersionClient(blobItemV2.getVersionId()).download(outputV2)

        then:
        outputV1.toByteArray() == contentV1.getBytes(StandardCharsets.UTF_8)
        outputV2.toByteArray() == contentV2.getBytes(StandardCharsets.UTF_8)
    }

    def "Download Blob by Version Streaming"() {
        given:
        def inputV1 = new ByteArrayInputStream(contentV1.getBytes(StandardCharsets.UTF_8))
        def inputV2 = new ByteArrayInputStream(contentV2.getBytes(StandardCharsets.UTF_8))
        def blobItemV1 = blobClient.getBlockBlobClient().upload(inputV1, inputV1.available())
        def blobItemV2 = blobClient.getBlockBlobClient().upload(inputV2, inputV2.available(), true)

        when:
        def outputV1 = new ByteArrayOutputStream()
        def outputV2 = new ByteArrayOutputStream()
        blobClient.getVersionClient(blobItemV1.getVersionId()).downloadStream(outputV1)
        blobClient.getVersionClient(blobItemV2.getVersionId()).downloadStream(outputV2)

        then:
        outputV1.toByteArray() == contentV1.getBytes(StandardCharsets.UTF_8)
        outputV2.toByteArray() == contentV2.getBytes(StandardCharsets.UTF_8)
    }

    def "Download Blob by Version Binary Data"() {
        given:
        def inputV1 = new ByteArrayInputStream(contentV1.getBytes(StandardCharsets.UTF_8))
        def inputV2 = new ByteArrayInputStream(contentV2.getBytes(StandardCharsets.UTF_8))
        def blobItemV1 = blobClient.getBlockBlobClient().upload(inputV1, inputV1.available())
        def blobItemV2 = blobClient.getBlockBlobClient().upload(inputV2, inputV2.available(), true)

        when:
        def outputV1 = blobClient.getVersionClient(blobItemV1.getVersionId()).downloadContent()
        def outputV2 = blobClient.getVersionClient(blobItemV2.getVersionId()).downloadContent()

        then:
        outputV1.toString() == contentV1
        outputV2.toString() == contentV2
    }

    def "Delete Blob by Version"() {
        given:
        def inputV1 = new ByteArrayInputStream(contentV1.getBytes(StandardCharsets.UTF_8))
        def inputV2 = new ByteArrayInputStream(contentV2.getBytes(StandardCharsets.UTF_8))
        def blobItemV1 = blobClient.getBlockBlobClient().upload(inputV1, inputV1.available())
        def blobItemV2 = blobClient.getBlockBlobClient().upload(inputV2, inputV2.available(), true)

        when:
        blobClient.getVersionClient(blobItemV1.getVersionId()).delete()

        then:
        !blobClient.getVersionClient(blobItemV1.getVersionId()).exists()
        blobClient.getVersionClient(blobItemV2.getVersionId()).exists()
    }

    def "Delete Blob by Version using SAS token"() {
        given:
        def inputV1 = new ByteArrayInputStream(contentV1.getBytes(StandardCharsets.UTF_8))
        def inputV2 = new ByteArrayInputStream(contentV2.getBytes(StandardCharsets.UTF_8))
        def blobItemV1 = blobClient.getBlockBlobClient().upload(inputV1, inputV1.available())
        def blobItemV2 = blobClient.getBlockBlobClient().upload(inputV2, inputV2.available(), true)

        def permission = new BlobSasPermission()
            .setDeleteVersionPermission(true)
        def sasToken = blobClient.getVersionClient(blobItemV1.getVersionId())
            .generateSas(new BlobServiceSasSignatureValues(namer.getUtcNow().plusDays(1), permission))

        def sasClient = getBlobClient(blobClient.getVersionClient(blobItemV1.getVersionId()).getBlobUrl(), sasToken)

        when:
        sasClient.delete()

        then:
        !blobClient.getVersionClient(blobItemV1.getVersionId()).exists()
        blobClient.getVersionClient(blobItemV2.getVersionId()).exists()
    }

    def "Delete Blob by Version using container SAS token"() {
        given:
        def inputV1 = new ByteArrayInputStream(contentV1.getBytes(StandardCharsets.UTF_8))
        def inputV2 = new ByteArrayInputStream(contentV2.getBytes(StandardCharsets.UTF_8))
        def blobItemV1 = blobClient.getBlockBlobClient().upload(inputV1, inputV1.available())
        def blobItemV2 = blobClient.getBlockBlobClient().upload(inputV2, inputV2.available(), true)

        def permission = new BlobSasPermission()
            .setDeleteVersionPermission(true)
        def sasToken = blobContainerClient
            .generateSas(new BlobServiceSasSignatureValues(namer.getUtcNow().plusDays(1), permission))

        def sasClient = getBlobClient(blobClient.getVersionClient(blobItemV1.getVersionId()).getBlobUrl(), sasToken)

        when:
        sasClient.delete()

        then:
        !blobClient.getVersionClient(blobItemV1.getVersionId()).exists()
        blobClient.getVersionClient(blobItemV2.getVersionId()).exists()
    }

    def "Delete Blob by Version using account SAS token"() {
        given:
        def inputV1 = new ByteArrayInputStream(contentV1.getBytes(StandardCharsets.UTF_8))
        def inputV2 = new ByteArrayInputStream(contentV2.getBytes(StandardCharsets.UTF_8))
        def blobItemV1 = blobClient.getBlockBlobClient().upload(inputV1, inputV1.available())
        def blobItemV2 = blobClient.getBlockBlobClient().upload(inputV2, inputV2.available(), true)

        def permission = new AccountSasPermission()
           .setDeleteVersionPermission(true)
        def sasToken = versionedBlobServiceClient.generateAccountSas(new AccountSasSignatureValues(
            namer.getUtcNow().plusDays(1), permission, new AccountSasService().setBlobAccess(true),
            new AccountSasResourceType().setObject(true)
        ))

        def sasClient = getBlobClient(blobClient.getVersionClient(blobItemV1.getVersionId()).getBlobUrl(), sasToken)

        when:
        sasClient.delete()

        then:
        !blobClient.getVersionClient(blobItemV1.getVersionId()).exists()
        blobClient.getVersionClient(blobItemV2.getVersionId()).exists()
    }

    def "Get Blob Properties by Version"() {
        given:
        def key = "key"
        def valV2 = "val2"
        def valV3 = "val3"
        def blobItemV1 = blobClient.getBlockBlobClient().upload(data.defaultInputStream, data.defaultDataSize)
        def responseV2 = blobClient.getBlockBlobClient().setMetadataWithResponse(Collections.singletonMap(key, valV2), null, null, Context.NONE)
        def responseV3 = blobClient.getBlockBlobClient().setMetadataWithResponse(Collections.singletonMap(key, valV3), null, null, Context.NONE)
        def versionId1 = blobItemV1.getVersionId()
        def versionId2 = responseV2.getHeaders().getValue("x-ms-version-id")
        def versionId3 = responseV3.getHeaders().getValue("x-ms-version-id")

        when:
        def receivedValV1 = blobClient.getVersionClient(versionId1).getProperties().getMetadata().get(key)
        def receivedValV2 = blobClient.getVersionClient(versionId2).getProperties().getMetadata().get(key)
        def receivedValV3 = blobClient.getVersionClient(versionId3).getProperties().getMetadata().get(key)

        then:
        receivedValV1 == null
        valV2 == receivedValV2
        valV3 == receivedValV3
    }

    def "List Blobs with Version"() {
        given:
        def blobItemV1 = blobClient.getBlockBlobClient().upload(data.defaultInputStream, data.defaultDataSize)
        def blobItemV2 = blobClient.getBlockBlobClient().upload(data.defaultInputStream, data.defaultDataSize, true)
        def blobItemV3 = blobClient.getBlockBlobClient().upload(data.defaultInputStream, data.defaultDataSize, true)

        when:
        def blobs = blobContainerClient.listBlobs(new ListBlobsOptions().setDetails(new BlobListDetails().setRetrieveVersions(true)), null)

        then:
        blobs.size() == 3
        blobs[0].getVersionId() == blobItemV1.getVersionId()
        blobs[1].getVersionId() == blobItemV2.getVersionId()
        blobs[2].getVersionId() == blobItemV3.getVersionId()
        blobs[0].isCurrentVersion() == null
        blobs[1].isCurrentVersion() == null
        blobs[2].isCurrentVersion()
    }

    def "List Blobs without Version"() {
        given:
        blobClient.getBlockBlobClient().upload(data.defaultInputStream, data.defaultDataSize)
        blobClient.getBlockBlobClient().upload(data.defaultInputStream, data.defaultDataSize, true)
        def blobItemV3 = blobClient.getBlockBlobClient().upload(data.defaultInputStream, data.defaultDataSize, true)

        when:
        def blobs = blobContainerClient.listBlobs(new ListBlobsOptions().setDetails(new BlobListDetails().setRetrieveVersions(false)), null)

        then:
        blobs.size() == 1
        blobs[0].getVersionId() == blobItemV3.getVersionId()
    }

    def "Begin Copy Blobs with Version"() {
        given:
        def blobItemV1 = blobClient.getBlockBlobClient().upload(data.defaultInputStream, data.defaultDataSize)
        def sourceBlob = blobContainerClient.getBlobClient(generateBlobName())
        sourceBlob.getBlockBlobClient().upload(data.defaultInputStream, data.defaultDataSize)

        when:
        def poller = blobClient.beginCopy(sourceBlob.getBlobUrl(), getPollingDuration(1000))
        def copyInfo = poller.waitForCompletion().getValue()

        then:
        copyInfo.getVersionId() != null
        copyInfo.getVersionId() != blobItemV1.getVersionId()
    }

    def "Copy From Url Blobs with Version"() {
        given:
        blobContainerClient.setAccessPolicy(PublicAccessType.CONTAINER, null)
        def blobItemV1 = blobClient.getBlockBlobClient().upload(data.defaultInputStream, data.defaultDataSize)
        def sourceBlob = blobContainerClient.getBlobClient(generateBlobName())
        sourceBlob.getBlockBlobClient().upload(data.defaultInputStream, data.defaultDataSize)

        when:
        def response = blobClient.copyFromUrlWithResponse(sourceBlob.getBlobUrl(), null, null, null, null, null, Context.NONE)
        def versionIdAfterCopy = response.getHeaders().getValue("x-ms-version-id")

        then:
        versionIdAfterCopy != null
        versionIdAfterCopy != blobItemV1.getVersionId()
    }

    def "Set tier with version"() {
        given:
        def inputV1 = new ByteArrayInputStream(contentV1.getBytes(StandardCharsets.UTF_8))
        def inputV2 = new ByteArrayInputStream(contentV2.getBytes(StandardCharsets.UTF_8))
        def blobItemV1 = blobClient.getBlockBlobClient().upload(inputV1, inputV1.available())
        blobClient.getBlockBlobClient().upload(inputV2, inputV2.available(), true)

        when:
        blobClient.getVersionClient(blobItemV1.getVersionId()).setAccessTier(AccessTier.COOL)

        then:
        blobClient.getVersionClient(blobItemV1.getVersionId()).getProperties().getAccessTier() == AccessTier.COOL
        blobClient.getProperties().getAccessTier() != AccessTier.COOL
    }

    def "Set tier with version error"() {
        given:
        def inputV1 = new ByteArrayInputStream(contentV1.getBytes(StandardCharsets.UTF_8))
        blobClient.getBlockBlobClient().upload(inputV1, inputV1.available())
        String fakeVersion = "2020-04-17T20:37:16.5129130Z"

        when:
        blobClient.getVersionClient(fakeVersion).setAccessTier(AccessTier.COOL)

        then:
        thrown(BlobStorageException)
    }

    def "Blob Properties should contain Version information"() {
        given:
        def blobItemV1 = blobClient.getBlockBlobClient().upload(data.defaultInputStream, data.defaultDataSize)
        def blobItemV2 = blobClient.getBlockBlobClient().upload(data.defaultInputStream, data.defaultDataSize, true)

        when:
        def propertiesV1 = blobClient.getVersionClient(blobItemV1.getVersionId()).getProperties()
        def propertiesV2 = blobClient.getVersionClient(blobItemV2.getVersionId()).getProperties()

        then:
        propertiesV1.getVersionId() == blobItemV1.getVersionId()
        propertiesV2.getVersionId() == blobItemV2.getVersionId()
        !propertiesV1.isCurrentVersion()
        propertiesV2.isCurrentVersion()
    }

    def "Do not look for snapshot of version"() {
        given:
        blobClient.getBlockBlobClient().upload(data.defaultInputStream, data.defaultDataSize)

        when:
        blobClient.getVersionClient("a").getSnapshotClient("b")

        then:
        thrown IllegalArgumentException
    }

    def "Do not look for version of snapshot"() {
        given:
        blobClient.getBlockBlobClient().upload(data.defaultInputStream, data.defaultDataSize)

        when:
        blobClient.getSnapshotClient("a").getVersionClient("b")

        then:
        thrown IllegalArgumentException
    }

    def "Snapshot creates new Version"() {
        given:
        def blobItemV1 = blobClient.getBlockBlobClient().upload(data.defaultInputStream, data.defaultDataSize)

        when:
        def versionIdAfterSnapshot = blobClient.createSnapshotWithResponse(null, null, null, Context.NONE)
            .getHeaders().getValue("x-ms-version-id")

        then:
        versionIdAfterSnapshot != null
        versionIdAfterSnapshot != blobItemV1.getVersionId()
    }

    def "Versioned Blob URL contains Version"() {
        given:
        def blobItem = blobClient.getBlockBlobClient().upload(data.defaultInputStream, data.defaultDataSize)

        when:
        def blobUrl = blobClient.getVersionClient(blobItem.getVersionId()).getBlobUrl()

        then:
        blobUrl.contains(blobItem.getVersionId())
    }
}
