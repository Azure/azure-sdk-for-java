package com.azure.storage.file.datalake

import com.azure.core.util.Context
import com.azure.storage.blob.models.BlobErrorCode
import com.azure.storage.blob.models.BlobStorageException

import com.azure.storage.file.datalake.implementation.models.StorageErrorException
import com.azure.storage.file.datalake.models.*
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
        fsc.getPropertiesWithResponse(leaseID, null, null).getStatusCode() == 200
    }

    def "Get properties lease fail"() {
        when:
        fsc.getPropertiesWithResponse("garbage", null, null)

        then:
        thrown(BlobStorageException)
    }

    def "Get properties error"() {
        setup:
        fsc = primaryDataLakeServiceClient.getFileSystemClient(generateFileSystemName())

        when:
        fsc.getProperties()

        then:
        thrown(BlobStorageException)
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
        thrown(BlobStorageException)

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
        thrown(BlobStorageException)
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
        thrown(BlobStorageException)
    }

    def "Create file min"() {
        when:
        fsc.createFile(generatePathName())

        then:
        notThrown(StorageErrorException)
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
        fsc.createFileWithResponse(generatePathName(), null, null,
            new DataLakeRequestConditions().setIfMatch("garbage"), null, null, null,
            Context.NONE)

        then:
        thrown(StorageErrorException)
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
        def client = fsc.createFileWithResponse(generatePathName(), headers, null, null, null, null, null, null).getValue()
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
        def client = fsc.createFileWithResponse(generatePathName(), null, metadata, null, null, null, null, null).getValue()
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
        fsc.createFileWithResponse(pathName, null, null, drc, null, null, null, null).getStatusCode() == 201

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
        fsc.createFileWithResponse(pathName, null, null, drc, null, null, null, Context.NONE)

        then:
        thrown(StorageErrorException)

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
        fsc.createFileWithResponse(generatePathName(), null, null, null, permissions, umask, null, Context.NONE).getStatusCode() == 201
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
        def e = thrown(BlobStorageException)
        e.getResponse().getStatusCode() == 404
        e.getErrorCode() == BlobErrorCode.BLOB_NOT_FOUND
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
        thrown(StorageErrorException)

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
        def dir = fsc.getDirectoryClient(generatePathName())
        dir.create()

        then:
        notThrown(StorageErrorException)
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
        fsc.createDirectoryWithResponse(generatePathName(), null, null,
            new DataLakeRequestConditions().setIfMatch("garbage"), null, null, null,
            Context.NONE)

        then:
        thrown(Exception)
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
        def client = fsc.createDirectoryWithResponse(generatePathName(), headers, null, null, null, null, null, null).getValue()
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
        def client = fsc.createDirectoryWithResponse(generatePathName(), null, metadata, null, null, null, null, null).getValue()
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
        fsc.createDirectoryWithResponse(pathName, null, null, drc, null, null, null, null).getStatusCode() == 201

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
        fsc.createDirectoryWithResponse(pathName, null, null, drc, null, null, null, Context.NONE)

        then:
        thrown(Exception)

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
        fsc.createDirectoryWithResponse(generatePathName(), null, null, null, permissions, umask, null, Context.NONE).getStatusCode() == 201
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
        def e = thrown(BlobStorageException)
        e.getResponse().getStatusCode() == 404
        e.getErrorCode() == BlobErrorCode.BLOB_NOT_FOUND
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
        thrown(StorageErrorException)

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
        dirPath.getLastModifiedTime()
        dirPath.getOwner()
        dirPath.getPermissions()
//        dirPath.getContentLength()
        dirPath.isDirectory()

        response.hasNext()
        def filePath = response.next()
        filePath.getName() == fileName
        filePath.getETag()
        filePath.getGroup()
        filePath.getLastModifiedTime()
        filePath.getOwner()
        filePath.getPermissions()
//        filePath.getContentLength()
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
        def response = fsc.listPaths(new ListPathsOptions().setReturnUpn(true), null).iterator()

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

//    def "List paths path"() {
//        setup:
//        def dirName = generatePathName()
//        fsc.getDirectoryClient("foo").create()
//
//        def fileName = generatePathName()
//        fsc.getFileClient(fileName).create()
//
//        when:
//        def response = fsc.listPaths(new ListPathsOptions().setPath("foo"), null).iterator()
//
//        then:
//        def dirPath = response.next()
////        response.hasNext()
////        def filePath = response.next()
//        !response.hasNext()
//    }

//    setupFileSystemForGetPaths() {}

}
