// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.certificates.implementation;

import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Utility methods for Key Vault Certificates.
 */
public final class CertificatesUtils {
    public static IdMetadata getIdMetadata(String id, int vaultUrlIndex, int nameIndex, int versionIndex,
        ClientLogger logger) {
        if (CoreUtils.isNullOrEmpty(id)) {
            return new IdMetadata(id, null, null, null);
        }

        try {
            URL url = new URL(id);
            String[] tokens = url.getPath().split("/");
            return new IdMetadata(id, getIdMetadataPiece(tokens, vaultUrlIndex), getIdMetadataPiece(tokens, nameIndex),
                getIdMetadataPiece(tokens, versionIndex));
        } catch (MalformedURLException e) {
            // Should never come here.
            logger.error("Received Malformed Secret Id URL from KV Service");
            return new IdMetadata(id, null, null, null);
        }
    }

    private static String getIdMetadataPiece(String[] pieces, int index) {
        if (index == -1) {
            return null;
        }

        return pieces.length >= index + 1 ? pieces[index] : null;
    }

    public static String toHexString(byte[] x509Thumbprint) {
        if (x509Thumbprint == null) {
            return null;
        }

        StringBuilder hexString = new StringBuilder();

        for (byte b : x509Thumbprint) {
            String hex = Integer.toHexString(0xFF & b);

            if (hex.length() == 1) {
                hexString.append('0');
            }

            hexString.append(hex);
        }

        return hexString.toString().replace("-", "");
    }

    private CertificatesUtils() {
    }
}
