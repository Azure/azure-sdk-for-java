// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.compute.implementation;

import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.compute.fluent.models.VirtualMachineExtensionInner;
import com.azure.resourcemanager.compute.models.VirtualMachineIdentity;
import com.azure.resourcemanager.compute.models.VirtualMachineIdentityUserAssignedIdentities;
import com.azure.resourcemanager.compute.fluent.models.VirtualMachineInner;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SerializationTests {
    @Test
    public void testIdentity() throws IOException {
        SerializerAdapter adapter = SerializerFactory.createDefaultManagementSerializerAdapter();

        Map<String, VirtualMachineIdentityUserAssignedIdentities> userAssignedIdentities = new HashMap<>();
        userAssignedIdentities.put("af.B/C", new VirtualMachineIdentityUserAssignedIdentities());
        userAssignedIdentities.put("af.B/D", new VirtualMachineIdentityUserAssignedIdentities());

        VirtualMachineIdentity identity = new VirtualMachineIdentity();
        identity.withUserAssignedIdentities(userAssignedIdentities);

        VirtualMachineInner virtualMachine = new VirtualMachineInner();
        virtualMachine.withIdentity(identity);

        virtualMachine.withLicenseType("abs");

        String serialized = adapter.serialize(virtualMachine, SerializerEncoding.JSON);

        System.out.println(serialized);
    }

    @Test
    public void testExtension() throws IOException {
        // invalid JSON object or JSON string will result in empty settings
        VirtualMachineExtensionInner extensionInner = new VirtualMachineExtensionInner().withSettings("invalidJSON").withProtectedSettings("invalidJSON");
        VirtualMachineExtensionImpl extension = new VirtualMachineExtensionImpl("myExt", null, extensionInner, null);
        Assertions.assertEquals(0, extension.publicSettings().size());

        extensionInner = new VirtualMachineExtensionInner().withSettings(123).withProtectedSettings(456);
        extension = new VirtualMachineExtensionImpl("myExt", null, extensionInner, null);
        Assertions.assertEquals(0, extension.publicSettings().size());

        // valid JSON string will result in non-empty settings
        Map<String, Object> settings = new HashMap<>();
        settings.put("my-setting", "my-value");
        SerializerAdapter serializerAdapter = SerializerFactory.createDefaultManagementSerializerAdapter();
        extensionInner = new VirtualMachineExtensionInner()
            .withSettings(serializerAdapter.serialize(settings, SerializerEncoding.JSON))
            .withProtectedSettings(serializerAdapter.serialize(settings, SerializerEncoding.JSON));
        extension = new VirtualMachineExtensionImpl("myExt", null, extensionInner, null);
        Assertions.assertEquals("my-value", extension.publicSettings().get("my-setting"));
    }
}
