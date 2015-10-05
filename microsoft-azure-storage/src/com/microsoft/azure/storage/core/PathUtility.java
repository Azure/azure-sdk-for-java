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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map.Entry;

import com.microsoft.azure.storage.Constants;
import com.microsoft.azure.storage.StorageCredentials;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.StorageUri;

/**
 * RESERVED FOR INTERNAL USE. A class to help modify paths
 */
public final class PathUtility {
    /**
     * Adds a queryString to an URI.
     * 
     * @param resourceURI
     *            the URI of the resource
     * @param fieldCollection
     *            the key/ values collection to append.
     * @return an appended URI.
     * @throws URISyntaxException
     *             if the resulting URI is invalid.
     * @throws StorageException
     */
    public static URI addToSingleUriQuery(final URI resourceURI, final HashMap<String, String[]> fieldCollection)
            throws URISyntaxException, StorageException {
        if (resourceURI == null) {
            return null;
        }

        final UriQueryBuilder outUri = new UriQueryBuilder();

        // Generate new queryString
        for (final Entry<String, String[]> entry : fieldCollection.entrySet()) {
            for (final String val : entry.getValue()) {
                outUri.add(entry.getKey(), val);
            }
        }

        return outUri.addToURI(resourceURI);
    }

    /**
     * Adds a queryString to an URI.
     * 
     * @param resourceURI
     *            the URI of the resource
     * @param queryString
     *            the query string to add
     * @return an appended URI.
     * @throws URISyntaxException
     *             if the resulting URI is invalid.
     * @throws StorageException
     */
    public static StorageUri addToQuery(final StorageUri resourceURI, final String queryString)
            throws URISyntaxException, StorageException {
        return new StorageUri(addToSingleUriQuery(resourceURI.getPrimaryUri(), parseQueryString(queryString)),
                addToSingleUriQuery(resourceURI.getSecondaryUri(), parseQueryString(queryString)));
    }

    /**
     * Adds a queryString to an URI.
     * 
     * @param resourceURI
     *            the URI of the resource
     * @param queryString
     *            the query string to add
     * @return an appended URI.
     * @throws URISyntaxException
     *             if the resulting URI is invalid.
     * @throws StorageException
     */
    public static URI addToQuery(final URI resourceURI, final String queryString) throws URISyntaxException,
            StorageException {
        return addToSingleUriQuery(resourceURI, parseQueryString(queryString));
    }

    /**
     * Appends a path to a list of URIs correctly using "/" as separator.
     * 
     * @param uriList
     *            The base Uri.
     * @param relativeOrAbslouteUri
     *            The relative or absloute URI.
     * @return The appended Uri.
     * @throws URISyntaxException
     */
    public static StorageUri appendPathToUri(final StorageUri uriList, final String relativeOrAbsoluteUri)
            throws URISyntaxException {
        return appendPathToUri(uriList, relativeOrAbsoluteUri, "/");
    }

    /**
     * Appends a path to a list of URIs correctly using "/" as separator.
     * 
     * @param uriList
     *            The base Uri.
     * @param relativeOrAbslouteUri
     *            The relative or absloute URI.
     * @return The appended Uri.
     * @throws URISyntaxException
     */
    public static StorageUri appendPathToUri(final StorageUri uriList, final String relativeOrAbsoluteUri,
            final String separator) throws URISyntaxException {
        return new StorageUri(appendPathToSingleUri(uriList.getPrimaryUri(), relativeOrAbsoluteUri, separator),
                appendPathToSingleUri(uriList.getSecondaryUri(), relativeOrAbsoluteUri, separator));
    }

    /**
     * Appends a path to a URI correctly using "/" as separator.
     * 
     * @param uriList
     *            The base Uri.
     * @param relativeOrAbslouteUri
     *            The relative or absloute URI.
     * @return The appended Uri.
     * @throws URISyntaxException
     */
    public static URI appendPathToSingleUri(final URI uri, final String relativeOrAbsoluteUri)
            throws URISyntaxException {
        return appendPathToSingleUri(uri, relativeOrAbsoluteUri, "/");
    }

