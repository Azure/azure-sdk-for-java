// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.client.traits.HttpTrait;
import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.rest.Response;
import com.azure.core.test.TestMode;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.models.CustomMatcher;
import com.azure.core.test.models.TestProxySanitizer;
import com.azure.core.test.models.TestProxySanitizerType;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.EnvironmentCredentialBuilder;
import com.azure.storage.blob.models.BlobContainerItem;
import com.azure.storage.blob.models.BlobErrorCode;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.models.BlobSignedIdentifier;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.models.LeaseStateType;
import com.azure.storage.blob.models.ListBlobContainersOptions;
import com.azure.storage.blob.models.PublicAccessType;
import com.azure.storage.blob.options.BlobBreakLeaseOptions;
import com.azure.storage.blob.specialized.BlobAsyncClientBase;
import com.azure.storage.blob.specialized.BlobClientBase;
import com.azure.storage.blob.specialized.BlobLeaseAsyncClient;
import com.azure.storage.blob.specialized.BlobLeaseClient;
import com.azure.storage.blob.specialized.BlobLeaseClientBuilder;
import com.azure.storage.blob.specialized.SpecializedBlobClientBuilder;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.policy.RequestRetryOptions;
import com.azure.storage.common.test.shared.StorageCommonTestUtils;
import com.azure.storage.common.test.shared.TestAccount;
import com.azure.storage.common.test.shared.TestDataFactory;
import com.azure.storage.common.test.shared.TestEnvironment;
import com.azure.storage.common.test.shared.policy.PerCallVersionPolicy;
import org.junit.jupiter.params.provider.Arguments;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * Base class for Azure Storage Blob tests.
 */
public class BlobTestBase extends TestProxyTestBase {
    public static final TestEnvironment ENVIRONMENT = TestEnvironment.getInstance();
    protected static final TestDataFactory DATA = TestDataFactory.getInstance();
    protected static final ClientLogger LOGGER = new ClientLogger(BlobTestBase.class);

    Integer entityNo = 0; // Used to generate stable container names for recording tests requiring multiple containers.

    // both sync and async clients point to same container
    protected BlobContainerClient cc;
    protected BlobContainerClient ccPremium;
    protected BlobContainerAsyncClient ccAsync;

    /*
    The values below are used to create data-driven tests for access conditions.
     */
    protected static final OffsetDateTime OLD_DATE = OffsetDateTime.now().minusDays(1);

    protected static final OffsetDateTime NEW_DATE = OffsetDateTime.now().plusDays(1);

    protected static final HttpHeaderName X_MS_COPY_STATUS = HttpHeaderName.fromString("x-ms-copy-status");
    protected static final HttpHeaderName X_MS_REQUEST_ID = HttpHeaderName.fromString("x-ms-request-id");
    protected static final HttpHeaderName LAST_MODIFIED = HttpHeaderName.fromString("last-modified");
    protected static final HttpHeaderName X_MS_VERSION = HttpHeaderName.fromString("x-ms-version");
    protected static final HttpHeaderName X_MS_VERSION_ID = HttpHeaderName.fromString("x-ms-version-id");
    protected static final HttpHeaderName X_MS_CONTENT_CRC64 = HttpHeaderName.fromString("x-ms-content-crc64");
    protected static final HttpHeaderName X_MS_REQUEST_SERVER_ENCRYPTED =
        HttpHeaderName.fromString("x-ms-request-server-encrypted");
    protected static final HttpHeaderName X_MS_SERVER_ENCRYPTED = HttpHeaderName.fromString("x-ms-server-encrypted");
    protected static final HttpHeaderName X_MS_ENCRYPTION_KEY_SHA256 =
        HttpHeaderName.fromString("x-ms-encryption-key-sha256");
    protected static final HttpHeaderName X_MS_BLOB_CONTENT_LENGTH =
        HttpHeaderName.fromString("x-ms-blob-content-length");

    protected static final HttpHeaderName X_MS_COPY_ID = HttpHeaderName.fromString("x-ms-copy-id");

    protected static final HttpHeaderName X_MS_ENCRYPTION_SCOPE = HttpHeaderName.fromString("x-ms-encryption-scope");

    /*
    Note that this value is only used to check if we are depending on the received etag. This value will not actually
    be used.
     */
    protected static final String RECEIVED_ETAG = "received";

    protected static final String GARBAGE_ETAG = "garbage";

    /*
    Note that this value is only used to check if we are depending on the received etag. This value will not actually
    be used.
     */
    protected static final String RECEIVED_LEASE_ID = "received";

    protected static final String GARBAGE_LEASE_ID = CoreUtils.randomUuid().toString();

