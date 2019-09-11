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
            .setStaticWebsite(new StaticWebsite().setEnabled(false))
            .setDeleteRetentionPolicy(disabled)
            .setCors(null)
            .setHourMetrics(new Metrics().setVersion("1.0").setEnabled(false)
                .setRetentionPolicy(disabled))
            .setMinuteMetrics(new Metrics().setVersion("1.0").setEnabled(false)
                .setRetentionPolicy(disabled))
            .setLogging(new Logging().setVersion("1.0")
                .setRetentionPolicy(disabled))
            .setDefaultServiceVersion("2018-03-28"))
    }

    def cleanup() {
        RetentionPolicy disabled = new RetentionPolicy().setEnabled(false)
        primaryBlobServiceClient.setProperties(new StorageServiceProperties()
            .setStaticWebsite(new StaticWebsite().setEnabled(false))
            .setDeleteRetentionPolicy(disabled)
            .setCors(null)
            .setHourMetrics(new Metrics().setVersion("1.0").setEnabled(false)
                .setRetentionPolicy(disabled))
            .setMinuteMetrics(new Metrics().setVersion("1.0").setEnabled(false)
                .setRetentionPolicy(disabled))
            .setLogging(new Logging().setVersion("1.0")
                .setRetentionPolicy(disabled))
            .setDefaultServiceVersion("2018-03-28"))
    }

    def "List containers"() {
        when:
        def response =
            primaryBlobServiceClient.listContainers(new ListContainersOptions().setPrefix(containerPrefix + testName), null)

        then:
        for (ContainerItem c : response) {
            assert c.getName().startsWith(containerPrefix)
            assert c.getProperties().getLastModified() != null
            assert c.getProperties().getEtag() != null
            assert c.getProperties().getLeaseStatus() != null
            assert c.getProperties().getLeaseState() != null
            assert c.getProperties().getLeaseDuration() == null
            assert c.getProperties().getPublicAccess() == null
            assert !c.getProperties().getHasLegalHold()
            assert !c.getProperties().getHasImmutabilityPolicy()
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
        cc = primaryBlobServiceClient.createContainerWithResponse("aaa" + generateContainerName(), metadata, null, null).getValue()

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
            .iterableByPage().iterator().next().getValue().size() == PAGE_RESULTS

        cleanup:
        containers.each { container -> container.setDelete() }
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
        primaryBlobServiceClient.listContainers(new ListContainersOptions().setMaxResults(PAGE_RESULTS), Duration.ofSeconds(10)).streamByPage().count()

        then: "Still have paging functionality"
        notThrown(Exception)

        cleanup:
        containers.each { container -> container.setDelete() }
    }

    def validatePropsSet(StorageServiceProperties sent, StorageServiceProperties received) {
        return received.getLogging().getRead() == sent.getLogging().getRead() &&
            received.getLogging().getDelete() == sent.getLogging().getDelete() &&
            received.getLogging().getWrite() == sent.getLogging().getWrite() &&
            received.getLogging().getVersion() == sent.getLogging().getVersion() &&
            received.getLogging().getRetentionPolicy().getDays() == sent.getLogging().getRetentionPolicy().getDays() &&
            received.getLogging().getRetentionPolicy().getEnabled() == sent.getLogging().getRetentionPolicy().getEnabled() &&

            received.getCors().size() == sent.getCors().size() &&
            received.getCors().get(0).getAllowedMethods() == sent.getCors().get(0).getAllowedMethods() &&
            received.getCors().get(0).getAllowedHeaders() == sent.getCors().get(0).getAllowedHeaders() &&
            received.getCors().get(0).getAllowedOrigins() == sent.getCors().get(0).getAllowedOrigins() &&
            received.getCors().get(0).getExposedHeaders() == sent.getCors().get(0).getExposedHeaders() &&
            received.getCors().get(0).getMaxAgeInSeconds() == sent.getCors().get(0).getMaxAgeInSeconds() &&

            received.getDefaultServiceVersion() == sent.getDefaultServiceVersion() &&

            received.getHourMetrics().getEnabled() == sent.getHourMetrics().getEnabled() &&
            received.getHourMetrics().getIncludeAPIs() == sent.getHourMetrics().getIncludeAPIs() &&
            received.getHourMetrics().getRetentionPolicy().getEnabled() == sent.getHourMetrics().getRetentionPolicy().getEnabled() &&
            received.getHourMetrics().getRetentionPolicy().getDays() == sent.getHourMetrics().getRetentionPolicy().getDays() &&
            received.getHourMetrics().getVersion() == sent.getHourMetrics().getVersion() &&

            received.getMinuteMetrics().getEnabled() == sent.getMinuteMetrics().getEnabled() &&
            received.getMinuteMetrics().getIncludeAPIs() == sent.getMinuteMetrics().getIncludeAPIs() &&
            received.getMinuteMetrics().getRetentionPolicy().getEnabled() == sent.getMinuteMetrics().getRetentionPolicy().getEnabled() &&
            received.getMinuteMetrics().getRetentionPolicy().getDays() == sent.getMinuteMetrics().getRetentionPolicy().getDays() &&
            received.getMinuteMetrics().getVersion() == sent.getMinuteMetrics().getVersion() &&

            received.getDeleteRetentionPolicy().getEnabled() == sent.getDeleteRetentionPolicy().getEnabled() &&
            received.getDeleteRetentionPolicy().getDays() == sent.getDeleteRetentionPolicy().getDays() &&

            received.getStaticWebsite().getEnabled() == sent.getStaticWebsite().getEnabled() &&
            received.getStaticWebsite().getIndexDocument() == sent.getStaticWebsite().getIndexDocument() &&
            received.getStaticWebsite().getErrorDocument404Path() == sent.getStaticWebsite().getErrorDocument404Path()
    }

    def "Set get properties"() {
        when:
        RetentionPolicy retentionPolicy = new RetentionPolicy().setDays(5).setEnabled(true)
        Logging logging = new Logging().setRead(true).setVersion("1.0")
            .setRetentionPolicy(retentionPolicy)
        ArrayList<CorsRule> corsRules = new ArrayList<>()
        corsRules.add(new CorsRule().setAllowedMethods("GET,PUT,HEAD")
            .setAllowedOrigins("*")
            .setAllowedHeaders("x-ms-version")
            .setExposedHeaders("x-ms-client-request-id")
            .setMaxAgeInSeconds(10))
        String defaultServiceVersion = "2016-05-31"
        Metrics hourMetrics = new Metrics().setEnabled(true).setVersion("1.0")
            .setRetentionPolicy(retentionPolicy).setIncludeAPIs(true)
        Metrics minuteMetrics = new Metrics().setEnabled(true).setVersion("1.0")
            .setRetentionPolicy(retentionPolicy).setIncludeAPIs(true)
        StaticWebsite website = new StaticWebsite().setEnabled(true)
            .setIndexDocument("myIndex.html")
            .setErrorDocument404Path("custom/error/path.html")

        StorageServiceProperties sentProperties = new StorageServiceProperties()
            .setLogging(logging).setCors(corsRules).setDefaultServiceVersion(defaultServiceVersion)
            .setMinuteMetrics(minuteMetrics).setHourMetrics(hourMetrics)
            .setDeleteRetentionPolicy(retentionPolicy)
            .setStaticWebsite(website)

        HttpHeaders headers = primaryBlobServiceClient.setPropertiesWithResponse(sentProperties, null, null).getHeaders()

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
        Logging logging = new Logging().setRead(true).setVersion("1.0")
            .setRetentionPolicy(retentionPolicy)
        ArrayList<CorsRule> corsRules = new ArrayList<>()
        corsRules.add(new CorsRule().setAllowedMethods("GET,PUT,HEAD")
            .setAllowedOrigins("*")
            .setAllowedHeaders("x-ms-version")
            .setExposedHeaders("x-ms-client-request-id")
            .setMaxAgeInSeconds(10))
        String defaultServiceVersion = "2016-05-31"
        Metrics hourMetrics = new Metrics().setEnabled(true).setVersion("1.0")
            .setRetentionPolicy(retentionPolicy).setIncludeAPIs(true)
        Metrics minuteMetrics = new Metrics().setEnabled(true).setVersion("1.0")
            .setRetentionPolicy(retentionPolicy).setIncludeAPIs(true)
        StaticWebsite website = new StaticWebsite().setEnabled(true)
            .setIndexDocument("myIndex.html")
            .setErrorDocument404Path("custom/error/path.html")

        StorageServiceProperties sentProperties = new StorageServiceProperties()
            .setLogging(logging).setCors(corsRules).setDefaultServiceVersion(defaultServiceVersion)
            .setMinuteMetrics(minuteMetrics).setHourMetrics(hourMetrics)
            .setDeleteRetentionPolicy(retentionPolicy)
            .setStaticWebsite(website)

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
        null                 | null                              || IllegalArgumentException
        OffsetDateTime.now() | OffsetDateTime.now().minusDays(1) || IllegalArgumentException
    }

    def "Get stats"() {
        setup:
        String secondaryEndpoint = String.format("https://%s-secondary.blob.core.windows.net", primaryCredential.accountName())
        BlobServiceClient serviceClient = getServiceClient(primaryCredential, secondaryEndpoint)
        Response<StorageServiceStats> response = serviceClient.getStatisticsWithResponse(null, null)

        expect:
        response.getHeaders().value("x-ms-version") != null
        response.getHeaders().value("x-ms-request-id") != null
        response.getHeaders().value("Date") != null
        response.getValue().getGeoReplication().getStatus() != null
        response.getValue().getGeoReplication().getLastSyncTime() != null
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
        response.getHeaders().value("Date") != null
        response.getHeaders().value("x-ms-version") != null
        response.getHeaders().value("x-ms-request-id") != null
        response.getValue().getAccountKind() != null
        response.getValue().getSkuName() != null
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
