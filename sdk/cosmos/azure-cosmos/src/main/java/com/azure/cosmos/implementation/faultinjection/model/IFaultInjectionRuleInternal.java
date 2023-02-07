// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.faultinjection.model;

import com.azure.cosmos.implementation.directconnectivity.Uri;

import java.util.List;

public interface IFaultInjectionRuleInternal {
    boolean isValid();
    List<Uri> getPhysicalAddresses();
    String getId();
}
