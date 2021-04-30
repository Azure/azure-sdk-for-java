// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.polling;

// I don't know why a poll result type is needed for a Poller but sure I will create one just for now
public class PollResult {
    private String nextPollUrl;

    public String getNextPollUrl() {
        return nextPollUrl;
    }

    public PollResult setNextPollUrl(String nextPollUrl) {
        this.nextPollUrl = nextPollUrl;
        return this;
    }
}
