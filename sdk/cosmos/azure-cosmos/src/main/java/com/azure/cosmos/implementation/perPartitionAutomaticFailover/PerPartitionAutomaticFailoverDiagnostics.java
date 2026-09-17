// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.perPartitionAutomaticFailover;

import java.io.Serializable;
import java.time.Instant;

final class PerPartitionAutomaticFailoverDiagnostics implements Serializable {
    private static final long serialVersionUID = 1L;

    static final PerPartitionAutomaticFailoverDiagnostics EMPTY
        = new PerPartitionAutomaticFailoverDiagnostics(null, null);

    private final String currentWriteRegion;
    private final Instant since;

    PerPartitionAutomaticFailoverDiagnostics(String currentWriteRegion, Instant since) {
        this.currentWriteRegion = currentWriteRegion;
        this.since = since;
    }

    String getCurrentWriteRegion() {
        return this.currentWriteRegion;
    }

    Instant getSince() {
        return this.since;
    }
}