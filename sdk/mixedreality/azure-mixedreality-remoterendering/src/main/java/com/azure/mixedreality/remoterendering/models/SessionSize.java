package com.azure.mixedreality.remoterendering.models;

import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

/** Defines values for SessionSize. */
public final class SessionSize extends ExpandableStringEnum<SessionSize> {
    /** Static value Standard for SessionSize. */
    public static final SessionSize STANDARD = fromString("Standard");

    /** Static value Premium for SessionSize. */
    public static final SessionSize PREMIUM = fromString("Premium");

    /**
     * Creates or finds a SessionSize from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding SessionSize.
     */
    public static SessionSize fromString(String name) {
        return fromString(name, SessionSize.class);
    }

    /** @return known SessionSize values. */
    public static Collection<SessionSize> values() {
        return values(SessionSize.class);
    }
}
