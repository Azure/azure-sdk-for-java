// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.model.Attachable;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;
import com.azure.resourcemanager.resources.fluentcore.model.Settable;
import java.io.File;
import java.io.IOException;

/** A client-side representation of point-to-site configuration for a virtual network gateway. */
@Fluent
public interface PointToSiteConfiguration extends HasInnerModel<VpnClientConfiguration> {
    /** Grouping of point-to-site configuration definition stages. */
    interface DefinitionStages {
        /**
         * The first stage of the point-to-site configuration definition.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface Blank<ParentT> extends WithAddressPool<ParentT> {
        }

        interface WithAddressPool<ParentT> {
            WithAuthenticationType<ParentT> withAddressPool(String addressPool);
        }

        /**
         * The stage of the point-to-site configuration definition allowing to specify authentication type.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithAuthenticationType<ParentT> extends WithAzureCertificate<ParentT> {
            /**
             * Specifies that RADIUS server will be used for authentication.
             *
             * @param serverIPAddress the radius server address
             * @param serverSecret the radius server secret
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withRadiusAuthentication(String serverIPAddress, String serverSecret);
        }

        /**
         * The stage of the point-to-site configuration definition allowing to add root certificate for Azure
         * authentication.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithAzureCertificate<ParentT> {
            /**
             * Specifies that Azure certificate authentication type will be used and certificate to use for Azure
             * authentication.
             *
             * @param name name of certificate
             * @param certificateData the certificate public data
             * @return the next stage of the definition
             */
            WithAttachAndAzureCertificate<ParentT> withAzureCertificate(String name, String certificateData);

            /**
             * Specifies that Azure certificate authentication type will be used and certificate to use for Azure
             * authentication.
             *
             * @param name name of certificate
             * @param certificateFile public Base64-encoded certificate file
             * @return the next stage of the definition
             * @throws IOException the IO Exception
             */
            WithAttachAndAzureCertificate<ParentT> withAzureCertificateFromFile(String name, File certificateFile)
                throws IOException;
        }

        interface WithRevokedCertificate<ParentT> {
            WithAttach<ParentT> withRevokedCertificate(String name, String thumbprint);
        }

        /**
         * The stage of a point-to-site configuration definition allowing to specify which tunnel type will be used.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithTunnelType<ParentT> {
            /**
             * Specifies that only SSTP tunnel type will be used.
             *
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withSstpOnly();
            /**
             * Specifies that only IKEv2 VPN tunnel type will be used.
             *
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withIkeV2Only();
        }

        /**
         * The final stage of the point-to-site configuration definition.
         *
         * <p>At this stage, any remaining optional settings can be specified, or the point-to-site configuration
         * definition can be attached to the parent virtual network gateway definition.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithAttach<ParentT> extends Attachable.InDefinition<ParentT>, WithTunnelType<ParentT> {
        }

        /**
         * The final stage of the point-to-site configuration definition.
         *
         * <p>At this stage, any remaining optional settings can be specified, or the point-to-site configuration
         * definition can be attached to the parent virtual network gateway definition.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithAttachAndAzureCertificate<ParentT>
            extends WithAttach<ParentT>,
                WithTunnelType<ParentT>,
                WithAzureCertificate<ParentT>,
                WithRevokedCertificate<ParentT> {
        }
    }

    /**
     * The entirety of a point-to-site configuration definition.
     *
     * @param <ParentT> the stage of the parent definition to return to after attaching this definition
     */
    interface Definition<ParentT>
        extends DefinitionStages.Blank<ParentT>,
            DefinitionStages.WithAuthenticationType<ParentT>,
            DefinitionStages.WithAddressPool<ParentT>,
            DefinitionStages.WithAttachAndAzureCertificate<ParentT> {
    }

    /** Grouping of point-to-site configuration update stages. */
    interface UpdateStages {
        /** The stage of the point-to-site configuration definition allowing to specify address pool. */
        interface WithAddressPool {
            /**
             * Specifies address pool.
             *
             * @param addressPool address pool
             * @return the next stage of the update
             */
            Update withAddressPool(String addressPool);
        }

        /** Specifies authentication type of the point-to-site configuration. */
        interface WithAuthenticationType extends WithAzureCertificate {
            /**
             * Specifies that RADIUS authentication type will be used.
             *
             * @param serverIPAddress the radius server address
             * @param serverSecret the radius server secret
             * @return the next stage of the update
             */
            Update withRadiusAuthentication(String serverIPAddress, String serverSecret);
        }

        /** Specifies Azure certificate for authentication. */
        interface WithAzureCertificate {
            /**
             * Specifies that Azure certificate authentication type will be used and certificate to use for Azure
             * authentication.
             *
             * @param name name of certificate
             * @param certificateData the certificate public data
             * @return the next stage of the update
             */
            Update withAzureCertificate(String name, String certificateData);

            /**
             * Specifies that azure certificate authentication type will be used and certificate to use for Azure
             * authentication.
             *
             * @param name name of certificate
             * @param certificateFile public Base64-encoded certificate file
             * @return the next stage of the update
             * @throws IOException the IO Exception
             */
            Update withAzureCertificateFromFile(String name, File certificateFile) throws IOException;

            /**
             * Removes attached azure certificate with specified name.
             *
             * @param name name of the certificate
             * @return the next stage of the update
             */
            Update withoutAzureCertificate(String name);
        }

        /** Specifies revoked certificate for azure authentication. */
        interface WithRevokedCertificate {
            /**
             * Specifies revoked certificate.
             *
             * @param name certificate name
             * @param thumbprint certificate thumbprint
             * @return the next stage of the update
             */
            Update withRevokedCertificate(String name, String thumbprint);
        }

        /** The stage of a point-to-site configuration definition allowing to specify which tunnel type will be used. */
        interface WithTunnelType {
            /**
             * Specifies that only SSTP tunnel type will be used.
             *
             * @return the next stage of the update
             */
            Update withSstpOnly();

            /**
             * Specifies that only IKEv2 VPN tunnel type will be used.
             *
             * @return the next stage of the update
             */
            Update withIkeV2Only();
        }
    }

    /** The entirety of a subnet update as part of a network update. */
    interface Update
        extends UpdateStages.WithAddressPool,
            UpdateStages.WithAuthenticationType,
            UpdateStages.WithRevokedCertificate,
            UpdateStages.WithTunnelType,
            Settable<VirtualNetworkGateway.Update> {
    }
}
