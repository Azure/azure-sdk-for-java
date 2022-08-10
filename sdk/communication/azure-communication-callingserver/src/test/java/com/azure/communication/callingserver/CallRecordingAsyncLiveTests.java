package com.azure.communication.callingserver;

import com.azure.communication.callingserver.models.CallingServerErrorException;
import com.azure.communication.callingserver.models.RecordingStatus;
import com.azure.communication.callingserver.models.RecordingStatusResponse;
import com.azure.communication.callingserver.models.ServerCallLocator;
import com.azure.core.http.HttpClient;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CallRecordingAsyncLiveTests extends CallingServerTestBase {

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(
        named = "SKIP_LIVE_TEST",
        matches = "(?i)(true)",
        disabledReason = "Requires human intervention")
    public void recordingOperations(HttpClient httpClient) {
        CallAutomationAsyncClient client = getCallingServerClientUsingConnectionString(httpClient)
            .addPolicy((context, next) -> logHeaders("recordingOperationsAsync", next))
            .buildAsyncClient();

        try {
            String ngrok = "https://localhost";
            String serverCallId = "serverCallId";
            CallRecordingAsync callRecording = client.getCallRecordingAsync();
            RecordingStatusResponse recordingResponse = callRecording.startRecording(
                new ServerCallLocator(serverCallId),
                new URI(ngrok))
                .block();
            assertNotNull(recordingResponse);
            String recordingId = recordingResponse.getRecordingId();
            assertNotNull(recordingId);

            recordingResponse = callRecording.getRecordingState(recordingId).block();
            assertNotNull(recordingResponse);
            assertEquals(RecordingStatus.ACTIVE, recordingResponse.getRecordingStatus());

            callRecording.pauseRecording(recordingId);
            recordingResponse = callRecording.getRecordingState(recordingId).block();
            assertNotNull(recordingResponse);
            assertEquals(RecordingStatus.INACTIVE, recordingResponse.getRecordingStatus());

            callRecording.resumeRecording(recordingId);
            recordingResponse = callRecording.getRecordingState(recordingId).block();
            assertNotNull(recordingResponse);
            assertEquals(RecordingStatus.ACTIVE, recordingResponse.getRecordingStatus());

            callRecording.stopRecording(recordingId).block();
            assertThrows(CallingServerErrorException.class, () -> callRecording.getRecordingState(recordingId).block());
        } catch (Exception ex) {
            System.out.println(ex);
        }
    }
}
