// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http.models;

import com.generic.core.annotation.ServiceInterface;
import com.generic.core.http.Response;

/**
 * Enum that defines how to handle the body of a HTTP {@link Response}.
 */
public enum ResponseBodyHandling {
    /**
     * Indicates that the body of a {@link Response} shall be entirely ignored. No attempt to read the body will be made
     * and the connection to the service will be closed a soon as the body-less {@link Response} is returned.
     */
    IGNORE,

    /**
     * Indicates that the body of a {@link Response} shall not be buffered into memory. The connection to the service
     * will be kept alive to read the body at a future point in time.
     */
    NO_BUFFER,

    /**
     * Indicates that the body of a {@link Response} shall be buffered into memory. The connection to the service will
     * be closed after the {@link Response} containing the body is returned. <b>This is the default behavior</b>.
     */
    BUFFER,

    /**
     * Indicates that the body of a {@link Response} shall be buffered into memory and deserialized to the type
     * specified in the {@link ServiceInterface} definition. The connection to the service will be closed after the
     * {@link Response} containing the deserialized body is returned.
     */
    DESERIALIZE
}
