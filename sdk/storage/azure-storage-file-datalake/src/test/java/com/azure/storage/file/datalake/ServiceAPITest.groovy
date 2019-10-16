// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake

import com.azure.core.http.rest.Response
import com.azure.storage.blob.models.BlobContainerItem
import com.azure.storage.blob.models.StorageException
import com.azure.storage.file.datalake.models.FileSystemListDetails
import com.azure.storage.file.datalake.models.ListFileSystemsOptions
import com.azure.storage.file.datalake.models.UserDelegationKey

import java.time.Duration
import java.time.OffsetDateTime

class ServiceAPITest extends APISpec {

    def "List file systems"() {
        when:
        def response =
            primaryDataLakeServiceClient.listFileSystems(new ListFileSystemsOptions().setPrefix(fileSystemPrefix + testName), null)

        then:
        for (BlobContainerItem c : response) {
            assert c.getName().startsWith(fileSystemPrefix)
            assert c.getProperties().getLastModified() != null
            assert c.getProperties().getEtag() != null
            assert c.getProperties().getLeaseStatus() != null
            assert c.getProperties().getLeaseState() != null
            assert c.getProperties().getLeaseDuration() == null
            assert c.getProperties().getPublicAccess() == null
            assert !c.getProperties().isHasLegalHold()
            assert !c.getProperties().isHasImmutabilityPolicy()
        }
    }

    def "List file systems min"() {
        when:
        primaryDataLakeServiceClient.listFileSystems().iterator().hasNext()

        then:
        notThrown(StorageException)
    }

    def "List file systems marker"() {
        setup:
        for (int i = 0; i < 10; i++) {
            primaryDataLakeServiceClient.createFileSystem(generateFileSystemName())
        }

        Iterator<BlobContainerItem> listResponse = primaryDataLakeServiceClient.listFileSystems().iterator()
        String firstContainerName = listResponse.next().getName()

        expect:
        // Assert that the second segment is indeed after the first alphabetically
        firstContainerName < listResponse.next().getName()
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
        def NUM_CONTAINERS = 5
        def PAGE_RESULTS = 3

        def containers = [] as Collection<FileSystemClient>
        for (i in (1..NUM_CONTAINERS)) {
            containers << primaryDataLakeServiceClient.createFileSystem(generateFileSystemName())
        }

        expect:
        primaryDataLakeServiceClient.listFileSystems(new ListFileSystemsOptions().setMaxResultsPerPage(PAGE_RESULTS), null)
            .iterableByPage().iterator().next().getValue().size() == PAGE_RESULTS

        cleanup:
        containers.each { container -> container.delete() }
    }

    def "List file systems error"() {
        when:
        primaryDataLakeServiceClient.listFileSystems().streamByPage("garbage continuation token").count()

        then:
        thrown(StorageException)
    }

    def "List file systems with timeout still backed by PagedFlux"() {
        setup:
        def NUM_CONTAINERS = 5
        def PAGE_RESULTS = 3

        def containers = [] as Collection<FileSystemClient>
        for (i in (1..NUM_CONTAINERS)) {
            containers << primaryDataLakeServiceClient.createFileSystem(generateFileSystemName())
        }

        when: "Consume results by page"
        primaryDataLakeServiceClient.listFileSystems(new ListFileSystemsOptions().setMaxResultsPerPage(PAGE_RESULTS), Duration.ofSeconds(10)).streamByPage().count()

        then: "Still have paging functionality"
        notThrown(Exception)

        cleanup:
        containers.each { container -> container.delete() }
    }

    def "Get UserDelegationKey"() {
        setup:
        def start = OffsetDateTime.now()
        def expiry = start.plusDays(1)

        Response<UserDelegationKey> response = getOAuthServiceClient().getUserDelegationKeyWithResponse(start, expiry, null, null)

        expect:
        response.getStatusCode() == 200
        response.getValue() != null
        response.getValue().getSignedOid() != null
        response.getValue().getSignedTid() != null
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

}
