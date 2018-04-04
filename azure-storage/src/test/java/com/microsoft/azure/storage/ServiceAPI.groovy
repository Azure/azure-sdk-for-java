package com.microsoft.azure.storage

import com.microsoft.azure.storage.blob.BlobURLParts
import com.microsoft.azure.storage.blob.ContainerListingDetails
import com.microsoft.azure.storage.blob.ContainerURL
import com.microsoft.azure.storage.blob.ListContainersOptions
import com.microsoft.azure.storage.blob.Metadata
import com.microsoft.azure.storage.blob.PipelineOptions
import com.microsoft.azure.storage.blob.ServiceURL
import com.microsoft.azure.storage.blob.StorageURL
import com.microsoft.azure.storage.blob.URLParser
import com.microsoft.azure.storage.blob.models.Container
import com.microsoft.azure.storage.blob.models.CorsRule
import com.microsoft.azure.storage.blob.models.Logging
import com.microsoft.azure.storage.blob.models.Metrics
import com.microsoft.azure.storage.blob.models.RetentionPolicy
import com.microsoft.azure.storage.blob.models.ServiceGetStatisticsResponse
import com.microsoft.azure.storage.blob.models.ServiceListContainersSegmentResponse
import com.microsoft.azure.storage.blob.models.ServiceSetPropertiesHeaders
import com.microsoft.azure.storage.blob.models.StorageServiceProperties
import spock.lang.Unroll

class ServiceAPI extends APISpec {
    StorageServiceProperties originalProps = primaryServiceURL.getProperties().blockingGet().body()

    def cleanup() {
        primaryServiceURL.setProperties(originalProps).blockingGet()
    }

    def "Service list containers"() {
        setup:
        ServiceListContainersSegmentResponse response =
                primaryServiceURL.listContainersSegment(null, new ListContainersOptions(null,
                containerPrefix, null)).blockingGet()

        expect:
        for (Container c : response.body().containers()) {
            c.name().startsWith(containerPrefix)
        }
        response.headers().requestId() != null
        response.headers().version() != null
    }

    def "Service list containers marker"() {
        setup:
        for (int i=0; i<10; i++) {
            ContainerURL cu = primaryServiceURL.createContainerURL(generateContainerName())
            cu.create(null, null).blockingGet()
        }

        ServiceListContainersSegmentResponse response =
                primaryServiceURL.listContainersSegment(null,
                new ListContainersOptions(null, null, 5)).blockingGet()
        String marker = response.body().nextMarker()
        String firstContainerName = response.body().containers().get(0).name()
        response = primaryServiceURL.listContainersSegment(marker,
                new ListContainersOptions(null, null, 5)).blockingGet()

        expect:
        // Assert that the second segment is indeed after the first alphabetically
        firstContainerName < response.body().containers().get(0).name()
    }

    def "Service list containers details"() {
        setup:
        Metadata metadata = new Metadata()
        metadata.put("foo", "bar")
        cu = primaryServiceURL.createContainerURL("aaa"+generateContainerName())
        cu.create(metadata, null).blockingGet()

        expect:
        primaryServiceURL.listContainersSegment(null,
                new ListContainersOptions(new ContainerListingDetails(true),
                        "aaa"+containerPrefix, null)).blockingGet().body().containers()
                .get(0).metadata() == metadata
    }

    def "Service list containers maxResults"() {
        expect:
        primaryServiceURL.listContainersSegment(null,
                new ListContainersOptions(null, null, 10))
                .blockingGet().body().containers().size() == 10
    }

    def "Service set get properties"() {
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

        ServiceSetPropertiesHeaders headers = primaryServiceURL.setProperties(new StorageServiceProperties()
                .withLogging(logging).withCors(corsRules).withDefaultServiceVersion(defaultServiceVersion)
                .withMinuteMetrics(minuteMetrics).withHourMetrics(hourMetrics)).blockingGet().headers()
        StorageServiceProperties receivedProperties = primaryServiceURL.getProperties()
                .blockingGet().body()

        expect:
        headers.requestId() != null
        headers.version() != null

        receivedProperties.logging().read()
        !receivedProperties.logging().delete()
        !receivedProperties.logging().write()
        receivedProperties.logging().version() == "1.0"
        receivedProperties.logging().retentionPolicy().days() == 5
        receivedProperties.logging().retentionPolicy().enabled()

        receivedProperties.cors().size() == 1
        receivedProperties.cors().get(0).allowedMethods() == "GET,PUT,HEAD"
        receivedProperties.cors().get(0).allowedHeaders() == "x-ms-version"
        receivedProperties.cors().get(0).allowedOrigins() == "*"
        receivedProperties.cors().get(0).exposedHeaders() == "x-ms-client-request-id"
        receivedProperties.cors().get(0).maxAgeInSeconds() == 10

        receivedProperties.defaultServiceVersion() == "2016-05-31"

        receivedProperties.hourMetrics().enabled()
        receivedProperties.hourMetrics().includeAPIs()
        receivedProperties.hourMetrics().retentionPolicy().enabled()
        receivedProperties.hourMetrics().retentionPolicy().days() == 5
        receivedProperties.hourMetrics().version() == "1.0"

        receivedProperties.minuteMetrics().enabled()
        receivedProperties.minuteMetrics().includeAPIs()
        receivedProperties.minuteMetrics().retentionPolicy().enabled()
        receivedProperties.minuteMetrics().retentionPolicy().days() == 5
        receivedProperties.minuteMetrics().version() == "1.0"
    }

    def "Service get stats"() {
        setup:
        BlobURLParts parts = URLParser.parse(primaryServiceURL.toURL())
        parts.host = "xclientdev3-secondary.blob.core.windows.net"
        ServiceURL secondary = new ServiceURL(parts.toURL(),
                StorageURL.createPipeline(primaryCreds, new PipelineOptions()))
        ServiceGetStatisticsResponse response = secondary.getStatistics().blockingGet()

        expect:
        response.headers().version() != null
        response.headers().requestId() != null
        response.headers().dateProperty() != null
        response.body().geoReplication().status() != null
        response.body().geoReplication().lastSyncTime() != null
    }
}
