// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized.cryptography;

import com.azure.core.client.traits.HttpTrait;
import com.azure.core.cryptography.AsyncKeyEncryptionKey;
import com.azure.core.cryptography.AsyncKeyEncryptionKeyResolver;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.http.okhttp.OkHttpAsyncHttpClientBuilder;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.rest.Response;
import com.azure.core.test.TestMode;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.models.CustomMatcher;
import com.azure.core.test.models.TestProxySanitizer;
import com.azure.core.test.models.TestProxySanitizerType;
import com.azure.core.util.ServiceVersion;
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
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.test.shared.ServiceVersionValidationPolicy;
import com.azure.storage.common.test.shared.TestAccount;
import com.azure.storage.common.test.shared.TestDataFactory;
import com.azure.storage.common.test.shared.TestEnvironment;
import okhttp3.ConnectionPool;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.zip.CRC32;

import static com.azure.core.test.utils.TestUtils.assertArraysEqual;
import static com.azure.core.test.utils.TestUtils.assertByteBuffersEqual;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class BlobCryptographyTestBase extends TestProxyTestBase {
    protected static final TestEnvironment ENV = TestEnvironment.getInstance();
    protected static final TestDataFactory DATA = TestDataFactory.getInstance();
    private static final HttpClient NETTY_HTTP_CLIENT = new NettyAsyncHttpClientBuilder().build();
    private static final HttpClient OK_HTTP_CLIENT = new OkHttpAsyncHttpClientBuilder()
        .connectionPool(new ConnectionPool(50, 5, TimeUnit.MINUTES))
        .build();

    protected static final OffsetDateTime OLD_DATE = OffsetDateTime.now().minusDays(1);
    protected static final OffsetDateTime NEW_DATE = OffsetDateTime.now().plusDays(1);
    protected static final String RECEIVED_ETAG = "received";
    protected static final String GARBAGE_ETAG = "garbage";
    protected static final String RECEIVED_LEASE_ID = "received";
    protected static final String GARBAGE_LEASE_ID = UUID.randomUUID().toString();
    protected static final byte[] MOCK_RANDOM_DATA = String.join("", Collections.nCopies(32, "password"))
        .getBytes(StandardCharsets.UTF_8);
    private static final byte[] MOCK_KEY = String.join("", Collections.nCopies(4, "password"))
        .getBytes(StandardCharsets.UTF_8);

    private int entityNo = 0; // Used to generate stable container names for recording tests requiring multiple containers.
    protected String prefix;

    @Override
    protected void beforeTest() {
        super.beforeTest();
        prefix = getCrc32(testContextManager.getTestPlaybackRecordingName());

        if (getTestMode() != TestMode.LIVE) {
            interceptorManager.addSanitizers(Arrays.asList(
                new TestProxySanitizer("x-ms-encryption-key", ".*", "REDACTED", TestProxySanitizerType.HEADER),
                new TestProxySanitizer("x-ms-encryption-key-sha256", ".*", "REDACTED", TestProxySanitizerType.HEADER)
            ));
        }

        // Ignore some portions of the request as they contain random data for cryptography.
        interceptorManager.addMatchers(Collections.singletonList(new CustomMatcher()
            .setComparingBodies(false)
            .setHeadersKeyOnlyMatch(Arrays.asList("x-ms-meta-encryptiondata", "x-ms-encryption-key-sha256",
                "x-ms-lease-id", "x-ms-proposed-lease-id", "If-Modified-Since", "If-Unmodified-Since"))
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
        return new BlobServiceClientBuilder()
            .httpClient(getHttpClient())
            .credential(ENV.getPrimaryAccount().getCredential())
            .endpoint(ENV.getPrimaryAccount().getBlobEndpoint())
            .buildClient();
    }

    protected ByteBuffer getRandomData(int size) {
        return ByteBuffer.wrap(getRandomByteArray(size));
    }

    protected byte[] getRandomByteArray(int size) {
        long seed = UUID.fromString(testResourceNamer.randomUuid()).getMostSignificantBits() & Long.MAX_VALUE;
        Random rand = new Random(seed);
        byte[] data = new byte[size];
        rand.nextBytes(data);
        return data;
    }

    protected File getRandomFile(int size) throws IOException {
        File file = File.createTempFile(UUID.randomUUID().toString(), ".txt");
        file.deleteOnExit();
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(getRandomData(size).array());
        fos.close();
        return file;
    }

    protected EncryptedBlobClientBuilder getEncryptedClientBuilder(AsyncKeyEncryptionKey key,
        AsyncKeyEncryptionKeyResolver keyResolver,
        StorageSharedKeyCredential credential, String endpoint,
        HttpPipelinePolicy... policies) {
        return getEncryptedClientBuilder(key, keyResolver, credential, endpoint, EncryptionVersion.V1, policies);
    }

    protected EncryptedBlobClientBuilder getEncryptedClientBuilder(AsyncKeyEncryptionKey key,
        AsyncKeyEncryptionKeyResolver keyResolver, StorageSharedKeyCredential credential, String endpoint,
        EncryptionVersion version, HttpPipelinePolicy... policies) {
        KeyWrapAlgorithm algorithm = key != null && "local".equals(key.getKeyId().block())
            ? KeyWrapAlgorithm.A256KW : KeyWrapAlgorithm.RSA_OAEP_256;
        EncryptedBlobClientBuilder builder = new EncryptedBlobClientBuilder(version)
            .key(key, algorithm.toString())
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
        BlobClientBuilder builder = new BlobClientBuilder()
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

    protected BlobServiceClientBuilder getServiceClientBuilder(TestAccount account,
        HttpPipelinePolicy... policies) {
        return getServiceClientBuilder(account.getCredential(), account.getBlobEndpoint(), policies);
    }

    protected BlobServiceClientBuilder getServiceClientBuilder(StorageSharedKeyCredential credential, String endpoint,
        HttpPipelinePolicy... policies) {
        BlobServiceClientBuilder builder = new BlobServiceClientBuilder()
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

    protected String setupBlobMatchCondition(EncryptedBlobAsyncClient ebbac, String match) {
        return RECEIVED_ETAG.equals(match)
            ? ebbac.getPropertiesWithResponse(null).block().getHeaders().getValue(HttpHeaderName.ETAG)
            : match;
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

    protected String setupBlobLeaseCondition(BlobAsyncClient bac, String leaseID) {
        String responseLeaseId = null;
        if (RECEIVED_LEASE_ID.equals(leaseID) || GARBAGE_LEASE_ID.equals(leaseID)) {
            responseLeaseId = new BlobLeaseClientBuilder()
                .blobAsyncClient(bac)
                .buildAsyncClient()
                .acquireLease(-1)
                .block();
        }

        return RECEIVED_LEASE_ID.equals(leaseID) ? responseLeaseId : leaseID;
    }

    protected static BlobLeaseClient createLeaseClient(BlobClient blobClient) {
        return createLeaseClient(blobClient, null);
    }

    protected static BlobLeaseClient createLeaseClient(BlobClient blobClient, String leaseId) {
        return new BlobLeaseClientBuilder()
            .blobClient(blobClient)
            .leaseId(leaseId)
            .buildClient();
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

    protected static final class MockRetryRangeResponsePolicy implements HttpPipelinePolicy {
        @Override
        public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
            return next.process().flatMap(response ->
                "bytes=0-15".equals(response.getRequest().getHeaders().getValue("x-ms-range"))
                    ? Mono.error(new IllegalArgumentException("The range header was not set correctly on retry."))
                    : Mono.just(new MockDownloadHttpResponse(response, 206, Flux.error(new IOException()))));
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
        long pos = 0L;
        int defaultBufferSize = 128 * Constants.KB;

        try (InputStream stream1 = new FileInputStream(file1);
             InputStream stream2 = new FileInputStream(file2)) {
            stream1.skip(offset);

            // If the amount we are going to read is smaller than the default buffer size use that instead.
            int bufferSize = (int) Math.min(defaultBufferSize, count);

            while (pos < count) {
                // Number of bytes we expect to read.
                int expectedReadCount = (int) Math.min(bufferSize, count - pos);
                byte[] buffer1 = new byte[expectedReadCount];
                byte[] buffer2 = new byte[expectedReadCount];

                int readCount1 = stream1.read(buffer1);
                int readCount2 = stream2.read(buffer2);

                // Use Arrays.equals as it is more optimized than Groovy/Spock's '==' for arrays.
                assertEquals(readCount1, readCount2);
                assertArraysEqual(buffer1, buffer2);

                pos += expectedReadCount;
            }

            assertEquals(count, pos);
            assertEquals(-1, stream2.read());
        }
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
        return  (ENV.getTestMode() == TestMode.PLAYBACK)
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

    private static String getCrc32(String input) {
        CRC32 crc32 = new CRC32();
        crc32.update(input.getBytes(StandardCharsets.UTF_8));
        return String.format(Locale.US, "%08X", crc32.getValue()).toLowerCase();
    }

    @SuppressWarnings("unchecked")
    protected <T extends HttpTrait<T>, E extends Enum<E>> T instrument(T builder) {
        builder.httpClient(getHttpClient());
        if (getTestMode() == TestMode.RECORD) {
            builder.addPolicy(interceptorManager.getRecordPolicy());
        }


        if (ENV.getServiceVersion() != null) {
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
                    ENV.getServiceVersion());
                serviceVersionMethod.invoke(builder, serviceVersion);
                builder.addPolicy(new ServiceVersionValidationPolicy(serviceVersion.getVersion()));
            } catch (ReflectiveOperationException ex) {
                throw new RuntimeException(ex);
            }
        }

        builder.httpLogOptions(BlobServiceClientBuilder.getDefaultHttpLogOptions());

        return builder;
    }

    protected HttpClient getHttpClient() {
        if (getTestMode() != TestMode.PLAYBACK) {
            switch (ENV.getHttpClientType()) {
                case NETTY:
                    return NETTY_HTTP_CLIENT;
                case OK_HTTP:
                    return OK_HTTP_CLIENT;
                default:
                    throw new IllegalArgumentException("Unknown http client type: " + ENV.getHttpClientType());
            }
        } else {
            return interceptorManager.getPlaybackClient();
        }
    }

    public static boolean liveOnly() {
        return ENV.getTestMode() == TestMode.LIVE;
    }
}
