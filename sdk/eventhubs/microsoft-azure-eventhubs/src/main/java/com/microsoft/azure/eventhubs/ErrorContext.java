// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs;

import com.microsoft.azure.eventhubs.impl.StringUtil;

import java.io.Serializable;
import java.util.Locale;

public abstract class ErrorContext implements Serializable {
    private static final long serialVersionUID = -841174412304936908L;
    
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
