// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.polling;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class TestResourcePollResult {
    private final String status;

    @JsonCreator
    public TestResourcePollResult(@JsonProperty(value = "status", required = true) String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
