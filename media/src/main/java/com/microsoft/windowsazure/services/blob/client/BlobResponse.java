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
package com.microsoft.windowsazure.services.blob.client;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import com.microsoft.windowsazure.services.core.storage.Constants;
import com.microsoft.windowsazure.services.core.storage.OperationContext;
import com.microsoft.windowsazure.services.core.storage.utils.Utility;
import com.microsoft.windowsazure.services.core.storage.utils.implementation.BaseResponse;

/**
 * RESERVED FOR INTERNAL USE. A class for parsing various responses from the blob service
 */
final class BlobResponse extends BaseResponse {

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
     * Gets the BlobAttributes from the given request
     * 
     * @param request
     *            The response from server.
     * @param resourceURI
     *            The blob uri to set.
     * @param snapshotID
     *            The snapshot version, if the blob is a snapshot.
     * @param opContext
     *            a tracking object for the request
     * @return the BlobAttributes from the given request
     * @throws ParseException
     * @throws URISyntaxException
     */
    public static BlobAttributes getAttributes(final HttpURLConnection request, final URI resourceURI,
            final String snapshotID, final OperationContext opContext) throws URISyntaxException, ParseException {

        final String blobType = request.getHeaderField(BlobConstants.BLOB_TYPE_HEADER);
        final BlobAttributes attributes = new BlobAttributes(BlobType.parse(blobType));
        final BlobProperties properties = attributes.getProperties();

        properties.setCacheControl(request.getHeaderField(Constants.HeaderConstants.CACHE_CONTROL));
        properties.setContentEncoding(request.getHeaderField(Constants.HeaderConstants.CONTENT_ENCODING));
        properties.setContentLanguage(request.getHeaderField(Constants.HeaderConstants.CONTENT_LANGUAGE));
        properties.setContentMD5(request.getHeaderField(Constants.HeaderConstants.CONTENT_MD5));
        properties.setContentType(request.getHeaderField(Constants.HeaderConstants.CONTENT_TYPE));
        properties.setEtag(request.getHeaderField(Constants.HeaderConstants.ETAG));

        final Calendar lastModifiedCalendar = Calendar.getInstance(Utility.LOCALE_US);
        lastModifiedCalendar.setTimeZone(Utility.UTC_ZONE);
        lastModifiedCalendar.setTime(new Date(request.getLastModified()));
        properties.setLastModified(lastModifiedCalendar.getTime());

        properties.setLeaseStatus(BaseResponse.getLeaseStatus(request));
        properties.setLeaseState(BaseResponse.getLeaseState(request));
        properties.setLeaseDuration(BaseResponse.getLeaseDuration(request));

        final String rangeHeader = request.getHeaderField(Constants.HeaderConstants.CONTENT_RANGE);
        final String xContentLengthHeader = request.getHeaderField(BlobConstants.CONTENT_LENGTH_HEADER);

        if (!Utility.isNullOrEmpty(rangeHeader)) {
            properties.setLength(Long.parseLong(rangeHeader));
        }
        else if (!Utility.isNullOrEmpty(xContentLengthHeader)) {
            properties.setLength(Long.parseLong(xContentLengthHeader));
        }
        else {
            // using this instead of the request property since the request
            // property only returns an int.
            final String contentLength = request.getHeaderField(Constants.HeaderConstants.CONTENT_LENGTH);

            if (!Utility.isNullOrEmpty(contentLength)) {
                properties.setLength(Long.parseLong(contentLength));
            }
        }

        attributes.uri = resourceURI;
        attributes.snapshotID = snapshotID;

        attributes.setMetadata(getMetadata(request));

        attributes.setCopyState(BlobResponse.getCopyState(request));
        return attributes;
    }

    /**
     * Gets the lease id from the request header.
     * 
     * @param request
     *            The response from server.
     * @param opContext
     *            a tracking object for the request
     * @return the lease id from the request header.
     */
    public static String getLeaseID(final HttpURLConnection request, final OperationContext opContext) {
        return request.getHeaderField("x-ms-lease-id");
    }

    /**
     * Gets the lease Time from the request header.
     * 
     * @param request
     *            The response from server.
     * @param opContext
     *            a tracking object for the request
     * @return the lease Time from the request header.
     */
    public static String getLeaseTime(final HttpURLConnection request, final OperationContext opContext) {
        return request.getHeaderField("x-ms-lease-time");
    }

    /**
     * Gets the snapshot ID from the request header.
     * 
     * @param request
     *            The response from server.
     * @param opContext
     *            a tracking object for the request
     * @return the snapshot ID from the request header.
     */
    public static String getSnapshotTime(final HttpURLConnection request, final OperationContext opContext) {
        return request.getHeaderField("x-ms-snapshot");
    }
}
