// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates.models;

import com.azure.core.util.ExpandableStringEnum;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Collection;

/**
 * Content type of the certificate when the managed secret is downloaded using a {@code SecretClient}.
 */
public final class CertificateContentType extends ExpandableStringEnum<CertificateContentType> {

    /**
     * Creates an instance of the {@link CertificateContentType}
     */
    public CertificateContentType() { }

    /**
     * Static value {@code PKCS12} for {@link CertificateContentType}.
     */
    public static final CertificateContentType PKCS12 = fromString("application/x-pkcs12");

    /**
     * Static value {@code PEM} for {@link CertificateContentType}.
     */
    public static final CertificateContentType PEM = fromString("application/x-pem-file");

    /**
     * Creates or finds a {@link CertificateContentType} from its string representation.
     *
     * @param name A name to look for.
     * @return The corresponding {@link CertificateContentType}.
     */
    @JsonCreator
    public static CertificateContentType fromString(String name) {
        return fromString(name, CertificateContentType.class);
    }

    /**
     * Returns the collection of {@link CertificateContentType} as values.
     *
     * @return Known {@link CertificateContentType} values.
     */
    public static Collection<CertificateContentType> values() {
        return values(CertificateContentType.class);
    }
}
