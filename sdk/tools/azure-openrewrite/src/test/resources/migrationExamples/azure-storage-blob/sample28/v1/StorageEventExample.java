// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelinePosition;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * This examples shows how to approximate the StorageEvent behavior from the track 1 SDK. It is a general translation to
 * achieve roughly the same results, but it is not an identical implementation. It may be modified to suit the use case.
 *
 * The general pattern is to create an {@link HttpPipelinePolicy} that will call the events at appropriate times. All
 * requests pass through a pipeline, so any pipeline which contains an instance of this policy will be able to invoke
 * the callbacks. Once the policy is defined, it must be set on the builder when constructing the clients.
 *
 * This sample can be run as is to demonstrate usage. Expected output is a console statement indicating the request
 * being sent, the response being received, and (in the async case) the request completing. To demonstrate the retry and
 * error handlers, uncomment the line {@code //.addPolicy(new ErrorPolicy())} when configuring the builder and run
 * again. This will print out several retries and errors. The async segment will not run in this case as the eventual
 * exception will terminate the program.
 *
 * The main areas of divergence from the original feature are:
 * - It is only possible to use the sendingRequestHandler on an async client.
 * - The callbacks do not all accept the same type as they did in the track 1 sdk and there is no StorageEvent type.
 * - The SendingRequest events come after the signature here. In track 1, they came before the signature. It would be
 * possible to create a second, near identical policy to put before the signature specifically for the SendingRequest
 * event if that behavior is desirable. The policy shown here should then be modified to not duplicate that event.
 * - Global handlers are not demonstrated here. They could be implemented in a very similar fashion by having some
 * static fields on the policy object that are called alongside the instance fields.
 */
public class StorageEventExample {

    public static void main(String[] args) {
        // Define the event handlers
        Consumer<HttpRequest> sendingRequestHandler =
            request -> System.out.println("Sending request " + request.getUrl());
        BiConsumer<HttpRequest, Integer> retryRequestHandler =
            (request, retryNumber) -> System.out.println("Retrying request. " + request.getUrl() + " Attempt number "
                + retryNumber);
        BiConsumer<HttpRequest, HttpResponse> responseReceivedHandler =
            (request, response) -> System.out.println("Received response. Request " + request.getUrl() + "\nResponse "
                + "status" + response.getStatusCode());
        BiConsumer<HttpRequest, Throwable> errorHandler =
            (request, t) -> System.out.println("Error. Request " + request.getUrl() + "\n " + t.getMessage());
        /*
        If actions specific to the request type must be taken, the consumer type parameter should correspond to the type
        returned by the api.
         */
        Consumer<Object> requestCompleteHandler =
            obj -> System.out.println("Request complete");

        // Instantiate the policy that will invoke the handlers at the proper time
        EventHandlerPolicy eventHandlerPolicy = new EventHandlerPolicy(sendingRequestHandler, retryRequestHandler,
            responseReceivedHandler, errorHandler);

        // Create clients whose pipeline contains the new policy
        BlobClientBuilder builder = new BlobClientBuilder()
            .connectionString("<connection-string>")
            .addPolicy(eventHandlerPolicy)
            .addPolicy(new ErrorPolicy())
            .containerName("<container-name>")
            .blobName("<blob-name>");
        BlobClient bc = builder.buildClient();
        BlobAsyncClient bac = builder.buildAsyncClient();

        // Use the client as usual, the handlers will now be automatically invoked at the proper times
        bc.downloadContent();
        /*
        The only way to use a requestCompleteHandler is to use the async client and set a side effect operator on the
        return value.
         */
        System.out.println("Async");
        bac.downloadWithResponse(null, null, null, false)
            .doOnNext(requestCompleteHandler)
            .block();
    }

    static class EventHandlerPolicy implements HttpPipelinePolicy {

        private final Consumer<HttpRequest> sendingRequestEvent;
        private final BiConsumer<HttpRequest, Integer> retryRequestEvent;
        private final BiConsumer<HttpRequest, HttpResponse> responseReceivedEvent;
        private final BiConsumer<HttpRequest, Throwable> errorResponseEvent;

        EventHandlerPolicy(Consumer<HttpRequest> sendingRequestEvent,
            BiConsumer<HttpRequest, Integer> retryRequestEvent,
            BiConsumer<HttpRequest, HttpResponse> responseReceivedEvent,
            BiConsumer<HttpRequest, Throwable> errorResponseEvent) {
            this.sendingRequestEvent = sendingRequestEvent;
            this.retryRequestEvent = retryRequestEvent;
            this.responseReceivedEvent = responseReceivedEvent;
            this.errorResponseEvent = errorResponseEvent;
        }

        @Override
        public Mono<HttpResponse> process(HttpPipelineCallContext httpPipelineCallContext, HttpPipelineNextPolicy httpPipelineNextPolicy) {
            HttpRequest request = httpPipelineCallContext.getHttpRequest();
            /*
            Check how many retries have gone out. Send an initial sendingRequest event or a retryRequest event as
            appropriate.
            This value is updated automatically by the retry policy before the request gets here
             */
            Optional<Object> retryOptional = httpPipelineCallContext.getData(HttpLoggingPolicy.RETRY_COUNT_CONTEXT);
            Integer retryCount = retryOptional.map(o -> (Integer) o).orElse(0);
            if (retryCount <= 1) {
                this.sendingRequestEvent.accept(request);
            } else {
                this.retryRequestEvent.accept(request, retryCount);
            }

            // Set side-effect call backs to process the event without affecting the normal request-response flow
            return httpPipelineNextPolicy.process()
                .doOnNext(response -> this.responseReceivedEvent.accept(request, response))
                .doOnError(throwable -> this.errorResponseEvent.accept(request, throwable));
        }

        @Override
        public HttpPipelinePosition getPipelinePosition() {
            // This policy must be in a position to see each retry
            return HttpPipelinePosition.PER_RETRY;
        }
    }

    /**
     * A simple policy that always returns a retryable error to demonstrate retry and error event handlers
     */
    static class ErrorPolicy implements HttpPipelinePolicy {
        @Override
        public Mono<HttpResponse> process(HttpPipelineCallContext httpPipelineCallContext, HttpPipelineNextPolicy httpPipelineNextPolicy) {
            return Mono.error(new IOException("Dummy error"));
        }

        @Override
        public HttpPipelinePosition getPipelinePosition() {
            return HttpPipelinePosition.PER_RETRY;
        }
    }
}
