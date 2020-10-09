// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs.impl;

import com.microsoft.azure.eventhubs.CommunicationException;
import com.microsoft.azure.eventhubs.ConnectionStringBuilder;
import com.microsoft.azure.eventhubs.EventHubClientOptions;
import com.microsoft.azure.eventhubs.EventHubException;
import com.microsoft.azure.eventhubs.ITokenProvider;
import com.microsoft.azure.eventhubs.ManagedIdentityTokenProvider;
import com.microsoft.azure.eventhubs.OperationCancelledException;
import com.microsoft.azure.eventhubs.ProxyConfiguration;
import com.microsoft.azure.eventhubs.RetryPolicy;
import com.microsoft.azure.eventhubs.TimeoutException;
import com.microsoft.azure.eventhubs.TransportType;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.engine.BaseHandler;
import org.apache.qpid.proton.engine.Connection;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.Handler;
import org.apache.qpid.proton.engine.HandlerException;
import org.apache.qpid.proton.engine.Link;
import org.apache.qpid.proton.engine.Session;
import org.apache.qpid.proton.engine.SslDomain;
import org.apache.qpid.proton.reactor.Reactor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.UnresolvedAddressException;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Abstracts all amqp related details and exposes AmqpConnection object
 * Manages connection life-cycle
 */
public final class MessagingFactory extends ClientEntity implements AmqpConnection, SessionProvider, SchedulerProvider {
    public static final Duration DefaultOperationTimeout = Duration.ofSeconds(60);

    private static final Logger TRACE_LOGGER = LoggerFactory.getLogger(MessagingFactory.class);
    private final String hostName;
    private final CompletableFuture<Void> closeTask;
    private final ConnectionHandler connectionHandler;
    private final LinkedList<Link> registeredLinks;
    private final Object reactorLock;
    private final Object cbsChannelCreateLock;
    private final Object mgmtChannelCreateLock;
    private final ITokenProvider tokenProvider;
    private final ReactorFactory reactorFactory;

    private static final long WATCHDOG_SCAN_DIVISOR = 2;
    private final LinkedList<MessageReceiver> watchdogReceivers;
    private final Object watchdogSyncObject;
    private final Duration watchdogTriggerTime;
    private ScheduledFuture<?> watchdogFuture;
    private final long watchdogScanSeconds;
    private boolean watchdogCleanupDone;

    private Reactor reactor;
    private ReactorDispatcher reactorDispatcher;
    private Connection connection;
    private CBSChannel cbsChannel;
    private ManagementChannel mgmtChannel;
    private Duration operationTimeout;
    private RetryPolicy retryPolicy;
    private CompletableFuture<MessagingFactory> open;
    private CompletableFuture<?> openTimer;
    private CompletableFuture<?> closeTimer;
    private String reactorCreationTime;            // used when looking at Java dumps, do not remove

    MessagingFactory(final String hostname,
                      final Duration operationTimeout,
                      final TransportType transportType,
                      final ITokenProvider tokenProvider,
                     final RetryPolicy retryPolicy,
                     final ScheduledExecutorService executor,
                     final ReactorFactory reactorFactory,
                     final ProxyConfiguration proxyConfiguration,
                     final Duration watchdogTriggerTime,
                    final SslDomain.VerifyMode verifyMode) {
        super(StringUtil.getRandomString("MF"), null, executor);

        if (StringUtil.isNullOrWhiteSpace(hostname)) {
            throw new IllegalArgumentException("Endpoint hostname cannot be null or empty");
        }
        Objects.requireNonNull(operationTimeout, "Operation timeout cannot be null.");
        Objects.requireNonNull(transportType, "Transport type cannot be null.");
        Objects.requireNonNull(tokenProvider, "Token provider cannot be null.");
        Objects.requireNonNull(retryPolicy, "Retry policy cannot be null.");
        Objects.requireNonNull(executor, "Executor cannot be null.");
        Objects.requireNonNull(reactorFactory, "Reactor factory cannot be null.");

        this.hostName = hostname;
        this.reactorFactory = reactorFactory;
        this.operationTimeout = operationTimeout;
        this.retryPolicy = retryPolicy;
        this.connectionHandler = ConnectionHandler.create(transportType, this,
            this.getClientId(), proxyConfiguration, verifyMode);
        this.tokenProvider = tokenProvider;

        this.registeredLinks = new LinkedList<>();
        this.reactorLock = new Object();
        this.cbsChannelCreateLock = new Object();
        this.mgmtChannelCreateLock = new Object();

        this.watchdogTriggerTime = watchdogTriggerTime;
        this.watchdogScanSeconds = watchdogTriggerTime.toMillis() / MessagingFactory.WATCHDOG_SCAN_DIVISOR / 1000;
        this.watchdogReceivers = new LinkedList<>();
        this.watchdogSyncObject = new Object();

        this.closeTask = new CompletableFuture<>();
    }

