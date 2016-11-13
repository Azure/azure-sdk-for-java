/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network;

import java.io.File;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.network.implementation.ApplicationGatewayHttpListenerInner;
import com.microsoft.azure.management.resources.fluentcore.arm.models.ChildResource;
import com.microsoft.azure.management.resources.fluentcore.model.Attachable;
import com.microsoft.azure.management.resources.fluentcore.model.Settable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;

/**
 * An immutable client-side representation of an application gateway's HTTP listener.
 */
@Fluent()
public interface ApplicationGatewayHttpListener extends
    Wrapper<ApplicationGatewayHttpListenerInner>,
    ChildResource<ApplicationGateway> {

    /**
     * @return the frontend IP configuration this listenet is associated with.
     */
    ApplicationGatewayFrontend frontend();

    /**
     * @return the number of the frontend port the listener is listening on
     */
    int frontendPortNumber();

    /**
     * @return the name of the frontend port the listener is listening on
     */
    String frontendPortName();

    /**
     * @return the associated SSL certificate, if any
     */
    ApplicationGatewaySslCertificate sslCertificate();

    /**
     * @return the protocol the listener listens to
     */
    ApplicationGatewayProtocol protocol();

    /**
     * Grouping of application gateway HTTP listener configuration stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of an application gateway HTTP listener.
         * @param <ParentT> the return type of the final {@link WithAttach#attach()}
         */
        interface Blank<ParentT> extends WithFrontendPort<ParentT> {
        }

        /**
         * The final stage of an application gateway HTTP listener.
         * <p>
         * At this stage, any remaining optional settings can be specified, or the definition
         * can be attached to the parent application gateway definition using {@link WithAttach#attach()}.
         * @param <ParentT> the stage of the parent application gateway definition to return to after attaching
         */
        interface WithAttach<ParentT> extends
            Attachable.InDefinition<ParentT>,
            WithProtocol<ParentT> {
        }

        /**
         * The stage of an application gateway HTTP listener definition allowing to specify the frontend IP configuration to associate the listener with.
         * @param <ParentT> the stage of the parent application gateway definition to return to after attaching
         */
        // TODO Since Azure does not yet support multiple frontends, this needs to be revisited when it does
        interface WithFrontend<ParentT> {
            /**
             * Associates the HTTP listener with a frontend existing on this application gateway.
             * @param name the name of an existing frontend
             * @return the next stage of the definition
             */
            WithFrontendPort<ParentT> withFrontend(String name);
        }

        /**
         * The stage of an application gateway HTTP listener definition allowing to specify the frontend port to associate the listener with.
         * @param <ParentT> the stage of the parent application gateway definition to return to after attaching
         */
        interface WithFrontendPort<ParentT> {
            /**
             * Enables the listener to listen on the specified existing frontend port.
             * @param name the name of an existing frontend port
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withFrontendPort(String name);

            /**
             * Enables the listener to listen on the specified port number, creating a new
             * automatically-named port for it if needed, or associating it with an existing frontend port
             * that is already configured to listen on this port number.
             * @param portNumber a port number
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withFrontendPort(int portNumber);
        }

        /**
         * The stage of an application gateway HTTP listener definition allowing to specify the SSL certificate to associate with the listener.
         * @param <ParentT> the stage of the parent application gateway definition to return to after attaching
         */
        interface WithSslCertificate<ParentT> {
            /**
             * Specifies an SSL certificate to associate with this listener, if its protocol is HTTPS.
             * @param name the name of an existing SSL certificate associated with this application gateway
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withSslCertificate(String name);

            /**
             * Specifies the PFX file to import the SSL certificate from to associate with this listener to enable HTTPS.
             * <p>
             * The certificate will be named using an auto-generated name.
             * @param pfxFile an existing PFX file
             * @return the next stage of the definition
             */
            WithSslPassword<ParentT> withSslCertificateFromPfxFile(File pfxFile);

            /**
             * Specifies the PFX file to import the SSL certificate from to associate with this listener to enable HTTPS.
             * @param pfxFile an existing PFX file
             * @param name a new name for the certificate that will be used to reference this certificate
             * @return the next stage of the definition
             */
            WithSslPassword<ParentT> withSslCertificateFromPfxFile(File pfxFile, String name);
        }

        /**
         * The stage of an application gateway HTTP listener definition allowing to specify the password for the private key of the imported SSL certificate.
         * @param <ParentT> the stage of the parent application gateway definition to return to after attaching
         */
        interface WithSslPassword<ParentT> {
            /**
             * Specifies the password for the specified PFX file containing the private key of the imported SSL certificate.
             * @param password the password of the imported PFX file
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withSslCertificatePassword(String password);
        }

        /**
         * The stage of an application gateway HTTP listener definition allowing to specify the protocol.
         * @param <ParentT> the stage of the parent application gateway definition to return to after attaching
         */
        interface WithProtocol<ParentT> {
            /**
             * Specifies that the listener is for the HTTP protocol.
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withHttp();

            /**
             * Specifies that the listener is for the HTTPS protocol.
             * @return the next stage of the definition
             */
            WithSslCertificate<ParentT> withHttps();
        }
    }

    /** The entirety of an application gateway HTTP listener definition.
     * @param <ParentT> the return type of the final {@link DefinitionStages.WithAttach#attach()}
     */
    interface Definition<ParentT> extends
        DefinitionStages.Blank<ParentT>,
        DefinitionStages.WithAttach<ParentT>,
        DefinitionStages.WithFrontend<ParentT>,
        DefinitionStages.WithFrontendPort<ParentT>,
        DefinitionStages.WithSslCertificate<ParentT>,
        DefinitionStages.WithSslPassword<ParentT> {
    }

    /**
     * Grouping of application gateway HTTP listener update stages.
     */
    interface UpdateStages {
    }

    /**
     * The entirety of an application gateway HTTP listener update as part of an application gateway update.
     */
    interface Update extends
        Settable<ApplicationGateway.Update> {
    }

    /**
     * Grouping of application gateway HTTP listener definition stages applicable as part of an application gateway update.
     */
    interface UpdateDefinitionStages {
        /**
         * The first stage of an application gateway HTTP listener configuration definition.
         * @param <ParentT> the return type of the final {@link WithAttach#attach()}
         */
        interface Blank<ParentT> extends WithAttach<ParentT> {
        }

        /** The final stage of an application gateway HTTP listener definition.
         * <p>
         * At this stage, any remaining optional settings can be specified, or the definition
         * can be attached to the parent application gateway definition using {@link WithAttach#attach()}.
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithAttach<ParentT> extends
            Attachable.InUpdate<ParentT> {
        }
    }

    /** The entirety of an application gateway HTTP listener definition as part of an application gateway update.
     * @param <ParentT> the return type of the final {@link UpdateDefinitionStages.WithAttach#attach()}
     */
    interface UpdateDefinition<ParentT> extends
        UpdateDefinitionStages.Blank<ParentT>,
        UpdateDefinitionStages.WithAttach<ParentT> {
    }
}
