package com.azure.storage.file.datalake

import com.azure.core.util.Context
import com.azure.identity.DefaultAzureCredentialBuilder
import com.azure.storage.blob.BlobUrlParts
import com.azure.storage.blob.models.BlobErrorCode
import com.azure.storage.common.Utility
import com.azure.storage.file.datalake.models.DataLakeAccessPolicy
import com.azure.storage.file.datalake.models.DataLakeRequestConditions
import com.azure.storage.file.datalake.models.DataLakeSignedIdentifier
import com.azure.storage.file.datalake.models.DataLakeStorageException
import com.azure.storage.file.datalake.models.LeaseStateType
import com.azure.storage.file.datalake.models.LeaseStatusType
import com.azure.storage.file.datalake.models.ListPathsOptions
import com.azure.storage.file.datalake.models.PathAccessControlEntry
import com.azure.storage.file.datalake.models.PathDeletedItem
import com.azure.storage.file.datalake.models.PathHttpHeaders
import com.azure.storage.file.datalake.models.PathItem
import com.azure.storage.file.datalake.models.PublicAccessType
import spock.lang.ResourceLock
import spock.lang.Unroll

import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.stream.Collectors

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

        notThrown(DataLakeStorageException)
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
        def access = fsc.getProperties().getDataLakePublicAccess()

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
        def e = thrown(DataLakeStorageException)
        e.getResponse().getStatusCode() == 409
        e.getErrorCode() == BlobErrorCode.CONTAINER_ALREADY_EXISTS.toString()
        e.getServiceMessage().contains("The specified container already exists.")
    }

    def "Get properties null"() {
        when:
        def response = fsc.getPropertiesWithResponse(null, null, null)

        then:
        validateBasicHeaders(response.getHeaders())
        response.getValue().getDataLakePublicAccess() == null
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
        fsc.getPropertiesWithResponse(leaseID, null, null).getStatusCode() == 200
    }

    def "Get properties lease fail"() {
        when:
        fsc.getPropertiesWithResponse("garbage", null, null)

        then:
        thrown(DataLakeStorageException)
    }

    def "Get properties error"() {
        setup:
        fsc = primaryDataLakeServiceClient.getFileSystemClient(generateFileSystemName())

        when:
        fsc.getProperties()

        then:
        thrown(DataLakeStorageException)
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
        def drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfModifiedSince(modified)

        expect:
        fsc.setMetadataWithResponse(null, drc, null, null).getStatusCode() == 200

        where:
        modified | leaseID
        null     | null
        oldDate  | null
        null     | receivedLeaseID
    }

    @Unroll
    def "Set metadata AC fail"() {
        setup:
        def drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfModifiedSince(modified)

        when:
        fsc.setMetadataWithResponse(null, drc, null, null)

        then:
        thrown(DataLakeStorageException)

        where:
        modified | leaseID
        newDate  | null
        null     | garbageLeaseID
    }

    @Unroll
    def "Set metadata AC illegal"() {
        setup:
        def drc = new DataLakeRequestConditions()
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfUnmodifiedSince(unmodified)

        when:
        fsc.setMetadataWithResponse(null, drc, null, null)

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
        thrown(DataLakeStorageException)
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
        def e = thrown(DataLakeStorageException)
        e.getResponse().getStatusCode() == 404
        e.getErrorCode() == BlobErrorCode.CONTAINER_NOT_FOUND.toString()
        e.getServiceMessage().contains("The specified container does not exist.")
    }

    @Unroll
    def "Delete AC"() {
        setup:
        leaseID = setupFileSystemLeaseCondition(fsc, leaseID)
        def drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

        expect:
        fsc.deleteWithResponse(drc, null, null).getStatusCode() == 202

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
        def drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

        when:
        fsc.deleteWithResponse(drc, null, null)

        then:
        thrown(DataLakeStorageException)

        where:
        modified | unmodified | leaseID
        newDate  | null       | null
        null     | oldDate    | null
        null     | null       | garbageLeaseID
    }

    @Unroll
    def "Delete AC illegal"() {
        setup:
        def drc = new DataLakeRequestConditions()
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)

        when:
        fsc.deleteWithResponse(drc, null, null)

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
        thrown(DataLakeStorageException)
    }

    def "Create file min"() {
        when:
        fsc.createFile(generatePathName())

        then:
        notThrown(DataLakeStorageException)
    }

    @Unroll
    def "Create file overwrite"() {
        setup:
        def pathName = generatePathName()
        fsc.createFile(pathName)

        when:
        def exceptionThrown = false
        try {
            fsc.createFile(pathName, overwrite)
        } catch (DataLakeStorageException ignored) {
            exceptionThrown = true
        }

        then:
        exceptionThrown != overwrite

        where:
        overwrite || _
        true      || _
        false     || _
    }

    def "Create file defaults"() {
        when:
        def createResponse = fsc.createFileWithResponse(generatePathName(), null, null, null, null, null, null, null)

        then:
        createResponse.getStatusCode() == 201
        validateBasicHeaders(createResponse.getHeaders())
    }

    def "Create file error"() {
        when:
        fsc.createFileWithResponse(generatePathName(), null, null, null, null,
            new DataLakeRequestConditions().setIfMatch("garbage"), null,
            Context.NONE)

        then:
        thrown(DataLakeStorageException)
    }

    @Unroll
    def "Create file headers"() {
        // Create does not set md5
        setup:
        def headers = new PathHttpHeaders().setCacheControl(cacheControl)
            .setContentDisposition(contentDisposition)
            .setContentEncoding(contentEncoding)
            .setContentLanguage(contentLanguage)
            .setContentType(contentType)

        when:
        def client = fsc.createFileWithResponse(generatePathName(), null, null, headers, null, null, null, null).getValue()
        def response = client.getPropertiesWithResponse(null, null, null)

        // If the value isn't set the service will automatically set it
        contentType = (contentType == null) ? "application/octet-stream" : contentType

        then:
        validatePathProperties(response, cacheControl, contentDisposition, contentEncoding, contentLanguage, null, contentType)

        where:
        cacheControl | contentDisposition | contentEncoding | contentLanguage | contentType
        null         | null               | null            | null            | null
        "control"    | "disposition"      | "encoding"      | "language"      | "type"
    }

    @Unroll
    def "Create file metadata"() {
        setup:
        def metadata = new HashMap<String, String>()
        if (key1 != null) {
            metadata.put(key1, value1)
        }
        if (key2 != null) {
            metadata.put(key2, value2)
        }

        when:
        def client = fsc.createFileWithResponse(generatePathName(), null, null, null, metadata, null, null, null).getValue()
        def response = client.getProperties()

        then:
        response.getMetadata() == metadata

        where:
        key1  | value1 | key2   | value2
        null  | null   | null   | null
        "foo" | "bar"  | "fizz" | "buzz"
    }

    @Unroll
    def "Create file AC"() {
        setup:
        def pathName = generatePathName()
        def client = fsc.getFileClient(pathName)
        client.create()
        match = setupPathMatchCondition(client, match)
        leaseID = setupPathLeaseCondition(client, leaseID)
        def drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)


        expect:
        fsc.createFileWithResponse(pathName, null, null, null, null, drc, null, null).getStatusCode() == 201

        where:
        modified | unmodified | match        | noneMatch   | leaseID
        null     | null       | null         | null        | null
        oldDate  | null       | null         | null        | null
        null     | newDate    | null         | null        | null
        null     | null       | receivedEtag | null        | null
        null     | null       | null         | garbageEtag | null
        null     | null       | null         | null        | receivedLeaseID
    }

    @Unroll
    def "Create file AC fail"() {
        setup:
        def pathName = generatePathName()
        def client = fsc.getFileClient(pathName)
        client.create()
        noneMatch = setupPathMatchCondition(client, noneMatch)
        setupPathLeaseCondition(client, leaseID)
        def drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

        when:
        fsc.createFileWithResponse(pathName, null, null, null, null, drc, null, Context.NONE)

        then:
        thrown(DataLakeStorageException)

        where:
        modified | unmodified | match       | noneMatch    | leaseID
        newDate  | null       | null        | null         | null
        null     | oldDate    | null        | null         | null
        null     | null       | garbageEtag | null         | null
        null     | null       | null        | receivedEtag | null
        null     | null       | null        | null         | garbageLeaseID
    }

    def "Create file permissions and umask"() {
        setup:
        def permissions = "0777"
        def umask = "0057"

        expect:
        fsc.createFileWithResponse(generatePathName(), permissions, umask, null, null, null, null, Context.NONE).getStatusCode() == 201
    }

    def "Delete file min"() {
        expect:
        def pathName = generatePathName()
        fsc.createFile(pathName)
        fsc.deleteFileWithResponse(pathName, null, null, null).getStatusCode() == 200
    }

    def "Delete file file does not exist anymore"() {
        when:
        def pathName = generatePathName()
        def client = fsc.createFile(pathName)
        fsc.deleteFileWithResponse(pathName, null, null, null)
        client.getPropertiesWithResponse(null, null, null)

        then:
        def e = thrown(DataLakeStorageException)
        e.getResponse().getStatusCode() == 404
        e.getErrorCode() == BlobErrorCode.BLOB_NOT_FOUND.toString()
//        e.getServiceMessage().contains("The specified blob does not exist.")
    }

    @Unroll
    def "Delete file AC"() {
        setup:
        def pathName = generatePathName()
        def client = fsc.createFile(pathName)
        match = setupPathMatchCondition(client, match)
        leaseID = setupPathLeaseCondition(client, leaseID)
        def drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

        expect:
        fsc.deleteFileWithResponse(pathName, drc, null, null).getStatusCode() == 200

        where:
        modified | unmodified | match        | noneMatch   | leaseID
        null     | null       | null         | null        | null
        oldDate  | null       | null         | null        | null
        null     | newDate    | null         | null        | null
        null     | null       | receivedEtag | null        | null
        null     | null       | null         | garbageEtag | null
        null     | null       | null         | null        | receivedLeaseID
    }

    @Unroll
    def "Delete file AC fail"() {
        setup:
        def pathName = generatePathName()
        def client = fsc.createFile(pathName)
        noneMatch = setupPathMatchCondition(client, noneMatch)
        setupPathLeaseCondition(client, leaseID)
        def drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

        when:
        fsc.deleteFileWithResponse(pathName, drc, null, null).getStatusCode()

        then:
        thrown(DataLakeStorageException)

        where:
        modified | unmodified | match       | noneMatch    | leaseID
        newDate  | null       | null        | null         | null
        null     | oldDate    | null        | null         | null
        null     | null       | garbageEtag | null         | null
        null     | null       | null        | receivedEtag | null
        null     | null       | null        | null         | garbageLeaseID
    }

    def "Create dir min"() {
        when:
        fsc.createDirectory(generatePathName())

        then:
        notThrown(DataLakeStorageException)
    }

    @Unroll
    def "Create dir overwrite"() {
        setup:
        def pathName = generatePathName()
        fsc.createDirectory(pathName)

        when:
        def exceptionThrown = false
        try {
            fsc.createDirectory(pathName, overwrite)
        } catch (DataLakeStorageException ignored) {
            exceptionThrown = true
        }

        then:
        exceptionThrown != overwrite

        where:
        overwrite || _
        true      || _
        false     || _
    }

    def "Create dir defaults"() {
        when:
        def createResponse = fsc.createDirectoryWithResponse(generatePathName(), null, null, null, null, null, null, null)

        then:
        createResponse.getStatusCode() == 201
        validateBasicHeaders(createResponse.getHeaders())
    }

    def "Create dir error"() {
        when:
        fsc.createDirectoryWithResponse(generatePathName(), null, null, null, null,
            new DataLakeRequestConditions().setIfMatch("garbage"), null,
            Context.NONE)

        then:
        thrown(DataLakeStorageException)
    }

    @Unroll
    def "Create dir headers"() {
        // Create does not set md5
        setup:
        def headers = new PathHttpHeaders().setCacheControl(cacheControl)
            .setContentDisposition(contentDisposition)
            .setContentEncoding(contentEncoding)
            .setContentLanguage(contentLanguage)
            .setContentType(contentType)

        when:
        def client = fsc.createDirectoryWithResponse(generatePathName(), null, null, headers, null, null, null, null).getValue()
        def response = client.getPropertiesWithResponse(null, null, null)

        // If the value isn't set the service will automatically set it
        contentType = (contentType == null) ? "application/octet-stream" : contentType

        then:
        validatePathProperties(response, cacheControl, contentDisposition, contentEncoding, contentLanguage, null, contentType)

        where:
        cacheControl | contentDisposition | contentEncoding | contentLanguage | contentType
        null         | null               | null            | null            | null
        "control"    | "disposition"      | "encoding"      | "language"      | "type"
    }

    @Unroll
    def "Create dir metadata"() {
        setup:
        def metadata = new HashMap<String, String>()
        if (key1 != null) {
            metadata.put(key1, value1)
        }
        if (key2 != null) {
            metadata.put(key2, value2)
        }

        when:
        def client = fsc.createDirectoryWithResponse(generatePathName(), null, null, null, metadata, null, null, null).getValue()
        def response = client.getProperties()

        then:
        // Directory adds a directory metadata value
        for(String k : metadata.keySet()) {
            response.getMetadata().containsKey(k)
            response.getMetadata().get(k) == metadata.get(k)
        }

        where:
        key1  | value1 | key2   | value2
        null  | null   | null   | null
        "foo" | "bar"  | "fizz" | "buzz"
    }

    @Unroll
    def "Create dir AC"() {
        setup:
        def pathName = generatePathName()
        def client = fsc.getDirectoryClient(pathName)
        client.create()
        match = setupPathMatchCondition(client, match)
        leaseID = setupPathLeaseCondition(client, leaseID)
        def drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)


        expect:
        fsc.createDirectoryWithResponse(pathName, null, null, null, null, drc, null, null).getStatusCode() == 201

        where:
        modified | unmodified | match        | noneMatch   | leaseID
        null     | null       | null         | null        | null
        oldDate  | null       | null         | null        | null
        null     | newDate    | null         | null        | null
        null     | null       | receivedEtag | null        | null
        null     | null       | null         | garbageEtag | null
        null     | null       | null         | null        | receivedLeaseID
    }

    @Unroll
    def "Create dir AC fail"() {
        setup:
        def pathName = generatePathName()
        def client = fsc.getDirectoryClient(pathName)
        client.create()
        noneMatch = setupPathMatchCondition(client, noneMatch)
        setupPathLeaseCondition(client, leaseID)
        def drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

        when:
        fsc.createDirectoryWithResponse(pathName, null, null, null, null, drc, null, Context.NONE)

        then:
        thrown(DataLakeStorageException)

        where:
        modified | unmodified | match       | noneMatch    | leaseID
        newDate  | null       | null        | null         | null
        null     | oldDate    | null        | null         | null
        null     | null       | garbageEtag | null         | null
        null     | null       | null        | receivedEtag | null
        null     | null       | null        | null         | garbageLeaseID
    }

    def "Create dir permissions and umask"() {
        setup:
        def permissions = "0777"
        def umask = "0057"

        expect:
        fsc.createDirectoryWithResponse(generatePathName(), permissions, umask, null, null, null, null, Context.NONE).getStatusCode() == 201
    }

    def "Delete dir min"() {
        expect:
        def pathName = generatePathName()
        fsc.createDirectory(pathName)
        fsc.deleteDirectoryWithResponse(pathName, false, null, null, null).getStatusCode() == 200
    }

    def "Delete dir recursive"() {
        expect:
        def pathName = generatePathName()
        fsc.createDirectory(pathName)
        fsc.deleteDirectoryWithResponse(pathName, true, null, null, null).getStatusCode() == 200
    }

    def "Delete dir dir does not exist anymore"() {
        when:
        def pathName = generatePathName()
        def client = fsc.createDirectory(pathName)
        fsc.deleteDirectoryWithResponse(pathName, false, null, null, null)
        client.getPropertiesWithResponse(null, null, null)

        then:
        def e = thrown(DataLakeStorageException)
        e.getResponse().getStatusCode() == 404
        e.getErrorCode() == BlobErrorCode.BLOB_NOT_FOUND.toString()
//        e.getServiceMessage().contains("The specified blob does not exist.")
    }

    @Unroll
    def "Delete dir AC"() {
        setup:
        def pathName = generatePathName()
        def client = fsc.createDirectory(pathName)
        match = setupPathMatchCondition(client, match)
        leaseID = setupPathLeaseCondition(client, leaseID)
        def drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

        expect:
        fsc.deleteDirectoryWithResponse(pathName, false, drc, null, null).getStatusCode() == 200

        where:
        modified | unmodified | match        | noneMatch   | leaseID
        null     | null       | null         | null        | null
        oldDate  | null       | null         | null        | null
        null     | newDate    | null         | null        | null
        null     | null       | receivedEtag | null        | null
        null     | null       | null         | garbageEtag | null
        null     | null       | null         | null        | receivedLeaseID
    }

    @Unroll
    def "Delete dir AC fail"() {
        setup:
        def pathName = generatePathName()
        def client = fsc.createDirectory(pathName)
        noneMatch = setupPathMatchCondition(client, noneMatch)
        setupPathLeaseCondition(client, leaseID)
        def drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

        when:
        fsc.deleteDirectoryWithResponse(pathName, false, drc, null, null).getStatusCode()

        then:
        thrown(DataLakeStorageException)

        where:
        modified | unmodified | match       | noneMatch    | leaseID
        newDate  | null       | null        | null         | null
        null     | oldDate    | null        | null         | null
        null     | null       | garbageEtag | null         | null
        null     | null       | null        | receivedEtag | null
        null     | null       | null        | null         | garbageLeaseID
    }

    def "List paths"() {
        setup:
        def dirName = generatePathName()
        fsc.getDirectoryClient(dirName).create()

        def fileName = generatePathName()
        fsc.getFileClient(fileName).create()

        when:
        def response = fsc.listPaths().iterator()

        then:
        def dirPath = response.next()
        dirPath.getName() == dirName
        dirPath.getETag()
        dirPath.getGroup()
        dirPath.getLastModified()
        dirPath.getOwner()
        dirPath.getPermissions()
//        dirPath.getContentLength() // known issue with service
        dirPath.isDirectory()

        response.hasNext()
        def filePath = response.next()
        filePath.getName() == fileName
        filePath.getETag()
        filePath.getGroup()
        filePath.getLastModified()
        filePath.getOwner()
        filePath.getPermissions()
//        filePath.getContentLength() // known issue with service
        !filePath.isDirectory()

        !response.hasNext()
    }

    def "List paths recursive"() {
        setup:
        def dirName = generatePathName()
        fsc.getDirectoryClient(dirName).create()

        def fileName = generatePathName()
        fsc.getFileClient(fileName).create()

        when:
        def response = fsc.listPaths(new ListPathsOptions().setRecursive(true), null).iterator()

        then:
        def dirPath = response.next()
        response.hasNext()
        def filePath = response.next()
        !response.hasNext()
    }

    def "List paths return upn"() {
        setup:
        def dirName = generatePathName()
        fsc.getDirectoryClient(dirName).create()

        def fileName = generatePathName()
        fsc.getFileClient(fileName).create()

        when:
        def response = fsc.listPaths(new ListPathsOptions().setUserPrincipalNameReturned(true), null).iterator()

        then:
        def dirPath = response.next()
        response.hasNext()
        def filePath = response.next()
        !response.hasNext()
    }

    def "List paths max results"() {
        setup:
        def dirName = generatePathName()
        fsc.getDirectoryClient(dirName).create()

        def fileName = generatePathName()
        fsc.getFileClient(fileName).create()

        when:
        def response = fsc.listPaths(new ListPathsOptions().setMaxResults(1), null).iterator()

        then:
        def dirPath = response.next()
        response.hasNext()
        def filePath = response.next()
        !response.hasNext()
    }

    def "List paths max results by page"() {
        setup:
        def dirName = generatePathName()
        fsc.getDirectoryClient(dirName).create()

        def fileName = generatePathName()
        fsc.getFileClient(fileName).create()

        expect:
        for (def page : fsc.listPaths(new ListPathsOptions(), null).iterableByPage(1)) {
            assert page.value.size() == 1
        }
    }

    @Unroll
    def "Create URL special chars encoded"() {
        // This test checks that we handle path names with encoded special characters correctly.
        setup:
        def fc1 = fsc.getFileClient(name + "file1")
        def fc2 = fsc.getFileClient(name + "file2")
        def dc1 = fsc.getDirectoryClient(name + "dir1")
        def dc2 = fsc.getDirectoryClient(name + "dir2")

        expect:
        fc1.createWithResponse(null, null, null, null, null, null, null).getStatusCode() == 201
        fc2.create()
        fc2.getPropertiesWithResponse(null, null, null).getStatusCode() == 200
        fc2.appendWithResponse(data.defaultInputStream, 0, data.defaultDataSize, null, null, null, null).getStatusCode() == 202
        dc1.createWithResponse(null, null, null, null, null, null, null).getStatusCode() == 201
        dc2.create()
        dc2.getPropertiesWithResponse(null, null, null).getStatusCode() == 200

        when:
        def paths = fsc.listPaths().iterator()

        then:
        paths.next().getName() == Utility.urlDecode(name) + "dir1"
        paths.next().getName() == Utility.urlDecode(name) + "dir2"
        paths.next().getName() == Utility.urlDecode(name) + "file1"
        paths.next().getName() == Utility.urlDecode(name) + "file2"

        // Note you cannot use the / character in a path in datalake unless it is to specify an absolute path
        where:
        name                                                     | _
        "%E4%B8%AD%E6%96%87"                                     | _
        "az%5B%5D"                                               | _
        "hello%20world"                                          | _
        "hello%26world"                                          | _
        "%21%2A%27%28%29%3B%3A%40%26%3D%2B%24%2C%3F%23%5B%5D"    | _
    }

    @Unroll
    def "Set access policy"() {
        setup:
        def response = fsc.setAccessPolicyWithResponse(access, null, null, null, null)

        expect:
        validateBasicHeaders(response.getHeaders())
        fsc.getProperties().getDataLakePublicAccess() == access

        where:
        access                     | _
        PublicAccessType.BLOB      | _
        PublicAccessType.CONTAINER | _
        null                       | _
    }

    def "Set access policy min access"() {
        when:
        fsc.setAccessPolicy(PublicAccessType.CONTAINER, null)

        then:
        fsc.getProperties().getDataLakePublicAccess() == PublicAccessType.CONTAINER
    }

    def "Set access policy min ids"() {
        setup:
        def identifier = new DataLakeSignedIdentifier()
            .setId("0000")
            .setAccessPolicy(new DataLakeAccessPolicy()
                .setStartsOn(OffsetDateTime.now().atZoneSameInstant(ZoneId.of("UTC")).toOffsetDateTime())
                .setExpiresOn(OffsetDateTime.now().atZoneSameInstant(ZoneId.of("UTC")).toOffsetDateTime()
                    .plusDays(1))
                .setPermissions("r"))

        def ids = [identifier] as List

        when:
        fsc.setAccessPolicy(null, ids)

        then:
        fsc.getAccessPolicy().getIdentifiers().get(0).getId() == "0000"
    }

    def "Set access policy ids"() {
        setup:
        def identifier = new DataLakeSignedIdentifier()
            .setId("0000")
            .setAccessPolicy(new DataLakeAccessPolicy()
                .setStartsOn(namer.getUtcNow())
                .setExpiresOn(namer.getUtcNow().plusDays(1))
                .setPermissions("r"))
        def identifier2 = new DataLakeSignedIdentifier()
            .setId("0001")
            .setAccessPolicy(new DataLakeAccessPolicy()
                .setStartsOn(namer.getUtcNow())
                .setExpiresOn(namer.getUtcNow().plusDays(2))
                .setPermissions("w"))
        def ids = [identifier, identifier2] as List

        when:
        def response = fsc.setAccessPolicyWithResponse(null, ids, null, null, null)
        def receivedIdentifiers = fsc.getAccessPolicyWithResponse(null, null, null).getValue().getIdentifiers()

        then:
        response.getStatusCode() == 200
        validateBasicHeaders(response.getHeaders())
        receivedIdentifiers.get(0).getAccessPolicy().getExpiresOn() == identifier.getAccessPolicy().getExpiresOn().truncatedTo(ChronoUnit.SECONDS)
        receivedIdentifiers.get(0).getAccessPolicy().getStartsOn() == identifier.getAccessPolicy().getStartsOn().truncatedTo(ChronoUnit.SECONDS)
        receivedIdentifiers.get(0).getAccessPolicy().getPermissions() == identifier.getAccessPolicy().getPermissions()
        receivedIdentifiers.get(1).getAccessPolicy().getExpiresOn() == identifier2.getAccessPolicy().getExpiresOn().truncatedTo(ChronoUnit.SECONDS)
        receivedIdentifiers.get(1).getAccessPolicy().getStartsOn() == identifier2.getAccessPolicy().getStartsOn().truncatedTo(ChronoUnit.SECONDS)
        receivedIdentifiers.get(1).getAccessPolicy().getPermissions() == identifier2.getAccessPolicy().getPermissions()
    }

    @Unroll
    def "Set access policy AC"() {
        setup:
        leaseID = setupFileSystemLeaseCondition(fsc, leaseID)
        def cac = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

        expect:
        fsc.setAccessPolicyWithResponse(null, null, cac, null, null).getStatusCode() == 200

        where:
        modified | unmodified | leaseID
        null     | null       | null
        oldDate  | null       | null
        null     | newDate    | null
        null     | null       | receivedLeaseID
    }

    @Unroll
    def "Set access policy AC fail"() {
        setup:
        def cac = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

        when:
        fsc.setAccessPolicyWithResponse(null, null, cac, null, null)

        then:
        thrown(DataLakeStorageException)

        where:
        modified | unmodified | leaseID
        newDate  | null       | null
        null     | oldDate    | null
        null     | null       | garbageLeaseID
    }

    @Unroll
    def "Set access policy AC illegal"() {
        setup:
        def mac = new DataLakeRequestConditions().setIfMatch(match).setIfNoneMatch(noneMatch)

        when:
        fsc.setAccessPolicyWithResponse(null, null, mac, null, null)

        then:
        thrown(UnsupportedOperationException)

        where:
        match        | noneMatch
        receivedEtag | null
        null         | garbageEtag
    }

    def "Set access policy error"() {
        setup:
        fsc = primaryDataLakeServiceClient.getFileSystemClient(generateFileSystemName())

        when:
        fsc.setAccessPolicy(null, null)

        then:
        thrown(DataLakeStorageException)
    }

    def "Get access policy"() {
        setup:
        def identifier = new DataLakeSignedIdentifier()
            .setId("0000")
            .setAccessPolicy(new DataLakeAccessPolicy()
                .setStartsOn(namer.getUtcNow())
                .setExpiresOn(namer.getUtcNow().plusDays(1))
                .setPermissions("r"))
        def ids = [identifier] as List
        fsc.setAccessPolicy(PublicAccessType.BLOB, ids)
        def response = fsc.getAccessPolicyWithResponse(null, null, null)

        expect:
        response.getStatusCode() == 200
        response.getValue().getDataLakeAccessType() == PublicAccessType.BLOB
        validateBasicHeaders(response.getHeaders())
        response.getValue().getIdentifiers().get(0).getAccessPolicy().getExpiresOn() == identifier.getAccessPolicy().getExpiresOn().truncatedTo(ChronoUnit.SECONDS)
        response.getValue().getIdentifiers().get(0).getAccessPolicy().getStartsOn() == identifier.getAccessPolicy().getStartsOn().truncatedTo(ChronoUnit.SECONDS)
        response.getValue().getIdentifiers().get(0).getAccessPolicy().getPermissions() == identifier.getAccessPolicy().getPermissions()
    }

    def "Get access policy lease"() {
        setup:
        def leaseID = setupFileSystemLeaseCondition(fsc, receivedLeaseID)

        expect:
        fsc.getAccessPolicyWithResponse(leaseID, null, null).getStatusCode() == 200
    }

    def "Get access policy lease fail"() {
        when:
        fsc.getAccessPolicyWithResponse(garbageLeaseID, null, null)

        then:
        thrown(DataLakeStorageException)
    }

    def "Get access policy error"() {
        setup:
        fsc = primaryDataLakeServiceClient.getFileSystemClient(generateFileSystemName())

        when:
        fsc.getAccessPolicy()

        then:
        thrown(DataLakeStorageException)
    }

    def "Builder bearer token validation"() {
        // Technically no additional checks need to be added to datalake builder since the corresponding blob builder fails
        setup:
        String endpoint = BlobUrlParts.parse(fsc.getFileSystemUrl()).setScheme("http").toUrl()
        def builder = new DataLakeFileSystemClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint)

        when:
        builder.buildClient()

        then:
        thrown(IllegalArgumentException)
    }

    def "List Paths OAuth"() {
        setup:
        def client = getOAuthServiceClient()
        def fsClient = client.getFileSystemClient(fsc.getFileSystemName())
        fsClient.createFile(generatePathName())

        when:
        Iterator<PathItem> items = fsClient.listPaths().iterator()

        then:
        items.hasNext()
    }

    def "Set ACL root directory"() {
        setup:
        def dc = fsc.getRootDirectoryClient()

        List<PathAccessControlEntry> pathAccessControlEntries = PathAccessControlEntry.parseList("user::rwx,group::r--,other::---,mask::rwx")

        when:
        def resp = dc.setAccessControlList(pathAccessControlEntries, null, null)

        then:
        notThrown(DataLakeStorageException)
        resp.getETag()
        resp.getLastModified()
    }

    // This tests the policy is in the right place because if it were added per retry, it would be after the credentials and auth would fail because we changed a signed header.
    def "Per call policy"() {
        setup:
        def fsc = getFileSystemClientBuilder(fsc.getFileSystemUrl()).addPolicy(getPerCallVersionPolicy()).credential(env.dataLakeAccount.credential).buildClient()

        when: "blob endpoint"
        def response = fsc.getPropertiesWithResponse(null, null, null)

        then:
        notThrown(DataLakeStorageException)
        response.getHeaders().getValue("x-ms-version") == "2019-02-02"

        when: "dfs endpoint"
        response = fsc.getAccessPolicyWithResponse(null, null, null)

        then:
        notThrown(DataLakeStorageException)
        response.getHeaders().getValue("x-ms-version") == "2019-02-02"
    }

