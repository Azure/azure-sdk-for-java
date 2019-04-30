// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.storage

import com.microsoft.azure.storage.blob.*
import com.microsoft.azure.storage.blob.models.*
import com.microsoft.rest.v2.Context
import com.microsoft.rest.v2.http.HttpPipeline
import org.junit.Assume

import java.time.OffsetDateTime

class ServiceAPITest extends APISpec {
    def setup() {
        RetentionPolicy disabled = new RetentionPolicy().withEnabled(false)
        primaryServiceURL.setProperties(new StorageServiceProperties()
                .withStaticWebsite(new StaticWebsite().withEnabled(false))
                .withDeleteRetentionPolicy(disabled)
                .withCors(null)
                .withHourMetrics(new Metrics().withVersion("1.0").withEnabled(false)
                .withRetentionPolicy(disabled))
                .withMinuteMetrics(new Metrics().withVersion("1.0").withEnabled(false)
                .withRetentionPolicy(disabled))
                .withLogging(new Logging().withVersion("1.0")
                .withRetentionPolicy(disabled))
                .withDefaultServiceVersion("2018-03-28"), null).blockingGet()
    }

    def cleanup() {
        Assume.assumeTrue("The test only runs in Live mode.", testMode.equalsIgnoreCase("RECORD"));
        RetentionPolicy disabled = new RetentionPolicy().withEnabled(false)
        primaryServiceURL.setProperties(new StorageServiceProperties()
                .withStaticWebsite(new StaticWebsite().withEnabled(false))
                .withDeleteRetentionPolicy(disabled)
                .withCors(null)
                .withHourMetrics(new Metrics().withVersion("1.0").withEnabled(false)
                .withRetentionPolicy(disabled))
                .withMinuteMetrics(new Metrics().withVersion("1.0").withEnabled(false)
                .withRetentionPolicy(disabled))
                .withLogging(new Logging().withVersion("1.0")
                .withRetentionPolicy(disabled))
                .withDefaultServiceVersion("2018-03-28"), null).blockingGet()
    }

