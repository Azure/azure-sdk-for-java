/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.network.models;

import com.azure.core.annotation.Fluent;
import com.azure.management.network.PublicIPAddress;
import com.azure.management.resources.fluentcore.model.Creatable;

/**
 * An interface representing a model's ability to reference a public IP address.
 */
@Fluent
public interface HasPublicIPAddress {
    /**
     * @return the resource ID of the associated public IP address
     */
    String publicIPAddressId();

    /**
     * @return the associated public IP address
     */
    PublicIPAddress getPublicIPAddress();

    /**
     * Grouping of definition stages involving specifying the public IP address.
     */
    interface DefinitionStages {
        /**
         * The stage of the definition allowing to associate the resource with an existing public IP address.
         *
         * @param <ReturnT> the next stage of the definition
         */
        interface WithExistingPublicIPAddress<ReturnT> {
            /**
             * Associates an existing public IP address with the resource.
             *
             * @param publicIPAddress an existing public IP address
             * @return the next stage of the definition
             */
            ReturnT withExistingPublicIPAddress(PublicIPAddress publicIPAddress);

            /**
             * Associates an existing public IP address with the resource.
             *
             * @param resourceId the resource ID of an existing public IP address
             * @return the next stage of the definition
             */
            ReturnT withExistingPublicIPAddress(String resourceId);
        }

        /**
         * The stage of the definition allowing to associate the resource with a new public IP address.
         *
         * @param <ReturnT> the next stage of the definition
         */
        interface WithNewPublicIPAddressNoDnsLabel<ReturnT> {
            /**
             * Creates a new public IP address to associate with the resource.
             *
             * @param creatable a creatable definition for a new public IP
             * @return the next stage of the definition
             */
            ReturnT withNewPublicIPAddress(Creatable<PublicIPAddress> creatable);

            /**
             * Creates a new public IP address in the same region and group as the resource and associates it with the resource.
             * <p>
             * The internal name and DNS label for the public IP address will be derived from the resource's name.
             *
             * @return the next stage of the definition
             */
            ReturnT withNewPublicIPAddress();
        }

        /**
         * The stage of the definition allowing to associate the resource with a new public IP address.
         *
         * @param <ReturnT> the next stage of the definition
         */
        interface WithNewPublicIPAddress<ReturnT> extends WithNewPublicIPAddressNoDnsLabel<ReturnT> {
            /**
             * Creates a new public IP address in the same region and group as the resource, with the specified DNS label
             * and associates it with the resource.
             * <p>
             * The internal name for the public IP address will be derived from the DNS label.
             *
             * @param leafDnsLabel the leaf domain label
             * @return the next stage of the definition
             */
            ReturnT withNewPublicIPAddress(String leafDnsLabel);
        }

        /**
         * The stage of the definition allowing to associate the resource with a public IP address,
         * but not allowing to create one with a DNS leaf label.
         *
         * @param <ReturnT> the next stage of the definition
         */
        interface WithPublicIPAddressNoDnsLabel<ReturnT> extends
                WithExistingPublicIPAddress<ReturnT>,
                WithNewPublicIPAddressNoDnsLabel<ReturnT> {
        }

        /**
         * The stage of the definition allowing to associate the resource with a public IP address.
         *
         * @param <ReturnT> the next stage of the definition
         */
        interface WithPublicIPAddress<ReturnT> extends
                WithExistingPublicIPAddress<ReturnT>,
                WithNewPublicIPAddress<ReturnT> {
        }
    }

    /**
     * Grouping of update stages involving modifying an existing reference to a public IP address.
     */
    interface UpdateStages {
        /**
         * The stage of the update allowing to associate the resource with an existing public IP address.
         *
         * @param <ReturnT> the next stage of the update
         */
        interface WithExistingPublicIPAddress<ReturnT> {
            /**
             * Associates an existing public IP address with the resource.
             *
             * @param publicIPAddress an existing public IP address
             * @return the next stage of the update
             */
            ReturnT withExistingPublicIPAddress(PublicIPAddress publicIPAddress);

            /**
             * Associates an existing public IP address with the resource.
             *
             * @param resourceId the resource ID of an existing public IP address
             * @return the next stage of the definition
             */
            ReturnT withExistingPublicIPAddress(String resourceId);

            /**
             * Removes the existing reference to a public IP address.
             *
             * @return the next stage of the update.
             */
            ReturnT withoutPublicIPAddress();
        }

        /**
         * The stage of the update allowing to associate the resource with a new public IP address.
         *
         * @param <ReturnT> the next stage of the definition
         */
        interface WithNewPublicIPAddressNoDnsLabel<ReturnT> {
            /**
             * Creates a new public IP address to associate with the resource.
             *
             * @param creatable a creatable definition for a new public IP
             * @return the next stage of the definition
             */
            ReturnT withNewPublicIPAddress(Creatable<PublicIPAddress> creatable);

