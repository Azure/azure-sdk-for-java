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
package com.microsoft.windowsazure.services.blob.client;

import java.io.IOException;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.microsoft.windowsazure.services.core.storage.Constants;
import com.microsoft.windowsazure.services.core.storage.Credentials;
import com.microsoft.windowsazure.services.core.storage.OperationContext;
import com.microsoft.windowsazure.services.core.storage.StorageException;
import com.microsoft.windowsazure.services.core.storage.utils.UriQueryBuilder;
import com.microsoft.windowsazure.services.core.storage.utils.Utility;
import com.microsoft.windowsazure.services.core.storage.utils.implementation.BaseRequest;
import com.microsoft.windowsazure.services.core.storage.utils.implementation.ListingContext;

/**
 * RESERVED FOR INTERNAL USE. A class used to generate requests for contianer objects.
 */
final class ContainerRequest {
    /**
     * Adds user-defined metadata to the request as one or more name-value pairs.
     * 
     * @param request
     *            The web request.
     * @param metadata
     *            The user-defined metadata.
     * */
    public static void addMetadata(final HttpURLConnection request, final HashMap<String, String> metadata,
            final OperationContext opContext) {
        BaseRequest.addMetadata(request, metadata, opContext);
    }

    /**
     * Adds user-defined metadata to the request as a single name-value pair.
     * 
     * @param request
     *            The web request.
     * @param name
     *            The metadata name.
     * @param value
     *            The metadata value.
     * */
    public static void addMetadata(final HttpURLConnection request, final String name, final String value,
            final OperationContext opContext) {
        BaseRequest.addMetadata(request, name, value, opContext);
    }

    /**
     * Constructs a web request to create a new container. Sign with 0 length.
     * 
     * @param uri
     *            The absolute URI to the container.
     * @param timeout
     *            The server timeout interval.
     * @return a HttpURLConnection configured for the operation.
     * @throws StorageException
     * @throws IllegalArgumentException
     */
    public static HttpURLConnection create(final URI uri, final int timeout, final OperationContext opContext)
            throws IOException, URISyntaxException, StorageException {
        final UriQueryBuilder containerBuilder = getContainerUriQueryBuilder();
        return BaseRequest.create(uri, timeout, containerBuilder, opContext);
    }

    /**
     * Creates the web request.
     * 
     * @param uri
     *            The absolute URI to the container.
     * @param timeout
     *            The server timeout interval.
     * @param query
     *            The query builder to use.
     * @return a HttpURLConnection configured for the operation.
     * @throws StorageException
     * @throws IllegalArgumentException
     */
    private static HttpURLConnection createURLConnection(final URI uri, final int timeout, final UriQueryBuilder query,
            final OperationContext opContext) throws IOException, URISyntaxException, StorageException {
        return BaseRequest.createURLConnection(uri, timeout, query, opContext);
    }

    /**
     * Constructs a web request to delete the container and all of blobs within it. Sign with no length specified.
     * 
     * @param uri
     *            The absolute URI to the container.
     * @param timeout
     *            The server timeout interval.
     * @return a HttpURLConnection configured for the operation.
     * @throws StorageException
     * @throws IllegalArgumentException
     */
    public static HttpURLConnection delete(final URI uri, final int timeout, final OperationContext opContext)
            throws IOException, URISyntaxException, StorageException {
        final UriQueryBuilder containerBuilder = getContainerUriQueryBuilder();
        return BaseRequest.delete(uri, timeout, containerBuilder, opContext);
    }

    /**
     * Constructs a web request to return the ACL for this container. Sign with no length specified.
     * 
     * @param uri
     *            The absolute URI to the container.
     * @param timeout
     *            The server timeout interval.
     * @return a HttpURLConnection configured for the operation.
     * @throws StorageException
     */
    public static HttpURLConnection getAcl(final URI uri, final int timeout, final OperationContext opContext)
            throws IOException, URISyntaxException, StorageException {
        final UriQueryBuilder builder = getContainerUriQueryBuilder();
        builder.add("comp", "acl");

        final HttpURLConnection request = createURLConnection(uri, timeout, builder, opContext);

        request.setRequestMethod("GET");

        return request;
    }

    /**
     * Gets the container Uri query builder.
     * 
     * A <CODE>UriQueryBuilder</CODE> for the container.
     * 
     * @throws StorageException
     */
    protected static UriQueryBuilder getContainerUriQueryBuilder() throws StorageException {
        final UriQueryBuilder uriBuilder = new UriQueryBuilder();
        try {
            uriBuilder.add("restype", "container");
        }
        catch (final IllegalArgumentException e) {
            throw Utility.generateNewUnexpectedStorageException(e);
        }
        return uriBuilder;
    }

