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
import java.util.Map;
import java.util.Map.Entry;

import com.microsoft.azure.storage.Constants;
import com.microsoft.azure.storage.OperationContext;
import com.microsoft.azure.storage.RequestOptions;
import com.microsoft.azure.storage.StorageException;

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
     * Adds the metadata.
     * 
     * @param request
     *            The request.
     * @param metadata
     *            The metadata.
     */
    public static void addMetadata(final HttpURLConnection request, final Map<String, String> metadata,
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
     *            A {@link RequestOptions} object that specifies execution options such as retry policy and timeout
     *            settings for the operation. 
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

        if (options.getTimeoutIntervalInMs() != null && options.getTimeoutIntervalInMs() != 0) {
            builder.add(TIMEOUT, String.valueOf(options.getTimeoutIntervalInMs() / 1000));
        }
        
        final URL resourceUrl = builder.addToURI(uri).toURL();

        final HttpURLConnection retConnection;
        
        // Set up connection, optionally with proxy settings
        if (opContext != null && opContext.getProxy() != null) {
            retConnection = (HttpURLConnection) resourceUrl.openConnection(opContext.getProxy());
        } 
        else {
            retConnection = (HttpURLConnection) resourceUrl.openConnection();
        }

        /*
         * ReadTimeout must be explicitly set to avoid a bug in JDK 6. In certain cases, this bug causes an immediate 
         * read timeout exception to be thrown even if ReadTimeout is not set.
         * 
         * Both connect and read timeout are set to the same value as we have no way of knowing how to partition
         * the remaining time between these operations. The user can override these timeouts using the SendingRequest
         * event handler if more control is desired.
         */
        int timeout = Utility.getRemainingTimeout(options.getOperationExpiryTimeInMs(), options.getTimeoutIntervalInMs());
        retConnection.setReadTimeout(timeout);
        retConnection.setConnectTimeout(timeout);

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
     * Gets a {@link UriQueryBuilder} for listing.
     * 
     * @param listingContext
     *            A {@link ListingContext} object that specifies parameters for
     *            the listing operation, if any. May be <code>null</code>.
     *            
     * @throws StorageException
     *             If a storage service error occurred during the operation.
     */
    public static UriQueryBuilder getListUriQueryBuilder(final ListingContext listingContext) throws StorageException {
        final UriQueryBuilder builder = new UriQueryBuilder();    
        builder.add(Constants.QueryConstants.COMPONENT, Constants.QueryConstants.LIST);

        if (listingContext != null) {
            if (!Utility.isNullOrEmpty(listingContext.getPrefix())) {
                builder.add(Constants.QueryConstants.PREFIX, listingContext.getPrefix());
            }

            if (!Utility.isNullOrEmpty(listingContext.getMarker())) {
                builder.add(Constants.QueryConstants.MARKER, listingContext.getMarker());
            }

            if (listingContext.getMaxResults() != null && listingContext.getMaxResults() > 0) {
                builder.add(Constants.QueryConstants.MAX_RESULTS, listingContext.getMaxResults().toString());
            }
        }

        return builder;
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
     * A private default constructor. All methods of this class are static so no instances of it should ever be created.
     */
    private BaseRequest() {
        // No op
    }
}
