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
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.util.HashMap;
import java.util.Map.Entry;

import com.microsoft.azure.storage.Constants;
import com.microsoft.azure.storage.Credentials;
import com.microsoft.azure.storage.OperationContext;
import com.microsoft.azure.storage.RequestOptions;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.StorageKey;

/**
 * RESERVED FOR INTERNAL USE. The Base Request class for the protocol layer.
 */
public final class BaseRequest {

    private static final String METADATA = "metadata";

    private static final String SERVICE = "service";

    private static final String STATS = "stats";

    private static final String TIMEOUT = "timeout";

    /**
     * Stores the user agent to send over the wire to identify the client.
     */
    private static String userAgent;

    /**
     * Adds the lease id.
     * 
     * @param request
     *            a HttpURLConnection for the operation.
     * @param leaseId
     *            the lease id to add to the HttpURLConnection.
     */
    public static void addLeaseId(final HttpURLConnection request, final String leaseId) {
        if (leaseId != null) {
            BaseRequest.addOptionalHeader(request, Constants.HeaderConstants.LEASE_ID_HEADER, leaseId);
        }
    }

    /**
     * Adds the metadata.
     * 
     * @param request
     *            The request.
     * @param metadata
     *            The metadata.
     */
    public static void addMetadata(final HttpURLConnection request, final HashMap<String, String> metadata,
            final OperationContext opContext) {
        if (metadata != null) {
            for (final Entry<String, String> entry : metadata.entrySet()) {
                addMetadata(request, entry.getKey(), entry.getValue(), opContext);
            }
        }
    }

    /**
     * Adds the metadata.
     * 
     * @param opContext
     *            an object used to track the execution of the operation
     * @param request
     *            The request.
     * @param name
     *            The metadata name.
     * @param value
     *            The metadata value.
     */
    private static void addMetadata(final HttpURLConnection request, final String name, final String value,
            final OperationContext opContext) {
        if (Utility.isNullOrEmptyOrWhitespace(name)) {
            throw new IllegalArgumentException(SR.METADATA_KEY_INVALID);
        }
        else if (Utility.isNullOrEmptyOrWhitespace(value)) {
            throw new IllegalArgumentException(SR.METADATA_VALUE_INVALID);
        }

        request.setRequestProperty(Constants.HeaderConstants.PREFIX_FOR_STORAGE_METADATA + name, value);
    }

    /**
     * Adds the optional header.
     * 
     * @param request
     *            a HttpURLConnection for the operation.
     * @param name
     *            the metadata name.
     * @param value
     *            the metadata value.
     */
    public static void addOptionalHeader(final HttpURLConnection request, final String name, final String value) {
        if (value != null && !value.equals(Constants.EMPTY_STRING)) {
            request.setRequestProperty(name, value);
        }
    }

    /**
     * Creates the specified resource. Note request is set to setFixedLengthStreamingMode(0); Sign with 0 length.
     * 
     * @param uri
     *            the request Uri.
     * @param options
     *            TODO
     * @param builder
     *            the UriQueryBuilder for the request
     * @param opContext
     *            an object used to track the execution of the operation
     * 
     * @return a HttpURLConnection to perform the operation.
     * @throws IOException
     *             if there is an error opening the connection
     * @throws URISyntaxException
     *             if there is an improperly formated URI
     * @throws StorageException
     * @throws IllegalArgumentException
     */
    public static HttpURLConnection create(final URI uri, final RequestOptions options, UriQueryBuilder builder,
            final OperationContext opContext) throws IOException, URISyntaxException, StorageException {
        if (builder == null) {
            builder = new UriQueryBuilder();
        }

        final HttpURLConnection retConnection = createURLConnection(uri, options, builder, opContext);
        retConnection.setFixedLengthStreamingMode(0);
        retConnection.setDoOutput(true);
        retConnection.setRequestMethod(Constants.HTTP_PUT);

        return retConnection;
    }

