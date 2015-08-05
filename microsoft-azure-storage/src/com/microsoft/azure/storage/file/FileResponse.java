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
package com.microsoft.azure.storage.file;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import com.microsoft.azure.storage.Constants;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.StorageUri;
import com.microsoft.azure.storage.core.BaseResponse;
import com.microsoft.azure.storage.core.PathUtility;
import com.microsoft.azure.storage.core.Utility;

/**
 * RESERVED FOR INTERNAL USE. A class used to parse the response from file, directory, and share operations.
 */
final class FileResponse extends BaseResponse {
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
            final CopyState copyState = new CopyState();
            
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

            final String copyCompletionTimeString =
                    request.getHeaderField(Constants.HeaderConstants.COPY_COMPLETION_TIME);
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
     * Gets the FileShareAttributes from the given request.
     * 
     * @param request
     *            the request to get attributes from
     * @param usePathStyleUris
     *            a value indicating if the account is using pathSytleUris
     * @return the FileShareAttributes from the given request.
     * @throws StorageException
     */
    public static FileShareAttributes getFileShareAttributes(final HttpURLConnection request,
            final boolean usePathStyleUris) throws StorageException {
        final FileShareAttributes shareAttributes = new FileShareAttributes();
        URI tempURI;
        try {
            tempURI = PathUtility.stripSingleURIQueryAndFragment(request.getURL().toURI());
        }
        catch (final URISyntaxException e) {
            final StorageException wrappedUnexpectedException = Utility.generateNewUnexpectedStorageException(e);
            throw wrappedUnexpectedException;
        }

        shareAttributes.setName(PathUtility.getShareNameFromUri(tempURI, usePathStyleUris));

        final FileShareProperties shareProperties = shareAttributes.getProperties();
        shareProperties.setEtag(BaseResponse.getEtag(request));
        shareProperties.setShareQuota(parseShareQuota(request));
        shareProperties.setLastModified(new Date(request.getLastModified()));
        shareAttributes.setMetadata(getMetadata(request));

        return shareAttributes;
    }

    /**
     * Gets the FileDirectoryAttributes from the given request.
     * 
     * @param request
     *            the request to get attributes from.
     * @param usePathStyleUris
     *            a value indicating if the account is using pathSytleUris.
     * @return the FileDirectoryAttributes from the given request.
     * @throws StorageException
     */
    public static FileDirectoryAttributes getFileDirectoryAttributes(final HttpURLConnection request,
            final boolean usePathStyleUris) throws StorageException {
        final FileDirectoryAttributes directoryAttributes = new FileDirectoryAttributes();
        URI tempURI;
        try {
            tempURI = PathUtility.stripSingleURIQueryAndFragment(request.getURL().toURI());
        }
        catch (final URISyntaxException e) {
            final StorageException wrappedUnexpectedException = Utility.generateNewUnexpectedStorageException(e);
            throw wrappedUnexpectedException;
        }

        directoryAttributes.setName(PathUtility.getDirectoryNameFromURI(tempURI, usePathStyleUris));

        final FileDirectoryProperties directoryProperties = directoryAttributes.getProperties();
        directoryProperties.setEtag(BaseResponse.getEtag(request));
        directoryProperties.setLastModified(new Date(request.getLastModified()));
        directoryAttributes.setMetadata(getMetadata(request));

        return directoryAttributes;
    }

    /**
     * Gets the CloudFileAttributes from the given request
     * 
     * @param request
     *            The response from server.
     * @param resourceURI
     *            The file uri to set.
     * 
     * @return the CloudFileAttributes from the given request
     * @throws ParseException 
     * @throws URISyntaxException 
     */
    public static FileAttributes getFileAttributes(final HttpURLConnection request, final StorageUri resourceURI)
            throws URISyntaxException, ParseException {
        final FileAttributes fileAttributes = new FileAttributes();
        final FileProperties properties = fileAttributes.getProperties();

        properties.setCacheControl(request.getHeaderField(Constants.HeaderConstants.CACHE_CONTROL));
        properties.setContentDisposition(request.getHeaderField(Constants.HeaderConstants.CONTENT_DISPOSITION));
        properties.setContentEncoding(request.getHeaderField(Constants.HeaderConstants.CONTENT_ENCODING));
        properties.setContentLanguage(request.getHeaderField(Constants.HeaderConstants.CONTENT_LANGUAGE));
        properties.setContentMD5(request.getHeaderField(Constants.HeaderConstants.CONTENT_MD5));
        properties.setContentType(request.getHeaderField(Constants.HeaderConstants.CONTENT_TYPE));
        properties.setEtag(BaseResponse.getEtag(request));
        properties.setCopyState(FileResponse.getCopyState(request));

        final Calendar lastModifiedCalendar = Calendar.getInstance(Utility.LOCALE_US);
        lastModifiedCalendar.setTimeZone(Utility.UTC_ZONE);
        lastModifiedCalendar.setTime(new Date(request.getLastModified()));
        properties.setLastModified(lastModifiedCalendar.getTime());

        final String rangeHeader = request.getHeaderField(Constants.HeaderConstants.CONTENT_RANGE);
        final String xContentLengthHeader = request.getHeaderField(FileConstants.CONTENT_LENGTH_HEADER);

        if (!Utility.isNullOrEmpty(rangeHeader)) {
            properties.setLength(Long.parseLong(rangeHeader.split("/")[1]));
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

        fileAttributes.setStorageUri(resourceURI);
        fileAttributes.setMetadata(BaseResponse.getMetadata(request));

        return fileAttributes;
    }

    /**
     * Parses out the share quota value from a <code>java.net.HttpURLConnection</code>.
     * 
     * @param request
     *            the request to get attributes from
     * @return the share quota (in GB) or <code>null</code> if none is specified
     */
    static Integer parseShareQuota(final HttpURLConnection request) {
        Integer shareQuota = request.getHeaderFieldInt(FileConstants.SHARE_QUOTA_HEADER, -1);
        return (shareQuota == -1) ? null : shareQuota;
    }

    /**
     * Private Default Ctor
     */
    private FileResponse() {
        super();
    }
}