// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.azurestackhci.generated;

import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.azurestackhci.models.ExtensionPatch;
import com.azure.resourcemanager.azurestackhci.models.ExtensionPatchParameters;
import org.junit.jupiter.api.Assertions;

public final class ExtensionPatchTests {
    @org.junit.jupiter.api.Test
    public void testDeserialize() throws Exception {
        ExtensionPatch model = BinaryData.fromString(
            "{\"properties\":{\"extensionParameters\":{\"typeHandlerVersion\":\"sjkmnwqj\",\"enableAutomaticUpgrade\":true,\"settings\":\"dataiyhddvi\",\"protectedSettings\":\"dataegfnmntfpmvmemfn\"}}}")
            .toObject(ExtensionPatch.class);
        Assertions.assertEquals("sjkmnwqj", model.extensionParameters().typeHandlerVersion());
        Assertions.assertEquals(true, model.extensionParameters().enableAutomaticUpgrade());
    }

    @org.junit.jupiter.api.Test
    public void testSerialize() throws Exception {
        ExtensionPatch model = new ExtensionPatch()
            .withExtensionParameters(new ExtensionPatchParameters().withTypeHandlerVersion("sjkmnwqj")
                .withEnableAutomaticUpgrade(true)
                .withSettings("dataiyhddvi")
                .withProtectedSettings("dataegfnmntfpmvmemfn"));
        model = BinaryData.fromObject(model).toObject(ExtensionPatch.class);
        Assertions.assertEquals("sjkmnwqj", model.extensionParameters().typeHandlerVersion());
        Assertions.assertEquals(true, model.extensionParameters().enableAutomaticUpgrade());
    }
}
