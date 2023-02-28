// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.faultinjection.model;

import java.net.URI;
import java.util.List;

public interface IFaultInjectionRuleInternal {
    void disable();
    List<URI> getAddresses();
    List<URI> getRegionEndpoints();
    boolean isValid();
    String getId();

    long getHitCount();
}
