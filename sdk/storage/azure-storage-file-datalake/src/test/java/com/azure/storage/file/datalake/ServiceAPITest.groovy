// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake

import com.azure.core.http.rest.PagedIterable
import com.azure.core.http.rest.Response
import com.azure.core.test.TestMode
import com.azure.core.util.Context
import com.azure.identity.DefaultAzureCredentialBuilder
import com.azure.storage.blob.BlobUrlParts
import com.azure.storage.blob.models.BlobStorageException
import com.azure.storage.common.ParallelTransferOptions
import com.azure.storage.common.test.shared.extensions.PlaybackOnly
import com.azure.storage.common.test.shared.extensions.RequiredServiceVersion
import com.azure.storage.file.datalake.models.DataLakeAnalyticsLogging
import com.azure.storage.file.datalake.models.DataLakeCorsRule
import com.azure.storage.file.datalake.models.DataLakeMetrics
import com.azure.storage.file.datalake.models.DataLakeRetentionPolicy
import com.azure.storage.file.datalake.models.DataLakeServiceProperties
import com.azure.storage.file.datalake.models.DataLakeStaticWebsite
import com.azure.storage.file.datalake.models.DataLakeStorageException
import com.azure.storage.file.datalake.models.FileSystemItem
import com.azure.storage.file.datalake.models.FileSystemListDetails
import com.azure.storage.file.datalake.models.ListFileSystemsOptions
import com.azure.storage.file.datalake.models.UserDelegationKey
import com.azure.storage.file.datalake.options.FileSystemUndeleteOptions
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import spock.lang.IgnoreIf
import spock.lang.ResourceLock

import java.time.Duration
import java.time.OffsetDateTime

class ServiceAPITest extends APISpec {

    def validatePropsSet(DataLakeServiceProperties sent, DataLakeServiceProperties received) {
        return received.getLogging().isRead() == sent.getLogging().isRead() &&
            received.getLogging().isDelete() == sent.getLogging().isDelete() &&
            received.getLogging().isWrite() == sent.getLogging().isWrite() &&
            received.getLogging().getVersion() == sent.getLogging().getVersion() &&
            received.getLogging().getRetentionPolicy().getDays() == sent.getLogging().getRetentionPolicy().getDays() &&
            received.getLogging().getRetentionPolicy().isEnabled() == sent.getLogging().getRetentionPolicy().isEnabled() &&

            received.getCors().size() == sent.getCors().size() &&
            received.getCors().get(0).getAllowedMethods() == sent.getCors().get(0).getAllowedMethods() &&
            received.getCors().get(0).getAllowedHeaders() == sent.getCors().get(0).getAllowedHeaders() &&
            received.getCors().get(0).getAllowedOrigins() == sent.getCors().get(0).getAllowedOrigins() &&
            received.getCors().get(0).getExposedHeaders() == sent.getCors().get(0).getExposedHeaders() &&
            received.getCors().get(0).getMaxAgeInSeconds() == sent.getCors().get(0).getMaxAgeInSeconds() &&

            received.getDefaultServiceVersion() == sent.getDefaultServiceVersion() &&

            received.getHourMetrics().isEnabled() == sent.getHourMetrics().isEnabled() &&
            received.getHourMetrics().isIncludeApis() == sent.getHourMetrics().isIncludeApis() &&
            received.getHourMetrics().getRetentionPolicy().isEnabled() == sent.getHourMetrics().getRetentionPolicy().isEnabled() &&
            received.getHourMetrics().getRetentionPolicy().getDays() == sent.getHourMetrics().getRetentionPolicy().getDays() &&
            received.getHourMetrics().getVersion() == sent.getHourMetrics().getVersion() &&

            received.getMinuteMetrics().isEnabled() == sent.getMinuteMetrics().isEnabled() &&
            received.getMinuteMetrics().isIncludeApis() == sent.getMinuteMetrics().isIncludeApis() &&
            received.getMinuteMetrics().getRetentionPolicy().isEnabled() == sent.getMinuteMetrics().getRetentionPolicy().isEnabled() &&
            received.getMinuteMetrics().getRetentionPolicy().getDays() == sent.getMinuteMetrics().getRetentionPolicy().getDays() &&
            received.getMinuteMetrics().getVersion() == sent.getMinuteMetrics().getVersion() &&

            received.getDeleteRetentionPolicy().isEnabled() == sent.getDeleteRetentionPolicy().isEnabled() &&
            received.getDeleteRetentionPolicy().getDays() == sent.getDeleteRetentionPolicy().getDays() &&

            received.getStaticWebsite().isEnabled() == sent.getStaticWebsite().isEnabled() &&
            received.getStaticWebsite().getIndexDocument() == sent.getStaticWebsite().getIndexDocument() &&
            received.getStaticWebsite().getErrorDocument404Path() == sent.getStaticWebsite().getErrorDocument404Path()
    }

