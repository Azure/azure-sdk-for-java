package com.azure.storage.file.datalake

import com.azure.storage.blob.models.BlobErrorCode
import com.azure.storage.blob.models.BlobStorageException
import com.azure.storage.file.datalake.models.LeaseAccessConditions
import com.azure.storage.file.datalake.models.ModifiedAccessConditions
import com.azure.storage.file.datalake.implementation.models.StorageErrorException
import com.azure.storage.file.datalake.models.FileSystemAccessConditions
import com.azure.storage.file.datalake.models.LeaseStateType
import com.azure.storage.file.datalake.models.LeaseStatusType
import com.azure.storage.file.datalake.models.PublicAccessType
import spock.lang.Unroll


class FileSystemAPITest extends APISpec {

    def "Create all null"() {
        setup:
        // Overwrite the existing fsc, which has already been created
        fsc = primaryDataLakeServiceClient.getFileSystemClient(generateFileSystemName())

        when:
        def response = fsc.createWithResponse(null, null, null, null)

        then:
        response.getStatusCode() == 201
        validateBasicHeaders(response.getHeaders())
    }

    def "Create min"() {
        when:
        def fsc = primaryDataLakeServiceClient.createFileSystem(generateFileSystemName())

        then:
        fsc.getProperties()

        notThrown(Exception)
    }

    @Unroll
    def "Create metadata"() {
        setup:
        fsc = primaryDataLakeServiceClient.getFileSystemClient(generateFileSystemName())
        def metadata = new HashMap<String, String>()
        if (key1 != null) {
            metadata.put(key1, value1)
        }
        if (key2 != null) {
            metadata.put(key2, value2)
        }

        when:
        fsc.createWithResponse(metadata, null, null, null)
        def response = fsc.getPropertiesWithResponse(null, null, null)

        then:
        response.getValue().getMetadata() == metadata

        where:
        key1  | value1 | key2   | value2
        null  | null   | null   | null
        "foo" | "bar"  | "fizz" | "buzz"
        "testFoo" | "testBar" | "testFizz" | "testBuzz"
    }

    @Unroll
    def "Create publicAccess"() {
        setup:
        fsc = primaryDataLakeServiceClient.getFileSystemClient(generateFileSystemName())

        when:
        fsc.createWithResponse(null, publicAccess, null, null)
        def access = fsc.getProperties().getPublicAccess()

        then:
        access == publicAccess

        where:
        publicAccess               | _
        PublicAccessType.BLOB      | _
        PublicAccessType.CONTAINER | _
        null                       | _
    }

    def "Create error"() {
        when:
        fsc.create()

        then:
        def e = thrown(BlobStorageException)
        e.getResponse().getStatusCode() == 409
        e.getErrorCode() == BlobErrorCode.CONTAINER_ALREADY_EXISTS
        e.getServiceMessage().contains("The specified container already exists.")
    }

    def "Get properties null"() {
        when:
        def response = fsc.getPropertiesWithResponse(null, null, null)

        then:
        validateBasicHeaders(response.getHeaders())
        response.getValue().getPublicAccess() == null
        !response.getValue().hasImmutabilityPolicy()
        !response.getValue().hasLegalHold()
        response.getValue().getLeaseDuration() == null
        response.getValue().getLeaseState() == LeaseStateType.AVAILABLE
        response.getValue().getLeaseStatus() == LeaseStatusType.UNLOCKED
        response.getValue().getMetadata().size() == 0
    }

    def "Get properties min"() {
        expect:
        fsc.getProperties() != null
    }

    def "Get properties lease"() {
        setup:
        def leaseID = setupFileSystemLeaseCondition(fsc, receivedLeaseID)

        expect:
        fsc.getPropertiesWithResponse(new LeaseAccessConditions().setLeaseId(leaseID), null, null).getStatusCode() == 200
    }

    def "Get properties lease fail"() {
        when:
        fsc.getPropertiesWithResponse(new LeaseAccessConditions().setLeaseId("garbage"), null, null)

        then:
        thrown(Exception)
    }

    def "Get properties error"() {
        setup:
        fsc = primaryDataLakeServiceClient.getFileSystemClient(generateFileSystemName())

        when:
        fsc.getProperties()

        then:
        thrown(Exception)
    }

    def "Set metadata"() {
        setup:
        fsc = primaryDataLakeServiceClient.getFileSystemClient(generateFileSystemName())
        def metadata = new HashMap<String, String>()
        metadata.put("key", "value")
        fsc.createWithResponse(metadata, null, null, null)

        when:
        def response = fsc.setMetadataWithResponse(null, null, null, null)

        then:
        response.getStatusCode() == 200
        validateBasicHeaders(response.getHeaders())
        fsc.getPropertiesWithResponse(null, null, null).getValue().getMetadata().size() == 0
    }

    def "Set metadata min"() {
        setup:
        def metadata = new HashMap<String, String>()
        metadata.put("foo", "bar")

        when:
        fsc.setMetadata(metadata)

        then:
        fsc.getPropertiesWithResponse(null, null, null).getValue().getMetadata() == metadata
    }

