// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.identity;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.provider.Arguments;

import static com.azure.communication.identity.models.CommunicationTokenScope.*;
import static java.util.Arrays.asList;

@SuppressWarnings("all")
public class TokenScopeTestHelper {

    /**
     * Generates various combination of token scopes for testing getToken() & createUserAndToken() methods.
     *
     * @return A stream of options to parameterized test.
     */
    static Stream<Arguments> getTokenScopes() {
        List<Arguments> argumentsList = new ArrayList<>();
        argumentsList.add(Arguments.of("ChatScope", asList(CHAT)));
        argumentsList.add(Arguments.of("VoipScope", asList(VOIP)));
        argumentsList.add(Arguments.of("ChatJoinScope", asList(CHAT_JOIN)));
        argumentsList.add(Arguments.of("ChatJoinLimitedScope", asList(CHAT_JOIN_LIMITED)));
        argumentsList.add(Arguments.of("VoipJoinScope", asList(VOIP_JOIN)));
        argumentsList.add(Arguments.of("ChatVoipScopes", asList(CHAT, VOIP)));
        argumentsList.add(Arguments.of("AllChatScopes", asList(CHAT, CHAT_JOIN, CHAT_JOIN_LIMITED)));
        argumentsList.add(Arguments.of("AllVoipScopes", asList(VOIP, VOIP_JOIN)));
        argumentsList.add(Arguments.of("ChatJoinVoipJoinScopes", asList(CHAT_JOIN, VOIP_JOIN)));
        return argumentsList.stream();
    }
}
