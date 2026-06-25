// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.implementation.ManagementConstants;
import com.azure.messaging.servicebus.implementation.MessagingEntityType;
import com.azure.messaging.servicebus.implementation.ServiceBusConstants;
import com.azure.messaging.servicebus.implementation.instrumentation.ServiceBusReceiverInstrumentation;
import com.azure.messaging.servicebus.implementation.instrumentation.ServiceBusTracer;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.Objects;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.pagedFluxError;
import static com.azure.messaging.servicebus.ReceiverOptions.createNamedSessionOptions;

/**
 * This <b>asynchronous</b> session receiver client is used to acquire session locks from a queue or topic and create
 * {@link ServiceBusReceiverAsyncClient} instances that are tied to the locked sessions.  Sessions can be used as a
 * first in, first out (FIFO) processing of messages.  Queues and topics/subscriptions support Service Bus sessions,
 * however, it must be <a href="https://learn.microsoft.com/azure/service-bus-messaging/enable-message-sessions">
 *     enabled at the time of entity creation</a>.
 *
 * <p>The examples shown in this document use a credential object named DefaultAzureCredential for authentication,
 * which is appropriate for most scenarios, including local development and production environments. Additionally, we
 * recommend using
 * <a href="https://learn.microsoft.com/azure/active-directory/managed-identities-azure-resources/">managed identity</a>
 * for authentication in production environments. You can find more information on different ways of authenticating and
 * their corresponding credential types in the
 * <a href="https://learn.microsoft.com/java/api/overview/azure/identity-readme">Azure Identity documentation"</a>.
 * </p>
 *
 * <p><strong>Sample: Receive messages from a specific session</strong></p>
 *
 * <p>Use {@link #acceptSession(String)} to acquire the lock of a session if you know the session id.
 * {@link ServiceBusReceiveMode#PEEK_LOCK} and
 * {@link ServiceBusClientBuilder.ServiceBusProcessorClientBuilder#disableAutoComplete() disableAutoComplete()} are
 * <strong>strongly</strong> recommended so users have control over message settlement.</p>
 *
 * <!-- src_embed com.azure.messaging.servicebus.servicebusreceiverasyncclient.instantiation#sessionId -->
 * <pre>
 * TokenCredential credential = new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;;
 *
 * &#47;&#47; 'fullyQualifiedNamespace' will look similar to &quot;&#123;your-namespace&#125;.servicebus.windows.net&quot;
 * &#47;&#47; 'disableAutoComplete' indicates that users will explicitly settle their message.
 * ServiceBusSessionReceiverAsyncClient sessionReceiver = new ServiceBusClientBuilder&#40;&#41;
 *     .credential&#40;fullyQualifiedNamespace, credential&#41;
 *     .sessionReceiver&#40;&#41;
 *     .disableAutoComplete&#40;&#41;
 *     .queueName&#40;sessionEnabledQueueName&#41;
 *     .buildAsyncClient&#40;&#41;;
 *
 * &#47;&#47; acceptSession&#40;String&#41; completes successfully with a receiver when &quot;&lt;&lt;my-session-id&gt;&gt;&quot; session is
 * &#47;&#47; successfully locked.
 * &#47;&#47; `Flux.usingWhen` is used, so we dispose of the receiver resource after `receiveMessages&#40;&#41;` and the settlement
 * &#47;&#47; operations complete.
 * &#47;&#47; `Mono.usingWhen` can also be used if the resource closure returns a single item.
 * Flux&lt;Void&gt; sessionMessages = Flux.usingWhen&#40;
 *     sessionReceiver.acceptSession&#40;&quot;&lt;&lt;my-session-id&gt;&gt;&quot;&#41;,
 *     receiver -&gt; &#123;
 *         &#47;&#47; Receive messages from &lt;&lt;my-session-id&gt;&gt; session.
 *         return receiver.receiveMessages&#40;&#41;.flatMap&#40;message -&gt; &#123;
 *             System.out.printf&#40;&quot;Received Sequence #: %s. Contents: %s%n&quot;, message.getSequenceNumber&#40;&#41;,
 *                 message.getBody&#40;&#41;&#41;;
 *
 *             &#47;&#47; Explicitly settle the message using complete, abandon, defer, dead-letter, etc.
 *             if &#40;isMessageProcessed&#41; &#123;
 *                 return receiver.complete&#40;message&#41;;
 *             &#125; else &#123;
 *                 return receiver.abandon&#40;message&#41;;
 *             &#125;
 *         &#125;&#41;;
 *     &#125;,
 *     receiver -&gt; Mono.fromRunnable&#40;&#40;&#41; -&gt; &#123;
 *         &#47;&#47; Dispose of resources.
 *         receiver.close&#40;&#41;;
 *         sessionReceiver.close&#40;&#41;;
 *     &#125;&#41;&#41;;
 *
 * &#47;&#47; When program ends, or you're done receiving all messages, the `subscription` can be disposed of. This code
 * &#47;&#47; is non-blocking and kicks off the operation.
 * Disposable subscription = sessionMessages.subscribe&#40;
 *     unused -&gt; &#123;
 *     &#125;, error -&gt; System.err.print&#40;&quot;Error receiving message from session: &quot; + error&#41;,
 *     &#40;&#41; -&gt; System.out.println&#40;&quot;Completed receiving from session.&quot;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.messaging.servicebus.servicebusreceiverasyncclient.instantiation#sessionId -->
 *
 * <p><strong>Sample: Receive messages from the first available session</strong></p>
 *
 * <p>Use {@link #acceptNextSession()} to acquire the lock of the next available session without specifying the session
 * id.</p>
 *
 * <!-- src_embed com.azure.messaging.servicebus.servicebusreceiverasyncclient.instantiation#nextsession -->
 * <pre>
 * TokenCredential credential = new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;;
 *
 * &#47;&#47; 'fullyQualifiedNamespace' will look similar to &quot;&#123;your-namespace&#125;.servicebus.windows.net&quot;
 * &#47;&#47; 'disableAutoComplete' indicates that users will explicitly settle their message.
 * ServiceBusSessionReceiverAsyncClient sessionReceiver = new ServiceBusClientBuilder&#40;&#41;
 *     .credential&#40;fullyQualifiedNamespace, credential&#41;
 *     .sessionReceiver&#40;&#41;
 *     .disableAutoComplete&#40;&#41;
 *     .queueName&#40;sessionEnabledQueueName&#41;
 *     .buildAsyncClient&#40;&#41;;
 *
 * &#47;&#47; Creates a client to receive messages from the first available session. It waits until
 * &#47;&#47; AmqpRetryOptions.getTryTimeout&#40;&#41; elapses. If no session is available within that operation timeout, it
 * &#47;&#47; completes with a retriable error. Otherwise, a receiver is returned when a lock on the session is acquired.
 * Mono&lt;ServiceBusReceiverAsyncClient&gt; receiverMono = sessionReceiver.acceptNextSession&#40;&#41;;
 *
 * Flux&lt;Void&gt; receiveMessagesFlux = Flux.usingWhen&#40;receiverMono,
 *     receiver -&gt; receiver.receiveMessages&#40;&#41;.flatMap&#40;message -&gt; &#123;
 *         System.out.println&#40;&quot;Received message: &quot; + message.getBody&#40;&#41;&#41;;
 *
 *         &#47;&#47; Explicitly settle the message via complete, abandon, defer, dead-letter, etc.
 *         if &#40;isMessageProcessed&#41; &#123;
 *             return receiver.complete&#40;message&#41;;
 *         &#125; else &#123;
 *             return receiver.abandon&#40;message&#41;;
 *         &#125;
 *     &#125;&#41;,
 *     receiver -&gt; Mono.fromRunnable&#40;&#40;&#41; -&gt; &#123;
 *         &#47;&#47; Dispose of the receiver and sessionReceiver when done receiving messages.
 *         receiver.close&#40;&#41;;
 *         sessionReceiver.close&#40;&#41;;
 *     &#125;&#41;&#41;;
 *
 * &#47;&#47; This is a non-blocking call that moves onto the next line of code after setting up and starting the receive
 * &#47;&#47; operation. Customers can keep a reference to `subscription` and dispose of it when they want to stop
 * &#47;&#47; receiving messages.
 * Disposable subscription = receiveMessagesFlux.subscribe&#40;unused -&gt; &#123;
 * &#125;, error -&gt; System.out.println&#40;&quot;Error occurred: &quot; + error&#41;,
 *     &#40;&#41; -&gt; System.out.println&#40;&quot;Receiving complete.&quot;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.messaging.servicebus.servicebusreceiverasyncclient.instantiation#nextsession -->
 *
 * @see ServiceBusClientBuilder
 */
