// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.models;

import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import reactor.core.publisher.Mono;

/** An interface representing a model's ability to reference a public IP address. */
public interface HasPublicIpAddress {
    /** @return the resource ID of the associated public IP address */
    String publicIpAddressId();

    /** @return the associated public IP address */
    PublicIpAddress getPublicIpAddress();

    /** @return the associated public IP address */
    Mono<PublicIpAddress> getPublicIpAddressAsync();

    /** Grouping of definition stages involving specifying the public IP address. */
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
            ReturnT withExistingPublicIpAddress(PublicIpAddress publicIPAddress);

            /**
             * Associates an existing public IP address with the resource.
             *
             * @param resourceId the resource ID of an existing public IP address
             * @return the next stage of the definition
             */
            ReturnT withExistingPublicIpAddress(String resourceId);
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
            ReturnT withNewPublicIpAddress(Creatable<PublicIpAddress> creatable);

            /**
             * Creates a new public IP address in the same region and group as the resource and associates it with the
             * resource.
             *
             * <p>The internal name and DNS label for the public IP address will be derived from the resource's name.
             *
             * @return the next stage of the definition
             */
            ReturnT withNewPublicIpAddress();
        }

        /**
         * The stage of the definition allowing to associate the resource with a new public IP address.
         *
         * @param <ReturnT> the next stage of the definition
         */
        interface WithNewPublicIPAddress<ReturnT> extends WithNewPublicIPAddressNoDnsLabel<ReturnT> {
            /**
             * Creates a new public IP address in the same region and group as the resource, with the specified DNS
             * label and associates it with the resource.
             *
             * <p>The internal name for the public IP address will be derived from the DNS label.
             *
             * @param leafDnsLabel the leaf domain label
             * @return the next stage of the definition
             */
            ReturnT withNewPublicIpAddress(String leafDnsLabel);
        }

        /**
         * The stage of the definition allowing to associate the resource with a public IP address, but not allowing to
         * create one with a DNS leaf label.
         *
         * @param <ReturnT> the next stage of the definition
         */
        interface WithPublicIPAddressNoDnsLabel<ReturnT>
            extends WithExistingPublicIPAddress<ReturnT>, WithNewPublicIPAddressNoDnsLabel<ReturnT> {
        }

        /**
         * The stage of the definition allowing to associate the resource with a public IP address.
         *
         * @param <ReturnT> the next stage of the definition
         */
        interface WithPublicIPAddress<ReturnT>
            extends WithExistingPublicIPAddress<ReturnT>, WithNewPublicIPAddress<ReturnT> {
        }
    }

    /** Grouping of update stages involving modifying an existing reference to a public IP address. */
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
            ReturnT withExistingPublicIpAddress(PublicIpAddress publicIPAddress);

            /**
             * Associates an existing public IP address with the resource.
             *
             * @param resourceId the resource ID of an existing public IP address
             * @return the next stage of the definition
             */
            ReturnT withExistingPublicIpAddress(String resourceId);

            /**
             * Removes the existing reference to a public IP address.
             *
             * @return the next stage of the update.
             */
            ReturnT withoutPublicIpAddress();
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
            ReturnT withNewPublicIpAddress(Creatable<PublicIpAddress> creatable);

            /**
             * Creates a new public IP address in the same region and group as the resource and associates it with the
             * resource.
             *
             * <p>The internal name and DNS label for the public IP address will be derived from the resource's name.
             *
             * @return the next stage of the definition
             */
            ReturnT withNewPublicIpAddress();
        }

        /**
         * The stage of the update allowing to associate the resource with a new public IP address.
         *
         * @param <ReturnT> the next stage of the definition
         */
        interface WithNewPublicIPAddress<ReturnT> extends WithNewPublicIPAddressNoDnsLabel<ReturnT> {
            /**
             * Creates a new public IP address in the same region and group as the resource, with the specified DNS
             * label and associates it with the resource.
             *
             * <p>The internal name for the public IP address will be derived from the DNS label.
             *
             * @param leafDnsLabel the leaf domain label
             * @return the next stage of the definition
             */
            ReturnT withNewPublicIpAddress(String leafDnsLabel);
        }

        /**
         * The stage of the update allowing to associate the resource with a public IP address, but not allowing to
         * create one with a DNS leaf label.
         *
         * @param <ReturnT> the next stage of the definition
         */
        interface WithPublicIPAddressNoDnsLabel<ReturnT>
            extends WithExistingPublicIPAddress<ReturnT>, WithNewPublicIPAddressNoDnsLabel<ReturnT> {
        }

        /**
         * The stage definition allowing to associate the resource with a public IP address.
         *
         * @param <ReturnT> the next stage of the update
         */
        interface WithPublicIPAddress<ReturnT>
            extends WithExistingPublicIPAddress<ReturnT>, WithNewPublicIPAddress<ReturnT> {
        }
    }

    /**
     * Grouping of definition stages applicable as part of a parent resource update, involving specifying a public IP
     * address.
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
            ReturnT withExistingPublicIpAddress(PublicIpAddress publicIPAddress);

            /**
             * Associates an existing public IP address with the resource.
             *
             * @param resourceId the resource ID of an existing public IP address
             * @return the next stage of the definition
             */
            ReturnT withExistingPublicIpAddress(String resourceId);
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
            ReturnT withNewPublicIpAddress(Creatable<PublicIpAddress> creatable);

            /**
             * Creates a new public IP address in the same region and group as the resource and associates it with the
             * resource.
             *
             * <p>The internal name and DNS label for the public IP address will be derived from the resource's name.
             *
             * @return the next stage of the definition
             */
            ReturnT withNewPublicIpAddress();
        }

        /**
         * The stage of the definition allowing to associate the resource with a public IP address, but not allowing to
         * create one with a DNS leaf label.
         *
         * @param <ReturnT> the next stage of the definition
         */
        interface WithPublicIPAddressNoDnsLabel<ReturnT>
            extends WithExistingPublicIPAddress<ReturnT>, WithNewPublicIPAddressNoDnsLabel<ReturnT> {
        }

        /**
         * The stage of the definition allowing to associate the resource with a new public IP address.
         *
         * @param <ReturnT> the next stage of the definition
         */
        interface WithNewPublicIPAddress<ReturnT> extends WithNewPublicIPAddressNoDnsLabel<ReturnT> {
            /**
             * Creates a new public IP address in the same region and group as the resource, with the specified DNS
             * label and associates it with the resource.
             *
             * <p>The internal name for the public IP address will be derived from the DNS label.
             *
             * @param leafDnsLabel the leaf domain label
             * @return the next stage of the definition
             */
            ReturnT withNewPublicIpAddress(String leafDnsLabel);
        }

        /**
         * The stage of the definition allowing to associate the resource with a public IP address.
         *
         * @param <ReturnT> the next stage of the definition
         */
        interface WithPublicIPAddress<ReturnT>
            extends WithExistingPublicIPAddress<ReturnT>, WithNewPublicIPAddress<ReturnT> {
        }
    }
}
