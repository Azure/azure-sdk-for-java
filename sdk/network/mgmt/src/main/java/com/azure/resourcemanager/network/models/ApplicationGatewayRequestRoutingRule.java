// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.network.fluent.inner.ApplicationGatewayRequestRoutingRuleInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.ChildResource;
import com.azure.resourcemanager.resources.fluentcore.model.Attachable;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;
import com.azure.resourcemanager.resources.fluentcore.model.Settable;

import java.util.Collection;

/** A client-side representation of an application gateway request routing rule. */
@Fluent()
public interface ApplicationGatewayRequestRoutingRule
    extends HasInner<ApplicationGatewayRequestRoutingRuleInner>,
        ChildResource<ApplicationGateway>,
        HasPublicIpAddress,
        HasSslCertificate<ApplicationGatewaySslCertificate>,
        HasFrontendPort,
        HasBackendPort,
        HasHostname,
        HasCookieBasedAffinity,
        HasServerNameIndication {

    /** @return the redirect configuration associated with this request routing rule, if any */
    ApplicationGatewayRedirectConfiguration redirectConfiguration();

    /** @return the frontend protocol */
    ApplicationGatewayProtocol frontendProtocol();

    /** @return rule type */
    ApplicationGatewayRequestRoutingRuleType ruleType();

    /** @return the associated backend address pool */
    ApplicationGatewayBackend backend();

    /** @return the associated backend HTTP settings configuration */
    ApplicationGatewayBackendHttpConfiguration backendHttpConfiguration();

    /** @return the associated frontend HTTP listener */
    ApplicationGatewayListener listener();

    /** @return the addresses assigned to the associated backend */
    Collection<ApplicationGatewayBackendAddress> backendAddresses();

    /** @return the associated URL path map */
    ApplicationGatewayUrlPathMap urlPathMap();

    /** Grouping of application gateway request routing rule definition stages. */
    interface DefinitionStages {
        /**
         * The first stage of an application gateway request routing rule definition.
         *
         * @param <ParentT> the stage of the application gateway definition to return to after attaching this definition
         */
        interface Blank<ParentT> extends WithListenerOrFrontend<ParentT> {
        }

        /**
         * The final stage of an application gateway request routing rule definition.
         *
         * <p>At this stage, any remaining optional settings can be specified, or the definition can be attached to the
         * parent application gateway definition.
         *
         * @param <ParentT> the stage of the application gateway definition to return to after attaching this definition
         */
        interface WithAttach<ParentT>
            extends Attachable.InDefinition<ParentT>,
                WithHostname<ParentT>,
                WithCookieBasedAffinity<ParentT>,
                WithUrlPathMap<ParentT> {
        }

        /**
         * The stage of an application gateway request routing rule definition allowing to specify an existing listener
         * to associate the routing rule with.
         *
         * @param <ParentT> the stage of the application gateway definition to return to after attaching this definition
         */
        interface WithListener<ParentT> {
            /**
             * Associates the request routing rule with a frontend listener.
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
            WithBackendHttpConfigOrRedirect<ParentT> fromListener(String name);
        }

        /**
         * The stage of an application gateway request routing rule definition allowing to associate an existing
         * listener with the rule, or create a new one implicitly by specifying the frontend to listen to.
         *
         * @param <ParentT> the stage of the application gateway definition to return to after attaching this definition
         */
        interface WithListenerOrFrontend<ParentT> extends WithListener<ParentT>, WithFrontend<ParentT> {
        }

        /**
         * The stage of an application gateway request routing rule definition allowing to specify the frontend for the
         * rule to apply to.
         *
         * @param <ParentT> the stage of the application gateway definition to return to after attaching this definition
         */
        interface WithFrontend<ParentT> {
            /**
             * Enables the rule to apply to the application gateway's public (Internet-facing) frontend.
             *
             * <p>If the public frontend IP configuration does not yet exist, it will be created under an auto-generated
             * name.
             *
             * <p>If the application gateway does not have a public IP address specified for its public frontend, one
             * will be created automatically, unless a specific public IP address is specified in the application
             * gateway definition's optional settings.
             *
             * @return the next stage of the definition
             */
            WithFrontendPort<ParentT> fromPublicFrontend();

            /**
             * Enables the rule to apply to the application gateway's private (internal) frontend.
             *
             * <p>If the private frontend IP configuration does not yet exist, it will be created under an
             * auto-generated name.
             *
             * <p>If the application gateway does not have a subnet specified for its private frontend, one will be
             * created automatically, unless a specific subnet is specified in the application gateway definition's
             * optional settings.
             *
             * @return the next stage of the definition
             */
            WithFrontendPort<ParentT> fromPrivateFrontend();
        }

        /**
         * The stage of an application gateway request routing rule definition allowing to create an associate listener
         * and frontend for a specific port number and protocol.
         *
         * @param <ParentT> the stage of the application gateway definition to return to after attaching this definition
         */
        interface WithFrontendPort<ParentT> {
            /**
             * Associates a new listener for the specified port number and the HTTP protocol with this rule.
             *
             * @param portNumber the port number to listen to
             * @return the next stage of the definition, or null if the specified port number is already used for a
             *     different protocol
             */
            WithBackendHttpConfigOrRedirect<ParentT> fromFrontendHttpPort(int portNumber);

            /**
             * Associates a new listener for the specified port number and the HTTPS protocol with this rule.
             *
             * @param portNumber the port number to listen to
             * @return the next stage of the definition, or null if the specified port number is already used for a
             *     different protocol
             */
            WithSslCertificate<ParentT> fromFrontendHttpsPort(int portNumber);
        }

        /**
         * The stage of an application gateway request routing rule allowing to specify an SSL certificate.
         *
         * @param <ParentT> the stage of the application gateway definition to return to after attaching this definition
         */
        interface WithSslCertificate<ParentT>
            extends HasSslCertificate.DefinitionStages.WithSslCertificate<
                WithBackendHttpConfigOrSniOrRedirect<ParentT>> {
        }

        /**
         * The stage of an application gateway request routing rule allowing to specify backend HTTP settings, or SNI,
         * or a redirect configuration.
         *
         * @param <ParentT> the stage of the application gateway definition to return to after attaching this definition
         */
        interface WithBackendHttpConfigOrSniOrRedirect<ParentT>
            extends WithBackendHttpConfigurationOrSni<ParentT>, WithRedirectConfig<ParentT> {
        }

        /**
         * The stage of an application gateway request routing rule allowing to specify an SSL certificate.
         *
         * @param <ParentT> the stage of the application gateway definition to return to after attaching this definition
         */
        interface WithSslPassword<ParentT> extends HasSslCertificate.DefinitionStages.WithSslPassword<ParentT> {
        }

        /**
         * The stage of an application gateway request routing rule allowing to enable cookie based affinity.
         *
         * @param <ParentT> the stage of the application gateway definition to return to after attaching this definition
         */
        interface WithCookieBasedAffinity<ParentT>
            extends HasCookieBasedAffinity.DefinitionStages.WithCookieBasedAffinity<WithAttach<ParentT>> {
        }

        /**
         * The stage of an application gateway request routing rule definition allowing to specify the backend to
         * associate the routing rule with.
         *
         * @param <ParentT> the stage of the application gateway definition to return to after attaching this definition
         */
        interface WithBackend<ParentT> {
            /**
             * Associates the request routing rule with a backend on this application gateway.
             *
             * <p>If the backend does not yet exist, it will be automatically created.
             *
             * @param name the name of an existing backend
             * @return the next stage of the definition
             */
            WithAttach<ParentT> toBackend(String name);
        }

        /**
         * The stage of an application gateway request routing rule definition allowing to add an address to the backend
         * used by this request routing rule.
         *
         * <p>A new backend will be created if none is associated with this rule yet.
         *
         * @param <ParentT> the stage of the application gateway definition to return to after attaching this definition
         */
        interface WithBackendAddress<ParentT> {
            /**
             * Adds an IP address to the backend associated with this rule.
             *
             * <p>If no backend has been associated with this rule yet, a new one will be created with an auto-generated
             * name.
             *
             * <p>This call can be used in a sequence to add multiple IP addresses.
             *
             * @param ipAddress an IP address
             * @return the next stage of the definition
             */
            WithBackendAddressOrAttach<ParentT> toBackendIPAddress(String ipAddress);

            /**
             * Adds the specified IP addresses to the backend associated with this rule.
             *
             * @param ipAddresses IP addresses to add
             * @return the next stage of the definition
             */
            WithBackendAddressOrAttach<ParentT> toBackendIPAddresses(String... ipAddresses);

            /**
             * Adds an FQDN (fully qualified domain name) to the backend associated with this rule.
             *
             * <p>If no backend has been associated with this rule yet, a new one will be created with an auto-generated
             * name.
             *
             * <p>This call can be used in a sequence to add multiple FQDNs.
             *
             * @param fqdn a fully qualified domain name
             * @return the next stage of the definition
             */
            WithBackendAddressOrAttach<ParentT> toBackendFqdn(String fqdn);
        }

        /**
         * The stage of an application gateway request routing rule definition allowing to add more backend addresses,
         * start specifying optional settings, or finish the definition by attaching it to the parent application
         * gateway.
         *
         * @param <ParentT> the stage of the application gateway definition to return to after attaching this definition
         */
        interface WithBackendAddressOrAttach<ParentT> extends WithBackendAddress<ParentT>, WithAttach<ParentT> {
        }

        /**
         * The stage of an application gateway request routing rule definition allowing to add an address to specify an
         * existing backend to associate with this request routing rule or create a new backend with an auto-generated
         * name and addresses to it.
         *
         * @param <ParentT> the stage of the application gateway definition to return to after attaching this definition
         */
        interface WithBackendOrAddress<ParentT> extends WithBackend<ParentT>, WithBackendAddress<ParentT> {
        }

        /**
         * The stage of an application gateway request routing rule definition allowing to require server name
         * indication if the application gateway is serving multiple websites in its backends and SSL is required.
         *
         * @param <ParentT> the stage of the application gateway definition to return to after attaching this definition
         */
        interface WithBackendHttpConfigurationOrSni<ParentT>
            extends WithBackendHttpConfiguration<ParentT>,
                HasServerNameIndication.DefinitionStages.WithServerNameIndication<
                    WithBackendHttpConfiguration<ParentT>> {
        }

        /**
         * The stage of an application gateway request routing rule definition allowing to specify the backend HTTP
         * settings configuration to associate the routing rule with.
         *
         * @param <ParentT> the stage of the application gateway definition to return to after attaching this definition
         */
        interface WithBackendHttpConfiguration<ParentT> {
            /**
             * Associates the specified backend HTTP settings configuration with this request routing rule.
             *
             * <p>If the backend configuration does not exist yet, it must be defined in the optional part of the
             * application gateway definition. The request routing rule references it only by name.
             *
             * @param name the name of a backend HTTP settings configuration
             * @return the next stage of the definition
             */
            WithBackendOrAddress<ParentT> toBackendHttpConfiguration(String name);

            /**
             * Creates a backend HTTP settings configuration for the specified backend port and the HTTP protocol, and
             * associates it with this request routing rule.
             *
             * <p>An auto-generated name will be used for this newly created configuration.
             *
             * @param portNumber the port number for a new backend HTTP settings configuration
             * @return the next stage of the definition
             */
            WithBackendOrAddress<ParentT> toBackendHttpPort(int portNumber);

            // TODO: toBackendHttpsPort(int portNumber) ?
        }

        /**
         * The stage of an application gateway request routing rule definition allowing to select either a backend or a
         * redirect configuration.
         *
         * @param <ParentT> the stage of the application gateway definition to return to after attaching this definition
         */
        interface WithBackendHttpConfigOrRedirect<ParentT>
            extends WithBackendHttpConfiguration<ParentT>, WithRedirectConfig<ParentT> {
        }

        /**
         * The stage of an application gateway request routing rule definition allowing to specify the host name of a
         * backend website for the listener to receive traffic for.
         *
         * @param <ParentT> the stage of the application gateway definition to return to after attaching this definition
         */
        interface WithHostname<ParentT> extends HasHostname.DefinitionStages.WithHostname<WithAttach<ParentT>> {
        }

        /**
         * The stage of an application gateway request routing rule definition allowing to require server name
         * indication.
         *
         * @param <ParentT> the stage of the application gateway definition to return to after attaching this definition
         */
        interface WithServerNameIndication<ParentT>
            extends HasServerNameIndication.DefinitionStages.WithServerNameIndication<WithAttach<ParentT>> {
        }

        /**
         * The stage of an application gateway request routing rule definition allowing to associate the rule with a
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

        interface WithUrlPathMap<ParentT> {
            WithAttach<ParentT> withUrlPathMap(String urlPathMapName);
        }
    }

    /**
     * The entirety of an application gateway request routing rule definition.
     *
     * @param <ParentT> the stage of the application gateway definition to return to after attaching this definition
     */
    interface Definition<ParentT>
        extends DefinitionStages.Blank<ParentT>,
            DefinitionStages.WithAttach<ParentT>,
            DefinitionStages.WithFrontend<ParentT>,
            DefinitionStages.WithListener<ParentT>,
            DefinitionStages.WithFrontendPort<ParentT>,
            DefinitionStages.WithListenerOrFrontend<ParentT>,
            DefinitionStages.WithBackend<ParentT>,
            DefinitionStages.WithBackendAddress<ParentT>,
            DefinitionStages.WithBackendOrAddress<ParentT>,
            DefinitionStages.WithBackendAddressOrAttach<ParentT>,
            DefinitionStages.WithBackendHttpConfigOrRedirect<ParentT>,
            DefinitionStages.WithBackendHttpConfiguration<ParentT>,
            DefinitionStages.WithBackendHttpConfigurationOrSni<ParentT>,
            DefinitionStages.WithSslCertificate<ParentT>,
            DefinitionStages.WithBackendHttpConfigOrSniOrRedirect<ParentT>,
            DefinitionStages.WithSslPassword<DefinitionStages.WithBackendHttpConfigOrSniOrRedirect<ParentT>>,
            DefinitionStages.WithUrlPathMap<ParentT> {
    }

    /** Grouping of application gateway request routing rule update stages. */
    interface UpdateStages {
        /**
         * The stage of an application gateway request routing rule update allowing to associate the rule with a
         * redirect configuration.
         */
        interface WithRedirectConfig {
            /**
             * Associates the specified redirect configuration with this request routing rule.
             *
             * <p>Note that no backend can be associated with this request routing rule if it has a redirect
             * configuration assigned to it, so this will also remove any backend and backend HTTP settings
             * configuration.
             *
             * @param name the name of a redirect configuration on this application gateway
             * @return the next stage of the update
             */
            Update withRedirectConfiguration(String name);

            /**
             * Removes the association with a redirect configuration, if any.
             *
             * @return the next stage of the update
             */
            Update withoutRedirectConfiguration();
        }

        /**
         * The stage of an application gateway request routing rule update allowing to specify an existing listener to
         * associate the routing rule with.
         */
        interface WithListener {
            /**
             * Associates the request routing rule with an existing frontend listener.
             *
             * <p>Also, note that a given listener can be used by no more than one request routing rule at a time.
             *
             * @param name the name of a listener to reference
             * @return the next stage of the update
             */
            Update fromListener(String name);
        }

        /**
         * The stage of an application gateway request routing rule update allowing to specify the backend to associate
         * the routing rule with.
         */
        interface WithBackend {
            /**
             * Associates the request routing rule with a backend on this application gateway.
             *
             * <p>If the specified backend does not yet exist, it will be automatically created.
             *
             * @param name the name of a backend
             * @return the next stage of the update
             */
            Update toBackend(String name);
        }

        /**
         * The stage of an application gateway request routing rule update allowing to specify the backend HTTP settings
         * configuration to associate the routing rule with.
         */
        interface WithBackendHttpConfiguration {
            /**
             * Associates the specified backend HTTP settings configuration with this request routing rule.
             *
             * @param name the name of a backend HTTP settings configuration
             * @return the next stage of the update
             */
            Update toBackendHttpConfiguration(String name);
        }

        /** The stage of an application gateway request routing rule allowing to specify an SSL certificate. */
        interface WithSslCertificate extends HasSslCertificate.UpdateStages.WithSslCertificate<Update> {
        }

        /**
         * The stage of an application gateway request routing rule allowing to specify password of the SSL certificate
         * pfx file.
         */
        interface WithSslPassword extends HasSslCertificate.UpdateStages.WithSslPassword<Update> {
        }
    }

    /** The entirety of an application gateway request routing rule update as part of an application gateway update. */
    interface Update
        extends Settable<ApplicationGateway.Update>,
            UpdateStages.WithListener,
            UpdateStages.WithBackend,
            UpdateStages.WithBackendHttpConfiguration,
            UpdateStages.WithSslCertificate,
            UpdateStages.WithSslPassword,
            UpdateStages.WithRedirectConfig {
    }

    /**
     * Grouping of application gateway request routing rule definition stages applicable as part of an application
     * gateway update.
     */
    interface UpdateDefinitionStages {
        /**
         * The first stage of an application gateway request routing rule definition.
         *
         * @param <ParentT> the stage of the application gateway definition to return to after attaching this definition
         */
        interface Blank<ParentT> extends WithListenerOrFrontend<ParentT> {
        }

        /**
         * The final stage of an application gateway request routing rule definition.
         *
         * <p>At this stage, any remaining optional settings can be specified, or the definition can be attached to the
         * parent application gateway definition.
         *
         * @param <ParentT> the stage of the application gateway definition to return to after attaching this definition
         */
        interface WithAttach<ParentT>
            extends Attachable.InUpdate<ParentT>,
                WithHostname<ParentT>,
                WithCookieBasedAffinity<ParentT>,
                WithRedirectConfig<ParentT> {
        }

        /**
         * The stage of an application gateway request routing rule definition allowing to select either a backend or a
         * redirect configuration.
         *
         * @param <ParentT> the stage of the application gateway update to return to after attaching this definition
         */
        interface WithBackendHttpConfigOrRedirect<ParentT>
            extends WithBackendHttpConfiguration<ParentT>, WithRedirectConfig<ParentT> {
        }

        /**
         * The stage of an application gateway request routing rule definition allowing to associate the rule with a
         * redirect configuration.
         *
         * @param <ParentT> the stage of the application gateway update to return to after attaching this definition
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
         * The stage of an application gateway request routing rule definition allowing to specify an existing listener
         * to associate the routing rule with.
         *
         * @param <ParentT> the stage of the application gateway definition to return to after attaching this definition
         */
        interface WithListener<ParentT> {
            /**
             * Associates the request routing rule with a frontend listener.
             *
             * <p>If the listener with the specified name does not yet exist, it must be defined separately in the
             * optional part of the application gateway definition. This only adds a reference to the listener by its
             * name.
             *
             * <p>Also, note that a given listener can be used by no more than one request routing rule at a time.
             *
             * @param name the name of a listener to reference
             * @return the next stage of the definition
             */
            WithBackendHttpConfigOrRedirect<ParentT> fromListener(String name);
        }

        /**
         * The stage of an application gateway request routing rule definition allowing to associate an existing
         * listener with the rule, or create a new one implicitly by specifying the frontend to listen to.
         *
         * @param <ParentT> the stage of the application gateway definition to return to after attaching this definition
         */
        interface WithListenerOrFrontend<ParentT> extends WithListener<ParentT>, WithFrontend<ParentT> {
        }

        /**
         * The stage of an application gateway request routing rule definition allowing to specify the frontend for the
         * rule to apply to.
         *
         * @param <ParentT> the stage of the application gateway definition to return to after attaching this definition
         */
        interface WithFrontend<ParentT> {
            /**
             * Enables the rule to apply to the application gateway's public (Internet-facing) frontend.
             *
             * <p>If the public frontend IP configuration does not yet exist, it will be created under an auto-generated
             * name.
             *
             * <p>If the application gateway does not have a public IP address specified for its public frontend, one
             * will be created automatically, unless a specific public IP address is specified in the application
             * gateway definition's optional settings.
             *
             * @return the next stage of the definition
             */
            WithFrontendPort<ParentT> fromPublicFrontend();

            /**
             * Enables the rule to apply to the application gateway's private (internal) frontend.
             *
             * <p>If the private frontend IP configuration does not yet exist, it will be created under an
             * auto-generated name.
             *
             * <p>If the application gateway does not have a subnet specified for its private frontend, one will be
             * created automatically, unless a specific subnet is specified in the application gateway definition's
             * optional settings.
             *
             * @return the next stage of the definition
             */
            WithFrontendPort<ParentT> fromPrivateFrontend();
        }

        /**
         * The stage of an application gateway request routing rule definition allowing to create an associate listener
         * and frontend for a specific port number and protocol.
         *
         * @param <ParentT> the stage of the application gateway definition to return to after attaching this definition
         */
        interface WithFrontendPort<ParentT> {
            /**
             * Associates a new listener for the specified port number and the HTTP protocol with this rule.
             *
             * @param portNumber the port number to listen to
             * @return the next stage of the definition, or null if the specified port number is already used for a
             *     different protocol
             */
            WithBackendHttpConfigOrRedirect<ParentT> fromFrontendHttpPort(int portNumber);

            /**
             * Associates a new listener for the specified port number and the HTTPS protocol with this rule.
             *
             * @param portNumber the port number to listen to
             * @return the next stage of the definition, or null if the specified port number is already used for a
             *     different protocol
             */
            WithSslCertificate<ParentT> fromFrontendHttpsPort(int portNumber);
        }

        /**
         * The stage of an application gateway request routing rule allowing to specify an SSL certificate.
         *
         * @param <ParentT> the next stage of the definition
         */
        interface WithSslCertificate<ParentT>
            extends HasSslCertificate.UpdateDefinitionStages.WithSslCertificate<
                WithBackendHttpConfigOrSniOrRedirect<ParentT>> {
        }

        /**
         * The stage of an application gateway request routing rule allowing to specify an SSL certificate.
         *
         * @param <ParentT> the next stage of the definition
         */
        interface WithSslPassword<ParentT> extends HasSslCertificate.UpdateDefinitionStages.WithSslPassword<ParentT> {
        }

        /**
         * The stage of an application gateway request routing rule allowing to enable cookie based affinity.
         *
         * @param <ParentT> the next stage of the definition
         */
        interface WithCookieBasedAffinity<ParentT>
            extends HasCookieBasedAffinity.UpdateDefinitionStages.WithCookieBasedAffinity<WithAttach<ParentT>> {
        }

        /**
         * The stage of an application gateway request routing rule definition allowing to specify the backend to
         * associate the routing rule with.
         *
         * @param <ParentT> the stage of the application gateway definition to return to after attaching this definition
         */
        interface WithBackend<ParentT> {
            /**
             * Associates the request routing rule with a backend on this application gateway.
             *
             * <p>If the backend does not yet exist, it will be automatically created.
             *
             * @param name the name of an existing backend
             * @return the next stage of the definition
             */
            WithAttach<ParentT> toBackend(String name);
        }

        /**
         * The stage of an application gateway request routing rule definition allowing to add an address to the backend
         * used by this request routing rule.
         *
         * <p>A new backend will be created if none is associated with this rule yet.
         *
         * @param <ParentT> the stage of the application gateway definition to return to after attaching this definition
         */
        interface WithBackendAddress<ParentT> {
            /**
             * Adds an IP address to the backend associated with this rule.
             *
             * <p>If no backend has been associated with this rule yet, a new one will be created with an auto-generated
             * name.
             *
             * <p>This call can be used in a sequence to add multiple IP addresses.
             *
             * @param ipAddress an IP address
             * @return the next stage of the definition
             */
            WithBackendAddressOrAttach<ParentT> toBackendIPAddress(String ipAddress);

            /**
             * Adds the specified IP addresses to the backend associated with this rule.
             *
             * @param ipAddresses IP addresses to add
             * @return the next stage of the definition
             */
            WithBackendAddressOrAttach<ParentT> toBackendIPAddresses(String... ipAddresses);

            /**
             * Adds an FQDN (fully qualified domain name) to the backend associated with this rule.
             *
             * <p>If no backend has been associated with this rule yet, a new one will be created with an auto-generated
             * name.
             *
             * <p>This call can be used in a sequence to add multiple FQDNs.
             *
             * @param fqdn a fully qualified domain name
             * @return the next stage of the definition
             */
            WithBackendAddressOrAttach<ParentT> toBackendFqdn(String fqdn);
        }

        /**
         * The stage of an application gateway request routing rule definition allowing to add more backend addresses,
         * start specifying optional settings, or finish the definition by attaching it to the parent application
         * gateway.
         *
         * @param <ParentT> the stage of the application gateway definition to return to after attaching this definition
         */
        interface WithBackendAddressOrAttach<ParentT> extends WithBackendAddress<ParentT>, WithAttach<ParentT> {
        }

        /**
         * The stage of an application gateway request routing rule definition allowing to add an address to specify an
         * existing backend to associate with this request routing rule or create a new backend with an auto-generated
         * name and addresses to it.
         *
         * @param <ParentT> the stage of the application gateway definition to return to after attaching this definition
         */
        interface WithBackendOrAddress<ParentT> extends WithBackend<ParentT>, WithBackendAddress<ParentT> {
        }

        /**
         * The stage of an application gateway request routing rule definition allowing to require server name
         * indication if the application gateway is serving multiple websites in its backends and SSL is required.
         *
         * @param <ParentT> the stage of the application gateway definition to return to after attaching this definition
         */
        interface WithBackendHttpConfigurationOrSni<ParentT>
            extends WithBackendHttpConfiguration<ParentT>,
                HasServerNameIndication.UpdateDefinitionStages.WithServerNameIndication<
                    WithBackendHttpConfiguration<ParentT>> {
        }

        /**
         * The stage of an application gateway request routing rule allowing to specify backend HTTP settings, or SNI,
         * or a redirect configuration.
         *
         * @param <ParentT> the stage of the application gateway update to return to after attaching this definition
         */
        interface WithBackendHttpConfigOrSniOrRedirect<ParentT>
            extends WithBackendHttpConfigurationOrSni<ParentT>, WithRedirectConfig<ParentT> {
        }

        /**
         * The stage of an application gateway request routing rule definition allowing to specify the backend HTTP
         * settings configuration to associate the routing rule with.
         *
         * @param <ParentT> the stage of the application gateway definition to return to after attaching this definition
         */
        interface WithBackendHttpConfiguration<ParentT> {
            /**
             * Associates the specified backend HTTP settings configuration with this request routing rule.
             *
             * <p>If the backend configuration does not exist yet, it must be defined in the optional part of the
             * application gateway definition. The request routing rule references it only by name.
             *
             * @param name the name of a backend HTTP settings configuration
             * @return the next stage of the definition
             */
            WithBackendOrAddress<ParentT> toBackendHttpConfiguration(String name);

            /**
             * Creates a backend HTTP settings configuration for the specified backend port and the HTTP protocol, and
             * associates it with this request routing rule.
             *
             * <p>An auto-generated name will be used for this newly created configuration.
             *
             * @param portNumber the port number for a new backend HTTP settings configuration
             * @return the next stage of the definition
             */
            WithBackendOrAddress<ParentT> toBackendHttpPort(int portNumber);

            // TODO: toBackendHttpsPort(int portNumber) ?
        }

        /**
         * The stage of an application gateway request routing rule definition allowing to specify the host name of a
         * backend website for the listener to receive traffic for.
         *
         * @param <ParentT> the stage of the application gateway definition to return to after attaching this definition
         */
        interface WithHostname<ParentT> extends HasHostname.UpdateDefinitionStages.WithHostname<WithAttach<ParentT>> {
        }

        /**
         * The stage of an application gateway request routing rule definition allowing to require server name
         * indication.
         *
         * @param <ParentT> the stage of the application gateway definition to return to after attaching this definition
         */
        interface WithServerNameIndication<ParentT>
            extends HasServerNameIndication.UpdateDefinitionStages.WithServerNameIndication<WithAttach<ParentT>> {
        }
    }

    /**
     * The entirety of an application gateway request routing rule definition as part of an application gateway update.
     *
     * @param <ParentT> the stage of the application gateway definition to return to after attaching this definition
     */
    interface UpdateDefinition<ParentT>
        extends UpdateDefinitionStages.Blank<ParentT>,
            UpdateDefinitionStages.WithAttach<ParentT>,
            UpdateDefinitionStages.WithFrontend<ParentT>,
            UpdateDefinitionStages.WithListener<ParentT>,
            UpdateDefinitionStages.WithFrontendPort<ParentT>,
            UpdateDefinitionStages.WithListenerOrFrontend<ParentT>,
            UpdateDefinitionStages.WithBackend<ParentT>,
            UpdateDefinitionStages.WithBackendAddress<ParentT>,
            UpdateDefinitionStages.WithBackendOrAddress<ParentT>,
            UpdateDefinitionStages.WithBackendAddressOrAttach<ParentT>,
            UpdateDefinitionStages.WithBackendHttpConfiguration<ParentT>,
            UpdateDefinitionStages.WithBackendHttpConfigOrRedirect<ParentT>,
            UpdateDefinitionStages.WithBackendHttpConfigurationOrSni<ParentT>,
            UpdateDefinitionStages.WithBackendHttpConfigOrSniOrRedirect<ParentT>,
            UpdateDefinitionStages.WithSslCertificate<ParentT>,
            UpdateDefinitionStages.WithSslPassword<
                UpdateDefinitionStages.WithBackendHttpConfigOrSniOrRedirect<ParentT>> {
    }
}
