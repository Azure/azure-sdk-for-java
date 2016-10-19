/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website;

import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasName;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Updatable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;
import com.microsoft.azure.management.website.implementation.AppServicePlanInner;

/**
 * An immutable client-side representation of an Azure Web App.
 */
public interface AppServicePlan extends
        GroupableResource,
        HasName,
        Refreshable<AppServicePlan>,
        Updatable<AppServicePlan.Update>,
        Wrapper<AppServicePlanInner> {

    /**
     * @return target worker tier assigned to the App Service Plan.
     */
    String workerTierName();

    /**
     * @return app Service Plan Status. Possible values include: 'Ready', 'Pending'.
     */
    StatusOptions status();

    /**
     * @return app Service Plan Subscription.
     */
    String subscription();

    /**
     * @return app Service Plan administration site.
     */
    String adminSiteName();

    /**
     * @return specification for the hosting environment (App Service Environment) to
     * use for the App Service Plan.
     */
    HostingEnvironmentProfile hostingEnvironmentProfile();

    /**
     * @return maximum number of instances that can be assigned to this App Service
     * Plan.
     */
    int maximumNumberOfWorkers();

    /**
     * @return geographical location for the App Service Plan.
     */
    String geoRegion();

    /**
     * @return if True apps assigned to this App Service Plan can be scaled
     * independently
     * If False apps assigned to this App Service Plan will scale
     * to all instances of the plan.
     */
    boolean perSiteScaling();

    /**
     * @return number of web apps assigned to this App Service Plan.
     */
    int numberOfSites();

    /**
     * @return resource group of the server farm.
     */
    String resourceGroup();

    /**
     * @return the sku property.
     */
    SkuDescription sku();

    /**************************************************************
     * Fluent interfaces to provision a App service plan
     **************************************************************/

    /**
     * Container interface for all the definitions that need to be implemented.
     */
    interface Definition extends
            DefinitionStages.Blank,
            DefinitionStages.WithGroup,
            DefinitionStages.WithPricingTier,
            DefinitionStages.WithCreate {
    }

    /**
     * Grouping of all the site definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of the app service plan definition.
         */
        interface Blank extends DefinitionWithRegion<WithGroup> {
        }

        /**
         * An app service plan definition allowing resource group to be set.
         */
        interface WithGroup extends GroupableResource.DefinitionStages.WithGroup<WithPricingTier> {
        }

        /**
         * An app service plan definition allowing pricing tier to be set.
         */
        interface WithPricingTier {
            WithCreate withPricingTier(AppServicePricingTier pricingTier);
        }

        /**
         * An app service plan definition with sufficient inputs to create a new
         * website in the cloud, but exposing additional optional inputs to
         * specify.
         */
        interface WithCreate extends Creatable<AppServicePlan> {
        }
    }

    /**
     * Grouping of all the site update stages.
     */
    interface UpdateStages {

    }

    /**
     * The template for a site update operation, containing all the settings that can be modified.
     */
    interface Update {
    }
}