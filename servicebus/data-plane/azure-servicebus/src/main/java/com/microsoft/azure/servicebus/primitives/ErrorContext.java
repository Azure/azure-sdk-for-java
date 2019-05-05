// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.servicebus.primitives;

import java.util.Locale;

abstract class ErrorContext {
    private final String namespaceName;

    ErrorContext(final String namespaceName) {
        this.namespaceName = namespaceName;
    }

    protected String getNamespaceName() {
        return this.namespaceName;
    }

    @Override
    public String toString() {
        return StringUtil.isNullOrEmpty(this.namespaceName) ? "" : String.format(Locale.US, "NS: %s", this.namespaceName);
    }
}
