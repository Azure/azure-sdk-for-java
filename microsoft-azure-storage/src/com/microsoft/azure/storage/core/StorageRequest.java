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
import java.security.InvalidKeyException;

import com.microsoft.azure.storage.AccessCondition;
import com.microsoft.azure.storage.LocationMode;
import com.microsoft.azure.storage.OperationContext;
import com.microsoft.azure.storage.RequestOptions;
import com.microsoft.azure.storage.RequestResult;
import com.microsoft.azure.storage.ServiceClient;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.StorageExtendedErrorInformation;
import com.microsoft.azure.storage.StorageLocation;
import com.microsoft.azure.storage.StorageUri;

/**
 * RESERVED FOR INTERNAL USE. A class which encapsulate the execution of a given storage operation.
 * 
 * @param <C>
 *            The service client type
 * @param <P>
 *            The type of the parent object, i.e. CloudBlobContainer for downloadAttributes etc.
 * @param <R>
 *            The type of the expected result
 */
public abstract class StorageRequest<C, P, R> {
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

    private InputStream sendStream;

    /**
     * Holds the blob/file offset for recovery action.
     */
    private Long offset = null;

    /**
     * Holds the length
     */
    private Long length = null;

    /**
     * Holds the locked ETag
     */
    private String lockedETag = null;

    /**
     * Holds the locked ETag condition
     */
    private AccessCondition etagLockCondition = null;

    /**
     * Denotes whether properties have been populated or not.
     */
    private boolean arePropertiesPopulated = false;

    /**
     * Holds the ContentMD5 for retries.
     */
    private String contentMD5 = null;

    /**
     * Denotes the StorageUri of the request
     */
    private StorageUri storageUri;

    /**
     * Location mode set by the user
     */
    private LocationMode locationMode;

    /**
     * Location mode of the request
     */
    private RequestLocationMode requestLocationMode;

    /**
     * Current location of the request
     */
    private StorageLocation currentLocation;

    /**
     * Total number of bytes read so far
     */
    private long currentRequestByteCount = 0;

    /**
     * Denotes whether the associated request has been sent.
     */
    private boolean isSent = false;

    /**
     * Default Ctor.
     */
    protected StorageRequest() {
        // no op
    }

    /**
     * Initializes a new instance of the StorageRequest class.
     * 
     * @param options
     *            the RequestOptions to use
     */
    public StorageRequest(final RequestOptions options, StorageUri storageUri) {
        this.setRequestOptions(options);
        this.setStorageUri(storageUri);
        this.locationMode = LocationMode.PRIMARY_ONLY;
        this.requestLocationMode = RequestLocationMode.PRIMARY_ONLY;
    }

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
     * @return the stream to send to server
     */
    public final InputStream getSendStream() {
        return this.sendStream;
    }

    /**
     * @return the offset to start reading from
     */
    public Long getOffset() {
        return this.offset;
    }

    /**
     * @return the length, in bytes, of the stream
     */
    public Long getLength() {
        return this.length;
    }

    /**
     * @return the locked ETag
     */
    public final String getLockedETag() {
        return this.lockedETag;
    }

    /**
     * @return the ContentMD5
     */
    public final String getContentMD5() {
        return this.contentMD5;
    }

    /**
     * @return the location mode used to decide which location the request should be sent to.
     */
    public LocationMode getLocationMode() {
        return this.locationMode;
    }

    /**
     * @return the location mode used to decide which location the request should be sent to.
     */
    public RequestLocationMode getRequestLocationMode() {
        return this.requestLocationMode;
    }

    /**
     * @return the current location to which the request will be sent.
     */
    public StorageLocation getCurrentLocation() {
        return this.currentLocation;
    }

    /**
     * @return the locked ETag condition
     */
    public AccessCondition getETagLockCondition() {
        return this.etagLockCondition;
    }

    /**
     * @return the arePropertiesPopulated value
     */
    public boolean getArePropertiesPopulated() {
        return this.arePropertiesPopulated;
    }

    /**
     * @return the URI to which the request will be sent.
     */
    public StorageUri getStorageUri() {
        return this.storageUri;
    }

