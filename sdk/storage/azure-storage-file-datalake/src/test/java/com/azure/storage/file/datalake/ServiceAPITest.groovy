// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake

import com.azure.core.http.rest.PagedIterable
import com.azure.core.http.rest.Response
import com.azure.core.util.Context
import com.azure.identity.DefaultAzureCredentialBuilder
import com.azure.storage.blob.BlobUrlParts
import com.azure.storage.blob.models.BlobStorageException
import com.azure.storage.common.ParallelTransferOptions
import com.azure.storage.common.sas.AccountSasPermission
import com.azure.storage.common.sas.AccountSasResourceType
import com.azure.storage.common.sas.AccountSasService
import com.azure.storage.common.sas.AccountSasSignatureValues
import com.azure.storage.file.datalake.implementation.util.DataLakeImplUtils
import com.azure.storage.file.datalake.models.DataLakeRequestConditions
import com.azure.storage.file.datalake.models.DataLakeStorageException
import com.azure.storage.file.datalake.models.FileSystemItem
import com.azure.storage.file.datalake.models.FileSystemListDetails
import com.azure.storage.file.datalake.models.ListFileSystemsOptions
import com.azure.storage.file.datalake.models.UserDelegationKey
import com.azure.storage.file.datalake.options.FileSystemRenameOptions
import com.azure.storage.file.datalake.options.FileSystemUndeleteOptions
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

import java.time.Duration
import java.time.OffsetDateTime

class ServiceAPITest extends APISpec {

    def "List file systems"() {
        when:
        def response =
            primaryDataLakeServiceClient.listFileSystems(new ListFileSystemsOptions().setPrefix(fileSystemPrefix + testName), null)

        then:
        for (FileSystemItem c : response) {
            assert c.getName().startsWith(fileSystemPrefix)
            assert c.getProperties().getLastModified() != null
            assert c.getProperties().getETag() != null
            assert c.getProperties().getLeaseStatus() != null
            assert c.getProperties().getLeaseState() != null
            assert c.getProperties().getLeaseDuration() == null
            assert c.getProperties().getPublicAccess() == null
            assert !c.getProperties().hasLegalHold()
            assert !c.getProperties().hasImmutabilityPolicy()
        }
    }

    def "List file systems min"() {
        when:
        primaryDataLakeServiceClient.listFileSystems().iterator().hasNext()

        then:
        notThrown(DataLakeStorageException)
    }

    def "List file systems marker"() {
        setup:
        for (int i = 0; i < 10; i++) {
            primaryDataLakeServiceClient.createFileSystem(generateFileSystemName())
        }

        def listResponse = primaryDataLakeServiceClient.listFileSystems().iterator()
        def firstFileSystemName = listResponse.next().getName()

        expect:
        // Assert that the second segment is indeed after the first alphabetically
        firstFileSystemName < listResponse.next().getName()
    }

    def "List file systems details"() {
        setup:
        def metadata = new HashMap<String, String>()
        metadata.put("foo", "bar")
        fsc = primaryDataLakeServiceClient.createFileSystemWithResponse("aaa" + generateFileSystemName(), metadata, null, null).getValue()

        expect:
        primaryDataLakeServiceClient.listFileSystems(new ListFileSystemsOptions()
            .setDetails(new FileSystemListDetails().setRetrieveMetadata(true))
            .setPrefix("aaa" + fileSystemPrefix), null)
            .iterator().next().getMetadata() == metadata

        // File system with prefix "aaa" will not be cleaned up by normal test cleanup.
        fsc.deleteWithResponse(null, null, null).getStatusCode() == 202
    }

    def "List file systems maxResults"() {
        setup:
        def NUM_FILESYSTEMS = 5
        def PAGE_RESULTS = 3
        def fileSystemName = generateFileSystemName()
        def fileSystemPrefix = fileSystemName.substring(0, Math.min(60, fileSystemName.length()))

        def fileSystems = [] as Collection<DataLakeFileSystemClient>
        for (i in (1..NUM_FILESYSTEMS)) {
            fileSystems << primaryDataLakeServiceClient.createFileSystem(fileSystemPrefix + i)
        }

        expect:
        primaryDataLakeServiceClient.listFileSystems(new ListFileSystemsOptions()
            .setPrefix(fileSystemPrefix)
            .setMaxResultsPerPage(PAGE_RESULTS), null)
            .iterableByPage().iterator().next().getValue().size() == PAGE_RESULTS

        cleanup:
        fileSystems.each { fileSystem -> fileSystem.delete() }
    }

    def "List file systems error"() {
        when:
        PagedIterable<FileSystemItem> items =  primaryDataLakeServiceClient.listFileSystems()
        items.streamByPage("garbage continuation token").count()

        then:
        thrown(DataLakeStorageException)
    }

