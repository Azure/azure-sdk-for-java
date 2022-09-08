// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.domain;

import com.azure.spring.data.cosmos.common.TestConstants;
import com.azure.spring.data.cosmos.core.mapping.Container;

@Container(ru = TestConstants.AUTOSCALE_MAX_THROUGHPUT, autoScale = true)
public class AutoScaleSample {
    private String id;

    public AutoScaleSample() {
    }

    public AutoScaleSample(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
