package com.azure.cosmos.implementation.changefeed.common;

import com.azure.cosmos.implementation.changefeed.Lease;
import com.azure.cosmos.implementation.changefeed.LeaseFactory;
import com.azure.cosmos.implementation.changefeed.epkversion.ServiceItemLeaseV1;
import com.azure.cosmos.implementation.changefeed.pkversion.ServiceItemLease;

public class DefaultLeaseFactory implements LeaseFactory {
    @Override
    public ServiceItemLease createServiceItemLease() {
        return new ServiceItemLease();
    }

    @Override
    public ServiceItemLeaseV1 createServiceItemLeaseV1() {
        return new ServiceItemLeaseV1();
    }
}
