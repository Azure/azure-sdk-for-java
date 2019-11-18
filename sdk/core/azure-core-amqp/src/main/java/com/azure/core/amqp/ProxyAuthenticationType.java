// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp;

/**
 * Supported methods of proxy authentication.
 *
 * @see ProxyOptions
 */
public enum ProxyAuthenticationType {
    /**
     * Proxy requires no authentication. Service calls will fail if proxy demands authentication.
     */
    NONE,
    /**
     * Authenticates against proxy with basic authentication scheme.
     */
    BASIC,
    /**
     * Authenticates against proxy with digest access authentication.
     */
    DIGEST,
}
