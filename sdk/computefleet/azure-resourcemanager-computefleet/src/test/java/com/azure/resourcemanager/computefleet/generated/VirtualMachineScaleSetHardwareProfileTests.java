// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) TypeSpec Code Generator.

package com.azure.resourcemanager.computefleet.generated;

import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.computefleet.models.VMSizeProperties;
import com.azure.resourcemanager.computefleet.models.VirtualMachineScaleSetHardwareProfile;
import org.junit.jupiter.api.Assertions;

public final class VirtualMachineScaleSetHardwareProfileTests {
    @org.junit.jupiter.api.Test
    public void testDeserialize() throws Exception {
        VirtualMachineScaleSetHardwareProfile model
            = BinaryData.fromString("{\"vmSizeProperties\":{\"vCPUsAvailable\":1603221085,\"vCPUsPerCore\":591654502}}")
                .toObject(VirtualMachineScaleSetHardwareProfile.class);
        Assertions.assertEquals(1603221085, model.vmSizeProperties().vCPUsAvailable());
        Assertions.assertEquals(591654502, model.vmSizeProperties().vCPUsPerCore());
    }

    @org.junit.jupiter.api.Test
    public void testSerialize() throws Exception {
        VirtualMachineScaleSetHardwareProfile model = new VirtualMachineScaleSetHardwareProfile()
            .withVmSizeProperties(new VMSizeProperties().withVCPUsAvailable(1603221085).withVCPUsPerCore(591654502));
        model = BinaryData.fromObject(model).toObject(VirtualMachineScaleSetHardwareProfile.class);
        Assertions.assertEquals(1603221085, model.vmSizeProperties().vCPUsAvailable());
        Assertions.assertEquals(591654502, model.vmSizeProperties().vCPUsPerCore());
    }
}
