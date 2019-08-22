// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.spock

import com.azure.storage.common.credentials.SharedKeyCredential
import com.azure.storage.file.DirectoryAsyncClient
import com.azure.storage.file.FileAsyncClient
import com.azure.storage.file.ShareClientBuilder
import com.azure.storage.file.models.FileHTTPHeaders
import com.azure.storage.file.models.StorageErrorCode
import reactor.test.StepVerifier
import spock.lang.Unroll

import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset

class ShareAsyncAPITests extends APISpec {
    def primaryShareAsyncClient
    def shareName
    static def testMetadata

    def setup() {
        shareName = testResourceName.randomName(methodName, 60)
        primaryFileServiceAsyncClient = fileServiceBuilderHelper(interceptorManager).buildAsyncClient()
        primaryShareAsyncClient = primaryFileServiceAsyncClient.getShareAsyncClient(shareName)
        testMetadata = Collections.singletonMap("testmetadata", "value")
    }

    def "Get share URL"() {
        given:
        def accoutName = SharedKeyCredential.fromConnectionString(connectionString).accountName()
        def expectURL = String.format("https://%s.file.core.windows.net", accoutName)
        when:
        def shareURL = primaryShareAsyncClient.getShareUrl().toString()
        then:
        expectURL.equals(shareURL)
    }

    def "Get root directory client"() {
        given:
        def directoryClient = primaryShareAsyncClient.getRootDirectoryClient()
        expect:
        directoryClient instanceof DirectoryAsyncClient
    }

    def "Get file client does not create a file"() {
        given:
        def fileClient = primaryShareAsyncClient.getFileClient("testFile")
        expect:
        fileClient instanceof FileAsyncClient
    }

    def "Create share"() {
        expect:
        StepVerifier.create(primaryShareAsyncClient.create())
            .assertNext {
                assert FileTestHelper.assertResponseStatusCode(it, 201)
            }.verifyComplete()
    }

    @Unroll
    def "Create share with args"() {
        expect:
        StepVerifier.create(primaryShareAsyncClient.create(metadata, quota))
            .assertNext {
                assert FileTestHelper.assertResponseStatusCode(it, 201)
            }.verifyComplete()
        where:
        metadata     | quota
        null         | null
        null         | 1
        testMetadata | null
        testMetadata | 1
    }

    @Unroll
    def "Create share with invalid args"() {
        when:
        def createShareVerifier = StepVerifier.create(primaryShareAsyncClient.create(metadata, quota))
        then:
        createShareVerifier.verifyErrorSatisfies {
            assert FileTestHelper.assertExceptionStatusCodeAndMessage(it, statusCode, errMessage)
        }

        where:
        metadata                                 | quota | statusCode | errMessage
        Collections.singletonMap("", "value")    | 1     | 400        | StorageErrorCode.EMPTY_METADATA_KEY
        Collections.singletonMap("a@B", "value") | 1     | 400        | "Bad Request"
        testMetadata                             | 6000  | 400        | StorageErrorCode.INVALID_HEADER_VALUE
    }

    def "Create snapshot"() {
        given:
        primaryShareAsyncClient.create().block()
        def shareSnapshotName = testResourceName.randomName(methodName, 60)
        when:
        def createSnapshotVerifier = StepVerifier.create(primaryShareAsyncClient.createSnapshot())

        then:
        createSnapshotVerifier.assertNext {
            assert FileTestHelper.assertResponseStatusCode(it, 201)
            def shareSnapshotClient = new ShareClientBuilder().shareName(shareSnapshotName).connectionString(connectionString)
                .snapshot(it.value().snapshot()).buildClient()
            assert Objects.equals(it.value().snapshot(),
                shareSnapshotClient.getSnapshotId())
        }.verifyComplete()

    }

    def "Create snapshot error"() {
        when:
        def createShareShnapshotErrorVerifier = StepVerifier.create(primaryShareAsyncClient.createSnapshot())
        then:
        createShareShnapshotErrorVerifier.verifyErrorSatisfies {
            assert FileTestHelper.assertExceptionStatusCodeAndMessage(it, 404, StorageErrorCode.SHARE_NOT_FOUND)
        }
    }

    def "Create snapshot metadata"() {
        given:
        primaryShareAsyncClient.create().block()
        def shareSnapshotName = testResourceName.randomName(methodName, 60)
        when:
        def createSnapshotVerifier = StepVerifier.create(primaryShareAsyncClient.createSnapshot(testMetadata))
        then:
        createSnapshotVerifier.assertNext {
            assert FileTestHelper.assertResponseStatusCode(it, 201)
            def shareSnapshotClient = new ShareClientBuilder().shareName(shareSnapshotName).connectionString(connectionString)
                .snapshot(it.value().snapshot()).buildClient()
            assert Objects.equals(it.value().snapshot(),
                shareSnapshotClient.getSnapshotId())
        }.verifyComplete()
    }

