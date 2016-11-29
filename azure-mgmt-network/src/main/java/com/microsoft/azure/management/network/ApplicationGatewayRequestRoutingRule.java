/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.network.implementation.ApplicationGatewayRequestRoutingRuleInner;
import com.microsoft.azure.management.network.model.HasFrontendPort;
import com.microsoft.azure.management.network.model.HasProtocol;
import com.microsoft.azure.management.network.model.HasPublicIpAddress;
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
    HasFrontendPort {

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
        interface Blank<ParentT> extends WithFrontendListener<ParentT> {
        }

        /** The final stage of an application gateway request routing rule definition.
         * <p>
         * At this stage, any remaining optional settings can be specified, or the definition
         * can be attached to the parent application gateway definition using {@link WithAttach#attach()}.
         * @param <ParentT> the stage of the application gateway definition to return to after attaching this definition
         */
        interface WithAttach<ParentT> extends
            Attachable.InDefinition<ParentT> {
        }

        /**
         * The stage of an application gateway request routing rule definition allowing to specify the listener to associate the routing rule with.
         * @param <ParentT> the stage of the application gateway definition to return to after attaching this definition
         */
        interface WithFrontendListener<ParentT> {
            /**
             * Associates the request routing rule with an HTTP listener.
             * <p>
             * If no listener with the specified name exists yet, one will be created for port 80 and the HTTP protocol by default.
             * @param name the name of an existing listener
             * @return the next stage of the definition
             */
            WithBackendHttpConfiguration<ParentT> fromFrontendListener(String name);

            /**
             * Associates the request routing rule with an existing frontend listener on this application gateway
             * associated with the specified port number and the HTTP protocol.
             * @param portNumber the port number used by an existing listener
             * @return the next stage of the definition, or null if the specified port number is already used for a non-HTTP protocol (e.g. HTTPS)
             */
            WithBackendHttpConfiguration<ParentT> fromFrontendHttpPort(int portNumber);

            /**
             * Associates the request routing rule with an existing frontend listener on this application gateway
             * associated with the specified port number and the HTTPS protocol.
             * @param portNumber the port number used by an existing listener
             * @return the next stage of the definition, or null if the specified port number is already used for a non-HTTPS protocol (e.g. HTTP)
             */
            WithSslCertificate<WithBackendHttpConfiguration<ParentT>> fromFrontendHttpsPort(int portNumber);
        }

        /**
         * The stage of an application gateway request routing rule allowing to specify an SSL certificate.
         * @param <ParentT> the next stage of the definition
         */
        interface WithSslCertificate<ParentT> extends HasSslCertificate.DefinitionStages.WithSslCertificate<ParentT> {
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
    }

    /** The entirety of an application gateway request routing rule definition.
     * @param <ParentT> the return type of the final {@link DefinitionStages.WithAttach#attach()}
     */
    interface Definition<ParentT> extends
        DefinitionStages.Blank<ParentT>,
        DefinitionStages.WithAttach<ParentT>,
        DefinitionStages.WithFrontendListener<ParentT>,
        DefinitionStages.WithBackend<ParentT>,
        DefinitionStages.WithBackendHttpConfiguration<ParentT>,
        DefinitionStages.WithSslCertificate<DefinitionStages.WithBackendHttpConfiguration<ParentT>>,
        DefinitionStages.WithSslPassword<DefinitionStages.WithBackendHttpConfiguration<ParentT>> {
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
