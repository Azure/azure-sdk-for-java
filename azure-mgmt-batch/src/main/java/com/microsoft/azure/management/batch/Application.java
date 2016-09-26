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
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;

import java.util.Map;

/**
 * An immutable client-side representation of an Azure batch account application.
 */
@Fluent
public interface Application extends
        ExternalChildResource<Application, BatchAccount>,
        Wrapper<ApplicationInner> {

    /**
     * @return the display name for application
     */
    String displayName();

    /**
     * @return the list of application packages
     */
    Map<String, ApplicationPackage> applicationPackages();

    /**
     * @return true if automatic updates are allowed, otherwise false
     */
    boolean updatesAllowed();

    /**
     * @return the default version for application.
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
    interface Definition<ParentT> extends
            DefinitionStages.Blank<ParentT>,
            DefinitionStages.WithAttach<ParentT> {
    }

    /**
     * Grouping of all the storage account definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of a batch account application definition.
         *
         * @param <ParentT> the return type of the final {@link WithAttach#attach()}
         */
        interface Blank<ParentT> extends WithAttach<ParentT> {
        }

        /**
         * A application definition to allow creation of application package.
         *
         * @param <ParentT> the return type of the final {@link WithAttach#attach()}
         */
        interface WithApplicationPackage<ParentT> {
            /**
             * First stage to create new application package in Batch account application.
             *
             * @param applicationPackageName the version of the application
             * @return next stage to create the application.
             */
            DefinitionStages.WithAttach<ParentT> defineNewApplicationPackage(String applicationPackageName);
        }

        /**
         * The final stage of the application definition.
         * <p>
         * At this stage, any remaining optional settings can be specified, or the application definition
         * can be attached to the parent batch account definition using {@link Application.DefinitionStages.WithAttach#attach()}.
         * @param <ParentT> the return type of {@link Application.DefinitionStages.WithAttach#attach()}
         */
        interface WithAttach<ParentT> extends
                Attachable.InDefinition<ParentT>,
                WithApplicationPackage<ParentT> {
            /**
             * Allow automatic application updates.
             *
             * @param allowUpdates true to allow the automatic updates of application, otherwise false
             * @return parent batch account definition.
             */
            DefinitionStages.WithAttach<ParentT> withAllowUpdates(boolean allowUpdates);

            /**
             * Specifies the display name for the application.
             *
             * @param displayName the displayName value to set
             * @return parent batch account definition.
             */
            DefinitionStages.WithAttach<ParentT> withDisplayName(String displayName);
        }
    }

    /**
     * The entirety of a application definition as a part of parent update.
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
         * The first stage of a application definition.
         *
         * @param <ParentT> the return type of the final {@link WithAttach#attach()}
         */
        interface Blank<ParentT>
                extends WithAttach<ParentT> {
        }

        /**
         * A application definition to allow creation of application package.
         *
         * @param <ParentT> the return type of the final {@link DefinitionStages.WithAttach#attach()}
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

        /** The final stage of the application definition.
         * <p>
         * At this stage, any remaining optional settings can be specified, or the application definition
         * can be attached to the parent batch account definition using {@link Application.DefinitionStages.WithAttach#attach()}.
         * @param <ParentT> the return type of {@link Application.DefinitionStages.WithAttach#attach()}
         */
        interface WithAttach<ParentT> extends
            Attachable.InUpdate<ParentT>,
            WithApplicationPackage<ParentT> {

            /**
             * Allow automatic application updates.
             *
             * @param allowUpdates true to allow the automatic updates of application, otherwise false
             * @return parent batch account update definition.
             */
            UpdateDefinitionStages.WithAttach<ParentT> withAllowUpdates(boolean allowUpdates);

            /**
             * Specifies the display name for the application.
             *
             * @param displayName display name for the application.
             * @return parent batch account update definition.
             */
            UpdateDefinitionStages.WithAttach<ParentT> withDisplayName(String displayName);
        }
    }

    /**
     * Grouping of application update stages.
     */
    interface UpdateStages {

        /**
         * A application definition to allow creation of application package.
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
         * The stage of the application update allowing to enable or disable auto upgrade of the
         * application.
         */
        interface WithOptionalProperties {
            /**
             * Allow automatic application updates.
             *
             * @param allowUpdates true to allow the automatic updates of application, otherwise false
             * @return the next stage of the update
             */
            Update withAllowUpdates(boolean allowUpdates);

            /**
             * Specifies the display name for the application.
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
            UpdateStages.WithOptionalProperties,
            UpdateStages.WithApplicationPackage {
    }
}