    /**
     * Appends a path to a URI correctly using the given separator.
     * 
     * @param uri
     *            The base Uri.
     * @param relativeUri
     *            The relative URI.
     * @param separator
     *            the separator to use.
     * @return The appended Uri.
     * @throws URISyntaxException
     *             a valid Uri cannot be constructed
     */
    public static URI appendPathToSingleUri(final URI uri, final String relativeUri, final String separator)
            throws URISyntaxException {

        if (uri == null) {
            return null;
        }

        if (relativeUri == null || relativeUri.isEmpty()) {
            return uri;
        }

        if (uri.getPath().length() == 0 && relativeUri.startsWith(separator)) {
            return new URI(uri.getScheme(), uri.getAuthority(), relativeUri, uri.getRawQuery(), uri.getRawFragment());
        }

        final StringBuilder pathString = new StringBuilder(uri.getPath());
        if (uri.getPath().endsWith(separator)) {
            pathString.append(relativeUri);
        }
        else {
            pathString.append(separator);
            pathString.append(relativeUri);
        }

        return new URI(uri.getScheme(), uri.getAuthority(), pathString.toString(), uri.getQuery(), uri.getFragment());
    }

    /**
     * Gets the blob name from the URI.
     * 
     * @param inURI
     *            the resource address
     * @param usePathStyleUris
     *            a value indicating if the address is a path style uri.
     * @return the blobs name
     * @throws URISyntaxException
     */
    public static String getBlobNameFromURI(final URI inURI, final boolean usePathStyleUris) throws URISyntaxException {
        return Utility.safeRelativize(new URI(getContainerURI(new StorageUri(inURI), usePathStyleUris).getPrimaryUri()
                .toString().concat("/")), inURI);
    }

    /**
     * Gets the canonical path for an object from the credentials.
     * 
     * @param credentials
     *            the credentials to use.
     * @param absolutePath
     *            the Absolute path of the object.
     * @return the canonical path for an object from the credentials
     */
    public static String getCanonicalPathFromCredentials(final StorageCredentials credentials, final String absolutePath) {
        final String account = credentials.getAccountName();

        if (account == null) {
            final String errorMessage = SR.CANNOT_CREATE_SAS_FOR_GIVEN_CREDENTIALS;
            throw new IllegalArgumentException(errorMessage);
        }
        final StringBuilder builder = new StringBuilder("/");
        builder.append(account);
        builder.append(absolutePath);
        return builder.toString();
    }

    /**
     * Get the container name from address from the URI.
     * 
     * @param resourceAddress
     *            The container Uri.
     * @param usePathStyleUris
     *            a value indicating if the address is a path style uri.
     * @return container name from address from the URI.
     * @throws IllegalArgumentException
     */
    public static String getContainerNameFromUri(final URI resourceAddress, final boolean usePathStyleUris) {
        return getResourceNameFromUri(resourceAddress, usePathStyleUris,
                String.format("Invalid blob address '%s', missing container information", resourceAddress));
    }

    /**
     * Gets the file name from the URI.
     * 
     * @param resourceAddress
     *            the file URI
     * @param usePathStyleUris
     *            a value indicating if the address is a path style URI
     * @return the file's name
     */
    public static String getFileNameFromURI(final URI resourceAddress, final boolean usePathStyleUris) {
        // generate an array of the different levels of the path
        final String[] pathSegments = resourceAddress.getRawPath().split("/");

        // usePathStyleUris ? baseuri/accountname/sharename/objectname : accountname.baseuri/sharename/objectname
        final int shareIndex = usePathStyleUris ? 2 : 1;

        if (pathSegments.length - 1 <= shareIndex) {
            // legal file addresses cannot end with or before the sharename
            throw new IllegalArgumentException(String.format("Invalid file address '%s'.", resourceAddress));
        }
        else {
            // in a legal file address the lowest level is the filename
            return pathSegments[pathSegments.length - 1];
        }
    }

    /**
     * Get the name of the lowest level directory from the given directory address.
     * 
     * @param resourceAddress
     *            the directory URI
     * @param usePathStyleUris
     *            a value indicating if the address is a path style URI
     * @return directory name from address from the URI
     */
    public static String getDirectoryNameFromURI(final URI resourceAddress, final boolean usePathStyleUris) {
        // generate an array of the different levels of the path
        final String[] pathSegments = resourceAddress.getRawPath().split("/");

        // usePathStyleUris ? baseuri/accountname/sharename/objectname : accountname.baseuri/sharename/objectname
        final int shareIndex = usePathStyleUris ? 2 : 1;

        if (pathSegments.length - 1 < shareIndex) {
            // if the sharename is missing or too close to the end 
            throw new IllegalArgumentException(String.format("Invalid directory address '%s'.", resourceAddress));
        }
        else if (pathSegments.length - 1 == shareIndex) {
            // this is the root directory; it has no name
            return "";
        }
        else {
            // in a legal directory address the lowest level is the directory
            return pathSegments[pathSegments.length - 1];
        }
    }
    
