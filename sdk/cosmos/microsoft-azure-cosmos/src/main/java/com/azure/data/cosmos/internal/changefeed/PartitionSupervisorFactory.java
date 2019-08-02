// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal.changefeed;

/**
 * Interface for the partition supervisor factory.
 */
public interface PartitionSupervisorFactory {
    /**
     *
     * @param lease the lease.
     * @return an instance of {@link PartitionSupervisor}.
     */
    PartitionSupervisor create(Lease lease);
}
