// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.util.Beta;

/**
 * Change feed mode
 */
@Beta(value = Beta.SinceVersion.V4_37_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
public enum ChangeFeedMode {
    /**
     * Represents the latest version change only. This mode only provides changes for Create, Replace and Upsert operations.
     * Only the most recent change for a given item is included in the change log. Intermediate changes may not be available.
     */
    LATEST_VERSION,
    /**
     * Represents all version changes including deletes. This mode provides changes for Create, Replace, Upsert and Delete operations.
     * All changes for a given item are included in the changes log.
     */
    ALL_VERSIONS_AND_DELETES
}
