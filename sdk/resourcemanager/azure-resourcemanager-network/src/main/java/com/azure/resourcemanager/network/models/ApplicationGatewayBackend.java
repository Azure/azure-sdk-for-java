// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.arm.models.ChildResource;
import com.azure.resourcemanager.resources.fluentcore.model.Attachable;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;
import com.azure.resourcemanager.resources.fluentcore.model.Settable;
import java.util.Collection;

/** A client-side representation of an application gateway backend. */
@Fluent()
public interface ApplicationGatewayBackend
    extends HasInner<ApplicationGatewayBackendAddressPool>, ChildResource<ApplicationGateway>, HasBackendNics {

    /** @return addresses on the backend of the application gateway */
    Collection<ApplicationGatewayBackendAddress> addresses();

    /**
     * Checks whether the specified IP address is referenced by this backend address pool.
     *
     * @param ipAddress an IP address
     * @return true if the specified IP address is referenced by this backend, else false
     */
    boolean containsIPAddress(String ipAddress);

    /**
     * Checks whether the specified FQDN is referenced by this backend address pool.
     *
     * @param fqdn a fully qualified domain name (FQDN)
     * @return true if the specified FQDN is referenced by this backend, else false
     */
    boolean containsFqdn(String fqdn);

    /** Grouping of application gateway backend definition stages. */
    interface DefinitionStages {
        /**
         * The first stage of an application gateway backend definition.
         *
         * @param <ParentT> the stage of the parent application gateway definition to return to after attaching this
         *     definition
         */
        interface Blank<ParentT> extends WithAttach<ParentT> {
        }

        /**
         * The stage of an application gateway backend definition allowing to add an address to the backend.
         *
         * @param <ParentT> the stage of the parent application gateway definition to return to after attaching this
         *     definition
         */
        interface WithAddress<ParentT> {
            /**
             * Adds the specified existing IP address to the backend.
             *
             * <p>This call can be made in a sequence to add multiple IP addresses.
             *
             * @param ipAddress an IP address
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withIPAddress(String ipAddress);

            /**
             * Adds the specified existing fully qualified domain name (FQDN) to the backend.
             *
             * <p>This call can be made in a sequence to add multiple FQDNs.
             *
             * @param fqdn a fully qualified domain name (FQDN)
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withFqdn(String fqdn);
        }

        /**
         * The final stage of an application gateway backend definition.
         *
         * <p>At this stage, any remaining optional settings can be specified, or the definition can be attached to the
         * parent application gateway definition.
         *
         * @param <ParentT> the stage of the parent application gateway definition to return to after attaching this
         *     definition
         */
        interface WithAttach<ParentT> extends Attachable.InDefinition<ParentT>, WithAddress<ParentT> {
        }
    }

    /**
     * The entirety of an application gateway backend definition.
     *
     * @param <ParentT> the stage of the parent application gateway definition to return to after attaching this
     *     definition
     */
    interface Definition<ParentT> extends DefinitionStages.Blank<ParentT>, DefinitionStages.WithAttach<ParentT> {
    }

    /** Grouping of application gateway backend update stages. */
    interface UpdateStages {
        /** The stage of an application gateway backend update allowing to add an address to the backend. */
        interface WithAddress {
            /**
             * Adds the specified existing IP address to the backend.
             *
             * @param ipAddress an IP address
             * @return the next stage of the update
             */
            Update withIPAddress(String ipAddress);

            /**
             * Adds the specified existing fully qualified domain name (FQDN) to the backend.
             *
             * @param fqdn a fully qualified domain name (FQDN)
             * @return the next stage of the update
             */
            Update withFqdn(String fqdn);

            /**
             * Ensures the specified IP address is not associated with this backend.
             *
             * @param ipAddress an IP address
             * @return the next stage of the update
             */
            Update withoutIPAddress(String ipAddress);

            /**
             * Ensure the specified address is not associated with this backend.
             *
             * @param address an existing address currently associated with the backend
             * @return the next stage of the update
             */
            Update withoutAddress(ApplicationGatewayBackendAddress address);

            /**
             * Ensures the specified fully qualified domain name (FQDN) is not associated with this backend.
             *
             * @param fqdn a fully qualified domain name
             * @return the next stage of the update
             */
            Update withoutFqdn(String fqdn);
        }
    }

    /** The entirety of an application gateway backend update as part of an application gateway update. */
    interface Update extends Settable<ApplicationGateway.Update>, UpdateStages.WithAddress {
    }

    /**
     * Grouping of application gateway backend definition stages applicable as part of an application gateway update.
     */
    interface UpdateDefinitionStages {
        /**
         * The first stage of an application gateway backend definition.
         *
         * @param <ParentT> the stage of the parent application gateway definition to return to after attaching this
         *     definition
         */
        interface Blank<ParentT> extends WithAttach<ParentT> {
        }

        /**
         * The stage of an application gateway backed definition allowing to add an address to the backend.
         *
         * @param <ParentT> the stage of the parent application gateway definition to return to after attaching this
         *     definition
         */
        interface WithAddress<ParentT> {
            /**
             * Adds the specified existing IP address to the backend.
             *
             * @param ipAddress an IP address
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withIPAddress(String ipAddress);

            /**
             * Adds the specified existing fully qualified domain name (FQDN) to the backend.
             *
             * @param fqdn a fully qualified domain name (FQDN)
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withFqdn(String fqdn);
        }

        /**
         * The final stage of an application gateway backend definition.
         *
         * <p>At this stage, any remaining optional settings can be specified, or the definition can be attached to the
         * parent application gateway definition.
         *
         * @param <ParentT> the stage of the parent application gateway definition to return to after attaching this
         *     definition
         */
        interface WithAttach<ParentT> extends Attachable.InUpdate<ParentT>, WithAddress<ParentT> {
        }
    }

    /**
     * The entirety of an application gateway backend definition as part of an application gateway update.
     *
     * @param <ParentT> the stage of the parent application gateway definition to return to after attaching this
     *     definition
     */
    interface UpdateDefinition<ParentT>
        extends UpdateDefinitionStages.Blank<ParentT>, UpdateDefinitionStages.WithAttach<ParentT> {
    }
}
