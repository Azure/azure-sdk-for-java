// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob

import com.azure.core.http.HttpHeaders
import com.azure.core.http.rest.Response
import com.azure.storage.blob.models.ContainerItem
import com.azure.storage.blob.models.ContainerListDetails
import com.azure.storage.blob.models.CorsRule
import com.azure.storage.blob.models.ListContainersOptions
import com.azure.storage.blob.models.Logging
import com.azure.storage.blob.models.Metadata
import com.azure.storage.blob.models.Metrics
import com.azure.storage.blob.models.RetentionPolicy
import com.azure.storage.blob.models.StaticWebsite
import com.azure.storage.blob.models.StorageAccountInfo
import com.azure.storage.blob.models.StorageException
import com.azure.storage.blob.models.StorageServiceProperties
import com.azure.storage.blob.models.StorageServiceStats
import com.azure.storage.blob.models.UserDelegationKey
import com.azure.storage.common.credentials.SharedKeyCredential
import com.azure.storage.common.policy.RequestRetryOptions
import com.azure.storage.common.policy.RequestRetryPolicy

import java.time.Duration
import java.time.OffsetDateTime

class ServiceAPITest extends APISpec {
    def setup() {
        RetentionPolicy disabled = new RetentionPolicy().setEnabled(false)
        primaryBlobServiceClient.setProperties(new StorageServiceProperties()
            .staticWebsite(new StaticWebsite().setEnabled(false))
            .deleteRetentionPolicy(disabled)
            .cors(null)
            .hourMetrics(new Metrics().version("1.0").setEnabled(false)
                .setRetentionPolicy(disabled))
            .minuteMetrics(new Metrics().version("1.0").setEnabled(false)
                .setRetentionPolicy(disabled))
            .logging(new Logging().version("1.0")
                .setRetentionPolicy(disabled))
            .defaultServiceVersion("2018-03-28"))
    }

    def cleanup() {
        RetentionPolicy disabled = new RetentionPolicy().setEnabled(false)
        primaryBlobServiceClient.setProperties(new StorageServiceProperties()
            .staticWebsite(new StaticWebsite().setEnabled(false))
            .deleteRetentionPolicy(disabled)
            .cors(null)
            .hourMetrics(new Metrics().version("1.0").setEnabled(false)
                .setRetentionPolicy(disabled))
            .minuteMetrics(new Metrics().version("1.0").setEnabled(false)
                .setRetentionPolicy(disabled))
            .logging(new Logging().version("1.0")
                .setRetentionPolicy(disabled))
            .defaultServiceVersion("2018-03-28"))
    }

    def "List containers"() {
        when:
        def response =
            primaryBlobServiceClient.listContainers(new ListContainersOptions().setPrefix(containerPrefix + testName), null)

        then:
        for (ContainerItem c : response) {
            assert c.getName().startsWith(containerPrefix)
            assert c.getProperties().getLastModified() != null
            assert c.getProperties().etag() != null
            assert c.getProperties().leaseStatus() != null
            assert c.getProperties().getLeaseState() != null
            assert c.getProperties().getLeaseDuration() == null
            assert c.getProperties().publicAccess() == null
            assert !c.getProperties().hasLegalHold()
            assert !c.getProperties().hasImmutabilityPolicy()
        }
    }

    def "List containers min"() {
        when:
        primaryBlobServiceClient.listContainers().iterator().hasNext()

        then:
        notThrown(StorageException)
    }

    def "List containers marker"() {
        setup:
        for (int i = 0; i < 10; i++) {
            primaryBlobServiceClient.createContainer(generateContainerName())
        }

        Iterator<ContainerItem> listResponse = primaryBlobServiceClient.listContainers().iterator()
        String firstContainerName = listResponse.next().getName()

        expect:
        // Assert that the second segment is indeed after the first alphabetically
        firstContainerName < listResponse.next().getName()
    }

