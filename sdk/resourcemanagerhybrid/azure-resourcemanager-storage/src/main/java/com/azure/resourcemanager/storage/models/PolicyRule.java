// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.storage.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.model.Attachable;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;
import com.azure.resourcemanager.resources.fluentcore.model.Settable;
import java.util.List;

/** An immutable client-side representation of a rule in an Azure Management Policy. */
@Fluent
public interface PolicyRule extends HasInnerModel<ManagementPolicyRule> {
    /** @return the name of the rule */
    String name();

    /** @return the type of the rule */
    RuleType type();

    /** @return an unmodifiable list of the blob types this rule will apply for */
    List<BlobTypes> blobTypesToFilterFor();

    /** @return an unmodifiable list of the prefixes of the blob types this rule will apply for */
    List<String> prefixesToFilterFor();

    /** @return an object describing the actions to take on the filtered base blobs in this rule */
    ManagementPolicyBaseBlob actionsOnBaseBlob();

    /** @return an object describing the actions to take on the filtered snapshot in this rule */
    ManagementPolicySnapShot actionsOnSnapShot();

    /** @return whether there is a tier to cool action specified for the filtered base blobs in this rule */
    boolean tierToCoolActionOnBaseBlobEnabled();

    /** @return whether there is a tier to archive action specified for the filtered base blobs in this rule */
    boolean tierToArchiveActionOnBaseBlobEnabled();

    /** @return whether there is a delete action specified for the filtered base blobs in this rule */
    boolean deleteActionOnBaseBlobEnabled();

    /** @return whether there is a delete action specified for the filtered snapshots in this rule */
    boolean deleteActionOnSnapShotEnabled();

    /**
     * @return the number of days after a filtered base blob is last modified when the tier to cool action is enacted
     */
    Float daysAfterBaseBlobModificationUntilCooling();

    /**
     * @return the number of days after a filtered base blob is last modified when the tier to archive action is enacted
     */
    Float daysAfterBaseBlobModificationUntilArchiving();

    /** @return the number of days after a filtered base blob is last modified when the delete action is enacted */
    Float daysAfterBaseBlobModificationUntilDeleting();

    /** @return the number of days after a filtered snapshot is created when the delete action is enacted */
    Float daysAfterSnapShotCreationUntilDeleting();

    /** Container interface for all of the definitions related to a rule in a management policy. */
    interface Definition
        extends DefinitionStages.Blank,
            DefinitionStages.WithPolicyRuleType,
            DefinitionStages.WithBlobTypesToFilterFor,
            DefinitionStages.PrefixActionFork,
            DefinitionStages.WithPrefixesToFilterFor,
            DefinitionStages.WithRuleActions,
            DefinitionStages.WithPolicyRuleAttachable {
    }

    /** Container interface for all of the updates related to a rule in a management policy. */
    interface Update
        extends UpdateStages.WithBlobTypesToFilterFor,
            UpdateStages.WithPrefixesToFilterFor,
            UpdateStages.WithActions,
            Settable<ManagementPolicy.Update> {
    }

    /** Grouping of management policy rule definition stages. */
    interface DefinitionStages {
        /** The first stage of a management policy rule definition. */
        interface Blank extends WithPolicyRuleType {
        }

        /** The stage of the management policy rule definition allowing to specify the type of the rule. */
        interface WithPolicyRuleType {
            /**
             * The function that specifies Lifecycle as the type of the management policy rule.
             *
             * @return the next stage of the management policy rule definition.
             */
            WithBlobTypesToFilterFor withLifecycleRuleType();
        }

        /**
         * The stage of the management policy rule definition allowing to specify the blob types that the rule will
         * apply to.
         */
        interface WithBlobTypesToFilterFor {
            /**
             * The function that specifies the list of blob types that the rule will apply to.
             *
             * @param blobTypes a list of the types of blob the rule will apply to.
             * @return the next stage of the management policy rule definition.
             */
            PrefixActionFork withBlobTypesToFilterFor(List<BlobTypes> blobTypes);

