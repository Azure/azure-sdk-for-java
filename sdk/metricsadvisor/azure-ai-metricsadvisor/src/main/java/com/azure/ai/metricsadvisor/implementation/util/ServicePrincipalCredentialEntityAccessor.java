// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.implementation.util;

import com.azure.ai.metricsadvisor.models.ServicePrincipalCredentialEntity;

public final class ServicePrincipalCredentialEntityAccessor {
    private static Accessor accessor;

    private ServicePrincipalCredentialEntityAccessor() {
    }

    /**
     * Type defining the methods to set the non-public properties of
     * an {@link ServicePrincipalCredentialEntity} instance.
     */
    public interface Accessor {
        void setId(ServicePrincipalCredentialEntity entity, String id);
        String getClientSecret(ServicePrincipalCredentialEntity entity);
    }

    /**
     * The method called from {@link ServicePrincipalCredentialEntity} to set it's accessor.
     *
     * @param accessor The accessor.
     */
    public static void setAccessor(final Accessor accessor) {
        ServicePrincipalCredentialEntityAccessor.accessor = accessor;
    }

    public static void setId(ServicePrincipalCredentialEntity entity, String id) {
        accessor.setId(entity, id);
    }

    public static String getClientSecret(ServicePrincipalCredentialEntity entity) {
        return accessor.getClientSecret(entity);
    }
}