    @ResourceLock("ServiceProperties")
    def "Set get properties"() {
        when:
        def retentionPolicy = new DataLakeRetentionPolicy().setDays(5).setEnabled(true)
        def logging = new DataLakeAnalyticsLogging().setRead(true).setVersion("1.0")
            .setRetentionPolicy(retentionPolicy)
        def corsRules = new ArrayList<DataLakeCorsRule>()
        corsRules.add(new DataLakeCorsRule().setAllowedMethods("GET,PUT,HEAD")
            .setAllowedOrigins("*")
            .setAllowedHeaders("x-ms-version")
            .setExposedHeaders("x-ms-client-request-id")
            .setMaxAgeInSeconds(10))
        def defaultServiceVersion = "2016-05-31"
        def hourMetrics = new DataLakeMetrics().setEnabled(true).setVersion("1.0")
            .setRetentionPolicy(retentionPolicy).setIncludeApis(true)
        def minuteMetrics = new DataLakeMetrics().setEnabled(true).setVersion("1.0")
            .setRetentionPolicy(retentionPolicy).setIncludeApis(true)
        def website = new DataLakeStaticWebsite().setEnabled(true)
            .setIndexDocument("myIndex.html")
            .setErrorDocument404Path("custom/error/path.html")

        def sentProperties = new DataLakeServiceProperties()
            .setLogging(logging).setCors(corsRules).setDefaultServiceVersion(defaultServiceVersion)
            .setMinuteMetrics(minuteMetrics).setHourMetrics(hourMetrics)
            .setDeleteRetentionPolicy(retentionPolicy)
            .setStaticWebsite(website)

        def headers = primaryDataLakeServiceClient.setPropertiesWithResponse(sentProperties, null, null).getHeaders()

        // Service properties may take up to 30s to take effect. If they weren't already in place, wait.
        sleepIfRecord(30 * 1000)

        def receivedProperties = primaryDataLakeServiceClient.getProperties()

        then:
        headers.getValue("x-ms-request-id") != null
        headers.getValue("x-ms-version") != null
        validatePropsSet(sentProperties, receivedProperties)
    }