//    def "Rename"() {
//        setup:
//        def newName = generateFileSystemName()
//
//        when:
//        def renamedContainer = fsc.rename(newName)
//
//        then:
//        renamedContainer.getPropertiesWithResponse(null, null, null).getStatusCode() == 200
//
//        cleanup:
//        renamedContainer.delete()
//    }
//
//    def "Rename sas"() {
//        setup:
//        def newName = generateFileSystemName()
//        def sas = primaryDataLakeServiceClient.generateAccountSas(new AccountSasSignatureValues(namer.getUtcNow().plusHours(1), AccountSasPermission.parse("rwdxlacuptf"), AccountSasService.parse("b"), AccountSasResourceType.parse("c")))
//        def sasClient = getFileSystemClient(sas, fsc.getFileSystemUrl())
//
//        when:
//        def renamedContainer = sasClient.rename(newName)
//
//        then:
//        renamedContainer.getPropertiesWithResponse(null, null, null).getStatusCode() == 200
//
//        cleanup:
//        renamedContainer.delete()
//    }
//
//    @Unroll
//    def "Rename AC"() {
//        setup:
//        leaseID = setupFileSystemLeaseCondition(fsc, leaseID)
//        def cac = new DataLakeRequestConditions()
//            .setLeaseId(leaseID)
//
//        expect:
//        fsc.renameWithResponse(new FileSystemRenameOptions(generateFileSystemName()).setRequestConditions(cac),
//            null, null).getStatusCode() == 200
//
//        where:
//        leaseID         || _
//        null            || _
//        receivedLeaseID || _
//    }
//
//    @Unroll
//    def "Rename AC fail"() {
//        setup:
//        def cac = new DataLakeRequestConditions()
//            .setLeaseId(leaseID)
//
//        when:
//        fsc.renameWithResponse(new FileSystemRenameOptions(generateFileSystemName()).setRequestConditions(cac),
//            null, null)
//
//        then:
//        thrown(BlobStorageException)
//
//        where:
//        leaseID         || _
//        garbageLeaseID  || _
//    }
//
//    @Unroll
//    def "Rename AC illegal"() {
//        setup:
//        def ac = new DataLakeRequestConditions().setIfMatch(match).setIfNoneMatch(noneMatch).setIfModifiedSince(modified).setIfUnmodifiedSince(unmodified)
//
//        when:
//        fsc.renameWithResponse(new FileSystemRenameOptions(generateFileSystemName()).setRequestConditions(ac),
//            null, null)
//
//        then:
//        thrown(UnsupportedOperationException)
//
//        where:
//        modified | unmodified | match        | noneMatch
//        oldDate  | null       | null         | null
//        null     | newDate    | null         | null
//        null     | null       | receivedEtag | null
//        null     | null       | null         | garbageEtag
//        null     | null       | null         | null
//    }
//
//    def "Rename error"() {
//        setup:
//        fsc = primaryDataLakeServiceClient.getFileSystemClient(generateFileSystemName())
//        def newName = generateFileSystemName()
//
//        when:
//        fsc.rename(newName)
//
//        then:
//        thrown(BlobStorageException)
//    }

}
