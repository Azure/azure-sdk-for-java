// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.batch;

import com.azure.cosmos.CosmosDiagnosticsContext;

public interface BulkExecutorDiagnosticsTracker {
    void trackDiagnostics(CosmosDiagnosticsContext ctx);
    boolean verboseLoggingAfterReEnqueueingRetriesEnabled();
}
