// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.polling;

public interface PollingStrategy {
    boolean canPoll();
    String getPollingUrl();
    String getFinalResultUrl();
    void setInitialStatus(PollResponse<?> response);
    LongRunningOperationStatus getStatus(PollResponse<?> response);
}
