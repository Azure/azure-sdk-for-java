// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.util.Beta;

/**
 * Change feed mode
 */
@Beta(value = Beta.SinceVersion.V4_34_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
//  TODO:(kuthapar) - do we really need to expose this now or should it be exposed when we do merge support.
public enum ChangeFeedMode {
    /**
     * Incremental mode
     */
    INCREMENTAL,
    /**
     * Full Fidelity mode
     */
    FULL_FIDELITY
}
