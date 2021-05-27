// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.implementation.util;

import com.azure.ai.metricsadvisor.models.DataLakeGen2SharedKeyCredentialEntity;

public final class DataLakeGen2SharedKeyCredentialEntityAccessor {
    private static Accessor accessor;

    private DataLakeGen2SharedKeyCredentialEntityAccessor() {
    }

    /**
     * Type defining the methods to set the non-public properties of
     * an {@link DataLakeGen2SharedKeyCredentialEntity} instance.
     */
    public interface Accessor {
        void setId(DataLakeGen2SharedKeyCredentialEntity entity, String id);
        String getSharedKey(DataLakeGen2SharedKeyCredentialEntity entity);
    }

    /**
     * The method called from {@link DataLakeGen2SharedKeyCredentialEntity} to set it's accessor.
     *
     * @param accessor The accessor.
     */
    public static void setAccessor(final Accessor accessor) {
        DataLakeGen2SharedKeyCredentialEntityAccessor.accessor = accessor;
    }

    public static void setId(DataLakeGen2SharedKeyCredentialEntity entity, String id) {
        accessor.setId(entity, id);
    }

    public static String getSharedKey(DataLakeGen2SharedKeyCredentialEntity entity) {
        return accessor.getSharedKey(entity);
    }
}
