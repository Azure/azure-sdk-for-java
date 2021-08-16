// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.compute.models;

/** The popular Azure Linux images. */
public enum KnownLinuxVirtualMachineImage {
    /** UbuntuServer 16.04LTS. */
    UBUNTU_SERVER_16_04_LTS("Canonical", "UbuntuServer", "16.04.0-LTS"),
    /** UbuntuServer 18.04LTS. */
    UBUNTU_SERVER_18_04_LTS("Canonical", "UbuntuServer", "18.04-LTS"),

    /** Debian 9. */
    DEBIAN_9("credativ", "Debian", "9"),
    /** Debian 10. */
    DEBIAN_10("Debian", "debian-10", "10"),

    /** CentOS 8.1. */
    CENTOS_8_1("OpenLogic", "CentOS", "8_1"),
    /** CentOS 8.3. */
    CENTOS_8_3("OpenLogic", "CentOS", "8_3"),

    /**
     * OpenSUSE-Leap 15.1.
     * @deprecated use OPENSUSE_LEAP_15.
     */
    @Deprecated
    OPENSUSE_LEAP_15_1("SUSE", "openSUSE-Leap", "15-2"),
    /** OpenSUSE-Leap 15. */
    OPENSUSE_LEAP_15("SUSE", "openSUSE-Leap", "15-2"),

    /**
     * SLES 15-SP1-gen1.
     * @deprecated use SLES_15.
     */
    @Deprecated
    SLES_15_SP1("SUSE", "sles-15-sp2", "gen1"),
    /** SLES 15. */
    SLES_15("SUSE", "sles-15-sp2", "gen1"),

    /** RedHat RHEL 8.2. */
    REDHAT_RHEL_8_2("RedHat", "RHEL", "8.2"),

    /** Oracle Linux 8.1. */
    ORACLE_LINUX_8_1("Oracle", "Oracle-Linux", "81");

    private final String publisher;
    private final String offer;
    private final String sku;

    KnownLinuxVirtualMachineImage(String publisher, String offer, String sku) {
        this.publisher = publisher;
        this.offer = offer;
        this.sku = sku;
    }

    /** @return the name of the image publisher */
    public String publisher() {
        return this.publisher;
    }

    /** @return the name of the image offer */
    public String offer() {
        return this.offer;
    }

    /** @return the name of the image SKU */
    public String sku() {
        return this.sku;
    }

    /** @return the image reference */
    public ImageReference imageReference() {
        return new ImageReference().withPublisher(publisher()).withOffer(offer()).withSku(sku()).withVersion("latest");
    }
}