    def "Create snapshot metadata error"() {
        when:
        def createSnapshotErrorVerifier = StepVerifier.create(primaryShareAsyncClient.createSnapshot(Collections.singletonMap("", "value")))
        then:
        createSnapshotErrorVerifier.verifyErrorSatisfies {
            assert FileTestHelper.assertExceptionStatusCodeAndMessage(it, 400, StorageErrorCode.EMPTY_METADATA_KEY)
        }
    }

    def "Delete share"() {
        given:
        primaryShareAsyncClient.create().block()
        expect:
        StepVerifier.create(primaryShareAsyncClient.delete())
            .assertNext {
                FileTestHelper.assertResponseStatusCode(it, 201)
            }
    }

    def "Delete share error"() {
        expect:
        StepVerifier.create(primaryShareAsyncClient.delete())
            .verifyErrorSatisfies {
                assert FileTestHelper.assertExceptionStatusCodeAndMessage(it, 404, StorageErrorCode.SHARE_NOT_FOUND)
            }
    }

    def "Get properties"() {
        given:
        primaryShareAsyncClient.create(testMetadata, 1).block()
        when:
        def getPropertiesVerifier = StepVerifier.create(primaryShareAsyncClient.getProperties())
        then:
        getPropertiesVerifier.assertNext {
            assert FileTestHelper.assertResponseStatusCode(it, 200)
            assert testMetadata.equals(it.value().metadata())
            assert it.value().quota() == 1L
        }.verifyComplete()
    }

    def "Get properties error"() {
        expect:
        StepVerifier.create(primaryShareAsyncClient.getProperties())
            .verifyErrorSatisfies {
                assert FileTestHelper.assertExceptionStatusCodeAndMessage(it, 404, StorageErrorCode.SHARE_NOT_FOUND)
            }
    }

    def "Set quota"() {
        given:
        primaryShareAsyncClient.create(null, 1).block()
        when:
        def getQuotaBeforeVerifier = StepVerifier.create(primaryShareAsyncClient.getProperties())
        def setQuotaVerifier = StepVerifier.create(primaryShareAsyncClient.setQuota(2))
        def getQuotaAfterVerifier = StepVerifier.create(primaryShareAsyncClient.getProperties())
        then:
        getQuotaBeforeVerifier.assertNext {
            assert it.quota() == 1
        }
        setQuotaVerifier.assertNext {
            assert FileTestHelper.assertResponseStatusCode(setQuotaResponse, 200)
        }
        getQuotaAfterVerifier.assertNext {
            assert it.quota() == 2
        }
    }

    def "Set quota error"() {
        expect:
        StepVerifier.create(primaryShareAsyncClient.setQuota(2))
            .verifyErrorSatisfies {
                assert FileTestHelper.assertExceptionStatusCodeAndMessage(it, 404, StorageErrorCode.SHARE_NOT_FOUND)
            }
    }

    def "Set metadata"() {
        given:
        primaryShareAsyncClient.create(testMetadata, null).block()
        def metadataAfterSet = Collections.singletonMap("afterset", "value")
        when:
        def getMetadataBeforeVerifer = StepVerifier.create(primaryShareAsyncClient.getProperties())
        def setMetadataVerifier = StepVerifier.create(primaryShareAsyncClient.setMetadata(metadataAfterSet))
        def getMetadataAfterVerifier = StepVerifier.create(primaryShareAsyncClient.getProperties())
        then:
        getMetadataBeforeVerifer.assertNext {
            assert testMetadata.equals(it.metadata())
        }
        setMetadataVerifier.assertNext {
            assert FileTestHelper.assertResponseStatusCode(it, 200)
        }
        getMetadataAfterVerifier.assertNext {
            assert metadataAfterSet.equals(it.metadata())
        }
    }

    def "Set metadata error"() {
        expect:
        StepVerifier.create(primaryShareAsyncClient.setMetadata(testMetadata))
            .verifyErrorSatisfies {
                assert FileTestHelper.assertExceptionStatusCodeAndMessage(it, 404, StorageErrorCode.SHARE_NOT_FOUND)
            }
    }

    def "Create directory"() {
        given:
        primaryShareAsyncClient.create().block()
        expect:
        StepVerifier.create(primaryShareAsyncClient.createDirectory("testCreateDirectory"))
            .assertNext {
                assert FileTestHelper.assertResponseStatusCode(it, 201)
            }.verifyComplete()
    }

    def "Create directory invalid name"() {
        given:
        primaryShareAsyncClient.create().block()
        when:
        def createDirectoryVerifier = StepVerifier.create(primaryShareAsyncClient.createDirectory("test/directory"))
        then:
        createDirectoryVerifier.verifyErrorSatisfies {
            assert FileTestHelper.assertExceptionStatusCodeAndMessage(it, 404, StorageErrorCode.PARENT_NOT_FOUND)
        }
    }

