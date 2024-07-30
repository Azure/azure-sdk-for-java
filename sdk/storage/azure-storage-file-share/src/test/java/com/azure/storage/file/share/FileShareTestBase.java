// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share;

import com.azure.core.client.traits.HttpTrait;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.test.TestMode;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.models.CustomMatcher;
import com.azure.core.test.models.TestProxySanitizer;
import com.azure.core.test.models.TestProxySanitizerType;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.test.shared.StorageCommonTestUtils;
import com.azure.storage.common.test.shared.TestAccount;
import com.azure.storage.common.test.shared.TestDataFactory;
import com.azure.storage.common.test.shared.TestEnvironment;
import com.azure.storage.common.test.shared.policy.PerCallVersionPolicy;
import com.azure.storage.file.share.models.LeaseStateType;
import com.azure.storage.file.share.models.ListSharesOptions;
import com.azure.storage.file.share.models.ShareItem;
import com.azure.storage.file.share.models.ShareSnapshotsDeleteOptionType;
import com.azure.storage.file.share.options.ShareAcquireLeaseOptions;
import com.azure.storage.file.share.options.ShareBreakLeaseOptions;
import com.azure.storage.file.share.options.ShareDeleteOptions;
import com.azure.storage.file.share.specialized.ShareLeaseAsyncClient;
import com.azure.storage.file.share.specialized.ShareLeaseClient;
import com.azure.storage.file.share.specialized.ShareLeaseClientBuilder;

import java.net.URL;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;


public class FileShareTestBase extends TestProxyTestBase {
    protected static final TestEnvironment ENVIRONMENT = TestEnvironment.getInstance();
    protected static final TestDataFactory DATA = TestDataFactory.getInstance();

    protected static final HttpHeaderName X_MS_VERSION = HttpHeaderName.fromString("x-ms-version");
    protected static final HttpHeaderName X_MS_REQUEST_ID = HttpHeaderName.fromString("x-ms-request-id");

    protected String prefix;

    private int entityNo = 0; // Used to generate stable share names for recording tests requiring multiple shares.

    /*
    Note that this value is only used to check if we are depending on the received etag. This value will not actually
    be used.
    */
    protected static final String RECEIVED_LEASE_ID = "received";
    static final String GARBAGE_LEASE_ID = CoreUtils.randomUuid().toString();

    URL testFolder = getClass().getClassLoader().getResource("testfiles");


    // Clients for API tests
    protected ShareServiceClient primaryFileServiceClient;
    protected ShareServiceAsyncClient primaryFileServiceAsyncClient;
    protected ShareServiceClient premiumFileServiceClient;
    protected ShareServiceAsyncClient premiumFileServiceAsyncClient;

    @Override
    public void beforeTest() {
        super.beforeTest();
        prefix = StorageCommonTestUtils.getCrc32(testContextManager.getTestPlaybackRecordingName());

        if (getTestMode() != TestMode.LIVE) {
            interceptorManager.addSanitizers(Arrays.asList(
                new TestProxySanitizer("sig=(.*)", "REDACTED", TestProxySanitizerType.URL),
                new TestProxySanitizer("x-ms-file-rename-source", ".*", "REDACTED", TestProxySanitizerType.HEADER),
                new TestProxySanitizer("x-ms-copy-source", "sig=(.*)", "REDACTED", TestProxySanitizerType.HEADER),
                new TestProxySanitizer("x-ms-copy-source-authorization", ".*", "REDACTED", TestProxySanitizerType.HEADER),
                new TestProxySanitizer("x-ms-file-rename-source-authorization", ".*", "REDACTED", TestProxySanitizerType.HEADER)));
        }

        // Ignore changes to the order of query parameters and wholly ignore the 'sv' (service version) query parameter
        // in SAS tokens.
        interceptorManager.addMatchers(Collections.singletonList(new CustomMatcher()
            .setComparingBodies(false)
            .setHeadersKeyOnlyMatch(Arrays.asList("x-ms-lease-id", "x-ms-proposed-lease-id", "If-Modified-Since",
                "If-Unmodified-Since", "x-ms-expiry-time", "x-ms-source-if-modified-since", "x-ms-copy-source",
                "x-ms-file-rename-source", "x-ms-source-if-unmodified-since", "x-ms-source-lease-id",
                "x-ms-encryption-key-sha256"))
            .setQueryOrderingIgnored(true)
            .setIgnoredQueryParameters(Arrays.asList("sv"))
            .setExcludedHeaders(Collections.singletonList("x-ms-meta-testmetadata"))));

        ShareServiceClientBuilder builder = getServiceClientBuilder(ENVIRONMENT.getPrimaryAccount());
        primaryFileServiceClient = builder.buildClient();
        primaryFileServiceAsyncClient = builder.buildAsyncClient();

        builder = getServiceClientBuilder(ENVIRONMENT.getPremiumFileAccount());
        premiumFileServiceClient = builder.buildClient();
        premiumFileServiceAsyncClient = builder.buildAsyncClient();
    }

