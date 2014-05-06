/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microsoft.azure.storage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.slf4j.Logger;

/**
 * Represents the current logical operation. A logical operation has potentially a one-to-many relationship with
 * individual physical requests.
 */
public final class OperationContext {

    /**
     * Name of the {@link org.slf4j.Logger} that will be created by default if logging is enabled and a
     * {@link org.slf4j.Logger} has not been specified.
     */
    public static final String defaultLoggerName = Logger.ROOT_LOGGER_NAME;

    /**
     * Whether or not the client library should produce log entries by default. The default can be overridden to
     * turn on logging for an individual operation context instance by using setLoggingEnabled.
     */
    private static boolean enableLoggingByDefault = false;

    /**
     * Represents the operation latency, in milliseconds, from the client's perspective. This may include any potential
     * retries.
     */
    private long clientTimeInMs;

    /**
     * The UUID representing the client side trace ID.
     */
    private String clientRequestID;

    /**
     * The boolean representing whether or not to enable to logging for a given operation context.
     */
    private Boolean enableLogging;

    /**
     * The {@link org.slf4j.Logger} object associated with this operation.
     */
    private org.slf4j.Logger logger;

    /**
     * Represents request results, in the form of an <code>ArrayList</code> object that contains the
     * {@link RequestResult} objects, for each physical request that is made.
     */
    private final ArrayList<RequestResult> requestResults;

    /**
     * Represents additional headers on the request, for example, for proxy or logging information.
     */
    private HashMap<String, String> userHeaders;

    /**
     * Represents an event that is triggered before sending a request.
     * 
     * @see StorageEvent
     * @see StorageEventMultiCaster
     * @see SendingRequestEvent
     */
    private static StorageEventMultiCaster<SendingRequestEvent, StorageEvent<SendingRequestEvent>> globalSendingRequestEventHandler = new StorageEventMultiCaster<SendingRequestEvent, StorageEvent<SendingRequestEvent>>();

    /**
     * Represents an event that is triggered when a response is received from the storage service while processing a
     * request.
     * 
     * @see StorageEvent
     * @see StorageEventMultiCaster
     * @see ResponseReceivedEvent
     */
    private static StorageEventMultiCaster<ResponseReceivedEvent, StorageEvent<ResponseReceivedEvent>> globalResponseReceivedEventHandler = new StorageEventMultiCaster<ResponseReceivedEvent, StorageEvent<ResponseReceivedEvent>>();

    /**
     * Represents an event that is triggered when a response received from the service is fully processed.
     * 
     * @see StorageEvent
     * @see StorageEventMultiCaster
     * @see RequestCompletedEvent
     */
    private static StorageEventMultiCaster<RequestCompletedEvent, StorageEvent<RequestCompletedEvent>> globalRequestCompletedEventHandler = new StorageEventMultiCaster<RequestCompletedEvent, StorageEvent<RequestCompletedEvent>>();

    /**
     * Represents an event that is triggered when a request is retried.
     * 
     * @see StorageEvent
     * @see StorageEventMultiCaster
     * @see RetryingEvent
     */
    private static StorageEventMultiCaster<RetryingEvent, StorageEvent<RetryingEvent>> globalRetryingEventHandler = new StorageEventMultiCaster<RetryingEvent, StorageEvent<RetryingEvent>>();

    /**
     * Represents an event that is triggered before sending a request.
     * 
     * @see StorageEvent
     * @see StorageEventMultiCaster
     * @see SendingRequestEvent
     */
    private StorageEventMultiCaster<SendingRequestEvent, StorageEvent<SendingRequestEvent>> sendingRequestEventHandler = new StorageEventMultiCaster<SendingRequestEvent, StorageEvent<SendingRequestEvent>>();

    /**
     * Represents an event that is triggered when a response is received from the storage service while processing a
     * request.
     * 
     * @see StorageEvent
     * @see StorageEventMultiCaster
     * @see ResponseReceivedEvent
     */
    private StorageEventMultiCaster<ResponseReceivedEvent, StorageEvent<ResponseReceivedEvent>> responseReceivedEventHandler = new StorageEventMultiCaster<ResponseReceivedEvent, StorageEvent<ResponseReceivedEvent>>();

