/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.eventhubs;

import java.io.IOException;
import java.nio.channels.UnresolvedAddressException;
import java.time.Duration;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.engine.BaseHandler;
import org.apache.qpid.proton.engine.Connection;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.Handler;
import org.apache.qpid.proton.engine.HandlerException;
import org.apache.qpid.proton.engine.Link;
import org.apache.qpid.proton.engine.Session;
import org.apache.qpid.proton.reactor.Reactor;

import com.microsoft.azure.eventhubs.amqp.BaseLinkHandler;
import com.microsoft.azure.eventhubs.amqp.ConnectionHandler;
import com.microsoft.azure.eventhubs.amqp.DispatchHandler;
import com.microsoft.azure.eventhubs.amqp.IAmqpConnection;
import com.microsoft.azure.eventhubs.amqp.IOperationResult;
import com.microsoft.azure.eventhubs.amqp.ISessionProvider;
import com.microsoft.azure.eventhubs.amqp.ProtonUtil;
import com.microsoft.azure.eventhubs.amqp.ReactorHandler;
import com.microsoft.azure.eventhubs.amqp.ReactorDispatcher;
import com.microsoft.azure.eventhubs.amqp.SessionHandler;

/**
 * Abstracts all amqp related details and exposes AmqpConnection object
 * Manages connection life-cycle
 */
public class MessagingFactory extends ClientEntity implements IAmqpConnection, ISessionProvider {
    public static final Duration DefaultOperationTimeout = Duration.ofSeconds(60);

    private static final Logger TRACE_LOGGER = LoggerFactory.getLogger(MessagingFactory.class);
    private final String hostName;
    private final CompletableFuture<Void> closeTask;
    private final ConnectionHandler connectionHandler;
    private final LinkedList<Link> registeredLinks;
    private final Object reactorLock;
    private final Object cbsChannelCreateLock;
    private final Object mgmtChannelCreateLock;
    private final SharedAccessSignatureTokenProvider tokenProvider;
    private final ReactorFactory reactorFactory;

    private Reactor reactor;
    private ReactorDispatcher reactorScheduler;
    private Connection connection;
    private CBSChannel cbsChannel;
    private ManagementChannel mgmtChannel;

    private Duration operationTimeout;
    private RetryPolicy retryPolicy;
    private CompletableFuture<MessagingFactory> open;
    private ScheduledFuture openTimer;
    private ScheduledFuture closeTimer;

    MessagingFactory(final ConnectionStringBuilder builder,
                     final RetryPolicy retryPolicy,
                     final Executor executor,
                     final ReactorFactory reactorFactory) {
        super("MessagingFactory".concat(StringUtil.getRandomString()), null, executor);

        Timer.register(this.getClientId());
        this.hostName = builder.getEndpoint().getHost();
        this.reactorFactory = reactorFactory;

        this.operationTimeout = builder.getOperationTimeout();
        this.retryPolicy = retryPolicy;
        this.registeredLinks = new LinkedList<>();
        this.reactorLock = new Object();
        this.connectionHandler = new ConnectionHandler(this);
        this.cbsChannelCreateLock = new Object();
        this.mgmtChannelCreateLock = new Object();
        this.tokenProvider = builder.getSharedAccessSignature() == null
                ? new SharedAccessSignatureTokenProvider(builder.getSasKeyName(), builder.getSasKey())
                : new SharedAccessSignatureTokenProvider(builder.getSharedAccessSignature());

        this.closeTask = new CompletableFuture<>();
        this.closeTask.thenAcceptAsync(new Consumer<Void>() {
            @Override
            public void accept(Void arg0) {
                Timer.unregister(getClientId());
            }
        }, this.executor);
    }

    public String getHostName() {
        return this.hostName;
    }

    private Reactor getReactor() {
        synchronized (this.reactorLock) {
            return this.reactor;
        }
    }

    public ReactorDispatcher getReactorScheduler() {
        synchronized (this.reactorLock) {
            return this.reactorScheduler;
        }
    }

    public SharedAccessSignatureTokenProvider getTokenProvider() {
        return this.tokenProvider;
    }

    private void createConnection() throws IOException {
        this.open = new CompletableFuture<>();
        this.startReactor(new ReactorHandler() {
            @Override
            public void onReactorInit(Event e) {
                super.onReactorInit(e);

                final Reactor r = e.getReactor();
                connection = r.connectionToHost(hostName, ClientConstants.AMQPS_PORT, connectionHandler);
            }
        });
    }

