// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models;

import com.azure.core.util.ExpandableStringEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Collection;

/** Defines values for StopTones. */
public final class StopTones extends ExpandableStringEnum<StopTones> {
    /** Static value zero for StopTones. */
    public static final StopTones ZERO = fromString("zero");

    /** Static value one for StopTones. */
    public static final StopTones ONE = fromString("one");

    /** Static value two for StopTones. */
    public static final StopTones TWO = fromString("two");

    /** Static value three for StopTones. */
    public static final StopTones THREE = fromString("three");

    /** Static value four for StopTones. */
    public static final StopTones FOUR = fromString("four");

    /** Static value five for StopTones. */
    public static final StopTones FIVE = fromString("five");

    /** Static value six for StopTones. */
    public static final StopTones SIX = fromString("six");

    /** Static value seven for StopTones. */
    public static final StopTones SEVEN = fromString("seven");

    /** Static value eight for StopTones. */
    public static final StopTones EIGHT = fromString("eight");

    /** Static value nine for StopTones. */
    public static final StopTones NINE = fromString("nine");

    /** Static value a for StopTones. */
    public static final StopTones A = fromString("a");

    /** Static value b for StopTones. */
    public static final StopTones B = fromString("b");

    /** Static value c for StopTones. */
    public static final StopTones C = fromString("c");

    /** Static value d for StopTones. */
    public static final StopTones D = fromString("d");

    /** Static value pound for StopTones. */
    public static final StopTones POUND = fromString("pound");

    /** Static value asterisk for StopTones. */
    public static final StopTones ASTERISK = fromString("asterisk");

    /**
     * Creates or finds a StopTones from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding StopTones.
     */
    @JsonCreator
    public static StopTones fromString(String name) {
        return fromString(name, StopTones.class);
    }

    /** @return known StopTones values. */
    public static Collection<StopTones> values() {
        return values(StopTones.class);
    }
}
