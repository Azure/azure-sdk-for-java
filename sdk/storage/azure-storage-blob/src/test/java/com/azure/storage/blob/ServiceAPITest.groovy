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
import com.azure.storage.common.policy.RequestRetryOptions
import com.azure.storage.common.policy.RequestRetryPolicy
import spock.lang.Ignore

import java.time.Duration
import java.time.OffsetDateTime

class ServiceAPITest extends APISpec {
    def setup() {
        RetentionPolicy disabled = new RetentionPolicy().enabled(false)
        primaryBlobServiceClient.setProperties(new StorageServiceProperties()
            .staticWebsite(new StaticWebsite().enabled(false))
            .deleteRetentionPolicy(disabled)
            .cors(null)
            .hourMetrics(new Metrics().version("1.0").enabled(false)
                .retentionPolicy(disabled))
            .minuteMetrics(new Metrics().version("1.0").enabled(false)
                .retentionPolicy(disabled))
            .logging(new Logging().version("1.0")
                .retentionPolicy(disabled))
            .defaultServiceVersion("2018-03-28"))
    }

    def cleanup() {
        RetentionPolicy disabled = new RetentionPolicy().enabled(false)
        primaryBlobServiceClient.setProperties(new StorageServiceProperties()
            .staticWebsite(new StaticWebsite().enabled(false))
            .deleteRetentionPolicy(disabled)
            .cors(null)
            .hourMetrics(new Metrics().version("1.0").enabled(false)
                .retentionPolicy(disabled))
            .minuteMetrics(new Metrics().version("1.0").enabled(false)
                .retentionPolicy(disabled))
            .logging(new Logging().version("1.0")
                .retentionPolicy(disabled))
            .defaultServiceVersion("2018-03-28"))
    }

