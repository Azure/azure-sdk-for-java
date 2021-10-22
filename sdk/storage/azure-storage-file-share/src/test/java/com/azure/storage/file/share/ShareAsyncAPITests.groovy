// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share

import com.azure.core.http.netty.NettyAsyncHttpClientBuilder
import com.azure.storage.common.StorageSharedKeyCredential
import com.azure.storage.common.implementation.Constants
import com.azure.storage.common.test.shared.extensions.PlaybackOnly
import com.azure.storage.file.share.implementation.util.ModelHelper
import com.azure.storage.file.share.models.NtfsFileAttributes
import com.azure.storage.file.share.models.ShareProtocols
import com.azure.storage.file.share.models.ShareErrorCode
import com.azure.storage.file.share.models.ShareFileHttpHeaders
import com.azure.storage.file.share.models.ShareRequestConditions
import com.azure.storage.file.share.models.ShareRootSquash
import com.azure.storage.file.share.models.ShareStorageException
import com.azure.storage.file.share.options.ShareCreateOptions
import com.azure.storage.file.share.options.ShareSetPropertiesOptions
import reactor.test.StepVerifier
import spock.lang.Unroll

import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset

class ShareAsyncAPITests extends APISpec {
    ShareAsyncClient primaryShareAsyncClient
    String shareName
    static Map<String, String> testMetadata
    FileSmbProperties smbProperties
    static def filePermission = "O:S-1-5-21-2127521184-1604012920-1887927527-21560751G:S-1-5-21-2127521184-1604012920-1887927527-513D:AI(A;;FA;;;SY)(A;;FA;;;BA)(A;;0x1200a9;;;S-1-5-21-397955417-626881126-188441444-3053964)S:NO_ACCESS_CONTROL"

    def setup() {
        shareName = namer.getRandomName(60)
        primaryFileServiceAsyncClient = fileServiceBuilderHelper().buildAsyncClient()
        primaryShareAsyncClient = primaryFileServiceAsyncClient.getShareAsyncClient(shareName)
        testMetadata = Collections.singletonMap("testmetadata", "value")
        smbProperties = new FileSmbProperties().setNtfsFileAttributes(EnumSet.<NtfsFileAttributes>of(NtfsFileAttributes.NORMAL))
    }

    def "Get share URL"() {
        given:
        def accountName = StorageSharedKeyCredential.fromConnectionString(environment.primaryAccount.connectionString).getAccountName()
        def expectURL = String.format("https://%s.file.core.windows.net/%s", accountName, shareName)
        when:
        def shareURL = primaryShareAsyncClient.getShareUrl()
        then:
        expectURL == shareURL
    }

    def "Get root directory client"() {
        given:
        def directoryClient = primaryShareAsyncClient.getRootDirectoryClient()
        expect:
        directoryClient instanceof ShareDirectoryAsyncClient
    }

    def "Get file client does not create a file"() {
        given:
        def fileClient = primaryShareAsyncClient.getFileClient("testFile")
        expect:
        fileClient instanceof ShareFileAsyncClient
    }

    def "Create share"() {
        expect:
        StepVerifier.create(primaryShareAsyncClient.createWithResponse(null, null))
            .assertNext {
                assert FileTestHelper.assertResponseStatusCode(it, 201)
            }.verifyComplete()
    }

    @Unroll
    def "Create share with args"() {
        expect:
        StepVerifier.create(primaryShareAsyncClient.createWithResponse(metadata, quota))
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
        def createShareVerifier = StepVerifier.create(primaryShareAsyncClient.createWithResponse(metadata, quota))
        then:
        createShareVerifier.verifyErrorSatisfies {
            assert FileTestHelper.assertExceptionStatusCodeAndMessage(it, statusCode, errMessage)
        }

        where:
        metadata                                       | quota | statusCode | errMessage
        Collections.singletonMap("", "value")          | 1     | 400        | ShareErrorCode.EMPTY_METADATA_KEY
        Collections.singletonMap("metadata!", "value") | 1     | 400        | ShareErrorCode.INVALID_METADATA
        testMetadata                                   | 6000  | 400        | ShareErrorCode.INVALID_HEADER_VALUE
    }

