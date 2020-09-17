// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.containerregistry.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.containerregistry.fluent.inner.RunInner;
import com.azure.resourcemanager.resources.fluentcore.model.Executable;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;
import com.azure.resourcemanager.resources.fluentcore.model.Refreshable;
import java.time.OffsetDateTime;
import java.util.Map;

/** An immutable client-side representation of an Azure RegistryDockerTaskRunRequest registry task run request. */
@Fluent()
public interface RegistryTaskRun extends HasInner<RunInner>, Refreshable<RegistryTaskRun> {
    /** @return the name of the resource group for this task run request */
    String resourceGroupName();

    /** @return the registry name of this task run request */
    String registryName();

    /**
     * @return the name of the task in the case of a TaskRunRequest (or null if task is still queued), null in other
     *     cases
     */
    String taskName();

    /** @return the status of the run request */
    RunStatus status();

    /** @return the run type of the run request */
    RunType runType();

    /** @return the last time the run request was updated */
    OffsetDateTime lastUpdatedTime();

    /** @return the time when the run request was created */
    OffsetDateTime createTime();

    /** @return whether archiving is enabled for the run request */
    boolean isArchiveEnabled();

    /** @return the platform properties of the run request */
    PlatformProperties platform();

    /** @return the numbers of cpu */
    int cpu();

    /** @return the provisioning state of the run request */
    ProvisioningState provisioningState();

    /** @return the id of the run */
    String runId();

    /** Container interface for all the definitions related to a RegistryTaskRun. */
    interface Definition
        extends DefinitionStages.BlankFromRegistry,
            DefinitionStages.BlankFromRuns,
            DefinitionStages.Platform,
            DefinitionStages.PlatformAltTaskRunRequest,
            DefinitionStages.RegistryTaskRunRequest,
            DefinitionStages.RunRequestType,
            DefinitionStages.RunRequestExecutableWithSourceLocation,
            DefinitionStages.RunRequestExecutable {
    }

    /** Grouping of registry task run definition stages. */
    interface DefinitionStages {
        /** The first stage of a a RegistryTaskRun definition if originating from a call on a registry. */
        interface BlankFromRegistry extends PlatformAltTaskRunRequest {
        }

        /**
         * The first stage of a RegistryTaskRun definition if definition is originating from a call on an existing
         * RegistryTaskRun.
         */
        interface BlankFromRuns {
            /**
             * The function that specifies the registry this task run is called on.
             *
             * @param resourceGroupName the name of the resource group of the registry.
             * @param registryName the name of the registry.
             * @return the next stage of the container registry task run definition.
             */
            PlatformAltTaskRunRequest withExistingRegistry(String resourceGroupName, String registryName);
        }

        /**
         * The stage of the container registry task run definition that allows to specify the task run is going to be
         * run with a TaskRunRequest.
         */
        interface PlatformAltTaskRunRequest extends Platform {
            /**
             * The function that specifies the name of the existing task to run.
             *
             * @param taskName the name of the created task to pass into the task run request.
             * @return the next stage of the container registry task run definition.
             */
            RegistryTaskRunRequest withTaskRunRequest(String taskName);
        }

        /**
         * The stage of the container registry task definition for TaskRunRequests that allows the user to specify
         * overriding values and whether archiving is enabled or not.
         */
        interface RegistryTaskRunRequest extends RunRequestExecutable {
            /**
             * The function that specifies whether there are any values that will be overridden and what they will be
             * overridden by.
             *
             * @param overridingValues a map that has the name of the value to be overridden as the key and the value is
             *     an OverridingValue.
             * @return the next stage of the container registry task run definition.
             */
            RegistryTaskRunRequest withOverridingValues(Map<String, OverridingValue> overridingValues);

            /**
             * The function that specifies whether a single value will be overridden and what it will be overridden by.
             *
             * @param name the name of the value to be overridden.
             * @param overridingValue the OverridingValue specifying what the value will be overridden with.
             * @return the next stage of the container registry task run definition.
             */
            RegistryTaskRunRequest withOverridingValue(String name, OverridingValue overridingValue);

            /**
             * The function that specifies archiving will or will not be enabled.
             *
             * @param enabled whether archive will be enabled.
             * @return the next stage of the container registry task run definition.
             */
            RegistryTaskRunRequest withArchiveEnabled(boolean enabled);
        }

