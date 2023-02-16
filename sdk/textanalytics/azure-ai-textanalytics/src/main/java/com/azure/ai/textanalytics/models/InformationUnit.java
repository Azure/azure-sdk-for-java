// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.util.ExpandableStringEnum;

/** Defines values for InformationUnit. */
public final class InformationUnit extends ExpandableStringEnum<InformationUnit> {
    /** Static value Unspecified for InformationUnit. */
    public static final InformationUnit UNSPECIFIED = fromString("Unspecified");

    /** Static value Bit for InformationUnit. */
    public static final InformationUnit BIT = fromString("Bit");

    /** Static value Kilobit for InformationUnit. */
    public static final InformationUnit KILOBIT = fromString("Kilobit");

    /** Static value Megabit for InformationUnit. */
    public static final InformationUnit MEGABIT = fromString("Megabit");

    /** Static value Gigabit for InformationUnit. */
    public static final InformationUnit GIGABIT = fromString("Gigabit");

    /** Static value Terabit for InformationUnit. */
    public static final InformationUnit TERABIT = fromString("Terabit");

    /** Static value Petabit for InformationUnit. */
    public static final InformationUnit PETABIT = fromString("Petabit");

    /** Static value Byte for InformationUnit. */
    public static final InformationUnit BYTE = fromString("Byte");

    /** Static value Kilobyte for InformationUnit. */
    public static final InformationUnit KILOBYTE = fromString("Kilobyte");

    /** Static value Megabyte for InformationUnit. */
    public static final InformationUnit MEGABYTE = fromString("Megabyte");

    /** Static value Gigabyte for InformationUnit. */
    public static final InformationUnit GIGABYTE = fromString("Gigabyte");

    /** Static value Terabyte for InformationUnit. */
    public static final InformationUnit TERABYTE = fromString("Terabyte");

    /** Static value Petabyte for InformationUnit. */
    public static final InformationUnit PETABYTE = fromString("Petabyte");

    /**
     * Creates or finds a InformationUnit from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding InformationUnit.
     */
    public static InformationUnit fromString(String name) {
        return fromString(name, InformationUnit.class);
    }
}