    public static CompletableFuture<MessagingFactory> createFromConnectionString(final String connectionString, final ScheduledExecutorService executor) throws IOException {
        return createFromConnectionString(connectionString, null, executor, null);
    }

    public static CompletableFuture<MessagingFactory> createFromConnectionString(
            final String connectionString,
            final RetryPolicy retryPolicy,
            final ScheduledExecutorService executor,
            final ProxyConfiguration proxyConfiguration) throws IOException {
        return createFromConnectionString(connectionString, retryPolicy, executor, null, proxyConfiguration, EventHubClientOptions.SILENT_OFF);
    }

    public static CompletableFuture<MessagingFactory> createFromConnectionString(
            final String connectionString,
            final RetryPolicy retryPolicy,
            final ScheduledExecutorService executor,
            final ProxyConfiguration proxyConfiguration,
            final Duration watchdogTriggerTime) throws IOException {
        return createFromConnectionString(connectionString, retryPolicy, executor, null, proxyConfiguration, watchdogTriggerTime);
    }

    public static CompletableFuture<MessagingFactory> createFromConnectionString(
            final String connectionString,
            final RetryPolicy retryPolicy,
            final ScheduledExecutorService executor,
            final ReactorFactory reactorFactory,
            final ProxyConfiguration proxyConfiguration,
            final Duration watchdogTriggerTime) throws IOException {
        final ConnectionStringBuilder csb = new ConnectionStringBuilder(connectionString);
        ITokenProvider tokenProvider = null;
        if (!StringUtil.isNullOrWhiteSpace(csb.getSharedAccessSignature())) {
            tokenProvider = new SharedAccessSignatureTokenProvider(csb.getSharedAccessSignature());
        } else if (!StringUtil.isNullOrWhiteSpace(csb.getSasKey())) {
            tokenProvider = new SharedAccessSignatureTokenProvider(csb.getSasKeyName(), csb.getSasKey());
        } else if ((csb.getAuthentication() != null) && csb.getAuthentication().equalsIgnoreCase(ConnectionStringBuilder.MANAGED_IDENTITY_AUTHENTICATION)) {
            tokenProvider = new ManagedIdentityTokenProvider();
        } else {
            throw new IllegalArgumentException("Connection string must specify a Shared Access Signature, Shared Access Key, or Managed Identity");
        }

        final MessagingFactoryBuilder builder = new MessagingFactoryBuilder(csb.getEndpoint().getHost(), tokenProvider, executor)
                .setOperationTimeout(csb.getOperationTimeout())
                .setTransportType(csb.getTransportType())
                .setRetryPolicy(retryPolicy)
                .setReactorFactory(reactorFactory)
                .setProxyConfiguration(proxyConfiguration)
                .setWatchdogTriggerTime(watchdogTriggerTime);

        return builder.build();
    }

    public static class MessagingFactoryBuilder {
        // These parameters must always be specified by the caller
        private final String hostname;
        private final ITokenProvider tokenProvider;
        private final ScheduledExecutorService executor;

        // Optional parameters with defaults
        private Duration operationTimeout = DefaultOperationTimeout;
        private TransportType transportType = TransportType.AMQP;
        private RetryPolicy retryPolicy = RetryPolicy.getDefault();
        private ReactorFactory reactorFactory = new ReactorFactory();
        private ProxyConfiguration proxyConfiguration;
        private Duration watchdogTriggerTime = EventHubClientOptions.SILENT_OFF;
        private SslDomain.VerifyMode verifyMode;

