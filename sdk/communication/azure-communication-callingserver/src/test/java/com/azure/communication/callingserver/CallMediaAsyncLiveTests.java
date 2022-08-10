package com.azure.communication.callingserver;

import com.azure.communication.callingserver.models.FileSource;
import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.Response;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class CallMediaAsyncLiveTests extends CallAutomationTestBase {
    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(
        named = "SKIP_LIVE_TEST",
        matches = "(?i)(true)",
        disabledReason = "Requires human intervention")
    public void playOperation(HttpClient httpClient) {
        String callConnectionId = "callConnectionId";
        String playAudioUrl = "https://localhost/bot-hold-music-2.wav";
        String targetUser = "targetUser";
        CallAutomationAsyncClient client = getCallingServerClientUsingConnectionString(httpClient)
            .addPolicy((context, next) -> logHeaders("recordingOperations", next))
            .buildAsyncClient();

        try {
            CallConnectionAsync callConnection = client.getCallConnectionAsync(callConnectionId);
            CallMediaAsync callMedia = callConnection.getCallMediaAsync();
            Response<Void> response = callMedia.playWithResponse(
                new FileSource().setUri(playAudioUrl).setPlaySourceId("playSourceId"),
                Collections.singletonList(new CommunicationUserIdentifier(targetUser)),
                null
            ).block();
            assertNotNull(response);
            assertEquals(202, response.getStatusCode());
        } catch (Exception ex) {
            System.out.println(ex);
        }
    }
}
