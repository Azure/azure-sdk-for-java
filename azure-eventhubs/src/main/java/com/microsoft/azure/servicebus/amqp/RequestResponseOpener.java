package com.microsoft.azure.servicebus.amqp;

import java.util.function.BiConsumer;

import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.engine.Session;

public class RequestResponseOpener implements IOperation<RequestResponseChannel> {
	private final ISessionProvider sessionProvider;
	private final String sessionName;
	private final String linkName;
	private final String endpointAddress;
	private final IAmqpConnection eventDispatcher;
	
	public RequestResponseOpener(final ISessionProvider sessionProvider, final String sessionName, final String linkName,
			final String endpointAddress, final IAmqpConnection eventDispatcher) {
		this.sessionProvider = sessionProvider;
		this.sessionName = sessionName;
		this.linkName = linkName;
		this.endpointAddress = endpointAddress;
		this.eventDispatcher = eventDispatcher;
	}
	
    @Override
    public void run(IOperationResult<RequestResponseChannel, Exception> operationCallback) {

        final Session session = this.sessionProvider.getSession(
                this.sessionName,
                null,
                new BiConsumer<ErrorCondition, Exception>() {
                    @Override
                    public void accept(ErrorCondition error, Exception exception) {
                        if (error != null)
                            operationCallback.onError(new AmqpException(error));
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
                new IOperationResult<Void, Exception>() {
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
                new IOperationResult<Void, Exception>() {
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
