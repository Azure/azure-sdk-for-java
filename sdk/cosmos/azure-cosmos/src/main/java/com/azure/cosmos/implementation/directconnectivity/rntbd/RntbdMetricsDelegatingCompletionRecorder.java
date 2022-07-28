package com.azure.cosmos.implementation.directconnectivity.rntbd;

import java.util.List;

public class RntbdMetricsDelegatingCompletionRecorder implements RntbdMetricsCompletionRecorder {

    private final List<RntbdMetricsCompletionRecorder> completionRecorders;

    public RntbdMetricsDelegatingCompletionRecorder(
        List<RntbdMetricsCompletionRecorder> completionRecorders) {

        this.completionRecorders = completionRecorders;
    }

    @Override
    public void markComplete(RntbdRequestRecord requestRecord) {
        for (RntbdMetricsCompletionRecorder current : completionRecorders) {
            current.markComplete(requestRecord);
        }
    }
}
