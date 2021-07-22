// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share

import com.azure.storage.common.implementation.Constants
import com.azure.storage.common.StorageSharedKeyCredential
import com.azure.storage.common.test.shared.extensions.RequiredServiceVersion
import com.azure.storage.file.share.models.ShareErrorCode
import com.azure.storage.file.share.models.ShareFileHttpHeaders
import com.azure.storage.file.share.models.ShareRequestConditions
import com.azure.storage.file.share.models.ShareStorageException
import com.azure.storage.file.share.models.NtfsFileAttributes
import reactor.test.StepVerifier
import spock.lang.Unroll

import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset

class DirectoryAsyncAPITests extends APISpec {
    ShareDirectoryAsyncClient primaryDirectoryAsyncClient
    ShareClient shareClient
    String directoryPath
    String shareName
    static Map<String, String> testMetadata
    FileSmbProperties smbProperties
    static def filePermission = "O:S-1-5-21-2127521184-1604012920-1887927527-21560751G:S-1-5-21-2127521184-1604012920-1887927527-513D:AI(A;;FA;;;SY)(A;;FA;;;BA)(A;;0x1200a9;;;S-1-5-21-397955417-626881126-188441444-3053964)S:NO_ACCESS_CONTROL"

    def setup() {
        shareName = namer.getRandomName(60)
        directoryPath = namer.getRandomName(60)
        shareClient = shareBuilderHelper(shareName).buildClient()
        shareClient.create()
        primaryDirectoryAsyncClient = directoryBuilderHelper(shareName, directoryPath).buildDirectoryAsyncClient()
        testMetadata = Collections.singletonMap("testmetadata", "value")
        smbProperties = new FileSmbProperties().setNtfsFileAttributes(EnumSet.<NtfsFileAttributes>of(NtfsFileAttributes.NORMAL))
    }

    def "Get directory URL"() {
        given:
        def accountName = StorageSharedKeyCredential.fromConnectionString(env.primaryAccount.connectionString).getAccountName()
        def expectURL = String.format("https://%s.file.core.windows.net/%s/%s", accountName, shareName, directoryPath)
        when:
        def directoryURL = primaryDirectoryAsyncClient.getDirectoryUrl()
        then:
        expectURL == directoryURL
    }

    def "Get sub directory client"() {
        given:
        def subDirectoryClient = primaryDirectoryAsyncClient.getSubdirectoryClient("testSubDirectory")
        expect:
        subDirectoryClient instanceof ShareDirectoryAsyncClient
    }

    def "Get file client"() {
        given:
        def fileClient = primaryDirectoryAsyncClient.getFileClient("testFile")
        expect:
        fileClient instanceof ShareFileAsyncClient
    }

    def "Create directory"() {
        expect:
        StepVerifier.create(primaryDirectoryAsyncClient.createWithResponse(null, null, null))
            .assertNext {
                assert FileTestHelper.assertResponseStatusCode(it, 201)
            }.verifyComplete()
    }

    def "Create directory error"() {
        given:
        def testShareName = namer.getRandomName(60)
        when:
        def createDirErrorVerifier = StepVerifier.create(directoryBuilderHelper(testShareName, directoryPath).buildDirectoryAsyncClient().create())
        then:
        createDirErrorVerifier.verifyErrorSatisfies {
            assert FileTestHelper.assertExceptionStatusCodeAndMessage(it, 404, ShareErrorCode.SHARE_NOT_FOUND)
        }
    }

    def "Create directory with metadata"() {
        expect:
        StepVerifier.create(primaryDirectoryAsyncClient.createWithResponse(null, null, testMetadata))
            .assertNext {
                assert FileTestHelper.assertResponseStatusCode(it, 201)
            }.verifyComplete()
    }

    def "Create directory with file permission"() {
        expect:
        StepVerifier.create(primaryDirectoryAsyncClient.createWithResponse(null, filePermission, testMetadata))
            .assertNext {
                assert FileTestHelper.assertResponseStatusCode(it, 201)
                assert it.getValue().getSmbProperties()
                assert it.getValue().getSmbProperties().getFilePermissionKey()
                assert it.getValue().getSmbProperties().getNtfsFileAttributes()
                assert it.getValue().getSmbProperties().getFileLastWriteTime()
                assert it.getValue().getSmbProperties().getFileCreationTime()
                assert it.getValue().getSmbProperties().getFileChangeTime()
                assert it.getValue().getSmbProperties().getParentId()
                assert it.getValue().getSmbProperties().getFileId()
            }.verifyComplete()
    }

