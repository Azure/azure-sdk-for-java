package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.management.compute.implementation.api.ImageReference;

public enum KnownVirtualMachineImage {
    /** Linux */
    UBUNTU_SERVER_14_04_LTS("Canonical", "UbuntuServer", "14.04.4-LTS"),
    UBUNTU_SERVER_16_04_LTS("Canonical", "UbuntuServer", "16.04.0-LTS"),
    DEBIAN_8("credativ", "Debian", "8"),
    CENTOS_7_2("OpenLogic", "CentOS", "7.2"),
    OPENSUSE_LEAP_42_1("SUSE", "openSUSE-Leap", "42.1"),
    SLES_12_SP1("SUSE", "SLES", "12-SP1"),
    /** WINDOWS */
    WINDOWS_SERVER_2008_R2_SP1("MicrosoftWindowsServer", "WindowsServer", "2008-R2-SP1"),
    WINDOWS_SERVER_2012_DATACENTER("MicrosoftWindowsServer", "WindowsServer", "2012-Datacenter"),
    WINDOWS_SERVER_2012_R2_DATACENTER("MicrosoftWindowsServer", "WindowsServer", "2012-R2-Datacenter"),
    WINDOWS_SERVER_2016_TECHNICAL_PREVIEW_WITH_CONTAINIERS("MicrosoftWindowsServer", "WindowsServer", "2016-Technical-Preview-with-Containers"),
    WINDOWS_SERVER_TECHNICAL_PREVIEW("MicrosoftWindowsServer", "WindowsServer", "Windows-Server-Technical-Preview");

    private final String publisher;
    private final String offer;
    private final String sku;

    KnownVirtualMachineImage(String publisher, String offer, String sku) {
        this.publisher = publisher;
        this.offer = offer;
        this.sku = sku;
    }

    public String publisher() {
        return this.publisher;
    }

    public String offer() {
        return this.offer;
    }

    public String sku() {
        return this.sku;
    }

    public ImageReference imageReference() {
        return new ImageReference()
            .setPublisher(publisher())
            .setOffer(offer())
            .setSku(sku())
            .setVersion("latest");
    }
}
