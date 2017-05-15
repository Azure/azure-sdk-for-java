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

package com.microsoft.azure.storage.core;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.Date;
import java.util.Map.Entry;
import java.util.concurrent.TimeoutException;

import com.microsoft.azure.storage.Constants;
import com.microsoft.azure.storage.LocationMode;
import com.microsoft.azure.storage.OperationContext;
import com.microsoft.azure.storage.RequestCompletedEvent;
import com.microsoft.azure.storage.RequestResult;
import com.microsoft.azure.storage.ResponseReceivedEvent;
import com.microsoft.azure.storage.RetryContext;
import com.microsoft.azure.storage.RetryInfo;
import com.microsoft.azure.storage.RetryNoRetry;
import com.microsoft.azure.storage.RetryPolicy;
import com.microsoft.azure.storage.RetryPolicyFactory;
import com.microsoft.azure.storage.RetryingEvent;
import com.microsoft.azure.storage.SendingRequestEvent;
import com.microsoft.azure.storage.StorageErrorCodeStrings;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.StorageLocation;

/**
 * RESERVED FOR INTERNAL USE. A class that handles execution of StorageOperations and enforces retry policies.
 */
public final class ExecutionEngine {
    /**
     * Executes an operation and enforces a retrypolicy to handle any potential errors
     * 
     * @param <CLIENT_TYPE>
     *            The type of the service client
     * @param <PARENT_TYPE>
     *            The type of the parent object, i.e. CloudBlobContainer for downloadAttributes etc.
     * @param <RESULT_TYPE>
     *            The type of the expected result
     * @param client
     *            the service client associated with the request
     * @param parentObject
     *            the parent object
     * @param task
     *            the StorageRequest to execute
     * @param policyFactory
     *            the factory used to generate a new retry policy instance
     * @param opContext
     *            an object used to track the execution of the operation
     * @return the result of the operation
     * @throws StorageException
     *             an exception representing any error which occurred during the operation.
     */
    public static <CLIENT_TYPE, PARENT_TYPE, RESULT_TYPE> RESULT_TYPE executeWithRetry(final CLIENT_TYPE client,
            final PARENT_TYPE parentObject, final StorageRequest<CLIENT_TYPE, PARENT_TYPE, RESULT_TYPE> task,
            final RetryPolicyFactory policyFactory, final OperationContext opContext) throws StorageException {

        RetryPolicy policy = null;

        if (policyFactory == null) {
            policy = new RetryNoRetry();
        }
        else {
            policy = policyFactory.createInstance(opContext);

            // if the returned policy is null, set to not retry
            if (policy == null) {
                policy = new RetryNoRetry();
            }
        }

        int currentRetryCount = 0;
        StorageException translatedException = null;
        HttpURLConnection request = null;
        final long startTime = new Date().getTime();

        while (true) {
            try {
                // 1-4: setup the request
                request = setupStorageRequest(client, parentObject, task, currentRetryCount, opContext);

                Logger.info(opContext, LogConstants.START_REQUEST, request.getURL(),
                        request.getRequestProperty(Constants.HeaderConstants.DATE));

                // 5. Potentially upload data
                if (task.getSendStream() != null) {
                    Logger.info(opContext, LogConstants.UPLOAD);
                    final StreamMd5AndLength descriptor = Utility.writeToOutputStream(task.getSendStream(),
                            request.getOutputStream(), task.getLength(), false /* rewindStream */,
                            false /* calculate MD5 */, opContext, task.getRequestOptions());

                    task.validateStreamWrite(descriptor);
                    Logger.info(opContext, LogConstants.UPLOADDONE);
                }

                Utility.logHttpRequest(request, opContext);

                // 6. Process the request - Get response
                RequestResult currResult = task.getResult();
                currResult.setStartDate(new Date());

                Logger.info(opContext, LogConstants.GET_RESPONSE);

                currResult.setStatusCode(request.getResponseCode());
                currResult.setStatusMessage(request.getResponseMessage());

                currResult.setStopDate(new Date());
                currResult.setServiceRequestID(BaseResponse.getRequestId(request));
                currResult.setEtag(BaseResponse.getEtag(request));
                currResult.setRequestDate(BaseResponse.getDate(request));
                currResult.setContentMD5(BaseResponse.getContentMD5(request));

                // 7. Fire ResponseReceived Event
                ExecutionEngine.fireResponseReceivedEvent(opContext, request, task.getResult());

                Logger.info(opContext, LogConstants.RESPONSE_RECEIVED, currResult.getStatusCode(),
                        currResult.getServiceRequestID(), currResult.getContentMD5(), currResult.getEtag(),
                        currResult.getRequestDate());
               
                Utility.logHttpResponse(request, opContext);   

                // 8. Pre-process response to check if there was an exception. Do Response parsing (headers etc).
                Logger.info(opContext, LogConstants.PRE_PROCESS);
                RESULT_TYPE result = task.preProcessResponse(parentObject, client, opContext);
                Logger.info(opContext, LogConstants.PRE_PROCESS_DONE);

                if (!task.isNonExceptionedRetryableFailure()) {

                    // 9. Post-process response. Read stream from server.
                    Logger.info(opContext, LogConstants.POST_PROCESS);
                    result = task.postProcessResponse(request, parentObject, client, opContext, result);
                    Logger.info(opContext, LogConstants.POST_PROCESS_DONE);

                    // Success return result and drain the input stream.
                    if ((task.getResult().getStatusCode() >= 200) && (task.getResult().getStatusCode() < 300)) {
                        if (request != null) {
                            InputStream inStream = request.getInputStream();
                            // At this point, we already have a result / exception to return to the user.
                            // This is just an optimization to improve socket reuse.
                            try {
                                Utility.writeToOutputStream(inStream, null, -1, false, false, null,
                                        task.getRequestOptions());
                            }
                            catch (final IOException ex) {
                            }
                            catch (StorageException e) {
                            }
                            finally {
                                inStream.close();
                            }
                        }
                    }
                    Logger.info(opContext, LogConstants.COMPLETE);

                    return result;
                }
                else {
                    Logger.warn(opContext, LogConstants.UNEXPECTED_RESULT_OR_EXCEPTION);
                    // The task may have already parsed an exception.
                    translatedException = task.materializeException(opContext);
                    task.getResult().setException(translatedException);

                    // throw on non retryable status codes: 501, 505, blob type mismatch
                    if (task.getResult().getStatusCode() == HttpURLConnection.HTTP_NOT_IMPLEMENTED
                            || task.getResult().getStatusCode() == HttpURLConnection.HTTP_VERSION
                            || translatedException.getErrorCode().equals(StorageErrorCodeStrings.INVALID_BLOB_TYPE)) {
                        throw translatedException;
                    }
                }
            }
            catch (final StorageException e) {
                // In case of table batch error or internal error, the exception will contain a different
                // status code and message than the original HTTP response. Reset based on error values.
                task.getResult().setStatusCode(e.getHttpStatusCode());
                task.getResult().setStatusMessage(e.getMessage());
                task.getResult().setException(e);
                
                Logger.warn(opContext, LogConstants.RETRYABLE_EXCEPTION, e.getClass().getName(), e.getMessage());
                translatedException = e;
            }
            catch (final Exception e) {
                // Retryable, wrap
                Logger.warn(opContext, LogConstants.RETRYABLE_EXCEPTION, e.getClass().getName(), e.getMessage());
                translatedException = StorageException.translateException(task, e, opContext);
                task.getResult().setException(translatedException);
            }
            finally {
                opContext.setClientTimeInMs(new Date().getTime() - startTime);

                // 10. Fire RequestCompleted Event
                if (task.isSent()) {
                    ExecutionEngine.fireRequestCompletedEvent(opContext, request, task.getResult());
                }
            }

            // Evaluate Retry Policy
            Logger.info(opContext, LogConstants.RETRY_CHECK, currentRetryCount, task.getResult().getStatusCode(),
                    translatedException == null ? null : translatedException.getMessage());

            task.setCurrentLocation(getNextLocation(task.getCurrentLocation(), task.getLocationMode()));
            Logger.info(opContext, LogConstants.NEXT_LOCATION, task.getCurrentLocation(), task.getLocationMode());

            RetryContext retryContext = new RetryContext(currentRetryCount++, task.getResult(),
                    task.getCurrentLocation(), task.getLocationMode());

            RetryInfo retryInfo = policy.evaluate(retryContext, opContext);

            if (retryInfo == null) {
                // policy does not allow for retry
                Logger.error(opContext, LogConstants.DO_NOT_RETRY_POLICY, translatedException == null ? null
                        : translatedException.getMessage());
                throw translatedException;
            }
            else if (Utility.validateMaxExecutionTimeout(task.getRequestOptions().getOperationExpiryTimeInMs(),
                    retryInfo.getRetryInterval())) {
                // maximum execution time would be exceeded by current time plus retry interval delay
                TimeoutException timeoutException = new TimeoutException(SR.MAXIMUM_EXECUTION_TIMEOUT_EXCEPTION);
                translatedException = new StorageException(StorageErrorCodeStrings.OPERATION_TIMED_OUT,
                        SR.MAXIMUM_EXECUTION_TIMEOUT_EXCEPTION, Constants.HeaderConstants.HTTP_UNUSED_306, null,
                        timeoutException);

                task.initialize(opContext);
                task.getResult().setException(translatedException);

                Logger.error(opContext, LogConstants.DO_NOT_RETRY_TIMEOUT, translatedException == null ? null
                        : translatedException.getMessage());

                throw translatedException;
            }
            else {
                // attempt to retry
                task.setCurrentLocation(retryInfo.getTargetLocation());
                task.setLocationMode(retryInfo.getUpdatedLocationMode());
                Logger.info(opContext, LogConstants.RETRY_INFO, task.getCurrentLocation(), task.getLocationMode());

                try {
                    ExecutionEngine.fireRetryingEvent(opContext, task.getConnection(), task.getResult(), retryContext);

                    Logger.info(opContext, LogConstants.RETRY_DELAY, retryInfo.getRetryInterval());
                    Thread.sleep(retryInfo.getRetryInterval());
                }
                catch (final InterruptedException e) {
                    // Restore the interrupted status
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    private static <CLIENT_TYPE, PARENT_TYPE, RESULT_TYPE> HttpURLConnection setupStorageRequest(
            final CLIENT_TYPE client, final PARENT_TYPE parentObject,
            final StorageRequest<CLIENT_TYPE, PARENT_TYPE, RESULT_TYPE> task, int currentRetryCount,
            final OperationContext opContext) throws StorageException {
        try {

            // reset result flags
            task.initialize(opContext);

            if (Utility.validateMaxExecutionTimeout(task.getRequestOptions().getOperationExpiryTimeInMs())) {
                // maximum execution time would be exceeded by current time
                TimeoutException timeoutException = new TimeoutException(SR.MAXIMUM_EXECUTION_TIMEOUT_EXCEPTION);
                throw new StorageException(StorageErrorCodeStrings.OPERATION_TIMED_OUT,
                        SR.MAXIMUM_EXECUTION_TIMEOUT_EXCEPTION, Constants.HeaderConstants.HTTP_UNUSED_306, null,
                        timeoutException);
            }

            // Run the recovery action if this is a retry. Else, initialize the location mode for the task. 
            // For retries, it will be initialized in retry logic.
            if (currentRetryCount > 0) {
                task.recoveryAction(opContext);
                Logger.info(opContext, LogConstants.RETRY);
            }
            else {
                task.applyLocationModeToRequest();
                task.initializeLocation();
                Logger.info(opContext, LogConstants.STARTING);
            }

            task.setRequestLocationMode();

            // If the command only allows for a specific location, we should target
            // that location no matter what the retry policy says.
            task.validateLocation();

            Logger.info(opContext, LogConstants.INIT_LOCATION, task.getCurrentLocation(), task.getLocationMode());

            // 1. Build the request
            HttpURLConnection request = task.buildRequest(client, parentObject, opContext);

            // 2. Add headers
            task.setHeaders(request, parentObject, opContext);

            // Add any other custom headers that users have set on the opContext
            if (opContext.getUserHeaders() != null) {
                for (final Entry<String, String> entry : opContext.getUserHeaders().entrySet()) {
                    request.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }

            // 3. Fire sending request event
            ExecutionEngine.fireSendingRequestEvent(opContext, request, task.getResult());
            task.setIsSent(true);

            // 4. Sign the request
            task.signRequest(request, client, opContext);

            // set the connection on the task
            task.setConnection(request);

            return request;
        }
        catch (StorageException e) {
            throw e;
        }
        catch (Exception e) {
            throw new StorageException(null, e.getMessage(), Constants.HeaderConstants.HTTP_UNUSED_306, null, e);
        }
    }

    private static StorageLocation getNextLocation(StorageLocation lastLocation, LocationMode locationMode) {
        switch (locationMode) {
            case PRIMARY_ONLY:
                return StorageLocation.PRIMARY;

            case SECONDARY_ONLY:
                return StorageLocation.SECONDARY;

            case PRIMARY_THEN_SECONDARY:
            case SECONDARY_THEN_PRIMARY:
                return (lastLocation == StorageLocation.PRIMARY) ? StorageLocation.SECONDARY : StorageLocation.PRIMARY;

            default:
                return StorageLocation.PRIMARY;
        }
    }

    /**
     * Fires events representing that a request will be sent.
     */
    private static void fireSendingRequestEvent(OperationContext opContext, HttpURLConnection request,
            RequestResult result) {
        if (opContext.getSendingRequestEventHandler().hasListeners()
                || OperationContext.getGlobalSendingRequestEventHandler().hasListeners()) {
            SendingRequestEvent event = new SendingRequestEvent(opContext, request, result);
            opContext.getSendingRequestEventHandler().fireEvent(event);
            OperationContext.getGlobalSendingRequestEventHandler().fireEvent(event);
        }
    }

    /**
     * Fires events representing that a response has been received.
     */
    private static void fireResponseReceivedEvent(OperationContext opContext, HttpURLConnection request,
            RequestResult result) {
        if (opContext.getResponseReceivedEventHandler().hasListeners()
                || OperationContext.getGlobalResponseReceivedEventHandler().hasListeners()) {
            ResponseReceivedEvent event = new ResponseReceivedEvent(opContext, request, result);
            opContext.getResponseReceivedEventHandler().fireEvent(event);
            OperationContext.getGlobalResponseReceivedEventHandler().fireEvent(event);
        }
    }

    /**
     * Fires events representing that a response received from the service is fully processed.
     */
    private static void fireRequestCompletedEvent(OperationContext opContext, HttpURLConnection request,
            RequestResult result) {
        if (opContext.getRequestCompletedEventHandler().hasListeners()
                || OperationContext.getGlobalRequestCompletedEventHandler().hasListeners()) {
            RequestCompletedEvent event = new RequestCompletedEvent(opContext, request, result);
            opContext.getRequestCompletedEventHandler().fireEvent(event);
            OperationContext.getGlobalRequestCompletedEventHandler().fireEvent(event);
        }
    }

    /**
     * Fires events representing that a request will be retried.
     */
    private static void fireRetryingEvent(OperationContext opContext, HttpURLConnection request, RequestResult result,
            RetryContext retryContext) {
        if (opContext.getRetryingEventHandler().hasListeners()
                || OperationContext.getGlobalRetryingEventHandler().hasListeners()) {
            RetryingEvent event = new RetryingEvent(opContext, request, result, retryContext);
            opContext.getRetryingEventHandler().fireEvent(event);
            OperationContext.getGlobalRetryingEventHandler().fireEvent(event);
        }
    }
}
