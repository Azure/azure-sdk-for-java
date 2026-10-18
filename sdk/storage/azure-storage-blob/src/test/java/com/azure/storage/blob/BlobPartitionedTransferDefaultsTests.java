// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.util.BinaryData;
import com.azure.core.util.FluxUtil;
import com.azure.storage.blob.implementation.util.ModelHelper;
import com.azure.storage.blob.models.ParallelTransferOptions;
import com.azure.storage.blob.options.BlobDownloadToFileOptions;
import com.azure.storage.blob.options.BlobParallelUploadOptions;
import com.azure.storage.blob.options.BlobUploadFromFileOptions;
import com.azure.storage.common.implementation.Constants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Offline tests verifying that, when not explicitly configured, the initial transfer of a partitioned upload or
 * download is the same size as the subsequent transfers (the default block size).
 */
public class BlobPartitionedTransferDefaultsTests {
    private static final String ENDPOINT = "https://account.blob.core.windows.net";
    private static final int DEFAULT_BLOCK_SIZE = BlobClient.BLOB_DEFAULT_UPLOAD_BLOCK_SIZE;
    private static final Pattern RANGE_PATTERN = Pattern.compile("bytes=(\\d+)-(\\d+)");

    @Test
    public void defaultMaxSingleUploadSizeIsDefaultBlockSize() {
        assertEquals(DEFAULT_BLOCK_SIZE, ModelHelper.BLOB_DEFAULT_MAX_SINGLE_UPLOAD_SIZE);
        assertEquals(DEFAULT_BLOCK_SIZE,
            ModelHelper.populateAndApplyDefaults((ParallelTransferOptions) null).getMaxSingleUploadSizeLong());
        assertEquals(DEFAULT_BLOCK_SIZE,
            ModelHelper.populateAndApplyDefaults(new com.azure.storage.common.ParallelTransferOptions())
                .getMaxSingleUploadSizeLong());

        // Explicitly set values are respected.
        long explicit = 256L * Constants.MB;
        assertEquals(explicit,
            ModelHelper.populateAndApplyDefaults(new ParallelTransferOptions().setMaxSingleUploadSizeLong(explicit))
                .getMaxSingleUploadSizeLong());
    }

    @ParameterizedTest
    @MethodSource("uploadSupplier")
    public void upload(long dataSize, ParallelTransferOptions options, int expectedStagedBlocks) {
        RequestCapturingHttpClient httpClient = new RequestCapturingHttpClient();
        BlobClient blobClient = blobClient(httpClient);

        blobClient.uploadWithResponse(new BlobParallelUploadOptions(BinaryData.fromBytes(new byte[(int) dataSize]))
            .setParallelTransferOptions(options), null, null);

        assertUploadRequests(httpClient, dataSize, expectedStagedBlocks);
    }

    @ParameterizedTest
    @MethodSource("uploadSupplier")
    public void uploadFromFile(long dataSize, ParallelTransferOptions options, int expectedStagedBlocks,
        @TempDir Path tempDir) throws IOException {
        Path file = Files.write(tempDir.resolve("upload"), new byte[(int) dataSize]);
        RequestCapturingHttpClient httpClient = new RequestCapturingHttpClient();
        BlobClient blobClient = blobClient(httpClient);

        blobClient.uploadFromFileWithResponse(
            new BlobUploadFromFileOptions(file.toString()).setParallelTransferOptions(options), null, null);

        assertUploadRequests(httpClient, dataSize, expectedStagedBlocks);
    }

    private static Stream<Arguments> uploadSupplier() {
        long maxSingleUploadSize = 256L * Constants.MB;
        return Stream.of(
            // Default options: content up to the default block size is uploaded in a single request.
            Arguments.of(DEFAULT_BLOCK_SIZE - 1, null, 0), Arguments.of(DEFAULT_BLOCK_SIZE, null, 0),
            // Default options: content larger than the default block size is partitioned.
            Arguments.of(DEFAULT_BLOCK_SIZE + 1, null, 2),
            Arguments.of(DEFAULT_BLOCK_SIZE + 1, new ParallelTransferOptions(), 2),
            // A custom block size does not change the default single upload threshold.
            Arguments.of(DEFAULT_BLOCK_SIZE + 1,
                new ParallelTransferOptions().setBlockSizeLong(2L * DEFAULT_BLOCK_SIZE), 1),
            // An explicitly set max single upload size is still respected.
            Arguments.of(DEFAULT_BLOCK_SIZE + 1,
                new ParallelTransferOptions().setMaxSingleUploadSizeLong(maxSingleUploadSize), 0));
    }

    @Test
    public void uploadFromFileNoOverwriteChecksExistenceOnlyWhenPartitioned(@TempDir Path tempDir) throws IOException {
        Path smallFile = Files.write(tempDir.resolve("small"), new byte[DEFAULT_BLOCK_SIZE]);
        RequestCapturingHttpClient smallClient = new RequestCapturingHttpClient();
        blobClient(smallClient).uploadFromFile(smallFile.toString(), false);
        assertFalse(smallClient.hasRequest(HttpMethod.HEAD));
        assertUploadRequests(smallClient, DEFAULT_BLOCK_SIZE, 0);

        Path largeFile = Files.write(tempDir.resolve("large"), new byte[DEFAULT_BLOCK_SIZE + 1]);
        RequestCapturingHttpClient largeClient = new RequestCapturingHttpClient();
        blobClient(largeClient).uploadFromFile(largeFile.toString(), false);
        assertTrue(largeClient.hasRequest(HttpMethod.HEAD));
        assertUploadRequests(largeClient, DEFAULT_BLOCK_SIZE + 1, 2);
    }

