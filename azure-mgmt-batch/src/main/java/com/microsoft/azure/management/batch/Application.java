/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.batch;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.batch.implementation.ApplicationInner;
import com.microsoft.azure.management.batch.implementation.ApplicationPackageInner;
import com.microsoft.azure.management.resources.fluentcore.arm.models.ExternalChildResource;
import com.microsoft.azure.management.resources.fluentcore.model.Attachable;
import com.microsoft.azure.management.resources.fluentcore.model.Settable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;

import java.util.List;

/**
 * An immutable client-side representation of an Azure batch account application.
 */
@LangDefinition(ContainerName = "~/")
public interface Application extends
        ExternalChildResource<Application>,
        Wrapper<ApplicationInner> {

    /**
     * Get the displayName value.
     *
     * @return the displayName value
     */
    String displayName();

    /**
     * @return the list of application packages
     */
    List<ApplicationPackageInner> packages();

    /**
     * Get the allowUpdates value.
     *
     * @return the allowUpdates value
     */
    boolean allowUpdates();

    /**
     * @return the defaultVersion value
     */
    String defaultVersion();

    /**************************************************************
     * Fluent interfaces to provision an Application
     **************************************************************/

    /**
     * The entirety of a application definition as a part of parent definition.
     *
     * @param <ParentT> the return type of the final {@link Attachable#attach()}
     */
    @LangDefinition(ContainerName = "~/Application.Definition")
    interface Definition<ParentT> extends
            DefinitionStages.Blank<ParentT>,
            DefinitionStages.WithAttach<ParentT> {
    }

    /**
     * Grouping of all the storage account definition stages.
     */
    @LangDefinition(ContainerName = "~/Application.Definition", ContainerFileName = "IDefinition", IsContainerOnly = true)
    interface DefinitionStages {
        /**
         * The first stage of a batch account application definition.
         *
         * @param <ParentT> the return type of the final {@link WithAttach#attach()}
         */
        interface Blank<ParentT> extends WithAttach<ParentT> {
        }

        /** The final stage of the application definition.
         * <p>
         * At this stage, any remaining optional settings can be specified, or the application definition
         * can be attached to the parent batch account definition using {@link Application.DefinitionStages.WithAttach#attach()}.
         * @param <ParentT> the return type of {@link Application.DefinitionStages.WithAttach#attach()}
         */
        interface WithAttach<ParentT> extends
                Attachable.InDefinition<ParentT> {

            /**
             * Set the allowUpdates value.
             *
             * @param allowUpdates the allowUpdates value to set
             * @return parent batch account definition.
             */
            DefinitionStages.WithAttach<ParentT> withAllowUpdates(boolean allowUpdates);

            /**
             * Set the displayName value.
             *
             * @param displayName the displayName value to set
             * @return parent batch account definition.
             */
            DefinitionStages.WithAttach<ParentT> withDisplayName(String displayName);
        }
    }

    /**
     * The entirety of a virtual machine extension definition as a part of parent update.
     * @param <ParentT> the return type of the final {@link Attachable#attach()}
     */
    interface UpdateDefinition<ParentT> extends
            UpdateDefinitionStages.Blank<ParentT>,
            UpdateDefinitionStages.WithAttach<ParentT> {
    }

    /**
     * Grouping of application definition stages as part of parent batch account update.
     */
    interface UpdateDefinitionStages {
        /**
         * The first stage of a virtual machine extension definition.
         *
         * @param <ParentT> the return type of the final {@link WithAttach#attach()}
         */
        interface Blank<ParentT>
                extends WithAttach<ParentT> {
        }

        /** The final stage of the application definition.
         * <p>
         * At this stage, any remaining optional settings can be specified, or the application definition
         * can be attached to the parent batch account definition using {@link Application.DefinitionStages.WithAttach#attach()}.
         * @param <ParentT> the return type of {@link Application.DefinitionStages.WithAttach#attach()}
         */
        interface WithAttach<ParentT> extends
            Attachable.InUpdate<ParentT> {

            /**
             * Set the allowUpdates value.
             *
             * @param allowUpdates the allowUpdates value to set
             * @return parent batch account update definition.
             */
            UpdateDefinitionStages.WithAttach<ParentT> withAllowUpdates(boolean allowUpdates);

            /**
             * Specify the display name.
             *
             * @param displayName display name for the application.
             * @return parent batch account update definition.
             */
            UpdateDefinitionStages.WithAttach<ParentT> withDisplayName(String displayName);
        }
    }

    /**
     * Grouping of virtual machine extension update stages.
     */
    interface UpdateStages {
        /**
         * The stage of the application update allowing to enable or disable auto upgrade of the
         * application.
         */
        interface WithOptionalProperties {
            /**
             * Set the allowUpdates value.
             *
             * @param allowUpdates the allowUpdates value to set
             * @return the next stage of the update
             */
            Update withAllowUpdates(boolean allowUpdates);

            /**
             * Set the displayName value.
             *
             * @param displayName the displayName value to set
             * @return the next stage of the update
             */
            Update withDisplayName(String displayName);
        }

    }
    /**
     * The entirety of application update as a part of parent batch account update.
     */
    interface Update extends
            Settable<BatchAccount.Update>,
            UpdateStages.WithOptionalProperties {
    }
}

