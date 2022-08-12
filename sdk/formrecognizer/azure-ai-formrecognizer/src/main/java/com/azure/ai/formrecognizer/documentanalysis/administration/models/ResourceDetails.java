// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.documentanalysis.administration.models;

import com.azure.ai.formrecognizer.documentanalysis.implementation.util.ResourceDetailsHelper;

/**
 * The ResourceDetails model.
 */
public final class ResourceDetails {

    /*
     * The current count of built document analysis models.
     */
    private int documentModelCount;

    /*
     * Max number of models that can be built for this account.
     */
    private int documentModelLimit;

    /**
     * Get the current count of built document analysis models
     *
     * @return the count value.
     */
    public int getDocumentModelCount() {
        return this.documentModelCount;
    }

    /**
     * Get the max number of models that can be built for this account.
     *
     * @return the limit value.
     */
    public int getDocumentModelLimit() {
        return this.documentModelLimit;
    }

    void setDocumentModelCount(int documentModelCount) {
        this.documentModelCount = documentModelCount;
    }

    void setDocumentModelLimit(int documentModelLimit) {
        this.documentModelLimit = documentModelLimit;
    }

    static {
        ResourceDetailsHelper.setAccessor(new ResourceDetailsHelper.ResourceDetailsAccessor() {
            @Override
            public void setDocumentModelCount(
                ResourceDetails resourceDetails, int documentModelCount) {
                resourceDetails.setDocumentModelCount(documentModelCount);
            }

            @Override
            public void setDocumentModelLimit(
                ResourceDetails resourceDetails, int documentModelLimit) {
                resourceDetails.setDocumentModelLimit(documentModelLimit);
            }
        });
    }
}
