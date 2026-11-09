// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.rntbd;

public interface RntbdMetricsCompletionRecorder {
    public final static RntbdMetricsCompletionRecorder NoOpSingletonInstance = new NoOpRecorder();
    void markComplete(RntbdRequestRecord requestRecord);

    public final static class NoOpRecorder implements RntbdMetricsCompletionRecorder {

        private NoOpRecorder() {}

        @Override
        public void markComplete(RntbdRequestRecord requestRecord) {
            requestRecord.stop();
        }
    }
}
