// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.core.util.ExpandableStringEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Collection;

/** Defines values for Tone. */
public final class DtmfTone extends ExpandableStringEnum<DtmfTone> {
    /** Static value zero for Tone. */
    public static final DtmfTone ZERO = fromString("zero");

    /** Static value one for Tone. */
    public static final DtmfTone ONE = fromString("one");

    /** Static value two for Tone. */
    public static final DtmfTone TWO = fromString("two");

    /** Static value three for Tone. */
    public static final DtmfTone THREE = fromString("three");

    /** Static value four for Tone. */
    public static final DtmfTone FOUR = fromString("four");

    /** Static value five for Tone. */
    public static final DtmfTone FIVE = fromString("five");

    /** Static value six for Tone. */
    public static final DtmfTone SIX = fromString("six");

    /** Static value seven for Tone. */
    public static final DtmfTone SEVEN = fromString("seven");

    /** Static value eight for Tone. */
    public static final DtmfTone EIGHT = fromString("eight");

    /** Static value nine for Tone. */
    public static final DtmfTone NINE = fromString("nine");

    /** Static value a for Tone. */
    public static final DtmfTone A = fromString("a");

    /** Static value b for Tone. */
    public static final DtmfTone B = fromString("b");

    /** Static value c for Tone. */
    public static final DtmfTone C = fromString("c");

    /** Static value d for Tone. */
    public static final DtmfTone D = fromString("d");

    /** Static value pound for Tone. */
    public static final DtmfTone POUND = fromString("pound");

    /** Static value asterisk for Tone. */
    public static final DtmfTone ASTERISK = fromString("asterisk");

    /**
     * Creates or finds a Tone from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding Tone.
     */
    @JsonCreator
    public static DtmfTone fromString(String name) {
        return fromString(name, DtmfTone.class);
    }

    /** @return known Tone values. */
    public static Collection<DtmfTone> values() {
        return values(DtmfTone.class);
    }
}
