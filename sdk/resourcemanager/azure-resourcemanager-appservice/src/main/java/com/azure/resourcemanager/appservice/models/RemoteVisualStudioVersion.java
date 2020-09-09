// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.models;

import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

/** Defines values for remote visual studio version for remote debugging. */
public final class RemoteVisualStudioVersion extends ExpandableStringEnum<RemoteVisualStudioVersion> {
    /**
     * Static value VS2012 for RemoteVisualStudioVersion.
     * @deprecated use {@link RemoteVisualStudioVersion#VS2019}
     */
    @Deprecated public static final RemoteVisualStudioVersion VS2012 = RemoteVisualStudioVersion.fromString("VS2012");

    /**
     * Static value VS2013 for RemoteVisualStudioVersion.
     * @deprecated use {@link RemoteVisualStudioVersion#VS2019}
    */
    @Deprecated public static final RemoteVisualStudioVersion VS2013 = RemoteVisualStudioVersion.fromString("VS2013");

    /**
     * Static value VS2015 for RemoteVisualStudioVersion.
     * @deprecated use {@link RemoteVisualStudioVersion#VS2019}
     */
    @Deprecated public static final RemoteVisualStudioVersion VS2015 = RemoteVisualStudioVersion.fromString("VS2015");

    /** Static value VS2017 for RemoteVisualStudioVersion. */
    public static final RemoteVisualStudioVersion VS2017 = RemoteVisualStudioVersion.fromString("VS2017");

    /** Static value VS2019 for RemoteVisualStudioVersion. */
    public static final RemoteVisualStudioVersion VS2019 = RemoteVisualStudioVersion.fromString("VS2019");

    /**
     * Finds or creates a Visual Studio version based on the specified name.
     *
     * @param name a name
     * @return a RemoteVisualStudioVersion instance
     */
    public static RemoteVisualStudioVersion fromString(String name) {
        return fromString(name, RemoteVisualStudioVersion.class);
    }

    /** @return known Visual Studio versions */
    public static Collection<RemoteVisualStudioVersion> values() {
        return values(RemoteVisualStudioVersion.class);
    }
}
