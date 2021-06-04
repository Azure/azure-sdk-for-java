// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.implementation.util;

import com.azure.ai.metricsadvisor.administration.models.AzureDataLakeStorageGen2DataFeedSource;
import com.azure.ai.metricsadvisor.administration.models.SqlServerDataFeedSource;

public final class AzureDataLakeStorageGen2DataFeedSourceAccessor {
    private static Accessor accessor;

    private AzureDataLakeStorageGen2DataFeedSourceAccessor() {
    }

    /**
     * Type defining the methods to set the non-public properties of
     * an {@link AzureDataLakeStorageGen2DataFeedSource} instance.
     */
    public interface Accessor {
        String getAccountKey(AzureDataLakeStorageGen2DataFeedSource feedSource);
    }

    /**
     * The method called from {@link SqlServerDataFeedSource} to set it's accessor.
     *
     * @param accessor The accessor.
     */
    public static void setAccessor(final Accessor accessor) {
        AzureDataLakeStorageGen2DataFeedSourceAccessor.accessor = accessor;
    }

    public static String getAccountKey(AzureDataLakeStorageGen2DataFeedSource feedSource) {
        return accessor.getAccountKey(feedSource);
    }
}
