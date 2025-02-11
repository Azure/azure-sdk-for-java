// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.analytics.defender.easm;

import com.azure.analytics.defender.easm.models.ReportAssetSnapshotRequest;
import com.azure.analytics.defender.easm.models.ReportAssetSnapshotResult;
import com.azure.analytics.defender.easm.models.ReportBillableAssetSummaryResult;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;

public class ReportsAsyncTest extends EasmClientTestBase {
    String metric = "savedfilter_metric_51126";

    @Test
    public void testReportsBillableAsync() {
        Mono<ReportBillableAssetSummaryResult> reportBillableAssetSummaryResultMono = easmAsyncClient.getBillable();
        StepVerifier.create(reportBillableAssetSummaryResultMono).assertNext(reportBillableAssetSummaryResult -> {
            assertTrue(reportBillableAssetSummaryResult.getAssetSummaries().size() > 0);
        }).expectComplete().verify(DEFAULT_TIMEOUT);
    }

    @Test
    public void testReportsSnapshotAsync() {
        ReportAssetSnapshotRequest reportAssetSnapshotRequest
            = new ReportAssetSnapshotRequest().setMetric(metric).setPage(0).setSize(25);
        Mono<ReportAssetSnapshotResult> reportAssetSnapshotResultMono
            = easmAsyncClient.getSnapshot(reportAssetSnapshotRequest);
        StepVerifier.create(reportAssetSnapshotResultMono).assertNext(reportAssetSnapshotResult -> {
            assertNotNull(reportAssetSnapshotResult.getDisplayName());
            assertEquals(metric, reportAssetSnapshotResult.getMetric());
            assertNotNull(reportAssetSnapshotResult.getDescription());
            assertNotNull(reportAssetSnapshotResult.getAssets());
        }).expectComplete().verify(DEFAULT_TIMEOUT);
    }
}