@ServiceClient(builder = ServiceBusClientBuilder.class, isAsync = true)
public final class ServiceBusSessionReceiverAsyncClient implements AutoCloseable {
    private static final ClientLogger LOGGER = new ClientLogger(ServiceBusSessionReceiverAsyncClient.class);
    private static final int LIST_SESSIONS_DEFAULT_PAGE_SIZE = 100;
    /**
     * Continuation-token format is {@code <decimal-skip>|<base64url-utf8(lastSessionId)>}. The
     * {@code |} is safe as a separator because the URL-safe Base64 alphabet (A-Z, a-z, 0-9, '-',
     * '_') does not contain it, so any byte sequence in {@code lastSessionId} survives a round
     * trip without escaping.
     */
    private static final char CURSOR_SEPARATOR = '|';

    private final String fullyQualifiedNamespace;
    private final String entityPath;
    private final MessagingEntityType entityType;
    private final ReceiverOptions receiverOptions;
    private final ConnectionCacheWrapper connectionCacheWrapper;
    private final ServiceBusReceiverInstrumentation instrumentation;
    private final ServiceBusTracer tracer;
    private final MessageSerializer messageSerializer;
    private final Runnable onClientClose;
    private final ServiceBusSessionManager unNamedSessionManager;  // to obtain a session in V1
    private final ServiceBusSessionAcquirer sessionAcquirer;       // to obtain a session in V2
    private final String identifier;

