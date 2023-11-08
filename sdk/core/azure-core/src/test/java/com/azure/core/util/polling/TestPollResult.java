// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.polling;

public class TestPollResult {
    private String status;
    private String resourceLocation;

    public TestPollResult() {
    }

    public TestPollResult(String status) {
        this.status = status;
        this.resourceLocation = null;
    }

    public TestPollResult(String status, String resourceLocation) {
        this.status = status;
        this.resourceLocation = resourceLocation;
    }

    public String getStatus() {
        return status;
    }

    public TestPollResult setStatus(String status) {
        this.status = status;
        return this;
    }

    public String getResourceLocation() {
        return resourceLocation;
    }

    public TestPollResult setResourceLocation(String resourceLocation) {
        this.resourceLocation = resourceLocation;
        return this;
    }

    @Override
    public String toString() {
        return "Status: " + status;
    }
}
