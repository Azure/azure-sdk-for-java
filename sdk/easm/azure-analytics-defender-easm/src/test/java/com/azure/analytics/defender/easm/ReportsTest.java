// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.analytics.defender.easm;

import com.azure.analytics.defender.easm.models.*;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class ReportsTest extends EasmClientTestBase {
    String metric = "savedfilter_metric_51126";

    @Test
    public void testreportsBillableWithResult() {
        ReportBillableAssetSummaryResult reportBillableAssetSummaryResult = easmClient.getBillable();
        assertTrue(reportBillableAssetSummaryResult.getAssetSummaries().size() > 0);
    }

    @Test
    public void testreportsSnapshotWithResult() {
        ReportAssetSnapshotRequest reportAssetSnapshotRequest
            = new ReportAssetSnapshotRequest().setMetric(metric).setPage(0).setSize(25);
        ReportAssetSnapshotResult reportAssetSnapshotResult = easmClient.getSnapshot(reportAssetSnapshotRequest);
        assertNotNull(reportAssetSnapshotResult.getDisplayName());
        assertEquals(metric, reportAssetSnapshotResult.getMetric());
        assertNotNull(reportAssetSnapshotResult.getDescription());
        assertNotNull(reportAssetSnapshotResult.getAssets());
    }

    @Test
    public void testreportsSummaryWithResult() {
        ReportAssetSummaryRequest reportAssetSummaryRequest
            = new ReportAssetSummaryRequest().setMetrics(Arrays.asList(metric));
        ReportAssetSummaryResult reportAssetSummaryResult = easmClient.getSummary(reportAssetSummaryRequest);
        assertTrue(reportAssetSummaryResult.getAssetSummaries().size() > 0);
    }
}
