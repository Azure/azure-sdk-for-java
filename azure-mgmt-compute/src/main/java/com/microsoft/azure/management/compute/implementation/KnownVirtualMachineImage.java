package com.microsoft.azure.management.compute.implementation;

public enum KnownVirtualMachineImage {
    /** Linux */
    UBUNTU_14_04_LTS("Canonical", "UbuntuServer", "14.04.4-LTS", "14.04.201604060"),
    UBUNTU_16_04_LTS("Canonical", "UbuntuServer", "16.04.0-LTS", "16.04.201604203"),
    DEBIAN_8("credativ", "Debian", "8", "8.0.201604200"),
    CENTOS_7_2("OpenLogic", "CentOS", "7.2", "7.2.20160308"),
    OPENSUSE_LEAP_42_1("SUSE", "openSUSE-Leap", "42.1", "2016.04.15"),
    SLES_12_SP1("SUSE", "SLES", "12-SP1", "2016.04.14"),
    /** WINDOWS */
    WINDOWS_SERVER_2008_R2_SP1("MicrosoftWindowsServer", "WindowsServer", "2008-R2-SP1", "2.0.20160430"),
    WINDOWS_SERVER_2012_DATACENTER("MicrosoftWindowsServer", "WindowsServer", "2012-Datacenter", "3.0.20160430"),
    WINDOWS_SERVER_2012_R2_DATACENTER("MicrosoftWindowsServer", "WindowsServer", "2012-R2-Datacenter", "4.0.20160430"),
    WINDOWS_SERVER_2016_TECHNICAL_PREVIEW_WITH_CONTAINIERS("MicrosoftWindowsServer", "WindowsServer", "2016-Technical-Preview-with-Containers", "2016.0.20151118"),
    WINDOWS_SERVER_TECHNICAL_PREVIEW("MicrosoftWindowsServer", "WindowsServer", "Windows-Server-Technical-Preview", "5.0.20160420");

    private final String publisher;
    private final String offer;
    private final String sku;
    private final String verion;

    KnownVirtualMachineImage(String publisher, String offer, String sku, String version) {
        this.publisher = publisher;
        this.offer = offer;
        this.sku = sku;
        this.verion = version;
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

    public String version() {
        return this.verion;
    }
}
