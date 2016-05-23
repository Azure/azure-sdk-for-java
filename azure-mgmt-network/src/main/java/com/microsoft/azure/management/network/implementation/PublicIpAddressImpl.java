/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.management.network.PublicIpAddress;
import com.microsoft.azure.management.network.implementation.api.IPAllocationMethod;
import com.microsoft.azure.management.network.implementation.api.PublicIPAddressInner;
import com.microsoft.azure.management.network.implementation.api.PublicIPAddressesInner;
import com.microsoft.azure.management.resources.ResourceGroups;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.microsoft.rest.ServiceResponse;

class PublicIpAddressImpl
	extends GroupableResourceImpl<PublicIpAddress, PublicIPAddressInner, PublicIpAddressImpl>
	implements
        PublicIpAddress,
        PublicIpAddress.Definitions,
        PublicIpAddress.Update {

    private final PublicIPAddressesInner client;

    PublicIpAddressImpl(String name,
    		PublicIPAddressInner innerModel,
    		final PublicIPAddressesInner client,
    		final ResourceGroups resourceGroups) {
        super(name, innerModel, resourceGroups);
        this.client = client;
    }

    /**************************************************
     * Verbs
     **************************************************/
    
    @Override
    public PublicIpAddressImpl apply() throws Exception {
        return this.create();
    }

    @Override
    public PublicIpAddressImpl update() throws Exception {
        return this;
    }

    @Override
    public PublicIpAddress refresh() throws Exception {
        ServiceResponse<PublicIPAddressInner> response =
            this.client.get(this.resourceGroupName(), this.name());
        PublicIPAddressInner inner = response.getBody();
        this.setInner(inner);
        return this;
    }

    @Override
    public PublicIpAddressImpl create() throws Exception {
    	super.create();

        ServiceResponse<PublicIPAddressInner> response =
                this.client.createOrUpdate(this.resourceGroupName(), this.key(), this.inner());
        this.setInner(response.getBody());
        return this;
    }

    /*****************************************
     * Setters (fluent)
     *****************************************/

	@Override
	public PublicIpAddressImpl withIdleTimeoutInMinutes(int minutes) {
		this.inner().setIdleTimeoutInMinutes(minutes);
		return this;
	}

	@Override
	public PublicIpAddressImpl withStaticIp() {
		this.inner().setPublicIPAllocationMethod(IPAllocationMethod.STATIC);
		return this;
	}


	@Override
	public PublicIpAddressImpl withDynamicIp() {
		this.inner().setPublicIPAllocationMethod(IPAllocationMethod.DYNAMIC);
		return this;
	}
	
	@Override
	public PublicIpAddressImpl withLeafDomainLabel(String dnsName) {
		this.inner().dnsSettings().setDomainNameLabel(dnsName.toLowerCase());
		return this;
	}
	
	@Override
	public PublicIpAddressImpl withoutLeafDomainLabel() {
		return this.withLeafDomainLabel(null);
	}

	@Override
	public PublicIpAddressImpl withReverseFqdn(String reverseFqdn) {
		this.inner().dnsSettings().setReverseFqdn(reverseFqdn.toLowerCase());
		return this;
	}

	@Override
	public PublicIpAddressImpl withoutReverseFqdn() {
		return this.withReverseFqdn(null);
	}
	
		
	/**********************************************
	 * Getters
	 **********************************************/
	@Override
	public int idleTimeoutInMinutes() {
		return this.inner().idleTimeoutInMinutes();
	}

	@Override
	public String ipAllocationMethod() { 	// TODO: This should really return an enum
		return this.inner().publicIPAllocationMethod();
	}

	@Override
	public String fqdn() {
		return this.inner().dnsSettings().fqdn();
	}

	@Override
	public String reverseFqdn() {
		return this.inner().dnsSettings().reverseFqdn();
	}

	@Override
	public String ipAddress() {
		return this.inner().ipAddress();
	}
	
	@Override
	public String leafDomainLabel() {
		if(this.inner().dnsSettings() == null) {
			return null;
		} else {
			return this.inner().dnsSettings().domainNameLabel();
		}
	}
}