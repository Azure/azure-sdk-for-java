// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.costmanagement;

import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.test.TestBase;
import com.azure.core.test.annotation.DoNotRecord;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.costmanagement.models.CostDetailsMetricType;
import com.azure.resourcemanager.costmanagement.models.CostDetailsOperationResults;
import com.azure.resourcemanager.costmanagement.models.CostDetailsTimePeriod;
import com.azure.resourcemanager.costmanagement.models.GenerateCostDetailsReportRequestDefinition;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class CostManagementTests extends TestBase {

    @Disabled("Unsupported offer type: AIRS for get detailed report request.Unsupported offer type: AIRS for get detailed report request.")
    @Test
    @DoNotRecord(skipInPlayback = true)
    public void testGenerateCostDetailsReports() {
        CostManagementManager costManagementManager = CostManagementManager
            .configure().withLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC))
            .authenticate(new DefaultAzureCredentialBuilder().build(), new AzureProfile(AzureEnvironment.AZURE));

        final String subscriptionId = "subscriptions/xxx";

        CostDetailsOperationResults result = costManagementManager.generateCostDetailsReports().createOperation(
            subscriptionId,
            new GenerateCostDetailsReportRequestDefinition()
                .withMetric(CostDetailsMetricType.ACTUAL_COST)
                .withTimePeriod(new CostDetailsTimePeriod().withStart("2023-01-01").withEnd("2023-01-31")));

        Assertions.assertNotNull(result.name());
    }
}
