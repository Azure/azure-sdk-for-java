// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.test.shared.policy;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelineNextSyncPolicy;
import com.azure.core.http.HttpPipelinePosition;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.function.Predicate;

public final class RequestAssertionPolicy implements HttpPipelinePolicy {

    private final Predicate<HttpRequest> requestPredicate;
    private final String message;

    public RequestAssertionPolicy(Predicate<HttpRequest> requestPredicate, String message) {
        this.requestPredicate = Objects.requireNonNull(requestPredicate);
        this.message = Objects.requireNonNull(message);
    }


    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        if (!requestPredicate.test(context.getHttpRequest())) {
            return Mono.error(new IllegalStateException(message));
        }
        return next.process();
    }

    @Override
    public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next) {
        if (!requestPredicate.test(context.getHttpRequest())) {
            throw new IllegalStateException(message);
        }
        return next.processSync();
    }

    @Override
    public HttpPipelinePosition getPipelinePosition() {
        return HttpPipelinePosition.PER_CALL;
    }

    /**
     * Policy to assert an outgoing request contains a given header.
     * @param headerName Header name to check for.
     * @return Pipeline policy.
     */
    public static RequestAssertionPolicy getHeaderExistsAssertionPolicy(String headerName) {
        return getHeaderExistsAssertionPolicy(headerName, req -> true);
    }

    /**
     * Policy to assert an outgoing request contains a given header.
     * @param headerName Header name to check for.
     * @param shouldCheckRequest Predicate for whether a particular request should be checked. Useful for something
     *                           like block blob partitioned upload, where we want to check stage block calls but not
     *                           commit block list.
     * @return Pipeline policy.
     */
    public static RequestAssertionPolicy getHeaderExistsAssertionPolicy(String headerName, Predicate<HttpRequest> shouldCheckRequest) {
        return getHeaderAssertionPolicy(headerName, Objects::nonNull,
            String.format("Expected value for header %s but was none.", headerName), shouldCheckRequest);
    }

    /**
     * Policy to assert an outgoing request contains a given header.
     * @param headerName Header name to check for.
     * @return Pipeline policy.
     */
    public static RequestAssertionPolicy getHeaderNotExistsAssertionPolicy(String headerName) {
        return getHeaderNotExistsAssertionPolicy(headerName, req -> true);
    }

    /**
     * Policy to assert an outgoing request contains a given header.
     * @param headerName Header name to check for.
     * @param shouldCheckRequest Predicate for whether a particular request should be checked. Useful for something
     *                           like block blob partitioned upload, where we want to check stage block calls but not
     *                           commit block list.
     * @return Pipeline policy.
     */
    public static RequestAssertionPolicy getHeaderNotExistsAssertionPolicy(String headerName, Predicate<HttpRequest> shouldCheckRequest) {
        return getHeaderAssertionPolicy(headerName, Objects::isNull,
            String.format("Expected no value for header %s but found one.", headerName), shouldCheckRequest);
    }

    /**
     * Policy to assert an outgoing request contains a given header with the provided value.
     * @param headerName Header name to check for.
     * @param expectedValue Value to check for.
     * @return Pipeline policy.
     */
    public static RequestAssertionPolicy getHeaderEqualsAssertionPolicy(String headerName, String expectedValue) {
        return getHeaderEqualsAssertionPolicy(headerName, expectedValue, req -> true);
    }

    /**
     * Policy to assert an outgoing request contains a given header with the provided value.
     * @param headerName Header name to check for.
     * @param expectedValue Value to check for.
     * @param shouldCheckRequest Predicate for whether a particular request should be checked. Useful for something
     *                           like block blob partitioned upload, where we want to check stage block calls but not
     *                           commit block list.
     * @return Pipeline policy.
     */
    public static RequestAssertionPolicy getHeaderEqualsAssertionPolicy(String headerName, String expectedValue,
        Predicate<HttpRequest> shouldCheckRequest) {
        return getHeaderAssertionPolicy(headerName, expectedValue::equals,
            String.format("Expected value for header %s but was none.", headerName), shouldCheckRequest);
    }

    /**
     * Policy to assert an outgoing request contains a given header that satisfies a predicate.
     * @param headerName Header name to check for.
     * @param predicate Predicate to apply to header value.
     * @param errorMessage Error message if predicate fails.
     * @return Pipeline policy.
     */
    public static RequestAssertionPolicy getHeaderAssertionPolicy(String headerName, Predicate<String> predicate,
        String errorMessage) {
        return getHeaderAssertionPolicy(headerName, predicate, errorMessage, req -> true);
    }

    /**
     * Policy to assert an outgoing request contains a given header that satisfies a predicate.
     * @param headerName Header name to check for.
     * @param predicate Predicate to apply to header value.
     * @param errorMessage Error message if predicate fails.
     * @param shouldCheckRequest Predicate for whether a particular request should be checked. Useful for something
     *                           like block blob partitioned upload, where we want to check stage block calls but not
     *                           commit block list.
     * @return Pipeline policy.
     */
    private static RequestAssertionPolicy getHeaderAssertionPolicy(String headerName, Predicate<String> predicate,
        String errorMessage, Predicate<HttpRequest> shouldCheckRequest) {
        Objects.requireNonNull(headerName);
        Objects.requireNonNull(predicate);
        Objects.requireNonNull(errorMessage);
        Objects.requireNonNull(shouldCheckRequest);
        return new RequestAssertionPolicy(req -> {
            if (shouldCheckRequest.test(req)) {
                return predicate.test(req.getHeaders().getValue(headerName));
            }
            return true;
        }, errorMessage);
    }

    public static boolean isStageBlock(HttpRequest request) {
        return request.getUrl().getQuery().contains("comp=block");
    }

    public static boolean isCommitBlockList(HttpRequest request) {
        return request.getUrl().getQuery().contains("comp=blocklist");
    }

    public static boolean isDataLakeAppend(HttpRequest request) {
        return request.getUrl().getQuery().contains("action=append");
    }
}
