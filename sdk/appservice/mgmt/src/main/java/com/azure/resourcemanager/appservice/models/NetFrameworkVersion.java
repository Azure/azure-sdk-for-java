// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.models;

import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

/** Defines values for .NET framework version. */
public final class NetFrameworkVersion extends ExpandableStringEnum<NetFrameworkVersion> {
    /** Static value v3.5 for NetFrameworkVersion. */
    public static final NetFrameworkVersion V3_0 = NetFrameworkVersion.fromString("v3.0");

    /** Static value v4.6 for NetFrameworkVersion. */
    public static final NetFrameworkVersion V4_6 = NetFrameworkVersion.fromString("v4.6");

    /**
     * Finds or creates a .NET Framework version based on the name.
     *
     * @param name a name
     * @return an instance of NetFrameworkVersion
     */
    public static NetFrameworkVersion fromString(String name) {
        return fromString(name, NetFrameworkVersion.class);
    }

    /** @return known .NET framework versions */
    public static Collection<NetFrameworkVersion> values() {
        return values(NetFrameworkVersion.class);
    }
}
