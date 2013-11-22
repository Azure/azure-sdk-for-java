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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;

import javax.xml.stream.XMLStreamException;

import com.microsoft.windowsazure.storage.core.BaseRequest;
import com.microsoft.windowsazure.storage.core.BaseResponse;
import com.microsoft.windowsazure.storage.core.RequestLocationMode;
import com.microsoft.windowsazure.storage.core.SR;
import com.microsoft.windowsazure.storage.core.StorageRequest;
import com.microsoft.windowsazure.storage.core.StreamMd5AndLength;
import com.microsoft.windowsazure.storage.core.Utility;

/**
 * Reserved for internal use. Provides a client for accessing the Windows Azure Storage service.
 */
public abstract class ServiceClient {

    /**
     * Holds the list of URIs for all locations.
     */
    private StorageUri storageUri;

    /**
     * Gets or sets the default location mode for requests made via the service client.
     */
    private LocationMode locationMode;

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
     * Holds the AuthenticationScheme associated with this Service Client.
     */
    protected AuthenticationScheme authenticationScheme = AuthenticationScheme.SHAREDKEYFULL;

    /**
     * Creates an instance of the <code>ServiceClient</code> class using the specified service endpoint.
     * 
     * @param baseUri
     *            A <code>java.net.URI</code> object that represents the service endpoint used to create the client.
     */
    public ServiceClient(final URI baseUri) {
        this(new StorageUri(baseUri), null /* credentials */);
    }

    /**
     * Creates an instance of the <code>ServiceClient</code> class using the specified service endpoint.
     * 
     * @param baseUri
     *            A <code>java.net.URI</code> object that represents the service endpoint used to create the client.
     * @param credentials
     *            A {@link StorageCredentials} object that represents the account credentials.
     */
    public ServiceClient(final URI baseUri, final StorageCredentials credentials) {
        this(new StorageUri(baseUri), credentials);
    }

    /**
     * Creates an instance of the <code>ServiceClient</code> class using the specified service endpoint and account
     * credentials.
     * 
     * @param storageUri
     *            A <code>StorageUri</code> object that represents the service endpoint used to create the client.
     * 
     * @param credentials
     *            A {@link StorageCredentials} object that represents the account credentials.
     */
    public ServiceClient(final StorageUri storageUri, final StorageCredentials credentials) {
        Utility.assertNotNull("baseUri", storageUri);
        if (!storageUri.isAbsolute()) {
            throw new IllegalArgumentException(String.format(SR.RELATIVE_ADDRESS_NOT_PERMITTED, storageUri));
        }

        this.credentials = credentials == null ? StorageCredentialsAnonymous.ANONYMOUS : credentials;

        this.retryPolicyFactory = new RetryExponentialRetry();
        this.timeoutInMs = Constants.DEFAULT_TIMEOUT_IN_MS;

        this.usePathStyleUris = Utility.determinePathStyleFromUri(storageUri.getPrimaryUri(),
                this.credentials.getAccountName());
        this.storageUri = storageUri;
        this.locationMode = LocationMode.PRIMARY_ONLY;
    }

    protected StorageRequest<ServiceClient, Void, ServiceProperties> downloadServicePropertiesImpl(
            final RequestOptions options, final boolean signAsTable) throws StorageException {
        final StorageRequest<ServiceClient, Void, ServiceProperties> getRequest = new StorageRequest<ServiceClient, Void, ServiceProperties>(
                options, this.getStorageUri()) {

            @Override
            public HttpURLConnection buildRequest(ServiceClient client, Void parentObject, OperationContext context)
                    throws Exception {
                return BaseRequest.getServiceProperties(client.getEndpoint(), options.getTimeoutIntervalInMs(), null,
                        context);
            }

            @Override
            public void signRequest(HttpURLConnection connection, ServiceClient client, OperationContext context)
                    throws Exception {
                if (signAsTable) {
                    StorageRequest.signTableRequest(connection, client, -1, null);
                }
                else {
                    StorageRequest.signBlobAndQueueRequest(connection, client, -1, null);
                }
            }

            @Override
            public ServiceProperties preProcessResponse(Void parentObject, ServiceClient client,
                    OperationContext context) throws Exception {
                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_OK) {
                    this.setNonExceptionedRetryableFailure(true);
                }

                return null;
            }

            @Override
            public ServiceProperties postProcessResponse(HttpURLConnection connection, Void parentObject,
                    ServiceClient client, OperationContext context, ServiceProperties storageObject) throws Exception {
                return BaseResponse.readServicePropertiesFromStream(connection.getInputStream(), context);
            }
        };

