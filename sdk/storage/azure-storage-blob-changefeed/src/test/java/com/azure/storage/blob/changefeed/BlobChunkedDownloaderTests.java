// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.blob.changefeed;

import com.azure.core.client.traits.HttpTrait;
import com.azure.core.http.HttpClient;
import com.azure.core.test.TestMode;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.utils.TestUtils;
import com.azure.core.util.FluxUtil;
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
import com.azure.storage.common.test.shared.StorageCommonTestUtils;
import com.azure.storage.common.test.shared.TestAccount;
import com.azure.storage.common.test.shared.TestEnvironment;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class BlobChunkedDownloaderTests extends TestProxyTestBase {
    protected static final TestEnvironment ENV = TestEnvironment.getInstance();

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
        return StorageCommonTestUtils.getRandomByteArray(size, testResourceNamer);
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
        return StorageCommonTestUtils.getCrc32(input);
    }

    protected <T extends HttpTrait<T>, E extends Enum<E>> T instrument(T builder) {
        return StorageCommonTestUtils.instrument(builder, BlobServiceClientBuilder.getDefaultHttpLogOptions(),
            interceptorManager);
    }

    protected HttpClient getHttpClient() {
        return StorageCommonTestUtils.getHttpClient(interceptorManager);
    }

    private static final class DownloadWithResponseTrackingClient extends BlobAsyncClient {
        private int downloadCalls;

        private DownloadWithResponseTrackingClient(BlobAsyncClient wrapped) {
            super(wrapped.getHttpPipeline(), wrapped.getBlobUrl(), wrapped.getServiceVersion(),
                wrapped.getAccountName(), wrapped.getContainerName(), wrapped.getBlobName(), wrapped.getSnapshotId(),
                wrapped.getCustomerProvidedKey(), null, wrapped.getVersionId());
        }

        @Override
        public Mono<BlobDownloadAsyncResponse> downloadWithResponse(BlobRange range, DownloadRetryOptions options,
            BlobRequestConditions requestConditions, boolean getRangeContentMd5) {
            downloadCalls++;
            return super.downloadWithResponse(range, options, requestConditions, getRangeContentMd5);
        }
    }
}
