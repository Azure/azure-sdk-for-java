// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.v2.security.keyvault.certificates.implementation;

import com.azure.v2.security.keyvault.certificates.implementation.models.DeletedCertificateBundle;
import com.azure.v2.security.keyvault.certificates.implementation.models.DeletedCertificateItem;
import com.azure.v2.security.keyvault.certificates.models.DeletedCertificate;

/**
 * Helper class to manage the conversion between the implementation and public models of {@link DeletedCertificate}.
 * This class is used to allow the implementation to be hidden from the public API.
 */
public final class DeletedCertificateHelper {
    private static DeletedCertificateAccessor accessor;

    /**
     * Interface to be implemented by the client to provide the logic for creating {@link DeletedCertificate}
     * instances.
     */
    public interface DeletedCertificateAccessor {
        /**
         * Creates a {@link DeletedCertificate} instance from the given implementation model.
         *
         * @param item The implementation model to create the deleted certificate from.
         * @return A {@link DeletedCertificate} instance.
         */
        DeletedCertificate createDeletedCertificate(DeletedCertificateItem item);

        /**
         * Creates a {@link DeletedCertificate} instance from the given implementation model.
         *
         * @param bundle The implementation model to create the deleted certificate from.
         * @return A {@link DeletedCertificate} instance.
         */
        DeletedCertificate createDeletedCertificate(DeletedCertificateBundle bundle);
    }

    /**
     * Creates a {@link DeletedCertificate} instance from the given implementation model.
     *
     * @param item The implementation model to create the deleted certificate from.
     * @return A {@link DeletedCertificate} instance.
     */
    public static DeletedCertificate createDeletedCertificate(DeletedCertificateItem item) {
        if (accessor == null) {
            new DeletedCertificate();
        }

        assert accessor != null;
        return accessor.createDeletedCertificate(item);
    }

    /**
     * Creates a {@link DeletedCertificate} instance from the given implementation model.
     *
     * @param bundle The implementation model to create the deleted certificate from.
     * @return A {@link DeletedCertificate} instance.
     */
    public static DeletedCertificate createDeletedCertificate(DeletedCertificateBundle bundle) {
        if (accessor == null) {
            new DeletedCertificate();
        }

        assert accessor != null;
        return accessor.createDeletedCertificate(bundle);
    }

    /**
     * Sets the accessor to be used for creating {@link DeletedCertificate} instances.
     *
     * @param accessor The accessor to set.
     */
    public static void setAccessor(DeletedCertificateAccessor accessor) {
        DeletedCertificateHelper.accessor = accessor;
    }

    private DeletedCertificateHelper() {
        // Private constructor to prevent instantiation.
    }
}