        return getRequest;
    }

    protected StorageRequest<ServiceClient, Void, ServiceStats> getServiceStatsImpl(final RequestOptions options,
            final boolean signAsTable) throws StorageException {
        final StorageRequest<ServiceClient, Void, ServiceStats> getRequest = new StorageRequest<ServiceClient, Void, ServiceStats>(
                options, this.getStorageUri()) {

            @Override
            public void setRequestLocationMode() {
                this.applyLocationModeToRequest();
                this.setRequestLocationMode(RequestLocationMode.PRIMARY_OR_SECONDARY);
            }

            @Override
            public HttpURLConnection buildRequest(ServiceClient client, Void parentObject, OperationContext context)
                    throws Exception {
                return BaseRequest.getServiceStats(client.getStorageUri().getUri(this.getCurrentLocation()),
                        options.getTimeoutIntervalInMs(), null, context);
            }

            @Override
            public void signRequest(HttpURLConnection connection, ServiceClient client, OperationContext context)
                    throws Exception {
                if (signAsTable) {
                    StorageRequest.signTableRequest(connection, client, -1, null);
                }
                else {
                    StorageRequest.signBlobAndQueueRequest(connection, client, -1, null);
                }
            }

            @Override
            public ServiceStats preProcessResponse(Void parentObject, ServiceClient client, OperationContext context)
                    throws Exception {
                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_OK) {
                    this.setNonExceptionedRetryableFailure(true);
                }

                return null;
            }

            @Override
            public ServiceStats postProcessResponse(HttpURLConnection connection, Void parentObject,
                    ServiceClient client, OperationContext context, ServiceStats storageObject) throws Exception {
                return BaseResponse.readServiceStatsFromStream(connection.getInputStream(), context);
            }

        };

        return getRequest;
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
     * Returns the AuthenticationScheme associated with this service client.
     * 
     * @return An {@link AuthenticationScheme} object that represents the authentication scheme associated with this
     *         client.
     */
    public final AuthenticationScheme getAuthenticationScheme() {
        return this.authenticationScheme;
    }

    /**
     * Returns the base URI for this service client.
     * 
     * @return A <code>java.net.URI</code> object that represents the base URI for the service client.
     */
    public final URI getEndpoint() {
        return this.storageUri.getPrimaryUri();
    }

    /**
     * Gets the default location mode for requests made via the service client.
     * 
     * @return A {@link LocationMode} object that represents the default location mode for the service client.
     */
    public final LocationMode getLocationMode() {
        return this.locationMode;
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
     * Returns the list of URIs for all locations.
     * 
     * @return A {@link StorageUri} object that represents the list of URIs for all locations.
     */
    public final StorageUri getStorageUri() {
        return this.storageUri;
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
     * Sets the default location mode for requests made via the service client.
     * 
     * @param locationMode
     *            the locationMode to set
     */
    public void setLocationMode(LocationMode locationMode) {
        this.locationMode = locationMode;
    }

    /**
     * Sets the list of URIs for all locations.
     * 
     * @param storageUri
     *            A <code>StorageUri</code> object that represents the list of URIs for all locations.
     */
    protected final void setStorageUri(final StorageUri storageUri) {
        this.usePathStyleUris = Utility.determinePathStyleFromUri(storageUri.getPrimaryUri(),
                this.credentials.getAccountName());
        this.storageUri = storageUri;
    }

    /**
     * Sets the Authentication Scheme to use with this service client.
     * 
     * @param scheme
     *            An <code>AuthenticationScheme</code> object that represents the authentication scheme being assigned
     *            for the service
     *            client.
     */
    public final void setAuthenticationScheme(final AuthenticationScheme scheme) {
        this.authenticationScheme = scheme;
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
     * The server timeout interval begins at the time that the complete request has been received by the service, and
     * the server begins processing the response. If the timeout interval elapses before the response is returned to the
     * client, the operation times out. The timeout interval resets with each retry, if the request is retried.
     * 
     * The default timeout interval for a request made via the service client is 90 seconds. You can change this value
     * on the service client by setting this property, so that all subsequent requests made via the service client will
     * use the new timeout interval. You can also change this value for an individual request, by setting the
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

    protected StorageRequest<ServiceClient, Void, Void> uploadServicePropertiesImpl(final ServiceProperties properties,
            final RequestOptions options, final OperationContext opContext, final boolean signAsTable)
            throws StorageException {
        try {
            byte[] propertiesBytes = BaseRequest.serializeServicePropertiesToByteArray(properties, opContext);

            final ByteArrayInputStream sendStream = new ByteArrayInputStream(propertiesBytes);
            final StreamMd5AndLength descriptor = Utility.analyzeStream(sendStream, -1L, -1L,
                    true /* rewindSourceStream */, true /* calculateMD5 */);

            final StorageRequest<ServiceClient, Void, Void> putRequest = new StorageRequest<ServiceClient, Void, Void>(
                    options, this.getStorageUri()) {

                @Override
                public HttpURLConnection buildRequest(ServiceClient client, Void parentObject, OperationContext context)
                        throws Exception {
                    this.setSendStream(sendStream);
                    this.setLength(descriptor.getLength());
                    return BaseRequest.setServiceProperties(client.getEndpoint(), options.getTimeoutIntervalInMs(),
                            null, context);
                }

                @Override
                public void setHeaders(HttpURLConnection connection, Void parentObject, OperationContext context) {
                    connection.setRequestProperty(Constants.HeaderConstants.CONTENT_MD5, descriptor.getMd5());
                }

                @Override
                public void signRequest(HttpURLConnection connection, ServiceClient client, OperationContext context)
                        throws Exception {
                    if (signAsTable) {
                        StorageRequest.signTableRequest(connection, client, descriptor.getLength(), null);
                    }
                    else {
                        StorageRequest.signBlobAndQueueRequest(connection, client, descriptor.getLength(), null);
                    }
                }

                @Override
                public Void preProcessResponse(Void parentObject, ServiceClient client, OperationContext context)
                        throws Exception {
                    if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_ACCEPTED) {
                        this.setNonExceptionedRetryableFailure(true);
                    }

                    return null;
                }

                @Override
                public void recoveryAction(OperationContext context) throws IOException {
                    sendStream.reset();
                    sendStream.mark(Constants.MAX_MARK_LENGTH);
                }
            };

            return putRequest;
        }
        catch (IllegalArgumentException e) {
            // to do : Move this to multiple catch clause so we can avoid the duplicated code once we move to Java 1.7.
            // The request was not even made. There was an error while trying to read the permissions. Just throw.
            StorageException translatedException = StorageException.translateException(null, e, null);
            throw translatedException;
        }
        catch (XMLStreamException e) {
            // The request was not even made. There was an error while trying to read the serviceProperties and write to stream. Just throw.
            StorageException translatedException = StorageException.translateException(null, e, null);
            throw translatedException;
        }
        catch (IOException e) {
            // The request was not even made. There was an error while trying to read the serviceProperties and write to stream. Just throw.
            StorageException translatedException = StorageException.translateException(null, e, null);
            throw translatedException;
        }
    }
}
