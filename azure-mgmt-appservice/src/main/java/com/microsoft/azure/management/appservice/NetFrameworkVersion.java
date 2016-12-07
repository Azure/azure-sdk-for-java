/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.appservice;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for .NET framework version.
 */
public final class NetFrameworkVersion {
    /** Static value v3.5 for NetFrameworkVersion. */
    public static final NetFrameworkVersion V3_0 = new NetFrameworkVersion("v3.0");

    /** Static value v4.6 for NetFrameworkVersion. */
    public static final NetFrameworkVersion V4_6 = new NetFrameworkVersion("v4.6");

    private String value;

    /**
     * Creates a custom value for NetFrameworkVersion.
     * @param value the custom value
     */
    public NetFrameworkVersion(String value) {
        this.value = value;
    }

    @JsonValue
    @Override
    public String toString() {
        return value;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof NetFrameworkVersion)) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        NetFrameworkVersion rhs = (NetFrameworkVersion) obj;
        if (value == null) {
            return rhs.value == null;
        } else {
            return value.equals(rhs.value);
        }
    }
}
