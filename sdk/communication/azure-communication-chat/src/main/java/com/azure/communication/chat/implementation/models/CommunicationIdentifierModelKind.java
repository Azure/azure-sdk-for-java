// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.communication.chat.implementation.models;

import com.azure.core.util.ExpandableStringEnum;
import java.util.Collection;

/**
 * The identifier kind, for example 'communicationUser' or 'phoneNumber'.
 */
public final class CommunicationIdentifierModelKind extends ExpandableStringEnum<CommunicationIdentifierModelKind> {
    /**
     * Static value unknown for CommunicationIdentifierModelKind.
     */
    public static final CommunicationIdentifierModelKind UNKNOWN = fromString("unknown");

    /**
     * Static value communicationUser for CommunicationIdentifierModelKind.
     */
    public static final CommunicationIdentifierModelKind COMMUNICATION_USER = fromString("communicationUser");

    /**
     * Static value phoneNumber for CommunicationIdentifierModelKind.
     */
    public static final CommunicationIdentifierModelKind PHONE_NUMBER = fromString("phoneNumber");

    /**
     * Static value microsoftTeamsUser for CommunicationIdentifierModelKind.
     */
    public static final CommunicationIdentifierModelKind MICROSOFT_TEAMS_USER = fromString("microsoftTeamsUser");

    /**
     * Static value microsoftTeamsApp for CommunicationIdentifierModelKind.
     */
    public static final CommunicationIdentifierModelKind MICROSOFT_TEAMS_APP = fromString("microsoftTeamsApp");

    /**
     * Creates a new instance of CommunicationIdentifierModelKind value.
     * 
     * @deprecated Use the {@link #fromString(String)} factory method.
     */
    @Deprecated
    public CommunicationIdentifierModelKind() {
    }

    /**
     * Creates or finds a CommunicationIdentifierModelKind from its string representation.
     * 
     * @param name a name to look for.
     * @return the corresponding CommunicationIdentifierModelKind.
     */
    public static CommunicationIdentifierModelKind fromString(String name) {
        return fromString(name, CommunicationIdentifierModelKind.class);
    }

    /**
     * Gets known CommunicationIdentifierModelKind values.
     * 
     * @return known CommunicationIdentifierModelKind values.
     */
    public static Collection<CommunicationIdentifierModelKind> values() {
        return values(CommunicationIdentifierModelKind.class);
    }
}
