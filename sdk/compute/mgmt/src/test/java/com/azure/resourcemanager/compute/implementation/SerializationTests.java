// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.compute.implementation;

import com.azure.core.management.serializer.AzureJacksonAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.compute.models.VirtualMachineIdentity;
import com.azure.resourcemanager.compute.models.VirtualMachineIdentityUserAssignedIdentities;
import com.azure.resourcemanager.compute.fluent.inner.VirtualMachineInner;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class SerializationTests {
    @Test
    public void test1() throws IOException {
        AzureJacksonAdapter jacksonAdapter = new AzureJacksonAdapter();

        Map<String, VirtualMachineIdentityUserAssignedIdentities> userAssignedIdentities = new HashMap<>();
        userAssignedIdentities.put("af.B/C", new VirtualMachineIdentityUserAssignedIdentities());
        userAssignedIdentities.put("af.B/D", new VirtualMachineIdentityUserAssignedIdentities());

        VirtualMachineIdentity identity = new VirtualMachineIdentity();
        identity.withUserAssignedIdentities(userAssignedIdentities);

        VirtualMachineInner virtualMachine = new VirtualMachineInner();
        virtualMachine.withIdentity(identity);

        virtualMachine.withLicenseType("abs");

        String serialized = jacksonAdapter.serialize(virtualMachine, SerializerEncoding.JSON);

        System.out.println(serialized);
    }
}
