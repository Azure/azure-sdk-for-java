// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.implementation.routing.PartitionKeyRangeIdentity;
import reactor.core.publisher.Mono;

public interface IOpenConnectionHandler {
    Mono<Void> openConnections(PartitionKeyRangeIdentity pkRangeIdentity, AddressInformation[] addressInformations);
}
