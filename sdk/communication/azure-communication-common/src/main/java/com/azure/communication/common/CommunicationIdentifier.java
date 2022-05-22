// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.common;

import com.azure.core.util.CoreUtils;

/**
 * Common communication identifier for Communication Services
 */
public abstract class CommunicationIdentifier {
    protected String rawId;

    /**
     * When storing rawIds, use this function to restore the identifier that was encoded in the rawId.
     *
     * @param rawId raw id.
     * @return CommunicationIdentifier
     */
    public static CommunicationIdentifier fromRawId(String rawId) {
        if (CoreUtils.isNullOrEmpty(rawId)) {
            throw new IllegalArgumentException("The parameter [rawId] cannot be null to empty.");
        }

        if (rawId.startsWith("4:")) {
            return new PhoneNumberIdentifier("+" + rawId.substring("4:".length()));
        }
        final String[] segments = rawId.split(":");
        if (segments.length < 3) {
            return new UnknownIdentifier(rawId);
        }

        final String prefix = segments[0] + ":" + segments[1]+":";
        final String suffix = rawId.substring(prefix.length());

        if (prefix.equals("8:teamsvisitor:")) {
            return new MicrosoftTeamsUserIdentifier(suffix, true);
        } else if (prefix.equals("8:orgid:")) {
            return new MicrosoftTeamsUserIdentifier(suffix, false);
        } else if (prefix.equals("8:dod:")) {
            return new MicrosoftTeamsUserIdentifier(suffix, false).setCloudEnvironment(CommunicationCloudEnvironment.DOD);
        } else if (prefix.equals("8:gcch:")) {
            return new MicrosoftTeamsUserIdentifier(suffix, false).setCloudEnvironment(CommunicationCloudEnvironment.GCCH);
        } else if (prefix.equals("8:acs:") || prefix.equals("8:spool:") || prefix.equals("8:dod-acs:") || prefix.equals("8:gcch-acs:")) {
            return new CommunicationUserIdentifier(rawId);
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
