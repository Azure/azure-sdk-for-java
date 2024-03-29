// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.hybridnetwork.generated;

import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.hybridnetwork.models.DependsOnProfile;
import java.util.Arrays;
import org.junit.jupiter.api.Assertions;

public final class DependsOnProfileTests {
    @org.junit.jupiter.api.Test
    public void testDeserialize() throws Exception {
        DependsOnProfile model = BinaryData.fromString(
            "{\"installDependsOn\":[\"yc\",\"rauwjuetaebu\",\"u\"],\"uninstallDependsOn\":[\"ovsm\",\"l\",\"wabm\",\"oefki\"],\"updateDependsOn\":[\"tpuqujmq\",\"gkfbtndoaong\",\"jcntuj\"]}")
            .toObject(DependsOnProfile.class);
        Assertions.assertEquals("yc", model.installDependsOn().get(0));
        Assertions.assertEquals("ovsm", model.uninstallDependsOn().get(0));
        Assertions.assertEquals("tpuqujmq", model.updateDependsOn().get(0));
    }

    @org.junit.jupiter.api.Test
    public void testSerialize() throws Exception {
        DependsOnProfile model = new DependsOnProfile().withInstallDependsOn(Arrays.asList("yc", "rauwjuetaebu", "u"))
            .withUninstallDependsOn(Arrays.asList("ovsm", "l", "wabm", "oefki"))
            .withUpdateDependsOn(Arrays.asList("tpuqujmq", "gkfbtndoaong", "jcntuj"));
        model = BinaryData.fromObject(model).toObject(DependsOnProfile.class);
        Assertions.assertEquals("yc", model.installDependsOn().get(0));
        Assertions.assertEquals("ovsm", model.uninstallDependsOn().get(0));
        Assertions.assertEquals("tpuqujmq", model.updateDependsOn().get(0));
    }
}
