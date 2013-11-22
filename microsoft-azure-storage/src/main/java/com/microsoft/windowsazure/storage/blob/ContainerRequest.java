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
package com.microsoft.windowsazure.storage.blob;

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

import com.microsoft.windowsazure.storage.AccessCondition;
import com.microsoft.windowsazure.storage.Constants;
import com.microsoft.windowsazure.storage.Constants.HeaderConstants;
import com.microsoft.windowsazure.storage.Credentials;
import com.microsoft.windowsazure.storage.OperationContext;
import com.microsoft.windowsazure.storage.StorageException;
import com.microsoft.windowsazure.storage.core.BaseRequest;
import com.microsoft.windowsazure.storage.core.ListingContext;
import com.microsoft.windowsazure.storage.core.SR;
import com.microsoft.windowsazure.storage.core.UriQueryBuilder;
import com.microsoft.windowsazure.storage.core.Utility;

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
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the container.
     * @return a HttpURLConnection configured for the operation.
     * @throws StorageException
     * @throws IllegalArgumentException
     */
    public static HttpURLConnection delete(final URI uri, final int timeout, final AccessCondition accessCondition,
            final OperationContext opContext) throws IOException, URISyntaxException, StorageException {
        final UriQueryBuilder containerBuilder = getContainerUriQueryBuilder();
        HttpURLConnection request = BaseRequest.delete(uri, timeout, containerBuilder, opContext);
        if (accessCondition != null) {
            accessCondition.applyConditionToRequest(request);
        }

        return request;
    }

    /**
     * Constructs a web request to return the ACL for this container. Sign with no length specified.
     * 
     * @param uri
     *            The absolute URI to the container.
     * @param timeout
     *            The server timeout interval.
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the container.
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * @return a HttpURLConnection configured for the operation.
     * @throws StorageException
     */
    public static HttpURLConnection getAcl(final URI uri, final int timeout, final AccessCondition accessCondition,
            final OperationContext opContext) throws IOException, URISyntaxException, StorageException {
        final UriQueryBuilder builder = getContainerUriQueryBuilder();
        builder.add(Constants.QueryConstants.COMPONENT, Constants.QueryConstants.ACL);

        final HttpURLConnection request = createURLConnection(uri, timeout, builder, opContext);

        request.setRequestMethod(Constants.HTTP_GET);

        if (accessCondition != null && !Utility.isNullOrEmpty(accessCondition.getLeaseID())) {
            BaseRequest.addLeaseId(request, accessCondition.getLeaseID());
        }

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
            uriBuilder.add(Constants.QueryConstants.RESOURCETYPE, "container");
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
    public static HttpURLConnection getMetadata(final URI uri, final int timeout, AccessCondition accessCondition,
            final OperationContext opContext) throws IOException, URISyntaxException, StorageException {
        final UriQueryBuilder containerBuilder = getContainerUriQueryBuilder();
        HttpURLConnection request = BaseRequest.getMetadata(uri, timeout, containerBuilder, opContext);
        if (accessCondition != null && !Utility.isNullOrEmpty(accessCondition.getLeaseID())) {
            BaseRequest.addLeaseId(request, accessCondition.getLeaseID());
        }
        return request;
    }

    /**
     * Constructs a web request to return the user-defined metadata for this container. Sign with no length specified.
     * 
     * @param uri
     *            The absolute URI to the container.
     * @param timeout
     *            The server timeout interval.
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the container.
     * @return a HttpURLConnection configured for the operation.
     * @throws StorageException
     * */
    public static HttpURLConnection getProperties(final URI uri, final int timeout, AccessCondition accessCondition,
            final OperationContext opContext) throws IOException, URISyntaxException, StorageException {
        final UriQueryBuilder containerBuilder = getContainerUriQueryBuilder();
        HttpURLConnection request = BaseRequest.getProperties(uri, timeout, containerBuilder, opContext);
        if (accessCondition != null && !Utility.isNullOrEmpty(accessCondition.getLeaseID())) {
            BaseRequest.addLeaseId(request, accessCondition.getLeaseID());
        }

        return request;
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

        if (detailsIncluded == ContainerListingDetails.ALL || detailsIncluded == ContainerListingDetails.METADATA) {
            builder.add(Constants.QueryConstants.INCLUDE, Constants.QueryConstants.METADATA);
        }

        final HttpURLConnection request = createURLConnection(uri, timeout, builder, opContext);

        request.setRequestMethod(Constants.HTTP_GET);

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
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the container.
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * @return a HttpURLConnection configured for the operation.
     * @throws StorageException
     * */
    public static HttpURLConnection setAcl(final URI uri, final int timeout,
            final BlobContainerPublicAccessType publicAccess, final AccessCondition accessCondition,
            final OperationContext opContext) throws IOException, URISyntaxException, StorageException {
        final UriQueryBuilder builder = getContainerUriQueryBuilder();
        builder.add(Constants.QueryConstants.COMPONENT, Constants.QueryConstants.ACL);

        final HttpURLConnection request = createURLConnection(uri, timeout, builder, opContext);

        request.setRequestMethod(Constants.HTTP_PUT);
        request.setDoOutput(true);

        if (publicAccess != BlobContainerPublicAccessType.OFF) {
            request.setRequestProperty(BlobConstants.BLOB_PUBLIC_ACCESS_HEADER, publicAccess.toString().toLowerCase());
        }

        if (accessCondition != null && !Utility.isNullOrEmpty(accessCondition.getLeaseID())) {
            BaseRequest.addLeaseId(request, accessCondition.getLeaseID());
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
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the container.
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * @return a HttpURLConnection configured for the operation.
     * @throws StorageException
     * */
    public static HttpURLConnection setMetadata(final URI uri, final int timeout,
            final AccessCondition accessCondition, final OperationContext opContext) throws IOException,
            URISyntaxException, StorageException {
        final UriQueryBuilder containerBuilder = getContainerUriQueryBuilder();
        HttpURLConnection request = BaseRequest.setMetadata(uri, timeout, containerBuilder, opContext);
        if (accessCondition != null) {
            accessCondition.applyConditionToRequest(request);
        }

        return request;
    }

    /**
     * Constructs a HttpURLConnection to Acquire,Release,Break, or Renew a blob lease. Sign with 0 length.
     * 
     * @param uri
     *            The absolute URI to the blob
     * @param timeout
     *            The server timeout interval
     * @param action
     *            the LeaseAction to perform
     * 
     * @param visibilityTimeoutInSeconds
     *            Specifies the the span of time for which to acquire the lease, in seconds.
     *            If null, an infinite lease will be acquired. If not null, this must be greater than zero.
     * 
     * @param proposedLeaseId
     *            A <code>String</code> that represents the proposed lease ID for the new lease,
     *            or null if no lease ID is proposed.
     * 
     * @param breakPeriodInSeconds
     *            Specifies the amount of time to allow the lease to remain, in seconds.
     *            If null, the break period is the remainder of the current lease, or zero for infinite leases.
     * 
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the blob.
     * @param blobOptions
     *            the options to use for the request.
     * @param opContext
     *            a tracking object for the request
     * @return a HttpURLConnection to use to perform the operation.
     * @throws IOException
     *             if there is an error opening the connection
     * @throws URISyntaxException
     *             if the resource URI is invalid
     * @throws StorageException
     *             an exception representing any error which occurred during the operation.
     * @throws IllegalArgumentException
     */
    public static HttpURLConnection lease(final URI uri, final int timeout, final LeaseAction action,
            final Integer leaseTimeInSeconds, final String proposedLeaseId, final Integer breakPeriodInSeconds,
            final AccessCondition accessCondition, final BlobRequestOptions blobOptions,
            final OperationContext opContext) throws IOException, URISyntaxException, StorageException {

        final UriQueryBuilder builder = getContainerUriQueryBuilder();
        builder.add(Constants.QueryConstants.COMPONENT, BlobConstants.LEASE);

        final HttpURLConnection request = createURLConnection(uri, timeout, builder, opContext);

        request.setDoOutput(true);
        request.setRequestMethod(Constants.HTTP_PUT);
        request.setFixedLengthStreamingMode(0);
        request.setRequestProperty(HeaderConstants.LEASE_ACTION_HEADER, action.toString());

        if (leaseTimeInSeconds != null) {
            request.setRequestProperty(HeaderConstants.LEASE_DURATION, leaseTimeInSeconds.toString());
        }
        else {
            request.setRequestProperty(HeaderConstants.LEASE_DURATION, "-1");
        }

        if (proposedLeaseId != null) {
            request.setRequestProperty(HeaderConstants.PROPOSED_LEASE_ID_HEADER, proposedLeaseId);
        }

        if (breakPeriodInSeconds != null) {
            request.setRequestProperty(HeaderConstants.LEASE_BREAK_PERIOD_HEADER, breakPeriodInSeconds.toString());
        }

        if (accessCondition != null) {
            accessCondition.applyConditionToRequest(request);
        }
        return request;
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
            final HashMap<String, SharedAccessBlobPolicy> sharedAccessPolicies, final StringWriter outWriter)
            throws XMLStreamException {
        Utility.assertNotNull("sharedAccessPolicies", sharedAccessPolicies);
        Utility.assertNotNull("outWriter", outWriter);

        final XMLOutputFactory xmlOutFactoryInst = XMLOutputFactory.newInstance();
        final XMLStreamWriter xmlw = xmlOutFactoryInst.createXMLStreamWriter(outWriter);

        if (sharedAccessPolicies.keySet().size() > Constants.MAX_SHARED_ACCESS_POLICY_IDENTIFIERS) {
            final String errorMessage = String.format(SR.TOO_MANY_SHARED_ACCESS_POLICY_IDENTIFIERS,
                    sharedAccessPolicies.keySet().size(), Constants.MAX_SHARED_ACCESS_POLICY_IDENTIFIERS);

            throw new IllegalArgumentException(errorMessage);
        }

        // default is UTF8
        xmlw.writeStartDocument();
        xmlw.writeStartElement(Constants.SIGNED_IDENTIFIERS_ELEMENT);

        for (final Entry<String, SharedAccessBlobPolicy> entry : sharedAccessPolicies.entrySet()) {
            final SharedAccessBlobPolicy policy = entry.getValue();
            xmlw.writeStartElement(Constants.SIGNED_IDENTIFIER_ELEMENT);

            // Set the identifier
            xmlw.writeStartElement(Constants.ID);
            xmlw.writeCharacters(entry.getKey());
            xmlw.writeEndElement();

            xmlw.writeStartElement(Constants.ACCESS_POLICY);

            // Set the Start Time
            xmlw.writeStartElement(Constants.START);
            xmlw.writeCharacters(Utility.getUTCTimeOrEmpty(policy.getSharedAccessStartTime()));
            // end Start
            xmlw.writeEndElement();

            // Set the Expiry Time
            xmlw.writeStartElement(Constants.EXPIRY);
            xmlw.writeCharacters(Utility.getUTCTimeOrEmpty(policy.getSharedAccessExpiryTime()));
            // end Expiry
            xmlw.writeEndElement();

            // Set the Permissions
            xmlw.writeStartElement(Constants.PERMISSION);
            xmlw.writeCharacters(SharedAccessBlobPolicy.permissionsToString(policy.getPermissions()));
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
