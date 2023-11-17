// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized;

import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.test.utils.TestUtils;
import com.azure.core.util.FluxUtil;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceAsyncClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobTestBase;
import com.azure.storage.blob.models.BlobDownloadAsyncResponse;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.models.DownloadRetryOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DownloadResponseTests extends BlobTestBase {
    private BlockBlobClient bu;

    @BeforeEach
    public void setup() {
        bu = cc.getBlobClient(generateBlobName()).getBlockBlobClient();
        bu.upload(DATA.getDefaultInputStream(), DATA.getDefaultText().length());
    }

    /*
    This shouldn't really be different from anything else we're doing in the other tests. Just a sanity check against
    a real use case.
     */
    @Test
    public void networkCall() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bu.downloadStream(outputStream);
        TestUtils.assertArraysEqual(DATA.getDefaultBytes(), outputStream.toByteArray());
    }

    @Test
    public void networkCallNoETagReturned() {
        HttpPipelinePolicy removeETagPolicy = (context, next) -> next.process().flatMap(response -> {
            HttpHeader eTagHeader = response.getHeaders().get(HttpHeaderName.ETAG);
            if (eTagHeader == null) {
                return  Mono.just(response);
            }
            HttpHeaders headers = response.getHeaders();
            headers.remove(HttpHeaderName.ETAG);
            return Mono.just(getStubDownloadResponse(response, response.getStatusCode(), response.getBody(), headers));
        });
        BlobServiceClient bsc = getServiceClientBuilder(ENVIRONMENT.getPrimaryAccount().getCredential(),
            primaryBlobServiceClient.getAccountUrl(), removeETagPolicy).buildClient();
        BlobContainerClient cc = bsc.getBlobContainerClient(containerName);
        BlockBlobClient blockBlobClient = cc.getBlobClient(bu.getBlobName()).getBlockBlobClient();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        blockBlobClient.downloadStream(outputStream);
        TestUtils.assertArraysEqual(DATA.getDefaultBytes(), outputStream.toByteArray());
    }

    @ParameterizedTest
    @MethodSource("successfulSupplier")
    public void successful(int scenario, int tryNumber, boolean setCount) {
        DownloadResponseMockFlux flux = new DownloadResponseMockFlux(scenario, this);

        DownloadRetryOptions options = new DownloadRetryOptions().setMaxRetryRequests(5);

        BlobServiceAsyncClient bsc = getServiceClientBuilder(ENVIRONMENT.getPrimaryAccount().getCredential(),
            primaryBlobServiceClient.getAccountUrl(), flux.asPolicy()).buildAsyncClient();
        BlobContainerAsyncClient cc = bsc.getBlobContainerAsyncClient(containerName);
        BlockBlobAsyncClient blockBlobAsyncClient = cc.getBlobAsyncClient(bu.getBlobName()).getBlockBlobAsyncClient();
        BlobRange range = setCount ? new BlobRange(0L, (long) flux.getScenarioData().remaining())
            : new BlobRange(0);

        StepVerifier.create(blockBlobAsyncClient.downloadStreamWithResponse(range, options, null, false))
            .assertNext(response -> {
                TestUtils.assertArraysEqual(FluxUtil.collectBytesInByteBufferStream(response.getValue()).block(),
                    flux.getScenarioData().array());
                assertEquals(tryNumber, flux.getTryNumber());
            }).verifyComplete();
    }

    private static Stream<Arguments> successfulSupplier() {
        return Stream.of(Arguments.of(DownloadResponseMockFlux.DR_TEST_SCENARIO_SUCCESSFUL_ONE_CHUNK, 1, true),
            Arguments.of(DownloadResponseMockFlux.DR_TEST_SCENARIO_SUCCESSFUL_MULTI_CHUNK, 1, true),
            Arguments.of(DownloadResponseMockFlux.DR_TEST_SCENARIO_SUCCESSFUL_STREAM_FAILURES, 4, true),
            Arguments.of(DownloadResponseMockFlux.DR_TEST_SCENARIO_NO_MULTIPLE_SUBSCRIPTION, 4, true),
            // Range download
            Arguments.of(DownloadResponseMockFlux.DR_TEST_SCENARIO_ERROR_AFTER_ALL_DATA, 1, true),
            // Non-range download
            Arguments.of(DownloadResponseMockFlux.DR_TEST_SCENARIO_ERROR_AFTER_ALL_DATA, 1, false));
    }

    @ParameterizedTest
    @MethodSource("failureSupplier")
    public void failure(int scenario, Class<?> exceptionType, int tryNumber) {
        DownloadResponseMockFlux flux = new DownloadResponseMockFlux(scenario, this);
        DownloadRetryOptions options = new DownloadRetryOptions().setMaxRetryRequests(5);

        BlobServiceAsyncClient bsc = getServiceClientBuilder(ENVIRONMENT.getPrimaryAccount().getCredential(),
            primaryBlobServiceClient.getAccountUrl(), flux.asPolicy()).buildAsyncClient();
        BlobContainerAsyncClient cc = bsc.getBlobContainerAsyncClient(containerName);
        BlockBlobAsyncClient blockBlobAsyncClient = cc.getBlobAsyncClient(bu.getBlobName()).getBlockBlobAsyncClient();

        BlobDownloadAsyncResponse response = blockBlobAsyncClient.downloadStreamWithResponse(null, options, null, false)
            .block();
        assertNotNull(response);

        StepVerifier.create(response.getValue())
            .verifyErrorSatisfies(e -> {
                Throwable cause = Exceptions.unwrap(e);
                assertInstanceOf(exceptionType, cause);
                assertEquals(tryNumber, flux.getTryNumber());
            });
    }

    private static Stream<Arguments> failureSupplier() {
        return Stream.of(
            Arguments.of(DownloadResponseMockFlux.DR_TEST_SCENARIO_MAX_RETRIES_EXCEEDED, IOException.class, 6),
            Arguments.of(DownloadResponseMockFlux.DR_TEST_SCENARIO_NON_RETRYABLE_ERROR, Exception.class, 1),
            Arguments.of(DownloadResponseMockFlux.DR_TEST_SCENARIO_ERROR_GETTER_MIDDLE, BlobStorageException.class, 2)
        );
    }

    @ParameterizedTest
    // We test retry count elsewhere. Just using small numbers to speed up the test.
    @CsvSource(value = {"0,1"})
    public void timeout(int retryCount) {
        DownloadResponseMockFlux flux =
            new DownloadResponseMockFlux(DownloadResponseMockFlux.DR_TEST_SCENARIO_TIMEOUT, this);
        DownloadRetryOptions options = new DownloadRetryOptions().setMaxRetryRequests(retryCount);

        BlobServiceAsyncClient bsc = getServiceClientBuilder(ENVIRONMENT.getPrimaryAccount().getCredential(),
            primaryBlobServiceClient.getAccountUrl(), flux.asPolicy()).buildAsyncClient();
        BlobContainerAsyncClient cc = bsc.getBlobContainerAsyncClient(containerName);
        BlockBlobAsyncClient blockBlobAsyncClient = cc.getBlobAsyncClient(bu.getBlobName()).getBlockBlobAsyncClient();

        Flux<ByteBuffer> bufferMono = blockBlobAsyncClient.downloadStreamWithResponse(null, options, null, false)
            .flatMapMany(r -> r.getValue());

        StepVerifier.create(bufferMono.timeout(Duration.ofSeconds(1)))
            .expectSubscription()
            .verifyErrorSatisfies(it -> assertInstanceOf(TimeoutException.class, Exceptions.unwrap(it)));
    }
}