    def "List file systems with timeout still backed by PagedFlux"() {
        setup:
        def NUM_FILESYSTEMS = 5
        def PAGE_RESULTS = 3

        def fileSystems = [] as Collection<DataLakeFileSystemClient>
        for (i in (1..NUM_FILESYSTEMS)) {
            fileSystems << primaryDataLakeServiceClient.createFileSystem(generateFileSystemName())
        }

        when: "Consume results by page"
        primaryDataLakeServiceClient.listFileSystems(new ListFileSystemsOptions().setMaxResultsPerPage(PAGE_RESULTS), Duration.ofSeconds(10)).streamByPage().count()

        then: "Still have paging functionality"
        notThrown(DataLakeStorageException)

        cleanup:
        fileSystems.each { fileSystem -> fileSystem.delete() }
    }

    def "Get UserDelegationKey"() {
        setup:
        def start = OffsetDateTime.now()
        def expiry = start.plusDays(1)

        Response<UserDelegationKey> response = getOAuthServiceClient().getUserDelegationKeyWithResponse(start, expiry, null, null)

        expect:
        response.getStatusCode() == 200
        response.getValue() != null
        response.getValue().getSignedObjectId() != null
        response.getValue().getSignedTenantId() != null
        response.getValue().getSignedStart() != null
        response.getValue().getSignedExpiry() != null
        response.getValue().getSignedService() != null
        response.getValue().getSignedVersion() != null
        response.getValue().getValue() != null
    }

    def "Get UserDelegationKey min"() {
        setup:
        def expiry = OffsetDateTime.now().plusDays(1)

        def response = getOAuthServiceClient().getUserDelegationKeyWithResponse(null, expiry, null, null)

        expect:
        response.getStatusCode() == 200
    }

    def "Get UserDelegationKey error"() {
        when:
        getOAuthServiceClient().getUserDelegationKey(start, expiry)

        then:
        thrown(exception)

        where:
        start                | expiry                            || exception
        null                 | null                              || NullPointerException
        OffsetDateTime.now() | OffsetDateTime.now().minusDays(1) || IllegalArgumentException
    }

    def "Builder bearer token validation"() {
        // Technically no additional checks need to be added to datalake builder since the corresponding blob builder fails
        setup:
        String endpoint = BlobUrlParts.parse(primaryDataLakeServiceClient.getAccountUrl()).setScheme("http").toUrl()
        def builder = new DataLakeServiceClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint)

        when:
        builder.buildClient()

