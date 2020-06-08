// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.authorization.models;

import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

/** Defines values for certificate types. */
public final class CertificateType extends ExpandableStringEnum<CertificateType> {
    /** Static value AsymmetricX509Cert for CertificateType. */
    public static final CertificateType ASYMMETRIC_X509_CERT = CertificateType.fromString("AsymmetricX509Cert");

    /** Static value Symmetric for CertificateType. */
    public static final CertificateType SYMMETRIC = CertificateType.fromString("Symmetric");

    /**
     * Finds or creates a certificate type instance based on the specified name.
     *
     * @param name a name
     * @return a CertificateType instance
     */
    public static CertificateType fromString(String name) {
        return fromString(name, CertificateType.class);
    }

    /** @return known certificate types */
    public static Collection<CertificateType> values() {
        return values(CertificateType.class);
    }
}
