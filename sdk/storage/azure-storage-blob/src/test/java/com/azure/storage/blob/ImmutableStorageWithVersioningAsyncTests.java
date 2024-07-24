// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.BearerTokenAuthenticationPolicy;
import com.azure.core.test.TestMode;
import com.azure.core.util.polling.PollerFlux;
import com.azure.identity.EnvironmentCredentialBuilder;
import com.azure.json.JsonProviders;
import com.azure.json.JsonWriter;
import com.azure.storage.blob.models.BlobContainerProperties;
import com.azure.storage.blob.models.BlobCopyInfo;
import com.azure.storage.blob.models.BlobErrorCode;
import com.azure.storage.blob.models.BlobImmutabilityPolicy;
import com.azure.storage.blob.models.BlobImmutabilityPolicyMode;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlobItemProperties;
import com.azure.storage.blob.models.BlobListDetails;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.models.LeaseStateType;
import com.azure.storage.blob.models.ListBlobContainersOptions;
import com.azure.storage.blob.models.ListBlobsOptions;
import com.azure.storage.blob.models.ParallelTransferOptions;
import com.azure.storage.blob.models.PublicAccessType;
import com.azure.storage.blob.options.AppendBlobCreateOptions;
import com.azure.storage.blob.options.BlobBeginCopyOptions;
import com.azure.storage.blob.options.BlobBreakLeaseOptions;
import com.azure.storage.blob.options.BlobCopyFromUrlOptions;
import com.azure.storage.blob.options.BlobParallelUploadOptions;
import com.azure.storage.blob.options.BlockBlobCommitBlockListOptions;
import com.azure.storage.blob.options.BlockBlobSimpleUploadOptions;
import com.azure.storage.blob.options.PageBlobCreateOptions;
import com.azure.storage.blob.sas.BlobContainerSasPermission;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.azure.storage.blob.specialized.AppendBlobAsyncClient;
import com.azure.storage.blob.specialized.BlockBlobAsyncClient;
import com.azure.storage.blob.specialized.PageBlobAsyncClient;
import com.azure.storage.common.sas.AccountSasPermission;
import com.azure.storage.common.sas.AccountSasResourceType;
import com.azure.storage.common.sas.AccountSasService;
import com.azure.storage.common.sas.AccountSasSignatureValues;
import com.azure.storage.common.test.shared.extensions.LiveOnly;
import com.azure.storage.common.test.shared.extensions.RequiredServiceVersion;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ImmutableStorageWithVersioningAsyncTests extends BlobTestBase {
    private static String vlwContainerName;
    private static final String ACCOUNT_NAME = ENVIRONMENT.getVersionedAccount().getName();
    private static final String RESOURCE_GROUP_NAME = ENVIRONMENT.getResourceGroupName();
    private static final String SUBSCRIPTION_ID = ENVIRONMENT.getSubscriptionId();
    private static final String API_VERSION = "2021-04-01";
    private static final TokenCredential CREDENTIAL = new EnvironmentCredentialBuilder().build();
    private static final BearerTokenAuthenticationPolicy CREDENTIAL_POLICY =
        new BearerTokenAuthenticationPolicy(CREDENTIAL, "https://management.azure.com/.default");
    private BlobContainerAsyncClient vlwContainer;
    private BlobAsyncClient vlwBlob;

    @BeforeAll
    public static void setupSpec() throws IOException {
        if (ENVIRONMENT.getTestMode() != TestMode.PLAYBACK) {
            vlwContainerName = UUID.randomUUID().toString();

            String url = String.format("https://management.azure.com/subscriptions/%s/resourceGroups/%s/providers/"
                    + "Microsoft.Storage/storageAccounts/%s/blobServices/default/containers/%s?api-version=%s",
                SUBSCRIPTION_ID, RESOURCE_GROUP_NAME, ACCOUNT_NAME, vlwContainerName, API_VERSION);
            HttpPipeline httpPipeline = new HttpPipelineBuilder()
                .policies(CREDENTIAL_POLICY)
                .build();

            ImmutableStorageWithVersioningTests.ImmutableStorageWithVersioning immutableStorageWithVersioning =
                new ImmutableStorageWithVersioningTests.ImmutableStorageWithVersioning();
            immutableStorageWithVersioning.setEnabled(true);
            ImmutableStorageWithVersioningTests.Properties properties = new ImmutableStorageWithVersioningTests.Properties();
            properties.setImmutableStorageWithVersioning(immutableStorageWithVersioning);
            ImmutableStorageWithVersioningTests.Body body = new ImmutableStorageWithVersioningTests.Body();
            body.setId(String.format("/subscriptions/%s/resourceGroups/%s/providers/Microsoft.Storage/storageAccounts/"
                    + "%s/blobServices/default/containers/%s", SUBSCRIPTION_ID, RESOURCE_GROUP_NAME, ACCOUNT_NAME,
                vlwContainerName));
            body.setName(vlwContainerName);
            body.setType("Microsoft.Storage/storageAccounts/blobServices/containers");
            body.setProperties(properties);

            ByteArrayOutputStream json = new ByteArrayOutputStream();
            try (JsonWriter jsonWriter = JsonProviders.createWriter(json)) {
                body.toJson(jsonWriter);
            }

            HttpResponse response = httpPipeline.send(new HttpRequest(HttpMethod.PUT, new URL(url), new HttpHeaders(),
                Flux.just(ByteBuffer.wrap(json.toByteArray())))).block();
            assertNotNull(response);
            if (response.getStatusCode() != 201) {
                LOGGER.warning(response.getBodyAsString().block());
            }
            assertEquals(201, response.getStatusCode());
        }
    }

    @BeforeEach
    public void setup() {
        vlwContainer = versionedBlobServiceAsyncClient.getBlobContainerAsyncClient(
            testResourceNamer.recordValueFromConfig(vlwContainerName));
        vlwBlob = vlwContainer.getBlobAsyncClient(generateBlobName());
        vlwBlob.upload(Flux.just(ByteBuffer.wrap(new byte[0])), null).block();
    }

    @AfterAll
    public static void cleanupSpec() throws MalformedURLException {
        if (ENVIRONMENT.getTestMode() != TestMode.PLAYBACK) {
            HttpPipeline httpPipeline = new HttpPipelineBuilder()
                .policies(CREDENTIAL_POLICY)
                .build();
            BlobServiceClient cleanupClient = new BlobServiceClientBuilder()
                .credential(ENVIRONMENT.getVersionedAccount().getCredential())
                .endpoint(ENVIRONMENT.getVersionedAccount().getBlobEndpoint())
                .buildClient();

            BlobContainerClient containerClient = cleanupClient.getBlobContainerClient(vlwContainerName);
            BlobContainerProperties containerProperties = containerClient.getProperties();

            if (containerProperties.getLeaseState() == LeaseStateType.LEASED) {
                createLeaseClient(containerClient).breakLeaseWithResponse(new BlobBreakLeaseOptions()
                    .setBreakPeriod(Duration.ofSeconds(0)), null, null);
            }
            if (containerProperties.isImmutableStorageWithVersioningEnabled()) {
                ListBlobsOptions options = new ListBlobsOptions().setDetails(new BlobListDetails()
                    .setRetrieveImmutabilityPolicy(true).setRetrieveLegalHold(true));
                for (BlobItem blob: containerClient.listBlobs(options, null)) {
                    BlobClient blobClient = containerClient.getBlobClient(blob.getName());
                    BlobItemProperties blobProperties = blob.getProperties();
                    if (Objects.equals(true, blobProperties.hasLegalHold())) {
                        blobClient.setLegalHold(false);
                    }
                    if (blobProperties.getImmutabilityPolicy().getPolicyMode() != null) {
                        blobClient.deleteImmutabilityPolicy();
                    }
                    blobClient.delete();
                }
            }

            String url = String.format("https://management.azure.com/subscriptions/%s/resourceGroups/%s/providers/"
                    + "Microsoft.Storage/storageAccounts/%s/blobServices/default/containers/%s?api-version=%s",
                SUBSCRIPTION_ID, RESOURCE_GROUP_NAME, ACCOUNT_NAME, vlwContainerName, API_VERSION);
            HttpResponse response = httpPipeline.send(new HttpRequest(HttpMethod.DELETE, new URL(url),
                new HttpHeaders(), Flux.empty())).block();
            assertNotNull(response);
            if (response.getStatusCode() != 200) {
                LOGGER.warning(response.getBodyAsString().block());
            }
            assertEquals(200, response.getStatusCode());
        }
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2020-10-02")
    @Test
    public void setImmutabilityPolicyMin() {
        OffsetDateTime expiryTime = testResourceNamer.now().plusDays(2);
        BlobImmutabilityPolicy immutabilityPolicy = new BlobImmutabilityPolicy()
            .setExpiryTime(expiryTime)
            .setPolicyMode(BlobImmutabilityPolicyMode.UNLOCKED);

        // The service rounds Immutability Policy Expiry to the nearest second.
        OffsetDateTime expectedImmutabilityPolicyExpiry = expiryTime.truncatedTo(ChronoUnit.SECONDS);

        StepVerifier.create(vlwBlob.setImmutabilityPolicy(immutabilityPolicy))
            .assertNext(r -> {
                assertEquals(expectedImmutabilityPolicyExpiry, r.getExpiryTime());
                assertEquals(BlobImmutabilityPolicyMode.UNLOCKED, r.getPolicyMode());
            })
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2020-10-02")
    @Test
    public void setImmutabilityPolicy() {
        BlobImmutabilityPolicyMode policyMode = BlobImmutabilityPolicyMode.UNLOCKED;
        OffsetDateTime expiryTime = testResourceNamer.now().plusDays(2);
        BlobImmutabilityPolicy immutabilityPolicy = new BlobImmutabilityPolicy()
            .setExpiryTime(expiryTime)
            .setPolicyMode(policyMode);

        // The service rounds Immutability Policy Expiry to the nearest second.
        OffsetDateTime expectedImmutabilityPolicyExpiry = expiryTime.truncatedTo(ChronoUnit.SECONDS);

        // when: "set immutability policy"
        StepVerifier.create(vlwBlob.setImmutabilityPolicyWithResponse(immutabilityPolicy, null))
            .assertNext(r -> {
                assertEquals(expectedImmutabilityPolicyExpiry, r.getValue().getExpiryTime());
                assertEquals(policyMode, r.getValue().getPolicyMode());
            })
            .verifyComplete();

        // when: "get properties"
        StepVerifier.create(vlwBlob.getProperties())
            .assertNext(r -> {
                assertEquals(expectedImmutabilityPolicyExpiry, r.getImmutabilityPolicy().getExpiryTime());
                assertEquals(policyMode, r.getImmutabilityPolicy().getPolicyMode());
            })
            .verifyComplete();

        // when: "list blob"
        ListBlobsOptions options = new ListBlobsOptions().setPrefix(vlwBlob.getBlobName()).setDetails(
            new BlobListDetails().setRetrieveImmutabilityPolicy(true).setRetrieveLegalHold(true));
        StepVerifier.create(vlwContainer.listBlobs(options, null))
            .assertNext(r -> {
                assertEquals(vlwBlob.getBlobName(), r.getName());
                assertEquals(expectedImmutabilityPolicyExpiry, r.getProperties().getImmutabilityPolicy().getExpiryTime());
                assertEquals(policyMode, r.getProperties().getImmutabilityPolicy().getPolicyMode());
            })
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2020-10-02")
    @Test
    public void setImmutabilityPolicyAC() {
        List<OffsetDateTime> unmodifiedDates = Arrays.asList(null, NEW_DATE);

        for (OffsetDateTime unmodified : unmodifiedDates) {
            BlobRequestConditions bac = new BlobRequestConditions()
                .setIfUnmodifiedSince(unmodified);
            OffsetDateTime expiryTime = testResourceNamer.now().plusDays(2);
            BlobImmutabilityPolicy immutabilityPolicy = new BlobImmutabilityPolicy()
                .setExpiryTime(expiryTime)
                .setPolicyMode(BlobImmutabilityPolicyMode.UNLOCKED);

            assertAsyncResponseStatusCode(vlwBlob.setImmutabilityPolicyWithResponse(immutabilityPolicy,
                bac), 200);
        }
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2020-10-02")
    @Test
    public void setImmutabilityPolicyACFail() {
        BlobRequestConditions bac = new BlobRequestConditions()
            .setIfUnmodifiedSince(OLD_DATE);
        OffsetDateTime expiryTime = testResourceNamer.now().plusDays(2);
        BlobImmutabilityPolicy immutabilityPolicy = new BlobImmutabilityPolicy()
            .setExpiryTime(expiryTime)
            .setPolicyMode(BlobImmutabilityPolicyMode.UNLOCKED);

        StepVerifier.create(vlwBlob.setImmutabilityPolicyWithResponse(immutabilityPolicy, bac))
            .verifyErrorSatisfies(r -> {
                BlobStorageException e = assertInstanceOf(BlobStorageException.class, r);
                assertEquals(BlobErrorCode.CONDITION_NOT_MET, e.getErrorCode());
            });
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2020-10-02")
    @ParameterizedTest
    @MethodSource("setImmutabilityPolicyACIASupplier")
    public void setImmutabilityPolicyACIA(String leaseId, String tags, String ifMatch, String ifNoneMatch,
                                          OffsetDateTime ifModifiedSince, String wrongCondition) {
        BlobRequestConditions bac = new BlobRequestConditions()
            .setLeaseId(leaseId)
            .setTagsConditions(tags)
            .setIfMatch(ifMatch)
            .setIfNoneMatch(ifNoneMatch)
            .setIfModifiedSince(ifModifiedSince);
        OffsetDateTime expiryTime = testResourceNamer.now().plusDays(2);
        BlobImmutabilityPolicy immutabilityPolicy = new BlobImmutabilityPolicy()
            .setExpiryTime(expiryTime)
            .setPolicyMode(BlobImmutabilityPolicyMode.UNLOCKED);

        StepVerifier.create(vlwBlob.setImmutabilityPolicyWithResponse(immutabilityPolicy, bac))
            .verifyErrorSatisfies(r -> {
                IllegalArgumentException e = assertInstanceOf(IllegalArgumentException.class, r);
                assertEquals(String.format("%s does not support the %s request condition(s) for parameter 'requestConditions'.",
                    "setImmutabilityPolicy(WithResponse)", wrongCondition), e.getMessage());
            });
    }

    private static Stream<Arguments> setImmutabilityPolicyACIASupplier() {
        return Stream.of(
            Arguments.of("leaseId", null, null, null, null, "LeaseId"),
            Arguments.of(null, "tagsConditions", null, null, null, "TagsConditions"),
            Arguments.of(null, null, "ifMatch", null, null, "IfMatch"),
            Arguments.of(null, null, null, "ifNoneMatch", null, "IfNoneMatch"),
            Arguments.of(null, null, null, null, OLD_DATE, "IfModifiedSince"),
            Arguments.of("leaseId", "tagsConditions", "ifMatch", "ifNoneMatch", OLD_DATE, "LeaseId, TagsConditions, "
                + "IfModifiedSince, IfMatch, IfNoneMatch")
        );
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2020-10-02")
    @Test
    public void setImmutabilityPolicyError() {
        BlobAsyncClient blob = vlwContainer.getBlobAsyncClient(generateBlobName());
        OffsetDateTime expiryTime = testResourceNamer.now().plusDays(2);
        BlobImmutabilityPolicy immutabilityPolicy = new BlobImmutabilityPolicy()
            .setExpiryTime(expiryTime)
            .setPolicyMode(BlobImmutabilityPolicyMode.UNLOCKED);

        StepVerifier.create(blob.setImmutabilityPolicyWithResponse(immutabilityPolicy, null))
            .verifyErrorSatisfies(r -> {
                BlobStorageException e = assertInstanceOf(BlobStorageException.class, r);
                assertEquals(BlobErrorCode.BLOB_NOT_FOUND, e.getErrorCode());
            });
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2020-10-02")
    @Test
    public void setImmutabilityPolicyIA() {
        OffsetDateTime expiryTime = testResourceNamer.now().plusDays(2);
        BlobImmutabilityPolicy immutabilityPolicy = new BlobImmutabilityPolicy()
            .setExpiryTime(expiryTime)
            .setPolicyMode(BlobImmutabilityPolicyMode.MUTABLE);

        StepVerifier.create(vlwBlob.setImmutabilityPolicyWithResponse(immutabilityPolicy, null))
            .verifyErrorSatisfies(r -> {
                IllegalArgumentException e = assertInstanceOf(IllegalArgumentException.class, r);
                assertEquals("immutabilityPolicy.policyMode must be Locked or Unlocked", e.getMessage());
            });
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2020-10-02")
    @Test
    public void deleteImmutabilityPolicyMin() {
        OffsetDateTime expiryTime = testResourceNamer.now().plusDays(2);
        BlobImmutabilityPolicy immutabilityPolicy = new BlobImmutabilityPolicy()
            .setExpiryTime(expiryTime)
            .setPolicyMode(BlobImmutabilityPolicyMode.UNLOCKED);
        vlwBlob.setImmutabilityPolicy(immutabilityPolicy).block();

        vlwBlob.deleteImmutabilityPolicy().block();

        StepVerifier.create(vlwBlob.getProperties())
            .assertNext(r -> {
                assertNull(r.getImmutabilityPolicy().getPolicyMode());
                assertNull(r.getImmutabilityPolicy().getExpiryTime());
            })
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2020-10-02")
    @Test
    public void deleteImmutabilityPolicy() {
        OffsetDateTime expiryTime = testResourceNamer.now().plusDays(2);
        BlobImmutabilityPolicy immutabilityPolicy = new BlobImmutabilityPolicy()
            .setExpiryTime(expiryTime)
            .setPolicyMode(BlobImmutabilityPolicyMode.UNLOCKED);
        vlwBlob.setImmutabilityPolicy(immutabilityPolicy).block();

        vlwBlob.deleteImmutabilityPolicyWithResponse().block();

        StepVerifier.create(vlwBlob.getProperties())
            .assertNext(r -> {
                assertNull(r.getImmutabilityPolicy().getPolicyMode());
                assertNull(r.getImmutabilityPolicy().getExpiryTime());
            })
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2020-10-02")
    @Test
    public void deleteImmutabilityPolicyError() {
        BlobAsyncClient blobClient = vlwContainer.getBlobAsyncClient(generateBlobName());
        StepVerifier.create(blobClient.deleteImmutabilityPolicyWithResponse())
            .verifyErrorSatisfies(r -> {
                BlobStorageException e = assertInstanceOf(BlobStorageException.class, r);
                assertEquals(BlobErrorCode.BLOB_NOT_FOUND, e.getErrorCode());
            });
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2020-10-02")
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void setLegalHoldMin(boolean legalHold) {
        StepVerifier.create(vlwBlob.setLegalHold(legalHold))
            .assertNext(r -> assertEquals(legalHold, r.hasLegalHold()))
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2020-10-02")
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void setLegalHold(boolean legalHold) {
        // when: "set legal hold"
        StepVerifier.create(vlwBlob.setLegalHoldWithResponse(legalHold))
            .assertNext(r -> assertEquals(legalHold, r.getValue().hasLegalHold()))
            .verifyComplete();

        // when: "get properties"
        StepVerifier.create(vlwBlob.getProperties())
            .assertNext(r -> assertEquals(legalHold, r.hasLegalHold()))
            .verifyComplete();

        // when: "list blob"
        ListBlobsOptions options = new ListBlobsOptions().setPrefix(vlwBlob.getBlobName())
            .setDetails(new BlobListDetails().setRetrieveImmutabilityPolicy(true).setRetrieveLegalHold(true));
        StepVerifier.create(vlwContainer.listBlobs(options, null))
            .assertNext(r -> {
                assertEquals(vlwBlob.getBlobName(), r.getName());
                assertEquals(legalHold, r.getProperties().hasLegalHold());
            })
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2020-10-02")
    @Test
    public void setLegalHoldError() {
        BlobAsyncClient blob = vlwContainer.getBlobAsyncClient(generateBlobName());

        StepVerifier.create(blob.setLegalHoldWithResponse(false))
            .verifyErrorSatisfies(r -> {
                BlobStorageException e = assertInstanceOf(BlobStorageException.class, r);
                assertEquals(BlobErrorCode.BLOB_NOT_FOUND, e.getErrorCode());
            });
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2020-10-02")
    @Test
    public void containerProperties() {
        StepVerifier.create(vlwContainer.getProperties())
            .assertNext(r -> assertTrue(r.isImmutableStorageWithVersioningEnabled()))
            .verifyComplete();

        StepVerifier.create(vlwContainer.getServiceAsyncClient().listBlobContainers(
            new ListBlobContainersOptions().setPrefix(vlwContainer.getBlobContainerName())))
            .assertNext(r -> assertTrue(r.getProperties().isImmutableStorageWithVersioningEnabled()))
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2020-10-02")
    @Test
    public void appendBlobCreate() {
        AppendBlobAsyncClient appendBlob = vlwContainer.getBlobAsyncClient(generateBlobName()).getAppendBlobAsyncClient();
        OffsetDateTime expiryTime = testResourceNamer.now().plusDays(2);
        // The service rounds Immutability Policy Expiry to the nearest second.
        OffsetDateTime expectedImmutabilityPolicyExpiry = expiryTime.truncatedTo(ChronoUnit.SECONDS);
        BlobImmutabilityPolicy immutabilityPolicy = new BlobImmutabilityPolicy()
            .setExpiryTime(expiryTime)
            .setPolicyMode(BlobImmutabilityPolicyMode.UNLOCKED);

        appendBlob.createWithResponse(new AppendBlobCreateOptions()
            .setImmutabilityPolicy(immutabilityPolicy)
            .setLegalHold(true)).block();

        StepVerifier.create(appendBlob.getProperties())
            .assertNext(r -> {
                assertEquals(expectedImmutabilityPolicyExpiry, r.getImmutabilityPolicy().getExpiryTime());
                assertEquals(BlobImmutabilityPolicyMode.UNLOCKED, r.getImmutabilityPolicy().getPolicyMode());
                assertTrue(r.hasLegalHold());
            })
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2020-10-02")
    @Test
    public void pageBlobCreate() {
        PageBlobAsyncClient pageBlob = vlwContainer.getBlobAsyncClient(generateBlobName()).getPageBlobAsyncClient();
        OffsetDateTime expiryTime = testResourceNamer.now().plusDays(2);
        // The service rounds Immutability Policy Expiry to the nearest second.
        OffsetDateTime expectedImmutabilityPolicyExpiry = expiryTime.truncatedTo(ChronoUnit.SECONDS);
        BlobImmutabilityPolicy immutabilityPolicy = new BlobImmutabilityPolicy()
            .setExpiryTime(expiryTime)
            .setPolicyMode(BlobImmutabilityPolicyMode.UNLOCKED);

        pageBlob.createWithResponse(new PageBlobCreateOptions(512)
            .setImmutabilityPolicy(immutabilityPolicy)
            .setLegalHold(true)).block();

        StepVerifier.create(pageBlob.getProperties())
            .assertNext(r -> {
                assertEquals(expectedImmutabilityPolicyExpiry, r.getImmutabilityPolicy().getExpiryTime());
                assertEquals(BlobImmutabilityPolicyMode.UNLOCKED, r.getImmutabilityPolicy().getPolicyMode());
                assertTrue(r.hasLegalHold());
            })
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2020-10-02")
    @Test
    public void blockBlobCommitBlockList() {
        BlockBlobAsyncClient blockBlob = vlwBlob.getBlockBlobAsyncClient();
        OffsetDateTime expiryTime = testResourceNamer.now().plusDays(2);
        // The service rounds Immutability Policy Expiry to the nearest second.
        OffsetDateTime expectedImmutabilityPolicyExpiry = expiryTime.truncatedTo(ChronoUnit.SECONDS);
        BlobImmutabilityPolicy immutabilityPolicy = new BlobImmutabilityPolicy()
            .setExpiryTime(expiryTime)
            .setPolicyMode(BlobImmutabilityPolicyMode.UNLOCKED);

        blockBlob.commitBlockListWithResponse(new BlockBlobCommitBlockListOptions(new ArrayList<>())
            .setImmutabilityPolicy(immutabilityPolicy)
            .setLegalHold(true)).block();

        StepVerifier.create(blockBlob.getProperties())
            .assertNext(r -> {
                assertEquals(expectedImmutabilityPolicyExpiry, r.getImmutabilityPolicy().getExpiryTime());
                assertEquals(BlobImmutabilityPolicyMode.UNLOCKED, r.getImmutabilityPolicy().getPolicyMode());
                assertTrue(r.hasLegalHold());
            })
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2020-10-02")
    @Test
    public void blockBlobUpload() {
        BlockBlobAsyncClient blockBlob = vlwBlob.getBlockBlobAsyncClient();
        OffsetDateTime expiryTime = testResourceNamer.now().plusDays(2);
        // The service rounds Immutability Policy Expiry to the nearest second.
        OffsetDateTime expectedImmutabilityPolicyExpiry = expiryTime.truncatedTo(ChronoUnit.SECONDS);
        BlobImmutabilityPolicy immutabilityPolicy = new BlobImmutabilityPolicy()
            .setExpiryTime(expiryTime)
            .setPolicyMode(BlobImmutabilityPolicyMode.UNLOCKED);

        blockBlob.uploadWithResponse(new BlockBlobSimpleUploadOptions(DATA.getDefaultFlux(), DATA.getDefaultDataSize())
            .setImmutabilityPolicy(immutabilityPolicy)
            .setLegalHold(true)).block();

        StepVerifier.create(blockBlob.getProperties())
            .assertNext(r -> {
                assertEquals(expectedImmutabilityPolicyExpiry, r.getImmutabilityPolicy().getExpiryTime());
                assertEquals(BlobImmutabilityPolicyMode.UNLOCKED, r.getImmutabilityPolicy().getPolicyMode());
                assertTrue(r.hasLegalHold());
            })
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2020-10-02")
    @LiveOnly
    @ParameterizedTest
    @MethodSource("blobUploadSupplier")
    public void blobUpload(Long blockSize) {
        OffsetDateTime expiryTime = testResourceNamer.now().plusDays(2);
        // The service rounds Immutability Policy Expiry to the nearest second.
        OffsetDateTime expectedImmutabilityPolicyExpiry = expiryTime.truncatedTo(ChronoUnit.SECONDS);
        BlobImmutabilityPolicy immutabilityPolicy = new BlobImmutabilityPolicy()
            .setExpiryTime(expiryTime)
            .setPolicyMode(BlobImmutabilityPolicyMode.UNLOCKED);

        vlwBlob.uploadWithResponse(new BlobParallelUploadOptions(DATA.getDefaultFlux())
            .setParallelTransferOptions(new ParallelTransferOptions().setBlockSizeLong(blockSize)
                .setMaxSingleUploadSizeLong(blockSize))
            .setImmutabilityPolicy(immutabilityPolicy)
            .setLegalHold(true)).block();

        StepVerifier.create(vlwBlob.getProperties())
            .assertNext(r -> {
                assertEquals(expectedImmutabilityPolicyExpiry, r.getImmutabilityPolicy().getExpiryTime());
                assertEquals(BlobImmutabilityPolicyMode.UNLOCKED, r.getImmutabilityPolicy().getPolicyMode());
                assertTrue(r.hasLegalHold());
            })
            .verifyComplete();
    }

    private static Stream<Arguments> blobUploadSupplier() {
        return Stream.of(Arguments.of(1L), Arguments.of((Long) null));
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2020-10-02")
    @Test
    public void syncCopy() {
        vlwContainer.setAccessPolicy(PublicAccessType.CONTAINER, null).block();
        sleepIfRunningAgainstService(30000); // Give time for the policy to take effect
        BlockBlobAsyncClient destination = vlwContainer.getBlobAsyncClient(generateBlobName()).getBlockBlobAsyncClient();
        OffsetDateTime expiryTime = testResourceNamer.now().plusDays(2);
        // The service rounds Immutability Policy Expiry to the nearest second.
        OffsetDateTime expectedImmutabilityPolicyExpiry = expiryTime.truncatedTo(ChronoUnit.SECONDS);
        BlobImmutabilityPolicy immutabilityPolicy = new BlobImmutabilityPolicy()
            .setExpiryTime(expiryTime)
            .setPolicyMode(BlobImmutabilityPolicyMode.UNLOCKED);

        destination.copyFromUrlWithResponse(new BlobCopyFromUrlOptions(vlwBlob.getBlobUrl())
            .setImmutabilityPolicy(immutabilityPolicy)
            .setLegalHold(true)).block();

        StepVerifier.create(destination.getProperties())
            .assertNext(r -> {
                assertEquals(expectedImmutabilityPolicyExpiry, r.getImmutabilityPolicy().getExpiryTime());
                assertEquals(BlobImmutabilityPolicyMode.UNLOCKED, r.getImmutabilityPolicy().getPolicyMode());
                assertTrue(r.hasLegalHold());
            })
            .verifyComplete();

        // cleanup:
        vlwContainer.setAccessPolicy(null, null).block();
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2020-10-02")
    @Test
    public void copy() {
        BlockBlobAsyncClient destination = vlwContainer.getBlobAsyncClient(generateBlobName()).getBlockBlobAsyncClient();
        OffsetDateTime expiryTime = testResourceNamer.now().plusDays(2);
        // The service rounds Immutability Policy Expiry to the nearest second.
        OffsetDateTime expectedImmutabilityPolicyExpiry = expiryTime.truncatedTo(ChronoUnit.SECONDS);
        BlobImmutabilityPolicy immutabilityPolicy = new BlobImmutabilityPolicy()
            .setExpiryTime(expiryTime)
            .setPolicyMode(BlobImmutabilityPolicyMode.UNLOCKED);

        PollerFlux<BlobCopyInfo, Void> poller = setPlaybackPollerFluxPollInterval(
            destination.beginCopy(new BlobBeginCopyOptions(vlwBlob.getBlobUrl())
            .setImmutabilityPolicy(immutabilityPolicy)
            .setLegalHold(true)));
        poller.blockLast();

        StepVerifier.create(destination.getProperties())
            .assertNext(r -> {
                assertEquals(expectedImmutabilityPolicyExpiry, r.getImmutabilityPolicy().getExpiryTime());
                assertEquals(BlobImmutabilityPolicyMode.UNLOCKED, r.getImmutabilityPolicy().getPolicyMode());
                assertTrue(r.hasLegalHold());
            })
            .verifyComplete();
    }

    /* SAS tests */

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2020-10-02")
    @Test
    public void accountSas() {
        OffsetDateTime expiryTime = testResourceNamer.now().plusDays(1);
        AccountSasPermission permissions = AccountSasPermission.parse("rwdxlacuptfi");
        AccountSasService service = new AccountSasService().setBlobAccess(true);
        AccountSasResourceType resource = new AccountSasResourceType().setObject(true).setContainer(true);
        AccountSasSignatureValues sasValues = new AccountSasSignatureValues(expiryTime, permissions, service, resource);
        expiryTime = testResourceNamer.now().plusDays(2);
        // The service rounds Immutability Policy Expiry to the nearest second.
        OffsetDateTime expectedImmutabilityPolicyExpiry = expiryTime.truncatedTo(ChronoUnit.SECONDS);
        BlobImmutabilityPolicy immutabilityPolicy = new BlobImmutabilityPolicy()
            .setExpiryTime(expiryTime)
            .setPolicyMode(BlobImmutabilityPolicyMode.UNLOCKED);
        String sas = versionedBlobServiceAsyncClient.generateAccountSas(sasValues);
        BlobAsyncClient client = getBlobAsyncClient(sas, vlwContainer.getBlobContainerUrl(), vlwBlob.getBlobName(),
            null);

        StepVerifier.create(client.setImmutabilityPolicy(immutabilityPolicy))
            .assertNext(r -> {
                assertEquals(expectedImmutabilityPolicyExpiry, r.getExpiryTime());
                assertEquals(BlobImmutabilityPolicyMode.UNLOCKED, r.getPolicyMode());
            })
            .verifyComplete();

        StepVerifier.create(client.setLegalHold(false))
            .assertNext(r -> assertFalse(r.hasLegalHold()))
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2020-10-02")
    @Test
    public void containerSas() {
        OffsetDateTime expiryTime = testResourceNamer.now().plusDays(1);
        BlobContainerSasPermission permissions = BlobContainerSasPermission.parse("racwdxltmei");
        BlobServiceSasSignatureValues sasValues = new BlobServiceSasSignatureValues(expiryTime, permissions);
        expiryTime = testResourceNamer.now().plusDays(2);
        // The service rounds Immutability Policy Expiry to the nearest second.
        OffsetDateTime expectedImmutabilityPolicyExpiry = expiryTime.truncatedTo(ChronoUnit.SECONDS);
        BlobImmutabilityPolicy immutabilityPolicy = new BlobImmutabilityPolicy()
            .setExpiryTime(expiryTime)
            .setPolicyMode(BlobImmutabilityPolicyMode.UNLOCKED);
        String sas = vlwContainer.generateSas(sasValues);
        BlobAsyncClient client = getBlobAsyncClient(sas, vlwContainer.getBlobContainerUrl(), vlwBlob.getBlobName(),
            null);

        StepVerifier.create(client.setImmutabilityPolicy(immutabilityPolicy))
            .assertNext(r -> {
                assertEquals(expectedImmutabilityPolicyExpiry, r.getExpiryTime());
                assertEquals(BlobImmutabilityPolicyMode.UNLOCKED.toString(), r.getPolicyMode().toString());
            })
            .verifyComplete();

        StepVerifier.create(client.setLegalHold(false))
            .assertNext(r -> assertFalse(r.hasLegalHold()))
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2020-10-02")
    @Test
    public void blobSas() {
        OffsetDateTime expiryTime = testResourceNamer.now().plusDays(1);
        BlobSasPermission permissions = BlobSasPermission.parse("racwdxtlmei");
        BlobServiceSasSignatureValues sasValues = new BlobServiceSasSignatureValues(expiryTime, permissions);
        expiryTime = testResourceNamer.now().plusDays(2);
        // The service rounds Immutability Policy Expiry to the nearest second.
        OffsetDateTime expectedImmutabilityPolicyExpiry = expiryTime.truncatedTo(ChronoUnit.SECONDS);
        BlobImmutabilityPolicy immutabilityPolicy = new BlobImmutabilityPolicy()
            .setExpiryTime(expiryTime)
            .setPolicyMode(BlobImmutabilityPolicyMode.UNLOCKED);
        String sas = vlwBlob.generateSas(sasValues);
        BlobAsyncClient client = getBlobAsyncClient(sas, vlwContainer.getBlobContainerUrl(), vlwBlob.getBlobName(),
            null);

        StepVerifier.create(client.setImmutabilityPolicy(immutabilityPolicy))
            .assertNext(r -> {
                assertEquals(expectedImmutabilityPolicyExpiry, r.getExpiryTime());
                assertEquals(BlobImmutabilityPolicyMode.UNLOCKED.toString(), r.getPolicyMode().toString());
            })
            .verifyComplete();

        StepVerifier.create(client.setLegalHold(false))
            .assertNext(r -> assertFalse(r.hasLegalHold()))
            .verifyComplete();
    }

}
