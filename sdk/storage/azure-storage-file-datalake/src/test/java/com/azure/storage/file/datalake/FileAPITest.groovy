package com.azure.storage.file.datalake

import com.azure.core.exception.UnexpectedLengthException
import com.azure.core.util.Context
import com.azure.identity.DefaultAzureCredentialBuilder
import com.azure.storage.blob.BlobUrlParts
import com.azure.storage.blob.models.BlobErrorCode
import com.azure.storage.blob.models.BlobStorageException

import com.azure.storage.file.datalake.implementation.models.StorageErrorException
import com.azure.storage.file.datalake.models.*
import spock.lang.Unroll

import java.nio.ByteBuffer
import java.security.MessageDigest

class FileAPITest extends APISpec {
    DataLakeFileClient fc
    String fileName

    PathPermissions permissions = new PathPermissions()
        .setOwner(new RolePermissions().setReadPermission(true).setWritePermission(true).setExecutePermission(true))
        .setGroup(new RolePermissions().setReadPermission(true).setExecutePermission(true))
        .setOther(new RolePermissions().setReadPermission(true))

    List<PathAccessControlEntry> pathAccessControlEntries = PathAccessControlEntry.parseList("user::rwx,group::r--,other::---,mask::rwx")

    String group = null
    String owner = null

    def setup() {
        fileName = generatePathName()
        fc = fsc.getFileClient(fileName)

        fc.create()
    }

    def "Create min"() {
        when:
        fc = fsc.getFileClient(generatePathName())
        fc.create()

        then:
        notThrown(StorageErrorException)
    }

    def "Create defaults"() {
        setup:
        fc = fsc.getFileClient(generatePathName())

        when:
        def createResponse = fc.createWithResponse(null, null, null, null, null, null, null)

        then:
        createResponse.getStatusCode() == 201
        validateBasicHeaders(createResponse.getHeaders())
    }

    def "Create error"() {
        setup:
        fc = fsc.getFileClient(generatePathName())

        when:
        fc.createWithResponse(null, null, null, null, new DataLakeRequestConditions().setIfMatch("garbage"), null,
            Context.NONE)

        then:
        thrown(StorageErrorException)
    }

