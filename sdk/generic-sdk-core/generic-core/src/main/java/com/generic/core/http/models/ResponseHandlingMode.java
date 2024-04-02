// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http.models;

import java.io.InputStream;

/**
 * Enum that defines how to handle the body of an HTTP response.
 */
public enum ResponseHandlingMode {
    /**
     * Indicates that the body of an HTTP response shall be entirely ignored. No attempt to read the body will be made
     * and the connection to the service will be closed as soon as a body-less {@link Response} instance is created.
     *
     * <p>This is the default behavior for {@link HttpMethod#HEAD HEAD} operations. For more information, please refer
     * to the <a href=https://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html>RFC documentation</a>.</p>
     */
    IGNORE,

    /**
     * Indicates that the body of an HTTP response shall not be buffered into memory. The connection to the service will
     * be kept alive to read the body at a future point in time when needed by the created {@link Response} instance.
     *
     * <p>This is the default behavior for responses with {@code Content-Type: application/octet-stream}, as well as
     * operations defined to return an {@link InputStream}.</p>
     */
    STREAM,

    /**
     * Indicates that the body of an HTTP response shall be buffered into memory. The connection to the service will be
     * closed after a {@link Response} instance containing the body is created.
     *
     * <p><b>This is the default behavior.</b></p>
     */
    BUFFER,

    /**
     * Indicates that the body of an HTTP response shall be buffered into memory, and an attempt will be made to
     * deserialize the body to a model type, accessible via the {@link Response#getValue} method. The connection to the
     * service will be closed after a {@link Response} instance containing the deserialized body is created.
     */
    DESERIALIZE
}
