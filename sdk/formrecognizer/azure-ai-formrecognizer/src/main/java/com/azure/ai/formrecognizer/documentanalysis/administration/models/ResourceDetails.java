// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.documentanalysis.administration.models;

import com.azure.ai.formrecognizer.documentanalysis.implementation.util.ResourceDetailsHelper;
import com.azure.core.annotation.Immutable;

/**
 * The ResourceDetails model representing general information regarding the current resource.
 */
@Immutable
public final class ResourceDetails {

    /*
     * The current count of built document analysis models.
     */
    private int customDocumentModelCount;

    /*
     * Max number of models that can be built for this account.
     */
    private int customDocumentModelLimit;

    private QuotaDetails customNeuralDocumentModelQuota;

    /**
     * Get the current count of built document analysis models
     *
     * @return the count value.
     */
    public int getCustomDocumentModelCount() {
        return this.customDocumentModelCount;
    }

    /**
     * Get the max number of models that can be built for this account.
     *
     * @return the limit value.
     */
    public int getCustomDocumentModelLimit() {
        return this.customDocumentModelLimit;
    }

    private void setCustomDocumentModelCount(int customDocumentModelCount) {
        this.customDocumentModelCount = customDocumentModelCount;
    }

    private void setCustomDocumentModelLimit(int customDocumentModelLimit) {
        this.customDocumentModelLimit = customDocumentModelLimit;
    }

    /**
     * Get the customNeuralDocumentModelBuilds property: Quota used, limit, and next reset date/time.
     *
     * @return the customNeuralDocumentModelBuilds value.
     */
    public QuotaDetails getNeuralDocumentModelQuota() {
        return customNeuralDocumentModelQuota;
    }

    private void setCustomNeuralDocumentModelQuota(
        QuotaDetails customNeuralDocumentModelQuota) {
        this.customNeuralDocumentModelQuota = customNeuralDocumentModelQuota;
    }

    static {
        ResourceDetailsHelper.setAccessor(new ResourceDetailsHelper.ResourceDetailsAccessor() {
            @Override
            public void setDocumentModelCount(
                ResourceDetails resourceDetails, int documentModelCount) {
                resourceDetails.setCustomDocumentModelCount(documentModelCount);
            }

            @Override
            public void setDocumentModelLimit(
                ResourceDetails resourceDetails, int documentModelLimit) {
                resourceDetails.setCustomDocumentModelLimit(documentModelLimit);
            }

            @Override
            public void setCustomNeuralDocumentModelBuilds(
                ResourceDetails resourceDetails, QuotaDetails quotaDetails) {
                resourceDetails.setCustomNeuralDocumentModelQuota(quotaDetails);
            }
        });
    }
}
