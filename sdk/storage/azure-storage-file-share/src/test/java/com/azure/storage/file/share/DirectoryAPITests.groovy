// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share

import com.azure.storage.common.StorageSharedKeyCredential
import com.azure.storage.common.implementation.Constants
import com.azure.storage.file.share.models.ShareErrorCode
import com.azure.storage.file.share.models.ShareFileHttpHeaders
import com.azure.storage.file.share.models.NtfsFileAttributes
import com.azure.storage.file.share.models.ShareSnapshotInfo
import com.azure.storage.file.share.models.ShareStorageException
import spock.lang.Unroll

import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset

class DirectoryAPITests extends APISpec {
    ShareDirectoryClient primaryDirectoryClient
    ShareClient shareClient
    String directoryPath
    String shareName
    static Map<String, String> testMetadata
    static FileSmbProperties smbProperties
    static def filePermission = "O:S-1-5-21-2127521184-1604012920-1887927527-21560751G:S-1-5-21-2127521184-1604012920-1887927527-513D:AI(A;;FA;;;SY)(A;;FA;;;BA)(A;;0x1200a9;;;S-1-5-21-397955417-626881126-188441444-3053964)S:NO_ACCESS_CONTROL"

    def setup() {
        shareName = testResourceName.randomName(methodName, 60)
        directoryPath = testResourceName.randomName(methodName, 60)
        shareClient = shareBuilderHelper(interceptorManager, shareName).buildClient()
        shareClient.create()
        primaryDirectoryClient = directoryBuilderHelper(interceptorManager, shareName, directoryPath).buildDirectoryClient()
        testMetadata = Collections.singletonMap("testmetadata", "value")
        smbProperties = new FileSmbProperties().setNtfsFileAttributes(EnumSet.<NtfsFileAttributes>of(NtfsFileAttributes.NORMAL))
    }

    def "Get directory URL"() {
        given:
        def accountName = StorageSharedKeyCredential.fromConnectionString(connectionString).getAccountName()
        def expectURL = String.format("https://%s.file.core.windows.net/%s/%s", accountName, shareName, directoryPath)

        when:
        def directoryURL = primaryDirectoryClient.getDirectoryUrl()

        then:
        expectURL == directoryURL
    }

    def "Get share snapshot URL"() {
        given:
        def accountName = StorageSharedKeyCredential.fromConnectionString(connectionString).getAccountName()
        def expectURL = String.format("https://%s.file.core.windows.net/%s/%s", accountName, shareName, directoryPath)

        when:
        ShareSnapshotInfo shareSnapshotInfo = shareClient.createSnapshot()
        expectURL = expectURL + "?snapshot=" + shareSnapshotInfo.getSnapshot()
        ShareDirectoryClient newDirClient = shareBuilderHelper(interceptorManager, shareName).snapshot(shareSnapshotInfo.getSnapshot())
            .buildClient().getDirectoryClient(directoryPath)
        def directoryURL = newDirClient.getDirectoryUrl()

        then:
        expectURL == directoryURL
    }

    def "Get sub directory client"() {
        given:
        def subDirectoryClient = primaryDirectoryClient.getSubdirectoryClient("testSubDirectory")

        expect:
        subDirectoryClient instanceof ShareDirectoryClient
    }

    def "Get file client"() {
        given:
        def fileClient = primaryDirectoryClient.getFileClient("testFile")

        expect:
        fileClient instanceof ShareFileClient
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
        def e = thrown(ShareStorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 404, ShareErrorCode.SHARE_NOT_FOUND)
    }

    def "Create directory with metadata"() {
        expect:
        FileTestHelper.assertResponseStatusCode(primaryDirectoryClient.createWithResponse(null, null, testMetadata, null, null), 201)
    }