    /**
     * Get the share name from address from the URI.
     * 
     * @param resourceAddress
     *            The share Uri.
     * @param usePathStyleUris
     *            a value indicating if the address is a path style uri.
     * @return share name from address from the URI.
     * @throws IllegalArgumentException
     */
    public static String getShareNameFromUri(final URI resourceAddress, final boolean usePathStyleUris) {
        return getResourceNameFromUri(resourceAddress, usePathStyleUris,
                String.format("Invalid file address '%s', missing share information", resourceAddress));
    }

    /**
     * Get the table name from address from the URI.
     * 
     * @param resourceAddress
     *            The table Uri.
     * @param usePathStyleUris
     *            a value indicating if the address is a path style uri.
     * @return table name from address from the URI.
     * @throws IllegalArgumentException
     */
    public static String getTableNameFromUri(final URI resourceAddress, final boolean usePathStyleUris) {
        return getResourceNameFromUri(resourceAddress, usePathStyleUris,
                String.format("Invalid table address '%s', missing table information", resourceAddress));
    }

    /**
     * Get the container, queue or table name from address from the URI.
     * 
     * @param resourceAddress
     *            The queue Uri.
     * @param usePathStyleUris
     *            a value indicating if the address is a path style uri.
     * @return container name from address from the URI.
     * @throws IllegalArgumentException
     */
    private static String getResourceNameFromUri(final URI resourceAddress, final boolean usePathStyleUris,
            final String error) {
        Utility.assertNotNull("resourceAddress", resourceAddress);

        final String[] pathSegments = resourceAddress.getRawPath().split("/");

        final int expectedPartsLength = usePathStyleUris ? 3 : 2;

        if (pathSegments.length < expectedPartsLength) {
            throw new IllegalArgumentException(error);
        }

        final String resourceName = usePathStyleUris ? pathSegments[2] : pathSegments[1];

        return Utility.trimEnd(resourceName, '/');
    }

    /**
     * Gets the container URI from a blob address
     * 
     * @param blobAddress
     *            the blob address
     * @param usePathStyleUris
     *            a value indicating if the address is a path style uri.
     * @return the container URI from a blob address
     * @throws URISyntaxException
     */
    public static StorageUri getContainerURI(final StorageUri blobAddress, final boolean usePathStyleUris)
            throws URISyntaxException {
        final String containerName = getContainerNameFromUri(blobAddress.getPrimaryUri(), usePathStyleUris);

        final StorageUri containerUri = appendPathToUri(getServiceClientBaseAddress(blobAddress, usePathStyleUris),
                containerName);
        return containerUri;
    }

    /**
     * Gets the share URI from a file address
     * 
     * @param fileAddress
     *            the file address
     * @param usePathStyleUris
     *            a value indicating if the address is a path style uri.
     * @return the share URI from a file address
     * @throws URISyntaxException
     */
    public static StorageUri getShareURI(final StorageUri fileAddress, final boolean usePathStyleUris)
            throws URISyntaxException {
        final String shareName = getShareNameFromUri(fileAddress.getPrimaryUri(), usePathStyleUris);

        final StorageUri shareUri = appendPathToUri(getServiceClientBaseAddress(fileAddress, usePathStyleUris),
                shareName);
        return shareUri;
    }

