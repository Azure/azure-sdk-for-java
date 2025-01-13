// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.realtime;

import com.azure.ai.openai.realtime.utils.AudioFile;
import com.azure.ai.openai.realtime.utils.FileUtils;
import com.azure.ai.openai.realtime.implementation.RealtimeEventHandler;
import com.azure.core.credential.KeyCredential;
import com.azure.core.util.Configuration;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

/**
 * This sample demonstrates the usage of the Azure OpenAI Realtime client to interact with the OpenAI service.
 * We use a {@link Sinks.Many} to react to the need to send more user input once the service is ready for it.
 * You can see the details in the {@link RealtimeEventHandler} class, but briefly described, we issue a
 * {@link com.azure.ai.openai.realtime.models.SessionUpdateEvent} once we've received from the server a
 * {@link com.azure.ai.openai.realtime.models.SessionCreatedEvent} event.
 * We proceed to send the audio file only once we know that the session was successfully updated. This is done by listening
 * for the {@link com.azure.ai.openai.realtime.models.SessionUpdatedEvent} event.
 * Finally, once we receive the {@link com.azure.ai.openai.realtime.models.ResponseDoneEvent} event, we
 * signal the client to end the session and close the connection.
 */
public class NonAzureLowLevelClient {

    /**
     * This sample demonstrates the usage of the Azure OpenAI Realtime client to interact with the OpenAI service.
     * We use a {@link Sinks.Many} to react to the need to send more user input once the service is ready for it.
     * You can see the details in the {@link RealtimeEventHandler} class, but briefly described, we issue a
     * {@link com.azure.ai.openai.realtime.models.SessionUpdateEvent} once we've received from the server a
     * {@link com.azure.ai.openai.realtime.models.SessionCreatedEvent} event.
     * We proceed to send the audio file only once we know that the session was successfully updated. This is done by listening
     * for the {@link com.azure.ai.openai.realtime.models.SessionUpdatedEvent} event.
     * Finally, once we receive the {@link com.azure.ai.openai.realtime.models.ResponseDoneEvent} event, we
     * signal the client to end the session and close the connection.
     *
     * @param args Unused. Arguments to the program.
     */
    public static void main(String[] args) {

        String openAIKey = Configuration.getGlobalConfiguration().get("OPENAI_KEY");
        String openAIModel = Configuration.getGlobalConfiguration().get("OPENAI_MODEL");

        RealtimeAsyncClient client = new RealtimeClientBuilder()
                .credential(new KeyCredential(openAIKey))
                .deploymentOrModelName(openAIModel)
                .buildAsyncClient();

        // We create our user input requester as a reactor.Sink
        Sinks.Many<RealtimeEventHandler.UserInputRequest> requestUserInput = Sinks.many().multicast().onBackpressureBuffer();
        Disposable.Composite disposables = Disposables.composite();

        // Add our server message consumer and pass our user input requester
        disposables.add(client.getServerEvents().subscribe(realtimeServerEvent ->
                RealtimeEventHandler.consumeServerEvent(realtimeServerEvent, requestUserInput)));

        // Start the client
        disposables.add(client.start().subscribe());

        // We setup our user input sender
        disposables.add(requestUserInput.asFlux()
                .flatMap(userInputRequest -> {
                    if (userInputRequest instanceof RealtimeEventHandler.SessionUpdateRequest) {
                        return client.sendMessage(RealtimeEventHandler.sessionUpdate());
                    } else if (userInputRequest instanceof RealtimeEventHandler.SendAudioRequest) {
                        return FileUtils.sendAudioFileAsync(client, new AudioFile(FileUtils.openResourceFile("audio_weather_alaw.wav")));
                    } else if (userInputRequest instanceof RealtimeEventHandler.EndSession) {
                        return Mono.empty();
                    } else {
                        return Mono.error(new IllegalStateException("Unexpected message type"));
                    }
                }).subscribe());


        // We block here until an EndSession request is sent by our RealtimeEventHandler.consumeServerEvent handler
        requestUserInput.asFlux()
                .ofType(RealtimeEventHandler.EndSession.class)
//                .timeout(Duration.ofSeconds(5))
                .blockFirst();

        try {
            client.stop().block();
            client.close();
            disposables.dispose();
        } catch (Exception e) {
            System.out.println("Error closing client: " + e.getMessage());
        }
    }
}
