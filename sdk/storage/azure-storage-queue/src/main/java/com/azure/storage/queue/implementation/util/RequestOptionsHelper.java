// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.queue.implementation.util;

import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.Context;
import com.azure.core.util.UrlBuilder;

import java.util.List;

/**
 * Builds the {@link RequestOptions} passed to the generated {@code implementation/*Impl} protocol methods.
 * <p>
 * The hand-written {@code Queue} clients call the emitter's protocol {@code xxxWithResponse(RequestOptions)} methods
 * (which target the account-scoped service URL and take all inputs through {@link RequestOptions}). These helpers
 * translate the typed client inputs into the storage wire contract -- query parameters and the resource path -- so the
 * request URL matches the pre-migration (AutoRest) behavior where the queue name was a path parameter rather than part
 * of the client base URL.
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
     * Builds a {@link RequestOptions} scoped to a queue-level operation ({@code {queueName}}).
     *
     * @param context The {@link Context} to thread through the pipeline.
     * @param baseUrl The client's account-scoped base URL.
     * @param queueName The queue name.
     * @return The scoped {@link RequestOptions}.
     */
    public static RequestOptions queueRequestOptions(Context context, String baseUrl, String queueName) {
        RequestOptions requestOptions = requestOptions(context);
        scopeRequestToResourcePath(requestOptions, resourcePath(baseUrl, queueName));
        return requestOptions;
    }

    /**
     * Builds a {@link RequestOptions} scoped to a messages-level operation ({@code {queueName}/messages}).
     *
     * @param context The {@link Context} to thread through the pipeline.
     * @param baseUrl The client's account-scoped base URL.
     * @param queueName The queue name.
     * @return The scoped {@link RequestOptions}.
     */
    public static RequestOptions messagesRequestOptions(Context context, String baseUrl, String queueName) {
        RequestOptions requestOptions = requestOptions(context);
        scopeRequestToResourcePath(requestOptions, resourcePath(baseUrl, queueName + "/messages"));
        return requestOptions;
    }

    /**
     * Builds a {@link RequestOptions} scoped to a message-id operation
     * ({@code {queueName}/messages/{messageId}}).
     *
     * @param context The {@link Context} to thread through the pipeline.
     * @param baseUrl The client's account-scoped base URL.
     * @param queueName The queue name.
     * @param messageId The message id.
     * @return The scoped {@link RequestOptions}.
     */
    public static RequestOptions messageIdRequestOptions(Context context, String baseUrl, String queueName,
        String messageId) {
        RequestOptions requestOptions = requestOptions(context);
        scopeRequestToResourcePath(requestOptions, resourcePath(baseUrl, queueName + "/messages/" + messageId));
        return requestOptions;
    }

    /**
     * Prefixes the resource path with the base URL's account path, which is present for path-style endpoints (e.g.
     * the Azurite emulator's {@code http://host/devstoreaccount1}) and empty for standard {@code account.queue.*}
     * endpoints where the account is the host. Required because {@link #scopeRequestToResourcePath} sets the whole URL
     * path, so the account segment must be reintroduced explicitly.
     */
    private static String resourcePath(String baseUrl, String resource) {
        String accountPath = UrlBuilder.parse(baseUrl).getPath();
        if (accountPath == null || accountPath.isEmpty() || "/".equals(accountPath)) {
            return resource;
        }
        return accountPath.replaceAll("/+$", "") + "/" + resource;
    }

    /**
     * The generated protocol methods target the account-scoped service URL; this appends the resource path (e.g.
     * {@code "{queueName}"}, {@code "{queueName}/messages"} or {@code "{queueName}/messages/{messageId}"}) to the
     * request URL while preserving the route's query parameters.
     *
     * @param requestOptions The {@link RequestOptions} to scope.
     * @param resourcePath The resource path to set on the request URL.
     */
    public static void scopeRequestToResourcePath(RequestOptions requestOptions, String resourcePath) {
        requestOptions.addRequestCallback(request -> {
            UrlBuilder urlBuilder = UrlBuilder.parse(request.getUrl());
            urlBuilder.setPath(resourcePath);
            try {
                request.setUrl(urlBuilder.toUrl());
            } catch (java.net.MalformedURLException e) {
                throw new IllegalStateException(e);
            }
        });
    }

    /**
     * Builds the {@link RequestOptions} for the {@code listQueues} operation.
     *
     * @param context The {@link Context} to thread through the pipeline.
     * @param prefix The queue name prefix filter.
     * @param marker The continuation token.
     * @param maxResults The maximum number of queues to return.
     * @param include The optional datasets to include.
     * @return The {@link RequestOptions} for the list-queues call.
     */
    public static RequestOptions listQueuesRequestOptions(Context context, String prefix, String marker,
        Integer maxResults, List<String> include) {
        RequestOptions requestOptions = requestOptions(context);
        addOptionalQueryParam(requestOptions, "prefix", prefix);
        addOptionalQueryParam(requestOptions, "marker", marker);
        addOptionalQueryParam(requestOptions, "maxresults", maxResults);
        // Match the AutoRest wire behavior: emit "include=" whenever the list is non-null (an empty list produces an
        // empty value), rather than omitting it. Keeps the request URL identical to the pre-migration implementation.
        if (include != null) {
            requestOptions.addQueryParam("include", String.join(",", include));
        }
        return requestOptions;
    }

    public static void addOptionalQueryParam(RequestOptions requestOptions, String name, Object value) {
        if (value != null) {
            requestOptions.addQueryParam(name, String.valueOf(value));
        }
    }
}
