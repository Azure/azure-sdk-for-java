// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.annotation.Immutable;
import com.azure.core.http.HttpHeaders;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Immutable Configuration options for {@link RequestIdPolicy}.
 * User can provide a {@link Supplier} to generated id for various id headers to be used in
 * {@link com.azure.core.http.HttpRequest}. Examples of such headers are 'x-ms-client-request-id
 * or 'x-ms-correlation-request-id' etc.
 */
@Immutable
public class RequestIdPolicyOptions {

    private volatile Supplier<HttpHeaders> idHeaderSupplier;

    /**
     * Creates {@link RequestIdPolicyOptions} with provided {@code idHeaderSupplier }.
     *
     * @param idHeaderSupplier {@link Supplier} to provide dynamically generated id for various request id headers.
     * Examples of such headers are 'x-ms-client-request-id or 'x-ms-correlation-request-id' etc.
     */
    public RequestIdPolicyOptions(Supplier<HttpHeaders> idHeaderSupplier) {
        this.idHeaderSupplier = Objects.requireNonNull(idHeaderSupplier,
            "'idHeaderSupplier' cannot be null.");
    }

    /**
     *
     * @return {@link Supplier} to provide dynamically generated id for various request id headers
     */
    public Supplier<HttpHeaders> getIdHeaderSupplier() {
        return idHeaderSupplier;
    }
}
