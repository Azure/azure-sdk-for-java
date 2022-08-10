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

public class CallRecordingLiveTests extends CallingServerTestBase {

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(
        named = "SKIP_LIVE_TEST",
        matches = "(?i)(true)",
        disabledReason = "Requires human intervention")
    public void recordingOperations(HttpClient httpClient) {
        CallAutomationClient client = getCallingServerClientUsingConnectionString(httpClient)
            .addPolicy((context, next) -> logHeaders("recordingOperations", next))
            .buildClient();

        try {
            String ngrok = "https://localhost";
            String serverCallId = "serverCallId";
            CallRecording callRecording = client.getCallRecording();
            RecordingStatusResponse recordingResponse = callRecording.startRecording(
                new ServerCallLocator(serverCallId),
                new URI(ngrok));
            assertNotNull(recordingResponse);
            String recordingId = recordingResponse.getRecordingId();
            assertNotNull(recordingId);

            recordingResponse = callRecording.getRecordingState(recordingId);
            assertNotNull(recordingResponse);
            assertEquals(RecordingStatus.ACTIVE, recordingResponse.getRecordingStatus());

            callRecording.pauseRecording(recordingId);
            recordingResponse = callRecording.getRecordingState(recordingId);
            assertNotNull(recordingResponse);
            assertEquals(RecordingStatus.INACTIVE, recordingResponse.getRecordingStatus());

            callRecording.resumeRecording(recordingId);
            recordingResponse = callRecording.getRecordingState(recordingId);
            assertNotNull(recordingResponse);
            assertEquals(RecordingStatus.ACTIVE, recordingResponse.getRecordingStatus());

            callRecording.stopRecording(recordingId);
            assertThrows(CallingServerErrorException.class, () -> callRecording.getRecordingState(recordingId));
        } catch (Exception ex) {
            System.out.println(ex);
        }
    }
}
