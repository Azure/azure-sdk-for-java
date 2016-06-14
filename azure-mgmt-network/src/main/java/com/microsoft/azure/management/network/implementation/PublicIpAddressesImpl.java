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
import com.microsoft.azure.management.resources.fluentcore.utils.PagedListConverter;
import com.microsoft.azure.management.resources.implementation.ResourceManager;
import com.microsoft.rest.ServiceResponse;

import java.io.IOException;

/**
 * Implementation of the PublicIpAddresses interface.
 * (Internal use only)
 */
class PublicIpAddressesImpl
        implements PublicIpAddresses {
    private final PublicIPAddressesInner client;
    private final ResourceManager resourceManager;
    private final PagedListConverter<PublicIPAddressInner, PublicIpAddress> converter;

    PublicIpAddressesImpl(final PublicIPAddressesInner client, final ResourceManager resourceManager) {
        this.client = client;
        this.resourceManager = resourceManager;
        this.converter = new PagedListConverter<PublicIPAddressInner, PublicIpAddress>() {
            @Override
            public PublicIpAddress typeConvert(PublicIPAddressInner inner) {
                return createFluentModel(inner);
            }
        };
    }

    @Override
    public PagedList<PublicIpAddress> list() throws CloudException, IOException {
        ServiceResponse<PagedList<PublicIPAddressInner>> response = client.listAll();
        return converter.convert(response.getBody());
    }

    @Override
    public PagedList<PublicIpAddress> listByGroup(String groupName) throws CloudException, IOException {
        ServiceResponse<PagedList<PublicIPAddressInner>> response = client.list(groupName);
        return converter.convert(response.getBody());
    }

    @Override
    public PublicIpAddressImpl getByGroup(String groupName, String name) throws CloudException, IOException {
        ServiceResponse<PublicIPAddressInner> serviceResponse = this.client.get(groupName, name);
        return createFluentModel(serviceResponse.getBody());
    }

    @Override
    public void delete(String id) throws Exception {
        this.delete(ResourceUtils.groupFromResourceId(id), ResourceUtils.nameFromResourceId(id));
    }

    @Override
    public void delete(String groupName, String name) throws Exception {
        this.client.delete(groupName, name);
    }

    @Override
    public PublicIpAddressImpl define(String name) {
        return createFluentModel(name);
    }

    // Fluent model create helpers

    private PublicIpAddressImpl createFluentModel(String name) {
        PublicIPAddressInner inner = new PublicIPAddressInner();

        if (null == inner.dnsSettings()) {
            inner.withDnsSettings(new PublicIPAddressDnsSettings());
        }

        return new PublicIpAddressImpl(name, inner, this.client, this.resourceManager);
    }

    private PublicIpAddressImpl createFluentModel(PublicIPAddressInner inner) {
        return new PublicIpAddressImpl(inner.id(), inner, this.client, this.resourceManager);
    }
}
