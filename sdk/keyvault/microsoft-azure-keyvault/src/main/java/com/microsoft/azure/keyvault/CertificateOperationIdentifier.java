// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.keyvault;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * The certificate operation identifier.
 */
public final class CertificateOperationIdentifier extends ObjectIdentifier {

    /**
     * Verifies whether the identifier belongs to a key vault certificate operation.
     * @param identifier the key vault certificate operation identifier.
     * @return true if the identifier belongs to a key vault certificate operation. False otherwise.
     */
    public static boolean isCertificateOperationIdentifier(String identifier) {
        identifier = verifyNonEmpty(identifier, "identifier");

        URI baseUri;
        try {
            baseUri = new URI(identifier);
        } catch (URISyntaxException e) {
            return false;
        }

        // Path is of the form "/certificates/[name]/pending"
        String[] segments = baseUri.getPath().split("/");
        if (segments.length != 4) {
            return false;
        }

        if (!(segments[1]).equals("certificates")) {
            return false;
        }

        if (!(segments[3]).equals("pending")) {
            return false;
        }

        return true;
    }

    /**
     * Constructor.
     * @param vault the vault url
     * @param name the name of certificate
     * @param version the certificate version
     */
    public CertificateOperationIdentifier(String vault, String name, String version) {
        super(vault, "certificates", name, "pending");
    }

   /**
    * Constructor.
    * @param identifier the key vault certificate operation identifier.
    */
    public CertificateOperationIdentifier(String identifier) {
        super("certificates", identifier);
        if (!version().equals("pending")) {
            throw new IllegalArgumentException(String.format("Invalid CertificateOperationIdentifier: %s", identifier));
        }
    }
}
