/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.monitor;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.monitor.implementation.AutoscaleSettingResourceInner;
import com.microsoft.azure.management.monitor.implementation.MonitorManager;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.management.resources.fluentcore.model.Appliable;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Updatable;

import java.util.List;
import java.util.Map;

/**
 */
@Fluent
public interface AutoscaleSetting extends
    GroupableResource<MonitorManager, AutoscaleSettingResourceInner>,
    Refreshable<AutoscaleSetting>,
    Updatable<AutoscaleSetting.Update> {

    /**
     * the collection of automatic scaling profiles that specify different scaling parameters for different time periods. A maximum of 20 profiles can be specified.
     */
    Map<String, AutoscaleProfile> profiles();
    /**
     * the collection of notifications.
     */
    List<AutoscaleNotification> notifications();
    /**
     * the enabled flag. Specifies whether automatic scaling is enabled for the resource. The default value is 'true'.
     */
    boolean enabled();
    /**
     * the name of the autoscale setting.
     */
    String name();
    /**
     * the resource identifier of the resource that the autoscale setting should be added to.
     */
    String targetResourceUri();


    interface Definition extends
        DefinitionStages.Blank,
        DefinitionStages.WithGroup,
        DefinitionStages.WithCreate{
    }

    interface DefinitionStages {

        interface DefineAutoscaleSettingResourceProfiles {
            /**
             * the collection of automatic scaling profiles that specify different scaling parameters for different time periods. A maximum of 20 profiles can be specified.
             *
             * @param name
             * @return the next stage
             */
            AutoscaleProfile.DefinitionStages.Blank<WithCreate> defineAutoscaleProfile(String name);
        }

        interface DefineAutoscaleSettingResourceNotifications {
            /**
             * the collection of notifications.
             *
             * @return the next stage
             */
            AutoscaleNotification.DefinitionStages.Blank<Definition> defineAutoscaleNotification();
        }

        interface WithAutoscaleSettingResourceEnabled {
            WithCreate withAutoscaleSettingEnabled();
            WithCreate withAutoscaleSettingDisabled();
        }

        interface WithAutoscaleSettingResourceTargetResourceUri {
            WithCreate withTargetResourceUri(String targetResourceUri);
        }

        interface WithCreate extends
            Creatable<AutoscaleSetting>,
            DefineAutoscaleSettingResourceProfiles,
            DefineAutoscaleSettingResourceNotifications,
            WithAutoscaleSettingResourceEnabled,
            WithAutoscaleSettingResourceTargetResourceUri{
        }

        interface Blank extends
            DefinitionWithRegion<WithGroup>{
        }

        interface WithGroup extends
            GroupableResource.DefinitionStages.WithGroup<DefineAutoscaleSettingResourceProfiles>{
        }
    }

    interface Update extends
            Appliable<AutoscaleSetting>,
            Resource.UpdateWithTags<Update>,
            UpdateStages.DefineAutoscaleSettingResourceProfiles,
            UpdateStages.DefineAutoscaleSettingResourceNotifications,
            UpdateStages.WithAutoscaleSettingResourceEnabled,
            UpdateStages.WithAutoscaleSettingResourceName,
            UpdateStages.WithAutoscaleSettingResourceTargetResourceUri {
    }

    interface UpdateStages {

        interface DefineAutoscaleSettingResourceProfiles {
            /**
             * the collection of automatic scaling profiles that specify different scaling parameters for different time periods. A maximum of 20 profiles can be specified.
             *
             * @return the next stage
             */
            AutoscaleProfile.DefinitionStages.Blank<Update> defineAutoscaleProfile(String name);
            /**
             * the collection of automatic scaling profiles that specify different scaling parameters for different time periods. A maximum of 20 profiles can be specified.
             *
             * @param name
             * @return the next stage
             */
            AutoscaleProfile.Update<Update> updateAutoscaleProfile(String name);
        }

        interface DefineAutoscaleSettingResourceNotifications {
            Update withoutAutoscaleNotification(String name);
            Update withoutAutoscaleNotifications();
            /**
             * the collection of notifications.
             *
             * @return the next stage
             */
            AutoscaleNotification.DefinitionStages.Blank<Update> defineAutoscaleNotification();
            /**
             * the collection of notifications.
             *
             * @param notification
             * @return the next stage
             */
            AutoscaleNotification.UpdateStages.Blank<Update> updateAutoscaleNotification(AutoscaleNotification notification);
        }

        interface WithAutoscaleSettingResourceEnabled {
            Update withAutoscaleSettingResource();
            Update withoutAutoscaleSettingResource();
        }

        interface WithAutoscaleSettingResourceName {
            Update withAutoscaleSettingResourceName(String name);
        }

        interface WithAutoscaleSettingResourceTargetResourceUri {
            Update withAutoscaleSettingResourceTargetResourceUri(String targetResourceUri);
        }
    }

}
