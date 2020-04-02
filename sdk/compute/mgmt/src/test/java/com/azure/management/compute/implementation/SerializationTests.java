/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.compute.implementation;

import com.azure.core.management.serializer.AzureJacksonAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.management.compute.VirtualMachineIdentity;
import com.azure.management.compute.VirtualMachineIdentityUserAssignedIdentities;
import com.azure.management.compute.models.VirtualMachineInner;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
