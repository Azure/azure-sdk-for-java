// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.spock

import com.azure.storage.common.Constants
import com.azure.storage.common.credentials.SharedKeyCredential
import com.azure.storage.file.DirectoryClient
import com.azure.storage.file.FileClient
import com.azure.storage.file.FileSmbProperties
import com.azure.storage.file.ShareClient
import com.azure.storage.file.models.FileHTTPHeaders
import com.azure.storage.file.models.FileProperties
import com.azure.storage.file.models.NtfsFileAttributes
import com.azure.storage.file.models.StorageErrorCode
import com.azure.storage.file.models.StorageException
import spock.lang.Ignore
import spock.lang.Unroll

import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset

class DirectoryAPITests extends APISpec {
    DirectoryClient primaryDirectoryClient
    ShareClient shareClient
    def directoryPath
    def shareName
    static def testMetadata
    static def smbProperties
    static def filePermission = "O:S-1-5-21-2127521184-1604012920-1887927527-21560751G:S-1-5-21-2127521184-1604012920-1887927527-513D:AI(A;;FA;;;SY)(A;;FA;;;BA)(A;;0x1200a9;;;S-1-5-21-397955417-626881126-188441444-3053964)S:NO_ACCESS_CONTROL"

    def setup() {
        shareName = testResourceName.randomName(methodName, 60)
        directoryPath = testResourceName.randomName(methodName, 60)
        shareClient = shareBuilderHelper(interceptorManager, shareName).buildClient()
        shareClient.create()
        primaryDirectoryClient = directoryBuilderHelper(interceptorManager, shareName, directoryPath).buildDirectoryClient()
        testMetadata = Collections.singletonMap("testmetadata", "value")
        smbProperties = new FileSmbProperties()
            .ntfsFileAttributes(EnumSet.of(NtfsFileAttributes.NORMAL))
    }

    def "Get directory URL"() {
        given:
        def accountName = SharedKeyCredential.fromConnectionString(connectionString).accountName()
        def expectURL = String.format("https://%s.file.core.windows.net", accountName)

        when:
        def directoryURL = primaryDirectoryClient.getDirectoryUrl().toString()

        then:
        expectURL == directoryURL
    }

    def "Get sub directory client"() {
        given:
        def subDirectoryClient = primaryDirectoryClient.getSubDirectoryClient("testSubDirectory")

        expect:
        subDirectoryClient instanceof DirectoryClient
    }

    def "Get file client"() {
        given:
        def fileClient = primaryDirectoryClient.getFileClient("testFile")

        expect:
        fileClient instanceof FileClient
    }

    def "Create directory"() {
        expect:
        FileTestHelper.assertResponseStatusCode(primaryDirectoryClient.createWithResponse(null, null, null, null, null), 201)
    }

    def "Create directory error"() {
        given:
        def testShareName = testResourceName.randomName(methodName, 60)

        when:
        directoryBuilderHelper(interceptorManager, testShareName, directoryPath).buildDirectoryClient().create()

        then:
        def e = thrown(StorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 404, StorageErrorCode.SHARE_NOT_FOUND)
    }

    def "Create directory with metadata"() {
        expect:
        FileTestHelper.assertResponseStatusCode(primaryDirectoryClient.createWithResponse(null, null, testMetadata, null, null), 201)
    }

    def "Create directory error with metadata"() {
        given:
        def errorMetadata = Collections.singletonMap("testMeta", "value")

        when:
        primaryDirectoryClient.createWithResponse(null, null, errorMetadata, null, null)

        then:
        def e = thrown(StorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 403, StorageErrorCode.AUTHENTICATION_FAILED)
    }

