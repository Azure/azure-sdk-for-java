// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.compute.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.compute.fluent.models.VirtualMachineInstanceViewInner;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;
import java.util.List;

/** An immutable client-side representation of an Azure VM Instance View object. */
@Fluent
public interface VirtualMachineInstanceView extends HasInnerModel<VirtualMachineInstanceViewInner> {
    /**
     * Get specifies the update domain of the virtual machine.
     *
     * @return the platformUpdateDomain value
     */
    int platformUpdateDomain();

    /**
     * Get specifies the fault domain of the virtual machine.
     *
     * @return the platformFaultDomain value
     */
    int platformFaultDomain();

    /**
     * Get the computer name assigned to the virtual machine.
     *
     * @return the computerName value
     */
    String computerName();

    /**
     * Get the Operating System running on the virtual machine.
     *
     * @return the osName value
     */
    String osName();

    /**
     * Get the version of Operating System running on the virtual machine.
     *
     * @return the osVersion value
     */
    String osVersion();

    /**
     * Get the Remote desktop certificate thumbprint.
     *
     * @return the rdpThumbPrint value
     */
    String rdpThumbPrint();

    /**
     * Get the VM Agent running on the virtual machine.
     *
     * @return the vmAgent value
     */
    VirtualMachineAgentInstanceView vmAgent();

    /**
     * Get the Maintenance Operation status on the virtual machine.
     *
     * @return the maintenanceRedeployStatus value
     */
    MaintenanceRedeployStatus maintenanceRedeployStatus();

    /**
     * Get the virtual machine disk information.
     *
     * @return the disks value
     */
    List<DiskInstanceView> disks();

    /**
     * Get the extensions information.
     *
     * @return the extensions value
     */
    List<VirtualMachineExtensionInstanceView> extensions();

    /**
     * Get boot Diagnostics is a debugging feature which allows you to view Console Output and Screenshot to diagnose VM
     * status. &lt;br&gt;&lt;br&gt; You can easily view the output of your console log. &lt;br&gt;&lt;br&gt; Azure also
     * enables you to see a screenshot of the VM from the hypervisor.
     *
     * @return the bootDiagnostics value
     */
    BootDiagnosticsInstanceView bootDiagnostics();

    /**
     * Get the resource status information.
     *
     * @return the statuses value
     */
    List<InstanceViewStatus> statuses();
}
