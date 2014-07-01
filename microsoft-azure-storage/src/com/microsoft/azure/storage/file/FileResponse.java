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
     * Gets the FileShareAttributes from the given request.
     * 
     * @param request
     *            the request to get attributes from.
     * @param usePathStyleUris
     *            a value indicating if the account is using pathSytleUris.
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
        shareProperties.setLastModified(new Date(request.getLastModified()));
        shareAttributes.setMetadata(getMetadata(request));

        return shareAttributes;
    }

    /**
     * Gets the FileDirectoryProperties from the given request.
     * 
     * @param request
     *            the request to get properties from.
     * @return the FileDirectoryProperties from the given request.
     */
    public static FileDirectoryProperties getFileDirectoryProperties(final HttpURLConnection request) {
        final FileDirectoryProperties directoryProperties = new FileDirectoryProperties();
        directoryProperties.setEtag(BaseResponse.getEtag(request));
        directoryProperties.setLastModified(new Date(request.getLastModified()));

        return directoryProperties;
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
     */
    public static FileAttributes getFileAttributes(final HttpURLConnection request, final StorageUri resourceURI) {
        final FileAttributes fileAttributes = new FileAttributes();
        final FileProperties properties = fileAttributes.getProperties();

        properties.setCacheControl(request.getHeaderField(Constants.HeaderConstants.CACHE_CONTROL));
        properties.setContentDisposition(request.getHeaderField(Constants.HeaderConstants.CONTENT_DISPOSITION));
        properties.setContentEncoding(request.getHeaderField(Constants.HeaderConstants.CONTENT_ENCODING));
        properties.setContentLanguage(request.getHeaderField(Constants.HeaderConstants.CONTENT_LANGUAGE));
        properties.setContentMD5(request.getHeaderField(Constants.HeaderConstants.CONTENT_MD5));
        properties.setContentType(request.getHeaderField(Constants.HeaderConstants.CONTENT_TYPE));
        properties.setEtag(BaseResponse.getEtag(request));

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
}