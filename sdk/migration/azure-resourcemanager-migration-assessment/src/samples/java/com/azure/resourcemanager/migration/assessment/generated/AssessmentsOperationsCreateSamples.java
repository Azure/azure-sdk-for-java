// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.migration.assessment.generated;

import com.azure.resourcemanager.migration.assessment.models.AssessmentSizingCriterion;
import com.azure.resourcemanager.migration.assessment.models.AzureCurrency;
import com.azure.resourcemanager.migration.assessment.models.AzureDiskType;
import com.azure.resourcemanager.migration.assessment.models.AzureHybridUseBenefit;
import com.azure.resourcemanager.migration.assessment.models.AzureOfferCode;
import com.azure.resourcemanager.migration.assessment.models.AzurePricingTier;
import com.azure.resourcemanager.migration.assessment.models.AzureReservedInstance;
import com.azure.resourcemanager.migration.assessment.models.AzureStorageRedundancy;
import com.azure.resourcemanager.migration.assessment.models.AzureVmFamily;
import com.azure.resourcemanager.migration.assessment.models.MachineAssessmentProperties;
import com.azure.resourcemanager.migration.assessment.models.Percentile;
import com.azure.resourcemanager.migration.assessment.models.ProvisioningState;
import com.azure.resourcemanager.migration.assessment.models.TimeRange;
import com.azure.resourcemanager.migration.assessment.models.VmUptime;
import java.time.OffsetDateTime;
import java.util.Arrays;

/**
 * Samples for AssessmentsOperations Create.
 */
public final class AssessmentsOperationsCreateSamples {
    /*
     * x-ms-original-file:
     * specification/migrate/resource-manager/Microsoft.Migrate/AssessmentProjects/stable/2023-03-15/examples/
     * AssessmentsOperations_Create_MaximumSet_Gen.json
     */
    /**
     * Sample code: AssessmentsOperations_Create_MaximumSet_Gen.
     * 
     * @param manager Entry point to MigrationAssessmentManager.
     */
    public static void assessmentsOperationsCreateMaximumSetGen(
        com.azure.resourcemanager.migration.assessment.MigrationAssessmentManager manager) {
        manager.assessmentsOperations()
            .define("asm1")
            .withExistingGroup("ayagrawrg", "app18700project", "kuchatur-test")
            .withProperties(new MachineAssessmentProperties().withProvisioningState(ProvisioningState.SUCCEEDED)
                .withEaSubscriptionId("kwsu")
                .withAzurePricingTier(AzurePricingTier.STANDARD)
                .withAzureStorageRedundancy(AzureStorageRedundancy.UNKNOWN)
                .withReservedInstance(AzureReservedInstance.NONE)
                .withAzureHybridUseBenefit(AzureHybridUseBenefit.UNKNOWN)
                .withAzureDiskTypes(Arrays.asList(AzureDiskType.PREMIUM, AzureDiskType.STANDARD_SSD))
                .withAzureVmFamilies(
                    Arrays.asList(AzureVmFamily.D_SERIES, AzureVmFamily.LSV2_SERIES, AzureVmFamily.M_SERIES,
                        AzureVmFamily.MDSV2_SERIES, AzureVmFamily.MSV2_SERIES, AzureVmFamily.MV2_SERIES))
                .withVmUptime(new VmUptime().withDaysPerMonth(13).withHoursPerDay(26))
                .withAzureLocation("njxbwdtsxzhichsnk")
                .withAzureOfferCode(AzureOfferCode.UNKNOWN)
                .withCurrency(AzureCurrency.UNKNOWN)
                .withScalingFactor(24.0F)
                .withPercentile(Percentile.PERCENTILE50)
                .withTimeRange(TimeRange.DAY)
                .withPerfDataStartTime(OffsetDateTime.parse("2023-09-26T09:36:48.491Z"))
                .withPerfDataEndTime(OffsetDateTime.parse("2023-09-26T09:36:48.491Z"))
                .withDiscountPercentage(6.0F)
                .withSizingCriterion(AssessmentSizingCriterion.PERFORMANCE_BASED))
            .create();
    }
}
