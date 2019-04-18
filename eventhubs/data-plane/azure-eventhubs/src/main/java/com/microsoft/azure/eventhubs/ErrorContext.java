// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs;

import com.microsoft.azure.eventhubs.impl.StringUtil;

import java.io.Serializable;
import java.util.Locale;

public abstract class ErrorContext implements Serializable {
    private final String namespaceName;

    protected ErrorContext(final String namespaceName) {
        this.namespaceName = namespaceName;
    }

    protected String getNamespaceName() {
        return this.namespaceName;
    }

    @Override
    public String toString() {
        return StringUtil.isNullOrEmpty(this.namespaceName)
                ? ""
                : String.format(Locale.US, "NS: %s", this.namespaceName);
    }
}
