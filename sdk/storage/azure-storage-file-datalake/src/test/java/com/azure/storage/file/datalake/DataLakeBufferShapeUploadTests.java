// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.test.utils.TestUtils;
import com.azure.core.util.FluxUtil;
import com.azure.storage.common.ParallelTransferOptions;
import com.azure.storage.common.test.shared.ShapedByteBuffer;
import com.azure.storage.common.test.shared.TestBufferShape;
import com.azure.storage.file.datalake.options.FileParallelUploadOptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Offline tests verifying that Data Lake file uploads send only the bytes between each source buffer's position and
 * limit, and don't modify the source buffers, for every kind of buffer (see {@link TestBufferShape}).
 */
public class DataLakeBufferShapeUploadTests {
    private static final String ENDPOINT = "https://account.dfs.core.windows.net";
    // Small sizes keep the tests fast; the partitioning logic doesn't depend on the absolute sizes.
    private static final int BLOCK_SIZE = 1024;

    @ParameterizedTest
    @MethodSource("partitionedUploadSupplier")
    public void partitionedUploadSendsOnlyExposedBytes(TestBufferShape shape, int length, int expectedAppends) {
        byte[] content = randomBytes(length);
        ShapedByteBuffer source = shape.wrap(content);
        RequestCapturingHttpClient httpClient = new RequestCapturingHttpClient();

        fileClient(httpClient).uploadWithResponse(
            new FileParallelUploadOptions(Flux.just(source.getBuffer())).setParallelTransferOptions(blockOptions()),
            null, null);

        assertEquals(expectedAppends, httpClient.getAppendCount());
        TestUtils.assertArraysEqual(content, httpClient.getFileContent());
        source.assertNoBytesChanged();
    }

    private static Stream<Arguments> partitionedUploadSupplier() {
        return Stream.of(TestBufferShape.values())
            .flatMap(shape -> Stream.of(
                // shape | length | expected appends
                Arguments.of(shape, BLOCK_SIZE / 2, 1), // A single append.
                Arguments.of(shape, BLOCK_SIZE, 1), // A single append, exactly the threshold.
                Arguments.of(shape, 2 * BLOCK_SIZE + 100, 3), // Several appends, with a partial last one.
                Arguments.of(shape, 3 * BLOCK_SIZE, 3))); // Several appends, an exact multiple of the block size.
    }

    @Test
    public void partitionedUploadSendsOnlyExposedBytesOfMixedBuffers() {
        byte[] content = randomBytes(5 * BLOCK_SIZE + 100);
        // Pieces that don't align with blocks, so appends span buffers of different kinds.
        List<ShapedByteBuffer> sources = TestBufferShape.mixed(content, 700);
        List<ByteBuffer> buffers = new ArrayList<>();
        sources.forEach(source -> buffers.add(source.getBuffer()));
        RequestCapturingHttpClient httpClient = new RequestCapturingHttpClient();

        fileClient(httpClient).uploadWithResponse(
            new FileParallelUploadOptions(Flux.fromIterable(buffers)).setParallelTransferOptions(blockOptions()), null,
            null);

        assertEquals(6, httpClient.getAppendCount());
        TestUtils.assertArraysEqual(content, httpClient.getFileContent());
        sources.forEach(ShapedByteBuffer::assertNoBytesChanged);
    }

    @ParameterizedTest
    @EnumSource(TestBufferShape.class)
    public void appendSendsOnlyExposedBytes(TestBufferShape shape) {
        byte[] content = randomBytes(BLOCK_SIZE + 100);
        ShapedByteBuffer source = shape.wrap(content);
        RequestCapturingHttpClient httpClient = new RequestCapturingHttpClient();

        fileAsyncClient(httpClient).append(Flux.just(source.getBuffer()), 0, content.length).block();

        assertEquals(1, httpClient.getAppendCount());
        TestUtils.assertArraysEqual(content, httpClient.getFileContent());
        source.assertNoBytesChanged();
    }

    private static ParallelTransferOptions blockOptions() {
        return new ParallelTransferOptions().setBlockSizeLong((long) BLOCK_SIZE)
            .setMaxSingleUploadSizeLong((long) BLOCK_SIZE);
    }

    private static byte[] randomBytes(int length) {
        byte[] bytes = new byte[length];
        new Random(length).nextBytes(bytes);
        return bytes;
    }

    private static DataLakeFileClient fileClient(HttpClient httpClient) {
        return fileSystemBuilder(httpClient).buildClient().getFileClient("file");
    }

    private static DataLakeFileAsyncClient fileAsyncClient(HttpClient httpClient) {
        return fileSystemBuilder(httpClient).buildAsyncClient().getFileAsyncClient("file");
    }

    private static DataLakeFileSystemClientBuilder fileSystemBuilder(HttpClient httpClient) {
        return new DataLakeFileSystemClientBuilder().endpoint(ENDPOINT)
            .fileSystemName("filesystem")
            .credential(new MockTokenCredential())
            .pipeline(new HttpPipelineBuilder().httpClient(httpClient).build());
    }

    /*
     * Captures the content of each append by its position, answering create and flush requests.
     */
    private static final class RequestCapturingHttpClient implements HttpClient {
        private final Map<Long, byte[]> appends = new TreeMap<>();

        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            String action = getQueryParameter(request, "action");
            int status = request.getHttpMethod() == HttpMethod.PUT ? 201 : "append".equals(action) ? 202 : 200;

            Mono<byte[]> body = request.getBody() == null
                ? Mono.just(new byte[0])
                : FluxUtil.collectBytesInByteBufferStream(request.getBody());
            return body.map(bytes -> {
                if ("append".equals(action)) {
                    synchronized (this) {
                        appends.put(Long.parseLong(getQueryParameter(request, "position")), bytes);
                    }
                }
                return new MockHttpResponse(request, status);
            });
        }

        synchronized int getAppendCount() {
            return appends.size();
        }

        /*
         * Gets the content of the file as it would be after the upload, from the appends ordered by their position.
         */
        synchronized byte[] getFileContent() {
            ByteArrayOutputStream content = new ByteArrayOutputStream();
            for (Map.Entry<Long, byte[]> append : appends.entrySet()) {
                assertEquals(content.size(), append.getKey().longValue());
                content.write(append.getValue(), 0, append.getValue().length);
            }
            return content.toByteArray();
        }

        private static String getQueryParameter(HttpRequest request, String name) {
            String query = request.getUrl().getQuery();
            if (query == null) {
                return null;
            }
            for (String parameter : query.split("&")) {
                if (parameter.startsWith(name + "=")) {
                    return parameter.substring(name.length() + 1);
                }
            }
            return null;
        }
    }
}
