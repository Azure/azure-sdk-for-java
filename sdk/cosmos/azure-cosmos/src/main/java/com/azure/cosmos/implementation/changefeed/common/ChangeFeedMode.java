// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.changefeed.common;

/**
 * Change feed mode
 * NOTE: We cannot rename these enums.
 * They are part of continuation token for Change feed pull model and are already in use for spark customers.
 */
public enum ChangeFeedMode {
    /**
     * Represents the latest version/Incremental change only. This mode only provides changes for Create, Replace and Upsert operations.
     * Only the most recent change for a given item is included in the change log. Intermediate changes may not be available.
     */
    INCREMENTAL,
    /**
     * Represents all version changes including deletes/Full Fidelity. This mode provides changes for Create, Replace, Upsert and Delete operations.
     * All changes for a given item are included in the changes log.
     */
    FULL_FIDELITY
}