    /**
     * Constructs a web request to retrieve the container's metadata. Sign with no length specified.
     * 
     * @param uri
     *            The absolute URI to the container.
     * @param timeout
     *            The server timeout interval.
     * @return a HttpURLConnection configured for the operation.
     * @throws StorageException
     */
    public static HttpURLConnection getMetadata(final URI uri, final int timeout, final OperationContext opContext)
            throws IOException, URISyntaxException, StorageException {
        final UriQueryBuilder containerBuilder = getContainerUriQueryBuilder();
        return BaseRequest.getMetadata(uri, timeout, containerBuilder, opContext);
    }

    /**
     * Constructs a web request to return the user-defined metadata for this container. Sign with no length specified.
     * 
     * @param uri
     *            The absolute URI to the container.
     * @param timeout
     *            The server timeout interval.
     * @return a HttpURLConnection configured for the operation.
     * @throws StorageException
     * */
    public static HttpURLConnection getProperties(final URI uri, final int timeout, final OperationContext opContext)
            throws IOException, URISyntaxException, StorageException {
        final UriQueryBuilder containerBuilder = getContainerUriQueryBuilder();
        return BaseRequest.getProperties(uri, timeout, containerBuilder, opContext);
    }

    /**
     * Constructs a request to return a listing of all containers in this storage account. Sign with no length
     * specified.
     * 
     * @param uri
     *            The absolute URI for the account.
     * @param timeout
     *            The absolute URI for the account.
     * @param listingContext
     *            A set of parameters for the listing operation.
     * @param detailsIncluded
     *            Additional details to return with the listing.
     * @param opContext
     *            a tracking object for the request
     * @return a HttpURLConnection configured for the operation.
     * @throws IOException
     * @throws URISyntaxException
     * @throws StorageException
     * @throws IllegalArgumentException
     */
    public static HttpURLConnection list(final URI uri, final int timeout, final ListingContext listingContext,
            final ContainerListingDetails detailsIncluded, final OperationContext opContext) throws URISyntaxException,
            IOException, StorageException {

        final UriQueryBuilder builder = getContainerUriQueryBuilder();
        builder.add("comp", "list");

        if (listingContext != null) {
            if (!Utility.isNullOrEmpty(listingContext.getPrefix())) {
                builder.add("prefix", listingContext.getPrefix());
            }

            if (!Utility.isNullOrEmpty(listingContext.getMarker())) {
                builder.add("marker", listingContext.getMarker());
            }

            if (listingContext.getMaxResults() != null && listingContext.getMaxResults() > 0) {
                builder.add("maxresults", listingContext.getMaxResults().toString());
            }
        }

        if (detailsIncluded == ContainerListingDetails.ALL || detailsIncluded == ContainerListingDetails.METADATA) {
            builder.add("include", "metadata");
        }

        final HttpURLConnection request = createURLConnection(uri, timeout, builder, opContext);

        request.setRequestMethod("GET");

        return request;
    }

    /**
     * Sets the ACL for the container. , Sign with length of aclBytes.
     * 
     * @param uri
     *            The absolute URI to the container.
     * @param timeout
     *            The server timeout interval.
     * @param publicAccess
     *            The type of public access to allow for the container.
     * @return a HttpURLConnection configured for the operation.
     * @throws StorageException
     * */
    public static HttpURLConnection setAcl(final URI uri, final int timeout,
            final BlobContainerPublicAccessType publicAccess, final OperationContext opContext) throws IOException,
            URISyntaxException, StorageException {
        final UriQueryBuilder builder = getContainerUriQueryBuilder();
        builder.add("comp", "acl");

        final HttpURLConnection request = createURLConnection(uri, timeout, builder, opContext);

        request.setRequestMethod("PUT");
        request.setDoOutput(true);

        if (publicAccess != BlobContainerPublicAccessType.OFF) {
            request.setRequestProperty(BlobConstants.BLOB_PUBLIC_ACCESS_HEADER, publicAccess.toString().toLowerCase());
        }

        return request;
    }

