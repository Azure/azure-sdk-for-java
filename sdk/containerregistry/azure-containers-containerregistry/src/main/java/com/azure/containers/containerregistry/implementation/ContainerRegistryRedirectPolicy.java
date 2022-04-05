// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry.implementation;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
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
        return this.attemptRedirect(context, next, context.getHttpRequest(), 1, new HashSet<>());
    }

    /**
     * Function to process through the HTTP Response received in the pipeline
     * and redirect sending the request with new redirect url.
     */
    private Mono<HttpResponse> attemptRedirect(HttpPipelineCallContext context, HttpPipelineNextPolicy next, HttpRequest originalHttpRequest, int redirectAttempt, Set<String> attemptedRedirectUrls) {
        context.setHttpRequest(originalHttpRequest.copy());
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
        });
    }

    public boolean shouldAttemptRedirect(HttpPipelineCallContext context, HttpResponse httpResponse, int tryCount, Set<String> attemptedRedirectUrls) {
        if (this.isValidRedirectStatusCode(httpResponse.getStatusCode()) && this.isValidRedirectCount(tryCount) && this.isAllowedRedirectMethod(httpResponse.getRequest().getHttpMethod())) {
            String redirectUrl = this.tryGetRedirectHeader(httpResponse.getHeaders(), REDIRECT_LOCATION_HEADER_NAME);
            if (redirectUrl != null && !this.alreadyAttemptedRedirectUrl(redirectUrl, attemptedRedirectUrls)) {
                LOGGER.verbose("[Redirecting] Try count:" + tryCount + ", Attempted Redirect URLs:" + String.join(",", attemptedRedirectUrls));
                attemptedRedirectUrls.add(redirectUrl);
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private HttpRequest createRedirectRequest(HttpResponse httpResponse) {
        String responseLocation = this.tryGetRedirectHeader(httpResponse.getHeaders(), REDIRECT_LOCATION_HEADER_NAME);
        HttpRequest request = httpResponse.getRequest();
        request.setUrl(responseLocation);
        request.getHeaders().remove(AUTHORIZATION);
        return httpResponse.getRequest().setUrl(responseLocation);
    }

    private boolean alreadyAttemptedRedirectUrl(String redirectUrl, Set<String> attemptedRedirectUrls) {
        if (attemptedRedirectUrls.contains(redirectUrl)) {
            LOGGER.error("Request was redirected more than once to:" + redirectUrl);
            return true;
        } else {
            return false;
        }
    }

    private boolean isValidRedirectCount(int tryCount) {
        if (tryCount >= MAX_REDIRECT_ATTEMPTS) {
            LOGGER.error("Request has been redirected more than " + MAX_REDIRECT_ATTEMPTS + "times");
            return false;
        } else {
            return true;
        }
    }

    private boolean isAllowedRedirectMethod(HttpMethod httpMethod) {
        if (REDIRECT_ALLOWED_METHODS.contains(httpMethod)) {
            return true;
        } else {
            LOGGER.error("Request was redirected from an invalid redirect allowed method:" + httpMethod);
            return false;
        }
    }

    private boolean isValidRedirectStatusCode(int statusCode) {
        return statusCode == PERMANENT_REDIRECT_STATUS_CODE || statusCode == TEMPORARY_REDIRECT_STATUS_CODE;
    }

    String tryGetRedirectHeader(HttpHeaders headers, String headerName) {
        String headerValue = headers.getValue(headerName);
        if (CoreUtils.isNullOrEmpty(headerValue)) {
            LOGGER.error("Redirect url was null for header name:" + headerName + " request redirect was terminated.");
            return null;
        } else {
            return headerValue;
        }
    }
}

