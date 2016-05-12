package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.SubResource;
import com.microsoft.azure.management.compute.AvailabilitySet;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.compute.VirtualMachines;
import com.microsoft.azure.management.compute.implementation.api.AvailabilitySetInner;
import com.microsoft.azure.management.compute.implementation.api.AvailabilitySetsInner;
import com.microsoft.azure.management.compute.implementation.api.InstanceViewStatus;
import com.microsoft.azure.management.resources.ResourceGroups;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceLazyList;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.microsoft.azure.management.resources.fluentcore.model.Provisionable;
import com.microsoft.rest.ServiceResponse;

import java.util.ArrayList;
import java.util.List;

class AvailabilitySetImpl
        extends GroupableResourceImpl<AvailabilitySet, AvailabilitySetInner, AvailabilitySetImpl>
        implements
        AvailabilitySet,
        AvailabilitySet.DefinitionBlank,
        AvailabilitySet.DefinitionWithGroup,
        AvailabilitySet.DefinitionProvisionable {
    private String name;
    private List<String> idOfVMsInSet;
    private List<VirtualMachine> vmsInSet;

    // Fluent collections this model depends on.
    private final VirtualMachines virtualMachines;

    // The client to make AvailabilitySet Management API calls
    private final AvailabilitySetsInner client;

    AvailabilitySetImpl(String name, AvailabilitySetInner innerModel,
                               final AvailabilitySetsInner client,
                               final ResourceGroups resourceGroups,
                               final VirtualMachines virtualMachines) {
        super(innerModel.id(), innerModel, resourceGroups);
        this.name = name;
        this.client = client;
        this.virtualMachines = virtualMachines;
    }

    @Override
    public Integer updateDomainCount() {
        return this.inner().platformUpdateDomainCount();
    }

    @Override
    public Integer FaultDomainCount() {
        return this.inner().platformFaultDomainCount();
    }

    @Override
    public List<String> virtualMachineIds() {
        if (idOfVMsInSet == null) {
            idOfVMsInSet = new ArrayList<>();
            for (SubResource resource : this.inner().virtualMachines()) {
                idOfVMsInSet.add(resource.id());
            }
        }

        return idOfVMsInSet;
    }

    @Override
    public List<VirtualMachine> virtualMachines() throws Exception {
        if (vmsInSet == null) {
            vmsInSet = new ResourceLazyList<>(virtualMachineIds(), new ResourceLazyList.Loader<VirtualMachine>() {
                @Override
                public VirtualMachine load(String resourceGroupName, String resourceName) throws Exception {
                    return virtualMachines.get(resourceGroupName, resourceName);
                }
            });
        }
        return vmsInSet;
    }

    @Override
    public List<InstanceViewStatus> statuses() {
        return this.inner().statuses();
    }

    @Override
    public String name() {
        // TODO: This method should be removed once once Runtime::Resource::setName is available.
        return this.name;
    }

    @Override
    public AvailabilitySet refresh() throws Exception {
        ServiceResponse<AvailabilitySetInner> response = client.get(this.resourceGroupName(), this.name());
        this.setInner(response.getBody());
        this.idOfVMsInSet = null;
        this.vmsInSet = null;
        return this;
    }

    @Override
    public AvailabilitySetImpl withUpdateDomainCount(Integer updateDomainCount) {
        this.inner().setPlatformUpdateDomainCount(updateDomainCount);
        return this;
    }

    @Override
    public AvailabilitySetImpl withFaultDomainCount(Integer faultDomainCount) {
        this.inner().setPlatformFaultDomainCount(faultDomainCount);
        return this;
    }

    @Override
    public AvailabilitySetImpl provision() throws Exception {
        for (Provisionable<?> provisionable : prerequisites()) {
            provisionable.provision();
        }
        ServiceResponse<AvailabilitySetInner> response = this.client.createOrUpdate(this.resourceGroupName(), this.name(), this.inner());
        AvailabilitySetInner availabilitySetInner = response.getBody();
        this.setInner(availabilitySetInner);
        this.idOfVMsInSet = null;
        this.vmsInSet = null;
        return this;
    }
}
