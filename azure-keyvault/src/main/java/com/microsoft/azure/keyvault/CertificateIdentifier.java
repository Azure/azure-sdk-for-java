/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.keyvault;

/**
 * The certificate identifier.
 */
public final class CertificateIdentifier extends ObjectIdentifier {

    /**
     * Verify whether the identifier is for certificate. 
     * @param identifier the certificate identifier
     * @return true if the identifier is the certificate identifier. False otherwise.
     */
    public static boolean isCertificateIdentifier(String identifier) {
        return ObjectIdentifier.isObjectIdentifier("certificates", identifier);
    }

    /**
     * Constructor.
     * @param vault The vault url
     * @param name the certificate name
     */
    public CertificateIdentifier(String vault, String name) {
        this(vault, name, "");
    }

    /**
     * Constructor.
     * @param vault the vault url
     * @param name the certificate name
     * @param version the certificate version
     */
    public CertificateIdentifier(String vault, String name, String version) {
        super(vault, "certificates", name, version);
    }

    /**
     * Constructor.
     * @param identifier the certificate identifier
     */
    public CertificateIdentifier(String identifier) {
        super("certificates", identifier);
    }
}
