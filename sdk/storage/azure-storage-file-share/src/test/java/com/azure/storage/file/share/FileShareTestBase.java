// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share;

import com.azure.core.client.traits.HttpTrait;
import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelinePosition;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.http.okhttp.OkHttpAsyncHttpClientBuilder;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.models.CustomMatcher;
import com.azure.core.test.models.TestProxySanitizer;
import com.azure.core.test.models.TestProxySanitizerType;
import com.azure.core.util.Context;
import com.azure.core.util.ServiceVersion;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.EnvironmentCredentialBuilder;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.test.shared.ServiceVersionValidationPolicy;
import com.azure.storage.common.test.shared.TestAccount;
import com.azure.storage.common.test.shared.TestDataFactory;
import com.azure.storage.common.test.shared.TestEnvironment;
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
import okhttp3.ConnectionPool;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.zip.CRC32;


public class FileShareTestBase extends TestProxyTestBase {
    protected static final TestEnvironment ENVIRONMENT = TestEnvironment.getInstance();
    protected static final TestDataFactory DATA = TestDataFactory.getInstance();
    private static final HttpClient NETTY_HTTP_CLIENT = new NettyAsyncHttpClientBuilder().build();
    private static final HttpClient OK_HTTP_CLIENT = new OkHttpAsyncHttpClientBuilder()
        .connectionPool(new ConnectionPool(50, 5, TimeUnit.MINUTES))
        .build();

    protected static final HttpHeaderName X_MS_VERSION = HttpHeaderName.fromString("x-ms-version");
    protected static final HttpHeaderName X_MS_REQUEST_ID = HttpHeaderName.fromString("x-ms-request-id");

    private static final ClientLogger LOGGER = new ClientLogger(FileShareTestBase.class);

    protected String prefix;

    private int entityNo = 0; // Used to generate stable share names for recording tests requiring multiple shares.

    /*
    Note that this value is only used to check if we are depending on the received etag. This value will not actually
    be used.
    */
    protected static final String RECEIVED_LEASE_ID = "received";
    static final String GARBAGE_LEASE_ID = UUID.randomUUID().toString();

    URL testFolder = getClass().getClassLoader().getResource("testfiles");


    // Clients for API tests
    protected ShareServiceClient primaryFileServiceClient;
    protected ShareServiceAsyncClient primaryFileServiceAsyncClient;
    protected ShareServiceClient premiumFileServiceClient;
    protected ShareServiceAsyncClient premiumFileServiceAsyncClient;

