// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.implementation.routing.PartitionKeyRangeIdentity;

public interface IOpenConnectionHandler {
    void openConnections(PartitionKeyRangeIdentity pkRangeIdentity, AddressInformation[] addressInformations);
}
