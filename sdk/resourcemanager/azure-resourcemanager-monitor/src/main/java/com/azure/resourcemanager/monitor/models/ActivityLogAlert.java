// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.monitor.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.monitor.MonitorManager;
import com.azure.resourcemanager.monitor.fluent.models.ActivityLogAlertResourceInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.GroupableResource;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasId;
import com.azure.resourcemanager.resources.fluentcore.arm.models.Resource;
import com.azure.resourcemanager.resources.fluentcore.model.Appliable;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.Refreshable;
import com.azure.resourcemanager.resources.fluentcore.model.Updatable;
import java.util.Collection;
import java.util.Map;

/** An immutable client-side representation of an Azure Activity Log Alert. */
@Fluent
public interface ActivityLogAlert
    extends GroupableResource<MonitorManager, ActivityLogAlertResourceInner>,
        Refreshable<ActivityLogAlert>,
        Updatable<ActivityLogAlert.Update> {

    /**
     * Get a list of resourceIds that will be used as prefixes. The alert will only apply to activityLogs with
     * resourceIds that fall under one of these prefixes. This list must include at least one item.
     *
     * @return the scopes value
     */
    Collection<String> scopes();

    /**
     * Get indicates whether this activity log alert is enabled. If an activity log alert is not enabled, then none of
     * its actions will be activated.
     *
     * @return the enabled value
     */
    Boolean enabled();

    /**
     * Get the condition that will cause this alert to activate.
     *
     * @return the condition value
     */
    Map<String, String> equalsConditions();

    /**
     * Get the actions that will activate when the condition is met.
     *
     * @return the actions value
     */
    Collection<String> actionGroupIds();

    /**
     * Get a description of this activity log alert.
     *
     * @return the description value
     */
    String description();

    /** The entirety of a activity log alerts definition. */
    interface Definition
        extends DefinitionStages.Blank,
            DefinitionStages.WithCreate,
            DefinitionStages.WithScopes,
            DefinitionStages.WithDescription,
            DefinitionStages.WithAlertEnabled,
            DefinitionStages.WithActionGroup,
            DefinitionStages.WithCriteriaDefinition {
    }

    /** Grouping of activity log alerts definition stages. */
    interface DefinitionStages {
        /** The first stage of a activity log alert definition. */
        interface Blank extends GroupableResource.DefinitionStages.WithGroupAndRegion<WithScopes> {
        }

        /** The stage of the definition which specifies target resource or subscription for activity log alert. */
        interface WithScopes {
            /**
             * Sets specified resource as a target to alert on activity log.
             *
             * @param resourceId resource Id string.
             * @return the next stage of activity log alert definition.
             */
            WithDescription withTargetResource(String resourceId);

            /**
             * Sets specified resource as a target to alert on activity log.
             *
             * @param resource resource type that is inherited from {@link HasId} interface
             * @return the next stage of activity log alert definition.
             */
            WithDescription withTargetResource(HasId resource);

            /**
             * Sets specified subscription as a target to alert on activity log.
             *
             * @param targetSubscriptionId subscription Id.
             * @return the next stage of activity log alert definition.
             */
            WithDescription withTargetSubscription(String targetSubscriptionId);
        }

        /** The stage of the definition which specifies description text for activity log alert. */
        interface WithDescription {
            /**
             * Sets description for activity log alert.
             *
             * @param description Human readable text description of the activity log alert.
             * @return the next stage of activity log alert definition.
             */
            WithAlertEnabled withDescription(String description);
        }

        /** The stage of the definition which specifies if the activity log alert should be enabled upon creation. */
        interface WithAlertEnabled {
            /**
             * Sets activity log alert as enabled during the creation.
             *
             * @return the next stage of activity log alert definition.
             */
            WithActionGroup withRuleEnabled();

            /**
             * Sets activity log alert as disabled during the creation.
             *
             * @return the next stage of activity log alert definition.
             */
            WithActionGroup withRuleDisabled();
        }

        /**
         * The stage of the definition which specifies actions that will be activated when the conditions are met in the
         * activity log alert rules.
         */
        interface WithActionGroup {
            /**
             * Sets the actions that will activate when the condition is met.
             *
             * @param actionGroupId resource Ids of the {@link ActionGroup}.
             * @return the next stage of activity log alert definition.
             */
            WithCriteriaDefinition withActionGroups(String... actionGroupId);
        }

        /** The stage of the definition which specifies condition that will cause this alert to activate. */
        interface WithCriteriaDefinition {
            /**
             * Adds a condition that will cause this alert to activate.
             *
             * @param field Set the name of the field that this condition will examine. The possible values for this
             *     field are (case-insensitive): 'resourceId', 'category', 'caller', 'level', 'operationName',
             *     'resourceGroup', 'resourceProvider', 'status', 'subStatus', 'resourceType', or anything beginning
             *     with 'properties.'.
             * @param equals Set the field value will be compared to this value (case-insensitive) to determine if the
             *     condition is met.
             * @return the next stage of activity log alert definition.
             */
            WithCreate withEqualsCondition(String field, String equals);

            /**
             * Sets all the conditions that will cause this alert to activate.
             *
             * @param fieldEqualsMap Set the names of the field that this condition will examine and their values to be
             *     compared to.
             * @return the next stage of activity log alert definition.
             */
            WithCreate withEqualsConditions(Map<String, String> fieldEqualsMap);
        }

        /**
         * The stage of the definition which contains all the minimum required inputs for the resource to be created but
         * also allows for any other optional settings to be specified.
         */
        interface WithCreate
            extends Creatable<ActivityLogAlert>, DefinitionWithTags<WithCreate>, WithCriteriaDefinition {
        }
    }

    /** Grouping of activity log alerts update stages. */
    interface UpdateStages {
        /** The stage of a activity log alerts update allowing to modify settings. */
        interface WithActivityLogUpdate {
            /**
             * Sets description for activity log alert.
             *
             * @param description Human readable text description of the activity log alert.
             * @return the next stage of the activity log alert update.
             */
            Update withDescription(String description);

            /**
             * Sets activity log alert as enabled.
             *
             * @return the next stage of the activity log alert update.
             */
            Update withRuleEnabled();

            /**
             * Sets activity log alert as disabled.
             *
             * @return the next stage of the activity log alert update.
             */
            Update withRuleDisabled();

            /**
             * Sets the actions that will activate when the condition is met.
             *
             * @param actionGroupId resource Ids of the {@link ActionGroup}.
             * @return the next stage of the activity log alert update.
             */
            Update withActionGroups(String... actionGroupId);

            /**
             * Removes the specified action group from the actions list.
             *
             * @param actionGroupId resource Id of the {@link ActionGroup} to remove.
             * @return the next stage of the activity log alert update.
             */
            Update withoutActionGroup(String actionGroupId);

            /**
             * Adds a condition that will cause this alert to activate.
             *
             * @param field Set the name of the field that this condition will examine. The possible values for this
             *     field are (case-insensitive): 'resourceId', 'category', 'caller', 'level', 'operationName',
             *     'resourceGroup', 'resourceProvider', 'status', 'subStatus', 'resourceType', or anything beginning
             *     with 'properties.'.
             * @param equals Set the field value will be compared to this value (case-insensitive) to determine if the
             *     condition is met.
             * @return the next stage of the activity log alert update.
             */
            Update withEqualsCondition(String field, String equals);

            /**
             * Sets all the conditions that will cause this alert to activate.
             *
             * @param fieldEqualsMap Set the names of the field that this condition will examine and their values to be
             *     compared to.
             * @return the next stage of the activity log alert update.
             */
            Update withEqualsConditions(Map<String, String> fieldEqualsMap);

            /**
             * Removes a condition from the list of conditions.
             *
             * @param field the name of the field that was used for condition examination.
             * @return the next stage of the activity log alert update.
             */
            Update withoutEqualsCondition(String field);
        }
    }

    /** The template for an update operation, containing all the settings that can be modified. */
    interface Update
        extends Appliable<ActivityLogAlert>, UpdateStages.WithActivityLogUpdate, Resource.UpdateWithTags<Update> {
    }
}
