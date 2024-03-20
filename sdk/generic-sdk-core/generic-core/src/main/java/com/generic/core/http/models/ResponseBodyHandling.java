// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http.models;

import com.generic.core.annotation.ServiceInterface;
import com.generic.core.http.Response;

/**
 * Enum that defines how to handle the body of an HTTP response.
 */
public enum ResponseBodyHandling {
    /**
     * Indicates that the body of an HTTP response shall be entirely ignored. No attempt to read the body will be made
     * and the connection to the service will be closed as soon as a body-less {@link Response} instance is created.
     */
    IGNORE,

    /**
     * Indicates that the body of an HTTP response shall not be buffered into memory. The connection to the service will
     * be kept alive to read the body at a future point in time when needed by the created {@link Response} instance.
     */
    STREAM,

    /**
     * Indicates that the body of an HTTP response shall be buffered into memory. The connection to the service will
     * be closed after a {@link Response} instance containing the body is created. <b>This is the default behavior</b>.
     */
    BUFFER,

    /**
     * Indicates that the body of an HTTP response shall be buffered into memory and deserialized to the type specified
     * in the {@link ServiceInterface} definition. The connection to the service will be closed after a {@link Response}
     * instance containing the deserialized body is created.
     */
    DESERIALIZE
}
