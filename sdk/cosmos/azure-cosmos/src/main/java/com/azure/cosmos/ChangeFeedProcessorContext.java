// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.util.Beta;

import java.util.function.BiConsumer;

/**
 * Encapsulates properties which are mapped to a batch of change feed documents
 * processed when {@link  ChangeFeedProcessorBuilder#handleAllVersionsAndDeletesChanges(BiConsumer)}
 * lambda is invoked.
 * <br>
 * <br>
 * NOTE: This interface is not designed to be implemented by end users.
 * */
@Beta(value = Beta.SinceVersion.V4_51_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
public interface ChangeFeedProcessorContext {
    /**
     * Gets the lease token corresponding to the source of
     * a batch of change feed documents.
     *
     * @return the lease token
     * */
    @Beta(value = Beta.SinceVersion.V4_51_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    String getLeaseToken();

    /**
     * Get the diagnostics from the underlying feed response.
     * @return
     */
    CosmosDiagnostics getDiagnostics();
}
