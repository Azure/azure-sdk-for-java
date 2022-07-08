// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.administration.models;

import com.azure.ai.formrecognizer.implementation.util.ResourceInfoHelper;

/**
 * The ResourceInfo model.
 */
public final class ResourceInfo {

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
        ResourceInfoHelper.setAccessor(new ResourceInfoHelper.ResourceInfoAccessor() {
            @Override
            public void setDocumentModelCount(
                ResourceInfo resourceInfo, int documentModelCount) {
                resourceInfo.setDocumentModelCount(documentModelCount);
            }

            @Override
            public void setDocumentModelLimit(
                ResourceInfo resourceInfo, int documentModelLimit) {
                resourceInfo.setDocumentModelLimit(documentModelLimit);
            }
        });
    }
}
