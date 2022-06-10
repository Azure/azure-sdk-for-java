// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.containerregistry.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.model.Attachable;
import com.azure.resourcemanager.resources.fluentcore.model.Settable;
import java.util.List;
import java.util.Map;

/** An immutable client-side representation of an Azure RegistryFileTaskStep registry task. */
@Fluent()
public interface RegistryFileTaskStep extends RegistryTaskStep {
    /** @return the task file path of this file task step */
    String taskFilePath();

    /** @return the values file path of this file task step */
    String valuesFilePath();

    /** @return the values of this file task step */
    List<SetValue> values();

    /** Container interface for all the definitions related to a RegistryFileTaskStep. */
    interface Definition
        extends RegistryFileTaskStep.DefinitionStages.Blank,
            RegistryFileTaskStep.DefinitionStages.FileTaskPath,
            RegistryFileTaskStep.DefinitionStages.FileTaskStepAttachable {
    }

    /** Container interface for all the updates related to a RegistryFileTaskStep. */
    interface Update
        extends RegistryFileTaskStep.UpdateStages.FileTaskPath,
            RegistryFileTaskStep.UpdateStages.ValuePath,
            RegistryFileTaskStep.UpdateStages.OverridingValues,
            Settable<RegistryTask.Update> {
    }

    /** Grouping of registry file task definition stages. */
    interface DefinitionStages {

        /** The first stage of a RegistryFileTaskStep definition. */
        interface Blank extends FileTaskPath {
        }

        /** The stage of the container registry FileTaskStep definition allowing to specify the task path. */
        interface FileTaskPath {
            /**
             * The function that specifies the path to the task file.
             *
             * @param path the path to the task file.
             * @return the next stage of the container registry FileTaskStep definition.
             */
            FileTaskStepAttachable withTaskPath(String path);
        }

        /**
         * The stage of the definition which contains all the minimum required inputs for the resource to be attached,
         * but also allows for any other optional settings to be specified.
         */
        interface FileTaskStepAttachable extends Attachable<RegistryTask.DefinitionStages.SourceTriggerDefinition> {
            /**
             * The function that specifies the path to the values.
             *
             * @param path the path to the values.
             * @return the next stage of the container registry FileTaskStep definition.
             */
            FileTaskStepAttachable withValuesPath(String path);

            /**
             * The function that specifies the values that override the corresponding values specified under the
             * function withValuesPath().
             *
             * @param overridingValues a map which contains the values that will override the corresponding values
             *     specified under the function withValuesPath().
             * @return the next stage of the container registry FileTaskStep definition.
             */
            FileTaskStepAttachable withOverridingValues(Map<String, OverridingValue> overridingValues);

            /**
             * The function that specifies a single value that will override the corresponding value specified under the
             * function withValuesPath().
             *
             * @param name the name of the value to be overridden.
             * @param overridingValue the value of the value to be overridden.
             * @return the next stage of the container registry FileTaskStep definition.
             */
            FileTaskStepAttachable withOverridingValue(String name, OverridingValue overridingValue);
        }
    }

    /** Grouping of registry file task update stages. */
    interface UpdateStages {
        /** The stage of the container registry FileTaskStep update allowing to specify the task path. */
        interface FileTaskPath {
            /**
             * The function that specifies the path to the task file.
             *
             * @param path the path to the task file.
             * @return the next stage of the container registry FileTaskStep update.
             */
            Update withTaskPath(String path);
        }

        /** The stage of the container registry FileTaskStep update allowing to specify the path to the values. */
        interface ValuePath {
            /**
             * The function that specifies the path to the values.
             *
             * @param path the path to the values.
             * @return the next stage of the container registry FileTaskStep update.
             */
            Update withValuesPath(String path);
        }

        /** The stage of the container registry FileTaskStep update allowing to specify the overriding values. */
        interface OverridingValues {
            /**
             * The function that specifies the values that override the corresponding values specified under the
             * function withValuesPath().
             *
             * @param overridingValues a map which contains the values that will override the corresponding values
             *     specified under the function withValuesPath().
             * @return the next stage of the container registry FileTaskStep update.
             */
            Update withOverridingValues(Map<String, OverridingValue> overridingValues);

            /**
             * The function that specifies a single value that will override the corresponding value specified under the
             * function withValuesPath().
             *
             * @param name the name of the value to be overridden.
             * @param overridingValue the value of the value to be overridden.
             * @return the next stage of the container registry FileTaskStep update.
             */
            Update withOverridingValue(String name, OverridingValue overridingValue);
        }
    }
}