    // In java, we don't have support from the validator for checking the bounds on days. The service will catch these.
    @ResourceLock("ServiceProperties")
    def "Set props min"() {
        setup:
        def retentionPolicy = new DataLakeRetentionPolicy().setDays(5).setEnabled(true)
        def logging = new DataLakeAnalyticsLogging().setRead(true).setVersion("1.0")
            .setRetentionPolicy(retentionPolicy)
        def corsRules = new ArrayList<DataLakeCorsRule>()
        corsRules.add(new DataLakeCorsRule().setAllowedMethods("GET,PUT,HEAD")
            .setAllowedOrigins("*")
            .setAllowedHeaders("x-ms-version")
            .setExposedHeaders("x-ms-client-request-id")
            .setMaxAgeInSeconds(10))
        def defaultServiceVersion = "2016-05-31"
        def hourMetrics = new DataLakeMetrics().setEnabled(true).setVersion("1.0")
            .setRetentionPolicy(retentionPolicy).setIncludeApis(true)
        def minuteMetrics = new DataLakeMetrics().setEnabled(true).setVersion("1.0")
            .setRetentionPolicy(retentionPolicy).setIncludeApis(true)
        def website = new DataLakeStaticWebsite().setEnabled(true)
            .setIndexDocument("myIndex.html")
            .setErrorDocument404Path("custom/error/path.html")


        def sentProperties = new DataLakeServiceProperties()
            .setLogging(logging).setCors(corsRules).setDefaultServiceVersion(defaultServiceVersion)
            .setMinuteMetrics(minuteMetrics).setHourMetrics(hourMetrics)
            .setDeleteRetentionPolicy(retentionPolicy)
            .setStaticWebsite(website)

        expect:
        primaryDataLakeServiceClient.setPropertiesWithResponse(sentProperties, null, null).getStatusCode() == 202
    }

    @ResourceLock("ServiceProperties")
    def "Set props cors check"() {
        setup:
        def serviceProperties = primaryDataLakeServiceClient.getProperties()

        // Some properties are not set and this test validates that they are not null when sent to the service
        def rule = new DataLakeCorsRule()
        rule.setAllowedOrigins("microsoft.com")
        rule.setMaxAgeInSeconds(60)
        rule.setAllowedMethods("GET")
        rule.setAllowedHeaders("x-ms-version")

        serviceProperties.setCors(Collections.singletonList(rule))

        expect:
        primaryDataLakeServiceClient.setPropertiesWithResponse(serviceProperties, null, null).getStatusCode() == 202
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "V2019_12_12")
    @ResourceLock("ServiceProperties")
    def "Set props static website"() {
        setup:
        def serviceProperties = primaryDataLakeServiceClient.getProperties()
        def errorDocument404Path = "error/404.html"
        def defaultIndexDocumentPath = "index.html"

        serviceProperties.setStaticWebsite(new DataLakeStaticWebsite()
            .setEnabled(true)
            .setErrorDocument404Path(errorDocument404Path)
            .setDefaultIndexDocumentPath(defaultIndexDocumentPath)
        )

        when:
        Response<Void> resp = primaryDataLakeServiceClient.setPropertiesWithResponse(serviceProperties, null, null)

        then:
        resp.getStatusCode() == 202
        def staticWebsite = primaryDataLakeServiceClient.getProperties().getStaticWebsite()
        staticWebsite.isEnabled()
        staticWebsite.getErrorDocument404Path() == errorDocument404Path
        staticWebsite.getDefaultIndexDocumentPath() == defaultIndexDocumentPath
    }

    @ResourceLock("ServiceProperties")
    def "Set props error"() {
        when:
        getServiceClient(environment.dataLakeAccount.credential, "https://error.blob.core.windows.net")
            .setProperties(new DataLakeServiceProperties())

        then:
        thrown(DataLakeStorageException)
    }

    @ResourceLock("ServiceProperties")
    def "Get props min"() {
        expect:
        primaryDataLakeServiceClient.getPropertiesWithResponse(null, null).getStatusCode() == 200
    }

    @ResourceLock("ServiceProperties")
    def "Get props error"() {
        when:
        getServiceClient(environment.dataLakeAccount.credential, "https://error.blob.core.windows.net")
            .getProperties()

        then:
        thrown(DataLakeStorageException)
    }

