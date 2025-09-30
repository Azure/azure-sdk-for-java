// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.changefeed.pkversion;

import com.azure.cosmos.implementation.changefeed.ChangeFeedObserver;
import com.azure.cosmos.implementation.changefeed.Lease;


/**
 * Factory class used to create instance(s) of {@link PartitionProcessor}.
 */
public interface PartitionProcessorFactory<T> {
    /**
     * Creates an instance of a {@link PartitionProcessor}.
     *
     * @param lease the lease to be used for partition processing.
     * @param changeFeedObserver the observer instace to be used.
     * @return an instance of {@link PartitionProcessor}.
     */
    PartitionProcessor create(Lease lease, ChangeFeedObserver<T> changeFeedObserver);
}
