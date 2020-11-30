// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.domain;

import com.azure.spring.data.cosmos.common.TestConstants;
import com.azure.spring.data.cosmos.core.mapping.Container;

@Container(timeToLive = TestConstants.TIME_TO_LIVE)
public class TimeToLiveSample {
    private String id;

    public TimeToLiveSample() {
    }

    public TimeToLiveSample(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