    def "Create dir with file perm key"() {
        def filePermissionKey = shareClient.createPermission(filePermission)
        smbProperties.setFileCreationTime(namer.getUtcNow())
            .setFileLastWriteTime(namer.getUtcNow())
            .setFilePermissionKey(filePermissionKey)
        expect:
        StepVerifier.create(primaryDirectoryAsyncClient.createWithResponse(smbProperties, null, null))
            .assertNext {
                assert FileTestHelper.assertResponseStatusCode(it, 201)
                assert it.getValue().getSmbProperties()
                assert it.getValue().getSmbProperties().getFilePermissionKey()
                assert it.getValue().getSmbProperties().getNtfsFileAttributes()
                assert it.getValue().getSmbProperties().getFileLastWriteTime()
                assert it.getValue().getSmbProperties().getFileCreationTime()
                assert it.getValue().getSmbProperties().getFileChangeTime()
                assert it.getValue().getSmbProperties().getParentId()
                assert it.getValue().getSmbProperties().getFileId()
            }.verifyComplete()
    }

    def "Delete directory"() {
        given:
        primaryDirectoryAsyncClient.create().block()
        expect:
        StepVerifier.create(primaryDirectoryAsyncClient.deleteWithResponse())
            .assertNext {
                assert FileTestHelper.assertResponseStatusCode(it, 202)
            }.verifyComplete()
    }

    def "Delete directory error"() {
        when:
        def deleteDirErrorVerifier = StepVerifier.create(primaryDirectoryAsyncClient.delete())
        then:
        deleteDirErrorVerifier.verifyErrorSatisfies {
            assert FileTestHelper.assertExceptionStatusCodeAndMessage(it, 404, ShareErrorCode.RESOURCE_NOT_FOUND)
        }
    }

    def "Get properties"() {
        given:
        primaryDirectoryAsyncClient.create().block()
        def getPropertiesVerifier = StepVerifier.create(primaryDirectoryAsyncClient.getPropertiesWithResponse())
        expect:
        getPropertiesVerifier.assertNext {
            assert FileTestHelper.assertResponseStatusCode(it, 200)
            assert it.getValue().getETag()
            assert it.getValue().getSmbProperties()
            assert it.getValue().getSmbProperties().getFilePermissionKey()
            assert it.getValue().getSmbProperties().getNtfsFileAttributes()
            assert it.getValue().getSmbProperties().getFileLastWriteTime()
            assert it.getValue().getSmbProperties().getFileCreationTime()
            assert it.getValue().getSmbProperties().getFileChangeTime()
            assert it.getValue().getSmbProperties().getParentId()
            assert it.getValue().getSmbProperties().getFileId()
        }.verifyComplete()
    }

    def "Get properties error"() {
        when:
        def getPropertiesErrorVerifier = StepVerifier.create(primaryDirectoryAsyncClient.getPropertiesWithResponse())
        then:
        getPropertiesErrorVerifier.verifyErrorSatisfies {
            assert FileTestHelper.assertExceptionStatusCodeAndMessage(it, 404, ShareErrorCode.RESOURCE_NOT_FOUND)
        }
    }

    def "Set properties file permission"() {
        given:
        primaryDirectoryAsyncClient.createWithResponse(null, null, null).block()
        expect:
        StepVerifier.create(primaryDirectoryAsyncClient.setPropertiesWithResponse(null, filePermission))
            .assertNext {
                assert FileTestHelper.assertResponseStatusCode(it, 200)
                assert it.getValue().getSmbProperties()
                assert it.getValue().getSmbProperties().getFilePermissionKey()
                assert it.getValue().getSmbProperties().getNtfsFileAttributes()
                assert it.getValue().getSmbProperties().getFileLastWriteTime()
                assert it.getValue().getSmbProperties().getFileCreationTime()
                assert it.getValue().getSmbProperties().getFileChangeTime()
                assert it.getValue().getSmbProperties().getParentId()
                assert it.getValue().getSmbProperties().getFileId()
            }.verifyComplete()
    }

