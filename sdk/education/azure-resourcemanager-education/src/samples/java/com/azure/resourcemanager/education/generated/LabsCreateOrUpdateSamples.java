// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.education.generated;

import com.azure.resourcemanager.education.fluent.models.Amount;
import com.azure.resourcemanager.education.fluent.models.LabDetailsInner;
import java.time.OffsetDateTime;

/**
 * Samples for Labs CreateOrUpdate.
 */
public final class LabsCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/education/resource-manager/Microsoft.Education/preview/2021-12-01-preview/examples/CreateLab.json
     */
    /**
     * Sample code: CreateLab.
     * 
     * @param manager Entry point to EducationManager.
     */
    public static void createLab(com.azure.resourcemanager.education.EducationManager manager) {
        manager.labs()
            .createOrUpdateWithResponse("{billingAccountName}", "{billingProfileName}", "{invoiceSectionName}",
                new LabDetailsInner().withDisplayName("example lab")
                    .withBudgetPerStudent(new Amount().withCurrency("USD").withValue(100.0F))
                    .withDescription("example lab description")
                    .withExpirationDate(OffsetDateTime.parse("2021-12-09T22:11:29.422Z")),
                com.azure.core.util.Context.NONE);
    }
}
