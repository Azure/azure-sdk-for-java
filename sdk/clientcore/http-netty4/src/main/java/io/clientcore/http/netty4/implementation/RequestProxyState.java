// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.http.netty4.implementation;

/**
 * State of the request's proxy.
 */
public enum RequestProxyState {
    /**
     * Request doesn't have a proxy set.
     */
    NOT_SET,

    /**
     * First attempt sending the request with a proxy.
     */
    FIRST_ATTEMPT,

    /**
     * Subsequent attempt sending the request with a proxy.
     */
    SUBSEQUENT_ATTEMPT
}