        then:
        thrown(IllegalArgumentException)
    }

    // This tests the policy is in the right place because if it were added per retry, it would be after the credentials and auth would fail because we changed a signed header.
    def "Per call policy"() {
        def serviceClient = getServiceClient(primaryCredential, primaryDataLakeServiceClient.getAccountUrl(), getPerCallVersionPolicy())

        when: "blob endpoint"
        def response = serviceClient.createFileSystemWithResponse(generateFileSystemName(), null, null, null)

        then:
        notThrown(BlobStorageException)
        response.getHeaders().getValue("x-ms-version") == "2019-02-02"
    }

    def "Restore file system"() {
        given:
        def cc1 = primaryDataLakeServiceClient.getFileSystemClient(generateFileSystemName())
        cc1.create()
        def blobName = generatePathName()
        cc1.getFileClient(blobName).upload(defaultInputStream.get(), 7)
        cc1.delete()
        def blobContainerItem = primaryDataLakeServiceClient.listFileSystems(
            new ListFileSystemsOptions()
                .setPrefix(cc1.getFileSystemName())
                .setDetails(new FileSystemListDetails().setRetrieveDeleted(true)),
            null).first()

        sleepIfRecord(30000)

        assert !cc1.blobContainerClient.exists() // TODO (gapra) : Expose exists on file system client

        when:
        def restoredContainerClient = primaryDataLakeServiceClient
            .undeleteFileSystem(blobContainerItem.getName(), blobContainerItem.getVersion())

        then:
        restoredContainerClient.listPaths().size() == 1
        restoredContainerClient.listPaths().first().getName() == blobName
    }

    def "Restore file system into other file system"() {
        given:
        def cc1 = primaryDataLakeServiceClient.getFileSystemClient(generateFileSystemName())
        cc1.create()
        def blobName = generatePathName()
        cc1.getFileClient(blobName).upload(defaultInputStream.get(), 7)
        cc1.delete()
        def blobContainerItem = primaryDataLakeServiceClient.listFileSystems(
            new ListFileSystemsOptions()
                .setPrefix(cc1.getFileSystemName())
                .setDetails(new FileSystemListDetails().setRetrieveDeleted(true)),
            null).first()
        def destinationFileSystemName = generateFileSystemName()

        sleepIfRecord(30000)

        when:
        def restoredContainerClient = primaryDataLakeServiceClient.undeleteFileSystemWithResponse(
            new FileSystemUndeleteOptions(blobContainerItem.getName(), blobContainerItem.getVersion())
                .setDestinationFileSystemName(destinationFileSystemName), null, Context.NONE)
            .getValue()

        then:
        restoredContainerClient.listPaths().size() == 1
        restoredContainerClient.listPaths().first().getName() == blobName
        restoredContainerClient.getFileSystemName() == destinationFileSystemName
    }

    def "Restore file system with response"() {
        given:
        def cc1 = primaryDataLakeServiceClient.getFileSystemClient(generateFileSystemName())
        cc1.create()
        def blobName = generatePathName()
        cc1.getFileClient(blobName).upload(defaultInputStream.get(), 7)
        cc1.delete()
        def blobContainerItem = primaryDataLakeServiceClient.listFileSystems(
            new ListFileSystemsOptions()
                .setPrefix(cc1.getFileSystemName())
                .setDetails(new FileSystemListDetails().setRetrieveDeleted(true)),
            null).first()

        sleepIfRecord(30000)

        when:
        def response = primaryDataLakeServiceClient.undeleteFileSystemWithResponse(
            new FileSystemUndeleteOptions(blobContainerItem.getName(), blobContainerItem.getVersion()),
            Duration.ofMinutes(1), Context.NONE)
        def restoredContainerClient = response.getValue()

        then:
        response != null
        response.getStatusCode() == 201
        restoredContainerClient.listPaths().size() == 1
        restoredContainerClient.listPaths().first().getName() == blobName
    }

    def "Restore file system async"() {
        given:
        def cc1 = primaryDataLakeServiceAsyncClient.getFileSystemAsyncClient(generateFileSystemName())
        def blobName = generatePathName()
        def delay = playbackMode() ? 0L : 30000L

        def blobContainerItemMono = cc1.create()
            .then(cc1.getFileAsyncClient(blobName).upload(defaultFlux, new ParallelTransferOptions()))
            .then(cc1.delete())
            .then(Mono.delay(Duration.ofMillis(delay)))
            .then(primaryDataLakeServiceAsyncClient.listFileSystems(
                new ListFileSystemsOptions()
                    .setPrefix(cc1.getFileSystemName())
                    .setDetails(new FileSystemListDetails().setRetrieveDeleted(true))
            ).next())

        when:
        def restoredContainerClientMono = blobContainerItemMono.flatMap {
            blobContainerItem -> primaryDataLakeServiceAsyncClient.undeleteFileSystem(blobContainerItem.getName(), blobContainerItem.getVersion())
        }

        then:
        StepVerifier.create(restoredContainerClientMono.flatMap { restoredContainerClient -> restoredContainerClient.listPaths().collectList() })
            .assertNext( {
                assert it.size() == 1
                assert it.first().getName() == blobName
            })
            .verifyComplete()
    }

    def "Restore file system async with response"() {
        given:
        def cc1 = primaryDataLakeServiceAsyncClient.getFileSystemAsyncClient(generateFileSystemName())
        def blobName = generatePathName()
        def delay = playbackMode() ? 0L : 30000L

        def blobContainerItemMono = cc1.create()
            .then(cc1.getFileAsyncClient(blobName).upload(defaultFlux, new ParallelTransferOptions()))
            .then(cc1.delete())
            .then(Mono.delay(Duration.ofMillis(delay)))
            .then(primaryDataLakeServiceAsyncClient.listFileSystems(
                new ListFileSystemsOptions()
                    .setPrefix(cc1.getFileSystemName())
                    .setDetails(new FileSystemListDetails().setRetrieveDeleted(true))
            ).next())

        when:
        def responseMono = blobContainerItemMono.flatMap {
            blobContainerItem -> primaryDataLakeServiceAsyncClient.undeleteFileSystemWithResponse(
                new FileSystemUndeleteOptions(blobContainerItem.getName(), blobContainerItem.getVersion()))
        }

        then:
        StepVerifier.create(responseMono)
            .assertNext({
                assert it != null
                assert it.getStatusCode() == 201
                assert it.getValue() != null
                assert it.getValue().getFileSystemName() == cc1.getFileSystemName()
            })
            .verifyComplete()
    }

    def "Restore file system error"() {
        when:
        primaryDataLakeServiceClient.undeleteFileSystem(generateFileSystemName(), "01D60F8BB59A4652")

        then:
        thrown(DataLakeStorageException.class)
    }

    def "Restore file system into existing file system error"() {
        given:
        def cc1 = primaryDataLakeServiceClient.getFileSystemClient(generateFileSystemName())
        cc1.create()
        def blobName = generatePathName()
        cc1.getFileClient(blobName).upload(defaultInputStream.get(), 7)
        cc1.delete()
        def blobContainerItem = primaryDataLakeServiceClient.listFileSystems(
            new ListFileSystemsOptions()
                .setPrefix(cc1.getFileSystemName())
                .setDetails(new FileSystemListDetails().setRetrieveDeleted(true)),
            null).first()

        sleepIfRecord(30000)

        when:
        def cc2 = primaryDataLakeServiceClient.createFileSystem(generateFileSystemName())
        primaryDataLakeServiceClient.undeleteFileSystemWithResponse(
            new FileSystemUndeleteOptions(blobContainerItem.getName(), blobContainerItem.getVersion())
                .setDestinationFileSystemName(cc2.getFileSystemName()), null, Context.NONE)

        then:
        thrown(DataLakeStorageException.class)
    }