    @Unroll
    def "Set metadata metadata"() {
        setup:
        def metadata = new HashMap<String, String>()
        if (key1 != null) {
            metadata.put(key1, value1)
        }
        if (key2 != null) {
            metadata.put(key2, value2)
        }

        expect:
        fsc.setMetadataWithResponse(metadata, null, null, null).getStatusCode() == 200
        fsc.getPropertiesWithResponse(null, null, null).getValue().getMetadata() == metadata

        where:
        key1  | value1 | key2   | value2
        null  | null   | null   | null
        "foo" | "bar"  | "fizz" | "buzz"
    }

    @Unroll
    def "Set metadata AC"() {
        setup:
        leaseID = setupFileSystemLeaseCondition(fsc, leaseID)
        def cac = new FileSystemAccessConditions()
            .setLeaseAccessConditions(new LeaseAccessConditions().setLeaseId(leaseID))
            .setModifiedAccessConditions(new ModifiedAccessConditions()
                .setIfModifiedSince(modified))

        expect:
        fsc.setMetadataWithResponse(null, cac, null, null).getStatusCode() == 200

        where:
        modified | leaseID
        null     | null
        oldDate  | null
        null     | receivedLeaseID
    }

    @Unroll
    def "Set metadata AC fail"() {
        setup:
        def cac = new FileSystemAccessConditions()
            .setLeaseAccessConditions(new LeaseAccessConditions().setLeaseId(leaseID))
            .setModifiedAccessConditions(new ModifiedAccessConditions()
                .setIfModifiedSince(modified))

        when:
        fsc.setMetadataWithResponse(null, cac, null, null)

        then:
        thrown(Exception)

        where:
        modified | leaseID
        newDate  | null
        null     | garbageLeaseID
    }

    @Unroll
    def "Set metadata AC illegal"() {
        setup:
        def mac = new ModifiedAccessConditions()
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)

        when:
        fsc.setMetadataWithResponse(null, new FileSystemAccessConditions().setModifiedAccessConditions(mac), null, null)

        then:
        thrown(UnsupportedOperationException)

        where:
        unmodified | match        | noneMatch
        newDate    | null         | null
        null       | receivedEtag | null
        null       | null         | garbageEtag
    }

    def "Set metadata error"() {
        setup:
        fsc = primaryDataLakeServiceClient.getFileSystemClient(generateFileSystemName())

        when:
        fsc.setMetadata(null)

        then:
        thrown(Exception)
    }

    def "Delete"() {
        when:
        def response = fsc.deleteWithResponse(null, null, null)

        then:
        response.getStatusCode() == 202
        response.getHeaders().getValue("x-ms-request-id") != null
        response.getHeaders().getValue("x-ms-version") != null
        response.getHeaders().getValue("Date") != null
    }

    def "Delete min"() {
        when:
        fsc.delete()

        and:
        fsc.getProperties()

        then:
        def e = thrown(BlobStorageException)
        e.getResponse().getStatusCode() == 404
        e.getErrorCode() == BlobErrorCode.CONTAINER_NOT_FOUND
        e.getServiceMessage().contains("The specified container does not exist.")
    }

    @Unroll
    def "Delete AC"() {
        setup:
        leaseID = setupFileSystemLeaseCondition(fsc, leaseID)
        def fsac = new FileSystemAccessConditions()
            .setLeaseAccessConditions(new LeaseAccessConditions().setLeaseId(leaseID))
            .setModifiedAccessConditions(new ModifiedAccessConditions()
                .setIfModifiedSince(modified)
                .setIfUnmodifiedSince(unmodified))

        expect:
        fsc.deleteWithResponse(fsac, null, null).getStatusCode() == 202

        where:
        modified | unmodified | leaseID
        null     | null       | null
        oldDate  | null       | null
        null     | newDate    | null
        null     | null       | receivedLeaseID
    }

    @Unroll
    def "Delete AC fail"() {
        setup:
        def fsac = new FileSystemAccessConditions()
            .setLeaseAccessConditions(new LeaseAccessConditions().setLeaseId(leaseID))
            .setModifiedAccessConditions(new ModifiedAccessConditions()
                .setIfModifiedSince(modified)
                .setIfUnmodifiedSince(unmodified))

        when:
        fsc.deleteWithResponse(fsac, null, null)

        then:
        thrown(BlobStorageException)

        where:
        modified | unmodified | leaseID
        newDate  | null       | null
        null     | oldDate    | null
        null     | null       | garbageLeaseID
    }

    @Unroll
    def "Delete AC illegal"() {
        setup:
        def mac = new ModifiedAccessConditions().setIfMatch(match).setIfNoneMatch(noneMatch)

        when:
        fsc.deleteWithResponse(new FileSystemAccessConditions().setModifiedAccessConditions(mac), null, null)

        then:
        thrown(UnsupportedOperationException)

        where:
        match        | noneMatch
        receivedEtag | null
        null         | garbageEtag
    }

    def "Delete error"() {
        setup:
        fsc = primaryDataLakeServiceClient.getFileSystemClient(generateFileSystemName())

        when:
        fsc.delete()

        then:
        thrown(BlobStorageException)
    }

    def "List paths"() {
        when:
        def result = fsc.getPaths()

        then:
        notThrown(StorageErrorException)
        result.iterator().size() > 0
    }
}
