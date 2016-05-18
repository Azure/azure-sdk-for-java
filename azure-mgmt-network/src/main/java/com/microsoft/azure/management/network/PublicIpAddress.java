/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network;

import com.microsoft.azure.management.network.implementation.api.PublicIPAddressInner;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Updatable;
import com.microsoft.azure.management.resources.fluentcore.model.Appliable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;

public interface PublicIpAddress extends
        GroupableResource,
        Refreshable<PublicIpAddress>,
        Wrapper<PublicIPAddressInner>,
        Updatable<PublicIpAddress.Update> {

    /***********************************************************
     * Getters
     ***********************************************************/
	String ipAddress();
	String leafDomainLabel();
	String fqdn();
	String reverseFqdn();

    /**************************************************************
     * Fluent interfaces for provisioning
     **************************************************************/
	interface Definitions extends 
		DefinitionBlank,
		DefinitionWithGroup,
		DefinitionWithIpAddress,
		DefinitionWithLeafDomainLabel,
		DefinitionCreatable {}
	
	
    interface DefinitionBlank extends GroupableResource.DefinitionWithRegion<DefinitionWithGroup> {
    }

    interface DefinitionWithGroup extends GroupableResource.DefinitionWithGroup<DefinitionCreatable> {
    }

	public interface DefinitionWithIpAddress {
		/**
		 * Enables static IP address allocation. The actual IP address allocated for this resource by Azure can be obtained 
		 * after the provisioning process is complete from ipAddress().
		 * @return The next stage of the public IP address definition
		 */
		DefinitionCreatable withStaticIp();
		
		/**
		 * Enables dynamic IP address allocation.
		 * @return The next stage of the public IP address definition
		 */
		DefinitionCreatable withDynamicIp();
	}
	
	
	public interface UpdatableWithIpAddress<T> {
		/**
		 * Enables static IP address allocation. The actual IP address allocated for this resource by Azure can be obtained 
		 * after the provisioning process is complete from ipAddress().
		 * @return The next stage of the public IP address definition
		 */
		T withStaticIp();
		
		/**
		 * Enables dynamic IP address allocation.
		 * @return The next stage of the public IP address definition
		 */
		T withDynamicIp();		
	}

	
	/**
	 * A public IP address definition allowing to specify the leaf domain label, if any
	 */
	public interface DefinitionWithLeafDomainLabel {
		/**
		 * Specifies the leaf domain label to associate with this public IP address. The fully qualified domain name (FQDN) 
		 * will be constructed automatically by appending the rest of the domain to this label.
		 * @param dnsName The leaf domain label to use. This must follow the required naming convention for leaf domain names.
		 * @return The next stage of the public IP address definition
		 */
		DefinitionCreatable withLeafDomainLabel(String dnsName);
		
		/**
		 * Ensures that no leaf domain label will be used. This means that this public IP address will not be associated with a domain name.
		 * @return The next stage of the public IP address definition
		 */
		DefinitionCreatable withoutLeafDomainLabel();
	}
	
	
	public interface UpdatableWithLeafDomainLabel<T> {
		/**
		 * Specifies the leaf domain label to associate with this public IP address. The fully qualified domain name (FQDN) 
		 * will be constructed automatically by appending the rest of the domain to this label.
		 * @param dnsName The leaf domain label to use. This must follow the required naming convention for leaf domain names.
		 * @return The next stage of the public IP address definition
		 */
		T withLeafDomainLabel(String dnsName);
		
		/**
		 * Ensures that no leaf domain label will be used. This means that this public IP address will not be associated with a domain name.
		 * @return The next stage of the resource definition
		 */
		T withoutLeafDomainLabel();
	}
	
	
	public interface DefinitionWithReverseFQDN<T> {
		/**
		 * Specifies the reverse FQDN to assign to this public IP address
		 * @param reverseFQDN The reverse FQDN to assign 
		 * @return The next stage of the resource definition
		 */
		T withReverseFqdn(String reverseFQDN);
		
		/**
		 * Ensures that no reverse FQDN will be used.
		 * @return The next stage of the resource definition
		 */
		T withoutReverseFqdn();
	}
	
	public interface UpdatableWithReverseFQDN<T> {
		/**
		 * Specifies the reverse FQDN to assign to this public IP address
		 * @param reverseFQDN The reverse FQDN to assign 
		 * @return The next stage of the resource definition
		 */
		T withReverseFqdn(String reverseFQDN);
		
		/**
		 * Ensures that no reverse FQDN will be used.
		 * @return The next stage of the resource definition
		 */
		T withoutReverseFqdn();
	}

	
    interface DefinitionCreatable extends 
    	Creatable<PublicIpAddress>,
    	DefinitionWithLeafDomainLabel,
    	DefinitionWithIpAddress,
    	DefinitionWithReverseFQDN<DefinitionCreatable> {
    }
    
    interface Update extends 
    	Appliable<PublicIpAddress>,
    	UpdatableWithIpAddress<Update>,
    	UpdatableWithLeafDomainLabel<Update>,
    	UpdatableWithReverseFQDN<Update> {
    }
}

