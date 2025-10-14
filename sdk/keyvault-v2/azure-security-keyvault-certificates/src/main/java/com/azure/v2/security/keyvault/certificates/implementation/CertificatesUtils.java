// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.v2.security.keyvault.certificates.implementation;

import io.clientcore.core.instrumentation.logging.ClientLogger;

import java.net.URI;
import java.net.URISyntaxException;

import static io.clientcore.core.utils.CoreUtils.isNullOrEmpty;

/**
 * Utility methods for Key Vault Certificates.
 */
public final class CertificatesUtils {
    /**
     * Get metadata from a certificate identifier.
     *
     * @param id The certificate identifier.
     * @param vaultUrlIndex The index of the vault URL in the identifier.
     * @param nameIndex The index of the name in the identifier.
     * @param versionIndex The index of the version in the identifier.
     * @param logger The logger to use when logging errors.
     * @return The metadata of the certificate identifier.
     */
    public static IdMetadata getIdMetadata(String id, int vaultUrlIndex, int nameIndex, int versionIndex,
        ClientLogger logger) {

        if (isNullOrEmpty(id)) {
            return new IdMetadata(id, null, null, null);
        }

        try {
            URI url = new URI(id);
            String[] tokens = url.getPath().split("/");

            return new IdMetadata(id, getIdMetadataPiece(tokens, vaultUrlIndex), getIdMetadataPiece(tokens, nameIndex),
                getIdMetadataPiece(tokens, versionIndex));
        } catch (URISyntaxException e) {
            // Should never come here.
            logger.atError().setThrowable(e).log("Received Malformed Secret Id URL from KV Service");

            return new IdMetadata(id, null, null, null);
        }
    }

    private static String getIdMetadataPiece(String[] pieces, int index) {
        if (index == -1) {
            return null;
        }

        return pieces.length >= index + 1 ? pieces[index] : null;
    }

    private CertificatesUtils() {
    }
}
