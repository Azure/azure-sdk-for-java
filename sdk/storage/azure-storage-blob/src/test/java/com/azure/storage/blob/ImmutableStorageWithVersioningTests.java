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
import com.azure.core.http.rest.Response;
import com.azure.core.test.TestMode;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.polling.SyncPoller;
import com.azure.identity.AzureCliCredentialBuilder;
import com.azure.identity.AzureDeveloperCliCredentialBuilder;
import com.azure.identity.AzurePipelinesCredentialBuilder;
import com.azure.identity.AzurePowerShellCredentialBuilder;
import com.azure.identity.ChainedTokenCredentialBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.identity.EnvironmentCredentialBuilder;
import com.azure.storage.blob.models.BlobContainerItem;
import com.azure.storage.blob.models.BlobContainerProperties;
import com.azure.storage.blob.models.BlobCopyInfo;
import com.azure.storage.blob.models.BlobErrorCode;
import com.azure.storage.blob.models.BlobImmutabilityPolicy;
import com.azure.storage.blob.models.BlobImmutabilityPolicyMode;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlobItemProperties;
import com.azure.storage.blob.models.BlobLegalHoldResult;
import com.azure.storage.blob.models.BlobListDetails;
import com.azure.storage.blob.models.BlobProperties;
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
import com.azure.storage.blob.specialized.AppendBlobClient;
import com.azure.storage.blob.specialized.BlockBlobClient;
import com.azure.storage.blob.specialized.PageBlobClient;
import com.azure.storage.common.sas.AccountSasPermission;
import com.azure.storage.common.sas.AccountSasResourceType;
import com.azure.storage.common.sas.AccountSasService;
import com.azure.storage.common.sas.AccountSasSignatureValues;
import com.azure.storage.common.test.shared.extensions.LiveOnly;
import com.azure.storage.common.test.shared.extensions.PlaybackOnly;
import com.azure.storage.common.test.shared.extensions.RequiredServiceVersion;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Flux;

