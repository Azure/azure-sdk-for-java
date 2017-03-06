/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.appservice;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Updatable;

/**
 * An immutable client-side representation of an Azure Function App.
 */
@Fluent(ContainerName = "/Microsoft.Azure.Management.AppService.Fluent")
public interface FunctionApp extends
    WebAppBase,
    Refreshable<FunctionApp>,
    Updatable<FunctionApp.Update> {

    /**************************************************************
     * Fluent interfaces to provision a Function App
     **************************************************************/

    /**
     * Container interface for all the definitions that need to be implemented.
     */
    interface Definition extends
        DefinitionStages.Blank,
        DefinitionStages.WithAppServicePlan,
        DefinitionStages.WithNewAppServicePlan,
        DefinitionStages.WithCreate {
    }

    /**
     * Grouping of all the function app definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of the function app definition.
         */
        interface Blank extends GroupableResource.DefinitionStages.WithGroupAndRegion<WithAppServicePlan> {
        }

        /**
         * A function app definition allowing app service plan to be set.
         */
        interface WithAppServicePlan {
            /**
             * Creates a new app service plan to use.
             * @return the next stage of the function app definition
             * @param name the name of the app service plan
             * @param region the region of the app service plan
             */
            WithNewAppServicePlan withNewAppServicePlan(String name, Region region);
            /**
             * Creates a new app service plan to use.
             * @return the next stage of the function app definition
             * @param name the name of the app service plan
             * @param regionName the region of the app service plan
             */
            WithNewAppServicePlan withNewAppServicePlan(String name, String regionName);

            /**
             * Uses an existing app service plan for the function app.
             * @param appServicePlan the existing app service plan
             * @return the next stage of the function app definition
             */
            WithCreate withExistingAppServicePlan(AppServicePlan appServicePlan);
        }

        /**
         * As function app definition allowing more information of a new app service plan to be set.
         */
        interface WithNewAppServicePlan {
            /**
             * Creates a new free app service plan to use. No custom domains or SSL bindings are available in this plan.
             * @return the next stage of the function app definition
             */
            WithCreate withFreePricingTier();

            /**
             * Creates a new free app service plan to use. No custom domains or SSL bindings are available in this plan.
             * @return the next stage of the function app definition
             */
            WithCreate withDynamicPricingTier();

            /**
             * Creates a new app service plan to use.
             * @param pricingTier the pricing tier to use
             * @return the next stage of the function app definition
             */
            WithCreate withPricingTier(AppServicePricingTier pricingTier);
        }

        interface WithCreate extends
            Creatable<FunctionApp>,
            GroupableResource.DefinitionWithTags<WithCreate>,
            WebAppBase.DefinitionStages.WithCreate<FunctionApp>,
            WebAppBase.DefinitionStages.WithSiteConfigs<WithCreate>,
            WebAppBase.DefinitionStages.WithAppSettings<WithCreate>,
            WebAppBase.DefinitionStages.WithConnectionString<WithCreate> {
        }
    }

    /**
     * Grouping of all the function app update stages.
     */
    interface UpdateStages {
        /**
         * A function app update allowing app service plan to be set.
         */
        interface WithAppServicePlan {
            /**
             * Creates a new app service plan to use.
             * @return the next stage of the function app definition
             * @param name the name of the app service plan
             */
            WithNewAppServicePlan withNewAppServicePlan(String name);

            /**
             * Uses an existing app service plan for the function app.
             * @param appServicePlan the existing app service plan
             * @return the next stage of the function app update
             */
            Update withExistingAppServicePlan(AppServicePlan appServicePlan);
        }

        /**
         * As function app update allowing more information of a new app service plan to be set.
         */
        interface WithNewAppServicePlan {
            /**
             * Creates a new free app service plan to use. No custom domains or SSL bindings are available in this plan.
             * @return the next stage of the function app update
             */
            Update withFreePricingTier();

            /**
             * Creates a new free app service plan to use. No custom domains or SSL bindings are available in this plan.
             * @return the next stage of the function app definition
             */
            Update withDynamicPricingTier();

            /**
             * Creates a new app service plan to use.
             * @param pricingTier the pricing tier to use
             * @return the next stage of the function app update
             */
            Update withPricingTier(AppServicePricingTier pricingTier);
        }
    }

    /**
     * The template for a function app update operation, containing all the settings that can be modified.
     */
    interface Update extends
        WebAppBase.Update<FunctionApp>,
        UpdateStages.WithAppServicePlan,
        UpdateStages.WithNewAppServicePlan,
        WebAppBase.UpdateStages.WithSiteConfigs<Update>,
        WebAppBase.UpdateStages.WithAppSettings<Update>,
        WebAppBase.UpdateStages.WithConnectionString<Update> {
    }
}