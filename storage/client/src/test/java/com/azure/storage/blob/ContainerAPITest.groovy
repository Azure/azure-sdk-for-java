// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob

import com.azure.storage.blob.models.BlobItem
import com.azure.storage.blob.models.BlobType
import com.azure.storage.blob.models.ContainerGetPropertiesHeaders
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
        cu.create(null, null, null, null)

        then:
        System.out.println(cu.properties.toString())
    }

    def "delete"(){
        setup:
        cu.delete(null, null, null)

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
        ContainerGetPropertiesHeaders response = cu.getProperties()

        then:
        response.metadata() == metadata

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
        PublicAccessType access =
            cu.getProperties().blobPublicAccess()

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
        cuContext.create(null, null, null, defaultContext)

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
        cuDeleteContext.delete(null, null, defaultContext)

        then:
        notThrown(RuntimeException)
    }


    def "List blobs flat"() {
        setup:
        String name = generateBlobName()
        PageBlobClient bu = cu.getPageBlobClient(name)
        bu.create(512, null, null, null, null, null, null)

        when:
        List<BlobItem> blobs = cu.listBlobsFlat(null, null, null).asList()

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
        blobs.get(0).properties().contentMD5 == null
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
        bu.upload(defaultInputStream, defaultDataSize)

        then:
        cu.listBlobsFlat(null).each() {
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
        cu.getProperties().metadata().size() == 1
    }

    def "Set metadata min"() {
        setup:
        Metadata metadata = new Metadata()
        metadata.put("foo", "bar")

        when:
        cu.setMetadata(metadata)

        then:
        cu.getProperties().metadata() == metadata
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
        cu.getProperties().metadata() == metadata

        where:
        key1  | value1 | key2   | value2
        null  | null   | null   | null
        "foo" | "bar"  | "fizz" | "buzz"
    }

    def "Set metadata error"() {
        setup:
        cu = primaryServiceURL.getContainerClient(generateContainerName())

        when:
        cu.setMetadata(null, null, null, null)

        then:
        thrown(StorageException)
    }

    def "Set metadata context"() {
        setup:
        def cuMetadataContext = primaryServiceURL.getContainerClient(generateContainerName())
        cuMetadataContext.create()

        when:
        // No service call is made. Just satisfy the parameters.
        cuMetadataContext.setMetadata(null, null, null, defaultContext)

        then:
        notThrown(RuntimeException)
    }


    def "Get properties null"() {
        when:
        ContainerGetPropertiesHeaders headers = cu.getProperties(null, null, null)

        then:
        validateBasicHeaders(headers)
        headers.blobPublicAccess() == null
        headers.leaseDuration() == null
        headers.metadata().size() == 0
        !headers.hasImmutabilityPolicy()
        !headers.hasLegalHold()
    }

    def "Get properties min"() {
        expect:
        cu.getProperties().blobPublicAccess() == null
    }

    def "Get properties error"() {
        setup:
        cu = primaryServiceURL.getContainerClient(generateContainerName())

        when:
        cu.getProperties(null, null, null)

        then:
        thrown(StorageException)
    }

    def "Get properties context"() {
        setup:
        def cuPropertyContext = primaryServiceURL.getContainerClient(generateContainerName())
        cuPropertyContext.create()

        when:
        // No service call is made. Just satisfy the parameters.
        cu.getProperties(null, null, defaultContext)

        then:
        notThrown(RuntimeException)
    }

}
