// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.implementation.util;

import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.Context;
import com.azure.core.util.UrlBuilder;
import com.azure.core.util.logging.ClientLogger;

import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;

/**
 * Builds {@link RequestOptions} that scope an account-targeted protocol call to a specific share/directory/file
 * resource path.
 * <p>
 * The generated operation groups target the account-scoped service URL ({@code @HostParam("url")}); this helper adds a
 * request callback that rewrites the request URL path to the resource (for example {@code "{share}/{directoryPath}"})
 * via {@link UrlBuilder#setPath(String)} while preserving the route's query string. Applying the resource as the URL
 * path here — rather than baking it into the host URL — avoids the {@code /?} sequence azure-core inserts when joining
 * a host that has a path to a query-only route, which the service rejects for some operations (such as directory
 * rename) with {@code InvalidResourceName}. This mirrors the Queue Storage client's {@code RequestOptionsHelper}.
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
     * Builds a {@link RequestOptions} scoped to a share-level operation ({@code {share}}).
     *
     * @param context The {@link Context} to thread through the pipeline.
     * @param accountUrl The client's account-scoped base URL.
     * @param shareName The share name.
     * @return The scoped {@link RequestOptions}.
     */
    public static RequestOptions shareRequestOptions(Context context, String accountUrl, String shareName) {
        return scopeRequestToResourcePath(requestOptions(context), accountUrl, shareName);
    }

    /**
     * Builds a {@link RequestOptions} scoped to a directory-level operation ({@code {share}/{directoryPath}}).
     *
     * @param context The {@link Context} to thread through the pipeline.
     * @param accountUrl The client's account-scoped base URL.
     * @param shareName The share name.
     * @param directoryPath The directory path within the share; may be {@code null} or empty for the share root
     * directory.
     * @return The scoped {@link RequestOptions}.
     */
    public static RequestOptions directoryRequestOptions(Context context, String accountUrl, String shareName,
        String directoryPath) {
        return scopeRequestToResourcePath(requestOptions(context), accountUrl, shareName, directoryPath);
    }

    /**
     * Builds a {@link RequestOptions} scoped to a file-level operation ({@code {share}/{filePath}}).
     *
     * @param context The {@link Context} to thread through the pipeline.
     * @param accountUrl The client's account-scoped base URL.
     * @param shareName The share name.
     * @param filePath The file path within the share.
     * @return The scoped {@link RequestOptions}.
     */
    public static RequestOptions fileRequestOptions(Context context, String accountUrl, String shareName,
        String filePath) {
        return scopeRequestToResourcePath(requestOptions(context), accountUrl, shareName, filePath);
    }

    /**
     * Adds a request callback that sets the request URL path to the account path plus the (percent-encoded) resource
     * path, preserving the route's query string.
     *
     * @param requestOptions The {@link RequestOptions} to scope.
     * @param accountUrl The client's account-scoped base URL.
     * @param resourceSegments The unencoded resource segments within the account (for example {@code shareName} then
     * {@code directoryPath}). Each segment is encoded as a single unit, so a {@code '/'} inside a directory/file path
     * becomes {@code %2F} (matching the shipped {@code @PathParam} URL shape).
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
     * the Azurite emulator's {@code http://host/devstoreaccount1}) and empty for standard {@code account.file.*}
     * endpoints where the account is the host. Each share/directory/file segment is percent-encoded as a single unit;
     * the share boundary stays a literal {@code '/'} while a {@code '/'} inside a directory/file path is encoded to
     * {@code %2F}, reproducing the shipped {@code @PathParam} URL shape (the service treats {@code %2F} and {@code '/'}
     * identically, but recorded requests match on the exact bytes).
     */
    private static String resourcePath(String accountUrl, String... resourceSegments) {
        String accountPath = UrlBuilder.parse(accountUrl).getPath();
        StringBuilder path = new StringBuilder();
        if (accountPath != null && !accountPath.isEmpty() && !"/".equals(accountPath)) {
            path.append(accountPath.replaceAll("/+$", ""));
        }
        for (String segment : resourceSegments) {
            if (segment == null) {
                continue;
            }
            // An empty directory segment is the share root directory, addressed with a trailing slash ("share/").
            String normalized = segment.startsWith("/") ? segment.substring(1) : segment;
            path.append('/').append(encodePathSegment(normalized));
        }
        return path.toString();
    }

    // Matches azure-core's RestProxy @PathParam encoding (UrlEscapers.PATH_ESCAPER): percent-encodes a path segment,
    // keeping RFC 3986 pchars (unreserved + sub-delims + ':' + '@') and encoding everything else, notably '/' -> %2F.
    // Utility.urlEncode (URLEncoder) over-encodes pchars such as ':' -> %3A, which diverges from the shipped recordings.
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
