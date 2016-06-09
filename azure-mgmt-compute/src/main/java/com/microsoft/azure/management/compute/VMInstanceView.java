package com.microsoft.azure.management.compute;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.compute.implementation.api.VirtualMachineAgentInstanceView;
import com.microsoft.azure.management.compute.implementation.api.DiskInstanceView;
import com.microsoft.azure.management.compute.implementation.api.VirtualMachineExtensionInstanceView;
import com.microsoft.azure.management.compute.implementation.api.BootDiagnosticsInstanceView;
import com.microsoft.azure.management.compute.implementation.api.InstanceViewStatus;
import com.microsoft.azure.management.compute.implementation.api.VirtualMachineInstanceView;

import java.io.IOException;
import java.util.List;

/**
 * An client-side representation of an Azure virtual machine instance view.
 */
public interface VMInstanceView {
    /**
     * Gets the power state of the virtual machine.
     * <p>
     * {@link PowerState#UNKNOWN} value will be returned if the
     *
     * @return {@link PowerState}
     */
    PowerState powerState();

    /**
     * @return the operating system state of the virtual machine
     */
    String osStateStatusCode();

    /**
     * @return the Update Domain count
     */
    int platformUpdateDomain();

    /**
     * @return the Fault Domain count
     */
    int platformFaultDomain();

    /**
     * @return the Remote desktop certificate thumbprint
     */
    String rdpThumbPrint();

    /**
     * @return the instance view of VM Agent running on the virtual machine
     */
    VirtualMachineAgentInstanceView vmAgent();

    /**
     * @return the instance view of disks
     */
    List<DiskInstanceView> disks();

    /**
     * @return the instance view of extensions on the virtual machine
     */
    List<VirtualMachineExtensionInstanceView> extensions();

    /**
     * @return the boot diagnostics of the virtual machine
     */
    BootDiagnosticsInstanceView bootDiagnostics();

    /**
     * Gets or sets the resource status information.
     */
    List<InstanceViewStatus> statuses();

    /**
     * @return the inner object
     */
    VirtualMachineInstanceView inner();

    /**
     * updates the state of this object to latest.
     * @throws CloudException exceptions thrown from the cloud.
     * @throws IOException exceptions thrown from serialization/deserialization.
     */
    void refresh() throws CloudException, IOException;
}
