// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.models;

import com.azure.resourcemanager.resources.fluentcore.model.Appliable;
import com.azure.resourcemanager.resources.fluentcore.model.Updatable;

/**
 * An immutable client-side representation of an Azure Web App deployment slot.
 *
 * @param <FluentT> the type of the resource
 */
public interface DeploymentSlotBase<FluentT> extends
    WebAppBase,
    Updatable<DeploymentSlotBase.Update<FluentT>> {

    /**
     * Grouping of all the deployment slot update stages.
     */
    interface UpdateStages {
        /**
         * A deployment slot update allowing runtime version to be specified.
         */
        interface WithRuntimeVersion<FluentT> {
            /**
             * Specifies the runtime for the function app.
             * @param runtime the Azure Functions runtime
             * @return the next stage of the definition
             */
            Update<FluentT> withRuntime(String runtime);

            /**
             * Specifies the runtime version for the function app.
             * @param version the version of the Azure Functions runtime
             * @return the next stage of the definition
             */
            Update<FluentT> withRuntimeVersion(String version);

            /**
             * Uses the latest runtime version for the function app.
             * @return the next stage of the definition
             */
            Update<FluentT> withLatestRuntimeVersion();
        }

        /**
         * A deployment slot update allowing docker image source to be specified.
         */
        interface WithDockerContainerImage<FluentT> {
            /**
             * Specifies the docker container image to be one from Docker Hub.
             * @param imageAndTag image and optional tag (eg 'image:tag')
             * @return the next stage of the web app update
             */
            Update<FluentT> withPublicDockerHubImage(String imageAndTag);

            /**
             * Specifies the docker container image to be one from Docker Hub.
             * @param imageAndTag image and optional tag (eg 'image:tag')
             * @return the next stage of the web app update
             */
            UpdateStages.WithCredentials<FluentT> withPrivateDockerHubImage(String imageAndTag);

            /**
             * Specifies the docker container image to be one from a private registry.
             * @param imageAndTag image and optional tag (eg 'image:tag')
             * @param serverUrl the URL to the private registry server
             * @return the next stage of the web app update
             */
            UpdateStages.WithCredentials<FluentT> withPrivateRegistryImage(String imageAndTag, String serverUrl);
        }

        /**
         * A deployment slot update allowing docker hub credentials to be set.
         */
        interface WithCredentials<FluentT> {
            /**
             * Specifies the username and password for Docker Hub.
             * @param username the username for Docker Hub
             * @param password the password for Docker Hub
             * @return the next stage of the web app update
             */
            Update<FluentT> withCredentials(String username, String password);
        }

        /**
         * A deployment slot update allowing docker startup command to be specified.
         * This will replace the "CMD" section in the Dockerfile.
         */
        interface WithStartUpCommand<FluentT> {
            /**
             * Specifies the startup command.
             *
             * @param startUpCommand startup command to replace "CMD" in Dockerfile
             * @return the next stage of the web app update
             */
            Update<FluentT> withStartUpCommand(String startUpCommand);
        }
    }

    /** The template for a web app update operation, containing all the settings that can be modified. */
    interface Update<FluentT> extends
        Appliable<FluentT>,
        WebAppBase.Update<FluentT>,
        UpdateStages.WithRuntimeVersion<FluentT>,
        UpdateStages.WithDockerContainerImage<FluentT>,
        UpdateStages.WithStartUpCommand<FluentT>,
        UpdateStages.WithCredentials<FluentT> {
    }
}
