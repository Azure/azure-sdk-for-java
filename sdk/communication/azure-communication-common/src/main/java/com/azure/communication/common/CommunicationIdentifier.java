// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.common;

import com.azure.core.util.CoreUtils;

/**
 * Common communication identifier for Communication Services
 */
public abstract class CommunicationIdentifier {

    static final String PHONE_NUMBER_PREFIX = "4:";

    static final String TEAMS_APP_PUBLIC_CLOUD_PREFIX = "28:orgid:";

    static final String TEAMS_APP_DOD_CLOUD_PREFIX = "28:dod:";

    static final String TEAMS_APP_GCCH_CLOUD_PREFIX = "28:gcch:";

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
     * Creates a new instance of {@link CommunicationIdentifier}.
     */
    public CommunicationIdentifier() {
    }

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
            return new UnknownIdentifier(rawId);
        }

        final String prefix = segments[0] + ":" + segments[1] + ":";
        final String suffix = segments[2];

        switch (prefix) {
            case TEAMS_USER_ANONYMOUS_PREFIX:
                return new MicrosoftTeamsUserIdentifier(suffix, true);

            case TEAMS_USER_PUBLIC_CLOUD_PREFIX:
                return new MicrosoftTeamsUserIdentifier(suffix, false);

            case TEAMS_USER_DOD_CLOUD_PREFIX:
                return new MicrosoftTeamsUserIdentifier(suffix, false)
                    .setCloudEnvironment(CommunicationCloudEnvironment.DOD);

            case TEAMS_USER_GCCH_CLOUD_PREFIX:
                return new MicrosoftTeamsUserIdentifier(suffix, false)
                    .setCloudEnvironment(CommunicationCloudEnvironment.GCCH);

            case SPOOL_USER_PREFIX:
                return new CommunicationUserIdentifier(rawId);

            case ACS_USER_PREFIX:
            case ACS_USER_DOD_CLOUD_PREFIX:
            case ACS_USER_GCCH_CLOUD_PREFIX:
                return tryCreateTeamsExtensionUserOrCommunicationUser(prefix, suffix, rawId);

            case TEAMS_APP_PUBLIC_CLOUD_PREFIX:
                return new MicrosoftTeamsAppIdentifier(suffix, CommunicationCloudEnvironment.PUBLIC);

            case TEAMS_APP_GCCH_CLOUD_PREFIX:
                return new MicrosoftTeamsAppIdentifier(suffix, CommunicationCloudEnvironment.GCCH);

            case TEAMS_APP_DOD_CLOUD_PREFIX:
                return new MicrosoftTeamsAppIdentifier(suffix, CommunicationCloudEnvironment.DOD);

            default:
                return new UnknownIdentifier(rawId);
        }
    }

    private static CommunicationIdentifier tryCreateTeamsExtensionUserOrCommunicationUser(String prefix, String suffix,
        String rawId) {
        String[] segments = suffix.split("_");
        if (segments.length != 3) {
            return new CommunicationUserIdentifier(rawId);
        }

        String resourceId = segments[0];
        String tenantId = segments[1];
        String userId = segments[2];

        CommunicationCloudEnvironment cloud;
        switch (prefix) {
            case ACS_USER_PREFIX:
                cloud = CommunicationCloudEnvironment.PUBLIC;
                break;

            case ACS_USER_DOD_CLOUD_PREFIX:
                cloud = CommunicationCloudEnvironment.DOD;
                break;

            case ACS_USER_GCCH_CLOUD_PREFIX:
                cloud = CommunicationCloudEnvironment.GCCH;
                break;

            default:
                throw new IllegalArgumentException("Invalid MRI");
        }

        return new TeamsExtensionUserIdentifier(userId, tenantId, resourceId).setCloudEnvironment(cloud);
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
