// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventprocessorhost;

/***
 * Used when implementing IEventProcessor. One argument to onClose is this enum.
 */
public enum CloseReason {
    /***
     * The IEventProcessor is closing because the lease on the partition has been lost.
     */
    LeaseLost,

    /***
     * The IEventProcessor is closing because the event processor host is being shut down,
     * or because an error has occurred.
     */
    Shutdown
}
