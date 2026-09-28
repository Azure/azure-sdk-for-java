// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.implementation.util;

import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.Context;
import com.azure.core.util.UrlBuilder;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.Utility;

import java.net.MalformedURLException;

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
        return scopeRequestToResourcePath(requestOptions(context), accountUrl, joinResource(shareName, directoryPath));
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
        return scopeRequestToResourcePath(requestOptions(context), accountUrl, joinResource(shareName, filePath));
    }

    /**
     * Adds a request callback that sets the request URL path to the account path plus the (percent-encoded) resource
     * path, preserving the route's query string.
     *
     * @param requestOptions The {@link RequestOptions} to scope.
     * @param accountUrl The client's account-scoped base URL.
     * @param resource The unencoded resource path within the account (for example {@code "share/dir"}).
     * @return The same {@link RequestOptions}, scoped.
     */
    public static RequestOptions scopeRequestToResourcePath(RequestOptions requestOptions, String accountUrl,
        String resource) {
        String path = resourcePath(accountUrl, resource);
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

    private static String joinResource(String shareName, String childPath) {
        if (childPath == null || childPath.isEmpty()) {
            return shareName;
        }
        // Strip any leading slash so it is not treated as an empty first segment when encoding.
        String normalizedChild = childPath.charAt(0) == '/' ? childPath.substring(1) : childPath;
        return normalizedChild.isEmpty() ? shareName : shareName + "/" + normalizedChild;
    }

    /**
     * Prefixes the resource path with the base URL's account path, which is present for path-style endpoints (such as
     * the Azurite emulator's {@code http://host/devstoreaccount1}) and empty for standard {@code account.file.*}
     * endpoints where the account is the host. Each resource segment is percent-encoded because the share/directory/file
     * names are supplied raw and, unlike the old RestProxy {@code @PathParam} layer, are not encoded on request.
     */
    private static String resourcePath(String accountUrl, String resource) {
        String accountPath = UrlBuilder.parse(accountUrl).getPath();
        StringBuilder path = new StringBuilder();
        if (accountPath != null && !accountPath.isEmpty() && !"/".equals(accountPath)) {
            path.append(accountPath.replaceAll("/+$", ""));
        }
        String[] segments = resource.split("/", -1);
        for (String segment : segments) {
            path.append('/').append(Utility.urlEncode(segment));
        }
        return path.toString();
    }
}
