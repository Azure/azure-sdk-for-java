/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.utils;

import com.microsoft.azure.CloudException;
import com.microsoft.rest.http.BufferedHttpResponse;
import com.microsoft.rest.http.HttpRequest;
import com.microsoft.rest.http.HttpResponse;
import com.microsoft.rest.policy.RequestPolicy;
import org.slf4j.LoggerFactory;
import rx.Single;
import rx.functions.Func1;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * An interceptor for automatic retry when Azure Resource Manager is throttling because of too many read/write requests.
 * <p>
 * For each subscription and tenant, Azure Resource Manager limits read requests to 15,000 per hour and
 *   write requests to 1,200 per hour. These limits apply to each Azure Resource Manager instance.
 */
public final class ResourceManagerThrottlingPolicy implements RequestPolicy {
    private static final int HTTP_TOO_MANY_REQUESTS = 429;
    private final RequestPolicy next;

    /**
     * Factory for creating ResourceManagerThrottlingPolicy.
     */
    public static final class Factory implements RequestPolicy.Factory {
        @Override
        public RequestPolicy create(RequestPolicy next) {
            return new ResourceManagerThrottlingPolicy(next);
        }
    }

    private ResourceManagerThrottlingPolicy(RequestPolicy next) {
        this.next = next;
    }

    @Override
    public Single<HttpResponse> sendAsync(final HttpRequest request) {
        return next.sendAsync(request).flatMap(new Func1<HttpResponse, Single<HttpResponse>>() {
            @Override
            public Single<HttpResponse> call(final HttpResponse response) {
                // TODO: Check MIME?
                if (response.statusCode() != HTTP_TOO_MANY_REQUESTS) {
                    return Single.just(response);
                } else {
                    return response.buffer().flatMap(new Func1<BufferedHttpResponse, Single<HttpResponse>>() {
                        @Override
                        public Single<HttpResponse> call(BufferedHttpResponse bufferedHttpResponse) {
                            return delayIfTooManyRequests(request, bufferedHttpResponse);
                        }
                    });
                }
            }
        });
    }

    private Single<HttpResponse> delayIfTooManyRequests(final HttpRequest request, BufferedHttpResponse bufferedResponse) {
        String retryAfterHeader = bufferedResponse.headerValue("Retry-After");
        int retryAfter = 0;
        if (retryAfterHeader != null) {
            try {
                retryAfter = Integer.parseInt(retryAfterHeader);
            } catch (NumberFormatException e) {
                return Single.error(new CloudException("Invalid format for Retry-After header", bufferedResponse, null));
            }
        }

        if (retryAfter <= 0) {
            Pattern pattern = Pattern.compile("try again after '([0-9]*)' minutes", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(bufferedResponse.body());
            if (matcher.find()) {
                retryAfter = (int) TimeUnit.MINUTES.toSeconds(Integer.parseInt(matcher.group(1)));
            }
        }

        if (retryAfter > 0) {
            String context = request.callerMethod();
            LoggerFactory.getLogger(context)
                    .info("Azure Resource Manager read/write per hour limit reached. Will retry in: " + retryAfter + " seconds");

            return Single.just(null)
                    .delay(retryAfter, TimeUnit.SECONDS)
                    .flatMap(new Func1<Object, Single<? extends HttpResponse>>() {
                                @Override
                                public Single<HttpResponse> call(Object ignored) {
                                    return next.sendAsync(request);
                                }
                            });
        }

        return next.sendAsync(request);
    }
}
