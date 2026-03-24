// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.secrets.models;

import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

/**
 * The media type (MIME type) of a certificate-backed secret.
 *
 * <p>Used with {@link com.azure.security.keyvault.secrets.SecretClient#getSecret(String, SecretContentType)} to
 * request on-demand format conversion when retrieving a certificate-backed secret from Azure Key Vault.</p>
 *
 * <p>Currently only PFX ({@code application/x-pkcs12}) to PEM ({@code application/x-pem-file}) conversion is
 * supported. If an unsupported conversion is requested, the service will return an error.</p>
 *
 * <p>This feature is available in service version {@code 2025-07-01} and later.</p>
 */
public final class SecretContentType extends ExpandableStringEnum<SecretContentType> {
    /**
     * The PKCS#12 (PFX) file format ({@code application/x-pkcs12}).
     */
    public static final SecretContentType PFX = fromString("application/x-pkcs12");

    /**
     * The PEM file format ({@code application/x-pem-file}).
     */
    public static final SecretContentType PEM = fromString("application/x-pem-file");

    /**
     * Creates a new instance of {@link SecretContentType} with no string value.
     *
     * @deprecated Use {@link #fromString(String)} to create or get an instance of {@link SecretContentType} instead.
     */
    @Deprecated
    public SecretContentType() {
    }

    /**
     * Creates or finds a {@link SecretContentType} from its string representation.
     *
     * @param name The string value to look up.
     * @return The corresponding {@link SecretContentType}.
     */
    public static SecretContentType fromString(String name) {
        return fromString(name, SecretContentType.class);
    }

    /**
     * Gets the known {@link SecretContentType} values.
     *
     * @return The known {@link SecretContentType} values.
     */
    public static Collection<SecretContentType> values() {
        return values(SecretContentType.class);
    }
}
