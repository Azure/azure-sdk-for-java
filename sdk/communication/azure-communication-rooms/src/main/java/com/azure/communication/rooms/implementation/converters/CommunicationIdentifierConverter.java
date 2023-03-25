// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.rooms.implementation.converters;

import com.azure.communication.rooms.implementation.models.CommunicationIdentifierModel;
import com.azure.communication.rooms.implementation.models.CommunicationUserIdentifierModel;

import com.azure.communication.common.CommunicationIdentifier;
import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.communication.common.UnknownIdentifier;

import java.util.ArrayList;
import java.util.Objects;


public class CommunicationIdentifierConverter {
    /**
     * Convert rawId into CommunicationIdentifier
     * @param rawId an acs rawID to be converted
     * @return CommunicationIdentifier
     */
    public static CommunicationIdentifier convert(String rawId) {
        Objects.requireNonNull(rawId);
        return new CommunicationUserIdentifier(rawId);
    }

    private static void assertSingleType(CommunicationIdentifierModel identifier) {
        CommunicationUserIdentifierModel communicationUser = identifier.getCommunicationUser();

        ArrayList<String> presentProperties = new ArrayList<String>();
        if (communicationUser != null) {
            presentProperties.add(communicationUser.getClass().getName());
        }

        if (presentProperties.size() > 1) {
            throw new IllegalArgumentException(String.format("Only one of the identifier models in %s should be present.",
                String.join(", ", presentProperties)));
        }
    }

    /**
     * Convert CommunicationIdentifier into rawId
     * @param identifier CommunicationIdentifier object to be converted
     * @return CommunicationIdentifierModel
     * @throws IllegalArgumentException when identifier is an unknown class derived from
     *          CommunicationIdentifier
     */
    public static String convert(CommunicationIdentifier identifier) {

        if (identifier instanceof CommunicationUserIdentifier) {
            return ((CommunicationUserIdentifier) identifier).getId();
        }

        if (identifier instanceof UnknownIdentifier) {
            return ((UnknownIdentifier) identifier).getId();
        }

        throw new IllegalArgumentException(String.format("Unknown identifier class '%s'", identifier.getClass().getName()));
    }

}
