// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http;

import com.azure.core.util.Context;
import com.azure.core.util.Contexts;
import com.azure.core.util.ProgressReporter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;

public class HttpPipelineCallContextTest {

    @Test
    public void canGetProgressReporter() {
        ProgressReporter progressReporter = ProgressReporter.withProgressReceiver(
            bytesTransferred -> { });
        Context context = Contexts.empty().setProgressReporter(progressReporter).getContext();

        HttpPipelineCallContext httpPipelineCallContext = new HttpPipelineCallContext(
            new HttpRequest(HttpMethod.GET, "http://foo"), context);

        assertSame(progressReporter, Contexts.with(httpPipelineCallContext).getProgressReporter());
    }
}
