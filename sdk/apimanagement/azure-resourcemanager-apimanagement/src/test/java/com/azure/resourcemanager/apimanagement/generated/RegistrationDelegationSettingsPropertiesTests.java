// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.apimanagement.generated;

import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.apimanagement.models.RegistrationDelegationSettingsProperties;
import org.junit.jupiter.api.Assertions;

public final class RegistrationDelegationSettingsPropertiesTests {
    @org.junit.jupiter.api.Test
    public void testDeserialize() throws Exception {
        RegistrationDelegationSettingsProperties model
            = BinaryData.fromString("{\"enabled\":false}").toObject(RegistrationDelegationSettingsProperties.class);
        Assertions.assertEquals(false, model.enabled());
    }

    @org.junit.jupiter.api.Test
    public void testSerialize() throws Exception {
        RegistrationDelegationSettingsProperties model
            = new RegistrationDelegationSettingsProperties().withEnabled(false);
        model = BinaryData.fromObject(model).toObject(RegistrationDelegationSettingsProperties.class);
        Assertions.assertEquals(false, model.enabled());
    }
}
