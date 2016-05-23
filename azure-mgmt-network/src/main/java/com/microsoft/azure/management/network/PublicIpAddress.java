/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network;

import com.microsoft.azure.management.network.implementation.api.PublicIPAddressInner;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Updatable;
import com.microsoft.azure.management.resources.fluentcore.model.Appliable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;

/**
 * Public IP address
 */
public interface PublicIpAddress extends
        GroupableResource,
        Refreshable<PublicIpAddress>,
        Wrapper<PublicIPAddressInner>,
        Updatable<PublicIpAddress.Update> {

    /***********************************************************
     * Getters
     ***********************************************************/

	/**
	 * @return the assigned IP address
	 */
	String ipAddress();

	/**
	 * @return the assigned leaf domain label
	 */
	String leafDomainLabel();

	/** 
	 * @return the assigned FQDN (fully qualified domain name)
	 */
	String fqdn();

	/**
	 * @return the assigned reverse FQDN, if any
	 */
	String reverseFqdn();

	/**
	 * @return the IP address allocation method (Static/Dynamic)
	 */
	String ipAllocationMethod();

	/**
	 * @return the idle connection timeout setting (in minutes)
	 */
	int idleTimeoutInMinutes();

    /**************************************************************
     * Fluent interfaces for provisioning
     **************************************************************/

	/**
	 * Container interface for all the definitions
	 */
	interface Definitions extends 
	    DefinitionBlank,
	    DefinitionWithGroup,
	    DefinitionWithIpAddress,
	    DefinitionWithLeafDomainLabel,
	    DefinitionCreatable {}

	/**
	 * The first stage of a public IP address definition
	 */
	interface DefinitionBlank 
	    extends GroupableResource.DefinitionWithRegion<DefinitionWithGroup> {
	}

    /**
     * The stage of the public IP address definition allowing to specify the resource group
     */
    interface DefinitionWithGroup 
        extends GroupableResource.DefinitionWithGroup<DefinitionCreatable> {
    }

    /**
     * A public IP address definition allowing to set the IP allocation method (static or dynamic)
     */
    interface DefinitionWithIpAddress {
        /**
         * Enables static IP address allocation. 
         * <p>
         * Use {@link PublicIpAddress#ipAddress()} after the public IP address is created to obtain the
         * actual IP address allocated for this resource by Azure
         * 
         * @return the next stage of the public IP address definition
         */
        DefinitionCreatable withStaticIp();

        /**
         * Enables dynamic IP address allocation.
         * 
         * @return the next stage of the public IP address definition
         */
        DefinitionCreatable withDynamicIp();
    }


    /**
     * A public IP address update allowing to change the IP allocation method (static or dynamic)
     */
    interface UpdateWithIpAddress {
        /**
         * Enables static IP address allocation. 
         * <p>
         * Use {@link PublicIpAddress#ipAddress()} after the public IP address is updated to 
         * obtain the actual IP address allocated for this resource by Azure
         * 
         * @return the next stage of the public IP address definition
         */
        Update withStaticIp();

        /**
         * Enables dynamic IP address allocation.
         *
         * @return the next stage of the public IP address definition
         */
        Update withDynamicIp();		
    }

    
    /**
     * A public IP address definition allowing to specify the leaf domain label, if any
     */
    interface DefinitionWithLeafDomainLabel {
        /**
         * Specifies the leaf domain label to associate with this public IP address. 
         * <p>
         * The fully qualified domain name (FQDN) 
         * will be constructed automatically by appending the rest of the domain to this label.
         * @param dnsName the leaf domain label to use. This must follow the required naming convention for leaf domain names.
         * @return the next stage of the public IP address definition
         */
        DefinitionCreatable withLeafDomainLabel(String dnsName);

        /**
         * Ensures that no leaf domain label will be used. 
         * <p>
         * This means that this public IP address will not be associated with a domain name.
         * @return the next stage of the public IP address definition
         */
        DefinitionCreatable withoutLeafDomainLabel();
    }


    /**
     * A public IP address update allowing to change the leaf domain label, if any
     */
    interface UpdateWithLeafDomainLabel {
        /**
         * Specifies the leaf domain label to associate with this public IP address. 
         * <p>
         * The fully qualified domain name (FQDN) 
         * will be constructed automatically by appending the rest of the domain to this label.
         * @param dnsName the leaf domain label to use. This must follow the required naming convention for leaf domain names.
         * @return the next stage of the public IP address definition
         */
        Update withLeafDomainLabel(String dnsName);

        /**
         * Ensures that no leaf domain label will be used. 
         * <p>
         * This means that this public IP address will not be associated with a domain name.
         * @return the next stage of the resource definition
         */
        Update withoutLeafDomainLabel();
    }


    /**
     * A public IP address definition allowing the reverse FQDN to be specified
     */
    interface DefinitionWithReverseFQDN<T> {
        /**
         * Specifies the reverse FQDN to assign to this public IP address.
         * <p>
         * 
         * @param reverseFQDN the reverse FQDN to assign 
         * @return the next stage of the resource definition
         */
        T withReverseFqdn(String reverseFQDN);

        /**
         * Ensures that no reverse FQDN will be used.
         * @return the next stage of the resource definition
         */
        T withoutReverseFqdn();
    }

    /**
     * A public IP address update allowing the reverse FQDN to be specified
     */
    interface UpdateWithReverseFQDN {
        /**
         * Specifies the reverse FQDN to assign to this public IP address
         * @param reverseFQDN the reverse FQDN to assign 
         * @return the next stage of the resource update
         */
        Update withReverseFqdn(String reverseFQDN);

        /**
         * Ensures that no reverse FQDN will be used.
         * @return The next stage of the resource update
         */
        Update withoutReverseFqdn();
    }

    /**
     * The stage of the public IP definition which contains all the minimum required inputs for
     * the resource to be created (via {@link DefinitionCreatable#create()}), but also allows 
     * for any other optional settings to be specified.
     */
    interface DefinitionCreatable extends 
        Creatable<PublicIpAddress>,
        DefinitionWithLeafDomainLabel,
        DefinitionWithIpAddress,
        DefinitionWithReverseFQDN<DefinitionCreatable>,
        Resource.DefinitionWithTags<DefinitionCreatable> {
        
        /**
         * Specifies the timeout (in minutes) for an idle connection 
         * @param minutes the length of the time out in minutes
         * @return the next stage of the resource definition
         */
        DefinitionCreatable withIdleTimeoutInMinutes(int minutes);
    }

    /**
     * The template for a public IP address update operation, containing all the settings that 
     * can be modified.
     * <p>
     * Call {@link Update#apply()} to apply the changes to the resource in Azure.
     */
    interface Update extends 
        Appliable<PublicIpAddress>,
        UpdateWithIpAddress,
        UpdateWithLeafDomainLabel,
        UpdateWithReverseFQDN,
        Resource.UpdateWithTags<Update> {
            /**
             * Specifies the timeout (in minutes) for an idle connection 
             * @param minutes the length of the time out in minutes
             * @return the next stage of the resource update
             */
            Update withIdleTimeoutInMinutes(int minutes);
    }
}
