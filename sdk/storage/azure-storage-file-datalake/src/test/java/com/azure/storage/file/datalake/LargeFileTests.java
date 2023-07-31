// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.file.datalake;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelinePosition;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.test.TestMode;
import com.azure.core.util.CoreUtils;
import com.azure.storage.common.ParallelTransferOptions;
import com.azure.storage.common.implementation.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.SequenceInputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ResourceLock("LargeFileTest")
@EnabledIf("runLargeTests")
public class LargeFileTests extends DataLakeTestBase {
    private static boolean runLargeTests() {
        boolean liveTesting = ENVIRONMENT.getTestMode() == TestMode.LIVE;
        boolean supportedServiceVersion = !olderThan(DataLakeServiceVersion.V2019_02_02);
        boolean notMacOs = OS.current() != OS.MAC; // Mac agents are not capable enough to run this test.

        return liveTesting && supportedServiceVersion && notMacOs;
    }

    private static final long DEFAULT_SINGLE_UPLOAD_THRESHOLD = 100L * Constants.MB;
    private static final long LARGE_BLOCK_SIZE = 2500L * Constants.MB;

    private CountingPolicy countingPolicy;
    private DataLakeFileClient fc;
    private DataLakeFileAsyncClient fcAsync;

    @BeforeEach
    public void setup() {
        String fileName = generatePathName();
        DataLakeFileClient fileClient = dataLakeFileSystemClient.getFileClient(fileName);
        countingPolicy = new CountingPolicy();
        fc = getFileClient(getDataLakeCredential(), fileClient.getFileUrl(), countingPolicy);
        fcAsync = getFileAsyncClient(getDataLakeCredential(), fileClient.getFileUrl(), countingPolicy);

        fileClient.create();
    }

    @Test
    public void appendLargeBlock() {
        InputStream stream = createLargeInputStream(LARGE_BLOCK_SIZE);

        fc.append(stream, 0, LARGE_BLOCK_SIZE);

        assertEquals(1, countingPolicy.count.get());
        assertEquals(LARGE_BLOCK_SIZE, countingPolicy.appendPayloadSizes.get(0));
    }

    @Test
    public void appendLargeBlockAsync() {
        Flux<ByteBuffer> data = createLargeBuffer(LARGE_BLOCK_SIZE);

        StepVerifier.create(fcAsync.append(data, 0, LARGE_BLOCK_SIZE)).verifyComplete();

        assertEquals(1, countingPolicy.count.get());
        assertEquals(LARGE_BLOCK_SIZE, countingPolicy.appendPayloadSizes.get(0));
    }

    @Test
    public void uploadLargeDataAsync() {
        long tail = Constants.MB;
        Flux<ByteBuffer> data = createLargeBuffer(LARGE_BLOCK_SIZE + tail);

        StepVerifier.create(fcAsync.upload(data, new ParallelTransferOptions().setBlockSizeLong(LARGE_BLOCK_SIZE), true))
            .expectNextCount(1)
            .verifyComplete();

        assertEquals(2, countingPolicy.count.get());
        assertTrue(countingPolicy.appendPayloadSizes.contains(LARGE_BLOCK_SIZE));
        assertTrue(countingPolicy.appendPayloadSizes.contains(tail));
    }

    @Test
    public void uploadLargeFile() {
        long tail = Constants.MB;
        File file = getLargeRandomFile(LARGE_BLOCK_SIZE + tail);

        fc.uploadFromFile(file.toPath().toString(), new ParallelTransferOptions().setBlockSizeLong(LARGE_BLOCK_SIZE),
            null, null, null, null);

        assertEquals(2, countingPolicy.count.get());
        assertTrue(countingPolicy.appendPayloadSizes.contains(LARGE_BLOCK_SIZE));
        assertTrue(countingPolicy.appendPayloadSizes.contains(tail));
    }

    // This test does not send large payload over the wire
    @ParameterizedTest
    @MethodSource("shouldHonorDefaultSingleUploadThresholdSupplier")
    public void shouldHonorDefaultSingleUploadThreshold(long dataSize, int expectedAppendRequests) {
        Flux<ByteBuffer> data = createLargeBuffer(dataSize);
        // set this much lower than default single upload size to make it tempting.
        ParallelTransferOptions transferOptions = new ParallelTransferOptions().setBlockSizeLong(10L * Constants.MB);

        StepVerifier.create(fcAsync.upload(data, transferOptions, true)).expectNextCount(1).verifyComplete();

        assertEquals(expectedAppendRequests, countingPolicy.count.get());
    }

