/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 *
 * Code generated by Microsoft (R) AutoRest Code Generator.
 */

package com.microsoft.azure.management.streamanalytics.v2020_03_01_preview;

import java.util.Collection;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.microsoft.rest.ExpandableStringEnum;

/**
 * Defines values for EventsOutOfOrderPolicy.
 */
public final class EventsOutOfOrderPolicy extends ExpandableStringEnum<EventsOutOfOrderPolicy> {
    /** Static value Adjust for EventsOutOfOrderPolicy. */
    public static final EventsOutOfOrderPolicy ADJUST = fromString("Adjust");

    /** Static value Drop for EventsOutOfOrderPolicy. */
    public static final EventsOutOfOrderPolicy DROP = fromString("Drop");

    /**
     * Creates or finds a EventsOutOfOrderPolicy from its string representation.
     * @param name a name to look for
     * @return the corresponding EventsOutOfOrderPolicy
     */
    @JsonCreator
    public static EventsOutOfOrderPolicy fromString(String name) {
        return fromString(name, EventsOutOfOrderPolicy.class);
    }

    /**
     * @return known EventsOutOfOrderPolicy values
     */
    public static Collection<EventsOutOfOrderPolicy> values() {
        return values(EventsOutOfOrderPolicy.class);
    }
}
