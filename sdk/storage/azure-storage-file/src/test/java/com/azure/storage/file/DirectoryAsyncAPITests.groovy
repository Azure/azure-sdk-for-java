// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file


import com.azure.storage.common.Constants
import com.azure.storage.file.models.FileHTTPHeaders
import com.azure.storage.file.models.NtfsFileAttributes
import com.azure.storage.file.models.StorageErrorCode
import reactor.test.StepVerifier
import spock.lang.Ignore
import spock.lang.Unroll

import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset

class DirectoryAsyncAPITests extends APISpec {
    DirectoryAsyncClient primaryDirectoryAsyncClient
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
        primaryDirectoryAsyncClient = directoryBuilderHelper(interceptorManager, shareName, directoryPath).buildDirectoryAsyncClient()
        testMetadata = Collections.singletonMap("testmetadata", "value")
        smbProperties = new FileSmbProperties()
            .setNtfsFileAttributes(EnumSet.of(NtfsFileAttributes.NORMAL))
    }

    def "Get directory URL"() {
        given:
        def accountName = SharedKeyCredential.fromConnectionString(connectionString).getAccountName()
        def expectURL = String.format("https://%s.file.core.windows.net/%s/%s", accountName, shareName, directoryPath)
        when:
        def directoryURL = primaryDirectoryAsyncClient.getDirectoryUrl()
        then:
        expectURL.equals(directoryURL)
    }

    def "Get sub directory client"() {
        given:
        def subDirectoryClient = primaryDirectoryAsyncClient.getSubDirectoryClient("testSubDirectory")
        expect:
        subDirectoryClient instanceof DirectoryAsyncClient
    }

    def "Get file client"() {
        given:
        def fileClient = primaryDirectoryAsyncClient.getFileClient("testFile")
        expect:
        fileClient instanceof FileAsyncClient
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
        def testShareName = testResourceName.randomName(methodName, 60)
        when:
        def createDirErrorVerifier = StepVerifier.create(directoryBuilderHelper(interceptorManager, testShareName, directoryPath).buildDirectoryAsyncClient().create())
        then:
        createDirErrorVerifier.verifyErrorSatisfies {
            assert FileTestHelper.assertExceptionStatusCodeAndMessage(it, 404, StorageErrorCode.SHARE_NOT_FOUND)
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
        smbProperties.setFileCreationTime(getUTCNow())
            .setFileLastWriteTime(getUTCNow())
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
            assert FileTestHelper.assertExceptionStatusCodeAndMessage(it, 404, StorageErrorCode.RESOURCE_NOT_FOUND)
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
            assert FileTestHelper.assertExceptionStatusCodeAndMessage(it, 404, StorageErrorCode.RESOURCE_NOT_FOUND)
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
        smbProperties.setFileCreationTime(getUTCNow())
            .setFileLastWriteTime(getUTCNow())
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
            assert testMetadata.equals(it.getValue().getMetadata())
        }.verifyComplete()
        setPropertiesVerifier.assertNext {
            assert FileTestHelper.assertResponseStatusCode(it, 200)
        }.verifyComplete()
        getPropertiesAfterVerifier.assertNext {
            assert updatedMetadata.equals(it.getValue().getMetadata())
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
            assert FileTestHelper.assertExceptionStatusCodeAndMessage(it, 400, StorageErrorCode.EMPTY_METADATA_KEY)
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
            primaryDirectoryAsyncClient.createSubDirectory(expectedDirectory).block()
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
    @Unroll
    def "List files and directories args"() {
        given:
        primaryDirectoryAsyncClient.create().block()
        LinkedList<String> nameList = new LinkedList<>()
        def dirPrefix = testResourceName.randomName(methodName, 60)
        for (int i = 0; i < 2; i++) {
            def subDirClient = primaryDirectoryAsyncClient.getSubDirectoryClient(dirPrefix + i)
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
        def listFileAndDirVerifier = StepVerifier.create(primaryDirectoryAsyncClient.listFilesAndDirectories(prefix,
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
        prefix                                                   | maxResults | numOfResults
        "directoryasyncapitestslistfilesanddirectoriesargs"      | null       | 3
        "directoryasyncapitestslistfilesanddirectoriesargs"      | 1          | 3
        "directoryasyncapitestslistfilesanddirectoriesargsnoops" | 1          | 0
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
            assert FileTestHelper.assertExceptionStatusCodeAndMessage(it, 404, StorageErrorCode.RESOURCE_NOT_FOUND)
        }
    }

    @Ignore
    def "Force close handles"() {
        // TODO: Need to find a way of mocking handles.
    }

    def "Force close handles error"() {
        given:
        primaryDirectoryAsyncClient.create().block()
        when:
        def forceCloseHandlesErrorVerifier = StepVerifier.create(primaryDirectoryAsyncClient.forceCloseHandles("handleId", true))
        then:
        forceCloseHandlesErrorVerifier.verifyErrorSatisfies {
            assert FileTestHelper.assertExceptionStatusCodeAndMessage(it, 400, StorageErrorCode.INVALID_HEADER_VALUE)
        }
    }

    def "Create sub directory"() {
        given:
        primaryDirectoryAsyncClient.create().block()
        expect:
        StepVerifier.create(primaryDirectoryAsyncClient.createSubDirectoryWithResponse("testCreateSubDirectory", null, null, null))
            .assertNext {
                assert FileTestHelper.assertResponseStatusCode(it, 201)
            }.verifyComplete()
    }

    def "Create sub directory invalid name"() {
        given:
        primaryDirectoryAsyncClient.create().block()
        when:
        def createDirErrorVerifier = StepVerifier.create(primaryDirectoryAsyncClient.createSubDirectory("test/subdirectory"))
        then:
        createDirErrorVerifier.verifyErrorSatisfies {
            assert FileTestHelper.assertExceptionStatusCodeAndMessage(it, 404, StorageErrorCode.PARENT_NOT_FOUND)
        }
    }

    def "Create sub directory metadata"() {
        given:
        primaryDirectoryAsyncClient.create().block()
        expect:
        StepVerifier.create(primaryDirectoryAsyncClient.createSubDirectoryWithResponse("testCreateSubDirectory", null, null, testMetadata))
            .assertNext {
                assert FileTestHelper.assertResponseStatusCode(it, 201)
            }.verifyComplete()
    }

    def "Create sub directory metadata error"() {
        given:
        primaryDirectoryAsyncClient.create().block()
        when:
        def createDirErrorVerifier = StepVerifier.create(primaryDirectoryAsyncClient.createSubDirectoryWithResponse("testsubdirectory", null, null, Collections.singletonMap("", "value")))
        then:
        createDirErrorVerifier.verifyErrorSatisfies {
            assert FileTestHelper.assertExceptionStatusCodeAndMessage(it, 400, StorageErrorCode.EMPTY_METADATA_KEY)
        }
    }

    def "Create sub directory file permission"() {
        given:
        primaryDirectoryAsyncClient.create().block()
        expect:
        StepVerifier.create(primaryDirectoryAsyncClient.createSubDirectoryWithResponse("testCreateSubDirectory", null, filePermission, null))
            .assertNext {
                assert FileTestHelper.assertResponseStatusCode(it, 201)
            }.verifyComplete()
    }

    def "Create sub directory file perm key"() {
        given:
        def filePermissionKey = shareClient.createPermission(filePermission)
        smbProperties.setFileCreationTime(getUTCNow())
            .setFileLastWriteTime(getUTCNow())
            .setFilePermissionKey(filePermissionKey)
        primaryDirectoryAsyncClient.create().block()
        expect:
        StepVerifier.create(primaryDirectoryAsyncClient.createSubDirectoryWithResponse("testCreateSubDirectory", smbProperties, null, null))
            .assertNext {
                assert FileTestHelper.assertResponseStatusCode(it, 201)
            }.verifyComplete()
    }

    def "Delete sub directory"() {
        given:
        def subDirectoryName = "testSubCreateDirectory"
        primaryDirectoryAsyncClient.create().block()
        primaryDirectoryAsyncClient.createSubDirectory(subDirectoryName).block()
        expect:
        StepVerifier.create(primaryDirectoryAsyncClient.deleteSubDirectoryWithResponse(subDirectoryName))
            .assertNext {
                assert FileTestHelper.assertResponseStatusCode(it, 202)
            }.verifyComplete()
    }

    def "Delete sub directory error"() {
        given:
        primaryDirectoryAsyncClient.create().block()
        when:
        def deleteDirErrorVerifier = StepVerifier.create(primaryDirectoryAsyncClient.deleteSubDirectoryWithResponse("testsubdirectory"))
        then:
        deleteDirErrorVerifier.verifyErrorSatisfies {
            assert FileTestHelper.assertExceptionStatusCodeAndMessage(it, 404, StorageErrorCode.RESOURCE_NOT_FOUND)
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
        "testfile:" | 1024    | 400        | StorageErrorCode.INVALID_RESOURCE_NAME
        "fileName"  | -1      | 400        | StorageErrorCode.OUT_OF_RANGE_INPUT

    }

    def "Create file maxOverload"() {
        given:
        primaryDirectoryAsyncClient.create().block()
        FileHTTPHeaders httpHeaders = new FileHTTPHeaders()
            .setFileContentType("txt")
        smbProperties.setFileCreationTime(getUTCNow())
            .setFileLastWriteTime(getUTCNow())

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
        fileName    | maxSize | httpHeaders                                       | metadata                              | errMsg
        "testfile:" | 1024    | null                                              | testMetadata                          | StorageErrorCode.INVALID_RESOURCE_NAME
        "fileName"  | -1      | null                                              | testMetadata                          | StorageErrorCode.OUT_OF_RANGE_INPUT
        "fileName"  | 1024    | new FileHTTPHeaders().setFileContentMD5(new byte[0]) | testMetadata                          | StorageErrorCode.INVALID_HEADER_VALUE
        "fileName"  | 1024    | null                                              | Collections.singletonMap("", "value") | StorageErrorCode.EMPTY_METADATA_KEY

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

    def "Delete file error"() {
        given:
        primaryDirectoryAsyncClient.create().block()
        when:
        def deleteFileErrorVerifier = StepVerifier.create(primaryDirectoryAsyncClient.deleteFileWithResponse("testfile"))
        then:
        deleteFileErrorVerifier.verifyErrorSatisfies {
            assert FileTestHelper.assertExceptionStatusCodeAndMessage(it, 404, StorageErrorCode.RESOURCE_NOT_FOUND)
        }
    }

    def "Get snapshot id"() {
        given:
        def snapshot = OffsetDateTime.of(LocalDateTime.of(2000, 1, 1,
            1, 1), ZoneOffset.UTC).toString()
        when:
        def shareSnapshotClient = directoryBuilderHelper(interceptorManager, shareName, directoryPath).snapshot(snapshot).buildDirectoryAsyncClient()
        then:
        snapshot.equals(shareSnapshotClient.getShareSnapshotId())
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
