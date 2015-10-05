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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;

import javax.xml.stream.XMLStreamException;

import com.microsoft.azure.storage.core.BaseRequest;
import com.microsoft.azure.storage.core.RequestLocationMode;
import com.microsoft.azure.storage.core.SR;
import com.microsoft.azure.storage.core.StorageRequest;
import com.microsoft.azure.storage.core.StreamMd5AndLength;
import com.microsoft.azure.storage.core.Utility;

/**
 * Provides a client for accessing the Microsoft Azure Storage service.
 */
public abstract class ServiceClient {

    /**
     * Holds the list of URIs for all locations.
     */
    private StorageUri storageUri;

    /**
     * Holds the StorageCredentials associated with this Service Client.
     */
    protected StorageCredentials credentials;

    /**
     * Reserved for internal use. An internal flag which indicates if path style uris should be used.
     */
    private boolean usePathStyleUris;

    /**
     * Creates an instance of the <code>ServiceClient</code> class using the specified service endpoint and account
     * credentials.
     * 
     * @param storageUri
     *            A {@link StorageUri} object which represents the service endpoint used to create the client.
     * @param credentials
     *            A {@link StorageCredentials} object which represents the account credentials.
     */
    protected ServiceClient(final StorageUri storageUri, final StorageCredentials credentials) {
        Utility.assertNotNull("baseUri", storageUri);
        if (!storageUri.isAbsolute()) {
            throw new IllegalArgumentException(String.format(SR.RELATIVE_ADDRESS_NOT_PERMITTED, storageUri));
        }

        this.credentials = credentials == null ? StorageCredentialsAnonymous.ANONYMOUS : credentials;
        this.usePathStyleUris = Utility.determinePathStyleFromUri(storageUri.getPrimaryUri());
        this.storageUri = storageUri;
    }

    protected StorageRequest<ServiceClient, Void, ServiceProperties> downloadServicePropertiesImpl(
            final RequestOptions options, final boolean signAsTable) {
        final StorageRequest<ServiceClient, Void, ServiceProperties> getRequest = new StorageRequest<ServiceClient, Void, ServiceProperties>(
                options, this.getStorageUri()) {

            @Override
            public HttpURLConnection buildRequest(ServiceClient client, Void parentObject, OperationContext context)
                    throws Exception {
                return BaseRequest.getServiceProperties(
                        credentials.transformUri(client.getEndpoint()), options, null, context);
            }

            @Override
            public void signRequest(HttpURLConnection connection, ServiceClient client, OperationContext context)
                    throws Exception {
                if (signAsTable) {
                    StorageRequest.signTableRequest(connection, client, -1, context);
                }
                else {
                    StorageRequest.signBlobQueueAndFileRequest(connection, client, -1, context);
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
                return ServicePropertiesHandler.readServicePropertiesFromStream(connection.getInputStream());
            }
        };

        return getRequest;
    }

    protected StorageRequest<ServiceClient, Void, ServiceStats> getServiceStatsImpl(final RequestOptions options,
            final boolean signAsTable) {
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
                return BaseRequest.getServiceStats(
                        credentials.transformUri(client.getStorageUri().getUri(this.getCurrentLocation())),
                        options, null, context);
            }

            @Override
            public void signRequest(HttpURLConnection connection, ServiceClient client, OperationContext context)
                    throws Exception {
                if (signAsTable) {
                    StorageRequest.signTableRequest(connection, client, -1, context);
                }
                else {
                    StorageRequest.signBlobQueueAndFileRequest(connection, client, -1, context);
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
                return ServiceStatsHandler.readServiceStatsFromStream(connection.getInputStream());
            }

        };

        return getRequest;
    }

    /**
     * Returns the storage credentials associated with this service client.
     * 
     * @return A {@link StorageCredentials} object which represents the storage credentials associated with this client.
     */
    public final StorageCredentials getCredentials() {
        return this.credentials;
    }

    /**
     * Returns the base URI for this service client.
     * 
     * @return A <code>java.net.URI</code> object which represents the base URI for the service client.
     */
    public final URI getEndpoint() {
        return this.storageUri.getPrimaryUri();
    }

    /**
     * Returns the list of URIs for all locations.
     * 
     * @return A {@link StorageUri} object which represents the list of URIs for all locations.
     */
    public final StorageUri getStorageUri() {
        return this.storageUri;
    }

    /**
     * @return <code>true</code> if path-style URIs are used; otherwise, <code>false</code>.
     */
    protected boolean isUsePathStyleUris() {
        return this.usePathStyleUris;
    }

    /**
     * Sets the credentials to use with this service client.
     * 
     * @param credentials
     *            A {@link StorageCredentials} object which represents the credentials being assigned for the service
     *            client.
     */
    protected final void setCredentials(final StorageCredentials credentials) {
        this.credentials = credentials;
    }

    /**
     * Sets the list of URIs for all locations.
     * 
     * @param storageUri
     *            A {@link StorageUri} object which represents the list of URIs for all locations.
     */
    protected final void setStorageUri(final StorageUri storageUri) {
        this.usePathStyleUris = Utility.determinePathStyleFromUri(storageUri.getPrimaryUri());
        this.storageUri = storageUri;
    }

    protected StorageRequest<ServiceClient, Void, Void> uploadServicePropertiesImpl(final ServiceProperties properties,
            final RequestOptions options, final OperationContext opContext, final boolean signAsTable)
            throws StorageException {
        try {
            byte[] propertiesBytes = ServicePropertiesSerializer.serializeToByteArray(properties);

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
                    return BaseRequest.setServiceProperties(
                            credentials.transformUri(client.getEndpoint()), options, null, context);
                }

                @Override
                public void setHeaders(HttpURLConnection connection, Void parentObject, OperationContext context) {
                    connection.setRequestProperty(Constants.HeaderConstants.CONTENT_MD5, descriptor.getMd5());
                }

                @Override
                public void signRequest(HttpURLConnection connection, ServiceClient client, OperationContext context)
                        throws Exception {
                    if (signAsTable) {
                        StorageRequest.signTableRequest(connection, client, descriptor.getLength(), context);
                    }
                    else {
                        StorageRequest.signBlobQueueAndFileRequest(connection, client, descriptor.getLength(), context);
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
            StorageException translatedException = StorageException.translateClientException(e);
            throw translatedException;
        }
        catch (XMLStreamException e) {
            // The request was not even made. There was an error while trying to read the serviceProperties and write to stream. Just throw.
            StorageException translatedException = StorageException.translateClientException(e);
            throw translatedException;
        }
        catch (IOException e) {
            // The request was not even made. There was an error while trying to read the serviceProperties and write to stream. Just throw.
            StorageException translatedException = StorageException.translateClientException(e);
            throw translatedException;
        }
    }

    /**
     * Gets the {@link RequestOptions} that is used for requests associated with this <code>ServiceClient</code>
     * 
     * @return The {@link RequestOptions} object containing the values used by this <code>ServiceClient</code>
     */
    public abstract RequestOptions getDefaultRequestOptions();
}
