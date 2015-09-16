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

import java.net.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.slf4j.Logger;

/**
 * Represents the current logical operation. A logical operation may have a one-to-many relationship with
 * multiple individual physical requests.
 */
public final class OperationContext {

    /**
     * Name of the {@link org.slf4j.Logger} that will be created by default if logging is enabled and a
     * {@link org.slf4j.Logger} has not been specified.
     */
    public static final String defaultLoggerName = Logger.ROOT_LOGGER_NAME;

    /**
     * Indicates whether the client library should produce log entries by default. The default can be overridden to
     * enable logging for an individual operation context instance by using {@link setLoggingEnabled}.
     */
    private static boolean enableLoggingByDefault = false;

    /**
     * Represents a proxy to be used when making a request.
     */
    private Proxy proxy;

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
     * Indicates whether to enable logging for a given operation context.
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
     * Gets the client side trace ID.
     * 
     * @return A <code>String</cod> which represents the client request ID.
     */
    public String getClientRequestID() {
        return this.clientRequestID;
    }

    /**
     * Gets the operation latency, in milliseconds, from the client's perspective. This may include any potential
     * retries.
     * 
     * @return A <code>long</code> which contains the client latency time in milliseconds.
     */
    public long getClientTimeInMs() {
        return this.clientTimeInMs;
    }

    /**
     * Gets the last request result encountered for the operation.
     * 
     * @return A {@link RequestResult} object which represents the last request result.
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
     * Gets the {@link org.slf4j.Logger} associated with this operation. If the {@link org.slf4j.Logger} object 
     * associated with this operation is null, this will return the class's default {@link org.slf4j.Logger} object.
     * 
     * @return The {@link org.slf4j.Logger} object associated with this operation,
     *         or the class's default {@link org.slf4j.Logger} if null.
     */
    public org.slf4j.Logger getLogger() {
        if (this.logger == null) {
            setDefaultLoggerSynchronized();
        }

        return this.logger;
    }

    /**
     * Gets a proxy which will be used when making a request. Default is <code>null</code>.
     * 
     * @return A {@link java.net.Proxy} to use when making a request.
     */
    public Proxy getProxy() {
        return this.proxy;
    }

    /**
     * Gets any additional headers for the request, for example, for proxy or logging information.
     * 
     * @return A <code>java.util.HashMap</code> which contains the the user headers for the request.
     */
    public HashMap<String, String> getUserHeaders() {
        return this.userHeaders;
    }

    /**
     * Sets the default logger.
     * 
     * This is in a separate method so that it's synchronized, just in case multiple threads are
     * sharing the <code>OperationContext</code>.
     */
    private synchronized void setDefaultLoggerSynchronized() {
        if (this.logger == null) {
            this.logger = org.slf4j.LoggerFactory.getLogger(OperationContext.defaultLoggerName);
        }
    }

    /**
     * Returns the set of request results that the current operation has created.
     * 
     * @return An <code>ArrayList</code> object that contains {@link RequestResult} objects that represent
     *         the request results created by the current operation.
     */
    public ArrayList<RequestResult> getRequestResults() {
        return this.requestResults;
    }

    /**
     * Reserved for internal use. Appends a {@link RequestResult} object to the internal collection in a synchronized
     * manner.
     * 
     * @param requestResult
     *        A {@link RequestResult} to append.
     */
    public synchronized void appendRequestResult(RequestResult requestResult) {
        this.requestResults.add(requestResult);
    }

    /**
     * Gets a global event multi-caster that is triggered before sending a request. It allows event listeners to be
     * dynamically added and removed.
     * 
     * @return A {@link StorageEventMultiCaster} object for the <code>globalSendingRequestEventHandler</code>.
     */
    public static StorageEventMultiCaster<SendingRequestEvent, StorageEvent<SendingRequestEvent>> getGlobalSendingRequestEventHandler() {
        return OperationContext.globalSendingRequestEventHandler;
    }

    /**
     * Gets a global event multi-caster that is triggered when a response is received. It allows event listeners to be
     * dynamically added and removed.
     * 
     * @return A {@link StorageEventMultiCaster} object for the <code>globalResponseReceivedEventHandler</code>.
     */
    public static StorageEventMultiCaster<ResponseReceivedEvent, StorageEvent<ResponseReceivedEvent>> getGlobalResponseReceivedEventHandler() {
        return OperationContext.globalResponseReceivedEventHandler;
    }

    /**
     * Gets a global event multi-caster that is triggered when a request is completed. It allows event listeners to be
     * dynamically added and removed.
     * 
     * @return A {@link StorageEventMultiCaster} object for the <code>globalRequestCompletedEventHandler</code>.
     */
    public static StorageEventMultiCaster<RequestCompletedEvent, StorageEvent<RequestCompletedEvent>> getGlobalRequestCompletedEventHandler() {
        return OperationContext.globalRequestCompletedEventHandler;
    }

