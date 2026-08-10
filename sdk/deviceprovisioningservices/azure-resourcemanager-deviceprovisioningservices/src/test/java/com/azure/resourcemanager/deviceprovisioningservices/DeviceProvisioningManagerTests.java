// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.deviceprovisioningservices;

import com.azure.core.test.annotation.LiveOnly;
import org.junit.jupiter.api.Test;

public class DeviceProvisioningManagerTests extends DeviceProvisioningTestBase {
    @Test
    @LiveOnly
    public void testListProvisioningServices() {
        createIotDpsManager().iotDpsResources().list().stream().count();
    }
}
