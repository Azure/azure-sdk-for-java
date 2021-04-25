// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.jca.implementation.http.model;

import java.io.Serializable;
import java.util.List;

public class CertificateListResult implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private List<CertificateItem> value;

    public List<CertificateItem> getValue() {
        return value;
    }

    public void setValue(List<CertificateItem> value) {
        this.value = value;
    }
}