    @Unroll
    def "Create headers"() {
        // Create does not set md5
        setup:
        def headers = new PathHttpHeaders().setCacheControl(cacheControl)
            .setContentDisposition(contentDisposition)
            .setContentEncoding(contentEncoding)
            .setContentLanguage(contentLanguage)
            .setContentType(contentType)
        fc = fsc.getFileClient(generatePathName())

        when:
        fc.createWithResponse(null, null, headers, null, null, null, null)
        def response = fc.getPropertiesWithResponse(null, null, null)

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
    def "Create metadata"() {
        setup:
        def metadata = new HashMap<String, String>()
        if (key1 != null) {
            metadata.put(key1, value1)
        }
        if (key2 != null) {
            metadata.put(key2, value2)
        }

        when:
        fc.createWithResponse(null, null, null, metadata, null, null, Context.NONE)
        def response = fc.getProperties()

        then:
        response.getMetadata() == metadata

        where:
        key1  | value1 | key2   | value2
        null  | null   | null   | null
        "foo" | "bar"  | "fizz" | "buzz"
    }

    @Unroll
    def "Create AC"() {
        setup:
        match = setupPathMatchCondition(fc, match)
        leaseID = setupPathLeaseCondition(fc, leaseID)
        def drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)


        expect:
        fc.createWithResponse(null, null, null, null, drc, null, null).getStatusCode() == 201

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
    def "Create AC fail"() {
        setup:
        noneMatch = setupPathMatchCondition(fc, noneMatch)
        setupPathLeaseCondition(fc, leaseID)
        def drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

        when:
        fc.createWithResponse(null, null, null, null, drc, null, Context.NONE)

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

    def "Create permissions and umask"() {
        setup:
        def permissions = "0777"
        def umask = "0057"

        expect:
        fc.createWithResponse(permissions, umask, null, null, null, null, Context.NONE).getStatusCode() == 201
    }

    def "Delete min"() {
        expect:
        fc.deleteWithResponse(null, null, null).getStatusCode() == 200
    }

    def "Delete file does not exist anymore"() {
        when:
        fc.deleteWithResponse(null, null, null)
        fc.getPropertiesWithResponse(null, null, null)

        then:
        def e = thrown(BlobStorageException)
        e.getResponse().getStatusCode() == 404
        e.getErrorCode() == BlobErrorCode.BLOB_NOT_FOUND
//        e.getServiceMessage().contains("The specified blob does not exist.")
    }

    @Unroll
    def "Delete AC"() {
        setup:
        match = setupPathMatchCondition(fc, match)
        leaseID = setupPathLeaseCondition(fc, leaseID)
        def drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

        expect:
        fc.deleteWithResponse(drc, null, null).getStatusCode() == 200

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
    def "Delete AC fail"() {
        setup:
        noneMatch = setupPathMatchCondition(fc, noneMatch)
        setupPathLeaseCondition(fc, leaseID)
        def drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

        when:
        fc.deleteWithResponse(drc, null, null).getStatusCode()

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

    def "Set permissions min"() {
        when:
        def resp = fc.setPermissions(permissions, group, owner)

        then:
        notThrown(StorageErrorException)
        resp.getETag()
        resp.getLastModified()
    }

    def "Set permissions with response"() {
        expect:
        fc.setPermissionsWithResponse(permissions, group, owner, null, null, Context.NONE).getStatusCode() == 200
    }

    @Unroll
    def "Set permissions AC"() {
        setup:
        match = setupPathMatchCondition(fc, match)
        leaseID = setupPathLeaseCondition(fc, leaseID)
        def drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

        expect:
        fc.setPermissionsWithResponse(permissions, group, owner, drc, null, Context.NONE).getStatusCode() == 200

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
    def "Set permissions AC fail"() {
        setup:
        noneMatch = setupPathMatchCondition(fc, noneMatch)
        setupPathLeaseCondition(fc, leaseID)
        def drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

        when:
        fc.setPermissionsWithResponse(permissions, group, owner, drc, null, Context.NONE).getStatusCode() == 200

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

    def "Set permissions error"() {
        setup:
        fc = fsc.getFileClient(generatePathName())

        when:
        fc.setPermissionsWithResponse(permissions, group, owner, null, null, null)

        then:
        thrown(StorageErrorException)
    }

    def "Set ACL min"() {
        when:
        def resp = fc.setAccessControlList(pathAccessControlEntries, group, owner)

        then:
        notThrown(StorageErrorException)
        resp.getETag()
        resp.getLastModified()
    }

    def "Set ACL with response"() {
        expect:
        fc.setAccessControlListWithResponse(pathAccessControlEntries, group, owner, null, null, Context.NONE).getStatusCode() == 200
    }

    @Unroll
    def "Set ACL AC"() {
        setup:
        match = setupPathMatchCondition(fc, match)
        leaseID = setupPathLeaseCondition(fc, leaseID)
        def drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

        expect:
        fc.setAccessControlListWithResponse(pathAccessControlEntries, group, owner, drc, null, Context.NONE).getStatusCode() == 200

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
    def "Set ACL AC fail"() {
        setup:
        noneMatch = setupPathMatchCondition(fc, noneMatch)
        setupPathLeaseCondition(fc, leaseID)
        def drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

        when:
        fc.setAccessControlListWithResponse(pathAccessControlEntries, group, owner, drc, null, Context.NONE).getStatusCode() == 200

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

    def "Set ACL error"() {
        setup:
        fc = fsc.getFileClient(generatePathName())

        when:
        fc.setAccessControlList(pathAccessControlEntries, group, owner)

        then:
        thrown(StorageErrorException)
    }

    def "Get access control min"() {
        when:
        PathAccessControl pac = fc.getAccessControl()

        then:
        notThrown(StorageErrorException)
        pac.getAccessControlList()
        pac.getPermissions()
        pac.getOwner()
        pac.getGroup()
    }

    def "Get access control with response"() {
        expect:
        fc.getAccessControlWithResponse(false, null, null, null).getStatusCode() == 200
    }

    def "Get access control return upn"() {
        expect:
        fc.getAccessControlWithResponse(true, null, null, null).getStatusCode() == 200
    }

    @Unroll
    def "Get access control AC"() {
        setup:
        match = setupPathMatchCondition(fc, match)
        leaseID = setupPathLeaseCondition(fc, leaseID)
        def drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)


        expect:
        fc.getAccessControlWithResponse(false, drc, null, null).getStatusCode() == 200

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
    def "Get access control AC fail"() {
        setup:
        noneMatch = setupPathMatchCondition(fc, noneMatch)
        setupPathLeaseCondition(fc, leaseID)
        def drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

        when:
        fc.getAccessControlWithResponse(false, drc, null, null).getStatusCode() == 200

        then:
        thrown(StorageErrorException)

        where:
        modified | unmodified | match       | noneMatch    | leaseID
        newDate  | null       | null        | null         | null
        null     | oldDate    | null        | null         | null
        null     | null       | garbageEtag | null         | null
        null     | null       | null        | receivedEtag | null
//        null     | null       | null        | null         | garbageLeaseID
    // known bug in dfs endpoint so this test fails
    }

    def "Get properties default"() {
        when:
        def response = fc.getPropertiesWithResponse(null, null, null)
        def headers = response.getHeaders()
        def properties = response.getValue()

        then:
        validateBasicHeaders(headers)
        headers.getValue("Accept-Ranges") == "bytes"
        properties.getCreationTime()
        properties.getLastModified()
        properties.getETag()
        properties.getFileSize() >= 0
        properties.getContentType()
        !properties.getContentMd5() // tested in "set HTTP headers"
        !properties.getContentEncoding() // tested in "set HTTP headers"
        !properties.getContentDisposition() // tested in "set HTTP headers"
        !properties.getContentLanguage() // tested in "set HTTP headers"
        !properties.getCacheControl() // tested in "set HTTP headers"
        properties.getLeaseStatus() == LeaseStatusType.UNLOCKED
        properties.getLeaseState() == LeaseStateType.AVAILABLE
        !properties.getLeaseDuration() // tested in "acquire lease"
        !properties.getCopyId() // tested in "abort copy"
        !properties.getCopyStatus() // tested in "copy"
        !properties.getCopySource() // tested in "copy"
        !properties.getCopyProgress() // tested in "copy"
        !properties.getCopyCompletionTime() // tested in "copy"
        !properties.getCopyStatusDescription() // only returned when the service has errors; cannot validate.
        properties.isServerEncrypted()
        !properties.isIncrementalCopy() // tested in PageBlob."start incremental copy"
        properties.getAccessTier() == AccessTier.HOT
        properties.getArchiveStatus() == null
        !properties.getMetadata() // new file does not have default metadata associated
        !properties.getAccessTierChangeTime()
        !properties.getEncryptionKeySha256()

    }

    def "Get properties min"() {
        expect:
        fc.getPropertiesWithResponse(null, null, null).getStatusCode() == 200
    }

    @Unroll
    def "Get properties AC"() {
        setup:
        def drc = new DataLakeRequestConditions()
            .setLeaseId(setupPathLeaseCondition(fc, leaseID))
            .setIfMatch(setupPathMatchCondition(fc, match))
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

        expect:
        fc.getPropertiesWithResponse(drc, null, null).getStatusCode() == 200

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
    def "Get properties AC fail"() {
        setup:
        def drc = new DataLakeRequestConditions()
            .setLeaseId(setupPathLeaseCondition(fc, leaseID))
            .setIfMatch(match)
            .setIfNoneMatch(setupPathMatchCondition(fc, noneMatch))
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

        when:
        fc.getPropertiesWithResponse(drc, null, null)

        then:
        thrown(BlobStorageException)

        where:
        modified | unmodified | match       | noneMatch    | leaseID
        newDate  | null       | null        | null         | null
        null     | oldDate    | null        | null         | null
        null     | null       | garbageEtag | null         | null
        null     | null       | null        | receivedEtag | null
        null     | null       | null        | null         | garbageLeaseID
    }

    def "Get properties error"() {
        setup:
        fc = fsc.getFileClient(generatePathName())

        when:
        fc.getProperties()

        then:
        thrown(BlobStorageException)
    }

    def "Set HTTP headers null"() {
        setup:
        def response = fc.setHttpHeadersWithResponse(null, null, null, null)

        expect:
        response.getStatusCode() == 200
        validateBasicHeaders(response.getHeaders())
    }

    def "Set HTTP headers min"() {
        setup:
        def properties = fc.getProperties()
        def headers = new PathHttpHeaders()
            .setContentEncoding(properties.getContentEncoding())
            .setContentDisposition(properties.getContentDisposition())
            .setContentType("type")
            .setCacheControl(properties.getCacheControl())
            .setContentLanguage(properties.getContentLanguage())
            .setContentMd5(Base64.getEncoder().encode(MessageDigest.getInstance("MD5").digest(defaultData.array())))

        fc.setHttpHeaders(headers)

        expect:
        fc.getProperties().getContentType() == "type"
    }

    @Unroll
    def "Set HTTP headers headers"() {
        setup:
        fc.append(new ByteArrayInputStream(defaultData.array()), 0, defaultDataSize)
        fc.flush(defaultDataSize)
        def putHeaders = new PathHttpHeaders()
            .setCacheControl(cacheControl)
            .setContentDisposition(contentDisposition)
            .setContentEncoding(contentEncoding)
            .setContentLanguage(contentLanguage)
            .setContentMd5(contentMD5)
            .setContentType(contentType)

        fc.setHttpHeaders(putHeaders)

        expect:
        validatePathProperties(
            fc.getPropertiesWithResponse(null, null, null),
            cacheControl, contentDisposition, contentEncoding, contentLanguage, contentMD5, contentType)

        where:
        cacheControl | contentDisposition | contentEncoding | contentLanguage | contentMD5                                                                               | contentType
        null         | null               | null            | null            | null                                                                                     | null
        "control"    | "disposition"      | "encoding"      | "language"      | Base64.getEncoder().encode(MessageDigest.getInstance("MD5").digest(defaultData.array())) | "type"
    }

    @Unroll
    def "Set HTTP headers AC"() {
        setup:
        match = setupPathMatchCondition(fc, match)
        leaseID = setupPathLeaseCondition(fc, leaseID)
        def drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

        expect:
        fc.setHttpHeadersWithResponse(null, drc, null, null).getStatusCode() == 200

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
    def "Set HTTP headers AC fail"() {
        setup:
        noneMatch = setupPathMatchCondition(fc, noneMatch)
        setupPathLeaseCondition(fc, leaseID)
        def drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

        when:
        fc.setHttpHeadersWithResponse(null, drc, null, null)

        then:
        thrown(BlobStorageException)

        where:
        modified | unmodified | match       | noneMatch    | leaseID
        newDate  | null       | null        | null         | null
        null     | oldDate    | null        | null         | null
        null     | null       | garbageEtag | null         | null
        null     | null       | null        | receivedEtag | null
        null     | null       | null        | null         | garbageLeaseID
    }

    def "Set HTTP headers error"() {
        setup:
        fc = fsc.getFileClient(generatePathName())

        when:
        fc.setHttpHeaders(null)

        then:
        thrown(BlobStorageException)
    }

    def "Set metadata min"() {
        setup:
        def metadata = new HashMap<String, String>()
        metadata.put("foo", "bar")

        when:
        fc.setMetadata(metadata)

        then:
        fc.getProperties().getMetadata() == metadata
    }

    @Unroll
    def "Set metadata metadata"() {
        setup:
        def metadata = new HashMap<String, String>()
        if (key1 != null && value1 != null) {
            metadata.put(key1, value1)
        }
        if (key2 != null && value2 != null) {
            metadata.put(key2, value2)
        }

        expect:
        fc.setMetadataWithResponse(metadata, null, null, null).getStatusCode() == statusCode
        fc.getProperties().getMetadata() == metadata

        where:
        key1  | value1 | key2   | value2 || statusCode
        null  | null   | null   | null   || 200
        "foo" | "bar"  | "fizz" | "buzz" || 200
    }

    @Unroll
    def "Set metadata AC"() {
        setup:
        match = setupPathMatchCondition(fc, match)
        leaseID = setupPathLeaseCondition(fc, leaseID)
        def drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

        expect:
        fc.setMetadataWithResponse(null, drc, null, null).getStatusCode() == 200

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
    def "Set metadata AC fail"() {
        setup:
        noneMatch = setupPathMatchCondition(fc, noneMatch)
        setupPathLeaseCondition(fc, leaseID)

        def drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

        when:
        fc.setMetadataWithResponse(null, drc, null, null)

        then:
        thrown(BlobStorageException)

        where:
        modified | unmodified | match       | noneMatch    | leaseID
        newDate  | null       | null        | null         | null
        null     | oldDate    | null        | null         | null
        null     | null       | garbageEtag | null         | null
        null     | null       | null        | receivedEtag | null
        null     | null       | null        | null         | garbageLeaseID
    }

    def "Set metadata error"() {
        setup:
        fc = fsc.getFileClient(generatePathName())

        when:
        fc.setMetadata(null)

        then:
        thrown(BlobStorageException)
    }

    def "Read all null"() {
        setup:
        fc.append(new ByteArrayInputStream(defaultData.array()), 0, defaultDataSize)
        fc.flush(defaultDataSize)

        when:
        def stream = new ByteArrayOutputStream()
        def response = fc.readWithResponse(stream, null, null, null, false, null, null)
        def body = ByteBuffer.wrap(stream.toByteArray())
        def headers = response.getHeaders()

        then:
        body == defaultData
        headers.toMap().keySet().stream().noneMatch({ it.startsWith("x-ms-meta-") })
        headers.getValue("Content-Length") != null
        headers.getValue("Content-Type") != null
        headers.getValue("Content-Range") == null
        headers.getValue("Content-Encoding") == null
        headers.getValue("Cache-Control") == null
        headers.getValue("Content-Disposition") == null
        headers.getValue("Content-Language") == null
        headers.getValue("x-ms-blob-sequence-number") == null
        headers.getValue("x-ms-copy-completion-time") == null
        headers.getValue("x-ms-copy-status-description") == null
        headers.getValue("x-ms-copy-id") == null
        headers.getValue("x-ms-copy-progress") == null
        headers.getValue("x-ms-copy-source") == null
        headers.getValue("x-ms-copy-status") == null
        headers.getValue("x-ms-lease-duration") == null
        headers.getValue("x-ms-lease-state") == LeaseStateType.AVAILABLE.toString()
        headers.getValue("x-ms-lease-status") == LeaseStatusType.UNLOCKED.toString()
        headers.getValue("Accept-Ranges") == "bytes"
        headers.getValue("x-ms-blob-committed-block-count") == null
        headers.getValue("x-ms-server-encrypted") != null
        headers.getValue("x-ms-blob-content-md5") == null
    }

    def "Read empty file"() {
        setup:
        fc = fsc.getFileClient("emptyFile")
        fc.create()

        when:
        def outStream = new ByteArrayOutputStream()
        fc.read(outStream)
        def result = outStream.toByteArray()

        then:
        notThrown(BlobStorageException)
        result.length == 0
    }

    /*
    This is to test the appropriate integration of DownloadResponse, including setting the correct range values on
    HttpGetterInfo.
     */

    def "Read with retry range"() {
        /*
        We are going to make a request for some range on a blob. The Flux returned will throw an exception, forcing
        a retry per the DownloadRetryOptions. The next request should have the same range header, which was generated
        from the count and offset values in HttpGetterInfo that was constructed on the initial call to download. We
        don't need to check the data here, but we want to ensure that the correct range is set each time. This will
        test the correction of a bug that was found which caused HttpGetterInfo to have an incorrect offset when it was
        constructed in BlobClient.download().
         */
        setup:
        def fileClient = getFileClient(primaryCredential, fc.getPathUrl(), new MockRetryRangeResponsePolicy())

        fc.append(new ByteArrayInputStream(defaultData.array()), 0, defaultDataSize)
        fc.flush(defaultDataSize)

        when:
        def range = new FileRange(2, 5L)
        def options = new DownloadRetryOptions().setMaxRetryRequests(3)
        fileClient.readWithResponse(new ByteArrayOutputStream(), range, options, null, false, null, null)

        then:
        /*
        Because the dummy Flux always throws an error. This will also validate that an IllegalArgumentException is
        NOT thrown because the types would not match.
         */
        def e = thrown(RuntimeException)
        e.getCause() instanceof IOException
    }

    def "Read min"() {
        setup:
        fc.append(new ByteArrayInputStream(defaultData.array()), 0, defaultDataSize)
        fc.flush(defaultDataSize)

        when:
        def outStream = new ByteArrayOutputStream()
        fc.read(outStream)
        def result = outStream.toByteArray()

        then:
        result == defaultData.array()
    }

    @Unroll
    def "Read range"() {
        setup:
        def range = (count == null) ? new FileRange(offset) : new FileRange(offset, count)
        fc.append(new ByteArrayInputStream(defaultData.array()), 0, defaultDataSize)
        fc.flush(defaultDataSize)


        when:
        def outStream = new ByteArrayOutputStream()
        fc.readWithResponse(outStream, range, null, null, false, null, null)
        String bodyStr = outStream.toString()

        then:
        bodyStr == expectedData

        where:
        offset | count || expectedData
        0      | null  || defaultText
        0      | 5L    || defaultText.substring(0, 5)
        3      | 2L    || defaultText.substring(3, 3 + 2)
    }

    @Unroll
    def "Read AC"() {
        setup:
        match = setupPathMatchCondition(fc, match)
        leaseID = setupPathLeaseCondition(fc, leaseID)
        def drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

        when:
        def response = fc.readWithResponse(new ByteArrayOutputStream(), null, null, drc, false, null, null)

        then:
        response.getStatusCode() == 200

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
    def "Read AC fail"() {
        setup:
        setupPathLeaseCondition(fc, leaseID)
        def drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(setupPathMatchCondition(fc, noneMatch))
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

        when:
        fc.readWithResponse(new ByteArrayOutputStream(), null, null, drc, false, null, null).getStatusCode()

        then:
        thrown(BlobStorageException)

        where:
        modified | unmodified | match       | noneMatch    | leaseID
        newDate  | null       | null        | null         | null
        null     | oldDate    | null        | null         | null
        null     | null       | garbageEtag | null         | null
        null     | null       | null        | receivedEtag | null
        null     | null       | null        | null         | garbageLeaseID
    }

    def "Read md5"() {
        setup:
        fc.append(new ByteArrayInputStream(defaultData.array()), 0, defaultDataSize)
        fc.flush(defaultDataSize)

        when:
        def response = fc.readWithResponse(new ByteArrayOutputStream(), new FileRange(0, 3), null, null, true, null, null)
        def contentMD5 = response.getHeaders().getValue("content-md5").getBytes()

        then:
        contentMD5 == Base64.getEncoder().encode(MessageDigest.getInstance("MD5").digest(defaultText.substring(0, 3).getBytes()))
    }

    def "Read error"() {
        setup:
        fc = fsc.getFileClient(generatePathName())

        when:
        fc.read(null)

        then:
        thrown(NullPointerException)
    }

    def "Rename min"() {
        expect:
        fc.renameWithResponse(generatePathName(), null, null, null, null).getStatusCode() == 201
    }

    def "Rename with response"() {
        when:
        def resp = fc.renameWithResponse(generatePathName(), null, null, null, null)

        def renamedClient = resp.getValue()
        renamedClient.getProperties()

        then:
        notThrown(StorageErrorException)

        when:
        fc.getProperties()

        then:
        thrown(BlobStorageException)
    }

    def "Rename error"() {
        setup:
        fc = fsc.getFileClient(generatePathName())

        when:
        fc.renameWithResponse(generatePathName(), null, null, null, null)

        then:
        thrown(StorageErrorException)
    }

    @Unroll
    def "Rename source AC"() {
        setup:
        match = setupPathMatchCondition(fc, match)
        leaseID = setupPathLeaseCondition(fc, leaseID)
        def drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

        expect:
        fc.renameWithResponse(generatePathName(), drc, null, null, null).getStatusCode() == 201

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
    def "Rename source AC fail"() {
        setup:
        fc = fsc.getFileClient(generatePathName())
        fc.create()

        noneMatch = setupPathMatchCondition(fc, noneMatch)
        setupPathLeaseCondition(fc, leaseID)
        def drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

        when:
        fc.renameWithResponse(generatePathName(), drc, null, null, null)

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

    @Unroll
    def "Rename dest AC"() {
        setup:
        def pathName = generatePathName()
        def destFile = fsc.getFileClient(pathName)
        destFile.create()
        match = setupPathMatchCondition(destFile, match)
        leaseID = setupPathLeaseCondition(destFile, leaseID)
        def drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

        expect:
        fc.renameWithResponse(pathName, null, drc, null, null).getStatusCode() == 201

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
    def "Rename dest AC fail"() {
        setup:
        def pathName = generatePathName()
        def destFile = fsc.getFileClient(pathName)
        destFile.create()
        noneMatch = setupPathMatchCondition(destFile, noneMatch)
        setupPathLeaseCondition(destFile, leaseID)
        def drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

        when:
        fc.renameWithResponse(pathName, null, drc, null, null)

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

    def "Append data min"() {
        when:
        fc.append(new ByteArrayInputStream(defaultData.array()), 0, defaultDataSize)

        then:
        notThrown(StorageErrorException)
    }

    def "Append data"() {
        setup:
        def response = fc.appendWithResponse(defaultInputStream.get(), 0, defaultDataSize, null, null, null, null)
        def headers = response.getHeaders()

        expect:
        response.getStatusCode() == 202
        headers.getValue("x-ms-request-id") != null
        headers.getValue("x-ms-version") != null
        headers.getValue("Date") != null
        Boolean.parseBoolean(headers.getValue("x-ms-request-server-encrypted"))
    }

    def "Append data md5"() {
        setup:
        fc = fsc.getFileClient(generatePathName())
        fc.create()
        def md5 = MessageDigest.getInstance("MD5").digest(defaultText.getBytes())
        def response = fc.appendWithResponse(defaultInputStream.get(), 0, defaultDataSize, md5, null, null, null)
        def headers = response.getHeaders()

        expect:
        response.getStatusCode() == 202
        headers.getValue("x-ms-request-id") != null
        headers.getValue("x-ms-version") != null
        headers.getValue("Date") != null
        Boolean.parseBoolean(headers.getValue("x-ms-request-server-encrypted"))
    }

    @Unroll
    def "Append data illegal arguments"() {
        when:
        fc.append(data == null ? null : data.get(), 0, dataSize)

        then:
        thrown(exceptionType)

        where:
        data               | dataSize            | exceptionType
        null               | defaultDataSize     | NullPointerException
        defaultInputStream | defaultDataSize + 1 | UnexpectedLengthException
        defaultInputStream | defaultDataSize - 1 | UnexpectedLengthException
    }

    def "Append data empty body"() {
        when:
        fc = fsc.getFileClient(generatePathName())
        fc.create()
        fc.append(new ByteArrayInputStream(new byte[0]), 0, 0)

        then:
        thrown(StorageErrorException)
    }

    def "Append data null body"() {
        when:
        fc = fsc.getFileClient(generatePathName())
        fc.create()
        fc.append(null, 0, 0)

        then:
        thrown(NullPointerException)
    }

    def "Append data lease"() {
        setup:
        def leaseID = setupPathLeaseCondition(fc, receivedLeaseID)

        expect:
        fc.appendWithResponse(defaultInputStream.get(), 0, defaultDataSize, null, leaseID, null, null).getStatusCode() == 202
    }

    def "Append data lease fail"() {
        setup:
        setupPathLeaseCondition(fc, receivedLeaseID)

        when:
        fc.appendWithResponse(defaultInputStream.get(), 0, defaultDataSize, null, garbageLeaseID, null, null)

        then:
        def e = thrown(StorageErrorException)
        e.getResponse().getStatusCode() == 412
    }

    def "Append data error"() {
        setup:
        fc = fsc.getFileClient(generatePathName())

        when:
        fc.appendWithResponse(defaultInputStream.get(), 0, defaultDataSize, null, null, null, null)

        then:
        def e = thrown(StorageErrorException)
        e.getResponse().getStatusCode() == 404
    }

    def "Flush data min"() {
        when:
        fc.append(new ByteArrayInputStream(defaultData.array()), 0, defaultDataSize)
        fc.flush(defaultDataSize)

        then:
        notThrown(StorageErrorException)
    }

    def "Flush close"() {
        when:
        fc = fsc.getFileClient(generatePathName())
        fc.create()
        fc.append(defaultInputStream.get(), 0, defaultDataSize)
        fc.flushWithResponse(defaultDataSize, false, true, null, null, null, null)

        then:
        notThrown(StorageErrorException)
    }

    def "Flush retain uncommitted data "() {
        when:
        fc = fsc.getFileClient(generatePathName())
        fc.create()
        fc.append(defaultInputStream.get(), 0, defaultDataSize)
        fc.flushWithResponse(defaultDataSize, true, false, null, null, null, null)

        then:
        notThrown(StorageErrorException)
    }

    def "Flush IA"() {
        when:
        fc = fsc.getFileClient(generatePathName())
        fc.create()
        fc.append(defaultInputStream.get(), 0, defaultDataSize)
        fc.flushWithResponse(4, false, false, null, null, null, null)

        then:
        thrown(StorageErrorException)
    }

    @Unroll
    def "Flush headers"() {
        setup:
        fc = fsc.getFileClient(generatePathName())
        fc.create()
        fc.append(defaultInputStream.get(), 0, defaultDataSize)
        def headers = new PathHttpHeaders().setCacheControl(cacheControl)
            .setContentDisposition(contentDisposition)
            .setContentEncoding(contentEncoding)
            .setContentLanguage(contentLanguage)
            .setContentType(contentType)

        when:
        fc.flushWithResponse(defaultDataSize, false, false, headers, null, null, null)
        def response = fc.getPropertiesWithResponse(null, null, null)

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
    def "Flush AC"() {
        setup:
        fc = fsc.getFileClient(generatePathName())
        fc.create()
        fc.append(defaultInputStream.get(), 0, defaultDataSize)

        match = setupPathMatchCondition(fc, match)
        leaseID = setupPathLeaseCondition(fc, leaseID)
        def drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)


        expect:
        fc.flushWithResponse(defaultDataSize, false, false, null, drc, null, null).getStatusCode() == 200

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
    def "Flush AC fail"() {
        setup:
        fc = fsc.getFileClient(generatePathName())
        fc.create()
        fc.append(defaultInputStream.get(), 0, defaultDataSize)
        noneMatch = setupPathMatchCondition(fc, noneMatch)
        setupPathLeaseCondition(fc, leaseID)
        def drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

        when:
        fc.flushWithResponse(defaultDataSize, false, false, null, drc, null, null)
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

    def "Flush error"() {
        setup:
        fc = fsc.getFileClient(generatePathName())

        when:
        fc.flush(1)

        then:
        thrown(StorageErrorException)
    }

    def "Get File Name and Build Client"() {
        when:
        DataLakeFileClient client = fsc.getFileClient(originalFileName)

        then:
        // Note : Here I use Path because there is a test that tests the use of a /
        client.getFilePath() == finalFileName

        where:
        originalFileName       | finalFileName
        "file"                 | "file"
        "path/to]a file"       | "path/to]a file"
        "path%2Fto%5Da%20file" | "path/to]a file"
        "斑點"                   | "斑點"
        "%E6%96%91%E9%BB%9E"   | "斑點"
    }

    def "Builder bearer token validation"() {
        // Technically no additional checks need to be added to datalake builder since the corresponding blob builder fails
        setup:
        String endpoint = BlobUrlParts.parse(fc.getFileUrl()).setScheme("http").toUrl()
        def builder = new DataLakePathClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint)

        when:
        builder.buildFileClient()

        then:
        thrown(IllegalArgumentException)
    }

}
