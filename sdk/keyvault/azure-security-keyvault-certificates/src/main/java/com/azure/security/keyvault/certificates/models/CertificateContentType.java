// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates.models;

import com.azure.core.util.ExpandableStringEnum;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Collection;

/**
 * Defines values for CertificateContentType.
 */
public final class CertificateContentType extends ExpandableStringEnum<CertificateContentType> {
    
    /**
     * Static value PKCS12 for CertificateContentType.
     */
    public static final CertificateContentType PKCS12 = fromString("application/x-pkcs12");

    /**
     * Static value PEM for CertificateContentType.
     */
    public static final CertificateContentType PEM = fromString("application/x-pem-file");
    
    /**
     * Creates or finds a CertificateContentType from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding CertificateContentType.
     */
    @JsonCreator
    public static CertificateContentType fromString(String name) {
        return fromString(name, CertificateContentType.class);
    }

    /**
     * @return known CertificateContentType values.
     */
    public static Collection<CertificateContentType> values() {
        return values(CertificateContentType.class);
    }
}
