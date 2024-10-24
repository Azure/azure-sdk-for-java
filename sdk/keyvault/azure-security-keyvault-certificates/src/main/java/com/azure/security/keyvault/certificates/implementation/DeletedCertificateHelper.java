// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.certificates.implementation;

import com.azure.security.keyvault.certificates.implementation.models.DeletedCertificateBundle;
import com.azure.security.keyvault.certificates.implementation.models.DeletedCertificateItem;
import com.azure.security.keyvault.certificates.models.DeletedCertificate;

public final class DeletedCertificateHelper {
    private static DeletedCertificateAccessor accessor;

    public interface DeletedCertificateAccessor {
        DeletedCertificate createDeletedCertificate(DeletedCertificateItem item);

        DeletedCertificate createDeletedCertificate(DeletedCertificateBundle bundle);
    }

    public static DeletedCertificate createDeletedCertificate(DeletedCertificateItem item) {
        if (accessor == null) {
            new DeletedCertificate();
        }

        assert accessor != null;
        return accessor.createDeletedCertificate(item);
    }

    public static DeletedCertificate createDeletedCertificate(DeletedCertificateBundle bundle) {
        if (accessor == null) {
            new DeletedCertificate();
        }

        assert accessor != null;
        return accessor.createDeletedCertificate(bundle);
    }

    public static void setAccessor(DeletedCertificateAccessor accessor) {
        DeletedCertificateHelper.accessor = accessor;
    }

    private DeletedCertificateHelper() {
    }
}
