// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates.models;

import com.azure.core.util.ExpandableStringEnum;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Collection;

/**
 * Defines values for CertificateKeyUsage.
 */
public final class CertificateKeyUsage extends ExpandableStringEnum<CertificateKeyUsage> {
    
    /**
     * Static value Digital Signature for CertificateKeyUsage.
     */
    public static final CertificateKeyUsage DIGITAL_SIGNATURE = fromString("digitalSignature");

    /**
     * Static value Non Repudiation for CertificateKeyUsage.
     */
    public static final CertificateKeyUsage NON_REPUDIATION = fromString("nonRepudiation");

    /**
     * Static value Key Encipherment for CertificateKeyUsage.
     */
    public static final CertificateKeyUsage KEY_ENCIPHERMENT = fromString("keyEncipherment");

    /**
     * Static value Data Encipherment for CertificateKeyUsage.
     */
    public static final CertificateKeyUsage DATA_ENCIPHERMENT = fromString("dataEncipherment");

    /**
     * Static value Key Agreement for CertificateKeyUsage.
     */
    public static final CertificateKeyUsage KEY_AGREEMENT = fromString("keyAgreement");
    
    /**
     * Static value Key CertSign for CertificateKeyUsage.
     */
    public static final CertificateKeyUsage KEY_CERT_SIGN = fromString("keyCertSign");

    /**
     * Static value CRLSign for CertificateKeyUsage.
     */
    public static final CertificateKeyUsage CRL_SIGN = fromString("cRLSign");

    /**
     * Static value Encipher Only for CertificateKeyUsage.
     */
    public static final CertificateKeyUsage ENCIPHER_ONLY = fromString("encipherOnly");

    /**
     * Static value Decipher Only for CertificateKeyUsage.
     */
    public static final CertificateKeyUsage DECIPHER_ONLY = fromString("decipherOnly");

    /**
     * Creates or finds a CertificateKeyUsage from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding CertificateKeyUsage.
     */
    @JsonCreator
    public static CertificateKeyUsage fromString(String name) {
        return fromString(name, CertificateKeyUsage.class);
    }

    /**
     * @return known CertificateKeyUsage values.
     */
    public static Collection<CertificateKeyUsage> values() {
        return values(CertificateKeyUsage.class);
    }
}
