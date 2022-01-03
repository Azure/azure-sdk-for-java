// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.containerregistry.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.model.Attachable;
import java.util.Map;

/** An immutable client-side representation of an Azure registry Encoded task run request. */
@Fluent()
public interface RegistryEncodedTaskRunRequest {
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

    /** Container interface for all the definitions related to a registry Encoded task run request. */
    interface Definition
        extends DefinitionStages.Blank,
            DefinitionStages.EncodedTaskContent,
            DefinitionStages.EncodedTaskRunRequestStepAttachable {
    }

    /** Grouping of registry encoded task run request definition stages. */
    interface DefinitionStages {
        /** The first stage of an encoded task run request definition. */
        interface Blank {
            /**
             * The function that begins the definition of the encoded task step in the task run request.
             *
             * @return the next stage of the container encoded task run request definition.
             */
            EncodedTaskContent defineEncodedTaskStep();
        }

        /**
         * The stage of the container encoded task run request definition that specifies the base64 encoded task
         * content.
         */
        interface EncodedTaskContent {
            /**
             * The function that specifies the base64 encoded task content.
             *
             * @param encodedTaskContent the base64 encoded task content.
             * @return the next stage of the container encoded task run request definition.
             */
            EncodedTaskRunRequestStepAttachable withBase64EncodedTaskContent(String encodedTaskContent);
        }

        /**
         * The stage of the definition which contains all the minimum required inputs for the resource to be attached,
         * but also allows for any other optional settings to be specified.
         */
        interface EncodedTaskRunRequestStepAttachable
            extends Attachable<RegistryTaskRun.DefinitionStages.RunRequestExecutableWithSourceLocation> {
            /**
             * The function that specifies the base64 encoded values content.
             *
             * @param encodedValueContent the base64 encoded values content.
             * @return the next stage of the container encoded task run request definition.
             */
            EncodedTaskRunRequestStepAttachable withBase64EncodedValueContent(String encodedValueContent);

            /**
             * The function that specifies the overriding values and what they will override.
             *
             * @param overridingValues map with key of the name of the value to be overridden and value OverridingValue
             *     specifying the content of the overriding value.
             * @return the next stage of the container encoded task run request definition.
             */
            EncodedTaskRunRequestStepAttachable withOverridingValues(Map<String, OverridingValue> overridingValues);

            /**
             * The function that specifies the overriding value and what it will override.
             *
             * @param name the name of the value to be overridden.
             * @param overridingValue the content of the overriding value.
             * @return the next stage of the container encoded task run request definition.
             */
            EncodedTaskRunRequestStepAttachable withOverridingValue(String name, OverridingValue overridingValue);
        }
    }
}
