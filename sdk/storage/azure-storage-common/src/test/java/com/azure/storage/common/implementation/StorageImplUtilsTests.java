// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.common.implementation;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.util.Context;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.storage.common.policy.DataLocalityPolicy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StorageImplUtilsTests {

    @ParameterizedTest
    @MethodSource("exceptionCallables")
    void sendRequestThrowsExceptions(Callable<?> operation, Class<? extends Exception> expectedCauseType) {
        RuntimeException e = assertThrows(RuntimeException.class,
            () -> StorageImplUtils.sendRequest(operation, Duration.ofSeconds(120), HttpResponseException.class));

        assertNotNull(e.getCause());
        assertInstanceOf(expectedCauseType, e.getCause());
    }

    @ParameterizedTest
    @MethodSource("etagValues")
    void toETagHeaderValueConvertsExpectedValues(String input, String expected) {
        assertEquals(expected, StorageImplUtils.toETagHeaderValue(input));
    }

    @ParameterizedTest
    @MethodSource("etagValues")
    void toETagHeaderValueIsIdempotent(String input, String expected) {
        String firstPass = StorageImplUtils.toETagHeaderValue(input);
        assertEquals(expected, firstPass);
        assertEquals(firstPass, StorageImplUtils.toETagHeaderValue(firstPass));
    }

    @Test
    void addDataLocalityEndpointReturnsContextUnchangedWhenEndpointIsNull() {
        Context context = Context.NONE;
        Context result = StorageImplUtils.addDataLocalityEndpoint(context, null);

        assertSame(context, result);
        assertFalse(Context.NONE.getData(DataLocalityPolicy.LAYOUT_ENDPOINT_KEY).isPresent());
    }

    @Test
    void addDataLocalityEndpointReturnsContextUnchangedWhenEndpointIsEmpty() {
        Context context = new Context("key", "value");
        Context result = StorageImplUtils.addDataLocalityEndpoint(context, "");

        assertSame(context, result);
    }

    @Test
    void addDataLocalityEndpointUsesContextNoneWhenContextIsNull() {
        Context result = StorageImplUtils.addDataLocalityEndpoint(null, "layout.example.net:8443");

        assertNotNull(result);
        assertTrue(result.getData(DataLocalityPolicy.LAYOUT_ENDPOINT_KEY).isPresent());
        assertEquals("layout.example.net:8443", result.getData(DataLocalityPolicy.LAYOUT_ENDPOINT_KEY).get());
    }

    @Test
    void addDataLocalityEndpointPreservesExistingContextData() {
        Context context = new Context("existingKey", "existingValue");
        Context result = StorageImplUtils.addDataLocalityEndpoint(context, "layout.example.net");

        assertTrue(result.getData("existingKey").isPresent());
        assertEquals("existingValue", result.getData("existingKey").get());
        assertTrue(result.getData(DataLocalityPolicy.LAYOUT_ENDPOINT_KEY).isPresent());
        assertEquals("layout.example.net", result.getData(DataLocalityPolicy.LAYOUT_ENDPOINT_KEY).get());
    }

    @Test
    void pipelineSupportsDataLocalityReturnsTrueWhenPolicyIsPresent() {
        HttpPipeline pipeline
            = new HttpPipelineBuilder().httpClient(request -> Mono.just(new MockHttpResponse(request, 200)))
                .policies(new DataLocalityPolicy())
                .build();

        assertTrue(StorageImplUtils.pipelineSupportsDataLocality(pipeline));
    }

    @Test
    void pipelineSupportsDataLocalityReturnsFalseWhenPolicyIsMissing() {
        HttpPipeline pipeline
            = new HttpPipelineBuilder().httpClient(request -> Mono.just(new MockHttpResponse(request, 200))).build();

        assertFalse(StorageImplUtils.pipelineSupportsDataLocality(pipeline));
    }

    @Test
    void pipelineSupportsDataLocalityReturnsFalseWhenPipelineIsNull() {
        assertFalse(StorageImplUtils.pipelineSupportsDataLocality(null));
    }

    private static Stream<Arguments> exceptionCallables() {
        Callable<Object> timeoutCallable = () -> {
            throw new TimeoutException();
        };

        Callable<Object> runtimeCallable = () -> {
            throw new RuntimeException("rt");
        };

        Callable<Object> executionCallable = () -> {
            throw new ExecutionException("exec", new RuntimeException("inner"));
        };

        Callable<Object> interruptedCallable = () -> {
            throw new InterruptedException("interrupted");
        };

        return Stream.of(Arguments.of(timeoutCallable, TimeoutException.class),
            Arguments.of(runtimeCallable, RuntimeException.class),
            Arguments.of(executionCallable, ExecutionException.class),
            Arguments.of(interruptedCallable, InterruptedException.class));
    }

    private static Stream<Arguments> etagValues() {
        return Stream.of(Arguments.of(null, null), Arguments.of("", ""),
            Arguments.of(Constants.HeaderConstants.ETAG_WILDCARD, Constants.HeaderConstants.ETAG_WILDCARD),
            Arguments.of("0x8DABC", "\"0x8DABC\""), Arguments.of("\"0x8DABC\"", "\"0x8DABC\""),
            Arguments.of("W/\"0x8DABC\"", "W/\"0x8DABC\""), Arguments.of("\"\"", "\"\""));
    }
}