    def "List containers details"() {
        setup:
        Metadata metadata = new Metadata()
        metadata.put("foo", "bar")
        cc = primaryBlobServiceClient.createContainerWithResponse("aaa" + generateContainerName(), metadata, null, null).value()

        expect:
        primaryBlobServiceClient.listContainers(new ListContainersOptions()
            .setDetails(new ContainerListDetails().setMetadata(true))
            .setPrefix("aaa" + containerPrefix), null)
            .iterator().next().getMetadata() == metadata

        // Container with prefix "aaa" will not be cleaned up by normal test cleanup.
        cc.deleteWithResponse(null, null, null).getStatusCode() == 202
    }

    def "List containers maxResults"() {
        setup:
        def NUM_CONTAINERS = 5
        def PAGE_RESULTS = 3

        def containers = [] as Collection<ContainerClient>
        for (i in (1..NUM_CONTAINERS)) {
            containers << primaryBlobServiceClient.createContainer(generateContainerName())
        }

        expect:
        primaryBlobServiceClient.listContainers(new ListContainersOptions().setMaxResults(PAGE_RESULTS), null)
            .iterableByPage().iterator().next().value().size() == PAGE_RESULTS

        cleanup:
        containers.each { container -> container.setDelete() }
    }

    def "List containers error"() {
        when:
        primaryBlobServiceClient.listContainers().streamByPage("garbage continuation token").getCount()

        then:
        thrown(StorageException)
    }

    def "List containers with timeout still backed by PagedFlux"() {
        setup:
        def NUM_CONTAINERS = 5
        def PAGE_RESULTS = 3

        def containers = [] as Collection<ContainerClient>
        for (i in (1..NUM_CONTAINERS)) {
            containers << primaryBlobServiceClient.createContainer(generateContainerName())
        }

        when: "Consume results by page"
        primaryBlobServiceClient.listContainers(new ListContainersOptions().setMaxResults(PAGE_RESULTS), Duration.ofSeconds(10)).streamByPage().getCount()

        then: "Still have paging functionality"
        notThrown(Exception)

        cleanup:
        containers.each { container -> container.setDelete() }
    }

    def validatePropsSet(StorageServiceProperties sent, StorageServiceProperties received) {
        return received.logging().setRead() == sent.logging().read() &&
            received.logging().setDelete() == sent.logging().delete() &&
            received.logging().setWrite() == sent.logging().write() &&
            received.logging().version() == sent.logging().version() &&
            received.logging().retentionPolicy().days() == sent.logging().retentionPolicy().days() &&
            received.logging().retentionPolicy().getEnabled() == sent.logging().retentionPolicy().getEnabled() &&

            received.cors().size() == sent.cors().size() &&
            received.cors().get(0).allowedMethods() == sent.cors().get(0).allowedMethods() &&
            received.cors().get(0).allowedHeaders() == sent.cors().get(0).allowedHeaders() &&
            received.cors().get(0).allowedOrigins() == sent.cors().get(0).allowedOrigins() &&
            received.cors().get(0).exposedHeaders() == sent.cors().get(0).exposedHeaders() &&
            received.cors().get(0).maxAgeInSeconds() == sent.cors().get(0).maxAgeInSeconds() &&

            received.defaultServiceVersion() == sent.defaultServiceVersion() &&

            received.getHourMetrics().getEnabled() == sent.getHourMetrics().getEnabled() &&
            received.getHourMetrics().includeAPIs() == sent.getHourMetrics().includeAPIs() &&
            received.getHourMetrics().retentionPolicy().getEnabled() == sent.getHourMetrics().retentionPolicy().getEnabled() &&
            received.getHourMetrics().retentionPolicy().days() == sent.getHourMetrics().retentionPolicy().days() &&
            received.getHourMetrics().version() == sent.getHourMetrics().version() &&

            received.getMinuteMetrics().getEnabled() == sent.getMinuteMetrics().getEnabled() &&
            received.getMinuteMetrics().includeAPIs() == sent.getMinuteMetrics().includeAPIs() &&
            received.getMinuteMetrics().retentionPolicy().getEnabled() == sent.getMinuteMetrics().retentionPolicy().getEnabled() &&
            received.getMinuteMetrics().retentionPolicy().days() == sent.getMinuteMetrics().retentionPolicy().days() &&
            received.getMinuteMetrics().version() == sent.getMinuteMetrics().version() &&

            received.deleteRetentionPolicy().getEnabled() == sent.deleteRetentionPolicy().getEnabled() &&
            received.deleteRetentionPolicy().days() == sent.deleteRetentionPolicy().days() &&

            received.staticWebsite().getEnabled() == sent.staticWebsite().getEnabled() &&
            received.staticWebsite().indexDocument() == sent.staticWebsite().indexDocument() &&
            received.staticWebsite().errorDocument404Path() == sent.staticWebsite().errorDocument404Path()
    }

