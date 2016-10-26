/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.network.implementation.ApplicationGatewayInner;
import com.microsoft.azure.management.network.model.HasPublicIpAddress;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.management.resources.fluentcore.model.Appliable;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Updatable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;

/**
 * Entry point for application gateway management API in Azure.
 */
@Fluent
public interface ApplicationGateway extends
        GroupableResource,
        Refreshable<ApplicationGateway>,
        Wrapper<ApplicationGatewayInner>,
        Updatable<ApplicationGateway.Update> {

    // Getters

    /**
     * @return the SKU of this application gateway
     */
    ApplicationGatewaySku sku();

    /**
     * @return the operational state of the application gateway
     */
    ApplicationGatewayOperationalState operationalState();

    /**
     * @return the SSL policy for the application gateway
     */
    ApplicationGatewaySslPolicy sslPolicy();

    /**
     * The entirety of the application gateway definition.
     */
    interface Definition extends
        DefinitionStages.Blank,
        DefinitionStages.WithGroup,
        DefinitionStages.WithCreate,
        DefinitionStages.WithSku,
        DefinitionStages.WithContainingSubnet,
        DefinitionStages.WithFrontendSubnet,
        DefinitionStages.WithPrivateFrontend,
        DefinitionStages.WithPrivateFrontendOrPort,
        DefinitionStages.WithPublicFrontend,
        DefinitionStages.WithPublicFrontendOrPort,
        DefinitionStages.WithFrontendPort,
        DefinitionStages.WithFrontend,
        DefinitionStages.WithFrontendPortOrBackend,
        DefinitionStages.WithBackend,
        DefinitionStages.WithBackendOrHttpConfig,
        DefinitionStages.WithBackendHttpConfig,
        DefinitionStages.WithBackendHttpConfigOrListener,
        DefinitionStages.WithHttpListener,
        DefinitionStages.WithHttpListenerOrRequestRoutingRule,
        DefinitionStages.WithRequestRoutingRule,
        DefinitionStages.WithRequestRoutingRuleOrCreate {
    }

    /**
     * Grouping of application gateway definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of an application gateway definition.
         */
        interface Blank
            extends GroupableResource.DefinitionWithRegion<WithGroup> {
        }

        /**
         * The stage of an application gateway definition allowing to specify the resource group.
         */
        interface WithGroup
            extends GroupableResource.DefinitionStages.WithGroup<WithSku> {
        }

        /**
         * The stage of an application gateway definition describing the nature of the frontend : internal or Internet-facing.
         */
        interface WithFrontend extends
            WithPublicIpAddress<WithPublicFrontendOrPort>,
            WithPublicFrontend,
            WithPrivateFrontend {
        }

        /**
         * The stage of an application gateway definition allowing to add a public IP address as the default public frontend.
         * @param <ReturnT> the next stage of the definition
         */
        interface WithPublicIpAddress<ReturnT> extends HasPublicIpAddress.DefinitionStages.WithPublicIpAddress<ReturnT> {
        }

        /**
         * The stage of an application gateway definition allowing to specify an existing subnet as the private frontend.
         */
        interface WithFrontendSubnet {
            /**
             * Assigns the specified subnet from the selected network as the default private frontend of this application gateway,
             * thereby making the application gateway internal.
             * @param network an existing virtual network
             * @param subnetName the name of an existing subnet on the specified network
             * @return the next stage of the definition
             */
            WithPrivateFrontendOrPort withFrontendSubnet(Network network, String subnetName);
        }

        /**
         * The stage of an internal application gateway definition allowing to define one or more private frontends.
         */
        interface WithPrivateFrontend extends WithFrontendSubnet {
            ApplicationGatewayPrivateFrontend.DefinitionStages.Blank<WithPrivateFrontendOrPort> definePrivateFrontend(String name);
        }

        /**
         * The stage of an internal application gateway definition allowing to specify another private frontend or start specifying a frontend port.
         */
        interface WithPrivateFrontendOrPort extends WithPrivateFrontend, WithFrontendPort {
        }

        /**
         * The stage of an Internet-facing application gateway definition allowing to add additional public frontends
         * or add the first frontend port.
         */
        interface WithPublicFrontendOrPort extends WithPublicFrontend, WithFrontendPort {
        }

        /**
         * The stage of an Internet-facing application gateway definition allowing to define one or more public frontends.
         */
        interface WithPublicFrontend {
            /**
             * Begins the definition of a new public frontend.
             * <p>
             * The definition must be completed with a call to {@link DefinitionStages.WithAttach#attach()}
             * @param name the name for the frontend
             * @return the first stage of the new frontend definition
             */
            ApplicationGatewayPublicFrontend.DefinitionStages.Blank<WithPublicFrontendOrPort> definePublicFrontend(String name);
        }

        /**
         * The stage of an application gateway definition allowing to add a frontend port.
         */
        interface WithFrontendPort {
            /**
             * Creates a port with an autogenerated name.
             * @param portNumber a port number
             * @return the next stage of the definition
             */
            WithFrontendPortOrBackend withFrontendPort(int portNumber);

            /**
             * Creates a port.
             * @param portNumber a port number
             * @param name the name to assign to the port
             * @return the next stage of the definition
             */
            WithFrontendPortOrBackend withFrontendPort(int portNumber, String name);
        }

        /**
         * The stage of an application gateway definition allowing to add a backend or continue adding frontend ports.
         */
        interface WithFrontendPortOrBackend extends WithFrontendPort, WithBackend {
        }

        /**
         * The stage of an application gateway definition allowing to add a backend.
         */
        interface WithBackend {
            /**
             * Begins the definition of a new application gateway backend to be attached to the gateway.
             * @param name a unique name for the backend
             * @return the first stage of the backend definition
             */
            ApplicationGatewayBackend.DefinitionStages.Blank<WithBackendOrHttpConfig> defineBackend(String name);
        }

        /**
         * The stage of an application gateway definition allowing to continue adding more backends
         * or start defining backend HTTP configurations.
         */
        interface WithBackendOrHttpConfig extends WithBackend, WithBackendHttpConfig {
        }

        /**
         * The stage of an application gateway definition allowing to add a backend HTTP configuration.
         */
        interface WithBackendHttpConfig {
            /**
             * Begins the definition of a new application gateway backend HTTP configuration to be attached to the gateway.
             * @param name a unique name for the backend HTTP configuration
             * @return the first stage of the backend HTTP configuration definition
             */
            ApplicationGatewayBackendHttpConfiguration.DefinitionStages.Blank<WithBackendHttpConfigOrListener> defineBackendHttpConfiguration(String name);
        }

        /**
         * The stage of an application gateway definition allowing to continue adding more backend
         * HTTP configurations or start adding HTTP listeners.
         */
        interface WithBackendHttpConfigOrListener extends WithBackendHttpConfig, WithHttpListener {
        }

        /**
         * The stage of an application gateway definition allowing to add an HTTP listener.
         */
        interface WithHttpListener {
            /**
             * Begins the definition of a new application gateway HTTP listener to be attached to the gateway.
             * @param name a unique name for the HTTP listener
             * @return the first stage of the HTTP listener definition
             */
            ApplicationGatewayHttpListener.DefinitionStages.Blank<WithHttpListenerOrRequestRoutingRule> defineHttpListener(String name);
        }

        /**
         * The stage of an application gateway definition allowing to continue adding more HTTP listeners,
         * or start specifying request routing rules.
         */
        interface WithHttpListenerOrRequestRoutingRule extends WithHttpListener, WithRequestRoutingRule {
        }

        /**
         * The stage of an application gateway definition allowing to add a request routing rule.
         */
        interface WithRequestRoutingRule {
            /**
             * Begins the definition of a new application gateway request routing rule to be attached to the gateway.
             * @param name a unique name for the request routing rule
             * @return the first stage of the request routing rule
             */
            ApplicationGatewayRequestRoutingRule.DefinitionStages.Blank<WithRequestRoutingRuleOrCreate> defineRequestRoutingRule(String name);
        }

        /**
         * The stage of an application gateway definition allowing to continue adding more request routing rules,
         * or start specifying optional settings, or create the resource.
         */
        interface WithRequestRoutingRuleOrCreate extends WithRequestRoutingRule, WithCreate {
        }

        /**
         * The stage of an application gateway definition allowing to specify the SKU.
         */
        interface WithSku {
            /**
             * Specifies the SKU of the application gateway to create.
             * @param skuName an application gateway SKU name
             * @param capacity the capacity of the SKU, between 1 and 10
             * @return the next stage of the definition
             */
            WithContainingSubnet withSku(ApplicationGatewaySkuName skuName, int capacity);
        }

        /**
         * The stage of an application gateway definition allowing to specify the subnet the app gateway is getting
         * its private IP address from.
         */
        interface WithContainingSubnet {
            /**
             * Specifies the default subnet the application gateway gets its private IP address from.
             * <p>
             * This will create an IP configuration named "default".
             * @param subnet an existing subnet
             * @return the next stage of the definition
             */
            WithFrontend withContainingSubnet(Subnet subnet);

            /**
             * Specifies the default subnet the application gateway gets its private IP address from.
             * <p>
             * This will create an IP configuration named "default".
             * @param network the virtual network the subnet is part of
             * @param subnetName the name of a subnet within the selected network
             * @return the next stage of the definition
             */
            WithFrontend withContainingSubnet(Network network, String subnetName);

            /**
             * Specifies the default subnet the application gateway gets its private IP address from.
             * <p>
             * This will create an IP configuration named "default".
             * @param networkResourceId the resource ID of the virtual network the subnet is part of
             * @param subnetName the name of a subnet within the selected network
             * @return the next stage of the definition
             */
            WithFrontend withContainingSubnet(String networkResourceId, String subnetName);

            /**
             * Begins the definition of a new IP configuration to add to this application gateway.
             * @param name a name to assign to the IP configuration
             * @return the first stage of the IP configuration definition
             */
            ApplicationGatewayIpConfiguration.DefinitionStages.Blank<WithFrontend> defineIpConfiguration(String name);
        }

        /**
         * The stage of an application gateway definition containing all the required inputs for
         * the resource to be created (via {@link WithCreate#create()}), but also allowing
         * for any other optional settings to be specified.
         */
        interface WithCreate extends
            Creatable<ApplicationGateway>,
            Resource.DefinitionWithTags<WithCreate> {
        }
    }

    /**
     * Grouping of application gateway update stages.
     */
    interface UpdateStages {
        /**
         * The stage of an application gateway definition allowing to modify the SKU.
         */
        interface WithSku {
            /**
             * Specifies the SKU of the application gateway.
             * @param skuName an application gateway SKU name
             * @param capacity the capacity of the SKU, between 1 and 10
             * @return the next stage of the update
             */
            Update withSku(ApplicationGatewaySkuName skuName, int capacity);
        }
    }

    /**
     * The template for an application gateway update operation, containing all the settings that
     * can be modified.
     * <p>
     * Call {@link Update#apply()} to apply the changes to the resource in Azure.
     */
    interface Update extends
        Appliable<ApplicationGateway>,
        Resource.UpdateWithTags<Update> {
    }
}