//    def "Rename file system"() {
//        setup:
//        def oldName = generateFileSystemName()
//        def newName = generateFileSystemName()
//        primaryDataLakeServiceClient.createFileSystem(oldName)
//
//        when:
//        def renamedContainer = primaryDataLakeServiceClient.renameFileSystem(oldName, newName)
//
//        then:
//        renamedContainer.getPropertiesWithResponse(null, null, null).getStatusCode() == 200
//
//        cleanup:
//        renamedContainer.delete()
//    }
//
//    def "Rename file system sas"() {
//        setup:
//        def oldName = generateFileSystemName()
//        def newName = generateFileSystemName()
//        primaryDataLakeServiceClient.createFileSystem(oldName)
//        def sas = primaryDataLakeServiceClient.generateAccountSas(new AccountSasSignatureValues(getUTCNow().plusHours(1), AccountSasPermission.parse("rwdxlacuptf"), AccountSasService.parse("b"), AccountSasResourceType.parse("c")))
//        def serviceClient = getServiceClient(sas, primaryDataLakeServiceClient.getAccountUrl())
//
//        when:
//        def renamedContainer = serviceClient.renameFileSystem(oldName, newName)
//
//        then:
//        renamedContainer.getPropertiesWithResponse(null, null, null).getStatusCode() == 200
//
//        cleanup:
//        renamedContainer.delete()
//    }
//
//    @Unroll
//    def "Rename file system AC"() {
//        setup:
//        leaseID = setupFileSystemLeaseCondition(fsc, leaseID)
//        def cac = new DataLakeRequestConditions()
//            .setLeaseId(leaseID)
//
//        expect:
//        primaryDataLakeServiceClient.renameFileSystemWithResponse(
//            new FileSystemRenameOptions(fsc.getFileSystemName(), generateFileSystemName()).setRequestConditions(cac),
//            null, null).getStatusCode() == 200
//
//        where:
//        leaseID         || _
//        null            || _
//        receivedLeaseID || _
//    }
//
//    @Unroll
//    def "Rename file system AC fail"() {
//        setup:
//        def cac = new DataLakeRequestConditions()
//            .setLeaseId(leaseID)
//
//        when:
//        primaryDataLakeServiceClient.renameFileSystemWithResponse(
//            new FileSystemRenameOptions(fsc.getFileSystemName(), generateFileSystemName()).setRequestConditions(cac),
//            null, null)
//
//        then:
//        thrown(DataLakeStorageException)
//
//        where:
//        leaseID         || _
//        garbageLeaseID  || _
//    }
//
//    @Unroll
//    def "Rename file system AC illegal"() {
//        setup:
//        def ac = new DataLakeRequestConditions().setIfMatch(match).setIfNoneMatch(noneMatch).setIfModifiedSince(modified).setIfUnmodifiedSince(unmodified)
//
//        when:
//        primaryDataLakeServiceClient.renameFileSystemWithResponse(
//            new FileSystemRenameOptions(fsc.getFileSystemName(), generateFileSystemName()).setRequestConditions(ac),
//            null, null)
//
//        then:
//        thrown(UnsupportedOperationException)
//
//        where:
//        modified | unmodified | match        | noneMatch
//        oldDate  | null       | null         | null
//        null     | newDate    | null         | null
//        null     | null       | receivedEtag | null
//        null     | null       | null         | garbageEtag
//    }
//
//    def "Rename file system error"() {
//        setup:
//        def oldName = generateFileSystemName()
//        def newName = generateFileSystemName()
//
//        when:
//        primaryDataLakeServiceClient.renameFileSystem(oldName, newName)
//
//        then:
//        thrown(DataLakeStorageException)
//    }

}
