// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.jca.implementation.http.model;

import java.io.Serializable;

public class KeyProperties implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean exportable;

    public boolean isExportable() {
        return exportable;
    }

    public void setExportable(boolean exportable) {
        this.exportable = exportable;
    }
}
