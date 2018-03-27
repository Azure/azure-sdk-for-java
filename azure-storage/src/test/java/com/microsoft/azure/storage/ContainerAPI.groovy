package com.microsoft.azure.storage

import com.microsoft.azure.storage.blob.ContainerAccessConditions
import com.microsoft.azure.storage.blob.ETag
import com.microsoft.azure.storage.blob.HTTPAccessConditions
import com.microsoft.azure.storage.blob.LeaseAccessConditions
import com.microsoft.azure.storage.blob.Metadata
import com.microsoft.azure.storage.blob.PageBlobURL
import com.microsoft.azure.storage.blob.models.Blob
import com.microsoft.azure.storage.blob.models.BlobList
import com.microsoft.azure.storage.blob.models.BlobPrefix
import com.microsoft.azure.storage.blob.models.ContainersAcquireLeaseHeaders
import com.microsoft.azure.storage.blob.models.ContainersCreateResponse
import com.microsoft.azure.storage.blob.models.ContainersGetPropertiesHeaders
import com.microsoft.azure.storage.blob.models.ListBlobsResponse
import com.microsoft.azure.storage.blob.models.PublicAccessType
import com.microsoft.rest.v2.RestException
import spock.lang.*

class ContainerAPI extends APISpec {

    def "Container create all null"() {
        setup:
        // Overwrite the existing cu, which has already been created
        cu = primaryServiceURL.createContainerURL(generateContainerName())

        when:
        ContainersCreateResponse response = cu.create(null, null).blockingGet()

        then:
        response.statusCode() == 201
        response.headers().eTag() != null
        response.headers().dateProperty() != null
        response.headers().lastModified() != null
        response.headers().requestId() != null
        response.headers().version() != null
    }

    def "Container create metadata"() {
        setup:
        cu = primaryServiceURL.createContainerURL(generateContainerName())

        Metadata metadata = new Metadata()
        metadata.put("foo", "bar")
        metadata.put("fizz", "buzz")

        when:
        int statusCode = cu.create(metadata, null).blockingGet().statusCode()
        Map<String, String> receivedMetadata =
                cu.getProperties(null).blockingGet().headers().metadata()

        then:
        statusCode == 201
        receivedMetadata.equals(metadata)
    }

    def "Container create publicAccess Blob"() {
        setup:
        cu = primaryServiceURL.createContainerURL(generateContainerName())

        when:
        int statusCode = cu.create(null, PublicAccessType.BLOB).blockingGet().statusCode()
        PublicAccessType access =
                cu.getProperties(null).blockingGet().headers().blobPublicAccess()

        then:
        statusCode == 201
        access.toString() == "blob"
    }

    def "Container get properties null"() {
        when:
        ContainersGetPropertiesHeaders headers =
                cu.getProperties(null).blockingGet().headers()

        then:
        headers.blobPublicAccess() == null
        headers.eTag() != null
        headers.version() != null
        headers.leaseState().toString() == "available"
    }

    @Unroll
    def "Container get properties AC"(){
        setup:
        ContainersAcquireLeaseHeaders headers =
                cu.acquireLease(UUID.randomUUID().toString(), -1, null)
                        .blockingGet().headers()
        if (leaseID.equals(receivedLeaseID)) {
            leaseID = headers.leaseId()
        }

        int code

        try {
            code = cu.getProperties(new LeaseAccessConditions(leaseID)).blockingGet().statusCode()
        }
        catch (RestException e) {
            code = e.response().statusCode()
        }
        expect:
        code == statusCode

        where:
        leaseID         || statusCode
        null            || 200
        garbageLeaseID  || 412
        receivedLeaseID || 200

    }

    @Unroll
    def "Container set metadata"() {
        setup:
        Metadata metadata = new Metadata()
        metadata.put(key1, value1)
        metadata.put(key2, value2)

        expect:
        cu.setMetadata(metadata, null).blockingGet().statusCode() == statusCode

        where:
        key1    | value1     | key2     | value2    || statusCode
        "foo"   | "bar"      | "fizz"   | "buzz"    || 200
        //TODO: invalid characters. empty metadata
    }

