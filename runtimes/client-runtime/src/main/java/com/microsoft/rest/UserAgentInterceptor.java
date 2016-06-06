/**
 *
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 *
 */

package com.microsoft.rest;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

/**
 * User agent interceptor for putting a 'User-Agent' header in the request.
 */
public class UserAgentInterceptor implements Interceptor {
    /**
     * The default user agent header.
     */
    private static final String DEFAULT_USER_AGENT_HEADER = "AutoRest-Java";

    /**
     * The user agent header string.
     */
    private String userAgent;

    /**
     * Initialize an instance of {@link UserAgentInterceptor} class with the default
     * 'User-Agent' header.
     */
    public UserAgentInterceptor() {
        this.userAgent = DEFAULT_USER_AGENT_HEADER;
    }

    /**
     * Overwrite the User-Agent header.
     *
     * @param userAgent the new user agent value.
     */
    public void withUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    /**
     * Append a text to the User-Agent header.
     *
     * @param userAgent the user agent value to append.
     */
    public void appendUserAgent(String userAgent) {
        this.userAgent += " " + userAgent;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        String header = request.header("User-Agent");
        if (header == null) {
            header = DEFAULT_USER_AGENT_HEADER;
        }
        if (!userAgent.equals(DEFAULT_USER_AGENT_HEADER)) {
            if (header.equals(DEFAULT_USER_AGENT_HEADER)) {
                header = userAgent;
            } else {
                header = userAgent + " " + header;
            }
        }
        request = chain.request().newBuilder()
                .header("User-Agent", header)
                .build();
        return chain.proceed(request);
    }
}
