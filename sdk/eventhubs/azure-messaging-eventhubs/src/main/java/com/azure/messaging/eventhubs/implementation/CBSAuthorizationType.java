// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.implementation;

import com.azure.core.amqp.CBSNode;

/**
 * An enumeration of supported authorization methods with the {@link CBSNode}.
 */
public enum CBSAuthorizationType {
    /**
     * Authorize with CBS through a shared access signature.
     */
    SHARED_ACCESS_SIGNATURE("sastoken"),
    /**
     * Authorize with CBS using a JSON web token.
     *
     * This is used in the case where Azure Active Directory is used for authentication and the authenticated user
     * wants to authorize with Azure Event Hubs.
     */
    JSON_WEB_TOKEN("jwt");

    private final String scheme;

    CBSAuthorizationType(String scheme) {
        this.scheme = scheme;
    }

    /**
     * Gets the token type scheme.
     *
     * @return The token type scheme.
     */
    public String getTokenType() {
        return scheme;
    }
}
