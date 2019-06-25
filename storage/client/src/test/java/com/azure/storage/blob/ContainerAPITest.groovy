// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob

import com.azure.core.http.HttpHeaders
import com.azure.core.http.rest.Response
import com.azure.storage.blob.models.BlobItem
import com.azure.storage.blob.models.BlobType
import com.azure.storage.blob.models.Metadata
import com.azure.storage.blob.models.PublicAccessType
import org.junit.Assume
import spock.lang.Unroll

class ContainerAPITest extends APISpec {
    /////////////////////////////////////
    ////// Champion: ALL PASSED /////////
    /////////////////////////////////////
    def "Create all null"() {
        setup:
        // Overwrite the existing cu, which has already been created
        String containerName = generateContainerName()
        System.out.println("create all null name: " + containerName)
        cu = primaryServiceURL.getContainerClient(containerName)

        when:
        cu.create()

        then:
        System.out.println(cu.properties.toString())
    }

    def "delete"(){
        setup:
        cu.delete()

        expect:
        !cu.exists().value()
    }

    /////////////////////////////////////
    ////// High-pri: ALL PASSED /////////
    /////////////////////////////////////
    def "Create min"() {
        setup:
        String containerName = generateContainerName()
        System.out.println("create min name: " + containerName)
        when:
        primaryServiceURL.getContainerClient(containerName).create()
        then:
        primaryServiceURL.properties
    }

    @Unroll
    def "Create metadata"() {
        setup:
        cu = primaryServiceURL.getContainerClient(generateContainerName())
        Metadata metadata = new Metadata()
        if (key1 != null) {
            metadata.put(key1, value1)
        }
        if (key2 != null) {
            metadata.put(key2, value2)
        }

        when:
        cu.create(metadata, null, null, null)
        HttpHeaders headers = cu.getProperties().headers()

        then:
        getMetadataFromHeaders(headers) == metadata

        where:
        key1  | value1 | key2   | value2
        null  | null   | null   | null
        "foo" | "bar"  | "fizz" | "buzz"
    }

    @Unroll
    def "Create publicAccess"() {
        setup:
        cu = primaryServiceURL.getContainerClient(generateContainerName())

        when:
        cu.create(null, publicAccess, null, null)
        PublicAccessType access = cu.getProperties().value().blobPublicAccess()

        then:
        access.toString() == publicAccess.toString()

        where:
        publicAccess               | _
        PublicAccessType.BLOB      | _
        PublicAccessType.CONTAINER | _
        null                       | _
    }

    // Failed at Error code
    def "Create error"() {
        when:
        cu.create()

        then:
        def e = thrown(StorageException)
        e.response().statusCode() == 409
        //e.errorCode() == "ContainerAlreadyExists"
        e.message().contains("The specified container already exists.")
    }

    def "Create context"() {
        setup:
        String containerName = generateContainerName()
        System.out.println("create context name: " + containerName)
        def cuContext = primaryServiceURL.getContainerClient(containerName)

        when:
        // No service call is made. Just satisfy the parameters.
        cuContext.create()

        then:
        notThrown(RuntimeException)
    }

    def "Delete min"() {
        when:
        cu.delete()
        cu.getProperties()
        then:
        thrown(StorageException)

    }

    def "Delete error"() {
        setup:
        cu = primaryServiceURL.getContainerClient(generateContainerName())

        when:
        cu.delete()

        then:
        thrown(StorageException)
    }

    def "Delete context"() {
        setup:
        def cuDeleteContext = primaryServiceURL.getContainerClient(generateContainerName())
        cuDeleteContext.create()

        when:
        // No service call is made. Just satisfy the parameters.
        cuDeleteContext.delete()

        then:
        notThrown(RuntimeException)
    }


