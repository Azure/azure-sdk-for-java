/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.appservice;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Updatable;

/**
 * An immutable client-side representation of an Azure Web App.
 */
@Fluent(ContainerName = "/Microsoft.Azure.Management.AppService.Fluent")
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
            DefinitionStages.WithRegion,
            DefinitionStages.WithAppServicePlan,
            DefinitionStages.WithNewAppServicePlan {
    }

    /**
     * Grouping of all the web app definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of the web app definition.
         */
        interface Blank extends GroupableResource.DefinitionStages.WithGroup<WithAppServicePlan> {
        }

        /**
         * A web app definition allowing new app service plan's region to be set.
         */
        interface WithRegion extends GroupableResource.DefinitionWithRegion<WithNewAppServicePlan> {
        }

        /**
         * A web app definition allowing app service plan to be set.
         */
        interface WithAppServicePlan {
            /**
             * Creates a new app service plan to use.
             * @return the next stage of the web app definition
             * @param name the name of the app service plan
             */
            WithRegion withNewAppServicePlan(String name);

            /**
             * Uses an existing app service plan for the web app.
             * @param appServicePlan the existing app service plan
             * @return the next stage of the web app definition
             */
            WebAppBase.DefinitionStages.WithHostNameBinding<WebApp> withExistingAppServicePlan(AppServicePlan appServicePlan);
        }

        /**
         * As web app definition allowing more information of a new app service plan to be set.
         */
        interface WithNewAppServicePlan {
            /**
             * Creates a new free app service plan to use. No custom domains or SSL bindings are available in this plan.
             * @return the next stage of the web app definition
             */
            WebAppBase.DefinitionStages.WithCreate<WebApp> withFreePricingTier();

            /**
             * Creates a new app service plan to use.
             * @param pricingTier the pricing tier to use
             * @return the next stage of the web app definition
             */
            WebAppBase.DefinitionStages.WithHostNameBinding<WebApp> withPricingTier(AppServicePricingTier pricingTier);
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
             * Creates a new app service plan to use.
             * @return the next stage of the web app update
             * @param name the name of the app service plan
             */
            WithNewAppServicePlan withNewAppServicePlan(String name);

            /**
             * Uses an existing app service plan for the web app.
             * @param appServicePlan the existing app service plan
             * @return the next stage of the web app update
             */
            Update withExistingAppServicePlan(AppServicePlan appServicePlan);
        }

        /**
         * As web app update allowing more information of a new app service plan to be set.
         */
        interface WithNewAppServicePlan {
            /**
             * Creates a new free app service plan to use. No custom domains or SSL bindings are available in this plan.
             * @return the next stage of the web app update
             */
            Update withFreePricingTier();

            /**
             * Creates a new app service plan to use.
             * @param pricingTier the pricing tier to use
             * @return the next stage of the web app update
             */
            Update withPricingTier(AppServicePricingTier pricingTier);
        }
    }

    /**
     * The template for a web app update operation, containing all the settings that can be modified.
     */
    interface Update extends
            WebAppBase.Update<WebApp>,
            UpdateStages.WithAppServicePlan,
            UpdateStages.WithNewAppServicePlan {
    }
}