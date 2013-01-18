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

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.stream.XMLStreamException;

import com.microsoft.windowsazure.services.blob.client.CopyState;
import com.microsoft.windowsazure.services.blob.client.CopyStatus;
import com.microsoft.windowsazure.services.core.storage.Constants;
import com.microsoft.windowsazure.services.core.storage.LeaseDuration;
import com.microsoft.windowsazure.services.core.storage.LeaseState;
import com.microsoft.windowsazure.services.core.storage.LeaseStatus;
import com.microsoft.windowsazure.services.core.storage.OperationContext;
import com.microsoft.windowsazure.services.core.storage.ServiceProperties;
import com.microsoft.windowsazure.services.core.storage.StorageException;
import com.microsoft.windowsazure.services.core.storage.utils.Utility;

/**
 * RESERVED FOR INTERNAL USE. The base response class for the protocol layer
 */
public class BaseResponse {
    /**
     * Gets the ContentMD5
     * 
     * @param request
     *            The response from server.
     * @return The ContentMD5.
     */
    public static String getContentMD5(final HttpURLConnection request) {
        return request.getHeaderField(Constants.HeaderConstants.CONTENT_MD5);
    }

    /**
     * Gets the Date
     * 
     * @param request
     *            The response from server.
     * @return The Date.
     */
    public static String getDate(final HttpURLConnection request) {
        final String retString = request.getHeaderField("Date");
        return retString == null ? request.getHeaderField(Constants.HeaderConstants.DATE) : retString;
    }

    /**
     * Gets the Etag
     * 
     * @param request
     *            The response from server.
     * @return The Etag.
     */
    public static String getEtag(final HttpURLConnection request) {
        return request.getHeaderField(Constants.ETAG_ELEMENT);
    }

    /**
     * Gets the metadata from the request The response from server.
     * 
     * @return the metadata from the request
     */
    public static HashMap<String, String> getMetadata(final HttpURLConnection request) {
        return getValuesByHeaderPrefix(request, Constants.HeaderConstants.PREFIX_FOR_STORAGE_METADATA);
    }

    /**
     * Gets the LeaseStatus
     * 
     * @param request
     *            The response from server.
     * @return The Etag.
     */
    public static LeaseStatus getLeaseStatus(final HttpURLConnection request) {
        final String leaseStatus = request.getHeaderField(Constants.HeaderConstants.LEASE_STATUS);
        if (!Utility.isNullOrEmpty(leaseStatus)) {
            return LeaseStatus.parse(leaseStatus);
        }

        return LeaseStatus.UNSPECIFIED;
    }

    /**
     * Gets the LeaseState
     * 
     * @param request
     *            The response from server.
     * @return The LeaseState.
     */
    public static LeaseState getLeaseState(final HttpURLConnection request) {
        final String leaseState = request.getHeaderField(Constants.HeaderConstants.LEASE_STATE);
        if (!Utility.isNullOrEmpty(leaseState)) {
            return LeaseState.parse(leaseState);
        }

        return LeaseState.UNSPECIFIED;
    }

    /**
     * Gets the LeaseDuration
     * 
     * @param request
     *            The response from server.
     * @return The LeaseDuration.
     */
    public static LeaseDuration getLeaseDuration(final HttpURLConnection request) {
        final String leaseDuration = request.getHeaderField(Constants.HeaderConstants.LEASE_DURATION);
        if (!Utility.isNullOrEmpty(leaseDuration)) {
            return LeaseDuration.parse(leaseDuration);
        }

        return LeaseDuration.UNSPECIFIED;
    }

    /**
     * Gets the copyState
     * 
     * @param request
     *            The response from server.
     * @return The CopyState.
     * @throws URISyntaxException
     * @throws ParseException
     */
    public static CopyState getCopyState(final HttpURLConnection request) throws URISyntaxException, ParseException {
        String copyStatusString = request.getHeaderField(Constants.HeaderConstants.COPY_STATUS);
        if (!Utility.isNullOrEmpty(copyStatusString)) {
            CopyState copyState = new CopyState();
            copyState.setStatus(CopyStatus.parse(copyStatusString));
            copyState.setCopyId(request.getHeaderField(Constants.HeaderConstants.COPY_ID));
            copyState.setStatusDescription(request.getHeaderField(Constants.HeaderConstants.COPY_STATUS_DESCRIPTION));

            final String copyProgressString = request.getHeaderField(Constants.HeaderConstants.COPY_PROGRESS);
            if (!Utility.isNullOrEmpty(copyProgressString)) {
                String[] progressSequence = copyProgressString.split("/");
                copyState.setBytesCopied(Long.parseLong(progressSequence[0]));
                copyState.setTotalBytes(Long.parseLong(progressSequence[1]));
            }

            final String copySourceString = request.getHeaderField(Constants.HeaderConstants.COPY_SOURCE);
            if (!Utility.isNullOrEmpty(copySourceString)) {
                copyState.setSource(new URI(copySourceString));
            }

            final String copyCompletionTimeString = request
                    .getHeaderField(Constants.HeaderConstants.COPY_COMPLETION_TIME);
            if (!Utility.isNullOrEmpty(copyCompletionTimeString)) {
                copyState.setCompletionTime(Utility.parseRFC1123DateFromStringInGMT(copyCompletionTimeString));
            }

            return copyState;
        }
        else {
            return null;
        }
    }

    /**
     * Gets the request id.
     * 
     * @param request
     *            The response from server.
     * @return The request ID.
     */
    public static String getRequestId(final HttpURLConnection request) {
        return request.getHeaderField(Constants.HeaderConstants.REQUEST_ID_HEADER);
    }

    /**
     * Returns all the header/value pairs with the given prefix.
     * 
     * @param request
     *            the request object containing headers to parse.
     * @param prefix
     *            the prefix for headers to be returned.
     * @return all the header/value pairs with the given prefix.
     */
    private static HashMap<String, String> getValuesByHeaderPrefix(final HttpURLConnection request, final String prefix) {
        final HashMap<String, String> retVals = new HashMap<String, String>();
        final Map<String, List<String>> headerMap = request.getHeaderFields();
        final int prefixLength = prefix.length();

        for (final Entry<String, List<String>> entry : headerMap.entrySet()) {
            if (entry.getKey() != null && entry.getKey().startsWith(prefix)) {
                final List<String> currHeaderValues = entry.getValue();
                retVals.put(entry.getKey().substring(prefixLength), currHeaderValues.get(0));
            }
        }

        return retVals;
    }

    /**
     * Deserializes the ServiceProperties object from an input stream.
     * 
     * @param inStream
     *            the stream to read from.
     * @param opContext
     *            an object used to track the execution of the operation
     * @return a ServiceProperties object representing the Analytics configuration for the client.
     * @throws XMLStreamException
     *             if the xml is invalid.
     * @throws StorageException
     *             if unexpected xml is found.
     */
    public static ServiceProperties readServicePropertiesFromStream(final InputStream inStream,
            final OperationContext opContext) throws XMLStreamException, StorageException {
        return ServiceProperties.readServicePropertiesFromStream(inStream, opContext);
    }

    /**
     * Private Default Ctor
     */
    protected BaseResponse() {
        // No op
    }
}
