// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.eventhubs.checkpoint;

import com.azure.messaging.eventhubs.models.EventContext;

/**
 *
 */
public interface EventCheckpoint {
    void checkpoint(EventContext context);
}
