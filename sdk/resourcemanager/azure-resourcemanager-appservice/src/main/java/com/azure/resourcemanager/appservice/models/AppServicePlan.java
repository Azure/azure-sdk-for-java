// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.appservice.AppServiceManager;
import com.azure.resourcemanager.appservice.fluent.models.AppServicePlanInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.GroupableResource;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasName;
import com.azure.resourcemanager.resources.fluentcore.model.Appliable;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.Refreshable;
import com.azure.resourcemanager.resources.fluentcore.model.Updatable;

/** An immutable client-side representation of an Azure App service plan. */
@Fluent
public interface AppServicePlan
    extends GroupableResource<AppServiceManager, AppServicePlanInner>,
        HasName,
        Refreshable<AppServicePlan>,
        Updatable<AppServicePlan.Update> {
    /** @return maximum number of instances that can be assigned */
    int maxInstances();

    /** @return maximum number of instances that can be assigned */
    int capacity();

    /** @return if apps assigned to this App Service Plan can be scaled independently */
    boolean perSiteScaling();

    /** @return number of web apps assigned to this App Service Plan */
    int numberOfWebApps();

    /** @return the pricing tier information of the App Service Plan */
    PricingTier pricingTier();

    /** @return the operating system the web app is running on */
    OperatingSystem operatingSystem();

    /**************************************************************
     * Fluent interfaces to provision a App service plan
     **************************************************************/

    /** Container interface for all the definitions that need to be implemented. */
    interface Definition
        extends DefinitionStages.Blank,
            DefinitionStages.WithGroup,
            DefinitionStages.WithPricingTier,
            DefinitionStages.WithOperatingSystem,
            DefinitionStages.WithCreate {
    }

    /** Grouping of all the site definition stages. */
    interface DefinitionStages {
        /** The first stage of the app service plan definition. */
        interface Blank extends DefinitionWithRegion<WithGroup> {
        }

        /** An app service plan definition allowing resource group to be set. */
        interface WithGroup extends GroupableResource.DefinitionStages.WithGroup<WithPricingTier> {
        }

        /** An app service plan definition allowing pricing tier to be set. */
        interface WithPricingTier {
            /**
             * Specifies free pricing tier for the app service plan.
             *
             * @return the next stage of the definition
             */
            WithCreate withFreePricingTier();

            /**
             * Specifies shared pricing tier for the app service plan.
             *
             * @return the next stage of the definition
             */
            WithCreate withSharedPricingTier();

            /**
             * Specifies the pricing tier for the app service plan.
             *
             * @param pricingTier the pricing tier enum
             * @return the next stage of the definition
             */
            WithOperatingSystem withPricingTier(PricingTier pricingTier);
        }

        /** An app service plan definition allowing the operating system to be set. */
        interface WithOperatingSystem {
            /**
             * Specifies the operating system of the app service plan.
             *
             * @param operatingSystem the operating system
             * @return the next stage of the definition
             */
            WithCreate withOperatingSystem(OperatingSystem operatingSystem);
        }

        /** An app service plan definition allowing per site scaling configuration to be set. */
        interface WithPerSiteScaling {
            /**
             * Specifies whether per-site scaling will be turned on.
             *
             * @param perSiteScaling if each site can be scaled individually
             * @return the next stage of the definition
             */
            WithCreate withPerSiteScaling(boolean perSiteScaling);
        }

        /** An app service plan definition allowing instance capacity to be set. */
        interface WithCapacity {
            /**
             * Specifies the maximum number of instances running for this app service plan.
             *
             * @param capacity the maximum number of instances
             * @return the next stage of an app service plan definition
             */
            WithCreate withCapacity(int capacity);
        }

        /**
         * An app service plan definition with sufficient inputs to create a new website in the cloud, but exposing
         * additional optional inputs to specify.
         */
        interface WithCreate
            extends WithPerSiteScaling,
                WithCapacity,
                Creatable<AppServicePlan>,
                GroupableResource.DefinitionWithTags<WithCreate> {
        }
    }

    /** Grouping of all the site update stages. */
    interface UpdateStages {
        /** An app service plan definition allowing pricing tier to be set. */
        interface WithPricingTier {
            /**
             * Specifies the pricing tier for the app service plan.
             *
             * @param pricingTier the pricing tier enum
             * @return the next stage of the app service plan update
             */
            Update withPricingTier(PricingTier pricingTier);
        }

        /** An app service plan update allowing per site scaling configuration to be set. */
        interface WithPerSiteScaling {
            /**
             * Specifies whether per-site scaling will be turned on.
             *
             * @param perSiteScaling if each site can be scaled individually
             * @return the next stage of the app service plan update
             */
            Update withPerSiteScaling(boolean perSiteScaling);
        }

        /** An app service plan definition allowing instance capacity to be set. */
        interface WithCapacity {
            /**
             * Specifies the maximum number of instances running for this app service plan.
             *
             * @param capacity the maximum number of instances
             * @return the next stage of an app service plan update
             */
            Update withCapacity(int capacity);
        }
    }

    /** The template for a site update operation, containing all the settings that can be modified. */
    interface Update
        extends Appliable<AppServicePlan>,
            UpdateStages.WithCapacity,
            UpdateStages.WithPerSiteScaling,
            UpdateStages.WithPricingTier,
            GroupableResource.UpdateWithTags<Update> {
    }
}
