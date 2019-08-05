// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob


import com.azure.core.http.HttpHeaders
import com.azure.core.http.rest.Response
import com.azure.storage.blob.models.*
import com.azure.storage.common.policy.RequestRetryOptions
import org.junit.Assume

import java.time.OffsetDateTime

class ServiceAPITest extends APISpec {
    def setup() {
        RetentionPolicy disabled = new RetentionPolicy().enabled(false)
        primaryServiceURL.setProperties(new StorageServiceProperties()
            .staticWebsite(new StaticWebsite().enabled(false))
            .deleteRetentionPolicy(disabled)
            .cors(null)
            .hourMetrics(new Metrics().version("1.0").enabled(false)
                .retentionPolicy(disabled))
            .minuteMetrics(new Metrics().version("1.0").enabled(false)
                .retentionPolicy(disabled))
            .logging(new Logging().version("1.0")
                .retentionPolicy(disabled))
            .defaultServiceVersion("2018-03-28"), null)
    }

    def cleanup() {
        Assume.assumeTrue("The test only runs in Live mode.", testMode.equalsIgnoreCase("RECORD"))
        RetentionPolicy disabled = new RetentionPolicy().enabled(false)
        primaryServiceURL.setProperties(new StorageServiceProperties()
                .staticWebsite(new StaticWebsite().enabled(false))
                .deleteRetentionPolicy(disabled)
                .cors(null)
                .hourMetrics(new Metrics().version("1.0").enabled(false)
                .retentionPolicy(disabled))
                .minuteMetrics(new Metrics().version("1.0").enabled(false)
                .retentionPolicy(disabled))
                .logging(new Logging().version("1.0")
                .retentionPolicy(disabled))
                .defaultServiceVersion("2018-03-28"), null)
    }

    def "List containers"() {
        when:
        Iterable<ContainerItem> response =
                primaryServiceURL.listContainers(new ListContainersOptions().prefix(containerPrefix), null)

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
        primaryServiceURL.listContainers().iterator().hasNext()

        then:
        notThrown(StorageException)
    }

    def "List containers marker"() {
        setup:
        for (int i = 0; i < 10; i++) {
            primaryServiceURL.createContainer(generateContainerName())
        }

        Iterator<ContainerItem> listResponse = primaryServiceURL.listContainers().iterator()
        String firstContainerName = listResponse.next().name()

        expect:
        // Assert that the second segment is indeed after the first alphabetically
        firstContainerName < listResponse.next().name()
    }

    def "List containers details"() {
        setup:
        Metadata metadata = new Metadata()
        metadata.put("foo", "bar")
        cu = primaryServiceURL.createContainer("aaa" + generateContainerName(), metadata, null).value()

        expect:
        primaryServiceURL.listContainers(new ListContainersOptions()
                .details(new ContainerListDetails().metadata(true))
                .prefix("aaa" + containerPrefix), null)
            .iterator().next().metadata() == metadata

        // Container with prefix "aaa" will not be cleaned up by normal test cleanup.
        cu.delete().statusCode() == 202
    }

    // TODO (alzimmer): Turn this test back on when listing by page is implemented
    /*def "List containers maxResults"() {
        setup:
        for (int i = 0; i < 11; i++) {
            primaryServiceURL.createContainer(generateContainerName())
        }

        expect:

        primaryServiceURL.listContainersSegment(null,
                new ListContainersOptions().maxResults(10), null)
                .blockingGet().body().containerItems().size() == 10
    }*/

    // TODO (alzimmer): Turn this test back on when listing by page is implemented as this requires being able to set a marker
    /*def "List containers error"() {
        when:
        primaryServiceURL.listContainers("garbage", null, null).blockingGet()

        then:
        thrown(StorageException)
    }*/

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

        HttpHeaders headers = primaryServiceURL.setProperties(sentProperties).headers()

        // Service properties may take up to 30s to take effect. If they weren't already in place, wait.
        sleep(30 * 1000)

