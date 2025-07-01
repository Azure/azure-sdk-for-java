// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.agents.persistent;

import com.azure.ai.agents.persistent.models.MessageContent;
import com.azure.ai.agents.persistent.models.MessageDeltaImageFileContent;
import com.azure.ai.agents.persistent.models.MessageDeltaTextContent;
import com.azure.ai.agents.persistent.models.MessageImageFileContent;
import com.azure.ai.agents.persistent.models.MessageTextContent;
import com.azure.ai.agents.persistent.models.RunStatus;
import com.azure.ai.agents.persistent.models.StreamMessageUpdate;
import com.azure.ai.agents.persistent.models.ThreadMessage;
import com.azure.ai.agents.persistent.models.ThreadRun;
import com.azure.core.http.rest.PagedIterable;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdkBuilder;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class SampleUtils {

    public static void printRunMessages(MessagesClient messagesClient, String threadId) {

        // BEGIN: com.azure.ai.agents.persistent.SampleUtils.printRunMessages

        PagedIterable<ThreadMessage> runMessages = messagesClient.listMessages(threadId);
        for (ThreadMessage message : runMessages) {
            System.out.print(String.format("%1$s - %2$s : ", message.getCreatedAt(), message.getRole()));
            for (MessageContent contentItem : message.getContent()) {
                if (contentItem instanceof MessageTextContent) {
                    System.out.print((((MessageTextContent) contentItem).getText().getValue()));
                } else if (contentItem instanceof MessageImageFileContent) {
                    String imageFileId = (((MessageImageFileContent) contentItem).getImageFile().getFileId());
                    System.out.print("Image from ID: " + imageFileId);
                }
                System.out.println();
            }
        }

        // END: com.azure.ai.agents.persistent.SampleUtils.printRunMessages
    }

    public static Mono<Void> printRunMessagesAsync(MessagesAsyncClient messagesAsyncClient, String threadId) {
        // BEGIN: com.azure.ai.agents.persistent.SampleUtils.printRunMessagesAsync

        return messagesAsyncClient.listMessages(threadId)
            .doOnNext(message -> {
                System.out.print(String.format("%1$s - %2$s : ", message.getCreatedAt(), message.getRole()));
                message.getContent().forEach(contentItem -> {
                    if (contentItem instanceof MessageTextContent) {
                        System.out.print((((MessageTextContent) contentItem).getText().getValue()));
                    } else if (contentItem instanceof MessageImageFileContent) {
                        String imageFileId = (((MessageImageFileContent) contentItem).getImageFile().getFileId());
                        System.out.print("Image from ID: " + imageFileId);
                    }
                    System.out.println();
                });
            })
            .then();

        // END: com.azure.ai.agents.persistent.SampleUtils.printRunMessagesAsync
    }


    public static void printStreamUpdate(StreamMessageUpdate messageUpdate) {

        // BEGIN: com.azure.ai.agents.persistent.SampleUtils.printStreamUpdate

        messageUpdate.getMessage().getDelta().getContent().stream().forEach(delta -> {
            if (delta instanceof MessageDeltaImageFileContent) {
                MessageDeltaImageFileContent imgContent = (MessageDeltaImageFileContent) delta;
                System.out.println("Image fileId: " + imgContent.getImageFile().getFileId());
            } else if (delta instanceof MessageDeltaTextContent) {
                MessageDeltaTextContent textContent = (MessageDeltaTextContent) delta;
                System.out.print(textContent.getText().getValue());
            }
        });

        // END: com.azure.ai.agents.persistent.SampleUtils.printStreamUpdate
    }

    public static Mono<Void> printStreamUpdateAsync(StreamMessageUpdate messageUpdate) {
        // BEGIN: com.azure.ai.agents.persistent.SampleUtils.printStreamUpdateAsync

        return Mono.fromRunnable(() -> {
            messageUpdate.getMessage().getDelta().getContent().stream().forEach(delta -> {
                if (delta instanceof MessageDeltaImageFileContent) {
                    MessageDeltaImageFileContent imgContent = (MessageDeltaImageFileContent) delta;
                    System.out.println("Image fileId: " + imgContent.getImageFile().getFileId());
                } else if (delta instanceof MessageDeltaTextContent) {
                    MessageDeltaTextContent textContent = (MessageDeltaTextContent) delta;
                    System.out.print(textContent.getText().getValue());
                }
            });
        });

        // END: com.azure.ai.agents.persistent.SampleUtils.printStreamUpdateAsync
    }

    public static void waitForRunCompletion(String threadId, ThreadRun threadRun, RunsClient runsClient)
        throws InterruptedException {

        // BEGIN: com.azure.ai.agents.persistent.SampleUtils.waitForRunCompletion

        do {
            Thread.sleep(500);
            threadRun = runsClient.getRun(threadId, threadRun.getId());
        }
        while (
            threadRun.getStatus() == RunStatus.QUEUED
                || threadRun.getStatus() == RunStatus.IN_PROGRESS
                || threadRun.getStatus() == RunStatus.REQUIRES_ACTION);

        if (threadRun.getStatus() == RunStatus.FAILED) {
            System.out.println(threadRun.getLastError().getMessage());
        }

        // END: com.azure.ai.agents.persistent.SampleUtils.waitForRunCompletion
    }

    public static Mono<ThreadRun> waitForRunCompletionAsync(String threadId, ThreadRun threadRun, RunsAsyncClient runsAsyncClient) {
        // BEGIN: com.azure.ai.agents.persistent.SampleUtils.waitForRunCompletionAsync

        return Mono.defer(() -> runsAsyncClient.getRun(threadId, threadRun.getId()))
            .flatMap(run -> {
                if (run.getStatus() == RunStatus.QUEUED
                    || run.getStatus() == RunStatus.IN_PROGRESS
                    || run.getStatus() == RunStatus.REQUIRES_ACTION) {
                    return Mono.delay(java.time.Duration.ofMillis(500))
                        .then(waitForRunCompletionAsync(threadId, run, runsAsyncClient));
                } else {
                    if (run.getStatus() == RunStatus.FAILED && run.getLastError() != null) {
                        System.out.println(run.getLastError().getMessage());
                    }
                    return Mono.just(run);
                }
            });

        // END: com.azure.ai.agents.persistent.SampleUtils.waitForRunCompletionAsync
    }

    @NotNull
    public static void cleanUpResources(AtomicReference<String> threadId, ThreadsAsyncClient threadsAsyncClient, AtomicReference<String> agentId, PersistentAgentsAdministrationAsyncClient administrationAsyncClient) {
        // Always clean up resources regardless of success or failure
        System.out.println("Cleaning up resources...");

        // Clean up thread if created
        if (threadId.get() != null) {
            threadsAsyncClient.deleteThread(threadId.get())
                .doOnSuccess(ignored -> System.out.println("Thread deleted: " + threadId.get()))
                .doOnError(error -> System.err.println("Failed to delete thread: " + error.getMessage()))
                .subscribe();
        }

        // Clean up agent if created
        if (agentId.get() != null) {
            administrationAsyncClient.deleteAgent(agentId.get())
                .doOnSuccess(ignored -> System.out.println("Agent deleted: " + agentId.get()))
                .doOnError(error -> System.err.println("Failed to delete agent: " + error.getMessage()))
                .subscribe();
        }
    }

    /**
     *  With the below configuration, the runtime sends OpenTelemetry data to the local OTLP/gRPC endpoint.
     *  Change to your endpoint address, "http://localhost:4317" is used by default
     *      `properties.put("otel.exporter.otlp.endpoint", "http://localhost:4317");`
     *  For debugging purposes, Aspire Dashboard can be run locally that listens for telemetry data and offer a UI for viewing the collected data.
     *  To run Aspire Dashboard, run the following docker command:
     *      `docker run --rm -p 18888:18888 -p 4317:18889 -p 4318:18890 --name aspire-dashboard mcr.microsoft.com/dotnet/nightly/aspire-dashboard:latest`
     *  The output of the docker command includes a link to the dashboard. For more information on Aspire Dashboard,
     *      see <a href="https://learn.microsoft.com/dotnet/aspire/fundamentals/dashboard/overview">Aspire Dashboard</a>
     *  See <a href="https://learn.microsoft.com/azure/developer/java/sdk/tracing">documentation</a> for more information on tracing with Azure SDK.
     */
    public static OpenTelemetrySdk configureOpenTelemetryEndpointTracing() {
        final AutoConfiguredOpenTelemetrySdkBuilder sdkBuilder = AutoConfiguredOpenTelemetrySdk.builder();
        return sdkBuilder
            .addPropertiesSupplier(() -> {
                final Map<String, String> properties = new HashMap<>();
                properties.put("otel.service.name", "agents-persistent");
                return properties;
            })
            .setResultAsGlobal()
            .build()
            .getOpenTelemetrySdk();
    }

    public static OpenTelemetrySdk configureOpenTelemetryConsoleTracing() {
        final AutoConfiguredOpenTelemetrySdkBuilder sdkBuilder = AutoConfiguredOpenTelemetrySdk.builder();
        return sdkBuilder
            .addPropertiesSupplier(() -> {
                final Map<String, String> properties = new HashMap<>();
                properties.put("otel.service.name", "agents-persistent");
                properties.put("otel.traces.exporter", "logging");
                return properties;
            })
            .setResultAsGlobal()
            .build()
            .getOpenTelemetrySdk();
    }
}
