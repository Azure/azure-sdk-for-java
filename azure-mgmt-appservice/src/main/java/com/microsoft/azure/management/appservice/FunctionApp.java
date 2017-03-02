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
            DefinitionStages.WithGroup,
            DefinitionStages.WithSourceControl,
            DefinitionStages.WithCreate {
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
         * A web app definition allowing new app service plan's region to be set.
         */
        interface WithGroup extends GroupableResource.DefinitionStages.WithGroup<WithSourceControl> {
        }

        /**
         * A function app definition stage allowing source control to be set.
         */
        interface WithSourceControl extends WebAppBase.DefinitionStages.WithSourceControl<FunctionApp> {
        }

        interface WithCreate extends WebAppBase.DefinitionStages.WithCreate<FunctionApp> {

        }
    }

    /**
     * Grouping of all the web app update stages.
     */
    interface UpdateStages {
    }

    /**
     * The template for a web app update operation, containing all the settings that can be modified.
     */
    interface Update extends WebAppBase.Update<FunctionApp> {
    }
}