    def "Create directory with file permission"() {
        when:
        def resp = primaryDirectoryClient.createWithResponse(null, filePermission, null, null, null)

        then:
        FileTestHelper.assertResponseStatusCode(resp, 201)
        resp.getValue().getSmbProperties()
        resp.getValue().getSmbProperties().getFilePermissionKey()
        resp.getValue().getSmbProperties().getNtfsFileAttributes()
        resp.getValue().getSmbProperties().getFileLastWriteTime()
        resp.getValue().getSmbProperties().getFileCreationTime()
        resp.getValue().getSmbProperties().getFileChangeTime()
        resp.getValue().getSmbProperties().getParentId()
        resp.getValue().getSmbProperties().getFileId()
    }

    def "Create directory with file permission key"() {
        setup:
        def filePermissionKey = shareClient.createPermission(filePermission)
        smbProperties.setFileCreationTime(getUTCNow())
            .setFileLastWriteTime(getUTCNow())
            .setFilePermissionKey(filePermissionKey)
        when:
        def resp = primaryDirectoryClient.createWithResponse(smbProperties, null, null, null, null)

        then:
        FileTestHelper.assertResponseStatusCode(resp, 201)
        resp.getValue().getSmbProperties()
        resp.getValue().getSmbProperties().getFilePermissionKey()
        resp.getValue().getSmbProperties().getNtfsFileAttributes()
        resp.getValue().getSmbProperties().getFileLastWriteTime()
        resp.getValue().getSmbProperties().getFileCreationTime()
        resp.getValue().getSmbProperties().getFileChangeTime()
        resp.getValue().getSmbProperties().getParentId()
        resp.getValue().getSmbProperties().getFileId()
    }

    @Unroll
    def "Create directory permission and key error"() {
        when:
        FileSmbProperties properties = new FileSmbProperties().setFilePermissionKey(filePermissionKey)
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
        def e = thrown(ShareStorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 404, ShareErrorCode.RESOURCE_NOT_FOUND)
    }

    def "Get properties"() {
        given:
        primaryDirectoryClient.create()
        def resp = primaryDirectoryClient.getPropertiesWithResponse(null, null)

        expect:
        FileTestHelper.assertResponseStatusCode(resp, 200)
        resp.getValue().getETag()
        resp.getValue().getSmbProperties()
        resp.getValue().getSmbProperties().getFilePermissionKey()
        resp.getValue().getSmbProperties().getNtfsFileAttributes()
        resp.getValue().getSmbProperties().getFileLastWriteTime()
        resp.getValue().getSmbProperties().getFileCreationTime()
        resp.getValue().getSmbProperties().getFileChangeTime()
        resp.getValue().getSmbProperties().getParentId()
        resp.getValue().getSmbProperties().getFileId()
    }

    def "Get properties error"() {
        when:
        primaryDirectoryClient.getPropertiesWithResponse(null, null)

        then:
        def e = thrown(ShareStorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 404, ShareErrorCode.RESOURCE_NOT_FOUND)
    }

    def "Set properties file permission"() {
        given:
        primaryDirectoryClient.create()
        def resp = primaryDirectoryClient.setPropertiesWithResponse(null, filePermission, null, null)
        expect:
        FileTestHelper.assertResponseStatusCode(resp, 200)
        resp.getValue().getSmbProperties()
        resp.getValue().getSmbProperties().getFilePermissionKey()
        resp.getValue().getSmbProperties().getNtfsFileAttributes()
        resp.getValue().getSmbProperties().getFileLastWriteTime()
        resp.getValue().getSmbProperties().getFileCreationTime()
        resp.getValue().getSmbProperties().getFileChangeTime()
        resp.getValue().getSmbProperties().getParentId()
        resp.getValue().getSmbProperties().getFileId()
    }

