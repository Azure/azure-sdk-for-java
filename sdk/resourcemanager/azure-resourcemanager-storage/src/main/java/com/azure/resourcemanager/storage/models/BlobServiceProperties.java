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
import com.azure.resourcemanager.storage.fluent.models.BlobServicePropertiesInner;
import java.util.List;

/** Type representing BlobServiceProperties. */
@Fluent
public interface BlobServiceProperties
    extends HasInnerModel<BlobServicePropertiesInner>,
        Indexable,
        Refreshable<BlobServiceProperties>,
        Updatable<BlobServiceProperties.Update>,
        HasManager<StorageManager> {
    /** @return the cors value. */
    CorsRules cors();

    /** @return the defaultServiceVersion value. */
    String defaultServiceVersion();

    /** @return blob soft delete policy used to restore a blob, snapshot, or version that has been deleted. */
    DeleteRetentionPolicy deleteRetentionPolicy();

    /** @return container soft delete policy used to restore a container that has been deleted. */
    DeleteRetentionPolicy containerDeleteRetentionPolicy();

    /** @return the id value. */
    String id();

    /** @return the name value. */
    String name();

    /** @return the type value. */
    String type();

    /** @return whether blob versioning is enabled */
    Boolean isBlobVersioningEnabled();

    /** @return whether last access time tracking policy is enabled */
    boolean isLastAccessTimeTrackingPolicyEnabled();

    /** @return the last access time tracking policy */
    LastAccessTimeTrackingPolicy lastAccessTimeTrackingPolicy();

    /** The entirety of the BlobServiceProperties definition. */
    interface Definition
        extends DefinitionStages.Blank, DefinitionStages.WithStorageAccount, DefinitionStages.WithCreate {
    }

    /** Grouping of BlobServiceProperties definition stages. */
    interface DefinitionStages {
        /** The first stage of a BlobServiceProperties definition. */
        interface Blank extends WithStorageAccount {
        }

        /** The stage of the blobserviceproperties definition allowing to specify StorageAccount. */
        interface WithStorageAccount {
            /**
             * Specifies resourceGroupName, accountName.
             *
             * @param resourceGroupName The name of the resource group within the user's subscription. The name is case
             *     insensitive
             * @param accountName The name of the storage account within the specified resource group. Storage account
             *     names must be between 3 and 24 characters in length and use numbers and lower-case letters only
             * @return the next definition stage
             */
            WithCreate withExistingStorageAccount(String resourceGroupName, String accountName);
        }

        /** The stage of the blobserviceproperties definition allowing to specify Cors. */
        interface WithCors {
            /**
             * Specifies all of the CORS rules.
             *
             * @param corsRules Specifies CORS rules for the Blob service. You can include up to five CorsRule elements
             *     in the request. If no CorsRule elements are included in the request body, all CORS rules will be
             *     deleted, and CORS will be disabled for the Blob service
             * @return the next definition stage
             */
            WithCreate withCORSRules(List<CorsRule> corsRules);

            /**
             * Specifies a single CORS rule.
             *
             * @param corsRule a single CORS rule
             * @return the next definition stage
             */
            WithCreate withCORSRule(CorsRule corsRule);
        }

        /** The stage of the blobserviceproperties definition allowing to specify DefaultServiceVersion. */
        interface WithDefaultServiceVersion {
            /**
             * Specifies defaultServiceVersion.
             *
             * @param defaultServiceVersion DefaultServiceVersion indicates the default version to use for requests to
             *     the Blob service if an incoming request’s version is not specified. Possible values include version
             *     2008-10-27 and all more recent versions
             * @return the next definition stage
             */
            WithCreate withDefaultServiceVersion(String defaultServiceVersion);
        }

        /** The stage of the blobserviceproperties definition allowing to specify DeleteRetentionPolicy. */
        interface WithDeleteRetentionPolicy {
            /**
             * Specifies deleteRetentionPolicy.
             * <p>For optimal protection for your blob data, Microsoft recommends enabling all of the following
             *    data protection features:</p>
             *    <p>- containerDeleteRetentionPolicy (Container soft delete)</p>
             *    <p>- Blob versioning</p>
             *    <p>- deleteRetentionPolicy (Blob soft delete)</p>
             *
             * @param deleteRetentionPolicy The blob service properties for soft delete
             * @return the next definition stage
             */
            WithCreate withDeleteRetentionPolicy(DeleteRetentionPolicy deleteRetentionPolicy);

            /**
             * Specifies that the delete retention policy is enabled for soft delete.
             * <p>For optimal protection for your blob data, Microsoft recommends enabling all of the following
             *    data protection features:</p>
             *    <p>- containerDeleteRetentionPolicy (Container soft delete)</p>
             *    <p>- Blob versioning</p>
             *    <p>- deleteRetentionPolicy (Blob soft delete)</p>
             *
             * @param numDaysEnabled number of days after soft delete that the blob service properties will actually be
             *     deleted
             * @return the next definition stage
             */
            WithCreate withDeleteRetentionPolicyEnabled(int numDaysEnabled);

            /**
             * Specifies that the delete retention policy is disabled.
             * <p>For optimal protection for your blob data, Microsoft recommends enabling all of the following
             *    data protection features:</p>
             *    <p>- containerDeleteRetentionPolicy (Container soft delete)</p>
             *    <p>- Blob versioning</p>
             *    <p>- deleteRetentionPolicy (Blob soft delete)</p>
             *
             * @return the next definition stage
             */
            WithCreate withDeleteRetentionPolicyDisabled();
        }

        /** The stage of the blobserviceproperties definition allowing to specify containerDeleteRetentionPolicy. */
        interface WithContainerDeleteRetentionPolicy {
            /**
             * Specifies containerDeleteRetentionPolicy.
             * <p>For optimal protection for your blob data, Microsoft recommends enabling all of the following
             *    data protection features:</p>
             *    <p>- containerDeleteRetentionPolicy (Container soft delete)</p>
             *    <p>- Blob versioning</p>
             *    <p>- deleteRetentionPolicy (Blob soft delete)</p>
             *
             * @param deleteRetentionPolicy The blob service properties for container soft delete
             * @return the next definition stage
             */
            WithCreate withContainerDeleteRetentionPolicy(DeleteRetentionPolicy deleteRetentionPolicy);

            /**
             * Specifies that the delete retention policy is enabled for container soft delete.
             * <p>For optimal protection for your blob data, Microsoft recommends enabling all of the following
             *    data protection features:</p>
             *    <p>- containerDeleteRetentionPolicy (Container soft delete)</p>
             *    <p>- Blob versioning</p>
             *    <p>- deleteRetentionPolicy (Blob soft delete)</p>
             *
             * @param numDaysEnabled number of days after container soft delete that the blob service properties will
             *     actually be deleted
             * @return the next definition stage
             */
            WithCreate withContainerDeleteRetentionPolicyEnabled(int numDaysEnabled);
        }

        /** The stage of the blobserviceproperties definition allowing to enable/disable blob versioning. */
        interface WithBlobVersioning {
            /**
             * Enables blob versioning.
             * <p>When blob versioning is enabled, you can access earlier versions of a blob to recover your data
             * if it is modified or deleted.</p>
             * <p>For optimal protection for your blob data, Microsoft recommends enabling all of the following
             *    data protection features:</p>
             *    <p>- containerDeleteRetentionPolicy (Container soft delete)</p>
             *    <p>- Blob versioning</p>
             *    <p>- deleteRetentionPolicy (Blob soft delete)</p>
             *
             * @return the next definition stage
             */
            WithCreate withBlobVersioningEnabled();
        }

        /** The stage of the blobserviceproperties definition allowing to specify last access time tracking policy. */
        interface WithLastAccessTimeTrackingPolicy {
            /**
             * Enables last access time tracking policy with default settings.
             * Last access time tracking integrates with lifecycle management to allow the automatic tiering and deletion
             * of data based on when individual blobs are last accessed.
             * It can also be used without lifecycle management by any solution that needs to understand
             * when individual blobs are last read and then take action.
             *
             * @return the next definition stage
             */
            WithCreate withLastAccessTimeTrackingPolicyEnabled();

            /**
             * Specify last access time tracking policy.
             * Last access time tracking integrates with lifecycle management to allow the automatic tiering and deletion
             * of data based on when individual blobs are last accessed.
             * It can also be used without lifecycle management by any solution that needs to understand
             * when individual blobs are last read and then take action.
             *
             * @param policy the last access time tracking policy
             * @return the next definition stage
             */
            WithCreate withLastAccessTimeTrackingPolicy(LastAccessTimeTrackingPolicy policy);
        }

        /**
         * The stage of the definition which contains all the minimum required inputs for the resource to be created
         * (via {@link WithCreate#create()}), but also allows for any other optional settings to be specified.
         */
        interface WithCreate
            extends Creatable<BlobServiceProperties>,
                DefinitionStages.WithCors,
                DefinitionStages.WithDefaultServiceVersion,
                DefinitionStages.WithDeleteRetentionPolicy,
                DefinitionStages.WithBlobVersioning,
                DefinitionStages.WithContainerDeleteRetentionPolicy,
                DefinitionStages.WithLastAccessTimeTrackingPolicy {
        }
    }
    /** The template for a BlobServiceProperties update operation, containing all the settings that can be modified. */
    interface Update
        extends Appliable<BlobServiceProperties>,
            UpdateStages.WithCors,
            UpdateStages.WithDefaultServiceVersion,
            UpdateStages.WithDeleteRetentionPolicy,
            UpdateStages.WithBlobVersioning,
            UpdateStages.WithContainerDeleteRetentionPolicy,
            UpdateStages.WithLastAccessTimeTrackingPolicy {
    }

    /** Grouping of BlobServiceProperties update stages. */
    interface UpdateStages {
        /** The stage of the blobserviceproperties update allowing to specify Cors. */
        interface WithCors {
            /**
             * Specifies all of the CORS rules.
             *
             * @param corsRules Specifies CORS rules for the Blob service. You can include up to five CorsRule elements
             *     in the request. If no CorsRule elements are included in the request body, all CORS rules will be
             *     deleted, and CORS will be disabled for the Blob service
             * @return the next update stage
             */
            Update withCORSRules(List<CorsRule> corsRules);

            /**
             * Specifies a single CORS rule.
             *
             * @param corsRule a single CORS rule
             * @return the next update stage
             */
            Update withCORSRule(CorsRule corsRule);
        }

        /** The stage of the blobserviceproperties update allowing to specify DefaultServiceVersion. */
        interface WithDefaultServiceVersion {
            /**
             * Specifies defaultServiceVersion.
             *
             * @param defaultServiceVersion DefaultServiceVersion indicates the default version to use for requests to
             *     the Blob service if an incoming request’s version is not specified. Possible values include version
             *     2008-10-27 and all more recent versions
             * @return the next update stage
             */
            Update withDefaultServiceVersion(String defaultServiceVersion);
        }

        /** The stage of the blobserviceproperties update allowing to specify DeleteRetentionPolicy. */
        interface WithDeleteRetentionPolicy {
            /**
             * Specifies deleteRetentionPolicy.
             * <p>For optimal protection for your blob data, Microsoft recommends enabling all of the following
             *    data protection features:</p>
             *    <p>- containerDeleteRetentionPolicy (Container soft delete)</p>
             *    <p>- Blob versioning</p>
             *    <p>- deleteRetentionPolicy (Blob soft delete)</p>
             *
             * @param deleteRetentionPolicy The blob service properties for soft delete
             * @return the next update stage
             */
            Update withDeleteRetentionPolicy(DeleteRetentionPolicy deleteRetentionPolicy);

            /**
             * Specifies that the delete retention policy is enabled for soft delete.
             * <p>For optimal protection for your blob data, Microsoft recommends enabling all of the following
             *    data protection features:</p>
             *    <p>- containerDeleteRetentionPolicy (Container soft delete)</p>
             *    <p>- Blob versioning</p>
             *    <p>- deleteRetentionPolicy (Blob soft delete)</p>
             *
             * @param numDaysEnabled number of days after soft delete that the blob service properties will actually be
             *     deleted
             * @return the next update stage
             */
            Update withDeleteRetentionPolicyEnabled(int numDaysEnabled);

            /**
             * Specifies that the delete retention policy is disabled.
             * <p>For optimal protection for your blob data, Microsoft recommends enabling all of the following
             *    data protection features:</p>
             *    <p>- containerDeleteRetentionPolicy (Container soft delete)</p>
             *    <p>- Blob versioning</p>
             *    <p>- deleteRetentionPolicy (Blob soft delete)</p>
             *
             * @return the next update stage
             */
            Update withDeleteRetentionPolicyDisabled();
        }

        /** The stage of the blobserviceproperties update allowing to specify containerDeleteRetentionPolicy. */
        interface WithContainerDeleteRetentionPolicy {
            /**
             * Specifies containerDeleteRetentionPolicy.
             * <p>For optimal protection for your blob data, Microsoft recommends enabling all of the following
             *    data protection features:</p>
             *    <p>- containerDeleteRetentionPolicy (Container soft delete)</p>
             *    <p>- Blob versioning</p>
             *    <p>- deleteRetentionPolicy (Blob soft delete)</p>
             *
             * @param deleteRetentionPolicy The blob service properties for container soft delete
             * @return the next update stage
             */
            Update withContainerDeleteRetentionPolicy(DeleteRetentionPolicy deleteRetentionPolicy);

            /**
             * Specifies that the delete retention policy is enabled for container soft delete.
             * <p>For optimal protection for your blob data, Microsoft recommends enabling all of the following
             *    data protection features:</p>
             *    <p>- containerDeleteRetentionPolicy (Container soft delete)</p>
             *    <p>- Blob versioning</p>
             *    <p>- deleteRetentionPolicy (Blob soft delete)</p>
             *
             * @param numDaysEnabled number of days after container soft delete that the blob service properties will
             *     actually be deleted
             * @return the next update stage
             */
            Update withContainerDeleteRetentionPolicyEnabled(int numDaysEnabled);

            /**
             * Specifies that the container delete retention policy is disabled.
             * <p>For optimal protection for your blob data, Microsoft recommends enabling all of the following
             *    data protection features:</p>
             *    <p>- containerDeleteRetentionPolicy (Container soft delete)</p>
             *    <p>- Blob versioning</p>
             *    <p>- deleteRetentionPolicy (Blob soft delete)</p>
             *
             * @return the next update stage
             */
            Update withContainerDeleteRetentionPolicyDisabled();
        }

        /** The stage of the blobserviceproperties update allowing to enable/disable blob versioning. */
        interface WithBlobVersioning {
            /**
             * Enables blob versioning.
             * <p>When blob versioning is enabled, you can access earlier versions of a blob to recover your data
             * if it is modified or deleted.</p>
             * <p>For optimal protection for your blob data, Microsoft recommends enabling all of the following
             *    data protection features:</p>
             *    <p>- containerDeleteRetentionPolicy (Container soft delete)</p>
             *    <p>- Blob versioning</p>
             *    <p>- deleteRetentionPolicy (Blob soft delete)</p>
             *
             * @return the next update stage
             */
            Update withBlobVersioningEnabled();

            /**
             * Disables blob versioning.
             * <p>After versioning is disabled, the first time you modify the blob with current version will result in
             * creating a new blob that has no version. All subsequent updates will go to this new blob and overwrite
             * its data without saving the previous state. All existing versions stay unaffected.</p>
             * <p>You can still list a blob's versions after versioning is disabled, or read or delete a specific
             * version of the blob using the version ID.
             * </p>
             * <p>For optimal protection for your blob data, Microsoft recommends enabling all of the following
             *    data protection features:</p>
             *    <p>- containerDeleteRetentionPolicy (Container soft delete)</p>
             *    <p>- Blob versioning</p>
             *    <p>- deleteRetentionPolicy (Blob soft delete)</p>
             *
             * @return the next update stage
             */
            Update withBlobVersioningDisabled();
        }

        /** The stage of the blobserviceproperties update allowing to specify last access time tracking policy. */
        interface WithLastAccessTimeTrackingPolicy {
            /**
             * Enables last access time tracking policy with default settings.
             * Last access time tracking integrates with lifecycle management to allow the automatic tiering and deletion
             * of data based on when individual blobs are last accessed.
             * It can also be used without lifecycle management by any solution that needs to understand
             * when individual blobs are last read and then take action.
             *
             * @return the next update stage
             */
            Update withLastAccessTimeTrackingPolicyEnabled();

            /**
             * Specify last access time tracking policy.
             * Last access time tracking integrates with lifecycle management to allow the automatic tiering and deletion
             * of data based on when individual blobs are last accessed.
             * It can also be used without lifecycle management by any solution that needs to understand
             * when individual blobs are last read and then take action.
             *
             * @param policy the last access time tracking policy
             * @return the next update stage
             */
            Update withLastAccessTimeTrackingPolicy(LastAccessTimeTrackingPolicy policy);

            /**
             * Disables last access time tracking policy.
             *
             * @return the next update stage
             */
            Update withLastAccessTimeTrackingPolicyDisabled();
        }
    }
}
