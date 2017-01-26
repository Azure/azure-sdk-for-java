/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.appservice;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.appservice.implementation.AppServiceManager;
import com.microsoft.azure.management.resources.fluentcore.arm.models.IndependentChildResource;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Updatable;

/**
 * An immutable client-side representation of an Azure Web App deployment slot.
 */
@Fluent(ContainerName = "/Microsoft.Azure.Management.AppService.Fluent")
public interface DeploymentSlot extends
        IndependentChildResource<AppServiceManager>,
        WebAppBase,
        Refreshable<DeploymentSlot>,
        Updatable<DeploymentSlot.Update> {

    /**
     * @return the web app containing this deployment slot
     */
    WebApp parent();

    /**************************************************************
     * Fluent interfaces to provision a deployment slot
     **************************************************************/

    /**
     * Container interface for all the definitions that need to be implemented.
     */
    interface Definition extends
            DefinitionStages.Blank,
            DefinitionStages.WithConfiguration {
    }

    /**
     * Grouping of all the deployment slot definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of the deployment slot definition.
         */
        interface Blank extends WithConfiguration {
        }

        /**
         * A deployment slot definition allowing the configuration to clone from to be specified.
         */
        interface WithConfiguration {
            /**
             * Creates the deployment slot with brand new site configurations.
             * @return the next stage of the deployment slot definition
             */
            WebAppBase.DefinitionStages.WithHostNameBinding<DeploymentSlot> withBrandNewConfiguration();

            /**
             * Copies the site configurations from the web app the deployment slot belongs to.
             * @return the next stage of the deployment slot definition
             */
            WebAppBase.DefinitionStages.WithHostNameBinding<DeploymentSlot> withConfigurationFromParent();

            /**
             * Copies the site configurations from a given web app.
             * @param webApp the web app to copy the configurations from
             * @return the next stage of the deployment slot definition
             */
            WebAppBase.DefinitionStages.WithHostNameBinding<DeploymentSlot> withConfigurationFromWebApp(WebApp webApp);

            /**
             * Copies the site configurations from a given deployment slot.
             * @param deploymentSlot the deployment slot to copy the configurations from
             * @return the next stage of the deployment slot definition
             */
            WebAppBase.DefinitionStages.WithHostNameBinding<DeploymentSlot> withConfigurationFromDeploymentSlot(DeploymentSlot deploymentSlot);
        }
    }

    /**
     * The template for a deployment slot update operation, containing all the settings that can be modified.
     */
    interface Update extends WebAppBase.Update<DeploymentSlot> {
    }
}