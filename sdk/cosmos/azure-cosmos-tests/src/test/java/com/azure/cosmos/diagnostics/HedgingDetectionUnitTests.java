// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.diagnostics;

import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.implementation.DiagnosticsClientContext;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.TestUtils;
import com.azure.cosmos.models.RequestedRegion;
import com.azure.cosmos.models.RequestedRegionReason;
import org.testng.annotations.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit-level tests for the hedging-detection state on {@link CosmosDiagnostics}.
 *
 * <p>Covers AC1, AC2, AC4, AC5, AC8 (lock-guarded compound atomicity), AC10 (unmodifiable
 * snapshot) and the spec invariants in {@code public-spec-java.md} §M5 / §M6 / §M8 (compound
 * atomicity of HEDGING dispatch + {@code hedgingStarted = true} flip).
 *
 * <p>Behavioural ACs that require the orchestrator wiring (AC3, AC6, AC7, AC9, AC11, AC14) are
 * exercised by end-to-end / fault-injection tests against the live multi-region service; this
 * class only covers the parts that are observable without a network/orchestrator.
 */
public class HedgingDetectionUnitTests {

    private static final ImplementationBridgeHelpers.CosmosDiagnosticsHelper.CosmosDiagnosticsAccessor DIAG_ACCESSOR =
        ImplementationBridgeHelpers.CosmosDiagnosticsHelper.getCosmosDiagnosticsAccessor();

    private static CosmosDiagnostics newDiagnostics() {
        DiagnosticsClientContext ctx = TestUtils.mockDiagnosticsClientContext();
        return ctx.createDiagnostics();
    }

    @Test(groups = {"unit"})
    public void newDiagnosticsHasEmptyHedgingState() {
        CosmosDiagnostics diagnostics = newDiagnostics();

        assertThat(diagnostics.isHedgingStarted()).isFalse();
        assertThat(diagnostics.getRequestedRegions()).isEmpty();
        assertThat(diagnostics.getRespondedRegions()).isEmpty();
    }

    @Test(groups = {"unit"})
    public void appendingHedgingFlipsHedgingStartedAtomically() {
        CosmosDiagnostics diagnostics = newDiagnostics();

        DIAG_ACCESSOR.appendRequestedRegion(diagnostics, new RequestedRegion("East US", RequestedRegionReason.INITIAL));
        assertThat(diagnostics.isHedgingStarted()).isFalse();

        DIAG_ACCESSOR.appendRequestedRegion(diagnostics, new RequestedRegion("West US", RequestedRegionReason.HEDGING));

        // Both writes occur under regionLock in a single appendRequestedRegionInternal call,
        // so any reader (also under the lock) sees the list update AND the flag flip together.
        assertThat(diagnostics.isHedgingStarted()).isTrue();
        assertThat(diagnostics.getRequestedRegions())
            .extracting(RequestedRegion::getReason)
            .contains(RequestedRegionReason.HEDGING);
    }

    @Test(groups = {"unit"})
    public void nonHedgingReasonsDoNotFlipHedgingStarted() {
        CosmosDiagnostics diagnostics = newDiagnostics();

        DIAG_ACCESSOR.appendRequestedRegion(diagnostics, new RequestedRegion("East US", RequestedRegionReason.INITIAL));
        DIAG_ACCESSOR.appendRequestedRegion(diagnostics, new RequestedRegion("East US", RequestedRegionReason.OPERATION_RETRY));
        DIAG_ACCESSOR.appendRequestedRegion(diagnostics, new RequestedRegion("West US", RequestedRegionReason.REGION_FAILOVER));
        DIAG_ACCESSOR.appendRequestedRegion(diagnostics, new RequestedRegion("West US", RequestedRegionReason.TRANSPORT_RETRY));
        DIAG_ACCESSOR.appendRequestedRegion(diagnostics, new RequestedRegion("West US", RequestedRegionReason.CIRCUIT_BREAKER_PROBE));

        assertThat(diagnostics.isHedgingStarted()).isFalse();
        assertThat(diagnostics.getRequestedRegions()).hasSize(5);
    }

    @Test(groups = {"unit"})
    public void requestedRegionsPreserveFifoOrder() {
        CosmosDiagnostics diagnostics = newDiagnostics();

        DIAG_ACCESSOR.appendRequestedRegion(diagnostics, new RequestedRegion("East US", RequestedRegionReason.INITIAL));
        DIAG_ACCESSOR.appendRequestedRegion(diagnostics, new RequestedRegion("West US", RequestedRegionReason.HEDGING));
        DIAG_ACCESSOR.appendRequestedRegion(diagnostics, new RequestedRegion("North Europe", RequestedRegionReason.HEDGING));
        DIAG_ACCESSOR.appendRequestedRegion(diagnostics, new RequestedRegion("North Europe", RequestedRegionReason.OPERATION_RETRY));

        List<RequestedRegion> regions = diagnostics.getRequestedRegions();
        assertThat(regions).containsExactly(
            new RequestedRegion("East US", RequestedRegionReason.INITIAL),
            new RequestedRegion("West US", RequestedRegionReason.HEDGING),
            new RequestedRegion("North Europe", RequestedRegionReason.HEDGING),
            new RequestedRegion("North Europe", RequestedRegionReason.OPERATION_RETRY));
    }

