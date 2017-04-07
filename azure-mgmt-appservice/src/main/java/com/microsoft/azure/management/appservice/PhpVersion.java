/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.appservice;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for PHP version.
 */
public final class PhpVersion {
    /** Static value 'Off' for PhpVersion. */
    public static final PhpVersion OFF = new PhpVersion("null");

    /** Static value 5.5 for PhpVersion. */
    public static final PhpVersion PHP5_5 = new PhpVersion("5.5");

    /** Static value 5.6 for PhpVersion. */
    public static final PhpVersion PHP5_6 = new PhpVersion("5.6");

    /** Static value 7.0 for PhpVersion. */
    public static final PhpVersion PHP7 = new PhpVersion("7.0");

    /** Static value 7.1 for PhpVersion. */
    public static final PhpVersion PHP7_1 = new PhpVersion("7.1");

    private String value;

    /**
     * Creates a custom value for PhpVersion.
     * @param value the custom value
     */
    public PhpVersion(String value) {
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
        if (!(obj instanceof PhpVersion)) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        PhpVersion rhs = (PhpVersion) obj;
        if (value == null) {
            return rhs.value == null;
        } else {
            return value.equals(rhs.value);
        }
    }
}
