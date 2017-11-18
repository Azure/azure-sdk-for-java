/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.graphrbac;

import com.microsoft.azure.management.resources.fluentcore.arm.ExpandableStringEnum;

import java.util.Collection;

/**
 * Defines values for certificate types.
 */
public final class CertificateType extends ExpandableStringEnum<CertificateType> {
    /** Static value AsymmetricX509Cert for CertificateType. */
    public static final CertificateType ASYMMETRIC_X509_CERT = CertificateType.fromString("AsymmetricX509Cert");

    /** Static value Symmetric for CertificateType. */
    public static final CertificateType SYMMETRIC = CertificateType.fromString("Symmetric");

    /**
     * Finds or creates a certificate type instance based on the specified name.
     * @param name a name
     * @return a CertificateType instance
     */
    public static CertificateType fromString(String name) {
        return fromString(name, CertificateType.class);
    }

    /**
     * @return known certificate types
     */
    public static Collection<CertificateType> values() {
        return values(CertificateType.class);
    }
}
