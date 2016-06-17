/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.network.PublicIpAddress;
import com.microsoft.azure.management.network.PublicIpAddresses;
import com.microsoft.azure.management.network.implementation.api.PublicIPAddressDnsSettings;
import com.microsoft.azure.management.network.implementation.api.PublicIPAddressInner;
import com.microsoft.azure.management.network.implementation.api.PublicIPAddressesInner;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.GroupableResourcesImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.PagedListConverter;
import com.microsoft.azure.management.resources.implementation.ResourceManager;
import com.microsoft.rest.ServiceResponse;

import java.io.IOException;

/**
 * Implementation of the PublicIpAddresses interface.
 * (Internal use only)
 */
class PublicIpAddressesImpl
        extends GroupableResourcesImpl<PublicIpAddress, PublicIpAddressImpl, PublicIPAddressInner, PublicIPAddressesInner>
        implements PublicIpAddresses {
    private final PagedListConverter<PublicIPAddressInner, PublicIpAddress> converter;

    PublicIpAddressesImpl(final PublicIPAddressesInner client, final ResourceManager resourceManager) {
        super(resourceManager, client);
        this.converter = new PagedListConverter<PublicIPAddressInner, PublicIpAddress>() {
            @Override
            public PublicIpAddress typeConvert(PublicIPAddressInner inner) {
                return createFluentModel(inner);
            }
        };
    }

    @Override
    public PagedList<PublicIpAddress> list() throws CloudException, IOException {
        ServiceResponse<PagedList<PublicIPAddressInner>> response = this.innerCollection.listAll();
        return converter.convert(response.getBody());
    }

    @Override
    public PagedList<PublicIpAddress> listByGroup(String groupName) throws CloudException, IOException {
        ServiceResponse<PagedList<PublicIPAddressInner>> response = this.innerCollection.list(groupName);
        return converter.convert(response.getBody());
    }

    @Override
    public PublicIpAddressImpl getByGroup(String groupName, String name) throws CloudException, IOException {
        ServiceResponse<PublicIPAddressInner> serviceResponse = this.innerCollection.get(groupName, name);
        return createFluentModel(serviceResponse.getBody());
    }

    @Override
    public void delete(String id) throws Exception {
        this.delete(ResourceUtils.groupFromResourceId(id), ResourceUtils.nameFromResourceId(id));
    }

    @Override
    public void delete(String groupName, String name) throws Exception {
        this.innerCollection.delete(groupName, name);
    }

    @Override
    public PublicIpAddressImpl define(String name) {
        return createFluentModel(name);
    }

    // Fluent model create helpers

    @Override
    protected PublicIpAddressImpl createFluentModel(String name) {
        PublicIPAddressInner inner = new PublicIPAddressInner();

        if (null == inner.dnsSettings()) {
            inner.withDnsSettings(new PublicIPAddressDnsSettings());
        }

        return new PublicIpAddressImpl(name, inner, this.innerCollection, this.resourceManager);
    }

    @Override
    protected PublicIpAddressImpl createFluentModel(PublicIPAddressInner inner) {
        return new PublicIpAddressImpl(inner.id(), inner, this.innerCollection, this.resourceManager);
    }
}
