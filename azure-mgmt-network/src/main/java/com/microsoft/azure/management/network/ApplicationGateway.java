/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network;

import java.util.Map;

import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.apigeneration.Method;
import com.microsoft.azure.management.network.implementation.ApplicationGatewayInner;
import com.microsoft.azure.management.network.implementation.NetworkManager;
import com.microsoft.azure.management.network.model.HasPrivateIPAddress;
import com.microsoft.azure.management.network.model.HasPublicIPAddress;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasSubnet;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.management.resources.fluentcore.model.Appliable;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Updatable;

import rx.Completable;

/**
 * Entry point for application gateway management API in Azure.
 */
@Fluent
@Beta
public interface ApplicationGateway extends
        GroupableResource<NetworkManager, ApplicationGatewayInner>,
        Refreshable<ApplicationGateway>,
        Updatable<ApplicationGateway.Update>,
        HasSubnet,
        HasPrivateIPAddress {

    // Actions
    /**
     * Starts the application gateway.
     */
    @Method
    void start();

    /**
     * Stops the application gateway.
     */
    @Method
    void stop();

    /**
     * Starts the application gateway asynchronously.
     * @return a representation of the deferred computation of this call
     */
    @Method
    @Beta
    Completable startAsync();

    /**
     * Stops the application gateway asynchronously.
     * @return a representation of the deferred computation of this call
     */
    @Method
    @Beta
    Completable stopAsync();

    // Getters

    /**
     * @return true if the application gateway has at least one internally load balanced frontend accessible within the virtual network
     */
    boolean isPrivate();

    /**
     * @return true if the application gateway has at least one Internet-facing frontend
     */
    boolean isPublic();

    /**
     * @return the frontend IP configuration associated with a public IP address, if any, that frontend listeners and request routing rules can reference implicitly
     */
    ApplicationGatewayFrontend defaultPublicFrontend();

    /**
     * @return the frontend IP configuration associated with a private IP address, if any, that frontend listeners and request routing rules can reference implicitly
     */
    ApplicationGatewayFrontend defaultPrivateFrontend();

    /**
     * @return the SKU of this application gateway
     */
    ApplicationGatewaySku sku();

    /**
     * @return number of instances
     */
    int instanceCount();

    /**
     * @return the size of the application gateway
     */
    ApplicationGatewaySkuName size();

    /**
     * @return the tier of the application gateway
     */
    ApplicationGatewayTier tier();

    /**
     * @return the operational state of the application gateway
     */
    ApplicationGatewayOperationalState operationalState();

    /**
     * @return the SSL policy for the application gateway
     */
    ApplicationGatewaySslPolicy sslPolicy();

    /**
     * @return IP configurations of this application gateway, indexed by name
     */
    Map<String, ApplicationGatewayIPConfiguration> ipConfigurations();

    /**
     * @return backend address pools of this application gateway, indexed by name
     */
    Map<String, ApplicationGatewayBackend> backends();

    /**
     * @return probes of this application gateway, indexed by name
     */
    Map<String, ApplicationGatewayProbe> probes();

    /**
     * @return the IP configuration named "default" if it exists, or the one existing IP configuration if only one exists, else null
     */
    ApplicationGatewayIPConfiguration defaultIPConfiguration();

    /**
     * @return frontend IP configurations, indexed by name
     */
    Map<String, ApplicationGatewayFrontend> frontends();

    /**
     * @return frontend IP configurations with a public IP address, indexed by name
     */
    Map<String, ApplicationGatewayFrontend> publicFrontends();

    /**
     * @return frontend IP configurations with a private IP address on a subnet, indexed by name
     */
    Map<String, ApplicationGatewayFrontend> privateFrontends();

    /**
     * @return named frontend ports of this application gateway, indexed by name
     */
    Map<String, Integer> frontendPorts();

    /**
     * @return backend HTTP configurations of this application gateway, indexed by name
     */
    Map<String, ApplicationGatewayBackendHttpConfiguration> backendHttpConfigurations();

    /**
     * @return SSL certificates, indexed by name
     */
    Map<String, ApplicationGatewaySslCertificate> sslCertificates();

    /**
     * @return Frontend listeners, indexed by name
     */
    Map<String, ApplicationGatewayListener> listeners();

    /**
     * @return request routing rules, indexed by name
     */
    Map<String, ApplicationGatewayRequestRoutingRule> requestRoutingRules();

    /**
     * Returns the name of the existing port, if any, that is associated with the specified port number.
     * @param portNumber a port number
     * @return the existing port name for that port number, or null if none found
     */
    String frontendPortNameFromNumber(int portNumber);

    /**
     * Finds a frontend listener associated with the specified frontend port number, if any.
     * @param portNumber a used port number
     * @return a frontend listener, or null if none found
     */
    ApplicationGatewayListener listenerByPortNumber(int portNumber);

    /**
     * The entirety of the application gateway definition.
     */
    interface Definition extends
        DefinitionStages.Blank,
        DefinitionStages.WithGroup,
        DefinitionStages.WithCreate,
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
            extends GroupableResource.DefinitionStages.WithGroup<WithRequestRoutingRule> {
        }

        /**
         * The stage of an application gateway definition allowing to add a new Internet-facing frontend with a public IP address.
         */
        interface WithPublicIPAddress extends HasPublicIPAddress.DefinitionStages.WithPublicIPAddressNoDnsLabel<WithCreate> {
        }

        /**
         * The stage of an application gateway definition allowing to define one or more public, or Internet-facing, frontends.
         */
        interface WithPublicFrontend extends WithPublicIPAddress {
            /**
             * Specifies that the application gateway should not be Internet-facing.
             * @return the next stage of the definition
             */
            @Method
            WithCreate withoutPublicFrontend();
        }

        /**
         * The stage of an internal application gateway definition allowing to make the application gateway accessible to its
         * virtual network.
         */
        interface WithPrivateFrontend {
            /**
             * Enables a private (internal) default frontend in the subnet containing the application gateway.
             * <p>
             * A frontend with the name "default" will be created if needed.
             * @return the next stage of the definition
             */
            @Method
            WithCreate withPrivateFrontend();

            /**
             * Specifies that no private (internal) frontend should be enabled.
             * @return the next stage of the definition
             */
            @Method
            WithCreate withoutPrivateFrontend();
        }

        /**
         * The stage of an application gateway definition allowing to add a listener.
         */
        interface WithListener {
            /**
             * Begins the definition of a new application gateway listener to be attached to the gateway.
             * @param name a unique name for the listener
             * @return the first stage of the listener definition
             */
            ApplicationGatewayListener.DefinitionStages.Blank<WithCreate> defineListener(String name);
        }

        /**
         * The stage of an application gateway definition allowing to add a probe.
         */
        interface WithProbe {
            /**
             * Begins the definition of a new probe.
             * @param name a unique name for the probe
             * @return the first stage of a probe definition
             */
            ApplicationGatewayProbe.DefinitionStages.Blank<WithCreate> defineProbe(String name);
        }

        /**
         * The stage of an application gateway definition allowing to add a frontend port.
         */
        interface WithFrontendPort {
            /**
             * Creates a frontend port with an auto-generated name and the specified port number, unless one already exists.
             * @param portNumber a port number
             * @return the next stage of the definition
             */
            WithCreate withFrontendPort(int portNumber);

            /**
             * Creates a frontend port with the specified name and port number, unless a port matching this name and/or number already exists.
             * @param portNumber a port number
             * @param name the name to assign to the port
             * @return the next stage of the definition, or null if a port matching either the name or the number, but not both, already exists.
             */
            WithCreate withFrontendPort(int portNumber, String name);
        }

        /**
         * The stage of an application gateway definition allowing to add an SSL certificate to be used by HTTPS listeners.
         */
        interface WithSslCert {
            /**
             * Begins the definition of a new application gateway SSL certificate to be attached to the gateway for use in HTTPS listeners.
             * @param name a unique name for the certificate
             * @return the first stage of the certificate definition
             */
            ApplicationGatewaySslCertificate.DefinitionStages.Blank<WithCreate> defineSslCertificate(String name);
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
            ApplicationGatewayBackend.DefinitionStages.Blank<WithCreate> defineBackend(String name);
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
            ApplicationGatewayBackendHttpConfiguration.DefinitionStages.Blank<WithCreate> defineBackendHttpConfiguration(String name);
        }

        /**
         * The stage of an application gateway definition allowing to add a request routing rule.
         */
        interface WithRequestRoutingRule {
            /**
             * Begins the definition of a request routing rule for this application gateway.
             * @param name a unique name for the request routing rule
             * @return the first stage of the request routing rule
             */
            ApplicationGatewayRequestRoutingRule.DefinitionStages.Blank<WithRequestRoutingRuleOrCreate> defineRequestRoutingRule(String name);
        }

        /**
         * The stage of an application gateway definition allowing to continue adding more request routing rules,
         * or start specifying optional settings, or create the application gateway.
         */
        interface WithRequestRoutingRuleOrCreate extends WithRequestRoutingRule, WithCreate {
        }

        /**
         * The stage of an application gateway update allowing to specify the size.
         */
        interface WithSize {
            /**
             * Specifies the size of the application gateway to create within the context of the selected tier.
             * <p>
             * By default, the smallest size is used.
             * @param size an application gateway SKU name
             * @return the next stage of the definition
             */
             /*
              * The API refers to this as the "SKU"/"SkuName", the docs refer to this as the "size" (and docs call Standard vs WAF as the "SKU"),
              * while the portal refers to this as the "SKU size"... The documentation naming sounds the most correct, so following that here.
              */
            WithCreate withSize(ApplicationGatewaySkuName size);
        }

        /**
         * The stage of an application gateway definition allowing to specify the capacity (number of instances) of the application gateway.
         */
        interface WithInstanceCount {
            /**
             * Specifies the capacity (number of instances) for the application gateway.
             * <p>
             * By default, 1 instance is used.
             * @param instanceCount the capacity as a number between 1 and 10 but also based on the limits imposed by the selected applicatiob gateway size
             * @return the next stage of the definition
             */
            /*
             * The API refers to this as "Capacity", but the portal and the docs refer to this as "instance count", so using that naming here
             */
            WithCreate withInstanceCount(int instanceCount);
        }

        /**
         * The stage of an application gateway definition allowing to specify the subnet the app gateway is getting
         * its private IP address from.
         */
        interface WithExistingSubnet extends HasSubnet.DefinitionStages.WithSubnet<WithCreate> {
            /**
             * Specifies the subnet the application gateway gets its private IP address from.
             * <p>
             * This will create a new IP configuration, if it does not already exist.
             * <p>
             * Private (internal) frontends, if any have been enabled, will be configured to use this subnet as well.
             * @param subnet an existing subnet
             * @return the next stage of the definition
             */
            WithCreate withExistingSubnet(Subnet subnet);

            /**
             * Specifies the subnet the application gateway gets its private IP address from.
             * <p>
             * This will create a new IP configuration, if it does not already exist.
             * <p>
             * Private (internal) frontends, if any have been enabled, will be configured to use this subnet as well.
             * @param network the virtual network the subnet is part of
             * @param subnetName the name of a subnet within the selected network
             * @return the next stage of the definition
             */
            WithCreate withExistingSubnet(Network network, String subnetName);
        }

        /**
         * The stage of an application gateway definition allowing to specify the default IP address the app gateway will be internally available at,
         * if the default private frontend has been enabled.
         */
        interface WithPrivateIPAddress extends HasPrivateIPAddress.DefinitionStages.WithPrivateIPAddress<WithCreate> {
        }

        /**
         * The stage of an application gateway definition containing all the required inputs for
         * the resource to be created (via {@link WithCreate#create()}), but also allowing
         * for any other optional settings to be specified.
         */
        interface WithCreate extends
            Creatable<ApplicationGateway>,
            Resource.DefinitionWithTags<WithCreate>,
            WithSize,
            WithInstanceCount,
            WithSslCert,
            WithFrontendPort,
            WithListener,
            WithBackendHttpConfig,
            WithBackend,
            WithExistingSubnet,
            WithPrivateIPAddress,
            WithPrivateFrontend,
            WithPublicFrontend,
            WithPublicIPAddress,
            WithProbe {
        }
    }

    /**
     * Grouping of application gateway update stages.
     */
    interface UpdateStages {
        /**
         * The stage of an internal application gateway update allowing to make the application gateway accessible to its
         * virtual network.
         */
        interface WithPrivateFrontend {
            /**
             * Enables a private (internal) default frontend in the subnet containing the application gateway.
             * <p>
             * A frontend with the name "default" will be created if needed.
             * @return the next stage of the update
             */
            @Method
            Update withPrivateFrontend();

            /**
             * Specifies that no private, or internal, frontend should be enabled.
             * @return the next stage of the definition
             */
            @Method
            Update withoutPrivateFrontend();
        }

        /**
         * The stage of an application gateway update allowing to specify the subnet the app gateway is getting
         * its private IP address from.
         */
        interface WithExistingSubnet extends HasSubnet.UpdateStages.WithSubnet<Update> {
            /**
             * Specifies the subnet the application gateway gets its private IP address from.
             * <p>
             * This will create a new IP configuration, if it does not already exist.
             * <p>
             * Private (internal) frontends, if any have been enabled, will be configured to use this subnet as well.
             * @param subnet an existing subnet
             * @return the next stage of the update
             */
            Update withExistingSubnet(Subnet subnet);

            /**
             * Specifies the subnet the application gateway gets its private IP address from.
             * <p>
             * This will create a new IP configuration, if it does not already exist.
             * <p>
             * Private (internal) frontends, if any have been enabled, will be configured to use this subnet as well.
             * @param network the virtual network the subnet is part of
             * @param subnetName the name of a subnet within the selected network
             * @return the next stage of the update
             */
            Update withExistingSubnet(Network network, String subnetName);
        }

        /**
         * The stage of an application gateway update allowing to modify IP configurations.
         */
        interface WithIPConfig {
            /**
             * Removes the specified IP configuration.
             * <p>
             * Note that removing an IP configuration referenced by other settings may break the application gateway.
             * Also, there must be at least one IP configuration for the application gateway to function.
             * @param ipConfigurationName the name of the IP configuration to remove
             * @return the next stage of the update
             */
            Update withoutIPConfiguration(String ipConfigurationName);

            /**
             * Begins the update of an existing IP configuration.
             * @param ipConfigurationName the name of an existing IP configuration
             * @return the first stage of an IP configuration update
             */
            ApplicationGatewayIPConfiguration.Update updateIPConfiguration(String ipConfigurationName);

            /**
             * Begins the update of the default IP configuration i.e. the only one IP configuration that exists, assuming only one exists.
             * @return the first stage of an IP configuration update.
             */
            @Method
            ApplicationGatewayIPConfiguration.Update updateDefaultIPConfiguration();

            /**
             * Begins the definition of the default IP configuration.
             * <p>
             * If a default IP configuration already exists, it will be this is equivalent to <code>updateDefaultIPConfiguration()</code>.
             * @return the first stage of an IP configuration update
             */
            @Method
            ApplicationGatewayIPConfiguration.UpdateDefinitionStages.Blank<Update> defineDefaultIPConfiguration();
        }

        /**
         * The stage of an application gateway update allowing to modify frontend ports.
         */
        interface WithFrontendPort {
            /**
             * Creates a frontend port with an auto-generated name and the specified port number, unless one already exists.
             * @param portNumber a port number
             * @return the next stage of the definition
             */
            Update withFrontendPort(int portNumber);

            /**
             * Creates a frontend port with the specified name and port number, unless a port matching this name and/or number already exists.
             * @param portNumber a port number
             * @param name the name to assign to the port
             * @return the next stage of the definition, or null if a port matching either the name or the number, but not both, already exists.
             */
            Update withFrontendPort(int portNumber, String name);

            /**
             * Removes the specified frontend port.
             * <p>
             * Note that removing a frontend port referenced by other settings may break the application gateway.
             * @param name the name of the frontend port to remove
             * @return the next stage of the update
             */
            Update withoutFrontendPort(String name);

            /**
             * Removes the specified frontend port.
             * <p>
             * Note that removing a frontend port referenced by other settings may break the application gateway.
             * @param portNumber the port number of the frontend port to remove
             * @return the next stage of the update
             */
            Update withoutFrontendPort(int portNumber);
        }

        /**
         * The stage of an application gateway update allowing to specify a public IP address for the public frontend.
         */
        interface WithPublicIPAddress extends HasPublicIPAddress.UpdateStages.WithPublicIPAddressNoDnsLabel<Update> {
        }

        /**
         * The stage of an application gateway update allowing to modify frontend IP configurations.
         */
        interface WithFrontend {
            /**
             * Removes the specified frontend IP configuration.
             * <p>
             * Note that removing a frontend referenced by other settings may break the application gateway.
             * @param frontendName the name of the frontend IP configuration to remove
             * @return the next stage of the update
             */
            Update withoutFrontend(String frontendName);

            /**
             * Begins the update of an existing frontend IP configuration.
             * @param frontendName the name of an existing frontend IP configuration
             * @return the first stage of the frontend IP configuration update
             */
            ApplicationGatewayFrontend.Update updateFrontend(String frontendName);

            /**
             * Specifies that the application gateway should not be Internet-facing.
             * <p>
             * Note that if there are any other settings referencing the public frontend, removing it may break the application gateway.
             * @return the next stage of the update
             */
            @Method
            Update withoutPublicFrontend();

            /**
             * Specifies that the application gateway should not be private, i.e. its endponts should not be internally accessible
             * from within the virtual network.
             * <p>
             * Note that if there are any other settings referencing the private frontend, removing it may break the application gateway.
             * @return the next stage of the update
             */
            @Method
            Update withoutPrivateFrontend();

            /**
             * Begins the update of the public frontend IP configuration, if it exists.
             * @return the first stage of a frontend update or null if no public frontend exists
             */
            @Method
            ApplicationGatewayFrontend.Update updatePublicFrontend();

            /**
             * Begins the update of the private frontend IP configuration, if it exists.
             * @return the first stage of a frontend update or null if no private frontend exists
             */
            /* TODO: Nothing to update in the private frontend today - changing Subnet and/or private IP not supported
             * @Method
             * ApplicationGatewayFrontend.Update updatePrivateFrontend();
             */

            /**
             * Begins the definition of the default public frontend IP configuration, creating one if it does not already exist.
             * @return the first stage of a frontend definition
             */
            @Method
            ApplicationGatewayFrontend.UpdateDefinitionStages.Blank<Update> definePublicFrontend();

            /**
             * Begins the definition of the default private frontend IP configuration, creating one if it does not already exist.
             * @return the first stage of a frontend definition
             */
            @Method
            ApplicationGatewayFrontend.UpdateDefinitionStages.Blank<Update> definePrivateFrontend();
        }

        /**
         * The stage of an application gateway update allowing to modify backends.
         */
        interface WithBackend {
            /**
             * Begins the definition of a new application gateway backend to be attached to the gateway.
             * @param name a unique name for the backend
             * @return the first stage of the backend definition
             */
            ApplicationGatewayBackend.UpdateDefinitionStages.Blank<Update> defineBackend(String name);

            /**
             * Ensures the specified fully qualified domain name (FQDN) is not associated with any backend.
             * @param fqdn a fully qualified domain name (FQDN)
             * @return the next stage of the update
             */
            Update withoutBackendFqdn(String fqdn);

            /**
             * Ensures the specified IP address is not associated with any backend.
             * @param ipAddress an IP address
             * @return the next stage of the update
             */
            Update withoutBackendIPAddress(String ipAddress);

            /**
             * Removes the specified backend.
             * <p>
             * Note that removing a backend referenced by other settings may break the application gateway.
             * @param backendName the name of an existing backend on this application gateway
             * @return the next stage of the update
             */
            Update withoutBackend(String backendName);

            /**
             * Begins the update of an existing backend on this application gateway.
             * @param name the name of the backend
             * @return the first stage of an update of the backend
             */
            ApplicationGatewayBackend.Update updateBackend(String name);
        }

        /**
         * The stage of an application gateway update allowing to modify probes.
         */
        interface WithProbe {
            /**
             * Begins the definition of a new probe.
             * @param name a unique name for the probe
             * @return the first stage of a probe definition
             */
            ApplicationGatewayProbe.UpdateDefinitionStages.Blank<Update> defineProbe(String name);

            /**
             * Begins the update of an existing probe.
             * @param name the name of an existing probe
             * @return the first stage of a probe update
             */
            ApplicationGatewayProbe.Update updateProbe(String name);

            /**
             * Removes a probe from the application gateway.
             * @param name the name of an existing probe
             * @return the next stage of the update
             */
            Update withoutProbe(String name);
        }

        /**
         * The stage of an application gateway update allowing to specify the size.
         */
        interface WithSize {
            /**
             * Specifies the size of the application gateway to use within the context of the selected tier.
             * @param size an application gateway size name
             * @return the next stage of the update
             */
            Update withSize(ApplicationGatewaySkuName size);
        }

        /**
         * The stage of an application gateway update allowing to specify the capacity (number of instances) of
         * the application gateway.
         */
        interface WithInstanceCount {
            /**
             * Specifies the capacity (number of instances) for the application gateway.
             * @param instanceCount the capacity as a number between 1 and 10 but also based on the limits imposed by the selected applicatiob gateway size
             * @return the next stage of the update
             */
            Update withInstanceCount(int instanceCount);
        }

        /**
         * The stage of an application gateway update allowing to modify SSL certificates.
         */
        interface WithSslCert {
            /**
             * Begins the definition of a new application gateway SSL certificate to be attached to the gateway for use in frontend HTTPS listeners.
             * @param name a unique name for the certificate
             * @return the first stage of the certificate definition
             */
            ApplicationGatewaySslCertificate.UpdateDefinitionStages.Blank<Update> defineSslCertificate(String name);

            /**
             * Removes the specified SSL certificate from the application gateway.
             * <p>
             * Note that removing a certificate referenced by other settings may break the application gateway.
             * @param name the name of the certificate to remove
             * @return the next stage of the update
             */
            Update withoutCertificate(String name);
        }

        /**
         * The stage of an application gateway update allowing to modify frontend listeners.
         */
        interface WithListener {
            /**
             * Begins the definition of a new application gateway listener to be attached to the gateway.
             * @param name a unique name for the listener
             * @return the first stage of the listener definition
             */
            ApplicationGatewayListener.UpdateDefinitionStages.Blank<Update> defineListener(String name);

            /**
             * Removes a frontend listener from the application gateway.
             * <p>
             * Note that removing a listener referenced by other settings may break the application gateway.
             * @param name the name of the listener to remove
             * @return the next stage of the update
             */
            Update withoutListener(String name);

            /**
             * Begins the update of a listener.
             * @param name the name of an existing listener to update
             * @return the next stage of the definition or null if the requested listener does not exist
             */
            ApplicationGatewayListener.Update updateListener(String name);
        }

        /**
         * The stage of an application gateway update allowing to modify backend HTTP configurations.
         */
        interface WithBackendHttpConfig {
            /**
             * Begins the definition of a new application gateway backend HTTP configuration to be attached to the gateway.
             * @param name a unique name for the backend HTTP configuration
             * @return the first stage of the backend HTTP configuration definition
             */
            ApplicationGatewayBackendHttpConfiguration.UpdateDefinitionStages.Blank<Update> defineBackendHttpConfiguration(String name);

            /**
             * Removes the specified backend HTTP configuration from this application gateway.
             * <p>
             * Note that removing a backend HTTP configuration referenced by other settings may break the application gateway.
             * @param name the name of an existing backend HTTP configuration on this application gateway
             * @return the next stage of the update
             */
            Update withoutBackendHttpConfiguration(String name);

            /**
             * Begins the update of a backend HTTP configuration.
             * @param name the name of an existing backend HTTP configuration on this application gateway
             * @return the next stage of the update
             */
            ApplicationGatewayBackendHttpConfiguration.Update updateBackendHttpConfiguration(String name);
        }

        /**
         * The stage of an application gateway update allowing to modify request routing rules.
         */
        interface WithRequestRoutingRule {
            /**
             * Begins the definition of a request routing rule for this application gateway.
             * @param name a unique name for the request routing rule
             * @return the first stage of the request routing rule
             */
            ApplicationGatewayRequestRoutingRule.UpdateDefinitionStages.Blank<Update> defineRequestRoutingRule(String name);

            /**
             * Removes a request routing rule from the application gateway.
             * @param name the name of the request routing rule to remove
             * @return the next stage of the update
             */
            Update withoutRequestRoutingRule(String name);

            /**
             * Begins the update of a request routing rule.
             * @param name the name of an existing request routing rule
             * @return the first stage of a request routing rule update or null if the requested rule does not exist
             */
            ApplicationGatewayRequestRoutingRule.Update updateRequestRoutingRule(String name);
        }
    }

    /**
     * The template for an application gateway update operation, containing all the settings that
     * can be modified.
     * <p>
     * Call {@code apply()} to apply the changes to the resource in Azure.
     */
    interface Update extends
        Appliable<ApplicationGateway>,
        Resource.UpdateWithTags<Update>,
        UpdateStages.WithSize,
        UpdateStages.WithInstanceCount,
        UpdateStages.WithBackend,
        UpdateStages.WithBackendHttpConfig,
        UpdateStages.WithIPConfig,
        UpdateStages.WithFrontend,
        UpdateStages.WithPublicIPAddress,
        UpdateStages.WithFrontendPort,
        UpdateStages.WithSslCert,
        UpdateStages.WithListener,
        UpdateStages.WithRequestRoutingRule,
        UpdateStages.WithExistingSubnet,
        UpdateStages.WithProbe {
    }
}