    ServiceBusSessionReceiverAsyncClient(String fullyQualifiedNamespace, String entityPath,
        MessagingEntityType entityType, ReceiverOptions receiverOptions, ConnectionCacheWrapper connectionCacheWrapper,
        ServiceBusReceiverInstrumentation instrumentation, MessageSerializer messageSerializer, Runnable onClientClose,
        String identifier, boolean timeoutRetryDisabled) {
        this.fullyQualifiedNamespace
            = Objects.requireNonNull(fullyQualifiedNamespace, "'fullyQualifiedNamespace' cannot be null.");
        this.entityPath = Objects.requireNonNull(entityPath, "'entityPath' cannot be null.");
        this.entityType = Objects.requireNonNull(entityType, "'entityType' cannot be null.");
        this.receiverOptions = Objects.requireNonNull(receiverOptions, "'receiveOptions cannot be null.'");
        this.connectionCacheWrapper
            = Objects.requireNonNull(connectionCacheWrapper, "'connectionCacheWrapper' cannot be null.");
        this.instrumentation = Objects.requireNonNull(instrumentation, "'instrumentation' cannot be null.");
        this.messageSerializer = Objects.requireNonNull(messageSerializer, "'messageSerializer' cannot be null.");
        this.onClientClose = Objects.requireNonNull(onClientClose, "'onClientClose' cannot be null.");
        if (connectionCacheWrapper.isV2()) {
            this.sessionAcquirer = new ServiceBusSessionAcquirer(LOGGER, identifier, entityPath, entityType,
                receiverOptions.getReceiveMode(), connectionCacheWrapper.getRetryOptions().getTryTimeout(),
                timeoutRetryDisabled, connectionCacheWrapper);
            this.unNamedSessionManager = null;
        } else {
            this.unNamedSessionManager = new ServiceBusSessionManager(entityPath, entityType, connectionCacheWrapper,
                messageSerializer, receiverOptions, identifier, instrumentation.getTracer());
            this.sessionAcquirer = null;
        }
        this.identifier = identifier;
        this.tracer = instrumentation.getTracer();
    }

