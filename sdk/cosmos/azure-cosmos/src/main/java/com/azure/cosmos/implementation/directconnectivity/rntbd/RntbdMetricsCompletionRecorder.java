package com.azure.cosmos.implementation.directconnectivity.rntbd;

public interface RntbdMetricsCompletionRecorder {
    void markComplete(RntbdRequestRecord requestRecord);
}
