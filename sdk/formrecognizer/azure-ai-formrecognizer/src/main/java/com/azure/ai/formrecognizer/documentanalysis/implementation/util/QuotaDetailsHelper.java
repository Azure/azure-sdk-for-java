// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.documentanalysis.implementation.util;

import com.azure.ai.formrecognizer.documentanalysis.administration.models.QuotaDetails;

import java.time.OffsetDateTime;

/**
 * The helper class to set the non-public properties of an {@link QuotaDetails} instance.
 */
public final class QuotaDetailsHelper {
    private static QuotaDetailsAccessor accessor;

    private QuotaDetailsHelper() {
    }

    /**
     * Type defining the methods to set the non-public properties of an {@link QuotaDetails} instance.
     */
    public interface QuotaDetailsAccessor {
        void setUsed(QuotaDetails quotaDetails, int used);
        void setQuota(QuotaDetails quotaDetails, int quota);
        void setQuotaResetDateTime(QuotaDetails quotaDetails, OffsetDateTime dateTime);
    }

    /**
     * The method called from {@link QuotaDetails} to set it's accessor.
     *
     * @param quotaDetailsAccessor The accessor.
     */
    public static void setAccessor(final QuotaDetailsAccessor quotaDetailsAccessor) {
        accessor = quotaDetailsAccessor;
    }

    static void setUsed(QuotaDetails quotaDetails, int used) {
        accessor.setUsed(quotaDetails, used);
    }
    static void setQuota(QuotaDetails quotaDetails, int quota) {
        accessor.setQuota(quotaDetails, quota);
    }

    static void setQuotaResetDateTime(QuotaDetails quotaDetails, OffsetDateTime dateTime) {
        accessor.setQuotaResetDateTime(quotaDetails, dateTime);
    }
}