    private static final Pattern URL_SANITIZER = Pattern.compile("(?<=http://|https://)([^/?]+)");

    protected BlobServiceClient primaryBlobServiceClient;
    protected BlobServiceAsyncClient primaryBlobServiceAsyncClient;
    protected BlobServiceClient alternateBlobServiceClient;
    protected BlobServiceAsyncClient alternateBlobServiceAsyncClient;

    protected BlobServiceClient premiumBlobServiceClient;
    protected BlobServiceAsyncClient premiumBlobServiceAsyncClient;
    protected BlobServiceClient versionedBlobServiceClient;
    protected BlobServiceAsyncClient versionedBlobServiceAsyncClient;
    protected BlobServiceClient softDeleteServiceClient;
    protected BlobServiceAsyncClient softDeleteServiceAsyncClient;

    protected String containerName;

    protected String prefix;

    @Override
    public void beforeTest() {
        super.beforeTest();
        prefix = StorageCommonTestUtils.getCrc32(testContextManager.getTestPlaybackRecordingName());

        if (getTestMode() != TestMode.LIVE) {
            interceptorManager.addSanitizers(Arrays.asList(
                new TestProxySanitizer("sig=(.*)", "REDACTED", TestProxySanitizerType.URL),
                new TestProxySanitizer("x-ms-encryption-key", ".+", "REDACTED", TestProxySanitizerType.HEADER),
                new TestProxySanitizer("x-ms-copy-source", "((?<=http://|https://)([^/?]+)|sig=(.*))", "REDACTED", TestProxySanitizerType.HEADER),
                new TestProxySanitizer("x-ms-copy-source-authorization", ".+", "REDACTED", TestProxySanitizerType.HEADER),
                new TestProxySanitizer("x-ms-rename-source", "((?<=http://|https://)([^/?]+)|sig=(.*))", "REDACTED", TestProxySanitizerType.HEADER)));
        }

        // Ignore changes to the order of query parameters and wholly ignore the 'sv' (service version) query parameter
        // in SAS tokens.
        interceptorManager.addMatchers(Collections.singletonList(new CustomMatcher()
            .setComparingBodies(false)
            .setHeadersKeyOnlyMatch(Arrays.asList("x-ms-lease-id", "x-ms-proposed-lease-id", "If-Modified-Since",
                "If-Unmodified-Since", "x-ms-expiry-time", "x-ms-source-if-modified-since",
                "x-ms-source-if-unmodified-since", "x-ms-source-lease-id", "x-ms-encryption-key-sha256"))
            .setQueryOrderingIgnored(true)
            .setIgnoredQueryParameters(Collections.singletonList("sv"))));

        primaryBlobServiceClient = getServiceClient(ENVIRONMENT.getPrimaryAccount());
        primaryBlobServiceAsyncClient = getServiceAsyncClient(ENVIRONMENT.getPrimaryAccount());
        alternateBlobServiceClient = getServiceClient(ENVIRONMENT.getSecondaryAccount());
        alternateBlobServiceAsyncClient = getServiceAsyncClient(ENVIRONMENT.getSecondaryAccount());
        premiumBlobServiceClient = getServiceClient(ENVIRONMENT.getPremiumAccount());
        premiumBlobServiceAsyncClient = getServiceAsyncClient(ENVIRONMENT.getPremiumAccount());
        versionedBlobServiceClient = getServiceClient(ENVIRONMENT.getVersionedAccount());
        versionedBlobServiceAsyncClient = getServiceAsyncClient(ENVIRONMENT.getVersionedAccount());
        softDeleteServiceClient = getServiceClient(ENVIRONMENT.getSoftDeleteAccount());
        softDeleteServiceAsyncClient = getServiceAsyncClient(ENVIRONMENT.getSoftDeleteAccount());

        containerName = generateContainerName();
        cc = primaryBlobServiceClient.getBlobContainerClient(containerName);
        ccAsync = primaryBlobServiceAsyncClient.getBlobContainerAsyncClient(containerName);
        cc.createIfNotExists();
        ccAsync.createIfNotExists().block();
    }

    /**
     * Clean up the test queues and messages for the account.
     */
    @Override
    protected void afterTest() {
        super.afterTest();
        if (getTestMode() == TestMode.PLAYBACK) {
            return;
        }

        BlobServiceClient cleanupClient = new BlobServiceClientBuilder()
            .httpClient(StorageCommonTestUtils.getHttpClient(interceptorManager))
            .credential(ENVIRONMENT.getPrimaryAccount().getCredential())
            .endpoint(ENVIRONMENT.getPrimaryAccount().getBlobEndpoint())
            .buildClient();

        ListBlobContainersOptions options = new ListBlobContainersOptions().setPrefix(prefix);
        for (BlobContainerItem container : cleanupClient.listBlobContainers(options, null)) {
            BlobContainerClient containerClient = cleanupClient.getBlobContainerClient(container.getName());

            if (container.getProperties().getLeaseState() == LeaseStateType.LEASED) {
                createLeaseClient(containerClient).breakLeaseWithResponse(
                    new BlobBreakLeaseOptions().setBreakPeriod(Duration.ofSeconds(0)), null, null);
            }
            containerClient.deleteIfExists();
        }
    }

