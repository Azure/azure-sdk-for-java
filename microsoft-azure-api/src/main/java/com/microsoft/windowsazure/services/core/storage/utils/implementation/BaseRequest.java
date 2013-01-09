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
package com.microsoft.windowsazure.services.core.storage.utils.implementation;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.xml.stream.XMLStreamException;

import com.microsoft.windowsazure.services.core.storage.Constants;
import com.microsoft.windowsazure.services.core.storage.Credentials;
import com.microsoft.windowsazure.services.core.storage.OperationContext;
import com.microsoft.windowsazure.services.core.storage.ServiceProperties;
import com.microsoft.windowsazure.services.core.storage.StorageException;
import com.microsoft.windowsazure.services.core.storage.StorageKey;
import com.microsoft.windowsazure.services.core.storage.utils.UriQueryBuilder;
import com.microsoft.windowsazure.services.core.storage.utils.Utility;

/**
 * RESERVED FOR INTERNAL USE. The Base Request class for the protocol layer.
 */
public final class BaseRequest {
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
            BaseRequest.addOptionalHeader(request, "x-ms-lease-id", leaseId);
        }
    }

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
    public static void addMetadata(final HttpURLConnection request, final String name, final String value,
            final OperationContext opContext) {
        Utility.assertNotNullOrEmpty("value", value);

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
     * Adds the snapshot.
     * 
     * @param builder
     *            a query builder.
     * @param snapshotVersion
     *            the snapshot version to the query builder.
     * @throws StorageException
     */
    public static void addSnapshot(final UriQueryBuilder builder, final String snapshotVersion) throws StorageException {
        if (snapshotVersion != null) {
            builder.add("snapshot", snapshotVersion);
        }
    }

    /**
     * Creates the specified resource. Note request is set to setFixedLengthStreamingMode(0); Sign with 0 length.
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
     * @throws IllegalArgumentException
     */
    public static HttpURLConnection create(final URI uri, final int timeout, UriQueryBuilder builder,
            final OperationContext opContext) throws IOException, URISyntaxException, StorageException {
        if (builder == null) {
            builder = new UriQueryBuilder();
        }

        final HttpURLConnection retConnection = createURLConnection(uri, timeout, builder, opContext);
        retConnection.setFixedLengthStreamingMode(0);
        retConnection.setDoOutput(true);
        retConnection.setRequestMethod("PUT");

        return retConnection;
    }

    /**
     * Creates the web request.
     * 
     * @param uri
     *            the request Uri.
     * @param timeoutInMs
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
    public static HttpURLConnection createURLConnection(final URI uri, final int timeoutInMs, UriQueryBuilder builder,
            final OperationContext opContext) throws IOException, URISyntaxException, StorageException {
        if (builder == null) {
            builder = new UriQueryBuilder();
        }

        if (timeoutInMs != 0) {
            builder.add("timeout", String.valueOf(timeoutInMs / 1000));
        }

        final URL resourceUrl = builder.addToURI(uri).toURL();

        final HttpURLConnection retConnection = (HttpURLConnection) resourceUrl.openConnection();

        retConnection.setReadTimeout(timeoutInMs);

        // Note : accept behavior, java by default sends Accept behavior
        // as text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2 This will need to be set for table requests.
        //
        // To override set retConnection.setRequestProperty("Accept",
        // "application/atom+xml");

        retConnection.setRequestProperty(Constants.HeaderConstants.STORAGE_VERSION_HEADER,
                Constants.HeaderConstants.TARGET_STORAGE_VERSION);
        retConnection.setRequestProperty(Constants.HeaderConstants.USER_AGENT, getUserAgent());

        // Java6 TODO remove me, this has to be manually set or it will
        // sometimes default to application/x-www-form-urlencoded without us
        // knowing causing auth fails in Java5.
        retConnection.setRequestProperty(Constants.HeaderConstants.CONTENT_TYPE, "");
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
    public static HttpURLConnection delete(final URI uri, final int timeout, UriQueryBuilder builder,
            final OperationContext opContext) throws IOException, URISyntaxException, StorageException {
        if (builder == null) {
            builder = new UriQueryBuilder();
        }

        final HttpURLConnection retConnection = createURLConnection(uri, timeout, builder, opContext);

        retConnection.setDoOutput(true);
        retConnection.setRequestMethod("DELETE");

        return retConnection;
    }

    /**
     * Gets the metadata. Sign with no length specified.
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
    public static HttpURLConnection getMetadata(final URI uri, final int timeout, UriQueryBuilder builder,
            final OperationContext opContext) throws StorageException, IOException, URISyntaxException {
        if (builder == null) {
            builder = new UriQueryBuilder();
        }

        builder.add("comp", "metadata");
        final HttpURLConnection retConnection = createURLConnection(uri, timeout, builder, opContext);

        retConnection.setDoOutput(true);
        retConnection.setRequestMethod("HEAD");

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
    public static HttpURLConnection getProperties(final URI uri, final int timeout, UriQueryBuilder builder,
            final OperationContext opContext) throws IOException, URISyntaxException, StorageException {
        if (builder == null) {
            builder = new UriQueryBuilder();
        }

        final HttpURLConnection retConnection = createURLConnection(uri, timeout, builder, opContext);

        retConnection.setDoOutput(true);
        retConnection.setRequestMethod("HEAD");

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
    public static HttpURLConnection getServiceProperties(final URI uri, final int timeout, UriQueryBuilder builder,
            final OperationContext opContext) throws IOException, URISyntaxException, StorageException {
        if (builder == null) {
            builder = new UriQueryBuilder();
        }

        builder.add("comp", "properties");
        builder.add("restype", "service");

        final HttpURLConnection retConnection = createURLConnection(uri, timeout, builder, opContext);

        retConnection.setDoOutput(true);
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
            userAgent = String.format("%s/%s", Constants.HeaderConstants.USER_AGENT_PREFIX,
                    Constants.HeaderConstants.USER_AGENT_VERSION);
        }
        return userAgent;
    }

    /**
     * Writes the contents of the ServiceProperties object to a byte array in XML form.
     * 
     * @param properties
     *            the ServiceProperties to write to the stream.
     * @param opContext
     *            an object used to track the execution of the operation
     * @return the number of bytes written to the output stream.
     * @throws XMLStreamException
     *             if there is an error writing the content to the stream.
     * @throws StorageException
     */
    public static byte[] serializeServicePropertiesToByteArray(final ServiceProperties properties,
            final OperationContext opContext) throws XMLStreamException, StorageException {
        return properties.serializeToByteArray(opContext);
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
    public static HttpURLConnection setMetadata(final URI uri, final int timeout, UriQueryBuilder builder,
            final OperationContext opContext) throws IOException, URISyntaxException, StorageException {

        if (builder == null) {
            builder = new UriQueryBuilder();
        }

        builder.add("comp", "metadata");
        final HttpURLConnection retConnection = createURLConnection(uri, timeout, builder, opContext);

        retConnection.setFixedLengthStreamingMode(0);
        retConnection.setDoOutput(true);
        retConnection.setRequestMethod("PUT");

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
    public static HttpURLConnection setServiceProperties(final URI uri, final int timeout, UriQueryBuilder builder,
            final OperationContext opContext) throws IOException, URISyntaxException, StorageException {
        if (builder == null) {
            builder = new UriQueryBuilder();
        }

        builder.add("comp", "properties");
        builder.add("restype", "service");

        final HttpURLConnection retConnection = createURLConnection(uri, timeout, builder, opContext);

        retConnection.setDoOutput(true);
        retConnection.setRequestMethod("PUT");

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

        final String stringToSign = canonicalizer.canonicalize(request, credentials.getAccountName(), contentLength,
                opContext);

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

        final String stringToSign = canonicalizer.canonicalize(request, credentials.getAccountName(), contentLength,
                opContext);

        final String computedBase64Signature = StorageKey.computeMacSha256(credentials.getKey(), stringToSign);

        // VNext add logging
        // System.out.println(String.format("Signing %s\r\n%s\r\n",
        // stringToSign, computedBase64Signature));
        request.setRequestProperty(Constants.HeaderConstants.AUTHORIZATION,
                String.format("%s %s:%s", "SharedKeyLite", credentials.getAccountName(), computedBase64Signature));
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
    public static void signRequestForTableSharedKeyLite(final HttpURLConnection request, final Credentials credentials,
            final Long contentLength, final OperationContext opContext) throws InvalidKeyException, StorageException {
        request.setRequestProperty(Constants.HeaderConstants.DATE, Utility.getGMTTime());

        final Canonicalizer canonicalizer = CanonicalizerFactory.getTableLiteCanonicalizer(request);

        final String stringToSign = canonicalizer.canonicalize(request, credentials.getAccountName(), contentLength,
                opContext);

        final String computedBase64Signature = StorageKey.computeMacSha256(credentials.getKey(), stringToSign);

        // TODO Vnext add logging
        // System.out.println(String.format("Signing %s\r\n%s\r\n", stringToSign, computedBase64Signature));
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
