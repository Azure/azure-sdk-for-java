// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.rntbd;

public interface RntbdMetricsCompletionRecorder {
    void markComplete(RntbdRequestRecord requestRecord);
}
