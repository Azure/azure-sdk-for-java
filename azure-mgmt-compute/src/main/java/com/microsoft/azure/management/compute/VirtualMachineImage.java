package com.microsoft.azure.management.compute;

import com.microsoft.azure.management.compute.implementation.api.DataDiskImage;
import com.microsoft.azure.management.compute.implementation.api.OSDiskImage;
import com.microsoft.azure.management.compute.implementation.api.PurchasePlan;
import com.microsoft.azure.management.compute.implementation.api.VirtualMachineImageInner;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;

import java.util.List;

public interface VirtualMachineImage extends
        Wrapper<VirtualMachineImageInner> {
    String location();
    String publisher();
    String offer();
    String sku();
    String version();
    PurchasePlan plan();
    OSDiskImage osDiskImage();
    List<DataDiskImage> dataDiskImages();
}
