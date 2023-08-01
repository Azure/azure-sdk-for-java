// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.containerregistry.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.model.Attachable;
import com.azure.resourcemanager.resources.fluentcore.model.Settable;
import java.util.List;
import java.util.Map;

/** An immutable client-side representation of an Azure RegistryEncodedTaskStep registry task. */
@Fluent()
public interface RegistryEncodedTaskStep extends RegistryTaskStep {
    /** @return the encoded task content of this encoded task step */
    String encodedTaskContent();

    /** @return the encoded values content of this encoded task step */
    String encodedValuesContent();

    /** @return the values of this encoded task step */
    List<SetValue> values();

    /** Container interface for all the definitions related to a RegistryEncodedTaskStep. */
    interface Definition
        extends RegistryEncodedTaskStep.DefinitionStages.Blank,
            RegistryEncodedTaskStep.DefinitionStages.EncodedTaskContent,
            RegistryEncodedTaskStep.DefinitionStages.EncodedTaskStepAttachable {
    }

    /** Container interface for all the updates related to a RegistryEncodedTaskStep. */
    interface Update
        extends RegistryEncodedTaskStep.UpdateStages.EncodedTaskContent,
            RegistryEncodedTaskStep.UpdateStages.ValuePath,
            RegistryEncodedTaskStep.UpdateStages.OverridingValues,
            Settable<RegistryTask.Update> {
    }

    /** Grouping of registry encoded task definition stages. */
    interface DefinitionStages {
        /** The first stage of a RegistryEncodedTaskStep definition. */
        interface Blank extends EncodedTaskContent {
        }

        /**
         * The stage of the container registry EncodedTaskStep definition allowing to specify the base64 encoded task
         * content.
         */
        interface EncodedTaskContent {
            /**
             * The function that specifies the base64 encoded task content.
             *
             * @param encodedTaskContent the base64 encoded task content.
             * @return the next stage of the container registry EncodedTaskStep definition.
             */
            EncodedTaskStepAttachable withBase64EncodedTaskContent(String encodedTaskContent);
        }

        /**
         * The stage of the definition which contains all the minimum required inputs for the resource to be attached,
         * but also allows for any other optional settings to be specified.
         */
        interface EncodedTaskStepAttachable extends Attachable<RegistryTask.DefinitionStages.SourceTriggerDefinition> {
            /**
             * The function that specifies the base64 encoded value content.
             *
             * @param encodedValueContent the base64 encoded value content.
             * @return the next stage of the container registry EncodedTaskStep definition.
             */
            EncodedTaskStepAttachable withBase64EncodedValueContent(String encodedValueContent);

            /**
             * The function that specifies the values that override the corresponding values specified under the
             * function withBase64EncodedValueContent().
             *
             * @param overridingValues a map which contains the values that will override the corresponding values
             *     specified under the function withBase64EncodedValueContent().
             * @return the next stage of the container registry EncodedTaskStep definition.
             */
            EncodedTaskStepAttachable withOverridingValues(Map<String, OverridingValue> overridingValues);

            /**
             * The function that specifies a single value that will override the corresponding value specified under the
             * function withBase64EncodedValueContent().
             *
             * @param name the name of the value to be overridden.
             * @param overridingValue the value of the value to be overridden.
             * @return the next stage of the container registry EncodedTaskStep definition.
             */
            EncodedTaskStepAttachable withOverridingValue(String name, OverridingValue overridingValue);
        }
    }

    /** Grouping of registry encoded task update stages. */
    interface UpdateStages {
        /** The stage of the container registry EncodedTaskStep update allowing to specify the task path. */
        interface EncodedTaskContent {
            /**
             * The function that specifies the path to the base64 encoded task content.
             *
             * @param encodedTaskContent the path to the base64 encoded task content.
             * @return the next stage of the container registry EncodedTaskStep update.
             */
            Update withBase64EncodedTaskContent(String encodedTaskContent);
        }

        /** The stage of the container registry EncodedTaskStep update allowing to specify the path to the values. */
        interface ValuePath {
            /**
             * The function that specifies the path to the base64 encoded value content.
             *
             * @param encodedValueContent the path to the base64 encoded value content.
             * @return the next stage of the container registry EncodedTaskStep update.
             */
            Update withBase64EncodedValueContent(String encodedValueContent);
        }

        /** The stage of the container registry EncodedTaskStep update allowing to specify the overriding values. */
        interface OverridingValues {
            /**
             * The function that specifies the values that override the corresponding values specified under the
             * function withBase64EncodedValueContent().
             *
             * @param overridingValues a map which contains the values that will override the corresponding values
             *     specified under the function withBase64EncodedValueContent().
             * @return the next stage of the container registry EncodedTaskStep update.
             */
            Update withOverridingValues(Map<String, OverridingValue> overridingValues);

            /**
             * The function that specifies a single value that will override the corresponding value specified under the
             * function withBase64EncodedValueContent().
             *
             * @param name the name of the value to be overridden.
             * @param overridingValue the value of the value to be overridden.
             * @return the next stage of the container registry EncodedTaskStep update.
             */
            Update withOverridingValue(String name, OverridingValue overridingValue);
        }
    }
}
