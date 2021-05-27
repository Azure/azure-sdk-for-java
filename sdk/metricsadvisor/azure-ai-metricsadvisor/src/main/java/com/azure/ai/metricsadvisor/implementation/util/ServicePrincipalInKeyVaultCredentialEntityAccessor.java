// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.implementation.util;

import com.azure.ai.metricsadvisor.models.ServicePrincipalInKeyVaultCredentialEntity;

public final class ServicePrincipalInKeyVaultCredentialEntityAccessor {
    private static Accessor accessor;

    private ServicePrincipalInKeyVaultCredentialEntityAccessor() {
    }

    /**
     * Type defining the methods to set the non-public properties of
     * an {@link ServicePrincipalInKeyVaultCredentialEntity} instance.
     */
    public interface Accessor {
        void setId(ServicePrincipalInKeyVaultCredentialEntity entity, String id);
        String getKeyVaultClientSecret(ServicePrincipalInKeyVaultCredentialEntity entity);
        String getSecretNameForDataSourceClientId(ServicePrincipalInKeyVaultCredentialEntity entity);
        String getSecretNameForDataSourceClientSecret(ServicePrincipalInKeyVaultCredentialEntity entity);
    }

    /**
     * The method called from {@link ServicePrincipalInKeyVaultCredentialEntity} to set it's accessor.
     *
     * @param accessor The accessor.
     */
    public static void setAccessor(final Accessor accessor) {
        ServicePrincipalInKeyVaultCredentialEntityAccessor.accessor = accessor;
    }

    public static void setId(ServicePrincipalInKeyVaultCredentialEntity entity, String id) {
        accessor.setId(entity, id);
    }

    public static String getKeyVaultClientSecret(ServicePrincipalInKeyVaultCredentialEntity entity) {
        return accessor.getKeyVaultClientSecret(entity);
    }

    public static String getSecretNameForDataSourceClientId(ServicePrincipalInKeyVaultCredentialEntity entity) {
        return accessor.getSecretNameForDataSourceClientId(entity);
    }

    public static String getSecretNameForDataSourceClientSecret(ServicePrincipalInKeyVaultCredentialEntity entity) {
        return accessor.getSecretNameForDataSourceClientSecret(entity);
    }
}