    /**
     * Clean up the test shares, directories and files for the account.
     */
    @Override
    protected void afterTest() {
        super.afterTest();
        if (getTestMode() == TestMode.PLAYBACK) {
            return;
        }

        ShareServiceClient cleanupFileServiceClient = new ShareServiceClientBuilder()
            .httpClient(getHttpClient())
            .connectionString(getPrimaryConnectionString())
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .buildClient();
        for (ShareItem share : cleanupFileServiceClient.listShares(new ListSharesOptions().setPrefix(prefix), null,
            Context.NONE)) {
            ShareClient shareClient = cleanupFileServiceClient.getShareClient(share.getName());
            if (share.getProperties().getLeaseState() == LeaseStateType.LEASED) {
                createLeaseClient(shareClient).breakLeaseWithResponse(new ShareBreakLeaseOptions()
                    .setBreakPeriod(Duration.ofSeconds(0)), null, null);
            }
            shareClient.deleteWithResponse(new ShareDeleteOptions()
                .setDeleteSnapshotsOptions(ShareSnapshotsDeleteOptionType.INCLUDE), null, null);
        }
    }

    protected String generateShareName() {
        return generateResourceName(entityNo++);
    }

    protected String generatePathName() {
        return generateResourceName(entityNo++);
    }

    protected String generateResourceName(int entityNo) {
        return testResourceNamer.randomName(prefix + entityNo, 63);
    }

    byte[] getRandomByteArray(int size) {
        return StorageCommonTestUtils.getRandomByteArray(size, testResourceNamer);
    }

    protected ShareServiceClientBuilder getServiceClientBuilder(TestAccount account) {
        return getServiceClientBuilder(account.getCredential(), account.getFileEndpoint(), (HttpPipelinePolicy) null);
    }

    protected ShareServiceClientBuilder fileServiceBuilderHelper() {
        ShareServiceClientBuilder shareServiceClientBuilder = instrument(new ShareServiceClientBuilder());
        return shareServiceClientBuilder
            .connectionString(ENVIRONMENT.getPrimaryAccount().getConnectionString());
    }

