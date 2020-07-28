// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.arm.models.ChildResource;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasSubnet;
import com.azure.resourcemanager.resources.fluentcore.model.Attachable;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;
import com.azure.resourcemanager.resources.fluentcore.model.Settable;

/** A client-side representation of an application gateway's HTTP listener. */
@Fluent()
public interface ApplicationGatewayListener
    extends HasInner<ApplicationGatewayHttpListener>,
        ChildResource<ApplicationGateway>,
        HasSslCertificate<ApplicationGatewaySslCertificate>,
        HasPublicIpAddress,
        HasProtocol<ApplicationGatewayProtocol>,
        HasHostname,
        HasServerNameIndication,
        HasSubnet {

    /** @return the frontend IP configuration this listener is associated with. */
    ApplicationGatewayFrontend frontend();

    /** @return the number of the frontend port the listener is listening on */
    int frontendPortNumber();

    /** @return the name of the frontend port the listener is listening on */
    String frontendPortName();

    /** Grouping of application gateway HTTP listener configuration stages. */
    interface DefinitionStages {
        /**
         * The first stage of an application gateway HTTP listener.
         *
         * @param <ParentT> the stage of the parent application gateway definition to return to after attaching this
         *     definition
         */
        interface Blank<ParentT> extends WithFrontend<ParentT> {
        }

        /**
         * The final stage of an application gateway HTTP listener.
         *
         * <p>At this stage, any remaining optional settings can be specified, or the definition can be attached to the
         * parent application gateway definition.
         *
         * @param <ParentT> the stage of the parent application gateway definition to return to after attaching this
         *     definition
         */
        interface WithAttach<ParentT>
            extends Attachable.InDefinition<ParentT>,
                WithProtocol<ParentT>,
                WithHostname<ParentT>,
                WithServerNameIndication<ParentT> {
        }

        /**
         * The stage of an application gateway frontend listener definition allowing to specify the frontend IP
         * configuration to associate the listener with.
         *
         * @param <ParentT> the stage of the parent application gateway definition to return to after attaching this
         *     definition
         */
        interface WithFrontend<ParentT> {
            /**
             * Associates the listener with the application gateway's private (internal) frontend.
             *
             * <p>If the private frontend does not exist yet, it will be created under an auto-generated name and
             * associated with the application gateway's subnet.
             *
             * @return the next stage of the definition
             */
            WithFrontendPort<ParentT> withPrivateFrontend();

            /**
             * Associates the listener with the application gateway's public (Internet-facing) frontend.
             *
             * <p>If the public frontend does not exist yet, it will be created under an auto-generated name and
             * associated with the application gateway's public IP address.
             *
             * @return the next stage of the definition
             */
            WithFrontendPort<ParentT> withPublicFrontend();
        }

        /**
         * The stage of an application gateway frontend listener definition allowing to specify the frontend port to
         * associate the listener with.
         *
         * @param <ParentT> the stage of the parent application gateway definition to return to after attaching this
         *     definition
         */
        interface WithFrontendPort<ParentT> {
            /**
             * Enables the listener to listen on the specified existing frontend port.
             *
             * @param name the name of an existing frontend port
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withFrontendPort(String name);

            /**
             * Enables the listener to listen on the specified frontend port number.
             *
             * <p>If a frontend port for this port number does not yet exist, a new will be created with an
             * auto-generated name.
             *
             * @param portNumber a port number
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withFrontendPort(int portNumber);
        }

        /**
         * The stage of an application gateway frontend listener definition allowing to specify the SSL certificate to
         * associate with the listener.
         *
         * @param <ParentT> the stage of the parent application gateway definition to return to after attaching this
         *     definition
         */
        interface WithSslCertificate<ParentT>
            extends HasSslCertificate.DefinitionStages.WithSslCertificate<WithAttach<ParentT>> {
        }

        /**
         * The stage of an application gateway frontend listener definition allowing to specify the password for the
         * private key of the imported SSL certificate.
         *
         * @param <ParentT> the stage of the parent application gateway definition to return to after attaching this
         *     definition
         */
        interface WithSslPassword<ParentT>
            extends HasSslCertificate.DefinitionStages.WithSslPassword<WithAttach<ParentT>> {
        }

        /**
         * The stage of an application gateway frontend listener definition allowing to specify the protocol.
         *
         * @param <ParentT> the stage of the parent application gateway definition to return to after attaching this
         *     definition
         */
        interface WithProtocol<ParentT> {
            /**
             * Specifies that the listener is for the HTTP protocol.
             *
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withHttp();

            /**
             * Specifies that the listener is for the HTTPS protocol.
             *
             * @return the next stage of the definition
             */
            WithSslCertificate<ParentT> withHttps();
        }

        /**
         * The stage of an application gateway frontend listener definition allowing to specify the hostname of the
         * website for which the traffic is received.
         *
         * @param <ParentT> the stage of the parent application gateway definition to return to after attaching this
         *     definition
         */
        interface WithHostname<ParentT> extends HasHostname.DefinitionStages.WithHostname<WithAttach<ParentT>> {
        }

        /**
         * The stage of an application gateway frontend listener definition allowing to require server name indication
         * (SNI).
         *
         * @param <ParentT> the stage of the parent application gateway definition to return to after attaching this
         *     definition
         */
        interface WithServerNameIndication<ParentT>
            extends HasServerNameIndication.DefinitionStages.WithServerNameIndication<WithAttach<ParentT>> {
        }
    }

    /**
     * The entirety of an application gateway HTTP listener definition.
     *
     * @param <ParentT> the stage of the parent application gateway definition to return to after attaching this
     *     definition
     */
    interface Definition<ParentT>
        extends DefinitionStages.Blank<ParentT>,
            DefinitionStages.WithAttach<ParentT>,
            DefinitionStages.WithFrontend<ParentT>,
            DefinitionStages.WithFrontendPort<ParentT>,
            DefinitionStages.WithSslCertificate<ParentT>,
            DefinitionStages.WithSslPassword<ParentT>,
            DefinitionStages.WithHostname<ParentT> {
    }

    /** Grouping of application gateway HTTP listener update stages. */
    interface UpdateStages {
        /**
         * The stage of an application gateway frontend listener update allowing to specify the frontend IP
         * configuration to associate the listener with.
         */
        interface WithFrontend {
            /**
             * Associates the listener with the application gateway's private (internal) frontend.
             *
             * <p>If the private frontend does not exist yet, it will be created under an auto-generated name and
             * associated with the application gateway's subnet.
             *
             * @return the next stage of the update
             */
            Update withPrivateFrontend();

            /**
             * Associates the listener with the application gateway's public (Internet-facing) frontend.
             *
             * <p>If the public frontend does not exist yet, it will be created under an auto-generated name and
             * associated with the application gateway's public IP address.
             *
             * @return the next stage of the update
             */
            Update withPublicFrontend();
        }

        /**
         * The stage of an application gateway frontend listener update allowing to specify the frontend port to
         * associate the listener with.
         */
        interface WithFrontendPort {
            /**
             * Enables the listener to listen on the specified existing frontend port.
             *
             * @param name the name of an existing frontend port
             * @return the next stage of the definition
             */
            Update withFrontendPort(String name);

            /**
             * Enables the listener to listen on the specified frontend port number.
             *
             * <p>If a frontend port for this port number does not yet exist, a new will be created with an
             * auto-generated name.
             *
             * @param portNumber a port number
             * @return the next stage of the definition
             */
            Update withFrontendPort(int portNumber);
        }

        /**
         * The stage of an application gateway frontend listener update allowing to specify the SSL certificate to
         * associate with the listener.
         */
        interface WithSslCertificate extends HasSslCertificate.UpdateStages.WithSslCertificate<Update> {
        }

        /**
         * The stage of an application gateway frontend listener update allowing to specify the password for the private
         * key of the imported SSL certificate.
         */
        interface WithSslPassword extends HasSslCertificate.UpdateStages.WithSslPassword<Update> {
        }

        /** The stage of an application gateway frontend listener update allowing to specify the protocol. */
        interface WithProtocol {
            /**
             * Specifies that the listener is for the HTTP protocol.
             *
             * @return the next stage of the update
             */
            Update withHttp();

            /**
             * Specifies that the listener is for the HTTPS protocol.
             *
             * @return the next stage of the update
             */
            WithSslCertificate withHttps();
        }

        /**
         * The stage of an application gateway frontend listener update allowing to require server name indication
         * (SNI).
         */
        interface WithServerNameIndication
            extends HasServerNameIndication.UpdateStages.WithServerNameIndication<Update> {
        }

        /**
         * The stage of an application gateway frontend listener update allowing to specify the hostname of the website
         * for which the traffic is received.
         */
        interface WithHostname extends HasHostname.UpdateStages.WithHostname<Update> {
        }
    }

    /** The entirety of an application gateway HTTP listener update as part of an application gateway update. */
    interface Update
        extends Settable<ApplicationGateway.Update>,
            UpdateStages.WithServerNameIndication,
            UpdateStages.WithHostname,
            UpdateStages.WithProtocol,
            UpdateStages.WithSslCertificate,
            UpdateStages.WithSslPassword,
            UpdateStages.WithFrontendPort,
            UpdateStages.WithFrontend {
    }

    /**
     * Grouping of application gateway HTTP listener definition stages applicable as part of an application gateway
     * update.
     */
    interface UpdateDefinitionStages {
        /**
         * The first stage of an application gateway HTTP listener configuration definition.
         *
         * @param <ParentT> the stage of the parent application gateway definition to return to after attaching this
         *     definition
         */
        interface Blank<ParentT> extends WithFrontend<ParentT> {
        }

        /**
         * The stage of an application gateway frontend listener definition allowing to specify the frontend IP
         * configuration to associate the listener with.
         *
         * @param <ParentT> the stage of the parent application gateway definition to return to after attaching this
         *     definition
         */
        interface WithFrontend<ParentT> {
            /**
             * Associates the listener with the application gateway's private (internal) frontend.
             *
             * <p>If the private frontend does not exist yet, it will be created under an auto-generated name and
             * associated with the application gateway's subnet.
             *
             * @return the next stage of the definition
             */
            WithFrontendPort<ParentT> withPrivateFrontend();

            /**
             * Associates the listener with the application gateway's public (Internet-facing) frontend.
             *
             * <p>If the public frontend does not exist yet, it will be created under an auto-generated name and
             * associated with the application gateway's public IP address.
             *
             * @return the next stage of the definition
             */
            WithFrontendPort<ParentT> withPublicFrontend();
        }

        /**
         * The stage of an application gateway frontend listener definition allowing to specify the frontend port to
         * associate the listener with.
         *
         * @param <ParentT> the stage of the parent application gateway definition to return to after attaching this
         *     definition
         */
        interface WithFrontendPort<ParentT> {
            /**
             * Enables the listener to listen on the specified existing frontend port.
             *
             * @param name the name of an existing frontend port
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withFrontendPort(String name);

            /**
             * Enables the listener to listen on the specified frontend port number.
             *
             * <p>If a frontend port for this port number does not yet exist, a new will be created with an
             * auto-generated name.
             *
             * @param portNumber a port number
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withFrontendPort(int portNumber);
        }

        /**
         * The stage of an application gateway frontend listener definition allowing to specify the protocol.
         *
         * @param <ParentT> the stage of the parent application gateway definition to return to after attaching this
         *     definition
         */
        interface WithProtocol<ParentT> {
            /**
             * Specifies that the listener is for the HTTP protocol.
             *
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withHttp();

            /**
             * Specifies that the listener is for the HTTPS protocol.
             *
             * @return the next stage of the definition
             */
            WithSslCertificate<ParentT> withHttps();
        }

        /**
         * The stage of an application gateway frontend listener definition allowing to specify the SSL certificate to
         * associate with the listener.
         *
         * @param <ParentT> the stage of the parent application gateway definition to return to after attaching this
         *     definition
         */
        interface WithSslCertificate<ParentT>
            extends HasSslCertificate.UpdateDefinitionStages.WithSslCertificate<WithAttach<ParentT>> {
        }

        /**
         * The stage of an application gateway frontend listener definition allowing to specify the password for the
         * private key of the imported SSL certificate.
         *
         * @param <ParentT> the stage of the parent application gateway definition to return to after attaching this
         *     definition
         */
        interface WithSslPassword<ParentT>
            extends HasSslCertificate.UpdateDefinitionStages.WithSslPassword<WithAttach<ParentT>> {
        }

        /**
         * The stage of an application gateway frontend listener definition allowing to specify the hostname of the
         * website for which the traffic is received.
         *
         * @param <ParentT> the stage of the parent application gateway definition to return to after attaching this
         *     definition
         */
        interface WithHostname<ParentT> extends HasHostname.UpdateDefinitionStages.WithHostname<WithAttach<ParentT>> {
        }

        /**
         * The stage of an application gateway frontend listener definition allowing to require server name indication
         * (SNI).
         *
         * @param <ParentT> the stage of the parent application gateway definition to return to after attaching this
         *     definition
         */
        interface WithServerNameIndication<ParentT>
            extends HasServerNameIndication.UpdateDefinitionStages.WithServerNameIndication<WithAttach<ParentT>> {
        }

        /**
         * The final stage of an application gateway HTTP listener definition.
         *
         * <p>At this stage, any remaining optional settings can be specified, or the definition can be attached to the
         * parent application gateway definition.
         *
         * @param <ParentT> the stage of the parent application gateway definition to return to after attaching this
         *     definition
         */
        interface WithAttach<ParentT>
            extends Attachable.InUpdate<ParentT>,
                UpdateDefinitionStages.WithServerNameIndication<ParentT>,
                UpdateDefinitionStages.WithHostname<ParentT>,
                UpdateDefinitionStages.WithProtocol<ParentT> {
        }
    }

    /**
     * The entirety of an application gateway HTTP listener definition as part of an application gateway update.
     *
     * @param <ParentT> the stage of the parent application gateway definition to return to after attaching this
     *     definition
     */
    interface UpdateDefinition<ParentT>
        extends UpdateDefinitionStages.Blank<ParentT>,
            UpdateDefinitionStages.WithFrontend<ParentT>,
            UpdateDefinitionStages.WithFrontendPort<ParentT>,
            UpdateDefinitionStages.WithAttach<ParentT>,
            UpdateDefinitionStages.WithSslCertificate<ParentT>,
            UpdateDefinitionStages.WithSslPassword<ParentT> {
    }
}