import java.io.ByteArrayInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ImmutableStorageWithVersioningTests extends BlobTestBase {
    private static String vlwContainerName;
    private static final String ACCOUNT_NAME = ENVIRONMENT.getVersionedAccount().getName();
    private static final String RESOURCE_GROUP_NAME = ENVIRONMENT.getResourceGroupName();
    private static final String SUBSCRIPTION_ID = ENVIRONMENT.getSubscriptionId();
    private static final String API_VERSION = "2021-04-01";
    private static final TokenCredential CREDENTIAL = getTokenCredential(ENVIRONMENT.getTestMode());
    private static final BearerTokenAuthenticationPolicy CREDENTIAL_POLICY =
        new BearerTokenAuthenticationPolicy(CREDENTIAL, "https://management.azure.com/.default");
    private BlobContainerClient vlwContainer;
    private BlobClient vlwBlob;

    @BeforeAll
    public static void setupSpec() throws JsonProcessingException, MalformedURLException {
        if (ENVIRONMENT.getTestMode() != TestMode.PLAYBACK) {
            vlwContainerName = CoreUtils.randomUuid().toString();

            String url = String.format("https://management.azure.com/subscriptions/%s/resourceGroups/%s/providers/"
                    + "Microsoft.Storage/storageAccounts/%s/blobServices/default/containers/%s?api-version=%s",
                SUBSCRIPTION_ID, RESOURCE_GROUP_NAME, ACCOUNT_NAME, vlwContainerName, API_VERSION);
            HttpPipeline httpPipeline = new HttpPipelineBuilder()
                .policies(CREDENTIAL_POLICY)
                .build();

            ImmutableStorageWithVersioning immutableStorageWithVersioning = new ImmutableStorageWithVersioning();
            immutableStorageWithVersioning.setEnabled(true);
            Properties properties = new Properties();
            properties.setImmutableStorageWithVersioning(immutableStorageWithVersioning);
            Body body = new Body();
            body.setId(String.format("/subscriptions/%s/resourceGroups/%s/providers/Microsoft.Storage/storageAccounts/"
                    + "%s/blobServices/default/containers/%s", SUBSCRIPTION_ID, RESOURCE_GROUP_NAME, ACCOUNT_NAME,
                vlwContainerName));
            body.setName(vlwContainerName);
            body.setType("Microsoft.Storage/storageAccounts/blobServices/containers");
            body.setProperties(properties);

            String serializedBody = new ObjectMapper().writeValueAsString(body);

            HttpResponse response = httpPipeline.send(new HttpRequest(HttpMethod.PUT, new URL(url), new HttpHeaders(),
                    Flux.just(ByteBuffer.wrap(serializedBody.getBytes(StandardCharsets.UTF_8)))))
                .block();
            assertNotNull(response);
            if (response.getStatusCode() != 201) {
                LOGGER.warning(response.getBodyAsString().block());
            }
            assertEquals(201, response.getStatusCode());
        }
    }

    @BeforeEach
    public void setup() {
        vlwContainer = versionedBlobServiceClient.getBlobContainerClient(
            testResourceNamer.recordValueFromConfig(vlwContainerName));
        vlwBlob = vlwContainer.getBlobClient(generateBlobName());
        vlwBlob.upload(new ByteArrayInputStream(new byte[0]), 0);
    }

    public static final class Body {
        private String id;
        private String name;
        private String type;
        private Properties properties;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Properties getProperties() {
            return properties;
        }

        public void setProperties(Properties properties) {
            this.properties = properties;
        }
    }
    public static final class Properties {
        private ImmutableStorageWithVersioning immutableStorageWithVersioning;

        public ImmutableStorageWithVersioning getImmutableStorageWithVersioning() {
            return immutableStorageWithVersioning;
        }

        public void setImmutableStorageWithVersioning(ImmutableStorageWithVersioning immutableStorageWithVersioning) {
            this.immutableStorageWithVersioning = immutableStorageWithVersioning;
        }
    }
    public static final class ImmutableStorageWithVersioning {
        private boolean enabled;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
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

    public static TokenCredential getTokenCredential(TestMode testMode) {
        if (testMode == TestMode.RECORD) {
            return new DefaultAzureCredentialBuilder().build();
        } else if (testMode == TestMode.LIVE) {
            Configuration config = Configuration.getGlobalConfiguration();

            ChainedTokenCredentialBuilder builder = new ChainedTokenCredentialBuilder()
                .addLast(new EnvironmentCredentialBuilder().build())
                .addLast(new AzureCliCredentialBuilder().build())
                .addLast(new AzureDeveloperCliCredentialBuilder().build());

            String serviceConnectionId = config.get("AZURESUBSCRIPTION_SERVICE_CONNECTION_ID");
            String clientId = config.get("AZURESUBSCRIPTION_CLIENT_ID");
            String tenantId = config.get("AZURESUBSCRIPTION_TENANT_ID");
            String systemAccessToken = config.get("SYSTEM_ACCESSTOKEN");

            if (!CoreUtils.isNullOrEmpty(serviceConnectionId)
                && !CoreUtils.isNullOrEmpty(clientId)
                && !CoreUtils.isNullOrEmpty(tenantId)
                && !CoreUtils.isNullOrEmpty(systemAccessToken)) {

                builder.addLast(new AzurePipelinesCredentialBuilder()
                    .systemAccessToken(systemAccessToken)
                    .clientId(clientId)
                    .tenantId(tenantId)
                    .serviceConnectionId(serviceConnectionId)
                    .build());
            }

            builder.addLast(new AzurePowerShellCredentialBuilder().build());

            return builder.build();
        } else { //playback or not set
            return new MockTokenCredential();
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

        BlobImmutabilityPolicy response = vlwBlob.setImmutabilityPolicy(immutabilityPolicy);

        assertEquals(expectedImmutabilityPolicyExpiry, response.getExpiryTime());
        assertEquals(BlobImmutabilityPolicyMode.UNLOCKED, response.getPolicyMode());
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
        BlobImmutabilityPolicy response = vlwBlob.setImmutabilityPolicyWithResponse(
            immutabilityPolicy, null, null, null).getValue();

        assertEquals(expectedImmutabilityPolicyExpiry, response.getExpiryTime());
        assertEquals(policyMode, response.getPolicyMode());

        // when: "get properties"
        BlobProperties props = vlwBlob.getProperties();
        assertEquals(expectedImmutabilityPolicyExpiry, props.getImmutabilityPolicy().getExpiryTime());
        assertEquals(policyMode, props.getImmutabilityPolicy().getPolicyMode());

        // when: "list blob"
        ListBlobsOptions options = new ListBlobsOptions().setPrefix(vlwBlob.getBlobName()).setDetails(
            new BlobListDetails().setRetrieveImmutabilityPolicy(true).setRetrieveLegalHold(true));
        Iterator<BlobItem> blobItemIterator = vlwContainer.listBlobs(options, null).iterator();

        BlobItem blob = blobItemIterator.next();
        assertFalse(blobItemIterator.hasNext());
        assertEquals(vlwBlob.getBlobName(), blob.getName());
        assertEquals(expectedImmutabilityPolicyExpiry, blob.getProperties().getImmutabilityPolicy().getExpiryTime());
        assertEquals(policyMode, blob.getProperties().getImmutabilityPolicy().getPolicyMode());
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

            Response<BlobImmutabilityPolicy> response = vlwBlob.setImmutabilityPolicyWithResponse(immutabilityPolicy,
                bac, null, null);
            assertResponseStatusCode(response, 200);
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

        BlobStorageException e = assertThrows(BlobStorageException.class, () ->
            vlwBlob.setImmutabilityPolicyWithResponse(immutabilityPolicy, bac, null, null));
        assertEquals(BlobErrorCode.CONDITION_NOT_MET, e.getErrorCode());
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

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () ->
            vlwBlob.setImmutabilityPolicyWithResponse(immutabilityPolicy, bac, null, null));
        assertEquals(String.format("%s does not support the %s request condition(s) for parameter 'requestConditions'.",
            "setImmutabilityPolicy(WithResponse)", wrongCondition), e.getMessage());
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
        BlobClient blob = vlwContainer.getBlobClient(generateBlobName());
        OffsetDateTime expiryTime = testResourceNamer.now().plusDays(2);
        BlobImmutabilityPolicy immutabilityPolicy = new BlobImmutabilityPolicy()
            .setExpiryTime(expiryTime)
            .setPolicyMode(BlobImmutabilityPolicyMode.UNLOCKED);

        BlobStorageException e = assertThrows(BlobStorageException.class, () ->
            blob.setImmutabilityPolicyWithResponse(immutabilityPolicy, null, null, null));
        assertEquals(BlobErrorCode.BLOB_NOT_FOUND, e.getErrorCode());
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2020-10-02")
    @Test
    public void setImmutabilityPolicyIA() {
        OffsetDateTime expiryTime = testResourceNamer.now().plusDays(2);
        BlobImmutabilityPolicy immutabilityPolicy = new BlobImmutabilityPolicy()
            .setExpiryTime(expiryTime)
            .setPolicyMode(BlobImmutabilityPolicyMode.MUTABLE);

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () ->
            vlwBlob.setImmutabilityPolicyWithResponse(immutabilityPolicy, null, null, null));
        assertEquals("immutabilityPolicy.policyMode must be Locked or Unlocked", e.getMessage());
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2020-10-02")
    @Test
    public void deleteImmutabilityPolicyMin() {
        OffsetDateTime expiryTime = testResourceNamer.now().plusDays(2);
        BlobImmutabilityPolicy immutabilityPolicy = new BlobImmutabilityPolicy()
            .setExpiryTime(expiryTime)
            .setPolicyMode(BlobImmutabilityPolicyMode.UNLOCKED);
        vlwBlob.setImmutabilityPolicy(immutabilityPolicy);

        vlwBlob.deleteImmutabilityPolicy();

        BlobProperties properties = vlwBlob.getProperties();
        assertNull(properties.getImmutabilityPolicy().getPolicyMode());
        assertNull(properties.getImmutabilityPolicy().getExpiryTime());
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2020-10-02")
    @Test
    public void deleteImmutabilityPolicy() {
        OffsetDateTime expiryTime = testResourceNamer.now().plusDays(2);
        BlobImmutabilityPolicy immutabilityPolicy = new BlobImmutabilityPolicy()
            .setExpiryTime(expiryTime)
            .setPolicyMode(BlobImmutabilityPolicyMode.UNLOCKED);
        vlwBlob.setImmutabilityPolicy(immutabilityPolicy);

        vlwBlob.deleteImmutabilityPolicyWithResponse(null, null);

        BlobProperties properties = vlwBlob.getProperties();
        assertNull(properties.getImmutabilityPolicy().getPolicyMode());
        assertNull(properties.getImmutabilityPolicy().getExpiryTime());
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2020-10-02")
    @Test
    public void deleteImmutabilityPolicyError() {
        BlobClient blobClient = vlwContainer.getBlobClient(generateBlobName());
        BlobStorageException e = assertThrows(BlobStorageException.class, () ->
            blobClient.deleteImmutabilityPolicyWithResponse(null, null));
        assertEquals(BlobErrorCode.BLOB_NOT_FOUND, e.getErrorCode());
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2020-10-02")
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void setLegalHoldMin(boolean legalHold) {
        BlobLegalHoldResult response = vlwBlob.setLegalHold(legalHold);
        assertEquals(legalHold, response.hasLegalHold());
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2020-10-02")
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void setLegalHold(boolean legalHold) {
        // when: "set legal hold"
        Response<BlobLegalHoldResult> response = vlwBlob.setLegalHoldWithResponse(legalHold, null, null);
        assertEquals(legalHold, response.getValue().hasLegalHold());

        // when: "get properties"
        BlobProperties properties = vlwBlob.getProperties();
        assertEquals(legalHold, properties.hasLegalHold());

        // when: "list blob"
        ListBlobsOptions options = new ListBlobsOptions().setPrefix(vlwBlob.getBlobName())
            .setDetails(new BlobListDetails().setRetrieveImmutabilityPolicy(true).setRetrieveLegalHold(true));
        Iterator<BlobItem> itr = vlwContainer.listBlobs(options, null).iterator();

        BlobItem blob = itr.next();
        assertFalse(itr.hasNext());
        assertEquals(vlwBlob.getBlobName(), blob.getName());
        assertEquals(legalHold, blob.getProperties().hasLegalHold());
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2020-10-02")
    @Test
    public void setLegalHoldError() {
        BlobClient blob = vlwContainer.getBlobClient(generateBlobName());

        BlobStorageException e = assertThrows(BlobStorageException.class, () ->
            blob.setLegalHoldWithResponse(false, null, null));
        assertEquals(BlobErrorCode.BLOB_NOT_FOUND, e.getErrorCode());
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2020-10-02")
    @Test
    public void containerProperties() {
        BlobContainerProperties properties = vlwContainer.getProperties();

        assertTrue(properties.isImmutableStorageWithVersioningEnabled());

        Iterator<BlobContainerItem> itr = vlwContainer.getServiceClient().listBlobContainers(
            new ListBlobContainersOptions().setPrefix(vlwContainer.getBlobContainerName()), null).iterator();

        BlobContainerItem container = itr.next();
        assertFalse(itr.hasNext());
        assertTrue(container.getProperties().isImmutableStorageWithVersioningEnabled());
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2020-10-02")
    @Test
    public void appendBlobCreate() {
        AppendBlobClient appendBlob = vlwContainer.getBlobClient(generateBlobName()).getAppendBlobClient();
        OffsetDateTime expiryTime = testResourceNamer.now().plusDays(2);
        // The service rounds Immutability Policy Expiry to the nearest second.
        OffsetDateTime expectedImmutabilityPolicyExpiry = expiryTime.truncatedTo(ChronoUnit.SECONDS);
        BlobImmutabilityPolicy immutabilityPolicy = new BlobImmutabilityPolicy()
            .setExpiryTime(expiryTime)
            .setPolicyMode(BlobImmutabilityPolicyMode.UNLOCKED);

        appendBlob.createWithResponse(new AppendBlobCreateOptions()
            .setImmutabilityPolicy(immutabilityPolicy)
            .setLegalHold(true), null, null);

        BlobProperties properties = appendBlob.getProperties();
        assertEquals(expectedImmutabilityPolicyExpiry, properties.getImmutabilityPolicy().getExpiryTime());
        assertEquals(BlobImmutabilityPolicyMode.UNLOCKED, properties.getImmutabilityPolicy().getPolicyMode());
        assertTrue(properties.hasLegalHold());
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2020-10-02")
    @Test
    public void pageBlobCreate() {
        PageBlobClient pageBlob = vlwContainer.getBlobClient(generateBlobName()).getPageBlobClient();
        OffsetDateTime expiryTime = testResourceNamer.now().plusDays(2);
        // The service rounds Immutability Policy Expiry to the nearest second.
        OffsetDateTime expectedImmutabilityPolicyExpiry = expiryTime.truncatedTo(ChronoUnit.SECONDS);
        BlobImmutabilityPolicy immutabilityPolicy = new BlobImmutabilityPolicy()
            .setExpiryTime(expiryTime)
            .setPolicyMode(BlobImmutabilityPolicyMode.UNLOCKED);

        pageBlob.createWithResponse(new PageBlobCreateOptions(512)
            .setImmutabilityPolicy(immutabilityPolicy)
            .setLegalHold(true), null, null);

        BlobProperties properties = pageBlob.getProperties();
        assertEquals(expectedImmutabilityPolicyExpiry, properties.getImmutabilityPolicy().getExpiryTime());
        assertEquals(BlobImmutabilityPolicyMode.UNLOCKED, properties.getImmutabilityPolicy().getPolicyMode());
        assertTrue(properties.hasLegalHold());
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2020-10-02")
    @Test
    public void blockBlobCommitBlockList() {
        BlockBlobClient blockBlob = vlwBlob.getBlockBlobClient();
        OffsetDateTime expiryTime = testResourceNamer.now().plusDays(2);
        // The service rounds Immutability Policy Expiry to the nearest second.
        OffsetDateTime expectedImmutabilityPolicyExpiry = expiryTime.truncatedTo(ChronoUnit.SECONDS);
        BlobImmutabilityPolicy immutabilityPolicy = new BlobImmutabilityPolicy()
            .setExpiryTime(expiryTime)
            .setPolicyMode(BlobImmutabilityPolicyMode.UNLOCKED);

        blockBlob.commitBlockListWithResponse(new BlockBlobCommitBlockListOptions(new ArrayList<>())
            .setImmutabilityPolicy(immutabilityPolicy)
            .setLegalHold(true), null, null);

        BlobProperties properties = blockBlob.getProperties();
        assertEquals(expectedImmutabilityPolicyExpiry, properties.getImmutabilityPolicy().getExpiryTime());
        assertEquals(BlobImmutabilityPolicyMode.UNLOCKED, properties.getImmutabilityPolicy().getPolicyMode());
        assertTrue(properties.hasLegalHold());
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2020-10-02")
    @Test
    public void blockBlobUpload() {
        BlockBlobClient blockBlob = vlwBlob.getBlockBlobClient();
        OffsetDateTime expiryTime = testResourceNamer.now().plusDays(2);
        // The service rounds Immutability Policy Expiry to the nearest second.
        OffsetDateTime expectedImmutabilityPolicyExpiry = expiryTime.truncatedTo(ChronoUnit.SECONDS);
        BlobImmutabilityPolicy immutabilityPolicy = new BlobImmutabilityPolicy()
            .setExpiryTime(expiryTime)
            .setPolicyMode(BlobImmutabilityPolicyMode.UNLOCKED);

        blockBlob.uploadWithResponse(new BlockBlobSimpleUploadOptions(DATA.getDefaultFlux(), DATA.getDefaultDataSize())
            .setImmutabilityPolicy(immutabilityPolicy)
            .setLegalHold(true), null, null);

        BlobProperties properties = blockBlob.getProperties();
        assertEquals(expectedImmutabilityPolicyExpiry, properties.getImmutabilityPolicy().getExpiryTime());
        assertEquals(BlobImmutabilityPolicyMode.UNLOCKED, properties.getImmutabilityPolicy().getPolicyMode());
        assertTrue(properties.hasLegalHold());
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
            .setLegalHold(true), null, null);

        BlobProperties response = vlwBlob.getProperties();
        assertEquals(expectedImmutabilityPolicyExpiry, response.getImmutabilityPolicy().getExpiryTime());
        assertEquals(BlobImmutabilityPolicyMode.UNLOCKED, response.getImmutabilityPolicy().getPolicyMode());
        assertTrue(response.hasLegalHold());
    }

    private static Stream<Arguments> blobUploadSupplier() {
        return Stream.of(Arguments.of(1L), Arguments.of((Long) null));
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2020-10-02")
    @Test
    public void syncCopy() {
        BlockBlobClient destination = vlwContainer.getBlobClient(generateBlobName()).getBlockBlobClient();
        OffsetDateTime expiryTime = testResourceNamer.now().plusDays(2);
        // The service rounds Immutability Policy Expiry to the nearest second.
        OffsetDateTime expectedImmutabilityPolicyExpiry = expiryTime.truncatedTo(ChronoUnit.SECONDS);
        BlobImmutabilityPolicy immutabilityPolicy = new BlobImmutabilityPolicy()
            .setExpiryTime(expiryTime)
            .setPolicyMode(BlobImmutabilityPolicyMode.UNLOCKED);

        String sas = vlwBlob.generateSas(new BlobServiceSasSignatureValues(testResourceNamer.now().plusDays(1),
            new BlobSasPermission().setTagsPermission(true).setReadPermission(true)));

        destination.copyFromUrlWithResponse(new BlobCopyFromUrlOptions(vlwBlob.getBlobUrl() + "?" + sas)
            .setImmutabilityPolicy(immutabilityPolicy)
            .setLegalHold(true), null, null);

        BlobProperties response = destination.getProperties();
        assertEquals(expectedImmutabilityPolicyExpiry, response.getImmutabilityPolicy().getExpiryTime());
        assertEquals(BlobImmutabilityPolicyMode.UNLOCKED, response.getImmutabilityPolicy().getPolicyMode());
        assertTrue(response.hasLegalHold());
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2020-10-02")
    @Test
    public void copy() {
        BlockBlobClient destination = vlwContainer.getBlobClient(generateBlobName()).getBlockBlobClient();
        OffsetDateTime expiryTime = testResourceNamer.now().plusDays(2);
        // The service rounds Immutability Policy Expiry to the nearest second.
        OffsetDateTime expectedImmutabilityPolicyExpiry = expiryTime.truncatedTo(ChronoUnit.SECONDS);
        BlobImmutabilityPolicy immutabilityPolicy = new BlobImmutabilityPolicy()
            .setExpiryTime(expiryTime)
            .setPolicyMode(BlobImmutabilityPolicyMode.UNLOCKED);

        SyncPoller<BlobCopyInfo, Void> poller = setPlaybackSyncPollerPollInterval(
            destination.beginCopy(new BlobBeginCopyOptions(vlwBlob.getBlobUrl())
                .setImmutabilityPolicy(immutabilityPolicy).setLegalHold(true)));
        poller.waitForCompletion();

        BlobProperties response = destination.getProperties();
        assertEquals(expectedImmutabilityPolicyExpiry, response.getImmutabilityPolicy().getExpiryTime());
        assertEquals(BlobImmutabilityPolicyMode.UNLOCKED, response.getImmutabilityPolicy().getPolicyMode());
        assertTrue(response.hasLegalHold());
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
        String sas = versionedBlobServiceClient.generateAccountSas(sasValues);
        BlobClient client = getBlobClient(sas, vlwContainer.getBlobContainerUrl(), vlwBlob.getBlobName());

        BlobImmutabilityPolicy response = client.setImmutabilityPolicy(immutabilityPolicy);
        assertEquals(expectedImmutabilityPolicyExpiry, response.getExpiryTime());
        assertEquals(BlobImmutabilityPolicyMode.UNLOCKED, response.getPolicyMode());

        BlobLegalHoldResult legalHold = client.setLegalHold(false);
        assertFalse(legalHold.hasLegalHold());
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
        BlobClient client = getBlobClient(sas, vlwContainer.getBlobContainerUrl(), vlwBlob.getBlobName());

        BlobImmutabilityPolicy response = client.setImmutabilityPolicy(immutabilityPolicy);
        assertEquals(expectedImmutabilityPolicyExpiry, response.getExpiryTime());
        assertEquals(BlobImmutabilityPolicyMode.UNLOCKED.toString(), response.getPolicyMode().toString());

        BlobLegalHoldResult legalHold = client.setLegalHold(false);
        assertFalse(legalHold.hasLegalHold());
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
        BlobClient client = getBlobClient(sas, vlwContainer.getBlobContainerUrl(), vlwBlob.getBlobName());

        BlobImmutabilityPolicy response = client.setImmutabilityPolicy(immutabilityPolicy);

        assertEquals(expectedImmutabilityPolicyExpiry, response.getExpiryTime());
        assertEquals(BlobImmutabilityPolicyMode.UNLOCKED.toString(), response.getPolicyMode().toString());

        BlobLegalHoldResult legalHold = client.setLegalHold(false);
        assertFalse(legalHold.hasLegalHold());
    }
}
