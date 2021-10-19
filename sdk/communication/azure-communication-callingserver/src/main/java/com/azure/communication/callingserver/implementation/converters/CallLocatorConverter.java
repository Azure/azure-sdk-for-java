// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.implementation.converters;

import java.util.ArrayList;

import com.azure.communication.callingserver.implementation.models.CallLocatorModel;
import com.azure.communication.callingserver.models.CallLocator;
import com.azure.communication.callingserver.models.GroupCallLocator;
import com.azure.communication.callingserver.models.ServerCallLocator;

/**
 * A converter between {@link CallLocatorModel} and {@link CallLocator}.
 */
public class CallLocatorConverter {

    /**
     * Maps from {@link CallLocatorModel} to {@link CallLocator}.
     */
    public static CallLocator convert(CallLocatorModel callLocatorModel) {
        if (callLocatorModel == null) {
            return null;
        }

        assertSingleType(callLocatorModel);

        if (callLocatorModel.getServerCallId() != null) {
            return new ServerCallLocator(callLocatorModel.getServerCallId());
        }

        if (callLocatorModel.getGroupCallId() != null) {
            return new GroupCallLocator(callLocatorModel.getGroupCallId());
        }

        throw new IllegalArgumentException(String.format("Unknown callLocatorModel class '%s'", callLocatorModel.getClass().getName()));
    }

    /**
     * Maps from {@link CallLocatorModel} to {@link CallLocator}.
     */
    public static CallLocatorModel convert(CallLocator callLocator)
        throws IllegalArgumentException {

        if (callLocator == null) {
            return null;
        }

        if (callLocator instanceof ServerCallLocator) {
            ServerCallLocator serverCallLocator = (ServerCallLocator) callLocator;
            return new CallLocatorModel()
                .setServerCallId(serverCallLocator.getServerCallId());
        }

        if (callLocator instanceof GroupCallLocator) {
            GroupCallLocator groupCallLocator = (GroupCallLocator) callLocator;
            return new CallLocatorModel()
                .setGroupCallId(groupCallLocator.getGroupCallId());
        }

        throw new IllegalArgumentException(String.format("Unknown identifier class '%s'", callLocator.getClass().getName()));
    }

    private static void assertSingleType(CallLocatorModel identifier) {
        String serverCallId = identifier.getServerCallId();
        String groupCallId = identifier.getGroupCallId();

        ArrayList<String> presentProperties = new ArrayList<>();
        if (serverCallId != null) {
            presentProperties.add(ServerCallLocator.class.getName());
        }
        if (groupCallId != null) {
            presentProperties.add(GroupCallLocator.class.getName());
        }

        if (presentProperties.size() > 1) {
            throw new IllegalArgumentException(
                String.format(
                    "Only one of the locators in %s should be present.",
                    String.join(", ", presentProperties)));
        }
    }
}