    /**
     * Constructs a web request to set user-defined metadata for the container, Sign with 0 Length.
     * 
     * @param uri
     *            The absolute URI to the container.
     * @param timeout
     *            The server timeout interval.
     * 
     * @return a HttpURLConnection configured for the operation.
     * @throws StorageException
     * */
    public static HttpURLConnection setMetadata(final URI uri, final int timeout, final OperationContext opContext)
            throws IOException, URISyntaxException, StorageException {
        final UriQueryBuilder containerBuilder = getContainerUriQueryBuilder();
        return BaseRequest.setMetadata(uri, timeout, containerBuilder, opContext);
    }

    /**
     * Signs the request for Shared Key authentication.
     * 
     * @param request
     *            The web request.
     * @param credentials
     *            The account credentials.
     * @throws StorageException
     * */
    public static void signRequest(final HttpURLConnection request, final Credentials credentials,
            final Long contentLength, final OperationContext opContext) throws InvalidKeyException, StorageException {
        BaseRequest.signRequestForBlobAndQueue(request, credentials, contentLength, opContext);
    }

    /**
     * Signs the request for Shared Key Lite authentication.
     * 
     * @param request
     *            The web request.
     * @param credentials
     *            The account credentials.
     * @throws StorageException
     * @throws InvalidKeyException
     * */
    public static void signRequestForSharedKeyLite(final HttpURLConnection request, final Credentials credentials,
            final Long contentLength, final OperationContext opContext) throws InvalidKeyException, StorageException {
        BaseRequest.signRequestForBlobAndQueueSharedKeyLite(request, credentials, contentLength, opContext);
    }

    /**
     * Writes a collection of shared access policies to the specified stream in XML format.
     * 
     * @param sharedAccessPolicies
     *            A collection of shared access policies
     * @param outWriter
     *            an sink to write the output to.
     * @throws XMLStreamException
     */
    public static void writeSharedAccessIdentifiersToStream(
            final HashMap<String, SharedAccessPolicy> sharedAccessPolicies, final StringWriter outWriter)
            throws XMLStreamException {
        Utility.assertNotNull("sharedAccessPolicies", sharedAccessPolicies);
        Utility.assertNotNull("outWriter", outWriter);

        final XMLOutputFactory xmlOutFactoryInst = XMLOutputFactory.newInstance();
        final XMLStreamWriter xmlw = xmlOutFactoryInst.createXMLStreamWriter(outWriter);

        if (sharedAccessPolicies.keySet().size() > BlobConstants.MAX_SHARED_ACCESS_POLICY_IDENTIFIERS) {
            final String errorMessage = String
                    .format("Too many %d shared access policy identifiers provided. Server does not support setting more than %d on a single container.",
                            sharedAccessPolicies.keySet().size(), BlobConstants.MAX_SHARED_ACCESS_POLICY_IDENTIFIERS);

            throw new IllegalArgumentException(errorMessage);
        }

        // default is UTF8
        xmlw.writeStartDocument();
        xmlw.writeStartElement(BlobConstants.SIGNED_IDENTIFIERS_ELEMENT);

        for (final Entry<String, SharedAccessPolicy> entry : sharedAccessPolicies.entrySet()) {
            final SharedAccessPolicy policy = entry.getValue();
            xmlw.writeStartElement(BlobConstants.SIGNED_IDENTIFIER_ELEMENT);

            // Set the identifier
            xmlw.writeStartElement(Constants.ID);
            xmlw.writeCharacters(entry.getKey());
            xmlw.writeEndElement();

            xmlw.writeStartElement(BlobConstants.ACCESS_POLICY);

            // Set the Start Time
            xmlw.writeStartElement(BlobConstants.START);
            xmlw.writeCharacters(Utility.getUTCTimeOrEmpty(policy.getSharedAccessStartTime()));
            // end Start
            xmlw.writeEndElement();

            // Set the Expiry Time
            xmlw.writeStartElement(BlobConstants.EXPIRY);
            xmlw.writeCharacters(Utility.getUTCTimeOrEmpty(policy.getSharedAccessExpiryTime()));
            // end Expiry
            xmlw.writeEndElement();

            // Set the Permissions
            xmlw.writeStartElement(BlobConstants.PERMISSION);
            xmlw.writeCharacters(SharedAccessPolicy.permissionsToString(policy.getPermissions()));
            // end Permission
            xmlw.writeEndElement();

            // end AccessPolicy
            xmlw.writeEndElement();
            // end SignedIdentifier
            xmlw.writeEndElement();
        }

        // end SignedIdentifiers
        xmlw.writeEndElement();
        // end doc
        xmlw.writeEndDocument();
    }

    /**
     * Private Default Ctor
     */
    private ContainerRequest() {
        // No op
    }
}
