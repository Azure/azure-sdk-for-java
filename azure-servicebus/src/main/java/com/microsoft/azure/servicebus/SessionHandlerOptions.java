// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

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

    private boolean autoComplete;
    private Duration maxAutoRenewDuration;
    private int maxConcurrentSessions;
    private int maxConcurrentCallsPerSession;

    /**
     * Default constructor with default values
     * Default {@link SessionHandlerOptions#getMaxConcurrentSessions()} is 1
     * Default {@link SessionHandlerOptions#getMaxConcurrentCallsPerSession()} is 1
     * Default {@link SessionHandlerOptions#getMaxAutoRenewDuration()} is 5 minutes
     * Default {@link SessionHandlerOptions#isAutoComplete()} is true.
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
        this.maxConcurrentSessions = maxConcurrentSessions;
        this.maxConcurrentCallsPerSession = maxConcurrentCallsPerSession;
        this.autoComplete = autoComplete;
        this.maxAutoRenewDuration = maxAutoRenewDuration;
    }

    /**
     * Whether the auto complete is set to true.
     *
     * @return true to complete the message processing automatically on successful execution of the operation; otherwise, false.
     */
    public boolean isAutoComplete() {
        return this.autoComplete;
    }


    /**
     * Gets the maximum number of concurrent sessions that the pump should initiate.
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
     *
     * @return The maximum duration during which locks are automatically renewed.
     */
    public Duration getMaxAutoRenewDuration() {
        return this.maxAutoRenewDuration;
    }

    @Override
    public String toString() {
        return String.format(Locale.US, "SessionHandlerOptions - AutoComplete:%s, MaxConcurrentSessions:%s, MaxConcurretnCallsPerSession:%s, MaxAutoRenewDuration:%s", this.autoComplete, this.maxConcurrentSessions, this.maxConcurrentCallsPerSession, this.maxAutoRenewDuration);
    }
}