    /**
     * Represents an event that is triggered when a response received from the service is fully processed.
     * 
     * @see StorageEvent
     * @see StorageEventMultiCaster
     * @see RequestCompletedEvent
     */
    private StorageEventMultiCaster<RequestCompletedEvent, StorageEvent<RequestCompletedEvent>> requestCompletedEventHandler = new StorageEventMultiCaster<RequestCompletedEvent, StorageEvent<RequestCompletedEvent>>();

    /**
     * Represents an event that is triggered when a response is received from the storage service while processing a
     * request.
     * 
     * @see StorageEvent
     * @see StorageEventMultiCaster
     * @see RetryingEvent
     */
    private StorageEventMultiCaster<RetryingEvent, StorageEvent<RetryingEvent>> retryingEventHandler = new StorageEventMultiCaster<RetryingEvent, StorageEvent<RetryingEvent>>();

    /**
     * Creates an instance of the <code>OperationContext</code> class.
     */
    public OperationContext() {
        this.clientRequestID = UUID.randomUUID().toString();
        this.requestResults = new ArrayList<RequestResult>();
    }

    /**
     * Get the client side trace ID.
     * 
     * @return the clientRequestID
     */
    public String getClientRequestID() {
        return this.clientRequestID;
    }

    /**
     * Gets the operation latency, in milliseconds, from the client's perspective. This may include any potential
     * retries.
     * 
     * @return the clientTimeInMs
     */
    public long getClientTimeInMs() {
        return this.clientTimeInMs;
    }

    /**
     * Returns the last request result encountered for the operation.
     * 
     * @return A {@link RequestResult} object that represents the last request result.
     */
    public synchronized RequestResult getLastResult() {
        if (this.requestResults == null || this.requestResults.size() == 0) {
            return null;
        }
        else {
            return this.requestResults.get(this.requestResults.size() - 1);
        }
    }

    /**
     * Gets the {@link org.slf4j.Logger} associated with this operation, or the class's default {@link org.slf4j.Logger}
     * if null.
     * 
     * @return the {@link org.slf4j.Logger} associated with this operation
     */
    public org.slf4j.Logger getLogger() {
        if (this.logger == null) {
            setDefaultLoggerSynchronized();
        }

        return this.logger;
    }

    /**
     * Gets any additional headers for the request, for example, for proxy or logging information.
     * 
     * @return the userHeaders
     */
    public HashMap<String, String> getUserHeaders() {
        return this.userHeaders;
    }

    /**
     * Sets the default logger
     * 
     * This is in a separate method so that it's synchronized, just in case multiple threads are
     * sharing the OperationContext.
     */
    private synchronized void setDefaultLoggerSynchronized() {
        if (this.logger == null) {
            this.logger = org.slf4j.LoggerFactory.getLogger(OperationContext.defaultLoggerName);
        }
    }

    /**
     * Returns the set of request results that the current operation has created.
     * 
     * @return An <code>ArrayList</code> object that contains {@link RequestResult} objects that represent the request
     *         results created by the current operation.
     */
    public ArrayList<RequestResult> getRequestResults() {
        return this.requestResults;
    }

    /**
     * Reserved for internal use. appends a {@link RequestResult} object to the internal collection in a synchronized
     * manner.
     * 
     * @param requestResult
     */
    public synchronized void appendRequestResult(RequestResult requestResult) {
        this.requestResults.add(requestResult);
    }

    /**
     * Gets a global event multi-caster that is triggered before sending a request. It allows event listeners to be
     * dynamically added and removed.
     * 
     * @return the globalSendingRequestEventHandler
     */
    public static StorageEventMultiCaster<SendingRequestEvent, StorageEvent<SendingRequestEvent>> getGlobalSendingRequestEventHandler() {
        return OperationContext.globalSendingRequestEventHandler;
    }

    /**
     * Gets a global event multi-caster that is triggered when a response is received. It allows event listeners to be
     * dynamically added and removed.
     * 
     * @return the globalResponseReceivedEventHandler
     */
    public static StorageEventMultiCaster<ResponseReceivedEvent, StorageEvent<ResponseReceivedEvent>> getGlobalResponseReceivedEventHandler() {
        return OperationContext.globalResponseReceivedEventHandler;
    }

