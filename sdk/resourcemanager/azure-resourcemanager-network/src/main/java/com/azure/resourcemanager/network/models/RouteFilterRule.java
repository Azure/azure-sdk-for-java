// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.network.fluent.models.RouteFilterRuleInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.ChildResource;
import com.azure.resourcemanager.resources.fluentcore.model.Attachable;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;
import com.azure.resourcemanager.resources.fluentcore.model.Settable;
import java.util.List;

/** A route filter rule in a route filter group. */
@Fluent
public interface RouteFilterRule extends HasInnerModel<RouteFilterRuleInner>, ChildResource<RouteFilter> {

    /** @return the access type of the rule */
    Access access();

    /** @return the rule type of the rule */
    RouteFilterRuleType routeFilterRuleType();

    /**
     * The collection for bgp community values to filter on. e.g. ['12076:5010','12076:5020'].
     *
     * @return collection of community values
     */
    List<String> communities();

    /** @return the provisioning state of the resource */
    String provisioningState();

    /** @return resource location */
    String location();

    /**
     * The entirety of a route filter rule definition.
     *
     * @param <ParentT> the return type of the final {@link Attachable#attach()}
     */
    interface Definition<ParentT>
        extends DefinitionStages.Blank<ParentT>,
            DefinitionStages.WithBgpCommunities<ParentT>,
            DefinitionStages.WithAttach<ParentT> {
    }

    /** Grouping of route filter rule definition stages applicable as part of a route filter group creation. */
    interface DefinitionStages {
        /**
         * The first stage of a route filter rule definition.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface Blank<ParentT> extends WithBgpCommunities<ParentT> {
        }

        /** The stage of the route filter rule definition allowing bgp service communities to be specified. */
        interface WithBgpCommunities<ParentT> {
            /**
             * Set the collection for bgp community values to filter on. e.g. ['12076:5010','12076:5020'].
             *
             * @param communities service communities
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withBgpCommunities(String... communities);

            /**
             * Set bgp community value to filter on. e.g. '12076:5020'.
             *
             * @param community service community
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withBgpCommunity(String community);

            /**
             * Remove the bgp community value to filter on. e.g. '12076:5010'
             *
             * @param community the bgp community value
             * @return the next stage of the update
             */
            Update withoutBgpCommunity(String community);
        }

        /** The stage of the route filter rule definition allowing access type of the rule. */
        interface WithAccessType<ParentT> {
            /**
             * Set 'Allow' acces type of the rule.
             *
             * @return the next stage of the definition
             */
            WithAttach<ParentT> allowAccess();

            /**
             * Set 'Deny' access type of the rule.
             *
             * @return the next stage of the definition
             */
            WithAttach<ParentT> denyAccess();
        }

        /**
         * The final stage of the route filter rule definition.
         *
         * <p>At this stage, any remaining optional settings can be specified, or the route filter rule definition can
         * be attached to the parent route filter group definition using {@link WithAttach#attach()}.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithAttach<ParentT> extends Attachable.InDefinition<ParentT>, WithAccessType<ParentT> {
        }
    }

    /**
     * The entirety of a route filter rule definition as part of a route filter group update.
     *
     * @param <ParentT> the return type of the final {@link UpdateDefinitionStages.WithAttach#attach()}
     */
    interface UpdateDefinition<ParentT>
        extends UpdateDefinitionStages.Blank<ParentT>, UpdateDefinitionStages.WithAttach<ParentT> {
    }

    /** Grouping of route filter rule definition stages applicable as part of a route filter group update. */
    interface UpdateDefinitionStages {
        /**
         * The first stage of a route filter rule description as part of an update of a networking route filter group.
         *
         * @param <ParentT> the return type of the final {@link Attachable#attach()}
         */
        interface Blank<ParentT> extends UpdateDefinitionStages.WithBgpCommunities<ParentT> {
        }

        /** The stage of the route filter rule definition allowing bgp service communities to be specified. */
        interface WithBgpCommunities<ParentT> {
            /**
             * Set the collection for bgp community values to filter on. e.g. ['12076:5010','12076:5020'].
             *
             * @param communities service communities
             * @return the next stage of the definition
             */
            UpdateDefinitionStages.WithAttach<ParentT> withBgpCommunities(String... communities);

            /**
             * Set bgp community value to filter on. e.g. '12076:5020'.
             *
             * @param community service community
             * @return the next stage of the definition
             */
            UpdateDefinitionStages.WithAttach<ParentT> withBgpCommunity(String community);

            /**
             * Remove the bgp community value to filter on. e.g. '12076:5010'
             *
             * @param community the bgp community value
             * @return the next stage of the update
             */
            Update withoutBgpCommunity(String community);
        }

        /** The stage of the route filter rule definition allowing access type of the rule. */
        interface WithAccessType<ParentT> {
            /**
             * Set 'Allow' acces type of the rule.
             *
             * @return the next stage of the definition
             */
            WithAttach<ParentT> allowAccess();

            /**
             * Set 'Deny' access type of the rule.
             *
             * @return the next stage of the definition
             */
            WithAttach<ParentT> denyAccess();
        }

        /**
         * The final stage of the route filter rule definition.
         *
         * <p>At this stage, any remaining optional settings can be specified, or the route filter rule definition can
         * be attached to the parent route filter group definition using {@link WithAttach#attach()}.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithAttach<ParentT> extends Attachable.InUpdate<ParentT> {
        }
    }

    /** The entirety of a route filter rule update as part of a route filter group update. */
    interface Update
        extends UpdateStages.WithBgpCommunities, UpdateStages.WithAccessType, Settable<RouteFilter.Update> {
    }

    /** Grouping of route filter rule update stages. */
    interface UpdateStages {
        /** The stage of the route filter rule description allowing bgp service communities to be specified. */
        interface WithBgpCommunities {
            /**
             * The collection for bgp community values to filter on. e.g. ['12076:5010','12076:5020']. Note: this method
             * will overwrite existing communities.
             *
             * @param communities service communities
             * @return the next stage of the update
             */
            Update withBgpCommunities(String... communities);

            /**
             * The bgp community values to filter on. e.g. '12076:5010'. This method has additive effect.
             *
             * @param community the bgp community value
             * @return the next stage of the update
             */
            Update withBgpCommunity(String community);

            /**
             * Remove the bgp community value to filter on. e.g. '12076:5010'
             *
             * @param community the bgp community value
             * @return the next stage of the update
             */
            Update withoutBgpCommunity(String community);
        }

        /** The stage of the route filter rule definition allowing access type of the rule. */
        interface WithAccessType {
            /**
             * Set 'Allow' acces type of the rule.
             *
             * @return the next stage of the definition
             */
            Update allowAccess();

            /**
             * Set 'Deny' access type of the rule.
             *
             * @return the next stage of the definition
             */
            Update denyAccess();
        }
    }
}