    /**
     * Gets a global event multi-caster that is triggered when a request is retried. It allows event listeners to be
     * dynamically added and removed.
     * 
     * @return A {@link StorageEventMultiCaster} object for the <code>globalRetryingEventHandler</code>.
     */
    public static StorageEventMultiCaster<RetryingEvent, StorageEvent<RetryingEvent>> getGlobalRetryingEventHandler() {
        return OperationContext.globalRetryingEventHandler;
    }

    /**
     * Gets an event multi-caster that is triggered before sending a request. It allows event listeners to be
     * dynamically added and removed.
     * 
     * @return A {@link StorageEventMultiCaster} object for the <code>sendingRequestEventHandler</code>.
     */
    public StorageEventMultiCaster<SendingRequestEvent, StorageEvent<SendingRequestEvent>> getSendingRequestEventHandler() {
        return this.sendingRequestEventHandler;
    }

    /**
     * Gets an event multi-caster that is triggered when a response is received. It allows event listeners to be
     * dynamically added and removed.
     * 
     * @return A {@link StorageEventMultiCaster} object for the <code>responseReceivedEventHandler</code>.
     */
    public StorageEventMultiCaster<ResponseReceivedEvent, StorageEvent<ResponseReceivedEvent>> getResponseReceivedEventHandler() {
        return this.responseReceivedEventHandler;
    }

    /**
     * Gets an event multi-caster that is triggered when a request is completed. It allows event listeners to be
     * dynamically added and removed.
     * 
     * @return A {@link StorageEventMultiCaster} object for the <code>requestCompletedEventHandler</code>.
     */
    public StorageEventMultiCaster<RequestCompletedEvent, StorageEvent<RequestCompletedEvent>> getRequestCompletedEventHandler() {
        return this.requestCompletedEventHandler;
    }

    /**
     * Gets an event multi-caster that is triggered when a request is retried. It allows event listeners to be
     * dynamically added and removed.
     * 
     * @return A {@link StorageEventMultiCaster} object for the <code>retryingEventHandler</code>.
     */
    public StorageEventMultiCaster<RetryingEvent, StorageEvent<RetryingEvent>> getRetryingEventHandler() {
        return this.retryingEventHandler;
    }

    /**
     * Reserved for internal use. Initializes the <code>OperationContext</code> in order to begin processing a
     * new operation. All operation specific information is erased.
     */
    public void initialize() {
        this.setClientTimeInMs(0);
        this.requestResults.clear();
    }

    /**
     * Indicates whether log entries will be produced for this request.
     * 
     * @return <code>true</code> if logging is enabled; otherwise <code>false</code>.
     */
    public boolean isLoggingEnabled() {
        if (this.enableLogging == null) {
            return enableLoggingByDefault;
        }
        return this.enableLogging;
    }

    /**
     * Sets the client side request ID.
     * 
     * @param clientRequestID
     *        A <code>String</code> which contains the client request ID to set.
     */
    public void setClientRequestID(final String clientRequestID) {
        this.clientRequestID = clientRequestID;
    }

    /**
     * Reserved for internal use. Represents the operation latency, in milliseconds, from the client's perspective. This
     * may include any potential retries.
     * 
     * @param clientTimeInMs
     *        A <code>long</code> which contains the client operation latency in milliseconds.
     */
    public void setClientTimeInMs(final long clientTimeInMs) {
        this.clientTimeInMs = clientTimeInMs;
    }

    /**
     * Sets the {@link org.slf4j.Logger} for this operation.
     * 
     * @param logger
     *        The <code>{@link org.slf4j.Logger}</code> to use for this operation.
     */
    public void setLogger(final org.slf4j.Logger logger) {
        this.logger = logger;
    }

    /**
     * Sets a proxy which will be used when making a request. Default is <code>null</code>.
     * 
     * @param proxy
     *            A {@link java.net.Proxy} to use when making a request.
     */
    public void setProxy(Proxy proxy) {
        this.proxy = proxy;
    }

    /**
     * Sets any additional headers for the request, for example, for proxy or logging information.
     * 
     * @param userHeaders
     *        A <code>java.util.HashMap</code> which contains any additional headers to set.
     */
    public void setUserHeaders(final HashMap<String, String> userHeaders) {
        this.userHeaders = userHeaders;
    }

    /**
     * Specifies whether log entries will be produced for this request.
     * 
     * @param loggingEnabled
     *        <code>true</code> to enable logging; otherwise <code>false</code> to disable logging.
     */
    public void setLoggingEnabled(boolean loggingEnabled) {
        this.enableLogging = loggingEnabled;
    }