    /**
     * Gets a global event multi-caster that is triggered when a request is completed. It allows event listeners to be
     * dynamically added and removed.
     * 
     * @return the globalRequestCompletedEventHandler
     */
    public static StorageEventMultiCaster<RequestCompletedEvent, StorageEvent<RequestCompletedEvent>> getGlobalRequestCompletedEventHandler() {
        return OperationContext.globalRequestCompletedEventHandler;
    }

    /**
     * Gets a global event multi-caster that is triggered when a request is retried. It allows event listeners to be
     * dynamically added and removed.
     * 
     * @return the globalRetryingEventHandler
     */
    public static StorageEventMultiCaster<RetryingEvent, StorageEvent<RetryingEvent>> getGlobalRetryingEventHandler() {
        return OperationContext.globalRetryingEventHandler;
    }

    /**
     * Gets an event multi-caster that is triggered before sending a request. It allows event listeners to be
     * dynamically added and removed.
     * 
     * @return the sendingRequestEventHandler
     */
    public StorageEventMultiCaster<SendingRequestEvent, StorageEvent<SendingRequestEvent>> getSendingRequestEventHandler() {
        return this.sendingRequestEventHandler;
    }

    /**
     * Gets an event multi-caster that is triggered when a response is received. It allows event listeners to be
     * dynamically added and removed.
     * 
     * @return the responseReceivedEventHandler
     */
    public StorageEventMultiCaster<ResponseReceivedEvent, StorageEvent<ResponseReceivedEvent>> getResponseReceivedEventHandler() {
        return this.responseReceivedEventHandler;
    }

    /**
     * Gets an event multi-caster that is triggered when a request is completed. It allows event listeners to be
     * dynamically added and removed.
     * 
     * @return the requestCompletedEventHandler
     */
    public StorageEventMultiCaster<RequestCompletedEvent, StorageEvent<RequestCompletedEvent>> getRequestCompletedEventHandler() {
        return this.requestCompletedEventHandler;
    }

    /**
     * Gets an event multi-caster that is triggered when a request is retried. It allows event listeners to be
     * dynamically added and removed.
     * 
     * @return the retryingEventHandler
     */
    public StorageEventMultiCaster<RetryingEvent, StorageEvent<RetryingEvent>> getRetryingEventHandler() {
        return this.retryingEventHandler;
    }

    /**
     * Reserved for internal use. Initializes the OperationContext in order to begin processing a new operation. All
     * operation specific information is erased.
     */
    public void initialize() {
        this.setClientTimeInMs(0);
        this.requestResults.clear();
    }

    /**
     * A boolean representing whether or not log entries will be produced for this request.
     * 
     * @return the <code>boolean</code>, true if logging is enabled, false if it is disabled
     */
    public boolean isLoggingEnabled() {
        if (this.enableLogging == null) {
            return enableLoggingByDefault;
        }
        return this.enableLogging;
    }

    /**
     * Set the client side trace ID.
     * 
     * @param clientRequestID
     *            the clientRequestID to set
     */
    public void setClientRequestID(final String clientRequestID) {
        this.clientRequestID = clientRequestID;
    }

    /**
     * Reserved for internal use. Represents the operation latency, in milliseconds, from the client's perspective. This
     * may include any potential retries.
     * 
     * @param clientTimeInMs
     *            the clientTimeInMs to set
     */
    public void setClientTimeInMs(final long clientTimeInMs) {
        this.clientTimeInMs = clientTimeInMs;
    }

    /**
     * Sets the {@link org.slf4j.Logger} for this operation.
     * 
     * @param logger
     *            the {@link org.slf4j.Logger} to use for this operation
     */
    public void setLogger(final org.slf4j.Logger logger) {
        this.logger = logger;
    }

    /**
     * Sets any additional headers for the request, for example, for proxy or logging information.
     * 
     * @param userHeaders
     *            the userHeaders to set
     */
    public void setUserHeaders(final HashMap<String, String> userHeaders) {
        this.userHeaders = userHeaders;
    }

    /**
     * A boolean representing whether or not log entries will be produced for this request
     * 
     * @param loggingEnabled
     *            the <code>boolean</code>, true to enable logging, false to disable
     */
    public void setLoggingEnabled(boolean loggingEnabled) {
        this.enableLogging = loggingEnabled;
    }

