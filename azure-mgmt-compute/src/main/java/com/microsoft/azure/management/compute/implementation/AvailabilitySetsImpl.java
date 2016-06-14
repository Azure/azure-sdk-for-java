/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.Page;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.compute.AvailabilitySet;
import com.microsoft.azure.management.compute.AvailabilitySets;
import com.microsoft.azure.management.compute.implementation.api.AvailabilitySetInner;
import com.microsoft.azure.management.compute.implementation.api.AvailabilitySetsInner;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupPagedList;
import com.microsoft.azure.management.resources.fluentcore.utils.PagedListConverter;
import com.microsoft.azure.management.resources.implementation.ResourceManager;
import com.microsoft.azure.management.resources.implementation.api.PageImpl;
import com.microsoft.rest.RestException;
import com.microsoft.rest.ServiceResponse;

import java.io.IOException;
import java.util.List;

/**
 * The implementation for {@link AvailabilitySets}.
 */
class AvailabilitySetsImpl implements AvailabilitySets {
    private final AvailabilitySetsInner client;
    private final ResourceManager resourceManager;
    private final PagedListConverter<AvailabilitySetInner, AvailabilitySet> converter;
    AvailabilitySetsImpl(
            final AvailabilitySetsInner client,
            final ResourceManager resourceManager) {
        this.client = client;
        this.resourceManager = resourceManager;
        this.converter = new PagedListConverter<AvailabilitySetInner, AvailabilitySet>() {
            @Override
            public AvailabilitySet typeConvert(AvailabilitySetInner inner) {
                return createFluentModel(inner);
            }
        };
    }

    @Override
    public PagedList<AvailabilitySet> list() throws CloudException, IOException {
        return new GroupPagedList<AvailabilitySet>(this.resourceManager.resourceGroups().list()) {
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
    public PagedList<AvailabilitySet> listByGroup(String groupName) throws CloudException, IOException {
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
    public AvailabilitySetImpl getByGroup(String groupName, String name) throws CloudException, IOException {
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

    /**************************************************************
     * Fluent model helpers.
     **************************************************************/

    private AvailabilitySetImpl createFluentModel(String name) {
        AvailabilitySetInner availabilitySetInner = new AvailabilitySetInner();
        return new AvailabilitySetImpl(name,
                availabilitySetInner,
                this.client,
                this.resourceManager);
    }

    private AvailabilitySetImpl createFluentModel(AvailabilitySetInner availabilitySetInner) {
        return new AvailabilitySetImpl(availabilitySetInner.name(),
                availabilitySetInner,
                this.client,
                this.resourceManager);
    }
}
