package com.azure.storage.file.datalake

import com.azure.core.http.HttpMethod
import com.azure.core.http.HttpPipelineCallContext
import com.azure.core.http.HttpPipelineNextPolicy
import com.azure.core.http.HttpRequest
import com.azure.core.http.policy.HttpPipelinePolicy
import com.azure.core.http.rest.Response
import com.azure.core.util.Context
import com.azure.identity.DefaultAzureCredentialBuilder
import com.azure.storage.blob.BlobUrlParts
import com.azure.storage.blob.models.BlobErrorCode
import com.azure.storage.common.Utility
import com.azure.storage.common.test.shared.extensions.RequiredServiceVersion
import com.azure.storage.file.datalake.models.*
import com.azure.storage.file.datalake.options.PathRemoveAccessControlRecursiveOptions
import com.azure.storage.file.datalake.options.PathSetAccessControlRecursiveOptions
import com.azure.storage.file.datalake.options.PathUpdateAccessControlRecursiveOptions
import com.azure.storage.file.datalake.sas.DataLakeServiceSasSignatureValues
import com.azure.storage.file.datalake.sas.PathSasPermission
import reactor.core.publisher.Mono
import spock.lang.IgnoreIf
import spock.lang.Unroll

import java.util.function.Consumer
import java.util.stream.Collectors

class DirectoryAPITest extends APISpec {
    DataLakeDirectoryClient dc
    String directoryName

    PathPermissions permissions = new PathPermissions()
        .setOwner(new RolePermissions().setReadPermission(true).setWritePermission(true).setExecutePermission(true))
        .setGroup(new RolePermissions().setReadPermission(true).setExecutePermission(true))
        .setOther(new RolePermissions().setReadPermission(true))