        public MessagingFactoryBuilder(final String hostname, final ITokenProvider tokenProvider, final ScheduledExecutorService executor) {
            if (StringUtil.isNullOrWhiteSpace(hostname)) {
                throw new IllegalArgumentException("Endpoint hostname cannot be null or empty");
            }
            this.hostname = hostname;

            this.tokenProvider = Objects.requireNonNull(tokenProvider);
            this.executor = Objects.requireNonNull(executor);
        }

        public MessagingFactoryBuilder setOperationTimeout(Duration operationTimeout) {
            if (operationTimeout != null) {
                this.operationTimeout = operationTimeout;
            }
            return this;
        }

        public MessagingFactoryBuilder setTransportType(TransportType transportType) {
            if (transportType != null) {
                this.transportType = transportType;
            }
            return this;
        }

        public MessagingFactoryBuilder setRetryPolicy(RetryPolicy retryPolicy) {
            if (retryPolicy != null) {
                this.retryPolicy = retryPolicy;
            }
            return this;
        }

        public MessagingFactoryBuilder setReactorFactory(ReactorFactory reactorFactory) {
            if (reactorFactory != null) {
                this.reactorFactory = reactorFactory;
            }
            return this;
        }

        public MessagingFactoryBuilder setProxyConfiguration(ProxyConfiguration proxyConfiguration) {
            this.proxyConfiguration = proxyConfiguration;
            return this;
        }

        public MessagingFactoryBuilder setWatchdogTriggerTime(Duration watchdogTriggerTime) {
            this.watchdogTriggerTime = watchdogTriggerTime;
            return this;
        }

        /**
         * Package-private verify mode setter.
         *
         * @param verifyMode The verify mode to use.
         * @return The updated {@link MessagingFactoryBuilder} object.
         */
         MessagingFactoryBuilder setVerifyMode(SslDomain.VerifyMode verifyMode) {
            this.verifyMode = verifyMode;
            return this;
        }

        public CompletableFuture<MessagingFactory> build() throws IOException {
            final SslDomain.VerifyMode mode = verifyMode != null
                ? verifyMode
                : SslDomain.VerifyMode.VERIFY_PEER_NAME;
            final MessagingFactory messagingFactory = new MessagingFactory(this.hostname,
                this.operationTimeout,
                this.transportType,
                this.tokenProvider,
                this.retryPolicy,
                this.executor,
                this.reactorFactory,
                this.proxyConfiguration,
                this.watchdogTriggerTime,
                mode);
            return MessagingFactory.factoryStartup(messagingFactory);
        }
    }

    private static CompletableFuture<MessagingFactory> factoryStartup(MessagingFactory messagingFactory) throws IOException {
        messagingFactory.createConnection();

        messagingFactory.startWatchdog();

        final Timer timer = new Timer(messagingFactory);
        messagingFactory.openTimer = timer.schedule(
                new Runnable() {
                    @Override
                    public void run() {
                        if (!messagingFactory.open.isDone()) {
                            messagingFactory.open.completeExceptionally(new TimeoutException("Opening MessagingFactory timed out."));
                            messagingFactory.getReactor().stop();
                        }
                    }
                },
                messagingFactory.getOperationTimeout());

        // if scheduling messagingfactory openTimer fails - notify user and stop
        messagingFactory.openTimer.handleAsync(
            (unUsed, exception) -> {
                if (exception != null && !(exception instanceof CancellationException)) {
                    messagingFactory.open.completeExceptionally(exception);
                    messagingFactory.getReactor().stop();
                }
                return null;
            }, messagingFactory.executor);

        return messagingFactory.open;
    }

    public void registerForWatchdog(final MessageReceiver rcvr) {
        if (this.watchdogTriggerTime.compareTo(EventHubClientOptions.SILENT_OFF) > 0) {
            TRACE_LOGGER.info("Registering for watchdog: " + rcvr.getClientId());
            synchronized (this.watchdogSyncObject) {
                this.watchdogReceivers.add(rcvr);
            }
        }
        // else ignore registration if watchdog is off
    }

