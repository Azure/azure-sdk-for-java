/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.protocol;

/**
 * An collection of endpoints in a region or a cloud.
 */
public interface Environment {
    /**
     * An endpoint identifier used for the provider to get a URL.
     */
    interface Endpoint {
        /**
         * @return a unique identifier for the endpoint in the environment
         */
        String identifier();
    }

    /**
     * Provides a URL for the endpoint.
     * @param endpoint the endpoint the client is accessing
     * @return the URL to make HTTP requests to
     */
    String url(Endpoint endpoint);
}