    def "List containers"() {
        when:
        def response =
            primaryBlobServiceClient.listContainers(new ListContainersOptions().prefix(containerPrefix), null)

        then:
        for (ContainerItem c : response) {
            assert c.name().startsWith(containerPrefix)
            assert c.properties().lastModified() != null
            assert c.properties().etag() != null
            assert c.properties().leaseStatus() != null
            assert c.properties().leaseState() != null
            assert c.properties().leaseDuration() == null
            assert c.properties().publicAccess() == null
            assert !c.properties().hasLegalHold()
            assert !c.properties().hasImmutabilityPolicy()
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
        String firstContainerName = listResponse.next().name()

        expect:
        // Assert that the second segment is indeed after the first alphabetically
        firstContainerName < listResponse.next().name()
    }

    def "List containers details"() {
        setup:
        Metadata metadata = new Metadata()
        metadata.put("foo", "bar")
        cc = primaryBlobServiceClient.createContainerWithResponse("aaa" + generateContainerName(), metadata, null, null).value()

        expect:
        primaryBlobServiceClient.listContainers(new ListContainersOptions()
            .details(new ContainerListDetails().metadata(true))
            .prefix("aaa" + containerPrefix), null)
            .iterator().next().metadata() == metadata

        // Container with prefix "aaa" will not be cleaned up by normal test cleanup.
        cc.deleteWithResponse(null, null, null).statusCode() == 202
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
        primaryBlobServiceClient.listContainers(new ListContainersOptions().maxResults(PAGE_RESULTS), null)
            .iterableByPage().iterator().next().value().size() == PAGE_RESULTS

        cleanup:
        containers.each { container -> container.delete() }
    }

    def "List containers error"() {
        when:
        primaryBlobServiceClient.listContainers().streamByPage("garbage continuation token").count()

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
        primaryBlobServiceClient.listContainers(new ListContainersOptions().maxResults(PAGE_RESULTS), Duration.ofSeconds(10)).streamByPage().count()

        then: "Still have paging functionality"
        notThrown(Exception)

        cleanup:
        containers.each { container -> container.delete() }
    }

    def validatePropsSet(StorageServiceProperties sent, StorageServiceProperties received) {
        return received.logging().read() == sent.logging().read() &&
            received.logging().delete() == sent.logging().delete() &&
            received.logging().write() == sent.logging().write() &&
            received.logging().version() == sent.logging().version() &&
            received.logging().retentionPolicy().days() == sent.logging().retentionPolicy().days() &&
            received.logging().retentionPolicy().enabled() == sent.logging().retentionPolicy().enabled() &&

            received.cors().size() == sent.cors().size() &&
            received.cors().get(0).allowedMethods() == sent.cors().get(0).allowedMethods() &&
            received.cors().get(0).allowedHeaders() == sent.cors().get(0).allowedHeaders() &&
            received.cors().get(0).allowedOrigins() == sent.cors().get(0).allowedOrigins() &&
            received.cors().get(0).exposedHeaders() == sent.cors().get(0).exposedHeaders() &&
            received.cors().get(0).maxAgeInSeconds() == sent.cors().get(0).maxAgeInSeconds() &&

            received.defaultServiceVersion() == sent.defaultServiceVersion() &&

            received.hourMetrics().enabled() == sent.hourMetrics().enabled() &&
            received.hourMetrics().includeAPIs() == sent.hourMetrics().includeAPIs() &&
            received.hourMetrics().retentionPolicy().enabled() == sent.hourMetrics().retentionPolicy().enabled() &&
            received.hourMetrics().retentionPolicy().days() == sent.hourMetrics().retentionPolicy().days() &&
            received.hourMetrics().version() == sent.hourMetrics().version() &&

            received.minuteMetrics().enabled() == sent.minuteMetrics().enabled() &&
            received.minuteMetrics().includeAPIs() == sent.minuteMetrics().includeAPIs() &&
            received.minuteMetrics().retentionPolicy().enabled() == sent.minuteMetrics().retentionPolicy().enabled() &&
            received.minuteMetrics().retentionPolicy().days() == sent.minuteMetrics().retentionPolicy().days() &&
            received.minuteMetrics().version() == sent.minuteMetrics().version() &&

            received.deleteRetentionPolicy().enabled() == sent.deleteRetentionPolicy().enabled() &&
            received.deleteRetentionPolicy().days() == sent.deleteRetentionPolicy().days() &&

            received.staticWebsite().enabled() == sent.staticWebsite().enabled() &&
            received.staticWebsite().indexDocument() == sent.staticWebsite().indexDocument() &&
            received.staticWebsite().errorDocument404Path() == sent.staticWebsite().errorDocument404Path()
    }

    def "Set get properties"() {
        when:
        RetentionPolicy retentionPolicy = new RetentionPolicy().days(5).enabled(true)
        Logging logging = new Logging().read(true).version("1.0")
            .retentionPolicy(retentionPolicy)
        ArrayList<CorsRule> corsRules = new ArrayList<>()
        corsRules.add(new CorsRule().allowedMethods("GET,PUT,HEAD")
            .allowedOrigins("*")
            .allowedHeaders("x-ms-version")
            .exposedHeaders("x-ms-client-request-id")
            .maxAgeInSeconds(10))
        String defaultServiceVersion = "2016-05-31"
        Metrics hourMetrics = new Metrics().enabled(true).version("1.0")
            .retentionPolicy(retentionPolicy).includeAPIs(true)
        Metrics minuteMetrics = new Metrics().enabled(true).version("1.0")
            .retentionPolicy(retentionPolicy).includeAPIs(true)
        StaticWebsite website = new StaticWebsite().enabled(true)
            .indexDocument("myIndex.html")
            .errorDocument404Path("custom/error/path.html")

        StorageServiceProperties sentProperties = new StorageServiceProperties()
            .logging(logging).cors(corsRules).defaultServiceVersion(defaultServiceVersion)
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
        RetentionPolicy retentionPolicy = new RetentionPolicy().days(5).enabled(true)
        Logging logging = new Logging().read(true).version("1.0")
            .retentionPolicy(retentionPolicy)
        ArrayList<CorsRule> corsRules = new ArrayList<>()
        corsRules.add(new CorsRule().allowedMethods("GET,PUT,HEAD")
            .allowedOrigins("*")
            .allowedHeaders("x-ms-version")
            .exposedHeaders("x-ms-client-request-id")
            .maxAgeInSeconds(10))
        String defaultServiceVersion = "2016-05-31"
        Metrics hourMetrics = new Metrics().enabled(true).version("1.0")
            .retentionPolicy(retentionPolicy).includeAPIs(true)
        Metrics minuteMetrics = new Metrics().enabled(true).version("1.0")
            .retentionPolicy(retentionPolicy).includeAPIs(true)
        StaticWebsite website = new StaticWebsite().enabled(true)
            .indexDocument("myIndex.html")
            .errorDocument404Path("custom/error/path.html")

        StorageServiceProperties sentProperties = new StorageServiceProperties()
            .logging(logging).cors(corsRules).defaultServiceVersion(defaultServiceVersion)
            .minuteMetrics(minuteMetrics).hourMetrics(hourMetrics)
            .deleteRetentionPolicy(retentionPolicy)
            .staticWebsite(website)

        expect:
        primaryBlobServiceClient.setPropertiesWithResponse(sentProperties, null, null).statusCode() == 202
    }

    def "Set props error"() {
        when:
        setupBlobServiceClientBuilder("https://error.blob.core.windows.net")
            .credential(primaryCredential)
            .buildClient()
            .setProperties(new StorageServiceProperties())

        then:
        thrown(StorageException)
    }

    def "Get props min"() {
        expect:
        primaryBlobServiceClient.getPropertiesWithResponse(null, null).statusCode() == 200
    }

    def "Get props error"() {
        when:
        setupBlobServiceClientBuilder("https://error.blob.core.windows.net")
            .credential(primaryCredential)
            .buildClient()
            .getProperties()

        then:
        thrown(StorageException)
    }

    // The eng sys account does not work as expected. Need to work with the account.
    @Ignore
    def "Get UserDelegationKey"() {
        setup:
        def start = OffsetDateTime.now()
        def expiry = start.plusDays(1)

        Response<UserDelegationKey> response = getOAuthServiceClient().getUserDelegationKeyWithResponse(start, expiry, null, null)

        expect:
        response.statusCode() == 200
        response.value() != null
        response.value().signedOid() != null
        response.value().signedTid() != null
        response.value().signedStart() != null
        response.value().signedExpiry() != null
        response.value().signedService() != null
        response.value().signedVersion() != null
        response.value().value() != null
    }

    // The eng sys account does not work as expected. Need to work with the account.
    @Ignore
    def "Get UserDelegationKey min"() {
        setup:
        def expiry = OffsetDateTime.now().plusDays(1)

        def response = getOAuthServiceClient().getUserDelegationKeyWithResponse(null, expiry, null, null)

        expect:
        response.statusCode() == 200
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
        BlobServiceClient serviceClient = setupBlobServiceClientBuilder(secondaryEndpoint).credential(primaryCredential).buildClient()
        Response<StorageServiceStats> response = serviceClient.getStatisticsWithResponse(null, null)

        expect:
        response.headers().value("x-ms-version") != null
        response.headers().value("x-ms-request-id") != null
        response.headers().value("Date") != null
        response.value().geoReplication().status() != null
        response.value().geoReplication().lastSyncTime() != null
    }

    def "Get stats min"() {
        setup:
        String secondaryEndpoint = String.format("https://%s-secondary.blob.core.windows.net", primaryCredential.accountName())
        BlobServiceClient serviceClient = setupBlobServiceClientBuilder(secondaryEndpoint).credential(primaryCredential).buildClient()

        expect:
        serviceClient.getStatisticsWithResponse(null, null).statusCode() == 200
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
        response.value().accountKind() != null
        response.value().skuName() != null
    }

    def "Get account info min"() {
        expect:
        primaryBlobServiceClient.getAccountInfoWithResponse(null, null).statusCode() == 200
    }

    def "Get account info error"() {
        when:
        BlobServiceClient serviceURL = setupBlobServiceClientBuilder(primaryBlobServiceClient.getAccountUrl().toString()).buildClient()
        serviceURL.getAccountInfo()

        then:
        thrown(StorageException)
    }


    // This test validates a fix for a bug that caused NPE to be thrown when the account did not exist.
    def "Invalid account name"() {
        setup:
        def client = setupBlobServiceClientBuilder("http://fake.blobfake.core.windows.net",
            new RequestRetryPolicy(new RequestRetryOptions(null, 2, null, null, null, null)))
            .credential(primaryCredential)
            .buildClient()

        when:
        client.getProperties()

        then:
        def e = thrown(RuntimeException)
        e.getCause() instanceof UnknownHostException
    }
}
