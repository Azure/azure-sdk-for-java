/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.Page;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.network.PublicIpAddress;
import com.microsoft.azure.management.network.PublicIpAddresses;
import com.microsoft.azure.management.network.implementation.api.PublicIPAddressDnsSettings;
import com.microsoft.azure.management.network.implementation.api.PublicIPAddressInner;
import com.microsoft.azure.management.network.implementation.api.PublicIPAddressesInner;
import com.microsoft.azure.management.resources.ResourceGroups;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.microsoft.azure.management.resources.fluentcore.utils.PagedListConverter;
import com.microsoft.azure.management.resources.implementation.api.PageImpl;
import com.microsoft.rest.RestException;
import com.microsoft.rest.ServiceResponse;

import java.io.IOException;
import java.util.List;

class PublicIpAddressesImpl
        implements PublicIpAddresses {
    private final PublicIPAddressesInner client;
    private final ResourceGroups resourceGroups;
    private final PagedListConverter<PublicIPAddressInner, PublicIpAddress> converter;

    PublicIpAddressesImpl(final PublicIPAddressesInner client, final ResourceGroups resourceGroups) {
        this.client = client;
        this.resourceGroups = resourceGroups;
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
        return converter.convert(toPagedList(response.getBody()));
    }

    @Override
    public PagedList<PublicIpAddress> list(String groupName) throws CloudException, IOException {
        ServiceResponse<PagedList<PublicIPAddressInner>> response = client.list(groupName);
        return converter.convert(toPagedList(response.getBody()));
    }

    @Override
    public PublicIpAddressImpl get(String id) throws CloudException, IOException {
    	PublicIPAddressInner inner = client.get(
    			ResourceUtils.groupFromResourceId(id), 
    			ResourceUtils.nameFromResourceId(id)).getBody();
        return createFluentModel(inner);
    }

    @Override
    public PublicIpAddressImpl get(String groupName, String name) throws CloudException, IOException {
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

    private PagedList<PublicIPAddressInner> toPagedList(List<PublicIPAddressInner> list) {
        PageImpl<PublicIPAddressInner> page = new PageImpl<>();
        page.setItems(list);
        page.setNextPageLink(null);
        return new PagedList<PublicIPAddressInner>(page) {
            @Override
            public Page<PublicIPAddressInner> nextPage(String nextPageLink) throws RestException, IOException {
                return null;
            }
        };
    }

    /** Fluent model create helpers **/

    private PublicIpAddressImpl createFluentModel(String name) {
        PublicIPAddressInner inner = new PublicIPAddressInner();

        if(null == inner.dnsSettings()) {
            inner.setDnsSettings(new PublicIPAddressDnsSettings());
        }
        
        return new PublicIpAddressImpl(name, inner, this.client, this.resourceGroups);
    }

    
    private PublicIpAddressImpl createFluentModel(PublicIPAddressInner inner) {
        return new PublicIpAddressImpl(inner.name(), inner, this.client, this.resourceGroups);
    }
}
