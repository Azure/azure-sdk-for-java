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
package com.microsoft.windowsazure.storage;

import java.net.HttpURLConnection;
import java.security.MessageDigest;
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
     * Reserved for internal use.
     */
    // Represents the intermediate MD5 value, which is used in resuming downloads.
    private MessageDigest intermediateMD5;

    /**
     * Reserved for internal use. Represents the current request object, which is used in continuation.
     */
    private HttpURLConnection currentRequestObject;

    /**
     * Reserved for internal use.
     */
    // Used internally for download resume.
    private volatile long currentOperationByteCount;

    /**
     * Creates an instance of the <code>OperationContext</code> class.
     */
    public OperationContext() {
        this.clientRequestID = UUID.randomUUID().toString();
        this.requestResults = new ArrayList<RequestResult>();
    }

    /**
     * @return the clientRequestID
     */
    public String getClientRequestID() {
        return this.clientRequestID;
    }

    /**
     * @return the clientTimeInMs
     */
    public long getClientTimeInMs() {
        return this.clientTimeInMs;
    }

    /**
     * @return the currentOperationByteCount
     */
    public long getCurrentOperationByteCount() {
        return this.currentOperationByteCount;
    }

    /**
     * @return the currentRequestObject
     */
    public HttpURLConnection getCurrentRequestObject() {
        return this.currentRequestObject;
    }

    /**
     * @return the intermediateMD5
     */
    public MessageDigest getIntermediateMD5() {
        return this.intermediateMD5;
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
     * @return the SendingRequestEvent
     */
    public StorageEventMultiCaster<SendingRequestEvent, StorageEvent<SendingRequestEvent>> getSendingRequestEventHandler() {
        return this.sendingRequestEventHandler;
    }

    /**
     * Initializes the OperationContext in order to begin processing a new operation. All operation specific information
     * is erased.
     */
    public void initialize() {
        this.setClientTimeInMs(0);
        this.requestResults.clear();
        this.setIntermediateMD5(null);
        this.setCurrentRequestObject(null);
    }

    /**
     * @return the responseReceivedEventHandler
     */
    public StorageEventMultiCaster<ResponseReceivedEvent, StorageEvent<ResponseReceivedEvent>> getResponseReceivedEventHandler() {
        return this.responseReceivedEventHandler;
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
     * @param clientRequestID
     *            the clientRequestID to set
     */
    public void setClientRequestID(final String clientRequestID) {
        this.clientRequestID = clientRequestID;
    }

    /**
     * @param clientTimeInMs
     *            the clientTimeInMs to set
     */
    public void setClientTimeInMs(final long clientTimeInMs) {
        this.clientTimeInMs = clientTimeInMs;
    }

    /**
     * @param currentOperationByteCount
     *            the currentOperationByteCount to set
     */
    public void setCurrentOperationByteCount(final long currentOperationByteCount) {
        this.currentOperationByteCount = currentOperationByteCount;
    }

    /**
     * @param currentRequestObject
     *            the currentRequestObject to set
     */
    public void setCurrentRequestObject(final HttpURLConnection currentRequestObject) {
        this.currentRequestObject = currentRequestObject;
    }

    /**
     * @param intermediateMD5
     *            the intermediateMD5 to set
     */
    public void setIntermediateMD5(final MessageDigest intermediateMD5) {
        this.intermediateMD5 = intermediateMD5;
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
     * @param sendingRequestEventHandler
     *            the sendingRequestEventHandler to set
     */
    public void setSendingRequestEventHandler(
            final StorageEventMultiCaster<SendingRequestEvent, StorageEvent<SendingRequestEvent>> sendingRequestEventHandler) {
        this.sendingRequestEventHandler = sendingRequestEventHandler;
    }

    /**
     * @param responseReceivedEventHandler
     *            the responseReceivedEventHandler to set
     */
    public void setResponseReceivedEventHandler(
            final StorageEventMultiCaster<ResponseReceivedEvent, StorageEvent<ResponseReceivedEvent>> responseReceivedEventHandler) {
        this.responseReceivedEventHandler = responseReceivedEventHandler;
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
