/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.credentials;

import java.io.IOException;

/**
 * Provides credentials to be put in the HTTP Authorization header.
 */
public interface ServiceClientCredentials {
    /**
     * @param uri The URI to which the request is being made.
     * @return The value containing currently valid credentials to put in the HTTP header.
     * @throws IOException if unable to get the authorization header value
     */
    String authorizationHeaderValue(String uri) throws IOException;
}