    def "Create snapshot"() {
        given:
        primaryShareAsyncClient.create().block()
        def shareSnapshotName = namer.getRandomName(60)
        when:
        def createSnapshotVerifier = StepVerifier.create(primaryShareAsyncClient.createSnapshotWithResponse(null))

        then:
        createSnapshotVerifier.assertNext {
            assert FileTestHelper.assertResponseStatusCode(it, 201)
            def shareSnapshotClient = new ShareClientBuilder().shareName(shareSnapshotName).connectionString(environment.primaryAccount.connectionString)
                .snapshot(it.getValue().getSnapshot()).httpClient(new NettyAsyncHttpClientBuilder().build()).buildClient()
            assert Objects.equals(it.getValue().getSnapshot(),
                shareSnapshotClient.getSnapshotId())
        }.verifyComplete()

    }

    def "Create snapshot error"() {
        when:
        def createShareShnapshotErrorVerifier = StepVerifier.create(primaryShareAsyncClient.createSnapshot())
        then:
        createShareShnapshotErrorVerifier.verifyErrorSatisfies {
            assert FileTestHelper.assertExceptionStatusCodeAndMessage(it, 404, ShareErrorCode.SHARE_NOT_FOUND)
        }
    }

    def "Create snapshot metadata"() {
        given:
        primaryShareAsyncClient.create().block()
        def shareSnapshotName = namer.getRandomName(60)
        when:
        def createSnapshotVerifier = StepVerifier.create(primaryShareAsyncClient.createSnapshotWithResponse(testMetadata))
        then:
        createSnapshotVerifier.assertNext {
            assert FileTestHelper.assertResponseStatusCode(it, 201)
            def shareSnapshotClient = new ShareClientBuilder().shareName(shareSnapshotName).connectionString(environment.primaryAccount.connectionString)
                .snapshot(it.getValue().getSnapshot()).httpClient(new NettyAsyncHttpClientBuilder().build()).buildClient()
            assert Objects.equals(it.getValue().getSnapshot(),
                shareSnapshotClient.getSnapshotId())
        }.verifyComplete()
    }

    def "Create snapshot metadata error"() {
        when:
        primaryShareAsyncClient.create().block()
        def createSnapshotErrorVerifier = StepVerifier.create(primaryShareAsyncClient.createSnapshotWithResponse(Collections.singletonMap("", "value")))

        then:
        createSnapshotErrorVerifier.verifyErrorSatisfies {
            assert FileTestHelper.assertExceptionStatusCodeAndMessage(it, 400, ShareErrorCode.EMPTY_METADATA_KEY)
        }
    }

    def "Delete share"() {
        given:
        primaryShareAsyncClient.create().block()
        expect:
        StepVerifier.create(primaryShareAsyncClient.deleteWithResponse())
            .assertNext {
                FileTestHelper.assertResponseStatusCode(it, 201)
            }
    }

    def "Delete share error"() {
        expect:
        StepVerifier.create(primaryShareAsyncClient.delete())
            .verifyErrorSatisfies {
                assert FileTestHelper.assertExceptionStatusCodeAndMessage(it, 404, ShareErrorCode.SHARE_NOT_FOUND)
            }
    }

    def "Get properties"() {
        given:
        primaryShareAsyncClient.createWithResponse(testMetadata, 1).block()
        when:
        def getPropertiesVerifier = StepVerifier.create(primaryShareAsyncClient.getPropertiesWithResponse())
        then:
        getPropertiesVerifier.assertNext {
            assert FileTestHelper.assertResponseStatusCode(it, 200)
            assert testMetadata == it.getValue().getMetadata()
            assert it.getValue().getQuota() == 1
        }.verifyComplete()
    }

    def "Get properties error"() {
        expect:
        StepVerifier.create(primaryShareAsyncClient.getProperties())
            .verifyErrorSatisfies {
                assert FileTestHelper.assertExceptionStatusCodeAndMessage(it, 404, ShareErrorCode.SHARE_NOT_FOUND)
            }
    }

