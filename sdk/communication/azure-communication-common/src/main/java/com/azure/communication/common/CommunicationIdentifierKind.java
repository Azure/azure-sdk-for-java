// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.common;

import com.azure.core.util.ExpandableStringEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Collection;

/** Defines values for CommunicationIdentifierKind. */
public final class CommunicationIdentifierKind extends ExpandableStringEnum<CommunicationIdentifierKind> {
    /** Static value unknown for CommunicationIdentifierKind. */
    public static final CommunicationIdentifierKind UNKNOWN = fromString("unknown");

    /** Static value communicationUser for CommunicationIdentifierKind. */
    public static final CommunicationIdentifierKind COMMUNICATION_USER = fromString("communicationUser");

    /** Static value phoneNumber for CommunicationIdentifierKind. */
    public static final CommunicationIdentifierKind PHONE_NUMBER = fromString("phoneNumber");

    /** Static value callingApplication for CommunicationIdentifierKind. */
    public static final CommunicationIdentifierKind CALLING_APPLICATION = fromString("callingApplication");

    /** Static value microsoftTeamsUser for CommunicationIdentifierKind. */
    public static final CommunicationIdentifierKind MICROSOFT_TEAMS_USER = fromString("microsoftTeamsUser");

    /**
     * Creates or finds a CommunicationIdentifierKind from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding CommunicationIdentifierKind.
     */
    @JsonCreator
    public static CommunicationIdentifierKind fromString(String name) {
        return fromString(name, CommunicationIdentifierKind.class);
    }

    /** @return known CommunicationIdentifierKind values. */
    public static Collection<CommunicationIdentifierKind> values() {
        return values(CommunicationIdentifierKind.class);
    }
}