    protected static Mono<ByteBuffer> collectBytesInBuffer(Flux<ByteBuffer> content) {
        return FluxUtil.collectBytesInByteBufferStream(content).map(ByteBuffer::wrap);
    }

    protected static String getAuthToken() {
        if (ENVIRONMENT.getTestMode() == TestMode.PLAYBACK) {
            // we just need some string to satisfy SDK for playback mode. Recording framework handles this fine.
            return "recordingBearerToken";
        }
        return new EnvironmentCredentialBuilder().build().getToken(new TokenRequestContext()
                .setScopes(Collections.singletonList("https://storage.azure.com/.default")))
            .map(AccessToken::getToken).block();
    }

    protected String generateContainerName() {
        return generateResourceName(entityNo++);
    }

    protected String generateBlobName() {
        return generateResourceName(entityNo++);
    }

    protected String generateResourceName(int entityNo) {
        return testResourceNamer.randomName(prefix + entityNo, 63);
    }

    protected String getBlockID() {
        return Base64.getEncoder().encodeToString(testResourceNamer.randomUuid().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * This will retrieve the etag to be used in testing match conditions. The result will typically be assigned to
     * the ifMatch condition when testing success and the ifNoneMatch condition when testing failure.
     *
     * @param bc The URL to the blob to get the etag on.
     * @param match
     *      The ETag value for this test. If {@code receivedEtag} is passed, that will signal that the test is expecting
     *      the blob's actual etag for this test, so it is retrieved.
     * @return The appropriate etag value to run the current test.
     */
    protected String setupBlobMatchCondition(BlobClientBase bc, String match) {
        if (Objects.equals(match, RECEIVED_ETAG)) {
            return bc.getProperties().getETag();
        } else {
            return match;
        }
    }

    protected String setupBlobMatchCondition(BlobAsyncClientBase bac, String match) {
        if (Objects.equals(match, RECEIVED_ETAG)) {
            return Objects.requireNonNull(bac.getProperties().block()).getETag();
        } else {
            return match;
        }
    }

    /**
     * This helper method will acquire a lease on a blob to prepare for testing lease Id. We want to test
     * against a valid lease in both the success and failure cases to guarantee that the results actually indicate
     * proper setting of the header. If we pass null, though, we don't want to acquire a lease, as that will interfere
     * with other AC tests.
     *
     * @param bc The blob on which to acquire a lease.
     * @param leaseID The signalID. Values should only ever be {@code receivedLeaseID}, {@code garbageLeaseID},
     * or {@code null}.
     * @return The actual lease Id of the blob if recievedLeaseID is passed, otherwise whatever was passed will be
     * returned.
     */
    protected String setupBlobLeaseCondition(BlobClientBase bc, String leaseID) {
        String responseLeaseId = null;
        if (Objects.equals(leaseID, RECEIVED_LEASE_ID) || Objects.equals(leaseID, GARBAGE_LEASE_ID)) {
            responseLeaseId = createLeaseClient(bc).acquireLease(-1);
        }
        if (Objects.equals(leaseID, RECEIVED_LEASE_ID)) {
            return responseLeaseId;
        } else {
            return leaseID;
        }
    }

    protected String setupBlobLeaseCondition(BlobAsyncClientBase bac, String leaseID) {
        String responseLeaseId = null;
        if (Objects.equals(leaseID, RECEIVED_LEASE_ID) || Objects.equals(leaseID, GARBAGE_LEASE_ID)) {
            responseLeaseId = new BlobLeaseClientBuilder()
                .blobAsyncClient(bac)
                .buildAsyncClient()
                .acquireLease(-1)
                .block();
        }
        if (Objects.equals(leaseID, RECEIVED_LEASE_ID)) {
            return responseLeaseId;
        } else {
            return leaseID;
        }
    }

    protected String setupContainerLeaseCondition(BlobContainerClient cu, String leaseID) {
        if (Objects.equals(leaseID, RECEIVED_LEASE_ID)) {
            return createLeaseClient(cu).acquireLease(-1);
        } else {
            return leaseID;
        }
    }

    protected String setupContainerAsyncLeaseCondition(BlobContainerAsyncClient cu, String leaseID) {
        if (Objects.equals(leaseID, RECEIVED_LEASE_ID)) {
            return createLeaseAsyncClient(cu).acquireLease(-1).block();
        } else {
            return leaseID;
        }
    }

    protected BlobServiceClient getOAuthServiceClient() {
        BlobServiceClientBuilder builder = new BlobServiceClientBuilder()
            .endpoint(ENVIRONMENT.getPrimaryAccount().getBlobEndpoint());

        instrument(builder);

        return setOauthCredentials(builder).buildClient();
    }

    protected BlobServiceAsyncClient getOAuthServiceAsyncClient() {
        BlobServiceClientBuilder builder = new BlobServiceClientBuilder()
            .endpoint(ENVIRONMENT.getPrimaryAccount().getBlobEndpoint());

        instrument(builder);

        return setOauthCredentials(builder).buildAsyncClient();
    }

    protected BlobServiceClientBuilder setOauthCredentials(BlobServiceClientBuilder builder) {
        if (ENVIRONMENT.getTestMode() != TestMode.PLAYBACK) {
            // AZURE_TENANT_ID, AZURE_CLIENT_ID, AZURE_CLIENT_SECRET
            return builder.credential(new EnvironmentCredentialBuilder().build());
        } else {
            // Running in playback, we don't have access to the AAD environment variables, just use SharedKeyCredential.
            return builder.credential(ENVIRONMENT.getPrimaryAccount().getCredential());
        }
    }

    protected BlobServiceClient getServiceClient(String endpoint) {
        return getServiceClient(null, endpoint, (HttpPipelinePolicy) null);
    }

    protected BlobServiceClient getServiceClient(TestAccount account) {
        return getServiceClient(account.getCredential(), account.getBlobEndpoint());
    }

    protected BlobServiceClient getServiceClient(StorageSharedKeyCredential credential, String endpoint) {
        return getServiceClient(credential, endpoint, (HttpPipelinePolicy) null);
    }

    protected BlobServiceClient getServiceClient(StorageSharedKeyCredential credential, String endpoint,
        HttpPipelinePolicy... policies) {
        return getServiceClientBuilder(credential, endpoint, policies)
            .buildClient();
    }

    protected BlobServiceClient getServiceClient(String sasToken, String endpoint) {
        return getServiceClientBuilder(null, endpoint, (HttpPipelinePolicy) null).sasToken(sasToken).buildClient();
    }

    protected BlobServiceAsyncClient getServiceAsyncClient(TestAccount account) {
        return getServiceClientBuilder(account.getCredential(), account.getBlobEndpoint()).buildAsyncClient();
    }

    protected BlobServiceAsyncClient getServiceAsyncClient(String sasToken, String endpoint) {
        return getServiceClientBuilder(null, endpoint, (HttpPipelinePolicy) null).sasToken(sasToken).buildAsyncClient();
    }

    protected BlobServiceAsyncClient getServiceAsyncClient(String endpoint) {
        return getServiceAsyncClient(null, endpoint, (HttpPipelinePolicy) null);
    }

    protected BlobServiceAsyncClient getServiceAsyncClient(StorageSharedKeyCredential credential, String endpoint,
                                                 HttpPipelinePolicy... policies) {
        return getServiceClientBuilder(credential, endpoint, policies)
            .buildAsyncClient();
    }

    protected BlobServiceClientBuilder getServiceClientBuilder(StorageSharedKeyCredential credential, String endpoint,
        HttpPipelinePolicy... policies) {
        BlobServiceClientBuilder builder = new BlobServiceClientBuilder()
            .endpoint(endpoint);

        for (HttpPipelinePolicy policy : policies) {
            if (policy != null) {
                builder.addPolicy(policy);
            }
        }

        instrument(builder);

        if (credential != null) {
            builder.credential(credential);
        }

        return builder;
    }

    protected BlobServiceClientBuilder getServiceClientBuilderWithTokenCredential(String endpoint,
        HttpPipelinePolicy... policies) {
        BlobServiceClientBuilder builder = new BlobServiceClientBuilder()
            .endpoint(endpoint);

        for (HttpPipelinePolicy policy : policies) {
            builder.addPolicy(policy);
        }

        if (ENVIRONMENT.getTestMode() != TestMode.PLAYBACK) {
            // AZURE_TENANT_ID, AZURE_CLIENT_ID, AZURE_CLIENT_SECRET
            builder.credential(new EnvironmentCredentialBuilder().build());
        } else {
            // Running in playback, we don't have access to the AAD environment variables, just use SharedKeyCredential.
            builder.credential(ENVIRONMENT.getPrimaryAccount().getCredential());
        }

        instrument(builder);
        return builder;
    }

    protected static BlobLeaseClient createLeaseClient(BlobClientBase blobClient) {
        return createLeaseClient(blobClient, null);
    }

    protected static BlobLeaseClient createLeaseClient(BlobClientBase blobClient, String leaseId) {
        return new BlobLeaseClientBuilder()
            .blobClient(blobClient)
            .leaseId(leaseId)
            .buildClient();
    }

    protected static BlobLeaseAsyncClient createLeaseAsyncClient(BlobAsyncClientBase blobAsyncClient) {
        return createLeaseAsyncClient(blobAsyncClient, null);
    }

    protected static BlobLeaseAsyncClient createLeaseAsyncClient(BlobAsyncClientBase blobAsyncClient, String leaseId) {
        return new BlobLeaseClientBuilder()
            .blobAsyncClient(blobAsyncClient)
            .leaseId(leaseId)
            .buildAsyncClient();
    }

    protected static BlobLeaseClient createLeaseClient(BlobContainerClient containerClient) {
        return createLeaseClient(containerClient, null);
    }

    protected static BlobLeaseClient createLeaseClient(BlobContainerClient containerClient, String leaseId) {
        return new BlobLeaseClientBuilder()
            .containerClient(containerClient)
            .leaseId(leaseId)
            .buildClient();
    }

    protected static BlobLeaseAsyncClient createLeaseAsyncClient(BlobContainerAsyncClient containerAsyncClient) {
        return createLeaseAsyncClient(containerAsyncClient, null);
    }

    protected static BlobLeaseAsyncClient createLeaseAsyncClient(BlobContainerAsyncClient containerAsyncClient,
        String leaseId) {
        return new BlobLeaseClientBuilder()
            .containerAsyncClient(containerAsyncClient)
            .leaseId(leaseId)
            .buildAsyncClient();
    }

    /**
     * Some tests require extra configuration for retries when writing.
     * <p>
     * It is possible that tests which upload a reasonable amount of data with tight resource limits may cause the
     * service to silently close a connection without returning a response due to high read latency (the resource
     * constraints cause a latency between sending the headers and writing the body often due to waiting for buffer pool
     * buffers). Without configuring a retry timeout, the operation will hang indefinitely. This is always something
     * that must be configured by the customer.
     * <p>
     * Typically this needs to be configured in retries so that we can retry the individual block writes rather than
     * the overall operation.
     * <p>
     * According to the following link, writes can take up to 10 minutes per MB before the service times out. In this
     * case, most of our instrumentation (e.g. CI pipelines) will timeout and fail anyway, so we don't want to wait that
     * long. The value is going to be a best guess and should be played with to allow test passes to succeed
     * <p>
     * https://docs.microsoft.com/en-us/rest/api/storageservices/setting-timeouts-for-blob-service-operations
     *
     * @param perRequestDataSize The amount of data expected to go out in each request. Will be used to calculate a
     * timeout value--about 20s/MB. Won't be less than 1 minute.
     */
    protected BlobServiceAsyncClient getPrimaryServiceClientForWrites(long perRequestDataSize) {
        int retryTimeout = Math.toIntExact((perRequestDataSize / (long) Constants.MB) * 20L);
        retryTimeout = Math.max(60, retryTimeout);
        return getServiceClientBuilder(ENVIRONMENT.getPrimaryAccount().getCredential(),
            ENVIRONMENT.getPrimaryAccount().getBlobEndpoint())
            .retryOptions(new RequestRetryOptions(null, null, retryTimeout, null, null, null))
            .buildAsyncClient();
    }

    protected BlobContainerClient getContainerClient(String sasToken, String endpoint) {
        return getContainerClientBuilder(endpoint).sasToken(sasToken).buildClient();
    }

    protected BlobContainerAsyncClient getContainerAsyncClient(String sasToken, String endpoint) {
        return getContainerClientBuilder(endpoint).sasToken(sasToken).buildAsyncClient();
    }

    protected BlobContainerClientBuilder getContainerClientBuilder(String endpoint) {
        BlobContainerClientBuilder builder = new BlobContainerClientBuilder().endpoint(endpoint);
        instrument(builder);
        return builder;
    }

    protected BlobContainerClientBuilder getContainerClientBuilderWithTokenCredential(String endpoint, HttpPipelinePolicy... policies) {
        BlobContainerClientBuilder builder = new BlobContainerClientBuilder()
            .endpoint(endpoint);

        for (HttpPipelinePolicy policy : policies) {
            builder.addPolicy(policy);
        }

        if (ENVIRONMENT.getTestMode() != TestMode.PLAYBACK) {
            // AZURE_TENANT_ID, AZURE_CLIENT_ID, AZURE_CLIENT_SECRET
            builder.credential(new EnvironmentCredentialBuilder().build());
        } else {
            // Running in playback, we don't have access to the AAD environment variables, just use SharedKeyCredential.
            builder.credential(ENVIRONMENT.getPrimaryAccount().getCredential());
        }

        instrument(builder);
        return builder;
    }

    protected BlobAsyncClient getBlobAsyncClient(StorageSharedKeyCredential credential, String endpoint, String blobName) {
        BlobClientBuilder builder = new BlobClientBuilder()
            .endpoint(endpoint)
            .blobName(blobName);

        instrument(builder);

        return builder.credential(credential).buildAsyncClient();
    }

    protected BlobAsyncClient getBlobAsyncClient(String sasToken, String endpoint, String blobName, String snapshotId) {
        BlobClientBuilder builder = new BlobClientBuilder()
            .endpoint(endpoint)
            .blobName(blobName)
            .snapshot(snapshotId);

        instrument(builder);

        return builder.sasToken(sasToken).buildAsyncClient();
    }

    protected BlobClient getBlobClient(String sasToken, String endpoint, String blobName) {
        return getBlobClient(sasToken, endpoint, blobName, null);
    }

    protected BlobClient getBlobClient(String sasToken, String endpoint, String blobName, String snapshotId) {
        BlobClientBuilder builder = new BlobClientBuilder()
            .endpoint(endpoint)
            .blobName(blobName)
            .snapshot(snapshotId);

        instrument(builder);

        return builder.sasToken(sasToken).buildClient();
    }

    protected BlobClient getBlobClient(StorageSharedKeyCredential credential, String endpoint, HttpPipelinePolicy... policies) {
        BlobClientBuilder builder = new BlobClientBuilder()
            .endpoint(endpoint);

        for (HttpPipelinePolicy policy : policies) {
            builder.addPolicy(policy);
        }

        instrument(builder);

        return builder.credential(credential).buildClient();
    }

    protected BlobClientBuilder getBlobClientBuilderWithTokenCredential(String endpoint, HttpPipelinePolicy... policies) {
        BlobClientBuilder builder = new BlobClientBuilder()
            .endpoint(endpoint);

        for (HttpPipelinePolicy policy : policies) {
            builder.addPolicy(policy);
        }

        if (ENVIRONMENT.getTestMode() != TestMode.PLAYBACK) {
            // AZURE_TENANT_ID, AZURE_CLIENT_ID, AZURE_CLIENT_SECRET
            builder.credential(new EnvironmentCredentialBuilder().build());
        } else {
            // Running in playback, we don't have access to the AAD environment variables, just use SharedKeyCredential.
            builder.credential(ENVIRONMENT.getPrimaryAccount().getCredential());
        }

        instrument(builder);
        return builder;
    }

    protected BlobAsyncClient getBlobAsyncClient(StorageSharedKeyCredential credential, String endpoint,
        HttpPipelinePolicy... policies) {
        BlobClientBuilder builder = new BlobClientBuilder()
            .endpoint(endpoint);

        for (HttpPipelinePolicy policy : policies) {
            builder.addPolicy(policy);
        }
        instrument(builder);
        return builder.credential(credential).buildAsyncClient();
    }

    protected BlobClient getBlobClient(StorageSharedKeyCredential credential, String endpoint, String blobName) {
        BlobClientBuilder builder = new BlobClientBuilder()
            .endpoint(endpoint)
            .blobName(blobName);

        instrument(builder);

        return builder.credential(credential).buildClient();
    }

    protected BlobClient getBlobClient(String endpoint, String sasToken) {
        BlobClientBuilder builder = new BlobClientBuilder()
            .endpoint(endpoint);
        instrument(builder);

        if (!CoreUtils.isNullOrEmpty(sasToken)) {
            builder.sasToken(sasToken);
        }
        return builder.buildClient();
    }

    protected BlobAsyncClient getBlobAsyncClient(String endpoint, String sasToken) {
        BlobClientBuilder builder = new BlobClientBuilder()
            .endpoint(endpoint);
        instrument(builder);

        if (!CoreUtils.isNullOrEmpty(sasToken)) {
            builder.sasToken(sasToken);
        }
        return builder.buildAsyncClient();
    }

    protected BlobClientBuilder getBlobClientBuilder(String endpoint) {
        BlobClientBuilder builder = new BlobClientBuilder()
            .endpoint(endpoint);

        builder.connectionString(ENVIRONMENT.getPrimaryAccount().getConnectionString());
        return instrument(builder);
    }

    protected SpecializedBlobClientBuilder getSpecializedBuilder(StorageSharedKeyCredential credential, String endpoint,
        HttpPipelinePolicy... policies) {
        SpecializedBlobClientBuilder builder = new SpecializedBlobClientBuilder()
            .endpoint(endpoint);

        for (HttpPipelinePolicy policy : policies) {
            builder.addPolicy(policy);
        }

        builder.credential(credential);
        return instrument(builder);
    }

    protected SpecializedBlobClientBuilder getSpecializedBuilder(String endpoint) {
        SpecializedBlobClientBuilder builder = new SpecializedBlobClientBuilder()
            .endpoint(endpoint);

        builder.connectionString(ENVIRONMENT.getPrimaryAccount().getConnectionString());
        return instrument(builder);
    }

    protected SpecializedBlobClientBuilder getSpecializedBuilderWithTokenCredential(String endpoint, HttpPipelinePolicy... policies) {
        SpecializedBlobClientBuilder builder = new SpecializedBlobClientBuilder()
            .endpoint(endpoint);

        for (HttpPipelinePolicy policy : policies) {
            builder.addPolicy(policy);
        }

        if (ENVIRONMENT.getTestMode() != TestMode.PLAYBACK) {
            // AZURE_TENANT_ID, AZURE_CLIENT_ID, AZURE_CLIENT_SECRET
            builder.credential(new EnvironmentCredentialBuilder().build());
        } else {
            // Running in playback, we don't have access to the AAD environment variables, just use SharedKeyCredential.
            builder.credential(ENVIRONMENT.getPrimaryAccount().getCredential());
        }

        instrument(builder);
        return builder;
    }

    protected HttpResponse getStubDownloadResponse(HttpResponse response, int code, Flux<ByteBuffer> body, HttpHeaders headers) {
        return new HttpResponse(response.getRequest()) {

            @Override
            public int getStatusCode() {
                return code;
            }

            @Override
            public String getHeaderValue(String s) {
                return headers.getValue(s);
            }

            @Override
            public HttpHeaders getHeaders() {
                return headers;
            }

            @Override
            public Flux<ByteBuffer> getBody() {
                return body;
            }

            @Override
            public Mono<byte[]> getBodyAsByteArray() {
                return Mono.just(new byte[0]);
            }

            @Override
            public Mono<String> getBodyAsString() {
                return Mono.just("");
            }

            @Override
            public Mono<String> getBodyAsString(Charset charset) {
                return Mono.just("");
            }
        };
    }

    /**
    * Validates the presence of headers that are present on a large number of responses. These headers are generally
    * random and can really only be checked as not null.
    * @param headers The object (may be headers object or response object) that has properties which expose these common headers.
    * @return Whether the header values are appropriate.
    */
    protected static boolean validateBasicHeaders(HttpHeaders headers) {
        return headers.getValue(HttpHeaderName.ETAG) != null
            // Quotes should be scrubbed from etag header values
            && !headers.getValue(HttpHeaderName.ETAG).contains("\"")
            && headers.getValue(LAST_MODIFIED) != null
            && headers.getValue(X_MS_REQUEST_ID) != null
            && headers.getValue(X_MS_VERSION) != null
            && headers.getValue(HttpHeaderName.DATE) != null;
    }

    protected static boolean validateBlobProperties(Response< BlobProperties > response, String cacheControl,
        String contentDisposition, String contentEncoding, String contentLanguage, byte[] contentMD5,
        String contentType) {
        return Objects.equals(response.getValue().getCacheControl(), cacheControl)
            && Objects.equals(response.getValue().getContentDisposition(), contentDisposition)
            && Objects.equals(response.getValue().getContentEncoding(), contentEncoding)
            && Objects.equals(response.getValue().getContentLanguage(), contentLanguage)
            && Arrays.equals(response.getValue().getContentMd5(), contentMD5)
            && Objects.equals(response.getValue().getContentType(), contentType);
    }

    /**
     * Insecurely and quickly generates a random AES256 key for the purpose of unit tests. No one should ever make a
     * real key this way.
     */
    protected byte[] getRandomKey() {
        return StorageCommonTestUtils.getRandomByteArray(32, testResourceNamer); // 256-bit key
    }

    protected void liveTestScenarioWithRetry(Runnable runnable) {
        if (!interceptorManager.isLiveMode()) {
            runnable.run();
            return;
        }

        int retry = 0;
        while (retry < 5) {
            try {
                runnable.run();
                break;
            } catch (Exception ex) {
                retry++;
                sleepIfRunningAgainstService(5000);
            }
        }
    }

    protected HttpPipelinePolicy getPerCallVersionPolicy() {
        return new PerCallVersionPolicy("2017-11-09");
    }

    // add this to BlobTestHelper class
    protected static void assertExceptionStatusCodeAndMessage(Throwable throwable, int expectedStatusCode,
        BlobErrorCode errMessage) {
        BlobStorageException exception = assertInstanceOf(BlobStorageException.class, throwable);
        assertEquals(expectedStatusCode, exception.getStatusCode());
        assertEquals(errMessage, exception.getErrorCode());
    }

    // add to TestHelper class
    protected static <T> Response<T> assertResponseStatusCode(Response<T> response, int expectedStatusCode) {
        assertEquals(expectedStatusCode, response.getStatusCode());
        return response;
    }

    protected static <T> void assertAsyncResponseStatusCode(Mono<Response<T>> response, int expectedStatusCode) {
        StepVerifier.create(response)
            .assertNext(r -> assertEquals(expectedStatusCode, r.getStatusCode()))
            .verifyComplete();
    }

    protected static Stream<Arguments> allConditionsSupplier() {
        return Stream.of(Arguments.of(null, null, null, null, null, null),
            Arguments.of(OLD_DATE, null, null, null, null, null),
            Arguments.of(null, NEW_DATE, null, null, null, null),
            Arguments.of(null, null, RECEIVED_ETAG, null, null, null),
            Arguments.of(null, null, null, GARBAGE_ETAG, null, null),
            Arguments.of(null, null, null, null, RECEIVED_LEASE_ID, null),
            Arguments.of(null, null, null, null, null, "\"foo\" = 'bar'"));
    }

    protected static Stream<Arguments> allConditionsFailSupplier() {
        return Stream.of(
            Arguments.of(NEW_DATE, null, null, null, null, null),
            Arguments.of(null, OLD_DATE, null, null, null, null),
            Arguments.of(null, null, GARBAGE_ETAG, null, null, null),
            Arguments.of(null, null, null, RECEIVED_ETAG, null, null),
            Arguments.of(null, null, null, null, GARBAGE_LEASE_ID, null),
            Arguments.of(null, null, null, null, null, "\"notfoo\" = 'notbar'"));
    }

    protected static Stream<Arguments> fileACFailSupplier() {
        return Stream.of(
            Arguments.of(NEW_DATE, null, null, null, null),
            Arguments.of(null, OLD_DATE, null, null, null),
            Arguments.of(null, null, GARBAGE_ETAG, null, null),
            Arguments.of(null, null, null, RECEIVED_ETAG, null),
            Arguments.of(null, null, null, null, GARBAGE_LEASE_ID)
        );
    }

    protected static String redactUrl(String url) {
        if (url == null) {
            return null;
        }

        return URL_SANITIZER.matcher(url).replaceAll("REDACTED");
    }

    protected HttpClient getHttpClient() {
        return StorageCommonTestUtils.getHttpClient(interceptorManager);
    }

    public static HttpClient getHttpClient(Supplier<HttpClient> playbackClientSupplier) {
        return StorageCommonTestUtils.getHttpClient(playbackClientSupplier);
    }

    protected  <T extends HttpTrait<T>, E extends Enum<E>> T instrument(T builder) {
        return StorageCommonTestUtils.instrument(builder, BlobServiceClientBuilder.getDefaultHttpLogOptions(),
            interceptorManager);
    }

    protected byte[] convertInputStreamToByteArray(InputStream inputStream) {
        return StorageCommonTestUtils.convertInputStreamToByteArray(inputStream);
    }

    protected boolean compareFiles(File file1, File file2, long offset, long count) throws IOException {
        return StorageCommonTestUtils.compareFiles(file1, file2, offset, count);
    }

    protected byte[] getRandomByteArray(int size) {
        return StorageCommonTestUtils.getRandomByteArray(size, testResourceNamer);
    }

    public ByteBuffer getRandomData(int size) {
        return StorageCommonTestUtils.getRandomData(size, testResourceNamer);
    }

    protected File getRandomFile(int size) throws IOException {
        return StorageCommonTestUtils.getRandomFile(size, testResourceNamer);
    }

    /*https://learn.microsoft.com/en-us/rest/api/storageservices/define-stored-access-policy#creating-or-modifying-a-stored-access-policy
    Second note, it can take up to 30 seconds to set/create an access policy and this was causing flakeyness in the live test pipeline
    */
    protected void setAccessPolicySleep(BlobContainerClient cc, PublicAccessType access,
                                        List<BlobSignedIdentifier> identifiers) {
        cc.setAccessPolicy(access, identifiers);
        sleepIfRunningAgainstService(30 * 1000);
    }

    protected void setAccessPolicySleepAsync(BlobContainerAsyncClient cc, PublicAccessType access,
                                        List<BlobSignedIdentifier> identifiers) {
        cc.setAccessPolicy(access, identifiers).block();
        sleepIfRunningAgainstService(30 * 1000);
    }
}