    @Test(groups = {"unit"})
    public void respondedRegionsAllowDuplicatesInArrivalOrder() {
        // Gate Q9=A: duplicates allowed in getRespondedRegions. A region that produces multiple
        // responses (late hedge response, retry) must appear once per response.
        CosmosDiagnostics diagnostics = newDiagnostics();

        DIAG_ACCESSOR.appendRespondedRegion(diagnostics, "East US");
        DIAG_ACCESSOR.appendRespondedRegion(diagnostics, "West US");
        DIAG_ACCESSOR.appendRespondedRegion(diagnostics, "East US");

        assertThat(diagnostics.getRespondedRegions()).containsExactly("East US", "West US", "East US");
    }

    @Test(groups = {"unit"})
    public void getRequestedRegionsReturnsUnmodifiableSnapshot() {
        CosmosDiagnostics diagnostics = newDiagnostics();
        DIAG_ACCESSOR.appendRequestedRegion(diagnostics, new RequestedRegion("East US", RequestedRegionReason.INITIAL));

        List<RequestedRegion> snapshot = diagnostics.getRequestedRegions();

        assertThatThrownBy(() -> snapshot.add(new RequestedRegion("West US", RequestedRegionReason.HEDGING)))
            .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test(groups = {"unit"})
    public void getRespondedRegionsReturnsUnmodifiableSnapshot() {
        CosmosDiagnostics diagnostics = newDiagnostics();
        DIAG_ACCESSOR.appendRespondedRegion(diagnostics, "East US");

        List<String> snapshot = diagnostics.getRespondedRegions();

        assertThatThrownBy(() -> snapshot.add("West US"))
            .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test(groups = {"unit"})
    public void snapshotIsDecoupledFromLaterMutations() {
        // The list returned at time T must NOT reflect appends after T (defensive snapshot).
        CosmosDiagnostics diagnostics = newDiagnostics();
        DIAG_ACCESSOR.appendRequestedRegion(diagnostics, new RequestedRegion("East US", RequestedRegionReason.INITIAL));

        List<RequestedRegion> snapshotBefore = diagnostics.getRequestedRegions();
        DIAG_ACCESSOR.appendRequestedRegion(diagnostics, new RequestedRegion("West US", RequestedRegionReason.HEDGING));
        List<RequestedRegion> snapshotAfter = diagnostics.getRequestedRegions();

        assertThat(snapshotBefore).hasSize(1);
        assertThat(snapshotAfter).hasSize(2);
    }

    @Test(groups = {"unit"}, timeOut = 30_000L)
    public void compoundAtomicityIsPreservedUnderConcurrentWriters() throws InterruptedException {
        // 16 writer threads, each appending 1000 HEDGING entries to the same diagnostics.
        // Invariant: every reader that observes isHedgingStarted() == true must also see at
        // least one HEDGING entry in getRequestedRegions(), and vice-versa.
        CosmosDiagnostics diagnostics = newDiagnostics();

        final int writers = 16;
        final int perWriter = 1000;
        final CountDownLatch latch = new CountDownLatch(writers);
        final AtomicInteger violations = new AtomicInteger();

        Thread reader = new Thread(() -> {
            while (latch.getCount() > 0) {
                boolean flag = diagnostics.isHedgingStarted();
                List<RequestedRegion> regions = diagnostics.getRequestedRegions();
                boolean hasHedge = regions.stream().anyMatch(r -> r.getReason() == RequestedRegionReason.HEDGING);
                if (flag != hasHedge) {
                    violations.incrementAndGet();
                }
            }
        }, "hedging-detection-reader");
        reader.setDaemon(true);
        reader.start();

        for (int w = 0; w < writers; w++) {
            final int writerId = w;
            new Thread(() -> {
                try {
                    for (int i = 0; i < perWriter; i++) {
                        DIAG_ACCESSOR.appendRequestedRegion(diagnostics,
                            new RequestedRegion("Region-" + writerId, RequestedRegionReason.HEDGING));
                    }
                } finally {
                    latch.countDown();
                }
            }, "hedging-detection-writer-" + w).start();
        }

        latch.await();
        reader.join(5_000L);

        assertThat(violations.get())
            .as("isHedgingStarted() and getRequestedRegions() must agree under concurrent writers (M5/M6/M8)")
            .isZero();
        assertThat(diagnostics.isHedgingStarted()).isTrue();
        assertThat(diagnostics.getRequestedRegions()).hasSize(writers * perWriter);
    }
}