    def "Set properties file permission key"() {
        given:
        def filePermissionKey = shareClient.createPermission(filePermission)
        smbProperties.setFileCreationTime(getUTCNow())
            .setFileLastWriteTime(getUTCNow())
            .setFilePermissionKey(filePermissionKey)
        primaryDirectoryClient.create()
        def resp = primaryDirectoryClient.setPropertiesWithResponse(smbProperties, null, null, null)

        expect:
        FileTestHelper.assertResponseStatusCode(resp, 200)
        resp.getValue().getSmbProperties()
        resp.getValue().getSmbProperties().getFilePermissionKey()
        resp.getValue().getSmbProperties().getNtfsFileAttributes()
        resp.getValue().getSmbProperties().getFileLastWriteTime()
        resp.getValue().getSmbProperties().getFileCreationTime()
        resp.getValue().getSmbProperties().getFileChangeTime()
        resp.getValue().getSmbProperties().getParentId()
        resp.getValue().getSmbProperties().getFileId()
    }

    @Unroll
    def "Set properties error"() {
        when:
        FileSmbProperties properties = new FileSmbProperties().setFilePermissionKey(filePermissionKey)
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
        testMetadata == getPropertiesBefore.getMetadata()
        FileTestHelper.assertResponseStatusCode(setPropertiesResponse, 200)
        updatedMetadata == getPropertiesAfter.getMetadata()
    }

    def "Set metadata error"() {
        given:
        primaryDirectoryClient.create()
        def errorMetadata = Collections.singletonMap("", "value")

        when:
        primaryDirectoryClient.setMetadata(errorMetadata)

        then:
        def e = thrown(ShareStorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 400, ShareErrorCode.EMPTY_METADATA_KEY)
    }

