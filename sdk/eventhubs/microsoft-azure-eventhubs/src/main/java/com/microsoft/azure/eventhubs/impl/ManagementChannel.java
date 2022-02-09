// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs.impl;

import com.microsoft.azure.eventhubs.OperationCancelledException;
import com.microsoft.azure.eventhubs.TimeoutException;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.messaging.AmqpValue;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.message.Message;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;

final class ManagementChannel {

    final FaultTolerantObject<RequestResponseChannel> innerChannel;

    ManagementChannel(final SessionProvider sessionProvider, final AmqpConnection connection, final String clientId, final ScheduledExecutorService executor) {

        final RequestResponseCloser closer = new RequestResponseCloser();
        final String sessionName = "mgmt-session";
        this.innerChannel = new FaultTolerantObject<>(
                new RequestResponseOpener(
                        sessionProvider,
                        clientId,
                        sessionName,
                        "mgmt",
                        ClientConstants.MANAGEMENT_ADDRESS,
                        connection,
                        executor),
                closer,
                clientId,
                sessionName);
        closer.setInnerChannel(this.innerChannel);
    }

    public CompletableFuture<Map<String, Object>> request(
        final ReactorDispatcher dispatcher,
        final Map<String, Object> request,
        final long timeoutInMillis) {
        // no body required
        final Message requestMessage = Proton.message();
        final ApplicationProperties applicationProperties = new ApplicationProperties(request);
        requestMessage.setApplicationProperties(applicationProperties);
        final CompletableFuture<Map<String, Object>> resultFuture = new CompletableFuture<Map<String, Object>>();
        try {
            // schedule client-timeout on the request
            dispatcher.invoke((int) timeoutInMillis,
                    new DispatchHandler() {
                        @Override
                        public void onEvent() {
                            final RequestResponseChannel channel = innerChannel.unsafeGetIfOpened();
                            final String errorMessage;
                            if (channel != null && channel.getState() == IOObject.IOObjectState.OPENED) {
                                final String remoteContainerId = channel.getSendLink().getSession().getConnection().getRemoteContainer();
                                errorMessage = String.format(Locale.US, "Management request timed out (%sms), after not receiving response from service. TrackingId: %s",
                                        timeoutInMillis, StringUtil.isNullOrEmpty(remoteContainerId) ? "n/a" : remoteContainerId);
                            } else {
                                errorMessage = "Management request timed out on the client - enable info level tracing to diagnose.";
                            }

                            resultFuture.completeExceptionally(new TimeoutException(errorMessage));
                        }
                    });
        } catch (final IOException ioException) {
            resultFuture.completeExceptionally(
                new OperationCancelledException(
                    "Sending request failed while dispatching to Reactor, see cause for more details.",
                    ioException));

            return resultFuture;
        }

        // if there isn't even 5 millis left - request will not make the round-trip
        // to the event hubs service. so don't schedule the request - let it timeout
        if (timeoutInMillis > ClientConstants.MGMT_CHANNEL_MIN_RETRY_IN_MILLIS) {
            final MessageOperationResult messageOperation = new MessageOperationResult(response -> {
                if (response.getBody() != null) {
                    resultFuture.complete((Map<String, Object>) ((AmqpValue) response.getBody()).getValue());
                }
            }, resultFuture::completeExceptionally);

            final OperationResultBase<RequestResponseChannel, Exception> operation = new OperationResultBase<>(
                result -> result.request(requestMessage, messageOperation),
                resultFuture::completeExceptionally);

            this.innerChannel.runOnOpenedObject(dispatcher, operation);
        }

        return resultFuture;
    }

    public void close(final ReactorDispatcher reactorDispatcher, final OperationResult<Void, Exception> closeCallback) {
        this.innerChannel.close(reactorDispatcher, closeCallback);
    }
}