    /**
     * Creates the web request.
     * 
     * @param uri
     *            the request Uri.
     * @param options
     *            A {@link RequestOptions} object that specifies execution options such as retry policy and timeout
     *            settings for the operation. This parameter is unused.
     * @param builder
     *            the UriQueryBuilder for the request
     * @param opContext
     *            an object used to track the execution of the operation
     * @return a HttpURLConnection to perform the operation.
     * @throws IOException
     *             if there is an error opening the connection
     * @throws URISyntaxException
     *             if there is an improperly formated URI
     * @throws StorageException
     */
    public static HttpURLConnection createURLConnection(final URI uri, final RequestOptions options,
            UriQueryBuilder builder, final OperationContext opContext) throws IOException, URISyntaxException,
            StorageException {
        if (builder == null) {
            builder = new UriQueryBuilder();
        }

        final URL resourceUrl = builder.addToURI(uri).toURL();

        final HttpURLConnection retConnection = (HttpURLConnection) resourceUrl.openConnection();

        if (options.getTimeoutIntervalInMs() != null && options.getTimeoutIntervalInMs() != 0) {
            builder.add(TIMEOUT, String.valueOf(options.getTimeoutIntervalInMs() / 1000));
        }

        // Note: ReadTimeout must be explicitly set to avoid a bug in JDK 6.
        // In certain cases, this bug causes an immediate read timeout exception to be thrown even if ReadTimeout is not set.
        retConnection.setReadTimeout(Utility.getRemainingTimeout(options.getOperationExpiryTimeInMs()));

        // Note : accept behavior, java by default sends Accept behavior as text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2
        retConnection.setRequestProperty(Constants.HeaderConstants.ACCEPT, Constants.HeaderConstants.XML_TYPE);
        retConnection.setRequestProperty(Constants.HeaderConstants.ACCEPT_CHARSET, Constants.UTF8_CHARSET);

        // Note : Content-Type behavior, java by default sends Content-type behavior as application/x-www-form-urlencoded for posts.
        retConnection.setRequestProperty(Constants.HeaderConstants.CONTENT_TYPE, Constants.EMPTY_STRING);

        retConnection.setRequestProperty(Constants.HeaderConstants.STORAGE_VERSION_HEADER,
                Constants.HeaderConstants.TARGET_STORAGE_VERSION);
        retConnection.setRequestProperty(Constants.HeaderConstants.USER_AGENT, getUserAgent());
        retConnection.setRequestProperty(Constants.HeaderConstants.CLIENT_REQUEST_ID_HEADER,
                opContext.getClientRequestID());

        return retConnection;
    }

    /**
     * Deletes the specified resource. Sign with no length specified.
     * 
     * @param uri
     *            the request Uri.
     * @param timeout
     *            the timeout for the request
     * @param builder
     *            the UriQueryBuilder for the request
     * @param opContext
     *            an object used to track the execution of the operation
     * @return a HttpURLConnection to perform the operation.
     * @throws IOException
     *             if there is an error opening the connection
     * @throws URISyntaxException
     *             if there is an improperly formated URI
     * @throws StorageException
     */
    public static HttpURLConnection delete(final URI uri, final RequestOptions options, UriQueryBuilder builder,
            final OperationContext opContext) throws IOException, URISyntaxException, StorageException {
        if (builder == null) {
            builder = new UriQueryBuilder();
        }

        final HttpURLConnection retConnection = createURLConnection(uri, options, builder, opContext);
        retConnection.setRequestMethod(Constants.HTTP_DELETE);

        return retConnection;
    }

    /**
     * Gets the properties. Sign with no length specified.
     * 
     * @param uri
     *            The Uri to query.
     * @param timeout
     *            The timeout.
     * @param builder
     *            The builder.
     * @param opContext
     *            an object used to track the execution of the operation
     * @return a web request for performing the operation.
     * @throws StorageException
     * @throws URISyntaxException
     * @throws IOException
     * */
    public static HttpURLConnection getProperties(final URI uri, final RequestOptions options, UriQueryBuilder builder,
            final OperationContext opContext) throws IOException, URISyntaxException, StorageException {
        if (builder == null) {
            builder = new UriQueryBuilder();
        }

        final HttpURLConnection retConnection = createURLConnection(uri, options, builder, opContext);
        retConnection.setRequestMethod(Constants.HTTP_HEAD);

        return retConnection;
    }

    /**
     * Creates a HttpURLConnection used to retrieve the Analytics service properties from the storage service.
     * 
     * @param uri
     *            The service endpoint.
     * @param timeout
     *            The timeout.
     * @param builder
     *            The builder.
     * @param opContext
     *            an object used to track the execution of the operation
     * @return a web request for performing the operation.
     * @throws IOException
     * @throws URISyntaxException
     * @throws StorageException
     */
    public static HttpURLConnection getServiceProperties(final URI uri, final RequestOptions options,
            UriQueryBuilder builder, final OperationContext opContext) throws IOException, URISyntaxException,
            StorageException {
        if (builder == null) {
            builder = new UriQueryBuilder();
        }

        builder.add(Constants.QueryConstants.COMPONENT, Constants.QueryConstants.PROPERTIES);
        builder.add(Constants.QueryConstants.RESOURCETYPE, SERVICE);

        final HttpURLConnection retConnection = createURLConnection(uri, options, builder, opContext);
        retConnection.setRequestMethod(Constants.HTTP_GET);

        return retConnection;
    }

