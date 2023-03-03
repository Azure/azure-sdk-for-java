// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.common;

import com.azure.core.util.CoreUtils;

/**
 * Common communication identifier for Communication Services
 */
public abstract class CommunicationIdentifier {

    /**
     * Prefix for a phone number.
     */
    static final String PHONE_NUMBER = "4:";

    /**
     * Prefix for a bot.
     */
    static final String BOT_GLOBAL = "28:";

    /**
     * Prefix for a bot with public cloud.
     */
    static final String BOT_PUBLIC_CLOUD = "28:orgid:";

    /**
     * Prefix for a bot with DOD cloud.
     */
    static final String BOT_DOD_CLOUD = "28:dod:";

    /**
     * Prefix for a global bot with DOD cloud.
     */
    static final String BOT_DOD_CLOUD_GLOBAL = "28:dod-global:";

    /**
     * Prefix for a bot with GCCH cloud.
     */
    static final String BOT_GCCH_CLOUD = "28:gcch:";

    /**
     * Prefix for a global bot with GCCH cloud.
     */
    static final String BOT_GCCH_CLOUD_GLOBAL = "28:gcch-global:";

    /**
     * Prefix for an anonymous Teams user.
     */
    static final String TEAMS_USER_ANONYMOUS = "8:teamsvisitor:";

    /**
     * Prefix for a Teams user with public cloud.
     */
    static final String TEAMS_USER_PUBLIC_CLOUD = "8:orgid:";

    /**
     * Prefix for a Teams user with DOD cloud.
     */
    static final String TEAMS_USER_DOD_CLOUD = "8:dod:";

    /**
     * Prefix for a Teams user with GCCH cloud.
     */
    static final String TEAMS_USER_GCCH_CLOUD = "8:gcch:";

    /**
     * Prefix for an ACS user.
     */
    static final String ACS_USER = "8:acs:";

    /**
     * Prefix for an ACS user with DOD cloud.
     */
    static final String ACS_USER_DOD_CLOUD = "8:dod-acs:";

    /**
     * Prefix for an ACS user with GCCH cloud.
     */
    static final String ACS_USER_GCCH_CLOUD = "8:gcch-acs:";

    /**
     * Prefix for a Spool user.
     */
    static final String SPOOL_USER = "8:spool:";

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

        if (rawId.startsWith(PHONE_NUMBER)) {
            return new PhoneNumberIdentifier(rawId.substring(PHONE_NUMBER.length()));
        }
        final String[] segments = rawId.split(":");
        if (segments.length != 3) {
            if (segments.length == 2 && rawId.startsWith(BOT_GLOBAL)) {
                return new MicrosoftBotIdentifier(segments[1], CommunicationCloudEnvironment.PUBLIC, true);
            }
            return new UnknownIdentifier(rawId);
        }

        final String prefix = segments[0] + ":" + segments[1] + ":";
        final String suffix = rawId.substring(prefix.length());

        if (TEAMS_USER_ANONYMOUS.equals(prefix)) {
            return new MicrosoftTeamsUserIdentifier(suffix, true);
        } else if (TEAMS_USER_PUBLIC_CLOUD.equals(prefix)) {
            return new MicrosoftTeamsUserIdentifier(suffix, false);
        } else if (TEAMS_USER_DOD_CLOUD.equals(prefix)) {
            return new MicrosoftTeamsUserIdentifier(suffix, false).setCloudEnvironment(CommunicationCloudEnvironment.DOD);
        } else if (TEAMS_USER_GCCH_CLOUD.equals(prefix)) {
            return new MicrosoftTeamsUserIdentifier(suffix, false).setCloudEnvironment(CommunicationCloudEnvironment.GCCH);
        } else if (ACS_USER.equals(prefix) || SPOOL_USER.equals(prefix) || ACS_USER_DOD_CLOUD.equals(prefix) || ACS_USER_GCCH_CLOUD.equals(prefix)) {
            return new CommunicationUserIdentifier(rawId);
        } else if (BOT_GCCH_CLOUD_GLOBAL.equals(prefix)) {
            return new MicrosoftBotIdentifier(suffix, CommunicationCloudEnvironment.GCCH,true);
        } else if (BOT_PUBLIC_CLOUD.equals(prefix)) {
            return new MicrosoftBotIdentifier(suffix, CommunicationCloudEnvironment.PUBLIC, false);
        } else if (BOT_DOD_CLOUD_GLOBAL.equals(prefix)) {
            return new MicrosoftBotIdentifier(suffix, CommunicationCloudEnvironment.DOD, true);
        } else if (BOT_GCCH_CLOUD.equals(prefix)) {
            return new MicrosoftBotIdentifier(suffix, CommunicationCloudEnvironment.GCCH, false);
        } else if (BOT_DOD_CLOUD.equals(prefix)) {
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