    def "Set properties file permission key"() {
        given:
        def filePermissionKey = shareClient.createPermission(filePermission)
        smbProperties.setFileCreationTime(namer.getUtcNow())
            .setFileLastWriteTime(namer.getUtcNow())
            .setFilePermissionKey(filePermissionKey)
        primaryDirectoryAsyncClient.createWithResponse(null, null, null).block()
        expect:
        StepVerifier.create(primaryDirectoryAsyncClient.setPropertiesWithResponse(smbProperties, null))
            .assertNext {
                assert FileTestHelper.assertResponseStatusCode(it, 200)
                assert it.getValue().getSmbProperties()
                assert it.getValue().getSmbProperties().getFilePermissionKey()
                assert it.getValue().getSmbProperties().getNtfsFileAttributes()
                assert it.getValue().getSmbProperties().getFileLastWriteTime()
                assert it.getValue().getSmbProperties().getFileCreationTime()
                assert it.getValue().getSmbProperties().getFileChangeTime()
                assert it.getValue().getSmbProperties().getParentId()
                assert it.getValue().getSmbProperties().getFileId()
            }.verifyComplete()
    }

    def "Set properties error"() {
        setup:
        primaryDirectoryAsyncClient.createWithResponse(null, null, null).block()
        when:
        FileSmbProperties properties = new FileSmbProperties().setFilePermissionKey("filePermissionKey")
        def setPropertiesVerifier = StepVerifier.create(primaryDirectoryAsyncClient.setProperties(properties, filePermission))
        then:
        setPropertiesVerifier.verifyErrorSatisfies {
            assert it instanceof IllegalArgumentException
        }
        when:
        setPropertiesVerifier = StepVerifier.create(primaryDirectoryAsyncClient.setProperties(null, new String(FileTestHelper.getRandomBuffer(9 * Constants.KB))))
        then:
        setPropertiesVerifier.verifyErrorSatisfies {
            assert it instanceof IllegalArgumentException
        }
    }

    def "Set metadata"() {
        given:
        primaryDirectoryAsyncClient.createWithResponse(null, null, testMetadata).block()
        def updatedMetadata = Collections.singletonMap("update", "value")
        when:
        def getPropertiesBeforeVerifier = StepVerifier.create(primaryDirectoryAsyncClient.getPropertiesWithResponse())
        def setPropertiesVerifier = StepVerifier.create(primaryDirectoryAsyncClient.setMetadataWithResponse(updatedMetadata))
        def getPropertiesAfterVerifier = StepVerifier.create(primaryDirectoryAsyncClient.getPropertiesWithResponse())
        then:
        getPropertiesBeforeVerifier.assertNext {
            assert testMetadata == it.getValue().getMetadata()
        }.verifyComplete()
        setPropertiesVerifier.assertNext {
            assert FileTestHelper.assertResponseStatusCode(it, 200)
        }.verifyComplete()
        getPropertiesAfterVerifier.assertNext {
            assert updatedMetadata == it.getValue().getMetadata()
        }.verifyComplete()
    }

    def "Set metadata error"() {
        given:
        primaryDirectoryAsyncClient.create().block()
        def errorMetadata = Collections.singletonMap("", "value")
        when:
        def setMetadataVerifier = StepVerifier.create(primaryDirectoryAsyncClient.setMetadata(errorMetadata))
        then:
        setMetadataVerifier.verifyErrorSatisfies {
            assert FileTestHelper.assertExceptionStatusCodeAndMessage(it, 400, ShareErrorCode.EMPTY_METADATA_KEY)
        }
    }

