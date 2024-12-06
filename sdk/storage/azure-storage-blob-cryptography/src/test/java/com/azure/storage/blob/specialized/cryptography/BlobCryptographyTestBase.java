// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized.cryptography;

import com.azure.core.client.traits.HttpTrait;
import com.azure.core.credential.TokenCredential;
import com.azure.core.cryptography.AsyncKeyEncryptionKey;
import com.azure.core.cryptography.AsyncKeyEncryptionKeyResolver;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.BearerTokenAuthenticationPolicy;
import com.azure.core.http.policy.ExponentialBackoff;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.HttpPolicyProviders;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.RetryStrategy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.http.rest.Response;
import com.azure.core.test.TestMode;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.models.CustomMatcher;
import com.azure.core.test.models.TestProxySanitizer;
import com.azure.core.test.models.TestProxySanitizerType;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.security.keyvault.keys.KeyServiceVersion;
import com.azure.security.keyvault.keys.cryptography.models.KeyWrapAlgorithm;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobClientBuilder;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobContainerItem;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.models.ListBlobContainersOptions;
import com.azure.storage.blob.specialized.BlobLeaseClient;
import com.azure.storage.blob.specialized.BlobLeaseClientBuilder;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.test.shared.StorageCommonTestUtils;
import com.azure.storage.common.test.shared.TestAccount;
import com.azure.storage.common.test.shared.TestDataFactory;
import com.azure.storage.common.test.shared.TestEnvironment;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import static com.azure.core.test.utils.TestUtils.assertArraysEqual;
import static com.azure.core.test.utils.TestUtils.assertByteBuffersEqual;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class BlobCryptographyTestBase extends TestProxyTestBase {
    protected static final TestEnvironment ENV = TestEnvironment.getInstance();
    protected static final TestDataFactory DATA = TestDataFactory.getInstance();

    protected static final OffsetDateTime OLD_DATE = OffsetDateTime.now().minusDays(1);
    protected static final OffsetDateTime NEW_DATE = OffsetDateTime.now().plusDays(1);
    protected static final String RECEIVED_ETAG = "received";
    protected static final String GARBAGE_ETAG = "garbage";
    protected static final String RECEIVED_LEASE_ID = "received";
    protected static final String GARBAGE_LEASE_ID = CoreUtils.randomUuid().toString();
    protected static final byte[] MOCK_RANDOM_DATA
        = String.join("", Collections.nCopies(32, "password")).getBytes(StandardCharsets.UTF_8);
    private static final byte[] MOCK_KEY
        = String.join("", Collections.nCopies(4, "password")).getBytes(StandardCharsets.UTF_8);

    private int entityNo = 0; // Used to generate stable container names for recording tests requiring multiple containers.
    protected String prefix;

    @Override
    protected void beforeTest() {
        super.beforeTest();
        prefix = StorageCommonTestUtils.getCrc32(testContextManager.getTestPlaybackRecordingName());

        if (getTestMode() != TestMode.LIVE) {
            interceptorManager.addSanitizers(Arrays.asList(
                new TestProxySanitizer("x-ms-encryption-key", ".*", "REDACTED", TestProxySanitizerType.HEADER),
                new TestProxySanitizer("x-ms-encryption-key-sha256", ".*", "REDACTED", TestProxySanitizerType.HEADER)));
        }

        // Ignore some portions of the request as they contain random data for cryptography.
        interceptorManager.addMatchers(Collections.singletonList(new CustomMatcher().setComparingBodies(false)
            .setHeadersKeyOnlyMatch(Arrays.asList("x-ms-meta-encryptiondata", "x-ms-encryption-key-sha256",
                "x-ms-lease-id", "x-ms-proposed-lease-id", "If-Modified-Since", "If-Unmodified-Since", "Accept"))
            .setExcludedHeaders(Arrays.asList("Accept-Language", "Content-Type"))
            .setQueryOrderingIgnored(true)
            .setIgnoredQueryParameters(Arrays.asList("sv"))));
    }

    @Override
    protected void afterTest() {
        super.afterTest();

        if (getTestMode() == TestMode.PLAYBACK) {
            return;
        }

        BlobServiceClient cleanupClient = getNonRecordingServiceClient();
        ListBlobContainersOptions options = new ListBlobContainersOptions().setPrefix(prefix);
        for (BlobContainerItem container : cleanupClient.listBlobContainers(options, Duration.ofSeconds(120))) {
            BlobContainerClient containerClient = cleanupClient.getBlobContainerClient(container.getName());

            containerClient.delete();
        }
    }

    protected BlobServiceClient getNonRecordingServiceClient() {
        return new BlobServiceClientBuilder().httpClient(getHttpClient())
            .credential(ENV.getPrimaryAccount().getCredential())
            .endpoint(ENV.getPrimaryAccount().getBlobEndpoint())
            .buildClient();
    }

    protected ByteBuffer getRandomData(int size) {
        return StorageCommonTestUtils.getRandomData(size, testResourceNamer);
    }

    protected byte[] getRandomByteArray(int size) {
        return StorageCommonTestUtils.getRandomByteArray(size, testResourceNamer);
    }

    protected File getRandomFile(int size) throws IOException {
        return StorageCommonTestUtils.getRandomFile(size, testResourceNamer);
    }

    protected EncryptedBlobClientBuilder getEncryptedClientBuilder(AsyncKeyEncryptionKey key,
        AsyncKeyEncryptionKeyResolver keyResolver, StorageSharedKeyCredential credential, String endpoint,
        HttpPipelinePolicy... policies) {
        return getEncryptedClientBuilder(key, keyResolver, credential, endpoint, EncryptionVersion.V1, policies);
    }

    protected EncryptedBlobClientBuilder getEncryptedClientBuilder(AsyncKeyEncryptionKey key,
        AsyncKeyEncryptionKeyResolver keyResolver, StorageSharedKeyCredential credential, String endpoint,
        EncryptionVersion version, HttpPipelinePolicy... policies) {
        KeyWrapAlgorithm algorithm = key != null && "local".equals(key.getKeyId().block())
            ? KeyWrapAlgorithm.A256KW
            : KeyWrapAlgorithm.RSA_OAEP_256;
        EncryptedBlobClientBuilder builder = new EncryptedBlobClientBuilder(version).key(key, algorithm.toString())
            .keyResolver(keyResolver)
            .endpoint(endpoint);

        for (HttpPipelinePolicy policy : policies) {
            builder.addPolicy(policy);
        }

        instrument(builder);

        if (credential != null) {
            builder.credential(credential);
        }

        return builder;
    }

    protected BlobClientBuilder getBlobClientBuilder(StorageSharedKeyCredential credential, String endpoint,
        HttpPipelinePolicy... policies) {
        BlobClientBuilder builder = new BlobClientBuilder().endpoint(endpoint);

        for (HttpPipelinePolicy policy : policies) {
            builder.addPolicy(policy);
        }

        instrument(builder);

        if (credential != null) {
            builder.credential(credential);
        }

        return builder;
    }

    protected BlobServiceClientBuilder getServiceClientBuilder(TestAccount account, HttpPipelinePolicy... policies) {
        return getServiceClientBuilder(account.getCredential(), account.getBlobEndpoint(), policies);
    }

    protected BlobServiceClientBuilder getServiceClientBuilder(StorageSharedKeyCredential credential, String endpoint,
        HttpPipelinePolicy... policies) {
        BlobServiceClientBuilder builder = new BlobServiceClientBuilder().endpoint(endpoint);

        for (HttpPipelinePolicy policy : policies) {
            builder.addPolicy(policy);
        }

        instrument(builder);

        if (credential != null) {
            builder.credential(credential);
        }

        return builder;
    }

    protected String generateContainerName() {
        return generateResourceName(entityNo++);
    }

    private String generateResourceName(int entityNo) {
        return testResourceNamer.randomName(prefix + entityNo, 63);
    }

    protected String generateBlobName() {
        return generateResourceName(entityNo++);
    }

    protected static void validateBlobProperties(Response<BlobProperties> response, String cacheControl,
        String contentDisposition, String contentEncoding, String contentLanguage, byte[] contentMD5,
        String contentType, boolean checkContentMD5) {
        BlobProperties properties = response.getValue();
        assertEquals(cacheControl, properties.getCacheControl());
        assertEquals(contentDisposition, properties.getContentDisposition());
        assertEquals(contentEncoding, properties.getContentEncoding());
        assertEquals(contentLanguage, properties.getContentLanguage());
        assertEquals(contentType, response.getHeaders().getValue(HttpHeaderName.CONTENT_TYPE));

        if (checkContentMD5) {
            assertArraysEqual(contentMD5, properties.getContentMd5());
        }
    }

    /**
     * This will retrieve the etag to be used in testing match conditions. The result will typically be assigned to the
     * ifMatch condition when testing success and the ifNoneMatch condition when testing failure.
     *
     * @param ebbc The URL to the blob to get the etag on.
     * @param match The ETag value for this test. If {@code receivedEtag} is passed, that will signal that the test is
     * expecting the blob's actual etag for this test, so it is retrieved.
     * @return The appropriate etag value to run the current test.
     */
    protected String setupBlobMatchCondition(EncryptedBlobClient ebbc, String match) {
        return RECEIVED_ETAG.equals(match)
            ? ebbc.getPropertiesWithResponse(null, null, null).getHeaders().getValue(HttpHeaderName.ETAG)
            : match;
    }

    protected Mono<String> setupBlobMatchCondition(EncryptedBlobAsyncClient ebbac, String match) {
        return RECEIVED_ETAG.equals(match)
            ? ebbac.getProperties().map(BlobProperties::getETag)
            : Mono.justOrEmpty(match).defaultIfEmpty("null");
    }

    protected static List<String> convertNulls(String... conditions) {
        return Arrays.stream(conditions)
            .map(condition -> "null".equals(condition) ? null : condition)
            .collect(Collectors.toList());
    }

    /**
     * This helper method will acquire a lease on a blob to prepare for testing leaseId. We want to test against a valid
     * lease in both the success and failure cases to guarantee that the results actually indicate proper setting of the
     * header. If we pass null, though, we don't want to acquire a lease, as that will interfere with other AC tests.
     *
     * @param bc The blob on which to acquire a lease.
     * @param leaseID The signalID. Values should only ever be {@code receivedLeaseID}, {@code garbageLeaseID}, or
     * {@code null}.
     * @return The actual leaseId of the blob if recievedLeaseID is passed, otherwise whatever was passed will be
     * returned.
     */
    protected String setupBlobLeaseCondition(BlobClient bc, String leaseID) {
        String responseLeaseId = null;
        if (RECEIVED_LEASE_ID.equals(leaseID) || GARBAGE_LEASE_ID.equals(leaseID)) {
            responseLeaseId = createLeaseClient(bc).acquireLease(-1);
        }

        return RECEIVED_LEASE_ID.equals(leaseID) ? responseLeaseId : leaseID;
    }

    protected Mono<String> setupBlobLeaseCondition(BlobAsyncClient bac, String leaseID) {
        Mono<String> responseLeaseId = null;
        if (RECEIVED_LEASE_ID.equals(leaseID) || GARBAGE_LEASE_ID.equals(leaseID)) {
            responseLeaseId = new BlobLeaseClientBuilder().blobAsyncClient(bac).buildAsyncClient().acquireLease(-1);
        }

        if (responseLeaseId == null) {
            return Mono.justOrEmpty(leaseID).defaultIfEmpty("null");
        }

        return responseLeaseId.map(returnedLeaseId -> RECEIVED_LEASE_ID.equals(leaseID)
            ? returnedLeaseId
            : (leaseID == null ? "null" : leaseID));
    }

    protected static BlobLeaseClient createLeaseClient(BlobClient blobClient) {
        return createLeaseClient(blobClient, null);
    }

    protected static BlobLeaseClient createLeaseClient(BlobClient blobClient, String leaseId) {
        return new BlobLeaseClientBuilder().blobClient(blobClient).leaseId(leaseId).buildClient();
    }

    protected static void compareDataToFile(Flux<ByteBuffer> data, File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {

            for (ByteBuffer received : data.toIterable()) {
                byte[] readBuffer = new byte[received.remaining()];
                fis.read(readBuffer);
                assertByteBuffersEqual(received, ByteBuffer.wrap(readBuffer));
            }
        }
    }

    /*
    This is for stubbing responses that will actually go through the pipeline and autorest code. Autorest does not seem
    to play too nicely with mocked objects and the complex reflection stuff on both ends made it more difficult to work
    with than was worth it. Because this type is just for BlobDownload, we don't need to accept a header type.
     */
    protected static final class MockDownloadHttpResponse extends HttpResponse {
        private final int statusCode;
        private final HttpHeaders headers;
        private final Flux<ByteBuffer> body;

        MockDownloadHttpResponse(HttpResponse response, int statusCode, Flux<ByteBuffer> body) {
            super(response.getRequest());
            this.statusCode = statusCode;
            this.headers = response.getHeaders();
            this.body = body;
        }

        @Override
        public int getStatusCode() {
            return statusCode;
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
            return Mono.error(new IOException());
        }

        @Override
        public Mono<String> getBodyAsString() {
            return Mono.error(new IOException());
        }

        @Override
        public Mono<String> getBodyAsString(Charset charset) {
            return Mono.error(new IOException());
        }
    }

    /**
     * Compares two files for having equivalent content.
     *
     * @param file1 File used to upload data to the service
     * @param file2 File used to download data from the service
     * @param offset Write offset from the upload file
     * @param count Size of the download from the service
     */
    protected static void compareFiles(File file1, File file2, long offset, long count) throws IOException {
        StorageCommonTestUtils.compareFiles(file1, file2, offset, count);
    }

    /**
     * Insecurely and quickly generates a random AES256 key for the purpose of unit tests. No one should ever make a
     * real key this way.
     */
    protected static byte[] getRandomKey(Long seed) {
        if (ENV.getTestMode() == TestMode.LIVE) {
            byte[] key = new byte[32]; // 256 bit key
            new Random((seed == null) ? ThreadLocalRandom.current().nextLong() : seed).nextBytes(key);
            return key;
        } else {
            return MOCK_KEY;
        }
    }

    /**
     * Stubs the generateAesKey method in EncryptedBlobAsyncClient to return a consistent SecretKey used in PLAYBACK and
     * RECORD testing modes only.
     */
    protected static EncryptedBlobAsyncClient mockAesKey(EncryptedBlobAsyncClient encryptedClient) {
        return (ENV.getTestMode() == TestMode.PLAYBACK)
            ? new EncryptedBlobAsyncClientSpy(encryptedClient)
            : encryptedClient;
    }

    static final SecretKey MOCK_AES_KEY = new SecretKey() {
        @Override
        public String getAlgorithm() {
            return CryptographyConstants.AES;
        }

        @Override
        public String getFormat() {
            return "RAW";
        }

        @Override
        public byte[] getEncoded() {
            return MOCK_KEY;
        }
    };

    protected <T extends HttpTrait<T>, E extends Enum<E>> T instrument(T builder) {
        return StorageCommonTestUtils.instrument(builder, BlobServiceClientBuilder.getDefaultHttpLogOptions(),
            interceptorManager);
    }

    protected HttpClient getHttpClient() {
        return StorageCommonTestUtils.getHttpClient(interceptorManager);
    }

    protected HttpPipeline getHttpPipeline(KeyServiceVersion serviceVersion) {
        Configuration global = Configuration.getGlobalConfiguration().clone();
        TokenCredential credential;

        credential = StorageCommonTestUtils.getTokenCredential(interceptorManager);

        // Closest to API goes first, closest to wire goes last.
        final List<HttpPipelinePolicy> policies = new ArrayList<>();
        policies.add(new UserAgentPolicy("client_name", "client_version", global, serviceVersion));
        HttpPolicyProviders.addBeforeRetryPolicies(policies);
        RetryStrategy strategy = new ExponentialBackoff(5, Duration.ofSeconds(2), Duration.ofSeconds(16));
        policies.add(new RetryPolicy(strategy));
        policies.add(new BearerTokenAuthenticationPolicy(credential, "https://vault.azure.net/.default"));
        HttpPolicyProviders.addAfterRetryPolicies(policies);

        if (getTestMode() == TestMode.RECORD) {
            policies.add(interceptorManager.getRecordPolicy());
        }

        return new HttpPipelineBuilder().policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(getHttpClient())
            .build();
    }

    protected static void compareListToBuffer(List<ByteBuffer> buffers, ByteBuffer result) {
        result.position(0);

        for (ByteBuffer buffer : buffers) {
            buffer.position(0);
            result.limit(result.position() + buffer.remaining());
            assertByteBuffersEqual(buffer, result);
            result.position(result.position() + buffer.remaining());
        }

        assertEquals(0, result.remaining());
    }
}
