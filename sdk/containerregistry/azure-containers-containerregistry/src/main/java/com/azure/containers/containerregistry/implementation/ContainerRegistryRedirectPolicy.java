// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry.implementation;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelineNextSyncPolicy;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.Set;

import static com.azure.containers.containerregistry.implementation.UtilsImpl.DOCKER_DIGEST_HEADER_NAME;

/**
 * <p> Redirect policy for the container registry.</p>
 *
 * <p> This reads some of the headers that are returned from the redirect call that core redirect policy does not handle.</p>
 */
public final class ContainerRegistryRedirectPolicy implements HttpPipelinePolicy {
    private static final ClientLogger LOGGER = new ClientLogger(ContainerRegistryRedirectPolicy.class);
    private static final int MAX_REDIRECT_ATTEMPTS = 3;
    private static final int PERMANENT_REDIRECT_STATUS_CODE = 308;
    private static final int TEMPORARY_REDIRECT_STATUS_CODE = 307;

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        context.setHttpRequest(context.getHttpRequest().copy());
        return next.clone().process()
            .flatMap(httpResponse -> {
                if (!isRedirectResponse(httpResponse)) {
                    return Mono.just(httpResponse);
                }

                return attemptRedirect(context, next, httpResponse, 1, new HashSet<>());
            });
    }

    @Override
    public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next) {
        context.setHttpRequest(context.getHttpRequest().copy());
        HttpResponse httpResponse = next.clone().processSync();
        if (!isRedirectResponse(httpResponse)) {
            return httpResponse;
        }

        return attemptRedirectSync(context, next, httpResponse, 1, new HashSet<>());
    }

    private static HttpResponse mapResponse(HttpResponse oldResponse, HttpResponse newResponse) {
        String digest = oldResponse.getHeaders().getValue(DOCKER_DIGEST_HEADER_NAME);
        if (digest != null) {
            newResponse.getHeaders().set(DOCKER_DIGEST_HEADER_NAME, digest);
        }
        return newResponse;
    }

    /**
     * Function to process through the HTTP Response received in the pipeline
     * and redirect sending the request with new redirect url.
     */
    private Mono<HttpResponse> attemptRedirect(HttpPipelineCallContext context, HttpPipelineNextPolicy next,
                                               HttpResponse redirectResponse,
                                               int redirectAttempt, Set<String> attemptedRedirectUrls) {
        final String redirectUrl = redirectResponse.getHeaderValue(HttpHeaderName.LOCATION);
        if (!shouldAttemptRedirect(redirectUrl, redirectAttempt + 1, attemptedRedirectUrls)) {
            return Mono.just(redirectResponse);
        }

        HttpRequest redirectRequest = createRedirectRequest(redirectResponse, redirectUrl);
        context.setHttpRequest(redirectRequest.copy());
        redirectResponse.close();

        return next.clone().process().flatMap((httpResponse) -> {
            if (!isRedirectResponse(httpResponse)) {
                return Mono.just(mapResponse(redirectResponse, httpResponse));
            }

            return attemptRedirect(context, next, httpResponse, redirectAttempt + 1, attemptedRedirectUrls);
        });
    }

    private HttpResponse attemptRedirectSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next,
                                             HttpResponse redirectResponse,
                                             int redirectAttempt, Set<String> attemptedRedirectUrls) {

        final String redirectUrl = redirectResponse.getHeaderValue(HttpHeaderName.LOCATION);
        if (!shouldAttemptRedirect(redirectUrl, redirectAttempt + 1, attemptedRedirectUrls)) {
            return redirectResponse;
        }

        HttpRequest redirectRequest = createRedirectRequest(redirectResponse, redirectUrl);
        context.setHttpRequest(redirectRequest.copy());
        redirectResponse.close();

        HttpResponse httpResponse = next.clone().processSync();
        if (!isRedirectResponse(httpResponse)) {
            return mapResponse(redirectResponse, httpResponse);
        }

        return attemptRedirectSync(context, next, httpResponse, redirectAttempt + 1, attemptedRedirectUrls);

    }

    public boolean shouldAttemptRedirect(String redirectUrl, int tryCount, Set<String> attemptedRedirectUrls) {
        if (tryCount >= MAX_REDIRECT_ATTEMPTS) {
            LOGGER.atError()
                .addKeyValue("tryCount", tryCount)
                .addKeyValue("maxAttempts", MAX_REDIRECT_ATTEMPTS)
                .log("Request has been redirected too many times.");
            return false;
        }

        if (CoreUtils.isNullOrEmpty(redirectUrl)) {
            LOGGER.error("Location header was null or empty for redirected request");
            return false;
        }

        if (!attemptedRedirectUrls.add(redirectUrl)) {
            LOGGER.atError()
                .addKeyValue("redirectUrl", redirectUrl)
                .addKeyValue("tryCount", tryCount)
                .log("Request was redirected more than once to the same location");
            return false;
        }

        LOGGER.atVerbose()
            .addKeyValue("redirectUrls", attemptedRedirectUrls::toString)
            .addKeyValue("redirectUrl", redirectUrl)
            .addKeyValue("tryCount", tryCount)
            .log("Redirecting.");

        return true;
    }


    private HttpRequest createRedirectRequest(HttpResponse httpResponse, String redirectUrl) {
        httpResponse.getRequest().getHeaders().remove(HttpHeaderName.AUTHORIZATION);
        return httpResponse.getRequest().setUrl(redirectUrl);
    }


    private boolean isRedirectResponse(HttpResponse httpResponse) {
        int responseStatusCode = httpResponse.getStatusCode();
        HttpMethod requestMethod = httpResponse.getRequest().getHttpMethod();

        return ((responseStatusCode == PERMANENT_REDIRECT_STATUS_CODE || responseStatusCode == TEMPORARY_REDIRECT_STATUS_CODE)
            && (requestMethod == HttpMethod.GET || requestMethod == HttpMethod.HEAD));
    }
}