    /**
     * Acquires a session lock for the next available session and creates a {@link ServiceBusReceiverAsyncClient}
     * to receive messages from the session. It will wait until a session is available if none is immediately
     * available.
     *
     * @return A {@link ServiceBusReceiverAsyncClient} that is tied to the available session.
     *
     * @throws UnsupportedOperationException if the queue or topic subscription is not session-enabled.
     * @throws AmqpException if the operation times out. The timeout duration is the tryTimeout
     *      of when you build this client with the {@link ServiceBusClientBuilder#retryOptions(AmqpRetryOptions)}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ServiceBusReceiverAsyncClient> acceptNextSession() {
        if (sessionAcquirer != null) {
            return acquireSpecificOrNextSession(null, sessionAcquirer);
        }

        return tracer.traceMono("ServiceBus.acceptNextSession",
            unNamedSessionManager.getActiveLink().flatMap(receiveLink -> receiveLink.getSessionId().map(sessionId -> {
                final ReceiverOptions newReceiverOptions
                    = createNamedSessionOptions(receiverOptions.getReceiveMode(), receiverOptions.getPrefetchCount(),
                        receiverOptions.getMaxLockRenewDuration(), receiverOptions.isEnableAutoComplete(), sessionId);
                final ServiceBusSessionManager sessionSpecificManager
                    = new ServiceBusSessionManager(entityPath, entityType, connectionCacheWrapper, messageSerializer,
                        newReceiverOptions, receiveLink, identifier, instrumentation.getTracer());
                return new ServiceBusReceiverAsyncClient(fullyQualifiedNamespace, entityPath, entityType,
                    newReceiverOptions, connectionCacheWrapper, ServiceBusConstants.OPERATION_TIMEOUT, instrumentation,
                    messageSerializer, () -> {
                    }, sessionSpecificManager);
            })));

    }

    /**
     * Acquires a session lock for {@code sessionId} and create a {@link ServiceBusReceiverAsyncClient}
     * to receive messages from the session. If the session is already locked by another client, an
     * {@link AmqpException} is thrown.
     *
     * @param sessionId The session id.
     *
     * @return A {@link ServiceBusReceiverAsyncClient} that is tied to the specified session.
     *
     * @throws NullPointerException if {@code sessionId} is null.
     * @throws IllegalArgumentException if {@code sessionId} is empty.
     * @throws UnsupportedOperationException if the queue or topic subscription is not session-enabled.
     * @throws AmqpException if the lock cannot be acquired.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ServiceBusReceiverAsyncClient> acceptSession(String sessionId) {
        if (sessionId == null) {
            return monoError(LOGGER, new NullPointerException("'sessionId' cannot be null"));
        }
        if (CoreUtils.isNullOrEmpty(sessionId)) {
            return monoError(LOGGER, new IllegalArgumentException("'sessionId' cannot be empty"));
        }

        if (sessionAcquirer != null) {
            return acquireSpecificOrNextSession(sessionId, sessionAcquirer);
        }

        final ReceiverOptions newReceiverOptions
            = createNamedSessionOptions(receiverOptions.getReceiveMode(), receiverOptions.getPrefetchCount(),
                receiverOptions.getMaxLockRenewDuration(), receiverOptions.isEnableAutoComplete(), sessionId);
        final ServiceBusSessionManager sessionSpecificManager = new ServiceBusSessionManager(entityPath, entityType,
            connectionCacheWrapper, messageSerializer, newReceiverOptions, identifier, instrumentation.getTracer());

        return tracer.traceMono("ServiceBus.acceptSession",
            sessionSpecificManager.getActiveLink()
                .map(receiveLink -> new ServiceBusReceiverAsyncClient(fullyQualifiedNamespace, entityPath, entityType,
                    newReceiverOptions, connectionCacheWrapper, ServiceBusConstants.OPERATION_TIMEOUT, instrumentation,
                    messageSerializer, () -> {
                    }, sessionSpecificManager)));
    }

    private Mono<ServiceBusReceiverAsyncClient> acquireSpecificOrNextSession(String specificSessionId,
        ServiceBusSessionAcquirer sessionAcquirer) {
        final Mono<ServiceBusSessionAcquirer.Session> acquireSession;
        if (specificSessionId != null) {
            acquireSession = sessionAcquirer.acquire(specificSessionId);
        } else {
            acquireSession = sessionAcquirer.acquire();
        }
        final Mono<ServiceBusReceiverAsyncClient> acquireSessionReceiver = acquireSession.map(session -> {
            final ServiceBusSessionReactorReceiver sessionReceiver = new ServiceBusSessionReactorReceiver(LOGGER,
                tracer, session, null, receiverOptions.getMaxLockRenewDuration());

            final ServiceBusSingleSessionManager sessionManager = new ServiceBusSingleSessionManager(LOGGER, identifier,
                sessionReceiver, receiverOptions.getPrefetchCount(), messageSerializer,
                connectionCacheWrapper.getRetryOptions(), instrumentation);

            final ReceiverOptions newReceiverOptions
                = createNamedSessionOptions(receiverOptions.getReceiveMode(), receiverOptions.getPrefetchCount(),
                    receiverOptions.getMaxLockRenewDuration(), receiverOptions.isEnableAutoComplete(), session.getId());
            return new ServiceBusReceiverAsyncClient(fullyQualifiedNamespace, entityPath, entityType,
                newReceiverOptions, connectionCacheWrapper, ServiceBusConstants.OPERATION_TIMEOUT, instrumentation,
                messageSerializer, () -> {
                }, sessionManager);
        });
        return tracer.traceMono("ServiceBus.acceptSession", acquireSessionReceiver);
    }

    /**
     * Lists the IDs of sessions that have active messages in this entity.
     *
     * <p>Only sessions with active messages in the queue or subscription are returned.
     * Sessions on the dead-letter queue or sessions having only a session state (but no messages)
     * are not returned.</p>
     *
     * <p>The returned {@link PagedFlux} fetches additional pages from the broker on demand using
     * cursor-based pagination (server-returned {@code skip} plus {@code lastSessionId} of the
     * previous page) and terminates when the broker returns an empty page. The default page size
     * is 100; callers can request a different size via
     * {@link PagedFlux#byPage(int)}.</p>
     *
     * @return A {@link PagedFlux} of session ID strings.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<String> listSessions() {
        // Wire value matches Track 1's SessionBrowser.MAXDATE so the broker switches into the
        // active-messages mode it has historically been validated against.
        return listSessionsInternal(ManagementConstants.ACTIVE_MESSAGES_SENTINEL);
    }

    /**
     * Lists the IDs of sessions whose state was updated after the specified time.
     *
     * <p>The returned {@link PagedFlux} fetches additional pages from the broker on demand using
     * cursor-based pagination (server-returned {@code skip} plus {@code lastSessionId} of the
     * previous page) and terminates when the broker returns an empty page. The default page size
     * is 100; callers can request a different size via
     * {@link PagedFlux#byPage(int)}.</p>
     *
     * <p>Values at or beyond the active-messages sentinel value
     * ({@code new Date(253402300800000L)}, rendered by {@code OffsetDateTime.toString()} as
     * {@code +10000-01-01T00:00Z}, matching Track 1's {@code SessionBrowser.MAXDATE}) are clamped
     * to that sentinel and behave the same as {@link #listSessions()}, returning sessions that
     * have active messages.</p>
     *
     * @param sessionStateUpdatedAfter Only sessions whose session state was updated after this time are returned.
     * @return A {@link PagedFlux} of session ID strings.
     * @throws NullPointerException if {@code sessionStateUpdatedAfter} is null.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<String> listSessions(OffsetDateTime sessionStateUpdatedAfter) {
        if (sessionStateUpdatedAfter == null) {
            return pagedFluxError(LOGGER, new NullPointerException("'sessionStateUpdatedAfter' cannot be null."));
        }
        return listSessionsInternal(sessionStateUpdatedAfter);
    }

    private PagedFlux<String> listSessionsInternal(OffsetDateTime lastUpdatedTime) {
        // Use the page-size-aware PagedFlux constructor so a caller's byPage(int) value flows
        // through to the management request's `top` parameter. When the caller doesn't request a
        // specific page size, pageSize is null and we fall back to the default. The first lambda
        // is the first-page retriever. The next-page retriever may also be invoked directly when
        // a caller starts from a continuation token via byPage(token) without going through any
        // previous PagedResponse, so it must validate the token it receives. Note: in azure-core,
        // byPage(null) returns Flux.empty() rather than routing to the first-page retriever, so a
        // null continuation token never reaches this lambda.
        return new PagedFlux<>(pageSize -> fetchSessionPage(lastUpdatedTime, 0, null, resolvePageSize(pageSize)),
            (continuationToken, pageSize) -> {
                // Treat an empty continuation token as "no more pages", matching
                // ServiceBusAdministrationAsyncClient.listQueuesNextPage / listRulesNextPage and
                // the wider Azure SDK paging convention. This is tolerant of callers that persist
                // the token to storage and read back an empty string.
                if (continuationToken.isEmpty()) {
                    return Mono.empty();
                }
                final int separator = continuationToken.indexOf(CURSOR_SEPARATOR);
                if (separator < 0) {
                    return monoError(LOGGER, new IllegalArgumentException(
                        "Invalid continuation token. Expected format '<skip>|<base64url(lastSessionId)>'."));
                }

                final int nextSkip;
                try {
                    nextSkip = Integer.parseInt(continuationToken.substring(0, separator));
                } catch (NumberFormatException ex) {
                    return monoError(LOGGER, new IllegalArgumentException(
                        "Invalid continuation token. Expected a numeric skip value before the '|' separator.", ex));
                }
                if (nextSkip < 0) {
                    return monoError(LOGGER, new IllegalArgumentException(
                        "Invalid continuation token. Skip value must be non-negative; got " + nextSkip + "."));
                }

                final String lastSessionId;
                try {
                    final byte[] decoded = Base64.getUrlDecoder().decode(continuationToken.substring(separator + 1));
                    // Strict UTF-8 decoding: a token whose payload base64-decodes cleanly but isn't valid
                    // UTF-8 must be rejected, otherwise new String(decoded, UTF_8) silently substitutes
                    // U+FFFD and we'd send a corrupted session ID to the broker as the cursor.
                    lastSessionId = StandardCharsets.UTF_8.newDecoder()
                        .onMalformedInput(CodingErrorAction.REPORT)
                        .onUnmappableCharacter(CodingErrorAction.REPORT)
                        .decode(ByteBuffer.wrap(decoded))
                        .toString();
                } catch (IllegalArgumentException ex) {
                    return monoError(LOGGER, new IllegalArgumentException(
                        "Invalid continuation token. Expected base64url-encoded UTF-8 bytes after the '|' separator.",
                        ex));
                } catch (CharacterCodingException ex) {
                    return monoError(LOGGER, new IllegalArgumentException(
                        "Invalid continuation token. Decoded bytes after the '|' separator are not valid UTF-8.", ex));
                }

                return fetchSessionPage(lastUpdatedTime, nextSkip, lastSessionId, resolvePageSize(pageSize));
            });
    }

    /**
     * Resolves the per-page request size: a positive caller-supplied value (via {@code byPage(int)})
     * is honored, anything else (null or non-positive) falls back to the default.
     */
    private static int resolvePageSize(Integer requested) {
        return requested != null && requested > 0 ? requested : LIST_SESSIONS_DEFAULT_PAGE_SIZE;
    }