    List<PathAccessControlEntry> pathAccessControlEntries = PathAccessControlEntry.parseList("user::rwx,group::r--,other::---,mask::rwx")
    List<PathAccessControlEntry> executeOnlyAccessControlEntries = PathAccessControlEntry.parseList("user::--x,group::--x,other::--x")
    List<PathRemoveAccessControlEntry> removeAccessControlEntries = PathRemoveAccessControlEntry.parseList("mask," +
        "default:user,default:group," +
        "user:ec3595d6-2c17-4696-8caa-7e139758d24a,group:ec3595d6-2c17-4696-8caa-7e139758d24a," +
        "default:user:ec3595d6-2c17-4696-8caa-7e139758d24a,default:group:ec3595d6-2c17-4696-8caa-7e139758d24a")

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
        notThrown(DataLakeStorageException)
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
        thrown(DataLakeStorageException)
    }

    def "Create overwrite"() {
        when:
        dc = fsc.getDirectoryClient(generatePathName())
        dc.create()

        // Try to create the resource again
        dc.create(false)

        then:
        thrown(DataLakeStorageException)
    }

    def "Exists"() {
        when:
        dc = fsc.getDirectoryClient(generatePathName())
        dc.create()

        then:
        dc.exists()
    }

    def "Does not exist"() {
        expect:
        !fsc.getDirectoryClient(generatePathName()).exists()
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
        def e = thrown(DataLakeStorageException)
        e.getResponse().getStatusCode() == 404
        e.getErrorCode() == BlobErrorCode.BLOB_NOT_FOUND.toString()
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
        def resp = dc.setPermissions(permissions, group, owner)

        then:
        notThrown(DataLakeStorageException)
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
        dc = fsc.getDirectoryClient(generatePathName())

        when:
        dc.setPermissionsWithResponse(permissions, group, owner, null, null, null)

        then:
        thrown(DataLakeStorageException)
    }

    def "Set ACL min"() {
        when:
        def resp = dc.setAccessControlList(pathAccessControlEntries, group, owner)

        then:
        notThrown(DataLakeStorageException)
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
        dc = fsc.getDirectoryClient(generatePathName())

        when:
        dc.setAccessControlList(pathAccessControlEntries, group, owner)

        then:
        thrown(DataLakeStorageException)
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "V2020_02_10")
    def "Set ACL recursive min"() {
        setup:
        setupStandardRecursiveAclTest()

        when:
        def result = dc.setAccessControlRecursive(pathAccessControlEntries)

        then:
        result.getCounters().getChangedDirectoriesCount() == 3 // Including the top level
        result.getCounters().getChangedFilesCount() == 4
        result.getCounters().getFailedChangesCount() == 0
        result.getContinuationToken() == null
        result.getBatchFailures() == null
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "V2020_02_10")
    def "Set ACL recursive batches"() {
        setup:
        setupStandardRecursiveAclTest()

        def options = new PathSetAccessControlRecursiveOptions(pathAccessControlEntries)
            .setBatchSize(2)

        when:
        def result = dc.setAccessControlRecursiveWithResponse(options, null, null).getValue()

        then:
        result.getCounters().getChangedDirectoriesCount() == 3 // Including the top level
        result.getCounters().getChangedFilesCount() == 4
        result.getCounters().getFailedChangesCount() == 0
        result.getContinuationToken() == null
        result.getBatchFailures() == null
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "V2020_02_10")
    def "Set ACL recursive batches resume"() {
        setup:
        setupStandardRecursiveAclTest()

        def options = new PathSetAccessControlRecursiveOptions(pathAccessControlEntries)
            .setBatchSize(2).setMaxBatches(1)

        when:
        def result = dc.setAccessControlRecursiveWithResponse(options, null, null).getValue()

        and:
        options.setMaxBatches(null).setContinuationToken(result.getContinuationToken())
        def result2 = dc.setAccessControlRecursiveWithResponse(options, null, null).getValue()

        then:
        (result.getCounters().getChangedDirectoriesCount() + result2.getCounters().getChangedDirectoriesCount()) == 3 // Including the top level
        (result.getCounters().getChangedFilesCount() + result2.getCounters().getChangedFilesCount()) == 4
        (result.getCounters().getFailedChangesCount() + result2.getCounters().getFailedChangesCount()) == 0
        result2.getContinuationToken() == null
        result.getBatchFailures() == null
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "V2020_02_10")
    def "Set ACL recursive batches progress"() {
        setup:
        setupStandardRecursiveAclTest()

        def progress = new InMemoryAccessControlRecursiveChangeProgress()

        def options = new PathSetAccessControlRecursiveOptions(pathAccessControlEntries)
            .setBatchSize(2).setProgressHandler(progress)

        when:
        def result = dc.setAccessControlRecursiveWithResponse(options, null, null).getValue()

        then:
        result.getCounters().getChangedDirectoriesCount() == 3
        result.getCounters().getChangedFilesCount() == 4
        result.getCounters().getFailedChangesCount() == 0
        result.getContinuationToken() == null
        result.getBatchFailures() == null
        progress.batchCounters.size() == 4
        (progress.batchCounters[0].getChangedFilesCount() + progress.batchCounters[0].getChangedDirectoriesCount()) == 2
        (progress.batchCounters[1].getChangedFilesCount() + progress.batchCounters[1].getChangedDirectoriesCount()) == 2
        (progress.batchCounters[2].getChangedFilesCount() + progress.batchCounters[2].getChangedDirectoriesCount()) == 2
        (progress.batchCounters[3].getChangedFilesCount() + progress.batchCounters[3].getChangedDirectoriesCount()) == 1
        progress.cumulativeCounters.size() == 4
        (progress.cumulativeCounters[0].getChangedFilesCount() + progress.cumulativeCounters[0].getChangedDirectoriesCount()) == 2
        (progress.cumulativeCounters[1].getChangedFilesCount() + progress.cumulativeCounters[1].getChangedDirectoriesCount()) == 4
        (progress.cumulativeCounters[2].getChangedFilesCount() + progress.cumulativeCounters[2].getChangedDirectoriesCount()) == 6
        (progress.cumulativeCounters[3].getChangedFilesCount() + progress.cumulativeCounters[3].getChangedDirectoriesCount()) == 7
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "V2020_02_10")
    def "Set ACL recursive batches follow token"() {
        setup:
        setupStandardRecursiveAclTest()

        def options = new PathSetAccessControlRecursiveOptions(pathAccessControlEntries)
            .setBatchSize(2).setMaxBatches(2)

        when:
        String continuation = "null"
        def failedChanges = 0
        def directoriesChanged = 0
        def filesChanged = 0
        def iterations = 0
        while(continuation != null && continuation != "" && iterations < 10) {
            if (iterations == 0) {
                continuation = null // do while not supported in Groovy
            }
            options.setContinuationToken(continuation)
            def result = dc.setAccessControlRecursiveWithResponse(options, null, null)
            failedChanges += result.getValue().getCounters().getFailedChangesCount()
            directoriesChanged += result.getValue().getCounters().getChangedDirectoriesCount()
            filesChanged += result.getValue().getCounters().getChangedFilesCount()
            iterations++
            continuation = result.getValue().getContinuationToken()
        }

        then:
        failedChanges == 0
        directoriesChanged == 3
        filesChanged == 4
        iterations == 2
    }

    def getSasDirectoryClient(DataLakeDirectoryClient directoryClient, String owner) {
        def key = getOAuthServiceClient().getUserDelegationKey(null, namer.getUtcNow().plusHours(1))
        def keyOid = namer.recordValueFromConfig(key.getSignedObjectId())
        key.setSignedObjectId(keyOid)
        def keyTid = namer.recordValueFromConfig(key.getSignedTenantId())
        key.setSignedTenantId(keyTid)
        def sas = directoryClient.generateUserDelegationSas(new DataLakeServiceSasSignatureValues(namer.getUtcNow().plusHours(1), PathSasPermission.parse("racwdlmeop")).setAgentObjectId(owner), key)
        return getDirectoryClient(sas, directoryClient.getDirectoryUrl(), directoryClient.getDirectoryPath())
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "V2020_02_10")
    def "Set ACL recursive progress with failure"() {
        setup:
        fsc.getRootDirectoryClient().setAccessControlList(executeOnlyAccessControlEntries, null, null)
        String topDirName = generatePathName()

        // Create tree using AAD creds
        def topDirOauthClient = getOAuthServiceClient().getFileSystemClient(fsc.getFileSystemName())
            .getDirectoryClient(topDirName)
        topDirOauthClient.create()
        def subdir1 = topDirOauthClient.createSubdirectory(generatePathName())
        def file1 = subdir1.createFile(generatePathName())
        def file2 = subdir1.createFile(generatePathName())
        def subdir2 = topDirOauthClient.createSubdirectory(generatePathName())
        def file3 = subdir2.createFile(generatePathName())

        // Only allow subowner rights to the directory and it's subpaths
        def subowner = namer.getRandomUuid()
        def rp = RolePermissions.parseSymbolic("rwx", false)
        def pathPermissions = new PathPermissions().setGroup(rp).setOther(rp).setOwner(rp)
        topDirOauthClient.setPermissions(pathPermissions, null, subowner)
        subdir1.setPermissions(pathPermissions, null, subowner)
        file1.setPermissions(pathPermissions, null, subowner)
        file2.setPermissions(pathPermissions, null, subowner)
        subdir2.setPermissions(pathPermissions, null, subowner)
        file3.setPermissions(pathPermissions, null, subowner)

        // Create file4 without assigning subowner permissions
        def file4 = fsc.getDirectoryClient(topDirName).getSubdirectoryClient(subdir2.getObjectName())
            .createFile(generatePathName())

        // Create a user delegation sas that delegates an owner when creating files
        def subOwnerDirClient = getSasDirectoryClient(topDirOauthClient, subowner)

        def progress = new InMemoryAccessControlRecursiveChangeProgress()

        when:
        def result = subOwnerDirClient.setAccessControlRecursiveWithResponse(
            new PathSetAccessControlRecursiveOptions(pathAccessControlEntries).setProgressHandler(progress), null, null)

        then:
        result.getValue().getCounters().getFailedChangesCount() == 1
        progress.failures.size() == 1
        progress.batchCounters.findIndexOf {counter -> counter.getFailedChangesCount() > 0} >= 0
        progress.cumulativeCounters.findIndexOf {counter -> counter.getFailedChangesCount() > 0} >= 0
        progress.failures[0].getName().contains(file4.getObjectName())
        !progress.failures[0].isDirectory()
        progress.failures[0].getErrorMessage()
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "V2020_02_10")
    def "Set ACL recursive continue on failure"() {
        setup:
        fsc.getRootDirectoryClient().setAccessControlList(executeOnlyAccessControlEntries, null, null)
        String topDirName = generatePathName()

        // Create tree using AAD creds
        def topDirOauthClient = getOAuthServiceClient().getFileSystemClient(fsc.getFileSystemName())
            .getDirectoryClient(topDirName)
        topDirOauthClient.create()
        def subdir1 = topDirOauthClient.createSubdirectory(generatePathName())
        def file1 = subdir1.createFile(generatePathName())
        def file2 = subdir1.createFile(generatePathName())
        def subdir2 = topDirOauthClient.createSubdirectory(generatePathName())
        def file3 = subdir2.createFile(generatePathName())

        // Only allow subowner rights to the directory and it's subpaths
        def subowner = namer.getRandomUuid()
        def rp = RolePermissions.parseSymbolic("rwx", false)
        def pathPermissions = new PathPermissions().setGroup(rp).setOther(rp).setOwner(rp)
        topDirOauthClient.setPermissions(pathPermissions, null, subowner)
        subdir1.setPermissions(pathPermissions, null, subowner)
        file1.setPermissions(pathPermissions, null, subowner)
        file2.setPermissions(pathPermissions, null, subowner)
        subdir2.setPermissions(pathPermissions, null, subowner)
        file3.setPermissions(pathPermissions, null, subowner)

        // Create resources as super user (using shared key)
        def file4 = fsc.getDirectoryClient(topDirName).getSubdirectoryClient(subdir2.getObjectName())
            .createFile(generatePathName())
        def file5 = fsc.getDirectoryClient(topDirName).getSubdirectoryClient(subdir2.getObjectName())
            .createFile(generatePathName())
        def file6 = fsc.getDirectoryClient(topDirName).getSubdirectoryClient(subdir2.getObjectName())
            .createFile(generatePathName())
        def subdir3 = fsc.getDirectoryClient(topDirName).getSubdirectoryClient(subdir2.getObjectName())
            .createSubdirectory(generatePathName())

        // Create a user delegation sas that delegates an owner when creating files
        def subOwnerDirClient = getSasDirectoryClient(topDirOauthClient, subowner)

        when:
        def result = subOwnerDirClient.setAccessControlRecursiveWithResponse(
            new PathSetAccessControlRecursiveOptions(pathAccessControlEntries).setContinueOnFailure(true), null, null)

        def batchFailures = result.getValue().getBatchFailures().stream().map( { failure -> failure.getName() } ).collect(Collectors.toList())

        then:
        result.getValue().getCounters().getChangedDirectoriesCount() == 3
        result.getValue().getCounters().getChangedFilesCount() == 3
        result.getValue().getCounters().getFailedChangesCount() == 4
        result.getValue().getContinuationToken() == null
        batchFailures.size() == 4
        batchFailures.contains(file4.getObjectPath())
        batchFailures.contains(file5.getObjectPath())
        batchFailures.contains(file6.getObjectPath())
        batchFailures.contains(subdir3.getObjectPath())
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "V2020_02_10")
    def "Set ACL recursive continue on failure batch failures"() {
        setup:
        fsc.getRootDirectoryClient().setAccessControlList(executeOnlyAccessControlEntries, null, null)
        String topDirName = generatePathName()

        // Create tree using AAD creds
        def topDirOauthClient = getOAuthServiceClient().getFileSystemClient(fsc.getFileSystemName())
            .getDirectoryClient(topDirName)
        topDirOauthClient.create()
        def subdir1 = topDirOauthClient.createSubdirectory(generatePathName())
        def file1 = subdir1.createFile(generatePathName())
        def file2 = subdir1.createFile(generatePathName())
        def subdir2 = topDirOauthClient.createSubdirectory(generatePathName())
        def file3 = subdir2.createFile(generatePathName())

        // Only allow subowner rights to the directory and it's subpaths
        def subowner = namer.getRandomUuid()
        def rp = RolePermissions.parseSymbolic("rwx", false)
        def pathPermissions = new PathPermissions().setGroup(rp).setOther(rp).setOwner(rp)
        topDirOauthClient.setPermissions(pathPermissions, null, subowner)
        subdir1.setPermissions(pathPermissions, null, subowner)
        file1.setPermissions(pathPermissions, null, subowner)
        file2.setPermissions(pathPermissions, null, subowner)
        subdir2.setPermissions(pathPermissions, null, subowner)
        file3.setPermissions(pathPermissions, null, subowner)

        // Create resources as super user (using shared key)
        def file4 = fsc.getDirectoryClient(topDirName).getSubdirectoryClient(subdir2.getObjectName())
            .createFile(generatePathName())
        def file5 = fsc.getDirectoryClient(topDirName).getSubdirectoryClient(subdir2.getObjectName())
            .createFile(generatePathName())
        def file6 = fsc.getDirectoryClient(topDirName).getSubdirectoryClient(subdir2.getObjectName())
            .createFile(generatePathName())
        def subdir3 = fsc.getDirectoryClient(topDirName).getSubdirectoryClient(subdir2.getObjectName())
            .createSubdirectory(generatePathName())

        // Create a user delegation sas that delegates an owner when creating files
        def subOwnerDirClient = getSasDirectoryClient(topDirOauthClient, subowner)

        def progress = new InMemoryAccessControlRecursiveChangeProgress()

        when:
        def result = subOwnerDirClient.setAccessControlRecursiveWithResponse(
            new PathSetAccessControlRecursiveOptions(pathAccessControlEntries).setContinueOnFailure(true).setBatchSize(2).setProgressHandler(progress), null, null)

        def batchFailures = result.getValue().getBatchFailures().stream().map( { failure -> failure.getName() } ).collect(Collectors.toList())

        then:
        result.getValue().getCounters().getChangedDirectoriesCount() == 3
        result.getValue().getCounters().getChangedFilesCount() == 3
        result.getValue().getCounters().getFailedChangesCount() == 4
        batchFailures.size() == progress.firstFailures.size()
        for (def f : progress.firstFailures) {
            assert batchFailures.contains(f.getName())
        }
        result.getValue().getContinuationToken() == null
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "V2020_02_10")
    def "Set ACL recursive continue on failure batches resume"() {
        setup:
        fsc.getRootDirectoryClient().setAccessControlList(executeOnlyAccessControlEntries, null, null)
        String topDirName = generatePathName()

        // Create tree using AAD creds
        def topDirOauthClient = getOAuthServiceClient().getFileSystemClient(fsc.getFileSystemName())
            .getDirectoryClient(topDirName)
        topDirOauthClient.create()
        def subdir1 = topDirOauthClient.createSubdirectory(generatePathName())
        def file1 = subdir1.createFile(generatePathName())
        def file2 = subdir1.createFile(generatePathName())
        def subdir2 = topDirOauthClient.createSubdirectory(generatePathName())
        def file3 = subdir2.createFile(generatePathName())

        // Only allow subowner rights to the directory and it's subpaths
        def subowner = namer.getRandomUuid()
        def rp = RolePermissions.parseSymbolic("rwx", false)
        def pathPermissions = new PathPermissions().setGroup(rp).setOther(rp).setOwner(rp)
        topDirOauthClient.setPermissions(pathPermissions, null, subowner)
        subdir1.setPermissions(pathPermissions, null, subowner)
        file1.setPermissions(pathPermissions, null, subowner)
        file2.setPermissions(pathPermissions, null, subowner)
        subdir2.setPermissions(pathPermissions, null, subowner)
        file3.setPermissions(pathPermissions, null, subowner)

        // Create resources as super user (using shared key)
        def file4 = fsc.getDirectoryClient(topDirName).getSubdirectoryClient(subdir2.getObjectName())
            .createFile(generatePathName())
        def file5 = fsc.getDirectoryClient(topDirName).getSubdirectoryClient(subdir2.getObjectName())
            .createFile(generatePathName())
        def file6 = fsc.getDirectoryClient(topDirName).getSubdirectoryClient(subdir2.getObjectName())
            .createFile(generatePathName())
        def subdir3 = fsc.getDirectoryClient(topDirName).getSubdirectoryClient(subdir2.getObjectName())
            .createSubdirectory(generatePathName())

        // Create more files as app
        def file7 = subdir1.createFile(generatePathName())
        def file8 = subdir1.createFile(generatePathName())
        def subdir4 = topDirOauthClient.createSubdirectory(generatePathName())
        def file9 = subdir4.createFile(generatePathName())

        // Only allow subowner rights to the directory and it's subpaths
        file7.setPermissions(pathPermissions, null, subowner)
        file8.setPermissions(pathPermissions, null, subowner)
        subdir4.setPermissions(pathPermissions, null, subowner)
        file9.setPermissions(pathPermissions, null, subowner)

        // Create a user delegation sas that delegates an owner when creating files
        def subOwnerDirClient = getSasDirectoryClient(topDirOauthClient, subowner)

        def options = new PathSetAccessControlRecursiveOptions(pathAccessControlEntries)
            .setBatchSize(2).setContinueOnFailure(true).setMaxBatches(1)

        when:
        def intermediateResult = subOwnerDirClient.setAccessControlRecursiveWithResponse(options, null, null)

        then:
        intermediateResult.getValue().getContinuationToken() != null

        when:
        options.setMaxBatches(null).setContinuationToken(intermediateResult.getValue().getContinuationToken())
        def result = subOwnerDirClient.setAccessControlRecursiveWithResponse(options, null, null)

        then:
        (result.getValue().getCounters().getChangedDirectoriesCount() + intermediateResult.getValue().getCounters().getChangedDirectoriesCount()) == 4
        (result.getValue().getCounters().getChangedFilesCount() + intermediateResult.getValue().getCounters().getChangedFilesCount()) == 6
        (result.getValue().getCounters().getFailedChangesCount() + intermediateResult.getValue().getCounters().getFailedChangesCount()) == 4
        result.getValue().getContinuationToken() == null
    }

    def "Set ACL recursive error"() {
        setup:
        fsc.getRootDirectoryClient().setAccessControlList(executeOnlyAccessControlEntries, null, null)

        String topDirName = generatePathName()
        def topDirOauthClient = getOAuthServiceClient().getFileSystemClient(fsc.getFileSystemName())
            .getDirectoryClient(topDirName)

        when:
        topDirOauthClient.setAccessControlRecursiveWithResponse(
            new PathSetAccessControlRecursiveOptions(pathAccessControlEntries), null, null)

        then:
        def e = thrown(DataLakeAclChangeFailedException)
        e.getCause().class == DataLakeStorageException.class
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "V2020_02_10")
    @Unroll
    def "Set ACL recursive error middle of batches"() {
        setup:
        setupStandardRecursiveAclTest()

        def options = new PathSetAccessControlRecursiveOptions(pathAccessControlEntries)
            .setBatchSize(2)

        // Mock a policy that will return an error on the call with the continuation token
        HttpPipelinePolicy mockPolicy = { HttpPipelineCallContext context, HttpPipelineNextPolicy next ->
            return context.getHttpRequest().getUrl().toString().contains("continuation") ? Mono.error(error) : next.process()
        }

        dc = getDirectoryClient(env.dataLakeAccount.credential, dc.getDirectoryUrl(), dc.getObjectPath(), mockPolicy)

        when:
        def result = dc.setAccessControlRecursiveWithResponse(options, null, null).getValue()

        then:
        def e = thrown(DataLakeAclChangeFailedException)
        e.getCause().class == error.class

        where:
        error                                                                                                                               || _
        new IllegalArgumentException()                                                                                                      || _
        new DataLakeStorageException("error", getStubResponse(500, new HttpRequest(HttpMethod.PUT, new URL("https://www.fake.com"))), null) || _
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "V2020_02_10")
    def "Update ACL recursive"() {
        setup:
        setupStandardRecursiveAclTest()

        when:
        def result = dc.updateAccessControlRecursive(pathAccessControlEntries)

        then:
        result.getCounters().getChangedDirectoriesCount() == 3
        result.getCounters().getChangedFilesCount() == 4
        result.getCounters().getFailedChangesCount() == 0
        result.getBatchFailures() == null
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "V2020_02_10")
    def "Update ACL recursive batches"() {
        setup:
        setupStandardRecursiveAclTest()

        def options = new PathUpdateAccessControlRecursiveOptions(pathAccessControlEntries)
            .setBatchSize(2)

        when:
        def result = dc.updateAccessControlRecursiveWithResponse(options, null, null).getValue()

        then:
        result.getCounters().getChangedDirectoriesCount() == 3 // Including the top level
        result.getCounters().getChangedFilesCount() == 4
        result.getCounters().getFailedChangesCount() == 0
        result.getContinuationToken() == null
        result.getBatchFailures() == null
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "V2020_02_10")
    def "Update ACL recursive batches resume"() {
        setup:
        setupStandardRecursiveAclTest()

        def options = new PathUpdateAccessControlRecursiveOptions(pathAccessControlEntries)
            .setBatchSize(2).setMaxBatches(1)

        when:
        def result = dc.updateAccessControlRecursiveWithResponse(options, null, null).getValue()

        and:
        options.setMaxBatches(null).setContinuationToken(result.getContinuationToken())
        def result2 = dc.updateAccessControlRecursiveWithResponse(options, null, null).getValue()

        then:
        (result.getCounters().getChangedDirectoriesCount() + result2.getCounters().getChangedDirectoriesCount()) == 3 // Including the top level
        (result.getCounters().getChangedFilesCount() + result2.getCounters().getChangedFilesCount()) == 4
        (result.getCounters().getFailedChangesCount() + result2.getCounters().getFailedChangesCount()) == 0
        result2.getContinuationToken() == null
        result.getBatchFailures() == null
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "V2020_02_10")
    def "Update ACL recursive batches progress"() {
        setup:
        setupStandardRecursiveAclTest()

        def progress = new InMemoryAccessControlRecursiveChangeProgress()

        def options = new PathUpdateAccessControlRecursiveOptions(pathAccessControlEntries)
            .setBatchSize(2).setProgressHandler(progress)

        when:
        def result = dc.updateAccessControlRecursiveWithResponse(options, null, null).getValue()

        then:
        result.getCounters().getChangedDirectoriesCount() == 3
        result.getCounters().getChangedFilesCount() == 4
        result.getCounters().getFailedChangesCount() == 0
        result.getContinuationToken() == null
        result.getBatchFailures() == null
        progress.batchCounters.size() == 4
        (progress.batchCounters[0].getChangedFilesCount() + progress.batchCounters[0].getChangedDirectoriesCount()) == 2
        (progress.batchCounters[1].getChangedFilesCount() + progress.batchCounters[1].getChangedDirectoriesCount()) == 2
        (progress.batchCounters[2].getChangedFilesCount() + progress.batchCounters[2].getChangedDirectoriesCount()) == 2
        (progress.batchCounters[3].getChangedFilesCount() + progress.batchCounters[3].getChangedDirectoriesCount()) == 1
        progress.cumulativeCounters.size() == 4
        (progress.cumulativeCounters[0].getChangedFilesCount() + progress.cumulativeCounters[0].getChangedDirectoriesCount()) == 2
        (progress.cumulativeCounters[1].getChangedFilesCount() + progress.cumulativeCounters[1].getChangedDirectoriesCount()) == 4
        (progress.cumulativeCounters[2].getChangedFilesCount() + progress.cumulativeCounters[2].getChangedDirectoriesCount()) == 6
        (progress.cumulativeCounters[3].getChangedFilesCount() + progress.cumulativeCounters[3].getChangedDirectoriesCount()) == 7
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "V2020_02_10")
    def "Update ACL recursive batches follow token"() {
        setup:
        setupStandardRecursiveAclTest()

        def options = new PathUpdateAccessControlRecursiveOptions(pathAccessControlEntries)
            .setBatchSize(2).setMaxBatches(2)

        when:
        String continuation = "null"
        def failedChanges = 0
        def directoriesChanged = 0
        def filesChanged = 0
        def iterations = 0
        while(continuation != null && continuation != "" && iterations < 10) {
            if (iterations == 0) {
                continuation = null // do while not supported in Groovy
            }
            options.setContinuationToken(continuation)
            def result = dc.updateAccessControlRecursiveWithResponse(options, null, null)
            failedChanges += result.getValue().getCounters().getFailedChangesCount()
            directoriesChanged += result.getValue().getCounters().getChangedDirectoriesCount()
            filesChanged += result.getValue().getCounters().getChangedFilesCount()
            iterations++
            continuation = result.getValue().getContinuationToken()
        }

        then:
        failedChanges == 0
        directoriesChanged == 3
        filesChanged == 4
        iterations == 2
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "V2020_02_10")
    def "Update ACL recursive progress with failure"() {
        setup:
        fsc.getRootDirectoryClient().setAccessControlList(executeOnlyAccessControlEntries, null, null)
        String topDirName = generatePathName()

        // Create tree using AAD creds
        def topDirOauthClient = getOAuthServiceClient().getFileSystemClient(fsc.getFileSystemName())
            .getDirectoryClient(topDirName)
        topDirOauthClient.create()
        def subdir1 = topDirOauthClient.createSubdirectory(generatePathName())
        def file1 = subdir1.createFile(generatePathName())
        def file2 = subdir1.createFile(generatePathName())
        def subdir2 = topDirOauthClient.createSubdirectory(generatePathName())
        def file3 = subdir2.createFile(generatePathName())

        // Only allow subowner rights to the directory and it's subpaths
        def subowner = namer.getRandomUuid()
        def rp = RolePermissions.parseSymbolic("rwx", false)
        def pathPermissions = new PathPermissions().setGroup(rp).setOther(rp).setOwner(rp)
        topDirOauthClient.setPermissions(pathPermissions, null, subowner)
        subdir1.setPermissions(pathPermissions, null, subowner)
        file1.setPermissions(pathPermissions, null, subowner)
        file2.setPermissions(pathPermissions, null, subowner)
        subdir2.setPermissions(pathPermissions, null, subowner)
        file3.setPermissions(pathPermissions, null, subowner)

        // Create file4 as super user (using shared key)
        def file4 = fsc.getDirectoryClient(topDirName).getSubdirectoryClient(subdir2.getObjectName())
            .createFile(generatePathName())

        def progress = new InMemoryAccessControlRecursiveChangeProgress()

        // Create a user delegation sas that delegates an owner when creating files
        def subOwnerDirClient = getSasDirectoryClient(topDirOauthClient, subowner)

        when:
        def result = subOwnerDirClient.updateAccessControlRecursiveWithResponse(
            new PathUpdateAccessControlRecursiveOptions(pathAccessControlEntries).setProgressHandler(progress), null, null)

        then:
        result.getValue().getCounters().getFailedChangesCount() == 1
        progress.failures.size() == 1
        progress.batchCounters.findIndexOf {counter -> counter.getFailedChangesCount() > 0} >= 0
        progress.cumulativeCounters.findIndexOf {counter -> counter.getFailedChangesCount() > 0} >= 0
        progress.failures[0].getName().contains(file4.getObjectName())
        !progress.failures[0].isDirectory()
        progress.failures[0].getErrorMessage()
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "V2020_02_10")
    def "Update ACL recursive continue on failure"() {
        setup:
        fsc.getRootDirectoryClient().setAccessControlList(executeOnlyAccessControlEntries, null, null)
        String topDirName = generatePathName()

        // Create tree using AAD creds
        def topDirOauthClient = getOAuthServiceClient().getFileSystemClient(fsc.getFileSystemName())
            .getDirectoryClient(topDirName)
        topDirOauthClient.create()
        def subdir1 = topDirOauthClient.createSubdirectory(generatePathName())
        def file1 = subdir1.createFile(generatePathName())
        def file2 = subdir1.createFile(generatePathName())
        def subdir2 = topDirOauthClient.createSubdirectory(generatePathName())
        def file3 = subdir2.createFile(generatePathName())

        // Only allow subowner rights to the directory and it's subpaths
        def subowner = namer.getRandomUuid()
        def rp = RolePermissions.parseSymbolic("rwx", false)
        def pathPermissions = new PathPermissions().setGroup(rp).setOther(rp).setOwner(rp)
        topDirOauthClient.setPermissions(pathPermissions, null, subowner)
        subdir1.setPermissions(pathPermissions, null, subowner)
        file1.setPermissions(pathPermissions, null, subowner)
        file2.setPermissions(pathPermissions, null, subowner)
        subdir2.setPermissions(pathPermissions, null, subowner)
        file3.setPermissions(pathPermissions, null, subowner)

        // Create resources as super user (using shared key)
        def file4 = fsc.getDirectoryClient(topDirName).getSubdirectoryClient(subdir2.getObjectName())
            .createFile(generatePathName())
        def file5 = fsc.getDirectoryClient(topDirName).getSubdirectoryClient(subdir2.getObjectName())
            .createFile(generatePathName())
        def file6 = fsc.getDirectoryClient(topDirName).getSubdirectoryClient(subdir2.getObjectName())
            .createFile(generatePathName())
        def subdir3 = fsc.getDirectoryClient(topDirName).getSubdirectoryClient(subdir2.getObjectName())
            .createSubdirectory(generatePathName())

        // Create a user delegation sas that delegates an owner when creating files
        def subOwnerDirClient = getSasDirectoryClient(topDirOauthClient, subowner)

        when:
        def result = subOwnerDirClient.updateAccessControlRecursiveWithResponse(
            new PathUpdateAccessControlRecursiveOptions(pathAccessControlEntries).setContinueOnFailure(true), null, null)

        def batchFailures = result.getValue().getBatchFailures().stream().map( { failure -> failure.getName() } ).collect(Collectors.toList())

        then:
        result.getValue().getCounters().getChangedDirectoriesCount() == 3
        result.getValue().getCounters().getChangedFilesCount() == 3
        result.getValue().getCounters().getFailedChangesCount() == 4
        result.getValue().getContinuationToken() == null
        batchFailures.size() == 4
        batchFailures.contains(file4.getObjectPath())
        batchFailures.contains(file5.getObjectPath())
        batchFailures.contains(file6.getObjectPath())
        batchFailures.contains(subdir3.getObjectPath())
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "V2020_02_10")
    def "Update ACL recursive continue on failure batch failures"() {
        setup:
        fsc.getRootDirectoryClient().setAccessControlList(executeOnlyAccessControlEntries, null, null)
        String topDirName = generatePathName()

        // Create tree using AAD creds
        def topDirOauthClient = getOAuthServiceClient().getFileSystemClient(fsc.getFileSystemName())
            .getDirectoryClient(topDirName)
        topDirOauthClient.create()
        def subdir1 = topDirOauthClient.createSubdirectory(generatePathName())
        def file1 = subdir1.createFile(generatePathName())
        def file2 = subdir1.createFile(generatePathName())
        def subdir2 = topDirOauthClient.createSubdirectory(generatePathName())
        def file3 = subdir2.createFile(generatePathName())

        // Only allow subowner rights to the directory and it's subpaths
        def subowner = namer.getRandomUuid()
        def rp = RolePermissions.parseSymbolic("rwx", false)
        def pathPermissions = new PathPermissions().setGroup(rp).setOther(rp).setOwner(rp)
        topDirOauthClient.setPermissions(pathPermissions, null, subowner)
        subdir1.setPermissions(pathPermissions, null, subowner)
        file1.setPermissions(pathPermissions, null, subowner)
        file2.setPermissions(pathPermissions, null, subowner)
        subdir2.setPermissions(pathPermissions, null, subowner)
        file3.setPermissions(pathPermissions, null, subowner)

        // Create resources as super user (using shared key)
        def file4 = fsc.getDirectoryClient(topDirName).getSubdirectoryClient(subdir2.getObjectName())
            .createFile(generatePathName())
        def file5 = fsc.getDirectoryClient(topDirName).getSubdirectoryClient(subdir2.getObjectName())
            .createFile(generatePathName())
        def file6 = fsc.getDirectoryClient(topDirName).getSubdirectoryClient(subdir2.getObjectName())
            .createFile(generatePathName())
        def subdir3 = fsc.getDirectoryClient(topDirName).getSubdirectoryClient(subdir2.getObjectName())
            .createSubdirectory(generatePathName())

        // Create a user delegation sas that delegates an owner when creating files
        def subOwnerDirClient = getSasDirectoryClient(topDirOauthClient, subowner)

        def progress = new InMemoryAccessControlRecursiveChangeProgress()

        when:
        def result = subOwnerDirClient.updateAccessControlRecursiveWithResponse(
            new PathUpdateAccessControlRecursiveOptions(pathAccessControlEntries).setContinueOnFailure(true).setBatchSize(2).setProgressHandler(progress), null, null)

        def batchFailures = result.getValue().getBatchFailures().stream().map( { failure -> failure.getName() } ).collect(Collectors.toList())

        then:
        result.getValue().getCounters().getChangedDirectoriesCount() == 3
        result.getValue().getCounters().getChangedFilesCount() == 3
        result.getValue().getCounters().getFailedChangesCount() == 4
        batchFailures.size() == progress.firstFailures.size()
        for (def f : progress.firstFailures) {
            assert batchFailures.contains(f.getName())
        }
        result.getValue().getContinuationToken() == null
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "V2020_02_10")
    def "Update ACL recursive continue on failure batches resume"() {
        setup:
        fsc.getRootDirectoryClient().setAccessControlList(executeOnlyAccessControlEntries, null, null)
        String topDirName = generatePathName()

        // Create tree using AAD creds
        def topDirOauthClient = getOAuthServiceClient().getFileSystemClient(fsc.getFileSystemName())
            .getDirectoryClient(topDirName)
        topDirOauthClient.create()
        def subdir1 = topDirOauthClient.createSubdirectory(generatePathName())
        def file1 = subdir1.createFile(generatePathName())
        def file2 = subdir1.createFile(generatePathName())
        def subdir2 = topDirOauthClient.createSubdirectory(generatePathName())
        def file3 = subdir2.createFile(generatePathName())

        // Only allow subowner rights to the directory and it's subpaths
        def subowner = namer.getRandomUuid()
        def rp = RolePermissions.parseSymbolic("rwx", false)
        def pathPermissions = new PathPermissions().setGroup(rp).setOther(rp).setOwner(rp)
        topDirOauthClient.setPermissions(pathPermissions, null, subowner)
        subdir1.setPermissions(pathPermissions, null, subowner)
        file1.setPermissions(pathPermissions, null, subowner)
        file2.setPermissions(pathPermissions, null, subowner)
        subdir2.setPermissions(pathPermissions, null, subowner)
        file3.setPermissions(pathPermissions, null, subowner)

        // Create resources as super user (using shared key)
        def file4 = fsc.getDirectoryClient(topDirName).getSubdirectoryClient(subdir2.getObjectName())
            .createFile(generatePathName())
        def file5 = fsc.getDirectoryClient(topDirName).getSubdirectoryClient(subdir2.getObjectName())
            .createFile(generatePathName())
        def file6 = fsc.getDirectoryClient(topDirName).getSubdirectoryClient(subdir2.getObjectName())
            .createFile(generatePathName())
        def subdir3 = fsc.getDirectoryClient(topDirName).getSubdirectoryClient(subdir2.getObjectName())
            .createSubdirectory(generatePathName())

        // Create more files as app
        def file7 = subdir1.createFile(generatePathName())
        def file8 = subdir1.createFile(generatePathName())
        def subdir4 = topDirOauthClient.createSubdirectory(generatePathName())
        def file9 = subdir4.createFile(generatePathName())

        // Only allow subowner rights to the directory and it's subpaths
        file7.setPermissions(pathPermissions, null, subowner)
        file8.setPermissions(pathPermissions, null, subowner)
        subdir4.setPermissions(pathPermissions, null, subowner)
        file9.setPermissions(pathPermissions, null, subowner)

        def options = new PathUpdateAccessControlRecursiveOptions(pathAccessControlEntries)
            .setBatchSize(2).setContinueOnFailure(true).setMaxBatches(1)

        // Create a user delegation sas that delegates an owner when creating files
        def subOwnerDirClient = getSasDirectoryClient(topDirOauthClient, subowner)

        when:
        def intermediateResult = subOwnerDirClient.updateAccessControlRecursiveWithResponse(options, null, null)

        then:
        intermediateResult.getValue().getContinuationToken() != null

        when:
        options.setMaxBatches(null).setContinuationToken(intermediateResult.getValue().getContinuationToken())
        def result = subOwnerDirClient.updateAccessControlRecursiveWithResponse(options, null, null)

        then:
        (result.getValue().getCounters().getChangedDirectoriesCount() + intermediateResult.getValue().getCounters().getChangedDirectoriesCount()) == 4
        (result.getValue().getCounters().getChangedFilesCount() + intermediateResult.getValue().getCounters().getChangedFilesCount()) == 6
        (result.getValue().getCounters().getFailedChangesCount() + intermediateResult.getValue().getCounters().getFailedChangesCount()) == 4
        result.getValue().getContinuationToken() == null
    }

    def "Update ACL recursive error"() {
        setup:
        fsc.getRootDirectoryClient().setAccessControlList(executeOnlyAccessControlEntries, null, null)

        String topDirName = generatePathName()
        def topDirOauthClient = getOAuthServiceClient().getFileSystemClient(fsc.getFileSystemName())
            .getDirectoryClient(topDirName)

        when:
        topDirOauthClient.updateAccessControlRecursiveWithResponse(
            new PathUpdateAccessControlRecursiveOptions(pathAccessControlEntries), null, null)

        then:
        def e = thrown(DataLakeAclChangeFailedException)
        e.getCause().class == DataLakeStorageException.class
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "V2020_02_10")
    @Unroll
    def "Update ACL recursive error middle of batches"() {
        setup:
        setupStandardRecursiveAclTest()

        def options = new PathUpdateAccessControlRecursiveOptions(pathAccessControlEntries)
            .setBatchSize(2)

        // Mock a policy that will return an error on the call with the continuation token
        HttpPipelinePolicy mockPolicy = { HttpPipelineCallContext context, HttpPipelineNextPolicy next ->
            return context.getHttpRequest().getUrl().toString().contains("continuation") ? Mono.error(error) : next.process()
        }

        dc = getDirectoryClient(env.dataLakeAccount.credential, dc.getDirectoryUrl(), dc.getObjectPath(), mockPolicy)

        when:
        def result = dc.updateAccessControlRecursiveWithResponse(options, null, null).getValue()

        then:
        def e = thrown(DataLakeAclChangeFailedException)
        e.getCause().class == error.class

        where:
        error                                                                                                                               || _
        new IllegalArgumentException()                                                                                                      || _
        new DataLakeStorageException("error", getStubResponse(500, new HttpRequest(HttpMethod.PUT, new URL("https://www.fake.com"))), null) || _
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "V2020_02_10")
    def "Remove ACL recursive"() {
        setup:
        setupStandardRecursiveAclTest()

        when:
        def result = dc.removeAccessControlRecursive(removeAccessControlEntries)

        then:
        result.getCounters().getChangedDirectoriesCount() == 3
        result.getCounters().getChangedFilesCount() == 4
        result.getCounters().getFailedChangesCount() == 0
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "V2020_02_10")
    def "Remove ACL recursive batches"() {
        setup:
        setupStandardRecursiveAclTest()

        def options = new PathRemoveAccessControlRecursiveOptions(removeAccessControlEntries)
            .setBatchSize(2)

        when:
        def result = dc.removeAccessControlRecursiveWithResponse(options, null, null).getValue()

        then:
        result.getCounters().getChangedDirectoriesCount() == 3 // Including the top level
        result.getCounters().getChangedFilesCount() == 4
        result.getCounters().getFailedChangesCount() == 0
        result.getContinuationToken() == null
        result.getBatchFailures() == null
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "V2020_02_10")
    def "Remove ACL recursive batches resume"() {
        setup:
        setupStandardRecursiveAclTest()

        def options = new PathRemoveAccessControlRecursiveOptions(removeAccessControlEntries)
            .setBatchSize(2).setMaxBatches(1)

        when:
        def result = dc.removeAccessControlRecursiveWithResponse(options, null, null).getValue()

        and:
        options.setMaxBatches(null).setContinuationToken(result.getContinuationToken())
        def result2 = dc.removeAccessControlRecursiveWithResponse(options, null, null).getValue()

        then:
        (result.getCounters().getChangedDirectoriesCount() + result2.getCounters().getChangedDirectoriesCount()) == 3 // Including the top level
        (result.getCounters().getChangedFilesCount() + result2.getCounters().getChangedFilesCount()) == 4
        (result.getCounters().getFailedChangesCount() + result2.getCounters().getFailedChangesCount()) == 0
        result2.getContinuationToken() == null
        result.getBatchFailures() == null
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "V2020_02_10")
    def "Remove ACL recursive batches progress"() {
        setup:
        setupStandardRecursiveAclTest()

        def progress = new InMemoryAccessControlRecursiveChangeProgress()

        def options = new PathRemoveAccessControlRecursiveOptions(removeAccessControlEntries)
            .setBatchSize(2).setProgressHandler(progress)

        when:
        def result = dc.removeAccessControlRecursiveWithResponse(options, null, null).getValue()

        then:
        result.getCounters().getChangedDirectoriesCount() == 3
        result.getCounters().getChangedFilesCount() == 4
        result.getCounters().getFailedChangesCount() == 0
        result.getContinuationToken() == null
        result.getBatchFailures() == null
        progress.batchCounters.size() == 4
        (progress.batchCounters[0].getChangedFilesCount() + progress.batchCounters[0].getChangedDirectoriesCount()) == 2
        (progress.batchCounters[1].getChangedFilesCount() + progress.batchCounters[1].getChangedDirectoriesCount()) == 2
        (progress.batchCounters[2].getChangedFilesCount() + progress.batchCounters[2].getChangedDirectoriesCount()) == 2
        (progress.batchCounters[3].getChangedFilesCount() + progress.batchCounters[3].getChangedDirectoriesCount()) == 1
        progress.cumulativeCounters.size() == 4
        (progress.cumulativeCounters[0].getChangedFilesCount() + progress.cumulativeCounters[0].getChangedDirectoriesCount()) == 2
        (progress.cumulativeCounters[1].getChangedFilesCount() + progress.cumulativeCounters[1].getChangedDirectoriesCount()) == 4
        (progress.cumulativeCounters[2].getChangedFilesCount() + progress.cumulativeCounters[2].getChangedDirectoriesCount()) == 6
        (progress.cumulativeCounters[3].getChangedFilesCount() + progress.cumulativeCounters[3].getChangedDirectoriesCount()) == 7
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "V2020_02_10")
    def "Remove ACL recursive batches follow token"() {
        setup:
        setupStandardRecursiveAclTest()

        def options = new PathRemoveAccessControlRecursiveOptions(removeAccessControlEntries)
            .setBatchSize(2).setMaxBatches(2)

        when:
        String continuation = "null"
        def failedChanges = 0
        def directoriesChanged = 0
        def filesChanged = 0
        def iterations = 0
        while(continuation != null && continuation != "" && iterations < 10) {
            if (iterations == 0) {
                continuation = null // do while not supported in Groovy
            }
            options.setContinuationToken(continuation)
            def result = dc.removeAccessControlRecursiveWithResponse(options, null, null)
            failedChanges += result.getValue().getCounters().getFailedChangesCount()
            directoriesChanged += result.getValue().getCounters().getChangedDirectoriesCount()
            filesChanged += result.getValue().getCounters().getChangedFilesCount()
            iterations++
            continuation = result.getValue().getContinuationToken()
        }

        then:
        failedChanges == 0
        directoriesChanged == 3
        filesChanged == 4
        iterations == 2
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "V2020_02_10")
    def "Remove ACL recursive progress with failure"() {
        setup:
        fsc.getRootDirectoryClient().setAccessControlList(executeOnlyAccessControlEntries, null, null)
        String topDirName = generatePathName()

        // Create tree using AAD creds
        def topDirOauthClient = getOAuthServiceClient().getFileSystemClient(fsc.getFileSystemName())
            .getDirectoryClient(topDirName)
        topDirOauthClient.create()
        def subdir1 = topDirOauthClient.createSubdirectory(generatePathName())
        def file1 = subdir1.createFile(generatePathName())
        def file2 = subdir1.createFile(generatePathName())
        def subdir2 = topDirOauthClient.createSubdirectory(generatePathName())
        def file3 = subdir2.createFile(generatePathName())

        // Only allow subowner rights to the directory and it's subpaths
        def subowner = namer.getRandomUuid()
        def rp = RolePermissions.parseSymbolic("rwx", false)
        def pathPermissions = new PathPermissions().setGroup(rp).setOther(rp).setOwner(rp)
        topDirOauthClient.setPermissions(pathPermissions, null, subowner)
        subdir1.setPermissions(pathPermissions, null, subowner)
        file1.setPermissions(pathPermissions, null, subowner)
        file2.setPermissions(pathPermissions, null, subowner)
        subdir2.setPermissions(pathPermissions, null, subowner)
        file3.setPermissions(pathPermissions, null, subowner)

        // Create file4 as super user (using shared key)
        def file4 = fsc.getDirectoryClient(topDirName).getSubdirectoryClient(subdir2.getObjectName())
            .createFile(generatePathName())

        // Create a user delegation sas that delegates an owner when creating files
        def subOwnerDirClient = getSasDirectoryClient(topDirOauthClient, subowner)

        def progress = new InMemoryAccessControlRecursiveChangeProgress()

        when:
        def result = subOwnerDirClient.removeAccessControlRecursiveWithResponse(
            new PathRemoveAccessControlRecursiveOptions(removeAccessControlEntries).setProgressHandler(progress), null, null)

        then:
        result.getValue().getCounters().getFailedChangesCount() == 1
        progress.failures.size() == 1
        progress.batchCounters.findIndexOf {counter -> counter.getFailedChangesCount() > 0} >= 0
        progress.cumulativeCounters.findIndexOf {counter -> counter.getFailedChangesCount() > 0} >= 0
        progress.failures[0].getName().contains(file4.getObjectName())
        !progress.failures[0].isDirectory()
        progress.failures[0].getErrorMessage()
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "V2020_02_10")
    def "Remove ACL recursive continue on failure"() {
        setup:
        fsc.getRootDirectoryClient().setAccessControlList(executeOnlyAccessControlEntries, null, null)
        String topDirName = generatePathName()

        // Create tree using AAD creds
        def topDirOauthClient = getOAuthServiceClient().getFileSystemClient(fsc.getFileSystemName())
            .getDirectoryClient(topDirName)
        topDirOauthClient.create()
        def subdir1 = topDirOauthClient.createSubdirectory(generatePathName())
        def file1 = subdir1.createFile(generatePathName())
        def file2 = subdir1.createFile(generatePathName())
        def subdir2 = topDirOauthClient.createSubdirectory(generatePathName())
        def file3 = subdir2.createFile(generatePathName())

        // Only allow subowner rights to the directory and it's subpaths
        def subowner = namer.getRandomUuid()
        def rp = RolePermissions.parseSymbolic("rwx", false)
        def pathPermissions = new PathPermissions().setGroup(rp).setOther(rp).setOwner(rp)
        topDirOauthClient.setPermissions(pathPermissions, null, subowner)
        subdir1.setPermissions(pathPermissions, null, subowner)
        file1.setPermissions(pathPermissions, null, subowner)
        file2.setPermissions(pathPermissions, null, subowner)
        subdir2.setPermissions(pathPermissions, null, subowner)
        file3.setPermissions(pathPermissions, null, subowner)

        // Create resources as super user (using shared key)
        def file4 = fsc.getDirectoryClient(topDirName).getSubdirectoryClient(subdir2.getObjectName())
            .createFile(generatePathName())
        def file5 = fsc.getDirectoryClient(topDirName).getSubdirectoryClient(subdir2.getObjectName())
            .createFile(generatePathName())
        def file6 = fsc.getDirectoryClient(topDirName).getSubdirectoryClient(subdir2.getObjectName())
            .createFile(generatePathName())
        def subdir3 = fsc.getDirectoryClient(topDirName).getSubdirectoryClient(subdir2.getObjectName())
            .createSubdirectory(generatePathName())

        // Create a user delegation sas that delegates an owner when creating files
        def subOwnerDirClient = getSasDirectoryClient(topDirOauthClient, subowner)

        when:
        def result = subOwnerDirClient.removeAccessControlRecursiveWithResponse(
            new PathRemoveAccessControlRecursiveOptions(removeAccessControlEntries).setContinueOnFailure(true), null, null)

        def batchFailures = result.getValue().getBatchFailures().stream().map( { failure -> failure.getName() } ).collect(Collectors.toList())

        then:
        result.getValue().getCounters().getChangedDirectoriesCount() == 3
        result.getValue().getCounters().getChangedFilesCount() == 3
        result.getValue().getCounters().getFailedChangesCount() == 4
        result.getValue().getContinuationToken() == null
        batchFailures.size() == 4
        batchFailures.contains(file4.getObjectPath())
        batchFailures.contains(file5.getObjectPath())
        batchFailures.contains(file6.getObjectPath())
        batchFailures.contains(subdir3.getObjectPath())
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "V2020_02_10")
    def "Remove ACL recursive continue on failure batch failures"() {
        setup:
        fsc.getRootDirectoryClient().setAccessControlList(executeOnlyAccessControlEntries, null, null)
        String topDirName = generatePathName()

        // Create tree using AAD creds
        def topDirOauthClient = getOAuthServiceClient().getFileSystemClient(fsc.getFileSystemName())
            .getDirectoryClient(topDirName)
        topDirOauthClient.create()
        def subdir1 = topDirOauthClient.createSubdirectory(generatePathName())
        def file1 = subdir1.createFile(generatePathName())
        def file2 = subdir1.createFile(generatePathName())
        def subdir2 = topDirOauthClient.createSubdirectory(generatePathName())
        def file3 = subdir2.createFile(generatePathName())

        // Only allow subowner rights to the directory and it's subpaths
        def subowner = namer.getRandomUuid()
        def rp = RolePermissions.parseSymbolic("rwx", false)
        def pathPermissions = new PathPermissions().setGroup(rp).setOther(rp).setOwner(rp)
        topDirOauthClient.setPermissions(pathPermissions, null, subowner)
        subdir1.setPermissions(pathPermissions, null, subowner)
        file1.setPermissions(pathPermissions, null, subowner)
        file2.setPermissions(pathPermissions, null, subowner)
        subdir2.setPermissions(pathPermissions, null, subowner)
        file3.setPermissions(pathPermissions, null, subowner)

        // Create resources as super user (using shared key)
        def file4 = fsc.getDirectoryClient(topDirName).getSubdirectoryClient(subdir2.getObjectName())
            .createFile(generatePathName())
        def file5 = fsc.getDirectoryClient(topDirName).getSubdirectoryClient(subdir2.getObjectName())
            .createFile(generatePathName())
        def file6 = fsc.getDirectoryClient(topDirName).getSubdirectoryClient(subdir2.getObjectName())
            .createFile(generatePathName())
        def subdir3 = fsc.getDirectoryClient(topDirName).getSubdirectoryClient(subdir2.getObjectName())
            .createSubdirectory(generatePathName())

        // Create a user delegation sas that delegates an owner when creating files
        def subOwnerDirClient = getSasDirectoryClient(topDirOauthClient, subowner)

        def progress = new InMemoryAccessControlRecursiveChangeProgress()

        when:
        def result = subOwnerDirClient.removeAccessControlRecursiveWithResponse(
            new PathRemoveAccessControlRecursiveOptions(removeAccessControlEntries).setContinueOnFailure(true).setBatchSize(2).setProgressHandler(progress), null, null)

        def batchFailures = result.getValue().getBatchFailures().stream().map( { failure -> failure.getName() } ).collect(Collectors.toList())

        then:
        result.getValue().getCounters().getChangedDirectoriesCount() == 3
        result.getValue().getCounters().getChangedFilesCount() == 3
        result.getValue().getCounters().getFailedChangesCount() == 4
        batchFailures.size() == progress.firstFailures.size()
        for (def f : progress.firstFailures) {
            assert batchFailures.contains(f.getName())
        }
        result.getValue().getContinuationToken() == null
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "V2020_02_10")
    def "Remove ACL recursive continue on failure batches resume"() {
        setup:
        fsc.getRootDirectoryClient().setAccessControlList(executeOnlyAccessControlEntries, null, null)
        String topDirName = generatePathName()

        // Create tree using AAD creds
        def topDirOauthClient = getOAuthServiceClient().getFileSystemClient(fsc.getFileSystemName())
            .getDirectoryClient(topDirName)
        topDirOauthClient.create()
        def subdir1 = topDirOauthClient.createSubdirectory(generatePathName())
        def file1 = subdir1.createFile(generatePathName())
        def file2 = subdir1.createFile(generatePathName())
        def subdir2 = topDirOauthClient.createSubdirectory(generatePathName())
        def file3 = subdir2.createFile(generatePathName())

        // Only allow subowner rights to the directory and it's subpaths
        def subowner = namer.getRandomUuid()
        def rp = RolePermissions.parseSymbolic("rwx", false)
        def pathPermissions = new PathPermissions().setGroup(rp).setOther(rp).setOwner(rp)
        topDirOauthClient.setPermissions(pathPermissions, null, subowner)
        subdir1.setPermissions(pathPermissions, null, subowner)
        file1.setPermissions(pathPermissions, null, subowner)
        file2.setPermissions(pathPermissions, null, subowner)
        subdir2.setPermissions(pathPermissions, null, subowner)
        file3.setPermissions(pathPermissions, null, subowner)

        // Create resources as super user (using shared key)
        def file4 = fsc.getDirectoryClient(topDirName).getSubdirectoryClient(subdir2.getObjectName())
            .createFile(generatePathName())
        def file5 = fsc.getDirectoryClient(topDirName).getSubdirectoryClient(subdir2.getObjectName())
            .createFile(generatePathName())
        def file6 = fsc.getDirectoryClient(topDirName).getSubdirectoryClient(subdir2.getObjectName())
            .createFile(generatePathName())
        def subdir3 = fsc.getDirectoryClient(topDirName).getSubdirectoryClient(subdir2.getObjectName())
            .createSubdirectory(generatePathName())

        // Create more files as app
        def file7 = subdir1.createFile(generatePathName())
        def file8 = subdir1.createFile(generatePathName())
        def subdir4 = topDirOauthClient.createSubdirectory(generatePathName())
        def file9 = subdir4.createFile(generatePathName())

        // Only allow subowner rights to the directory and it's subpaths
        file7.setPermissions(pathPermissions, null, subowner)
        file8.setPermissions(pathPermissions, null, subowner)
        subdir4.setPermissions(pathPermissions, null, subowner)
        file9.setPermissions(pathPermissions, null, subowner)

        // Create a user delegation sas that delegates an owner when creating files
        def subOwnerDirClient = getSasDirectoryClient(topDirOauthClient, subowner)

        def options = new PathRemoveAccessControlRecursiveOptions(removeAccessControlEntries)
            .setBatchSize(2).setContinueOnFailure(true).setMaxBatches(1)

        when:
        def intermediateResult = subOwnerDirClient.removeAccessControlRecursiveWithResponse(options, null, null)

        then:
        intermediateResult.getValue().getContinuationToken() != null

        when:
        options.setMaxBatches(null).setContinuationToken(intermediateResult.getValue().getContinuationToken())
        def result = subOwnerDirClient.removeAccessControlRecursiveWithResponse(options, null, null)

        then:
        (result.getValue().getCounters().getChangedDirectoriesCount() + intermediateResult.getValue().getCounters().getChangedDirectoriesCount()) == 4
        (result.getValue().getCounters().getChangedFilesCount() + intermediateResult.getValue().getCounters().getChangedFilesCount()) == 6
        (result.getValue().getCounters().getFailedChangesCount() + intermediateResult.getValue().getCounters().getFailedChangesCount()) == 4
        result.getValue().getContinuationToken() == null
    }

    def "Remove ACL recursive error"() {
        setup:
        fsc.getRootDirectoryClient().setAccessControlList(executeOnlyAccessControlEntries, null, null)

        String topDirName = generatePathName()
        def topDirOauthClient = getOAuthServiceClient().getFileSystemClient(fsc.getFileSystemName())
            .getDirectoryClient(topDirName)

        when:
        topDirOauthClient.removeAccessControlRecursiveWithResponse(
            new PathRemoveAccessControlRecursiveOptions(removeAccessControlEntries), null, null)

        then:
        def e = thrown(DataLakeAclChangeFailedException)
        e.getCause().class == DataLakeStorageException.class
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "V2020_02_10")
    @Unroll
    def "Remove ACL recursive error middle of batches"() {
        setup:
        setupStandardRecursiveAclTest()

        def options = new PathRemoveAccessControlRecursiveOptions(removeAccessControlEntries)
            .setBatchSize(2)

        // Mock a policy that will return an error on the call with the continuation token
        HttpPipelinePolicy mockPolicy = { HttpPipelineCallContext context, HttpPipelineNextPolicy next ->
            return context.getHttpRequest().getUrl().toString().contains("continuation") ? Mono.error(error) : next.process()
        }

        dc = getDirectoryClient(env.dataLakeAccount.credential, dc.getDirectoryUrl(), dc.getObjectPath(), mockPolicy)

        when:
        def result = dc.removeAccessControlRecursiveWithResponse(options, null, null).getValue()

        then:
        def e = thrown(DataLakeAclChangeFailedException)
        e.getCause().class == error.class

        where:
        error                                                                                                                               || _
        new IllegalArgumentException()                                                                                                      || _
        new DataLakeStorageException("error", getStubResponse(500, new HttpRequest(HttpMethod.PUT, new URL("https://www.fake.com"))), null) || _
    }

    def setupStandardRecursiveAclTest() {
        def subdir1 = dc.createSubdirectory(generatePathName())
        def file1 = subdir1.createFile(generatePathName())
        def file2 = subdir1.createFile(generatePathName())
        def subdir2 = dc.createSubdirectory(generatePathName())
        def file3 = subdir2.createFile(generatePathName())
        def file4 = dc.createFile(generatePathName())
    }

    static class InMemoryAccessControlRecursiveChangeProgress implements Consumer<Response<AccessControlChanges>> {

        List<AccessControlChangeFailure> failures = new ArrayList<>()
        List<AccessControlChangeCounters> batchCounters = new ArrayList<>()
        List<AccessControlChangeCounters> cumulativeCounters = new ArrayList<>()

        List<AccessControlChangeFailure> firstFailures = new ArrayList<>()
        boolean firstFailure = false

        @Override
        void accept(Response<AccessControlChanges> response) {
            if (!firstFailure && response.getValue().getBatchFailures().size() > 0) {
                firstFailures.addAll(response.getValue().getBatchFailures())
                firstFailure = true
            }
            failures.addAll(response.getValue().getBatchFailures())
            batchCounters.addAll(response.getValue().getBatchCounters())
            cumulativeCounters.addAll(response.getValue().getAggregateCounters())
        }
    }

    // set recursive acl error, with response
    // Test null or empty lists

    def "Get access control min"() {
        when:
        PathAccessControl pac = dc.getAccessControl()

        then:
        notThrown(DataLakeStorageException)
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
        thrown(DataLakeStorageException)

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
        dc.renameWithResponse(null, generatePathName(), null, null, null, null).getStatusCode() == 201
    }

    def "Rename with response"() {
        when:
        def resp = dc.renameWithResponse(null, generatePathName(), null, null, null, null)

        def renamedClient = resp.getValue()
        renamedClient.getProperties()

        then:
        notThrown(DataLakeStorageException)

        when:
        dc.getProperties()

        then:
        thrown(DataLakeStorageException)
    }

    def "Rename filesystem with response"() {
        setup:
        def newFileSystem = primaryDataLakeServiceClient.createFileSystem(generateFileSystemName())

        when:
        def resp = dc.renameWithResponse(newFileSystem.getFileSystemName(), generatePathName(), null, null, null, null)

        def renamedClient = resp.getValue()
        renamedClient.getProperties()

        then:
        notThrown(DataLakeStorageException)

        when:
        dc.getProperties()

        then:
        thrown(DataLakeStorageException)
    }

    def "Rename error"() {
        setup:
        dc = fsc.getDirectoryClient(generatePathName())

        when:
        dc.renameWithResponse(null, generatePathName(), null, null, null, null)

        then:
        thrown(DataLakeStorageException)
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
        dc.renameWithResponse(null, generatePathName(), drc, null, null, null).getStatusCode() == 201

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
        dc.renameWithResponse(null, generatePathName(), drc, null, null, null)

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
        dc.renameWithResponse(null, pathName, null, drc, null, null).getStatusCode() == 201

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
        dc.renameWithResponse(null, pathName, null, drc, null, null)

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
        properties.isDirectory()

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
        dc = fsc.getDirectoryClient(generatePathName())

        when:
        dc.getProperties()

        then:
        def ex = thrown(DataLakeStorageException)
        ex.getMessage().contains("BlobNotFound")
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
        dc = fsc.getDirectoryClient(generatePathName())

        when:
        dc.setHttpHeaders(null)

        then:
        thrown(DataLakeStorageException)
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
        dc = fsc.getDirectoryClient(generatePathName())

        when:
        dc.setMetadata(null)

        then:
        thrown(DataLakeStorageException)
    }

    def "Create file min"() {
        when:
        dc.createFile(generatePathName())

        then:
        notThrown(DataLakeStorageException)
    }

    @Unroll
    def "Create file overwrite"() {
        setup:
        def pathName = generatePathName()
        dc.createFile(pathName)

        when:
        def exceptionThrown = false
        try {
            dc.createFile(pathName, overwrite)
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
        def e = thrown(DataLakeStorageException)
        e.getResponse().getStatusCode() == 404
        e.getErrorCode() == BlobErrorCode.BLOB_NOT_FOUND.toString()
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
        thrown(DataLakeStorageException)

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
        dc.createSubdirectory(generatePathName())

        then:
        notThrown(DataLakeStorageException)
    }

    @Unroll
    def "Create sub dir overwrite"() {
        setup:
        def pathName = generatePathName()
        dc.createSubdirectory(pathName)

        when:
        def exceptionThrown = false
        try {
            dc.createSubdirectory(pathName, overwrite)
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
        thrown(DataLakeStorageException)
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
        thrown(DataLakeStorageException)

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
        def e = thrown(DataLakeStorageException)
        e.getResponse().getStatusCode() == 404
        e.getErrorCode() == BlobErrorCode.BLOB_NOT_FOUND.toString()
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
        notThrown(DataLakeStorageException)
    }

    @IgnoreIf( { getEnv().serviceVersion != null } )
    // This tests the policy is in the right place because if it were added per retry, it would be after the credentials and auth would fail because we changed a signed header.
    def "Per call policy"() {
        setup:
        def directoryClient = getDirectoryClient(env.dataLakeAccount.credential, fsc.getFileSystemUrl(), dc.getObjectPath(), getPerCallVersionPolicy())

        when: "blob endpoint"
        def response = directoryClient.getPropertiesWithResponse(null, null, null)

        then:
        notThrown(DataLakeStorageException)
        response.getHeaders().getValue("x-ms-version") == "2019-02-02"

        when: "dfs endpoint"
        response = directoryClient.getAccessControlWithResponse(false, null, null, null)

        then:
        notThrown(DataLakeStorageException)
        response.getHeaders().getValue("x-ms-version") == "2019-02-02"
    }

    def setupDirectoryForListing(DataLakeDirectoryClient client) {
        // Create 3 subdirs
        def foo = client.createSubdirectory("foo")
        def bar = client.createSubdirectory("bar")
        def baz = client.createSubdirectory("baz")

        // Create subdirs for foo
        foo.createSubdirectory("foo")
        foo.createSubdirectory("bar")

        // Creat subdirs for baz
        baz.createSubdirectory("foo").createSubdirectory("bar")
        baz.createSubdirectory("bar/foo")
    }

    def "List paths"() {
        setup:
        def dirName = generatePathName()
        def dir = fsc.getDirectoryClient(dirName)
        dir.create()
        setupDirectoryForListing(dir)

        when:
        def response = dir.listPaths().iterator()

        then:
        response.next().getName() == dirName + "/bar"
        response.next().getName() == dirName + "/baz"
        response.next().getName() == dirName + "/foo"
        !response.hasNext()
    }

    def "List paths recursive"() {
        setup:
        def dirName = generatePathName()
        def dir = fsc.getDirectoryClient(dirName)
        dir.create()
        setupDirectoryForListing(dir)

        when:
        def response = dir.listPaths(true, false, null, null).iterator()

        then:
        response.next().getName() == dirName + "/bar"
        response.next().getName() == dirName + "/baz"
        response.next().getName() == dirName + "/baz/bar"
        response.next().getName() == dirName + "/baz/bar/foo"
        response.next().getName() == dirName + "/baz/foo"
        response.next().getName() == dirName + "/baz/foo/bar"
        response.next().getName() == dirName + "/foo"
        response.next().getName() == dirName + "/foo/bar"
        response.next().getName() == dirName + "/foo/foo"
        !response.hasNext()
    }

    def "List paths upn"() {
        setup:
        def dirName = generatePathName()
        def dir = fsc.getDirectoryClient(dirName)
        dir.create()
        setupDirectoryForListing(dir)

        when:
        def response = dir.listPaths(false, true, null, null).iterator()

        then:
        def first = response.next()
        first.getName() == dirName + "/bar"
        first.getGroup()
        first.getOwner()
        response.next().getName() == dirName + "/baz"
        response.next().getName() == dirName + "/foo"
        !response.hasNext()
    }

    def "List paths max results"() {
        setup:
        def dirName = generatePathName()
        def dir = fsc.getDirectoryClient(dirName)
        dir.create()
        setupDirectoryForListing(dir)

        when:
        def response = dir.listPaths(false, false, 2, null).iterableByPage().iterator().next()

        then:
        response.getValue().get(0).getName() == dirName + "/bar"
        response.getValue().get(1).getName() == dirName + "/baz"
        response.getValue().size() == 2
    }

    def "List paths max results by page"() {
        setup:
        def dirName = generatePathName()
        def dir = fsc.getDirectoryClient(dirName)
        dir.create()
        setupDirectoryForListing(dir)

        expect:
        for (def page : dir.listPaths(false, false, null, null).iterableByPage(2)) {
            assert page.getValue().size() <= 2
        }
    }

    def "List paths error"() {
        def dirName = generatePathName()
        def dir = fsc.getDirectoryClient(dirName)

        when:
        def response = dir.listPaths().iterator()

        then:
        thrown(DataLakeStorageException)
    }

    @Unroll
    def "Get file and subdirectory client"() {
        setup:
        def dirName = generatePathName()
        def subPath = generatePathName()
        dc = fsc.getDirectoryClient(resourcePrefix +  dirName)

        when:
        def fileClient = dc.getFileClient(subResourcePrefix + subPath)

        then:
        notThrown(IllegalArgumentException)
        fileClient.getFilePath() == Utility.urlDecode(resourcePrefix) + dirName + "/" + Utility.urlDecode(subResourcePrefix) + subPath

        when:
        def subDirectoryClient = dc.getSubdirectoryClient(subResourcePrefix + subPath)

        then:
        notThrown(IllegalArgumentException)
        subDirectoryClient.getDirectoryPath() == Utility.urlDecode(resourcePrefix) + dirName + "/" + Utility.urlDecode(subResourcePrefix) + subPath

        where:
        resourcePrefix          | subResourcePrefix         || _
        ""                      | ""                        || _
        Utility.urlEncode("%")  | ""                        || _ // Resource has special character
        ""                      | Utility.urlEncode("%")    || _ // Sub resource has special character
        Utility.urlEncode("%")  | Utility.urlEncode("%")    || _
    }

    def "File in root directory rename"() {
        setup:
        def oldName = generatePathName()
        def renamedName = generatePathName()
        dc = fsc.getRootDirectoryClient()
        // Create file in root directory
        def file = dc.createFile(oldName)

        when:
        def renamedFile = file.rename(null, renamedName)

        then:
        renamedFile.getObjectPath() == renamedName
        renamedFile.getProperties().getETag() == renamedFile.setAccessControlList(pathAccessControlEntries, group, owner).getETag()
    }

    def "Directory in root directory rename"() {
        setup:
        def oldName = generatePathName()
        def renamedName = generatePathName()
        dc = fsc.getRootDirectoryClient()
        // Create dir in root directory
        def dir = dc.createSubdirectory(oldName)

        when:
        def renamedDir = dir.rename(null, renamedName)

        then:
        renamedDir.getObjectPath() == renamedName
        renamedDir.getProperties().getETag() == renamedDir.setAccessControlList(pathAccessControlEntries, group, owner).getETag()
    }
}
