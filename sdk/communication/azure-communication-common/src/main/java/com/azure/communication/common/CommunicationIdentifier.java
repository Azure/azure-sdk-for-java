// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.common;

import com.azure.core.util.CoreUtils;

/**
 * Common communication identifier for Communication Services
 */
public abstract class CommunicationIdentifier {

    protected static final String PhoneNumber = "4:";
    protected static final String Bot = "28:";
    protected static final String BotPublicCloud = "28:orgid:";
    protected static final String BotDodCloud = "28:dod:";
    protected static final String BotDodCloudGlobal = "28:dod-global:";
    protected static final String BotGcchCloud = "28:gcch:";
    protected static final String BotGcchCloudGlobal = "28:gcch-global:";
    protected static final String TeamUserAnonymous = "8:teamsvisitor:";
    protected static final String TeamUserPublicCloud = "8:orgid:";
    protected static final String TeamUserDodCloud = "8:dod:";
    protected static final String TeamUserGcchCloud = "8:gcch:";
    protected static final String AcsUser = "8:acs:";
    protected static final String AcsUserDodCloud = "8:dod-acs:";
    protected static final String AcsUserGcchCloud = "8:gcch-acs:";
    protected static final String SpoolUser = "8:spool:";

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

        if (rawId.startsWith(PhoneNumber)) {
            return new PhoneNumberIdentifier(rawId.substring("4:".length()));
        }
        final String[] segments = rawId.split(":");
        if (segments.length < 3) {
            if(segments.length == 2 && segments[0].equals("28")){
               return new MicrosoftBotIdentifier(segments[1], true).setCloudEnvironment(CommunicationCloudEnvironment.PUBLIC);
            }
            return new UnknownIdentifier(rawId);
        }

        final String prefix = segments[0] + ":" + segments[1] + ":";
        final String suffix = rawId.substring(prefix.length());

        if (TeamUserAnonymous.equals(prefix)) {
            return new MicrosoftTeamsUserIdentifier(suffix, true);
        } else if (TeamUserPublicCloud.equals(prefix)) {
            return new MicrosoftTeamsUserIdentifier(suffix, false);
        } else if (TeamUserDodCloud.equals(prefix)) {
            return new MicrosoftTeamsUserIdentifier(suffix, false).setCloudEnvironment(CommunicationCloudEnvironment.DOD);
        } else if (TeamUserGcchCloud.equals(prefix)) {
            return new MicrosoftTeamsUserIdentifier(suffix, false).setCloudEnvironment(CommunicationCloudEnvironment.GCCH);
        } else if (AcsUser.equals(prefix) || SpoolUser.equals(prefix) || AcsUserDodCloud.equals(prefix) || AcsUserGcchCloud.equals(prefix)) {
            return new CommunicationUserIdentifier(rawId);
        } else if (BotGcchCloudGlobal.equals(prefix)) {
            return new MicrosoftBotIdentifier(suffix, true).setCloudEnvironment(CommunicationCloudEnvironment.GCCH);
        } else if (BotPublicCloud.equals(prefix)) {
            return new MicrosoftBotIdentifier(suffix, false).setCloudEnvironment(CommunicationCloudEnvironment.PUBLIC);
        } else if (BotDodCloudGlobal.equals(prefix)) {
            return new MicrosoftBotIdentifier(suffix, true).setCloudEnvironment(CommunicationCloudEnvironment.DOD);
        } else if (BotGcchCloud.equals(prefix)) {
            return new MicrosoftBotIdentifier(suffix, false).setCloudEnvironment(CommunicationCloudEnvironment.GCCH);
        } else if (BotDodCloud.equals(prefix)) {
            return new MicrosoftBotIdentifier(suffix, false).setCloudEnvironment(CommunicationCloudEnvironment.DOD);
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