    /**
     * Creates a web request to get the stats of the service.
     * 
     * @param uri
     *            The service endpoint.
     * @param timeout
     *            The timeout.
     * @param builder
     *            The builder.
     * @param opContext
     *            an object used to track the execution of the operation
     * @return a web request for performing the operation.
     * @throws IOException
     * @throws URISyntaxException
     * @throws StorageException
     */
    public static HttpURLConnection getServiceStats(final URI uri, final RequestOptions options,
            UriQueryBuilder builder, final OperationContext opContext) throws IOException, URISyntaxException,
            StorageException {
        if (builder == null) {
            builder = new UriQueryBuilder();
        }

        builder.add(Constants.QueryConstants.COMPONENT, STATS);
        builder.add(Constants.QueryConstants.RESOURCETYPE, SERVICE);

        final HttpURLConnection retConnection = createURLConnection(uri, options, builder, opContext);
        retConnection.setRequestMethod("GET");

        return retConnection;
    }

    /**
     * Gets the user agent to send over the wire to identify the client.
     * 
     * @return the user agent to send over the wire to identify the client.
     */
    public static String getUserAgent() {
        if (userAgent == null) {
            String userAgentComment = String.format(Utility.LOCALE_US, "(JavaJRE %s; %s %s)",
                    System.getProperty("java.version"), System.getProperty("os.name").replaceAll(" ", ""),
                    System.getProperty("os.version"));
            userAgent = String.format("%s/%s %s", Constants.HeaderConstants.USER_AGENT_PREFIX,
                    Constants.HeaderConstants.USER_AGENT_VERSION, userAgentComment);
        }

        return userAgent;
    }

    /**
     * Sets the metadata. Sign with 0 length.
     * 
     * @param uri
     *            The blob Uri.
     * @param timeout
     *            The timeout.
     * @param builder
     *            The builder.
     * @param opContext
     *            an object used to track the execution of the operation
     * @return a web request for performing the operation.
     * @throws StorageException
     * @throws URISyntaxException
     * @throws IOException
     * */
    public static HttpURLConnection setMetadata(final URI uri, final RequestOptions options, UriQueryBuilder builder,
            final OperationContext opContext) throws IOException, URISyntaxException, StorageException {

        if (builder == null) {
            builder = new UriQueryBuilder();
        }

        builder.add(Constants.QueryConstants.COMPONENT, METADATA);
        final HttpURLConnection retConnection = createURLConnection(uri, options, builder, opContext);

        retConnection.setFixedLengthStreamingMode(0);
        retConnection.setDoOutput(true);
        retConnection.setRequestMethod(Constants.HTTP_PUT);

        return retConnection;
    }

    /**
     * Creates a HttpURLConnection used to set the Analytics service properties on the storage service.
     * 
     * @param uri
     *            The service endpoint.
     * @param timeout
     *            The timeout.
     * @param builder
     *            The builder.
     * @param opContext
     *            an object used to track the execution of the operation
     * @return a web request for performing the operation.
     * @throws IOException
     * @throws URISyntaxException
     * @throws StorageException
     */
    public static HttpURLConnection setServiceProperties(final URI uri, final RequestOptions options,
            UriQueryBuilder builder, final OperationContext opContext) throws IOException, URISyntaxException,
            StorageException {
        if (builder == null) {
            builder = new UriQueryBuilder();
        }

        builder.add(Constants.QueryConstants.COMPONENT, Constants.QueryConstants.PROPERTIES);
        builder.add(Constants.QueryConstants.RESOURCETYPE, SERVICE);

        final HttpURLConnection retConnection = createURLConnection(uri, options, builder, opContext);

        retConnection.setDoOutput(true);
        retConnection.setRequestMethod(Constants.HTTP_PUT);

        return retConnection;
    }

    /**
     * Signs the request appropriately to make it an authenticated request for Blob and Queue.
     * 
     * @param request
     *            a HttpURLConnection for the operation.
     * @param credentials
     *            the credentials to use for signing.
     * @param contentLength
     *            the length of the content written to the output stream, -1 if unknown.
     * @param opContext
     *            an object used to track the execution of the operation
     * @throws InvalidKeyException
     *             if the credentials key is invalid.
     * @throws StorageException
     */
    public static void signRequestForBlobAndQueue(final HttpURLConnection request, final Credentials credentials,
            final Long contentLength, final OperationContext opContext) throws InvalidKeyException, StorageException {
        request.setRequestProperty(Constants.HeaderConstants.DATE, Utility.getGMTTime());
        final Canonicalizer canonicalizer = CanonicalizerFactory.getBlobQueueFullCanonicalizer(request);

        final String stringToSign = canonicalizer.canonicalize(request, credentials.getAccountName(), contentLength);

        final String computedBase64Signature = StorageKey.computeMacSha256(credentials.getKey(), stringToSign);

        // V2 add logging
        // System.out.println(String.format("Signing %s\r\n%s\r\n", stringToSign, computedBase64Signature));
        request.setRequestProperty(Constants.HeaderConstants.AUTHORIZATION,
                String.format("%s %s:%s", "SharedKey", credentials.getAccountName(), computedBase64Signature));
    }

