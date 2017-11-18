/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.batch;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.batch.implementation.ApplicationInner;
import com.microsoft.azure.management.resources.fluentcore.arm.models.ExternalChildResource;
import com.microsoft.azure.management.resources.fluentcore.model.Attachable;
import com.microsoft.azure.management.resources.fluentcore.model.Settable;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;

import java.util.Map;

/**
 * An immutable client-side representation of an Azure Batch account application.
 */
@Fluent
// TODO: This should be renamed as BatchApplication in 2.0
public interface Application extends
        ExternalChildResource<Application, BatchAccount>,
        HasInner<ApplicationInner> {

    /**
     * @return the display name of the application
     */
    String displayName();

    /**
     * @return application packages
     */
    Map<String, ApplicationPackage> applicationPackages();

    /**
     * @return true if automatic updates are allowed, otherwise false
     */
    boolean updatesAllowed();

    /**
     * @return the default version for the application.
     */
    String defaultVersion();

    /**
     * The entirety of a Batch application definition as a part of a Batch account definition.
     *
     * @param <ParentT> the stage of the parent Batch account definition to return to after attaching this definition
     */
    interface Definition<ParentT> extends
            DefinitionStages.Blank<ParentT>,
            DefinitionStages.WithAttach<ParentT> {
    }

    /**
     * Grouping of all the application package definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of a batch application definition.
         *
         * @param <ParentT> the stage of the parent Batch account definition to return to after attaching this definition
         */
        interface Blank<ParentT> extends WithAttach<ParentT> {
        }

        /**
         * The stage of a Batch application definition that allows the creation of an application package.
         *
         * @param <ParentT> the stage of the parent Batch account definition to return to after attaching this definition
         */
        interface WithApplicationPackage<ParentT> {
            /**
             * The first stage of a new application package definition in a Batch account application.
             *
             * @param applicationPackageName the version of the application
             * @return the next stage of the definition
             */
            DefinitionStages.WithAttach<ParentT> defineNewApplicationPackage(String applicationPackageName);
        }

        /**
         * The final stage of the application definition.
         * <p>
         * At this stage, any remaining optional settings can be specified, or the application definition
         * can be attached to the parent batch account definition.
         * @param <ParentT> the stage of the parent Batch account definition to return to after attaching this definition
         */
        interface WithAttach<ParentT> extends
                Attachable.InDefinition<ParentT>,
                WithApplicationPackage<ParentT> {
            /**
             * The stage of a Batch application definition allowing automatic application updates.
             *
             * @param allowUpdates true to allow the automatic updates of application, otherwise false
             * @return the next stage of the definition
             */
            DefinitionStages.WithAttach<ParentT> withAllowUpdates(boolean allowUpdates);

            /**
             * Specifies a display name for the Batch application.
             *
             * @param displayName a display name
             * @return the next stage of the definition
             */
            DefinitionStages.WithAttach<ParentT> withDisplayName(String displayName);
        }
    }

    /**
     * The entirety of a Batch application definition as a part of parent update.
     * @param <ParentT> the stage of the parent Batch account update to return to after attaching this definition
     */
    interface UpdateDefinition<ParentT> extends
            UpdateDefinitionStages.Blank<ParentT>,
            UpdateDefinitionStages.WithAttach<ParentT> {
    }

    /**
     * Grouping of application definition stages as part of a Batch account update.
     */
    interface UpdateDefinitionStages {
        /**
         * The first stage of a Batch application definition.
         *
         * @param <ParentT> the stage of the parent Batch account definition to return to after attaching this definition
         */
        interface Blank<ParentT>
                extends WithAttach<ParentT> {
        }

        /**
         * The stage of a Batch application definition allowing the creation of an application package.
         *
         * @param <ParentT> the stage of the parent Batch account definition to return to after attaching this definition
         */
        interface WithApplicationPackage<ParentT> {
            /**
             * First stage to create new application package in Batch account application.
             *
             * @param version the version of the application
             * @return next stage to create the application.
             */
            UpdateDefinitionStages.WithAttach<ParentT> defineNewApplicationPackage(String version);
        }

        /** The final stage of a Batch application definition.
         * <p>
         * At this stage, any remaining optional settings can be specified, or the application definition
         * can be attached to the parent batch Account update.
         * @param <ParentT> the stage of the parent Batch account update to return to after attaching this definition
         */
        interface WithAttach<ParentT> extends
            Attachable.InUpdate<ParentT>,
            WithApplicationPackage<ParentT> {

            /**
             * Allows automatic application updates.
             *
             * @param allowUpdates true to allow automatic updates of a Batch application, otherwise false
             * @return the next stage of the definition
             */
            UpdateDefinitionStages.WithAttach<ParentT> withAllowUpdates(boolean allowUpdates);

            /**
             * Specifies the display name for the Batch application.
             *
             * @param displayName a display name for the application.
             * @return the next stage of the definition
             */
            UpdateDefinitionStages.WithAttach<ParentT> withDisplayName(String displayName);
        }
    }

    /**
     * Grouping of Batch application update stages.
     */
    interface UpdateStages {

        /**
         * The stage of a Batch application update allowing the creation of an application package.
         */
        interface WithApplicationPackage {
            /**
             * First stage to create new application package in Batch account application.
             *
             * @param version the version of the application
             * @return next stage to create the application.
             */
            Update defineNewApplicationPackage(String version);

            /**
             * Deletes specified application package from the application.
             *
             * @param version the reference version of the application to be removed
             * @return the stage representing updatable batch account definition.
             */
            Update withoutApplicationPackage(String version);
        }
        /**
         * The stage of an application update allowing to enable or disable auto upgrade of the
         * application.
         */
        interface WithOptionalProperties {
            /**
             * Allows automatic application updates.
             *
             * @param allowUpdates true to allow the automatic updates of the application, otherwise false
             * @return the next stage of the update
             */
            Update withAllowUpdates(boolean allowUpdates);

            /**
             * Specifies the display name for the application.
             *
             * @param displayName a display name
             * @return the next stage of the update
             */
            Update withDisplayName(String displayName);
        }

    }
    /**
     * The entirety of a Batch application update as a part of a Batch account update.
     */
    interface Update extends
            Settable<BatchAccount.Update>,
            UpdateStages.WithOptionalProperties,
            UpdateStages.WithApplicationPackage {
    }
}

