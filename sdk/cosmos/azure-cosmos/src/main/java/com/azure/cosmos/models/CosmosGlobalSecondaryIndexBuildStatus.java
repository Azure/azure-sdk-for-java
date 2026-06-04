// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.core.util.ExpandableStringEnum;
import com.azure.cosmos.util.Beta;

import java.util.Collection;

/**
 * Represents the build status of a global secondary index as returned by the Azure Cosmos DB service.
 * <p>
 * This is an {@link ExpandableStringEnum} so that values added by the service in the future are not
 * a breaking change for clients. {@link #fromString(String)} will return an instance for any value,
 * including ones not declared as a constant on this class.
 */
@Beta(value = Beta.SinceVersion.V4_81_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
public final class CosmosGlobalSecondaryIndexBuildStatus
    extends ExpandableStringEnum<CosmosGlobalSecondaryIndexBuildStatus> {

    /**
     * The global secondary index has been created and is initializing.
     */
    @Beta(value = Beta.SinceVersion.V4_81_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public static final CosmosGlobalSecondaryIndexBuildStatus INITIALIZING = fromString("Initializing");

    /**
     * The global secondary index is performing its initial build after being created.
     */
    @Beta(value = Beta.SinceVersion.V4_81_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public static final CosmosGlobalSecondaryIndexBuildStatus INITIAL_BUILD_AFTER_CREATE
        = fromString("InitialBuildAfterCreate");

    /**
     * The global secondary index is performing its initial build after the source container was restored.
     */
    @Beta(value = Beta.SinceVersion.V4_81_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public static final CosmosGlobalSecondaryIndexBuildStatus INITIAL_BUILD_AFTER_RESTORE
        = fromString("InitialBuildAfterRestore");

    /**
     * The global secondary index has been fully built and is actively serving queries.
     */
    @Beta(value = Beta.SinceVersion.V4_81_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public static final CosmosGlobalSecondaryIndexBuildStatus ACTIVE = fromString("Active");

    /**
     * The global secondary index is being deleted.
     */
    @Beta(value = Beta.SinceVersion.V4_81_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public static final CosmosGlobalSecondaryIndexBuildStatus DELETE_IN_PROGRESS = fromString("DeleteInProgress");

    /**
     * Creates a new instance of {@link CosmosGlobalSecondaryIndexBuildStatus} without a name.
     * <p>
     * Prefer {@link #fromString(String)} to obtain instances; this constructor is required by
     * {@link ExpandableStringEnum}.
     *
     * @deprecated Use {@link #fromString(String)} instead.
     */
    @Beta(value = Beta.SinceVersion.V4_81_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    @Deprecated
    public CosmosGlobalSecondaryIndexBuildStatus() {
    }

    /**
     * Creates or finds a {@link CosmosGlobalSecondaryIndexBuildStatus} from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding {@link CosmosGlobalSecondaryIndexBuildStatus}, or {@code null} when {@code name}
     *     is {@code null}.
     */
    @Beta(value = Beta.SinceVersion.V4_81_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public static CosmosGlobalSecondaryIndexBuildStatus fromString(String name) {
        return fromString(name, CosmosGlobalSecondaryIndexBuildStatus.class);
    }

    /**
     * Gets the known {@link CosmosGlobalSecondaryIndexBuildStatus} values declared on this class.
     *
     * @return the known {@link CosmosGlobalSecondaryIndexBuildStatus} values.
     */
    @Beta(value = Beta.SinceVersion.V4_81_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public static Collection<CosmosGlobalSecondaryIndexBuildStatus> values() {
        return values(CosmosGlobalSecondaryIndexBuildStatus.class);
    }
}
