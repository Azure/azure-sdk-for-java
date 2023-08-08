package com.azure.analytics.defender.easm;

import com.azure.analytics.defender.easm.generated.EasmDefenderClientTestBase;
import com.azure.analytics.defender.easm.models.*;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class ReportsTest extends EasmDefenderClientTestBase {
    String metric = "savedfilter_metric_51126";

    @Test
    public void testreportsBillableWithResponse(){
        ReportBillableAssetSummaryResponse reportBillableAssetSummaryResponse = reportsClient.getBillable();
        assertTrue(reportBillableAssetSummaryResponse.getAssetSummaries().size() > 0);
    }

    @Test
    public void testreportsSnapshotWithResponse(){
        ReportAssetSnapshotRequest reportAssetSnapshotRequest = new ReportAssetSnapshotRequest().setMetric(metric).setPage(0).setSize(25);
        ReportAssetSnapshotResponse reportAssetSnapshotResponse = reportsClient.getSnapshot(reportAssetSnapshotRequest);
        assertNotNull(reportAssetSnapshotResponse.getDisplayName());
        assertEquals(metric, reportAssetSnapshotResponse.getMetric());
        assertNotNull(reportAssetSnapshotResponse.getDescription());
        assertNotNull(reportAssetSnapshotResponse.getAssets());
    }

    @Test
    public void testreportsSummaryWithResponse(){
        ReportAssetSummaryRequest reportAssetSummaryRequest = new ReportAssetSummaryRequest().setMetrics(Arrays.asList(metric));
        ReportAssetSummaryResponse reportAssetSummaryResponse = reportsClient.getSummary(reportAssetSummaryRequest);
        assertTrue(reportAssetSummaryResponse.getAssetSummaries().size() > 0);
    }
}
