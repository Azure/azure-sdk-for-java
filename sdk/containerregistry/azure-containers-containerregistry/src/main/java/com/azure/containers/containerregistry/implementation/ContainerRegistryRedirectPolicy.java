// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry.implementation;

import com.azure.core.http.HttpHeaders;
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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static com.azure.containers.containerregistry.implementation.UtilsImpl.DOCKER_DIGEST_HEADER_NAME;
import static com.azure.containers.containerregistry.implementation.UtilsImpl.getDigestFromHeader;

/**
 * <p> Redirect policy for the container registry.</p>
 *
 * <p> This reads some of the headers that are returned from the redirect call that core redirect policy does not handle.</p>
 */
public final class ContainerRegistryRedirectPolicy implements HttpPipelinePolicy {
    private static final ClientLogger LOGGER = new ClientLogger(ContainerRegistryRedirectPolicy.class);
    private static final int MAX_REDIRECT_ATTEMPTS;
    private static final String REDIRECT_LOCATION_HEADER_NAME;
    private static final int PERMANENT_REDIRECT_STATUS_CODE;
    private static final int TEMPORARY_REDIRECT_STATUS_CODE;
    private static final Set<HttpMethod> REDIRECT_ALLOWED_METHODS;
    private static final String AUTHORIZATION;

    static {
        REDIRECT_ALLOWED_METHODS = new HashSet<>(Arrays.asList(HttpMethod.GET, HttpMethod.HEAD));
        PERMANENT_REDIRECT_STATUS_CODE = 308;
        TEMPORARY_REDIRECT_STATUS_CODE = 307;
        REDIRECT_LOCATION_HEADER_NAME = "Location";
        MAX_REDIRECT_ATTEMPTS = 3;
        AUTHORIZATION = "Authorization";
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        context.setHttpRequest(context.getHttpRequest().copy());
        return next.clone().process()
            .flatMap(httpResponse -> {
                if (!isRedirecResponse(httpResponse)) {
                    return Mono.just(httpResponse);
                }

                return attemptRedirect(context, next, httpResponse,1, new HashSet<>());
            });
    }

    @Override
    public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next) {
        context.setHttpRequest(context.getHttpRequest().copy());
        HttpResponse httpResponse = next.clone().processSync();
        if (!isRedirecResponse(httpResponse)) {
            return httpResponse;
        }

        return attemptRedirectSync(context, next, httpResponse, 1, new HashSet<>());
    }

    private static HttpResponse mapResponse(HttpResponse oldResponse, HttpResponse newResponse) {
        String digest = getDigestFromHeader(oldResponse.getHeaders());
        if (digest != null) {
            newResponse.getHeaders().set(DOCKER_DIGEST_HEADER_NAME, digest);
        }
        return newResponse;
    }

    @Override
    public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next) {
        return this.attemptRedirectSync(context, next, context.getHttpRequest(), 1, new HashSet<>());
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

        return next.clone().process().flatMap((httpResponse) -> {
            if (this.shouldAttemptRedirect(context, httpResponse, redirectAttempt, attemptedRedirectUrls)) {
                HttpRequest redirectRequestCopy = this.createRedirectRequest(httpResponse);
                return httpResponse.getBody().ignoreElements()
                    .then(this.attemptRedirect(context, next, redirectRequestCopy, redirectAttempt + 1, attemptedRedirectUrls))
                    .flatMap(newResponse -> {
                        String digest = getDigestFromHeader(httpResponse.getHeaders());
                        if (digest != null) {
                            newResponse.getHeaders().set(DOCKER_DIGEST_HEADER_NAME, digest);
                        }
                        return Mono.just(newResponse);
                    });
            } else {
                return Mono.just(httpResponse);
            }

            return attemptRedirect(context, next, httpResponse, redirectAttempt + 1, attemptedRedirectUrls);
        });
    }

    private HttpResponse attemptRedirectSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next,
                                             HttpResponse redirectResponse,
                                             int redirectAttempt, Set<String> attemptedRedirectUrls) {

        final String redirectUrl = redirectResponse.getHeaderValue(HttpHeaderName.LOCATION);
        if (!shouldAttemptRedirect(redirectUrl,redirectAttempt + 1, attemptedRedirectUrls)) {
            return redirectResponse;
        }

        HttpRequest redirectRequest = createRedirectRequest(redirectResponse, redirectUrl);
        context.setHttpRequest(redirectRequest.copy());

        HttpResponse httpResponse = next.clone().processSync();
        if (!isRedirecResponse(httpResponse)) {
            return mapResponse(redirectResponse, httpResponse);
        }

        return attemptRedirectSync(context, next, httpResponse, redirectAttempt + 1, attemptedRedirectUrls);

    }

    public boolean shouldAttemptRedirect(String redirectUrl, int tryCount, Set<String> attemptedRedirectUrls) {
        if (tryCount >= MAX_REDIRECT_ATTEMPTS) {
            LOGGER.error("Request has been redirected more than " + MAX_REDIRECT_ATTEMPTS + "times");
            return false;
        } else {
            return true;
        }
    }


    private HttpRequest createRedirectRequest(HttpResponse httpResponse, String redirectUrl) {
        httpResponse.getRequest().getHeaders().remove(HttpHeaderName.AUTHORIZATION);
        return httpResponse.getRequest().setUrl(redirectUrl);
    }

    private boolean isValidRedirectStatusCode(int statusCode) {
        return statusCode == PERMANENT_REDIRECT_STATUS_CODE || statusCode == TEMPORARY_REDIRECT_STATUS_CODE;
    }
}

