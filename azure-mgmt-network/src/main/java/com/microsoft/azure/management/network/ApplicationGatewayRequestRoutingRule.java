/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network;

import java.util.List;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.apigeneration.Method;
import com.microsoft.azure.management.network.implementation.ApplicationGatewayRequestRoutingRuleInner;
import com.microsoft.azure.management.network.model.HasBackendPort;
import com.microsoft.azure.management.network.model.HasCookieBasedAffinity;
import com.microsoft.azure.management.network.model.HasFrontendPort;
import com.microsoft.azure.management.network.model.HasHostName;
import com.microsoft.azure.management.network.model.HasProtocol;
import com.microsoft.azure.management.network.model.HasPublicIpAddress;
import com.microsoft.azure.management.network.model.HasServerNameIndication;
import com.microsoft.azure.management.network.model.HasSslCertificate;
import com.microsoft.azure.management.resources.fluentcore.arm.models.ChildResource;
import com.microsoft.azure.management.resources.fluentcore.model.Attachable;
import com.microsoft.azure.management.resources.fluentcore.model.Settable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;

/**
 * An immutable client-side representation of an application gateway request routing rule.
 */
@Fluent()
public interface ApplicationGatewayRequestRoutingRule extends
    Wrapper<ApplicationGatewayRequestRoutingRuleInner>,
    ChildResource<ApplicationGateway>,
    HasPublicIpAddress,
    HasProtocol<ApplicationGatewayProtocol>,
    HasSslCertificate<ApplicationGatewaySslCertificate>,
    HasFrontendPort,
    HasBackendPort,
    HasHostName,
    HasCookieBasedAffinity,
    HasServerNameIndication {

    /**
     * @return rule type
     */
    ApplicationGatewayRequestRoutingRuleType ruleType();

    /**
     * @return the associated backend address pool
     */
    ApplicationGatewayBackend backend();

    /**
     * @return the associated backend HTTP settings configuration
     */
    ApplicationGatewayBackendHttpConfiguration backendHttpConfiguration();

    /**
     * @return the associated frontend HTTP listener
     */
    ApplicationGatewayFrontendListener frontendListener();

    /**
     * @return the addresses assigned to the associated backend
     */
    List<ApplicationGatewayBackendAddress> backendAddresses();

    // TODO urlPathMap()

    /**
     * Grouping of application gateway request routing rule definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of an application gateway request routing rule definition.
         * @param <ParentT> the stage of the application gateway definition to return to after attaching this definition
         */
        interface Blank<ParentT> extends WithFrontendListenerOrFrontend<ParentT> {
        }

        /** The final stage of an application gateway request routing rule definition.
         * <p>
         * At this stage, any remaining optional settings can be specified, or the definition
         * can be attached to the parent application gateway definition using {@link WithAttach#attach()}.
         * @param <ParentT> the stage of the application gateway definition to return to after attaching this definition
         */
        interface WithAttach<ParentT> extends
            Attachable.InDefinition<ParentT>,
            WithHostName<ParentT>,
            WithCookieBasedAffinity<ParentT> {
        }

        /**
         * The stage of an application gateway request routing rule definition allowing to specify an existing listener to
         * associate the routing rule with.
         * @param <ParentT> the stage of the application gateway definition to return to after attaching this definition
         */
        interface WithFrontendListener<ParentT> {
            /**
             * Associates the request routing rule with a frontend listener.
             * <p>
             * If the listener with the specified name does not yeyt exist, it must be defined separately in a later part of the application gateway definition.
             * This only adds a reference to the listener by its name.
             * @param name the name of a listener to reference
             * @return the next stage of the definition
             */
            WithBackendHttpConfiguration<ParentT> fromFrontendListener(String name);
        }

        /**
         * The stage of an application gateway request routing rule definition allowing to associate an existing listener
         * with the request routing rule, or create a new one implicitly by specifying the frontend to listen to.
         * @param <ParentT> the stage of the application gateway definition to return to after attaching this definition
         */
        interface WithFrontendListenerOrFrontend<ParentT> extends
            WithFrontendListener<ParentT>,
            WithFrontend<ParentT> {
        }

        /**
         * The stage of an application gateway request routing rule definition allowing to specify the frontend for the rule to apply to.
         * @param <ParentT> the stage of the application gateway definition to return to after attaching this definition
         */
        interface WithFrontend<ParentT> {
            /**
             * Uses the application gateway's public (Internet-facing) frontend as the frontend for the rule to apply to.
             * <p>
             * If the public frontend does not yet exist, it will be created under an auto-generated name.
             * <p>
             * If the application gateway does not have a public IP address specified for its public frontend, one will be created automatically.
             * A specific public IP address can be specified in the application gateway definition's optional settings.
             * @return the next stage of the definition
             */
            @Method
            WithFrontendPort<ParentT> fromDefaultPublicFrontend();

            /**
             * Selects the default private frontend as the frontend for the rule to apply to.
             * @return the next stage of the definition
             */
            @Method
            WithFrontendPort<ParentT> fromDefaultPrivateFrontend();
        }

        /** The stage of an application gateway request routing rule definition allowing to create an associate listener and frontend
         * for a specific port number and protocol.
         * @param <ParentT> the stage of the application gateway definition to return to after attaching this definition
         */
        interface WithFrontendPort<ParentT> {
            /**
             * Creates a new frontend and a frontend listener on this application gateway for the specified port number and the HTTP protocol
             * and associates it with this rule.
             * @param portNumber the port number to listen to
             * @return the next stage of the definition, or null if the specified port number is already used for a different protocol
             */
            WithBackendHttpConfiguration<ParentT> fromFrontendHttpPort(int portNumber);

            /**
             * Creates a new frontend and a frontend listener on this application gateway for the specified port number and the HTTPS protocol
             * and associates it with this rule.
             * @param portNumber the port number to listen to
             * @return the next stage of the definition, or null if the specified port number is already used for a different protocol
             */
            WithSslCertificate<ParentT> fromFrontendHttpsPort(int portNumber);
        }

        /**
         * The stage of an application gateway request routing rule allowing to specify an SSL certificate.
         * @param <ParentT> the next stage of the definition
         */
        interface WithSslCertificate<ParentT> extends
            HasSslCertificate.DefinitionStages.WithSslCertificate<WithBackendHttpConfigurationOrSni<ParentT>> {
        }

        /**
         * The stage of an application gateway request routing rule allowing to specify an SSL certificate.
         * @param <ParentT> the next stage of the definition
         */
        interface WithSslPassword<ParentT> extends HasSslCertificate.DefinitionStages.WithSslPassword<ParentT> {
        }

        /**
         * The stage of an application gateway request routing rule allowing to enable cookie based affinity.
         * @param <ParentT> the next stage of the definition
         */
        interface WithCookieBasedAffinity<ParentT> extends HasCookieBasedAffinity.DefinitionStages.WithCookieBasedAffinity<WithAttach<ParentT>> {
        }

        /**
         * The stage of an application gateway request routing rule definition allowing to specify the backend to associate the routing rule with.
         * @param <ParentT> the stage of the application gateway definition to return to after attaching this definition
         */
        interface WithBackend<ParentT> {
            /**
             * Associates the request routing rule with a backend on this application gateway.
             * <p>
             * If the backend does not yet exist, it must be defined in the optional part of the application gateway definition.
             * The request routing rule references it only by name.
             * @param name the name of an existing backend
             * @return the next stage of the definition
             */
            WithAttach<ParentT> toBackend(String name);
        }

        /**
         * The stage of an application gateway request routing rule definition allowing to add an address to the backend used by this request routing rule.
         * <p>
         * A new backend will be created if none is associated with this rule yet.
         * @param <ParentT> the stage of the application gateway definition to return to after attaching this definition
         */
        interface WithBackendAddress<ParentT> {
            /**
             * Adds an IP address to the backend associated with this rule.
             * <p>
             * If no backend has been associated with this rule yet, a new one will be created with an auto-generated name.
             * <p>
             * This call can be used in a sequence to add multiple FQDNs.
             * @param ipAddress an IP address
             * @return the next stage of the definition
             */
            WithBackendAddressOrAttach<ParentT> toBackendIpAddress(String ipAddress);

            /**
             * Adds an FQDN (fully qualified domain name) to the backend associated with this rule.
             * <p>
             * If no backend has been associated with this rule yet, a new one will be created with an auto-generated name.
             * <p>
             * This call can be used in a sequence to add multiple FQDNs.
             * @param fqdn a fully qualified domain name
             * @return the next stage of the definition
             */
            WithBackendAddressOrAttach<ParentT> toBackendFqdn(String fqdn);
        }

        /**
         * The stage of an application gateway request routing rule definition allowing to add more backend addresses,
         * start specifying optional settings, or finish the definition by attaching it to the parent application gateway.
         * @param <ParentT> the stage of the application gateway definition to return to after attaching this definition
         */
        interface WithBackendAddressOrAttach<ParentT> extends WithBackendAddress<ParentT>, WithAttach<ParentT> {
        }

        /**
         * The stage of an application gateway request routing rule definition allowing to add an address to specify an existing
         * backend to associate with this request routing rule or create a new backend with an auto-generated name and addresses to it.
         * @param <ParentT> the stage of the application gateway definition to return to after attaching this definition
         */
        interface WithBackendOrAddress<ParentT> extends WithBackend<ParentT>, WithBackendAddress<ParentT> {
        }

        /**
         * The stage of an application gateway request routing rule definition allowing to require server name indication if the
         * application gateway is serving multiple websites in its backends and SSL is required.
         * @param <ParentT> the stage of the application gateway definition to return to after attaching this definition
         */
        interface WithBackendHttpConfigurationOrSni<ParentT> extends
            WithBackendHttpConfiguration<ParentT>,
            HasServerNameIndication.DefinitionStages.WithServerNameIndication<WithBackendHttpConfiguration<ParentT>> {
        }

        /**
         * The stage of an application gateway request routing rule definition allowing to specify the backend HTTP settings configuration
         * to associate the routing rule with.
         * @param <ParentT> the stage of the application gateway definition to return to after attaching this definition
         */
        interface WithBackendHttpConfiguration<ParentT> {
            /**
             * Associates the specified backend HTTP settings configuration with this request routing rule.
             * <p>
             * If the backend configuration does not exist yet, it must be defined in the optional part of the application gateway definition.
             * The request routing rule references it only by name.
             * @param name the name of a backend HTTP settings configuration
             * @return the next stage of the definition
             */
            WithBackendOrAddress<ParentT> toBackendHttpConfiguration(String name);

            /**
             * Creates a backend HTTP settings configuration for the specified backend port and the HTTP protocol, and associates it with this
             * request routing rule.
             * <p>
             * An auto-generated name will be uses for this newly created configuration.
             * @param portNumber the port number for a new backend HTTP settings configuration
             * @return the next stage of the definition
             */
            WithBackendOrAddress<ParentT> toBackendHttpPort(int portNumber);

            // TODO: toBackendHttpsPort(int portNumber) ?
        }

        /**
         * The stage of an application gateway request routing rule definition allowing to specify the host name of a backend website
         * for the listener to receive traffic for.
         * @param <ParentT> the stage of the application gateway definition to return to after attaching this definition
         */
        interface WithHostName<ParentT> extends HasHostName.DefinitionStages.WithHostName<WithAttach<ParentT>> {
        }

        /**
         * The stage of an application gateway request routing rule definition allowing to require server name indication.
         * @param <ParentT> the stage of the application gateway definition to return to after attaching this definition
         */
        interface WithServerNameIndication<ParentT> extends HasServerNameIndication.DefinitionStages.WithServerNameIndication<WithAttach<ParentT>> {
        }
    }

    /** The entirety of an application gateway request routing rule definition.
     * @param <ParentT> the return type of the final {@link DefinitionStages.WithAttach#attach()}
     */
    interface Definition<ParentT> extends
        DefinitionStages.Blank<ParentT>,
        DefinitionStages.WithAttach<ParentT>,
        DefinitionStages.WithFrontend<ParentT>,
        DefinitionStages.WithFrontendListener<ParentT>,
        DefinitionStages.WithFrontendPort<ParentT>,
        DefinitionStages.WithFrontendListenerOrFrontend<ParentT>,
        DefinitionStages.WithBackend<ParentT>,
        DefinitionStages.WithBackendAddress<ParentT>,
        DefinitionStages.WithBackendOrAddress<ParentT>,
        DefinitionStages.WithBackendAddressOrAttach<ParentT>,
        DefinitionStages.WithBackendHttpConfiguration<ParentT>,
        DefinitionStages.WithBackendHttpConfigurationOrSni<ParentT>,
        DefinitionStages.WithSslCertificate<ParentT>,
        DefinitionStages.WithSslPassword<DefinitionStages.WithBackendHttpConfigurationOrSni<ParentT>> {
    }

    /**
     * Grouping of application gateway request routing rule update stages.
     */
    interface UpdateStages {
    }

    /**
     * The entirety of an application gateway request routing rule update as part of an application gateway update.
     */
    interface Update extends
        Settable<ApplicationGateway.Update> {
    }

    /**
     * Grouping of application gateway request routing rule definition stages applicable as part of an application gateway update.
     */
    interface UpdateDefinitionStages {
        /**
         * The first stage of an application gateway request routing rule definition.
         * @param <ParentT> the return type of the final {@link WithAttach#attach()}
         */
        interface Blank<ParentT> extends WithAttach<ParentT> {
        }

        /** The final stage of an application gateway request routing rule definition.
         * <p>
         * At this stage, any remaining optional settings can be specified, or the definition
         * can be attached to the parent application gateway definition using {@link WithAttach#attach()}.
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithAttach<ParentT> extends
            Attachable.InUpdate<ParentT> {
        }
    }

    /** The entirety of an application gateway request routing rule definition as part of an application gateway update.
     * @param <ParentT> the return type of the final {@link UpdateDefinitionStages.WithAttach#attach()}
     */
    interface UpdateDefinition<ParentT> extends
        UpdateDefinitionStages.Blank<ParentT>,
        UpdateDefinitionStages.WithAttach<ParentT> {
    }
}
