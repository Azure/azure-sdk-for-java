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

import java.net.HttpURLConnection;

import com.microsoft.azure.storage.OperationContext;
import com.microsoft.azure.storage.RequestOptions;
import com.microsoft.azure.storage.RequestResult;
import com.microsoft.azure.storage.StorageException;

/**
 * RESERVED FOR INTERNAL USE. A base class which encapsulate the execution of a given storage operation.
 * 
 * @param <C>
 *            The service client type
 * @param <P>
 *            The type of the parent object, i.e. CloudBlobContainer for downloadAttributes etc.
 * @param <R>
 *            The type of the expected result
 */
public abstract class StorageOperation<C, P, R> {

    /**
     * Holds a reference to a realized exception which occurred during execution.
     */
    private StorageException exceptionReference;

    /**
     * A flag to indicate a failure which did not result in an exception, i.e a 400 class status code.
     */
    private boolean nonExceptionedRetryableFailure;

    /**
     * The RequestOptions to use for the request.
     */
    private RequestOptions requestOptions;

    /**
     * Holds the result for the operation.
     */
    private RequestResult result;

    /**
     * Holds the url connection for the operation.
     */
    private HttpURLConnection connection;

    /**
     * Default Ctor.
     */
    protected StorageOperation() {
        // no op
    }

    /**
     * Initializes a new instance of the StorageOperation class.
     * 
     * @param options
     *            the RequestOptions to use
     */
    public StorageOperation(final RequestOptions options) {
        this.setRequestOptions(options);
    }

    /**
     * Executes the operation.
     * 
     * @param client
     *            a reference to the service client associated with the object being operated against
     * @param parentObject
     *            a reference to the parent object of the operation, (i.e. CloudBlobContainer for Create)
     * @param opContext
     *            an object used to track the execution of the operation
     * @return the result from the operation
     * @throws Exception
     *             an error which occurred during execution
     */
    public abstract R execute(C client, P parentObject, OperationContext opContext) throws Exception;

    /**
     * @return the exception
     */
    public final StorageException getException() {
        return this.exceptionReference;
    }

    /**
     * @return the requestOptions
     */
    public final RequestOptions getRequestOptions() {
        return this.requestOptions;
    }

    /**
     * @return the result
     */
    public final RequestResult getResult() {
        return this.result;
    }

    /**
     * @return the URL connection
     */
    public final HttpURLConnection getConnection() {
        return this.connection;
    }

    /**
     * Resets the operation status flags between operations.
     */
    protected final void initialize(OperationContext opContext) {
        RequestResult currResult = new RequestResult();
        this.setResult(currResult);
        opContext.appendRequestResult(currResult);

        this.setException(null);
        this.setNonExceptionedRetryableFailure(false);
    }

    /**
     * @return the nonExceptionedRetryableFailure
     */
    public final boolean isNonExceptionedRetryableFailure() {
        return this.nonExceptionedRetryableFailure;
    }

    /**
     * Returns either the held exception from the operation if it is set, otherwise the translated exception.
     * 
     * @param request
     *            the reference to the HttpURLConnection for the operation.
     * @param opContext
     *            an object used to track the execution of the operation
     * @return the exception to throw.
     */
    protected final StorageException materializeException(final HttpURLConnection request,
            final OperationContext opContext) {
        if (this.getException() != null) {
            return this.getException();
        }

        return StorageException.translateException(request, null, opContext);
    }

    /**
     * @param exceptionReference
     *            the exception to set
     */
    protected final void setException(final StorageException exceptionReference) {
        this.exceptionReference = exceptionReference;
    }

    /**
     * @param nonExceptionedRetryableFailure
     *            the nonExceptionedRetryableFailure to set
     */
    public final void setNonExceptionedRetryableFailure(final boolean nonExceptionedRetryableFailure) {
        this.nonExceptionedRetryableFailure = nonExceptionedRetryableFailure;
    }

    /**
     * @param requestOptions
     *            the requestOptions to set
     */
    protected final void setRequestOptions(final RequestOptions requestOptions) {
        this.requestOptions = requestOptions;
    }

    /**
     * @param result
     *            the result to set
     */
    public final void setResult(final RequestResult result) {
        this.result = result;
    }

    /**
     * @param connection
     *            the connection to set
     */
    public final void setConnection(final HttpURLConnection connection) {
        this.connection = connection;
    }
}
