// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.apachecommons.lang.NotImplementedException;

import java.util.function.BiConsumer;

/**
 * Encapsulates properties which are mapped to a batch of change feed documents
 * processed when {@link  ChangeFeedProcessorBuilder#handleAllVersionsAndDeletesChanges(BiConsumer)}
 * lambda is invoked.
 * <br>
 * <br>
 * NOTE: This interface is not designed to be implemented by end users.
 * */
public interface ChangeFeedProcessorContext {
    /**
     * Gets the lease token corresponding to the source of
     * a batch of change feed documents.
     *
     * @return the lease token
     * */
    String getLeaseToken();

    /**
     * Get the diagnostics context from the underlying feed response.
     *
     * @return The diagnostics object.
     */
    default CosmosDiagnosticsContext getDiagnostics() {
        throw new NotImplementedException("Method has not been implemented. NOTE: This method is not designed to be implemented by end users.");
    }
}
