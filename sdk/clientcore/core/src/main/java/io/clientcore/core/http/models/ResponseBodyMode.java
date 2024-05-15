// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.models;

import java.io.InputStream;

/**
 * Enum that defines how to handle the body of an HTTP response.
 */
public enum ResponseBodyMode {
    /**
     * Indicates that the body of an HTTP response shall be entirely ignored. No attempt to read the body will be made
     * and the service connection will be terminated immediately once a {@link Response} instance without a body is
     * generated.
     *
     * <p>This is the default behavior for {@link HttpMethod#HEAD HEAD} operations. For more information, please refer
     * to the <a href=https://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html>RFC documentation</a>.</p>
     */
    IGNORE,

    /**
     * Indicates that the body of an HTTP response shall not be buffered into memory. The service connection will remain
     * open for future reading of the body when required by the created {@link Response} instance.
     *
     * <p>This is the default behavior for responses with {@code Content-Type: application/octet-stream}, as well as
     * operations defined to return an {@link InputStream}.</p>
     */
    STREAM,

    /**
     * Indicates that the body of an HTTP response shall be buffered into memory. The service connection will be
     * terminated once a {@link Response} instance that includes the body is generated.
     *
     * <p><b>This is the default behavior.</b></p>
     */
    BUFFER,

    /**
     * Indicates that the body of an HTTP response shall be buffered into memory and an attempt to deserialize it
     * into a model type will be made. The service connection will be terminated once a {@link Response} instance with
     * the deserialized body is generated.
     *
     * <p>The deserialized response body can be accessed through the {@link Response#getValue()} method.</p>
     */
    DESERIALIZE
}
