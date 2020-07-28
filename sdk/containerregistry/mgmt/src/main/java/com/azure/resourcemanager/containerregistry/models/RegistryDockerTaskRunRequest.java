// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.containerregistry.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.model.Attachable;
import java.util.List;
import java.util.Map;

/** An immutable client-side representation of an Azure registry Docker task run request. */
@Fluent()
public interface RegistryDockerTaskRunRequest {
    /** @return the length of the timeout. */
    int timeout();

    /** @return the properties of the platform. */
    PlatformProperties platform();

    /** @return the number of CPUs. */
    int cpuCount();

    /** @return the location of the source control. */
    String sourceLocation();

    /** @return whether archive is enabled. */
    boolean isArchiveEnabled();

    /** Container interface for all the definitions related to a registry Docker task run request. */
    interface Definition
        extends DefinitionStages.Blank,
            DefinitionStages.DockerFilePath,
            DefinitionStages.DockerTaskRunRequestStepAttachable {
    }

    /** Grouping of registry Docker task run request definition stages. */
    interface DefinitionStages {
        /** The first stage of a container registry Docker task run request definition. */
        interface Blank {
            /**
             * The function that begins the definition of the Docker task step in the task run request.
             *
             * @return the next stage of the container Docker task run request definition.
             */
            DockerFilePath defineDockerTaskStep();
        }

        /** The stage of the container Docker task run request definition that specifies the path to the Docker file. */
        interface DockerFilePath {
            /**
             * The function that specifies the path to the Docker file.
             *
             * @param path the path to the Docker file.
             * @return the next stage of the container Docker task run request definition.
             */
            DockerTaskRunRequestStepAttachable withDockerFilePath(String path);
        }

        /**
         * The stage of the definition which contains all the minimum required inputs for the resource to be attached,
         * but also allows for any other optional settings to be specified.
         */
        interface DockerTaskRunRequestStepAttachable
            extends Attachable<RegistryTaskRun.DefinitionStages.RunRequestExecutableWithSourceLocation> {
            /**
             * The function that specifies the list of image names.
             *
             * @param imageNames the list of image names.
             * @return the next stage of the container Docker task run request definition.
             */
            DockerTaskRunRequestStepAttachable withImageNames(List<String> imageNames);

            /**
             * The function that specifies push is enabled or not.
             *
             * @param enabled whether push is enabled.
             * @return the next stage of the container Docker task run request definition.
             */
            DockerTaskRunRequestStepAttachable withPushEnabled(boolean enabled);

            /**
             * The function that specifies a cache will be used or not.
             *
             * @param enabled whether caching is enabled or not.
             * @return the next stage of the container Docker task run request definition.
             */
            DockerTaskRunRequestStepAttachable withCacheEnabled(boolean enabled);

            /**
             * The function that specifies the overriding arguments and what they will override.
             *
             * @param overridingArguments map with key of the name of the argument to be overridden and value
             *     OverridingArgument specifying the content of the overriding argument.
             * @return the next stage of the container Docker task run request definition.
             */
            DockerTaskRunRequestStepAttachable withOverridingArguments(
                Map<String, OverridingArgument> overridingArguments);

            /**
             * The function that specifies the overriding argument and what it will override.
             *
             * @param name the name of the value to be overridden.
             * @param overridingArgument the content of the overriding argument.
             * @return the next stage of the container Docker task run request definition.
             */
            DockerTaskRunRequestStepAttachable withOverridingArgument(
                String name, OverridingArgument overridingArgument);
        }
    }
}
