package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.compute.PowerState;
import com.microsoft.azure.management.compute.VMInstanceView;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.compute.implementation.api.DiskInstanceView;
import com.microsoft.azure.management.compute.implementation.api.VirtualMachineExtensionInstanceView;
import com.microsoft.azure.management.compute.implementation.api.BootDiagnosticsInstanceView;
import com.microsoft.azure.management.compute.implementation.api.InstanceViewStatus;
import com.microsoft.azure.management.compute.implementation.api.VirtualMachineInstanceView;
import com.microsoft.azure.management.compute.implementation.api.VirtualMachinesInner;
import com.microsoft.azure.management.compute.implementation.api.VirtualMachineAgentInstanceView;
import com.microsoft.azure.management.compute.implementation.api.VirtualMachineInner;
import com.microsoft.azure.management.compute.implementation.api.InstanceViewTypes;
import com.microsoft.azure.management.resources.fluentcore.utils.Utils;
import com.microsoft.rest.ServiceResponse;


import java.io.IOException;
import java.util.List;

/**
 * The type representing Azure virtual machine instance view.
 */
class VMInstanceViewImpl implements VMInstanceView {
    private final VirtualMachinesInner client;
    private final VirtualMachine parent;
    private VirtualMachineInstanceView inner;

    VMInstanceViewImpl(VirtualMachinesInner client, VirtualMachine parent) {
        this.client = client;
        this.parent = parent;
    }

    @Override
    public PowerState powerState() {
        String powerStateCode = getStatusCode("PowerState");
        if (powerStateCode != null) {
            return PowerState.fromValue(powerStateCode);
        }
        return null;
    }

    @Override
    public String osStateStatusCode() {
        return getStatusCode("OSState");
    }

    @Override
    public int platformUpdateDomain() {
        return Utils.toPrimitiveInt(this.inner().platformUpdateDomain());
    }

    @Override
    public int platformFaultDomain() {
        return Utils.toPrimitiveInt(this.inner().platformFaultDomain());
    }

    @Override
    public String rdpThumbPrint() {
        return this.inner().rdpThumbPrint();
    }

    @Override
    public VirtualMachineAgentInstanceView vmAgent() {
        return this.inner().vmAgent();
    }

    @Override
    public List<DiskInstanceView> disks() {
        return this.inner().disks();
    }

    @Override
    public List<VirtualMachineExtensionInstanceView> extensions() {
        return this.inner().extensions();
    }

    @Override
    public BootDiagnosticsInstanceView bootDiagnostics() {
        return this.inner().bootDiagnostics();
    }

    @Override
    public List<InstanceViewStatus> statuses() {
        return this.inner().statuses();
    }

    @Override
    public VirtualMachineInstanceView inner() {
        if (this.inner == null) {
            try {
                this.refresh();
            } catch (CloudException cloudException) {
                throw new RuntimeException(cloudException);
            } catch (IOException ioException) {
                throw new RuntimeException(ioException);
            }
        }

        return this.inner;
    }

    @Override
    public void refresh() throws CloudException, IOException  {
        ServiceResponse<VirtualMachineInner> response = this.client.get(this.parent.resourceGroupName(),
                this.parent.name(),
                InstanceViewTypes.INSTANCE_VIEW);
        this.inner = response.getBody().instanceView();
    }

    private String getStatusCode(String codePrefix) {
        if (this.statuses() != null) {
            for (InstanceViewStatus status : this.statuses()) {
                if (status.code() != null && status.code().startsWith(codePrefix)) {
                    return status.code();
                }
            }
        }
        return null;
    }
}