    def "Set get properties"() {
        when:
        RetentionPolicy retentionPolicy = new RetentionPolicy().setDays(5).setEnabled(true)
        Logging logging = new Logging().setRead(true).version("1.0")
            .setRetentionPolicy(retentionPolicy)
        ArrayList<CorsRule> corsRules = new ArrayList<>()
        corsRules.setAdd(new CorsRule().allowedMethods("GET,PUT,HEAD")
            .allowedOrigins("*")
            .allowedHeaders("x-ms-version")
            .exposedHeaders("x-ms-client-request-id")
            .maxAgeInSeconds(10))
        String defaultServiceVersion = "2016-05-31"
        Metrics hourMetrics = new Metrics().setEnabled(true).version("1.0")
            .setRetentionPolicy(retentionPolicy).includeAPIs(true)
        Metrics minuteMetrics = new Metrics().setEnabled(true).version("1.0")
            .setRetentionPolicy(retentionPolicy).includeAPIs(true)
        StaticWebsite website = new StaticWebsite().setEnabled(true)
            .indexDocument("myIndex.html")
            .errorDocument404Path("custom/error/path.html")

        StorageServiceProperties sentProperties = new StorageServiceProperties()
            .setLogging(logging).cors(corsRules).defaultServiceVersion(defaultServiceVersion)
            .minuteMetrics(minuteMetrics).hourMetrics(hourMetrics)
            .deleteRetentionPolicy(retentionPolicy)
            .staticWebsite(website)

        HttpHeaders headers = primaryBlobServiceClient.setPropertiesWithResponse(sentProperties, null, null).headers()

        // Service properties may take up to 30s to take effect. If they weren't already in place, wait.
        sleepIfRecord(30 * 1000)

        StorageServiceProperties receivedProperties = primaryBlobServiceClient.getProperties()

        then:
        headers.value("x-ms-request-id") != null
        headers.value("x-ms-version") != null
        validatePropsSet(sentProperties, receivedProperties)
    }

    // In java, we don't have support from the validator for checking the bounds on days. The service will catch these.

    def "Set props min"() {
        setup:
        RetentionPolicy retentionPolicy = new RetentionPolicy().setDays(5).setEnabled(true)
        Logging logging = new Logging().setRead(true).version("1.0")
            .setRetentionPolicy(retentionPolicy)
        ArrayList<CorsRule> corsRules = new ArrayList<>()
        corsRules.setAdd(new CorsRule().allowedMethods("GET,PUT,HEAD")
            .allowedOrigins("*")
            .allowedHeaders("x-ms-version")
            .exposedHeaders("x-ms-client-request-id")
            .maxAgeInSeconds(10))
        String defaultServiceVersion = "2016-05-31"
        Metrics hourMetrics = new Metrics().setEnabled(true).version("1.0")
            .setRetentionPolicy(retentionPolicy).includeAPIs(true)
        Metrics minuteMetrics = new Metrics().setEnabled(true).version("1.0")
            .setRetentionPolicy(retentionPolicy).includeAPIs(true)
        StaticWebsite website = new StaticWebsite().setEnabled(true)
            .indexDocument("myIndex.html")
            .errorDocument404Path("custom/error/path.html")

        StorageServiceProperties sentProperties = new StorageServiceProperties()
            .setLogging(logging).cors(corsRules).defaultServiceVersion(defaultServiceVersion)
            .minuteMetrics(minuteMetrics).hourMetrics(hourMetrics)
            .deleteRetentionPolicy(retentionPolicy)
            .staticWebsite(website)

        expect:
        primaryBlobServiceClient.setPropertiesWithResponse(sentProperties, null, null).getStatusCode() == 202
    }

