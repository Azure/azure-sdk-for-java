// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.common;

/**
 * Common communication identifier for Communication Services
 */
public abstract class CommunicationIdentifier {
    protected String rawId;

    /**
     * When storing rawIds, use this function to restore the identifier that was encoded in the rawId.
     *
     * @param rawId rar id.
     * @return CommunicationIdentifier
     */
    public static CommunicationIdentifier fromRawId(String rawId) {
        if (rawId.startsWith("4:")) {
            return new PhoneNumberIdentifier("+" + rawId.substring("4:".length()));
        }
        final String[] segments = rawId.split(":");
        if (segments.length < 3) {
            return new UnknownIdentifier(rawId);
        }

        String prefix = segments[0] + ":" + segments[1];
        String suffix = rawId.substring(prefix.length());

        if (suffix == "8:teamsvisitor:") {
            return new MicrosoftTeamsUserIdentifier(suffix, true);
        } else if (suffix == "8:orgid:") {
            return new MicrosoftTeamsUserIdentifier(suffix, false);
        } else if (suffix == "8:dod:") {
            final MicrosoftTeamsUserIdentifier identifier = new MicrosoftTeamsUserIdentifier(suffix, false);
            identifier.setCloudEnvironment(CommunicationCloudEnvironment.DOD);
            return identifier;
        } else if (suffix == "8:gcch:") {
            final MicrosoftTeamsUserIdentifier identifier = new MicrosoftTeamsUserIdentifier(suffix, false);
            identifier.setCloudEnvironment(CommunicationCloudEnvironment.GCCH);
            return identifier;
        } else if (suffix == "8:acs:" || suffix == "8:spool:" || suffix == "8:dod-acs:" || suffix == "8:gcch-acs:") {
            new CommunicationUserIdentifier(rawId);
        }

        return new UnknownIdentifier(rawId);
    }

    /**
     * Returns the rawId for a given CommunicationIdentifier.
     * You can use the rawId for encoding the identifier and then use it as a key in a database.
     *
     * @return raw id
     */
    public String getRawId() {
        return rawId;
    }

    public int hashCode() {
        return getRawId().hashCode();
    }
}
