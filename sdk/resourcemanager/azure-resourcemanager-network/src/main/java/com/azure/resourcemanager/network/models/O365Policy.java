// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.network.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.arm.models.ChildResource;
import com.azure.resourcemanager.resources.fluentcore.model.Attachable;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;
import com.azure.resourcemanager.resources.fluentcore.model.Settable;

/**
 * Entry point for Virtual Network management API in Azure.
 */
@Fluent
public interface O365Policy extends HasInnerModel<O365PolicyProperties>, ChildResource<VpnSite> {

    /** @return Flag to control allow category. */
    boolean allow();

    /** @return Flag to control optimize category. */
    boolean optimize();

    /** @return Flag to control default category. */
    boolean defaultProperty();


    /**
     * The entirety of an O365Policy definition.
     *
     * @param <ParentT> the return type of the final {@link Attachable#attach()}
     */
    interface Definition<ParentT>
        extends DefinitionStages.Blank<ParentT>,
        DefinitionStages.WithAttach<ParentT> {
    }

    /** Grouping of an O365Policy definition stages applicable as part of a vpn site creation. */
    interface DefinitionStages {
        /**
         * The first stage of a O365Policy definition.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface Blank<ParentT> extends WithAttach<ParentT> {
        }

        /**
         * The stage of the O365Policy definition allowing to be specified.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithAttach<ParentT>
            extends Attachable.InDefinition<ParentT> {

            /**
             * Specifies the value to which this allow category applies.
             *
             * @param allow allow category
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withAllow(Boolean allow);

            /**
             * Specifies the ip address to which this optimize category applies.
             *
             * @param optimize optimize category
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withOptimize(Boolean optimize);

            /**
             * Specifies the ip address to which this defaultProperty category applies.
             *
             * @param defaultProperty default property category
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withDefaultProperty(Boolean defaultProperty);
        }
    }

    /**
     * The entirety of an O365Policy definition as part of a vpn site update.
     *
     * @param <ParentT> the return type of the final {@link VpnSiteLink.UpdateDefinitionStages.WithAttach#attach()}
     */
    interface UpdateDefinition<ParentT>
        extends UpdateDefinitionStages.Blank<ParentT>,
        UpdateDefinitionStages.WithAttach<ParentT> {
    }

    /** Grouping of O365Policy definition stages applicable as part of a vpn site update. */
    interface UpdateDefinitionStages {
        /** Grouping of an O365Policy definition stages applicable as part of a vpn site update. */
        interface Blank<ParentT> extends WithAttach<ParentT> {
        }
        /**
         * The stage of the O365Policy definition allowing to be specified.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this update
         */
        interface WithAttach<ParentT> extends Attachable.InUpdate<ParentT> {

            /**
             * Specifies the value to which this allow category applies.
             *
             * @param allow allow category
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withAllow(Boolean allow);

            /**
             * Specifies the value to which this optimize category applies.
             *
             * @param optimize optimize category
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withOptimize(Boolean optimize);

            /**
             * Specifies the value to which this defaultProperty category applies.
             *
             * @param defaultProperty default property category
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withDefaultProperty(Boolean defaultProperty);
        }
    }

    /** The entirety of an O365Policy update as part of a vpn site update. */
    interface Update
        extends Settable<VpnSite.Update> {

        /**
         * Specifies the value to which this allow category applies.
         *
         * @param allow allow category
         * @return the next stage of the update
         */
        Update withAllow(Boolean allow);

        /**
         * Specifies the value to which this optimize category applies.
         *
         * @param optimize optimize category
         * @return the next stage of the update
         */
        Update withOptimize(Boolean optimize);

        /**
         * Specifies the value to which this default property category applies.
         *
         * @param defaultProperty default property category
         * @return the next stage of the update
         */
        Update withDefaultProperty(Boolean defaultProperty);
    }
}
