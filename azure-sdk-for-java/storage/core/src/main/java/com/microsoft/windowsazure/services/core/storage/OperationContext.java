/**
 * Copyright 2011 Microsoft Corporation
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
/**
 * 
 */
package com.microsoft.windowsazure.services.core.storage;

import java.net.HttpURLConnection;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Represents the current logical operation. A logical operation has potentially a one-to-many relationship with
 * individual physical requests.
 */
public final class OperationContext {

    /**
     * Represents the operation latency, in milliseconds, from the client's perspective. This may include any potential
     * retries.
     */
    private long clientTimeInMs;

    /**
     * The UUID representing the client side trace ID.
     */
    // V2 expose when logging is available.
    @SuppressWarnings("unused")
    private final String clientTraceID;

    /**
     * The Logger object associated with this operation.
     */
    private Logger logger;

    /**
     * Represents request results, in the form of an <code>ArrayList</code> object that contains the
     * {@link RequestResult} objects, for each physical request that is made.
     */
    private ArrayList<RequestResult> requestResults;

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
     * Represents the current operation state object.
     */
    protected Object operationState;

    /**
     * Reserved for internal use.
     */
    // Used internally for download resume.
    private volatile int currentOperationByteCount;

    /**
     * Creates an instance of the <code>OperationContext</code> class.
     */
    public OperationContext() {
        this.clientTraceID = UUID.randomUUID().toString();
        this.requestResults = new ArrayList<RequestResult>();
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
    public int getCurrentOperationByteCount() {
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
    public RequestResult getLastResult() {
        if (this.requestResults == null || this.requestResults.size() == 0) {
            return null;
        }
        else {
            return this.requestResults.get(this.requestResults.size() - 1);
        }
    }

    /**
     * Gets the <code>Logger</code> associated with this operation.
     * 
     * @return the <code>Logger</code> associated with this operation
     */
    @SuppressWarnings("unused")
    private Logger getLogger() {
        // V2 throw on null logger reference , use default dummy logger.
        return this.logger;
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
     * @return the responseReceivedEventHandler
     */
    public StorageEventMultiCaster<ResponseReceivedEvent, StorageEvent<ResponseReceivedEvent>> getResponseReceivedEventHandler() {
        return this.responseReceivedEventHandler;
    }

    /**
     * Initializes the OperationContext in order to begin processing a new operation. All operation specific information
     * is erased.
     */
    public void initialize() {
        this.setClientTimeInMs(0);
        this.requestResults.clear();
        this.setIntermediateMD5(null);
        this.operationState = null;
        this.setCurrentRequestObject(null);
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
    public void setCurrentOperationByteCount(final int currentOperationByteCount) {
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
     * Sets the <code>Logger</code> for this operation.
     * 
     * @param logger
     *            the <code>Logger</code> to use for this operation
     */
    @SuppressWarnings("unused")
    private void setLogger(final Logger logger) {
        this.logger = logger;
    }

    /**
     * @param responseReceivedEventHandler
     *            the responseReceivedEventHandler to set
     */
    public void setResponseReceivedEventHandler(
            final StorageEventMultiCaster<ResponseReceivedEvent, StorageEvent<ResponseReceivedEvent>> responseReceivedEventHandler) {
        this.responseReceivedEventHandler = responseReceivedEventHandler;
    }
}