    def "List file systems"() {
        when:
        def response =
            primaryDataLakeServiceClient.listFileSystems(new ListFileSystemsOptions().setPrefix(namer.getResourcePrefix()), null)

        then:
        for (FileSystemItem c : response) {
            assert c.getName().startsWith(namer.getResourcePrefix())
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
        def fileSystemName = generateFileSystemName()
        fsc = primaryDataLakeServiceClient.createFileSystemWithResponse(fileSystemName, metadata, null, null).getValue()

        expect:
        primaryDataLakeServiceClient.listFileSystems(new ListFileSystemsOptions()
            .setDetails(new FileSystemListDetails().setRetrieveMetadata(true))
            .setPrefix(fileSystemName), null)
            .iterator().next().getMetadata() == metadata
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

    def "List file systems maxResults by page"() {
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
        for (def page : primaryDataLakeServiceClient.listFileSystems(new ListFileSystemsOptions()
            .setPrefix(fileSystemPrefix)
            .setMaxResultsPerPage(), null)
            .iterableByPage(PAGE_RESULTS)) {
            assert page.getValue().size() <= PAGE_RESULTS
        }

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

    @IgnoreIf( { getEnvironment().serviceVersion != null } )
    // This tests the policy is in the right place because if it were added per retry, it would be after the credentials and auth would fail because we changed a signed header.
    def "Per call policy"() {
        def serviceClient = getServiceClient(environment.dataLakeAccount.credential, primaryDataLakeServiceClient.getAccountUrl(), getPerCallVersionPolicy())

        when: "blob endpoint"
        def response = serviceClient.createFileSystemWithResponse(generateFileSystemName(), null, null, null)

        then:
        notThrown(BlobStorageException)
        response.getHeaders().getValue("x-ms-version") == "2019-02-02"
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "V2019_12_12")
    def "Restore file system"() {
        given:
        def cc1 = primaryDataLakeServiceClient.getFileSystemClient(generateFileSystemName())
        cc1.create()
        def blobName = generatePathName()
        cc1.getFileClient(blobName).upload(data.defaultInputStream, 7)
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

    @PlaybackOnly
    def "Restore file system into other file system"() {
        given:
        def cc1 = primaryDataLakeServiceClient.getFileSystemClient(generateFileSystemName())
        cc1.create()
        def blobName = generatePathName()
        cc1.getFileClient(blobName).upload(data.defaultInputStream, 7)
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

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "V2019_12_12")
    def "Restore file system with response"() {
        given:
        def cc1 = primaryDataLakeServiceClient.getFileSystemClient(generateFileSystemName())
        cc1.create()
        def blobName = generatePathName()
        cc1.getFileClient(blobName).upload(data.defaultInputStream, 7)
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

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "V2019_12_12")
    def "Restore file system async"() {
        given:
        def cc1 = primaryDataLakeServiceAsyncClient.getFileSystemAsyncClient(generateFileSystemName())
        def blobName = generatePathName()
        def delay = environment.testMode == TestMode.PLAYBACK ? 0L : 30000L

        def blobContainerItemMono = cc1.create()
            .then(cc1.getFileAsyncClient(blobName).upload(data.defaultFlux, new ParallelTransferOptions()))
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

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "V2019_12_12")
    def "Restore file system async with response"() {
        given:
        def cc1 = primaryDataLakeServiceAsyncClient.getFileSystemAsyncClient(generateFileSystemName())
        def blobName = generatePathName()
        def delay = environment.testMode == TestMode.PLAYBACK ? 0L : 30000L

        def blobContainerItemMono = cc1.create()
            .then(cc1.getFileAsyncClient(blobName).upload(data.defaultFlux, new ParallelTransferOptions()))
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

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "V2019_12_12")
    def "Restore file system into existing file system error"() {
        given:
        def cc1 = primaryDataLakeServiceClient.getFileSystemClient(generateFileSystemName())
        cc1.create()
        def blobName = generatePathName()
        cc1.getFileClient(blobName).upload(data.defaultInputStream, 7)
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
//        def sas = primaryDataLakeServiceClient.generateAccountSas(new AccountSasSignatureValues(namer.getUtcNow().plusHours(1), AccountSasPermission.parse("rwdxlacuptf"), AccountSasService.parse("b"), AccountSasResourceType.parse("c")))
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