    def "Create directory metadata"() {
        given:
        primaryShareAsyncClient.create().block()
        expect:
        StepVerifier.create(primaryShareAsyncClient.createDirectory("testCreateDirectory", testMetadata))
            .assertNext {
                assert FileTestHelper.assertResponseStatusCode(it, 201)
            }.verifyComplete()
    }

    def "Create directory metadata error"() {
        given:
        primaryShareAsyncClient.create().block()
        when:
        def createDirErrorVerifier = StepVerifier.create(primaryShareAsyncClient.createDirectory("testdirectory", Collections.singletonMap("", "value")))
        then:
        createDirErrorVerifier.verifyErrorSatisfies {
            assert FileTestHelper.assertExceptionStatusCodeAndMessage(it, 400, StorageErrorCode.EMPTY_METADATA_KEY)
        }
    }

    def "Create file"() {
        given:
        primaryShareAsyncClient.create().block()
        expect:
        StepVerifier.create(primaryShareAsyncClient.createFile("testCreateFile", 1024))
            .assertNext {
                assert FileTestHelper.assertResponseStatusCode(it, 201)
            }.verifyComplete()
    }

    @Unroll
    def "Create file invalid args"() {
        given:
        primaryShareAsyncClient.create().block()
        when:
        def createFileErrorVerifier = StepVerifier.create(primaryShareAsyncClient.createFile(fileName, maxSize))
        then:
        createFileErrorVerifier.verifyErrorSatisfies {
            assert FileTestHelper.assertExceptionStatusCodeAndMessage(it, statusCode, errMsg)
        }
        where:
        fileName    | maxSize | statusCode | errMsg
        "test\file" | 1024    | 400        | "Bad Request"
        "fileName"  | -1      | 400        | StorageErrorCode.OUT_OF_RANGE_INPUT

    }

    def "Create file maxOverload"() {
        given:
        primaryShareAsyncClient.create().block()
        FileHTTPHeaders httpHeaders = new FileHTTPHeaders().fileContentType("txt")
        expect:
        StepVerifier.create(primaryShareAsyncClient.createFile("testCreateFile", 1024, httpHeaders, testMetadata))
            .assertNext {
                assert FileTestHelper.assertResponseStatusCode(it, 201)
            }.verifyComplete()
    }

    @Unroll
    def "Create file maxOverload invalid args"() {
        given:
        primaryShareAsyncClient.create().block()
        when:
        def createFileVerifier = StepVerifier.create(primaryShareAsyncClient.createFile("test\file", maxSize))
        then:
        createFileVerifier.verifyErrorSatisfies {
            assert FileTestHelper.assertExceptionStatusCodeAndMessage(it, 400, "Bad Request")
        }
        where:
        fileName    | maxSize | httpHeaders                                       | metadata
        "test\file" | 1024    | new FileHTTPHeaders()                             | testMetadata
        "fileName"  | -1      | new FileHTTPHeaders()                             | testMetadata
        "fileName"  | 1024    | new FileHTTPHeaders().fileContentMD5(new byte[0]) | testMetadata
        "fileName"  | 1024    | new FileHTTPHeaders()                             | Collections.singletonMap("", "value")

    }

    def "Delete directory"() {
        given:
        def directoryName = "testCreateDirectory"
        primaryShareAsyncClient.create().block()
        primaryShareAsyncClient.createDirectory(directoryName).block()
        expect:
        StepVerifier.create(primaryShareAsyncClient.deleteDirectory(directoryName))
            .assertNext {
                assert FileTestHelper.assertResponseStatusCode(it, 202)
            }.verifyComplete()
    }

    def "Delete directory error"() {
        given:
        primaryShareAsyncClient.create().block()
        when:
        def deleteDirErrorVerifier = StepVerifier.create(primaryShareAsyncClient.deleteDirectory("testdirectory"))
        then:
        deleteDirErrorVerifier.verifyErrorSatisfies {
            assert FileTestHelper.assertExceptionStatusCodeAndMessage(it, 404, StorageErrorCode.RESOURCE_NOT_FOUND)
        }
    }

    def "Delete file"() {
        given:
        def fileName = "testCreateFile"
        primaryShareAsyncClient.create().block()
        primaryShareAsyncClient.createFile(fileName, 1024).block()
        expect:
        StepVerifier.create(primaryShareAsyncClient.deleteFile(fileName))
            .assertNext {
                assert FileTestHelper.assertResponseStatusCode(it, 202)
            }.verifyComplete()

    }

    def "Delete file error"() {
        given:
        primaryShareAsyncClient.create().block()
        when:
        def deleteFileErrorVerifier = StepVerifier.create(primaryShareAsyncClient.deleteFile("testdirectory"))
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
        def shareSnapshotClient = shareBuilderHelper(interceptorManager, shareName).snapshot(snapshot).buildAsyncClient()
        then:
        snapshot.equals(shareSnapshotClient.getSnapshotId())
    }

}
