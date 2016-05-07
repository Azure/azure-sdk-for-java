package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.management.compute.VirtualMachineImage;

public enum KnownVirtualMachineImage {
    UBUNTU_14_04_LTS(new VirtualMachineImageImpl(
            "Canonical", "UbuntuServer", "14.04.4-LTS", "14.04.201604060"
    )),
    UBUNTU_16_04_LTS(new VirtualMachineImageImpl(
            "Canonical", "UbuntuServer", "16.04.0-LTS", "16.04.201604203"
    )),
    WINDOWS_SERVER_2008_R2_SP1(new VirtualMachineImageImpl(
            "MicrosoftWindowsServer", "WindowsServer", "2008-R2-SP1", "2.0.20160430"
    )),
    WINDOWS_SERVER_2012_DATACENTER(new VirtualMachineImageImpl(
            "MicrosoftWindowsServer", "WindowsServer", "2012-Datacenter", "3.0.20160430"
    )),
    WINDOWS_SERVER_2012_R2_DATACENTER(new VirtualMachineImageImpl(
            "MicrosoftWindowsServer", "WindowsServer", "2012-R2-Datacenter", "4.0.20160430"
    )),
    WINDOWS_SERVER_2016_TECHNICAL_PREVIEW_WITH_CONTAINIERS(new VirtualMachineImageImpl(
            "MicrosoftWindowsServer", "WindowsServer", "2016-Technical-Preview-with-Containers", "2016.0.20151118"
    )),
    WINDOWS_SERVER_TECHNICAL_PREVIEW(new VirtualMachineImageImpl(
            "MicrosoftWindowsServer", "WindowsServer", "Windows-Server-Technical-Preview", "5.0.20160420"
    ));

    private final VirtualMachineImage image;

    KnownVirtualMachineImage(VirtualMachineImage image) {
        this.image = image;
    }

    public VirtualMachineImage image() {
        return this.image;
    }
}