    @Unroll
    @PlaybackOnly
    def "Get properties premium"() {
        given:
        ShareProtocols enabledProtocol = ModelHelper.parseShareProtocols(protocol)

        def premiumShare = premiumFileServiceAsyncClient.createShareWithResponse(generateShareName(),
            new ShareCreateOptions().setMetadata(testMetadata).setProtocols(enabledProtocol)
            .setRootSquash(rootSquash), null).block().getValue()
        when:
        def getPropertiesVerifier = StepVerifier.create(premiumShare.getPropertiesWithResponse())
        then:
        getPropertiesVerifier.assertNext {
            assert FileTestHelper.assertResponseStatusCode(it, 200)
            assert testMetadata == it.getValue().getMetadata()
            assert it.getValue().getQuota()
            assert it.getValue().getProvisionedIops()
            assert it.getValue().getProvisionedIngressMBps()
            assert it.getValue().getProvisionedEgressMBps()
            assert it.getValue().getNextAllowedQuotaDowngradeTime()
            assert it.getValue().getProtocols().toString() == enabledProtocol.toString()
            assert it.getValue().getRootSquash() == rootSquash
        }.verifyComplete()

        where:
        protocol                               | rootSquash
        Constants.HeaderConstants.SMB_PROTOCOL | null
        Constants.HeaderConstants.NFS_PROTOCOL | ShareRootSquash.ALL_SQUASH
        Constants.HeaderConstants.NFS_PROTOCOL | ShareRootSquash.NO_ROOT_SQUASH
        Constants.HeaderConstants.NFS_PROTOCOL | ShareRootSquash.ROOT_SQUASH
    }

    @Unroll
    @PlaybackOnly
    def "Set premium properties"() {
        setup:
        def premiumShareClient = premiumFileServiceAsyncClient.createShareWithResponse(generateShareName(),
            new ShareCreateOptions().setProtocols(new ShareProtocols().setNfsEnabled(true)), null)
            .block().getValue()

        when:
        premiumShareClient.setProperties(new ShareSetPropertiesOptions().setRootSquash(rootSquash)).block()

        then:
        StepVerifier.create(premiumShareClient.getProperties())
            .assertNext{
                assert it.getRootSquash() == rootSquash
            }.verifyComplete()

        where:
        rootSquash                     | _
        ShareRootSquash.ROOT_SQUASH    | _
        ShareRootSquash.NO_ROOT_SQUASH | _
        ShareRootSquash.ALL_SQUASH     | _
    }

    def "Set quota"() {
        given:
        primaryShareAsyncClient.createWithResponse(null, 1).block()
        when:
        def getQuotaBeforeVerifier = StepVerifier.create(primaryShareAsyncClient.getProperties())
        def setQuotaVerifier = StepVerifier.create(primaryShareAsyncClient.setQuotaWithResponse(2))
        def getQuotaAfterVerifier = StepVerifier.create(primaryShareAsyncClient.getProperties())
        then:
        getQuotaBeforeVerifier.assertNext {
            assert it.getQuota() == 1
        }
        setQuotaVerifier.assertNext {
            assert FileTestHelper.assertResponseStatusCode(it, 200)
        }
        getQuotaAfterVerifier.assertNext {
            assert it.getQuota() == 2
        }
    }

    def "Set quota error"() {
        expect:
        StepVerifier.create(primaryShareAsyncClient.setQuota(2))
            .verifyErrorSatisfies {
                assert FileTestHelper.assertExceptionStatusCodeAndMessage(it, 404, ShareErrorCode.SHARE_NOT_FOUND)
            }
    }

    def "Set metadata"() {
        given:
        primaryShareAsyncClient.createWithResponse(testMetadata, null).block()
        def metadataAfterSet = Collections.singletonMap("afterset", "value")
        when:
        def getMetadataBeforeVerifier = StepVerifier.create(primaryShareAsyncClient.getProperties())
        def setMetadataVerifier = StepVerifier.create(primaryShareAsyncClient.setMetadataWithResponse(metadataAfterSet))
        def getMetadataAfterVerifier = StepVerifier.create(primaryShareAsyncClient.getProperties())
        then:
        getMetadataBeforeVerifier.assertNext {
            assert testMetadata == it.getMetadata()
        }
        setMetadataVerifier.assertNext {
            assert FileTestHelper.assertResponseStatusCode(it, 200)
        }
        getMetadataAfterVerifier.assertNext {
            assert metadataAfterSet == it.getMetadata()
        }
    }

    def "Set metadata error"() {
        expect:
        StepVerifier.create(primaryShareAsyncClient.setMetadata(testMetadata))
            .verifyErrorSatisfies {
                assert FileTestHelper.assertExceptionStatusCodeAndMessage(it, 404, ShareErrorCode.SHARE_NOT_FOUND)
            }
    }