        StorageServiceProperties receivedProperties = primaryServiceURL.getProperties().value()

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
        primaryServiceURL.setProperties(sentProperties).statusCode() == 202
    }

    def "Set props error"() {
        when:
        new BlobServiceClientBuilder()
            .endpoint("https://error.blob.core.windows.net")
            .credential(primaryCreds)
            .buildClient()
            .setProperties(new StorageServiceProperties())

        then:
        thrown(StorageException)
    }

    def "Get props min"() {
        expect:
        primaryServiceURL.getProperties().statusCode() == 200
    }

    def "Get props error"() {
        when:
        new BlobServiceClientBuilder()
            .endpoint("https://error.blob.core.windows.net")
            .credential(primaryCreds)
            .buildClient()
            .getProperties()

        then:
        thrown(StorageException)
    }

    def "Get UserDelegationKey"() {
        setup:
        def start = OffsetDateTime.now()
        def expiry = start.plusDays(1)

        Response<UserDelegationKey> response = getOAuthServiceURL().getUserDelegationKey(start, expiry, null)

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

    def "Get UserDelegationKey min"() {
        setup:
        def expiry = OffsetDateTime.now().plusDays(1)

        def response = getOAuthServiceURL().getUserDelegationKey(null, expiry)

        expect:
        response.statusCode() == 200
    }

    def "Get UserDelegationKey error"() {
        when:
        getOAuthServiceURL().getUserDelegationKey(start, expiry)

        then:
        thrown(exception)

        where:
        start                | expiry                            || exception
        null                 | null                              || IllegalArgumentException
        OffsetDateTime.now() | OffsetDateTime.now().minusDays(1) || IllegalArgumentException
    }

    def "Get stats"() {
        setup:
        String secondaryEndpoint = String.format("https://%s-secondary.blob.core.windows.net", primaryCreds.accountName())
        BlobServiceClient serviceClient = new BlobServiceClientBuilder().endpoint(secondaryEndpoint)
                                        .credential(primaryCreds).buildClient()
        Response<StorageServiceStats> response = serviceClient.getStatistics()

        expect:
        response.headers().value("x-ms-version") != null
        response.headers().value("x-ms-request-id") != null
        response.headers().value("Date") != null
        response.value().geoReplication().status() != null
        response.value().geoReplication().lastSyncTime() != null
    }

    def "Get stats min"() {
        setup:
        String secondaryEndpoint = String.format("https://%s-secondary.blob.core.windows.net", primaryCreds.accountName())
        BlobServiceClient serviceClient = new BlobServiceClientBuilder().endpoint(secondaryEndpoint)
            .credential(primaryCreds).buildClient()
        expect:
        serviceClient.getStatistics().statusCode() == 200
    }

    def "Get stats error"() {
        when:
        primaryServiceURL.getStatistics()

        then:
        thrown(StorageException)
    }

    def "Get account info"() {
        when:
        Response<StorageAccountInfo> response = primaryServiceURL.getAccountInfo()

        then:
        response.headers().value("Date") != null
        response.headers().value("x-ms-version") != null
        response.headers().value("x-ms-request-id") != null
        response.value().accountKind() != null
        response.value().skuName() != null
    }

    def "Get account info min"() {
        expect:
        primaryServiceURL.getAccountInfo().statusCode() == 200
    }

    def "Get account info error"() {
        when:
        BlobServiceClient serviceURL = new BlobServiceClientBuilder()
            .endpoint(primaryServiceURL.getAccountUrl().toString())
            .buildClient()
        serviceURL.getAccountInfo()

        then:
        thrown(StorageException)
    }


    // This test validates a fix for a bug that caused NPE to be thrown when the account did not exist.
    def "Invalid account name"() {
        setup:
        URL badURL = new URL("http://fake.blobfake.core.windows.net")
        BlobServiceClient client = new BlobServiceClientBuilder()
            .endpoint(badURL.toString())
            .credential(primaryCreds)
            .retryOptions(new RequestRetryOptions(null, 2, null, null, null, null))
            .buildClient()

        when:
        client.getProperties()

        then:
        def e = thrown(RuntimeException)
        e.getCause() instanceof UnknownHostException
    }
}
