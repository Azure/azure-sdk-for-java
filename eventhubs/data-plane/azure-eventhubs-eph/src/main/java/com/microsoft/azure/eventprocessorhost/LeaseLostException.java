/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.eventprocessorhost;

public class LeaseLostException extends Exception {
    private static final long serialVersionUID = -4625001822439809869L;

    private final BaseLease lease;

    LeaseLostException(BaseLease lease, Throwable cause) {
        super(null, cause);
        this.lease = lease;
    }

    LeaseLostException(BaseLease lease, String message) {
        super(message, null);
        this.lease = lease;
    }

    // We don't want to expose Lease to the public.
    public String getPartitionId() {
        return this.lease.getPartitionId();
    }
}
