/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Beta.SinceVersion;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.apigeneration.Method;
import com.microsoft.azure.management.network.implementation.ApplicationGatewayBackendHttpSettingsInner;
import com.microsoft.azure.management.network.model.HasPort;
import com.microsoft.azure.management.network.model.HasProtocol;
import com.microsoft.azure.management.resources.fluentcore.arm.models.ChildResource;
import com.microsoft.azure.management.resources.fluentcore.model.Attachable;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;
import com.microsoft.azure.management.resources.fluentcore.model.Settable;

/**
 * A client-side representation of an application gateway's backend HTTP configuration.
 */
@Fluent()
public interface ApplicationGatewayBackendHttpConfiguration extends
    HasInner<ApplicationGatewayBackendHttpSettingsInner>,
    ChildResource<ApplicationGateway>,
    HasProtocol<ApplicationGatewayProtocol>,
    HasPort {
    /**
     * @return authentication certificates associated with this backend HTTPS configuration
     */
    @Beta(SinceVersion.V1_4_0)
    Map<String, ApplicationGatewayAuthenticationCertificate> authenticationCertificates();

    /**
     * @return true if cookie based affinity (sticky sessions) is enabled, else false
     */
    boolean cookieBasedAffinity();

    /**
     * @return HTTP request timeout in seconds. Requests will fail if no response is received within the specified time.
     */
    int requestTimeout();

    /**
     * @return the probe associated with this backend
     */
    ApplicationGatewayProbe probe();

    /**
     * @return host header to be sent to the backend servers
     */
    @Beta(SinceVersion.V1_4_0)
    String hostHeader();

    /**
     * @return whether the host header should come from the host name of the backend server
     */
    @Beta(SinceVersion.V1_4_0)
    boolean isHostHeaderFromBackend();

    /**
     * @return true if the probe is enabled
     */
    @Beta(SinceVersion.V1_4_0)
    boolean isProbeEnabled();

    /**
     * @return if 0 then connection draining is not enabled, otherwise if between 1 and 3600, then the number of seconds when connection draining is active
     */
    @Beta(SinceVersion.V1_4_0)
    int connectionDrainingTimeoutInSeconds();

    /**
     * @return name used for the affinity cookie
     */
    @Beta(SinceVersion.V1_4_0)
    String affinityCookieName();

    /**
     * @return the path, if any, used as a prefix for all HTTP requests
     */
    @Beta(SinceVersion.V1_4_0)
    String path();

    /**
     * Grouping of application gateway backend HTTPS configuration stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of an application gateway backend HTTP configuration.
         * @param <ReturnT> the stage of the parent application gateway definition to return to after attaching this definition
         */
        interface Blank<ReturnT> extends WithAttach<ReturnT> {
        }

        /**
         * The stage of an application gateway backend HTTP configuration allowing to specify the port number.
         * @param <ReturnT> the stage of the parent application gateway definition to return to after attaching this definition
         */
        interface WithPort<ReturnT> extends HasPort.DefinitionStages.WithPort<WithAttach<ReturnT>> {
        }

        /**
         * The stage of an application gateway backend HTTP configuration allowing to specify the protocol.
         * @param <ReturnT> the stage of the parent application gateway definition to return to after attaching this definition
         */
        interface WithProtocol<ReturnT> {
            /**
             * Specifies the transport protocol.
             * @param protocol a transport protocol
             * @return the next stage of the definition
             * @deprecated use {@link #withHttps()} instead (HTTP is the default)
             */
            @Deprecated
            WithAttach<ReturnT> withProtocol(ApplicationGatewayProtocol protocol);

            /**
             * Specifies HTTPS as the protocol.
             * @return the next stage of the definition
             */
            @Method
            @Beta(SinceVersion.V1_4_0)
            WithAttachAndAuthCert<ReturnT> withHttps();
        }

        /**
         * The stage of an application gateway backend HTTP configuration allowing to enable cookie based affinity.
         * @param <ReturnT> the stage of the parent application gateway definition to return to after attaching this definition
         */
        interface WithAffinity<ReturnT> {
            /**
             * Enables cookie based affinity.
             * @return the next stage of the update
             */
            @Method
            WithAttach<ReturnT> withCookieBasedAffinity();
        }

        /**
         * The stage of an application gateway backend HTTP configuration allowing to specify the name for the affinity cookie.
         * @param <ReturnT> the stage of the parent application gateway definition to return to after attaching this definition
         */
        interface WithCookieName<ReturnT> {
            /**
             * Specifies the name for the affinity cookie.
             * @param name a name
             * @return the next stage of the definition
             */
            @Beta(SinceVersion.V1_4_0)
            WithAttach<ReturnT> withAffinityCookieName(String name);
        }

        /**
         * The stage of an application gateway backend HTTP configuration allowing to specify the path to use as the prefix for all HTTP requests.
         * @param <ReturnT> the stage of the parent application gateway definition to return to after attaching this definition
         */
        interface WithPath<ReturnT> {
            /**
             * Specifies the path prefix for all HTTP requests.
             * @param path a path
             * @return the next stage of the definition
             */
            @Beta(SinceVersion.V1_4_0)
            WithAttach<ReturnT> withPath(String path);
        }

        /**
         * The stage of an application gateway backend HTTP configuration allowing to specify the request timeout.
         * @param <ReturnT> the stage of the parent application gateway definition to return to after attaching this definition
         */
        interface WithRequestTimeout<ReturnT> {
            /**
             * Specifies the request timeout.
             * @param seconds a number of seconds
             * @return the next stage of the definition
             */
            WithAttach<ReturnT> withRequestTimeout(int seconds);
        }

        /**
         * The stage of an application gateway backend HTTP configuration allowing to associate an existing probe.
         * @param <ReturnT> the stage of the parent application gateway definition to return to after attaching this definition
         */
        interface WithProbe<ReturnT> {
            /**
             * Specifies an existing probe on this application gateway to associate with this backend.
             * <p>
             * If the probe with the specified name does not yet exist, it must be defined separately in the optional part
             * of the application gateway definition. This only adds a reference to the probe by its name.
             * @param name the name of an existing probe
             * @return the next stage of the definition
             */
            WithAttach<ReturnT> withProbe(String name);
        }

        /**
         * The stage of an application gateway backend HTTP configuration allowing to specify the host header.
         * @param <ReturnT> the stage of the parent application gateway definition to return to after attaching this definition
         */
        interface WithHostHeader<ReturnT> {
            /**
             * Specifies that the host header should come from the host name of the backend server.
             * @return the next stage of the definition
             */
            @Beta(SinceVersion.V1_4_0)
            WithAttach<ReturnT> withHostHeaderFromBackend();

            /**
             * Specifies the host header.
             * @param hostHeader the host header
             * @return the next stage of the definition
             */
            @Beta(SinceVersion.V1_4_0)
            WithAttach<ReturnT> withHostHeader(String hostHeader);
        }

        /**
         * The stage of an application gateway backend HTTP configuration allowing to control connection draining.
         * @param <ReturnT> the stage of the parent application gateway definition to return to after attaching this definition
         */
        interface WithConnectionDraining<ReturnT> {
            /**
             * Specifies the number of seconds when connection draining is active.
             * @param seconds a number of seconds between 1 and 3600
             * @return the next stage of the definition
             */
            @Beta(SinceVersion.V1_4_0)
            WithAttach<ReturnT> withConnectionDrainingTimeoutInSeconds(int seconds);
        }

        /**
         * The stage of an application gateway backend HTTP configuration allowing to add an authentication certificate.
         * @param <ReturnT> the stage of the parent application gateway definition to return to after attaching this definition
         */
        interface WithAuthenticationCertificate<ReturnT> {
            /**
             * Associates the specified authentication certificate that exists on this application gateway with this backend HTTP confifuration.
             * <p>
             * Multiple calls to this method will add additional certificate references.
             * @param name the name of an existing authentication certificate
             * @return the next stage of the definition
             */
            @Beta(SinceVersion.V1_4_0)
            WithAttachAndAuthCert<ReturnT> withAuthenticationCertificate(String name);

            /**
             * Associates a new, automatically named certificate with this HTTP backend configuration based on the specified data.
             * <p>
             * Multiple calls to this method will add additional certificate references.
             * @param derData the DER-encoded data of an X.509 certificate
             * @return the next stage of the definition
             */
            @Beta(SinceVersion.V1_4_0)
            WithAttachAndAuthCert<ReturnT> withAuthenticationCertificateFromBytes(byte[] derData);

            /**
             * Associates a new, automatically named certificate with this HTTP backend configuration loaded from the specified file.
             * <p>
             * Multiple calls to this method will add additional certificate references.
             * @param certificateFile a file containing the DER format representation of an X.509 certificate
             * @return the next stage of the definition
             * @throws IOException when there are issues reading from the specified file
             */
            @Beta(SinceVersion.V1_4_0)
            WithAttachAndAuthCert<ReturnT> withAuthenticationCertificateFromFile(File certificateFile) throws IOException;

            /**
             * Associates a new, automatically named certificate with this HTTP backend configuration loaded from the specified file.
             * <p>
             * Multiple calls to this method will add additional certificate references.
             * @param base64Data the base-64 encoded data of an X.509 certificate
             * @return the next stage of the definition
             */
            @Beta(SinceVersion.V1_4_0)
            WithAttachAndAuthCert<ReturnT> withAuthenticationCertificateFromBase64(String base64Data);
        }

        /**
         * The stage of an application gateway backend HTTP configuration allowing to add an authentication certificate,
         * specify other options or attach to the parent application gateway definition.
         * @param <ReturnT> the stage of the parent application gateway definition to return to after attaching this definition
         */
        interface WithAttachAndAuthCert<ReturnT> extends WithAttach<ReturnT>, WithAuthenticationCertificate<ReturnT> {
        }

        /** The final stage of an application gateway backend HTTP configuration.
         * <p>
         * At this stage, any remaining optional settings can be specified, or the definition
         * can be attached to the parent application gateway definition using {@link WithAttach#attach()}.
         * @param <ReturnT> the stage of the parent application gateway definition to return to after attaching this definition
         */
        interface WithAttach<ReturnT> extends
            Attachable.InDefinition<ReturnT>,
            WithPort<ReturnT>,
            WithAffinity<ReturnT>,
            WithProtocol<ReturnT>,
            WithRequestTimeout<ReturnT>,
            WithProbe<ReturnT>,
            WithHostHeader<ReturnT>,
            WithConnectionDraining<ReturnT>,
            WithCookieName<ReturnT>,
            WithPath<ReturnT> {
        }
    }

    /** The entirety of an application gateway backend HTTPS configuration definition.
     * @param <ReturnT> the stage of the parent application gateway definition to return to after attaching this definition
     */
    interface Definition<ReturnT> extends
        DefinitionStages.Blank<ReturnT>,
        DefinitionStages.WithAttach<ReturnT>,
        DefinitionStages.WithAttachAndAuthCert<ReturnT> {
    }

    /**
     * Grouping of application gateway backend HTTPS configuration update stages.
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
        interface WithProtocol {
            /**
             * Specifies the transport protocol.
             * @param protocol a transport protocol
             * @return the next stage of the update
             * @deprecated use {@link #withHttp()} or {@link #withHttps()} instead
             */
            @Deprecated
            Update withProtocol(ApplicationGatewayProtocol protocol);

            /**
             * Specifies HTTPS as the protocol.
             * @return the next stage of the update
             */
            @Method
            @Beta(SinceVersion.V1_4_0)
            Update withHttps();

            /**
             * Specifies HTTP as the protocol.
             * @return the next stage of the update
             */
            @Method
            @Beta(SinceVersion.V1_4_0)
            Update withHttp();
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

        /**
         * The stage of an application gateway backend HTTP configuration allowing to associate an existing probe.
         */
        interface WithProbe {
            /**
             * Specifies an existing probe on this application gateway to associate with this backend.
             * <p>
             * If the probe with the specified name does not yet exist, it must be defined separately in the optional part
             * of the application gateway definition. This only adds a reference to the probe by its name.
             * @param name the name of an existing probe
             * @return the next stage of the update
             */
            Update withProbe(String name);

            /**
             * Removes the association with a probe.
             * @return the next stage of the update
             */
            @Method
            Update withoutProbe();
        }

        /**
         * The stage of an application gateway backend HTTP configuration allowing to specify the host header.
         */
        interface WithHostHeader {
            /**
             * Specifies that the host header should come from the host name of the backend server.
             * @return the next stage of the update
             */
            @Beta(SinceVersion.V1_4_0)
            Update withHostHeaderFromBackend();

            /**
             * Specifies that no host header should be used.
             * @return the next stage of the update
             */
            @Beta(SinceVersion.V1_4_0)
            Update withoutHostHeader();

            /**
             * Specifies the host header.
             * @param hostHeader the host header
             * @return the next stage of the definition
             */
            @Beta(SinceVersion.V1_4_0)
            Update withHostHeader(String hostHeader);
        }

        /**
         * The stage of an application gateway backend HTTP configuration allowing to control connection draining.
         */
        interface WithConnectionDraining {
            /**
             * Specifies the number of seconds when connection draining is active.
             * @param seconds a number of seconds between 1 and 3600
             * @return the next stage of the update
             */
            @Beta(SinceVersion.V1_4_0)
            Update withConnectionDrainingTimeoutInSeconds(int seconds);

            /**
             * Disables connection draining.
             * @return the next stage of the update
             */
            @Beta(SinceVersion.V1_4_0)
            Update withoutConnectionDraining();
        }

        /**
         * The stage of an application gateway backend HTTP configuration allowing to specify the name for the affinity cookie.
         */
        interface WithCookieName {
            /**
             * Specifies the name for the affinity cookie.
             * @param name a name
             * @return the next stage of the update
             */
            @Beta(SinceVersion.V1_4_0)
            Update withAffinityCookieName(String name);
        }

        /**
         * The stage of an application gateway backend HTTP configuration allowing to specify the path to use as the prefix for all HTTP requests.
         */
        interface WithPath {
            /**
             * Specifies the path prefix for all HTTP requests.
             * @param path a path
             * @return the next stage of the update
             */
            @Beta(SinceVersion.V1_4_0)
            Update withPath(String path);
        }

        /**
         * The stage of an application gateway backend HTTP configuration allowing to add an authentication certificate.
         */
        interface WithAuthenticationCertificate {
            /**
             * Associates the specified authentication certificate that exists on this application gateway with this backend HTTP confifuration.
             * <p>
             * Multiple calls to this method will add additional certificate references.
             * @param name the name of an existing authentication certificate
             * @return the next stage of the update
             */
            @Beta(SinceVersion.V1_4_0)
            Update withAuthenticationCertificate(String name);

            /**
             * Associates a new, automatically named certificate with this HTTP backend configuration based on the specified data.
             * <p>
             * Multiple calls to this method will add additional certificate references.
             * @param derData the DER-encoded data of an X.509 certificate
             * @return the next stage of the update
             */
            @Beta(SinceVersion.V1_4_0)
            Update withAuthenticationCertificateFromBytes(byte[] derData);

            /**
             * Associates a new, automatically named certificate with this HTTP backend configuration loaded from the specified file.
             * @param certificateFile a file containing the DER representation of an X.509 certificate
             * @return the next stage of the update
             * @throws IOException when there are issues reading the specified file
             */
            @Beta(SinceVersion.V1_4_0)
            Update withAuthenticationCertificateFromFile(File certificateFile) throws IOException;

            /**
             * Associates a new, automatically named certificate with this HTTP backend configuration loaded from the specified file.
             * <p>
             * Multiple calls to this method will add additional certificate references.
             * @param base64Data the base-64 encoded data of an X.509 certificate
             * @return the next stage of the update
             */
            @Beta(SinceVersion.V1_4_0)
            Update withAuthenticationCertificateFromBase64(String base64Data);

            /**
             * Removes the reference to the specified authentication certificate from this HTTP backend configuration.
             * <p>
             * Note the certificate will remain associated with the application gateway until removed from it explicitly.
             * @param name the name of an existing authentication certificate associated with this HTTP backend configuration
             * @return the next stage of the update
             */
            @Beta(SinceVersion.V1_4_0)
            Update withoutAuthenticationCertificate(String name);

            /**
             * Removes all references to any authentication certificates.
             * @return the next stage of the update
             */
            @Beta(SinceVersion.V1_4_0)
            @Method
            Update withoutAuthenticationCertificates();
        }
    }

    /**
     * The entirety of an application gateway backend HTTPS configuration update as part of an application gateway update.
     */
    interface Update extends
        Settable<ApplicationGateway.Update>,
        UpdateStages.WithPort,
        UpdateStages.WithAffinity,
        UpdateStages.WithProtocol,
        UpdateStages.WithRequestTimeout,
        UpdateStages.WithProbe,
        UpdateStages.WithHostHeader,
        UpdateStages.WithConnectionDraining,
        UpdateStages.WithCookieName,
        UpdateStages.WithPath,
        UpdateStages.WithAuthenticationCertificate {
    }

    /**
     * Grouping of application gateway backend HTTPS configuration definition stages applicable as part of an application gateway update.
     */
    interface UpdateDefinitionStages {
        /**
         * The first stage of an application gateway backend HTTP configuration definition.
         * @param <ReturnT> the stage of the parent application gateway update to return to after attaching this definition
         */
        interface Blank<ReturnT> extends WithAttach<ReturnT> {
        }

        /**
         * The stage of an application gateway backend HTTP configuration allowing to add an authentication certificate,
         * specify other options or attach to the parent application gateway update.
         * @param <ReturnT> the stage of the parent application gateway update to return to after attaching this definition
         */
        interface WithAttachAndAuthCert<ReturnT> extends WithAttach<ReturnT>, WithAuthenticationCertificate<ReturnT> {
        }

        /** The final stage of an application gateway backend HTTP configuration definition.
         * <p>
         * At this stage, any remaining optional settings can be specified, or the definition
         * can be attached to the parent application gateway definition using {@link WithAttach#attach()}.
         * @param <ReturnT> the stage of the parent application gateway definition to return to after attaching this definition
         */
        interface WithAttach<ReturnT> extends
            Attachable.InUpdate<ReturnT>,
            WithPort<ReturnT>,
            WithAffinity<ReturnT>,
            WithProtocol<ReturnT>,
            WithRequestTimeout<ReturnT>,
            WithHostHeader<ReturnT>,
            WithConnectionDraining<ReturnT>,
            WithCookieName<ReturnT>,
            WithPath<ReturnT>,
            WithAuthenticationCertificate<ReturnT> {
        }

        /**
         * The stage of an application gateway backend HTTP configuration allowing to add an authentication certificate.
         * @param <ReturnT> the stage of the parent application gateway update to return to after attaching this definition
         */
        interface WithAuthenticationCertificate<ReturnT> {
            /**
             * Associates the specified authentication certificate that exists on this application gateway with this backend HTTP confifuration.
             * <p>
             * Multiple calls to this method will add additional certificate references.
             * @param name the name of an existing authentication certificate
             * @return the next stage of the definition
             */
            @Beta(SinceVersion.V1_4_0)
            WithAttachAndAuthCert<ReturnT> withAuthenticationCertificate(String name);

            /**
             * Associates a new, automatically named certificate with this HTTP backend configuration based on the specified data.
             * <p>
             * Multiple calls to this method will add additional certificate references.
             * @param derData the DER encoded data of an X.509 certificate
             * @return the next stage of the definition
             */
            @Beta(SinceVersion.V1_4_0)
            WithAttachAndAuthCert<ReturnT> withAuthenticationCertificateFromBytes(byte[] derData);

            /**
             * Associates a new, automatically named certificate with this HTTP backend configuration loaded from the specified file.
             * <p>
             * Multiple calls to this method will add additional certificate references.
             * @param certificateFile a file containing the DER representation of an X.509 certificate
             * @return the next stage of the definition
             * @throws IOException when there are issues reading from the specified file
             */
            @Beta(SinceVersion.V1_4_0)
            WithAttachAndAuthCert<ReturnT> withAuthenticationCertificateFromFile(File certificateFile) throws IOException;

            /**
             * Associates a new, automatically named certificate with this HTTP backend configuration loaded from the specified file.
             * <p>
             * Multiple calls to this method will add additional certificate references.
             * @param base64Data the base-64 encoded data of an X.509 certificate
             * @return the next stage of the definition
             */
            @Beta(SinceVersion.V1_4_0)
            WithAttachAndAuthCert<ReturnT> withAuthenticationCertificateFromBase64(String base64Data);
        }

        /**
         * The stage of an application gateway backend HTTP configuration allowing to specify the port number.
         * @param <ReturnT> the stage of the parent application gateway update to return to after attaching this definition
         */
        interface WithPort<ReturnT> extends HasPort.UpdateDefinitionStages.WithPort<WithAttach<ReturnT>> {
        }

        /**
         * The stage of an application gateway backend HTTP configuration allowing to specify the request timeout.
         * @param <ReturnT> the stage of the parent application gateway update to return to after attaching this definition
         */
        interface WithRequestTimeout<ReturnT> {
            /**
             * Specifies the request timeout.
             * @param seconds a number of seconds
             * @return the next stage of the definition
             */
            WithAttach<ReturnT> withRequestTimeout(int seconds);
        }

        /**
         * The stage of an application gateway backend HTTP configuration allowing to specify the protocol.
         * @param <ReturnT> the stage of the parent application gateway update to return to after attaching this definition
         */
        interface WithProtocol<ReturnT> {
            /**
             * Specifies the transport protocol.
             * @param protocol a transport protocol
             * @return the next stage of the definition
             * @deprecated use {@link #withHttps()} instead (HTTP is the default)
             */
            @Deprecated
            WithAttach<ReturnT> withProtocol(ApplicationGatewayProtocol protocol);

            /**
             * Specifies HTTPS as the protocol.
             * @return the next stage of the definition
             */
            @Method
            @Beta(SinceVersion.V1_4_0)
            WithAttachAndAuthCert<ReturnT> withHttps();
        }

        /**
         * The stage of an application gateway backend HTTP configuration allowing to enable or disable cookie based affinity.
         * @param <ReturnT> the stage of the parent application gateway update to return to after attaching this definition
         */
        interface WithAffinity<ReturnT> {
            /**
             * Enables cookie based affinity.
             * @return the next stage of the update
             */
            @Method
            WithAttach<ReturnT> withCookieBasedAffinity();

            /**
             * Disables cookie based affinity.
             * @return the next stage of the update
             */
            @Method
            WithAttach<ReturnT> withoutCookieBasedAffinity();
        }

        /**
         * The stage of an application gateway backend HTTP configuration allowing to specify the name for the affinity cookie.
         * @param <ReturnT> the stage of the parent application gateway update to return to after attaching this definition
         */
        interface WithCookieName<ReturnT> {
            /**
             * Specifies the name for the affinity cookie.
             * @param name a name
             * @return the next stage of the definition
             */
            @Beta(SinceVersion.V1_4_0)
            WithAttach<ReturnT> withAffinityCookieName(String name);
        }

        /**
         * The stage of an application gateway backend HTTP configuration allowing to specify the path to use as the prefix for all HTTP requests.
         * @param <ReturnT> the stage of the parent application gateway update to return to after attaching this definition
         */
        interface WithPath<ReturnT> {
            /**
             * Specifies the path prefix for all HTTP requests.
             * @param path a path
             * @return the next stage of the definition
             */
            @Beta(SinceVersion.V1_4_0)
            WithAttach<ReturnT> withPath(String path);
        }

        /**
         * The stage of an application gateway backend HTTP configuration allowing to associate an existing probe.
         * @param <ReturnT> the stage of the parent application gateway update to return to after attaching this definition
         */
        interface WithProbe<ReturnT> {
            /**
             * Specifies an existing probe on this application gateway to associate with this backend.
             * <p>
             * If the probe with the specified name does not yet exist, it must be defined separately in the optional part
             * of the application gateway definition. This only adds a reference to the probe by its name.
             * @param name the name of an existing probe
             * @return the next stage of the definition
             */
            WithAttach<ReturnT> withProbe(String name);
        }

        /**
         * The stage of an application gateway backend HTTP configuration allowing to specify the host header.
         * @param <ReturnT> the stage of the parent application gateway update to return to after attaching this definition
         */
        interface WithHostHeader<ReturnT> {
            /**
             * Specifies the host header.
             * @param hostHeader the host header
             * @return the next stage of the definition
             */
            @Beta(SinceVersion.V1_4_0)
            WithAttach<ReturnT> withHostHeader(String hostHeader);

            /**
             * Specifies that the host header should come from the host name of the backend server.
             * @return the next stage of the definition
             */
            @Beta(SinceVersion.V1_4_0)
            WithAttach<ReturnT> withHostHeaderFromBackend();
        }

        /**
         * The stage of an application gateway backend HTTP configuration allowing to control connection draining.
         * @param <ReturnT> the stage of the parent application gateway update to return to after attaching this definition
         */
        interface WithConnectionDraining<ReturnT> {
            /**
             * Specifies the number of seconds when connection draining is active.
             * @param seconds a number of seconds between 1 and 3600
             * @return the next stage of the definition
             */
            @Beta(SinceVersion.V1_4_0)
            WithAttach<ReturnT> withConnectionDrainingTimeoutInSeconds(int seconds);
        }
    }

    /** The entirety of an application gateway backend HTTPS configuration definition as part of an application gateway update.
     * @param <ReturnT> the stage of the parent application gateway update to return to after attaching this definition
     */
    interface UpdateDefinition<ReturnT> extends
        UpdateDefinitionStages.Blank<ReturnT>,
        UpdateDefinitionStages.WithAttach<ReturnT>,
        UpdateDefinitionStages.WithAttachAndAuthCert<ReturnT> {
    }
}
