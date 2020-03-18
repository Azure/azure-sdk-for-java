// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob

import com.azure.storage.blob.models.PageRange
import com.azure.storage.blob.specialized.PageBlobClient
import org.apache.commons.lang3.StringUtils

import java.nio.charset.StandardCharsets

class VersioningTest extends APISpec {

    BlobContainerClient blobContainerClient
    BlobClient blobClient
    String blobName
    String containerName
    String contentV1 = UUID.randomUUID().toString()
    String contentV2 = UUID.randomUUID().toString()

    def setup() {
        blobName = generateBlobName()
        containerName = generateContainerName()
        blobContainerClient = versionedBlobServiceClient.createBlobContainer(containerName)
        blobClient = blobContainerClient.getBlobClient(blobName)
    }

    def cleanup() {
        blobContainerClient.delete();
    }

    def "Create Block Blob with Version"() {
        when:
        def blobItemV1 = blobClient.getBlockBlobClient().upload(defaultInputStream.get(), defaultDataSize)
        def blobItemV2 = blobClient.getBlockBlobClient().upload(defaultInputStream.get(), defaultDataSize, true)

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
        def blobItemV1 = blobClient.getAppendBlobClient().create();
        def blobItemV2 = blobClient.getAppendBlobClient().create(true);

        then:
        blobItemV1.getVersionId() != null
        blobItemV2.getVersionId() != null
        !StringUtils.equals(blobItemV1.getVersionId(), blobItemV2.getVersionId())
    }

    def "Retrieve Block Blob by Version"() {
        given:
        def inputV1 = new ByteArrayInputStream(contentV1.getBytes(StandardCharsets.UTF_8))
        def inputV2 = new ByteArrayInputStream(contentV2.getBytes(StandardCharsets.UTF_8))
        def blobItemV1 = blobClient.getBlockBlobClient().upload(inputV1, inputV1.available())
        def blobItemV2 = blobClient.getBlockBlobClient().upload(inputV2, inputV2.available(), true)

        when:
        def outputV1 = new ByteArrayOutputStream()
        def outputV2 = new ByteArrayOutputStream()
        blobClient.getVersionClient(blobItemV1.getVersionId()).getBlockBlobClient().download(outputV1)
        blobClient.getVersionClient(blobItemV2.getVersionId()).getBlockBlobClient().download(outputV2)

        then:
        outputV1.toString(StandardCharsets.UTF_8).equals(contentV1)
        outputV2.toString(StandardCharsets.UTF_8).equals(contentV2)
    }

    def "Retrieve Page Blob by Version"() {
        given:
        def contentV1 = getRandomByteArray(PageBlobClient.PAGE_BYTES)
        def contentV2 = getRandomByteArray(PageBlobClient.PAGE_BYTES)
        def inputV1 = new ByteArrayInputStream(contentV1)
        def inputV2 = new ByteArrayInputStream(contentV2)
        def blobItemV1 = blobClient.getPageBlobClient().create(PageBlobClient.PAGE_BYTES)
        blobClient.getPageBlobClient().uploadPages(new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES-1), inputV1)
        def blobItemV2 = blobClient.getPageBlobClient().create(PageBlobClient.PAGE_BYTES, true)
        blobClient.getPageBlobClient().uploadPages(new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES-1), inputV2)

        when:
        def outputV1 = new ByteArrayOutputStream()
        def outputV2 = new ByteArrayOutputStream()
        blobClient.getVersionClient(blobItemV1.getVersionId()).getPageBlobClient().download(outputV1)
        blobClient.getVersionClient(blobItemV2.getVersionId()).getPageBlobClient().download(outputV2)

        then:
        Arrays.equals(contentV1, outputV1.toByteArray())
        Arrays.equals(contentV2, outputV2.toByteArray())
    }

    def "Retrieve Append Blob by Version"() {
        given:
        def inputV1 = new ByteArrayInputStream(contentV1.getBytes(StandardCharsets.UTF_8))
        def inputV2 = new ByteArrayInputStream(contentV2.getBytes(StandardCharsets.UTF_8))
        def blobItemV1 = blobClient.getAppendBlobClient().create()
        blobClient.getAppendBlobClient().appendBlock(inputV1, inputV1.available())
        def blobItemV2 = blobClient.getAppendBlobClient().create(true)
        blobClient.getAppendBlobClient().appendBlock(inputV2, inputV2.available())

        when:
        def outputV1 = new ByteArrayOutputStream()
        def outputV2 = new ByteArrayOutputStream()
        blobClient.getVersionClient(blobItemV1.getVersionId()).getAppendBlobClient().download(outputV1)
        blobClient.getVersionClient(blobItemV2.getVersionId()).getAppendBlobClient().download(outputV2)

        then:
        outputV1.toString(StandardCharsets.UTF_8).equals(contentV1)
        outputV2.toString(StandardCharsets.UTF_8).equals(contentV2)
    }

    def "Delete Block Blob by Version"() {
        given:
        def inputV1 = new ByteArrayInputStream(contentV1.getBytes(StandardCharsets.UTF_8))
        def inputV2 = new ByteArrayInputStream(contentV2.getBytes(StandardCharsets.UTF_8))
        def blobItemV1 = blobClient.getBlockBlobClient().upload(inputV1, inputV1.available())
        def blobItemV2 = blobClient.getBlockBlobClient().upload(inputV2, inputV2.available(), true)

        when:
        blobClient.getVersionClient(blobItemV1.getVersionId()).getBlockBlobClient().delete()

        then:
        !blobClient.getVersionClient(blobItemV1.getVersionId()).exists()
        blobClient.getVersionClient(blobItemV2.getVersionId()).exists()
    }

    def "Delete Page Blob by Version"() {
        given:
        def contentV1 = getRandomByteArray(PageBlobClient.PAGE_BYTES)
        def contentV2 = getRandomByteArray(PageBlobClient.PAGE_BYTES)
        def inputV1 = new ByteArrayInputStream(contentV1)
        def inputV2 = new ByteArrayInputStream(contentV2)
        def blobItemV1 = blobClient.getPageBlobClient().create(PageBlobClient.PAGE_BYTES)
        blobClient.getPageBlobClient().uploadPages(new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES-1), inputV1)
        def blobItemV2 = blobClient.getPageBlobClient().create(PageBlobClient.PAGE_BYTES, true)
        blobClient.getPageBlobClient().uploadPages(new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES-1), inputV2)

        when:
        blobClient.getVersionClient(blobItemV1.getVersionId()).getPageBlobClient().delete()

        then:
        !blobClient.getVersionClient(blobItemV1.getVersionId()).exists()
        blobClient.getVersionClient(blobItemV2.getVersionId()).exists()
    }

    def "Delete Append Blob by Version"() {
        given:
        def inputV1 = new ByteArrayInputStream(contentV1.getBytes(StandardCharsets.UTF_8))
        def inputV2 = new ByteArrayInputStream(contentV2.getBytes(StandardCharsets.UTF_8))
        def blobItemV1 = blobClient.getAppendBlobClient().create()
        blobClient.getAppendBlobClient().appendBlock(inputV1, inputV1.available())
        def blobItemV2 = blobClient.getAppendBlobClient().create(true)
        blobClient.getAppendBlobClient().appendBlock(inputV2, inputV2.available())

        when:
        blobClient.getVersionClient(blobItemV1.getVersionId()).getAppendBlobClient().delete()

        then:
        !blobClient.getVersionClient(blobItemV1.getVersionId()).exists()
        blobClient.getVersionClient(blobItemV2.getVersionId()).exists()
    }
}
