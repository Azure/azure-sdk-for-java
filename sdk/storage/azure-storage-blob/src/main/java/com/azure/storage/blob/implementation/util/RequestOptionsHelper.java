// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.blob.implementation.util;

import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.Context;

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
}
