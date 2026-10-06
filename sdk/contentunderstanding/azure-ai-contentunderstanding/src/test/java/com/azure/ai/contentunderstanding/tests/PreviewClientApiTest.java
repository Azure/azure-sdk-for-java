// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentunderstanding.tests;

import com.azure.ai.contentunderstanding.ContentUnderstandingAsyncClient;
import com.azure.ai.contentunderstanding.ContentUnderstandingClient;
import com.azure.ai.contentunderstanding.models.AnalyzeBinaryOptions;
import com.azure.ai.contentunderstanding.models.AnalyzeOptions;
import com.azure.ai.contentunderstanding.models.ContentAnalyzerInlineResponse;
import com.azure.ai.contentunderstanding.models.ContentRange;
import com.azure.ai.contentunderstanding.models.ProcessingLocation;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.polling.PollerFlux;
import com.azure.core.util.polling.SyncPoller;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for the public client surface added for preview analysis operations.
 */
public class PreviewClientApiTest {

    @Test
    public void syncClientExposesMinimalAndOptionsInlineConvenienceMethods() throws NoSuchMethodException {
        assertReturnType(ContentUnderstandingClient.class, "beginAnalyze", SyncPoller.class, String.class, List.class,
            AnalyzeOptions.class);
        assertReturnType(ContentUnderstandingClient.class, "beginAnalyzeBinary", SyncPoller.class, String.class,
            BinaryData.class, AnalyzeBinaryOptions.class);
        assertReturnType(ContentUnderstandingClient.class, "analyzeInline", ContentAnalyzerInlineResponse.class,
            String.class, List.class);
        assertReturnType(ContentUnderstandingClient.class, "analyzeInline", ContentAnalyzerInlineResponse.class,
            String.class, List.class, AnalyzeOptions.class);
        assertReturnType(ContentUnderstandingClient.class, "analyzeBinaryInline", ContentAnalyzerInlineResponse.class,
            String.class, BinaryData.class);
        assertReturnType(ContentUnderstandingClient.class, "analyzeBinaryInline", ContentAnalyzerInlineResponse.class,
            String.class, BinaryData.class, AnalyzeBinaryOptions.class);

        assertMethodMissing(ContentUnderstandingClient.class, "analyzeInline", String.class, List.class, Map.class,
            ProcessingLocation.class);
        assertMethodMissing(ContentUnderstandingClient.class, "analyzeInlineWithResponse", String.class, List.class,
            AnalyzeOptions.class, Context.class);
        assertMethodMissing(ContentUnderstandingClient.class, "analyzeInlineDetailsWithResponse", String.class,
            List.class, AnalyzeOptions.class, Context.class);
        assertMethodMissing(ContentUnderstandingClient.class, "analyzeBinaryInline", String.class, BinaryData.class,
            ContentRange.class, String.class, ProcessingLocation.class);
        assertMethodMissing(ContentUnderstandingClient.class, "analyzeBinaryInlineWithResponse", String.class,
            BinaryData.class, AnalyzeBinaryOptions.class, Context.class);
        assertMethodMissing(ContentUnderstandingClient.class, "analyzeBinaryInlineDetailsWithResponse", String.class,
            BinaryData.class, AnalyzeBinaryOptions.class, Context.class);

        assertEquals(Arrays.stream(new Integer[] { 2, 3 }).collect(Collectors.toSet()),
            publicParameterCounts(ContentUnderstandingClient.class, "analyzeInline"));
        assertEquals(Arrays.stream(new Integer[] { 2, 3 }).collect(Collectors.toSet()),
            publicParameterCounts(ContentUnderstandingClient.class, "analyzeBinaryInline"));
    }

