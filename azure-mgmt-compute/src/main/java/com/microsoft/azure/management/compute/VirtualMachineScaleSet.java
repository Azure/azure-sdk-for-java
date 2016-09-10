package com.microsoft.azure.management.compute;

import com.microsoft.azure.management.network.LoadBalancer;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;

/**
 * An immutable client-side representation of an Azure virtual machine scale set.
 */
public interface VirtualMachineScaleSet {

    /**
     * The entirety of the load balancer definition.
     */
    interface Definition extends
            DefinitionStages.Blank,
            DefinitionStages.WithGroup,
            DefinitionStages.WithPrimaryInternetFacingLoadBalancer,
            DefinitionStages.WithPrimaryInternalLoadBalancer,
            DefinitionStages.WithPrimaryInternetFacingLoadBalancerBackendOrNatPool,
            DefinitionStages.WithInternalLoadBalancerBackendOrNatPool,
            DefinitionStages.WithCreate {
    }

    /**
     * Grouping of virtual machine scale set definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of a virtual machine scale set definition.
         */
        interface Blank
                extends GroupableResource.DefinitionWithRegion<VirtualMachineScaleSet.DefinitionStages.WithGroup> {
        }

        /**
         * The stage of the virtual machine scale set definition allowing to specify the resource group.
         */
        interface WithGroup
                extends GroupableResource.DefinitionStages.WithGroup<WithPrimaryInternetFacingLoadBalancer> {
        }

        /**
         * The stage of the virtual machine scale set definition allowing to specify a public load balancer for
         * the primary network interface configuration.
         */
        interface WithPrimaryInternetFacingLoadBalancer {
            /**
             * Specify the public load balancer where it's backends and/or NAT pools can be assigned to the primary network
             * interface configuration of virtual machine scale set.
             * <p>
             * By default all the backend and inbound NAT pool of the load balancer will be associated with the primary
             * network interface configuration unless one of them is selected in the next stage
             * {@link WithInternalLoadBalancerBackendOrNatPool}.
             * <p>
             * @param loadBalancer an existing public load balancer
             * @return the next stage of the definition
             */
            WithPrimaryInternetFacingLoadBalancerBackendOrNatPool withPrimaryInternetFacingLoadBalancer(LoadBalancer loadBalancer);

            /**
             * Specifies that no public load balancer needs to be associated with virtual machine scale set.
             *
             * @return the next stage of the virtual machine scale set definition
             */
            WithPrimaryInternalLoadBalancer withoutPrimaryInternetFacingLoadBalancer();
        }

        /**
         * The stage of the virtual machine scale set definition allowing to specify an internal load balancer for
         * the primary network interface configuration.
         */
        interface WithPrimaryInternalLoadBalancer {
            /**
             * Specify the internal load balancer where it's backends and/or NAT pools can be assigned to the primary
             * network interface configuration of the virtual machine scale set.
             * <p>
             * By default all the backend and inbound NAT pool of the load balancer will be associated with the primary
             * network interface configuration unless one of them is selected in the next stage
             * {@link WithInternalLoadBalancerBackendOrNatPool}.
             * <p>
             * @param loadBalancer an existing internal load balancer
             * @return the next stage of the definition
             */
            WithInternalLoadBalancerBackendOrNatPool withPrimaryInternalLoadBalancer(LoadBalancer loadBalancer);

            /**
             * Specifies that no internal load balancer needs to be associated with virtual machine scale set.
             *
             * @return the next stage of the virtual machine scale set definition
             */
            WithCreate withoutPrimaryInternalLoadBalancer();
        }

        /**
         * The stage of the virtual machine scale set definition allowing to associate backend pool and/or inbound NAT pool
         * of the internet facing load balancer selected in the previous state {@link WithPrimaryInternetFacingLoadBalancer}
         * with the primary network interface configuration.
         */
        interface WithPrimaryInternetFacingLoadBalancerBackendOrNatPool extends WithPrimaryInternetFacingLoadBalancerNatPool {
            /**
             * Associate internet facing load balancer backends with the primary network interface configuration of the
             * virtual machine scale set.
             *
             * @param backendNames the backend names
             * @return the next stage of the virtual machine scale set definition
             */
            WithPrimaryInternetFacingLoadBalancerNatPool withPrimaryInternetFacingLoadBalancerBackend(String ...backendNames);
        }

        /**
         * The stage of the virtual machine scale set definition allowing to associate inbound NAT pool of the internet
         * facing load balancer selected in the previous state {@link WithPrimaryInternetFacingLoadBalancer} with the
         * primary network interface configuration.
         */
        interface WithPrimaryInternetFacingLoadBalancerNatPool extends WithPrimaryInternalLoadBalancer {
            /**
             * Associate internet facing load balancer inbound NAT pools with the to the primary network interface
             * configuration of the virtual machine scale set.
             *
             * @param natPoolNames the inbound NAT pool names
             * @return the next stage of the virtual machine scale set definition
             */
            WithPrimaryInternalLoadBalancer withPrimaryInternetFacingLoadBalancerInboundNatPool(String ...natPoolNames);
        }

        /**
         * The stage of the virtual machine scale set definition allowing to associate backend pool and/or inbound NAT pool
         * of the internal load balancer selected in the previous state {@link WithPrimaryInternalLoadBalancer} with the
         * primary network interface configuration.
         */
        interface WithInternalLoadBalancerBackendOrNatPool extends WithCreate {
            /**
             * Associates internal load balancer backend pools with the primary network interface configuration
             * of the virtual machine scale set.
             *
             * @param backendNames the backend names
             * @return the next stage of the virtual machine scale set definition
             */
            WithInternalInternalLoadBalancerNatPool withPrimaryInternalLoadBalancerBackend(String ...backendNames);
         }

