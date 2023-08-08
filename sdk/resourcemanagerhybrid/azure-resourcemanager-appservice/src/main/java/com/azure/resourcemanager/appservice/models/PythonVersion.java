// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.models;

import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

/** Defines values for Python version. */
public final class PythonVersion extends ExpandableStringEnum<PythonVersion> {
    /** Static value 'Off' for PythonVersion. */
    public static final PythonVersion OFF = PythonVersion.fromString("null");

    /** Static value 2.7 for PythonVersion. */
    public static final PythonVersion PYTHON_27 = PythonVersion.fromString("2.7");

    /** Static value 3.4 for PythonVersion. */
    public static final PythonVersion PYTHON_34 = PythonVersion.fromString("3.4");

    /**
     * Finds or creates a Python version based on the specified name.
     *
     * @param name a name
     * @return a PythonVersion instance
     */
    public static PythonVersion fromString(String name) {
        return fromString(name, PythonVersion.class);
    }

    /** @return known Python versions */
    public static Collection<PythonVersion> values() {
        return values(PythonVersion.class);
    }
}
