// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.administration.models;

import com.azure.ai.formrecognizer.implementation.util.AccountPropertiesHelper;

/**
 * The AccountProperties model.
 */
public final class AccountProperties {

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
        AccountPropertiesHelper.setAccessor(new AccountPropertiesHelper.AccountPropertiesAccessor() {
            @Override
            public void setDocumentModelCount(
                AccountProperties accountProperties, int documentModelCount) {
                accountProperties.setDocumentModelCount(documentModelCount);
            }

            @Override
            public void setDocumentModelLimit(
                AccountProperties accountProperties, int documentModelLimit) {
                accountProperties.setDocumentModelLimit(documentModelLimit);
            }
        });
    }
}
