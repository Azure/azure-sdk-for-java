/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.network.implementation.ApplicationGatewayRequestRoutingRuleInner;
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
    HasHostName,
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

    // TODO urlPathMap()

    /**
     * Grouping of application gateway request routing rule definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of an application gateway request routing rule definition.
         * @param <ParentT> the stage of the application gateway definition to return to after attaching this definition
         */
        interface Blank<ParentT> extends WithFrontendListenerOrPort<ParentT> {
        }

        /** The final stage of an application gateway request routing rule definition.
         * <p>
         * At this stage, any remaining optional settings can be specified, or the definition
         * can be attached to the parent application gateway definition using {@link WithAttach#attach()}.
         * @param <ParentT> the stage of the application gateway definition to return to after attaching this definition
         */
        interface WithAttach<ParentT> extends
            Attachable.InDefinition<ParentT>,
            WithHostName<ParentT> {
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
             * A listener with the referenced name must be defined separately but as part of the same application gateway creation process.
             * @param name the name of a listener to reference
             * @return the next stage of the definition
             */
            WithBackendHttpConfiguration<ParentT> fromFrontendListener(String name);
        }

        /**
         * The stage of an application gateway request routing rule definition allowing to associate a new or existing listener
         * to associate the routing rule with.
         * @param <ParentT>
         */
        interface WithFrontendListenerOrPort<ParentT> extends
            WithFrontendListener<ParentT>,
            WithFrontendPort<ParentT> {
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
         * The stage of an application gateway request routing rule definition allowing to specify the backend to associate the routing rule with.
         * @param <ParentT> the stage of the application gateway definition to return to after attaching this definition
         */
        interface WithBackend<ParentT> {
            /**
             * Associates the request routing rule with an existing backend on this application gateway.
             * @param name the name of an existing backend
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withBackend(String name);
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
             * Associates the request routing rule with an existing backend HTTP settings configuration on this application gateway.
             * @param name the name of an existing backend HTTP settings configuration
             * @return the next stage of the definition
             */
            WithBackend<ParentT> toBackendHttpConfiguration(String name);

            /**
             * Associates the request routing rule with an existing backend HTTP settings configuration on this application gateway
             * configured to send traffic to the specified backend port, if such a configuration exists.
             * @param portNumber the port number of a backend HTTP settings configuration on this application gateway
             * @return teh next stage of the definition or null if no backend HTTP configuration exists for the specified port number
             */
            WithBackend<ParentT> toBackendPort(int portNumber);
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
        DefinitionStages.WithFrontendListener<ParentT>,
        DefinitionStages.WithFrontendPort<ParentT>,
        DefinitionStages.WithFrontendListenerOrPort<ParentT>,
        DefinitionStages.WithBackend<ParentT>,
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
