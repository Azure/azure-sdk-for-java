// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.identity;

import org.junit.jupiter.params.provider.Arguments;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static com.azure.communication.identity.models.CommunicationTokenScope.CHAT;
import static com.azure.communication.identity.models.CommunicationTokenScope.VOIP;

public class TokenScopeTestHelper {

    /**
     * Generates various combination of token scopes for testing getToken() & createUserAndToken() methods.
     *
     * @return A stream of options to parameterized test.
     */
    static Stream<Arguments> getTokenScopes() {
        List<Arguments> argumentsList = new ArrayList<>();
        argumentsList.add(Arguments.of("ChatScope", Arrays.asList(CHAT)));
        argumentsList.add(Arguments.of("VoipScope", Arrays.asList(VOIP)));
        argumentsList.add(Arguments.of("MultipleScopes", Arrays.asList(CHAT, VOIP)));
        return argumentsList.stream();
    }
}
