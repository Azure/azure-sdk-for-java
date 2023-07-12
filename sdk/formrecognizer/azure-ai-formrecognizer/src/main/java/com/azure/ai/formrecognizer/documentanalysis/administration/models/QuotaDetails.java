// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.documentanalysis.administration.models;

import com.azure.ai.formrecognizer.documentanalysis.implementation.util.QuotaDetailsHelper;
import com.azure.core.annotation.Immutable;

import java.time.OffsetDateTime;

/** Quota used, limit, and next reset date/time. */
@Immutable
public final class QuotaDetails {
    /*
     * Amount of the resource quota used.
     */
    private int used;

    /*
     * Resource quota limit.
     */
    private int quota;

    /*
     * Date/time when the resource quota usage will be reset.
     */
    private OffsetDateTime quotaResetDateTime;

    /**
     * Get the used property: Amount of the resource quota used.
     *
     * @return the used value.
     */
    public int getUsed() {
        return this.used;
    }

    /**
     * Get the quota property: Resource quota limit.
     *
     * @return the quota value.
     */
    public int getQuota() {
        return this.quota;
    }

    /**
     * Get the quotaResetDateTime property: Date/time when the resource quota usage will be reset.
     *
     * @return the quotaResetDateTime value.
     */
    public OffsetDateTime getQuotaResetDateTime() {
        return this.quotaResetDateTime;
    }

    private void setUsed(int used) {
        this.used = used;
    }

    private void setQuota(int quota) {
        this.quota = quota;
    }

    private void setQuotaResetDateTime(OffsetDateTime quotaResetDateTime) {
        this.quotaResetDateTime = quotaResetDateTime;
    }

    static {
        QuotaDetailsHelper.setAccessor(new QuotaDetailsHelper.QuotaDetailsAccessor() {
            @Override
            public void setUsed(
                QuotaDetails quotaDetails, int used) {
                quotaDetails.setUsed(used);
            }

            @Override
            public void setQuota(
                QuotaDetails quotaDetails, int quota) {
                quotaDetails.setQuota(quota);
            }

            @Override
            public void setQuotaResetDateTime(
                QuotaDetails quotaDetails, OffsetDateTime quotaResetDateTime) {
                quotaDetails.setQuotaResetDateTime(quotaResetDateTime);
            }
        });
    }
}
