/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.appservice;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for Python version.
 */
public final class PythonVersion {
    /** Static value 'Off' for PythonVersion. */
    public static final PythonVersion OFF = new PythonVersion("null");

    /** Static value 2.7 for PythonVersion. */
    public static final PythonVersion PYTHON_27 = new PythonVersion("2.7");

    /** Static value 3.4 for PythonVersion. */
    public static final PythonVersion PYTHON_34 = new PythonVersion("3.4");

    private String value;

    /**
     * Creates a custom value for PythonVersion.
     * @param value the custom value
     */
    public PythonVersion(String value) {
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
        if (!(obj instanceof PythonVersion)) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        PythonVersion rhs = (PythonVersion) obj;
        if (value == null) {
            return rhs.value == null;
        } else {
            return value.equals(rhs.value);
        }
    }
}
