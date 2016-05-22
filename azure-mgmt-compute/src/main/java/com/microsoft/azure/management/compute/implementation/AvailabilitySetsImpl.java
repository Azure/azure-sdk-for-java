package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.Page;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.compute.AvailabilitySet;
import com.microsoft.azure.management.compute.AvailabilitySets;
import com.microsoft.azure.management.compute.VirtualMachines;
import com.microsoft.azure.management.compute.implementation.api.AvailabilitySetInner;
import com.microsoft.azure.management.compute.implementation.api.AvailabilitySetsInner;
import com.microsoft.azure.management.resources.ResourceGroups;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupPagedList;
import com.microsoft.azure.management.resources.fluentcore.utils.PagedListConverter;
import com.microsoft.azure.management.resources.implementation.api.PageImpl;
import com.microsoft.rest.RestException;
import com.microsoft.rest.ServiceResponse;

import java.io.IOException;
import java.util.List;

public class AvailabilitySetsImpl implements AvailabilitySets {
    private final AvailabilitySetsInner client;
    private final ResourceGroups resourceGroups;
    private final VirtualMachines virtualMachines;
    private final PagedListConverter<AvailabilitySetInner, AvailabilitySet> converter;

    public AvailabilitySetsImpl(final AvailabilitySetsInner client,
                                final ResourceGroups resourceGroups,
                                final VirtualMachines virtualMachines) {
        this.client = client;
        this.resourceGroups = resourceGroups;
        this.virtualMachines = virtualMachines;
        this.converter = new PagedListConverter<AvailabilitySetInner, AvailabilitySet>() {
            @Override
            public AvailabilitySet typeConvert(AvailabilitySetInner inner) {
                return createFluentModel(inner);
            }
        };
    }

    @Override
    public PagedList<AvailabilitySet> list() throws CloudException, IOException {
        return new GroupPagedList<AvailabilitySet>(resourceGroups.list()) {
            @Override
            public List<AvailabilitySet> listNextGroup(String resourceGroupName) throws RestException, IOException {
                PageImpl<AvailabilitySetInner> page = new PageImpl<>();
                page.setItems(client.list(resourceGroupName).getBody());
                page.setNextPageLink(null);
                return converter.convert(new PagedList<AvailabilitySetInner>(page) {
                    @Override
                    public Page<AvailabilitySetInner> nextPage(String nextPageLink) throws RestException, IOException {
                        return null;
                    }
                });
            }
        };
    }

    @Override
    public PagedList<AvailabilitySet> list(String groupName) throws CloudException, IOException {
        PageImpl<AvailabilitySetInner> page = new PageImpl<>();
        page.setItems(client.list(groupName).getBody());
        page.setNextPageLink(null);
        return this.converter.convert(new PagedList<AvailabilitySetInner>(page) {
            @Override
            public Page<AvailabilitySetInner> nextPage(String nextPageLink) throws RestException, IOException {
                return null;
            }
        });
    }

    @Override
    public AvailabilitySetImpl get(String groupName, String name) throws CloudException, IOException {
        ServiceResponse<AvailabilitySetInner> response = this.client.get(groupName, name);
        return createFluentModel(response.getBody());
    }

    @Override
    public AvailabilitySetImpl define(String name) {
        return createFluentModel(name);
    }

    @Override
    public void delete(String id) throws Exception {
        this.delete(ResourceUtils.groupFromResourceId(id), ResourceUtils.nameFromResourceId(id));
    }

    @Override
    public void delete(String groupName, String name) throws Exception {
        this.client.delete(groupName, name);
    }

    /** Fluent model create helpers **/

    private AvailabilitySetImpl createFluentModel(String name) {
        AvailabilitySetInner availabilitySetInner = new AvailabilitySetInner();
        return new AvailabilitySetImpl(name,
                availabilitySetInner,
                this.client,
                this.resourceGroups,
                this.virtualMachines);
    }

    private AvailabilitySetImpl createFluentModel(AvailabilitySetInner availabilitySetInner) {
        return new AvailabilitySetImpl(availabilitySetInner.name(),
                availabilitySetInner,
                this.client,
                this.resourceGroups,
                this.virtualMachines);
    }
}
