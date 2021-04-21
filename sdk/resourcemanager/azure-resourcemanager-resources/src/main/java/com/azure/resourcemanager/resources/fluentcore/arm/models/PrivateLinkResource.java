// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.arm.models;

import java.util.List;

/** An interface representing a private link resource. */
public interface PrivateLinkResource {

    /** @return The group ID of Private link resource. */
    String groupId();

    /** @return The collection of required member names of Private link resource. */
    List<String> requiredMemberNames();

    /** @return The collection of DNS zone names of Private link resource. */
    List<String> requiredDnsZoneNames();
}
