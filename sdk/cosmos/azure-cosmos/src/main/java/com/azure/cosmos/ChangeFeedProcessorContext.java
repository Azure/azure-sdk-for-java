// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import java.util.function.BiConsumer;

/**
 * Encapsulates properties which are mapped to a batch of change feed documents
 * processed when {@link  ChangeFeedProcessorBuilder#handleAllVersionsAndDeletesChanges(BiConsumer)}
 * lambda is invoked.
 * */
public interface ChangeFeedProcessorContext {
    /**
     * Gets the lease token corresponding to the source of
     * a batch of change feed documents.
     *
     * @return the lease token
     * */
    String getLeaseToken();
}