    @ParameterizedTest
    @MethodSource("downloadToFileSupplier")
    public void downloadToFileInitialRangeMatchesSubsequentRanges(ParallelTransferOptions options,
        long expectedRangeSize, @TempDir Path tempDir) {
        long blobSize = 3L * DEFAULT_BLOCK_SIZE + 1;
        RequestCapturingHttpClient httpClient = new RequestCapturingHttpClient(blobSize);

        blobClient(httpClient)
            .downloadToFileWithResponse(new BlobDownloadToFileOptions(tempDir.resolve("download").toString())
                .setParallelTransferOptions(options == null ? null : ModelHelper.wrapBlobOptions(options)), null, null);

        List<long[]> ranges = httpClient.getRequests()
            .stream()
            .filter(r -> r.getHttpMethod() == HttpMethod.GET)
            .map(RequestCapturingHttpClient::parseRange)
            .sorted((a, b) -> Long.compare(a[0], b[0]))
            .collect(Collectors.toList());

        int expectedRequests = (int) ((blobSize + expectedRangeSize - 1) / expectedRangeSize);
        assertEquals(expectedRequests, ranges.size());
        assertEquals(0, ranges.get(0)[0]);
        assertEquals(expectedRangeSize, ranges.get(0)[1] - ranges.get(0)[0] + 1);
        for (int i = 1; i < ranges.size() - 1; i++) {
            assertEquals(expectedRangeSize, ranges.get(i)[1] - ranges.get(i)[0] + 1);
        }
    }

    private static Stream<Arguments> downloadToFileSupplier() {
        return Stream.of(Arguments.of(null, DEFAULT_BLOCK_SIZE),
            Arguments.of(new ParallelTransferOptions(), DEFAULT_BLOCK_SIZE),
            Arguments.of(new ParallelTransferOptions().setBlockSizeLong((long) Constants.MB), Constants.MB));
    }

    private static void assertUploadRequests(RequestCapturingHttpClient httpClient, long dataSize,
        int expectedStagedBlocks) {
        List<HttpRequest> puts = httpClient.getRequests()
            .stream()
            .filter(r -> r.getHttpMethod() == HttpMethod.PUT)
            .collect(Collectors.toList());
        long stageBlockCount = puts.stream().filter(r -> queryContains(r, "comp=block&")).count();
        long commitCount = puts.stream().filter(r -> queryContains(r, "comp=blocklist")).count();
        long putBlobCount = puts.size() - stageBlockCount - commitCount;

        if (expectedStagedBlocks == 0) {
            assertEquals(1, putBlobCount);
            assertEquals(0, stageBlockCount);
            assertEquals(0, commitCount);
        } else {
            assertEquals(0, putBlobCount);
            assertEquals(expectedStagedBlocks, stageBlockCount);
            assertEquals(1, commitCount);
        }
        assertEquals(dataSize, httpClient.getUploadedBytes());
    }

    private static boolean queryContains(HttpRequest request, String value) {
        String query = request.getUrl().getQuery();
        return query != null && (query + "&").contains(value);
    }

    private static BlobClient blobClient(HttpClient httpClient) {
        return new BlobClientBuilder().endpoint(ENDPOINT)
            .containerName("container")
            .blobName("blob")
            .pipeline(new HttpPipelineBuilder().httpClient(httpClient).build())
            .buildClient();
    }

    private static final class RequestCapturingHttpClient implements HttpClient {
        private final List<HttpRequest> requests = new ArrayList<>();
        private final long blobSize;
        private long uploadedBytes;

        RequestCapturingHttpClient() {
            this(0);
        }

        RequestCapturingHttpClient(long blobSize) {
            this.blobSize = blobSize;
        }

        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            synchronized (this) {
                requests.add(request);
            }

            if (request.getHttpMethod() == HttpMethod.HEAD) {
                HttpHeaders headers
                    = new HttpHeaders().set(HttpHeaderName.fromString("x-ms-error-code"), "BlobNotFound");
                return Mono.just(new MockHttpResponse(request, 404, headers));
            } else if (request.getHttpMethod() == HttpMethod.GET) {
                long[] range = parseRange(request);
                long end = Math.min(range[1], blobSize - 1);
                byte[] body = new byte[(int) (end - range[0] + 1)];
                HttpHeaders headers = new HttpHeaders().set(HttpHeaderName.ETAG, "\"0x8D000000000000\"")
                    .set(HttpHeaderName.CONTENT_LENGTH, String.valueOf(body.length))
                    .set(HttpHeaderName.CONTENT_RANGE, "bytes " + range[0] + "-" + end + "/" + blobSize);
                return Mono.just(new MockHttpResponse(request, 206, headers, body));
            }

            return (request.getBody() == null
                ? Mono.just(new byte[0])
                : FluxUtil.collectBytesInByteBufferStream(request.getBody())).map(bytes -> {
                    synchronized (this) {
                        if (!queryContains(request, "comp=blocklist")) {
                            uploadedBytes += bytes.length;
                        }
                    }
                    return new MockHttpResponse(request, 201);
                });
        }

        synchronized List<HttpRequest> getRequests() {
            return new ArrayList<>(requests);
        }

        synchronized long getUploadedBytes() {
            return uploadedBytes;
        }

        synchronized boolean hasRequest(HttpMethod method) {
            return requests.stream().anyMatch(r -> r.getHttpMethod() == method);
        }

        static long[] parseRange(HttpRequest request) {
            Matcher matcher
                = RANGE_PATTERN.matcher(request.getHeaders().getValue(HttpHeaderName.fromString("x-ms-range")));
            assertTrue(matcher.matches());
            return new long[] { Long.parseLong(matcher.group(1)), Long.parseLong(matcher.group(2)) };
        }
    }
}
