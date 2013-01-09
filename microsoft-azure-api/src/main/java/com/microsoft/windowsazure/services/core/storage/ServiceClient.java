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
package com.microsoft.windowsazure.services.core.storage;

import java.io.ByteArrayInputStream;
import java.net.HttpURLConnection;
import java.net.URI;

import com.microsoft.windowsazure.services.blob.client.CloudBlobClient;
import com.microsoft.windowsazure.services.core.storage.utils.StreamMd5AndLength;
import com.microsoft.windowsazure.services.core.storage.utils.Utility;
import com.microsoft.windowsazure.services.core.storage.utils.implementation.BaseRequest;
import com.microsoft.windowsazure.services.core.storage.utils.implementation.BaseResponse;
import com.microsoft.windowsazure.services.core.storage.utils.implementation.ExecutionEngine;
import com.microsoft.windowsazure.services.core.storage.utils.implementation.StorageOperation;
import com.microsoft.windowsazure.services.queue.client.CloudQueueClient;

/**
 * Reserved for internal use. Provides a client for accessing the Windows Azure Storage service.
 */
public abstract class ServiceClient {

    /**
     * Holds the base URI for the Service Client.
     */
    protected URI endpoint;

    /**
     * Holds the StorageCredentials associated with this Service Client.
     */
    protected StorageCredentials credentials;

    /**
     * Reserved for internal use. An internal flag which indicates if path style uris should be used.
     */
    private boolean usePathStyleUris;

    /**
     * Holds the default retry policy for requests made via the service client to set.
     */
    protected RetryPolicyFactory retryPolicyFactory = new RetryExponentialRetry();

    /**
     * Holds the default server and client timeout for requests made by the service client.
     */
    protected int timeoutInMs = Constants.DEFAULT_TIMEOUT_IN_MS;

    /**
     * Creates an instance of the <code>ServiceClient</code> class using the specified service endpoint.
     * 
     * @param baseUri
     *            A <code>java.net.URI</code> object that represents the service endpoint used to create the client.
     */
    public ServiceClient(final URI baseUri) {
        this(baseUri, null);
    }

    /**
     * Creates an instance of the <code>ServiceClient</code> class using the specified service endpoint and account
     * credentials.
     * 
     * @param baseUri
     *            A <code>java.net.URI</code> object that represents the service endpoint used to create the client.
     * 
     * @param credentials
     *            A {@link StorageCredentials} object that represents the account credentials.
     */
    public ServiceClient(final URI baseUri, final StorageCredentials credentials) {
        Utility.assertNotNull("baseUri", baseUri);
        if (!baseUri.isAbsolute()) {
            throw new IllegalArgumentException(String.format(
                    "Address '%s' is not an absolute address. Relative addresses are not permitted in here.", baseUri));
        }

        this.credentials = credentials == null ? StorageCredentialsAnonymous.ANONYMOUS : credentials;

        this.retryPolicyFactory = new RetryExponentialRetry();
        this.timeoutInMs = Constants.DEFAULT_TIMEOUT_IN_MS;

        this.usePathStyleUris = Utility.determinePathStyleFromUri(baseUri, this.credentials.getAccountName());
        this.endpoint = baseUri;
    }

    /**
     * Retrieves the current ServiceProperties for the given storage service. This includes Metrics and Logging
     * Configurations.
     * 
     * @return the ServiceProperties object representing the current configuration of the service.
     * @throws StorageException
     *             If a storage service error occurred.
     */
    public final ServiceProperties downloadServiceProperties() throws StorageException {
        return this.downloadServiceProperties(null, null);
    }

    /**
     * Retrieves the current ServiceProperties for the given storage service. This includes Metrics and Logging
     * Configurations.
     * 
     * @param options
     *            A {@link RequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudBlobClient}{@link CloudQueueClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * @return the ServiceProperties object representing the current configuration of the service.
     * @throws StorageException
     *             If a storage service error occurred.
     */
    public final ServiceProperties downloadServiceProperties(RequestOptions options, OperationContext opContext)
            throws StorageException {
        if (opContext == null) {
            opContext = new OperationContext();
        }

        if (options == null) {
            options = new RequestOptions();
        }

        opContext.initialize();
        options.applyBaseDefaults(this);

        final StorageOperation<ServiceClient, Void, ServiceProperties> impl = new StorageOperation<ServiceClient, Void, ServiceProperties>(
                options) {
            @Override
            public ServiceProperties execute(final ServiceClient client, final Void v, final OperationContext opContext)
                    throws Exception {

                final HttpURLConnection request = BaseRequest.getServiceProperties(client.getEndpoint(), this
                        .getRequestOptions().getTimeoutIntervalInMs(), null, opContext);

                client.getCredentials().signRequest(request, -1);
                this.setResult(ExecutionEngine.processRequest(request, opContext));

                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_OK) {
                    this.setNonExceptionedRetryableFailure(true);
                    return null;
                }

                return BaseResponse.readServicePropertiesFromStream(request.getInputStream(), opContext);
            }
        };

