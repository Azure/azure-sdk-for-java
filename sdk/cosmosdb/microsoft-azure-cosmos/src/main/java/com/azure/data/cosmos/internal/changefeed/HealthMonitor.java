// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal.changefeed;

import reactor.core.publisher.Mono;

/**
 * A strategy for handling the situation when the change feed processor is not able to acquire lease due to unknown reasons.
 */
public interface HealthMonitor {
    /**
     * A logic to handle that exceptional situation.
     *
     * @param record the monitoring record.
     * @return a representation of the deferred computation of this call.
     */
    Mono<Void> inspect(HealthMonitoringRecord record);
}