    /**
     * 
     * Signs the request appropriately to make it an authenticated request for Blob and Queue.
     * 
     * @param request
     *            a HttpURLConnection for the operation.
     * @param credentials
     *            the credentials to use for signing.
     * @param contentLength
     *            the length of the content written to the output stream, -1 if unknown.
     * @param opContext
     *            an object used to track the execution of the operation
     * @throws InvalidKeyException
     *             if the credentials key is invalid.
     * @throws StorageException
     */
    public static void signRequestForBlobAndQueueSharedKeyLite(final HttpURLConnection request,
            final Credentials credentials, final Long contentLength, final OperationContext opContext)
            throws InvalidKeyException, StorageException {
        request.setRequestProperty(Constants.HeaderConstants.DATE, Utility.getGMTTime());

        final Canonicalizer canonicalizer = CanonicalizerFactory.getBlobQueueLiteCanonicalizer(request);

        final String stringToSign = canonicalizer.canonicalize(request, credentials.getAccountName(), contentLength);

        final String computedBase64Signature = StorageKey.computeMacSha256(credentials.getKey(), stringToSign);

        // VNext add logging
        // System.out.println(String.format("Signing %s\r\n%s\r\n",
        // stringToSign, computedBase64Signature));
        request.setRequestProperty(Constants.HeaderConstants.AUTHORIZATION,
                String.format("%s %s:%s", "SharedKeyLite", credentials.getAccountName(), computedBase64Signature));
    }

    /**
     * 
     * Signs the request appropriately to make it an authenticated request for Table.
     * 
     * @param request
     *            a HttpURLConnection for the operation.
     * @param credentials
     *            the credentials to use for signing.
     * @param contentLength
     *            the length of the content written to the output stream, -1 if unknown.
     * @param opContext
     *            an object used to track the execution of the operation
     * @throws InvalidKeyException
     *             if the credentials key is invalid.
     * @throws StorageException
     */
    public static void signRequestForTableSharedKey(final HttpURLConnection request, final Credentials credentials,
            final Long contentLength, final OperationContext opContext) throws InvalidKeyException, StorageException {
        request.setRequestProperty(Constants.HeaderConstants.DATE, Utility.getGMTTime());

        final Canonicalizer canonicalizer = CanonicalizerFactory.getTableFullCanonicalizer(request);

        final String stringToSign = canonicalizer.canonicalize(request, credentials.getAccountName(), contentLength);

        final String computedBase64Signature = StorageKey.computeMacSha256(credentials.getKey(), stringToSign);

        request.setRequestProperty(Constants.HeaderConstants.AUTHORIZATION,
                String.format("%s %s:%s", "SharedKey", credentials.getAccountName(), computedBase64Signature));
    }

    /**
     * 
     * Signs the request appropriately to make it an authenticated request for Table.
     * 
     * @param request
     *            a HttpURLConnection for the operation.
     * @param credentials
     *            the credentials to use for signing.
     * @param contentLength
     *            the length of the content written to the output stream, -1 if unknown.
     * @param opContext
     *            an object used to track the execution of the operation
     * @throws InvalidKeyException
     *             if the credentials key is invalid.
     * @throws StorageException
     */
    public static void signRequestForTableSharedKeyLite(final HttpURLConnection request, final Credentials credentials,
            final Long contentLength, final OperationContext opContext) throws InvalidKeyException, StorageException {
        request.setRequestProperty(Constants.HeaderConstants.DATE, Utility.getGMTTime());

        final Canonicalizer canonicalizer = CanonicalizerFactory.getTableLiteCanonicalizer(request);

        final String stringToSign = canonicalizer.canonicalize(request, credentials.getAccountName(), contentLength);

        final String computedBase64Signature = StorageKey.computeMacSha256(credentials.getKey(), stringToSign);

        request.setRequestProperty(Constants.HeaderConstants.AUTHORIZATION,
                String.format("%s %s:%s", "SharedKeyLite", credentials.getAccountName(), computedBase64Signature));
    }

    /**
     * Private Default Ctor
     */
    private BaseRequest() {
        // No op
    }
}
