// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.models;

import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

/** Defines values for PHP version. */
public final class PhpVersion extends ExpandableStringEnum<PhpVersion> {
    /** Static value 'Off' for PhpVersion. */
    public static final PhpVersion OFF = PhpVersion.fromString("null");

    /** Static value 5.5 for PhpVersion. */
    public static final PhpVersion PHP5_5 = PhpVersion.fromString("5.5");

    /** Static value 5.6 for PhpVersion. */
    public static final PhpVersion PHP5_6 = PhpVersion.fromString("5.6");

    /** Static value 7.0 for PhpVersion. */
    public static final PhpVersion PHP7 = PhpVersion.fromString("7.0");

    /** Static value 7.1 for PhpVersion. */
    public static final PhpVersion PHP7_1 = PhpVersion.fromString("7.1");

    /**
     * Finds or creates a PHP version based on the specified name.
     *
     * @param name a name
     * @return a PhpVersion instance
     */
    public static PhpVersion fromString(String name) {
        return fromString(name, PhpVersion.class);
    }

    /** @return known PHP versions */
    public static Collection<PhpVersion> values() {
        return values(PhpVersion.class);
    }
}
