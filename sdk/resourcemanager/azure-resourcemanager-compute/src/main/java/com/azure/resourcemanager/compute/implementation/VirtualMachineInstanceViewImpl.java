// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.compute.implementation;

import com.azure.resourcemanager.compute.models.BootDiagnosticsInstanceView;
import com.azure.resourcemanager.compute.models.DiskInstanceView;
import com.azure.resourcemanager.compute.models.InstanceViewStatus;
import com.azure.resourcemanager.compute.models.MaintenanceRedeployStatus;
import com.azure.resourcemanager.compute.models.VirtualMachineAgentInstanceView;
import com.azure.resourcemanager.compute.models.VirtualMachineExtensionInstanceView;
import com.azure.resourcemanager.compute.models.VirtualMachineInstanceView;
import com.azure.resourcemanager.compute.fluent.inner.VirtualMachineInstanceViewInner;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.WrapperImpl;
import java.util.List;

/** The implementation of ComputeUsage. */
class VirtualMachineInstanceViewImpl extends WrapperImpl<VirtualMachineInstanceViewInner>
    implements VirtualMachineInstanceView {
    VirtualMachineInstanceViewImpl(VirtualMachineInstanceViewInner innerObject) {
        super(innerObject);
    }

    /**
     * Get specifies the update domain of the virtual machine.
     *
     * @return the platformUpdateDomain value
     */
    @Override
    public int platformUpdateDomain() {
        return inner().platformUpdateDomain() == null ? 0 : inner().platformUpdateDomain();
    }

    /**
     * Get specifies the fault domain of the virtual machine.
     *
     * @return the platformFaultDomain value
     */
    @Override
    public int platformFaultDomain() {
        return inner().platformFaultDomain() == null ? 0 : inner().platformFaultDomain();
    }

    /**
     * Get the computer name assigned to the virtual machine.
     *
     * @return the computerName value
     */
    @Override
    public String computerName() {
        return inner().computerName();
    }

    /**
     * Get the Operating System running on the virtual machine.
     *
     * @return the osName value
     */
    @Override
    public String osName() {
        return inner().osName();
    }

    /**
     * Get the version of Operating System running on the virtual machine.
     *
     * @return the osVersion value
     */
    @Override
    public String osVersion() {
        return inner().osVersion();
    }

    /**
     * Get the Remote desktop certificate thumbprint.
     *
     * @return the rdpThumbPrint value
     */
    @Override
    public String rdpThumbPrint() {
        return inner().rdpThumbPrint();
    }

    /**
     * Get the VM Agent running on the virtual machine.
     *
     * @return the vmAgent value
     */
    @Override
    public VirtualMachineAgentInstanceView vmAgent() {
        return inner().vmAgent();
    }

    /**
     * Get the Maintenance Operation status on the virtual machine.
     *
     * @return the maintenanceRedeployStatus value
     */
    @Override
    public MaintenanceRedeployStatus maintenanceRedeployStatus() {
        return inner().maintenanceRedeployStatus();
    }

    /**
     * Get the virtual machine disk information.
     *
     * @return the disks value
     */
    @Override
    public List<DiskInstanceView> disks() {
        return inner().disks();
    }

    /**
     * Get the extensions information.
     *
     * @return the extensions value
     */
    @Override
    public List<VirtualMachineExtensionInstanceView> extensions() {
        return inner().extensions();
    }

    /**
     * Get boot Diagnostics is a debugging feature which allows you to view Console Output and Screenshot to diagnose VM
     * status. &lt;br&gt;&lt;br&gt; You can easily view the output of your console log. &lt;br&gt;&lt;br&gt; Azure also
     * enables you to see a screenshot of the VM from the hypervisor.
     *
     * @return the bootDiagnostics value
     */
    @Override
    public BootDiagnosticsInstanceView bootDiagnostics() {
        return inner().bootDiagnostics();
    }

    /**
     * Get the resource status information.
     *
     * @return the statuses value
     */
    @Override
    public List<InstanceViewStatus> statuses() {
        return inner().statuses();
    }
}
