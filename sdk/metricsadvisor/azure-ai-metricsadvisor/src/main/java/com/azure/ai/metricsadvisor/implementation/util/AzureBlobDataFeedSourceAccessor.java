// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.implementation.util;

import com.azure.ai.metricsadvisor.administration.models.AzureBlobDataFeedSource;
import com.azure.ai.metricsadvisor.administration.models.SqlServerDataFeedSource;

public final class AzureBlobDataFeedSourceAccessor {
    private static Accessor accessor;

    private AzureBlobDataFeedSourceAccessor() {
    }

    /**
     * Type defining the methods to set the non-public properties of
     * an {@link SqlServerDataFeedSource} instance.
     */
    public interface Accessor {
        String getConnectionString(AzureBlobDataFeedSource feedSource);
    }

    /**
     * The method called from {@link AzureBlobDataFeedSource} to set it's accessor.
     *
     * @param accessor The accessor.
     */
    public static void setAccessor(final Accessor accessor) {
        AzureBlobDataFeedSourceAccessor.accessor = accessor;
    }

    public static String getConnectionString(AzureBlobDataFeedSource feedSource) {
        return accessor.getConnectionString(feedSource);
    }
}
