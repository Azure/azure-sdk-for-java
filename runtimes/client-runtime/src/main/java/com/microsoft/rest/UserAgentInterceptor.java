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
        setUserAgent(DEFAULT_USER_AGENT_HEADER, null, null);
    }

    public void setUserAgent(String userAgent) {
        setUserAgent(userAgent, null, null);
    }

    public void setUserAgent(String product, String version, String extras) {
        this.userAgent = product;
        if (version != null) {
            this.userAgent += "/" + version;
        }
        if (extras != null) {
            this.userAgent += String.format(" (%s)", extras);
        }
    }

    public void appendUserAgent(String userAgent) {
        appendUserAgent(userAgent, null, null);
    }

    public void appendUserAgent(String product, String version, String extras) {
        this.userAgent += " " + product;
        if (version != null) {
            this.userAgent += "/" + version;
        }
        if (extras != null) {
            this.userAgent += String.format(" (%s)", extras);
        }
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        String header = request.header("User-Agent");
        request = chain.request().newBuilder()
                .header("User-Agent", userAgent + " " + header)
                .build();
        return chain.proceed(request);
    }
}
