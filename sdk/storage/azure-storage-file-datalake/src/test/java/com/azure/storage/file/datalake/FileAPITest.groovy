package com.azure.storage.file.datalake

import com.azure.core.exception.UnexpectedLengthException
import com.azure.core.util.Context
import com.azure.core.util.FluxUtil
import com.azure.identity.DefaultAzureCredentialBuilder
import com.azure.storage.blob.BlobAsyncClient
import com.azure.storage.blob.BlobClient
import com.azure.storage.blob.BlobUrlParts
import com.azure.storage.blob.models.BlobErrorCode
import com.azure.storage.blob.models.BlobStorageException
import com.azure.storage.blob.models.BlobType
import com.azure.storage.common.ParallelTransferOptions
import com.azure.storage.common.ProgressReceiver
import com.azure.storage.common.implementation.Constants
import com.azure.storage.file.datalake.models.*
import reactor.core.Exceptions
import reactor.core.publisher.Flux
import reactor.core.publisher.Hooks
import reactor.test.StepVerifier
import spock.lang.Requires
import spock.lang.Unroll

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.nio.file.FileAlreadyExistsException
import java.nio.file.OpenOption
import java.nio.file.StandardOpenOption
import java.nio.file.Files
import java.security.MessageDigest
import java.time.Duration

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
        notThrown(DataLakeStorageException)
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
        thrown(DataLakeStorageException)
    }

    def "Create overwrite"() {
        when:
        fc = fsc.getFileClient(generatePathName())
        fc.create()

        // Try to create the resource again
        fc.create(false)

        then:
        thrown(DataLakeStorageException)
    }

    def "Exists"() {
        when:
        fc = fsc.getFileClient(generatePathName())
        fc.create()

        then:
        fc.exists()
    }

    def "Does not exist"() {
        expect:
        !fsc.getFileClient(generatePathName()).exists()
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
        thrown(DataLakeStorageException)

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
        def e = thrown(DataLakeStorageException)
        e.getResponse().getStatusCode() == 404
        e.getErrorCode() == BlobErrorCode.BLOB_NOT_FOUND.toString()
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
        thrown(DataLakeStorageException)

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
        notThrown(DataLakeStorageException)
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
        thrown(DataLakeStorageException)

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
        thrown(DataLakeStorageException)
    }

    def "Set ACL min"() {
        when:
        def resp = fc.setAccessControlList(pathAccessControlEntries, group, owner)

        then:
        notThrown(DataLakeStorageException)
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
        thrown(DataLakeStorageException)

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
        thrown(DataLakeStorageException)
    }

    def "Get access control min"() {
        when:
        PathAccessControl pac = fc.getAccessControl()

        then:
        notThrown(DataLakeStorageException)
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
        thrown(DataLakeStorageException)

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
        thrown(DataLakeStorageException)

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
        thrown(DataLakeStorageException)
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
        thrown(DataLakeStorageException)

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
        thrown(DataLakeStorageException)
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
        thrown(DataLakeStorageException)

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
        thrown(DataLakeStorageException)
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
        notThrown(DataLakeStorageException)
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
        thrown(DataLakeStorageException)

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

    def "Download to file exists"() {
        setup:
        def testFile = new File(testName + ".txt")
        if (!testFile.exists()) {
            assert testFile.createNewFile()
        }
        fc.append(new ByteArrayInputStream(defaultData.array()), 0, defaultDataSize)
        fc.flush(defaultDataSize)

        when:
        // Default overwrite is false so this should fail
        fc.readToFile(testFile.getPath())

        then:
        def ex = thrown(UncheckedIOException)
        ex.getCause() instanceof FileAlreadyExistsException

        cleanup:
        testFile.delete()
    }

    def "Download to file exists succeeds"() {
        setup:
        def testFile = new File(testName + ".txt")
        if (!testFile.exists()) {
            assert testFile.createNewFile()
        }
        fc.append(new ByteArrayInputStream(defaultData.array()), 0, defaultDataSize)
        fc.flush(defaultDataSize)

        when:
        fc.readToFile(testFile.getPath(), true)

        then:
        new String(Files.readAllBytes(testFile.toPath()), StandardCharsets.UTF_8) == defaultText

        cleanup:
        testFile.delete()
    }

    def "Download to file does not exist"() {
        setup:
        def testFile = new File(testName + ".txt")
        if (testFile.exists()) {
            assert testFile.delete()
        }
        fc.append(new ByteArrayInputStream(defaultData.array()), 0, defaultDataSize)
        fc.flush(defaultDataSize)

        when:
        fc.readToFile(testFile.getPath())

        then:
        new String(Files.readAllBytes(testFile.toPath()), StandardCharsets.UTF_8) == defaultText

        cleanup:
        testFile.delete()
    }

    def "Download file does not exist open options"() {
        setup:
        def testFile = new File(testName + ".txt")
        if (testFile.exists()) {
            assert testFile.delete()
        }
        fc.append(new ByteArrayInputStream(defaultData.array()), 0, defaultDataSize)
        fc.flush(defaultDataSize)

        when:
        Set<OpenOption> openOptions = new HashSet<>()
        openOptions.add(StandardOpenOption.CREATE_NEW)
        openOptions.add(StandardOpenOption.READ)
        openOptions.add(StandardOpenOption.WRITE)
        fc.readToFileWithResponse(testFile.getPath(), null, null, null, null, false, openOptions, null, null)

        then:
        new String(Files.readAllBytes(testFile.toPath()), StandardCharsets.UTF_8) == defaultText

        cleanup:
        testFile.delete()
    }

    def "Download file exist open options"() {
        setup:
        def testFile = new File(testName + ".txt")
        if (!testFile.exists()) {
            assert testFile.createNewFile()
        }
        fc.append(new ByteArrayInputStream(defaultData.array()), 0, defaultDataSize)
        fc.flush(defaultDataSize)

        when:
        Set<OpenOption> openOptions = new HashSet<>()
        openOptions.add(StandardOpenOption.CREATE)
        openOptions.add(StandardOpenOption.TRUNCATE_EXISTING)
        openOptions.add(StandardOpenOption.READ)
        openOptions.add(StandardOpenOption.WRITE)
        fc.readToFileWithResponse(testFile.getPath(), null, null, null, null, false, openOptions, null, null)

        then:
        new String(Files.readAllBytes(testFile.toPath()), StandardCharsets.UTF_8) == defaultText

        cleanup:
        testFile.delete()
    }

    @Requires({ liveMode() })
    @Unroll
    def "Download file"() {
        setup:
        def file = getRandomFile(fileSize)
        fc.uploadFromFile(file.toPath().toString(), true)
        def outFile = new File(resourceNamer.randomName(testName, 60) + ".txt")
        if (outFile.exists()) {
            assert outFile.delete()
        }

        when:
        def properties = fc.readToFileWithResponse(outFile.toPath().toString(), null,
            new ParallelTransferOptions(4 * 1024 * 1024, null, null, null), null, null, false, null, null, null)

        then:
        compareFiles(file, outFile, 0, fileSize)
        properties.getValue().fileSize == fileSize

        cleanup:
        outFile.delete()
        file.delete()

        where:
        fileSize             | _
        20                   | _ // small file
        16 * 1024 * 1024     | _ // medium file in several chunks
        8 * 1026 * 1024 + 10 | _ // medium file not aligned to block
        50 * Constants.MB    | _ // large file requiring multiple requests
        // Files larger than 2GB to test no integer overflow are left to stress/perf tests to keep test passes short.
    }

    @Requires({ liveMode() })
    @Unroll
    def "Download file sync buffer copy"() {
        setup:
        def fileSystemName = generateFileSystemName()
        def datalakeServiceClient = new DataLakeServiceClientBuilder()
            .endpoint(String.format(defaultEndpointTemplate, primaryCredential.getAccountName()))
            .credential(primaryCredential)
            .buildClient()

        def fileClient = datalakeServiceClient.createFileSystem(fileSystemName)
            .getFileClient(generatePathName())


        def file = getRandomFile(fileSize)
        fileClient.uploadFromFile(file.toPath().toString(), true)
        def outFile = new File(resourceNamer.randomName(testName, 60) + ".txt")
        if (outFile.exists()) {
            assert outFile.delete()
        }

        when:
        def properties = fileClient.readToFileWithResponse(outFile.toPath().toString(), null,
            new ParallelTransferOptions(4 * 1024 * 1024, null, null, null), null, null, false, null, null, null)

        then:
        compareFiles(file, outFile, 0, fileSize)
        properties.getValue().getFileSize() == fileSize

        cleanup:
        datalakeServiceClient.deleteFileSystem(fileSystemName)
        outFile.delete()
        file.delete()

        where:
        fileSize             | _
        20                   | _ // small file
        16 * 1024 * 1024     | _ // medium file in several chunks
        8 * 1026 * 1024 + 10 | _ // medium file not aligned to block
        50 * Constants.MB    | _ // large file requiring multiple requests
    }

    @Requires({ liveMode() })
    @Unroll
    def "Download file async buffer copy"() {
        setup:
        def fileSystemName = generateFileSystemName()
        def datalakeServiceAsyncClient = new DataLakeServiceClientBuilder()
            .endpoint(String.format(defaultEndpointTemplate, primaryCredential.getAccountName()))
            .credential(primaryCredential)
            .buildAsyncClient()

        def fileAsyncClient = datalakeServiceAsyncClient.createFileSystem(fileSystemName).block()
            .getFileAsyncClient(generatePathName())

        def file = getRandomFile(fileSize)
        fileAsyncClient.uploadFromFile(file.toPath().toString(), true).block()
        def outFile = new File(resourceNamer.randomName(testName, 60) + ".txt")
        if (outFile.exists()) {
            assert outFile.delete()
        }

        when:
        def downloadMono = fileAsyncClient.readToFileWithResponse(outFile.toPath().toString(), null,
            new ParallelTransferOptions(4 * 1024 * 1024, null, null, null), null, null, false, null)

        then:
        StepVerifier.create(downloadMono)
            .assertNext({ it -> it.getValue().getFileSize() == fileSize })
            .verifyComplete()

        compareFiles(file, outFile, 0, fileSize)

        cleanup:
        datalakeServiceAsyncClient.deleteFileSystem(fileSystemName)
        outFile.delete()
        file.delete()

        where:
        fileSize             | _
        20                   | _ // small file
        16 * 1024 * 1024     | _ // medium file in several chunks
        8 * 1026 * 1024 + 10 | _ // medium file not aligned to block
        50 * Constants.MB    | _ // large file requiring multiple requests
    }
    @Unroll
    def "Download file range"() {
        setup:
        def file = getRandomFile(defaultDataSize)
        fc.uploadFromFile(file.toPath().toString(), true)
        def outFile = new File(resourceNamer.randomName(testName, 60))
        if (outFile.exists()) {
            assert outFile.delete()
        }

        when:
        fc.readToFileWithResponse(outFile.toPath().toString(), range, null, null, null, false, null, null, null)

        then:
        compareFiles(file, outFile, range.getOffset(), range.getCount())

        cleanup:
        outFile.delete()
        file.delete()

        /*
        The last case is to test a range much much larger than the size of the file to ensure we don't accidentally
        send off parallel requests with invalid ranges.
         */
        where:
        range                                         | _
        new FileRange(0, defaultDataSize)             | _ // Exact count
        new FileRange(1, defaultDataSize - 1 as Long) | _ // Offset and exact count
        new FileRange(3, 2)                           | _ // Narrow range in middle
        new FileRange(0, defaultDataSize - 1 as Long) | _ // Count that is less than total
        new FileRange(0, 10 * 1024)                   | _ // Count much larger than remaining data
    }

    @Unroll
    def "Download file range fail"() {
        setup:
        def file = getRandomFile(defaultDataSize)
        fc.uploadFromFile(file.toPath().toString(), true)
        def outFile = new File(testName + "")
        if (outFile.exists()) {
            assert outFile.delete()
        }

        when:
        fc.readToFileWithResponse(outFile.toPath().toString(), new FileRange(defaultDataSize + 1), null, null, null, false,
            null, null, null)

        then:
        thrown(DataLakeStorageException)

        cleanup:
        outFile.delete()
        file.delete()
    }

    def "Download file count null"() {
        setup:
        def file = getRandomFile(defaultDataSize)
        fc.uploadFromFile(file.toPath().toString(), true)
        def outFile = new File(testName + "")
        if (outFile.exists()) {
            assert outFile.delete()
        }

        when:
        fc.readToFileWithResponse(outFile.toPath().toString(), new FileRange(0), null, null, null, false, null, null, null)

        then:
        compareFiles(file, outFile, 0, defaultDataSize)

        cleanup:
        outFile.delete()
        file.delete()
    }

    @Unroll
    def "Download file AC"() {
        setup:
        def file = getRandomFile(defaultDataSize)
        fc.uploadFromFile(file.toPath().toString(), true)
        def outFile = new File(testName + "")
        if (outFile.exists()) {
            assert outFile.delete()
        }

        match = setupPathMatchCondition(fc, match)
        leaseID = setupPathLeaseCondition(fc, leaseID)
        DataLakeRequestConditions bro = new DataLakeRequestConditions().setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified).setIfMatch(match).setIfNoneMatch(noneMatch)
            .setLeaseId(leaseID)

        when:
        fc.readToFileWithResponse(outFile.toPath().toString(), null, null, null, bro, false, null, null, null)

        then:
        notThrown(DataLakeStorageException)

        cleanup:
        outFile.delete()
        file.delete()

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
    def "Download file AC fail"() {
        setup:
        def file = getRandomFile(defaultDataSize)
        fc.uploadFromFile(file.toPath().toString(), true)
        def outFile = new File(testName + "")
        if (outFile.exists()) {
            assert outFile.delete()
        }

        noneMatch = setupPathMatchCondition(fc, noneMatch)
        setupPathLeaseCondition(fc, leaseID)
        DataLakeRequestConditions bro = new DataLakeRequestConditions().setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified).setIfMatch(match).setIfNoneMatch(noneMatch)
            .setLeaseId(leaseID)

        when:
        fc.readToFileWithResponse(outFile.toPath().toString(), null, null, null, bro, false, null, null, null)

        then:
        def e = thrown(DataLakeStorageException)
        e.getErrorCode() == "ConditionNotMet" ||
            e.getErrorCode() == "LeaseIdMismatchWithBlobOperation"

        cleanup:
        outFile.delete()
        file.delete()

        where:
        modified | unmodified | match       | noneMatch    | leaseID
        newDate  | null       | null        | null         | null
        null     | oldDate    | null        | null         | null
        null     | null       | garbageEtag | null         | null
        null     | null       | null        | receivedEtag | null
        null     | null       | null        | null         | garbageLeaseID
    }

    @Requires({ liveMode() })
    def "Download file etag lock"() {
        setup:
        def file = getRandomFile(Constants.MB)
        fc.uploadFromFile(file.toPath().toString(), true)
        def outFile = new File(testName + "")
        Files.deleteIfExists(file.toPath())

        expect:
        def fac = new DataLakePathClientBuilder()
            .pipeline(fc.getHttpPipeline())
            .endpoint(fc.getPathUrl())
            .buildFileAsyncClient()

        /*
         * Setup the download to happen in small chunks so many requests need to be sent, this will give the upload time
         * to change the ETag therefore failing the download.
         */
        def options = new ParallelTransferOptions(Constants.KB, null, null, null)

        /*
         * This is done to prevent onErrorDropped exceptions from being logged at the error level. If no hook is
         * registered for onErrorDropped the error is logged at the ERROR level.
         *
         * onErrorDropped is triggered once the reactive stream has emitted one element, after that exceptions are
         * dropped.
         */
        Hooks.onErrorDropped({ ignored -> /* do nothing with it */ })

        /*
         * When the download begins trigger an upload to overwrite the downloading blob after waiting 500 milliseconds
         * so that the download is able to get an ETag before it is changed.
         */
        StepVerifier.create(fac.readToFileWithResponse(outFile.toPath().toString(), null, options, null, null, false, null)
            .doOnSubscribe({ fac.upload(defaultFlux, null, true).delaySubscription(Duration.ofMillis(500)).subscribe() }))
            .verifyErrorSatisfies({
                /*
                 * If an operation is running on multiple threads and multiple return an exception Reactor will combine
                 * them into a CompositeException which needs to be unwrapped. If there is only a single exception
                 * 'Exceptions.unwrapMultiple' will return a singleton list of the exception it was passed.
                 *
                 * These exceptions may be wrapped exceptions where the exception we are expecting is contained within
                 * ReactiveException that needs to be unwrapped. If the passed exception isn't a 'ReactiveException' it
                 * will be returned unmodified by 'Exceptions.unwrap'.
                 */
                assert Exceptions.unwrapMultiple(it).stream().anyMatch({ it2 ->
                    def exception = Exceptions.unwrap(it2)
                    if (exception instanceof DataLakeStorageException) {
                        assert ((DataLakeStorageException) exception).getStatusCode() == 412
                        return true
                    }
                })
            })

        // Give the file a chance to be deleted by the download operation before verifying its deletion
        sleep(500)
        !outFile.exists()

        cleanup:
        file.delete()
        outFile.delete()
    }

    @Requires({ liveMode() })
    @Unroll
    def "Download file progress receiver"() {
        def file = getRandomFile(fileSize)
        fc.uploadFromFile(file.toPath().toString(), true)
        def outFile = new File(testName + "")
        if (outFile.exists()) {
            assert outFile.delete()
        }

        def mockReceiver = Mock(ProgressReceiver)

        def numBlocks = fileSize / (4 * 1024 * 1024)
        def prevCount = 0

        when:
        fc.readToFileWithResponse(outFile.toPath().toString(), null,
            new ParallelTransferOptions(null, null, mockReceiver, null),
            new DownloadRetryOptions().setMaxRetryRequests(3), null, false, null, null, null)

        then:
        /*
         * Should receive at least one notification indicating completed progress, multiple notifications may be
         * received if there are empty buffers in the stream.
         */
        (1.._) * mockReceiver.reportProgress(fileSize)

        // There should be NO notification with a larger than expected size.
        0 * mockReceiver.reportProgress({ it > fileSize })

        /*
        We should receive at least one notification reporting an intermediary value per block, but possibly more
        notifications will be received depending on the implementation. We specify numBlocks - 1 because the last block
        will be the total size as above. Finally, we assert that the number reported monotonically increases.
         */
        (numBlocks - 1.._) * mockReceiver.reportProgress(!file.size()) >> { long bytesTransferred ->
            if (!(bytesTransferred >= prevCount)) {
                throw new IllegalArgumentException("Reported progress should monotonically increase")
            } else {
                prevCount = bytesTransferred
            }
        }

        // We should receive no notifications that report more progress than the size of the file.
        0 * mockReceiver.reportProgress({ it > fileSize })

        cleanup:
        file.delete()
        outFile.delete()

        where:
        fileSize             | _
        100                  | _
        8 * 1026 * 1024 + 10 | _
    }

    def "Rename min"() {
        expect:
        fc.renameWithResponse(null, generatePathName(), null, null, null, null).getStatusCode() == 201
    }

    def "Rename with response"() {
        when:
        def resp = fc.renameWithResponse(null, generatePathName(), null, null, null, null)

        def renamedClient = resp.getValue()
        renamedClient.getProperties()

        then:
        notThrown(DataLakeStorageException)

        when:
        fc.getProperties()

        then:
        thrown(DataLakeStorageException)
    }

    def "Rename filesystem with response"() {
        setup:
        def newFileSystem = primaryDataLakeServiceClient.createFileSystem(generateFileSystemName())

        when:
        def resp = fc.renameWithResponse(newFileSystem.getFileSystemName(), generatePathName(), null, null, null, null)

        def renamedClient = resp.getValue()
        renamedClient.getProperties()

        then:
        notThrown(DataLakeStorageException)

        when:
        fc.getProperties()

        then:
        thrown(DataLakeStorageException)
    }

    def "Rename error"() {
        setup:
        fc = fsc.getFileClient(generatePathName())

        when:
        fc.renameWithResponse(null, generatePathName(), null, null, null, null)

        then:
        thrown(DataLakeStorageException)
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
        fc.renameWithResponse(null, generatePathName(), drc, null, null, null).getStatusCode() == 201

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
        fc.renameWithResponse(null, generatePathName(), drc, null, null, null)

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
        fc.renameWithResponse(null, pathName, null, drc, null, null).getStatusCode() == 201

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
        fc.renameWithResponse(null, pathName, null, drc, null, null)

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

    def "Append data min"() {
        when:
        fc.append(new ByteArrayInputStream(defaultData.array()), 0, defaultDataSize)

        then:
        notThrown(DataLakeStorageException)
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
        thrown(DataLakeStorageException)
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
        def e = thrown(DataLakeStorageException)
        e.getResponse().getStatusCode() == 412
    }

    def "Append data error"() {
        setup:
        fc = fsc.getFileClient(generatePathName())

        when:
        fc.appendWithResponse(defaultInputStream.get(), 0, defaultDataSize, null, null, null, null)

        then:
        def e = thrown(DataLakeStorageException)
        e.getResponse().getStatusCode() == 404
    }

    def "Flush data min"() {
        when:
        fc.append(new ByteArrayInputStream(defaultData.array()), 0, defaultDataSize)
        fc.flush(defaultDataSize)

        then:
        notThrown(DataLakeStorageException)
    }

    def "Flush close"() {
        when:
        fc = fsc.getFileClient(generatePathName())
        fc.create()
        fc.append(defaultInputStream.get(), 0, defaultDataSize)
        fc.flushWithResponse(defaultDataSize, false, true, null, null, null, null)

        then:
        notThrown(DataLakeStorageException)
    }

    def "Flush retain uncommitted data "() {
        when:
        fc = fsc.getFileClient(generatePathName())
        fc.create()
        fc.append(defaultInputStream.get(), 0, defaultDataSize)
        fc.flushWithResponse(defaultDataSize, true, false, null, null, null, null)

        then:
        notThrown(DataLakeStorageException)
    }

    def "Flush IA"() {
        when:
        fc = fsc.getFileClient(generatePathName())
        fc.create()
        fc.append(defaultInputStream.get(), 0, defaultDataSize)
        fc.flushWithResponse(4, false, false, null, null, null, null)

        then:
        thrown(DataLakeStorageException)
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
        thrown(DataLakeStorageException)

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
        thrown(DataLakeStorageException)
    }

    def "Flush data overwrite"() {
        when:
        fc.append(new ByteArrayInputStream(defaultData.array()), 0, defaultDataSize)
        fc.flush(defaultDataSize)
        fc.append(new ByteArrayInputStream(defaultData.array()), 0, defaultDataSize)
        // Attempt to write data without overwrite enabled
        fc.flush(defaultDataSize, true)

        then:
        thrown(DataLakeStorageException)
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

    // "No overwrite interrupted" tests were not ported over for datalake. This is because the access condition check
    // occurs on the create method, so simple access conditions tests suffice.
    @Unroll
    @Requires({liveMode()}) // Test uploads large amount of data
    def "Upload from file"() {
        setup:
        DataLakeFileAsyncClient fac = fscAsync.getFileAsyncClient(generatePathName())
        def file = getRandomFile(fileSize)

        when:
        // Block length will be ignored for single shot.
        StepVerifier.create(fac.uploadFromFile(file.getPath(), new ParallelTransferOptions(blockSize, null,
            null, null), null, null, null))
            .verifyComplete()

        then:
        def outFile = new File(file.getPath().toString() + "result")
        outFile.createNewFile()

        def outStream = new FileOutputStream(outFile)
        outStream.write(FluxUtil.collectBytesInByteBufferStream(fac.read()).block())
        outStream.close()

        compareFiles(file, outFile, 0, fileSize)

        cleanup:
        outFile.delete()
        file.delete()

        where:
        fileSize                                       | blockSize       || commitedBlockCount
        10                                             | null            || 0  // Size is too small to trigger block uploading
        10 * Constants.KB                              | null            || 0  // Size is too small to trigger block uploading
        50 * Constants.MB                              | null            || 0  // Size is too small to trigger block uploading
        DataLakeFileAsyncClient.MAX_APPEND_FILE_BYTES + 1 | null            || Math.ceil((DataLakeFileAsyncClient.MAX_APPEND_FILE_BYTES + 1) / BlobAsyncClient.BLOB_DEFAULT_HTBB_UPLOAD_BLOCK_SIZE) // HTBB optimizations should trigger when file size is >100MB and defaults are used.
        101 * Constants.MB                             | 4 * 1024 * 1024 || 0  // Size is too small to trigger block uploading
    }

    def "Upload from file with metadata"() {
        given:
        def metadata = Collections.singletonMap("metadata", "value")
        def file = getRandomFile(Constants.KB)
        def outStream = new ByteArrayOutputStream()

        when:
        fc.uploadFromFile(file.getPath(), null, null, metadata, null, null)

        then:
        metadata == fc.getProperties().getMetadata()
        fc.read(outStream)
        outStream.toByteArray() == Files.readAllBytes(file.toPath())

        cleanup:
        file.delete()
    }

    def "Upload from file default no overwrite"() {
        setup:
        DataLakeFileAsyncClient fac = fscAsync.getFileAsyncClient(generatePathName())
        fac.create().block()
        when:
        def file = getRandomFile(50)
        fc.uploadFromFile(file.toPath().toString())

        then:
        thrown(DataLakeStorageException)

        and:
        def uploadVerifier = StepVerifier.create(fac.uploadFromFile(getRandomFile(50).toPath().toString()))

        then:
        uploadVerifier.verifyError(DataLakeStorageException)

        cleanup:
        file.delete()
    }

    def "Upload from file overwrite"() {
        setup:
        DataLakeFileAsyncClient fac = fscAsync.getFileAsyncClient(generatePathName())
        fac.create().block()

        when:
        def file = getRandomFile(50)
        fc.uploadFromFile(file.toPath().toString(), true)

        then:
        notThrown(BlobStorageException)

        and:
        def uploadVerifier = StepVerifier.create(fac.uploadFromFile(getRandomFile(50).toPath().toString(), true))

        then:
        uploadVerifier.verifyComplete()

        cleanup:
        file.delete()
    }

    /*
     * Reports the number of bytes sent when uploading a file. This is different than other reporters which track the
     * number of reportings as upload from file hooks into the loading data from disk data stream which is a hard-coded
     * read size.
     */
    class FileUploadReporter implements ProgressReceiver {
        private long reportedByteCount

        @Override
        void reportProgress(long bytesTransferred) {
            this.reportedByteCount += bytesTransferred
        }

        long getReportedByteCount() {
            return this.reportedByteCount
        }
    }

    @Unroll
    @Requires({ liveMode() })
    def "Upload from file reporter"() {
        setup:
        DataLakeFileAsyncClient fac = fscAsync.getFileAsyncClient(generatePathName())

        when:
        def uploadReporter = new FileUploadReporter()
        def file = getRandomFile(size)

        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions(blockSize, bufferCount,
            uploadReporter, blockSize - 1)

        then:
        StepVerifier.create(fac.uploadFromFile(file.toPath().toString(), parallelTransferOptions,
            null, null, null))
            .verifyComplete()

        // Check if the reported size is equal to or greater than the file size in case there are retries.
        uploadReporter.getReportedByteCount() >= size

        cleanup:
        file.delete()

        where:
        size              | blockSize         | bufferCount
        10 * Constants.MB | 10 * Constants.MB | 8
        20 * Constants.MB | 1 * Constants.MB  | 5
        10 * Constants.MB | 5 * Constants.MB  | 2
        10 * Constants.MB | 10 * Constants.KB | 100
    }

    @Unroll
    def "Upload from file options"() {
        setup:
        def file = getRandomFile(dataSize)

        when:
        fc.uploadFromFile(file.toPath().toString(),
            new ParallelTransferOptions(blockSize, null, null, singleUploadSize), null, null, null, null)

        then:
        fc.getProperties().getFileSize() == dataSize


        cleanup:
        file.delete()

        where:
        dataSize                                       | singleUploadSize | blockSize || expectedBlockCount
        DataLakeFileAsyncClient.MAX_APPEND_FILE_BYTES - 1 | null             | null      || 0 // Test that the default for singleUploadSize is the maximum
        DataLakeFileAsyncClient.MAX_APPEND_FILE_BYTES + 1 | null             | null      || Math.ceil(((double) DataLakeFileAsyncClient.MAX_APPEND_FILE_BYTES + 1) / (double) BlobClient.BLOB_DEFAULT_HTBB_UPLOAD_BLOCK_SIZE) // "". This also validates the default for blockSize
        100                                            | 50               | null      || 1 // Test that singleUploadSize is respected
        100                                            | 50               | 20        || 5 // Test that blockSize is respected
    }

    def "Async buffered upload empty"() {
        setup:
        DataLakeFileAsyncClient fac = fscAsync.getFileAsyncClient(generatePathName())

        expect:
        StepVerifier.create(fac.upload(Flux.just(ByteBuffer.wrap(new byte[0])), null))
            .verifyErrorSatisfies({it instanceof DataLakeStorageException})
    }

    @Unroll
    def "Async buffered upload empty buffers"() {
        setup:
        DataLakeFileAsyncClient fac = fscAsync.getFileAsyncClient(generatePathName())

        expect:
        StepVerifier.create(fac.upload(Flux.fromIterable([buffer1, buffer2, buffer3]), null, true))
            .assertNext({ assert it.getETag() != null })
            .verifyComplete()

        StepVerifier.create(FluxUtil.collectBytesInByteBufferStream(fac.read()))
            .assertNext({ assert it == expectedDownload })
            .verifyComplete()

        where:
        buffer1                                                   | buffer2                                               | buffer3                                                    || expectedDownload
        ByteBuffer.wrap("Hello".getBytes(StandardCharsets.UTF_8)) | ByteBuffer.wrap(" ".getBytes(StandardCharsets.UTF_8)) | ByteBuffer.wrap("world!".getBytes(StandardCharsets.UTF_8)) || "Hello world!".getBytes(StandardCharsets.UTF_8)
        ByteBuffer.wrap("Hello".getBytes(StandardCharsets.UTF_8)) | ByteBuffer.wrap(" ".getBytes(StandardCharsets.UTF_8)) | ByteBuffer.wrap(new byte[0])                               || "Hello ".getBytes(StandardCharsets.UTF_8)
        ByteBuffer.wrap("Hello".getBytes(StandardCharsets.UTF_8)) | ByteBuffer.wrap(new byte[0])                          | ByteBuffer.wrap("world!".getBytes(StandardCharsets.UTF_8)) || "Helloworld!".getBytes(StandardCharsets.UTF_8)
        ByteBuffer.wrap(new byte[0])                              | ByteBuffer.wrap(" ".getBytes(StandardCharsets.UTF_8)) | ByteBuffer.wrap("world!".getBytes(StandardCharsets.UTF_8)) || " world!".getBytes(StandardCharsets.UTF_8)
    }

    @Unroll
    @Requires({ liveMode() }) // Test uploads large amount of data
    def "Async buffered upload"() {
        setup:
        DataLakeFileAsyncClient fac = fscAsync.getFileAsyncClient(generatePathName())
        fac.create().block()

        when:
        def data = getRandomData(dataSize)
        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions(bufferSize, numBuffs, null, 4 * Constants.MB)
        fac.upload(Flux.just(data), parallelTransferOptions, true).block()
        data.position(0)

        then:
        // Due to memory issues, this check only runs on small to medium sized data sets.
        if (dataSize < 100 * 1024 * 1024) {
            StepVerifier.create(collectBytesInBuffer(fac.read()))
                .assertNext({ assert it == data })
                .verifyComplete()
        }

        where:
        dataSize           | bufferSize        | numBuffs || blockCount
        35 * Constants.MB  | 5 * Constants.MB  | 2        || 7 // Requires cycling through the same buffers multiple times.
        35 * Constants.MB  | 5 * Constants.MB  | 5        || 7 // Most buffers may only be used once.
        100 * Constants.MB | 10 * Constants.MB | 2        || 10 // Larger data set.
        100 * Constants.MB | 10 * Constants.MB | 5        || 10 // Larger number of Buffs.
        10 * Constants.MB  | 1 * Constants.MB  | 10       || 10 // Exactly enough buffer space to hold all the data.
        50 * Constants.MB  | 10 * Constants.MB | 2        || 5 // Larger data.
        10 * Constants.MB  | 2 * Constants.MB  | 4        || 5
        10 * Constants.MB  | 3 * Constants.MB  | 3        || 4 // Data does not squarely fit in buffers.
    }

    def compareListToBuffer(List<ByteBuffer> buffers, ByteBuffer result) {
        result.position(0)
        for (ByteBuffer buffer : buffers) {
            buffer.position(0)
            result.limit(result.position() + buffer.remaining())
            if (buffer != result) {
                return false
            }
            result.position(result.position() + buffer.remaining())
        }
        return result.remaining() == 0
    }

    /*      Reporter for testing Progress Receiver
    *        Will count the number of reports that are triggered         */

    class Reporter implements ProgressReceiver {
        private final long blockSize
        private long reportingCount

        Reporter(long blockSize) {
            this.blockSize = blockSize
        }

        @Override
        void reportProgress(long bytesTransferred) {
            assert bytesTransferred % blockSize == 0
            this.reportingCount += 1
        }

        long getReportingCount() {
            return this.reportingCount
        }
    }

    @Unroll
    @Requires({ liveMode() })
    def "Buffered upload with reporter"() {
        setup:
        DataLakeFileAsyncClient fac = fscAsync.getFileAsyncClient(generatePathName())

        when:
        def uploadReporter = new Reporter(blockSize)

        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions(blockSize, bufferCount,
            uploadReporter, 4 * Constants.MB)

        then:
        StepVerifier.create(fac.uploadWithResponse(Flux.just(getRandomData(size)),
            parallelTransferOptions, null, null, null))
            .assertNext({
                assert it.getStatusCode() == 200

                /*
                 * Verify that the reporting count is equal or greater than the size divided by block size in the case
                 * that operations need to be retried. Retry attempts will increment the reporting count.
                 */
                assert uploadReporter.getReportingCount() >= (long) (size / blockSize)
            }).verifyComplete()

        where:
        size              | blockSize          | bufferCount
        10 * Constants.MB | 10 * Constants.MB  | 8
        20 * Constants.MB | 1 * Constants.MB   | 5
        10 * Constants.MB | 5 * Constants.MB   | 2
        10 * Constants.MB | 512 * Constants.KB | 20
    }

    @Unroll
    @Requires({liveMode()}) // Test uploads large amount of data
    def "Buffered upload chunked source"() {
        setup:
        DataLakeFileAsyncClient fac = fscAsync.getFileAsyncClient(generatePathName())
        /*
        This test should validate that the upload should work regardless of what format the passed data is in because
        it will be chunked appropriately.
         */
        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions(bufferSize * Constants.MB, numBuffers, null, 4 * Constants.MB)
        def dataList = [] as List<ByteBuffer>

        for (def size : dataSizeList) {
            dataList.add(getRandomData(size * Constants.MB))
        }
        def uploadOperation = fac.upload(Flux.fromIterable(dataList), parallelTransferOptions, true)

        expect:
        StepVerifier.create(uploadOperation.then(collectBytesInBuffer(fac.read())))
            .assertNext({ assert compareListToBuffer(dataList, it) })
            .verifyComplete()

        where:
        dataSizeList          | bufferSize | numBuffers || blockCount
        [7, 7]                | 10         | 2          || 2 // First item fits entirely in the buffer, next item spans two buffers
        [3, 3, 3, 3, 3, 3, 3] | 10         | 2          || 3 // Multiple items fit non-exactly in one buffer.
        [10, 10]              | 10         | 2          || 2 // Data fits exactly and does not need chunking.
        [50, 51, 49]          | 10         | 2          || 15 // Data needs chunking and does not fit neatly in buffers. Requires waiting for buffers to be released.
        // The case of one large buffer needing to be broken up is tested in the previous test.
    }

    // These two tests are to test optimizations in buffered upload for small files.
    @Unroll
    def "Buffered upload handle pathing"() {
        setup:
        DataLakeFileAsyncClient fac = fscAsync.getFileAsyncClient(generatePathName())
        def dataList = [] as List<ByteBuffer>
        for (def size : dataSizeList) {
            dataList.add(getRandomData(size))
        }

        def uploadOperation = fac.upload(Flux.fromIterable(dataList), new ParallelTransferOptions(null, null, null, 4 * Constants.MB), true)

        expect:
        StepVerifier.create(uploadOperation.then(collectBytesInBuffer(fac.read())))
            .assertNext({ assert compareListToBuffer(dataList, it) })
            .verifyComplete()

        where:
        dataSizeList                         | blockCount
        [4 * Constants.MB + 1, 10]           | 2
        [4 * Constants.MB]                   | 0
        [10, 100, 1000, 10000]               | 0
        [4 * Constants.MB, 4 * Constants.MB] | 2
    }

    @Unroll
    def "Buffered upload handle pathing hot flux"() {
        setup:
        DataLakeFileAsyncClient fac = fscAsync.getFileAsyncClient(generatePathName())
        def dataList = [] as List<ByteBuffer>
        for (def size : dataSizeList) {
            dataList.add(getRandomData(size))
        }
        def uploadOperation = fac.upload(Flux.fromIterable(dataList).publish().autoConnect(), new ParallelTransferOptions(null, null, null, 4 * Constants.MB), true)

        expect:
        StepVerifier.create(uploadOperation.then(collectBytesInBuffer(fac.read())))
            .assertNext({ assert compareListToBuffer(dataList, it) })
            .verifyComplete()

        where:
        dataSizeList                         | blockCount
        [4 * Constants.MB + 1, 10]           | 2
        [4 * Constants.MB]                   | 0
        [10, 100, 1000, 10000]               | 0
        [4 * Constants.MB, 4 * Constants.MB] | 2
    }

    def "Buffered upload illegal arguments null"() {
        setup:
        DataLakeFileAsyncClient fac = fscAsync.getFileAsyncClient(generatePathName())
        fac.create().block()
        expect:
        StepVerifier.create(fac.upload(null, new ParallelTransferOptions(4, 4, null, null), true))
            .verifyErrorSatisfies({ assert it instanceof NullPointerException })
    }

    @Unroll
    def "Buffered upload headers"() {
        setup:
        DataLakeFileAsyncClient fac = fscAsync.getFileAsyncClient(generatePathName())

        when:
        def data = getRandomByteArray(dataSize)
        def contentMD5 = validateContentMD5 ? MessageDigest.getInstance("MD5").digest(data) : null
        def uploadOperation = fac.uploadWithResponse(Flux.just(ByteBuffer.wrap(data)), new ParallelTransferOptions(null, null, null, 4 * Constants.MB), new PathHttpHeaders()
            .setCacheControl(cacheControl)
            .setContentDisposition(contentDisposition)
            .setContentEncoding(contentEncoding)
            .setContentLanguage(contentLanguage)
            .setContentMd5(contentMD5)
            .setContentType(contentType),
            null, null)

        then:
        StepVerifier.create(uploadOperation.then(fac.getPropertiesWithResponse(null)))
            .assertNext({
                assert validatePathProperties(it, cacheControl, contentDisposition, contentEncoding, contentLanguage,
                    contentMD5, contentType == null ? "application/octet-stream" : contentType)
            }).verifyComplete()
        // HTTP default content type is application/octet-stream.

        where:
        // Depending on the size of the stream either a single append will be called or multiple.
        dataSize         | cacheControl | contentDisposition | contentEncoding | contentLanguage | validateContentMD5 | contentType
        defaultDataSize  | null         | null               | null            | null            | true               | null
        defaultDataSize  | "control"    | "disposition"      | "encoding"      | "language"      | true               | "type"
        6 * Constants.MB | null         | null               | null            | null            | false              | null
        6 * Constants.MB | "control"    | "disposition"      | "encoding"      | "language"      | true               | "type"
    }

    @Unroll
    def "Buffered upload metadata"() {
        setup:
        DataLakeFileAsyncClient fac = fscAsync.getFileAsyncClient(generatePathName())
        def metadata = [:] as Map<String, String>
        if (key1 != null) {
            metadata.put(key1, value1)
        }
        if (key2 != null) {
            metadata.put(key2, value2)
        }

        when:
        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions(10, 10, null, null)
        def uploadOperation = fac.uploadWithResponse(Flux.just(getRandomData(10)),
            parallelTransferOptions, null, metadata, null)

        then:
        StepVerifier.create(uploadOperation.then(fac.getPropertiesWithResponse(null)))
            .assertNext({
                assert it.getStatusCode() == 200
                assert it.getValue().getMetadata() == metadata
            }).verifyComplete()

        where:
        key1  | value1 | key2   | value2
        null  | null   | null   | null
        "foo" | "bar"  | "fizz" | "buzz"
    }

    @Unroll
    def "Buffered upload options"() {
        setup:
        DataLakeFileAsyncClient fac = fscAsync.getFileAsyncClient(generatePathName())
        def data = getRandomData(dataSize)

        when:
        fac.uploadWithResponse(Flux.just(data),
            new ParallelTransferOptions(blockSize, null, null, singleUploadSize), null, null, null).block()

        then:
        fac.getProperties().block().getFileSize() == dataSize

        where:
        dataSize                                          | singleUploadSize | blockSize || _
        DataLakeFileAsyncClient.MAX_APPEND_FILE_BYTES - 1 | null             | null      || _
        DataLakeFileAsyncClient.MAX_APPEND_FILE_BYTES + 1 | null             | null      || _
        100                                               | 50               | null      || _
        100                                               | 50               | 20        || _
    }

    @Unroll
    def "Buffered upload AC"() {
        setup:
        DataLakeFileAsyncClient fac = fscAsync.getFileAsyncClient(generatePathName())
        fac.create().block()

        match = setupPathMatchCondition(fac, match)
        leaseID = setupPathLeaseCondition(fac, leaseID)
        def requestConditions = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

        expect:
        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions(10, null, null, null)
        StepVerifier.create(fac.uploadWithResponse(Flux.just(getRandomData(10)),
            parallelTransferOptions, null, null, requestConditions))
            .assertNext({ assert it.getStatusCode() == 200 })
            .verifyComplete()

        where:
        modified | unmodified | match        | noneMatch   | leaseID
        null     | null       | null         | null        | null
        null     | newDate    | null         | null        | null
        oldDate  | null       | null         | null        | null
        null     | null       | receivedEtag | null        | null
        null     | null       | null         | garbageEtag | null
        null     | null       | null         | null        | receivedLeaseID
    }

    @Unroll
    def "Buffered upload AC fail"() {
        setup:
        DataLakeFileAsyncClient fac = fscAsync.getFileAsyncClient(generatePathName())
        fac.create().block()
        noneMatch = setupPathMatchCondition(fac, noneMatch)
        leaseID = setupPathLeaseCondition(fac, leaseID)
        def requestConditions = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
        def parallelTransferOptions = new ParallelTransferOptions(10, null, null, null)

        expect:
        StepVerifier.create(fac.uploadWithResponse(Flux.just(getRandomData(10)),
            parallelTransferOptions, null, null, requestConditions))
            .verifyErrorSatisfies({
                assert it instanceof DataLakeStorageException
                def storageException = (DataLakeStorageException) it
                assert storageException.getStatusCode() == 412
            })

        where:
        modified | unmodified | match       | noneMatch    | leaseID
        newDate  | null       | null        | null         | null
        null     | oldDate    | null        | null         | null
        null     | null       | garbageEtag | null         | null
        null     | null       | null        | receivedEtag | null
        null     | null       | null        | null         | garbageLeaseID
    }

    // UploadBufferPool used to lock when the number of failed stageblocks exceeded the maximum number of buffers
    // (discovered when a leaseId was invalid)
    @Unroll
    def "UploadBufferPool lock three or more buffers"() {
        setup:
        DataLakeFileAsyncClient fac = fscAsync.getFileAsyncClient(generatePathName())
        fac.create().block()
        def leaseID = setupPathLeaseCondition(fac, garbageLeaseID)
        def requestConditions = new DataLakeRequestConditions().setLeaseId(leaseID)

        when:
        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions(blockSize as int,
            numBuffers as int, null, null)

        then:
        StepVerifier.create(fac.uploadWithResponse(Flux.just(getRandomData(10)),
            parallelTransferOptions, null, null, requestConditions))
            .verifyErrorSatisfies({ assert it instanceof DataLakeStorageException })

        where:
        dataLength | blockSize | numBuffers
        16         | 7         | 2
        16         | 5         | 2
    }

//    /*def "Upload NRF progress"() {
//        setup:
//        def data = getRandomData(BlockBlobURL.MAX_UPLOAD_BLOB_BYTES + 1)
//        def numBlocks = data.remaining() / BlockBlobURL.MAX_STAGE_BLOCK_BYTES
//        long prevCount = 0
//        def mockReceiver = Mock(IProgressReceiver)
//
//
//        when:
//        TransferManager.uploadFromNonReplayableFlowable(Flowable.just(data), bu, BlockBlobURL.MAX_STAGE_BLOCK_BYTES, 10,
//            new TransferManagerUploadToBlockBlobOptions(mockReceiver, null, null, null, 20)).blockingGet()
//        data.position(0)
//
//        then:
//        // We should receive exactly one notification of the completed progress.
//        1 * mockReceiver.reportProgress(data.remaining()) */
//
//    /*
//    We should receive at least one notification reporting an intermediary value per block, but possibly more
//    notifications will be received depending on the implementation. We specify numBlocks - 1 because the last block
//    will be the total size as above. Finally, we assert that the number reported monotonically increases.
//     */
//    /*(numBlocks - 1.._) * mockReceiver.reportProgress(!data.remaining()) >> { long bytesTransferred ->
//        if (!(bytesTransferred > prevCount)) {
//            throw new IllegalArgumentException("Reported progress should monotonically increase")
//        } else {
//            prevCount = bytesTransferred
//        }
//    }
//
//    // We should receive no notifications that report more progress than the size of the file.
//    0 * mockReceiver.reportProgress({ it > data.remaining() })
//    notThrown(IllegalArgumentException)
//}*/
//

//    def "Buffered upload network error"() {
//        setup:
//        DataLakeFileAsyncClient fac = fscAsync.getFileAsyncClient(generatePathName())
//
//        /*
//         This test uses a Flowable that does not allow multiple subscriptions and therefore ensures that we are
//         buffering properly to allow for retries even given this source behavior.
//         */
//        fac.upload(Flux.just(defaultData), defaultDataSize, null, true).block()
//
//        // Mock a response that will always be retried.
//        def mockHttpResponse = getStubResponse(500, new HttpRequest(HttpMethod.PUT, new URL("https://www.fake.com")))
//
//        // Mock a policy that will always then check that the data is still the same and return a retryable error.
//        def mockPolicy = { HttpPipelineCallContext context, HttpPipelineNextPolicy next ->
//            return context.getHttpRequest().getBody() == null ? next.process() :
//                collectBytesInBuffer(context.getHttpRequest().getBody())
//                    .map({ it == defaultData })
//                    .flatMap({ it ? Mono.just(mockHttpResponse) : Mono.error(new IllegalArgumentException()) })
//            }
//
//        // Build the pipeline
//        def fileAsyncClient = new DataLakeServiceClientBuilder()
//            .credential(primaryCredential)
//            .endpoint(String.format(defaultEndpointTemplate, primaryCredential.getAccountName()))
//            .httpClient(getHttpClient())
//            .retryOptions(new RequestRetryOptions(null, 3, null, 500, 1500, null))
//            .addPolicy(mockPolicy).buildAsyncClient()
//            .getFileSystemAsyncClient(fac.getFileSystemName())
//            .getFileAsyncClient(generatePathName())
//
//        when:
//        // Try to upload the flowable, which will hit a retry. A normal upload would throw, but buffering prevents that.
//        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions(1024, 4, null, null)
//        // TODO: It could be that duplicates aren't getting made in the retry policy? Or before the retry policy?
//
//        then:
//        // A second subscription to a download stream will
//        StepVerifier.create(fileAsyncClient.upload(fac.read(), defaultDataSize, parallelTransferOptions))
//            .verifyErrorSatisfies({
//                assert it instanceof DataLakeStorageException
//                assert it.getStatusCode() == 500
//            })
//    }

    def "Buffered upload default no overwrite"() {
        setup:
        DataLakeFileAsyncClient fac = fscAsync.getFileAsyncClient(generatePathName())
        fac.upload(defaultFlux, null).block()

        expect:
        StepVerifier.create(fac.upload(defaultFlux, null))
            .verifyError(IllegalArgumentException)
    }

    def "Buffered upload overwrite"() {
        setup:
        DataLakeFileAsyncClient fac = fscAsync.getFileAsyncClient(generatePathName())

        when:
        def file = getRandomFile(50)
        fc.uploadFromFile(file.toPath().toString(), true)

        then:
        notThrown(BlobStorageException)

        and:
        def uploadVerifier = StepVerifier.create(fac.uploadFromFile(getRandomFile(50).toPath().toString(), true))

        then:
        uploadVerifier.verifyComplete()

        cleanup:
        file.delete()
    }

}
