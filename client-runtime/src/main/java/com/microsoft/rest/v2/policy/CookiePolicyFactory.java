/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.policy;

import com.microsoft.rest.v2.http.HttpHeader;
import com.microsoft.rest.v2.http.HttpRequest;
import com.microsoft.rest.v2.http.HttpResponse;
import io.reactivex.Single;
import io.reactivex.functions.Function;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Creates a RequestPolicy which stores cookies based on the response Set-Cookie header and adds cookies to requests.
 */
public final class CookiePolicyFactory implements RequestPolicyFactory {
    private final CookieHandler cookies = new CookieManager();

    @Override
    public RequestPolicy create(RequestPolicy next, RequestPolicyOptions options) {
        return new CookiePolicy(next);
    }

    private final class CookiePolicy implements RequestPolicy {
        private final RequestPolicy next;
        private CookiePolicy(RequestPolicy next) {
            this.next = next;
        }

        @Override
        public Single<HttpResponse> sendAsync(HttpRequest request) {
            try {
                final URI uri = request.url().toURI();

                Map<String, List<String>> cookieHeaders = new HashMap<>();
                for (HttpHeader header : request.headers()) {
                    cookieHeaders.put(header.name(), Arrays.asList(request.headers().values(header.name())));
                }

                Map<String, List<String>> requestCookies = cookies.get(uri, cookieHeaders);
                for (Map.Entry<String, List<String>> entry : requestCookies.entrySet()) {
                    request.headers().set(entry.getKey(), StringUtils.join(entry.getValue(), ","));
                }

                return next.sendAsync(request).map(new Function<HttpResponse, HttpResponse>() {
                    @Override
                    public HttpResponse apply(HttpResponse httpResponse) throws Exception {
                        Map<String, List<String>> responseHeaders = new HashMap<>();
                        for (HttpHeader header : httpResponse.headers()) {
                            responseHeaders.put(header.name(), Collections.singletonList(header.value()));
                        }

                        cookies.put(uri, responseHeaders);
                        return httpResponse;
                    }
                });
            } catch (URISyntaxException | IOException e) {
                return Single.error(e);
            }
        }
    }

}
