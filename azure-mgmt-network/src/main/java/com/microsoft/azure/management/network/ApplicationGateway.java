/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network;

import java.util.Map;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.apigeneration.Method;
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
     * @return backend address pools of this application gateway, indexed by name
     */
    Map<String, ApplicationGatewayBackend> backends();

    /**
     * @return frontend IP configurations of this application gateway, indexed by name
     */
    Map<String, ApplicationGatewayFrontend> frontends();

    /**
     * The entirety of the application gateway definition.
     */
    interface Definition extends
        DefinitionStages.Blank,
        DefinitionStages.WithGroup,
        DefinitionStages.WithCreate,
        DefinitionStages.WithSku,
        DefinitionStages.WithContainingSubnet,
        DefinitionStages.WithPrivateFrontend,
        DefinitionStages.WithPrivateFrontendOptional,
        DefinitionStages.WithPublicFrontend,
        DefinitionStages.WithFrontendPort,
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
         * The stage of an application gateway definition allowing to add a public IP address as the default public frontend.
         * @param <ReturnT> the next stage of the definition
         */
        interface WithPublicIpAddress<ReturnT> extends HasPublicIpAddress.DefinitionStages.WithPublicIpAddress<ReturnT> {
        }

        /**
         * The stage of an internal application gateway definition allowing to define a private frontend.
         */
        interface WithPrivateFrontend {
            /**
             * Begins the definition of a private, or internal, application gateway frontend IP configuration.
             * @param name the name for the frontend
             * @return the first stage of a private frontend IP configuration definition
             */
            ApplicationGatewayPrivateFrontend.DefinitionStages.Blank<WithFrontendPort> definePrivateFrontend(String name);

            /**
             * Enables a private default frontend in the subnet containing the application gateway.
             * <p>
             * A frontend with the name "default" will be created if needed.
             * @return the next stage of the definition
             */
            WithFrontendPort withPrivateFrontend();

            /**
             * Enables a private frontend in the subnet containing the application gateway.
             * @param frontendName the name for the frontend to create
             * @return the next stage of the definition
             */
            WithFrontendPort withPrivateFrontend(String frontendName);
        }

        /**
         * The stage of an internal application gateway definition allowing to optionally define a private,
         * or internal, frontend IP configuration.
         */
        interface WithPrivateFrontendOptional extends WithPrivateFrontend {
            /**
             * Specifies that no private, or internal, frontend should be enabled.
             * @return the next stage of the definition
             */
            @Method
            WithFrontendPort withoutPrivateFrontend();
        }

        /**
         * The stage of an application gateway definition allowing to define one or more public, or Internet-facing, frontends.
         */
        interface WithPublicFrontend extends WithPublicIpAddress<WithPrivateFrontendOptional> {
            /**
             * Begins the definition of a new public, or Internet-facing, frontend.
             * @param name the name for the frontend
             * @return the first stage of the new frontend definition
             */
            ApplicationGatewayPublicFrontend.DefinitionStages.Blank<WithPrivateFrontendOptional> definePublicFrontend(String name);

            /**
             * Specifies that the application gateway should not be Internet-facing.
             * @return the next stage of the definition
             */
            @Method
            WithPrivateFrontend withoutPublicFrontend();
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

            /**
             * Adds an IP address to the default backend.
             * <p>
             * A backend with the name "default" will be created if needed.
             * @param ipAddress an IP address
             * @return the next stage of the definition
             */
            WithBackendOrHttpConfig withBackendIpAddress(String ipAddress);

            /**
             * Adds an FQDN (fully qualified domain name) to the default backend.
             * <p>
             * A backend with the name "default" will be created if needed.
             * @param fqdn a fully qualified domain name
             * @return the next stage of the definition
             */
            WithBackendOrHttpConfig withBackendFqdn(String fqdn);

            /**
             * Adds an IP address to a backend.
             * @param ipAddress an IP address
             * @param backendName the name for the backend to add the address to
             * @return the next stage of the definition
             */
            WithBackendOrHttpConfig withBackendIpAddress(String ipAddress, String backendName);

            /**
             * Adds an FQDN (fully qualified domain name) to a backend.
             * @param fqdn a fully qualified domain name
             * @param backendName the name for the backend to add the FQDN to
             * @return the next stage of the definition
             */
            WithBackendOrHttpConfig withBackendFqdn(String fqdn, String backendName);
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
            ApplicationGatewayBackendHttpConfiguration.DefinitionStages.Blank<WithBackendHttpConfigOrListener> defineHttpConfiguration(String name);
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
            WithPublicFrontend withContainingSubnet(Subnet subnet);

            /**
             * Specifies the default subnet the application gateway gets its private IP address from.
             * <p>
             * This will create an IP configuration named "default".
             * @param network the virtual network the subnet is part of
             * @param subnetName the name of a subnet within the selected network
             * @return the next stage of the definition
             */
            WithPublicFrontend withContainingSubnet(Network network, String subnetName);

            /**
             * Specifies the default subnet the application gateway gets its private IP address from.
             * <p>
             * This will create an IP configuration named "default".
             * @param networkResourceId the resource ID of the virtual network the subnet is part of
             * @param subnetName the name of a subnet within the selected network
             * @return the next stage of the definition
             */
            WithPublicFrontend withContainingSubnet(String networkResourceId, String subnetName);

            /**
             * Begins the definition of a new IP configuration to add to this application gateway.
             * @param name a name to assign to the IP configuration
             * @return the first stage of the IP configuration definition
             */
            ApplicationGatewayIpConfiguration.DefinitionStages.Blank<WithPublicFrontend> defineIpConfiguration(String name);
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