    /**
     * Get the service client address from a complete Uri.
     * 
     * @param address
     *            Complete address of the resource.
     * @param usePathStyleUris
     *            a value indicating if the address is a path style uri.
     * @return the service client address from a complete Uri.
     * @throws URISyntaxException
     */
    public static String getServiceClientBaseAddress(final URI address, final boolean usePathStyleUris)
            throws URISyntaxException {
        if (address == null) {
            return null;
        }

        if (usePathStyleUris) {
            final String[] pathSegments = address.getRawPath().split("/");

            if (pathSegments.length < 2) {
                final String error = String.format(SR.PATH_STYLE_URI_MISSING_ACCOUNT_INFORMATION);
                throw new IllegalArgumentException(error);
            }

            final StringBuilder completeAddress = new StringBuilder(new URI(address.getScheme(),
                    address.getAuthority(), null, null, null).toString());
            completeAddress.append("/");
            completeAddress.append(Utility.trimEnd(pathSegments[1], '/'));

            return completeAddress.toString();
        }
        else {
            return new URI(address.getScheme(), address.getAuthority(), null, null, null).toString();
        }
    }

    /**
     * Get the service client address from a complete Uri.
     * 
     * @param address
     *            Complete address of the resource.
     * @param usePathStyleUris
     *            a value indicating if the address is a path style uri.
     * @return the service client address from a complete Uri.
     * @throws URISyntaxException
     */
    public static StorageUri getServiceClientBaseAddress(final StorageUri addressUri, final boolean usePathStyleUris)
            throws URISyntaxException {
        return new StorageUri(new URI(getServiceClientBaseAddress(addressUri.getPrimaryUri(), usePathStyleUris)),
                addressUri.getSecondaryUri() != null ?
                new URI(getServiceClientBaseAddress(addressUri.getSecondaryUri(), usePathStyleUris)) : null);
    }

    /**
     * Parses a query string into a one to many hashmap.
     * 
     * @param parseString
     *            the string to parse
     * @return a HashMap<String, String[]> of the key values.
     * @throws StorageException
     */
    public static HashMap<String, String[]> parseQueryString(String parseString) throws StorageException {
        final HashMap<String, String[]> retVals = new HashMap<String, String[]>();
        if (Utility.isNullOrEmpty(parseString)) {
            return retVals;
        }

        // 1. Remove ? if present
        final int queryDex = parseString.indexOf("?");
        if (queryDex >= 0 && parseString.length() > 0) {
            parseString = parseString.substring(queryDex + 1);
        }

        // 2. split name value pairs by splitting on the 'c&' character
        final String[] valuePairs = parseString.contains("&") ? parseString.split("&") : parseString.split(";");

        // 3. for each field value pair parse into appropriate map entries
        for (int m = 0; m < valuePairs.length; m++) {
            final int equalDex = valuePairs[m].indexOf("=");

            if (equalDex < 0 || equalDex == valuePairs[m].length() - 1) {
                continue;
            }

            String key = valuePairs[m].substring(0, equalDex);
            String value = valuePairs[m].substring(equalDex + 1);

            key = Utility.safeDecode(key);
            value = Utility.safeDecode(value);

            // 3.1 add to map
            String[] values = retVals.get(key);

            if (values == null) {
                values = new String[] { value };
                if (!value.equals(Constants.EMPTY_STRING)) {
                    retVals.put(key, values);
                }
            }
            else if (!value.equals(Constants.EMPTY_STRING)) {
                final String[] newValues = new String[values.length + 1];
                for (int j = 0; j < values.length; j++) {
                    newValues[j] = values[j];
                }

                newValues[newValues.length] = value;
            }
        }

        return retVals;
    }

    /**
     * Strips the Query and Fragment from the uri.
     * 
     * @param inUri
     *            the uri to alter
     * @return the stripped uri.
     * @throws StorageException
     */
    public static URI stripSingleURIQueryAndFragment(final URI inUri) throws StorageException {
        if (inUri == null) {
            return null;
        }
        try {
            return new URI(inUri.getScheme(), inUri.getAuthority(), inUri.getPath(), null, null);
        }
        catch (final URISyntaxException e) {
            throw Utility.generateNewUnexpectedStorageException(e);
        }
    }

    /**
     * Strips the Query and Fragment from the uri.
     * 
     * @param inUri
     *            the uri to alter
     * @return the stripped uri.
     * @throws StorageException
     */
    public static StorageUri stripURIQueryAndFragment(final StorageUri inUri) throws StorageException {
        return new StorageUri(stripSingleURIQueryAndFragment(inUri.getPrimaryUri()),
                stripSingleURIQueryAndFragment(inUri.getSecondaryUri()));
    }

    /**
     * Private Default Ctor.
     */
    private PathUtility() {
        // No op
    }
}
