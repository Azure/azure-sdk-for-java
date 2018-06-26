/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
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
                ? null
                : String.format(Locale.US, "NS: %s", this.namespaceName);
    }
}