    def "List containers"() {
        when:
        ServiceListContainersSegmentResponse response =
                primaryServiceURL.listContainersSegment(null, new ListContainersOptions().withPrefix(containerPrefix),
                        null).blockingGet()

        then:
        for (ContainerItem c : response.body().containerItems()) {
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
        response.headers().requestId() != null
        response.headers().version() != null
    }

    def "List containers min"() {
        expect:
        primaryServiceURL.listContainersSegment(null, null).blockingGet().statusCode() == 200
    }

    def "List containers marker"() {
        setup:
        for (int i = 0; i < 10; i++) {
            ContainerURL cu = primaryServiceURL.createContainerURL(generateContainerName())
            cu.create(null, null, null).blockingGet()
        }

        ServiceListContainersSegmentResponse response =
                primaryServiceURL.listContainersSegment(null,
                        new ListContainersOptions().withMaxResults(5), null).blockingGet()
        String marker = response.body().nextMarker()
        String firstContainerName = response.body().containerItems().get(0).name()
        response = primaryServiceURL.listContainersSegment(marker,
                new ListContainersOptions().withMaxResults(5), null).blockingGet()

        expect:
        // Assert that the second segment is indeed after the first alphabetically
        firstContainerName < response.body().containerItems().get(0).name()
    }

    def "List containers details"() {
        setup:
        Metadata metadata = new Metadata()
        metadata.put("foo", "bar")
        cu = primaryServiceURL.createContainerURL("aaa" + generateContainerName())
        cu.create(metadata, null, null).blockingGet()

        expect:
        primaryServiceURL.listContainersSegment(null,
                new ListContainersOptions().withDetails(new ContainerListDetails().withMetadata(true))
                        .withPrefix("aaa" + containerPrefix), null).blockingGet().body().containerItems()
                .get(0).metadata() == metadata
        // Container with prefix "aaa" will not be cleaned up by normal test cleanup.
        cu.delete(null, null).blockingGet().statusCode() == 202
    }

    def "List containers maxResults"() {
        setup:
        for (int i = 0; i < 11; i++) {
            primaryServiceURL.createContainerURL(generateContainerName()).create(null, null, null)
                    .blockingGet()
        }
        expect:
        primaryServiceURL.listContainersSegment(null,
                new ListContainersOptions().withMaxResults(10), null)
                .blockingGet().body().containerItems().size() == 10
    }

    def "List containers error"() {
        when:
        primaryServiceURL.listContainersSegment("garbage", null, null).blockingGet()

        then:
        thrown(StorageException)
    }

    def "List containers context"() {
        setup:
        def pipeline =
                HttpPipeline.build(getStubFactory(getContextStubPolicy(200, ServiceListContainersSegmentHeaders)))

        def su = primaryServiceURL.withPipeline(pipeline)

        when:
        // No service call is made. Just satisfy the parameters.
        su.listContainersSegment(null, null, defaultContext)

        then:
        notThrown(RuntimeException)
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
        RetentionPolicy retentionPolicy = new RetentionPolicy().withDays(5).withEnabled(true)
        Logging logging = new Logging().withRead(true).withVersion("1.0")
                .withRetentionPolicy(retentionPolicy)
        ArrayList<CorsRule> corsRules = new ArrayList<>()
        corsRules.add(new CorsRule().withAllowedMethods("GET,PUT,HEAD")
                .withAllowedOrigins("*")
                .withAllowedHeaders("x-ms-version")
                .withExposedHeaders("x-ms-client-request-id")
                .withMaxAgeInSeconds(10))
        String defaultServiceVersion = "2016-05-31"
        Metrics hourMetrics = new Metrics().withEnabled(true).withVersion("1.0")
                .withRetentionPolicy(retentionPolicy).withIncludeAPIs(true)
        Metrics minuteMetrics = new Metrics().withEnabled(true).withVersion("1.0")
                .withRetentionPolicy(retentionPolicy).withIncludeAPIs(true)
        StaticWebsite website = new StaticWebsite().withEnabled(true)
                .withIndexDocument("myIndex.html")
                .withErrorDocument404Path("custom/error/path.html")

        StorageServiceProperties sentProperties = new StorageServiceProperties()
                .withLogging(logging).withCors(corsRules).withDefaultServiceVersion(defaultServiceVersion)
                .withMinuteMetrics(minuteMetrics).withHourMetrics(hourMetrics)
                .withDeleteRetentionPolicy(retentionPolicy)
                .withStaticWebsite(website)

        ServiceSetPropertiesHeaders headers = primaryServiceURL.setProperties(sentProperties, null)
                .blockingGet().headers()

        // Service properties may take up to 30s to take effect. If they weren't already in place, wait.
        sleep(30 * 1000)

        StorageServiceProperties receivedProperties = primaryServiceURL.getProperties(null)
                .blockingGet().body()

        then:
        headers.requestId() != null
        headers.version() != null
        validatePropsSet(sentProperties, receivedProperties)
    }

    // In java, we don't have support from the validator for checking the bounds on days. The service will catch these.

    def "Set props min"() {
        setup:
        RetentionPolicy retentionPolicy = new RetentionPolicy().withDays(5).withEnabled(true)
        Logging logging = new Logging().withRead(true).withVersion("1.0")
                .withRetentionPolicy(retentionPolicy)
        ArrayList<CorsRule> corsRules = new ArrayList<>()
        corsRules.add(new CorsRule().withAllowedMethods("GET,PUT,HEAD")
                .withAllowedOrigins("*")
                .withAllowedHeaders("x-ms-version")
                .withExposedHeaders("x-ms-client-request-id")
                .withMaxAgeInSeconds(10))
        String defaultServiceVersion = "2016-05-31"
        Metrics hourMetrics = new Metrics().withEnabled(true).withVersion("1.0")
                .withRetentionPolicy(retentionPolicy).withIncludeAPIs(true)
        Metrics minuteMetrics = new Metrics().withEnabled(true).withVersion("1.0")
                .withRetentionPolicy(retentionPolicy).withIncludeAPIs(true)
        StaticWebsite website = new StaticWebsite().withEnabled(true)
                .withIndexDocument("myIndex.html")
                .withErrorDocument404Path("custom/error/path.html")

        StorageServiceProperties sentProperties = new StorageServiceProperties()
                .withLogging(logging).withCors(corsRules).withDefaultServiceVersion(defaultServiceVersion)
                .withMinuteMetrics(minuteMetrics).withHourMetrics(hourMetrics)
                .withDeleteRetentionPolicy(retentionPolicy)
                .withStaticWebsite(website)

        expect:
        primaryServiceURL.setProperties(sentProperties).blockingGet().statusCode() == 202
    }

    def "Set props error"() {
        when:
        new ServiceURL(new URL("https://error.blob.core.windows.net"),
                StorageURL.createPipeline(primaryCreds, new PipelineOptions()))
                .setProperties(new StorageServiceProperties(), null).blockingGet()

        then:
        thrown(StorageException)
    }

    def "Set props context"() {
        setup:
        def pipeline =
                HttpPipeline.build(getStubFactory(getContextStubPolicy(200, ServiceSetPropertiesHeaders)))

        def su = primaryServiceURL.withPipeline(pipeline)

        when:
        // No service call is made. Just satisfy the parameters.
        su.setProperties(new StorageServiceProperties(), defaultContext)

        then:
        notThrown(RuntimeException)
    }

    def "Get props min"() {
        expect:
        primaryServiceURL.getProperties().blockingGet().statusCode() == 200
    }

    def "Get props error"() {
        when:
        new ServiceURL(new URL("https://error.blob.core.windows.net"),
                StorageURL.createPipeline(primaryCreds, new PipelineOptions())).getProperties(null).blockingGet()

        then:
        thrown(StorageException)
    }

    def "Get props context"() {
        setup:
        def pipeline =
                HttpPipeline.build(getStubFactory(getContextStubPolicy(200, ServiceGetPropertiesHeaders)))

        def su = primaryServiceURL.withPipeline(pipeline)

        when:
        // No service call is made. Just satisfy the parameters.
        su.getProperties(defaultContext)

        then:
        notThrown(RuntimeException)
    }

    def "Get UserDelegationKey"() {
        setup:
        def start = OffsetDateTime.now()
        def expiry = start.plusDays(1)

        def response = getOAuthServiceURL().getUserDelegationKey(start, expiry, Context.NONE).blockingGet()

        expect:
        response.statusCode() == 200
        response.body() != null
        response.body().signedOid() != null
        response.body().signedTid() != null
        response.body().signedStart() != null
        response.body().signedExpiry() != null
        response.body().signedService() != null
        response.body().signedVersion() != null
        response.body().value() != null
    }

    def "Get UserDelegationKey min"() {
        setup:
        def expiry = OffsetDateTime.now().plusDays(1)

        def response = getOAuthServiceURL().getUserDelegationKey(null, expiry).blockingGet()

        expect:
        response.statusCode() == 200
    }

    def "Get UserDelegationKey error"() {
        when:
        getOAuthServiceURL().getUserDelegationKey(null, null).blockingGet()

        then:
        thrown(exception)

        where:
        start                | expiry                            || exception
        null                 | null                              || IllegalArgumentException
        OffsetDateTime.now() | OffsetDateTime.now().minusDays(1) || IllegalArgumentException
    }

    def "Get UserDelegationKey context"() {
        setup:
        def pipeline =
                HttpPipeline.build(getStubFactory(getContextStubPolicy(200, ServiceGetUserDelegationKeyHeaders)))

        def su = primaryServiceURL.withPipeline(pipeline)

        when:
        // No service call is made. Just satisfy the parameters.
        su.getUserDelegationKey(null, OffsetDateTime.now(), defaultContext)

        then:
        notThrown(RuntimeException)
    }

    def "Get stats"() {
        setup:
        BlobURLParts parts = URLParser.parse(primaryServiceURL.toURL())
        parts.withHost(primaryCreds.getAccountName() + "-secondary.blob.core.windows.net")
        ServiceURL secondary = new ServiceURL(parts.toURL(),
                StorageURL.createPipeline(primaryCreds, new PipelineOptions()))
        ServiceGetStatisticsResponse response = secondary.getStatistics(null).blockingGet()

        expect:
        response.headers().version() != null
        response.headers().requestId() != null
        response.headers().date() != null
        response.body().geoReplication().status() != null
        response.body().geoReplication().lastSyncTime() != null
    }

    def "Get stats min"() {
        setup:
        BlobURLParts parts = URLParser.parse(primaryServiceURL.toURL())
        parts.withHost(primaryCreds.getAccountName() + "-secondary.blob.core.windows.net")
        ServiceURL secondary = new ServiceURL(parts.toURL(),
                StorageURL.createPipeline(primaryCreds, new PipelineOptions()))

        expect:
        secondary.getStatistics(null).blockingGet().statusCode() == 200
    }

    def "Get stats error"() {
        when:
        primaryServiceURL.getStatistics(null).blockingGet()

        then:
        thrown(StorageException)
    }

    def "Get stats context"() {
        setup:
        def pipeline =
                HttpPipeline.build(getStubFactory(getContextStubPolicy(200, ServiceGetStatisticsHeaders)))

        def su = primaryServiceURL.withPipeline(pipeline)

        when:
        // No service call is made. Just satisfy the parameters.
        su.getStatistics(defaultContext)

        then:
        notThrown(RuntimeException)
    }

    def "Get account info"() {
        when:
        def response = primaryServiceURL.getAccountInfo(null).blockingGet()

        then:
        response.headers().date() != null
        response.headers().version() != null
        response.headers().requestId() != null
        response.headers().accountKind() != null
        response.headers().skuName() != null
    }

    def "Get account info min"() {
        expect:
        primaryServiceURL.getAccountInfo().blockingGet().statusCode() == 200
    }

    def "Get account info error"() {
        when:
        ServiceURL serviceURL = new ServiceURL(primaryServiceURL.toURL(),
                StorageURL.createPipeline(new AnonymousCredentials(), new PipelineOptions()))
        serviceURL.getAccountInfo(null).blockingGet()

        then:
        thrown(StorageException)
    }

    def "Get account info context"() {
        setup:
        def pipeline =
                HttpPipeline.build(getStubFactory(getContextStubPolicy(200, ServiceGetAccountInfoHeaders)))

        def su = primaryServiceURL.withPipeline(pipeline)

        when:
        // No service call is made. Just satisfy the parameters.
        su.getAccountInfo(defaultContext)

        then:
        notThrown(RuntimeException)
    }


    // This test validates a fix for a bug that caused NPE to be thrown when the account did not exist.
    def "Invalid account name"() {
        setup:
        def badURL = new URL("http://fake.blobfake.core.windows.net")
        def po = new PipelineOptions().withRequestRetryOptions(new RequestRetryOptions(null, 2, null, null, null, null))
        def sURL = new ServiceURL(badURL, StorageURL.createPipeline(primaryCreds, po))

        when:
        sURL.getProperties().blockingGet()

        then:
        def e = thrown(RuntimeException)
        e.getCause() instanceof UnknownHostException
    }
}
