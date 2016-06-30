package com.microsoft.azure.management.network;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.resources.fluentcore.arm.models.ChildResource;
import com.microsoft.azure.management.resources.fluentcore.model.Attachable;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Settable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;

import java.io.IOException;

/**
 * An IP configuration in a network interface.
 */
public interface NicIpConfiguration extends
        Wrapper<NetworkInterfaceIPConfiguration>,
        ChildResource {
    // Getters

    /**
     * Gets the resource id of the public IP address associated with this IP configuration.
     *
     * @return public IP resource ID or null if there is no public IP associated
     */
    String publicIpAddressId();

    /**
     * Gets the public IP address associated with this IP configuration.
     * <p>
     * This method makes a rest API call to fetch the public IP.
     *
     * @return the public IP associated with this IP configuration or null if there is no public IP associated
     * @throws CloudException exceptions thrown from the cloud.
     * @throws IOException exceptions thrown from serialization/deserialization.
     */
    PublicIpAddress publicIpAddress() throws CloudException, IOException;

    /**
     * @return the resource id of the virtual network subnet associated with this IP configuration.
     */
    String subnetId();

    /**
     * Gets the virtual network associated with this IP configuration.
     * <p>
     * This method makes a rest API call to fetch the public IP.
     *
     * @return the virtual network associated with this this IP configuration.
     * @throws CloudException exceptions thrown from the cloud.
     * @throws IOException exceptions thrown from serialization/deserialization.
     */
    Network network() throws CloudException, IOException;

    /**
     * Gets the private IP address allocated to this IP configuration.
     * <p>
     * The private IP will be within the virtual network subnet of this IP configuration.
     *
     * @return the private IP addresses
     */
    String privateIp();

    /**
     * @return the private IP allocation method (Dynamic, Static)
     */
    String privateIpAllocationMethod();

    // Setters (fluent)

    /**
     * The entirety of the network interface IP configuration definition.
     * @param <ParentT> the return type of the final {@link Attachable#attach()}
     */
    interface Definition<ParentT> extends
        DefinitionStages.Blank<ParentT>,
        DefinitionStages.WithAttach<ParentT>,
        DefinitionStages.WithNetwork<ParentT>,
        DefinitionStages.WithSubnet<ParentT>,
        DefinitionStages.WithPrivateIp<ParentT> {
    }

    /**
     * Grouping of network interface IP configuration definition stages applicable as part of a
     * network interface update.
     */
    interface DefinitionStages {
        /**
         * The first stage of network interface IP configuration definition.
         *
         * @param <ParentT> the return type of the final {@link Attachable#attach()}
         */
        interface Blank<ParentT> extends WithNetwork<ParentT> {
        }

        /**
         * The stage of the network interface IP configuration definition allowing to specify the virtual network.
         *
         * @param <ParentT> the return type of the final {@link Attachable#attach()}
         */
        interface WithNetwork<ParentT> {
            /**
             * Create a new virtual network to associate with the  network interface IP configuration,
             * based on the provided definition.
             *
             * @param creatable a creatable definition for a new virtual network
             * @return the next stage of the network interface IP configuration definition
             */
            WithPrivateIp<ParentT> withNewNetwork(Creatable<Network> creatable);

            /**
             * Creates a new virtual network to associate with the network interface IP configuration.
             * <p>
             * the virtual network will be created in the same resource group and region as of parent
             * network interface, it will be created with the specified address space and a default subnet
             * covering the entirety of the network IP address space.
             *
             * @param name the name of the new virtual network
             * @param addressSpace the address space for rhe virtual network
             * @return the next stage of the network interface IP configuration definition
             */
            WithPrivateIp<ParentT> withNewNetwork(String name, String addressSpace);

            /**
             * Creates a new virtual network to associate with the network interface IP configuration.
             * <p>
             * the virtual network will be created in the same resource group and region as of parent network interface,
             * it will be created with the specified address space and a default subnet covering the entirety of the
             * network IP address space.
             *
             * @param addressSpace the address space for the virtual network
             * @return the next stage of the network interface IP configuration definition
             */
            WithPrivateIp<ParentT> withNewNetwork(String addressSpace);

            /**
             * Associate an existing virtual network with the network interface IP configuration.
             *
             * @param network an existing virtual network
             * @return the next stage of the network interface IP configuration definition
             */
            WithSubnet<ParentT> withExistingNetwork(Network network);
        }

        /**
         * The stage of the network interface IP configuration definition allowing to specify private IP address
         * within a virtual network subnet.
         *
         * @param <ParentT> the return type of the final {@link Attachable#attach()}
         */
        interface WithPrivateIp<ParentT> {
            /**
             * Enables dynamic private IP address allocation within the specified existing virtual network
             * subnet for the network interface IP configuration.
             *
             * @return the next stage of network interface IP configuration definition
             */
            WithAttach<ParentT> withPrivateIpAddressDynamic();

            /**
             * Assigns the specified static private IP address within the specified existing virtual network
             * subnet to the network interface IP configuration.
             *
             * @param staticPrivateIpAddress the static IP address within the specified subnet to assign to
             *                               the network interface
             * @return the next stage of network interface IP configuration definition
             */
            WithAttach<ParentT> withPrivateIpAddressStatic(String staticPrivateIpAddress);
        }

        /**
         * The stage of the network interface IP configuration definition allowing to specify subnet.
         *
         * @param <ParentT> the return type of the final {@link Attachable#attach()}
         */
        interface WithSubnet<ParentT> {
            /**
             * Associate a subnet with the network interface IP configuration.
             *
             * @param name the subnet name
             * @return the next stage of the network interface IP configuration definition
             */
            WithPrivateIp<ParentT> withSubnet(String name);
        }

        /**
         * The stage of the network interface IP configuration definition allowing to associate it with
         * a public IP address.
         *
         * @param <ParentT> the return type of the final {@link Attachable#attach()}
         */
        interface WithPublicIpAddress<ParentT> {
            /**
             * Create a new public IP address to associate with the network interface IP configuration,
             * based on the provided definition.
             *
             * @param creatable a creatable definition for a new public IP
             * @return the next stage of the network interface IP configuration definition
             */
            WithAttach<ParentT> withNewPublicIpAddress(Creatable<PublicIpAddress> creatable);

            /**
             * Creates a new public IP address in the same region and group as the resource and associate it
             * with with the network interface IP configuration.
             * <p>
             * The internal name and DNS label for the public IP address will be derived from the network interface name.
             *
             * @return the next stage of the network interface IP configuration definition
             */
            WithAttach<ParentT> withNewPublicIpAddress();

            /**
             * Creates a new public IP address in the same region and group as the resource, with the specified DNS label
             * and associate it with the network interface IP configuration.
             * <p>
             * The internal name for the public IP address will be derived from the DNS label.
             *
             * @param leafDnsLabel the leaf domain label
             * @return tthe next stage of the IP configuration definition
             */
            WithAttach<ParentT> withNewPublicIpAddress(String leafDnsLabel);

            /**
             * Associates an existing public IP address with the network interface IP configuration.
             *
             * @param publicIpAddress an existing public IP address
             * @return the next stage of the IP configuration definition
             */
            WithAttach<ParentT> withExistingPublicIpAddress(PublicIpAddress publicIpAddress);
        }

        /**
         * The final stage of network interface IP configuration.
         * <p>
         * At this stage, any remaining optional settings can be specified, or the network interface IP configuration
         * definition can be attached to the parent network interface definition using {@link WithAttach#attach()}.
         *
         * @param <ParentT> the return type of the final {@link Attachable#attach()}
         */
        interface WithAttach<ParentT>
                extends
                Attachable.InDefinition<ParentT>,
                WithPublicIpAddress<ParentT> {
        }
    }

    /** The entirety of a network interface IP configuration definition as part of a network interface update.
     * @param <ParentT> the return type of the final {@link UpdateDefinitionStages.WithAttach#attach()}
     */
    interface UpdateDefinition<ParentT> extends
            UpdateDefinitionStages.Blank<ParentT>,
            UpdateDefinitionStages.WithAttach<ParentT>,
            UpdateDefinitionStages.WithNetwork<ParentT>,
            UpdateDefinitionStages.WithPrivateIp<ParentT>,
            UpdateDefinitionStages.WithSubnet<ParentT>,
            UpdateDefinitionStages.WithPublicIpAddress<ParentT> {
    }

    /**
     * Grouping of network interface IP configuration definition stages.
     */
    interface UpdateDefinitionStages {
        /**
         * The first stage of network interface IP configuration definition.
         *
         * @param <ParentT> the return type of the final {@link Attachable#attach()}
         */
        interface Blank<ParentT> extends WithNetwork<ParentT> {
        }

        /**
         * The stage of the network interface IP configuration definition allowing to specify the virtual network.
         *
         * @param <ParentT> the return type of the final {@link Attachable#attach()}
         */
        interface WithNetwork<ParentT> {
            /**
             * Create a new virtual network to associate with the  network interface IP configuration,
             * based on the provided definition.
             *
             * @param creatable a creatable definition for a new virtual network
             * @return the next stage of the network interface IP configuration definition
             */
            WithPrivateIp<ParentT> withNewNetwork(Creatable<Network> creatable);

            /**
             * Creates a new virtual network to associate with the network interface IP configuration.
             * <p>
             * the virtual network will be created in the same resource group and region as of parent
             * network interface, it will be created with the specified address space and a default subnet
             * covering the entirety of the network IP address space.
             *
             * @param name the name of the new virtual network
             * @param addressSpace the address space for rhe virtual network
             * @return the next stage of the network interface IP configuration definition
             */
            WithPrivateIp<ParentT> withNewNetwork(String name, String addressSpace);

            /**
             * Creates a new virtual network to associate with the network interface IP configuration.
             * <p>
             * the virtual network will be created in the same resource group and region as of parent network interface,
             * it will be created with the specified address space and a default subnet covering the entirety of the
             * network IP address space.
             *
             * @param addressSpace the address space for the virtual network
             * @return the next stage of the network interface IP configuration definition
             */
            WithPrivateIp<ParentT> withNewNetwork(String addressSpace);

            /**
             * Associate an existing virtual network with the network interface IP configuration.
             *
             * @param network an existing virtual network
             * @return the next stage of the network interface IP configuration definition
             */
            WithSubnet<ParentT> withExistingNetwork(Network network);
        }

        /**
         * The stage of the network interface IP configuration definition allowing to specify private IP address
         * within a virtual network subnet.
         *
         * @param <ParentT> the return type of the final {@link Attachable#attach()}
         */
        interface WithPrivateIp<ParentT> {
            /**
             * Enables dynamic private IP address allocation within the specified existing virtual network
             * subnet for the network interface IP configuration.
             *
             * @return the next stage of network interface IP configuration definition
             */
            WithAttach<ParentT> withPrivateIpAddressDynamic();

            /**
             * Assigns the specified static private IP address within the specified existing virtual network
             * subnet to the network interface IP configuration.
             *
             * @param staticPrivateIpAddress the static IP address within the specified subnet to assign to
             *                               the network interface
             * @return the next stage of network interface IP configuration definition
             */
            WithAttach<ParentT> withPrivateIpAddressStatic(String staticPrivateIpAddress);
        }

        /**
         * The stage of the network interface IP configuration definition allowing to specify subnet.
         *
         * @param <ParentT> the return type of the final {@link Attachable#attach()}
         */
        interface WithSubnet<ParentT> {
            /**
             * Associate a subnet with the network interface IP configuration.
             *
             * @param name the subnet name
             * @return the next stage of the network interface IP configuration definition
             */
            WithPrivateIp<ParentT> withSubnet(String name);
        }

        /**
         * The stage of the network interface IP configuration definition allowing to associate it with
         * a public IP address.
         *
         * @param <ParentT> the return type of the final {@link Attachable#attach()}
         */
        interface WithPublicIpAddress<ParentT> {
            /**
             * Create a new public IP address to associate with the network interface IP configuration,
             * based on the provided definition.
             *
             * @param creatable a creatable definition for a new public IP
             * @return the next stage of the network interface IP configuration definition
             */
            WithAttach<ParentT> withNewPublicIpAddress(Creatable<PublicIpAddress> creatable);

            /**
             * Creates a new public IP address in the same region and group as the resource and associate it
             * with with the network interface IP configuration.
             * <p>
             * The internal name and DNS label for the public IP address will be derived from the network interface name.
             *
             * @return the next stage of the network interface IP configuration definition
             */
            WithAttach<ParentT> withNewPublicIpAddress();

            /**
             * Creates a new public IP address in the same region and group as the resource, with the specified DNS label
             * and associate it with the network interface IP configuration.
             * <p>
             * The internal name for the public IP address will be derived from the DNS label.
             *
             * @param leafDnsLabel the leaf domain label
             * @return tthe next stage of the IP configuration definition
             */
            WithAttach<ParentT> withNewPublicIpAddress(String leafDnsLabel);

            /**
             * Associates an existing public IP address with the network interface IP configuration.
             *
             * @param publicIpAddress an existing public IP address
             * @return the next stage of the IP configuration definition
             */
            WithAttach<ParentT> withExistingPublicIpAddress(PublicIpAddress publicIpAddress);
        }

        /**
         * The final stage of network interface IP configuration.
         * <p>
         * At this stage, any remaining optional settings can be specified, or the network interface IP configuration
         * definition can be attached to the parent network interface definition using {@link WithAttach#attach()}.
         *
         * @param <ParentT> the return type of the final {@link Attachable#attach()}
         */
        interface WithAttach<ParentT>
                extends
                Attachable.InUpdate<ParentT>,
                WithPublicIpAddress<ParentT> {
        }
    }

    /**
     * The entirety of a network interface IP configuration update as part of a network interface update.
     */
    interface Update extends
        Settable<NetworkInterface.Update>,
        UpdateStages.WithSubnet,
        UpdateStages.WithPrivateIp,
        UpdateStages.WithPublicIpAddress {
    }

    /**
     * Grouping of network interface IP configuration update stages.
     */
    interface UpdateStages {
        /**
         * The stage of the network interface IP configuration update allowing to specify subnet.
         */
        interface WithSubnet {
            /**
             * Associate a subnet with the network interface IP configuration.
             *
             * @param name the subnet name
             * @return the next stage of the network interface IP configuration update
             */
            Update withSubnet(String name);
        }

        /**
         * The stage of the network interface IP configuration update allowing to specify private IP.
         */
        interface WithPrivateIp {
            /**
             * Enables dynamic private IP address allocation within the specified existing virtual network
             * subnet to the network interface IP configuration.
             *
             * @return the next stage of the network interface IP configuration update
             */
            Update withPrivateIpAddressDynamic();

            /**
             * Assigns the specified static private IP address within the specified existing virtual network
             * subnet to the network interface IP configuration.
             *
             * @param staticPrivateIpAddress the static IP address within the specified subnet to assign to
             *                               the  IP configuration
             * @return the next stage of the network interface IP configuration update
             */
            Update withPrivateIpAddressStatic(String staticPrivateIpAddress);
        }

        /**
         * The stage of the network interface IP configuration update allowing to specify public IP address.
         */
        interface WithPublicIpAddress {
            /**
             * Create a new public IP address to associate the network interface IP configuration with,
             * based on the provided definition.
             * <p>
             * If there is public IP associated with the IP configuration then that will be removed in
             * favour of this.
             *
             * @param creatable a creatable definition for a new public IP
             * @return the next stage of the network interface IP configuration update
             */
            Update withNewPublicIpAddress(Creatable<PublicIpAddress> creatable);

            /**
             * Creates a new public IP address in the same region and group as the resource and associate it
             * with the IP configuration.
             * <p>
             * The internal name and DNS label for the public IP address will be derived from the network interface
             * name, if there is an existing public IP association then that will be removed in favour of this.
             *
             * @return the next stage of the network interface IP configuration update
             */
            Update withNewPublicIpAddress();

            /**
             * Creates a new public IP address in the same region and group as the resource, with the specified DNS
             * label and associate it with the IP configuration.
             * <p>
             * The internal name for the public IP address will be derived from the DNS label, if there is an existing
             * public IP association then that will be removed in favour of this
             *
             * @param leafDnsLabel the leaf domain label
             * @return the next stage of the network interface IP configuration update
             */
            Update withNewPublicIpAddress(String leafDnsLabel);

            /**
             * Specifies that remove any public IP associated with the IP configuration.
             *
             * @return the next stage of the network interface IP configuration update
             */
            Update withoutPublicIpAddress();
        }
    }
}