        /**
         * The stage of the virtual machine scale set definition allowing to assign inbound NAT pool of the internal
         * load balancer selected in the previous state {@link WithPrimaryInternalLoadBalancer} with the
         * primary network interface configuration.
         */
        interface WithInternalInternalLoadBalancerNatPool extends WithCreate {
            /**
             * Associates internal load balancer inbound NAT pools with the primary network interface configuration
             * of the virtual machine scale set.
             *
             * @param natPoolNames inbound NAT pool names
             * @return the next stage of the virtual machine scale set definition
             */
            WithCreate withPrimaryInternalLoadBalancerInboundNatPool(String ...natPoolNames);
        }

        /**
         * The stage of a virtual machine scale set definition containing all the required inputs for the resource
         * to be created (via {@link WithCreate#create()}), but also allowing for any other optional settings
         * to be specified.
         */
        interface WithCreate extends
                Creatable<VirtualMachineScaleSet>,
                Resource.DefinitionWithTags<VirtualMachineScaleSet.DefinitionStages.WithCreate> {
        }
    }

    /**
     * Grouping of virtual machine scale set update stages.
     */
    interface UpdateStages {
        /**
         * Stage of the virtual machine scale set update allowing to remove public and internal load balancer
         * from the primary network interface configuration.
         */
        interface WithoutPrimaryLoadBalancer {
            /**
             * Remove the internet facing load balancer associated to the primary network interface configuration.
             * <p>
             * This removes the association between primary network interface configuration and all backend and
             * inbound NAT pools in the load balancer.
             * </p>
             *
             * @return the next stage of the virtual machine scale set update
             */
            Update withoutPrimaryInternetFacingLoadBalancer();

            /**
             * Remove the internal load balancer associated to the primary network interface configuration.
             * <p>
             * This removes the association between primary network interface configuration and all backend and
             * inbound NAT pools in the load balancer.
             * </p>
             *
             * @return the next stage of the virtual machine scale set update
             */
            Update withoutPrimaryInternalLoadBalancer();
        }

        /**
         * Stage of the virtual machine scale set update allowing to associate a backend from the load balancer
         * with the primary network interface configuration.
         */
        interface WithPrimaryLoadBalancerBackend {
            /**
             * Associate a backend of the internet facing load balancer with the the primary network interface configuration.
             *
             * @param backendName the name of an existing backend
             * @return the next stage of the virtual machine scale set update
             */
            Update withPrimaryInternetFacingLoadBalancerBackend(String backendName);

            /**
             * Associate a backend of the internal load balancer with the the primary network interface configuration.
             *
             * @param backendName the name of an existing backend
             * @return the next stage of the virtual machine scale set update
             */
            Update withPrimaryInternalLoadBalancerBackend(String backendName);
        }

        /**
         * Stage of the virtual machine scale set update allowing to associate a inbound NAT pool from the load balancer
         * with the primary network interface configuration.
         */
        interface WithPrimaryLoadBalancerNatPoold {
            /**
             * Associate an inbound NAT pool of the internet facing load balancer with the the primary network interface configuration.
             *
             * @param natPoolName the name of an existing inbound NAT pool
             * @return the next stage of the virtual machine scale set update
             */
            Update withPrimaryInternetFacingLoadBalancerNatPool(String natPoolName);

            /**
             * Associate an inbound NAT pool of the internal load balancer with the the primary network interface configuration.
             *
             * @param natPoolName the name of an existing inbound NAT pool
             * @return the next stage of the virtual machine scale set update
             */
            Update withPrimaryInternalLoadBalancerNatPool(String natPoolName);
        }

        /**
         * Stage of the virtual machine scale set update allowing to remove association between the primary network interface
         * configuration and backend of the load balancer.
         */
        interface WithoutPrimaryLoadBalancerBackend {
            /**
             * Removes association between the primary network interface configuration and backend of the internet facing
             * load balancer.
             *
             * @param backendName the name of an existing backend
             * @return the next stage of the virtual machine scale set update
             */
            Update withoutPrimaryInternetFacingLoadBalancerBackend(String backendName);

            /**
             * Removes association between the primary network interface configuration and backend of the internal load balancer.
             *
             * @param backendName the name of an existing backend
             * @return the next stage of the virtual machine scale set update
             */
            Update withoutPrimaryInternalLoadBalancerBackend(String backendName);
        }

        /**
         * Stage of the virtual machine scale set update allowing to remove association between the primary network interface
         * configuration and inbound NAT pool of the load balancer.
         */
        interface WithoutPrimaryLoadBalancerNatPool {
            /**
             * Removes association between the primary network interface configuration and inbound NAT pool of the
             * internet facing load balancer.
             *
             * @param natPoolName the name of an existing inbound NAT pool
             * @return the next stage of the virtual machine scale set update
             */
            Update withoutPrimaryInternetFacingLoadBalancerNatPool(String natPoolName);

            /**
             * Removes association between the primary network interface configuration and inbound NAT pool of the
             * internal load balancer.
             *
             * @param natPoolName the name of an existing inbound NAT pool
             * @return the next stage of the virtual machine scale set update
             */
            Update withoutPrimaryInternalLoadBalancerNatPool(String natPoolName);
        }

        interface Update {
        }
    }
}