    private void startReactor(final ReactorHandler reactorHandler) throws IOException {
        final Reactor newReactor = this.reactorFactory.create(reactorHandler);
        synchronized (this.reactorLock) {
            this.reactor = newReactor;
            this.reactorScheduler = new ReactorDispatcher(newReactor);
            reactorHandler.unsafeSetReactorDispatcher(this.reactorScheduler);
        }

        executor.execute(new RunReactor(newReactor, executor));
    }

    public CBSChannel getCBSChannel() {
        synchronized (this.cbsChannelCreateLock) {
            if (this.cbsChannel == null) {
                this.cbsChannel = new CBSChannel(this, this, "cbs-link");
            }
        }

        return this.cbsChannel;
    }
    
    public ManagementChannel getManagementChannel() {
    	synchronized (this.mgmtChannelCreateLock) {
    		if (this.mgmtChannel == null) {
    			this.mgmtChannel = new ManagementChannel(this, this, "mgmt-link");
    		}
    	}
    	
    	return this.mgmtChannel;
    }
    
    @Override
    public Session getSession(final String path, final Consumer<Session> onRemoteSessionOpen, final BiConsumer<ErrorCondition, Exception> onRemoteSessionOpenError) {
        if (this.getIsClosingOrClosed()) {

            onRemoteSessionOpenError.accept(null, new OperationCancelledException("underlying messagingFactory instance is closed"));
            return null;
        }

        if (this.connection == null || this.connection.getLocalState() == EndpointState.CLOSED || this.connection.getRemoteState() == EndpointState.CLOSED) {
            this.connection = this.getReactor().connectionToHost(this.hostName, ClientConstants.AMQPS_PORT, this.connectionHandler);
        }

        final Session session = this.connection.session();
        BaseHandler.setHandler(session, new SessionHandler(path, onRemoteSessionOpen, onRemoteSessionOpenError));
        session.open();

        return session;
    }

    public Duration getOperationTimeout() {
        return this.operationTimeout;
    }

    public RetryPolicy getRetryPolicy() {
        return this.retryPolicy;
    }

    public static CompletableFuture<MessagingFactory> createFromConnectionString(final String connectionString, final Executor executor) throws IOException {
        return createFromConnectionString(connectionString, RetryPolicy.getDefault(), executor);
    }

    public static CompletableFuture<MessagingFactory> createFromConnectionString(
            final String connectionString,
            final RetryPolicy retryPolicy,
            final Executor executor) throws IOException {
        return createFromConnectionString(connectionString, retryPolicy, executor, new ReactorFactory());
    }