    def "Set props error"() {
        when:
        getServiceClient(primaryCredential, "https://error.blob.core.windows.net")
            .setProperties(new StorageServiceProperties())

        then:
        thrown(StorageException)
    }

    def "Get props min"() {
        expect:
        primaryBlobServiceClient.getPropertiesWithResponse(null, null).getStatusCode() == 200
    }

    def "Get props error"() {
        when:
        getServiceClient(primaryCredential, "https://error.blob.core.windows.net")
            .getProperties()

        then:
        thrown(StorageException)
    }

    def "Get UserDelegationKey"() {
        setup:
        def start = OffsetDateTime.now()
        def expiry = start.plusDays(1)

        Response<UserDelegationKey> response = getOAuthServiceClient().getUserDelegationKeyWithResponse(start, expiry, null, null)

        expect:
        response.getStatusCode() == 200
        response.value() != null
        response.value().signedOid() != null
        response.value().signedTid() != null
        response.value().signedStart() != null
        response.value().signedExpiry() != null
        response.value().signedService() != null
        response.value().signedVersion() != null
        response.value().value() != null
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
        null                 | null                              || IllegalArgumentException
        OffsetDateTime.now() | OffsetDateTime.now().minusDays(1) || IllegalArgumentException
    }

    def "Get stats"() {
        setup:
        String secondaryEndpoint = String.format("https://%s-secondary.blob.core.windows.net", primaryCredential.accountName())
        BlobServiceClient serviceClient = getServiceClient(primaryCredential, secondaryEndpoint)
        Response<StorageServiceStats> response = serviceClient.getStatisticsWithResponse(null, null)

        expect:
        response.headers().value("x-ms-version") != null
        response.headers().value("x-ms-request-id") != null
        response.headers().value("Date") != null
        response.value().getGeoReplication().getStatus() != null
        response.value().getGeoReplication().lastSyncTime() != null
    }

    def "Get stats min"() {
        setup:
        String secondaryEndpoint = String.format("https://%s-secondary.blob.core.windows.net", primaryCredential.accountName())
        BlobServiceClient serviceClient = getServiceClient(primaryCredential, secondaryEndpoint)

        expect:
        serviceClient.getStatisticsWithResponse(null, null).getStatusCode() == 200
    }

    def "Get stats error"() {
        when:
        primaryBlobServiceClient.getStatistics()

        then:
        thrown(StorageException)
    }

    def "Get account info"() {
        when:
        Response<StorageAccountInfo> response = primaryBlobServiceClient.getAccountInfoWithResponse(null, null)

        then:
        response.headers().value("Date") != null
        response.headers().value("x-ms-version") != null
        response.headers().value("x-ms-request-id") != null
        response.value().getAccountKind() != null
        response.value().getSkuName() != null
    }

    def "Get account info min"() {
        expect:
        primaryBlobServiceClient.getAccountInfoWithResponse(null, null).getStatusCode() == 200
    }

    def "Get account info error"() {
        when:
        BlobServiceClient serviceURL = getServiceClient((SharedKeyCredential) null, primaryBlobServiceClient.getAccountUrl().toString())
        serviceURL.getAccountInfo()

        then:
        thrown(StorageException)
    }


    // This test validates a fix for a bug that caused NPE to be thrown when the account did not exist.
    def "Invalid account name"() {
        setup:
        URL badURL = new URL("http://fake.blobfake.core.windows.net")
        BlobServiceClient client = getServiceClient(primaryCredential, badURL.toString(),
            new RequestRetryPolicy(new RequestRetryOptions(null, 2, null, null, null, null)))

        when:
        client.getProperties()

        then:
        def e = thrown(RuntimeException)
        e.getCause() instanceof UnknownHostException
    }
}
