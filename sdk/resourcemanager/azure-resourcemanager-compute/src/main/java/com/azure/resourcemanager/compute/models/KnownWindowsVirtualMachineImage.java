// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.compute.models;

/** The popular Azure Windows images. */
public enum KnownWindowsVirtualMachineImage {
    /** Windows 10 2020 H1 Pro */
    WINDOWS_DESKTOP_10_20H1_PRO("MicrosoftWindowsDesktop", "Windows-10", "20h1-pro"),
    /** Windows Server 2019 Data center. */
    WINDOWS_SERVER_2019_DATACENTER("MicrosoftWindowsServer", "WindowsServer", "2019-Datacenter"),
    /** Windows Server 2019 Data center with containers. */
    WINDOWS_SERVER_2019_DATACENTER_WITH_CONTAINERS(
        "MicrosoftWindowsServer", "WindowsServer", "2019-Datacenter-with-Containers"),
    /** Windows Server 2016 Data center. */
    WINDOWS_SERVER_2016_DATACENTER("MicrosoftWindowsServer", "WindowsServer", "2016-Datacenter"),
    /** Windows Server 2012 R2 Data center. */
    WINDOWS_SERVER_2012_R2_DATACENTER("MicrosoftWindowsServer", "WindowsServer", "2012-R2-Datacenter");

    private final String publisher;
    private final String offer;
    private final String sku;

    KnownWindowsVirtualMachineImage(String publisher, String offer, String sku) {
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