    public static CompletableFuture<MessagingFactory> createFromConnectionString(
            final String connectionString,
            final RetryPolicy retryPolicy,
            final Executor executor,
            final ReactorFactory reactorFactory) throws IOException {
        final ConnectionStringBuilder builder = new ConnectionStringBuilder(connectionString);
        final MessagingFactory messagingFactory = new MessagingFactory(builder,
                (retryPolicy != null) ? retryPolicy : RetryPolicy.getDefault(),
                executor,
                reactorFactory);
        messagingFactory.openTimer = Timer.schedule(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            if (!messagingFactory.open.isDone()) {
                                                                messagingFactory.open.completeExceptionally(new TimeoutException("Opening MessagingFactory timed out."));
                                                                messagingFactory.getReactor().stop();
                                                            }
                                                        }
                                                    },
                messagingFactory.getOperationTimeout(),
                TimerType.OneTimeRun);
        messagingFactory.createConnection();
        return messagingFactory.open;
    }

    @Override
    public void onOpenComplete(Exception exception) {
        if (exception == null) {
            this.open.complete(this);

            // if connection creation is in progress and then msgFactory.close call came thru
            if (this.getIsClosingOrClosed())
                this.connection.close();
        } else {
            this.open.completeExceptionally(exception);
        }

        if (this.openTimer != null)
            this.openTimer.cancel(false);
    }

    @Override
    public void onConnectionError(ErrorCondition error) {

        if (!this.open.isDone()) {
            Timer.unregister(this.getClientId());
            this.getReactor().stop();
            this.onOpenComplete(ExceptionUtil.toException(error));
        } else {
            final Connection currentConnection = this.connection;
            final List<Link> registeredLinksCopy = new LinkedList<>(this.registeredLinks);
            final List<Link> closedLinks = new LinkedList<>();
            for (Link link : registeredLinksCopy) {
                if (link.getLocalState() != EndpointState.CLOSED && link.getRemoteState() != EndpointState.CLOSED) {
                    link.close();
                    closedLinks.add(link);
                }
            }

            // if proton-j detects transport error - onConnectionError is invoked, but, the connection state is not set to closed
            // in connection recreation we depend on currentConnection state to evaluate need for recreation
            if (currentConnection.getLocalState() != EndpointState.CLOSED) {
                // this should ideally be done in Connectionhandler
                // - but, since proton doesn't automatically emit close events
                // for all child objects (links & sessions) we are doing it here
                currentConnection.close();
            }

            for (Link link : closedLinks) {
                final Handler handler = BaseHandler.getHandler(link);
                if (handler != null && handler instanceof BaseLinkHandler) {
                    final BaseLinkHandler linkHandler = (BaseLinkHandler) handler;
                    linkHandler.processOnClose(link, error);
                }
            }
        }

        if (this.getIsClosingOrClosed() && !this.closeTask.isDone()) {
            this.getReactor().stop();
        }
    }

    private void onReactorError(Exception cause) {
        if (!this.open.isDone()) {
            this.onOpenComplete(cause);
        } else {
            final Connection currentConnection = this.connection;

            try {
                if (this.getIsClosingOrClosed()) {
                    return;
                } else {
                    this.startReactor(new ReactorHandler());
                }
            } catch (IOException e) {
                TRACE_LOGGER.error("messagingFactory[%s], hostName[%s], error[%s]",
                        this.getClientId(),
                        this.getHostName(),
                        ExceptionUtil.toStackTraceString(e, "Re-starting reactor failed with error"));

                this.onReactorError(cause);
            }

            if (currentConnection.getLocalState() != EndpointState.CLOSED && currentConnection.getRemoteState() != EndpointState.CLOSED) {
                currentConnection.close();
            }

            for (Link link : this.registeredLinks) {
                if (link.getLocalState() != EndpointState.CLOSED && link.getRemoteState() != EndpointState.CLOSED) {
                    link.close();
                }

                final Handler handler = BaseHandler.getHandler(link);
                if (handler != null && handler instanceof BaseLinkHandler) {
                    final BaseLinkHandler linkHandler = (BaseLinkHandler) handler;
                    linkHandler.processOnClose(link, cause);
                }
            }
        }
    }

    @Override
    protected CompletableFuture<Void> onClose() {
        if (!this.getIsClosed()) {
            try {
                this.closeTimer = Timer.schedule(new Runnable() {
                                                     @Override
                                                     public void run() {
                                                         if (!closeTask.isDone()) {
                                                             closeTask.completeExceptionally(new TimeoutException("Closing MessagingFactory timed out."));
                                                             getReactor().stop();
                                                         }
                                                     }
                                                 },
                        operationTimeout, TimerType.OneTimeRun);
                this.scheduleOnReactorThread(new CloseWork());
            } catch (IOException ioException) {
                this.closeTask.completeExceptionally(new EventHubException(false, "Failed to Close MessagingFactory, see cause for more details.", ioException));
            }
        }

        return this.closeTask;
    }

    private class CloseWork extends DispatchHandler {
        @Override
        public void onEvent() {
            final ReactorDispatcher dispatcher = getReactorScheduler();
            synchronized (cbsChannelCreateLock) {

                if (cbsChannel != null) {

                    cbsChannel.close(
                            dispatcher,
                            new IOperationResult<Void, Exception>() {

                                @Override
                                public void onComplete(Void result) {
                                    if (TRACE_LOGGER.isInfoEnabled()) {
                                        TRACE_LOGGER.info("messagingFactory[%s], hostName[%s], info[%s]",
                                                getClientId(),
                                                getHostName(),
                                                "cbsChannel closed");
                                    }
                                }

                                @Override
                                public void onError(Exception error) {

                                    if (TRACE_LOGGER.isWarnEnabled()) {
                                        TRACE_LOGGER.warn("messagingFactory[%s], hostName[%s], cbsChannelCloseError[%s]",
                                                getClientId(),
                                                getHostName(),
                                                error.getMessage());
                                    }
                                }
                            });
                }
            }

            synchronized (mgmtChannelCreateLock) {

                if (mgmtChannel != null) {
                    mgmtChannel.close(
                            dispatcher,
                            new IOperationResult<Void, Exception>() {

                                @Override
                                public void onComplete(Void result) {
                                    if (TRACE_LOGGER.isInfoEnabled()) {
                                        TRACE_LOGGER.info("messagingFactory[%s], hostName[%s], info[%s]",
                                                getClientId(),
                                                getHostName(),
                                                "mgmtChannel closed");
                                    }
                                }

                                @Override
                                public void onError(Exception error) {

                                    if (TRACE_LOGGER.isWarnEnabled()) {
                                        TRACE_LOGGER.warn("messagingFactory[%s], hostName[%s], mgmtChannelCloseError[%s]",
                                                getClientId(),
                                                getHostName(),
                                                error.getMessage());
                                    }
                                }
                            });
                }
            }

            if (connection != null && connection.getRemoteState() != EndpointState.CLOSED && connection.getLocalState() != EndpointState.CLOSED)
                connection.close();
        }
    }

    private class RunReactor implements Runnable {
        final private Reactor rctr;
        final private Executor executor;

        volatile boolean hasStarted;

        public RunReactor(final Reactor reactor, final Executor executor) {
            this.rctr = reactor;
            this.executor = executor;
            this.hasStarted = false;
        }

        public void run() {
            if (TRACE_LOGGER.isInfoEnabled() && !this.hasStarted) {
                TRACE_LOGGER.info("messagingFactory[%s], hostName[%s], info[%s]",
                        getClientId(),
                        getHostName(),
                        "starting reactor instance.");
            }

            boolean reScheduledReactor = false;

            try {
                if (!this.hasStarted) {
                    this.rctr.start();
                    this.hasStarted = true;
                }

                if (!Thread.interrupted() && this.rctr.process()) {
                    try {
                        this.executor.execute(this);
                        reScheduledReactor = true;
                    } catch (RejectedExecutionException exception) {
                        if (TRACE_LOGGER.isWarnEnabled()) {
                            TRACE_LOGGER.warn("messagingFactory[%s], hostName[%s], error[%s]",
                                    getClientId(),
                                    getHostName(),
                                    ExceptionUtil.toStackTraceString(exception, "scheduling reactor failed"));
                        }

                        MessagingFactory.this.setClosed();
                    }

                    return;
                }

                this.rctr.stop();
            } catch (HandlerException handlerException) {
                Throwable cause = handlerException.getCause();
                if (cause == null) {
                    cause = handlerException;
                }

                if (TRACE_LOGGER.isWarnEnabled()) {
                    TRACE_LOGGER.warn("messagingFactory[%s], hostName[%s], error[%s]",
                            getClientId(),
                            getHostName(),
                            ExceptionUtil.toStackTraceString(handlerException, "UnHandled exception while processing events in reactor:"));
                }

                final String message = !StringUtil.isNullOrEmpty(cause.getMessage()) ?
                        cause.getMessage() :
                        !StringUtil.isNullOrEmpty(handlerException.getMessage()) ?
                                handlerException.getMessage() :
                                "Reactor encountered unrecoverable error";

                EventHubException sbException = new EventHubException(
                        true,
                        String.format(Locale.US, "%s, %s", message, ExceptionUtil.getTrackingIDAndTimeToLog()),
                        cause);

                if (cause instanceof UnresolvedAddressException) {
                    sbException = new CommunicationException(
                            String.format(Locale.US, "%s. This is usually caused by incorrect hostname or network configuration. Please check to see if namespace information is correct. %s", message, ExceptionUtil.getTrackingIDAndTimeToLog()),
                            cause);
                }

                MessagingFactory.this.onReactorError(sbException);
            } finally {
                if (reScheduledReactor) {
                    return;
                }

                this.rctr.free();

                if (getIsClosingOrClosed() && !closeTask.isDone()) {
                    closeTask.complete(null);

                    if (closeTimer != null)
                        closeTimer.cancel(false);
                }
            }
        }
    }

    @Override
    public void registerForConnectionError(Link link) {
        this.registeredLinks.add(link);
    }

    @Override
    public void deregisterForConnectionError(Link link) {
        this.registeredLinks.remove(link);
    }

    public void scheduleOnReactorThread(final DispatchHandler handler) throws IOException {
        this.getReactorScheduler().invoke(handler);
    }

    public void scheduleOnReactorThread(final int delay, final DispatchHandler handler) throws IOException {
        this.getReactorScheduler().invoke(delay, handler);
    }

    public static class ReactorFactory {

        public Reactor create(final ReactorHandler reactorHandler) throws IOException {
            return ProtonUtil.reactor(reactorHandler);
        }
    }
}
