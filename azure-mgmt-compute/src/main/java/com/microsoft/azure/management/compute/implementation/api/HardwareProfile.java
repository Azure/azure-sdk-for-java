/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute.implementation.api;


/**
 * Describes a hardware profile.
 */
public class HardwareProfile {
    /**
     * The virtual machine size name. Possible values include: 'Basic_A0',
     * 'Basic_A1', 'Basic_A2', 'Basic_A3', 'Basic_A4', 'Standard_A0',
     * 'Standard_A1', 'Standard_A2', 'Standard_A3', 'Standard_A4',
     * 'Standard_A5', 'Standard_A6', 'Standard_A7', 'Standard_A8',
     * 'Standard_A9', 'Standard_A10', 'Standard_A11', 'Standard_D1',
     * 'Standard_D2', 'Standard_D3', 'Standard_D4', 'Standard_D11',
     * 'Standard_D12', 'Standard_D13', 'Standard_D14', 'Standard_D1_v2',
     * 'Standard_D2_v2', 'Standard_D3_v2', 'Standard_D4_v2',
     * 'Standard_D5_v2', 'Standard_D11_v2', 'Standard_D12_v2',
     * 'Standard_D13_v2', 'Standard_D14_v2', 'Standard_DS1', 'Standard_DS2',
     * 'Standard_DS3', 'Standard_DS4', 'Standard_DS11', 'Standard_DS12',
     * 'Standard_DS13', 'Standard_DS14', 'Standard_G1', 'Standard_G2',
     * 'Standard_G3', 'Standard_G4', 'Standard_G5', 'Standard_GS1',
     * 'Standard_GS2', 'Standard_GS3', 'Standard_GS4', 'Standard_GS5'.
     */
    private String vmSize;

    /**
     * Get the vmSize value.
     *
     * @return the vmSize value
     */
    public String vmSize() {
        return this.vmSize;
    }

    /**
     * Set the vmSize value.
     *
     * @param vmSize the vmSize value to set
     * @return the HardwareProfile object itself.
     */
    public HardwareProfile withVmSize(String vmSize) {
        this.vmSize = vmSize;
        return this;
    }

}
