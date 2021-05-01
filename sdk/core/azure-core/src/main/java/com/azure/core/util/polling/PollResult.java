// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.polling;

// I don't know why a poll result type is needed for a Poller but sure I will create one just for now
public class PollResult {
    private String status;
    private String resourceLocation;

    public String getStatus() {
        return status;
    }

    public PollResult setStatus(String status) {
        this.status = status;
        return this;
    }

    public String getResourceLocation() {
        return resourceLocation;
    }

    public void setResourceLocation(String resourceLocation) {
        this.resourceLocation = resourceLocation;
    }
}