    def "Create directory with file permission"() {
        when:
        def resp = primaryDirectoryClient.createWithResponse(null, filePermission, null, null, null)

        then:
        FileTestHelper.assertResponseStatusCode(resp, 201)
        resp.value().smbProperties()
        resp.value().smbProperties().filePermissionKey()
        resp.value().smbProperties().ntfsFileAttributes()
        resp.value().smbProperties().fileLastWriteTime()
        resp.value().smbProperties().fileCreationTime()
        resp.value().smbProperties().fileChangeTime()
        resp.value().smbProperties().parentId()
        resp.value().smbProperties().fileId()
    }

    def "Create directory with file permission key"() {
        setup:
        def filePermissionKey = shareClient.createPermission(filePermission)
        smbProperties.fileCreationTime(getUTCNow())
            .fileLastWriteTime(getUTCNow())
            .filePermissionKey(filePermissionKey)
        when:
        def resp = primaryDirectoryClient.createWithResponse(smbProperties, null, null, null, null)

        then:
        FileTestHelper.assertResponseStatusCode(resp, 201)
        resp.value().smbProperties()
        resp.value().smbProperties().filePermissionKey()
        resp.value().smbProperties().ntfsFileAttributes()
        resp.value().smbProperties().fileLastWriteTime()
        resp.value().smbProperties().fileCreationTime()
        resp.value().smbProperties().fileChangeTime()
        resp.value().smbProperties().parentId()
        resp.value().smbProperties().fileId()
    }

    @Unroll
    def "Create directory permission and key error"() {
        when:
        FileSmbProperties properties = new FileSmbProperties().filePermissionKey(filePermissionKey)
        primaryDirectoryClient.createWithResponse(properties, permission, null, null, null)
        then:
        thrown(IllegalArgumentException)
        where:
        filePermissionKey   | permission
        "filePermissionKey" | filePermission
        null                | new String(FileTestHelper.getRandomBuffer(9 * Constants.KB))
    }

    def "Delete directory"() {
        given:
        primaryDirectoryClient.create()

        expect:
        FileTestHelper.assertResponseStatusCode(primaryDirectoryClient.deleteWithResponse(null, null), 202)
    }

    def "Delete directory error"() {
        when:
        primaryDirectoryClient.delete()

        then:
        def e = thrown(StorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 404, StorageErrorCode.RESOURCE_NOT_FOUND)
    }

    def "Get properties"() {
        given:
        primaryDirectoryClient.create()
        def resp = primaryDirectoryClient.getPropertiesWithResponse(null, null)

        expect:
        FileTestHelper.assertResponseStatusCode(resp, 200)
        resp.value().eTag()
        resp.value().smbProperties()
        resp.value().smbProperties().filePermissionKey()
        resp.value().smbProperties().ntfsFileAttributes()
        resp.value().smbProperties().fileLastWriteTime()
        resp.value().smbProperties().fileCreationTime()
        resp.value().smbProperties().fileChangeTime()
        resp.value().smbProperties().parentId()
        resp.value().smbProperties().fileId()
    }

    def "Get properties error"() {
        when:
        primaryDirectoryClient.getPropertiesWithResponse(null, null)

        then:
        def e = thrown(StorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 404, StorageErrorCode.RESOURCE_NOT_FOUND)
    }

    def "Set properties file permission"() {
        given:
        primaryDirectoryClient.create()
        def resp = primaryDirectoryClient.setPropertiesWithResponse(null, filePermission, null, null)
        expect:
        FileTestHelper.assertResponseStatusCode(resp, 200)
        resp.value().smbProperties()
        resp.value().smbProperties().filePermissionKey()
        resp.value().smbProperties().ntfsFileAttributes()
        resp.value().smbProperties().fileLastWriteTime()
        resp.value().smbProperties().fileCreationTime()
        resp.value().smbProperties().fileChangeTime()
        resp.value().smbProperties().parentId()
        resp.value().smbProperties().fileId()
    }

