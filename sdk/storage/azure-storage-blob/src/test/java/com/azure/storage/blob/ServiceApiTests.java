// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.paging.ContinuablePage;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.storage.blob.models.BlobAnalyticsLogging;
import com.azure.storage.blob.models.BlobAudience;
import com.azure.storage.blob.models.BlobContainerItem;
import com.azure.storage.blob.models.BlobContainerListDetails;
import com.azure.storage.blob.models.BlobCorsRule;
import com.azure.storage.blob.models.BlobErrorCode;
import com.azure.storage.blob.models.BlobMetrics;
import com.azure.storage.blob.models.BlobRetentionPolicy;
import com.azure.storage.blob.models.BlobServiceProperties;
import com.azure.storage.blob.models.BlobServiceStatistics;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.models.CustomerProvidedKey;
import com.azure.storage.blob.models.GeoReplicationStatus;
import com.azure.storage.blob.models.ListBlobContainersOptions;
import com.azure.storage.blob.models.StaticWebsite;
import com.azure.storage.blob.models.StorageAccountInfo;
import com.azure.storage.blob.models.TaggedBlobItem;
import com.azure.storage.blob.models.UserDelegationKey;
import com.azure.storage.blob.options.BlobParallelUploadOptions;
import com.azure.storage.blob.options.FindBlobsOptions;
import com.azure.storage.blob.options.UndeleteBlobContainerOptions;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.policy.RequestRetryOptions;
import com.azure.storage.common.policy.RetryPolicyType;
import com.azure.storage.common.policy.ServiceTimeoutPolicy;
import com.azure.storage.common.sas.AccountSasPermission;
import com.azure.storage.common.sas.AccountSasResourceType;
import com.azure.storage.common.sas.AccountSasService;
import com.azure.storage.common.sas.AccountSasSignatureValues;
import com.azure.storage.common.test.shared.StorageCommonTestUtils;
import com.azure.storage.common.test.shared.extensions.LiveOnly;
import com.azure.storage.common.test.shared.extensions.PlaybackOnly;
import com.azure.storage.common.test.shared.extensions.RequiredServiceVersion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ServiceApiTests extends BlobTestBase {
    private BlobServiceClient anonymousClient;
    private String tagKey;
    private String tagValue;

    @BeforeEach
    public void setup() {
        // We shouldn't be getting to the network layer anyway
        anonymousClient = new BlobServiceClientBuilder()
            .endpoint(ENVIRONMENT.getPrimaryAccount().getBlobEndpoint())
            .buildClient();

        tagKey = testResourceNamer.randomName(prefix, 20);
        tagValue = testResourceNamer.randomName(prefix, 20);
    }

    private void setInitialProperties() {
        BlobRetentionPolicy disabled = new BlobRetentionPolicy().setEnabled(false);
        primaryBlobServiceClient.setProperties(new BlobServiceProperties()
            .setStaticWebsite(new StaticWebsite().setEnabled(false))
            .setDeleteRetentionPolicy(disabled)
            .setCors(null)
            .setHourMetrics(new BlobMetrics().setVersion("1.0").setEnabled(false)
                .setRetentionPolicy(disabled))
            .setMinuteMetrics(new BlobMetrics().setVersion("1.0").setEnabled(false)
                .setRetentionPolicy(disabled))
            .setLogging(new BlobAnalyticsLogging().setVersion("1.0")
                .setRetentionPolicy(disabled))
            .setDefaultServiceVersion("2018-03-28"));
    }

    private void resetProperties() {
        BlobRetentionPolicy disabled = new BlobRetentionPolicy().setEnabled(false);
        primaryBlobServiceClient.setProperties(new BlobServiceProperties()
            .setStaticWebsite(new StaticWebsite().setEnabled(false))
            .setDeleteRetentionPolicy(disabled)
            .setCors(null)
            .setHourMetrics(new BlobMetrics().setVersion("1.0").setEnabled(false)
                .setRetentionPolicy(disabled))
            .setMinuteMetrics(new BlobMetrics().setVersion("1.0").setEnabled(false)
                .setRetentionPolicy(disabled))
            .setLogging(new BlobAnalyticsLogging().setVersion("1.0")
                .setRetentionPolicy(disabled))
            .setDefaultServiceVersion("2018-03-28"));
    }

    @Test
    public void listContainers() {
        PagedIterable<BlobContainerItem> response = primaryBlobServiceClient.listBlobContainers(
            new ListBlobContainersOptions().setPrefix(prefix), null);

        for (BlobContainerItem c : response) {
            assertTrue(c.getName().startsWith(prefix));
            assertNotNull(c.getProperties().getLastModified());
            assertNotNull(c.getProperties().getETag());
            assertNotNull(c.getProperties().getLeaseStatus());
            assertNotNull(c.getProperties().getLeaseState());
            assertNull(c.getProperties().getLeaseDuration());
            assertNull(c.getProperties().getPublicAccess());
            assertFalse(c.getProperties().isHasLegalHold());
            assertFalse(c.getProperties().isHasImmutabilityPolicy());
            assertFalse(c.getProperties().isEncryptionScopeOverridePrevented());
            assertNotNull(c.getProperties().getDefaultEncryptionScope());
//            !c.isDeleted() // Container soft delete
        }
    }

    @Test
    public void listContainersMin() {
        assertDoesNotThrow(() -> primaryBlobServiceClient.listBlobContainers().iterator().hasNext());
    }

    @Test
    public void listContainersMarker() {
        for (int i = 0; i < 10; i++) {
            primaryBlobServiceClient.createBlobContainer(generateContainerName());
        }

        ListBlobContainersOptions options = new ListBlobContainersOptions().setMaxResultsPerPage(5);
        PagedResponse<BlobContainerItem> firstPage = primaryBlobServiceClient.listBlobContainers(options, null)
            .iterableByPage().iterator().next();
        String marker = firstPage.getContinuationToken();
        String firstContainerName = firstPage.getValue().get(0).getName();

        PagedResponse<BlobContainerItem> secondPage = primaryBlobServiceClient.listBlobContainers()
            .iterableByPage(marker).iterator().next();

        // Assert that the second segment is indeed after the first alphabetically
        assertTrue(firstContainerName.compareTo(secondPage.getValue().get(0).getName()) < 0);
    }

    @Test
    public void listContainersDetails() {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("foo", "bar");
        String containerName = generateContainerName();
        cc = primaryBlobServiceClient.createBlobContainerWithResponse(containerName, metadata, null, null)
            .getValue();

        assertEquals(metadata,
            primaryBlobServiceClient.listBlobContainers(new ListBlobContainersOptions()
                .setDetails(new BlobContainerListDetails().setRetrieveMetadata(true))
                .setPrefix(containerName), null)
                .iterator().next().getMetadata());
    }

    @Test
    public void listContainersMaxResults() {
        int numContainers = 5;
        int pageResults = 3;
        String containerNamePrefix = generateContainerName();

        List<BlobContainerClient> containers = new ArrayList<>();
        for (int i = 0; i < numContainers; i++) {
            containers.add(primaryBlobServiceClient.createBlobContainer(containerNamePrefix + i));
        }

        assertEquals(pageResults, primaryBlobServiceClient.listBlobContainers(
            new ListBlobContainersOptions().setPrefix(containerNamePrefix).setMaxResultsPerPage(pageResults), null)
            .iterableByPage().iterator().next().getValue().size());

        // cleanup:
        for (BlobContainerClient container : containers) {
            container.delete();
        }
    }

    @Test
    public void listContainersMaxResultsByPage() {
        int numContainers = 5;
        int pageResults = 3;
        String containerNamePrefix = generateContainerName();

        List<BlobContainerClient> containers = new ArrayList<>();
        for (int i = 0; i < numContainers; i++) {
            containers.add(primaryBlobServiceClient.createBlobContainer(containerNamePrefix + i));
        }

        for (PagedResponse<BlobContainerItem> page : primaryBlobServiceClient.listBlobContainers(
            new ListBlobContainersOptions().setPrefix(containerNamePrefix), null).iterableByPage(pageResults)) {
            assertTrue(page.getValue().size() <= pageResults);
        }


        // cleanup:
        for (BlobContainerClient container : containers) {
            container.delete();
        }
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @Test
    public void listDeleted() {
        int numContainers = 5;
        String containerNamePrefix = generateContainerName();

        List<BlobContainerClient> containers = new ArrayList<>();
        for (int i = 0; i < numContainers; i++) {
            containers.add(primaryBlobServiceClient.createBlobContainer(containerNamePrefix + i));
        }

        // delete each container
        for (BlobContainerClient container : containers) {
            container.delete();
        }

        PagedIterable<BlobContainerItem> listResult = primaryBlobServiceClient.listBlobContainers(
            new ListBlobContainersOptions().setPrefix(containerNamePrefix).setDetails(
                new BlobContainerListDetails().setRetrieveDeleted(true)), null);

        for (BlobContainerItem item : listResult) {
            assertTrue(item.isDeleted());
        }
        assertEquals(numContainers, listResult.stream().count());
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @Test
    public void listWithAllDetails() {
        int numContainers = 5;
        String containerNamePrefix = generateContainerName();

        List<BlobContainerClient> containers = new ArrayList<>();
        for (int i = 0; i < numContainers; i++) {
            containers.add(primaryBlobServiceClient.createBlobContainer(containerNamePrefix + i));
        }

        // delete each container
        for (BlobContainerClient container : containers) {
            container.delete();
        }

        PagedIterable<BlobContainerItem> listResult = primaryBlobServiceClient.listBlobContainers(
            new ListBlobContainersOptions().setPrefix(containerNamePrefix).setDetails(new BlobContainerListDetails()
                .setRetrieveDeleted(true)
                .setRetrieveMetadata(true)), null);

        for (BlobContainerItem item : listResult) {
            assertTrue(item.isDeleted());
        }
        assertEquals(numContainers, listResult.stream().count());
    }

    @Test
    public void listContainersError() {
        assertThrows(BlobStorageException.class, () ->
            primaryBlobServiceClient.listBlobContainers().streamByPage("garbage continuation token").count());
    }

    @Test
    public void listContainersAnonymous() {
        assertThrows(IllegalStateException.class, () -> anonymousClient.listBlobContainers().iterator());
    }

    @Test
    public void listContainersWithTimeoutStillBackedByPagedFlux() {
        int numContainers = 5;
        int pageResults = 3;

        List<BlobContainerClient> containers = new ArrayList<>();
        for (int i = 0; i < numContainers; i++) {
            containers.add(primaryBlobServiceClient.createBlobContainer(generateContainerName()));
        }

        // when: "Consume results by page, then should still have paging functionality""
        assertDoesNotThrow(() -> primaryBlobServiceClient.listBlobContainers(
            new ListBlobContainersOptions().setMaxResultsPerPage(pageResults),
            Duration.ofSeconds(10)).streamByPage().count());

        // cleanup:
        for (BlobContainerClient container : containers) {
            container.delete();
        }
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2020-10-02")
    @Test
    @ResourceLock("ServiceProperties")
    public void listSystemContainers() {
        setInitialProperties();

        try {
            BlobRetentionPolicy retentionPolicy = new BlobRetentionPolicy().setDays(5).setEnabled(true);
            BlobAnalyticsLogging logging =
                new BlobAnalyticsLogging().setRead(true).setVersion("1.0").setRetentionPolicy(retentionPolicy);
            BlobServiceProperties serviceProps = new BlobServiceProperties().setLogging(logging);

            // Ensure $logs container exists. These will be reverted in test cleanup
            primaryBlobServiceClient.setPropertiesWithResponse(serviceProps, null, null);

            sleepIfRunningAgainstService(30 * 1000); // allow the service properties to take effect

            PagedIterable<BlobContainerItem> containers = primaryBlobServiceClient.listBlobContainers(
                new ListBlobContainersOptions()
                    .setDetails(new BlobContainerListDetails().setRetrieveSystemContainers(true)), null);

            assertTrue(containers.stream().anyMatch(c -> c.getName().equals("$logs")));
        } finally {
            resetProperties();
        }
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @Test
    public void findBlobsMin() {
        assertDoesNotThrow(() -> primaryBlobServiceClient.findBlobsByTags("\"key\"='value'").iterator().hasNext());
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2020-04-08")
    @Test
    public void findBlobsQuery() {
        BlobContainerClient containerClient = primaryBlobServiceClient.createBlobContainer(generateContainerName());
        BlobClient blobClient = containerClient.getBlobClient(generateBlobName());
        blobClient.uploadWithResponse(new BlobParallelUploadOptions(DATA.getDefaultInputStream(),
            DATA.getDefaultDataSize()).setTags(Collections.singletonMap("key", "value")), null, null);
        blobClient = containerClient.getBlobClient(generateBlobName());
        blobClient.uploadWithResponse(new BlobParallelUploadOptions(DATA.getDefaultInputStream(),
            DATA.getDefaultDataSize()).setTags(Collections.singletonMap("bar", "foo")), null, null);
        blobClient = containerClient.getBlobClient(generateBlobName());
        blobClient.upload(DATA.getDefaultInputStream(), DATA.getDefaultDataSize());

        sleepIfRunningAgainstService(10 * 1000); // To allow tags to index

        PagedIterable<TaggedBlobItem> results = primaryBlobServiceClient.findBlobsByTags(
            String.format("@container='%s' AND \"bar\"='foo'", containerClient.getBlobContainerName()));

        assertEquals(1, results.stream().count());
        Map<String, String> tags = results.iterator().next().getTags();
        assertEquals(1, tags.size());
        assertEquals("foo", tags.get("bar"));

        // cleanup:
        containerClient.delete();
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @Test
    public void findBlobsMarker() {
        BlobContainerClient cc = primaryBlobServiceClient.createBlobContainer(generateContainerName());
        Map<String, String> tags = Collections.singletonMap(tagKey, tagValue);
        for (int i = 0; i < 10; i++) {
            cc.getBlobClient(generateBlobName()).uploadWithResponse(
                new BlobParallelUploadOptions(DATA.getDefaultInputStream(), DATA.getDefaultDataSize()).setTags(tags),
                null, null);
        }

        sleepIfRunningAgainstService(10 * 1000); // To allow tags to index


        PagedResponse<TaggedBlobItem> firstPage = primaryBlobServiceClient.findBlobsByTags(
            new FindBlobsOptions(String.format("\"%s\"='%s'", tagKey, tagValue))
                .setMaxResultsPerPage(5), null, Context.NONE).iterableByPage().iterator().next();
        String marker = firstPage.getContinuationToken();
        String firstBlobName = firstPage.getValue().get(0).getName();

        PagedResponse<TaggedBlobItem> secondPage = primaryBlobServiceClient.findBlobsByTags(
            new FindBlobsOptions(String.format("\"%s\"='%s'", tagKey, tagValue)).setMaxResultsPerPage(5), null,
            Context.NONE).iterableByPage(marker).iterator().next();

        // Assert that the second segment is indeed after the first alphabetically
        assertTrue(firstBlobName.compareTo(secondPage.getValue().get(0).getName()) < 0);

        // cleanup:
        cc.delete();
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @Test
    public void findBlobsMaxResults() {
        int numBlobs = 7;
        int pageResults = 3;
        BlobContainerClient cc = primaryBlobServiceClient.createBlobContainer(generateContainerName());
        Map<String, String> tags = Collections.singletonMap(tagKey, tagValue);

        for (int i = 0; i < numBlobs; i++) {
            cc.getBlobClient(generateBlobName()).uploadWithResponse(
                new BlobParallelUploadOptions(DATA.getDefaultInputStream(), DATA.getDefaultDataSize()).setTags(tags),
                null, null);
        }

        for (PagedResponse<TaggedBlobItem> page : primaryBlobServiceClient.findBlobsByTags(
            new FindBlobsOptions(String.format("\"%s\"='%s'", tagKey, tagValue)).setMaxResultsPerPage(pageResults),
            null, Context.NONE).iterableByPage()) {
            assertTrue(page.getValue().size() <= pageResults);
        }

        // cleanup:
        cc.delete();
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @Test
    public void findBlobsMaxResultsByPage() {
        int numBlobs = 7;
        int pageResults = 3;
        BlobContainerClient cc = primaryBlobServiceClient.createBlobContainer(generateContainerName());
        Map<String, String> tags = Collections.singletonMap(tagKey, tagValue);

        for (int i = 0; i < numBlobs; i++) {
            cc.getBlobClient(generateBlobName()).uploadWithResponse(
                new BlobParallelUploadOptions(DATA.getDefaultInputStream(), DATA.getDefaultDataSize()).setTags(tags),
                null, null);
        }

        for (PagedResponse<TaggedBlobItem> page : primaryBlobServiceClient.findBlobsByTags(
            new FindBlobsOptions(String.format("\"%s\"='%s'", tagKey, tagValue)), null, Context.NONE)
            .iterableByPage(pageResults)) {
            assertTrue(page.getValue().size() <= pageResults);
        }

        // cleanup:
        cc.delete();
    }

    @Test
    public void findBlobsError() {
        assertThrows(BlobStorageException.class, () ->
                primaryBlobServiceClient.findBlobsByTags("garbageTag").streamByPage().count());

    }

    @Test
    public void findBlobsAnonymous() {
        // Invalid query, but the anonymous check will fail before hitting the wire
        assertThrows(IllegalStateException.class, () ->
                anonymousClient.findBlobsByTags("foo=bar").iterator().next());

    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @Test
    public void findBlobsWithTimeoutStillBackedByPagedFlux() {
        int numBlobs = 5;
        int pageResults = 3;
        BlobContainerClient cc = primaryBlobServiceClient.createBlobContainer(generateContainerName());
        Map<String, String> tags = Collections.singletonMap(tagKey, tagValue);

        for (int i = 0; i < numBlobs; i++) {
            cc.getBlobClient(generateBlobName()).uploadWithResponse(
                new BlobParallelUploadOptions(DATA.getDefaultInputStream(), DATA.getDefaultDataSize()).setTags(tags),
                null, null);
        }

        // when: "Consume results by page, then still have paging functionality"
        assertDoesNotThrow(() -> primaryBlobServiceClient.findBlobsByTags(new FindBlobsOptions(
            String.format("\"%s\"='%s'", tagKey, tagValue)).setMaxResultsPerPage(pageResults), Duration.ofSeconds(10),
                Context.NONE).streamByPage().count());

        // cleanup:
        cc.delete();
    }

    private static void validatePropsSet(BlobServiceProperties sent, BlobServiceProperties received) {
        assertEquals(sent.getLogging().isRead(), received.getLogging().isRead());
        assertEquals(sent.getLogging().isWrite(), received.getLogging().isWrite());
        assertEquals(sent.getLogging().isDelete(), received.getLogging().isDelete());
        assertEquals(sent.getLogging().getVersion(), received.getLogging().getVersion());
        assertEquals(sent.getLogging().getRetentionPolicy().isEnabled(),
            received.getLogging().getRetentionPolicy().isEnabled());
        assertEquals(sent.getLogging().getRetentionPolicy().getDays(),
            received.getLogging().getRetentionPolicy().getDays());
        assertEquals(sent.getCors().size(), received.getCors().size());
        assertEquals(sent.getCors().get(0).getAllowedMethods(), received.getCors().get(0).getAllowedMethods());
        assertEquals(sent.getCors().get(0).getAllowedHeaders(), received.getCors().get(0).getAllowedHeaders());
        assertEquals(sent.getCors().get(0).getAllowedOrigins(), received.getCors().get(0).getAllowedOrigins());
        assertEquals(sent.getCors().get(0).getExposedHeaders(), received.getCors().get(0).getExposedHeaders());
        assertEquals(sent.getCors().get(0).getMaxAgeInSeconds(), received.getCors().get(0).getMaxAgeInSeconds());
        assertEquals(sent.getDefaultServiceVersion(), received.getDefaultServiceVersion());
        assertEquals(sent.getHourMetrics().isEnabled(), received.getHourMetrics().isEnabled());
        assertEquals(sent.getHourMetrics().isIncludeApis(), received.getHourMetrics().isIncludeApis());
        assertEquals(sent.getHourMetrics().getRetentionPolicy().isEnabled(),
            received.getHourMetrics().getRetentionPolicy().isEnabled());
        assertEquals(sent.getHourMetrics().getRetentionPolicy().getDays(),
            received.getHourMetrics().getRetentionPolicy().getDays());
        assertEquals(sent.getHourMetrics().getVersion(), received.getHourMetrics().getVersion());
        assertEquals(sent.getMinuteMetrics().isEnabled(), received.getMinuteMetrics().isEnabled());
        assertEquals(sent.getMinuteMetrics().isIncludeApis(), received.getMinuteMetrics().isIncludeApis());
        assertEquals(sent.getMinuteMetrics().getRetentionPolicy().isEnabled(),
            received.getMinuteMetrics().getRetentionPolicy().isEnabled());
        assertEquals(sent.getMinuteMetrics().getRetentionPolicy().getDays(),
            received.getMinuteMetrics().getRetentionPolicy().getDays());
        assertEquals(sent.getMinuteMetrics().getVersion(), received.getMinuteMetrics().getVersion());
        assertEquals(sent.getDeleteRetentionPolicy().isEnabled(), received.getDeleteRetentionPolicy().isEnabled());
        assertEquals(sent.getDeleteRetentionPolicy().getDays(), received.getDeleteRetentionPolicy().getDays());
        assertEquals(sent.getStaticWebsite().isEnabled(), received.getStaticWebsite().isEnabled());
        assertEquals(sent.getStaticWebsite().getIndexDocument(), received.getStaticWebsite().getIndexDocument());
        assertEquals(sent.getStaticWebsite().getErrorDocument404Path(),
            received.getStaticWebsite().getErrorDocument404Path());
    }

    @Test
    @ResourceLock("ServiceProperties")
    public void setGetProperties() {
        setInitialProperties();

        try {
            BlobRetentionPolicy retentionPolicy = new BlobRetentionPolicy().setDays(5).setEnabled(true);
            BlobAnalyticsLogging logging =
                new BlobAnalyticsLogging().setRead(true).setVersion("1.0").setRetentionPolicy(retentionPolicy);
            List<BlobCorsRule> corsRules = new ArrayList<>();
            corsRules.add(new BlobCorsRule()
                .setAllowedMethods("GET,PUT,HEAD")
                .setAllowedOrigins("*")
                .setAllowedHeaders("x-ms-version")
                .setExposedHeaders("x-ms-client-request-id")
                .setMaxAgeInSeconds(10));
            String defaultServiceVersion = "2016-05-31";
            BlobMetrics hourMetrics = new BlobMetrics()
                .setEnabled(true)
                .setVersion("1.0")
                .setRetentionPolicy(retentionPolicy)
                .setIncludeApis(true);
            BlobMetrics minuteMetrics = new BlobMetrics()
                .setEnabled(true)
                .setVersion("1.0")
                .setRetentionPolicy(retentionPolicy)
                .setIncludeApis(true);
            StaticWebsite website = new StaticWebsite().setEnabled(true).setIndexDocument("myIndex.html")
                .setErrorDocument404Path("custom/error/path.html");

            BlobServiceProperties sentProperties = new BlobServiceProperties()
                .setLogging(logging)
                .setCors(corsRules)
                .setDefaultServiceVersion(defaultServiceVersion)
                .setMinuteMetrics(minuteMetrics)
                .setHourMetrics(hourMetrics)
                .setDeleteRetentionPolicy(retentionPolicy)
                .setStaticWebsite(website);

            HttpHeaders headers =
                primaryBlobServiceClient.setPropertiesWithResponse(sentProperties, null, null).getHeaders();

            // Service properties may take up to 30s to take effect. If they weren't already in place, wait.
            sleepIfRunningAgainstService(30 * 1000);

            BlobServiceProperties receivedProperties = primaryBlobServiceClient.getProperties();

            assertNotNull(headers.getValue(X_MS_REQUEST_ID));
            assertNotNull(headers.getValue(X_MS_VERSION));
            validatePropsSet(sentProperties, receivedProperties);
        } finally {
            resetProperties();
        }
    }

    // In java, we don't have support from the validator for checking the bounds on days. The service will catch these.
    @Test
    @ResourceLock("ServiceProperties")
    public void setPropsMin() {
        setInitialProperties();

        try {
            BlobRetentionPolicy retentionPolicy = new BlobRetentionPolicy().setDays(5).setEnabled(true);
            BlobAnalyticsLogging logging =
                new BlobAnalyticsLogging().setRead(true).setVersion("1.0").setRetentionPolicy(retentionPolicy);
            List<BlobCorsRule> corsRules = new ArrayList<>();
            corsRules.add(new BlobCorsRule()
                .setAllowedMethods("GET,PUT,HEAD")
                .setAllowedOrigins("*")
                .setAllowedHeaders("x-ms-version")
                .setExposedHeaders("x-ms-client-request-id")
                .setMaxAgeInSeconds(10));
            String defaultServiceVersion = "2016-05-31";
            BlobMetrics hourMetrics = new BlobMetrics()
                .setEnabled(true)
                .setVersion("1.0")
                .setRetentionPolicy(retentionPolicy)
                .setIncludeApis(true);
            BlobMetrics minuteMetrics = new BlobMetrics()
                .setEnabled(true)
                .setVersion("1.0")
                .setRetentionPolicy(retentionPolicy)
                .setIncludeApis(true);
            StaticWebsite website = new StaticWebsite().setEnabled(true).setIndexDocument("myIndex.html")
                .setErrorDocument404Path("custom/error/path.html");

            BlobServiceProperties sentProperties = new BlobServiceProperties()
                .setLogging(logging)
                .setCors(corsRules)
                .setDefaultServiceVersion(defaultServiceVersion)
                .setMinuteMetrics(minuteMetrics)
                .setHourMetrics(hourMetrics)
                .setDeleteRetentionPolicy(retentionPolicy)
                .setStaticWebsite(website);

            assertResponseStatusCode(primaryBlobServiceClient.setPropertiesWithResponse(sentProperties, null, null), 202);
        } finally {
            resetProperties();
        }
    }

    @Test
    @ResourceLock("ServiceProperties")
    public void setPropsCorsCheck() {
        setInitialProperties();

        try {
            BlobServiceProperties serviceProperties = primaryBlobServiceClient.getProperties();

            // Some properties are not set and this test validates that they are not null when sent to the service
            BlobCorsRule rule = new BlobCorsRule()
                .setAllowedOrigins("microsoft.com")
                .setMaxAgeInSeconds(60)
                .setAllowedMethods("GET")
                .setAllowedHeaders("x-ms-version");

            serviceProperties.setCors(Collections.singletonList(rule));
            assertResponseStatusCode(primaryBlobServiceClient.setPropertiesWithResponse(serviceProperties, null, null),
                202);
        } finally {
            resetProperties();
        }
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @Test
    @ResourceLock("ServiceProperties")
    public void setPropsStaticWebsite() {
        setInitialProperties();

        try {
            BlobServiceProperties serviceProperties = primaryBlobServiceClient.getProperties();
            String errorDocument404Path = "error/404.html";
            String defaultIndexDocumentPath = "index.html";

            serviceProperties.setStaticWebsite(new StaticWebsite()
                .setEnabled(true)
                .setErrorDocument404Path(errorDocument404Path)
                .setDefaultIndexDocumentPath(defaultIndexDocumentPath));

            Response<Void> resp = primaryBlobServiceClient.setPropertiesWithResponse(serviceProperties, null, null);

            assertResponseStatusCode(resp, 202);
            StaticWebsite staticWebsite = primaryBlobServiceClient.getProperties().getStaticWebsite();
            assertTrue(staticWebsite.isEnabled());
            assertEquals(errorDocument404Path, staticWebsite.getErrorDocument404Path());
            assertEquals(defaultIndexDocumentPath, staticWebsite.getDefaultIndexDocumentPath());
        } finally {
            resetProperties();
        }
    }

    @Test
    @ResourceLock("ServiceProperties")
    public void setPropsError() {
        assertThrows(Exception.class, () -> getServiceClient(ENVIRONMENT.getPrimaryAccount().getCredential(),
            "https://error.blob.core.windows.net").setProperties(new BlobServiceProperties()));
    }

    @Test
    @ResourceLock("ServiceProperties")
    public void setPropsAnonymous() {
        assertThrows(IllegalStateException.class, () -> anonymousClient.setProperties(new BlobServiceProperties()));
    }

    @Test
    @ResourceLock("ServiceProperties")
    public void getPropsMin() {
        setInitialProperties();

        try {
            assertResponseStatusCode(primaryBlobServiceClient.getPropertiesWithResponse(null, null), 200);
        } finally {
            resetProperties();
        }
    }

    @Test
    public void getPropsError() {
        assertThrows(Exception.class, () -> getServiceClient(ENVIRONMENT.getPrimaryAccount().getCredential(),
            "https://error.blob.core.windows.net").getProperties());
    }

    @Test
    public void getPropsAnonymous() {
        assertThrows(IllegalStateException.class, () -> anonymousClient.getProperties());
    }

    @Test
    public void getUserDelegationKey() {
        OffsetDateTime start = testResourceNamer.now();
        OffsetDateTime expiry = start.plusDays(1);

        Response<UserDelegationKey> response = getOAuthServiceClient()
            .getUserDelegationKeyWithResponse(start, expiry, null, null);

        assertResponseStatusCode(response, 200);
        assertNotNull(response.getValue());
        assertNotNull(response.getValue().getSignedObjectId());
        assertNotNull(response.getValue().getSignedTenantId());
        assertNotNull(response.getValue().getSignedStart());
        assertNotNull(response.getValue().getSignedExpiry());
        assertNotNull(response.getValue().getSignedService());
        assertNotNull(response.getValue().getSignedVersion());
        assertNotNull(response.getValue().getValue());
    }

    @Test
    public void getUserDelegationKeyMin() {
        OffsetDateTime expiry = testResourceNamer.now().plusDays(1);

        Response<UserDelegationKey> response = getOAuthServiceClient().getUserDelegationKeyWithResponse(null, expiry,
            null, null);

        assertResponseStatusCode(response, 200);
    }

    @ParameterizedTest
    @MethodSource("getUserDelegationKeyErrorSupplier")
    public void getUserDelegationKeyError(OffsetDateTime start, OffsetDateTime expiry,
        Class<? extends Throwable> exception) {
        assertThrows(exception, () -> getOAuthServiceClient().getUserDelegationKey(start, expiry));
    }

    private static Stream<Arguments> getUserDelegationKeyErrorSupplier() {
        return Stream.of(
            Arguments.of(null, null, NullPointerException.class),
            Arguments.of(OffsetDateTime.now(), OffsetDateTime.now().minusDays(1), IllegalArgumentException.class)
        );
    }

    @Test
    public void getUserDelegationKeyAnonymous() {
        assertThrows(IllegalStateException.class, () ->
            anonymousClient.getUserDelegationKey(null, testResourceNamer.now().plusDays(1)));
    }

    @Test
    public void getStats() {
        BlobServiceClient serviceClient = getServiceClient(ENVIRONMENT.getPrimaryAccount().getCredential(),
            ENVIRONMENT.getPrimaryAccount().getBlobEndpointSecondary());
        Response<BlobServiceStatistics> response = serviceClient.getStatisticsWithResponse(null, null);

        assertNotNull(response.getHeaders().getValue(X_MS_VERSION));
        assertNotNull(response.getHeaders().getValue(X_MS_REQUEST_ID));
        assertNotNull(response.getHeaders().getValue(HttpHeaderName.DATE));
        assertNotNull(response.getValue().getGeoReplication());

        // The LastSyncTime will return a DateTimeRfc1123 if the replication status is LIVE
        // but there are two other statuses, unavailable and bootstrap, which will return null.
        if (response.getValue().getGeoReplication().getStatus() == GeoReplicationStatus.LIVE) {
            assertNotNull(response.getValue().getGeoReplication().getLastSyncTime());
        } else {
            assertNull(response.getValue().getGeoReplication().getLastSyncTime());
        }
    }

    @Test
    public void getStatsMin() {
        BlobServiceClient serviceClient = getServiceClient(ENVIRONMENT.getPrimaryAccount().getCredential(),
            ENVIRONMENT.getPrimaryAccount().getBlobEndpointSecondary());

        assertResponseStatusCode(serviceClient.getStatisticsWithResponse(null, null), 200);
    }

    @Test
    public void getStatsError() {
        assertThrows(BlobStorageException.class, () -> primaryBlobServiceClient.getStatistics());
    }

    @Test
    public void getStatsAnonymous() {
        assertThrows(IllegalStateException.class, () -> anonymousClient.getStatistics());
    }

    @Test
    public void getAccountInfo() {
        Response<StorageAccountInfo> response = primaryBlobServiceClient.getAccountInfoWithResponse(null, null);

        assertNotNull(response.getHeaders().getValue(HttpHeaderName.DATE));
        assertNotNull(response.getHeaders().getValue(X_MS_VERSION));
        assertNotNull(response.getHeaders().getValue(X_MS_REQUEST_ID));
        assertNotNull(response.getValue().getAccountKind());
        assertNotNull(response.getValue().getSkuName());
    }

    @Test
    public void getAccountInfoMin() {
        assertResponseStatusCode(primaryBlobServiceClient.getAccountInfoWithResponse(null, null), 200);
    }

    // This test validates a fix for a bug that caused NPE to be thrown when the account did not exist.
    @Test
    @ResourceLock("ServiceProperties")
    public void invalidAccountName() throws MalformedURLException {
        URL badURL = new URL("http://fake.blobfake.core.windows.net");
        BlobServiceClient client = getServiceClientBuilder(ENVIRONMENT.getPrimaryAccount().getCredential(),
            badURL.toString())
            .retryOptions(new RequestRetryOptions(RetryPolicyType.FIXED, 2, 60, 100L, 1000L, null))
            .buildClient();

        assertThrows(RuntimeException.class, client::getProperties);
    }

    @Test
    public void getAccountInfoAnonymous() {
        assertThrows(IllegalStateException.class, () -> anonymousClient.getAccountInfo());
    }

    @Test
    public void getAccountSasAnonymous() {
        OffsetDateTime expiryTime = testResourceNamer.now().plusDays(1);
        AccountSasPermission permissions = new AccountSasPermission().setReadPermission(true);
        AccountSasService services = new AccountSasService().setBlobAccess(true);
        AccountSasResourceType resourceTypes = new AccountSasResourceType().setService(true);

        assertThrows(IllegalStateException.class, () -> anonymousClient.generateAccountSas(
            new AccountSasSignatureValues(expiryTime, permissions, services, resourceTypes)));
    }

    @Test
    public void builderCpkValidation() {
        String endpoint = BlobUrlParts.parse(primaryBlobServiceClient.getAccountUrl()).setScheme("http").toUrl()
            .toString();
        BlobServiceClientBuilder builder = new BlobServiceClientBuilder()
            .customerProvidedKey(new CustomerProvidedKey(Base64.getEncoder()
                .encodeToString(getRandomByteArray(256)))).endpoint(endpoint);

        assertThrows(IllegalArgumentException.class, builder::buildClient);
    }

    @Test
    public void builderBearerTokenValidation() {
        String endpoint = BlobUrlParts.parse(primaryBlobServiceClient.getAccountUrl()).setScheme("http").toUrl()
            .toString();
        BlobServiceClientBuilder builder = new BlobServiceClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint);

        assertThrows(IllegalArgumentException.class, builder::buildClient);
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @Test
    public void restoreContainer() {
        BlobContainerClient cc1 = primaryBlobServiceClient.getBlobContainerClient(generateContainerName());
        cc1.create();
        String blobName = generateBlobName();
        cc1.getBlobClient(blobName).upload(DATA.getDefaultInputStream(), 7);
        cc1.delete();
        BlobContainerItem blobContainerItem = primaryBlobServiceClient.listBlobContainers(
            new ListBlobContainersOptions()
                .setPrefix(cc1.getBlobContainerName())
                .setDetails(new BlobContainerListDetails().setRetrieveDeleted(true)),
            null).stream().iterator().next();

        sleepIfRunningAgainstService(30000);

        BlobContainerClient restoredContainerClient = primaryBlobServiceClient.undeleteBlobContainer(
            blobContainerItem.getName(), blobContainerItem.getVersion());

        assertEquals(1, restoredContainerClient.listBlobs().stream().count());
        assertEquals(blobName, restoredContainerClient.listBlobs().stream().iterator().next().getName());
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @PlaybackOnly
    @Test
    public void restoreContainerIntoOtherContainer() {
        BlobContainerClient cc1 = primaryBlobServiceClient.getBlobContainerClient(generateContainerName());
        cc1.create();
        String blobName = generateBlobName();
        cc1.getBlobClient(blobName).upload(DATA.getDefaultInputStream(), 7);
        cc1.delete();
        BlobContainerItem blobContainerItem = primaryBlobServiceClient.listBlobContainers(
            new ListBlobContainersOptions()
                .setPrefix(cc1.getBlobContainerName())
                .setDetails(new BlobContainerListDetails().setRetrieveDeleted(true)), null).iterator().next();

        sleepIfRunningAgainstService(30000);

        BlobContainerClient restoredContainerClient =
            primaryBlobServiceClient.undeleteBlobContainerWithResponse(
                new UndeleteBlobContainerOptions(blobContainerItem.getName(), blobContainerItem.getVersion()), null,
                Context.NONE).getValue();

        assertEquals(1, restoredContainerClient.listBlobs().stream().count());
        assertEquals(blobName, restoredContainerClient.listBlobs().stream().iterator().next().getName());
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @Test
    public void restoreContainerWithResponse() {
        BlobContainerClient cc1 = primaryBlobServiceClient.getBlobContainerClient(generateContainerName());
        cc1.create();
        String blobName = generateBlobName();
        cc1.getBlobClient(blobName).upload(DATA.getDefaultInputStream(), 7);
        cc1.delete();
        BlobContainerItem blobContainerItem = primaryBlobServiceClient.listBlobContainers(
            new ListBlobContainersOptions()
                .setPrefix(cc1.getBlobContainerName())
                .setDetails(new BlobContainerListDetails().setRetrieveDeleted(true)), null).iterator().next();

        sleepIfRunningAgainstService(30000);

        Response<BlobContainerClient> response = primaryBlobServiceClient.undeleteBlobContainerWithResponse(
            new UndeleteBlobContainerOptions(blobContainerItem.getName(), blobContainerItem.getVersion()),
            Duration.ofMinutes(1), Context.NONE);
        BlobContainerClient restoredContainerClient = response.getValue();

        assertNotNull(response);
        assertResponseStatusCode(response, 201);
        assertEquals(1, restoredContainerClient.listBlobs().stream().count());
        assertEquals(blobName, restoredContainerClient.listBlobs().stream().iterator().next().getName());
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @Test
    public void restoreContainerError() {
        assertThrows(BlobStorageException.class,
            () -> primaryBlobServiceClient.undeleteBlobContainer(generateContainerName(), "01D60F8BB59A4652"));
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @Test
    public void restoreContainerIntoExistingContainerError() {
        BlobContainerClient cc1 = primaryBlobServiceClient.getBlobContainerClient(generateContainerName());
        cc1.create();
        String blobName = generateBlobName();
        cc1.getBlobClient(blobName).upload(DATA.getDefaultInputStream(), 7);
        cc1.delete();
        BlobContainerItem blobContainerItem = primaryBlobServiceClient.listBlobContainers(
            new ListBlobContainersOptions()
                .setPrefix(cc1.getBlobContainerName())
                .setDetails(new BlobContainerListDetails().setRetrieveDeleted(true)),
            null).iterator().next();

        sleepIfRunningAgainstService(30000);

        BlobContainerClient cc2 = primaryBlobServiceClient.createBlobContainer(generateContainerName());
        assertThrows(BlobStorageException.class, () -> primaryBlobServiceClient.undeleteBlobContainerWithResponse(
            new UndeleteBlobContainerOptions(blobContainerItem.getName(), blobContainerItem.getVersion())
                .setDestinationContainerName(cc2.getBlobContainerName()), null, Context.NONE));
    }

    @Test
    public void oAuthOnSecondary() {
        BlobServiceClientBuilder secondaryBuilder = getServiceClientBuilder(null,
            ENVIRONMENT.getPrimaryAccount().getBlobEndpointSecondary());
        BlobServiceClient secondaryClient = secondaryBuilder
            .credential(StorageCommonTestUtils.getTokenCredential(interceptorManager)).buildClient();

        assertDoesNotThrow(secondaryClient::getProperties);
    }

    @ParameterizedTest
    @MethodSource("sasTokenDoesNotShowUpOnInvalidUriSupplier")
    public void sasTokenDoesNotShowUpOnInvalidUri(String service, String container) {
        /* random sas token. this does not actually authenticate anything. */
        String mockSas =
            "?sv=2019-10-10&ss=b&srt=sco&sp=r&se=2019-06-04T12:04:58Z&st=2090-05-04T04:04:58Z&spr=http&sig=doesntmatter";

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> {
            BlobServiceClient client = new BlobServiceClientBuilder().endpoint(service).sasToken(mockSas).buildClient();
            client.getBlobContainerClient(container).getBlobClient("blobname");
        });

        assertFalse(e.getMessage().contains(mockSas));

    }

    /* Note: the check is on the blob builder as well but I can't test it this way since we encode all blob names - so
    it will not be invalid. */
    private static Stream<Arguments> sasTokenDoesNotShowUpOnInvalidUriSupplier() {
        return Stream.of(
            Arguments.of("https://doesntmatter. blob.core.windows.net", "containername"),
            Arguments.of("https://doesntmatter.blob.core.windows.net", "container name"));
    }

    @Test
    // This tests the policy is in the right place because if it were added per retry, it would be after the credentials
    // and auth would fail because we changed a signed header.
    public void perCallPolicy() {
        BlobServiceClient sc = getServiceClientBuilder(ENVIRONMENT.getPrimaryAccount().getCredential(),
            primaryBlobServiceClient.getAccountUrl())
            .addPolicy(getPerCallVersionPolicy())
            .buildClient();

        Response<BlobServiceProperties> response = sc.getPropertiesWithResponse(null, null);
        assertEquals("2017-11-09", response.getHeaders().getValue(X_MS_VERSION));
    }

    @Test
    public void createContainerIfNotExists() {
        String containerName = generateContainerName();
        Response<BlobContainerClient> response = primaryBlobServiceClient
            .createBlobContainerIfNotExistsWithResponse(containerName, null, null);
        Response<BlobContainerClient> response2 = primaryBlobServiceClient
            .createBlobContainerIfNotExistsWithResponse(containerName, null, null);

        assertResponseStatusCode(response, 201);
        assertResponseStatusCode(response2, 409);
    }

    @Test
    public void deleteContainerIfExists() {
        String containerName = generateContainerName();
        primaryBlobServiceClient.createBlobContainer(containerName);

        Response<Boolean> response = primaryBlobServiceClient.deleteBlobContainerIfExistsWithResponse(
            containerName, null);

        assertTrue(response.getValue());
        assertResponseStatusCode(response, 202);
    }

    @Test
    public void deleteContainerIfExistsMin() {
        String containerName = generateContainerName();
        primaryBlobServiceClient.createBlobContainer(containerName);

        assertTrue(primaryBlobServiceClient.deleteBlobContainerIfExists(containerName));
    }

    @Test
    public void deleteContainerIfExistsContainerDoesNotExist() {
        assertFalse(primaryBlobServiceClient.deleteBlobContainerIfExists(generateContainerName()));
    }

    // We can't guarantee that the requests will always happen before the container is garbage collected
    @PlaybackOnly
    @Test
    public void deleteContainerIfExistsAlreadyDeleted() {
        String containerName = generateContainerName();
        primaryBlobServiceClient.createBlobContainer(containerName);

        Response<Boolean> response = primaryBlobServiceClient.deleteBlobContainerIfExistsWithResponse(
            containerName, null);
        Response<Boolean> response2 = primaryBlobServiceClient.deleteBlobContainerIfExistsWithResponse(
            containerName, null);

        assertResponseStatusCode(response, 202);
        // Confirming the behavior of the api when the container is in the deleting state.
        // After delete has been called once but before it has been garbage collected
        assertResponseStatusCode(response2, 202);
    }

    @LiveOnly
    @Test
    public void serviceTimeoutPolicy() {
        BlobServiceClient serviceClient = new BlobServiceClientBuilder()
            .endpoint(ENVIRONMENT.getPrimaryAccount().getBlobEndpoint())
            .credential(ENVIRONMENT.getPrimaryAccount().getCredential())
            .addPolicy(new ServiceTimeoutPolicy(Duration.ofSeconds(1)))
            .buildClient();

        BlobContainerClient blobContainerClient = serviceClient.getBlobContainerClient(generateContainerName());
        blobContainerClient.createIfNotExists();
        BlobClient blobClient = blobContainerClient.getBlobClient(generateBlobName());

        // testing with large dataset that is guaranteed to take longer than the specified timeout (1 second)
        byte[] randomData = getRandomByteArray(256 * Constants.MB);
        ByteArrayInputStream input = new ByteArrayInputStream(randomData);

        BlobStorageException e = assertThrows(BlobStorageException.class, () ->
                blobClient.uploadWithResponse(new BlobParallelUploadOptions(input), null, null));

        assertEquals(BlobErrorCode.OPERATION_TIMED_OUT, e.getErrorCode());
    }

    @Test
    public void defaultAudience() {
        BlobServiceClient aadService = getServiceClientBuilderWithTokenCredential(cc.getBlobContainerUrl())
            .audience(null)
            .buildClient();

        assertNotNull(aadService.getProperties());
    }

    @Test
    public void storageAccountAudience() {
        BlobServiceClient aadService = getServiceClientBuilderWithTokenCredential(cc.getBlobContainerUrl())
            .audience(BlobAudience.createBlobServiceAccountAudience(cc.getAccountName()))
            .buildClient();

        assertNotNull(aadService.getProperties());
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2024-08-04")
    @LiveOnly
    @Test
    /* This test tests if the bearer challenge is working properly. A bad audience is passed in, the service returns
    the default audience, and the request gets retried with this default audience, making the call function as expected.
     */
    public void audienceErrorBearerChallengeRetry() {
        BlobServiceClient aadService = getServiceClientBuilderWithTokenCredential(cc.getBlobContainerUrl())
                .audience(BlobAudience.createBlobServiceAccountAudience("badAudience"))
                .buildClient();

        assertNotNull(aadService.getProperties());
    }

    @Test
    public void audienceFromString() {
        String url = String.format("https://%s.blob.core.windows.net/", cc.getAccountName());
        BlobAudience audience = BlobAudience.fromString(url);

        BlobServiceClient aadService = getServiceClientBuilderWithTokenCredential(cc.getBlobContainerUrl())
            .audience(audience)
            .buildClient();

        assertNotNull(aadService.getProperties());
    }

//    public void renameBlob() container() {
//        setup:
//        def oldName = generateContainerName()
//        def newName = generateContainerName()
//        primaryBlobServiceClient.createBlobContainer(oldName)
//
//        when:
//        def renamedContainer = primaryBlobServiceClient.renameBlobContainer(oldName, newName)
//
//        then:
//        renamedContainer.getPropertiesWithResponse(null, null, null), 200);
//
//        cleanup:
//        renamedContainer.delete()
//    }
//
//    public void renameBlob() container sas() {
//        setup:
//        def oldName = generateContainerName()
//        def newName = generateContainerName()
//        primaryBlobServiceClient.createBlobContainer(oldName)
//        def sas = primaryBlobServiceClient.generateAccountSas(new AccountSasSignatureValues(testResourceNamer.now().plusHours(1), AccountSasPermission.parse("rwdxlacuptf"), AccountSasService.parse("b"), AccountSasResourceType.parse("c")))
//        def serviceClient = getServiceClient(sas, primaryBlobServiceClient.getAccountUrl())
//
//        when:
//        def renamedContainer = serviceClient.renameBlobContainer(oldName, newName)
//
//        then:
//        renamedContainer.getPropertiesWithResponse(null, null, null), 200);
//
//        cleanup:
//        renamedContainer.delete()
//    }
//
//    @ParameterizedTest
//    public void renameBlob() container AC() {
//        setup:
//        leaseID = setupContainerLeaseCondition(cc, leaseID)
//        BlobRequestConditions cac = new BlobRequestConditions()
//            .setLeaseId(leaseID)
//
//        expect:
//        primaryBlobServiceClient.renameBlobContainerWithResponse(cc.getBlobContainerName(),
//            new BlobContainerRenameOptions(generateContainerName()).setRequestConditions(cac),
//            null, null), 200);
//
//        where:
//        leaseID         || _
//        null            || _
//        receivedLeaseID || _
//    }
//
//    @ParameterizedTest
//    public void renameBlob() container AC fail() {
//        setup:
//        BlobRequestConditions cac = new BlobRequestConditions()
//            .setLeaseId(leaseID)
//
//        when:
//        primaryBlobServiceClient.renameBlobContainerWithResponse(cc.getBlobContainerName(),
//            new BlobContainerRenameOptions(generateContainerName()).setRequestConditions(cac),
//            null, null)
//
//        then:
//        assertThrows(BlobStorageException.class, () ->
//
//        where:
//        leaseID         || _
//        garbageLeaseID  || _
//    }
//
//    @ParameterizedTest
//    public void renameBlob() container AC illegal() {
//        setup:
//        BlobRequestConditions ac = new BlobRequestConditions().setIfMatch(match).setIfNoneMatch(noneMatch).setIfModifiedSince(modified).setIfUnmodifiedSince(unmodified).setTagsConditions(tags)
//
//        when:
//        primaryBlobServiceClient.renameBlobContainerWithResponse(cc.getBlobContainerName(),
//            new BlobContainerRenameOptions(generateContainerName()).setRequestConditions(ac),
//            null, null)
//
//        then:
//        thrown(UnsupportedOperationException)
//
//        where:
//        modified | unmodified | match        | noneMatch    | tags
//        oldDate  | null       | null         | null         | null
//        null     | newDate    | null         | null         | null
//        null     | null       | receivedEtag | null         | null
//        null     | null       | null         | garbageEtag  | null
//        null     | null       | null         | null         | "tags"
//    }
//
//    public void renameBlob() container error() {
//        setup:
//        def oldName = generateContainerName()
//        def newName = generateContainerName()
//
//        when:
//        primaryBlobServiceClient.renameBlobContainer(oldName, newName)
//
//        then:
//        assertThrows(BlobStorageException.class, () ->
//    }
}