    @Unroll
    def "List files and directories"() {
        given:
        primaryDirectoryClient.create()

        for (def expectedFile : expectedFiles) {
            primaryDirectoryClient.createFile(expectedFile, 2)
        }

        for (def expectedDirectory : expectedDirectories) {
            primaryDirectoryClient.createSubdirectory(expectedDirectory)
        }

        when:
        def foundFiles = [] as Set
        def foundDirectories = [] as Set
        for (def fileRef : primaryDirectoryClient.listFilesAndDirectories()) {
            if (fileRef.isDirectory()) {
                foundDirectories << fileRef.getName()
            } else {
                foundFiles << fileRef.getName()
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
            def subDirClient = primaryDirectoryClient.getSubdirectoryClient(dirPrefix + i)
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
            Objects.equals(nameList.pop(), fileRefIter.next().getName())
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
        def e = thrown(ShareStorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 404, ShareErrorCode.RESOURCE_NOT_FOUND)
    }

    def "Force close handle min"() {
        given:
        primaryDirectoryClient.create()

        when:
        def handlesClosedInfo = primaryDirectoryClient.forceCloseHandle("1")

        then:
        handlesClosedInfo.getClosedHandles() == 0
        notThrown(ShareStorageException)
    }

    def "Force close handle invalid handle ID"() {
        given:
        primaryDirectoryClient.create()

        when:
        primaryDirectoryClient.forceCloseHandle("invalidHandleId")

        then:
        thrown(ShareStorageException)
    }

    def "Force close all handles min"() {
        given:
        primaryDirectoryClient.create()

        when:
        def handlesClosedInfo  = primaryDirectoryClient.forceCloseAllHandles(false, null, null)

        then:
        notThrown(ShareStorageException)
        handlesClosedInfo.getClosedHandles() == 0
    }

    def "Create sub directory"() {
        given:
        primaryDirectoryClient.create()

        expect:
        FileTestHelper.assertResponseStatusCode(
            primaryDirectoryClient.createSubdirectoryWithResponse("testCreateSubDirectory", null, null, null, null, null), 201)
    }

    def "Create sub directory invalid name"() {
        given:
        primaryDirectoryClient.create()

        when:
        primaryDirectoryClient.createSubdirectory("test/subdirectory")

        then:
        def e = thrown(ShareStorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 404, ShareErrorCode.PARENT_NOT_FOUND)
    }

    def "Create sub directory metadata"() {
        given:
        primaryDirectoryClient.create()

        expect:
        FileTestHelper.assertResponseStatusCode(
            primaryDirectoryClient.createSubdirectoryWithResponse("testCreateSubDirectory", null, null, testMetadata, null, null), 201)
    }

    def "Create sub directory metadata error"() {
        given:
        primaryDirectoryClient.create()

        when:
        primaryDirectoryClient.createSubdirectoryWithResponse("testsubdirectory", null, null, Collections.singletonMap("", "value"), null, null)

        then:
        def e = thrown(ShareStorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 400, ShareErrorCode.EMPTY_METADATA_KEY)
    }

    def "Create sub directory file permission"() {
        given:
        primaryDirectoryClient.create()
        expect:
        FileTestHelper.assertResponseStatusCode(
            primaryDirectoryClient.createSubdirectoryWithResponse("testCreateSubDirectory", null, filePermission, null, null, null), 201)
    }

    def "Create sub directory file permission key"() {
        given:
        primaryDirectoryClient.create()
        def filePermissionKey = shareClient.createPermission(filePermission)
        smbProperties.setFileCreationTime(getUTCNow())
            .setFileLastWriteTime(getUTCNow())
            .setFilePermissionKey(filePermissionKey)
        expect:
        FileTestHelper.assertResponseStatusCode(
            primaryDirectoryClient.createSubdirectoryWithResponse("testCreateSubDirectory", smbProperties, null, null, null, null), 201)
    }

    def "Delete sub directory"() {
        given:
        def subDirectoryName = "testSubCreateDirectory"
        primaryDirectoryClient.create()
        primaryDirectoryClient.createSubdirectory(subDirectoryName)

        expect:
        FileTestHelper.assertResponseStatusCode(primaryDirectoryClient.deleteSubdirectoryWithResponse(subDirectoryName, null, null), 202)
    }

    def "Delete sub directory error"() {
        given:
        primaryDirectoryClient.create()

        when:
        primaryDirectoryClient.deleteSubdirectory("testsubdirectory")

        then:
        def e = thrown(ShareStorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 404, ShareErrorCode.RESOURCE_NOT_FOUND)
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
        def e = thrown(ShareStorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, statusCode, errMsg)

        where:
        fileName    | maxSize | statusCode | errMsg
        "testfile:" | 1024    | 400        | ShareErrorCode.INVALID_RESOURCE_NAME
        "fileName"  | -1      | 400        | ShareErrorCode.OUT_OF_RANGE_INPUT

    }

    def "Create file maxOverload"() {
        given:
        primaryDirectoryClient.create()
        ShareFileHttpHeaders httpHeaders = new ShareFileHttpHeaders()
            .setContentType("txt")
        smbProperties.setFileCreationTime(getUTCNow())
            .setFileLastWriteTime(getUTCNow())

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
        def e = thrown(ShareStorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 400, errMsg)

        where:
        fileName    | maxSize | httpHeaders                                           | metadata                              | errMsg
        "testfile:" | 1024    | null                                                  | testMetadata                          | ShareErrorCode.INVALID_RESOURCE_NAME
        "fileName"  | -1      | null                                                  | testMetadata                          | ShareErrorCode.OUT_OF_RANGE_INPUT
        "fileName"  | 1024    | new ShareFileHttpHeaders().setContentMd5(new byte[0]) | testMetadata                          | ShareErrorCode.INVALID_HEADER_VALUE
        "fileName"  | 1024    | null                                                  | Collections.singletonMap("", "value") | ShareErrorCode.EMPTY_METADATA_KEY

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
        def e = thrown(ShareStorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 404, ShareErrorCode.RESOURCE_NOT_FOUND)
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

    def "Get Share Name"() {
        expect:
        shareName == primaryDirectoryClient.getShareName()
    }

    def "Get Directory Path"() {
        expect:
        directoryPath == primaryDirectoryClient.getDirectoryPath()
    }
}
