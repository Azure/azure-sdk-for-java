package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.management.compute.implementation.api.ImageReference;

/**
 * The popular Azure images.
 */
public enum KnownVirtualMachineImage {
    /** Linux  images*/

    /** Ubuntu server 14.04 LTS. **/
    UBUNTU_SERVER_14_04_LTS("Canonical", "UbuntuServer", "14.04.4-LTS"),
    /** Ubuntu server 16.04 LTS. **/
    UBUNTU_SERVER_16_04_LTS("Canonical", "UbuntuServer", "16.04.0-LTS"),
    /** Debian 8.  **/
    DEBIAN_8("credativ", "Debian", "8"),
    /** OpenLogic CentOS 7.2. **/
    CENTOS_7_2("OpenLogic", "CentOS", "7.2"),
    /** OpenSUSE 42.1. **/
    OPENSUSE_LEAP_42_1("SUSE", "openSUSE-Leap", "42.1"),
    /** SUSE 12-SP1. **/
    SLES_12_SP1("SUSE", "SLES", "12-SP1"),

    /** WINDOWS images */

    /** Windows Server 2008 R2 SP1. **/
    WINDOWS_SERVER_2008_R2_SP1("MicrosoftWindowsServer", "WindowsServer", "2008-R2-SP1"),
    /** Windows Server 2012 data center. **/
    WINDOWS_SERVER_2012_DATACENTER("MicrosoftWindowsServer", "WindowsServer", "2012-Datacenter"),
    /** Windows Server 2012 R2 data center. **/
    WINDOWS_SERVER_2012_R2_DATACENTER("MicrosoftWindowsServer", "WindowsServer", "2012-R2-Datacenter"),
    /** Windows Server 2016 technical preview with containers. **/
    WINDOWS_SERVER_2016_TECHNICAL_PREVIEW_WITH_CONTAINIERS("MicrosoftWindowsServer", "WindowsServer", "2016-Technical-Preview-with-Containers"),
    /** Windows Server technical preview . **/
    WINDOWS_SERVER_TECHNICAL_PREVIEW("MicrosoftWindowsServer", "WindowsServer", "Windows-Server-Technical-Preview");

    private final String publisher;
    private final String offer;
    private final String sku;

    KnownVirtualMachineImage(String publisher, String offer, String sku) {
        this.publisher = publisher;
        this.offer = offer;
        this.sku = sku;
    }

    /**
     * @return the name of the image publisher
     */
    public String publisher() {
        return this.publisher;
    }

    /**
     * @return the name of the image offer
     */
    public String offer() {
        return this.offer;
    }

    /**
     * @return the name of the image SKU
     */
    public String sku() {
        return this.sku;
    }

    /**
     * @return the image reference
     */
    public ImageReference imageReference() {
        return new ImageReference()
            .withPublisher(publisher())
            .withOffer(offer())
            .withSku(sku())
            .withVersion("latest");
    }
}
