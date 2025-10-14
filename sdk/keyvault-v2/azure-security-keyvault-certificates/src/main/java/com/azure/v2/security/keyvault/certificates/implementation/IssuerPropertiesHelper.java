// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.v2.security.keyvault.certificates.implementation;

import com.azure.v2.security.keyvault.certificates.implementation.models.CertificateIssuerItem;
import com.azure.v2.security.keyvault.certificates.models.IssuerProperties;

/**
 * Helper class to manage the conversion between the implementation and public models of {@link IssuerProperties}.
 * This class is used to allow the implementation to be hidden from the public API.
 */
public final class IssuerPropertiesHelper {
    private static IssuerPropertiesAccessor accessor;

    /**
     * Interface to be implemented by the client to provide the logic for creating {@link IssuerProperties}
     * instances.
     */
    public interface IssuerPropertiesAccessor {
        /**
         * Creates a {@link IssuerProperties} instance from the given implementation model.
         *
         * @param impl The implementation model to create the issuer properties from.
         * @return A {@link IssuerProperties} instance.
         */
        IssuerProperties createIssuerProperties(CertificateIssuerItem impl);
    }

    /**
     * Creates a {@link IssuerProperties} instance from the given implementation model.
     *
     * @param impl The implementation model to create the issuer properties from.
     * @return A {@link IssuerProperties} instance.
     */
    public static IssuerProperties createIssuerProperties(CertificateIssuerItem impl) {
        if (accessor == null) {
            new IssuerProperties();
        }

        assert accessor != null;
        return accessor.createIssuerProperties(impl);
    }

    /**
     * Sets the accessor to be used for creating {@link IssuerProperties} instances.
     *
     * @param accessor The accessor to set.
     */
    public static void setAccessor(IssuerPropertiesAccessor accessor) {
        IssuerPropertiesHelper.accessor = accessor;
    }

    private IssuerPropertiesHelper() {
        // Private constructor to prevent instantiation
    }
}