    @Unroll
    def "Get statistics"() {
        setup:
        primaryShareAsyncClient.create().block()
        primaryShareAsyncClient.createFile("tempFile", (long) size).block()

        when:
        def respVerifier = StepVerifier.create(primaryShareAsyncClient.getStatisticsWithResponse())

        then:
        respVerifier.assertNext {
            assert FileTestHelper.assertResponseStatusCode(it, 200)
            assert it.getValue().getShareUsageInBytes() == size
            assert it.getValue().getShareUsageInGB() == gigabytes
        }

        where:
        size                    || gigabytes
        0                       || 0
        Constants.KB            || 1
        Constants.GB            || 1
        (long) 3 * Constants.GB || 3
    }

    def "Get statistics error"() {
        expect:
        StepVerifier.create(primaryShareAsyncClient.getStatistics())
            .verifyErrorSatisfies {
                assert FileTestHelper.assertExceptionStatusCodeAndMessage(it, 404, ShareErrorCode.SHARE_NOT_FOUND)
            }
    }

    def "Create directory"() {
        given:
        primaryShareAsyncClient.create().block()
        expect:
        StepVerifier.create(primaryShareAsyncClient.createDirectoryWithResponse("testCreateDirectory", null, null, null))
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
            assert FileTestHelper.assertExceptionStatusCodeAndMessage(it, 404, ShareErrorCode.PARENT_NOT_FOUND)
        }
    }

    def "Create directory metadata"() {
        given:
        primaryShareAsyncClient.create().block()
        expect:
        StepVerifier.create(primaryShareAsyncClient.createDirectoryWithResponse("testCreateDirectory", null, null, testMetadata))
            .assertNext {
                assert FileTestHelper.assertResponseStatusCode(it, 201)
            }.verifyComplete()
    }

    def "Create directory file permission"() {
        given:
        primaryShareAsyncClient.create().block()
        expect:
        StepVerifier.create(primaryShareAsyncClient.createDirectoryWithResponse("testCreateDirectory", null, filePermission, null))
            .assertNext {
                assert FileTestHelper.assertResponseStatusCode(it, 201)
            }.verifyComplete()
    }

    def "Create directory file permission key"() {
        given:
        primaryShareAsyncClient.create().block()
        def permissionKey = primaryShareAsyncClient.createPermission(filePermission).block()
        smbProperties.setFileCreationTime(namer.getUtcNow())
            .setFileLastWriteTime(namer.getUtcNow())
            .setFilePermissionKey(permissionKey)
        expect:
        StepVerifier.create(primaryShareAsyncClient.createDirectoryWithResponse("testCreateDirectory", smbProperties, null, null))
            .assertNext {
                assert FileTestHelper.assertResponseStatusCode(it, 201)
            }.verifyComplete()
    }

    def "Create directory metadata error"() {
        given:
        primaryShareAsyncClient.create().block()
        when:
        def createDirErrorVerifier = StepVerifier.create(primaryShareAsyncClient.createDirectoryWithResponse("testdirectory", null, null, Collections.singletonMap("", "value")))
        then:
        createDirErrorVerifier.verifyErrorSatisfies {
            assert FileTestHelper.assertExceptionStatusCodeAndMessage(it, 400, ShareErrorCode.EMPTY_METADATA_KEY)
        }
    }

    def "Create file"() {
        given:
        primaryShareAsyncClient.create().block()
        expect:
        StepVerifier.create(primaryShareAsyncClient.createFileWithResponse("testCreateFile", 1024, null, null, null, null))
            .assertNext {
                assert FileTestHelper.assertResponseStatusCode(it, 201)
            }.verifyComplete()
    }

    def "Create file file permission"() {
        given:
        primaryShareAsyncClient.create().block()

        expect:
        StepVerifier.create(primaryShareAsyncClient.createFileWithResponse("testCreateFile", 1024, null, null, filePermission, null))
            .assertNext {
                assert FileTestHelper.assertResponseStatusCode(it, 201)
            }.verifyComplete()
    }

    def "Create file file permission key"() {
        given:
        primaryShareAsyncClient.create().block()
        def permissionKey = primaryShareAsyncClient.createPermission(filePermission).block()
        smbProperties.setFileCreationTime(namer.getUtcNow())
            .setFileLastWriteTime(namer.getUtcNow())
            .setFilePermissionKey(permissionKey)

        expect:
        StepVerifier.create(primaryShareAsyncClient.createFileWithResponse("testCreateFile", 1024, null, smbProperties, null, null))
            .assertNext {
                assert FileTestHelper.assertResponseStatusCode(it, 201)
            }.verifyComplete()
    }

    @Unroll
    def "Create file invalid args"() {
        given:
        primaryShareAsyncClient.create().block()
        when:
        def createFileErrorVerifier = StepVerifier.create(primaryShareAsyncClient.createFileWithResponse(fileName, maxSize, null, null, null, null))
        then:
        createFileErrorVerifier.verifyErrorSatisfies {
            assert FileTestHelper.assertExceptionStatusCodeAndMessage(it, statusCode, errMsg)
        }
        where:
        fileName    | maxSize | statusCode | errMsg
        "testfile:" | 1024    | 400        | ShareErrorCode.INVALID_RESOURCE_NAME
        "fileName"  | -1      | 400        | ShareErrorCode.OUT_OF_RANGE_INPUT

    }

    def "Create file lease"() {
        given:
        primaryShareAsyncClient.create().block()
        primaryShareAsyncClient.getFileClient("testCreateFile").create(512).block()
        def leaseId = createLeaseClient(primaryShareAsyncClient.getFileClient("testCreateFile")).acquireLease().block()

        expect:
        StepVerifier.create(primaryShareAsyncClient.createFileWithResponse("testCreateFile", 1024, null, null, null,
            null, new ShareRequestConditions().setLeaseId(leaseId)))
            .expectNextCount(1).verifyComplete()
    }

    def "Create file lease fail"() {
        given:
        primaryShareAsyncClient.create().block()
        primaryShareAsyncClient.getFileClient("testCreateFile").create(512).block()
        createLeaseClient(primaryShareAsyncClient.getFileClient("testCreateFile")).acquireLease().block()

        expect:
        StepVerifier.create(primaryShareAsyncClient.createFileWithResponse("testCreateFile", 1024, null, null, null,
            null, new ShareRequestConditions().setLeaseId(namer.getRandomUuid())))
            .verifyError(ShareStorageException)
    }

    def "Create file maxOverload"() {
        given:
        primaryShareAsyncClient.create().block()
        ShareFileHttpHeaders httpHeaders = new ShareFileHttpHeaders().setContentType("txt")
        smbProperties.setFileCreationTime(namer.getUtcNow())
            .setFileLastWriteTime(namer.getUtcNow())
        expect:
        StepVerifier.create(primaryShareAsyncClient.createFileWithResponse("testCreateFile", 1024, httpHeaders, smbProperties, filePermission, testMetadata))
                .assertNext {
                assert FileTestHelper.assertResponseStatusCode(it, 201)
            }.verifyComplete()
    }

    @Unroll
    def "Create file maxOverload invalid args"() {
        given:
        primaryShareAsyncClient.create().block()

        when:

        def createFileVerifier = StepVerifier.create(primaryShareAsyncClient.createFileWithResponse(fileName, maxSize, httpHeaders, null, null, metadata))

        then:
        createFileVerifier.verifyErrorSatisfies {
            assert FileTestHelper.assertExceptionStatusCodeAndMessage(it, 400, errMsg)
        }

        where:
        fileName    | maxSize | httpHeaders                                           | metadata                              | errMsg
        "testfile:" | 1024    | null                                                  | testMetadata                          | ShareErrorCode.INVALID_RESOURCE_NAME
        "fileName"  | -1      | null                                                  | testMetadata                          | ShareErrorCode.OUT_OF_RANGE_INPUT
        "fileName"  | 1024    | new ShareFileHttpHeaders().setContentMd5(new byte[0]) | testMetadata                          | ShareErrorCode.INVALID_HEADER_VALUE
        "fileName"  | 1024    | null                                                  | Collections.singletonMap("", "value") | ShareErrorCode.EMPTY_METADATA_KEY

    }

    def "Delete directory"() {
        given:
        def directoryName = "testCreateDirectory"
        primaryShareAsyncClient.create().block()
        primaryShareAsyncClient.createDirectory(directoryName).block()
        expect:
        StepVerifier.create(primaryShareAsyncClient.deleteDirectoryWithResponse(directoryName))
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
            assert FileTestHelper.assertExceptionStatusCodeAndMessage(it, 404, ShareErrorCode.RESOURCE_NOT_FOUND)
        }
    }

    def "Delete file"() {
        given:
        def fileName = "testCreateFile"
        primaryShareAsyncClient.create().block()
        primaryShareAsyncClient.createFile(fileName, 1024).block()
        expect:
        StepVerifier.create(primaryShareAsyncClient.deleteFileWithResponse(fileName))
            .assertNext {
                assert FileTestHelper.assertResponseStatusCode(it, 202)
            }.verifyComplete()

    }

    def "Delete file lease"() {
        given:
        def fileName = "testCreateFile"
        primaryShareAsyncClient.create().block()
        primaryShareAsyncClient.createFile(fileName, 1024).block()
        def leaseId = createLeaseClient(primaryShareAsyncClient.getFileClient(fileName)).acquireLease().block()

        expect:
        StepVerifier.create(primaryShareAsyncClient.deleteFileWithResponse(fileName,
            new ShareRequestConditions().setLeaseId(leaseId)))
            .expectNextCount(1).verifyComplete()
    }

    def "Delete file lease fail"() {
        given:
        def fileName = "testCreateFile"
        primaryShareAsyncClient.create().block()
        primaryShareAsyncClient.createFile(fileName, 1024).block()
        createLeaseClient(primaryShareAsyncClient.getFileClient(fileName)).acquireLease().block()

        expect:
        StepVerifier.create(primaryShareAsyncClient.deleteFileWithResponse(fileName,
            new ShareRequestConditions().setLeaseId(namer.getRandomUuid())))
            .verifyError(ShareStorageException)
    }

    def "Delete file error"() {
        given:
        primaryShareAsyncClient.create().block()
        when:
        def deleteFileErrorVerifier = StepVerifier.create(primaryShareAsyncClient.deleteFile("testdirectory"))
        then:
        deleteFileErrorVerifier.verifyErrorSatisfies {
            assert FileTestHelper.assertExceptionStatusCodeAndMessage(it, 404, ShareErrorCode.RESOURCE_NOT_FOUND)
        }
    }

    def "Create permission"() {
        given:
        primaryShareAsyncClient.create().block()

        expect:
        StepVerifier.create(primaryShareAsyncClient.createPermissionWithResponse(filePermission))
            .assertNext() {
                assert FileTestHelper.assertResponseStatusCode(it, 201)
            }.verifyComplete()
    }

    def "Create and get permission"() {
        given:
        primaryShareAsyncClient.create().block()
        def filePermissionKey = primaryShareAsyncClient.createPermission(filePermission).block()

        expect:
        StepVerifier.create(primaryShareAsyncClient.getPermissionWithResponse(filePermissionKey))
            .assertNext {
                assert FileTestHelper.assertResponseStatusCode(it, 200)
            }.verifyComplete()
    }

    def "Create permission error"() {
        given:
        primaryShareAsyncClient.create().block()

        expect:
        // Invalid permission
        StepVerifier.create(primaryShareAsyncClient.createPermissionWithResponse("abcde"))
            .verifyErrorSatisfies {
                assert FileTestHelper.assertExceptionStatusCodeAndMessage(it, 400, ShareErrorCode.fromString("FileInvalidPermission"))
            }
    }

    def "Get permission error"() {
        given:
        primaryShareAsyncClient.create().block()

        expect:
        // Invalid permission key
        StepVerifier.create(primaryShareAsyncClient.getPermissionWithResponse("abcde"))
            .verifyErrorSatisfies {
                assert FileTestHelper.assertExceptionStatusCodeAndMessage(it, 400, ShareErrorCode.INVALID_HEADER_VALUE)
            }
    }

    def "Get snapshot id"() {
        given:
        def snapshot = OffsetDateTime.of(LocalDateTime.of(2000, 1, 1,
            1, 1), ZoneOffset.UTC).toString()
        when:
        def shareSnapshotClient = shareBuilderHelper(shareName).snapshot(snapshot).buildAsyncClient()
        then:
        snapshot == shareSnapshotClient.getSnapshotId()
    }

    def "Get Share Name"() {
        expect:
        shareName == primaryShareAsyncClient.getShareName()
    }
}
