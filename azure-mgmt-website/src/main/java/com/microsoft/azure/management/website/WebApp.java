/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website;

import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.model.Updatable;

/**
 * An immutable client-side representation of an Azure Web App.
 */
public interface WebApp extends
        WebAppBase<WebApp>,
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
            DefinitionStages.WithGroup {
    }

    /**
     * Grouping of all the web app definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of the web app definition.
         */
        interface Blank extends GroupableResource.DefinitionWithRegion<WithGroup> {
        }

        /**
         * A web app definition allowing resource group to be set.
         */
        interface WithGroup extends GroupableResource.DefinitionStages.WithGroup<
                WebAppBase.DefinitionStages.WithAppServicePlan<WebApp>> {
        }
    }

    /**
     * The template for a web app update operation, containing all the settings that can be modified.
     */
    interface Update extends WebAppBase.Update<WebApp> {
    }
}