// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob

import com.azure.storage.blob.models.BlobListDetails
import com.azure.storage.blob.models.BlobStorageException
import com.azure.storage.blob.models.ListBlobsOptions

class SoftDeleteTest extends APISpec {

    BlobContainerClient containerClient
    BlobClient blobClient

    def setup() {
        containerClient = softDeleteServiceClient.getBlobContainerClient(generateContainerName())
        containerClient.create()
        blobClient = containerClient.getBlobClient(generateBlobName())
        blobClient.getBlockBlobClient().upload(data.defaultInputStream, data.defaultDataSize)
    }

    def cleanup() {
        containerClient.delete()
    }

    def "Undelete min"() {
        setup:
        blobClient.delete()

        expect:
        blobClient.undeleteWithResponse(null, null).getStatusCode() == 200
    }

    def "Undelete"() {
        setup:
        blobClient.delete()

        when:
        def undeleteHeaders = blobClient.undeleteWithResponse(null, null).getHeaders()
        blobClient.getProperties()

        then:
        notThrown(BlobStorageException)
        undeleteHeaders.getValue("x-ms-request-id") != null
        undeleteHeaders.getValue("x-ms-version") != null
        undeleteHeaders.getValue("Date") != null
    }

    def "List blobs flat options deleted"() {
        setup:
        blobClient.delete()

        when:
        def options = new ListBlobsOptions().setDetails(new BlobListDetails().setRetrieveDeletedBlobs(true)).setPrefix(namer.getResourcePrefix())
        def blobs = containerClient.listBlobs(options, null).iterator()

        then:
        blobs.next().getName() == blobClient.getBlobName()
        !blobs.hasNext()
    }

    def "List blobs hier options deleted"() {
        setup:
        blobClient.delete()

        when:
        def options = new ListBlobsOptions().setDetails(new BlobListDetails().setRetrieveDeletedBlobs(true)).setPrefix(namer.getResourcePrefix())
        def blobs = containerClient.listBlobsByHierarchy("", options, null).iterator()

        then:
        blobs.next().getName() == blobClient.getBlobName()
        !blobs.hasNext()
    }
}
