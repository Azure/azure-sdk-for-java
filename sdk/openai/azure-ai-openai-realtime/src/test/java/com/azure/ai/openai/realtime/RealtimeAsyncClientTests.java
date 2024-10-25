package com.azure.ai.openai.realtime;

import com.azure.ai.openai.realtime.models.RealtimeServerEventSessionCreated;
import com.azure.ai.openai.realtime.models.RealtimeServerEventSessionUpdated;
import com.azure.ai.openai.realtime.models.RealtimeServerEventType;
import com.azure.ai.openai.realtime.utils.FileUtils;
import com.azure.ai.openai.realtime.utils.RealtimeEventHandler;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

public class RealtimeAsyncClientTests extends RealtimeClientTestBase {

    private RealtimeAsyncClient client;

    @Test
    public void testAlawSendAudio() {
        client = getRealtimeClientBuilder(null, OpenAIServiceVersion.V2024_10_01_PREVIEW)
                .buildAsyncClient();

        client.start().block();
        StepVerifier.create(client.getServerEvents())
                .assertNext(event -> {
                    System.out.println("event type: " + event.getType());
                    assertInstanceOf(RealtimeServerEventSessionCreated.class, event);
                })
                .then(() -> {
                    client.sendMessage(RealtimeEventHandler.sessionUpdate()).block();
                })
                .assertNext(event -> {
                    System.out.println("event type: " + event.getType());
                    assertInstanceOf(RealtimeServerEventSessionUpdated.class, event);
                })
                .then(() -> {
                    FileUtils.sendAudioFile(client, FileUtils.openResourceFile("audio_weather_alaw.wav")).block();
                })
                .thenConsumeWhile(
                    event -> event.getType() != RealtimeServerEventType.RESPONSE_DONE,
                    event -> System.out.println("event type: " + event.getType()))
                .thenRequest(1) // Requesting the last expected element RESPONSE_DONE
                .then(() -> client.stop().block())
                .verifyComplete();
    }
}
