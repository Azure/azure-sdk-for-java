// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.secrets;


import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.Context;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Predicate;

/**
 * An HTTP Client that helps in providing assertions for invoking http client implementations.
 */
public final class AssertingClient implements HttpClient {
    private final HttpClient delegate;

    private final List<Predicate<HttpRequest>> syncAssertions;
    private final List<Predicate<HttpRequest>> asyncAssertions;
    AssertingClient(HttpClient delegate, List<Predicate<HttpRequest>> syncAssertions,
                    List<Predicate<HttpRequest>> asyncAssertions) {
        this.delegate = delegate;
        this.syncAssertions = syncAssertions;
        this.asyncAssertions = asyncAssertions;
    }

    @Override
    public Mono<HttpResponse> send(HttpRequest request) {
        for (Predicate<HttpRequest> asyncAssertion : asyncAssertions) {
            if (!asyncAssertion.test(request)) {
                return Mono.error(new IllegalStateException("unexpected request"));
            }
        }
        return delegate.send(request);
    }

    @Override
    public Mono<HttpResponse> send(HttpRequest request, Context context) {
        //TODO: @g2vinay stop skipping paging requests once #30031 resolved
        skipPagingRequest(context, asyncAssertions);
        //TODO: @g2vinay stop skipping polling requests once sync polling supports is added in core.
        skipPollingRequest(context, asyncAssertions);
        //TODO: @g2vinay stop skipping crypto request once sync cryptography client is migrated to sync stack in followup PR.
        for (Predicate<HttpRequest> asyncAssertion : asyncAssertions) {
            if (!asyncAssertion.test(request)) {
                return Mono.error(new IllegalStateException("unexpected request"));
            }
        }
        return delegate.send(request, context);
    }

    @Override
    public HttpResponse sendSync(HttpRequest request, Context context) {
        //skip paging requests until #30031 resolved
        skipPagingRequest(context, syncAssertions);
        skipPollingRequest(context, syncAssertions);
        for (Predicate<HttpRequest> syncAssertion : syncAssertions) {
            if (!syncAssertion.test(request)) {
                throw new IllegalStateException("unexpected request");
            }
        }
        return delegate.sendSync(request, context);
    }

    private void skipPagingRequest(Context context, List<Predicate<HttpRequest>> assertions) {
        String callerMethod = (String) context.getData("caller-method").orElse("");
        if ((callerMethod.contains("list") || callerMethod.contains("getSecrets") || callerMethod.contains("getSecretVersions")) && !assertions.isEmpty()) {
            assertions.remove(assertions.size() - 1);
        }
    }

    private void skipPollingRequest(Context context, List<Predicate<HttpRequest>> assertions) {
        String callerMethod = (String) context.getData("caller-method").orElse("");
        if ((callerMethod.contains("delete") || callerMethod.contains("recover")) && !assertions.isEmpty()) {
            assertions.remove(assertions.size() - 1);
        }
    }
}
