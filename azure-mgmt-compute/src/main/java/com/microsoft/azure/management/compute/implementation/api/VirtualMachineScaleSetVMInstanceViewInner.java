/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute.implementation.api;

import java.util.List;

/**
 * The instance view of a virtual machine scale set VM.
 */
public class VirtualMachineScaleSetVMInstanceViewInner {
    /**
     * Gets or sets the Update Domain count.
     */
    private Integer platformUpdateDomain;

    /**
     * Gets or sets the Fault Domain count.
     */
    private Integer platformFaultDomain;

    /**
     * Gets or sets the Remote desktop certificate thumbprint.
     */
    private String rdpThumbPrint;

    /**
     * Gets or sets the VM Agent running on the virtual machine.
     */
    private VirtualMachineAgentInstanceView vmAgent;

    /**
     * Gets or sets the disks information.
     */
    private List<DiskInstanceView> disks;

    /**
     * Gets or sets the extensions information.
     */
    private List<VirtualMachineExtensionInstanceView> extensions;

    /**
     * Gets or sets the boot diagnostics.
     */
    private BootDiagnosticsInstanceView bootDiagnostics;

    /**
     * Gets or sets the resource status information.
     */
    private List<InstanceViewStatus> statuses;

    /**
     * Get the platformUpdateDomain value.
     *
     * @return the platformUpdateDomain value
     */
    public Integer platformUpdateDomain() {
        return this.platformUpdateDomain;
    }

    /**
     * Set the platformUpdateDomain value.
     *
     * @param platformUpdateDomain the platformUpdateDomain value to set
     * @return the VirtualMachineScaleSetVMInstanceViewInner object itself.
     */
    public VirtualMachineScaleSetVMInstanceViewInner withPlatformUpdateDomain(Integer platformUpdateDomain) {
        this.platformUpdateDomain = platformUpdateDomain;
        return this;
    }

    /**
     * Get the platformFaultDomain value.
     *
     * @return the platformFaultDomain value
     */
    public Integer platformFaultDomain() {
        return this.platformFaultDomain;
    }

    /**
     * Set the platformFaultDomain value.
     *
     * @param platformFaultDomain the platformFaultDomain value to set
     * @return the VirtualMachineScaleSetVMInstanceViewInner object itself.
     */
    public VirtualMachineScaleSetVMInstanceViewInner withPlatformFaultDomain(Integer platformFaultDomain) {
        this.platformFaultDomain = platformFaultDomain;
        return this;
    }

    /**
     * Get the rdpThumbPrint value.
     *
     * @return the rdpThumbPrint value
     */
    public String rdpThumbPrint() {
        return this.rdpThumbPrint;
    }

    /**
     * Set the rdpThumbPrint value.
     *
     * @param rdpThumbPrint the rdpThumbPrint value to set
     * @return the VirtualMachineScaleSetVMInstanceViewInner object itself.
     */
    public VirtualMachineScaleSetVMInstanceViewInner withRdpThumbPrint(String rdpThumbPrint) {
        this.rdpThumbPrint = rdpThumbPrint;
        return this;
    }

    /**
     * Get the vmAgent value.
     *
     * @return the vmAgent value
     */
    public VirtualMachineAgentInstanceView vmAgent() {
        return this.vmAgent;
    }

    /**
     * Set the vmAgent value.
     *
     * @param vmAgent the vmAgent value to set
     * @return the VirtualMachineScaleSetVMInstanceViewInner object itself.
     */
    public VirtualMachineScaleSetVMInstanceViewInner withVmAgent(VirtualMachineAgentInstanceView vmAgent) {
        this.vmAgent = vmAgent;
        return this;
    }

    /**
     * Get the disks value.
     *
     * @return the disks value
     */
    public List<DiskInstanceView> disks() {
        return this.disks;
    }

    /**
     * Set the disks value.
     *
     * @param disks the disks value to set
     * @return the VirtualMachineScaleSetVMInstanceViewInner object itself.
     */
    public VirtualMachineScaleSetVMInstanceViewInner withDisks(List<DiskInstanceView> disks) {
        this.disks = disks;
        return this;
    }

    /**
     * Get the extensions value.
     *
     * @return the extensions value
     */
    public List<VirtualMachineExtensionInstanceView> extensions() {
        return this.extensions;
    }

    /**
     * Set the extensions value.
     *
     * @param extensions the extensions value to set
     * @return the VirtualMachineScaleSetVMInstanceViewInner object itself.
     */
    public VirtualMachineScaleSetVMInstanceViewInner withExtensions(List<VirtualMachineExtensionInstanceView> extensions) {
        this.extensions = extensions;
        return this;
    }

    /**
     * Get the bootDiagnostics value.
     *
     * @return the bootDiagnostics value
     */
    public BootDiagnosticsInstanceView bootDiagnostics() {
        return this.bootDiagnostics;
    }

    /**
     * Set the bootDiagnostics value.
     *
     * @param bootDiagnostics the bootDiagnostics value to set
     * @return the VirtualMachineScaleSetVMInstanceViewInner object itself.
     */
    public VirtualMachineScaleSetVMInstanceViewInner withBootDiagnostics(BootDiagnosticsInstanceView bootDiagnostics) {
        this.bootDiagnostics = bootDiagnostics;
        return this;
    }

    /**
     * Get the statuses value.
     *
     * @return the statuses value
     */
    public List<InstanceViewStatus> statuses() {
        return this.statuses;
    }

    /**
     * Set the statuses value.
     *
     * @param statuses the statuses value to set
     * @return the VirtualMachineScaleSetVMInstanceViewInner object itself.
     */
    public VirtualMachineScaleSetVMInstanceViewInner withStatuses(List<InstanceViewStatus> statuses) {
        this.statuses = statuses;
        return this;
    }

}