    @Unroll
    def "List files and directories"() {
        given:
        primaryDirectoryAsyncClient.create().block()

        for (def expectedFile : expectedFiles) {
            primaryDirectoryAsyncClient.createFile(expectedFile, 2).block()
        }

        for (def expectedDirectory : expectedDirectories) {
            primaryDirectoryAsyncClient.createSubdirectory(expectedDirectory).block()
        }

        when:
        def foundFiles = [] as Set
        def foundDirectories = [] as Set
        for (def fileRef : primaryDirectoryAsyncClient.listFilesAndDirectories().toIterable()) {
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
    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "V2020_10_02")
    @Unroll
    def "List files and directories args"() {
        given:
        primaryDirectoryAsyncClient.create().block()
        LinkedList<String> nameList = new LinkedList<>()
        def dirPrefix = namer.getRandomName(60)
        for (int i = 0; i < 2; i++) {
            def subDirClient = primaryDirectoryAsyncClient.getSubdirectoryClient(dirPrefix + i)
            subDirClient.create().block()
            for (int j = 0; j < 2; j++) {
                def num = i * 2 + j + 3
                subDirClient.createFile(dirPrefix + num, 1024).block()
            }
        }
        primaryDirectoryAsyncClient.createFile(dirPrefix + 2, 1024).block()
        for (int i = 0; i < 3; i++) {
            nameList.add(dirPrefix + i)
        }

        when:
        def listFileAndDirVerifier = StepVerifier.create(primaryDirectoryAsyncClient.listFilesAndDirectories(
            namer.getResourcePrefix() + extraPrefix,
            maxResults))

        then:
        listFileAndDirVerifier.thenConsumeWhile {
            Objects.equals(it.getName(), nameList.pop())
        }.verifyComplete()
        for (int i = 0; i < 3 - numOfResults; i++) {
            nameList.pop()
        }
        nameList.isEmpty()
        where:
        extraPrefix | maxResults | numOfResults
        ""          | null       | 3
        ""          | 1          | 3
        "noops"     | 1          | 0
    }

    @Unroll
    def "List handles"() {
        given:
        primaryDirectoryAsyncClient.create().block()
        expect:
        StepVerifier.create(primaryDirectoryAsyncClient.listHandles(maxResult, recursive)).verifyComplete()
        where:
        maxResult | recursive
        2         | true
        null      | false
    }

    def "List handles error"() {
        when:
        def listHandlesVerifier = StepVerifier.create(primaryDirectoryAsyncClient.listHandles(null, true))
        then:
        listHandlesVerifier.verifyErrorSatisfies {
            assert FileTestHelper.assertExceptionStatusCodeAndMessage(it, 404, ShareErrorCode.RESOURCE_NOT_FOUND)
        }
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "V2019_07_07")
    def "Force close handle min"() {
        given:
        primaryDirectoryAsyncClient.create().block()

        expect:
        StepVerifier.create(primaryDirectoryAsyncClient.forceCloseHandle("1"))
            .assertNext {
                assert it.getClosedHandles() == 0
                assert it.getFailedHandles() == 0
            }.verifyComplete()
    }

    def "Force close handle invalid handle ID"() {
        given:
        primaryDirectoryAsyncClient.create().block()

        expect:
        StepVerifier.create(primaryDirectoryAsyncClient.forceCloseHandle("invalidHandleId"))
            .verifyErrorSatisfies({ it instanceof  ShareStorageException })
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "V2019_07_07")
    def "Force close all handles min"() {
        given:
        primaryDirectoryAsyncClient.create().block()

        expect:
        StepVerifier.create(primaryDirectoryAsyncClient.forceCloseAllHandles(false))
            .assertNext {
                assert it.getClosedHandles() == 0
                assert it.getFailedHandles() == 0
            }.verifyComplete()
    }

    def "Create sub directory"() {
        given:
        primaryDirectoryAsyncClient.create().block()
        expect:
        StepVerifier.create(primaryDirectoryAsyncClient.createSubdirectoryWithResponse("testCreateSubDirectory", null, null, null))
            .assertNext {
                assert FileTestHelper.assertResponseStatusCode(it, 201)
            }.verifyComplete()
    }

    def "Create sub directory invalid name"() {
        given:
        primaryDirectoryAsyncClient.create().block()
        when:
        def createDirErrorVerifier = StepVerifier.create(primaryDirectoryAsyncClient.createSubdirectory("test/subdirectory"))
        then:
        createDirErrorVerifier.verifyErrorSatisfies {
            assert FileTestHelper.assertExceptionStatusCodeAndMessage(it, 404, ShareErrorCode.PARENT_NOT_FOUND)
        }
    }

    def "Create sub directory metadata"() {
        given:
        primaryDirectoryAsyncClient.create().block()
        expect:
        StepVerifier.create(primaryDirectoryAsyncClient.createSubdirectoryWithResponse("testCreateSubDirectory", null, null, testMetadata))
            .assertNext {
                assert FileTestHelper.assertResponseStatusCode(it, 201)
            }.verifyComplete()
    }

    def "Create sub directory metadata error"() {
        given:
        primaryDirectoryAsyncClient.create().block()
        when:
        def createDirErrorVerifier = StepVerifier.create(primaryDirectoryAsyncClient.createSubdirectoryWithResponse("testsubdirectory", null, null, Collections.singletonMap("", "value")))
        then:
        createDirErrorVerifier.verifyErrorSatisfies {
            assert FileTestHelper.assertExceptionStatusCodeAndMessage(it, 400, ShareErrorCode.EMPTY_METADATA_KEY)
        }
    }

    def "Create sub directory file permission"() {
        given:
        primaryDirectoryAsyncClient.create().block()
        expect:
        StepVerifier.create(primaryDirectoryAsyncClient.createSubdirectoryWithResponse("testCreateSubDirectory", null, filePermission, null))
            .assertNext {
                assert FileTestHelper.assertResponseStatusCode(it, 201)
            }.verifyComplete()
    }

    def "Create sub directory file perm key"() {
        given:
        def filePermissionKey = shareClient.createPermission(filePermission)
        smbProperties.setFileCreationTime(namer.getUtcNow())
            .setFileLastWriteTime(namer.getUtcNow())
            .setFilePermissionKey(filePermissionKey)
        primaryDirectoryAsyncClient.create().block()
        expect:
        StepVerifier.create(primaryDirectoryAsyncClient.createSubdirectoryWithResponse("testCreateSubDirectory", smbProperties, null, null))
            .assertNext {
                assert FileTestHelper.assertResponseStatusCode(it, 201)
            }.verifyComplete()
    }

    def "Delete sub directory"() {
        given:
        def subDirectoryName = "testSubCreateDirectory"
        primaryDirectoryAsyncClient.create().block()
        primaryDirectoryAsyncClient.createSubdirectory(subDirectoryName).block()
        expect:
        StepVerifier.create(primaryDirectoryAsyncClient.deleteSubdirectoryWithResponse(subDirectoryName))
            .assertNext {
                assert FileTestHelper.assertResponseStatusCode(it, 202)
            }.verifyComplete()
    }

    def "Delete sub directory error"() {
        given:
        primaryDirectoryAsyncClient.create().block()
        when:
        def deleteDirErrorVerifier = StepVerifier.create(primaryDirectoryAsyncClient.deleteSubdirectoryWithResponse("testsubdirectory"))
        then:
        deleteDirErrorVerifier.verifyErrorSatisfies {
            assert FileTestHelper.assertExceptionStatusCodeAndMessage(it, 404, ShareErrorCode.RESOURCE_NOT_FOUND)
        }
    }

    def "Create file"() {
        given:
        primaryDirectoryAsyncClient.create().block()
        expect:
        StepVerifier.create(primaryDirectoryAsyncClient.createFileWithResponse("testCreateFile", 1024, null, null, null, null))
            .assertNext {
                assert FileTestHelper.assertResponseStatusCode(it, 201)
            }.verifyComplete()
    }

    @Unroll
    def "Create file invalid args"() {
        given:
        primaryDirectoryAsyncClient.create().block()
        when:
        def createFileErrorVerifier = StepVerifier.create(primaryDirectoryAsyncClient.createFileWithResponse(fileName, maxSize, null, null, null, null))
        then:
        createFileErrorVerifier.verifyErrorSatisfies {
            assert FileTestHelper.assertExceptionStatusCodeAndMessage(it, statusCode, errMsg)
        }
        where:
        fileName    | maxSize | statusCode | errMsg
        "testfile:" | 1024    | 400        | ShareErrorCode.INVALID_RESOURCE_NAME
        "fileName"  | -1      | 400        | ShareErrorCode.OUT_OF_RANGE_INPUT

    }

    def "Create file maxOverload"() {
        given:
        primaryDirectoryAsyncClient.create().block()
        ShareFileHttpHeaders httpHeaders = new ShareFileHttpHeaders()
            .setContentType("txt")
        smbProperties.setFileCreationTime(namer.getUtcNow())
            .setFileLastWriteTime(namer.getUtcNow())

        expect:
        StepVerifier.create(primaryDirectoryAsyncClient.createFileWithResponse("testCreateFile", 1024, httpHeaders, smbProperties, filePermission, testMetadata))
            .assertNext {
                assert FileTestHelper.assertResponseStatusCode(it, 201)
            }.verifyComplete()
    }

    @Unroll
    def "Create file maxOverload invalid args"() {
        given:
        primaryDirectoryAsyncClient.create().block()

        when:

        def errorVerifier = StepVerifier.create(primaryDirectoryAsyncClient.createFileWithResponse(fileName, maxSize, httpHeaders, null, null, metadata))

        then:
        errorVerifier.verifyErrorSatisfies({
            assert FileTestHelper.assertExceptionStatusCodeAndMessage(it, 400, errMsg)
        })

        where:
        fileName    | maxSize | httpHeaders                                           | metadata                              | errMsg
        "testfile:" | 1024    | null                                                  | testMetadata                          | ShareErrorCode.INVALID_RESOURCE_NAME
        "fileName"  | -1      | null                                                  | testMetadata                          | ShareErrorCode.OUT_OF_RANGE_INPUT
        "fileName"  | 1024    | new ShareFileHttpHeaders().setContentMd5(new byte[0]) | testMetadata                          | ShareErrorCode.INVALID_HEADER_VALUE
        "fileName"  | 1024    | null                                                  | Collections.singletonMap("", "value") | ShareErrorCode.EMPTY_METADATA_KEY
    }

    def "Create file lease"() {
        given:
        primaryDirectoryAsyncClient.create().block()
        primaryDirectoryAsyncClient.getFileClient("testCreateFile").create(512).block()
        def leaseId = createLeaseClient(primaryDirectoryAsyncClient.getFileClient("testCreateFile")).acquireLease().block()

        expect:
        StepVerifier.create(primaryDirectoryAsyncClient.createFileWithResponse("testCreateFile", 1024, null, null, null,
            null, new ShareRequestConditions().setLeaseId(leaseId))).expectNextCount(1).verifyComplete()
    }

    def "Create file lease fail"() {
        given:
        primaryDirectoryAsyncClient.create().block()
        primaryDirectoryAsyncClient.getFileClient("testCreateFile").create(512).block()
        createLeaseClient(primaryDirectoryAsyncClient.getFileClient("testCreateFile")).acquireLease().block()

        expect:
        StepVerifier.create(primaryDirectoryAsyncClient.createFileWithResponse("testCreateFile", 1024, null, null, null,
            null, new ShareRequestConditions().setLeaseId(namer.getRandomUuid()))).verifyError(ShareStorageException)
    }

    def "Delete file"() {
        given:
        def fileName = "testCreateFile"
        primaryDirectoryAsyncClient.create().block()
        primaryDirectoryAsyncClient.createFile(fileName, 1024).block()
        expect:
        StepVerifier.create(primaryDirectoryAsyncClient.deleteFileWithResponse(fileName))
            .assertNext {
                assert FileTestHelper.assertResponseStatusCode(it, 202)
            }.verifyComplete()
    }

    def "Delete file lease"() {
        given:
        def fileName = "testCreateFile"
        primaryDirectoryAsyncClient.create().block()
        primaryDirectoryAsyncClient.createFile(fileName, 1024).block()
        def leaseId = createLeaseClient(primaryDirectoryAsyncClient.getFileClient(fileName)).acquireLease().block()

        expect:
        StepVerifier.create(primaryDirectoryAsyncClient.deleteFileWithResponse(fileName,
        new ShareRequestConditions().setLeaseId(leaseId)))
            .expectNextCount(1).verifyComplete()
    }

    def "Delete file lease fail"() {
        given:
        def fileName = "testCreateFile"
        primaryDirectoryAsyncClient.create().block()
        primaryDirectoryAsyncClient.createFile(fileName, 1024).block()
        createLeaseClient(primaryDirectoryAsyncClient.getFileClient(fileName)).acquireLease().block()

        expect:
        StepVerifier.create(primaryDirectoryAsyncClient.deleteFileWithResponse(fileName,
            new ShareRequestConditions().setLeaseId(namer.getRandomUuid())))
            .verifyError(ShareStorageException)
    }

    def "Get snapshot id"() {
        given:
        def snapshot = OffsetDateTime.of(LocalDateTime.of(2000, 1, 1,
            1, 1), ZoneOffset.UTC).toString()
        when:
        def shareSnapshotClient = directoryBuilderHelper(shareName, directoryPath).snapshot(snapshot).buildDirectoryAsyncClient()
        then:
        snapshot == shareSnapshotClient.getShareSnapshotId()
    }

    def "Get Share Name"() {
        expect:
        shareName == primaryDirectoryAsyncClient.getShareName()
    }

    def "Get Directory Path"() {
        expect:
        directoryPath == primaryDirectoryAsyncClient.getDirectoryPath()
    }
}
