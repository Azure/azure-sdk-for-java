package com.azure.storage.file.datalake

import com.azure.core.http.rest.Response
import com.azure.core.util.Context
import com.azure.identity.DefaultAzureCredentialBuilder
import com.azure.storage.blob.BlobUrlParts
import com.azure.storage.blob.models.BlobErrorCode
import com.azure.storage.blob.models.BlobStorageException

import com.azure.storage.file.datalake.implementation.models.StorageErrorException
import com.azure.storage.file.datalake.models.*
import spock.lang.Unroll

class DirectoryAPITest extends APISpec {
    DataLakeDirectoryClient dc
    String directoryName

    PathPermissions permissions = new PathPermissions()
        .setOwner(new RolePermissions().setReadPermission(true).setWritePermission(true).setExecutePermission(true))
        .setGroup(new RolePermissions().setReadPermission(true).setExecutePermission(true))
        .setOther(new RolePermissions().setReadPermission(true))

    List<PathAccessControlEntry> pathAccessControlEntries = PathAccessControlEntry.parseList("user::rwx,group::r--,other::---,mask::rwx")

    String group = null
    String owner = null

    def setup() {
        directoryName = generatePathName()
        dc = fsc.getDirectoryClient(directoryName)
        dc.create()
    }

    def "Create min"() {
        when:
        dc = fsc.getDirectoryClient(generatePathName())
        dc.create()

        then:
        notThrown(StorageErrorException)
    }

    def "Create defaults"() {
        setup:
        dc = fsc.getDirectoryClient(generatePathName())

        when:
        def createResponse = dc.createWithResponse(null, null, null, null, null, null, null)

        then:
        createResponse.getStatusCode() == 201
        validateBasicHeaders(createResponse.getHeaders())
    }