    @Override
    public void beforeTest() {
        super.beforeTest();
        prefix = getCrc32(testContextManager.getTestPlaybackRecordingName());

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
            .setIgnoredQueryParameters(Arrays.asList("sv"))));

        primaryFileServiceClient = getServiceClient(ENVIRONMENT.getPrimaryAccount());
        primaryFileServiceAsyncClient = getServiceAsyncClient(ENVIRONMENT.getPrimaryAccount());

        premiumFileServiceClient = getServiceClient(ENVIRONMENT.getPremiumFileAccount());
        premiumFileServiceAsyncClient = getServiceAsyncClient(ENVIRONMENT.getPremiumFileAccount());
    }

    private static String getCrc32(String input) {
        CRC32 crc32 = new CRC32();
        crc32.update(input.getBytes(StandardCharsets.UTF_8));
        return String.format(Locale.US, "%08X", crc32.getValue()).toLowerCase();
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
        long seed = UUID.fromString(testResourceNamer.randomUuid()).getMostSignificantBits() & Long.MAX_VALUE;
        Random rand = new Random(seed);
        byte[] data = new byte[size];
        rand.nextBytes(data);
        return data;
    }

    protected ShareServiceAsyncClient getServiceAsyncClient(TestAccount account) {
        return getServiceAsyncClient(account.getCredential(), account.getFileEndpoint(), (HttpPipelinePolicy) null);
    }

    protected ShareServiceAsyncClient getServiceAsyncClient(StorageSharedKeyCredential credential, String endpoint,
        HttpPipelinePolicy... policies) {
        return getServiceClientBuilder(credential, endpoint, policies).buildAsyncClient();
    }

    protected ShareServiceClient getServiceClient(TestAccount account) {
        return getServiceClient(account.getCredential(), account.getFileEndpoint(), (HttpPipelinePolicy) null);
    }

    protected ShareServiceClient getServiceClient(StorageSharedKeyCredential credential, String endpoint,
        HttpPipelinePolicy... policies) {
        return getServiceClientBuilder(credential, endpoint, policies).buildClient();
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

    protected ShareFileClientBuilder getFileClientBuilderWithTokenCredential(String endpoint,
                                                                             HttpPipelinePolicy... policies) {
        ShareFileClientBuilder builder = new ShareFileClientBuilder().endpoint(endpoint);
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


    protected static ShareLeaseAsyncClient createLeaseClient(ShareAsyncClient shareClient) {
        return createLeaseClient(shareClient, null);
    }

    protected static ShareLeaseAsyncClient createLeaseClient(ShareAsyncClient shareClient, String leaseId) {
        return new ShareLeaseClientBuilder()
            .shareAsyncClient(shareClient)
            .leaseId(leaseId)
            .buildAsyncClient();
    }

    protected ShareServiceClient getOAuthServiceClient(ShareServiceClientBuilder builder) {
        if (builder == null) {
            builder = new ShareServiceClientBuilder();
        }
        builder.endpoint(ENVIRONMENT.getPrimaryAccount().getFileEndpoint());

        instrument(builder);

        return setOauthCredentials(builder).buildClient();
    }

    protected ShareServiceClientBuilder setOauthCredentials(ShareServiceClientBuilder builder) {
        if (ENVIRONMENT.getTestMode() != TestMode.PLAYBACK) {
            // AZURE_TENANT_ID, AZURE_CLIENT_ID, AZURE_CLIENT_SECRET
            return builder.credential(new EnvironmentCredentialBuilder().build());
        } else {
            // Running in playback, we don't have access to the AAD environment variables, just use SharedKeyCredential.
            return builder.credential(ENVIRONMENT.getPrimaryAccount().getCredential());
        }
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

        return setOauthCredentials(builder);
    }


    protected ShareClientBuilder setOauthCredentials(ShareClientBuilder builder) {
        if (ENVIRONMENT.getTestMode() != TestMode.PLAYBACK) {
            // AZURE_TENANT_ID, AZURE_CLIENT_ID, AZURE_CLIENT_SECRET
            return builder.credential(new EnvironmentCredentialBuilder().build());
        } else {
            // Running in playback, we don't have access to the AAD environment variables, just use SharedKeyCredential.
            return builder.credential(ENVIRONMENT.getPrimaryAccount().getCredential());
        }
    }

    @SuppressWarnings("unchecked")
    protected <T extends HttpTrait<T>, E extends Enum<E>> T instrument(T builder) {
        // Groovy style reflection. All our builders follow this pattern.
        builder.httpClient(getHttpClient());

        if (interceptorManager.isRecordMode()) {
            builder.addPolicy(interceptorManager.getRecordPolicy());
        }

        if (ENVIRONMENT.getServiceVersion() != null) {
            try {
                Method serviceVersionMethod = Arrays.stream(builder.getClass().getDeclaredMethods())
                    .filter(method -> "serviceVersion".equals(method.getName())
                        && method.getParameterCount() == 1
                        && ServiceVersion.class.isAssignableFrom(method.getParameterTypes()[0]))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Unable to find serviceVersion method for builder: "
                        + builder.getClass()));
                Class<E> serviceVersionClass = (Class<E>) serviceVersionMethod.getParameterTypes()[0];
                ServiceVersion serviceVersion = (ServiceVersion) Enum.valueOf(serviceVersionClass,
                    ENVIRONMENT.getServiceVersion());
                serviceVersionMethod.invoke(builder, serviceVersion);
                builder.addPolicy(new ServiceVersionValidationPolicy(serviceVersion.getVersion()));
            } catch (ReflectiveOperationException ex) {
                throw new RuntimeException(ex);
            }
        }

        return builder;
    }

    protected HttpClient getHttpClient() {
        if (ENVIRONMENT.getTestMode() != TestMode.PLAYBACK) {
            switch (ENVIRONMENT.getHttpClientType()) {
                case NETTY:
                    return NETTY_HTTP_CLIENT;
                case OK_HTTP:
                    return OK_HTTP_CLIENT;
                default:
                    throw new IllegalArgumentException("Unknown http client type: " + ENVIRONMENT.getHttpClientType());
            }
        } else {
            return interceptorManager.getPlaybackClient();
        }
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

    // Only sleep if test is running in live or record mode
    protected void sleepIfRecord(long milliseconds) {
        try {
            if (ENVIRONMENT.getTestMode() != TestMode.PLAYBACK) {
                Thread.sleep(milliseconds);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Temporary utility method that waits if running against a live service.
     * <p>
     * Remove this once {@code azure-core-test} has a static equivalent of
     * {@link TestBase#sleepIfRunningAgainstService(long)}.
     *
     * @param sleepMillis Milliseconds to sleep.
     */
    public static void sleepIfLiveTesting(long sleepMillis) {
        if (ENVIRONMENT.getTestMode() == TestMode.PLAYBACK) {
            return;
        }

        try {
            Thread.sleep(sleepMillis);
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
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

    protected static String getAuthToken() {
        if (ENVIRONMENT.getTestMode() == TestMode.PLAYBACK) {
            // we just need some string to satisfy SDK for playback mode. Recording framework handles this fine.
            return "recordingBearerToken";
        }
        List<String> scopes = new ArrayList<>();
        scopes.add("https://storage.azure.com/.default");
        return new EnvironmentCredentialBuilder().build().getToken(new TokenRequestContext().setScopes(scopes)).map(AccessToken::getToken).block();
    }

    protected HttpPipelinePolicy getPerCallVersionPolicy() {
        return new HttpPipelinePolicy() {
            @Override
            public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
                context.getHttpRequest().setHeader(X_MS_VERSION, "2017-11-09");
                return next.process();
            }
            @Override
            public HttpPipelinePosition getPipelinePosition() {
                return HttpPipelinePosition.PER_CALL;
            }
        };
    }

    /**
     * Injects one retry-able IOException failure per url.
     */
    protected class TransientFailureInjectingHttpPipelinePolicy implements HttpPipelinePolicy {

        private ConcurrentHashMap<String, Boolean> failureTracker = new ConcurrentHashMap<>();

        @Override
        public Mono<HttpResponse> process(HttpPipelineCallContext httpPipelineCallContext,
            HttpPipelineNextPolicy httpPipelineNextPolicy) {
            HttpRequest request = httpPipelineCallContext.getHttpRequest();
            String key = request.getUrl().toString();
            // Make sure that failure happens once per url.
            if (failureTracker.getOrDefault(key, false)) {
                return httpPipelineNextPolicy.process();
            } else {
                failureTracker.put(key, true);
                return request.getBody().flatMap(byteBuffer -> {
                    // Read a byte from each buffer to simulate that failure occurred in the middle of transfer.
                    byteBuffer.get();
                    return Flux.just(byteBuffer);
                }).reduce(0L, (a, byteBuffer) -> a + byteBuffer.remaining()).flatMap(aLong -> {
                    // Throw retry-able error.
                    return Mono.error(new IOException("KABOOM!"));
                });
            }
        }
    }

    protected static boolean olderThan20190707ServiceVersion() {
        return olderThan(ShareServiceVersion.V2019_07_07);
    }

    protected static boolean olderThan20191212ServiceVersion() {
        return olderThan(ShareServiceVersion.V2019_12_12);
    }

    protected static boolean olderThan20200210ServiceVersion() {
        return olderThan(ShareServiceVersion.V2020_02_10);
    }

    protected static boolean olderThan20201002ServiceVersion() {
        return olderThan(ShareServiceVersion.V2020_10_02);
    }

    protected static boolean olderThan20210212ServiceVersion() {
        return olderThan(ShareServiceVersion.V2021_02_12);
    }

    protected static boolean olderThan20210608ServiceVersion() {
        return olderThan(ShareServiceVersion.V2021_06_08);
    }

    protected static boolean olderThan20210410ServiceVersion() {
        return olderThan(ShareServiceVersion.V2021_04_10);
    }

    protected static boolean olderThan20211202ServiceVersion() {
        return olderThan(ShareServiceVersion.V2021_12_02);
    }

    protected static boolean olderThan20221102ServiceVersion() {
        return olderThan(ShareServiceVersion.V2022_11_02);
    }

    protected static boolean olderThan20230103ServiceVersion() {
        return olderThan(ShareServiceVersion.V2023_01_03);
    }

    protected static boolean olderThan(ShareServiceVersion targetVersion) {
        String targetServiceVersionFromEnvironment = ENVIRONMENT.getServiceVersion();
        ShareServiceVersion version = (targetServiceVersionFromEnvironment != null)
            ? Enum.valueOf(ShareServiceVersion.class, targetServiceVersionFromEnvironment)
            : ShareServiceVersion.getLatest();

        return version.ordinal() < targetVersion.ordinal();
    }

    public static boolean isLiveMode() {
        return ENVIRONMENT.getTestMode() == TestMode.LIVE;
    }

    protected Duration getPollingDuration(long liveTestDurationInMillis) {
        return (ENVIRONMENT.getTestMode() == TestMode.PLAYBACK) ? Duration.ofMillis(10)
            : Duration.ofMillis(liveTestDurationInMillis);
    }

    protected static boolean isServiceVersionSpecified() {
        return ENVIRONMENT.getServiceVersion() != null;
    }

    protected static boolean isPlaybackMode() {
        return ENVIRONMENT.getTestMode() == TestMode.PLAYBACK;
    }
}
