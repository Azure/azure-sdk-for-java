// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.changefeed;

import com.azure.cosmos.implementation.changefeed.epkversion.ServiceItemLeaseV1;
import com.azure.cosmos.implementation.changefeed.pkversion.ServiceItemLease;

public interface LeaseFactory {

    ServiceItemLease createServiceItemLease();

    ServiceItemLeaseV1 createServiceItemLeaseV1();
}
