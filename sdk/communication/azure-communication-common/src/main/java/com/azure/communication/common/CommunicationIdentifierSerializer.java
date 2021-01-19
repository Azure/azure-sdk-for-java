package com.azure.communication.common;

import java.util.Objects;

class CommunicationIdentifierSerializer {
    public static CommunicationIdentifier Deserialize(CommunicationIdentifierModel identifier)
    {
        var id = identifier.getId();
        var kind = identifier.getKind();

        if (kind == CommunicationIdentifierKind.COMMUNICATION_USER) {
            Objects.requireNonNull(id);
            return new CommunicationUserIdentifier(id);
        }

        if (kind == CommunicationIdentifierKind.CALLING_APPLICATION) {
            Objects.requireNonNull(id);
            return new CallingApplicationIdentifier(id);
        }

        if (kind == CommunicationIdentifierKind.PHONE_NUMBER) {
            Objects.requireNonNull(identifier.getPhoneNumber());
            return new PhoneNumberIdentifier(identifier.getPhoneNumber());
        }

        if (kind == CommunicationIdentifierKind.MICROSOFT_TEAMS_USER) {
            Objects.requireNonNull(identifier.getMicrosoftTeamsUserId());
            return new MicrosoftTeamsUserIdentifier(identifier.getMicrosoftTeamsUserId(), identifier.isAnonymous());
        }

        return new UnknownIdentifier(id);
    }

    public static CommunicationIdentifierModel Serialize(CommunicationIdentifier identifier)
    {
        if (identifier.getClass().equals(CommunicationUserIdentifier.class))
            return new CommunicationIdentifierModel()
                .setKind(CommunicationIdentifierKind.COMMUNICATION_USER)
                .setId(((CommunicationUserIdentifier)identifier).getId());

        if (identifier.getClass().equals(CallingApplicationIdentifier.class))
            return new CommunicationIdentifierModel()
                .setKind(CommunicationIdentifierKind.CALLING_APPLICATION)
                .setId(((CallingApplicationIdentifier)identifier).getId());

        if (identifier.getClass().equals(PhoneNumberIdentifier.class))
            return new CommunicationIdentifierModel()
                .setKind(CommunicationIdentifierKind.PHONE_NUMBER)
                .setId(((PhoneNumberIdentifier)identifier).getValue());

        if (identifier.getClass().equals(MicrosoftTeamsUserIdentifier.class))
            return new CommunicationIdentifierModel()
                .setKind(CommunicationIdentifierKind.MICROSOFT_TEAMS_USER)
                .setId(((MicrosoftTeamsUserIdentifier)identifier).getUserId())
                .setIsAnonymous(((MicrosoftTeamsUserIdentifier)identifier).isAnonymous());

        return new CommunicationIdentifierModel()
            .setKind(CommunicationIdentifierKind.UNKNOWN)
            .setId(((UnknownIdentifier)identifier).getId());
    }
}
