// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.containerregistry.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.model.Attachable;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;
import com.azure.resourcemanager.resources.fluentcore.model.Settable;
import java.util.List;
import java.util.Map;

/** An immutable client-side representation of an Azure RegistryDockerTaskStep registry task. */
@Fluent()
public interface RegistryDockerTaskStep extends HasInner<DockerTaskStep>, RegistryTaskStep {
    /** @return the image names of this Docker task step */
    List<String> imageNames();

    /** @return whether push is enabled for this Docker task step */
    boolean isPushEnabled();

    /** @return whether there is no cache for this Docker task step */
    boolean noCache();

    /** @return Docker file path for this Docker task step */
    String dockerFilePath();

    /** @return the arguments this Docker task step */
    List<Argument> arguments();

    /** Container interface for all the definitions related to a RegistryDockerTaskStep. */
    interface Definition
        extends RegistryDockerTaskStep.DefinitionStages.Blank,
            RegistryDockerTaskStep.DefinitionStages.DockerFilePath,
            RegistryDockerTaskStep.DefinitionStages.DockerTaskStepAttachable {
    }

    /** Container interface for all the updates related to a RegistryDockerTaskStep. */
    interface Update
        extends RegistryDockerTaskStep.UpdateStages.DockerFilePath,
            RegistryDockerTaskStep.UpdateStages.ImageNames,
            RegistryDockerTaskStep.UpdateStages.Push,
            RegistryDockerTaskStep.UpdateStages.Cache,
            RegistryDockerTaskStep.UpdateStages.OverridingArgumentUpdate,
            Settable<RegistryTask.Update> {
    }

    /** Grouping of registry Docker task definition stages. */
    interface DefinitionStages {

        /** The first stage of a DockerFileTaskStep definition. */
        interface Blank extends DockerFilePath {
        }

        /**
         * The stage of the container registry DockerTaskStep definition allowing to specify the path to the Docker
         * file.
         */
        interface DockerFilePath {
            /**
             * The function that specifies the path to the Docker file.
             *
             * @param path the path to the Docker file.
             * @return the next stage of the container registry DockerTaskStep definition.
             */
            DockerTaskStepAttachable withDockerFilePath(String path);
        }

        /**
         * The stage of the definition which contains all the minimum required inputs for the resource to be attached,
         * but also allows for any other optional settings to be specified.
         */
        interface DockerTaskStepAttachable extends Attachable<RegistryTask.DefinitionStages.SourceTriggerDefinition> {
            /**
             * The function that specifies the list of image names.
             *
             * @param imageNames the image names.
             * @return the next step of the container registry DockerTaskStep definition.
             */
            DockerTaskStepAttachable withImageNames(List<String> imageNames);

            /**
             * The function that enables push depending on user input parameter.
             *
             * @param enabled whether push will be enabled
             * @return the next step of the container registry DockerTaskStep definition.
             */
            DockerTaskStepAttachable withPushEnabled(boolean enabled);

            /**
             * The function that specifies the use of a cache based on user input parameter.
             *
             * @param enabled whether caching will be enabled.
             * @return the next step of the container registry DockerTaskStep definition.
             */
            DockerTaskStepAttachable withCacheEnabled(boolean enabled);

            /**
             * The function that specifies the overriding arguments and what they will override.
             *
             * @param overridingArguments map with key of the name of the value to be overridden and value
             *     OverridingArgument specifying the content of the overriding argument.
             * @return the next stage of the container Docker task step definition.
             */
            DockerTaskStepAttachable withOverridingArguments(Map<String, OverridingArgument> overridingArguments);

            /**
             * The function that specifies the overriding argument and what it will override.
             *
             * @param name the name of the value to be overridden.
             * @param overridingArgument the content of the overriding argument.
             * @return the next stage of the container Docker task step definition.
             */
            DockerTaskStepAttachable withOverridingArgument(String name, OverridingArgument overridingArgument);
        }
    }

    /** Grouping of registry Docker task update stages. */
    interface UpdateStages {
        /** The stage of the container registry DockerTaskStep update allowing to specify the Docker file path. */
        interface DockerFilePath {
            /**
             * The function that specifies the path to the Docker file.
             *
             * @param path the path to the Docker file.
             * @return the next stage of the container registry DockerTaskStep update.
             */
            Update withDockerFilePath(String path);
        }

        /** The stage of the container registry DockerTaskStep update allowing to specify the image names. */
        interface ImageNames {
            /**
             * The function that specifies the image names.
             *
             * @param imageNames the list of the names of the images.
             * @return the next stage of the container registry DockerTaskStep update.
             */
            Update withImageNames(List<String> imageNames);
        }

        /**
         * The stage of the container registry DockerTaskStep update allowing to specify whether push is enabled or not.
         */
        interface Push {
            /**
             * The function that specifies push is enabled.
             *
             * @param enabled whether push is enabled.
             * @return the next stage of the container registry DockerTaskStep update.
             */
            Update withPushEnabled(boolean enabled);
        }

        /**
         * The stage of the container registry DockerTaskStep update allowing to specify whether to have a cache or not.
         */
        interface Cache {
            /**
             * The function that specifies the task has a cache.
             *
             * @param enabled whether caching is enabled.
             * @return the next stage of the container registry DockerTaskStep update.
             */
            Update withCacheEnabled(boolean enabled);
        }

        /** The stage of the container registry DockerTaskStep update allowing to specify any overriding arguments. */
        interface OverridingArgumentUpdate {
            /**
             * The function that specifies the overriding arguments and what they will override.
             *
             * @param overridingArguments map with key of the name of the value to be overridden and value
             *     OverridingArgument specifying the content of the overriding argument.
             * @return the next stage of the container Docker task step update.
             */
            Update withOverridingArguments(Map<String, OverridingArgument> overridingArguments);

            /**
             * The function that specifies the overriding argument and what it will override.
             *
             * @param name the name of the value to be overridden.
             * @param overridingArgument the content of the overriding argument.
             * @return the next stage of the container Docker task step update.
             */
            Update withOverridingArgument(String name, OverridingArgument overridingArgument);
        }
    }
}
