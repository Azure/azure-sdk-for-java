// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.common;

import com.azure.core.util.CoreUtils;

/**
 * Common communication identifier for Communication Services
 */
public abstract class CommunicationIdentifier {

    static final String PHONE_NUMBER_PREFIX = "4:";

    static final String BOT_PREFIX = "28:";

    static final String BOT_PUBLIC_CLOUD_PREFIX = "28:orgid:";

    static final String BOT_DOD_CLOUD_PREFIX = "28:dod:";

    static final String BOT_DOD_CLOUD_GLOBAL_PREFIX = "28:dod-global:";

    static final String BOT_GCCH_CLOUD_PREFIX = "28:gcch:";

    static final String BOT_GCCH_CLOUD_GLOBAL_PREFIX = "28:gcch-global:";

    static final String TEAMS_USER_ANONYMOUS_PREFIX = "8:teamsvisitor:";

    static final String TEAMS_USER_PUBLIC_CLOUD_PREFIX = "8:orgid:";

    static final String TEAMS_USER_DOD_CLOUD_PREFIX = "8:dod:";

    static final String TEAMS_USER_GCCH_CLOUD_PREFIX = "8:gcch:";

    static final String ACS_USER_PREFIX = "8:acs:";

    static final String ACS_USER_DOD_CLOUD_PREFIX = "8:dod-acs:";

    static final String ACS_USER_GCCH_CLOUD_PREFIX = "8:gcch-acs:";

    static final String SPOOL_USER_PREFIX = "8:spool:";

    private String rawId;

    /**
     * When storing rawIds, use this function to restore the identifier that was encoded in the rawId.
     *
     * @param rawId raw id.
     * @return CommunicationIdentifier
     * @throws IllegalArgumentException raw id is null or empty.
     */
    public static CommunicationIdentifier fromRawId(String rawId) {
        if (CoreUtils.isNullOrEmpty(rawId)) {
            throw new IllegalArgumentException("The parameter [rawId] cannot be null to empty.");
        }

        if (rawId.startsWith(PHONE_NUMBER_PREFIX)) {
            return new PhoneNumberIdentifier(rawId.substring(PHONE_NUMBER_PREFIX.length()));
        }
        final String[] segments = rawId.split(":");
        if (segments.length != 3) {
            if (segments.length == 2 && rawId.startsWith(BOT_PREFIX)) {
                return new MicrosoftBotIdentifier(segments[1], CommunicationCloudEnvironment.PUBLIC, true);
            }
            return new UnknownIdentifier(rawId);
        }

        final String prefix = segments[0] + ":" + segments[1] + ":";
        final String suffix = segments[2];

        if (TEAMS_USER_ANONYMOUS_PREFIX.equals(prefix)) {
            return new MicrosoftTeamsUserIdentifier(suffix, true);
        } else if (TEAMS_USER_PUBLIC_CLOUD_PREFIX.equals(prefix)) {
            return new MicrosoftTeamsUserIdentifier(suffix, false);
        } else if (TEAMS_USER_DOD_CLOUD_PREFIX.equals(prefix)) {
            return new MicrosoftTeamsUserIdentifier(suffix, false).setCloudEnvironment(CommunicationCloudEnvironment.DOD);
        } else if (TEAMS_USER_GCCH_CLOUD_PREFIX.equals(prefix)) {
            return new MicrosoftTeamsUserIdentifier(suffix, false).setCloudEnvironment(CommunicationCloudEnvironment.GCCH);
        } else if (ACS_USER_PREFIX.equals(prefix) || SPOOL_USER_PREFIX.equals(prefix) || ACS_USER_DOD_CLOUD_PREFIX.equals(prefix) || ACS_USER_GCCH_CLOUD_PREFIX.equals(prefix)) {
            return new CommunicationUserIdentifier(rawId);
        } else if (BOT_GCCH_CLOUD_GLOBAL_PREFIX.equals(prefix)) {
            return new MicrosoftBotIdentifier(suffix, CommunicationCloudEnvironment.GCCH, true);
        } else if (BOT_PUBLIC_CLOUD_PREFIX.equals(prefix)) {
            return new MicrosoftBotIdentifier(suffix, CommunicationCloudEnvironment.PUBLIC, false);
        } else if (BOT_DOD_CLOUD_GLOBAL_PREFIX.equals(prefix)) {
            return new MicrosoftBotIdentifier(suffix, CommunicationCloudEnvironment.DOD, true);
        } else if (BOT_GCCH_CLOUD_PREFIX.equals(prefix)) {
            return new MicrosoftBotIdentifier(suffix, CommunicationCloudEnvironment.GCCH, false);
        } else if (BOT_DOD_CLOUD_PREFIX.equals(prefix)) {
            return new MicrosoftBotIdentifier(suffix, CommunicationCloudEnvironment.DOD, false);
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

    /**
     * Set full id of the identifier
     * RawId is the encoded format for identifiers to store in databases or as stable keys in general.
     *
     * @param rawId full id of the identifier
     * @return CommunicationIdentifier object itself
     */
    protected CommunicationIdentifier setRawId(String rawId) {
        this.rawId = rawId;
        return this;
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }

        if (!(that instanceof CommunicationIdentifier)) {
            return false;
        }

        CommunicationIdentifier thatId = (CommunicationIdentifier) that;
        return this.getRawId().equals(thatId.getRawId());
    }

    @Override
    public int hashCode() {
        return getRawId().hashCode();
    }
}