            /**
             * The function that specifies a blob type that the rule will apply to.
             *
             * @param blobType a blob type that the rule will apply to.
             * @return the next stage of the management policy rule definition.
             */
            PrefixActionFork withBlobTypeToFilterFor(BlobTypes blobType);
        }

        /**
         * The stage of the management policy rule definition allowing input an optional blob prefix to filter for
         * before specifying the actions.
         */
        interface PrefixActionFork extends WithPrefixesToFilterFor, WithRuleActions {
        }

        /**
         * The stage of the management policy rule definition allowing the specify the prefixes for the blobs that the
         * rule will apply to.
         */
        interface WithPrefixesToFilterFor {
            /**
             * The function that specifies the list of prefixes for the blobs that the rule will apply to.
             *
             * @param prefixes a list of the prefixes for the blobs that the rule will apply to.
             * @return the next stage of the management policy rule definition.
             */
            WithRuleActions withPrefixesToFilterFor(List<String> prefixes);

            /**
             * The function that specifies a prefix for the blobs that the rule will apply to.
             *
             * @param prefix a prefix for the blobs that the rule will apply to.
             * @return the next stage of the management policy rule definition.
             */
            WithRuleActions withPrefixToFilterFor(String prefix);
        }

        /**
         * The stage of the management policy rule definition allowing to specify the actions to perform on the selected
         * blobs.
         */
        interface WithRuleActions {
            /**
             * The function that specifies a tier to cool action on the selected base blobs.
             *
             * @param daysAfterBaseBlobModificationUntilCooling the number of days after a base blob is last modified
             *     until it is cooled.
             * @return the next stage of the management policy rule definition.
             */
            WithPolicyRuleAttachable withTierToCoolActionOnBaseBlob(float daysAfterBaseBlobModificationUntilCooling);

            /**
             * The function that specifies a tier to archive action on the selected base blobs.
             *
             * @param daysAfterBaseBlobModificationUntilArchiving the number of days after a base blob is last modified
             *     until it is archived.
             * @return the next stage of the management policy rule definition.
             */
            WithPolicyRuleAttachable withTierToArchiveActionOnBaseBlob(
                float daysAfterBaseBlobModificationUntilArchiving);

            /**
             * The function that specifies a delete action on the selected base blobs.
             *
             * @param daysAfterBaseBlobModificationUntilDeleting the number of days after a base blob is last modified
             *     until it is deleted.
             * @return the next stage of the management policy rule definition.
             */
            WithPolicyRuleAttachable withDeleteActionOnBaseBlob(float daysAfterBaseBlobModificationUntilDeleting);

            /**
             * The function that specifies a delete action on the selected snapshots.
             *
             * @param daysAfterSnapShotCreationUntilDeleting the number of days after a snapshot is created until it is
             *     deleted.
             * @return the next stage of the management policy rule definition
             */
            WithPolicyRuleAttachable withDeleteActionOnSnapShot(float daysAfterSnapShotCreationUntilDeleting);

            /**
             * The function that specifies all of the actions to apply to selected base blobs.
             *
             * @param baseBlobActions an object including all of the actions to apply to selected base blobs.
             * @return the next stage of the management policy rule definition.
             */
            WithPolicyRuleAttachable withActionsOnBaseBlob(ManagementPolicyBaseBlob baseBlobActions);

            /**
             * The function that specifies all of the actions to apply to selected snapshots.
             *
             * @param snapShotActions an object including all of the actions to apply to selected snapshots.
             * @return the next stage of the management policy rule definition.
             */
            WithPolicyRuleAttachable withActionsOnSnapShot(ManagementPolicySnapShot snapShotActions);
        }

        /**
         * The stage of the definition which contains all of the minimum required inputs for the resource to be
         * attached, but also allows for any other optional settings to be specified.
         */
        interface WithPolicyRuleAttachable
            extends PolicyRule.DefinitionStages.WithRuleActions,
                PolicyRule.DefinitionStages.WithPrefixesToFilterFor,
                Attachable<ManagementPolicy.DefinitionStages.WithCreate> {
        }
    }