    @Test
    public void asyncClientExposesMinimalAndOptionsInlineConvenienceMethods() throws NoSuchMethodException {
        assertReturnType(ContentUnderstandingAsyncClient.class, "beginAnalyze", PollerFlux.class, String.class,
            List.class, AnalyzeOptions.class);
        assertReturnType(ContentUnderstandingAsyncClient.class, "beginAnalyzeBinary", PollerFlux.class, String.class,
            BinaryData.class, AnalyzeBinaryOptions.class);
        assertGenericReturnType(ContentUnderstandingAsyncClient.class, "analyzeInline", Mono.class,
            ContentAnalyzerInlineResponse.class, String.class, List.class);
        assertGenericReturnType(ContentUnderstandingAsyncClient.class, "analyzeInline", Mono.class,
            ContentAnalyzerInlineResponse.class, String.class, List.class, AnalyzeOptions.class);
        assertGenericReturnType(ContentUnderstandingAsyncClient.class, "analyzeBinaryInline", Mono.class,
            ContentAnalyzerInlineResponse.class, String.class, BinaryData.class);
        assertGenericReturnType(ContentUnderstandingAsyncClient.class, "analyzeBinaryInline", Mono.class,
            ContentAnalyzerInlineResponse.class, String.class, BinaryData.class, AnalyzeBinaryOptions.class);

        assertMethodMissing(ContentUnderstandingAsyncClient.class, "analyzeInline", String.class, List.class, Map.class,
            ProcessingLocation.class);
        assertMethodMissing(ContentUnderstandingAsyncClient.class, "analyzeInlineWithResponse", String.class,
            List.class, AnalyzeOptions.class);
        assertMethodMissing(ContentUnderstandingAsyncClient.class, "analyzeInlineDetailsWithResponse", String.class,
            List.class, AnalyzeOptions.class);
        assertMethodMissing(ContentUnderstandingAsyncClient.class, "analyzeBinaryInline", String.class,
            BinaryData.class, ContentRange.class, String.class, ProcessingLocation.class);
        assertMethodMissing(ContentUnderstandingAsyncClient.class, "analyzeBinaryInlineWithResponse", String.class,
            BinaryData.class, AnalyzeBinaryOptions.class);
        assertMethodMissing(ContentUnderstandingAsyncClient.class, "analyzeBinaryInlineDetailsWithResponse",
            String.class, BinaryData.class, AnalyzeBinaryOptions.class);

        assertEquals(Arrays.stream(new Integer[] { 2, 3 }).collect(Collectors.toSet()),
            publicParameterCounts(ContentUnderstandingAsyncClient.class, "analyzeInline"));
        assertEquals(Arrays.stream(new Integer[] { 2, 3 }).collect(Collectors.toSet()),
            publicParameterCounts(ContentUnderstandingAsyncClient.class, "analyzeBinaryInline"));
    }

    private static void assertReturnType(Class<?> clientType, String methodName, Class<?> returnType,
        Class<?>... parameterTypes) throws NoSuchMethodException {
        Method method = clientType.getMethod(methodName, parameterTypes);
        assertEquals(returnType, method.getReturnType());
    }

    private static void assertGenericReturnType(Class<?> clientType, String methodName, Class<?> rawReturnType,
        Class<?> typeArgument, Class<?>... parameterTypes) throws NoSuchMethodException {
        Method method = clientType.getMethod(methodName, parameterTypes);
        assertEquals(rawReturnType, method.getReturnType());
        ParameterizedType parameterizedType = (ParameterizedType) method.getGenericReturnType();
        assertEquals(typeArgument, parameterizedType.getActualTypeArguments()[0]);
    }

    private static Set<Integer> publicParameterCounts(Class<?> clientType, String methodName) {
        return java.util.Arrays.stream(clientType.getDeclaredMethods())
            .filter(method -> method.getName().equals(methodName))
            .filter(method -> Modifier.isPublic(method.getModifiers()))
            .map(Method::getParameterCount)
            .collect(Collectors.toSet());
    }

    private static void assertMethodMissing(Class<?> clientType, String methodName, Class<?>... parameterTypes) {
        assertThrows(NoSuchMethodException.class, () -> clientType.getMethod(methodName, parameterTypes));
    }
}