    private Mono<PagedResponse<String>> fetchSessionPage(OffsetDateTime lastUpdatedTime, int skip, String lastSessionId,
        int pageSize) {
        // Wrap each page fetch in a tracing span so distributed traces show one
        // "ServiceBus.listSessions" span per page, matching the tracing pattern used by
        // acceptSession/acceptNextSession in this client.
        final Mono<PagedResponse<String>> pageMono = connectionCacheWrapper.getConnection()
            .flatMap(connection -> connection.getManagementNode(entityPath, entityType))
            .flatMap(
                managementNode -> managementNode.getMessageSessions(lastUpdatedTime, skip, pageSize, lastSessionId))
            .map(result -> {
                final java.util.List<String> sessionIds = result.getSessionIds();
                // Empty page terminates pagination (matches Track 1's SessionBrowser loop and the
                // broker contract). Continuation token encodes the server-returned skip and the
                // last session ID of the page so the next call uses the same cursor Track 1 does.
                // Base64url-encode the session ID so arbitrary byte sequences (including the '|'
                // separator) round-trip without escaping.
                final String continuationToken;
                if (sessionIds.isEmpty()) {
                    continuationToken = null;
                } else {
                    final String last = sessionIds.get(sessionIds.size() - 1);
                    final String encoded
                        = Base64.getUrlEncoder().withoutPadding().encodeToString(last.getBytes(StandardCharsets.UTF_8));
                    continuationToken = result.getNextSkip() + String.valueOf(CURSOR_SEPARATOR) + encoded;
                }
                // Allocate a fresh HttpHeaders per page so callers cannot mutate a shared
                // instance (HttpHeaders is mutable and PagedResponseBase exposes the reference
                // via getHeaders()).
                return new PagedResponseBase<Void, String>(null, 200, new HttpHeaders(), sessionIds, continuationToken,
                    null);
            });
        return tracer.traceMono("ServiceBus.listSessions", pageMono);
    }

    @Override
    public void close() {
        if (this.unNamedSessionManager != null) {
            this.unNamedSessionManager.close();
        }
        this.onClientClose.run();
    }
}
