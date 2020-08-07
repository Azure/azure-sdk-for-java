// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.privatedns.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.privatedns.fluent.inner.VirtualNetworkLinkInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.ExternalChildResource;
import com.azure.resourcemanager.resources.fluentcore.arm.models.Resource;
import com.azure.resourcemanager.resources.fluentcore.model.Attachable;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;
import com.azure.resourcemanager.resources.fluentcore.model.Settable;

/** An immutable client-side representation of an Azure Virtual Network Link. */
@Fluent
public interface VirtualNetworkLink
    extends ExternalChildResource<VirtualNetworkLink, PrivateDnsZone>, HasInner<VirtualNetworkLinkInner> {
    /**
     * @return the ETag of the virtual network link.
     */
    String etag();

    /**
     * @return the id of referenced virtual network.
     */
    String referencedVirtualNetworkId();

    /**
     * @return whether auto-registration of virtual machine records in the virtual network
     * gets enabled in the private DNS zone.
     */
    boolean isAutoRegistrationEnabled();

    /**
     * @return the status of the virtual network link to the private DNS zone.
     */
    VirtualNetworkLinkState virtualNetworkLinkState();

    /**
     * @return the provisioning state of the virtual network link.
     */
    ProvisioningState provisioningState();

    /**
     * The entirety of a virtual network link definition as a part of parent definition.
     *
     * @param <ParentT> the stage of the parent definition to return to after attaching this definition
     */
    interface Definition<ParentT>
        extends DefinitionStages.Blank<ParentT>,
            DefinitionStages.WithAttach<ParentT> {
    }

    /** Grouping of virtual network link definition stages as a part of parent DNS zone definition. */
    interface DefinitionStages {
        /**
         * The first stage of a virtual network link definition.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface Blank<ParentT> extends WithAttach<ParentT> {
        }

        /**
         * The stage of the record set definition allowing to manage auto-registration of the virtual network records.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithAutoRegistration<ParentT> {
            /**
             * Enables auto-registration for virtual network records.
             *
             * @return the next stage of the definition
             */
            WithAttach<ParentT> enableAutoRegistration();

            /**
             * Disables auto-registration for virtual network records.
             *
             * @return the next stage of the definition
             */
            WithAttach<ParentT> disableAutoRegistration();
        }

        /**
         * The stage of the record set definition allowing to enable ETag validation.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithETagCheck<ParentT> {
            /**
             * Specifies the If-None-Match with * to prevent updating an existing record set.
             *
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withETagCheck();
        }

        /**
         * The stage of the record set definition allowing to reference the virtual network.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithReferencedVirtualNetwork<ParentT> {
            /**
             * Specifies the reference of the virtual network.
             *
             * @param virtualNetworkId the id of the virtual network
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withVirtualNetworkId(String virtualNetworkId);
        }

        /**
         * The final stage of the DNS zone record set definition.
         *
         * <p>At this stage, any remaining optional settings can be specified, or the DNS zone record set definition can
         * be attached to the parent traffic manager profile definition using {@link
         * VirtualNetworkLink.DefinitionStages.WithAttach#attach()}.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithAttach<ParentT>
            extends Attachable.InDefinition<ParentT>,
                DefinitionStages.WithAutoRegistration<ParentT>,
                DefinitionStages.WithReferencedVirtualNetwork<ParentT>,
                DefinitionStages.WithETagCheck<ParentT>,
                Resource.DefinitionWithRegion<WithAttach<ParentT>>,
                Resource.DefinitionWithTags<WithAttach<ParentT>> {
        }
    }

    /**
     * The entirety of a virtual network link definition as a part of parent update.
     *
     * @param <ParentT> the stage of the parent definition to return to after attaching this definition
     */
    interface UpdateDefinition<ParentT>
        extends UpdateDefinitionStages.Blank<ParentT>,
            UpdateDefinitionStages.WithAttach<ParentT> {
    }

    /** Grouping of DNS zone record set definition stages as a part of parent DNS zone update. */
    interface UpdateDefinitionStages {
        /**
         * The first stage of a virtual network link definition.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface Blank<ParentT> extends WithAttach<ParentT> {
        }

        /**
         * The stage of the record set definition allowing to manage auto-registration of the virtual network records.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithAutoRegistration<ParentT> {
            /**
             * Enables auto-registration for virtual network records.
             *
             * @return the next stage of the definition
             */
            WithAttach<ParentT> enableAutoRegistration();

            /**
             * Disables auto-registration for virtual network records.
             *
             * @return the next stage of the definition
             */
            WithAttach<ParentT> disableAutoRegistration();
        }

        /**
         * The stage of the record set definition allowing to enable ETag validation.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithETagCheck<ParentT> {
            /**
             * Specifies the If-None-Match header with * to prevent updating an existing record set.
             *
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withETagCheck();
        }

        /**
         * The stage of the record set definition allowing to reference the virtual network.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithReferencedVirtualNetwork<ParentT> {
            /**
             * Specifies the reference of the virtual network.
             *
             * @param virtualNetworkId the id of the virtual network
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withVirtualNetworkId(String virtualNetworkId);
        }

        /**
         * The final stage of the DNS zone record set definition.
         *
         * <p>At this stage, any remaining optional settings can be specified, or the DNS zone record set definition can
         * be attached to the parent traffic manager profile definition using {@link
         * VirtualNetworkLink.UpdateDefinitionStages.WithAttach#attach()}.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithAttach<ParentT>
            extends Attachable.InUpdate<ParentT>,
            UpdateDefinitionStages.WithAutoRegistration<ParentT>,
            UpdateDefinitionStages.WithReferencedVirtualNetwork<ParentT>,
            UpdateDefinitionStages.WithETagCheck<ParentT> {
        }
    }

    /**
     * the set of configurations that can be updated for virtual network link.
     */
    interface Update
        extends Settable<PrivateDnsZone.Update>,
            UpdateStages.WithAutoRegistration,
            UpdateStages.WithETagCheck,
            Resource.UpdateWithTags<Update> {
    }

    /** Grouping of virtual network link update stages. */
    interface UpdateStages {
        /**
         * The stage of the virtual network link update allowing to manage auto-registration
         * of the virtual network records.
         */
        interface WithAutoRegistration {
            /**
             * Enables auto-registration for virtual network records.
             *
             * @return the next stage of the virtual network link update
             */
            Update enableAutoRegistration();

            /**
             * Disables auto-registration for virtual network records.
             *
             * @return the next stage of the virtual network link update
             */
            Update disableAutoRegistration();
        }

        /** The stage of the virtual network link update allowing to enable ETag validation. */
        interface WithETagCheck {
            /**
             * Specifies the If-Match header with the current etag value associated with the virtual network link.
             *
             * @return the next stage of the update
             */
            Update withETagCheck();

            /**
             * Specifies the If-Match header with the given etag value.
             *
             * @param etagValue the etag value
             * @return the next stage of the update
             */
            Update withETagCheck(String etagValue);
        }
    }
}
