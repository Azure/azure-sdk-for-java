/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.appservice;

import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.model.Appliable;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Updatable;

/**
 * An immutable client-side representation of an Azure Web App.
 */
@Fluent(ContainerName = "/Microsoft.Azure.Management.AppService.Fluent")
@Beta
public interface WebApp extends
        WebAppBase,
        Refreshable<WebApp>,
        Updatable<WebApp.Update> {
    /**
     * @return the entry point to deployment slot management API under the web app
     */
    DeploymentSlots deploymentSlots();

    /**************************************************************
     * Fluent interfaces to provision a Web App
     **************************************************************/

    /**
     * Container interface for all the definitions that need to be implemented.
     */
    interface Definition extends
            DefinitionStages.Blank,
            DefinitionStages.ExistingAppServicePlanWithGroup,
            DefinitionStages.WithNewAppServicePlan,
            DefinitionStages.WithCreate {
    }

    /**
     * Grouping of all the web app definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of the web app definition.
         */
        interface Blank extends DefinitionWithRegion<NewAppServicePlanWithGroup> {
            /**
             * Uses an existing app service plan for the web app.
             * @param appServicePlan the existing app service plan
             * @return the next stage of the web app definition
             */
            ExistingAppServicePlanWithGroup withExistingAppServicePlan(AppServicePlan appServicePlan);
        }

        /**
         * A web app definition allowing resource group to be specified when an existing app service plan is used.
         */
        interface ExistingAppServicePlanWithGroup extends GroupableResource.DefinitionStages.WithGroup<WithCreate> {
        }

        /**
         * A web app definition allowing resource group to be specified when a new app service plan is to be created.
         */
        interface NewAppServicePlanWithGroup {
            /**
             * Associates the resource with an existing resource group.
             * @param groupName the name of an existing resource group to put this resource in.
             * @return the next stage of the resource definition
             */
            WithNewAppServicePlan withExistingResourceGroup(String groupName);

            /**
             * Associates the resource with an existing resource group.
             * @param group an existing resource group to put the resource in
             * @return the next stage of the resource definition
             */
            WithNewAppServicePlan withExistingResourceGroup(ResourceGroup group);

            /**
             * Creates a new resource group to put the resource in.
             * <p>
             * The group will be created in the same location as the resource.
             * @param name the name of the new group
             * @return the next stage of the resource definition
             */
            WithNewAppServicePlan withNewResourceGroup(String name);

            /**
             * Creates a new resource group to put the resource in.
             * <p>
             * The group will be created in the same location as the resource.
             * The group's name is automatically derived from the resource's name.
             * @return the next stage of the resource definition
             */
            WithNewAppServicePlan withNewResourceGroup();

            /**
             * Creates a new resource group to put the resource in, based on the definition specified.
             * @param groupDefinition a creatable definition for a new resource group
             * @return the next stage of the resource definition
             */
            WithNewAppServicePlan withNewResourceGroup(Creatable<ResourceGroup> groupDefinition);
        }

        /**
         * A web app definition allowing app service plan to be set.
         */
        interface WithNewAppServicePlan {
            /**
             * Creates a new free app service plan. This will fail if there are 10 or more
             * free plans in the current subscription.
             *
             * @return the next stage of the web app definition
             */
            WithCreate withNewFreeAppServicePlan();

            /**
             * Creates a new shared app service plan.
             *
             * @return the next stage of the web app definition
             */
            WithCreate withNewSharedAppServicePlan();

            /**
             * Creates a new app service plan to use.
             *
             * @param operatingSystem the operating system of the VM running the web app
             * @param pricingTier the sku of the app service plan
             * @return the next stage of the web app definition
             */
            WithCreate withNewAppServicePlan(OperatingSystem operatingSystem, PricingTier pricingTier);

            /**
             * Creates a new app service plan to use.
             *
             * @param appServicePlanCreatable the new app service plan creatable
             * @return the next stage of the web app definition
             */
            WithCreate withNewAppServicePlan(Creatable<AppServicePlan> appServicePlanCreatable);
        }

        /**
         * A site definition with sufficient inputs to create a new web app /
         * deployments slot in the cloud, but exposing additional optional
         * inputs to specify.
         */
        interface WithCreate extends
            Creatable<WebApp>,
            WebAppBase.DefinitionStages.WithCreate<WebApp> {
        }
    }

    /**
     * Grouping of all the web app update stages.
     */
    interface UpdateStages {
        /**
         * A web app update allowing app service plan to be set.
         */
        interface WithAppServicePlan {
            /**
             * Creates a new free app service plan. This will fail if there are 10 or more
             * free plans in the current subscription.
             *
             * @return the next stage of the web app update
             */
            Update withNewFreeAppServicePlan();

            /**
             * Creates a new shared app service plan.
             *
             * @return the next stage of the web app update
             */
            Update withNewSharedAppServicePlan();

            /**
             * Creates a new app service plan to use.
             *
             * @param pricingTier the sku of the app service plan
             * @return the next stage of the web app update
             */
            Update withNewAppServicePlan(PricingTier pricingTier);

            /**
             * Creates a new app service plan to use.
             *
             * @param appServicePlanCreatable the new app service plan creatable
             * @return the next stage of the web app update
             */
            Update withNewAppServicePlan(Creatable<AppServicePlan> appServicePlanCreatable);

            /**
             * Uses an existing app service plan for the web app.
             * @param appServicePlan the existing app service plan
             * @return the next stage of the web app update
             */
            Update withExistingAppServicePlan(AppServicePlan appServicePlan);
        }
    }

    /**
     * The template for a web app update operation, containing all the settings that can be modified.
     */
    interface Update extends
        Appliable<WebApp>,
        UpdateStages.WithAppServicePlan,
        WebAppBase.Update<WebApp> {
    }
}