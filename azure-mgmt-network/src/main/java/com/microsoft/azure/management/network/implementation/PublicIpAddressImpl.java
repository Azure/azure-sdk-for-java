/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.management.network.PublicIpAddress;
import com.microsoft.azure.management.network.implementation.api.PublicIPAddressInner;
import com.microsoft.azure.management.network.implementation.api.PublicIPAddressesInner;
import com.microsoft.azure.management.resources.ResourceGroups;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.microsoft.rest.ServiceResponse;

class PublicIpAddressImpl
        extends GroupableResourceImpl<PublicIpAddress, PublicIPAddressInner, PublicIpAddressImpl>
        implements
        	PublicIpAddress,
        	PublicIpAddress.DefinitionBlank,
        	PublicIpAddress.DefinitionWithGroup,
        	PublicIpAddress.DefinitionProvisionable
        {

    private String name;

    private final PublicIPAddressesInner client;

    PublicIpAddressImpl(String name,
                              PublicIPAddressInner innerModel,
                              final PublicIPAddressesInner client,
                              final ResourceGroups resourceGroups) {
        super(innerModel.id(), innerModel, resourceGroups);
        this.name = name;
        this.client = client;
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public PublicIpAddress refresh() throws Exception {
        ServiceResponse<PublicIPAddressInner> response =
            this.client.get(this.group(), this.name());
        PublicIPAddressInner inner = response.getBody();
        this.setInner(inner);
        clearWrapperProperties();
        return this;
    }

    @Override
    public PublicIpAddressImpl provision() throws Exception {
        ensureGroup();

        ServiceResponse<PublicIPAddressInner> response =
                this.client.createOrUpdate(this.group(), this.name(), this.inner());
        this.setInner(response.getBody());
        clearWrapperProperties();
        return this;
    }

    private void clearWrapperProperties() {
    	
    }
}