    protected ShareServiceClientBuilder getServiceClientBuilder(StorageSharedKeyCredential credential, String endpoint,
        HttpPipelinePolicy... policies) {
        ShareServiceClientBuilder builder = new ShareServiceClientBuilder().endpoint(endpoint);
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

    protected ShareClientBuilder getShareClientBuilder(String endpoint) {
        ShareClientBuilder builder = new ShareClientBuilder().endpoint(endpoint);
        instrument(builder);
        return builder;
    }

    protected ShareClientBuilder shareBuilderHelper(final String shareName) {
        return shareBuilderHelper(shareName, null);
    }

    protected ShareClient getShareClient(final String shareName, Boolean allowTrailingDot, Boolean allowSourceTrailingDot) {
        ShareClientBuilder builder = shareBuilderHelper(shareName, null);
        if (allowTrailingDot != null) {
            builder.allowTrailingDot(allowTrailingDot);
        }
        if (allowSourceTrailingDot != null) {
            builder.allowSourceTrailingDot(allowSourceTrailingDot);
        }
        return builder.buildClient();
    }

    protected ShareClientBuilder shareBuilderHelper(final String shareName, final String snapshot) {
        ShareClientBuilder builder = instrument(new ShareClientBuilder());
        return builder.connectionString(ENVIRONMENT.getPrimaryAccount().getConnectionString())
            .shareName(shareName)
            .snapshot(snapshot)
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS));
    }

    protected ShareFileClientBuilder directoryBuilderHelper(final String shareName, final String directoryPath) {
        ShareFileClientBuilder builder = instrument(new ShareFileClientBuilder());
        return builder.connectionString(ENVIRONMENT.getPrimaryAccount().getConnectionString())
            .shareName(shareName)
            .resourcePath(directoryPath);
    }

    protected ShareDirectoryClient getDirectoryClient(StorageSharedKeyCredential credential, String endpoint,
        HttpPipelinePolicy... policies) {
        ShareFileClientBuilder builder = getFileClientBuilder(endpoint, policies).credential(credential);
        return builder.buildDirectoryClient();
    }

    protected ShareDirectoryClient getDirectoryClient(String sasToken, String endpoint, HttpPipelinePolicy... policies) {
        ShareFileClientBuilder builder = getFileClientBuilder(endpoint, policies).sasToken(sasToken);
        return builder.buildDirectoryClient();
    }

    protected ShareFileClientBuilder fileBuilderHelper(final String shareName, final String filePath) {
        ShareFileClientBuilder builder = instrument(new ShareFileClientBuilder());
        return builder
            .connectionString(ENVIRONMENT.getPrimaryAccount().getConnectionString())
            .shareName(shareName)
            .resourcePath(filePath);
    }

    protected ShareFileClient getFileClient(StorageSharedKeyCredential credential, String endpoint,
        HttpPipelinePolicy... policies) {
        ShareFileClientBuilder builder = getFileClientBuilder(endpoint, policies);
        if (credential != null) {
            builder.credential(credential);
        }
        return builder.buildFileClient();
    }

    protected ShareFileClient getFileClient(String sasToken, String endpoint, HttpPipelinePolicy... policies) {
        ShareFileClientBuilder builder = getFileClientBuilder(endpoint, policies);
        if (sasToken != null) {
            builder.sasToken(sasToken);
        }
        return builder.buildFileClient();
    }

    protected ShareFileClient getFileClient(final String shareName, final String fileName, Boolean allowTrailingDot,
        Boolean allowSourceTrailingDot) {
        ShareFileClientBuilder builder = fileBuilderHelper(shareName, fileName);
        if (allowTrailingDot != null) {
            builder.allowTrailingDot(allowTrailingDot);
        }
        if (allowSourceTrailingDot != null) {
            builder.allowSourceTrailingDot(allowSourceTrailingDot);
        }
        return builder.buildFileClient();
    }

    protected ShareFileClientBuilder getFileClientBuilder(String endpoint, HttpPipelinePolicy... policies) {
        ShareFileClientBuilder builder = new ShareFileClientBuilder().endpoint(endpoint);
        for (HttpPipelinePolicy policy : policies) {
            builder.addPolicy(policy);
        }
        instrument(builder);
        return builder;
    }

    protected static ShareLeaseClient createLeaseClient(ShareFileClient fileClient) {
        return createLeaseClient(fileClient, null);
    }

    protected static ShareLeaseClient createLeaseClient(ShareFileClient fileClient, String leaseId) {
        return new ShareLeaseClientBuilder()
            .fileClient(fileClient)
            .leaseId(leaseId)
            .buildClient();
    }

    protected static ShareLeaseAsyncClient createLeaseClient(ShareFileAsyncClient fileClient) {
        return createLeaseClient(fileClient, null);
    }

    protected static ShareLeaseAsyncClient createLeaseClient(ShareFileAsyncClient fileClient, String leaseId) {
        return new ShareLeaseClientBuilder()
            .fileAsyncClient(fileClient)
            .leaseId(leaseId)
            .buildAsyncClient();
    }

    protected static ShareLeaseClient createLeaseClient(ShareClient shareClient) {
        return createLeaseClient(shareClient, null);
    }

    protected static ShareLeaseClient createLeaseClient(ShareClient shareClient, String leaseId) {
        return new ShareLeaseClientBuilder()
            .shareClient(shareClient)
            .leaseId(leaseId)
            .buildClient();
    }

    protected ShareServiceClient getOAuthServiceClient(ShareServiceClientBuilder builder) {
        if (builder == null) {
            builder = new ShareServiceClientBuilder();
        }
        builder.endpoint(ENVIRONMENT.getPrimaryAccount().getFileEndpoint());

        instrument(builder);

        return builder.credential(StorageCommonTestUtils.getTokenCredential(interceptorManager)).buildClient();
    }

    protected ShareServiceClient getOAuthServiceClientSharedKey(ShareServiceClientBuilder builder) {
        if (builder == null) {
            builder = new ShareServiceClientBuilder();
        }
        builder.endpoint(ENVIRONMENT.getPrimaryAccount().getFileEndpoint());

        instrument(builder);

        return builder.credential(StorageCommonTestUtils.getTokenCredential(interceptorManager)).buildClient();
    }

    protected ShareServiceAsyncClient getOAuthServiceAsyncClient(ShareServiceClientBuilder builder) {
        if (builder == null) {
            builder = new ShareServiceClientBuilder();
        }
        builder.endpoint(ENVIRONMENT.getPrimaryAccount().getFileEndpoint());

        instrument(builder);

        return builder.credential(StorageCommonTestUtils.getTokenCredential(interceptorManager)).buildAsyncClient();
    }

    protected ShareServiceAsyncClient getOAuthServiceClientAsyncSharedKey(ShareServiceClientBuilder builder) {
        if (builder == null) {
            builder = new ShareServiceClientBuilder();
        }
        builder.endpoint(ENVIRONMENT.getPrimaryAccount().getFileEndpoint());

        instrument(builder);

        return builder.credential(ENVIRONMENT.getPrimaryAccount().getCredential()).buildAsyncClient();
    }

    protected ShareClient getOAuthShareClient(ShareClientBuilder builder) {
        return getOAuthShareClientBuilder(builder).buildClient();
    }

    protected ShareClientBuilder getOAuthShareClientBuilder(ShareClientBuilder builder) {
        if (builder == null) {
            builder = new ShareClientBuilder();
        }
        builder.endpoint(ENVIRONMENT.getPrimaryAccount().getFileEndpoint());

        instrument(builder);

        return builder.credential(StorageCommonTestUtils.getTokenCredential(interceptorManager));
    }

    protected <T extends HttpTrait<T>, E extends Enum<E>> T instrument(T builder) {
        return StorageCommonTestUtils.instrument(builder, ShareFileClientBuilder.getDefaultHttpLogOptions(),
            interceptorManager);
    }

    protected HttpClient getHttpClient() {
        return StorageCommonTestUtils.getHttpClient(interceptorManager);
    }

    protected String getPrimaryConnectionString() {
        return ENVIRONMENT.getPrimaryAccount().getConnectionString();
    }

    /**
     * This helper method will acquire a lease on a blob to prepare for testing lease ID. We want to test
     * against a valid lease in both the success and failure cases to guarantee that the results actually indicate
     * proper setting of the header. If we pass null, though, we don't want to acquire a lease, as that will interfere
     * with other AC tests.
     *
     * @param fc The blob on which to acquire a lease.
     * @param leaseID The signalID. Values should only ever be {@code receivedLeaseID}, {@code garbageLeaseID}, or
     * {@code null}.
     * @return The actual lease ID of the blob if recievedLeaseID is passed, otherwise whatever was passed will be
     * returned.
     */
    protected String setupFileLeaseCondition(ShareFileClient fc, String leaseID) {
        String responseLeaseId = null;
        if (Objects.equals(leaseID, RECEIVED_LEASE_ID) || Objects.equals(leaseID, GARBAGE_LEASE_ID)) {
            responseLeaseId = createLeaseClient(fc).acquireLease();
        }
        if (Objects.equals(leaseID, RECEIVED_LEASE_ID)) {
            return responseLeaseId;
        } else {
            return leaseID;
        }
    }

    /**
     * Validates the presence of headers that are present on a large number of responses. These headers are generally
     * random and can really only be checked as not null.
     * @param headers The object (may be headers object or response object) that has properties which expose these
     * common headers.
     * @return Whether or not the header values are appropriate.
     */
    protected boolean validateBasicHeaders(HttpHeaders headers) {
        return headers.getValue(HttpHeaderName.ETAG) != null
            // Quotes should be scrubbed from etag header values
            && !headers.getValue(HttpHeaderName.ETAG).contains("\"")
            && headers.getValue(HttpHeaderName.LAST_MODIFIED) != null
            && headers.getValue(X_MS_REQUEST_ID) != null
            && headers.getValue(X_MS_VERSION) != null
            && headers.getValue(HttpHeaderName.DATE) != null;
    }

    protected String setupShareLeaseCondition(ShareClient sc, String leaseID) {
        if (Objects.equals(leaseID, RECEIVED_LEASE_ID)) {
            return createLeaseClient(sc)
                .acquireLeaseWithResponse(new ShareAcquireLeaseOptions().setDuration(-1), null, null).getValue();
        } else {
            return leaseID;
        }
    }

    @SuppressWarnings("unchecked")
    public static <T, E extends Exception> T retry(Supplier<T> action, Predicate<E> retryPredicate, int times,
        Duration delay) throws E, InterruptedException {
        for (int i = 0; i < times; i++) {
            try {
                return action.get();
            } catch (Exception e) {
                if (!retryPredicate.test((E) e)) {
                    throw e;
                } else {
                    Thread.sleep(delay.toMillis());
                }
            }
        }
        // Or handle the case when all retries are exhausted
        return null;
    }

    protected String getAuthToken() {
        if (ENVIRONMENT.getTestMode() == TestMode.PLAYBACK) {
            // we just need some string to satisfy SDK for playback mode. Recording framework handles this fine.
            return "recordingBearerToken";
        }
        return StorageCommonTestUtils.getTokenCredential(interceptorManager).getTokenSync(new TokenRequestContext()
                .setScopes(Collections.singletonList("https://storage.azure.com/.default"))).getToken();
    }

    protected HttpPipelinePolicy getPerCallVersionPolicy() {
        return new PerCallVersionPolicy("2017-11-09");
    }

    protected static boolean isServiceVersionSpecified() {
        return ENVIRONMENT.getServiceVersion() != null;
    }
}
