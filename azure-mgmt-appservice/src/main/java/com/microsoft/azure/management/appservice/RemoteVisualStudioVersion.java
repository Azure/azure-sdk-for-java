/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.appservice;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for remote visual studio version for remote debugging.
 */
public final class RemoteVisualStudioVersion {
    /** Static value VS2012 for RemoteVisualStudioVersion. */
    public static final RemoteVisualStudioVersion VS2012 = new RemoteVisualStudioVersion("VS2012");

    /** Static value VS2013 for RemoteVisualStudioVersion. */
    public static final RemoteVisualStudioVersion VS2013 = new RemoteVisualStudioVersion("VS2013");

    /** Static value VS2015 for RemoteVisualStudioVersion. */
    public static final RemoteVisualStudioVersion VS2015 = new RemoteVisualStudioVersion("VS2015");

    private String value;

    /**
     * Creates a custom value for RemoteVisualStudioVersion.
     * @param value the custom value
     */
    public RemoteVisualStudioVersion(String value) {
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
        if (!(obj instanceof RemoteVisualStudioVersion)) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        RemoteVisualStudioVersion rhs = (RemoteVisualStudioVersion) obj;
        if (value == null) {
            return rhs.value == null;
        } else {
            return value.equals(rhs.value);
        }
    }
}
