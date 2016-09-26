/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.network.implementation.PublicIPAddressInner;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Updatable;
import com.microsoft.azure.management.resources.fluentcore.model.Appliable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;

/**
 * Public IP address.
 */
@Fluent()
public interface PublicIpAddress extends
        GroupableResource,
        Refreshable<PublicIpAddress>,
        Wrapper<PublicIPAddressInner>,
        Updatable<PublicIpAddress.Update> {

    // Getters

    /**
     * @return the IP version of the public IP address
     */
    IPVersion version();

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
    IPAllocationMethod ipAllocationMethod();

    /**
     * @return the idle connection timeout setting (in minutes)
     */
    int idleTimeoutInMinutes();

    /**
     * @return the load balancer public frontend that this public IP address is assigned to
     */
    PublicFrontend getAssignedLoadBalancerFrontend();

    /**
     * @return true if this public IP address is assigned to a load balancer
     */
    boolean hasAssignedLoadBalancer();

    /**
     * @return the network interface IP configuration that this public IP address is assigned to
     */
    NicIpConfiguration getAssignedNetworkInterfaceIpConfiguration();

    /**
     * @return true if this public IP address is assigned to a network interface
     */
    boolean hasAssignedNetworkInterface();

    /**
     * Container interface for all the definitions.
     */
    interface Definition extends
        DefinitionStages.Blank,
        DefinitionStages.WithGroup,
        DefinitionStages.WithCreate {
    }

    /**
     * Grouping of public IP address definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of a public IP address definition.
         */
        interface Blank
            extends GroupableResource.DefinitionWithRegion<WithGroup> {
        }

        /**
         * The stage of the public IP address definition allowing to specify the resource group.
         */
        interface WithGroup
            extends GroupableResource.DefinitionStages.WithGroup<WithCreate> {
        }

        /**
         * A public IP address definition allowing to set the IP allocation method (static or dynamic).
         */
        interface WithIpAddress {
            /**
             * Enables static IP address allocation.
             * <p>
             * Use {@link PublicIpAddress#ipAddress()} after the public IP address is created to obtain the
             * actual IP address allocated for this resource by Azure
             *
             * @return the next stage of the public IP address definition
             */
            WithCreate withStaticIp();

            /**
             * Enables dynamic IP address allocation.
             *
             * @return the next stage of the public IP address definition
             */
            WithCreate withDynamicIp();
        }

        /**
         * A public IP address definition allowing to specify the leaf domain label, if any.
         */
        interface WithLeafDomainLabel {
            /**
             * Specifies the leaf domain label to associate with this public IP address.
             * <p>
             * The fully qualified domain name (FQDN)
             * will be constructed automatically by appending the rest of the domain to this label.
             * @param dnsName the leaf domain label to use. This must follow the required naming convention for leaf domain names.
             * @return the next stage of the public IP address definition
             */
            WithCreate withLeafDomainLabel(String dnsName);

            /**
             * Ensures that no leaf domain label will be used.
             * <p>
             * This means that this public IP address will not be associated with a domain name.
             * @return the next stage of the public IP address definition
             */
            WithCreate withoutLeafDomainLabel();
        }

        /**
         * A public IP address definition allowing the reverse FQDN to be specified.
         */
        interface WithReverseFQDN {
            /**
             * Specifies the reverse FQDN to assign to this public IP address.
             * <p>
             *
             * @param reverseFQDN the reverse FQDN to assign
             * @return the next stage of the resource definition
             */
            WithCreate withReverseFqdn(String reverseFQDN);

            /**
             * Ensures that no reverse FQDN will be used.
             * @return the next stage of the resource definition
             */
            WithCreate withoutReverseFqdn();
        }

        /**
         * A public IP address definition allowing the idle timeout to be specified.
         */
        interface WithIdleTimeout {
            /**
             * Specifies the timeout (in minutes) for an idle connection.
             *
             * @param minutes the length of the time out in minutes
             * @return the next stage of the resource definition
             */
            WithCreate withIdleTimeoutInMinutes(int minutes);
        }

        /**
         * The stage of the public IP definition which contains all the minimum required inputs for
         * the resource to be created (via {@link WithCreate#create()}), but also allows
         * for any other optional settings to be specified.
         */
        interface WithCreate extends
            Creatable<PublicIpAddress>,
            DefinitionStages.WithLeafDomainLabel,
            DefinitionStages.WithIpAddress,
            DefinitionStages.WithReverseFQDN,
            DefinitionStages.WithIdleTimeout,
            Resource.DefinitionWithTags<WithCreate> {
        }
    }

    /**
     * Container interface for all the updates.
     * <p>
     * Use {@link Update#apply()} to apply the changes to the resource in Azure.
     */
    interface Update extends
        Appliable<PublicIpAddress>,
        UpdateStages.WithIpAddress,
        UpdateStages.WithLeafDomainLabel,
        UpdateStages.WithReverseFQDN,
        UpdateStages.WithIdleTimout,
        Resource.UpdateWithTags<Update> {
    }

    /**
     * Grouping of public IP address update stages.
     */
    interface UpdateStages {
        /**
         * A public IP address update allowing to change the IP allocation method (static or dynamic).
         */
        interface WithIpAddress {
            /**
             * Enables static IP address allocation.
             * <p>
             * Use {@link PublicIpAddress#ipAddress()} after the public IP address is updated to
             * obtain the actual IP address allocated for this resource by Azure
             *
             * @return the next stage of the resource update
             */
            Update withStaticIp();

            /**
             * Enables dynamic IP address allocation.
             *
             * @return the next stage of the resource update
             */
            Update withDynamicIp();
        }

        /**
         * A public IP address update allowing to change the leaf domain label, if any.
         */
        interface WithLeafDomainLabel {
            /**
             * Specifies the leaf domain label to associate with this public IP address.
             * <p>
             * The fully qualified domain name (FQDN)
             * will be constructed automatically by appending the rest of the domain to this label.
             * @param dnsName the leaf domain label to use. This must follow the required naming convention for leaf domain names.
             * @return the next stage of the resource update
             */
            Update withLeafDomainLabel(String dnsName);

            /**
             * Ensures that no leaf domain label will be used.
             * <p>
             * This means that this public IP address will not be associated with a domain name.
             * @return the next stage of the resource update
             */
            Update withoutLeafDomainLabel();
        }

        /**
         * A public IP address update allowing the reverse FQDN to be changed.
         */
        interface WithReverseFQDN {
            /**
             * Specifies the reverse FQDN to assign to this public IP address.
             *
             * @param reverseFQDN the reverse FQDN to assign
             * @return the next stage of the resource update
             */
            Update withReverseFqdn(String reverseFQDN);

            /**
             * Ensures that no reverse FQDN will be used.
             *
             * @return The next stage of the resource update
             */
            Update withoutReverseFqdn();
        }

        /**
         * A public IP address update allowing the idle timeout to be changed.
         */
        interface WithIdleTimout {
            /**
             * Specifies the timeout (in minutes) for an idle connection.
             *
             * @param minutes the length of the time out in minutes
             * @return the next stage of the resource update
             */
            Update withIdleTimeoutInMinutes(int minutes);
        }
    }
}