    def "Create error"() {
        setup:
        dc = fsc.getDirectoryClient(generatePathName())

        when:
        dc.createWithResponse(null, null, null, null, new DataLakeRequestConditions().setIfMatch("garbage"), null,
            Context.NONE)

        then:
        thrown(Exception)
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
        dc = fsc.getDirectoryClient(generatePathName())

        when:
        dc.createWithResponse(null, null, headers, null, null, null, null)
        def response = dc.getPropertiesWithResponse(null, null, null)

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
        dc.createWithResponse(null, null, null, metadata, null, null, Context.NONE)
        def response = dc.getProperties()

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
    def "Create AC"() {
        setup:
        match = setupPathMatchCondition(dc, match)
        leaseID = setupPathLeaseCondition(dc, leaseID)
        def drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

        expect:
        dc.createWithResponse(null, null, null, null, drc, null, null).getStatusCode() == 201

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
        noneMatch = setupPathMatchCondition(dc, noneMatch)
        setupPathLeaseCondition(dc, leaseID)
        def drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

        when:
        dc.createWithResponse(null, null, null, null, drc, null, Context.NONE)

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

    def "Create permissions and umask"() {
        setup:
        def permissions = "0777"
        def umask = "0057"

        expect:
        dc.createWithResponse(permissions, umask, null, null, null, null, Context.NONE).getStatusCode() == 201
    }

    def "Delete min"() {
        expect:
        dc.deleteWithResponse(false, null, null, null).getStatusCode() == 200
    }

    def "Delete recursive"() {
        expect:
        dc.deleteWithResponse(true, null, null, null).getStatusCode() == 200
    }

    def "Delete dir does not exist anymore"() {
        when:
        dc.deleteWithResponse(false, null, null, null)
        dc.getPropertiesWithResponse(null, null, null)

        then:
        def e = thrown(BlobStorageException)
        e.getResponse().getStatusCode() == 404
        e.getErrorCode() == BlobErrorCode.BLOB_NOT_FOUND
//        e.getServiceMessage().contains("The specified blob does not exist.")
    }

    @Unroll
    def "Delete AC"() {
        setup:
        match = setupPathMatchCondition(dc, match)
        leaseID = setupPathLeaseCondition(dc, leaseID)
        def drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

        expect:
        dc.deleteWithResponse(false, drc, null, null).getStatusCode() == 200

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
        noneMatch = setupPathMatchCondition(dc, noneMatch)
        setupPathLeaseCondition(dc, leaseID)
        def drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

        when:
        dc.deleteWithResponse(false, drc, null, null).getStatusCode()

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
        def resp = dc.setPermissions(permissions, group, owner)

        then:
        notThrown(StorageErrorException)
        resp.getETag()
        resp.getLastModified()
    }

    def "Set permissions with response"() {
        expect:
        dc.setPermissionsWithResponse(permissions, group, owner, null, null, Context.NONE).getStatusCode() == 200
    }

    @Unroll
    def "Set permissions AC"() {
        setup:
        match = setupPathMatchCondition(dc, match)
        leaseID = setupPathLeaseCondition(dc, leaseID)
        def drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

        expect:
        dc.setPermissionsWithResponse(permissions, group, owner, drc, null, Context.NONE).getStatusCode() == 200

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
        noneMatch = setupPathMatchCondition(dc, noneMatch)
        setupPathLeaseCondition(dc, leaseID)
        def drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

        when:
        dc.setPermissionsWithResponse(permissions, group, owner, drc, null, Context.NONE).getStatusCode() == 200

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
        dc = fsc.getDirectoryClient(generatePathName())

        when:
        dc.setPermissionsWithResponse(permissions, group, owner, null, null, null)

        then:
        thrown(StorageErrorException)
    }

    def "Set ACL min"() {
        when:
        def resp = dc.setAccessControlList(pathAccessControlEntries, group, owner)

        then:
        notThrown(StorageErrorException)
        resp.getETag()
        resp.getLastModified()
    }

    def "Set ACL with response"() {
        expect:
        dc.setAccessControlListWithResponse(pathAccessControlEntries, group, owner, null, null, Context.NONE).getStatusCode() == 200
    }

    @Unroll
    def "Set ACL AC"() {
        setup:
        match = setupPathMatchCondition(dc, match)
        leaseID = setupPathLeaseCondition(dc, leaseID)
        def drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

        expect:
        dc.setAccessControlListWithResponse(pathAccessControlEntries, group, owner, drc, null, Context.NONE).getStatusCode() == 200

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
        noneMatch = setupPathMatchCondition(dc, noneMatch)
        setupPathLeaseCondition(dc, leaseID)
        def drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

        when:
        dc.setAccessControlListWithResponse(pathAccessControlEntries, group, owner, drc, null, Context.NONE).getStatusCode() == 200

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
        dc = fsc.getDirectoryClient(generatePathName())

        when:
        dc.setAccessControlList(pathAccessControlEntries, group, owner)

        then:
        thrown(StorageErrorException)
    }

    def "Get access control min"() {
        when:
        PathAccessControl pac = dc.getAccessControl()

        then:
        notThrown(StorageErrorException)
        pac.getAccessControlList()
        pac.getPermissions()
        pac.getOwner()
        pac.getGroup()
    }

    def "Get access control with response"() {
        expect:
        dc.getAccessControlWithResponse(false, null, null, null).getStatusCode() == 200
    }

    def "Get access control return upn"() {
        expect:
        dc.getAccessControlWithResponse(true, null, null, null).getStatusCode() == 200
    }

    @Unroll
    def "Get access control AC"() {
        setup:
        match = setupPathMatchCondition(dc, match)
        leaseID = setupPathLeaseCondition(dc, leaseID)
        def drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

        expect:
        dc.getAccessControlWithResponse(false, drc, null, null).getStatusCode() == 200

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
        noneMatch = setupPathMatchCondition(dc, noneMatch)
        setupPathLeaseCondition(dc, leaseID)
        def drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

        when:
        dc.getAccessControlWithResponse(false, drc, null, null).getStatusCode() == 200

        then:
        thrown(StorageErrorException)

        where:
        modified | unmodified | match       | noneMatch    | leaseID
        newDate  | null       | null        | null         | null
        null     | oldDate    | null        | null         | null
        null     | null       | garbageEtag | null         | null
        null     | null       | null        | receivedEtag | null
//        null     | null       | null        | null         | garbageLeaseID
        // Known issue - uncomment when resolved
    }

    def "Rename min"() {
        expect:
        dc.renameWithResponse(generatePathName(), null, null, null, null).getStatusCode() == 201
    }

    def "Rename with response"() {
        when:
        def resp = dc.renameWithResponse(generatePathName(), null, null, null, null)

        def renamedClient = resp.getValue()
        renamedClient.getProperties()

        then:
        notThrown(StorageErrorException)

        when:
        dc.getProperties()

        then:
        thrown(BlobStorageException)
    }

    def "Rename error"() {
        setup:
        dc = fsc.getDirectoryClient(generatePathName())

        when:
        dc.renameWithResponse(generatePathName(), null, null, null, null)

        then:
        thrown(StorageErrorException)
    }

    @Unroll
    def "Rename source AC"() {
        setup:
        match = setupPathMatchCondition(dc, match)
        leaseID = setupPathLeaseCondition(dc, leaseID)
        def drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

        expect:
        dc.renameWithResponse(generatePathName(), drc, null, null, null).getStatusCode() == 201

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
        noneMatch = setupPathMatchCondition(dc, noneMatch)
        setupPathLeaseCondition(dc, leaseID)
        def drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

        when:
        dc.renameWithResponse(generatePathName(), drc, null, null, null)

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
        def destDir = fsc.getDirectoryClient(pathName)
        destDir.create()
        match = setupPathMatchCondition(destDir, match)
        leaseID = setupPathLeaseCondition(destDir, leaseID)
        def drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

        expect:
        dc.renameWithResponse(pathName, null, drc, null, null).getStatusCode() == 201

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
        def destDir = fsc.getDirectoryClient(pathName)
        destDir.create()
        noneMatch = setupPathMatchCondition(destDir, noneMatch)
        setupPathLeaseCondition(destDir, leaseID)
        def drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

        when:
        dc.renameWithResponse(pathName, null, drc, null, null)

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

    def "Get properties default"() {
        when:
        def response = dc.getPropertiesWithResponse(null, null, null)
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
        properties.getMetadata()
        !properties.getAccessTierChangeTime()
        !properties.getEncryptionKeySha256()

    }

    def "Get properties min"() {
        expect:
        dc.getPropertiesWithResponse(null, null, null).getStatusCode() == 200
    }

    @Unroll
    def "Get properties AC"() {
        setup:
        def drc = new DataLakeRequestConditions()
            .setLeaseId(setupPathLeaseCondition(dc, leaseID))
            .setIfMatch(setupPathMatchCondition(dc, match))
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

        expect:
        dc.getPropertiesWithResponse(drc, null, null).getStatusCode() == 200

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
            .setLeaseId(setupPathLeaseCondition(dc, leaseID))
            .setIfMatch(match)
            .setIfNoneMatch(setupPathMatchCondition(dc, noneMatch))
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

        when:
        dc.getPropertiesWithResponse(drc, null, null)

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
        dc = fsc.getDirectoryClient(generatePathName())

        when:
        dc.getProperties()

        then:
        thrown(BlobStorageException)
    }

    def "Set HTTP headers null"() {
        setup:
        def response = dc.setHttpHeadersWithResponse(null, null, null, null)

        expect:
        response.getStatusCode() == 200
        validateBasicHeaders(response.getHeaders())
    }

    def "Set HTTP headers min"() {
        setup:
        def properties = dc.getProperties()
        def headers = new PathHttpHeaders()
            .setContentEncoding(properties.getContentEncoding())
            .setContentDisposition(properties.getContentDisposition())
            .setContentType("type")
            .setCacheControl(properties.getCacheControl())
            .setContentLanguage(properties.getContentLanguage())

        dc.setHttpHeaders(headers)

        expect:
        dc.getProperties().getContentType() == "type"
    }

    @Unroll
    def "Set HTTP headers headers"() {
        setup:
        def putHeaders = new PathHttpHeaders()
            .setCacheControl(cacheControl)
            .setContentDisposition(contentDisposition)
            .setContentEncoding(contentEncoding)
            .setContentLanguage(contentLanguage)
            .setContentMd5(contentMD5)
            .setContentType(contentType)

        dc.setHttpHeaders(putHeaders)

        expect:
        validatePathProperties(
            dc.getPropertiesWithResponse(null, null, null),
            cacheControl, contentDisposition, contentEncoding, contentLanguage, contentMD5, contentType)

        where:
        cacheControl | contentDisposition | contentEncoding | contentLanguage | contentMD5 | contentType
        null         | null               | null            | null            | null       | null
        "control"    | "disposition"      | "encoding"      | "language"      | null       | "type"
    }

    @Unroll
    def "Set HTTP headers AC"() {
        setup:
        match = setupPathMatchCondition(dc, match)
        leaseID = setupPathLeaseCondition(dc, leaseID)
        def drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

        expect:
        dc.setHttpHeadersWithResponse(null, drc, null, null).getStatusCode() == 200

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
        noneMatch = setupPathMatchCondition(dc, noneMatch)
        setupPathLeaseCondition(dc, leaseID)
        def drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

        when:
        dc.setHttpHeadersWithResponse(null, drc, null, null)

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
        dc = fsc.getDirectoryClient(generatePathName())

        when:
        dc.setHttpHeaders(null)

        then:
        thrown(BlobStorageException)
    }

    def "Set metadata all null"() {
        when:
        def response = dc.setMetadataWithResponse(null, null, null, null)

        then:
        // Directories have an is directory metadata param by default
        dc.getProperties().getMetadata().size() == 1
        response.getStatusCode() == 200
        validateBasicHeaders(response.getHeaders())
        Boolean.parseBoolean(response.getHeaders().getValue("x-ms-request-server-encrypted"))
    }

    def "Set metadata min"() {
        setup:
        def metadata = new HashMap<String, String>()
        metadata.put("foo", "bar")

        when:
        dc.setMetadata(metadata)

        then:
        // Directory adds a directory metadata value
        for(String k : metadata.keySet()) {
            dc.getProperties().getMetadata().containsKey(k)
            dc.getProperties().getMetadata().get(k) == metadata.get(k)
        }
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
        dc.setMetadataWithResponse(metadata, null, null, null).getStatusCode() == statusCode
        // Directory adds a directory metadata value
        for(String k : metadata.keySet()) {
            dc.getProperties().getMetadata().containsKey(k)
            dc.getProperties().getMetadata().get(k) == metadata.get(k)
        }

        where:
        key1  | value1 | key2   | value2 || statusCode
        null  | null   | null   | null   || 200
        "foo" | "bar"  | "fizz" | "buzz" || 200
    }

    @Unroll
    def "Set metadata AC"() {
        setup:
        match = setupPathMatchCondition(dc, match)
        leaseID = setupPathLeaseCondition(dc, leaseID)
        def drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

        expect:
        dc.setMetadataWithResponse(null, drc, null, null).getStatusCode() == 200

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
        noneMatch = setupPathMatchCondition(dc, noneMatch)
        setupPathLeaseCondition(dc, leaseID)

        def drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

        when:
        dc.setMetadataWithResponse(null, drc, null, null)

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
        dc = fsc.getDirectoryClient(generatePathName())

        when:
        dc.setMetadata(null)

        then:
        thrown(BlobStorageException)
    }

    def "Create file min"() {
        when:
        dc.getFileClient(generatePathName()).create()

        then:
        notThrown(StorageErrorException)
    }

    def "Create file defaults"() {
        when:
        def createResponse = dc.createFileWithResponse(generatePathName(), null, null, null, null, null, null, null)

        then:
        createResponse.getStatusCode() == 201
        validateBasicHeaders(createResponse.getHeaders())
    }

    def "Create file error"() {
        when:
        dc.createFileWithResponse(generatePathName(), null, null, null, null, new DataLakeRequestConditions().setIfMatch("garbage"), null,
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
        def client = dc.createFileWithResponse(generatePathName(), null, null, headers, null, null, null, null).getValue()
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
        def client = dc.createFileWithResponse(generatePathName(), null, null, null, metadata, null, null, null).getValue()
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
        def client = dc.getFileClient(pathName)
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
        dc.createFileWithResponse(pathName, null, null, null, null, drc, null, null).getStatusCode() == 201

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
        def client = dc.getFileClient(pathName)
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
        dc.createFileWithResponse(pathName, null, null, null, null, drc, null, Context.NONE)

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
        dc.createFileWithResponse(generatePathName(), permissions, umask, null, null, null, null, Context.NONE).getStatusCode() == 201
    }

    def "Delete file min"() {
        expect:
        def pathName = generatePathName()
        dc.createFile(pathName)
        dc.deleteFileWithResponse(pathName, null, null, null).getStatusCode() == 200
    }

    def "Delete file file does not exist anymore"() {
        when:
        def pathName = generatePathName()
        def client = dc.createFile(pathName)
        dc.deleteFileWithResponse(pathName, null, null, null)
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
        def client = dc.createFile(pathName)
        match = setupPathMatchCondition(client, match)
        leaseID = setupPathLeaseCondition(client, leaseID)
        def drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

        expect:
        dc.deleteFileWithResponse(pathName, drc, null, null).getStatusCode() == 200

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
        def client = dc.createFile(pathName)
        noneMatch = setupPathMatchCondition(client, noneMatch)
        setupPathLeaseCondition(client, leaseID)
        def drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

        when:
        dc.deleteFileWithResponse(pathName, drc, null, null).getStatusCode()

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

    def "Create sub dir min"() {
        when:
        def subdir = dc.getSubdirectoryClient(generatePathName())
        subdir.create()

        then:
        notThrown(StorageErrorException)
    }

    def "Create sub dir defaults"() {
        when:
        def createResponse = dc.createSubdirectoryWithResponse(generatePathName(), null, null, null, null, null, null, null)

        then:
        createResponse.getStatusCode() == 201
        validateBasicHeaders(createResponse.getHeaders())
    }

    def "Create sub dir error"() {
        when:
        dc.createSubdirectoryWithResponse(generatePathName(), null, null, null, null,
            new DataLakeRequestConditions().setIfMatch("garbage"), null,
            Context.NONE)

        then:
        thrown(Exception)
    }

    @Unroll
    def "Create sub dir headers"() {
        // Create does not set md5
        setup:
        def headers = new PathHttpHeaders().setCacheControl(cacheControl)
            .setContentDisposition(contentDisposition)
            .setContentEncoding(contentEncoding)
            .setContentLanguage(contentLanguage)
            .setContentType(contentType)

        when:
        def client = dc.createSubdirectoryWithResponse(generatePathName(), null, null, headers, null, null, null, null).getValue()
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
    def "Create sub dir metadata"() {
        setup:
        def metadata = new HashMap<String, String>()
        if (key1 != null) {
            metadata.put(key1, value1)
        }
        if (key2 != null) {
            metadata.put(key2, value2)
        }

        when:
        def client = dc.createSubdirectoryWithResponse(generatePathName(), null, null, null, metadata, null, null, null).getValue()
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
    def "Create sub dir AC"() {
        setup:
        def pathName = generatePathName()
        def client = dc.getSubdirectoryClient(pathName)
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
        dc.createSubdirectoryWithResponse(pathName, null, null, null, null, drc, null, null).getStatusCode() == 201

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
    def "Create sub dir AC fail"() {
        setup:
        def pathName = generatePathName()
        def client = dc.getSubdirectoryClient(pathName)
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
        dc.createSubdirectoryWithResponse(pathName, null, null, null, null, drc, null, Context.NONE)

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

    def "Create sub dir permissions and umask"() {
        setup:
        def permissions = "0777"
        def umask = "0057"

        expect:
        dc.createSubdirectoryWithResponse(generatePathName(), permissions, umask, null, null, null, null, Context.NONE).getStatusCode() == 201
    }

    def "Delete sub dir min"() {
        expect:
        def pathName = generatePathName()
        dc.createSubdirectory(pathName)
        dc.deleteSubdirectoryWithResponse(pathName, false, null, null, null).getStatusCode() == 200
    }

    def "Delete sub dir recursive"() {
        expect:
        def pathName = generatePathName()
        dc.createSubdirectory(pathName)
        dc.deleteSubdirectoryWithResponse(pathName, true, null, null, null).getStatusCode() == 200
    }

    def "Delete sub dir dir does not exist anymore"() {
        when:
        def pathName = generatePathName()
        def client = dc.createSubdirectory(pathName)
        dc.deleteSubdirectoryWithResponse(pathName, false, null, null, null)
        client.getPropertiesWithResponse(null, null, null)

        then:
        def e = thrown(BlobStorageException)
        e.getResponse().getStatusCode() == 404
        e.getErrorCode() == BlobErrorCode.BLOB_NOT_FOUND
//        e.getServiceMessage().contains("The specified blob does not exist.")
    }

    @Unroll
    def "Delete sub dir AC"() {
        setup:
        def pathName = generatePathName()
        def client = dc.createSubdirectory(pathName)
        match = setupPathMatchCondition(client, match)
        leaseID = setupPathLeaseCondition(client, leaseID)
        def drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

        expect:
        dc.deleteSubdirectoryWithResponse(pathName, false, drc, null, null).getStatusCode() == 200

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
    def "Delete sub dir AC fail"() {
        setup:
        def pathName = generatePathName()
        def client = dc.createSubdirectory(pathName)
        noneMatch = setupPathMatchCondition(client, noneMatch)
        setupPathLeaseCondition(client, leaseID)
        def drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

        when:
        dc.deleteSubdirectoryWithResponse(pathName, false, drc, null, null).getStatusCode()

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
    def "Get Directory Name and Build Client"() {
        when:
        DataLakeDirectoryClient client = fsc.getDirectoryClient(originalDirectoryName)

        then:
        // Note : Here I use Path because there is a test that tests the use of a /
        client.getDirectoryPath() == finalDirectoryName

        where:
        originalDirectoryName  || finalDirectoryName
        "dir"                  || "dir"
        "path/to]a dir"        || "path/to]a dir"
        "path%2Fto%5Da%20dir"  || "path/to]a dir"
        "斑點"                  || "斑點"
        "%E6%96%91%E9%BB%9E"   || "斑點"
    }

    @Unroll
    def "Create Delete sub directory url encoding"() {
        setup:
        def dirName = generatePathName()
        DataLakeDirectoryClient client = fsc.getDirectoryClient(dirName)

        when:
        Response<DataLakeDirectoryClient> resp = client.createSubdirectoryWithResponse(originalDirectoryName, null, null, null, null, null, null, null)

        then:
        resp.getStatusCode() == 201
        resp.getValue().getDirectoryPath() == dirName + "/" + finalDirectoryName

        expect:
        client.deleteSubdirectoryWithResponse(originalDirectoryName, false, null, null, null).getStatusCode() == 200

        where:
        originalDirectoryName  || finalDirectoryName
        "dir"                  || "dir"
        "path/to]a dir"        || "path/to]a dir"
        "path%2Fto%5Da%20dir"  || "path/to]a dir"
        "斑點"                  || "斑點"
        "%E6%96%91%E9%BB%9E"   || "斑點"
    }

    @Unroll
    def "Create Delete file url encoding"() {
        setup:
        def fileName = generatePathName()
        DataLakeDirectoryClient client = fsc.getDirectoryClient(fileName)

        when:
        Response<DataLakeFileClient> resp = client.createFileWithResponse(originalFileName, null, null, null, null, null, null, null)

        then:
        resp.getStatusCode() == 201
        resp.getValue().getFilePath() == fileName + "/" + finalFileName

        expect:
        client.deleteSubdirectoryWithResponse(originalFileName, false, null, null, null).getStatusCode() == 200

        where:
        originalFileName       || finalFileName
        "file"                 || "file"
        "path/to]a file"       || "path/to]a file"
        "path%2Fto%5Da%20file" || "path/to]a file"
        "斑點"                  || "斑點"
        "%E6%96%91%E9%BB%9E"   || "斑點"
    }

    @Unroll
    def "Create file with path structure"() {
        when:
        DataLakeFileClient fileClient = fsc.getFileClient(pathName as String)
        fileClient.create()
        // Check that service created underlying directory
        DataLakeDirectoryClient dirClient = fsc.getDirectoryClient("dir")

        then:
        dirClient.getPropertiesWithResponse(null, null, null).getStatusCode() == 200

        when:
        // Delete file
        fileClient.deleteWithResponse(null, null, null).getStatusCode() == 200

        then:
        // Directory should still exist
        dirClient.getPropertiesWithResponse(null, null, null).getStatusCode() == 200

        where:
        pathName     || _
        "dir/file"   || _
        "dir%2Ffile" || _
    }

    def "Builder bearer token validation"() {
        // Technically no additional checks need to be added to datalake builder since the corresponding blob builder fails
        setup:
        String endpoint = BlobUrlParts.parse(dc.getDirectoryUrl()).setScheme("http").toUrl()
        def builder = new DataLakePathClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint)

        when:
        builder.buildDirectoryClient()

        then:
        thrown(IllegalArgumentException)
    }

    def "Get Access Control OAuth"() {
        setup:
        def client = getOAuthServiceClient()
        def fsClient = client.getFileSystemClient(dc.getFileSystemName())
        def dirClient = fsClient.getDirectoryClient(dc.getDirectoryPath())

        when:
        dirClient.getAccessControl()

        then:
        notThrown(StorageErrorException)
    }
}
