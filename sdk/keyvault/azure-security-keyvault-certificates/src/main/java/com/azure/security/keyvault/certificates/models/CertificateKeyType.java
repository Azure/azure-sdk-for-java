// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates.models;

import com.azure.core.util.ExpandableStringEnum;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Collection;

/**
 * Defines values for CertificateKeyType.
 */
public final class CertificateKeyType extends ExpandableStringEnum<CertificateKeyType> {

    /**
     * Static value EC for CertificateKeyType.
     */
    public static final CertificateKeyType EC = fromString("EC");

    /**
     * Static value EC-HSM for CertificateKeyType.
     */
    public static final CertificateKeyType EC_HSM = fromString("EC-HSM");

    /**
     * Static value RSA for CertificateKeyType.
     */
    public static final CertificateKeyType RSA = fromString("RSA");

    /**
     * Static value RSA-HSM for CertificateKeyType.
     */
    public static final CertificateKeyType RSA_HSM = fromString("RSA-HSM");

    /**
     * Creates or finds a CertificateKeyType from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding CertificateKeyType.
     */
    @JsonCreator
    public static CertificateKeyType fromString(String name) {
        return fromString(name, CertificateKeyType.class);
    }

    /**
     * @return known CertificateKeyType values.
     */
    public static Collection<CertificateKeyType> values() {
        return values(CertificateKeyType.class);
    }
}
