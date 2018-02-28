package com.microsoft.azure.eventhubs.impl;

import java.util.function.BiConsumer;

import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.engine.Session;

public class RequestResponseOpener implements Operation<RequestResponseChannel> {
	private final SessionProvider sessionProvider;
	private final String sessionName;
	private final String linkName;
	private final String endpointAddress;
	private final AmqpConnection eventDispatcher;
	
	public RequestResponseOpener(final SessionProvider sessionProvider, final String sessionName, final String linkName,
                                 final String endpointAddress, final AmqpConnection eventDispatcher) {
		this.sessionProvider = sessionProvider;
		this.sessionName = sessionName;
		this.linkName = linkName;
		this.endpointAddress = endpointAddress;
		this.eventDispatcher = eventDispatcher;
	}
	
    @Override
    public void run(OperationResult<RequestResponseChannel, Exception> operationCallback) {

        final Session session = this.sessionProvider.getSession(
                this.sessionName,
                null,
                new BiConsumer<ErrorCondition, Exception>() {
                    @Override
                    public void accept(ErrorCondition error, Exception exception) {
                        if (error != null)
                            operationCallback.onError(ExceptionUtil.toException(error));
                        else if (exception != null)
                            operationCallback.onError(exception);
                    }
                });

        if (session == null)
            return;

        final RequestResponseChannel requestResponseChannel = new RequestResponseChannel(
                this.linkName,
                this.endpointAddress,
                session);

        requestResponseChannel.open(
                new OperationResult<Void, Exception>() {
                    @Override
                    public void onComplete(Void result) {
                        eventDispatcher.registerForConnectionError(requestResponseChannel.getSendLink());
                        eventDispatcher.registerForConnectionError(requestResponseChannel.getReceiveLink());

                        operationCallback.onComplete(requestResponseChannel);
                    }

                    @Override
                    public void onError(Exception error) {
                        operationCallback.onError(error);
                    }
                },
                new OperationResult<Void, Exception>() {
                    @Override
                    public void onComplete(Void result) {
                        eventDispatcher.deregisterForConnectionError(requestResponseChannel.getSendLink());
                        eventDispatcher.deregisterForConnectionError(requestResponseChannel.getReceiveLink());
                    }

                    @Override
                    public void onError(Exception error) {
                        eventDispatcher.deregisterForConnectionError(requestResponseChannel.getSendLink());
                        eventDispatcher.deregisterForConnectionError(requestResponseChannel.getReceiveLink());
                    }
                });
    }
}
