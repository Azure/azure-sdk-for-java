// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.implementation.util;

import com.azure.ai.formrecognizer.administration.models.AccountProperties;

/**
 * The helper class to set the non-public properties of an {@link AccountProperties} instance.
 */
public final class AccountPropertiesHelper {
    private static AccountPropertiesAccessor accessor;

    private AccountPropertiesHelper() {
    }

    /**
     * Type defining the methods to set the non-public properties of an {@link AccountProperties} instance.
     */
    public interface AccountPropertiesAccessor {
        void setDocumentModelCount(AccountProperties accountProperties, int documentModelCount);
        void setDocumentModelLimit(AccountProperties accountProperties, int documentModelLimit);
    }

    /**
     * The method called from {@link AccountProperties} to set it's accessor.
     *
     * @param accountPropertiesAccessor The accessor.
     */
    public static void setAccessor(final AccountPropertiesHelper.AccountPropertiesAccessor accountPropertiesAccessor) {
        accessor = accountPropertiesAccessor;
    }

    static void setDocumentModelCount(AccountProperties accountProperties, int documentModelCount) {
        accessor.setDocumentModelCount(accountProperties, documentModelCount);
    }

    static void setDocumentModelLimit(AccountProperties accountProperties, int documentModelLimit) {
        accessor.setDocumentModelLimit(accountProperties, documentModelLimit);
    }
}
