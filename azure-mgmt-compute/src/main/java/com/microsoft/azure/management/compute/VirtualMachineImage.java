package com.microsoft.azure.management.compute;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.compute.implementation.api.DataDiskImage;
import com.microsoft.azure.management.compute.implementation.api.OSDiskImage;
import com.microsoft.azure.management.compute.implementation.api.PurchasePlan;
import com.microsoft.azure.management.compute.implementation.api.VirtualMachineImageInner;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;

import java.io.IOException;
import java.util.List;

public interface VirtualMachineImage extends
        Wrapper<VirtualMachineImageInner> {
    Region location();
    String publisher();
    String offer();
    String sku();
    String version();
    PurchasePlan plan();
    OSDiskImage osDiskImage();
    List<DataDiskImage> dataDiskImages();

    interface Publisher {
        Region location();
        String publisher();
        List<Offer> listOffers() throws CloudException, IOException;
    }

    interface Offer {
        Region location();
        String publisher();
        String offer();
        List<Sku> listSkus() throws CloudException, IOException;
    }

    interface Sku {
        Region location();
        String publisher();
        String offer();
        String sku();
        List<VirtualMachineImage> listImages() throws CloudException, IOException;
    }
}