    /**
     * @return the currentRequestByteCount
     */
    public long getCurrentRequestByteCount() {
        return this.currentRequestByteCount;
    }

    /**
     * @return the isSent value
     */
    protected boolean isSent() {
        return this.isSent;
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
        this.setIsSent(false);
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
    protected final StorageException materializeException(final OperationContext opContext) {
        if (this.getException() != null) {
            return this.getException();
        }

        return StorageException.translateException(this, null, opContext);
    }

    public static final void signBlobQueueAndFileRequest(HttpURLConnection request, ServiceClient client,
            long contentLength, OperationContext context) throws InvalidKeyException, StorageException {
        StorageCredentialsHelper.signBlobQueueAndFileRequest(client.getCredentials(), request, contentLength, context);
    }

    public static final void signTableRequest(HttpURLConnection request, ServiceClient client, long contentLength,
            OperationContext context) throws InvalidKeyException, StorageException {
        StorageCredentialsHelper.signTableRequest(client.getCredentials(), request, contentLength, context);
    }

    public void applyLocationModeToRequest() {
        if (this.getRequestOptions().getLocationMode() != null) {
            this.setLocationMode(this.getRequestOptions().getLocationMode());
        }
    }

    public void initializeLocation() {
        if (this.getStorageUri() != null) {
            switch (this.getLocationMode()) {
                case PRIMARY_ONLY:
                case PRIMARY_THEN_SECONDARY:
                    this.setCurrentLocation(StorageLocation.PRIMARY);
                    break;

                case SECONDARY_ONLY:
                case SECONDARY_THEN_PRIMARY:
                    this.setCurrentLocation(StorageLocation.SECONDARY);
                    break;

                default:
                    throw new IllegalArgumentException(String.format(SR.ARGUMENT_OUT_OF_RANGE_ERROR, "locationMode",
                            this.getLocationMode()));
            }
        }
        else {
            this.setCurrentLocation(StorageLocation.PRIMARY);
        }
    }

    @SuppressWarnings("incomplete-switch")
    public void validateLocation() {
        if (this.getStorageUri() != null) {
            if (!this.getStorageUri().validateLocationMode(this.locationMode)) {
                throw new UnsupportedOperationException(SR.STORAGE_URI_MISSING_LOCATION);
            }
        }

        // If the command only allows for a specific location, we should target
        // that location no matter what the retry policy says.
        switch (this.getRequestLocationMode()) {
            case PRIMARY_ONLY:
                if (this.getLocationMode() == LocationMode.SECONDARY_ONLY) {
                    throw new IllegalArgumentException(SR.PRIMARY_ONLY_COMMAND);
                }

                this.setCurrentLocation(StorageLocation.PRIMARY);
                this.setLocationMode(LocationMode.PRIMARY_ONLY);
                break;

            case SECONDARY_ONLY:
                if (this.getLocationMode() == LocationMode.PRIMARY_ONLY) {
                    throw new IllegalArgumentException(SR.SECONDARY_ONLY_COMMAND);
                }

                this.setCurrentLocation(StorageLocation.SECONDARY);
                this.setLocationMode(LocationMode.SECONDARY_ONLY);
                break;
        }

        this.getResult().setTargetLocation(this.currentLocation);
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

    /**
     * @param sendStream
     *            the stream to send to the server
     */
    public void setSendStream(InputStream sendStream) {
        this.sendStream = sendStream;
    }

    /**
     * @param offset
     *            the stream offset to start copying from
     */
    public void setOffset(Long offset) {
        this.offset = offset;
    }

    /**
     * @param length
     *            the length, in bytes, of the stream
     */
    public void setLength(Long length) {
        this.length = length;
    }

    /**
     * @param lockedETag
     *            the locked ETag
     */
    public void setLockedETag(String lockedETag) {
        this.lockedETag = lockedETag;
    }

    /**
     * @param contentMD5
     *            the contentMD5
     */
    public void setContentMD5(String contentMD5) {
        this.contentMD5 = contentMD5;
    }

    /**
     * @param etagLockCondition
     *            the locked ETag condition
     */
    public void setETagLockCondition(AccessCondition etagLockCondition) {
        this.etagLockCondition = etagLockCondition;
    }

    /**
     * @param arePropertiesPopulated
     *            the arePropertiesPopulated value
     */
    public void setArePropertiesPopulated(boolean arePropertiesPopulated) {
        this.arePropertiesPopulated = arePropertiesPopulated;
    }

    /**
     * @param locationMode
     *            the locationMode value
     */
    public void setLocationMode(LocationMode locationMode) {
        this.locationMode = locationMode;
    }

    /**
     * @param requestLocationMode
     *            the requestLocationMode value
     */
    public void setRequestLocationMode(RequestLocationMode requestLocationMode) {
        this.requestLocationMode = requestLocationMode;
    }

    /**
     * @param storageLocation
     *            the storageLocation value
     */
    public void setCurrentLocation(StorageLocation currentLocation) {
        this.currentLocation = currentLocation;
    }

    /**
     * @param storageUri
     *            the storageUri value
     */
    public void setStorageUri(StorageUri storageUri) {
        this.storageUri = storageUri;
    }

    /**
     * @param currentRequestByteCount
     *            the currentRequestByteCount to set
     */
    public void setCurrentRequestByteCount(long currentRequestByteCount) {
        this.currentRequestByteCount = currentRequestByteCount;
    }

    /**
     * Function to apply the location mode to the request.
     */
    public void setRequestLocationMode() {
        // no-op
    }

    /**
     * @param isSent
     *            the isSent value
     */
    protected void setIsSent(boolean isSent) {
        this.isSent = isSent;
    }

    /**
     * Function to construct the request.
     * 
     * @param parentObject
     *            Parent object, i.e. CloudBlobContainer for downloadAttributes etc.
     * @param context
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * @return a HttpURLConnection configured for the operation.
     * @throws Exception
     */
    public abstract HttpURLConnection buildRequest(C client, P parentObject, OperationContext context) throws Exception;

    /**
     * Function to set custom headers.
     * 
     * @param connection
     *            HttpURLConnection configured for the operation.
     * @param parentObject
     *            Parent object, i.e. CloudBlobContainer for downloadAttributes etc.
     * @param context
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     */
    public void setHeaders(HttpURLConnection connection, P parentObject, OperationContext context) {
        // no-op
    }

    /**
     * Function to Sign headers.
     * 
     * @param connection
     *            HttpURLConnection configured for the operation.
     * @param client
     *            The service client.
     * @param context
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * @throws Exception
     */
    public abstract void signRequest(HttpURLConnection connection, C client, OperationContext context) throws Exception;

    /**
     * Pre-Stream Retrieval function.
     * 
     * @param command
     *            StorageCommand for the operation.
     * @param parentObject
     *            Parent object, i.e. CloudBlobContainer for downloadAttributes etc.
     * @param client
     *            The service client.
     * @return an Object of the expected result's type.
     * @throws Exception
     */
    public abstract R preProcessResponse(P parentObject, C client, OperationContext context) throws Exception;

    /**
     * Post-Stream Retrieval function.
     * 
     * @param connection
     *            HttpURLConnection configured for the operation.
     * @param storageObject
     *            An object of the expected result's type.
     * @return the expected result of the operation.
     * @throws Exception
     */
    public R postProcessResponse(HttpURLConnection connection, P parentObject, C client, OperationContext context,
            R storageObject) throws Exception {
        return storageObject;
    }

    /**
     * Validate the written stream length when length is provided.
     * 
     * @throws StorageException
     */
    public void validateStreamWrite(StreamMd5AndLength descriptor) throws StorageException {
        // no-op
    }

    /**
     * Recovery action for retries.
     * 
     * @throws IOException
     */
    public void recoveryAction(OperationContext context) throws IOException {
        // no-op
    }

    /**
     * Returns extended error information for this request.
     * 
     * @return A {@link StorageExtendedErrorInformation} object that represents the error details for the specified
     *         request.
     */
    public StorageExtendedErrorInformation parseErrorDetails() {
        try {
            if (this.getConnection() == null || this.getConnection().getErrorStream() == null) {
                return null;
            }

            return StorageErrorHandler.getExtendedErrorInformation(this.getConnection().getErrorStream());
        } catch (final Exception e) {
            return null;
        }
    }
}