    /** Grouping of management policy rule update stages. */
    interface UpdateStages {
        /**
         * The stage of the management policy rule update allowing to specify the blob types that the rule will apply
         * to.
         */
        interface WithBlobTypesToFilterFor {
            /**
             * The function that specifies the list of blob types that the rule will apply to.
             *
             * @param blobTypes a list of the types of blob the rule will apply to.
             * @return the next stage of the management policy rule update.
             */
            Update withBlobTypesToFilterFor(List<BlobTypes> blobTypes);

            /**
             * The function that specifies a blob type that the rule will apply to.
             *
             * @param blobType a blob type that the rule will apply to.
             * @return the next stage of the management policy rule update.
             */
            Update withBlobTypeToFilterFor(BlobTypes blobType);

            /**
             * The function that specifies to remove a blob type that the rule will apply to.
             *
             * @param blobType the blob type that you wish the rule to no longer apply to.
             * @return the next stage of the management policy rule update.
             */
            Update withBlobTypeToFilterForRemoved(BlobTypes blobType);
        }

        /**
         * THe stage of the management policy rule update allowing to specify the prefixes for the blobs that the rule
         * will apply to.
         */
        interface WithPrefixesToFilterFor {
            /**
             * The function that specifies the list of prefixes for the blobs that the rule will apply to.
             *
             * @param prefixes a list of the prefixes for the blobs that the rule will apply to.
             * @return the next stage of the management policy rule update.
             */
            Update withPrefixesToFilterFor(List<String> prefixes);

            /**
             * The function that specifies a prefix for the blobs that the rule will apply to.
             *
             * @param prefix a prefix for the blobs that the rule will apply to.
             * @return the next stage of the management policy rule update.
             */
            Update withPrefixToFilterFor(String prefix);

            /**
             * The function that clears all blob prefixes so the rule will apply to blobs regardless of prefixes.
             *
             * @return the next stage of the management policy rule update.
             */
            Update withoutPrefixesToFilterFor();
        }

        /**
         * The stage of the management policy rule update allowing to specify the actions to perform on the selected
         * blobs.
         */
        interface WithActions {
            /**
             * The function that specifies a tier to cool action on the selected base blobs.
             *
             * @param daysAfterBaseBlobModificationUntilCooling the number of days after a base blob is last modified
             *     until it is cooled.
             * @return the next stage of the management policy rule update.
             */
            Update withTierToCoolActionOnBaseBlob(float daysAfterBaseBlobModificationUntilCooling);

            /**
             * The function that specifies a tier to archive action on the selected base blobs.
             *
             * @param daysAfterBaseBlobModificationUntilArchiving the number of days after a base blob is last modified
             *     until it is archived.
             * @return the next stage of the management policy rule update.
             */
            Update withTierToArchiveActionOnBaseBlob(float daysAfterBaseBlobModificationUntilArchiving);

            /**
             * The function that specifies a delete action on the selected base blobs.
             *
             * @param daysAfterBaseBlobModificationUntilDeleting the number of days after a base blob is last modified
             *     until it is deleted.
             * @return the next stage of the management policy rule update.
             */
            Update withDeleteActionOnBaseBlob(float daysAfterBaseBlobModificationUntilDeleting);

            /**
             * The function that specifies a delete action on the selected snapshots.
             *
             * @param daysAfterSnapShotCreationUntilDeleting the number of days after a snapshot is created until it is
             *     deleted.
             * @return the next stage of the management policy rule update
             */
            Update withDeleteActionOnSnapShot(float daysAfterSnapShotCreationUntilDeleting);

            /**
             * The function that specifies all of the actions to apply to selected base blobs.
             *
             * @param baseBlobActions an object including all of the actions to apply to selected base blobs.
             * @return the next stage of the management policy rule update.
             */
            Update updateActionsOnBaseBlob(ManagementPolicyBaseBlob baseBlobActions);

            /**
             * The function that specifies all of the actions to apply to selected snapshots.
             *
             * @param snapShotActions an object including all of the actions to apply to selected snapshots.
             * @return the next stage of the management policy rule update.
             */
            Update updateActionsOnSnapShot(ManagementPolicySnapShot snapShotActions);
        }
    }
}
