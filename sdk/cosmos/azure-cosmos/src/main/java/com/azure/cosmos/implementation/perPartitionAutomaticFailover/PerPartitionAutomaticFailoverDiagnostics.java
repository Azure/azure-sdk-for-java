// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.perPartitionAutomaticFailover;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class PerPartitionAutomaticFailoverDiagnostics implements Serializable {
    private static final long serialVersionUID = 1L;

    static final PerPartitionAutomaticFailoverDiagnostics EMPTY
        = new PerPartitionAutomaticFailoverDiagnostics(null, Collections.emptyList(), null);

    private final String currentWriteRegion;
    private final List<String> failedRegions;
    private final Instant since;

    private PerPartitionAutomaticFailoverDiagnostics(String currentWriteRegion, List<String> failedRegions, Instant since) {
        this.currentWriteRegion = currentWriteRegion;
        this.failedRegions = failedRegions;
        this.since = since;
    }

    PerPartitionAutomaticFailoverDiagnostics withFailover(
        String currentWriteRegion,
        String failedRegion,
        Instant since) {

        List<String> updatedFailedRegions = new ArrayList<>(this.failedRegions.size() + 1);
        updatedFailedRegions.addAll(this.failedRegions);
        updatedFailedRegions.add(failedRegion);

        return new PerPartitionAutomaticFailoverDiagnostics(
            currentWriteRegion,
            Collections.unmodifiableList(updatedFailedRegions),
            since);
    }

    String getCurrentWriteRegion() {
        return this.currentWriteRegion;
    }

    List<String> getFailedRegions() {
        return this.failedRegions;
    }

    Instant getSince() {
        return this.since;
    }
}