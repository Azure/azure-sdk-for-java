/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.apigeneration.Method;
import com.microsoft.azure.management.network.implementation.ApplicationGatewayBackendHttpSettingsInner;
import com.microsoft.azure.management.network.model.HasPort;
import com.microsoft.azure.management.network.model.HasProtocol;
import com.microsoft.azure.management.resources.fluentcore.arm.models.ChildResource;
import com.microsoft.azure.management.resources.fluentcore.model.Attachable;
import com.microsoft.azure.management.resources.fluentcore.model.Settable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;

/**
 * An immutable client-side representation of an application gateway's backend HTTP configuration.
 */
@Fluent()
public interface ApplicationGatewayBackendHttpConfiguration extends
    Wrapper<ApplicationGatewayBackendHttpSettingsInner>,
    ChildResource<ApplicationGateway>,
    HasProtocol<ApplicationGatewayProtocol>,
    HasPort {

    /**
     * @return true if cookie based affinity (sticky sessions) is enabled, else false
     */
    boolean cookieBasedAffinity();

    /**
     * @return HTTP request timeout in seconds. Requests will fail if no response is received within the specified time.
     */
    int requestTimeout();

    //TODO ApplicationGatewayProbe probe();
    //TODO Map<String, ApplicationGatewayCertificate> authenticationCertificates()

    /**
     * Grouping of application gateway backend HTTP configuration stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of an application gateway backend HTTP configuration.
         * @param <ParentT> the stage of the parent application gateway definition to return to after attaching this definition
         */
        interface Blank<ParentT> extends WithAttach<ParentT> {
        }

        /**
         * The stage of an application gateway backend HTTP configuration allowing to specify the port number.
         * @param <ParentT> the stage of the parent application gateway definition to return to after attaching this definition
         */
        interface WithPort<ParentT> extends HasPort.DefinitionStages.WithPort<WithAttach<ParentT>> {
        }

        /**
         * The stage of an application gateway backend HTTP configuration allowing to specify the protocol.
         * @param <ParentT> the stage of the parent application gateway definition to return to after attaching this definition
         */
        interface WithProtocol<ParentT> extends HasProtocol.DefinitionStages.WithProtocol<WithAttach<ParentT>, ApplicationGatewayProtocol> {
        }

        /**
         * The stage of an application gateway backend HTTP configuration allowing to enable cookie based affinity.
         * @param <ParentT> the stage of the parent application gateway definition to return to after attaching this definition
         */
        interface WithAffinity<ParentT> {
            /**
             * Enables cookie based affinity.
             * @return the next stage of the update
             */
            @Method
            WithAttach<ParentT> withCookieBasedAffinity();
        }

        /**
         * The stage of an application gateway backend HTTP configuration allowing to specify the request timeout.
         * @param <ParentT> the stage of the parent application gateway definition to return to after attaching this definition
         */
        interface WithRequestTimeout<ParentT> {
            /**
             * Specifies the request timeout.
             * @param seconds a number of seconds
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withRequestTimeout(int seconds);
        }

        /** The final stage of an application gateway backend HTTP configuration.
         * <p>
         * At this stage, any remaining optional settings can be specified, or the definition
         * can be attached to the parent application gateway definition using {@link WithAttach#attach()}.
         * @param <ParentT> the stage of the parent application gateway definition to return to after attaching this definition
         */
        interface WithAttach<ParentT> extends
            Attachable.InDefinition<ParentT>,
            WithPort<ParentT>,
            WithAffinity<ParentT>,
            WithProtocol<ParentT>,
            WithRequestTimeout<ParentT> {
        }
    }

    /** The entirety of an application gateway backend HTTP configuration definition.
     * @param <ParentT> the stage of the parent application gateway definition to return to after attaching this definition
     */
    interface Definition<ParentT> extends
        DefinitionStages.Blank<ParentT>,
        DefinitionStages.WithAttach<ParentT> {
    }

    /**
     * Grouping of application gateway backend HTTP configuration update stages.
     */
    interface UpdateStages {
        /**
         * The stage of an application gateway backend HTTP configuration allowing to specify the port number.
         */
        interface WithPort extends HasPort.UpdateStages.WithPort<Update> {
        }

        /**
         * The stage of an application gateway backend HTTP configuration allowing to enable or disable cookie based affinity.
         */
        interface WithAffinity {
            /**
             * Enables cookie based affinity.
             * @return the next stage of the update
             */
            @Method
            Update withCookieBasedAffinity();

            /**
             * Disables cookie based affinity.
             * @return the next stage of the update.
             */
            @Method
            Update withoutCookieBasedAffinity();
        }

        /**
         * The stage of an application gateway backend HTTP configuration allowing to specify the protocol.
         */
        interface WithProtocol extends HasProtocol.UpdateStages.WithProtocol<Update, ApplicationGatewayProtocol> {
        }

        /**
         * The stage of an application gateway backend HTTP configuration allowing to specify the request timeout.
         */
        interface WithRequestTimeout {
            /**
             * Specifies the request timeout.
             * @param seconds a number of seconds
             * @return the next stage of the definition
             */
            Update withRequestTimeout(int seconds);
        }
    }

    /**
     * The entirety of an application gateway backend HTTP configuration update as part of an application gateway update.
     */
    interface Update extends
        Settable<ApplicationGateway.Update>,
        UpdateStages.WithPort,
        UpdateStages.WithAffinity,
        UpdateStages.WithProtocol,
        UpdateStages.WithRequestTimeout {
    }

    /**
     * Grouping of application gateway backend HTTP configuration definition stages applicable as part of an application gateway update.
     */
    interface UpdateDefinitionStages {
        /**
         * The first stage of an application gateway backend HTTP configuration definition.
         * @param <ParentT> the stage of the parent application gateway definition to return to after attaching this definition
         */
        interface Blank<ParentT> extends WithAttach<ParentT> {
        }

        /** The final stage of an application gateway backend HTTP configuration definition.
         * <p>
         * At this stage, any remaining optional settings can be specified, or the definition
         * can be attached to the parent application gateway definition using {@link WithAttach#attach()}.
         * @param <ParentT> the stage of the parent application gateway definition to return to after attaching this definition
         */
        interface WithAttach<ParentT> extends
            Attachable.InUpdate<ParentT>,
            WithPort<ParentT>,
            WithAffinity<ParentT>,
            WithProtocol<ParentT>,
            WithRequestTimeout<ParentT> {
        }

        /**
         * The stage of an application gateway backend HTTP configuration allowing to specify the port number.
         * @param <ParentT> the stage of the parent application gateway definition to return to after attaching this definition
         */
        interface WithPort<ParentT> extends HasPort.UpdateDefinitionStages.WithPort<WithAttach<ParentT>> {
        }

        /**
         * The stage of an application gateway backend HTTP configuration allowing to specify the request timeout.
         * @param <ParentT> the stage of the parent application gateway definition to return to after attaching this definition
         */
        interface WithRequestTimeout<ParentT> {
            /**
             * Specifies the request timeout.
             * @param seconds a number of seconds
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withRequestTimeout(int seconds);
        }

        /**
         * The stage of an application gateway backend HTTP configuration allowing to specify the protocol.
         * @param <ParentT> the stage of the parent application gateway definition to return to after attaching this definition
         */
        interface WithProtocol<ParentT> extends HasProtocol.UpdateDefinitionStages.WithProtocol<WithAttach<ParentT>, ApplicationGatewayProtocol> {
        }

        /**
         * The stage of an application gateway backend HTTP configuration allowing to enable or disable cookie based affinity.
         * @param <ParentT> the stage of the parent application gateway definition to return to after attaching this definition
         */
        interface WithAffinity<ParentT> {
            /**
             * Enables cookie based affinity.
             * @return the next stage of the update
             */
            @Method
            WithAttach<ParentT> withCookieBasedAffinity();

            /**
             * Disables cookie based affinity.
             * @return the next stage of the update
             */
            @Method
            WithAttach<ParentT> withoutCookieBasedAffinity();
        }
    }

    /** The entirety of an application gateway backend HTTP configuration definition as part of an application gateway update.
     * @param <ParentT> the stage of the parent application gateway definition to return to after attaching this definition
     */
    interface UpdateDefinition<ParentT> extends
        UpdateDefinitionStages.Blank<ParentT>,
        UpdateDefinitionStages.WithAttach<ParentT> {
    }
}
