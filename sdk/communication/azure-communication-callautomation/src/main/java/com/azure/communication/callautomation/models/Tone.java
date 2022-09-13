// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.core.util.ExpandableStringEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Collection;

/** Defines values for Tone. */
public final class Tone extends ExpandableStringEnum<Tone> {
    /** Static value zero for Tone. */
    public static final Tone ZERO = fromString("zero");

    /** Static value one for Tone. */
    public static final Tone ONE = fromString("one");

    /** Static value two for Tone. */
    public static final Tone TWO = fromString("two");

    /** Static value three for Tone. */
    public static final Tone THREE = fromString("three");

    /** Static value four for Tone. */
    public static final Tone FOUR = fromString("four");

    /** Static value five for Tone. */
    public static final Tone FIVE = fromString("five");

    /** Static value six for Tone. */
    public static final Tone SIX = fromString("six");

    /** Static value seven for Tone. */
    public static final Tone SEVEN = fromString("seven");

    /** Static value eight for Tone. */
    public static final Tone EIGHT = fromString("eight");

    /** Static value nine for Tone. */
    public static final Tone NINE = fromString("nine");

    /** Static value a for Tone. */
    public static final Tone A = fromString("a");

    /** Static value b for Tone. */
    public static final Tone B = fromString("b");

    /** Static value c for Tone. */
    public static final Tone C = fromString("c");

    /** Static value d for Tone. */
    public static final Tone D = fromString("d");

    /** Static value pound for Tone. */
    public static final Tone POUND = fromString("pound");

    /** Static value asterisk for Tone. */
    public static final Tone ASTERISK = fromString("asterisk");

    /**
     * Creates or finds a Tone from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding Tone.
     */
    @JsonCreator
    public static Tone fromString(String name) {
        return fromString(name, Tone.class);
    }

    /** @return known Tone values. */
    public static Collection<Tone> values() {
        return values(Tone.class);
    }
}
