package com.microsoft.azure.management.network;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.network.implementation.api.NetworkInterfaceIPConfiguration;
import com.microsoft.azure.management.resources.fluentcore.arm.models.ChildResource;
import com.microsoft.azure.management.resources.fluentcore.model.Appliable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;

import java.io.IOException;

/**
 * An Ip configuration in a network interface.
 */
public interface NicIpConfiguration extends
        Wrapper<NetworkInterfaceIPConfiguration>,
        ChildResource {

    /**
     * Container interface for all the definitions.
     *
     * @param <parentT> the return type of the final {@link DefinitionAttachable#attach()}
     */
    interface Definitions<parentT> extends
            NicIpConfiguration.DefinitionBlank<parentT>,
            NicIpConfiguration.DefinitionWithNetwork<parentT>,
            NicIpConfiguration.DefinitionWithPrivateIp<parentT>,
            NicIpConfiguration.DefinitionWithSubnet,
            NicIpConfiguration.DefinitionWithPublicIpAddress<parentT>,
            NicIpConfiguration.DefinitionAttachable<parentT> {
    }

    /**
     * Gets the resource id of the public IP address associated with this Ip configuration.
     * <p>
     * returns null if there is no public IP associated
     *
     * @return public ip resource id
     */
    String publicIpAddressId();

    /**
     * Gets the public IP address associated with this Ip configuration.
     * <p>
     * note that this method makes a rest API call to fetch the public IP
     *
     * @return the public IP associated with this Ip configuration.
     * @throws CloudException exceptions thrown from the cloud.
     * @throws IOException exceptions thrown from serialization/deserialization.
     */
    PublicIpAddress publicIpAddress() throws CloudException, IOException;

    /**
     * @return the resource id of the virtual network subnet associated with this Ip configuration.
     */
    String subnetId();

    /**
     * Gets the virtual network associated with this Ip configuration.
     * <p>
     * note that this method makes a rest API call to fetch the public IP
     *
     * @return the virtual network associated with this this Ip configuration.
     * @throws CloudException exceptions thrown from the cloud.
     * @throws IOException exceptions thrown from serialization/deserialization.
     */
    Network network() throws CloudException, IOException;

    /**
     * Gets the private IP address allocated to this Ip configuration.
     * <p>
     * the private IP will be within the virtual network subnet of this Ip configuration
     *
     * @return the private IP addresses
     */
    String privateIp();

    /**
     * @return the private IP allocation method (Dynamic, Static)
     */
    String privateIpAllocationMethod();

    /**
     * The first stage of Ip configuration definition.
     *
     * @param <parentT> the return type of the final {@link DefinitionAttachable#attach()}
     */
    interface DefinitionBlank<parentT> extends DefinitionWithNetwork<parentT> {
    }

    /**
     * The stage of the Ip configuration definition allowing to specify the virtual network.
     *
     * @param <parentT> the return type of the final {@link DefinitionAttachable#attach()}
     */
    interface DefinitionWithNetwork<parentT> {
        /**
         * Create a new virtual network to associate with the Ip configuration, based on the provided definition.
         *
         * @param creatable a creatable definition for a new virtual network
         * @return the next stage of the Ip configuration definition
         */
        DefinitionWithPrivateIp<parentT> withNewNetwork(Network.DefinitionCreatable creatable);

        /**
         * Creates a new virtual network to associate with the Ip configuration.
         * <p>
         * the virtual network will be created in the same resource group and region as of parent network interface,
         * it will be created with the specified address space and a default subnet covering the entirety of the
         * network IP address space.
         *
         * @param name the name of the new virtual network
         * @param addressSpace the address space for rhe virtual network
         * @return the next stage of the Ip configuration definition
         */
        DefinitionWithPrivateIp<parentT> withNewNetwork(String name, String addressSpace);

        /**
         * Creates a new virtual network to associate with the Ip configuration.
         * <p>
         * the virtual network will be created in the same resource group and region as of parent network interface,
         * it will be created with the specified address space and a default subnet covering the entirety of the
         * network IP address space.
         *
         * @param addressSpace the address space for the virtual network
         * @return the next stage of the Ip configuration definition
         */
        DefinitionWithPrivateIp<parentT> withNewNetwork(String addressSpace);

        /**
         * Associate an existing virtual network with the Ip configuration.
         *
         * @param network an existing virtual network
         * @return the next stage of the Ip configuration definition
         */
        DefinitionWithSubnet<DefinitionWithPrivateIp<parentT>> withExistingNetwork(Network network);
    }

    /**
     * The stage of the Ip configuration definition allowing to specify private IP address within
     * a virtual network subnet.
     *
     * @param <parentT> the return type of the final {@link DefinitionAttachable#attach()}
     */
    interface DefinitionWithPrivateIp<parentT> {
        /**
         * Enables dynamic private IP address allocation within the specified existing virtual network
         * subnet for the Ip configuration.
         *
         * @return the next stage of Ip configuration definition
         */
        DefinitionAttachable<parentT> withPrivateIpAddressDynamic();

        /**
         * Assigns the specified static private IP address within the specified existing virtual network
         * subnet to the Ip configuration.
         *
         * @param staticPrivateIpAddress the static IP address within the specified subnet to assign to
         *                               the network interface
         * @return the next stage of Ip configuration definition
         */
        DefinitionAttachable<parentT> withPrivateIpAddressStatic(String staticPrivateIpAddress);
    }

    /**
     * The stage of the Ip configuration definition allowing to specify subnet.
     *
     * @param <T> the next stage after setting the subnet
     */
    interface DefinitionWithSubnet<T> {
        /**
         * Associate a subnet with the Ip configuration.
         *
         * @param name the subnet name
         * @return the next stage of the Ip configuration definition
         */
        T withSubnet(String name);
    }

    /**
     * The stage of the Ip configuration definition allowing to associate it with a public IP address.
     *
     * @param <parentT> the return type of the final {@link DefinitionAttachable#attach()}
     */
    interface DefinitionWithPublicIpAddress<parentT> {
        /**
         * Create a new public IP address to associate with the Ip configuration, based on the provided definition.
         *
         * @param creatable a creatable definition for a new public IP
         * @return the next stage of the Ip configuration definition
         */
        DefinitionAttachable<parentT> withNewPublicIpAddress(PublicIpAddress.DefinitionCreatable creatable);

        /**
         * Creates a new public IP address in the same region and group as the resource and associate it
         * with with the Ip configuration.
         * <p>
         * the internal name and DNS label for the public IP address will be derived from the network interface name
         *
         * @return the next stage of the Ip configuration definition
         */
        DefinitionAttachable<parentT> withNewPublicIpAddress();

        /**
         * Creates a new public IP address in the same region and group as the resource, with the specified DNS label
         * and associate it with the Ip configuration.
         * <p>
         * the internal name for the public IP address will be derived from the DNS label
         *
         * @param leafDnsLabel the leaf domain label
         * @return tthe next stage of the Ip configuration definition
         */
        DefinitionAttachable<parentT> withNewPublicIpAddress(String leafDnsLabel);

        /**
         * Associates an existing public IP address with the Ip configuration.
         *
         * @param publicIpAddress an existing public IP address
         * @return the next stage of the Ip configuration definition
         */
        DefinitionAttachable<parentT> withExistingPublicIpAddress(PublicIpAddress publicIpAddress);
    }

    /**
     * Attaches the Ip configuration to the parent network interface.
     *
     * @param <parentT> the return type of the final {@link DefinitionAttachable#attach()}
     */
    interface DefinitionAttachable<parentT> extends DefinitionWithPublicIpAddress<parentT> {
        /**
         * Apply the Ip configuration.
         *
         * @return the next stage of the network interface definition
         */
        parentT attach();
    }

    /**
     * The template for a ip configuration update operation, containing all the settings that
     * can be modified.
     *
     * @param <parentT> the return type of the final {@link Appliable#apply()}
     */
    interface Update<parentT> extends
            Appliable<parentT> {
        /**
         * Associate a subnet with the Ip configuration.
         *
         * @param name the subnet name
         * @return the next stage of the Ip configuration update
         */
        Update<parentT> withSubnet(String name);

        /**
         * Enables dynamic private IP address allocation within the specified existing virtual network
         * subnet for Ip configuration.
         *
         * @return the next stage of the Ip configuration update
         */
        Update<parentT> withPrivateIpAddressDynamic();

        /**
         * Assigns the specified static private IP address within the specified existing virtual network
         * subnet to Ip configuration.
         *
         * @param staticPrivateIpAddress the static IP address within the specified subnet to assign to
         *                               the  Ip configuration
         * @return the next stage of the Ip configuration update
         */
        Update<parentT> withPrivateIpAddressStatic(String staticPrivateIpAddress);

        /**
         * Create a new public IP address to associate the Ip configuration with, based on the provided definition.
         *
         * <p>
         * if there is public IP associated with the primary Ip configuration then that will be removed in
         * favour of this
         *
         * @param creatable a creatable definition for a new public IP
         * @return the next stage of the Ip configuration update
         */
        Update<parentT> withNewPublicIpAddress(PublicIpAddress.DefinitionCreatable creatable);

        /**
         * Creates a new public IP address in the same region and group as the resource and associate it
         * with the Ip configuration.
         * <p>
         * the internal name and DNS label for the public IP address will be derived from the network interface name,
         * if there is an existing public IP association then that will be removed in favour of this
         *
         * @return the next stage of the Ip configuration update
         */
        Update<parentT> withNewPublicIpAddress();

        /**
         * Creates a new public IP address in the same region and group as the resource, with the specified DNS label
         * and associate it with the Ip configuration.
         * <p>
         * the internal name for the public IP address will be derived from the DNS label, if there is an existing
         * public IP association then that will be removed in favour of this
         *
         * @param leafDnsLabel the leaf domain label
         * @return the next stage of the Ip configuration update
         */
        Update<parentT> withNewPublicIpAddress(String leafDnsLabel);

        /**
         * Specifies that remove any public IP associated with the Ip configuration.
         *
         * @return the next stage of the Ip configuration update
         */
        Update<parentT> withoutPublicIpAddress();
    }
}