    def "Set properties file permission key"() {
        given:
        def filePermissionKey = shareClient.createPermission(filePermission)
        smbProperties.fileCreationTime(getUTCNow())
            .fileLastWriteTime(getUTCNow())
            .filePermissionKey(filePermissionKey)
        primaryDirectoryClient.create()
        def resp = primaryDirectoryClient.setPropertiesWithResponse(smbProperties, null, null, null)

        expect:
        FileTestHelper.assertResponseStatusCode(resp, 200)
        resp.value().smbProperties()
        resp.value().smbProperties().filePermissionKey()
        resp.value().smbProperties().ntfsFileAttributes()
        resp.value().smbProperties().fileLastWriteTime()
        resp.value().smbProperties().fileCreationTime()
        resp.value().smbProperties().fileChangeTime()
        resp.value().smbProperties().parentId()
        resp.value().smbProperties().fileId()
    }

    @Unroll
    def "Set properties error"() {
        when:
        FileSmbProperties properties = new FileSmbProperties().filePermissionKey(filePermissionKey)
        primaryDirectoryClient.create()
        primaryDirectoryClient.setPropertiesWithResponse(properties, permission, null, null)

        then:
        thrown(IllegalArgumentException)

        where:
        filePermissionKey   | permission
        "filePermissionKey" | filePermission
        null                | new String(FileTestHelper.getRandomBuffer(9 * Constants.KB))
    }

    def "Set metadata"() {
        given:
        primaryDirectoryClient.createWithResponse(null, null, testMetadata, null, null)
        def updatedMetadata = Collections.singletonMap("update", "value")

        when:
        def getPropertiesBefore = primaryDirectoryClient.getProperties()
        def setPropertiesResponse = primaryDirectoryClient.setMetadataWithResponse(updatedMetadata, null, null)
        def getPropertiesAfter = primaryDirectoryClient.getProperties()

        then:
        testMetadata == getPropertiesBefore.metadata()
        FileTestHelper.assertResponseStatusCode(setPropertiesResponse, 200)
        updatedMetadata == getPropertiesAfter.metadata()
    }

    def "Set metadata error"() {
        given:
        primaryDirectoryClient.create()
        def errorMetadata = Collections.singletonMap("", "value")

        when:
        primaryDirectoryClient.setMetadata(errorMetadata)

        then:
        def e = thrown(StorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 400, StorageErrorCode.EMPTY_METADATA_KEY)
    }

    @Unroll
    def "List files and directories"() {
        given:
        primaryDirectoryClient.create()

        for (def expectedFile : expectedFiles) {
            primaryDirectoryClient.createFile(expectedFile, 2)
        }

        for (def expectedDirectory : expectedDirectories) {
            primaryDirectoryClient.createSubDirectory(expectedDirectory)
        }

        when:
        def foundFiles = [] as Set
        def foundDirectories = [] as Set
        for (def fileRef : primaryDirectoryClient.listFilesAndDirectories()) {
            if (fileRef.isDirectory()) {
                foundDirectories << fileRef.name()
            } else {
                foundFiles << fileRef.name()
            }
        }

        then:
        expectedFiles == foundFiles
        expectedDirectories == foundDirectories

        where:
        expectedFiles          | expectedDirectories
        ["a", "b", "c"] as Set | ["d", "e"] as Set
        ["a", "c", "e"] as Set | ["b", "d"] as Set
    }

    /**
     * The listing hierarchy:
     * share -> dir -> listOp0 (dir) -> listOp3 (file)
     *                               -> listOp4 (file)
     *              -> listOp1 (dir) -> listOp5 (file)
     *                               -> listOp6 (file)
     *              -> listOp2 (file)
     */
    @Unroll
    def "List files and directories args"() {
        given:
        primaryDirectoryClient.create()
        def nameList = new LinkedList()
        def dirPrefix = testResourceName.randomName(methodName, 60)
        for (int i = 0; i < 2; i++) {
            def subDirClient = primaryDirectoryClient.getSubDirectoryClient(dirPrefix + i)
            subDirClient.create()
            for (int j = 0; j < 2; j++) {
                def num = i * 2 + j + 3
                subDirClient.createFile(dirPrefix + num, 1024)
            }
        }
        primaryDirectoryClient.createFile(dirPrefix + 2, 1024)
        for (int i = 0; i < 3; i++) {
            nameList.add(dirPrefix + i)
        }

        when:
        def fileRefIter = primaryDirectoryClient.listFilesAndDirectories(prefix, maxResults, null, null).iterator()

        then:
        for (int i = 0; i < numOfResults; i++) {
            Objects.equals(nameList.pop(), fileRefIter.next().name())
        }
        !fileRefIter.hasNext()

        where:
        prefix                                         | maxResults | numOfResults
        null                                           | null       | 3
        "directoryapitestslistfilesanddirectoriesargs" | 1          | 3
        "noOp"                                         | 3          | 0
    }

