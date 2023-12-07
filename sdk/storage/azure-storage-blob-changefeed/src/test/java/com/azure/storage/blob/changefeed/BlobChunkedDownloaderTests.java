// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.blob.changefeed;

import com.azure.core.client.traits.HttpTrait;
import com.azure.core.http.HttpClient;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.http.okhttp.OkHttpAsyncHttpClientBuilder;
import com.azure.core.test.TestMode;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.utils.TestUtils;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.ServiceVersion;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.BlobServiceAsyncClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobContainerItem;
import com.azure.storage.blob.models.BlobDownloadAsyncResponse;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.models.DownloadRetryOptions;
import com.azure.storage.blob.models.ListBlobContainersOptions;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.test.shared.ServiceVersionValidationPolicy;
import com.azure.storage.common.test.shared.TestAccount;
import com.azure.storage.common.test.shared.TestEnvironment;
import okhttp3.ConnectionPool;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import java.util.zip.CRC32;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class BlobChunkedDownloaderTests extends TestProxyTestBase {
    protected static final TestEnvironment ENV = TestEnvironment.getInstance();
    private static final HttpClient NETTY_HTTP_CLIENT = new NettyAsyncHttpClientBuilder().build();
    private static final HttpClient OK_HTTP_CLIENT = new OkHttpAsyncHttpClientBuilder()
        .connectionPool(new ConnectionPool(50, 5, TimeUnit.MINUTES))
        .build();

    private DownloadWithResponseTrackingClient bc;
    private BlobChunkedDownloaderFactory factory;

    private String prefix;
    private int entityNo = 0; // Used to generate stable container names for recording tests requiring multiple containers.

    @Override
    protected void beforeTest() {
        super.beforeTest();

        prefix = getCrc32(testContextManager.getTestPlaybackRecordingName());
        BlobServiceAsyncClient primaryBlobServiceAsyncClient = getServiceAsyncClient(ENV.getPrimaryAccount());

        BlobContainerAsyncClient cc = primaryBlobServiceAsyncClient.getBlobContainerAsyncClient(generateContainerName());
        cc.create().block();
        bc = new DownloadWithResponseTrackingClient(cc.getBlobAsyncClient(generateBlobName()));
        factory = new BlobChunkedDownloaderFactory(cc);
    }

    @Override
    protected void afterTest() {
        if (ENV.getTestMode() == TestMode.PLAYBACK) {
            return;
        }

        BlobServiceClient cleanupClient = new BlobServiceClientBuilder()
            .httpClient(getHttpClient())
            .credential(ENV.getPrimaryAccount().getCredential())
            .endpoint(ENV.getPrimaryAccount().getBlobEndpoint())
            .buildClient();

        ListBlobContainersOptions options = new ListBlobContainersOptions().setPrefix(prefix);
        for (BlobContainerItem container : cleanupClient.listBlobContainers(options, Duration.ofSeconds(120))) {
            cleanupClient.getBlobContainerClient(container.getName()).delete();
        }
    }

    private String generateContainerName() {
        return generateResourceName(entityNo++);
    }

    private String generateBlobName() {
        return generateResourceName(entityNo++);
    }

    private String generateResourceName(int entityNo) {
        return testResourceNamer.randomName(prefix + entityNo, 63);
    }

    BlobServiceAsyncClient getServiceAsyncClient(TestAccount account) {
        BlobServiceClientBuilder builder = new BlobServiceClientBuilder()
            .endpoint(account.getBlobEndpoint());

        instrument(builder);

        if (account.getCredential() != null) {
            builder.credential(account.getCredential());
        }

        return builder.buildAsyncClient();
    }

    byte[] getRandomByteArray(int size) {
        long seed = UUID.fromString(testResourceNamer.randomUuid()).getMostSignificantBits() & Long.MAX_VALUE;
        Random rand = new Random(seed);
        byte[] data = new byte[size];
        rand.nextBytes(data);
        return data;
    }

    private static byte[] downloadHelper(BlobChunkedDownloader downloader) {
        return FluxUtil.collectBytesInByteBufferStream(downloader.download()).block();
    }

    byte[] uploadHelper(int size) {
        byte[] input = getRandomByteArray(size);
        bc.upload(Flux.just(ByteBuffer.wrap(input)), null).block();
        return input;
    }

    @ParameterizedTest
    @MethodSource("downloadBlockSizeSupplier")
    public void downloadBlockSize(int size, int blockSize, int numDownloads) {
        byte[] input = uploadHelper(size);
        byte[] output = downloadHelper(new BlobChunkedDownloader(bc, blockSize, 0));

        TestUtils.assertArraysEqual(input, output);
        assertEquals(numDownloads, bc.downloadCalls);
    }

    private static Stream<Arguments> downloadBlockSizeSupplier() {
        // size | blockSize | numDownloads
        return Stream.of(
            Arguments.of(Constants.KB, Constants.KB, 1), // blockSize = size. 1 download call.
            Arguments.of(Constants.KB, Constants.MB, 1), // blockSize > size. 1 download call.
            Arguments.of(4 * Constants.KB, Constants.KB, 4) // blockSize < size. 4 download calls.
        );
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 200, 512, 1000})
    public void downloadOffset(int offset) {
        byte[] input = uploadHelper(Constants.KB);
        byte[] output = downloadHelper(new BlobChunkedDownloader(bc, Constants.KB, offset));

        TestUtils.assertArraysEqual(input, offset, output, 0, input.length - offset);
        assertEquals(1, bc.downloadCalls);
    }

    @ParameterizedTest
    @MethodSource("downloadBlockSizeOffsetSupplier")
    public void downloadBlockSizeOffsetSupplier(int size, int blockSize, int offset, int numDownloads) {
        byte[] input = uploadHelper(size);
        byte[] output = downloadHelper(new BlobChunkedDownloader(bc, Constants.KB, offset));

        TestUtils.assertArraysEqual(input, offset, output, 0, input.length - offset);
        assertEquals(numDownloads, bc.downloadCalls);
    }

    private static Stream<Arguments> downloadBlockSizeOffsetSupplier() {
        // size | blockSize | offset | numDownloads
        return Stream.of(
            Arguments.of(4 * Constants.KB, Constants.KB, Constants.KB, 3), // 3 download calls.
            Arguments.of(4 * Constants.KB, Constants.KB, 2 * Constants.KB, 2) // 2 download calls.
        );
    }

    /* Tests offset > length of blob. */
    @Test
    public void downloadInvalidOffset() {
        uploadHelper(Constants.KB);

        assertThrows(BlobStorageException.class, () ->
            downloadHelper(factory.getBlobLazyDownloader(bc.getBlobName(), Constants.KB, Constants.KB * 2)));
    }

    /* Tests case for downloading only the header. */
    @ParameterizedTest
    @MethodSource("downloadPartialSupplier")
    public void downloadPartialSupplier(int uploadSize, int downloadSize) {
        byte[] input = uploadHelper(uploadSize);
        byte[] output = downloadHelper(new BlobChunkedDownloader(bc, downloadSize));


        assertEquals(downloadSize, output.length);
        TestUtils.assertArraysEqual(input, 0, output, 0, downloadSize);
        assertEquals(1, bc.downloadCalls);
    }

    private static Stream<Arguments> downloadPartialSupplier() {
        // uploadSize | downloadSize
        return Stream.of(Arguments.of(Constants.MB, Constants.KB), Arguments.of(Constants.MB, 4 * Constants.KB),
            Arguments.of(Constants.MB, Constants.MB));
    }

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

    private static final class DownloadWithResponseTrackingClient extends BlobAsyncClient {
        private final BlobAsyncClient wrapped;
        private int downloadCalls;

        private DownloadWithResponseTrackingClient(BlobAsyncClient wrapped) {
            super(wrapped.getHttpPipeline(), wrapped.getBlobUrl(), wrapped.getServiceVersion(),
                wrapped.getAccountName(), wrapped.getContainerName(), wrapped.getBlobName(), wrapped.getSnapshotId(),
                wrapped.getCustomerProvidedKey(), null, wrapped.getVersionId());

            this.wrapped = wrapped;
        }

        @Override
        public Mono<BlobDownloadAsyncResponse> downloadWithResponse(BlobRange range, DownloadRetryOptions options,
            BlobRequestConditions requestConditions, boolean getRangeContentMd5) {
            downloadCalls++;
            return super.downloadWithResponse(range, options, requestConditions, getRangeContentMd5);
        }
    }
}
