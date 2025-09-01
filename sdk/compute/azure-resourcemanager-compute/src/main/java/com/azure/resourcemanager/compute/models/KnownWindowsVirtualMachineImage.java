// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.compute.models;

/** The popular Azure Windows images. */
public enum KnownWindowsVirtualMachineImage {
    /**
     * Windows 10 2020 H1 Pro
     * @deprecated The image for Windows 10 2020 H1 Pro has been removed form Azure image gallery.
     */
    @Deprecated
    WINDOWS_DESKTOP_10_20H1_PRO("MicrosoftWindowsDesktop", "Windows-10", "20h1-pro"),
    /** Windows Server 2019 Data center. */
    WINDOWS_SERVER_2019_DATACENTER("MicrosoftWindowsServer", "WindowsServer", "2019-Datacenter"),
    /**
     * Windows Server 2019 Data center with containers.
     * @deprecated On May 23rd, 2023, Microsoft was contractually obligated to remove all “*-with-Containers-*” VM images from Azure image gallery.
     */
    @Deprecated
    WINDOWS_SERVER_2019_DATACENTER_WITH_CONTAINERS("MicrosoftWindowsServer", "WindowsServer",
        "2019-Datacenter-with-Containers"),
    /** Windows Server 2016 Data center. */
    WINDOWS_SERVER_2016_DATACENTER("MicrosoftWindowsServer", "WindowsServer", "2016-Datacenter"),
    /** Windows Server 2012 R2 Data center. */
    WINDOWS_SERVER_2012_R2_DATACENTER("MicrosoftWindowsServer", "WindowsServer", "2012-R2-Datacenter"),
    /** Windows Server 2019 Data center gen2. */
    WINDOWS_SERVER_2019_DATACENTER_GEN2("MicrosoftWindowsServer", "WindowsServer", "2019-datacenter-gensecond"),
    /**
     * Windows Server 2019 Data center with containers gen2.
     * @deprecated On May 23rd, 2023, Microsoft was contractually obligated to remove all “*-with-Containers-*” VM images from Azure image gallery.
     */
    @Deprecated
    WINDOWS_SERVER_2019_DATACENTER_WITH_CONTAINERS_GEN2("MicrosoftWindowsServer", "WindowsServer",
        "2019-datacenter-with-containers-g2"),
    /** Windows Server 2016 Data center gen2. */
    WINDOWS_SERVER_2016_DATACENTER_GEN2("MicrosoftWindowsServer", "WindowsServer", "2016-datacenter-gensecond"),
    /** Windows 10 2021 H2 Pro gen2. */
    WINDOWS_DESKTOP_10_21H2_PRO_GEN2("MicrosoftWindowsDesktop", "Windows-10", "win10-21h2-pro-g2"),
    /** Windows desktop 10 pro. */
    WINDOWS_DESKTOP_10_PRO("MicrosoftWindowsDesktop", "Windows-10", "win10-22h2-pro");

    private final String publisher;
    private final String offer;
    private final String sku;

    KnownWindowsVirtualMachineImage(String publisher, String offer, String sku) {
        this.publisher = publisher;
        this.offer = offer;
        this.sku = sku;
    }

    /**
     * Gets the name of the image publisher.
     *
     * @return the name of the image publisher
     */
    public String publisher() {
        return this.publisher;
    }

    /**
     * Gets the name of the image offer.
     *
     * @return the name of the image offer
     */
    public String offer() {
        return this.offer;
    }

    /**
     * Gets the name of the image SKU.
     *
     * @return the name of the image SKU
     */
    public String sku() {
        return this.sku;
    }

    /**
     * Gets the image reference.
     *
     * @return the image reference
     */
    public ImageReference imageReference() {
        return new ImageReference().withPublisher(publisher()).withOffer(offer()).withSku(sku()).withVersion("latest");
    }
}
