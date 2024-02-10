package com.azure.cosmos.implementation.changefeed;

import com.azure.cosmos.implementation.changefeed.epkversion.ServiceItemLeaseV1;
import com.azure.cosmos.implementation.changefeed.pkversion.ServiceItemLease;

public interface LeaseFactory {

    ServiceItemLease createServiceItemLease();

    ServiceItemLeaseV1 createServiceItemLeaseV1();
}