        return ExecutionEngine.executeWithRetry(this, null, impl, options.getRetryPolicyFactory(), opContext);
    }

    /**
     * Returns the storage credentials associated with this service client.
     * 
     * @return A {@link StorageCredentials} object that represents the storage credentials associated with this client.
     */
    public final StorageCredentials getCredentials() {
        return this.credentials;
    }

    /**
     * Returns the base URI for this service client.
     * 
     * @return A <code>java.net.URI</code> object that represents the base URI for the service client.
     */
    public final URI getEndpoint() {
        return this.endpoint;
    }

    /**
     * Returns the retry policy currently in effect for this Blob service client.
     * 
     * @return An {@link RetryPolicyFactory} object that represents the current retry policy.
     * 
     * @see RetryPolicy
     * @see RetryExponentialRetry
     * @see RetryLinearRetry
     * @see RetryNoRetry
     */
    public final RetryPolicyFactory getRetryPolicyFactory() {
        return this.retryPolicyFactory;
    }

    /**
     * Returns the timeout value for requests made to the service. For more information about the timeout, see
     * {@link #setTimeoutInMs}.
     * 
     * @return The current timeout value, in milliseconds, for requests made to the storage service.
     */
    public final int getTimeoutInMs() {
        return this.timeoutInMs;
    }

    /**
     * @return the usePathStyleUris
     */
    public final boolean isUsePathStyleUris() {
        return this.usePathStyleUris;
    }

    /**
     * Sets the base URI for the service client.
     * 
     * @param baseUri
     *            A <code>java.net.URI</code> object that represents the base URI being assigned to the service client.
     */
    protected final void setBaseURI(final URI baseUri) {
        this.usePathStyleUris = Utility.determinePathStyleFromUri(baseUri, this.credentials.getAccountName());
        this.endpoint = baseUri;
    }

    /**
     * Sets the credentials to use with this service client.
     * 
     * @param credentials
     *            A <code>Credentials</code> object that represents the credentials being assigned for the service
     *            client.
     */
    protected final void setCredentials(final StorageCredentials credentials) {
        this.credentials = credentials;
    }

    /**
     * Sets the RetryPolicyFactory object to use when making service requests.
     * 
     * @param retryPolicyFactory
     *            the RetryPolicyFactory object to use when making service requests.
     */
    public void setRetryPolicyFactory(final RetryPolicyFactory retryPolicyFactory) {
        this.retryPolicyFactory = retryPolicyFactory;
    }

    /**
     * Sets the timeout to use when making requests to the storage service.
     * <p>
     * The server timeout interval begins at the time that the complete request has been received by the service, and the
     * server begins processing the response. If the timeout interval elapses before the response is returned to the
     * client, the operation times out. The timeout interval resets with each retry, if the request is retried.
     * 
     * The default timeout interval for a request made via the service client is 90 seconds. You can change this value on
     * the service client by setting this property, so that all subsequent requests made via the service client will use
     * the new timeout interval. You can also change this value for an individual request, by setting the
     * {@link RequestOptions#timeoutIntervalInMs} property.
     * 
     * If you are downloading a large blob, you should increase the value of the timeout beyond the default value.
     * 
     * @param timeoutInMs
     *            The timeout, in milliseconds, to use when making requests to the storage service.
     */
    public final void setTimeoutInMs(final int timeoutInMs) {
        this.timeoutInMs = timeoutInMs;
    }

    /**
     * Uploads a new configuration to the storage service. This includes Metrics and Logging Configuration.
     * 
     * @param properties
     *            The ServiceProperties to upload.
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public void uploadServiceProperties(final ServiceProperties properties) throws StorageException {
        this.uploadServiceProperties(properties, null, null);
    }

    /**
     * Uploads a new configuration to the storage service. This includes Metrics and Logging Configuration.
     * 
     * @param properties
     *            The ServiceProperties to upload.
     * @param options
     *            A {@link RequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudBlobClient}{@link CloudQueueClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public void uploadServiceProperties(final ServiceProperties properties, RequestOptions options,
            OperationContext opContext) throws StorageException {
        if (opContext == null) {
            opContext = new OperationContext();
        }

        if (options == null) {
            options = new RequestOptions();
        }

        opContext.initialize();
        options.applyBaseDefaults(this);

        Utility.assertNotNull("properties", properties);
        Utility.assertNotNull("properties.Logging", properties.getLogging());
        Utility.assertNotNull("properties.Logging.LogOperationTypes", properties.getLogging().getLogOperationTypes());
        Utility.assertNotNull("properties.Merics", properties.getMetrics());
        Utility.assertNotNull("properties.Merics.Configuration", properties.getMetrics().getMetricsLevel());

        final StorageOperation<ServiceClient, Void, Void> impl = new StorageOperation<ServiceClient, Void, Void>(
                options) {
            @Override
            public Void execute(final ServiceClient client, final Void v, final OperationContext opContext)
                    throws Exception {

                final HttpURLConnection request = BaseRequest.setServiceProperties(client.getEndpoint(), this
                        .getRequestOptions().getTimeoutIntervalInMs(), null, opContext);

                final byte[] propertiesBytes = BaseRequest.serializeServicePropertiesToByteArray(properties, opContext);

                final ByteArrayInputStream dataInputStream = new ByteArrayInputStream(propertiesBytes);

                final StreamMd5AndLength descriptor = Utility.analyzeStream(dataInputStream, -1L, -1L,
                        true /* rewindSourceStream */, true /* calculateMD5 */);
                request.setRequestProperty(Constants.HeaderConstants.CONTENT_MD5, descriptor.getMd5());

                client.getCredentials().signRequest(request, descriptor.getLength());
                Utility.writeToOutputStream(dataInputStream, request.getOutputStream(), descriptor.getLength(),
                        false /* rewindSourceStream */, false /* calculateMD5 */, null, opContext);

                this.setResult(ExecutionEngine.processRequest(request, opContext));

                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_ACCEPTED) {
                    this.setNonExceptionedRetryableFailure(true);
                    return null;
                }

                return null;
            }
        };

        ExecutionEngine.executeWithRetry(this, null, impl, options.getRetryPolicyFactory(), opContext);
    }
}
