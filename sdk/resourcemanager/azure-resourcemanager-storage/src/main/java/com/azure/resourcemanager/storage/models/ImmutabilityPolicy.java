// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.storage.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasManager;
import com.azure.resourcemanager.resources.fluentcore.model.Appliable;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;
import com.azure.resourcemanager.resources.fluentcore.model.Indexable;
import com.azure.resourcemanager.resources.fluentcore.model.Refreshable;
import com.azure.resourcemanager.resources.fluentcore.model.Updatable;
import com.azure.resourcemanager.storage.StorageManager;
import com.azure.resourcemanager.storage.fluent.models.ImmutabilityPolicyInner;

/** Type representing ImmutabilityPolicy. */
@Fluent
public interface ImmutabilityPolicy
    extends HasInnerModel<ImmutabilityPolicyInner>,
        Indexable,
        Refreshable<ImmutabilityPolicy>,
        Updatable<ImmutabilityPolicy.Update>,
        HasManager<StorageManager> {
    /** @return the etag value. */
    String etag();

    /** @return the id value. */
    String id();

    /** @return the immutabilityPeriodSinceCreationInDays value. */
    int immutabilityPeriodSinceCreationInDays();

    /** @return the name value. */
    String name();

    /** @return the state value. */
    ImmutabilityPolicyState state();

    /** @return the type value. */
    String type();

    /** The entirety of the ImmutabilityPolicy definition. */
    interface Definition
        extends DefinitionStages.Blank,
            DefinitionStages.WithContainer,
            DefinitionStages.WithImmutabilityPeriodSinceCreationInDays,
            DefinitionStages.WithCreate {
    }

    /** Grouping of ImmutabilityPolicy definition stages. */
    interface DefinitionStages {
        /** The first stage of a ImmutabilityPolicy definition. */
        interface Blank extends WithContainer {
        }

        /** The stage of the immutabilitypolicy definition allowing to specify Container. */
        interface WithContainer {
            /**
             * Specifies resourceGroupName, accountName, containerName.
             *
             * @param resourceGroupName The name of the resource group within the user's subscription. The name is case
             *     insensitive
             * @param accountName The name of the storage account within the specified resource group. Storage account
             *     names must be between 3 and 24 characters in length and use numbers and lower-case letters only
             * @param containerName The name of the blob container within the specified storage account. Blob container
             *     names must be between 3 and 63 characters in length and use numbers, lower-case letters and dash (-)
             *     only. Every dash (-) character must be immediately preceded and followed by a letter or number
             * @return the next definition stage
             */
            WithImmutabilityPeriodSinceCreationInDays withExistingContainer(
                String resourceGroupName, String accountName, String containerName);
        }

        /** The stage of the immutabilitypolicy definition allowing to specify ImmutabilityPeriodSinceCreationInDays. */
        interface WithImmutabilityPeriodSinceCreationInDays {
            /**
             * Specifies immutabilityPeriodSinceCreationInDays.
             *
             * @param immutabilityPeriodSinceCreationInDays The immutability period for the blobs in the container since
             *     the policy creation, in days
             * @return the next definition stage
             */
            WithCreate withImmutabilityPeriodSinceCreationInDays(int immutabilityPeriodSinceCreationInDays);
        }

        /** The stage of the immutabilitypolicy definition allowing to specify If-Match header. */
        interface WithETagCheck {
            /**
             * Specifies If-Match header.
             *
             * @return the next definition stage
             */
            WithCreate withETagCheck();
        }

        /**
         * The stage of the definition which contains all the minimum required inputs for the resource to be created
         * (via {@link WithCreate#create()}), but also allows for any other optional settings to be specified.
         */
        interface WithCreate extends WithETagCheck, Creatable<ImmutabilityPolicy> {
        }
    }
    /** The template for a ImmutabilityPolicy update operation, containing all the settings that can be modified. */
    interface Update
        extends Appliable<ImmutabilityPolicy>,
            UpdateStages.WithETagCheck,
            UpdateStages.WithImmutabilityPeriodSinceCreationInDays {
    }

    /** Grouping of ImmutabilityPolicy update stages. */
    interface UpdateStages {
        /** The stage of the immutabilitypolicy update allowing to specify If-Match header. */
        interface WithETagCheck {
            /**
             * Specifies If-Match header.
             *
             * @return the next update stage
             */
            Update withETagCheck();

            /**
             * Specifies If-Match header.
             *
             * @param eTagValue The entity state (ETag) version of the immutability policy to update. A value of "*" can
             *     be used to apply the operation only if the immutability policy already exists. If omitted, this
             *     operation will always be applied
             * @return the next update stage
             */
            Update withETagCheck(String eTagValue);
        }

        /** The stage of the immutabilitypolicy update allowing to specify ImmutabilityPeriodSinceCreationInDays. */
        interface WithImmutabilityPeriodSinceCreationInDays {
            /**
             * Specifies immutabilityPeriodSinceCreationInDays.
             *
             * @param immutabilityPeriodSinceCreationInDays The immutability period for the blobs in the container since
             *     the policy creation, in days
             * @return the next update stage
             */
            Update withImmutabilityPeriodSinceCreationInDays(int immutabilityPeriodSinceCreationInDays);
        }
    }
}
