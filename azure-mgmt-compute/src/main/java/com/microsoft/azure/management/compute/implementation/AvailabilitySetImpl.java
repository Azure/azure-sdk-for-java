/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.SubResource;
import com.microsoft.azure.management.compute.AvailabilitySet;
import com.microsoft.azure.management.compute.implementation.api.AvailabilitySetInner;
import com.microsoft.azure.management.compute.implementation.api.AvailabilitySetsInner;
import com.microsoft.azure.management.compute.implementation.api.InstanceViewStatus;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.Utils;
import com.microsoft.rest.ServiceCall;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.azure.management.resources.implementation.ResourceManager;
import com.microsoft.rest.ServiceResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The implementation for {@link AvailabilitySet} and its create and update interfaces.
 */
class AvailabilitySetImpl
    extends
        GroupableResourceImpl<AvailabilitySet, AvailabilitySetInner, AvailabilitySetImpl>
    implements
        AvailabilitySet,
        AvailabilitySet.Definitions,
        AvailabilitySet.Update {
    private List<String> idOfVMsInSet;
    // The client to make AvailabilitySet Management API calls
    private final AvailabilitySetsInner client;

    AvailabilitySetImpl(String name, AvailabilitySetInner innerModel,
                               final AvailabilitySetsInner client,
                               final ResourceManager resourceManager) {
        super(name, innerModel, resourceManager);
        this.client = client;
    }

    @Override
    public int updateDomainCount() {
        return this.inner().platformUpdateDomainCount();
    }

    @Override
    public int faultDomainCount() {
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

        return Collections.unmodifiableList(idOfVMsInSet);
    }

    @Override
    public List<InstanceViewStatus> statuses() {
        return Collections.unmodifiableList(this.inner().statuses());
    }

    @Override
    public AvailabilitySet refresh() throws Exception {
        ServiceResponse<AvailabilitySetInner> response = client.get(this.resourceGroupName(), this.name());
        this.setInner(response.getBody());
        this.idOfVMsInSet = null;
        return this;
    }

    @Override
    public AvailabilitySetImpl withUpdateDomainCount(int updateDomainCount) {
        this.inner().withPlatformUpdateDomainCount(updateDomainCount);
        return this;
    }

    @Override
    public AvailabilitySetImpl withFaultDomainCount(int faultDomainCount) {
        this.inner().withPlatformFaultDomainCount(faultDomainCount);
        return this;
    }

    @Override
    public AvailabilitySetImpl apply() throws Exception {
        return this.create();
    }

    @Override
    public ServiceCall applyAsync(ServiceCallback<AvailabilitySet> callback) {
        return this.createAsync(callback);
    }

    @Override
    protected void createResource() throws Exception {
        ServiceResponse<AvailabilitySetInner> response = this.client.createOrUpdate(this.resourceGroupName(), this.name(), this.inner());
        AvailabilitySetInner availabilitySetInner = response.getBody();
        this.setInner(availabilitySetInner);
        this.idOfVMsInSet = null;
    }

    @Override
    protected ServiceCall createResourceAsync(final ServiceCallback<Void> callback) {
        return this.client.createOrUpdateAsync(this.resourceGroupName(), this.name(), this.inner(),
                Utils.fromVoidCallback(this, new ServiceCallback<Void>() {
                    @Override
                    public void failure(Throwable t) {
                        callback.failure(t);
                    }

                    @Override
                    public void success(ServiceResponse<Void> result) {
                        idOfVMsInSet = null;
                        callback.success(result);
                    }
                }));
    }
}
