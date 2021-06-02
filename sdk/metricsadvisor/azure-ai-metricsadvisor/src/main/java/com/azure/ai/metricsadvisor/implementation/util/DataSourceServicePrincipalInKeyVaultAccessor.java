// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.implementation.util;

import com.azure.ai.metricsadvisor.administration.models.DatasourceServicePrincipalInKeyVault;

public final class DataSourceServicePrincipalInKeyVaultAccessor {
    private static Accessor accessor;

    private DataSourceServicePrincipalInKeyVaultAccessor() {
    }

    /**
     * Type defining the methods to set the non-public properties of
     * an {@link DatasourceServicePrincipalInKeyVault} instance.
     */
    public interface Accessor {
        void setId(DatasourceServicePrincipalInKeyVault entity, String id);
        String getKeyVaultClientSecret(DatasourceServicePrincipalInKeyVault entity);
    }

    /**
     * The method called from {@link DatasourceServicePrincipalInKeyVault} to set it's accessor.
     *
     * @param accessor The accessor.
     */
    public static void setAccessor(final Accessor accessor) {
        DataSourceServicePrincipalInKeyVaultAccessor.accessor = accessor;
    }

    public static void setId(DatasourceServicePrincipalInKeyVault entity, String id) {
        accessor.setId(entity, id);
    }

    public static String getKeyVaultClientSecret(DatasourceServicePrincipalInKeyVault entity) {
        return accessor.getKeyVaultClientSecret(entity);
    }
}
