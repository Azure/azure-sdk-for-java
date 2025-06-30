// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.v2.security.keyvault.certificates.implementation;

import com.azure.v2.security.keyvault.certificates.implementation.models.CertificateItem;
import com.azure.v2.security.keyvault.certificates.implementation.models.DeletedCertificateItem;
import com.azure.v2.security.keyvault.certificates.models.CertificateProperties;
import com.azure.v2.security.keyvault.certificates.models.DeletedCertificate;

/**
 * Helper class to manage the conversion between the implementation and public models of {@link CertificateProperties}.
 * This class is used to allow the implementation to be hidden from the public API.
 */
public final class CertificatePropertiesHelper {
    private static CertificatePropertiesAccessor accessor;

    /**
     * Interface to be implemented by the client to provide the logic for creating {@link CertificateProperties}
     * instances.
     */
    public interface CertificatePropertiesAccessor {
        /**
         * Creates a {@link CertificateProperties} instance from the given implementation model.
         *
         * @param item The implementation model to create the certificate properties from.
         * @return A {@link CertificateProperties} instance.
         */
        CertificateProperties createCertificateProperties(CertificateItem item);

        /**
         * Creates a {@link CertificateProperties} instance from the given implementation model.
         *
         * @param item The implementation model to create the certificate properties from.
         * @return A {@link CertificateProperties} instance.
         */
        CertificateProperties createCertificateProperties(DeletedCertificateItem item);
    }

    /**
     * Creates a {@link CertificateProperties} instance from the given implementation model.
     *
     * @param item The implementation model to create the certificate properties from.
     * @return A {@link CertificateProperties} instance.
     */
    public static CertificateProperties createCertificateProperties(CertificateItem item) {
        if (accessor == null) {
            // CertificateProperties doesn't have a public constructor but DeletedCertificate does and creates an
            // instance of CertificateProperties. This will result in CertificateProperties being loaded by the class
            // loader.
            new DeletedCertificate();
        }

        assert accessor != null;
        return accessor.createCertificateProperties(item);
    }

    /**
     * Creates a {@link CertificateProperties} instance from the given implementation model.
     *
     * @param item The implementation model to create the certificate properties from.
     * @return A {@link CertificateProperties} instance.
     */
    public static CertificateProperties createCertificateProperties(DeletedCertificateItem item) {
        if (accessor == null) {
            // CertificateProperties doesn't have a public constructor but DeletedCertificate does and creates an
            // instance of CertificateProperties. This will result in CertificateProperties being loaded by the class
            // loader.
            new DeletedCertificate();
        }

        assert accessor != null;
        return accessor.createCertificateProperties(item);
    }

    /**
     * Sets the accessor to be used for creating {@link CertificateProperties} instances.
     *
     * @param accessor The accessor to set.
     */
    public static void setAccessor(CertificatePropertiesAccessor accessor) {
        CertificatePropertiesHelper.accessor = accessor;
    }

    private CertificatePropertiesHelper() {
        // Private constructor to prevent instantiation.
    }
}