    private static Stream<Arguments> shouldHonorDefaultSingleUploadThresholdSupplier() {
        return Stream.of(
            // dataSize, expectedAppendRequests
            Arguments.of(DEFAULT_SINGLE_UPLOAD_THRESHOLD, 1),
            Arguments.of(DEFAULT_SINGLE_UPLOAD_THRESHOLD + 1, 11)
        );
    }

    private Flux<ByteBuffer> createLargeBuffer(long size) {
        return createLargeBuffer(size, Constants.MB);
    }

    private Flux<ByteBuffer> createLargeBuffer(long size, int bufferSize) {
        byte[] bytes = getRandomByteArray(bufferSize);
        long numberOfSubBuffers = size / bufferSize;
        int remainder = (int) (size - numberOfSubBuffers * bufferSize);

        Flux<ByteBuffer> result =  Flux.just(ByteBuffer.wrap(bytes))
            .map(ByteBuffer::duplicate)
            .repeat(numberOfSubBuffers - 1);

        if (remainder > 0) {
            byte[] extraBytes = getRandomByteArray(remainder);
            result = Flux.concat(result, Flux.just(ByteBuffer.wrap(extraBytes)));
        }

        return result;
    }

    private InputStream createLargeInputStream(long size) {
        return createLargeInputStream(size, Constants.MB);
    }

    private InputStream createLargeInputStream(long size, int chunkSize) {
        long numberOfSubStreams = size / chunkSize;
        Vector<InputStream> subStreams = new Vector<>();
        byte[] bytes = getRandomByteArray(chunkSize);
        for (long i = 0; i < numberOfSubStreams; i++) {
            subStreams.add(new ByteArrayInputStream(bytes));
        }

        return new SequenceInputStream(subStreams.elements()) {
            @Override
            public void reset() {
                // no-op
            }
        };
    }

    File getLargeRandomFile(long size) {
        try {
            File file = File.createTempFile(CoreUtils.randomUuid().toString(), ".txt");
            file.deleteOnExit();

            try (OutputStream fos = new BufferedOutputStream(new FileOutputStream(file), 8 * Constants.MB)) {
                if (size > Constants.MB) {
                    byte[] oneMb = getRandomByteArray(Constants.MB);
                    int oneMbWrites = (int) (size / Constants.MB);
                    int remaining = (int) (size % Constants.MB);
                    for (int i = 0; i < oneMbWrites; i++) {
                        fos.write(oneMb);
                    }

                    if (remaining > 0) {
                        fos.write(oneMb, 0, remaining);
                    }
                } else {
                    fos.write(getRandomByteArray((int) size));
                }
            }

            return file;
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    /**
     * This class is intended for large payload test cases only and reports directly into this test class's
     * state members.
     */
    private static final class CountingPolicy implements HttpPipelinePolicy {
        private final AtomicInteger count = new AtomicInteger();
        private final List<Long> appendPayloadSizes = Collections.synchronizedList(new ArrayList<>());

        @Override
        public Mono<HttpResponse> process(HttpPipelineCallContext httpPipelineCallContext,
            HttpPipelineNextPolicy httpPipelineNextPolicy) {
            HttpRequest request = httpPipelineCallContext.getHttpRequest();

            if (isAppendRequest(request)) {
                count.incrementAndGet();

                AtomicLong size = new AtomicLong();
                request.setBody(request.getBody()
                    .map(buffer -> {
                        size.addAndGet(buffer.remaining());
                        return buffer;
                    })
                    .doOnComplete(() -> appendPayloadSizes.add(size.get())));
            }

            return httpPipelineNextPolicy.process();
        }

        private static boolean isAppendRequest(HttpRequest request) {
            return request.getUrl().getQuery() != null && request.getUrl().getQuery().contains("action=append");
        }

        @Override
        public HttpPipelinePosition getPipelinePosition() {
            return HttpPipelinePosition.PER_CALL;
        }
    }
}
