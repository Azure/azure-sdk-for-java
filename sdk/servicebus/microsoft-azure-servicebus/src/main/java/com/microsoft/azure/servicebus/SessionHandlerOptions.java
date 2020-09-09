// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.servicebus;

import java.time.Duration;
import java.util.Locale;

/**
 * The OnSession handler processing options.
 */
public final class SessionHandlerOptions {
    private static final boolean DEFAULT_AUTO_COMPLETE = true;
    private static final int DEFAULT_MAX_CONCURRENT_SESSIONS = 1;
    private static final int DEFAULT_MAX_CONCURRENT_CALLS_PER_SESSION = 1;
    private static final int DEFAULT_MAX_RENEW_TIME_MINUTES = 5;
    private static final int DEFAULT_MESSAGE_WAIT_TIME_MINUTES = 1;

    private boolean autoComplete;
    private Duration maxAutoRenewDuration;
    private Duration messageWaitDuration;
    private int maxConcurrentSessions;
    private int maxConcurrentCallsPerSession;

    /**
     * Default constructor with default values
     * Default {@link SessionHandlerOptions#getMaxConcurrentSessions()} is 1
     * Default {@link SessionHandlerOptions#getMaxConcurrentCallsPerSession()} is 1
     * Default {@link SessionHandlerOptions#getMaxAutoRenewDuration()} is 5 minutes
     * Default {@link SessionHandlerOptions#isAutoComplete()} is true.
     * Default {@link SessionHandlerOptions#getMessageWaitDuration()} is 1 minute
     */
    public SessionHandlerOptions() {
        this(DEFAULT_MAX_CONCURRENT_SESSIONS, DEFAULT_AUTO_COMPLETE, Duration.ofMinutes(DEFAULT_MAX_RENEW_TIME_MINUTES));
    }

    /**
     * @param maxConcurrentSessions maximum number of concurrent sessions accepted by the session pump
     * @param autoComplete          true if the pump should automatically complete message after onMessageHandler action is completed. false otherwise.
     * @param maxAutoRenewDuration  - Maximum duration within which the client keeps renewing the session lock if the processing of the session messages or onclose action
     *                              is not completed by the handler.
     */
    public SessionHandlerOptions(int maxConcurrentSessions, boolean autoComplete, Duration maxAutoRenewDuration) {
        this(maxConcurrentSessions, DEFAULT_MAX_CONCURRENT_CALLS_PER_SESSION, autoComplete, maxAutoRenewDuration);
    }

    /**
     * @param maxConcurrentSessions        maximum number of concurrent sessions accepted by the session pump
     * @param maxConcurrentCallsPerSession maximum number of concurrent calls to the onMessage handler
     * @param autoComplete                 true if the pump should automatically complete message after onMessageHandler action is completed. false otherwise
     * @param maxAutoRenewDuration         Maximum duration within which the client keeps renewing the session lock if the processing of the session messages or onclose action
     *                                     is not completed by the handler.
     */
    public SessionHandlerOptions(int maxConcurrentSessions, int maxConcurrentCallsPerSession, boolean autoComplete, Duration maxAutoRenewDuration) {
        this(maxConcurrentSessions, maxConcurrentCallsPerSession, autoComplete, maxAutoRenewDuration, Duration.ofMinutes(DEFAULT_MESSAGE_WAIT_TIME_MINUTES));
    }

    /**
     * @param maxConcurrentSessions        maximum number of concurrent sessions accepted by the session pump
     * @param maxConcurrentCallsPerSession maximum number of concurrent calls to the onMessage handler
     * @param autoComplete                 true if the pump should automatically complete message after onMessageHandler action is completed. false otherwise
     * @param maxAutoRenewDuration         Maximum duration within which the client keeps renewing the session lock if the processing of the session messages or onclose action
     *                                     is not completed by the handler.
     * @param messageWaitDuration          Duration to wait for receiving the message
     */
    public SessionHandlerOptions(int maxConcurrentSessions, int maxConcurrentCallsPerSession, boolean autoComplete, Duration maxAutoRenewDuration, Duration messageWaitDuration) {
        this.maxConcurrentSessions = maxConcurrentSessions;
        this.maxConcurrentCallsPerSession = maxConcurrentCallsPerSession;
        this.autoComplete = autoComplete;
        this.maxAutoRenewDuration = maxAutoRenewDuration;
        this.messageWaitDuration = messageWaitDuration;
    }

    /**
     * Whether the auto complete is set to true.
     * If this value is true, if the handler returns without any failure, then the message is completed and will
     * not show up in the session; if any exception is thrown from the handler, the message is abandoned and the
     * DeliveryCount of this message will increase by one. If this value is false, if the handler returns without any
     * failure, then user has to write the logic to explicitly complete the message, otherwise the message is not
     * considered 'completed' and will reappear in the session.
     *
     * @return true to complete the message processing automatically on successful execution of the operation; otherwise, false.
     */
    public boolean isAutoComplete() {
        return this.autoComplete;
    }


    /**
     * Gets the maximum number of concurrent sessions that the pump should initiate.
     * Setting this value to be greater than the max number of active sessions in the service will not increase message throughput.
     * <remarks>The session-pump (SDK) will accept MaxConcurrentSessions number of sessions in parallel and dispatch the messages.
     * The messages within a session are delivered sequentially. If more than MaxConcurrentSessions number of sessions are present
     * in the entity, they will be accepted one-by-one after closing the existing sessions.</remarks>
     *
     * @return The maximum number of concurrent sessions
     */
    public int getMaxConcurrentSessions() {
        return this.maxConcurrentSessions;
    }

    /**
     * Gets the maximum number of concurrent calls to the callback the message pump should initiate for each session.
     *
     * @return The maximum number of concurrent calls to the callback.
     */
    public int getMaxConcurrentCallsPerSession() {
        return this.maxConcurrentCallsPerSession;
    }

    /**
     * Gets the maximum duration within which the lock will be renewed automatically. This value should be greater than the longest message lock duration; for example, the LockDuration Property.
     * If a session lock is going to expire, this value is the max duration for the session lock to be automatically renewed.
     *
     * @return The maximum duration during which locks are automatically renewed.
     */
    public Duration getMaxAutoRenewDuration() {
        return this.maxAutoRenewDuration;
    }

    /**
     * Gets the time to wait for receiving a message.
     * This is the time the session-pump waits before closing down the current session and switching to a different session.
     * <remarks>This value has an impact on the message throughput. If the value is very large, then every time the SDK waits
     * for this duration before closing to make sure that all the messages have been received. If users are having a lot of
     * sessions and fewer messages per session, try setting this to be a relative smaller value based on how frequent new
     * messages arrive in the session. </remarks>
     * 
     * @return The wait duration for receive calls. Defaults to 1 minute.
     */
    public Duration getMessageWaitDuration() {
        return this.messageWaitDuration;
    }

    @Override
    public String toString() {
        return String.format(Locale.US, "SessionHandlerOptions - AutoComplete:%s, MaxConcurrentSessions:%s, MaxConcurretnCallsPerSession:%s, MaxAutoRenewDuration:%s", this.autoComplete, this.maxConcurrentSessions, this.maxConcurrentCallsPerSession, this.maxAutoRenewDuration);
    }
}
