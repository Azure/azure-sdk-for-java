// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob

import com.azure.core.http.HttpHeaders
import com.azure.core.http.rest.Response
import com.azure.storage.blob.models.BlobContainerItem
import com.azure.storage.blob.models.BlobContainerListDetails
import com.azure.storage.blob.models.BlobServiceProperties
import com.azure.storage.blob.models.CorsRule
import com.azure.storage.blob.models.ListBlobContainersOptions
import com.azure.storage.blob.models.Logging

import com.azure.storage.blob.models.Metrics
import com.azure.storage.blob.models.RetentionPolicy
import com.azure.storage.blob.models.StaticWebsite
import com.azure.storage.blob.models.StorageAccountInfo
import com.azure.storage.blob.models.StorageException
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
        primaryBlobServiceClient.setProperties(new BlobServiceProperties()
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
        primaryBlobServiceClient.setProperties(new BlobServiceProperties()
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
            primaryBlobServiceClient.listBlobContainers(new ListBlobContainersOptions().setPrefix(containerPrefix + testName), null)

        then:
        for (BlobContainerItem c : response) {
            assert c.getName().startsWith(containerPrefix)
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

    def "List containers min"() {
        when:
        primaryBlobServiceClient.listBlobContainers().iterator().hasNext()

        then:
        notThrown(StorageException)
    }

    def "List containers marker"() {
        setup:
        for (int i = 0; i < 10; i++) {
            primaryBlobServiceClient.createBlobContainer(generateContainerName())
        }

        Iterator<BlobContainerItem> listResponse = primaryBlobServiceClient.listBlobContainers().iterator()
        String firstContainerName = listResponse.next().getName()

        expect:
        // Assert that the second segment is indeed after the first alphabetically
        firstContainerName < listResponse.next().getName()
    }

    def "List containers details"() {
        setup:
        def metadata = new HashMap<String, String>()
        metadata.put("foo", "bar")
        cc = primaryBlobServiceClient.createBlobContainerWithResponse("aaa" + generateContainerName(), metadata, null, null).getValue()

        expect:
        primaryBlobServiceClient.listBlobContainers(new ListBlobContainersOptions()
            .setDetails(new BlobContainerListDetails().setRetrieveMetadata(true))
            .setPrefix("aaa" + containerPrefix), null)
            .iterator().next().getMetadata() == metadata

        // Container with prefix "aaa" will not be cleaned up by normal test cleanup.
        cc.deleteWithResponse(null, null, null).getStatusCode() == 202
    }

    def "List containers maxResults"() {
        setup:
        def NUM_CONTAINERS = 5
        def PAGE_RESULTS = 3

        def containers = [] as Collection<BlobContainerClient>
        for (i in (1..NUM_CONTAINERS)) {
            containers << primaryBlobServiceClient.createBlobContainer(generateContainerName())
        }

        expect:
        primaryBlobServiceClient.listBlobContainers(new ListBlobContainersOptions().setMaxResultsPerPage(PAGE_RESULTS), null)
            .iterableByPage().iterator().next().getValue().size() == PAGE_RESULTS

        cleanup:
        containers.each { container -> container.delete() }
    }

    def "List containers error"() {
        when:
        primaryBlobServiceClient.listBlobContainers().streamByPage("garbage continuation token").count()

        then:
        thrown(StorageException)
    }

    def "List containers with timeout still backed by PagedFlux"() {
        setup:
        def NUM_CONTAINERS = 5
        def PAGE_RESULTS = 3

        def containers = [] as Collection<BlobContainerClient>
        for (i in (1..NUM_CONTAINERS)) {
            containers << primaryBlobServiceClient.createBlobContainer(generateContainerName())
        }

        when: "Consume results by page"
        primaryBlobServiceClient.listBlobContainers(new ListBlobContainersOptions().setMaxResultsPerPage(PAGE_RESULTS), Duration.ofSeconds(10)).streamByPage().count()

        then: "Still have paging functionality"
        notThrown(Exception)

        cleanup:
        containers.each { container -> container.delete() }
    }

    def validatePropsSet(BlobServiceProperties sent, BlobServiceProperties received) {
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
            received.getHourMetrics().isIncludeAPIs() == sent.getHourMetrics().isIncludeAPIs() &&
            received.getHourMetrics().getRetentionPolicy().isEnabled() == sent.getHourMetrics().getRetentionPolicy().isEnabled() &&
            received.getHourMetrics().getRetentionPolicy().getDays() == sent.getHourMetrics().getRetentionPolicy().getDays() &&
            received.getHourMetrics().getVersion() == sent.getHourMetrics().getVersion() &&

            received.getMinuteMetrics().isEnabled() == sent.getMinuteMetrics().isEnabled() &&
            received.getMinuteMetrics().isIncludeAPIs() == sent.getMinuteMetrics().isIncludeAPIs() &&
            received.getMinuteMetrics().getRetentionPolicy().isEnabled() == sent.getMinuteMetrics().getRetentionPolicy().isEnabled() &&
            received.getMinuteMetrics().getRetentionPolicy().getDays() == sent.getMinuteMetrics().getRetentionPolicy().getDays() &&
            received.getMinuteMetrics().getVersion() == sent.getMinuteMetrics().getVersion() &&

            received.getDeleteRetentionPolicy().isEnabled() == sent.getDeleteRetentionPolicy().isEnabled() &&
            received.getDeleteRetentionPolicy().getDays() == sent.getDeleteRetentionPolicy().getDays() &&

            received.getStaticWebsite().isEnabled() == sent.getStaticWebsite().isEnabled() &&
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

        BlobServiceProperties sentProperties = new BlobServiceProperties()
            .setLogging(logging).setCors(corsRules).setDefaultServiceVersion(defaultServiceVersion)
            .setMinuteMetrics(minuteMetrics).setHourMetrics(hourMetrics)
            .setDeleteRetentionPolicy(retentionPolicy)
            .setStaticWebsite(website)

        HttpHeaders headers = primaryBlobServiceClient.setPropertiesWithResponse(sentProperties, null, null).getHeaders()

        // Service properties may take up to 30s to take effect. If they weren't already in place, wait.
        sleepIfRecord(30 * 1000)

        BlobServiceProperties receivedProperties = primaryBlobServiceClient.getProperties()

        then:
        headers.getValue("x-ms-request-id") != null
        headers.getValue("x-ms-version") != null
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

        BlobServiceProperties sentProperties = new BlobServiceProperties()
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
            .setProperties(new BlobServiceProperties())

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
        null                 | null                              || NullPointerException
        OffsetDateTime.now() | OffsetDateTime.now().minusDays(1) || IllegalArgumentException
    }

    def "Get stats"() {
        setup:
        String secondaryEndpoint = String.format("https://%s-secondary.blob.core.windows.net", primaryCredential.getAccountName())
        BlobServiceClient serviceClient = getServiceClient(primaryCredential, secondaryEndpoint)
        Response<StorageServiceStats> response = serviceClient.getStatisticsWithResponse(null, null)

        expect:
        response.getHeaders().getValue("x-ms-version") != null
        response.getHeaders().getValue("x-ms-request-id") != null
        response.getHeaders().getValue("Date") != null
        response.getValue().getGeoReplication().getStatus() != null
        response.getValue().getGeoReplication().getLastSyncTime() != null
    }

    def "Get stats min"() {
        setup:
        String secondaryEndpoint = String.format("https://%s-secondary.blob.core.windows.net", primaryCredential.getAccountName())
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
        response.getHeaders().getValue("Date") != null
        response.getHeaders().getValue("x-ms-version") != null
        response.getHeaders().getValue("x-ms-request-id") != null
        response.getValue().getAccountKind() != null
        response.getValue().getSkuName() != null
    }

    def "Get account info min"() {
        expect:
        primaryBlobServiceClient.getAccountInfoWithResponse(null, null).getStatusCode() == 200
    }

    def "Get account info error"() {
        when:
        BlobServiceClient serviceURL = getServiceClient((SharedKeyCredential) null, primaryBlobServiceClient.getAccountUrl())
        serviceURL.getAccountInfo()

        then:
        thrown(IllegalArgumentException)
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
