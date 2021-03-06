/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 *
 * Code generated by Microsoft (R) AutoRest Code Generator.
 */

package com.microsoft.azure.management.streamanalytics.v2020_03_01_preview;

import com.microsoft.azure.arm.model.HasInner;
import com.microsoft.azure.management.streamanalytics.v2020_03_01_preview.implementation.OutputInner;
import com.microsoft.azure.arm.model.Indexable;
import com.microsoft.azure.arm.model.Refreshable;
import com.microsoft.azure.arm.model.Updatable;
import com.microsoft.azure.arm.model.Appliable;
import com.microsoft.azure.arm.model.Creatable;
import com.microsoft.azure.arm.resources.models.HasManager;
import com.microsoft.azure.management.streamanalytics.v2020_03_01_preview.implementation.StreamAnalyticsManager;

/**
 * Type representing Output.
 */
public interface Output extends HasInner<OutputInner>, Indexable, Refreshable<Output>, Updatable<Output.Update>, HasManager<StreamAnalyticsManager> {
    /**
     * @return the datasource value.
     */
    OutputDataSource datasource();

    /**
     * @return the diagnostics value.
     */
    Diagnostics diagnostics();

    /**
     * @return the etag value.
     */
    String etag();

    /**
     * @return the id value.
     */
    String id();

    /**
     * @return the name value.
     */
    String name();

    /**
     * @return the serialization value.
     */
    Serialization serialization();

    /**
     * @return the sizeWindow value.
     */
    Double sizeWindow();

    /**
     * @return the timeWindow value.
     */
    String timeWindow();

    /**
     * @return the type value.
     */
    String type();

    /**
     * The entirety of the Output definition.
     */
    interface Definition extends DefinitionStages.Blank, DefinitionStages.WithStreamingjob, DefinitionStages.WithIfMatch, DefinitionStages.WithIfNoneMatch, DefinitionStages.WithCreate {
    }

    /**
     * Grouping of Output definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of a Output definition.
         */
        interface Blank extends WithStreamingjob {
        }

        /**
         * The stage of the output definition allowing to specify Streamingjob.
         */
        interface WithStreamingjob {
           /**
            * Specifies resourceGroupName, jobName.
            * @param resourceGroupName The name of the resource group. The name is case insensitive
            * @param jobName The name of the streaming job
            * @return the next definition stage
            */
            WithIfMatch withExistingStreamingjob(String resourceGroupName, String jobName);
        }

        /**
         * The stage of the output definition allowing to specify IfMatch.
         */
        interface WithIfMatch {
           /**
            * Specifies ifMatch.
            * @param ifMatch The ETag of the output. Omit this value to always overwrite the current output. Specify the last-seen ETag value to prevent accidentally overwriting concurrent changes
            * @return the next definition stage
            */
            WithIfNoneMatch withIfMatch(String ifMatch);
        }

        /**
         * The stage of the output definition allowing to specify IfNoneMatch.
         */
        interface WithIfNoneMatch {
           /**
            * Specifies ifNoneMatch.
            * @param ifNoneMatch Set to '*' to allow a new output to be created, but to prevent updating an existing output. Other values will result in a 412 Pre-condition Failed response
            * @return the next definition stage
            */
            WithCreate withIfNoneMatch(String ifNoneMatch);
        }

        /**
         * The stage of the output definition allowing to specify Datasource.
         */
        interface WithDatasource {
            /**
             * Specifies datasource.
             * @param datasource Describes the data source that output will be written to. Required on PUT (CreateOrReplace) requests
             * @return the next definition stage
             */
            WithCreate withDatasource(OutputDataSource datasource);
        }

        /**
         * The stage of the output definition allowing to specify Name.
         */
        interface WithName {
            /**
             * Specifies name.
             * @param name Resource name
             * @return the next definition stage
             */
            WithCreate withName(String name);
        }

        /**
         * The stage of the output definition allowing to specify Serialization.
         */
        interface WithSerialization {
            /**
             * Specifies serialization.
             * @param serialization Describes how data from an input is serialized or how data is serialized when written to an output. Required on PUT (CreateOrReplace) requests
             * @return the next definition stage
             */
            WithCreate withSerialization(Serialization serialization);
        }

        /**
         * The stage of the output definition allowing to specify SizeWindow.
         */
        interface WithSizeWindow {
            /**
             * Specifies sizeWindow.
             * @param sizeWindow the sizeWindow parameter value
             * @return the next definition stage
             */
            WithCreate withSizeWindow(Double sizeWindow);
        }

        /**
         * The stage of the output definition allowing to specify TimeWindow.
         */
        interface WithTimeWindow {
            /**
             * Specifies timeWindow.
             * @param timeWindow the timeWindow parameter value
             * @return the next definition stage
             */
            WithCreate withTimeWindow(String timeWindow);
        }

        /**
         * The stage of the definition which contains all the minimum required inputs for
         * the resource to be created (via {@link WithCreate#create()}), but also allows
         * for any other optional settings to be specified.
         */
        interface WithCreate extends Creatable<Output>, DefinitionStages.WithDatasource, DefinitionStages.WithName, DefinitionStages.WithSerialization, DefinitionStages.WithSizeWindow, DefinitionStages.WithTimeWindow {
        }
    }
    /**
     * The template for a Output update operation, containing all the settings that can be modified.
     */
    interface Update extends Appliable<Output>, UpdateStages.WithIfMatch, UpdateStages.WithDatasource, UpdateStages.WithName, UpdateStages.WithSerialization, UpdateStages.WithSizeWindow, UpdateStages.WithTimeWindow {
    }

    /**
     * Grouping of Output update stages.
     */
    interface UpdateStages {
        /**
         * The stage of the output update allowing to specify IfMatch.
         */
        interface WithIfMatch {
            /**
             * Specifies ifMatch.
             * @param ifMatch The ETag of the output. Omit this value to always overwrite the current output. Specify the last-seen ETag value to prevent accidentally overwriting concurrent changes
             * @return the next update stage
             */
            Update withIfMatch(String ifMatch);
        }

        /**
         * The stage of the output update allowing to specify Datasource.
         */
        interface WithDatasource {
            /**
             * Specifies datasource.
             * @param datasource Describes the data source that output will be written to. Required on PUT (CreateOrReplace) requests
             * @return the next update stage
             */
            Update withDatasource(OutputDataSource datasource);
        }

        /**
         * The stage of the output update allowing to specify Name.
         */
        interface WithName {
            /**
             * Specifies name.
             * @param name Resource name
             * @return the next update stage
             */
            Update withName(String name);
        }

        /**
         * The stage of the output update allowing to specify Serialization.
         */
        interface WithSerialization {
            /**
             * Specifies serialization.
             * @param serialization Describes how data from an input is serialized or how data is serialized when written to an output. Required on PUT (CreateOrReplace) requests
             * @return the next update stage
             */
            Update withSerialization(Serialization serialization);
        }

        /**
         * The stage of the output update allowing to specify SizeWindow.
         */
        interface WithSizeWindow {
            /**
             * Specifies sizeWindow.
             * @param sizeWindow the sizeWindow parameter value
             * @return the next update stage
             */
            Update withSizeWindow(Double sizeWindow);
        }

        /**
         * The stage of the output update allowing to specify TimeWindow.
         */
        interface WithTimeWindow {
            /**
             * Specifies timeWindow.
             * @param timeWindow the timeWindow parameter value
             * @return the next update stage
             */
            Update withTimeWindow(String timeWindow);
        }

    }
}
