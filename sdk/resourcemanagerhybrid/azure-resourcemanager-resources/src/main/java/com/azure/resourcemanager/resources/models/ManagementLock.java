// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.ResourceManager;
import com.azure.resourcemanager.resources.fluent.models.ManagementLockObjectInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasId;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasManager;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasName;
import com.azure.resourcemanager.resources.fluentcore.arm.models.Resource;
import com.azure.resourcemanager.resources.fluentcore.model.Appliable;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;
import com.azure.resourcemanager.resources.fluentcore.model.Indexable;
import com.azure.resourcemanager.resources.fluentcore.model.Refreshable;
import com.azure.resourcemanager.resources.fluentcore.model.Updatable;

import java.util.List;

/**
 * Management lock.
 */
@Fluent
public interface ManagementLock extends
    Indexable,
    Refreshable<ManagementLock>,
    Updatable<ManagementLock.Update>,
    HasInnerModel<ManagementLockObjectInner>,
    HasManager<ResourceManager>,
    HasId,
    HasName {

    /**
     * @return the lock level
     */
    LockLevel level();

    /**
     * @return the resource ID of the locked resource
     */
    String lockedResourceId();

    /**
     * @return any notes associated with the lock
     */
    String notes();

    /**
     * @return the owners of the lock
     */
    List<ManagementLockOwner> owners();

    /**
     * Container interface for all the definitions.
     */
    interface Definition extends
        DefinitionStages.Blank,
        DefinitionStages.WithLockedResource,
        DefinitionStages.WithLevel,
        DefinitionStages.WithCreate {
    }

    /**
     * Grouping of management lock definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of a management lock definition.
         */
        interface Blank
            extends WithLockedResource {
        }

        /**
         * The stage of a management lock definition allowing to specify notes for the lock.
         */
        interface WithLevel {
            /**
             * Specifies the lock level.
             * @param level the level of the lock
             * @return the next stage of the definition
             */
            WithCreate withLevel(LockLevel level);
        }

        /**
         * The stage of a management lock definition allowing to specify the level of the lock.
         */
        interface WithNotes {
            /**
             * Specifies the notes for the lock.
             *
             * @param notes the notes
             * @return the next stage of the definition
             */
            WithCreate withNotes(String notes);
        }

        /**
         * The stage of a management lock definition allowing to specify the resource to lock.
         */
        interface WithLockedResource {
            /**
             * Specifies the resource to lock.
             * @param resourceId the resource ID of the resource to lock
             * @return the next stage of the definition
             */
            WithLevel withLockedResource(String resourceId);

            /**
             * Specifies the resource to lock.
             * @param resource the resource to lock
             * @return the next stage of the definition
             */
            WithLevel withLockedResource(Resource resource);

            /**
             * Specifies the resource group to lock.
             * @param resourceGroupName the name of a resource group
             * @return the next stage of the definition
             */
            WithLevel withLockedResourceGroup(String resourceGroupName);

            /**
             * Specifies the resource group to lock.
             * @param resourceGroup a resource group
             * @return then next stage of the definition
             */
            WithLevel withLockedResourceGroup(ResourceGroup resourceGroup);
        }

        /**
         * The stage of the management lock definition which contains all the minimum required inputs for
         * the resource to be created but also allows
         * for any other optional settings to be specified.
         */
        interface WithCreate extends
            Creatable<ManagementLock>,
            DefinitionStages.WithNotes {
        }
    }

    /**
     * Container interface for all the updates.
     */
    interface Update extends
        Appliable<ManagementLock>,
        UpdateStages.WithLevel,
        UpdateStages.WithLockedResource {
    }

    /**
     * Grouping of management lock update stages.
     */
    interface UpdateStages {

        /**
         * The stage of a management lock definition allowing to specify the level of the lock.
         */
        interface WithNotes {
            /**
             * Specifies the notes for the lock.
             *
             * @param notes the notes
             * @return the next stage of the definition
             */
            Update withNotes(String notes);
        }

        /**
         * The stage of a management lock update allowing to specify the resource to lock.
         */
        interface WithLockedResource {
            /**
             * Specifies the resource to lock.
             * @param resourceId the resource ID of the resource to lock
             * @return the next stage of the update
             */
            Update withLockedResource(String resourceId);

            /**
             * Specifies the resource to lock.
             * @param resource the resource to lock
             * @return the next stage of the update
             */
            Update withLockedResource(Resource resource);

            /**
             * Specifies the resource group to lock.
             * @param resourceGroupName the name of a resource group
             * @return the next stage of the update
             */
            Update withLockedResourceGroup(String resourceGroupName);

            /**
             * Specifies the resource group to lock.
             * @param resourceGroup a resource group
             * @return then next stage of the update
             */
            Update withLockedResourceGroup(ResourceGroup resourceGroup);
        }

        /**
         * The stage of a management lock update allowing to modify the level of the lock.
         */
        interface WithLevel {
            /**
             * Specifies the lock level.
             * @param level the level of the lock
             * @return the next stage of the definition
             */
            Update withLevel(LockLevel level);
        }
    }
}