    /**
     * Sets a global event multi-caster that is triggered before sending a request.
     * 
     * @param globalSendingRequestEventHandler
     *            the globalSendingRequestEventHandler to set
     */
    public static void setGlobalSendingRequestEventHandler(
            final StorageEventMultiCaster<SendingRequestEvent, StorageEvent<SendingRequestEvent>> globalSendingRequestEventHandler) {
        OperationContext.globalSendingRequestEventHandler = globalSendingRequestEventHandler;
    }

    /**
     * Sets a global event multi-caster that is triggered when a response is received.
     * 
     * @param globalResponseReceivedEventHandler
     *            the globalResponseReceivedEventHandler to set
     */
    public static void setGlobalResponseReceivedEventHandler(
            final StorageEventMultiCaster<ResponseReceivedEvent, StorageEvent<ResponseReceivedEvent>> globalResponseReceivedEventHandler) {
        OperationContext.globalResponseReceivedEventHandler = globalResponseReceivedEventHandler;
    }

    /**
     * Sets a global event multi-caster that is triggered when a request is completed.
     * 
     * @param globalRequestCompletedEventHandler
     *            the globalRequestCompletedEventHandler to set
     */
    public static void setGlobalRequestCompletedEventHandler(
            final StorageEventMultiCaster<RequestCompletedEvent, StorageEvent<RequestCompletedEvent>> globalRequestCompletedEventHandler) {
        OperationContext.globalRequestCompletedEventHandler = globalRequestCompletedEventHandler;
    }

    /**
     * Sets a global event multi-caster that is triggered when a request is retried.
     * 
     * @param globalRetryingEventHandler
     *            the globalRetryingEventHandler to set
     */
    public static void setGlobalRetryingEventHandler(
            final StorageEventMultiCaster<RetryingEvent, StorageEvent<RetryingEvent>> globalRetryingEventHandler) {
        OperationContext.globalRetryingEventHandler = globalRetryingEventHandler;
    }

    /**
     * Sets an event multi-caster that is triggered before sending a request.
     * 
     * @param sendingRequestEventHandler
     *            the sendingRequestEventHandler to set
     */
    public void setSendingRequestEventHandler(
            final StorageEventMultiCaster<SendingRequestEvent, StorageEvent<SendingRequestEvent>> sendingRequestEventHandler) {
        this.sendingRequestEventHandler = sendingRequestEventHandler;
    }

    /**
     * Sets an event multi-caster that is triggered when a response is received.
     * 
     * @param responseReceivedEventHandler
     *            the responseReceivedEventHandler to set
     */
    public void setResponseReceivedEventHandler(
            final StorageEventMultiCaster<ResponseReceivedEvent, StorageEvent<ResponseReceivedEvent>> responseReceivedEventHandler) {
        this.responseReceivedEventHandler = responseReceivedEventHandler;
    }

    /**
     * Sets an event multi-caster that is triggered when a request is completed.
     * 
     * @param requestCompletedEventHandler
     *            the requestCompletedEventHandler to set
     */
    public void setRequestCompletedEventHandler(
            final StorageEventMultiCaster<RequestCompletedEvent, StorageEvent<RequestCompletedEvent>> requestCompletedEventHandler) {
        this.requestCompletedEventHandler = requestCompletedEventHandler;
    }

    /**
     * Sets an event multi-caster that is triggered when a request is retried.
     * 
     * @param retryingEventHandler
     *            the retryingEventHandler to set
     */
    public void setRetryingEventHandler(
            final StorageEventMultiCaster<RetryingEvent, StorageEvent<RetryingEvent>> retryingEventHandler) {
        this.retryingEventHandler = retryingEventHandler;
    }

    /**
     * Gets a boolean value indicating whether or not the client library should produce log entries by default. The
     * default can be overridden to turn on logging for an individual operation context instance by using
     * setLoggingEnabled().
     * 
     * @return
     *         the boolean representing whether or not logging is enabled by default
     */
    public static boolean isLoggingEnabledByDefault() {
        return enableLoggingByDefault;
    }

    /**
     * Specifies whether or not the client library should produce log entries by default. The default can be overridden
     * to turn on logging for an individual operation context instance by using setLoggingEnabled.
     * 
     * @param enableLoggingByDefault
     *            the boolean representing whether or not logging is enabled by default
     */
    public static void setLoggingEnabledByDefault(boolean enableLoggingByDefault) {
        OperationContext.enableLoggingByDefault = enableLoggingByDefault;
    }
}
