// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.billing.generated;

import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.billing.models.EnrollmentDetailsIndirectRelationshipInfo;
import org.junit.jupiter.api.Assertions;

public final class EnrollmentDetailsIndirectRelationshipInfoTests {
    @org.junit.jupiter.api.Test
    public void testDeserialize() throws Exception {
        EnrollmentDetailsIndirectRelationshipInfo model = BinaryData.fromString(
            "{\"billingAccountName\":\"rzpwvlqdqgbiq\",\"billingProfileName\":\"ihkaetcktvfc\",\"displayName\":\"fsnkymuctq\"}")
            .toObject(EnrollmentDetailsIndirectRelationshipInfo.class);
        Assertions.assertEquals("rzpwvlqdqgbiq", model.billingAccountName());
        Assertions.assertEquals("ihkaetcktvfc", model.billingProfileName());
        Assertions.assertEquals("fsnkymuctq", model.displayName());
    }

    @org.junit.jupiter.api.Test
    public void testSerialize() throws Exception {
        EnrollmentDetailsIndirectRelationshipInfo model
            = new EnrollmentDetailsIndirectRelationshipInfo().withBillingAccountName("rzpwvlqdqgbiq")
                .withBillingProfileName("ihkaetcktvfc")
                .withDisplayName("fsnkymuctq");
        model = BinaryData.fromObject(model).toObject(EnrollmentDetailsIndirectRelationshipInfo.class);
        Assertions.assertEquals("rzpwvlqdqgbiq", model.billingAccountName());
        Assertions.assertEquals("ihkaetcktvfc", model.billingProfileName());
        Assertions.assertEquals("fsnkymuctq", model.displayName());
    }
}