    @Unroll
    def "List handles"() {
        given:
        primaryDirectoryClient.create()

        expect:
        primaryDirectoryClient.listHandles(maxResult, recursive, null, null).size() == 0

        where:
        maxResult | recursive
        2         | true
        null      | false
    }

    def "List handles error"() {
        when:
        primaryDirectoryClient.listHandles(null, true, null, null).iterator().hasNext()

        then:
        def e = thrown(StorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 404, StorageErrorCode.RESOURCE_NOT_FOUND)
    }

    @Ignore
    def "Force close handles"() {
        // TODO: Need to find a way of mocking handles.
    }

    def "Force close handles error"() {
        given:
        primaryDirectoryClient.create()

        when:
        primaryDirectoryClient.forceCloseHandles("handleId", true, null, null).iterator().hasNext()

        then:
        def e = thrown(StorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 400, StorageErrorCode.INVALID_HEADER_VALUE)
    }

    def "Create sub directory"() {
        given:
        primaryDirectoryClient.create()

        expect:
        FileTestHelper.assertResponseStatusCode(
            primaryDirectoryClient.createSubDirectoryWithResponse("testCreateSubDirectory", null, null, null, null, null), 201)
    }

    def "Create sub directory invalid name"() {
        given:
        primaryDirectoryClient.create()

        when:
        primaryDirectoryClient.createSubDirectory("test/subdirectory")

        then:
        def e = thrown(StorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 404, StorageErrorCode.PARENT_NOT_FOUND)
    }

    def "Create sub directory metadata"() {
        given:
        primaryDirectoryClient.create()

        expect:
        FileTestHelper.assertResponseStatusCode(
            primaryDirectoryClient.createSubDirectoryWithResponse("testCreateSubDirectory", null, null, testMetadata, null, null), 201)
    }

    def "Create sub directory metadata error"() {
        given:
        primaryDirectoryClient.create()

        when:
        primaryDirectoryClient.createSubDirectoryWithResponse("testsubdirectory", null, null, Collections.singletonMap("", "value"), null, null)

        then:
        def e = thrown(StorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 400, StorageErrorCode.EMPTY_METADATA_KEY)
    }

    def "Create sub directory file permission"() {
        given:
        primaryDirectoryClient.create()
        expect:
        FileTestHelper.assertResponseStatusCode(
            primaryDirectoryClient.createSubDirectoryWithResponse("testCreateSubDirectory", null, filePermission, null, null, null), 201)
    }

    def "Create sub directory file permission key"() {
        given:
        primaryDirectoryClient.create()
        def filePermissionKey = shareClient.createPermission(filePermission)
        smbProperties.fileCreationTime(getUTCNow())
            .fileLastWriteTime(getUTCNow())
            .filePermissionKey(filePermissionKey)
        expect:
        FileTestHelper.assertResponseStatusCode(
            primaryDirectoryClient.createSubDirectoryWithResponse("testCreateSubDirectory", smbProperties, null, null, null, null), 201)
    }

    def "Delete sub directory"() {
        given:
        def subDirectoryName = "testSubCreateDirectory"
        primaryDirectoryClient.create()
        primaryDirectoryClient.createSubDirectory(subDirectoryName)

        expect:
        FileTestHelper.assertResponseStatusCode(primaryDirectoryClient.deleteSubDirectoryWithResponse(subDirectoryName, null, null), 202)
    }

