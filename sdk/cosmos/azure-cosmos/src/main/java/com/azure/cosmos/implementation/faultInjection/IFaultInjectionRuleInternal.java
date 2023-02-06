// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.faultInjection;

import java.util.List;

public interface IFaultInjectionRuleInternal {
    boolean isValid();
    List<String> getEndpointAddresses();
    String getId();
}
