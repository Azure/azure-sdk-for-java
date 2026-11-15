// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.queue.implementation.util;

import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.Context;
import com.azure.core.util.UrlBuilder;
import com.azure.core.util.logging.ClientLogger;

import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;

/**
 * Builds {@link RequestOptions} that scope an account-targeted protocol call to a specific queue, its messages, or a
 * single message.
 * <p>
 * The generated operation groups target the account-scoped service URL ({@code @HostParam("url")}); this helper adds a
 * request callback that rewrites the request URL path to the resource (for example {@code "{queue}/messages"}) via
 * {@link UrlBuilder#setPath(String)} while preserving the route's query string. Applying the resource as the URL path
 * here, rather than baking it into the host URL, avoids the {@code /?} sequence azure-core inserts when joining a host
 * that has a path to a query-only route such as {@code ?comp=acl}. This mirrors the File Share client's
 * {@code RequestOptionsHelper}.
 */
public final class RequestOptionsHelper {
    private static final ClientLogger LOGGER = new ClientLogger(RequestOptionsHelper.class);

    private RequestOptionsHelper() {
    }

    /**
     * Creates a {@link RequestOptions} for an account-scoped protocol call, threading the supplied {@link Context}.
     *
     * @param context The {@link Context} to thread through the pipeline; may be {@code null}.
     * @return A new {@link RequestOptions}.
     */
    public static RequestOptions requestOptions(Context context) {
        RequestOptions requestOptions = new RequestOptions();
        if (context != null) {
            requestOptions.setContext(context);
        }
        return requestOptions;
    }

    /**
     * Builds a {@link RequestOptions} scoped to a queue-level operation ({@code {queue}}).
     *
     * @param context The {@link Context} to thread through the pipeline.
     * @param accountUrl The client's account-scoped base URL.
     * @param queueName The queue name.
     * @return The scoped {@link RequestOptions}.
     */
    public static RequestOptions queueRequestOptions(Context context, String accountUrl, String queueName) {
        return scopeRequestToResourcePath(requestOptions(context), accountUrl, queueName);
    }

    /**
     * Builds a {@link RequestOptions} scoped to a messages-level operation ({@code {queue}/messages}).
     *
     * @param context The {@link Context} to thread through the pipeline.
     * @param accountUrl The client's account-scoped base URL.
     * @param queueName The queue name.
     * @return The scoped {@link RequestOptions}.
     */
    public static RequestOptions messagesRequestOptions(Context context, String accountUrl, String queueName) {
        return scopeRequestToResourcePath(requestOptions(context), accountUrl, queueName, "messages");
    }

    /**
     * Builds a {@link RequestOptions} scoped to a message-level operation ({@code {queue}/messages/{messageId}}).
     *
     * @param context The {@link Context} to thread through the pipeline.
     * @param accountUrl The client's account-scoped base URL.
     * @param queueName The queue name.
     * @param messageId The message ID.
     * @return The scoped {@link RequestOptions}.
     */
    public static RequestOptions messageIdRequestOptions(Context context, String accountUrl, String queueName,
        String messageId) {
        return scopeRequestToResourcePath(requestOptions(context), accountUrl, queueName, "messages", messageId);
    }

    /**
     * Adds a request callback that sets the request URL path to the account path plus the (percent-encoded) resource
     * path, preserving the route's query string.
     *
     * @param requestOptions The {@link RequestOptions} to scope.
     * @param accountUrl The client's account-scoped base URL.
     * @param resourceSegments The unencoded resource segments within the account. Each segment is encoded as a single
     * unit, matching the shipped {@code @PathParam} URL shape.
     * @return The same {@link RequestOptions}, scoped.
     */
    public static RequestOptions scopeRequestToResourcePath(RequestOptions requestOptions, String accountUrl,
        String... resourceSegments) {
        String path = resourcePath(accountUrl, resourceSegments);
        requestOptions.addRequestCallback(request -> {
            UrlBuilder urlBuilder = UrlBuilder.parse(request.getUrl());
            urlBuilder.setPath(path);
            try {
                request.setUrl(urlBuilder.toUrl());
            } catch (MalformedURLException e) {
                throw LOGGER.logExceptionAsError(new IllegalStateException(e));
            }
        });
        return requestOptions;
    }

    /**
     * Prefixes the resource path with the base URL's account path, which is present for path-style endpoints (such as
     * the Azurite emulator's {@code http://host/devstoreaccount1}) and empty for standard {@code account.queue.*}
     * endpoints where the account is the host. Each segment is percent-encoded as a single unit.
     */
    private static String resourcePath(String accountUrl, String... resourceSegments) {
        String accountPath = UrlBuilder.parse(accountUrl).getPath();
        StringBuilder path = new StringBuilder();
        if (accountPath != null && !accountPath.isEmpty() && !"/".equals(accountPath)) {
            path.append(accountPath.replaceAll("/+$", ""));
        }
        for (String segment : resourceSegments) {
            path.append('/').append(encodePathSegment(segment));
        }
        return path.toString();
    }

    // Matches azure-core's RestProxy @PathParam encoding (UrlEscapers.PATH_ESCAPER): percent-encodes a path segment,
    // keeping RFC 3986 pchars (unreserved + sub-delims + ':' + '@') and encoding everything else, notably '/' -> %2F.
    // Utility.urlEncode (URLEncoder) over-encodes pchars such as ':' -> %3A, which diverges from the shipped behavior.
    private static final char[] HEX = "0123456789ABCDEF".toCharArray();
    private static final String PATH_SAFE_SYMBOLS = "-._~!$&'()*+,;=:@";

    private static String encodePathSegment(String value) {
        StringBuilder encoded = new StringBuilder(value.length());
        for (byte rawByte : value.getBytes(StandardCharsets.UTF_8)) {
            int b = rawByte & 0xFF;
            if ((b >= 'a' && b <= 'z')
                || (b >= 'A' && b <= 'Z')
                || (b >= '0' && b <= '9')
                || PATH_SAFE_SYMBOLS.indexOf(b) >= 0) {
                encoded.append((char) b);
            } else {
                encoded.append('%').append(HEX[b >> 4]).append(HEX[b & 0xF]);
            }
        }
        return encoded.toString();
    }
}