    public void unregisterForWatchdog(final MessageReceiver rcvr) {
        if (this.watchdogTriggerTime.compareTo(EventHubClientOptions.SILENT_OFF) > 0) {
            TRACE_LOGGER.info("Unregistering for watchdog: " + rcvr.getClientId());
            synchronized (this.watchdogSyncObject) {
                this.watchdogReceivers.remove(rcvr);
            }
        }
    }

    private void startWatchdog() {
        if (this.watchdogTriggerTime.compareTo(EventHubClientOptions.SILENT_OFF) > 0) {
            TRACE_LOGGER.info("Watchdog scheduling first run in " + this.watchdogScanSeconds + " seconds");
            this.watchdogFuture = this.executor.schedule(new WatchDog(), this.watchdogScanSeconds, TimeUnit.SECONDS);
        } else {
            TRACE_LOGGER.info("Watchdog is OFF");
        }
    }

    private class WatchDog implements Runnable {
        @Override
        public void run() {
            TRACE_LOGGER.debug("Watchdog run");
            if (MessagingFactory.this.getIsClosingOrClosed()) {
                return;
            }

            LinkedList<MessageReceiver> copiedList = null;
            synchronized (MessagingFactory.this.watchdogSyncObject) {
                copiedList = new LinkedList<MessageReceiver>(MessagingFactory.this.watchdogReceivers);
            }
            if (!copiedList.isEmpty()) {
                boolean anyReceiverIsAlive = false;
                final long longestAgoAllowable = Instant.now().getEpochSecond()
                        - (MessagingFactory.this.watchdogTriggerTime.toMillis() / 1000);

                for (MessageReceiver rcvr : copiedList) {
                    TRACE_LOGGER.debug("Watchdog checking receiver " + rcvr.getClientId() + " last: "
                            + rcvr.getLastReceivedTime() + "  allowable: " + longestAgoAllowable);
                    if (!rcvr.getIsClosingOrClosed() && (rcvr.getLastReceivedTime() >= longestAgoAllowable)) {
                        anyReceiverIsAlive = true;
                        // Found one live receiver, no need to check the rest.
                        break;
                    }
                }

                if (!anyReceiverIsAlive && !MessagingFactory.this.getIsClosingOrClosed()) {
                    TRACE_LOGGER.warn("Watchdog forcing connection closed");
                    ErrorCondition suspect = new ErrorCondition(ClientConstants.WATCHDOG_ERROR,
                            "receiver watchdog has fired, all receivers silent");
                    MessagingFactory.this.watchdogCleanupDone = false;
                    MessagingFactory.this.connection.setCondition(suspect);
                    MessagingFactory.this.connection.close();
                    // If the remote host is still responding at the TCP level, then the socket will
                    // close normally and cleanup/recreation will happen automatically. However, if it
                    // isn't, then we must call onConnectionError here in order to force cleanup and
                    // recreation.
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                    }
                    if (!MessagingFactory.this.watchdogCleanupDone) {
                        TRACE_LOGGER.warn("Watchdog forcing cleanup");
                        MessagingFactory.this.onConnectionError(suspect);
                    } else {
                        TRACE_LOGGER.info("Watchdog cleanup already in progress");
                    }
                }
            }