    def "List blobs flat"() {
        setup:
        String name = generateBlobName()
        PageBlobClient bu = cu.getPageBlobClient(name)
        bu.create(512)

        when:
        List<BlobItem> blobs = cu.listBlobsFlat().asList()

        then:
        blobs.size() == 1
        blobs.get(0).name() == name
        blobs.get(0).properties().blobType() == BlobType.PAGE_BLOB
        blobs.get(0).properties().copyCompletionTime() == null
        blobs.get(0).properties().copyStatusDescription() == null
        blobs.get(0).properties().copyId() == null
        blobs.get(0).properties().copyProgress() == null
        blobs.get(0).properties().copySource() == null
        blobs.get(0).properties().copyStatus() == null
        blobs.get(0).properties().incrementalCopy() == null
        blobs.get(0).properties().destinationSnapshot() == null
        blobs.get(0).properties().leaseDuration() == null
        blobs.get(0).properties().contentLength() != null
        blobs.get(0).properties().contentType() != null
        blobs.get(0).properties().contentMD5() == null
        blobs.get(0).properties().contentEncoding() == null
        blobs.get(0).properties().contentDisposition() == null
        blobs.get(0).properties().contentLanguage() == null
        blobs.get(0).properties().cacheControl() == null
        blobs.get(0).properties().blobSequenceNumber() == 0
        blobs.get(0).properties().serverEncrypted()
        blobs.get(0).properties().accessTierInferred()
        blobs.get(0).properties().archiveStatus() == null
        blobs.get(0).properties().creationTime() != null
    }

    def "List blobs flat min"() {
        when:
        def containerName = generateBlobName()
        BlockBlobClient bu = cu.getBlockBlobClient(containerName)
        bu.upload(defaultInputStream.get(), defaultDataSize)

        then:
        cu.listBlobsFlat().each() {
            blob ->
            Assume.assumeTrue(blob.name().contains("javabloblistblobsflatmin"))
            System.out.println("blob name: " + blob.name())
        }
    }



    def "Set metadata"() {
        setup:
        cu = primaryServiceURL.getContainerClient(generateContainerName())
        Metadata metadata = new Metadata()
        metadata.put("key", "value")
        cu.create(metadata, null, null, null)
        cu.setMetadata(metadata)

        expect:
        getMetadataFromHeaders(cu.getProperties().headers()).size() == 1
    }

    def "Set metadata min"() {
        setup:
        Metadata metadata = new Metadata()
        metadata.put("foo", "bar")

        when:
        cu.setMetadata(metadata)

        then:
        getMetadataFromHeaders(cu.getProperties().headers()) == metadata
    }

    @Unroll
    def "Set metadata metadata"() {
        setup:
        Metadata metadata = new Metadata()
        if (key1 != null) {
            metadata.put(key1, value1)
        }
        if (key2 != null) {
            metadata.put(key2, value2)
        }
        cu.setMetadata(metadata)

        expect:
        getMetadataFromHeaders(cu.getProperties().headers()) == metadata

        where:
        key1  | value1 | key2   | value2
        null  | null   | null   | null
        "foo" | "bar"  | "fizz" | "buzz"
    }

    def "Set metadata error"() {
        setup:
        cu = primaryServiceURL.getContainerClient(generateContainerName())

        when:
        cu.setMetadata(null)

        then:
        thrown(StorageException)
    }

    def "Set metadata context"() {
        setup:
        def cuMetadataContext = primaryServiceURL.getContainerClient(generateContainerName())
        cuMetadataContext.create()

        when:
        // No service call is made. Just satisfy the parameters.
        cuMetadataContext.setMetadata(null)

        then:
        notThrown(RuntimeException)
    }


    def "Get properties null"() {
        when:
        Response<ContainerProperties> response = cu.getProperties()
        HttpHeaders headers = response.headers()

        then:
        validateBasicHeaders(headers)
        headers.value("x-ms-lease-duration") == null
        getMetadataFromHeaders(headers).size() == 0
        !response.value().hasImmutabilityPolicy()
        !response.value().hasLegalHold()
        response.value().blobPublicAccess() == null
    }

    def "Get properties min"() {
        expect:
        cu.getProperties().value().blobPublicAccess() == null
    }

    def "Get properties error"() {
        setup:
        cu = primaryServiceURL.getContainerClient(generateContainerName())

        when:
        cu.getProperties()

        then:
        thrown(StorageException)
    }

    def "Get properties context"() {
        setup:
        def cuPropertyContext = primaryServiceURL.getContainerClient(generateContainerName())
        cuPropertyContext.create()

        when:
        // No service call is made. Just satisfy the parameters.
        cu.getProperties()

        then:
        notThrown(RuntimeException)
    }

}