        /**
         * The stage of the container registry task definition that specifies the platform for the container registry
         * task run.
         */
        interface Platform {
            /**
             * The function that specifies the platform will have a Linux OS.
             *
             * @return the next stage of the container registry task run definition.
             */
            RunRequestType withLinux();

            /**
             * The function that specifies the platform will have a Windows OS.
             *
             * @return the next stage of the container registry task run definition.
             */
            RunRequestType withWindows();

            /**
             * The function that specifies the platform will have a Linux OS with Architecture architecture.
             *
             * @param architecture the architecture the platform will have.
             * @return the next stage of the container registry task run definition.
             */
            RunRequestType withLinux(Architecture architecture);

            /**
             * The function that specifies the platform will have a Windows OS with Architecture architecture.
             *
             * @param architecture the architecture the platform will have.
             * @return the next stage of the container registry task run definition.
             */
            RunRequestType withWindows(Architecture architecture);

            /**
             * The function that specifies the platform will have a Linux OS with Architecture architecture and Variant
             * variant.
             *
             * @param architecture the architecture the platform will have.
             * @param variant the variant the platform will have.
             * @return the next stage of the container registry task run definition.
             */
            RunRequestType withLinux(Architecture architecture, Variant variant);

            /**
             * The function that specifies the platform will have a Windows OS with Architecture architecture and
             * Variant variant.
             *
             * @param architecture the architecture the platform will have.
             * @param variant the variant the platform will have.
             * @return the next stage of the container registry task run definition.
             */
            RunRequestType withWindows(Architecture architecture, Variant variant);

            /**
             * The function that specifies the platform properties of the registry task run.
             *
             * @param platformProperties the properties of the platform.
             * @return the next stage of the container registry task run definition.
             */
            RunRequestType withPlatform(PlatformProperties platformProperties);
        }

        /** The stage of the definition that specifies the task run request type. */
        interface RunRequestType {
            /**
             * The function that specifies the task run request type will be a file task.
             *
             * @return the next stage of the container registry task run definition.
             */
            RegistryFileTaskRunRequest.DefinitionStages.Blank withFileTaskRunRequest();

            /**
             * The function that specifies the task run request type will be an encoded task.
             *
             * @return the next stage of the container registry task run definition.
             */
            RegistryEncodedTaskRunRequest.DefinitionStages.Blank withEncodedTaskRunRequest();

            /**
             * The function that specifies the task run request type will be a Docker task.
             *
             * @return the next stage of the container registry task run definition.
             */
            RegistryDockerTaskRunRequest.DefinitionStages.Blank withDockerTaskRunRequest();
        }

        /**
         * The stage of the container registry task run that specifies the AgentConfiguration for the container registry
         * task run.
         */
        interface AgentConfiguration {
            /**
             * The function that specifies the count of the CPU.
             *
             * @param count the CPU count.
             * @return the next stage of the container registry task run definition.
             */
            RunRequestExecutable withCpuCount(int count);
        }

        /**
         * The stage of the container registry task run definition that specifies the enabling and disabling of
         * archiving.
         */
        interface Archive {
            /**
             * The function that specifies archiving is enabled or disabled.
             *
             * @param enabled whether archiving is enabled or not.
             * @return the next stage of the container registry task run definition.
             */
            RunRequestExecutable withArchiveEnabled(boolean enabled);
        }

        /**
         * The stage of the container registry task run definition which contains all the minimum required inputs for
         * the resource to be executed if the task run request type is either file, encoded, or Docker, but also allows
         * for any other optional settings to be specified.
         */
        interface RunRequestExecutableWithSourceLocation extends AgentConfiguration, RunRequestExecutable {
            /**
             * The function that specifies the location of the source control.
             *
             * @param location the location of the source control.
             * @return the next stage of the container registry task run definition.
             */
            RunRequestExecutableWithSourceLocation withSourceLocation(String location);

            /**
             * The function that specifies the timeout.
             *
             * @param timeout the time the timeout lasts.
             * @return the next stage of the container registry task run definition.
             */
            RunRequestExecutableWithSourceLocation withTimeout(int timeout);
        }

        /**
         * The stage of the definition in the case of using a TaskRunRequest which contains all the minimum required
         * inputs for the resource to be executed, but also allows for any other optional settings to be specified.
         */
        interface RunRequestExecutable extends Archive, Executable<RegistryTaskRun> {
        }
    }
}
