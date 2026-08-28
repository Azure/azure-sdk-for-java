// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.administration.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;

import java.util.List;
import java.util.Objects;

/**
 * An External Key Manager (EKM) connection.
 */
@Fluent
public final class KeyVaultEkmConnection {
    private static final ClientLogger LOGGER = new ClientLogger(KeyVaultEkmConnection.class);

    private final String host;
    private final List<byte[]> serverCaCertificates;
    private String pathPrefix;
    private String serverSubjectCommonName;

    /**
     * Creates a new {@link KeyVaultEkmConnection} with the specified details.
     *
     * @param host The EKM proxy FQDN (Fully Qualified Domain Name). Only allowed characters are {@code a-z},
     * {@code A-Z}, {@code 0-9}, hyphen ({@code -}), dot ({@code .}), and colon ({@code :}).
     * @param serverCaCertificates The root CA certificate chain that issued the proxy server's certificate. A list of
     * certificates in the certificate chain, each in DER format.
     * @throws IllegalArgumentException if {@code host} is {@code null} or empty.
     * @throws NullPointerException if {@code serverCaCertificates} is {@code null}.
     */
    public KeyVaultEkmConnection(String host, List<byte[]> serverCaCertificates) {
        if (CoreUtils.isNullOrEmpty(host)) {
            throw LOGGER
                .logExceptionAsError(new IllegalArgumentException("The 'host' parameter cannot be null or empty."));
        }

        this.host = host;
        this.serverCaCertificates
            = Objects.requireNonNull(serverCaCertificates, "The 'serverCaCertificates' parameter cannot be null.");
    }

    /**
     * Get the EKM proxy FQDN (Fully Qualified Domain Name). Only allowed characters are {@code a-z}, {@code A-Z},
     * {@code 0-9}, hyphen ({@code -}), dot ({@code .}), and colon ({@code :}).
     *
     * @return The host value.
     */
    public String getHost() {
        return this.host;
    }

    /**
     * Get the root CA certificate chain that issued the proxy server's certificate. A list of certificates in the
     * certificate chain, each in DER format.
     *
     * @return The server CA certificates.
     */
    public List<byte[]> getServerCaCertificates() {
        return this.serverCaCertificates;
    }

    /**
     * Get the optional path prefix for the EKM proxy (if any).
     *
     * @return The path prefix.
     */
    public String getPathPrefix() {
        return this.pathPrefix;
    }

    /**
     * Set the optional path prefix for the EKM proxy (if any).
     *
     * @param pathPrefix The path prefix to set.
     * @return The updated {@link KeyVaultEkmConnection} object.
     */
    public KeyVaultEkmConnection setPathPrefix(String pathPrefix) {
        this.pathPrefix = pathPrefix;
        return this;
    }

    /**
     * Get the subject common name of the server certificate of the EKM proxy.
     *
     * @return The server subject common name.
     */
    public String getServerSubjectCommonName() {
        return this.serverSubjectCommonName;
    }

    /**
     * Set the subject common name of the server certificate of the EKM proxy.
     *
     * @param serverSubjectCommonName The server subject common name to set.
     * @return The updated {@link KeyVaultEkmConnection} object.
     */
    public KeyVaultEkmConnection setServerSubjectCommonName(String serverSubjectCommonName) {
        this.serverSubjectCommonName = serverSubjectCommonName;
        return this;
    }
}
