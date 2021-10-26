// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.network.fluent.models.ApplicationGatewayUrlPathMapInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.ChildResource;
import com.azure.resourcemanager.resources.fluentcore.model.Attachable;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;
import com.azure.resourcemanager.resources.fluentcore.model.Settable;
import java.util.Map;

/** A client-side representation of an application gateway's URL path map. */
@Fluent
public interface ApplicationGatewayUrlPathMap
    extends HasInnerModel<ApplicationGatewayUrlPathMapInner>, ChildResource<ApplicationGateway> {

    /** @return default backend address pool */
    ApplicationGatewayBackend defaultBackend();

    /** @return default backend HTTP settings configuration */
    ApplicationGatewayBackendHttpConfiguration defaultBackendHttpConfiguration();

    /** @return default redirect configuration */
    ApplicationGatewayRedirectConfiguration defaultRedirectConfiguration();

    /** @return path rules of URL path map resource */
    Map<String, ApplicationGatewayPathRule> pathRules();

    /** Grouping of application gateway URL path map definition stages. */
    interface DefinitionStages {
        /**
         * The first stage of an application gateway URL path map definition.
         *
         * @param <ReturnT> the stage of the parent application gateway definition to return to after attaching this
         *     definition
         */
        interface Blank<ReturnT> extends WithListener<ReturnT> {
        }

        /**
         * The stage of an application gateway URL path map definition allowing to specify an existing listener to
         * associate the URL path map with.
         *
         * @param <ParentT> the stage of the application gateway definition to return to after attaching this definition
         */
        interface WithListener<ParentT> {
            /**
             * Associates the application gateway URL path map with a frontend listener.
             *
             * <p>If the listener with the specified name does not yet exist, it must be defined separately in the
             * optional stages of the application gateway definition. This only adds a reference to the listener by its
             * name.
             *
             * <p>Also, note that a given listener can be used by no more than one request routing rule at a time.
             *
             * @param name the name of a listener to reference
             * @return the next stage of the definition
             */
            WithBackendHttpConfiguration<ParentT> fromListener(String name);
        }

        /**
         * The stage of an application gateway URL path map definition allowing to specify the backend HTTP settings
         * configuration to associate the routing rule with.
         *
         * @param <ParentT> the stage of the application gateway definition to return to after attaching this definition
         */
        interface WithBackendHttpConfiguration<ParentT> {
            /**
             * Associates the specified backend HTTP settings configuration with this application gateway URL path map.
             *
             * <p>If the backend configuration does not exist yet, it must be defined in the optional part of the
             * application gateway definition. The request routing rule references it only by name.
             *
             * @param name the name of a backend HTTP settings configuration
             * @return the next stage of the definition
             */
            WithBackend<ParentT> toBackendHttpConfiguration(String name);

            /**
             * Creates a backend HTTP settings configuration for the specified backend port and the HTTP protocol, and
             * associates it with this URL path map.
             *
             * <p>An auto-generated name will be used for this newly created configuration.
             *
             * @param portNumber the port number for a new backend HTTP settings configuration
             * @return the next stage of the definition
             */
            WithBackend<ParentT> toBackendHttpPort(int portNumber);
        }

        /**
         * The stage of an application gateway URL path map definition allowing to specify the backend to associate the
         * URL path map with.
         *
         * @param <ParentT> the stage of the application gateway definition to return to after attaching this definition
         */
        interface WithBackend<ParentT> {
            /**
             * Associates the URL path map with a backend on this application gateway.
             *
             * <p>If the backend does not yet exist, it will be automatically created.
             *
             * @param name the name of an existing backend
             * @return the next stage of the definition
             */
            WithPathRule<ParentT> toBackend(String name);
        }

        /**
         * The stage of an application gateway URL path map definition allowing to associate the URL path map with a
         * redirect configuration.
         *
         * @param <ParentT> the stage of the application gateway definition to return to after attaching this definition
         */
        interface WithRedirectConfig<ParentT> {
            /**
             * Associates the specified redirect configuration with this URL path map.
             *
             * @param name the name of a redirect configuration on this application gateway
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withRedirectConfiguration(String name);
        }

        /**
         * The stage of an application gateway URL path map definition allowing to specify path rules.
         *
         * @param <ParentT> the stage of the application gateway definition to return to after attaching this definition
         */
        interface WithPathRule<ParentT> {
            /**
             * Begins the definition of path rule.
             *
             * @param name name of the path rule
             * @return next stage of the path rule definition
             */
            ApplicationGatewayPathRule.DefinitionStages.Blank<WithAttach<ParentT>> definePathRule(String name);
        }

        /**
         * The final stage of an application gateway URL path map definition.
         *
         * <p>At this stage, any remaining optional settings can be specified, or the definition can be attached to the
         * parent application gateway definition.
         *
         * @param <ReturnT> the stage of the parent application gateway definition to return to after attaching this
         *     definition
         */
        interface WithAttach<ReturnT>
            extends Attachable.InDefinition<ReturnT>, WithPathRule<ReturnT>, WithRedirectConfig<ReturnT> {
        }
    }

    /**
     * The entirety of an application gateway URL path map definition.
     *
     * @param <ReturnT> the stage of the parent application gateway definition to return to after attaching this
     *     definition
     */
    interface Definition<ReturnT>
        extends DefinitionStages.Blank<ReturnT>,
            DefinitionStages.WithBackendHttpConfiguration<ReturnT>,
            DefinitionStages.WithBackend<ReturnT>,
            DefinitionStages.WithPathRule<ReturnT>,
            DefinitionStages.WithAttach<ReturnT> {
    }

    /** The entirety of an application gateway URL path map update as part of an application gateway update. */
    interface Update extends Settable<ApplicationGateway.Update> {
    }

    /**
     * Grouping of application gateway URL path map definition stages applicable as part of an application gateway
     * update.
     */
    interface UpdateDefinitionStages {
        /**
         * The first stage of an application gateway URL path map definition.
         *
         * @param <ReturnT> the stage of the parent application gateway definition to return to after attaching this
         *     definition
         */
        interface Blank<ReturnT> extends WithListener<ReturnT> {
        }

        /**
         * The stage of an application gateway URL path map definition allowing to specify an existing listener to
         * associate the URL path map with.
         *
         * @param <ParentT> the stage of the application gateway definition to return to after attaching this definition
         */
        interface WithListener<ParentT> {
            /**
             * Associates the URL path map with a frontend listener.
             *
             * <p>If the listener with the specified name does not yet exist, it must be defined separately in the
             * optional stages of the application gateway definition. This only adds a reference to the listener by its
             * name.
             *
             * <p>Also, note that a given listener can be used by no more than one request routing rule at a time.
             *
             * @param name the name of a listener to reference
             * @return the next stage of the definition
             */
            WithBackendHttpConfiguration<ParentT> fromListener(String name);
        }

        /**
         * The stage of an application gateway URL path map definition allowing to specify the backend HTTP settings
         * configuration to associate the URL path map with.
         *
         * @param <ParentT> the stage of the application gateway definition to return to after attaching this definition
         */
        interface WithBackendHttpConfiguration<ParentT> {
            /**
             * Associates the specified backend HTTP settings configuration with this URL path map.
             *
             * <p>If the backend configuration does not exist yet, it must be defined in the optional part of the
             * application gateway definition. The URL path map references it only by name.
             *
             * @param name the name of a backend HTTP settings configuration
             * @return the next stage of the definition
             */
            WithBackend<ParentT> toBackendHttpConfiguration(String name);

            /**
             * Creates a backend HTTP settings configuration for the specified backend port and the HTTP protocol, and
             * associates it with this URL path map.
             *
             * <p>An auto-generated name will be used for this newly created configuration.
             *
             * @param portNumber the port number for a new backend HTTP settings configuration
             * @return the next stage of the definition
             */
            WithBackendOrAddress<ParentT> toBackendHttpPort(int portNumber);
        }

        /**
         * The stage of an application gateway URL path map definition allowing to add an address to specify an existing
         * backend to associate with this URL path map or create a new backend with an auto-generated name and addresses
         * to it.
         *
         * @param <ParentT> the stage of the application gateway definition to return to after attaching this definition
         */
        interface WithBackendOrAddress<ParentT> extends WithBackend<ParentT>, WithBackendAddress<ParentT> {
        }

        /**
         * The stage of an application gateway URL path map definition allowing to add an address to the backend used by
         * this URL path map.
         *
         * <p>A new backend will be created if none is associated with this rule yet.
         *
         * @param <ParentT> the stage of the application gateway definition to return to after attaching this definition
         */
        interface WithBackendAddress<ParentT> {
            /**
             * Adds an IP address to the backend associated with this URL path map.
             *
             * <p>If no backend has been associated with this URL path map yet, a new one will be created with an
             * auto-generated name.
             *
             * <p>This call can be used in a sequence to add multiple IP addresses.
             *
             * @param ipAddress an IP address
             * @return the next stage of the definition
             */
            WithBackendAddressOrPath<ParentT> toBackendIPAddress(String ipAddress);

            /**
             * Adds the specified IP addresses to the backend associated with this URL path map.
             *
             * @param ipAddresses IP addresses to add
             * @return the next stage of the definition
             */
            WithBackendAddressOrPath<ParentT> toBackendIPAddresses(String... ipAddresses);

            /**
             * Adds an FQDN (fully qualified domain name) to the backend associated with this URL path map.
             *
             * <p>If no backend has been associated with this URL path map yet, a new one will be created with an
             * auto-generated name.
             *
             * <p>This call can be used in a sequence to add multiple FQDNs.
             *
             * @param fqdn a fully qualified domain name
             * @return the next stage of the definition
             */
            WithBackendAddressOrPath<ParentT> toBackendFqdn(String fqdn);
        }

        /**
         * The stage of an application gateway URL path map definition allowing to add more backend addresses, start
         * specifying optional settings, or finish the definition by attaching it to the parent application gateway.
         *
         * @param <ParentT> the stage of the application gateway definition to return to after attaching this definition
         */
        interface WithBackendAddressOrPath<ParentT> extends WithBackendAddress<ParentT>, WithPathRule<ParentT> {
        }

        /**
         * The stage of an application gateway URL path map definition allowing to specify the backend to associate the
         * URL path map with.
         *
         * @param <ParentT> the stage of the application gateway definition to return to after attaching this definition
         */
        interface WithBackend<ParentT> {
            /**
             * Associates the URL path map with a backend on this application gateway.
             *
             * <p>If the backend does not yet exist, it will be automatically created.
             *
             * @param name the name of an existing backend
             * @return the next stage of the definition
             */
            WithPathRule<ParentT> toBackend(String name);
        }

        interface WithPathRule<ParentT> {
            ApplicationGatewayPathRule.UpdateDefinitionStages.Blank<WithAttach<ParentT>> definePathRule(String name);
        }

        /**
         * The stage of an application gateway URL path map definition allowing to associate the URL path map with a
         * redirect configuration.
         *
         * @param <ParentT> the stage of the application gateway definition to return to after attaching this definition
         */
        interface WithRedirectConfig<ParentT> {
            /**
             * Associates the specified redirect configuration with this request routing rule.
             *
             * @param name the name of a redirect configuration on this application gateway
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withRedirectConfiguration(String name);
        }

        /**
         * The final stage of an application gateway URL path map definition.
         *
         * <p>At this stage, any remaining optional settings can be specified, or the definition can be attached to the
         * parent application gateway definition.
         *
         * @param <ReturnT> the stage of the parent application gateway definition to return to after attaching this
         *     definition
         */
        interface WithAttach<ReturnT>
            extends Attachable.InDefinitionAlt<ReturnT>, WithPathRule<ReturnT>, WithRedirectConfig<ReturnT> {
        }
    }

    /**
     * The entirety of an application gateway URL path map definition as part of an application gateway update.
     *
     * @param <ReturnT> the stage of the parent application gateway definition to return to after attaching this
     *     definition
     */
    interface UpdateDefinition<ReturnT>
        extends UpdateDefinitionStages.Blank<ReturnT>,
            UpdateDefinitionStages.WithBackendOrAddress<ReturnT>,
            UpdateDefinitionStages.WithBackendHttpConfiguration<ReturnT>,
            UpdateDefinitionStages.WithBackendAddressOrPath<ReturnT>,
            UpdateDefinitionStages.WithPathRule<ReturnT>,
            UpdateDefinitionStages.WithAttach<ReturnT> {
    }
}