    def "Delete sub directory error"() {
        given:
        primaryDirectoryClient.create()

        when:
        primaryDirectoryClient.deleteSubDirectory("testsubdirectory")

        then:
        def e = thrown(StorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 404, StorageErrorCode.RESOURCE_NOT_FOUND)
    }


    def "Create file"() {
        given:
        primaryDirectoryClient.create()

        expect:
        FileTestHelper.assertResponseStatusCode(
            primaryDirectoryClient.createFileWithResponse("testCreateFile", 1024, null, null, null, null, null, null), 201)
    }

    @Unroll
    def "Create file invalid args"() {
        given:
        primaryDirectoryClient.create()

        when:
        primaryDirectoryClient.createFileWithResponse(fileName, maxSize, null, null, null, null, null, null)
        then:
        def e = thrown(StorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, statusCode, errMsg)

        where:
        fileName    | maxSize | statusCode | errMsg
        "testfile:" | 1024    | 400        | StorageErrorCode.INVALID_RESOURCE_NAME
        "fileName"  | -1      | 400        | StorageErrorCode.OUT_OF_RANGE_INPUT

    }

    def "Create file maxOverload"() {
        given:
        primaryDirectoryClient.create()
        FileHTTPHeaders httpHeaders = new FileHTTPHeaders()
            .fileContentType("txt")
        smbProperties.fileCreationTime(getUTCNow())
            .fileLastWriteTime(getUTCNow())

        expect:
        FileTestHelper.assertResponseStatusCode(
            primaryDirectoryClient.createFileWithResponse("testCreateFile", 1024, httpHeaders, smbProperties, filePermission, testMetadata, null, null), 201)
    }

    @Unroll
    def "Create file maxOverload invalid args"() {
        given:
        primaryDirectoryClient.create()

        when:
        primaryDirectoryClient.createFileWithResponse(fileName, maxSize, httpHeaders, null, null, metadata, null, null)

        then:
        def e = thrown(StorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 400, errMsg)

        where:
        fileName    | maxSize | httpHeaders                                       | metadata                              | errMsg
        "testfile:" | 1024    | null                                              | testMetadata                          | StorageErrorCode.INVALID_RESOURCE_NAME
        "fileName"  | -1      | null                                              | testMetadata                          | StorageErrorCode.OUT_OF_RANGE_INPUT
        "fileName"  | 1024    | new FileHTTPHeaders().fileContentMD5(new byte[0]) | testMetadata                          | StorageErrorCode.INVALID_HEADER_VALUE
        "fileName"  | 1024    | null                                              | Collections.singletonMap("", "value") | StorageErrorCode.EMPTY_METADATA_KEY

    }

    def "Delete file"() {
        given:
        def fileName = "testCreateFile"
        primaryDirectoryClient.create()
        primaryDirectoryClient.createFile(fileName, 1024)

        expect:
        FileTestHelper.assertResponseStatusCode(
            primaryDirectoryClient.deleteFileWithResponse(fileName, null, null), 202)
    }

    def "Delete file error"() {
        given:
        primaryDirectoryClient.create()

        when:
        primaryDirectoryClient.deleteFileWithResponse("testfile", null, null)

        then:
        def e = thrown(StorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 404, StorageErrorCode.RESOURCE_NOT_FOUND)
    }

    def "Get snapshot id"() {
        given:
        def snapshot = OffsetDateTime.of(LocalDateTime.of(2000, 1, 1,
            1, 1), ZoneOffset.UTC).toString()

        when:
        def shareSnapshotClient = directoryBuilderHelper(interceptorManager, shareName, directoryPath).snapshot(snapshot).buildDirectoryClient()

        then:
        snapshot == shareSnapshotClient.getShareSnapshotId()
    }

}
