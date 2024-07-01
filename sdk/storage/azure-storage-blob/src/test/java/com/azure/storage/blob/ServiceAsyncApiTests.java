// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.test.TestMode;
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
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.models.CustomerProvidedKey;
import com.azure.storage.blob.models.GeoReplicationStatus;
import com.azure.storage.blob.models.ListBlobContainersOptions;
import com.azure.storage.blob.models.ParallelTransferOptions;
import com.azure.storage.blob.models.StaticWebsite;
import com.azure.storage.blob.models.TaggedBlobItem;
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
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ServiceAsyncApiTests extends BlobTestBase {

    private BlobServiceAsyncClient anonymousClient;
    private String tagKey;
    private String tagValue;

    @BeforeEach
    public void setup() {
        // We shouldn't be getting to the network layer anyway
        anonymousClient = new BlobServiceClientBuilder()
            .endpoint(ENVIRONMENT.getPrimaryAccount().getBlobEndpoint())
            .buildAsyncClient();

        tagKey = testResourceNamer.randomName(prefix, 20);
        tagValue = testResourceNamer.randomName(prefix, 20);
    }

    private void setInitialProperties() {
        BlobRetentionPolicy disabled = new BlobRetentionPolicy().setEnabled(false);
        primaryBlobServiceAsyncClient.setProperties(new BlobServiceProperties()
            .setStaticWebsite(new StaticWebsite().setEnabled(false))
            .setDeleteRetentionPolicy(disabled)
            .setCors(null)
            .setHourMetrics(new BlobMetrics().setVersion("1.0").setEnabled(false)
                .setRetentionPolicy(disabled))
            .setMinuteMetrics(new BlobMetrics().setVersion("1.0").setEnabled(false)
                .setRetentionPolicy(disabled))
            .setLogging(new BlobAnalyticsLogging().setVersion("1.0")
                .setRetentionPolicy(disabled))
            .setDefaultServiceVersion("2018-03-28")).block();
    }

    private void resetProperties() {
        BlobRetentionPolicy disabled = new BlobRetentionPolicy().setEnabled(false);
        primaryBlobServiceAsyncClient.setProperties(new BlobServiceProperties()
            .setStaticWebsite(new StaticWebsite().setEnabled(false))
            .setDeleteRetentionPolicy(disabled)
            .setCors(null)
            .setHourMetrics(new BlobMetrics().setVersion("1.0").setEnabled(false)
                .setRetentionPolicy(disabled))
            .setMinuteMetrics(new BlobMetrics().setVersion("1.0").setEnabled(false)
                .setRetentionPolicy(disabled))
            .setLogging(new BlobAnalyticsLogging().setVersion("1.0")
                .setRetentionPolicy(disabled))
            .setDefaultServiceVersion("2018-03-28")).block();
    }

    @Test
    public void listContainers() {
        StepVerifier.create(primaryBlobServiceAsyncClient.listBlobContainers(
            new ListBlobContainersOptions().setPrefix(prefix)))
            .thenConsumeWhile(c -> {
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
                return true;
            })
            .verifyComplete();
    }

    @Test
    public void listContainersMin() {
        StepVerifier.create(primaryBlobServiceAsyncClient.listBlobContainers())
            .thenConsumeWhile(r -> true)
            .verifyComplete();
    }

    @Test
    public void listContainersMarker() {
        for (int i = 0; i < 10; i++) {
            primaryBlobServiceAsyncClient.createBlobContainer(generateContainerName()).block();
        }

        ListBlobContainersOptions options = new ListBlobContainersOptions().setMaxResultsPerPage(5);
        PagedResponse<BlobContainerItem> firstPage = primaryBlobServiceAsyncClient.listBlobContainers(options)
            .byPage().blockFirst();
        String marker = firstPage.getContinuationToken();
        String firstContainerName = firstPage.getValue().get(0).getName();

        PagedResponse<BlobContainerItem> secondPage = primaryBlobServiceAsyncClient.listBlobContainers()
            .byPage(marker).blockLast();

        // Assert that the second segment is indeed after the first alphabetically
        assertTrue(firstContainerName.compareTo(secondPage.getValue().get(0).getName()) < 0);
    }

    @Test
    public void listContainersDetails() {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("foo", "bar");
        String containerName = generateContainerName();
        ccAsync = primaryBlobServiceAsyncClient.createBlobContainerWithResponse(containerName, metadata,
            null, null)
            .block().getValue();

        StepVerifier.create(primaryBlobServiceAsyncClient.listBlobContainers(new ListBlobContainersOptions()
            .setDetails(new BlobContainerListDetails().setRetrieveMetadata(true))
            .setPrefix(containerName)))
            .assertNext(r -> assertEquals(metadata, r.getMetadata()))
            .verifyComplete();
    }

    @Test
    public void listContainersMaxResults() {
        int numContainers = 5;
        int pageResults = 3;
        String containerNamePrefix = generateContainerName();

        List<BlobContainerAsyncClient> containers = new ArrayList<>();
        for (int i = 0; i < numContainers; i++) {
            containers.add(primaryBlobServiceAsyncClient.createBlobContainer(containerNamePrefix + i).block());
        }

        StepVerifier.create(primaryBlobServiceAsyncClient.listBlobContainers(
            new ListBlobContainersOptions().setPrefix(containerNamePrefix).setMaxResultsPerPage(pageResults)).byPage())
            .assertNext(r -> assertEquals(pageResults, r.getValue().size()))
            .expectNextCount(1)
            .verifyComplete();

        // cleanup:
        for (BlobContainerAsyncClient container : containers) {
            container.delete().block();
        }
    }

    @Test
    public void listContainersMaxResultsByPage() {
        int numContainers = 5;
        int pageResults = 3;
        String containerNamePrefix = generateContainerName();

        List<BlobContainerAsyncClient> containers = new ArrayList<>();
        for (int i = 0; i < numContainers; i++) {
            containers.add(primaryBlobServiceAsyncClient.createBlobContainer(containerNamePrefix + i).block());
        }

        StepVerifier.create(primaryBlobServiceAsyncClient.listBlobContainers(
            new ListBlobContainersOptions().setPrefix(containerNamePrefix)).byPage(pageResults))
            .thenConsumeWhile(r -> {
                assertTrue(r.getValue().size() <= pageResults);
                return true;
            })
            .verifyComplete();

        // cleanup:
        for (BlobContainerAsyncClient container : containers) {
            container.delete().block();
        }
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @Test
    public void listDeleted() {
        int numContainers = 5;
        String containerNamePrefix = generateContainerName();

        List<BlobContainerAsyncClient> containers = new ArrayList<>();
        for (int i = 0; i < numContainers; i++) {
            containers.add(primaryBlobServiceAsyncClient.createBlobContainer(containerNamePrefix + i).block());
        }

        // delete each container
        for (BlobContainerAsyncClient container : containers) {
            container.delete().block();
        }

        StepVerifier.create(primaryBlobServiceAsyncClient.listBlobContainers(new ListBlobContainersOptions()
            .setPrefix(containerNamePrefix).setDetails(new BlobContainerListDetails().setRetrieveDeleted(true))))
            .thenConsumeWhile(r -> {
                assertTrue(r.isDeleted());
                return true;
            })
            .verifyComplete();

        StepVerifier.create(primaryBlobServiceAsyncClient.listBlobContainers(new ListBlobContainersOptions()
            .setPrefix(containerNamePrefix).setDetails(new BlobContainerListDetails().setRetrieveDeleted(true))).count())
            .assertNext(r -> assertEquals(numContainers, r))
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @Test
    public void listWithAllDetails() {
        int numContainers = 5;
        String containerNamePrefix = generateContainerName();

        List<BlobContainerAsyncClient> containers = new ArrayList<>();
        for (int i = 0; i < numContainers; i++) {
            containers.add(primaryBlobServiceAsyncClient.createBlobContainer(containerNamePrefix + i).block());
        }

        // delete each container
        for (BlobContainerAsyncClient container : containers) {
            container.delete().block();
        }

        StepVerifier.create(primaryBlobServiceAsyncClient.listBlobContainers(
            new ListBlobContainersOptions().setPrefix(containerNamePrefix).setDetails(new BlobContainerListDetails()
                .setRetrieveDeleted(true)
                .setRetrieveMetadata(true))))
            .thenConsumeWhile(r -> {
                assertTrue(r.isDeleted());
                return true;
            })
            .verifyComplete();

        StepVerifier.create(primaryBlobServiceAsyncClient.listBlobContainers(
            new ListBlobContainersOptions().setPrefix(containerNamePrefix).setDetails(new BlobContainerListDetails()
                .setRetrieveDeleted(true)
                .setRetrieveMetadata(true))).count())
            .assertNext(r -> assertEquals(numContainers, r))
            .verifyComplete();
    }

    @Test
    public void listContainersError() {
        StepVerifier.create(primaryBlobServiceAsyncClient.listBlobContainers()
            .byPage("garbage continuation token").count())
            .verifyError(BlobStorageException.class);
    }

    @Test
    public void listContainersAnonymous() {
        StepVerifier.create(anonymousClient.listBlobContainers())
            .verifyError(IllegalStateException.class);
    }

    @Test
    public void listContainersWithTimeoutStillBackedByPagedFlux() {
        int numContainers = 5;
        int pageResults = 3;

        List<BlobContainerAsyncClient> containers = new ArrayList<>();
        for (int i = 0; i < numContainers; i++) {
            containers.add(primaryBlobServiceAsyncClient.createBlobContainer(generateContainerName()).block());
        }

        // when: "Consume results by page, then should still have paging functionality""
        StepVerifier.create(primaryBlobServiceAsyncClient.listBlobContainersWithOptionalTimeout(
            new ListBlobContainersOptions().setMaxResultsPerPage(pageResults),
            Duration.ofSeconds(10)).byPage().count())
            .expectNextCount(1)
            .verifyComplete();

        // cleanup:
        for (BlobContainerAsyncClient container : containers) {
            container.delete().block();
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
            primaryBlobServiceAsyncClient.setPropertiesWithResponse(serviceProps).block();

            sleepIfRunningAgainstService(30 * 1000); // allow the service properties to take effect

            StepVerifier.create(primaryBlobServiceAsyncClient.listBlobContainers(new ListBlobContainersOptions()
                .setDetails(new BlobContainerListDetails().setRetrieveSystemContainers(true))))
                .recordWith(ArrayList::new)
                .thenConsumeWhile(x -> true)
                .consumeRecordedWith(r -> assertTrue(r.stream().anyMatch(c -> c.getName().equals("$logs"))))
                .verifyComplete();
        } finally {
            resetProperties();
        }
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @Test
    public void findBlobsMin() {
        StepVerifier.create(primaryBlobServiceAsyncClient.findBlobsByTags("\"key\"='value'"))
            .thenConsumeWhile(r -> true)
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2020-04-08")
    @Test
    public void findBlobsQuery() {
        BlobContainerAsyncClient containerClient = primaryBlobServiceAsyncClient
            .createBlobContainer(generateContainerName()).block();
        BlobAsyncClient blobClient = containerClient.getBlobAsyncClient(generateBlobName());
        blobClient.uploadWithResponse(new BlobParallelUploadOptions(DATA.getDefaultInputStream(),
            DATA.getDefaultDataSize()).setTags(Collections.singletonMap("key", "value"))).block();
        blobClient = containerClient.getBlobAsyncClient(generateBlobName());
        blobClient.uploadWithResponse(new BlobParallelUploadOptions(DATA.getDefaultInputStream(),
            DATA.getDefaultDataSize()).setTags(Collections.singletonMap("bar", "foo"))).block();
        blobClient = containerClient.getBlobAsyncClient(generateBlobName());
        blobClient.upload(DATA.getDefaultFlux(), null).block();

        sleepIfRunningAgainstService(10 * 1000); // To allow tags to index

        StepVerifier.create(primaryBlobServiceAsyncClient.findBlobsByTags(
            String.format("@container='%s' AND \"bar\"='foo'", containerClient.getBlobContainerName())))
            .assertNext(r -> {
                assertEquals(1, r.getTags().size());
                assertEquals("foo", r.getTags().get("bar"));
            })
            .verifyComplete();

        // cleanup:
        containerClient.delete().block();
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @Test
    public void findBlobsMarker() {
        BlobContainerAsyncClient cc = primaryBlobServiceAsyncClient.createBlobContainer(generateContainerName()).block();
        Map<String, String> tags = Collections.singletonMap(tagKey, tagValue);
        for (int i = 0; i < 10; i++) {
            cc.getBlobAsyncClient(generateBlobName()).uploadWithResponse(
                new BlobParallelUploadOptions(DATA.getDefaultInputStream(), DATA.getDefaultDataSize()).setTags(tags)).block();
        }

        sleepIfRunningAgainstService(10 * 1000); // To allow tags to index

        PagedResponse<TaggedBlobItem> firstPage = primaryBlobServiceAsyncClient.findBlobsByTags(
            new FindBlobsOptions(String.format("\"%s\"='%s'", tagKey, tagValue))
                .setMaxResultsPerPage(5), null, Context.NONE).byPage().blockFirst();
        String marker = firstPage.getContinuationToken();
        String firstBlobName = firstPage.getValue().get(0).getName();

        PagedResponse<TaggedBlobItem> secondPage = primaryBlobServiceAsyncClient.findBlobsByTags(
            new FindBlobsOptions(String.format("\"%s\"='%s'", tagKey, tagValue)).setMaxResultsPerPage(5), null,
            Context.NONE).byPage(marker).blockLast();

        // Assert that the second segment is indeed after the first alphabetically
        assertTrue(firstBlobName.compareTo(secondPage.getValue().get(0).getName()) < 0);

        // cleanup:
        cc.delete().block();
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @Test
    public void findBlobsMaxResults() {
        int numBlobs = 7;
        int pageResults = 3;
        BlobContainerAsyncClient cc = primaryBlobServiceAsyncClient.createBlobContainer(generateContainerName()).block();
        Map<String, String> tags = Collections.singletonMap(tagKey, tagValue);

        for (int i = 0; i < numBlobs; i++) {
            cc.getBlobAsyncClient(generateBlobName()).uploadWithResponse(
                new BlobParallelUploadOptions(DATA.getDefaultInputStream(), DATA.getDefaultDataSize()).setTags(tags)).block();
        }

        StepVerifier.create(primaryBlobServiceAsyncClient.findBlobsByTags(
            new FindBlobsOptions(String.format("\"%s\"='%s'", tagKey, tagValue)).setMaxResultsPerPage(pageResults),
            null, Context.NONE).byPage())
            .thenConsumeWhile(r -> {
                assertTrue(r.getValue().size() <= pageResults);
                return true;
            })
            .verifyComplete();

        // cleanup:
        cc.delete().block();
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @Test
    public void findBlobsMaxResultsByPage() {
        int numBlobs = 7;
        int pageResults = 3;
        BlobContainerAsyncClient cc = primaryBlobServiceAsyncClient.createBlobContainer(generateContainerName()).block();
        Map<String, String> tags = Collections.singletonMap(tagKey, tagValue);

        for (int i = 0; i < numBlobs; i++) {
            cc.getBlobAsyncClient(generateBlobName()).uploadWithResponse(
                new BlobParallelUploadOptions(DATA.getDefaultInputStream(), DATA.getDefaultDataSize()).setTags(tags)).block();
        }

        StepVerifier.create(primaryBlobServiceAsyncClient.findBlobsByTags(
            new FindBlobsOptions(String.format("\"%s\"='%s'", tagKey, tagValue))).byPage(pageResults))
            .thenConsumeWhile(r -> {
                assertTrue(r.getValue().size() <= pageResults);
                return true;
            })
            .verifyComplete();

        // cleanup:
        cc.delete().block();
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @Test
    public void findBlobsByPageAsync() {
        BlobContainerAsyncClient containerAsyncClient =
            primaryBlobServiceAsyncClient.getBlobContainerAsyncClient(generateContainerName());
        containerAsyncClient.create().block();
        Map<String, String> tags = Collections.singletonMap(tagKey, tagValue);

        for (int i = 0; i < 15; i++) {
            ccAsync.getBlobAsyncClient(generateBlobName()).uploadWithResponse(
                new BlobParallelUploadOptions(DATA.getDefaultInputStream()).setTags(tags)).block();
        }
        sleepIfRunningAgainstService(10 * 1000); // To allow tags to index
        String query = String.format("\"%s\"='%s'", tagKey, tagValue);
        FindBlobsOptions searchOptions = new FindBlobsOptions(query).setMaxResultsPerPage(12);

        List<TaggedBlobItem> list = primaryBlobServiceAsyncClient
            .findBlobsByTags(searchOptions)
            .byPage(10) // byPage should take precedence
            .take(1, true)
            .concatMapIterable(ContinuablePage::getElements).collectList().block();

        assertEquals(10, list.size());

        List<TaggedBlobItem> list2 = primaryBlobServiceAsyncClient
            .findBlobsByTags(searchOptions)
            .byPage() // since no number is specified, it should use the max number specified in options
            .take(1, true)
            .concatMapIterable(ContinuablePage::getElements).collectList().block();

        assertEquals(12, list2.size());
    }

    @Test
    public void findBlobsError() {
        StepVerifier.create(primaryBlobServiceAsyncClient.findBlobsByTags("garbageTag").byPage().count())
            .verifyError(BlobStorageException.class);
    }

    @Test
    public void findBlobsAnonymous() {
        // Invalid query, but the anonymous check will fail before hitting the wire
        StepVerifier.create(anonymousClient.findBlobsByTags("foo=bar"))
            .verifyError(IllegalStateException.class);
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @Test
    public void findBlobsWithTimeoutStillBackedByPagedFlux() {
        int numBlobs = 5;
        int pageResults = 3;
        BlobContainerAsyncClient cc = primaryBlobServiceAsyncClient.createBlobContainer(generateContainerName()).block();
        Map<String, String> tags = Collections.singletonMap(tagKey, tagValue);

        for (int i = 0; i < numBlobs; i++) {
            cc.getBlobAsyncClient(generateBlobName()).uploadWithResponse(
                new BlobParallelUploadOptions(DATA.getDefaultInputStream(), DATA.getDefaultDataSize()).setTags(tags)).block();
        }

        // when: "Consume results by page, then still have paging functionality"
        StepVerifier.create(primaryBlobServiceAsyncClient.findBlobsByTags(new FindBlobsOptions(
            String.format("\"%s\"='%s'", tagKey, tagValue)).setMaxResultsPerPage(pageResults)).byPage().count())
            .expectNextCount(1)
            .verifyComplete();

        // cleanup:
        cc.delete().block();
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

            StepVerifier.create(primaryBlobServiceAsyncClient.setPropertiesWithResponse(sentProperties))
                .assertNext(r -> {
                    assertNotNull(r.getHeaders().getValue(X_MS_REQUEST_ID));
                    assertNotNull(r.getHeaders().getValue(X_MS_VERSION));
                })
                .verifyComplete();

            // Service properties may take up to 30s to take effect. If they weren't already in place, wait.
            sleepIfRunningAgainstService(30 * 1000);


            StepVerifier.create(primaryBlobServiceAsyncClient.getProperties())
                .assertNext(r -> validatePropsSet(sentProperties, r))
                .verifyComplete();
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

            assertAsyncResponseStatusCode(primaryBlobServiceAsyncClient.setPropertiesWithResponse(sentProperties),
                202);
        } finally {
            resetProperties();
        }
    }

    @Test
    @ResourceLock("ServiceProperties")
    public void setPropsCorsCheck() {
        setInitialProperties();

        try {
            BlobServiceProperties serviceProperties = primaryBlobServiceAsyncClient.getProperties().block();

            // Some properties are not set and this test validates that they are not null when sent to the service
            BlobCorsRule rule = new BlobCorsRule()
                .setAllowedOrigins("microsoft.com")
                .setMaxAgeInSeconds(60)
                .setAllowedMethods("GET")
                .setAllowedHeaders("x-ms-version");

            serviceProperties.setCors(Collections.singletonList(rule));
            assertAsyncResponseStatusCode(primaryBlobServiceAsyncClient.setPropertiesWithResponse(serviceProperties),
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
            BlobServiceProperties serviceProperties = primaryBlobServiceAsyncClient.getProperties().block();
            String errorDocument404Path = "error/404.html";
            String defaultIndexDocumentPath = "index.html";

            serviceProperties.setStaticWebsite(new StaticWebsite()
                .setEnabled(true)
                .setErrorDocument404Path(errorDocument404Path)
                .setDefaultIndexDocumentPath(defaultIndexDocumentPath));

            assertAsyncResponseStatusCode(primaryBlobServiceAsyncClient.setPropertiesWithResponse(serviceProperties),
                202);

            StepVerifier.create(primaryBlobServiceAsyncClient.getProperties())
                .assertNext(r -> {
                    assertTrue(r.getStaticWebsite().isEnabled());
                    assertEquals(errorDocument404Path, r.getStaticWebsite().getErrorDocument404Path());
                    assertEquals(defaultIndexDocumentPath, r.getStaticWebsite().getDefaultIndexDocumentPath());
                })
                .verifyComplete();
        } finally {
            resetProperties();
        }
    }

    @Test
    @ResourceLock("ServiceProperties")
    public void setPropsError() {
        StepVerifier.create(getServiceAsyncClient(ENVIRONMENT.getPrimaryAccount().getCredential(),
            "https://error.blob.core.windows.net").setProperties(new BlobServiceProperties()))
            .verifyError(Exception.class);
    }

    @Test
    @ResourceLock("ServiceProperties")
    public void setPropsAnonymous() {
        StepVerifier.create(anonymousClient.setProperties(new BlobServiceProperties()))
            .verifyError(IllegalStateException.class);
    }

    @Test
    @ResourceLock("ServiceProperties")
    public void getPropsMin() {
        setInitialProperties();

        try {
            assertAsyncResponseStatusCode(primaryBlobServiceAsyncClient.getPropertiesWithResponse(),
                200);
        } finally {
            resetProperties();
        }
    }

    @Test
    public void getPropsError() {
        StepVerifier.create(getServiceAsyncClient(ENVIRONMENT.getPrimaryAccount().getCredential(),
            "https://error.blob.core.windows.net").getProperties())
            .verifyError(Exception.class);
    }

    @Test
    public void getPropsAnonymous() {
        StepVerifier.create(anonymousClient.getProperties())
            .verifyError(IllegalStateException.class);
    }

    @Test
    public void getUserDelegationKey() {
        OffsetDateTime start = testResourceNamer.now();
        OffsetDateTime expiry = start.plusDays(1);

        StepVerifier.create(getOAuthServiceAsyncClient().getUserDelegationKeyWithResponse(start, expiry))
            .assertNext(r -> {
                assertResponseStatusCode(r, 200);
                assertNotNull(r.getValue());
                assertNotNull(r.getValue().getSignedObjectId());
                assertNotNull(r.getValue().getSignedTenantId());
                assertNotNull(r.getValue().getSignedStart());
                assertNotNull(r.getValue().getSignedExpiry());
                assertNotNull(r.getValue().getSignedService());
                assertNotNull(r.getValue().getSignedVersion());
                assertNotNull(r.getValue().getValue());
            })
            .verifyComplete();
    }

    @Test
    public void getUserDelegationKeyMin() {
        OffsetDateTime expiry = testResourceNamer.now().plusDays(1);

        assertAsyncResponseStatusCode(getOAuthServiceAsyncClient().getUserDelegationKeyWithResponse(null, expiry),
            200);
    }

    @ParameterizedTest
    @MethodSource("getUserDelegationKeyErrorSupplier")
    public void getUserDelegationKeyError(OffsetDateTime start, OffsetDateTime expiry,
                                          Class<? extends Throwable> exception) {
        StepVerifier.create(getOAuthServiceAsyncClient().getUserDelegationKey(start, expiry))
            .verifyError(exception);
    }

    private static Stream<Arguments> getUserDelegationKeyErrorSupplier() {
        return Stream.of(
            Arguments.of(null, null, NullPointerException.class),
            Arguments.of(OffsetDateTime.now(), OffsetDateTime.now().minusDays(1), IllegalArgumentException.class)
        );
    }

    @Test
    public void getUserDelegationKeyAnonymous() {
        StepVerifier.create(anonymousClient.getUserDelegationKey(null, testResourceNamer.now().plusDays(1)))
            .verifyError(IllegalStateException.class);
    }

    @Test
    public void getStats() {
        BlobServiceAsyncClient serviceClient = getServiceAsyncClient(ENVIRONMENT.getPrimaryAccount().getCredential(),
            ENVIRONMENT.getPrimaryAccount().getBlobEndpointSecondary());
        StepVerifier.create(serviceClient.getStatisticsWithResponse())
            .assertNext(r -> {
                assertNotNull(r.getHeaders().getValue(X_MS_VERSION));
                assertNotNull(r.getHeaders().getValue(X_MS_REQUEST_ID));
                assertNotNull(r.getHeaders().getValue(HttpHeaderName.DATE));
                assertNotNull(r.getValue().getGeoReplication());
                // The LastSyncTime will return a DateTimeRfc1123 if the replication status is LIVE
                // but there are two other statuses, unavailable and bootstrap, which will return null.
                if (r.getValue().getGeoReplication().getStatus() == GeoReplicationStatus.LIVE) {
                    assertNotNull(r.getValue().getGeoReplication().getLastSyncTime());
                } else {
                    assertNull(r.getValue().getGeoReplication().getLastSyncTime());
                }
            })
            .verifyComplete();
    }

    @Test
    public void getStatsMin() {
        BlobServiceAsyncClient serviceClient = getServiceAsyncClient(ENVIRONMENT.getPrimaryAccount().getCredential(),
            ENVIRONMENT.getPrimaryAccount().getBlobEndpointSecondary());

        assertAsyncResponseStatusCode(serviceClient.getStatisticsWithResponse(), 200);
    }

    @Test
    public void getStatsError() {
        StepVerifier.create(primaryBlobServiceAsyncClient.getStatistics())
            .verifyError(BlobStorageException.class);
    }

    @Test
    public void getStatsAnonymous() {
        StepVerifier.create(anonymousClient.getStatistics())
            .verifyError(IllegalStateException.class);
    }

    @Test
    public void getAccountInfo() {
        StepVerifier.create(primaryBlobServiceAsyncClient.getAccountInfoWithResponse())
            .assertNext(r -> {
                assertNotNull(r.getHeaders().getValue(HttpHeaderName.DATE));
                assertNotNull(r.getHeaders().getValue(X_MS_VERSION));
                assertNotNull(r.getHeaders().getValue(X_MS_REQUEST_ID));
                assertNotNull(r.getValue().getAccountKind());
                assertNotNull(r.getValue().getSkuName());
            })
            .verifyComplete();
    }

    @Test
    public void getAccountInfoMin() {
        assertAsyncResponseStatusCode(primaryBlobServiceAsyncClient.getAccountInfoWithResponse(), 200);
    }

    // This test validates a fix for a bug that caused NPE to be thrown when the account did not exist.
    @Test
    @ResourceLock("ServiceProperties")
    public void invalidAccountName() throws MalformedURLException {
        URL badURL = new URL("http://fake.blobfake.core.windows.net");
        BlobServiceAsyncClient client = getServiceClientBuilder(ENVIRONMENT.getPrimaryAccount().getCredential(),
            badURL.toString()).retryOptions(new RequestRetryOptions(RetryPolicyType.FIXED, 2,
            60, 100L, 1000L, null))
            .buildAsyncClient();

        StepVerifier.create(client.getProperties())
            .expectError();
    }

    @Test
    public void getAccountInfoAnonymous() {
        StepVerifier.create(anonymousClient.getAccountInfo())
            .verifyError(IllegalStateException.class);
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
        String endpoint = BlobUrlParts.parse(primaryBlobServiceAsyncClient.getAccountUrl()).setScheme("http").toUrl()
            .toString();
        BlobServiceClientBuilder builder = new BlobServiceClientBuilder()
            .customerProvidedKey(new CustomerProvidedKey(Base64.getEncoder()
                .encodeToString(getRandomByteArray(256)))).endpoint(endpoint);

        assertThrows(IllegalArgumentException.class, builder::buildAsyncClient);
    }

    @Test
    public void builderBearerTokenValidation() {
        String endpoint = BlobUrlParts.parse(primaryBlobServiceAsyncClient.getAccountUrl()).setScheme("http").toUrl()
            .toString();
        BlobServiceClientBuilder builder = new BlobServiceClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint);

        assertThrows(IllegalArgumentException.class, builder::buildAsyncClient);
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @Test
    public void restoreContainerAsync() {
        BlobContainerAsyncClient cc1 =
            primaryBlobServiceAsyncClient.getBlobContainerAsyncClient(generateContainerName());
        String blobName = generateBlobName();
        long delay = ENVIRONMENT.getTestMode() == TestMode.PLAYBACK ? 0L : 30000L;

        Mono<BlobContainerItem> blobContainerItemMono = cc1.create()
            .then(cc1.getBlobAsyncClient(blobName).upload(DATA.getDefaultFlux(), new ParallelTransferOptions()))
            .then(cc1.delete())
            .then(Mono.delay(Duration.ofMillis(delay)))
            .then(primaryBlobServiceAsyncClient.listBlobContainers(
                new ListBlobContainersOptions()
                    .setPrefix(cc1.getBlobContainerName())
                    .setDetails(new BlobContainerListDetails().setRetrieveDeleted(true))
            ).next());

        Mono<BlobContainerAsyncClient> restoredContainerClientMono = blobContainerItemMono.flatMap(blobContainerItem ->
            primaryBlobServiceAsyncClient.undeleteBlobContainer(blobContainerItem.getName(),
                blobContainerItem.getVersion()));

        StepVerifier.create(restoredContainerClientMono.flatMap(restoredContainerClient ->
                restoredContainerClient.listBlobs().collectList()))
            .assertNext(it -> {
                assertEquals(1, it.size());
                assertEquals(blobName, it.get(0).getName());
            }).verifyComplete();
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @PlaybackOnly
    @Test
    public void restoreContainerIntoOtherContainer() {
        BlobContainerAsyncClient cc1 = primaryBlobServiceAsyncClient.getBlobContainerAsyncClient(generateContainerName());
        cc1.create().block();
        String blobName = generateBlobName();
        cc1.getBlobAsyncClient(blobName).upload(DATA.getDefaultFlux(), null).block();
        cc1.delete().block();
        BlobContainerItem blobContainerItem = primaryBlobServiceAsyncClient.listBlobContainers(
            new ListBlobContainersOptions()
                .setPrefix(cc1.getBlobContainerName())
                .setDetails(new BlobContainerListDetails().setRetrieveDeleted(true))).blockFirst();

        sleepIfRunningAgainstService(30000);

        StepVerifier.create(primaryBlobServiceAsyncClient.undeleteBlobContainerWithResponse(
                new UndeleteBlobContainerOptions(blobContainerItem.getName(), blobContainerItem.getVersion()))
            .flatMap(r -> r.getValue().listBlobs().collectList()))
            .assertNext(r -> {
                assertEquals(1, r.size());
                assertEquals(blobName, r.get(0).getName());
            })
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @Test
    public void restoreContainerAsyncWithResponse() {
        BlobContainerAsyncClient cc1 = primaryBlobServiceAsyncClient.getBlobContainerAsyncClient(generateContainerName());
        String blobName = generateBlobName();
        long delay = ENVIRONMENT.getTestMode() == TestMode.PLAYBACK ? 0L : 30000L;

        Mono<BlobContainerItem> blobContainerItemMono = cc1.create()
            .then(cc1.getBlobAsyncClient(blobName).upload(DATA.getDefaultFlux(), new ParallelTransferOptions()))
            .then(cc1.delete())
            .then(Mono.delay(Duration.ofMillis(delay)))
            .then(primaryBlobServiceAsyncClient.listBlobContainers(
                new ListBlobContainersOptions()
                    .setPrefix(cc1.getBlobContainerName())
                    .setDetails(new BlobContainerListDetails().setRetrieveDeleted(true))
            ).next());

        Mono<Response<BlobContainerAsyncClient>> responseMono = blobContainerItemMono.flatMap(blobContainerItem ->
            primaryBlobServiceAsyncClient.undeleteBlobContainerWithResponse(
                new UndeleteBlobContainerOptions(blobContainerItem.getName(), blobContainerItem.getVersion())));

        StepVerifier.create(responseMono).assertNext(it -> {
            assertNotNull(it);
            assertEquals(201, it.getStatusCode());
            assertNotNull(it.getValue());
            assertEquals(cc1.getBlobContainerName(), it.getValue().getBlobContainerName());
        }).verifyComplete();
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @Test
    public void restoreContainerError() {
        StepVerifier.create(primaryBlobServiceAsyncClient.undeleteBlobContainer(generateContainerName(),
            "01D60F8BB59A4652"))
            .verifyError(BlobStorageException.class);
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @Test
    public void restoreContainerIntoExistingContainerError() {
        BlobContainerAsyncClient cc1 = primaryBlobServiceAsyncClient.getBlobContainerAsyncClient(generateContainerName());
        cc1.create().block();
        String blobName = generateBlobName();
        cc1.getBlobAsyncClient(blobName).upload(DATA.getDefaultFlux(), null).block();
        cc1.delete().block();
        BlobContainerItem blobContainerItem = primaryBlobServiceAsyncClient.listBlobContainers(
            new ListBlobContainersOptions()
                .setPrefix(cc1.getBlobContainerName())
                .setDetails(new BlobContainerListDetails().setRetrieveDeleted(true))).blockFirst();

        sleepIfRunningAgainstService(30000);

        BlobContainerAsyncClient cc2 = primaryBlobServiceAsyncClient.createBlobContainer(generateContainerName()).block();
        StepVerifier.create(primaryBlobServiceAsyncClient.undeleteBlobContainerWithResponse(
            new UndeleteBlobContainerOptions(blobContainerItem.getName(), blobContainerItem.getVersion())
            .setDestinationContainerName(cc2.getBlobContainerName())))
            .verifyError(BlobStorageException.class);
    }

    @Test
    public void oAuthOnSecondary() {
        BlobServiceClientBuilder secondaryBuilder = getServiceClientBuilder(null,
            ENVIRONMENT.getPrimaryAccount().getBlobEndpointSecondary());
        BlobServiceAsyncClient secondaryClient = secondaryBuilder
            .credential(StorageCommonTestUtils.getTokenCredential(interceptorManager)).buildAsyncClient();

        StepVerifier.create(secondaryClient.getProperties())
            .expectNextCount(1)
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("sasTokenDoesNotShowUpOnInvalidUriSupplier")
    public void sasTokenDoesNotShowUpOnInvalidUri(String service, String container) {
        /* random sas token. this does not actually authenticate anything. */
        String mockSas =
            "?sv=2019-10-10&ss=b&srt=sco&sp=r&se=2019-06-04T12:04:58Z&st=2090-05-04T04:04:58Z&spr=http&sig=doesntmatter";

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> {
            BlobServiceAsyncClient client = new BlobServiceClientBuilder().endpoint(service).sasToken(mockSas).buildAsyncClient();
            client.getBlobContainerAsyncClient(container).getBlobAsyncClient("blobname");
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
        BlobServiceAsyncClient sc = getServiceClientBuilder(ENVIRONMENT.getPrimaryAccount().getCredential(),
            primaryBlobServiceAsyncClient.getAccountUrl())
            .addPolicy(getPerCallVersionPolicy())
            .buildAsyncClient();

        StepVerifier.create(sc.getPropertiesWithResponse())
            .assertNext(r -> assertEquals("2017-11-09", r.getHeaders().getValue(X_MS_VERSION)))
            .verifyComplete();
    }

    @Test
    public void createContainerIfNotExists() {
        String containerName = generateContainerName();
        assertAsyncResponseStatusCode(primaryBlobServiceAsyncClient
            .createBlobContainerIfNotExistsWithResponse(containerName, null), 201);
        assertAsyncResponseStatusCode(primaryBlobServiceAsyncClient
            .createBlobContainerIfNotExistsWithResponse(containerName, null), 409);
    }

    @Test
    public void deleteContainerIfExists() {
        String containerName = generateContainerName();
        primaryBlobServiceAsyncClient.createBlobContainer(containerName).block();

        StepVerifier.create(primaryBlobServiceAsyncClient.deleteBlobContainerIfExistsWithResponse(containerName))
            .assertNext(r -> {
                assertTrue(r.getValue());
                assertResponseStatusCode(r, 202);
            })
            .verifyComplete();
    }

    @Test
    public void deleteContainerIfExistsMin() {
        String containerName = generateContainerName();
        primaryBlobServiceAsyncClient.createBlobContainer(containerName).block();

        StepVerifier.create(primaryBlobServiceAsyncClient.deleteBlobContainerIfExists(containerName))
            .expectNext(true)
            .verifyComplete();
    }

    @Test
    public void deleteContainerIfExistsContainerDoesNotExist() {
        StepVerifier.create(primaryBlobServiceAsyncClient.deleteBlobContainerIfExists(generateContainerName()))
            .expectNext(false)
            .verifyComplete();
    }

    // We can't guarantee that the requests will always happen before the container is garbage collected
    @PlaybackOnly
    @Test
    public void deleteContainerIfExistsAlreadyDeleted() {
        String containerName = generateContainerName();
        primaryBlobServiceAsyncClient.createBlobContainer(containerName).block();

        assertAsyncResponseStatusCode(primaryBlobServiceAsyncClient.deleteBlobContainerIfExistsWithResponse(
            containerName), 202);
        // Confirming the behavior of the api when the container is in the deleting state.
        // After delete has been called once but before it has been garbage collected
        assertAsyncResponseStatusCode(primaryBlobServiceAsyncClient.deleteBlobContainerIfExistsWithResponse(
            containerName), 202);
    }

    @LiveOnly
    @Test
    public void serviceTimeoutPolicy() {
        BlobServiceAsyncClient serviceClient = new BlobServiceClientBuilder()
            .endpoint(ENVIRONMENT.getPrimaryAccount().getBlobEndpoint())
            .credential(ENVIRONMENT.getPrimaryAccount().getCredential())
            .addPolicy(new ServiceTimeoutPolicy(Duration.ofSeconds(1)))
            .buildAsyncClient();

        BlobContainerAsyncClient blobContainerClient = serviceClient.getBlobContainerAsyncClient(generateContainerName());
        blobContainerClient.createIfNotExists().block();
        BlobAsyncClient blobClient = blobContainerClient.getBlobAsyncClient(generateBlobName());

        // testing with large dataset that is guaranteed to take longer than the specified timeout (1 second)
        byte[] randomData = getRandomByteArray(256 * Constants.MB);
        ByteArrayInputStream input = new ByteArrayInputStream(randomData);

        StepVerifier.create(blobClient.uploadWithResponse(new BlobParallelUploadOptions(input)))
            .verifyErrorSatisfies(r -> {
                BlobStorageException e = assertInstanceOf(BlobStorageException.class, r);
                assertEquals(BlobErrorCode.OPERATION_TIMED_OUT, e.getErrorCode());
            });
    }

    @Test
    public void defaultAudience() {
        BlobServiceAsyncClient aadService = getServiceClientBuilderWithTokenCredential(ccAsync.getBlobContainerUrl())
            .audience(null)
            .buildAsyncClient();

        StepVerifier.create(aadService.getProperties())
            .assertNext(r -> assertNotNull(r))
            .verifyComplete();
    }

    @Test
    public void storageAccountAudience() {
        BlobServiceAsyncClient aadService = getServiceClientBuilderWithTokenCredential(ccAsync.getBlobContainerUrl())
            .audience(BlobAudience.createBlobServiceAccountAudience(ccAsync.getAccountName()))
            .buildAsyncClient();

        StepVerifier.create(aadService.getProperties())
            .assertNext(r -> assertNotNull(r))
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2024-08-04")
    @LiveOnly
    @Test
    /* This test tests if the bearer challenge is working properly. A bad audience is passed in, the service returns
    the default audience, and the request gets retried with this default audience, making the call function as expected.
     */
    public void audienceErrorBearerChallengeRetry() {
        BlobServiceAsyncClient aadService = getServiceClientBuilderWithTokenCredential(ccAsync.getBlobContainerUrl())
                .audience(BlobAudience.createBlobServiceAccountAudience("badAudience"))
                .buildAsyncClient();

        StepVerifier.create(aadService.getProperties())
            .assertNext(r -> assertNotNull(r))
            .verifyComplete();
    }

    @Test
    public void audienceFromString() {
        String url = String.format("https://%s.blob.core.windows.net/", ccAsync.getAccountName());
        BlobAudience audience = BlobAudience.fromString(url);

        BlobServiceAsyncClient aadService = getServiceClientBuilderWithTokenCredential(ccAsync.getBlobContainerUrl())
            .audience(audience)
            .buildAsyncClient();

        StepVerifier.create(aadService.getProperties())
            .assertNext(r -> assertNotNull(r))
            .verifyComplete();        }
}
