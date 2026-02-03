// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.storage.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasManager;
import com.azure.resourcemanager.resources.fluentcore.model.Appliable;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;
import com.azure.resourcemanager.resources.fluentcore.model.Indexable;
import com.azure.resourcemanager.resources.fluentcore.model.Updatable;
import com.azure.resourcemanager.storage.StorageManager;
import com.azure.resourcemanager.storage.fluent.models.BlobContainerInner;
import java.time.OffsetDateTime;
import java.util.Map;

/** Type representing BlobContainer. */
@Fluent
public interface BlobContainer
    extends HasInnerModel<BlobContainerInner>, Indexable, Updatable<BlobContainer.Update>, HasManager<StorageManager> {
    /**
     * Gets the etag value.
     *
     * @return the etag value.
     */
    String etag();

    /**
     * Checks whether the container has immutability policy.
     *
     * @return the hasImmutabilityPolicy value.
     */
    Boolean hasImmutabilityPolicy();

    /**
     * Checks whether the container has legal hold.
     *
     * @return the hasLegalHold value.
     */
    Boolean hasLegalHold();

    /**
     * Gets container resource ID.
     *
     * @return the id value.
     */
    String id();

    /**
     * Gets the properties of the immutability policy.
     *
     * @return the immutabilityPolicy value.
     */
    ImmutabilityPolicyProperties immutabilityPolicy();

    /**
     * Gets the last modified time.
     *
     * @return the lastModifiedTime value.
     */
    OffsetDateTime lastModifiedTime();

    /**
     * Gets the lease duration.
     *
     * @return the leaseDuration value.
     */
    LeaseDuration leaseDuration();

    /**
     * Gets the lease state.
     *
     * @return the leaseState value.
     */
    LeaseState leaseState();

    /**
     * gets the lease status.
     *
     * @return the leaseStatus value.
     */
    LeaseStatus leaseStatus();

    /**
     * Gets the legal hold.
     *
     * @return the legalHold value.
     */
    LegalHoldProperties legalHold();

    /**
     * Gets the metadata.
     *
     * @return the metadata value.
     */
    Map<String, String> metadata();

    /**
     * Gets the name of the blob container.
     *
     * @return the name value.
     */
    String name();

    /**
     * Gets the public access.
     *
     * @return the publicAccess value.
     */
    PublicAccess publicAccess();

    /**
     * Gets the type of the blob container.
     *
     * @return the type value.
     */
    String type();

    /** The entirety of the BlobContainer definition. */
    interface Definition extends DefinitionStages.Blank, DefinitionStages.WithBlobService,
        DefinitionStages.WithPublicAccess, DefinitionStages.WithMetadata, DefinitionStages.WithCreate {
    }

    /** Grouping of BlobContainer definition stages. */
    interface DefinitionStages {
        /** The first stage of a BlobContainer definition. */
        interface Blank extends WithBlobService {
        }

        /** The stage of the blobcontainer definition allowing to specify BlobService. */
        interface WithBlobService {
            /**
             * Specifies resourceGroupName, accountName.
             *
             * @deprecated use {@link #withExistingStorageAccount(String, String)}
             *
             * @param resourceGroupName The name of the resource group within the user's subscription. The name is case
             *     insensitive
             * @param accountName The name of the storage account within the specified resource group. Storage account
             *     names must be between 3 and 24 characters in length and use numbers and lower-case letters only
             * @return the next definition stage
             */
            @Deprecated
            WithPublicAccess withExistingBlobService(String resourceGroupName, String accountName);

            /**
             * Specifies resourceGroupName, accountName.
             *
             * @param resourceGroupName The name of the resource group within the user's subscription. The name is case
             *     insensitive
             * @param accountName The name of the storage account within the specified resource group. Storage account
             *     names must be between 3 and 24 characters in length and use numbers and lower-case letters only
             * @return the next definition stage
             */
            WithPublicAccess withExistingStorageAccount(String resourceGroupName, String accountName);

            /**
             * Specifies resourceGroupName, accountName.
             *
             * @param storageAccount the storage account.
             * @return the next definition stage
             */
            WithPublicAccess withExistingStorageAccount(StorageAccount storageAccount);
        }

        /** The stage of the blobcontainer definition allowing to specify PublicAccess. */
        interface WithPublicAccess {
            /**
             * Specifies publicAccess.
             *
             * @param publicAccess Specifies whether data in the container may be accessed publicly and the level of
             *     access. Possible values include: 'Container', 'Blob', 'None'
             * @return the next definition stage
             */
            WithCreate withPublicAccess(PublicAccess publicAccess);
        }

        /** The stage of the blobcontainer definition allowing to specify Metadata. */
        interface WithMetadata {
            /**
             * Specifies metadata.
             *
             * @param metadata A name-value pair to associate with the container as metadata
             * @return the next definition stage
             */
            WithCreate withMetadata(Map<String, String> metadata);

            /**
             * Specifies a singluar instance of metadata.
             *
             * @param name A name to associate with the container as metadata
             * @param value A value to associate with the container as metadata
             * @return the next definition stage
             */
            WithCreate withMetadata(String name, String value);
        }

        /**
         * The stage of the definition which contains all the minimum required inputs for the resource to be created
         * (via {@link WithCreate#create()}), but also allows for any other optional settings to be specified.
         */
        interface WithCreate extends WithMetadata, Creatable<BlobContainer> {
        }
    }

    /** The template for a BlobContainer update operation, containing all the settings that can be modified. */
    interface Update extends Appliable<BlobContainer>, UpdateStages.WithPublicAccess, UpdateStages.WithMetadata {
    }

    /** Grouping of BlobContainer update stages. */
    interface UpdateStages {
        /** The stage of the blobcontainer update allowing to specify PublicAccess. */
        interface WithPublicAccess {
            /**
             * Specifies publicAccess.
             *
             * @param publicAccess Specifies whether data in the container may be accessed publicly and the level of
             *     access. Possible values include: 'Container', 'Blob', 'None'
             * @return the next update stage
             */
            Update withPublicAccess(PublicAccess publicAccess);
        }

        /** The stage of the blobcontainer update allowing to specify Metadata. */
        interface WithMetadata {
            /**
             * Specifies metadata.
             *
             * @param metadata A name-value pair to associate with the container as metadata
             * @return the next update stage
             */
            Update withMetadata(Map<String, String> metadata);

            /**
             * Specifies a singluar instance of metadata.
             *
             * @param name A name to associate with the container as metadata
             * @param value A value to associate with the container as metadata
             * @return the next definition stage
             */
            Update withMetadata(String name, String value);
        }
    }
}