            synchronized (MessagingFactory.this.watchdogSyncObject) {
                if (!MessagingFactory.this.getIsClosingOrClosed() && !MessagingFactory.this.watchdogFuture.isCancelled()) {
                    TRACE_LOGGER.debug("Watchdog scheduling next run");
                    MessagingFactory.this.watchdogFuture = MessagingFactory.this.executor.schedule(this, MessagingFactory.this.watchdogScanSeconds, TimeUnit.SECONDS);
                } else {
                    TRACE_LOGGER.info("Watchdog stopping due to MessagingFactory close");
                }
            }
        }
    }

    @Override
    public String getHostName() {
        return this.hostName;
    }

    private Reactor getReactor() {
        synchronized (this.reactorLock) {
            return this.reactor;
        }
    }

    public ReactorDispatcher getReactorDispatcher() {
        synchronized (this.reactorLock) {
            return this.reactorDispatcher;
        }
    }

    public ITokenProvider getTokenProvider() {
        return this.tokenProvider;
    }

    private void createConnection() throws IOException {
        this.open = new CompletableFuture<>();
        this.startReactor(new ReactorHandlerWithConnection());
    }

    private void startReactor(final ReactorHandler reactorHandler) throws IOException {
        final Reactor newReactor = this.reactorFactory.create(reactorHandler, this.connectionHandler.getMaxFrameSize(), this.getClientId());
        synchronized (this.reactorLock) {
            this.reactor = newReactor;
            this.reactorDispatcher = new ReactorDispatcher(newReactor);
            reactorHandler.unsafeSetReactorDispatcher(this.reactorDispatcher);
        }

        this.reactorCreationTime = Instant.now().toString();

        executor.execute(new RunReactor(newReactor, executor));
    }

    public CBSChannel getCBSChannel() {
        synchronized (this.cbsChannelCreateLock) {
            if (this.cbsChannel == null) {
                this.cbsChannel = new CBSChannel(this, this, this.getClientId(), this.executor);
            }
        }

        return this.cbsChannel;
    }

    public ManagementChannel getManagementChannel() {
        synchronized (this.mgmtChannelCreateLock) {
            if (this.mgmtChannel == null) {
                this.mgmtChannel = new ManagementChannel(this, this, this.getClientId(), this.executor);
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

        if (TRACE_LOGGER.isInfoEnabled()) {
            TRACE_LOGGER.info(
                    String.format(Locale.US, "messagingFactory[%s], hostName[%s], getting a session.",
                            getClientId(), getHostName()));
        }

        if (this.connection == null || this.connection.getLocalState() == EndpointState.CLOSED || this.connection.getRemoteState() == EndpointState.CLOSED) {
            this.connection = this.getReactor().connectionToHost(
                    this.connectionHandler.getRemoteHostName(),
                    this.connectionHandler.getRemotePort(),
                    this.connectionHandler);
        }

        final Session session = this.connection.session();
        BaseHandler.setHandler(session, new SessionHandler(path, onRemoteSessionOpen, onRemoteSessionOpenError, this.operationTimeout, this.getClientId()));
        session.open();

        return session;
    }

    public Duration getOperationTimeout() {
        return this.operationTimeout;
    }

    public RetryPolicy getRetryPolicy() {
        return this.retryPolicy;
    }

    @Override
    public void onOpenComplete(Exception exception) {
        if (exception == null) {
            this.open.complete(this);

            // if connection creation is in progress and then msgFactory.close call came thru
            if (this.getIsClosingOrClosed()) {
                this.connection.close();
            }
        } else {
            this.open.completeExceptionally(exception);
        }

        if (this.openTimer != null) {
            this.openTimer.cancel(false);
        }
    }

    @Override
    public void onConnectionError(ErrorCondition error) {
        this.watchdogCleanupDone = true;

        if (TRACE_LOGGER.isWarnEnabled()) {
            TRACE_LOGGER.warn(String.format(Locale.US, "onConnectionError messagingFactory[%s], hostname[%s], error[%s]",
                    this.getClientId(),
                    this.hostName,
                    error != null ? error.getDescription() : "n/a"));
        }

        if (!this.open.isDone()) {
            if (TRACE_LOGGER.isWarnEnabled()) {
                TRACE_LOGGER.warn(String.format(Locale.US, "onConnectionError messagingFactory[%s], hostname[%s], open hasn't complete, stopping the reactor",
                        this.getClientId(),
                        this.hostName));
            }

            this.getReactor().stop();
            this.onOpenComplete(ExceptionUtil.toException(error));
        } else {
            final Connection oldConnection = this.connection;
            final List<Link> oldRegisteredLinksCopy = new LinkedList<>(this.registeredLinks);
            final List<Link> closedLinks = new LinkedList<>();

            for (Link link : oldRegisteredLinksCopy) {
                if (link.getLocalState() != EndpointState.CLOSED) {
                    if (TRACE_LOGGER.isWarnEnabled()) {
                        TRACE_LOGGER.warn(String.format(Locale.US, "onConnectionError messagingFactory[%s], hostname[%s], closing link [%s]",
                                this.getClientId(),
                                this.hostName, link.getName()));
                    }

                    link.setCondition(error);
                    link.close();
                    closedLinks.add(link);
                }
            }

            // if proton-j detects transport error - onConnectionError is invoked, but, the connection state is not set to closed
            // in connection recreation we depend on currentConnection state to evaluate need for recreation
            if (oldConnection.getLocalState() != EndpointState.CLOSED) {
                if (TRACE_LOGGER.isWarnEnabled()) {
                    TRACE_LOGGER.warn(String.format(Locale.US, "onConnectionError messagingFactory[%s], hostname[%s], closing current connection",
                            this.getClientId(),
                            this.hostName));
                }

                // this should ideally be done in Connectionhandler
                // - but, since proton doesn't automatically emit close events
                // for all child objects (links & sessions) we are doing it here
                oldConnection.setCondition(error);
                oldConnection.close();
            }

            for (Link link : closedLinks) {
                final Handler handler = BaseHandler.getHandler(link);
                if (handler instanceof BaseLinkHandler) {
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
            if (this.getIsClosingOrClosed()) {
                return;
            }

            TRACE_LOGGER.warn(String.format(Locale.US, "onReactorError messagingFactory[%s], hostName[%s], error[%s]",
                    this.getClientId(), this.getHostName(),
                    cause.getMessage()));

            final Connection oldConnection = this.connection;
            final List<Link> oldRegisteredLinksCopy = new LinkedList<>(this.registeredLinks);

            try {
                TRACE_LOGGER.info(String.format(Locale.US, "onReactorError messagingFactory[%s], hostName[%s], message[%s]",
                        this.getClientId(), this.getHostName(),
                        "starting new reactor"));

                this.startReactor(new ReactorHandlerWithConnection());
            } catch (IOException e) {
                TRACE_LOGGER.error(String.format(Locale.US, "messagingFactory[%s], hostName[%s], error[%s]",
                        this.getClientId(), this.getHostName(),
                        ExceptionUtil.toStackTraceString(e, "Re-starting reactor failed with error")));

                // TODO: stop retrying on the error after multiple attempts.
                this.onReactorError(cause);
            }

            // when the instance of the reactor itself faults - Connection and Links will not be cleaned up even after the
            // below .close() calls (local closes).
            // But, we still need to change the states of these to Closed - so that subsequent retries - will
            // treat the links and connection as closed and re-establish them and continue running on new Reactor instance.
            ErrorCondition errorCondition = new ErrorCondition(Symbol.getSymbol("messagingfactory.onreactorerror"), cause.getMessage());
            if (oldConnection.getLocalState() != EndpointState.CLOSED) {
                if (TRACE_LOGGER.isWarnEnabled()) {
                    TRACE_LOGGER.warn(String.format(Locale.US, "onReactorError: messagingFactory[%s], hostname[%s], closing current connection",
                            this.getClientId(),
                            this.hostName));
                }

                oldConnection.setCondition(errorCondition);
                oldConnection.close();
            }

            for (final Link link : oldRegisteredLinksCopy) {
                if (link.getLocalState() != EndpointState.CLOSED) {
                    link.setCondition(errorCondition);
                    link.close();
                }

                final Handler handler = BaseHandler.getHandler(link);
                if (handler instanceof BaseLinkHandler) {
                    final BaseLinkHandler linkHandler = (BaseLinkHandler) handler;
                    linkHandler.processOnClose(link, cause);
                }
            }
        }
    }

    @Override
    protected CompletableFuture<Void> onClose() {
        if (!this.getIsClosed()) {
            synchronized (this.watchdogSyncObject) {
                if (this.watchdogFuture != null) {
                    this.watchdogFuture.cancel(true);
                }
            }

            final Timer timer = new Timer(this);
            this.closeTimer = timer.schedule(new Runnable() {
                    @Override
                    public void run() {
                        if (!closeTask.isDone()) {
                            closeTask.completeExceptionally(new TimeoutException("Closing MessagingFactory timed out."));
                            getReactor().stop();
                        }
                    }
            }, operationTimeout);

            if (this.closeTimer.isCompletedExceptionally()) {
                this.closeTask.completeExceptionally(ExceptionUtil.getExceptionFromCompletedFuture(this.closeTimer));
            } else {
                try {
                    this.scheduleOnReactorThread(new CloseWork());
                } catch (IOException | RejectedExecutionException schedulerException) {
                    this.closeTask.completeExceptionally(schedulerException);
                }
            }
        }

        return this.closeTask;
    }

    @Override
    public void registerForConnectionError(Link link) {
        this.registeredLinks.add(link);
    }

    @Override
    public void deregisterForConnectionError(Link link) {
        this.registeredLinks.remove(link);
    }

    public void scheduleOnReactorThread(final DispatchHandler handler) throws IOException, RejectedExecutionException {
        this.getReactorDispatcher().invoke(handler);
    }

    public void scheduleOnReactorThread(final int delay, final DispatchHandler handler) throws IOException, RejectedExecutionException {
        this.getReactorDispatcher().invoke(delay, handler);
    }

    public static class ReactorFactory {

        public Reactor create(final ReactorHandler reactorHandler, final int maxFrameSize, final String name) throws IOException {
            return ProtonUtil.reactor(reactorHandler, maxFrameSize, name);
        }
    }

    private class CloseWork extends DispatchHandler {
        @Override
        public void onEvent() {
            final ReactorDispatcher dispatcher = getReactorDispatcher();
            synchronized (cbsChannelCreateLock) {
                if (cbsChannel != null) {
                    cbsChannel.close(
                            dispatcher,
                            new OperationResult<Void, Exception>() {
                                @Override
                                public void onComplete(Void result) {
                                    if (TRACE_LOGGER.isInfoEnabled()) {
                                        TRACE_LOGGER.info(
                                                String.format(Locale.US, "messagingFactory[%s], hostName[%s], info[%s]",
                                                        getClientId(), getHostName(), "cbsChannel closed"));
                                    }
                                }

                                @Override
                                public void onError(Exception error) {
                                    if (TRACE_LOGGER.isWarnEnabled()) {
                                        TRACE_LOGGER.warn(String.format(Locale.US,
                                                "messagingFactory[%s], hostName[%s], cbsChannelCloseError[%s]",
                                                getClientId(), getHostName(), error.getMessage()));
                                    }
                                }
                            });
                }
            }

            synchronized (mgmtChannelCreateLock) {
                if (mgmtChannel != null) {
                    mgmtChannel.close(
                            dispatcher,
                            new OperationResult<Void, Exception>() {
                                @Override
                                public void onComplete(Void result) {
                                    if (TRACE_LOGGER.isInfoEnabled()) {
                                        TRACE_LOGGER.info(
                                                String.format(Locale.US, "messagingFactory[%s], hostName[%s], info[%s]",
                                                        getClientId(), getHostName(), "mgmtChannel closed"));
                                    }
                                }

                                @Override
                                public void onError(Exception error) {

                                    if (TRACE_LOGGER.isWarnEnabled()) {
                                        TRACE_LOGGER.warn(String.format(Locale.US,
                                                "messagingFactory[%s], hostName[%s], mgmtChannelCloseError[%s]",
                                                getClientId(), getHostName(), error.getMessage()));
                                    }
                                }
                            });
                }
            }

            if (connection != null && connection.getRemoteState() != EndpointState.CLOSED && connection.getLocalState() != EndpointState.CLOSED) {
                connection.close();
            }
        }
    }

    private class RunReactor implements Runnable {
        private final Reactor rctr;
        private final ScheduledExecutorService executor;

        volatile boolean hasStarted;

        RunReactor(final Reactor reactor, final ScheduledExecutorService executor) {
            this.rctr = reactor;
            this.executor = executor;
            this.hasStarted = false;
        }

        public void run() {
            boolean reScheduledReactor = false;

            try {
                if (!this.hasStarted) {
                    if (TRACE_LOGGER.isInfoEnabled()) {
                        TRACE_LOGGER.info(String.format(Locale.US, "messagingFactory[%s], hostName[%s], info[%s]",
                                getClientId(), getHostName(), "starting reactor instance."));
                    }

                    this.rctr.start();
                    this.hasStarted = true;
                }

                if (!Thread.interrupted() && this.rctr.process()) {
                    try {
                        this.executor.execute(this);
                        reScheduledReactor = true;
                    } catch (RejectedExecutionException exception) {
                        if (TRACE_LOGGER.isWarnEnabled()) {
                            TRACE_LOGGER.warn(String.format(Locale.US, "messagingFactory[%s], hostName[%s], error[%s]",
                                    getClientId(), getHostName(),
                                    ExceptionUtil.toStackTraceString(exception, "scheduling reactor failed because the executor has been shut down")));
                        }

                        this.rctr.attachments().set(RejectedExecutionException.class, RejectedExecutionException.class, exception);
                    }

                    return;
                }

                if (TRACE_LOGGER.isWarnEnabled()) {
                    TRACE_LOGGER.warn(String.format(Locale.US, "messagingFactory[%s], hostName[%s], message[%s]",
                            getClientId(), getHostName(),
                            "stopping the reactor because thread was interrupted or the reactor has no more events to process."));
                }

                this.rctr.stop();
            } catch (HandlerException handlerException) {
                Throwable cause = handlerException.getCause();
                if (cause == null) {
                    cause = handlerException;
                }

                if (TRACE_LOGGER.isWarnEnabled()) {
                    TRACE_LOGGER.warn(String.format(Locale.US, "messagingFactory[%s], hostName[%s], error[%s]",
                            getClientId(), getHostName(), ExceptionUtil.toStackTraceString(handlerException,
                                    "Unhandled exception while processing events in reactor, report this error.")));
                }

                final String message = !StringUtil.isNullOrEmpty(cause.getMessage())
                        ? cause.getMessage()
                        : !StringUtil.isNullOrEmpty(handlerException.getMessage())
                            ? handlerException.getMessage()
                            : "Reactor encountered unrecoverable error";

                final EventHubException sbException;

                if (cause instanceof UnresolvedAddressException) {
                    sbException = new CommunicationException(
                            String.format(Locale.US, "%s. This is usually caused by incorrect hostname or network configuration. Check correctness of namespace information. %s",
                                    message, ExceptionUtil.getTrackingIDAndTimeToLog()),
                            cause);
                } else {
                    sbException = new EventHubException(
                            true,
                            String.format(Locale.US, "%s, %s", message, ExceptionUtil.getTrackingIDAndTimeToLog()),
                            cause);
                }

                MessagingFactory.this.onReactorError(sbException);
            } finally {
                if (reScheduledReactor) {
                    return;
                }

                if (getIsClosingOrClosed() && !closeTask.isDone()) {
                    this.rctr.free();
                    closeTask.complete(null);
                    if (closeTimer != null) {
                        closeTimer.cancel(false);
                    }
                } else {
                    scheduleCompletePendingTasks();
                }
            }
        }

        private void scheduleCompletePendingTasks() {
            this.executor.schedule(new Runnable() {
                @Override
                public void run() {
                    if (TRACE_LOGGER.isWarnEnabled()) {
                        TRACE_LOGGER.warn(String.format(Locale.US, "messagingFactory[%s], hostName[%s], message[%s]",
                                getClientId(), getHostName(),
                                "Processing all pending tasks and closing old reactor."));
                    }

                    try {
                        rctr.stop();
                        rctr.process();
                    } catch (HandlerException e) {
                        if (TRACE_LOGGER.isWarnEnabled()) {
                            TRACE_LOGGER.warn(String.format(Locale.US, "messagingFactory[%s], hostName[%s], error[%s]",
                                    getClientId(), getHostName(), ExceptionUtil.toStackTraceString(e,
                                            "scheduleCompletePendingTasks - exception occurred while processing events.")));
                        }
                    } finally {
                        rctr.free();
                    }
                }
            }, MessagingFactory.this.getOperationTimeout().getSeconds(), TimeUnit.SECONDS);
        }
    }

    private class ReactorHandlerWithConnection extends ReactorHandler {
        ReactorHandlerWithConnection() {
            super(getClientId());
        }

        @Override
        public void onReactorInit(Event e) {
            super.onReactorInit(e);

            final Reactor r = e.getReactor();
            connection = r.connectionToHost(
                    connectionHandler.getRemoteHostName(),
                    connectionHandler.getRemotePort(),
                    connectionHandler);
        }
    }
}