    @Unroll
    def "Container set public access"() {
        setup:
        cu.setAccessPolicy(access, null, null).blockingGet()

        expect:
        cu.getProperties(null).blockingGet()
                .headers().blobPublicAccess().toString() == accessStr

        where:
        access                     | accessStr
        PublicAccessType.BLOB      | "blob"
        PublicAccessType.CONTAINER | "container"
        null                       | "null" // Calling .toString() on a null object gives "null"

    }

    def "Container set get access policy"() {
        setup:
        cu.setAccessPolicy(PublicAccessType.BLOB, null, null).blockingGet()

        expect:
        cu.getAccessPolicy(null).blockingGet().headers().blobPublicAccess().toString() == "blob"
    }

    @Unroll
    def "Container delete AC"() {
        setup:
        ContainersGetPropertiesHeaders headers =
                cu.getProperties(null).blockingGet().headers()
        if (match.equals(receivedEtag)) {
            match = new ETag(headers.eTag())
        }
        if (leaseID != null) {
            ContainersAcquireLeaseHeaders headers2 =
                    cu.acquireLease(UUID.randomUUID().toString(), -1, null).blockingGet().headers()
            if (leaseID.equals(receivedLeaseID)) {
                leaseID = headers2.leaseId()
            }
        }
        ContainerAccessConditions cac = new ContainerAccessConditions(
                new HTTPAccessConditions(modified, unmodified, match, noneMatch), new LeaseAccessConditions(leaseID))

        int code = 0
        boolean foundErrorMessage = false
        try{
            code = cu.delete(cac).blockingGet().statusCode()
        }
        catch (RestException e) {
            code = e.response().statusCode()
            // We don't need to check the error message here because we check the status code.
        }
        catch (IllegalArgumentException e) {
            foundErrorMessage = e.message.contains("ETag access conditions are not supported")
        }
        expect:
        code == statusCode
        foundErrorMessage == foundMessage

        /*
        For some reason, putting the 400 cases first works fine but putting them later in the sequence
        yields some sort of timeout error.
         */
        where:
        modified | unmodified | match        | noneMatch   | leaseID         || statusCode | foundMessage
        newDate  | null       | null         | null        | null            || 412        | false
        null     | oldDate    | null         | null        | null            || 412        | false
        null     | null       | null         | null        | null            || 202        | false
        oldDate  | null       | null         | null        | null            || 202        | false
        null     | newDate    | null         | null        | null            || 202        | false
        null     | null       | receivedEtag | null        | null            || 0          | true
        null     | null       | null         | garbageEtag | null            || 0          | true
        null     | null       | null         | null        | garbageLeaseID  || 412        | false
        null     | null       | null         | null        | receivedLeaseID || 202        | false
    }
    def "Container list blobs flat"() {
        setup:
        String name = generateBlobName()
        PageBlobURL bu = cu.createPageBlobURL(name)
        bu.create(512, null, null, null, null).blockingGet()

        when:
        List<Blob> blobs = cu.listBlobsFlatSegment(null, null).blockingGet().body().blobs().blob()

        then:
        blobs.size() == 1
        blobs.get(0).name().equals(name)
    }

    def "container list blobs hierarchy"() {
        setup:
        String name1 = generateBlobName()
        String name2 = generateBlobName() + "/"
        PageBlobURL bu = cu.createPageBlobURL(name1)
        bu.create(512, null, null, null, null).blockingGet()
        PageBlobURL bu2 = cu.createPageBlobURL(name2)
        bu2.create(512, null, null, null, null).blockingGet()

        when:
        BlobList response = cu.listBlobsHierarchySegment(null, "/", null).blockingGet()
                .body().blobs()
        List<Blob> blobs = response.blob()
        List<BlobPrefix> prefixes = response.blobPrefix()

        then:
        blobs.size() == 1
        blobs.get(0).name().equals(name1)

        prefixes.size() == 1
        prefixes.get(0).name().equals(name2)
    }
}
