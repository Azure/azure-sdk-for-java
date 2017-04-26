/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.compute;

/**
 * The popular Azure Windows images.
 */
public enum KnownWindowsVirtualMachineImage {
    /** Windows Server 2008 R2 SP1. */
    WINDOWS_SERVER_2008_R2_SP1("MicrosoftWindowsServer", "WindowsServer", "2008-R2-SP1"),
    /** Windows Server 2012 Data center. */
    WINDOWS_SERVER_2012_DATACENTER("MicrosoftWindowsServer", "WindowsServer", "2012-Datacenter"),
    /** Windows Server 2012 R2 Data center. */
    WINDOWS_SERVER_2012_R2_DATACENTER("MicrosoftWindowsServer", "WindowsServer", "2012-R2-Datacenter"),
    /** Windows Server 2016 technical preview with container support. */
    WINDOWS_SERVER_2016_TECHNICAL_PREVIEW_WITH_CONTAINERS("MicrosoftWindowsServer", "WindowsServer", "2016-Technical-Preview-with-Containers"),
    /** Windows Server 2016 technical preview. */
    WINDOWS_SERVER_TECHNICAL_PREVIEW("MicrosoftWindowsServer", "WindowsServer", "Windows-Server-Technical-Preview");

    private final String publisher;
    private final String offer;
    private final String sku;

    KnownWindowsVirtualMachineImage(String publisher, String offer, String sku) {
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
