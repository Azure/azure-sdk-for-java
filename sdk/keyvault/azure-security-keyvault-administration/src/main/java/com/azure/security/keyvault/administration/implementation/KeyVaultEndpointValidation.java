// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.administration.implementation;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

/**
 * Validates Key Vault continuation destinations independently of the HTTP pipeline.
 */
final class KeyVaultEndpointValidation {
    private KeyVaultEndpointValidation() {
    }

    static String validateNextLink(String nextLink, String endpoint) {
        if (!getOrigin(endpoint).equals(getOrigin(nextLink))) {
            throw new IllegalArgumentException(
                "The continuation URL must have the same origin as the configured endpoint.");
        }
        return nextLink;
    }

    static String getOrigin(String url) {
        final URI uri;
        try {
            uri = new URI(url).parseServerAuthority();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid Key Vault URL.", e);
        }

        String scheme = uri.getScheme();
        if ((!"https".equalsIgnoreCase(scheme) && !"http".equalsIgnoreCase(scheme))
            || uri.getHost() == null
            || uri.getRawUserInfo() != null
            || uri.getRawFragment() != null
            || uri.getPort() < -1
            || uri.getPort() > 65535) {
            throw new IllegalArgumentException("Invalid Key Vault URL.");
        }

        int port = uri.getPort() == -1 ? ("https".equalsIgnoreCase(scheme) ? 443 : 80) : uri.getPort();
        return scheme.toLowerCase(Locale.ROOT) + "://" + uri.getHost().toLowerCase(Locale.ROOT) + ":" + port;
    }
}