            /**
             * Creates a new public IP address in the same region and group as the resource and associates it with the resource.
             * <p>
             * The internal name and DNS label for the public IP address will be derived from the resource's name.
             *
             * @return the next stage of the definition
             */
            ReturnT withNewPublicIPAddress();
        }

        /**
         * The stage of the update allowing to associate the resource with a new public IP address.
         *
         * @param <ReturnT> the next stage of the definition
         */
        interface WithNewPublicIPAddress<ReturnT> extends WithNewPublicIPAddressNoDnsLabel<ReturnT> {
            /**
             * Creates a new public IP address in the same region and group as the resource, with the specified DNS label
             * and associates it with the resource.
             * <p>
             * The internal name for the public IP address will be derived from the DNS label.
             *
             * @param leafDnsLabel the leaf domain label
             * @return the next stage of the definition
             */
            ReturnT withNewPublicIPAddress(String leafDnsLabel);
        }

        /**
         * The stage of the update allowing to associate the resource with a public IP address,
         * but not allowing to create one with a DNS leaf label.
         *
         * @param <ReturnT> the next stage of the definition
         */
        interface WithPublicIPAddressNoDnsLabel<ReturnT> extends
                WithExistingPublicIPAddress<ReturnT>,
                WithNewPublicIPAddressNoDnsLabel<ReturnT> {
        }

        /**
         * The stage definition allowing to associate the resource with a public IP address.
         *
         * @param <ReturnT> the next stage of the update
         */
        interface WithPublicIPAddress<ReturnT> extends
                WithExistingPublicIPAddress<ReturnT>,
                WithNewPublicIPAddress<ReturnT> {
        }
    }

    /**
     * Grouping of definition stages applicable as part of a parent resource update, involving specifying a public IP address.
     */
    interface UpdateDefinitionStages {
        /**
         * The stage of the definition allowing to associate the resource with an existing public IP address.
         *
         * @param <ReturnT> the next stage of the definition
         */
        interface WithExistingPublicIPAddress<ReturnT> {
            /**
             * Associates an existing public IP address with the resource.
             *
             * @param publicIPAddress an existing public IP address
             * @return the next stage of the definition
             */
            ReturnT withExistingPublicIPAddress(PublicIPAddress publicIPAddress);

            /**
             * Associates an existing public IP address with the resource.
             *
             * @param resourceId the resource ID of an existing public IP address
             * @return the next stage of the definition
             */
            ReturnT withExistingPublicIPAddress(String resourceId);
        }

        /**
         * The stage of the definition allowing to associate the resource with a new public IP address.
         *
         * @param <ReturnT> the next stage of the definition
         */
        interface WithNewPublicIPAddressNoDnsLabel<ReturnT> {
            /**
             * Creates a new public IP address to associate with the resource.
             *
             * @param creatable a creatable definition for a new public IP
             * @return the next stage of the definition
             */
            ReturnT withNewPublicIPAddress(Creatable<PublicIPAddress> creatable);

            /**
             * Creates a new public IP address in the same region and group as the resource and associates it with the resource.
             * <p>
             * The internal name and DNS label for the public IP address will be derived from the resource's name.
             *
             * @return the next stage of the definition
             */
            ReturnT withNewPublicIPAddress();
        }

        /**
         * The stage of the definition allowing to associate the resource with a public IP address,
         * but not allowing to create one with a DNS leaf label.
         *
         * @param <ReturnT> the next stage of the definition
         */
        interface WithPublicIPAddressNoDnsLabel<ReturnT> extends
                WithExistingPublicIPAddress<ReturnT>,
                WithNewPublicIPAddressNoDnsLabel<ReturnT> {
        }

        /**
         * The stage of the definition allowing to associate the resource with a new public IP address.
         *
         * @param <ReturnT> the next stage of the definition
         */
        interface WithNewPublicIPAddress<ReturnT> extends WithNewPublicIPAddressNoDnsLabel<ReturnT> {
            /**
             * Creates a new public IP address in the same region and group as the resource, with the specified DNS label
             * and associates it with the resource.
             * <p>
             * The internal name for the public IP address will be derived from the DNS label.
             *
             * @param leafDnsLabel the leaf domain label
             * @return the next stage of the definition
             */
            ReturnT withNewPublicIPAddress(String leafDnsLabel);
        }

        /**
         * The stage of the definition allowing to associate the resource with a public IP address.
         *
         * @param <ReturnT> the next stage of the definition
         */
        interface WithPublicIPAddress<ReturnT> extends
                WithExistingPublicIPAddress<ReturnT>,
                WithNewPublicIPAddress<ReturnT> {
        }
    }
}