    /**
     * Sets a global event multi-caster that is triggered before sending a request.
     * 
     * @param globalSendingRequestEventHandler
     *        The {@link StorageEventMultiCaster} object to set for the <code>globalSendingRequestEventHandler</code>.
     */
    public static void setGlobalSendingRequestEventHandler(
            final StorageEventMultiCaster<SendingRequestEvent, StorageEvent<SendingRequestEvent>> globalSendingRequestEventHandler) {
        OperationContext.globalSendingRequestEventHandler = globalSendingRequestEventHandler;
    }

    /**
     * Sets a global event multi-caster that is triggered when a response is received.
     * 
     * @param globalResponseReceivedEventHandler
     *        The {@link StorageEventMultiCaster} object to set for the <code>globalResponseReceivedEventHandler</code>.
     */
    public static void setGlobalResponseReceivedEventHandler(
            final StorageEventMultiCaster<ResponseReceivedEvent, StorageEvent<ResponseReceivedEvent>> globalResponseReceivedEventHandler) {
        OperationContext.globalResponseReceivedEventHandler = globalResponseReceivedEventHandler;
    }

    /**
     * Sets a global event multi-caster that is triggered when a request is completed.
     * 
     * @param globalRequestCompletedEventHandler
     *        The {@link StorageEventMultiCaster} object to set for the <code>globalRequestCompletedEventHandler</code>.
     */
    public static void setGlobalRequestCompletedEventHandler(
            final StorageEventMultiCaster<RequestCompletedEvent, StorageEvent<RequestCompletedEvent>> globalRequestCompletedEventHandler) {
        OperationContext.globalRequestCompletedEventHandler = globalRequestCompletedEventHandler;
    }

    /**
     * Sets a global event multi-caster that is triggered when a request is retried.
     * 
     * @param globalRetryingEventHandler
     *        The {@link StorageEventMultiCaster} object to set for the <code>globalRetryingEventHandler</code>.
     */
    public static void setGlobalRetryingEventHandler(
            final StorageEventMultiCaster<RetryingEvent, StorageEvent<RetryingEvent>> globalRetryingEventHandler) {
        OperationContext.globalRetryingEventHandler = globalRetryingEventHandler;
    }

    /**
     * Sets an event multi-caster that is triggered before sending a request.
     * 
     * @param sendingRequestEventHandler
     *        The {@link StorageEventMultiCaster} object to set for the <code>sendingRequestEventHandler</code>.
     */
    public void setSendingRequestEventHandler(
            final StorageEventMultiCaster<SendingRequestEvent, StorageEvent<SendingRequestEvent>> sendingRequestEventHandler) {
        this.sendingRequestEventHandler = sendingRequestEventHandler;
    }

    /**
     * Sets an event multi-caster that is triggered when a response is received.
     * 
     * @param responseReceivedEventHandler
     *        The {@link StorageEventMultiCaster} object to set for the <code>responseReceivedEventHandler</code>.
     */
    public void setResponseReceivedEventHandler(
            final StorageEventMultiCaster<ResponseReceivedEvent, StorageEvent<ResponseReceivedEvent>> responseReceivedEventHandler) {
        this.responseReceivedEventHandler = responseReceivedEventHandler;
    }

    /**
     * Sets an event multi-caster that is triggered when a request is completed.
     * 
     * @param requestCompletedEventHandler
     *        The {@link StorageEventMultiCaster} object to set for the <code>requestCompletedEventHandler</code>.
     */
    public void setRequestCompletedEventHandler(
            final StorageEventMultiCaster<RequestCompletedEvent, StorageEvent<RequestCompletedEvent>> requestCompletedEventHandler) {
        this.requestCompletedEventHandler = requestCompletedEventHandler;
    }

    /**
     * Sets an event multi-caster that is triggered when a request is retried.
     * 
     * @param retryingEventHandler
     *        The {@link StorageEventMultiCaster} object to set for the <code>retryingEventHandler</code>.
     */
    public void setRetryingEventHandler(
            final StorageEventMultiCaster<RetryingEvent, StorageEvent<RetryingEvent>> retryingEventHandler) {
        this.retryingEventHandler = retryingEventHandler;
    }

    /**
     * Indicates whether the client library should produce log entries by default. The default can be overridden
     * to enable logging for an individual operation context instance by using {@link #setLoggingEnabled}.
     * 
     * @return
     *         <code>true</code> if logging is enabled by default; otherwise <code>false</code>.
     */
    public static boolean isLoggingEnabledByDefault() {
        return enableLoggingByDefault;
    }

    /**
     * Specifies whether the client library should produce log entries by default. The default can be overridden
     * to turn on logging for an individual operation context instance by using {@link #setLoggingEnabled}.
     * 
     * @param enableLoggingByDefault
     *        <code>true</code> if logging should be enabled by default; otherwise <code>false</code> if logging should
     *        be disabled by default.
     */
    public static void setLoggingEnabledByDefault(boolean enableLoggingByDefault) {
        OperationContext.enableLoggingByDefault = enableLoggingByDefault;
    }
}
