// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.blob.implementation.util;

import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.Context;
import com.azure.core.util.UrlBuilder;
import com.azure.storage.common.Utility;

import java.net.MalformedURLException;
import java.net.URL;

import java.util.List;
import java.util.Objects;

/**
 * Builds the {@link RequestOptions} passed to the generated {@code implementation/*Impl} protocol methods.
 * <p>
 * The TypeSpec migration replaces the AutoRest explicit-parameter operation methods (which took every query
 * parameter, header, and body as a typed argument) with protocol-style {@code xxxWithResponse(RequestOptions)}
 * methods. The hand-written Blob clients keep their public API but translate their typed inputs into the storage
 * wire contract through these helpers, so the emitted request matches the pre-migration (AutoRest) behavior.
 * <p>
 * Route-level query parameters (e.g. {@code ?restype=service&comp=properties}) are baked into the generated
 * service interface, and {@code x-ms-version}/{@code Accept} are added by the impl, so those are not set here.
 */
public final class RequestOptionsHelper {

    private RequestOptionsHelper() {
    }

    /**
     * Creates a {@link RequestOptions} for a protocol call, threading the supplied {@link Context}.
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
     * Adds an optional query parameter to {@code requestOptions} when {@code value} is non-null, matching the
     * AutoRest behavior of omitting the parameter entirely when it has no value.
     *
     * @param requestOptions The {@link RequestOptions} to mutate.
     * @param name The query parameter name.
     * @param value The value; when {@code null} the parameter is not added.
     */
    public static void addOptionalQueryParam(RequestOptions requestOptions, String name, Object value) {
        if (value != null) {
            requestOptions.addQueryParam(name, String.valueOf(value));
        }
    }

    /**
     * Adds a comma-joined query parameter when {@code values} is non-null and non-empty.
     * <p>
     * AutoRest omitted the parameter entirely when the collection was empty rather than sending an empty value, and
     * recorded sessions depend on that: sending {@code include=} would not match.
     *
     * @param requestOptions The {@link RequestOptions} to mutate.
     * @param name The query parameter name.
     * @param values The values to join; when {@code null} or empty the parameter is not added.
     */
    public static void addOptionalCsvQueryParam(RequestOptions requestOptions, String name, List<?> values) {
        if (values == null || values.isEmpty()) {
            return;
        }
        StringBuilder joined = new StringBuilder();
        for (Object value : values) {
            if (joined.length() > 0) {
                joined.append(',');
            }
            joined.append(Objects.toString(value, ""));
        }
        requestOptions.addQueryParam(name, joined.toString());
    }

    /**
     * Builds a {@link RequestOptions} scoped to a container-level operation ({@code {containerName}}).
     *
     * @param context The {@link Context} to thread through the pipeline.
     * @param baseUrl The client's account-scoped base URL.
     * @param containerName The container name.
     * @return The scoped {@link RequestOptions}.
     */
    public static RequestOptions containerRequestOptions(Context context, String baseUrl, String containerName) {
        RequestOptions requestOptions = requestOptions(context);
        scopeRequestToResourcePath(requestOptions, resourcePath(baseUrl, Utility.urlEncode(containerName)));
        return requestOptions;
    }

    /**
     * Builds a {@link RequestOptions} scoped to a blob-level operation ({@code {containerName}/{blobName}}).
     *
     * @param context The {@link Context} to thread through the pipeline.
     * @param baseUrl The client's account-scoped base URL.
     * @param containerName The container name.
     * @param blobName The blob name; may contain path separators and characters requiring encoding.
     * @return The scoped {@link RequestOptions}.
     */
    public static RequestOptions blobRequestOptions(Context context, String baseUrl, String containerName,
        String blobName) {
        RequestOptions requestOptions = requestOptions(context);
        scopeRequestToResourcePath(requestOptions,
            resourcePath(baseUrl, Utility.urlEncode(containerName) + "/" + Utility.urlEncode(blobName)));
        return requestOptions;
    }

    /**
     * Prefixes the resource path with the base URL's account path, which is present for path-style endpoints (e.g.
     * the Azurite emulator's {@code http://host/devstoreaccount1}) and empty for standard {@code account.blob.*}
     * endpoints where the account is the host. Required because {@link #scopeRequestToResourcePath} sets the whole
     * URL path, so the account segment must be reintroduced explicitly.
     */
    private static String resourcePath(String baseUrl, String resource) {
        String accountPath = UrlBuilder.parse(baseUrl).getPath();
        if (accountPath == null || accountPath.isEmpty() || "/".equals(accountPath)) {
            return resource;
        }
        return accountPath.replaceAll("/+$", "") + "/" + resource;
    }

    /**
     * The generated protocol methods target the account-scoped service URL; this appends the resource path to the
     * request URL while preserving the route's query parameters.
     * <p>
     * The path arrives already encoded, one component at a time, exactly as {@code BlobAsyncClientBase.getBlobUrl()}
     * builds it: the separator between container and blob stays literal while each component is percent-encoded.
     * Encoding the assembled path instead would turn that separator into {@code %2F} and address a blob whose name
     * contains a slash rather than a blob inside the container.
     *
     * @param requestOptions The {@link RequestOptions} to scope.
     * @param resourcePath The already-encoded resource path to set on the request URL.
     */
    public static void scopeRequestToResourcePath(RequestOptions requestOptions, String resourcePath) {
        requestOptions.addRequestCallback(request -> {
            UrlBuilder urlBuilder = UrlBuilder.parse(request.getUrl());
            urlBuilder.setPath(resourcePath);
            try {
                request.setUrl(new URL(urlBuilder.toString()));
            } catch (MalformedURLException e) {
                throw new IllegalStateException(e);
            }
        });
    }
}
