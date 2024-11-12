// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.realtime;

import com.azure.ai.openai.realtime.implementation.FileUtils;
import com.azure.ai.openai.realtime.implementation.RealtimeEventHandler;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.Configuration;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

public class LowLevelClient {

    public static void main(String[] args) {
        String azureOpenaiKey = Configuration.getGlobalConfiguration().get("AZURE_OPENAI_KEY");
        String endpoint = Configuration.getGlobalConfiguration().get("AZURE_OPENAI_ENDPOINT");
        String deploymentOrModelId = Configuration.getGlobalConfiguration().get("MODEL_OR_DEPLOYMENT_NAME");

        RealtimeAsyncClient client = new RealtimeClientBuilder()
                .endpoint(endpoint)
                .deploymentOrModelName(deploymentOrModelId)
                .credential(new AzureKeyCredential(azureOpenaiKey))
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
                        return FileUtils.sendAudioFileAsync(client, FileUtils.openResourceFile("audio_weather_alaw.wav"));
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
