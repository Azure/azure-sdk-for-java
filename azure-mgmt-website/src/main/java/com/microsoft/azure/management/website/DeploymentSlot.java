/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website;

import com.microsoft.azure.management.resources.fluentcore.arm.models.IndependentChildResource;
import com.microsoft.azure.management.resources.fluentcore.model.Updatable;

/**
 * An immutable client-side representation of an Azure Web App.
 */
public interface DeploymentSlot extends
        IndependentChildResource,
        WebAppBase<DeploymentSlot>,
        Updatable<DeploymentSlot.Update> {

    WebApp parent();

    /**************************************************************
     * Fluent interfaces to provision a Web App
     **************************************************************/

    /**
     * Container interface for all the definitions that need to be implemented.
     */
    interface Definition extends
            DefinitionStages.Blank,
            DefinitionStages.WithConfiguration,
            WebAppBase.Definition<DeploymentSlot> {
    }

    /**
     * Grouping of all the site definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of the web app definition.
         */
        interface Blank extends WithConfiguration {
        }

        interface WithConfiguration {
            WebAppBase.DefinitionStages.WithHostNameBinding<DeploymentSlot> withBrandNewConfiguration();
            WebAppBase.DefinitionStages.WithHostNameBinding<DeploymentSlot> withConfigurationFromParent();
            WebAppBase.DefinitionStages.WithHostNameBinding<DeploymentSlot> withConfigurationFromWebApp(WebApp webApp);
            WebAppBase.DefinitionStages.WithHostNameBinding<DeploymentSlot> withConfigurationFromDeploymentSlot(DeploymentSlot deploymentSlot);
        }

        /**
         * A site definition with sufficient inputs to create a new
         * website in the cloud, but exposing additional optional inputs to
         * specify.
         */
        interface WithCreate extends WebAppBase.DefinitionStages.WithCreate<DeploymentSlot> {
        }
    }

    /**
     * The template for a site update operation, containing all the settings that can be modified.
     */
    interface Update extends WebAppBase.Update<DeploymentSlot> {
    }
}