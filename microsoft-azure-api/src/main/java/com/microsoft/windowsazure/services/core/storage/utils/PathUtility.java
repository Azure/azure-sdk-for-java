package com.microsoft.windowsazure.services.core.storage.utils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map.Entry;

import com.microsoft.windowsazure.services.core.storage.Constants;
import com.microsoft.windowsazure.services.core.storage.StorageCredentials;
import com.microsoft.windowsazure.services.core.storage.StorageException;

/**
 * RESERVED FOR INTERNAL USE. A class to help modify paths
 * 
 * Copyright (c)2011 Microsoft. All rights reserved.
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
    public static URI addToQuery(final URI resourceURI, final HashMap<String, String[]> fieldCollection)
            throws URISyntaxException, StorageException {
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
    public static URI addToQuery(final URI resourceURI, final String queryString)
            throws URISyntaxException, StorageException {
        return addToQuery(resourceURI, parseQueryString(queryString));
    }

    /**
     * Appends a path to a Uri correctly using "/" as separator.
     * 
     * @param uri
     *            The base Uri.
     * @param relativeOrAbslouteUri
     *            The relative or absloute URI.
     * @return The appended Uri.
     * @throws URISyntaxException
     */
    public static URI appendPathToUri(final URI uri, final String relativeOrAbslouteUri) throws URISyntaxException {
        return appendPathToUri(uri, relativeOrAbslouteUri, "/");
    }

    /**
     * Appends a path to a Uri correctly using the given separator.
     * 
     * @param uri
     *            The base Uri.
     * @param relativeOrAbsoluteUri
     *            The relative or absloute URI.
     * @param separator
     *            the separator to use.
     * @return The appended Uri.
     * @throws URISyntaxException
     */
    public static URI appendPathToUri(final URI uri, final String relativeOrAbsoluteUri, final String separator)
            throws URISyntaxException {

        int hostNameBeginIndex = -1;
        if (relativeOrAbsoluteUri.length() > 8) {
            final String header = relativeOrAbsoluteUri.substring(0, 8).toLowerCase();
            if ("https://".equals(header)) {
                hostNameBeginIndex = 8;
            } else if ("http://".equals(header.substring(0, 7))) {
                hostNameBeginIndex = 7;
            }
        }

        // absolute URI
        if (hostNameBeginIndex > 0) {
            final int authorityLength = relativeOrAbsoluteUri.substring(hostNameBeginIndex).indexOf(separator);
            final String authorityName =
                    relativeOrAbsoluteUri.substring(hostNameBeginIndex, hostNameBeginIndex + authorityLength);
            final URI absoluteUri = new URI(relativeOrAbsoluteUri);

            if (uri.getAuthority().equals(authorityName)) {
                return absoluteUri;
            } else {
                // Happens when using fiddler, DNS aliases, or potentially NATs
                return new URI(uri.getScheme(), uri.getAuthority(), absoluteUri.getPath(), absoluteUri.getRawQuery(),
                        absoluteUri.getRawFragment());
            }
        } else {
            // relative URI
            // used by directory
            if (uri.getPath().length() == 0 && relativeOrAbsoluteUri.startsWith(separator)) {
                return new URI(uri.getScheme(), uri.getAuthority(), relativeOrAbsoluteUri, uri.getRawQuery(),
                        uri.getRawFragment());
            }

            final StringBuilder pathString = new StringBuilder(uri.getPath());

            if (uri.getPath().endsWith(separator)) {
                pathString.append(relativeOrAbsoluteUri);
            } else {
                pathString.append(separator);
                pathString.append(relativeOrAbsoluteUri);
            }

            return new URI(uri.getScheme(), uri.getAuthority(), pathString.toString(), uri.getQuery(),
                    uri.getFragment());
        }
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
        return Utility.safeRelativize(new URI(getContainerURI(inURI, usePathStyleUris).toString().concat("/")), inURI);
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
    public static
            String getCanonicalPathFromCredentials(final StorageCredentials credentials, final String absolutePath) {
        final String account = credentials.getAccountName();

        if (account == null) {
            final String errorMessage =
                    "Cannot create Shared Access Signature as the credentials does not have account name information. Please check that the credentials used support creating Shared Access Signature.";
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
        return getContainerOrQueueNameFromUri(resourceAddress,
                usePathStyleUris,
                String.format("Invalid blob address '%s', missing container information", resourceAddress));
    }

    /**
     * Get the container or queue name from address from the URI.
     * 
     * @param resourceAddress
     *            The queue Uri.
     * @param usePathStyleUris
     *            a value indicating if the address is a path style uri.
     * @return container name from address from the URI.
     * @throws IllegalArgumentException
     */
    private static String getContainerOrQueueNameFromUri(
            final URI resourceAddress, final boolean usePathStyleUris, final String error) {
        Utility.assertNotNull("resourceAddress", resourceAddress);

        final String[] pathSegments = resourceAddress.getRawPath().split("/");

        final int expectedPartsLength = usePathStyleUris ? 3 : 2;

        if (pathSegments.length < expectedPartsLength) {
            throw new IllegalArgumentException(error);
        }

        final String containerOrQueueName = usePathStyleUris ? pathSegments[2] : pathSegments[1];

        return Utility.trimEnd(containerOrQueueName, '/');
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
    public static URI getContainerURI(final URI blobAddress, final boolean usePathStyleUris) throws URISyntaxException {
        final String containerName = getContainerNameFromUri(blobAddress, usePathStyleUris);

        final URI containerUri =
                appendPathToUri(new URI(getServiceClientBaseAddress(blobAddress, usePathStyleUris)), containerName);
        return containerUri;
    }

    /**
     * Retrieves the parent address for a blob Uri.
     * 
     * @param blobAddress
     *            The blob address
     * @param delimiter
     *            The delimiter.
     * @param usePathStyleUris
     *            a value indicating if the address is a path style uri.
     * @return The address of the parent.
     * @throws URISyntaxException
     * @throws StorageException
     */
    public static URI getParentAddress(final URI blobAddress, final String delimiter, final boolean usePathStyleUris)
            throws URISyntaxException, StorageException {
        final String parentName = getParentNameFromURI(blobAddress, delimiter, usePathStyleUris);

        final URI parentUri =
                appendPathToUri(new URI(getServiceClientBaseAddress(blobAddress, usePathStyleUris)), parentName);
        return parentUri;
    }

    /**
     * Retrieves the parent name for a blob Uri.
     * 
     * @param resourceAddress
     *            The resource Uri.
     * @param delimiter
     *            the directory delimiter to use
     * @param usePathStyleUris
     *            a value indicating if the address is a path style uri.
     * @return the parent address for a blob Uri.
     * @throws URISyntaxException
     * @throws StorageException
     */
    public static String getParentNameFromURI(
            final URI resourceAddress, final String delimiter, final boolean usePathStyleUris)
            throws URISyntaxException, StorageException {
        Utility.assertNotNull("resourceAddress", resourceAddress);
        Utility.assertNotNullOrEmpty("delimiter", delimiter);

        final String containerName = getContainerNameFromUri(resourceAddress, usePathStyleUris);

        /*
         * URI baseURI = appendPathToUri( new URI(getServiceClientBaseAddress(resourceAddress, usePathStyleUris)),
         * containerName);
         */
        URI baseURI = new URI(getServiceClientBaseAddress(resourceAddress, usePathStyleUris));
        if (usePathStyleUris && !baseURI.getRawPath().endsWith("/")) {
            baseURI =
                    new URI(baseURI.getScheme(), baseURI.getRawAuthority(), baseURI.getRawPath().concat("/"), null,
                            null);
        }

        final URI relativeURI = new URI(Utility.safeRelativize(baseURI, resourceAddress));

        String relativeURIString = relativeURI.toString();

        if (relativeURIString.endsWith(delimiter)) {
            relativeURIString = relativeURIString.substring(0, relativeURIString.length() - delimiter.length());
        }

        String parentName = Constants.EMPTY_STRING;

        if (Utility.isNullOrEmpty(relativeURIString)) {
            // Case 1 /<ContainerName>[Delimiter]*? => /<ContainerName>
            // Parent of container is container itself
            parentName = containerName.concat(delimiter);
        } else {
            final int lastDelimiterDex = relativeURIString.lastIndexOf(delimiter);

            if (lastDelimiterDex <= 0) {
                // Case 2 /<Container>/<folder>
                // Parent of a folder is container
                parentName = containerName.concat(delimiter);
            } else {
                // Case 3 /<Container>/<folder>/[<subfolder>/]*<BlobName>
                // Parent of blob is folder
                parentName = relativeURIString.substring(0, lastDelimiterDex + delimiter.length());
            }
        }

        return parentName;
    }

    /**
     * Get the queue name from address from the URI.
     * 
     * @param resourceAddress
     *            The queue Uri.
     * @param usePathStyleUris
     *            a value indicating if the address is a path style uri.
     * @return container name from address from the URI.
     * @throws IllegalArgumentException
     */
    public static String getQueueNameFromUri(final URI resourceAddress, final boolean usePathStyleUris) {
        return getContainerOrQueueNameFromUri(resourceAddress,
                usePathStyleUris,
                String.format("Invalid queue URI '%s'.", resourceAddress));
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
        if (usePathStyleUris) {
            final String[] pathSegments = address.getRawPath().split("/");

            if (pathSegments.length < 2) {
                final String error =
                        String.format("Missing account name information inside path style uri. Path style uris should be of the form http://<IPAddressPlusPort>/<accountName>");
                throw new IllegalArgumentException(error);
            }

            final StringBuilder completeAddress =
                    new StringBuilder(new URI(address.getScheme(), address.getAuthority(), null, null, null).toString());
            completeAddress.append("/");
            completeAddress.append(Utility.trimEnd(pathSegments[1], '/'));

            return completeAddress.toString();
        } else {
            return new URI(address.getScheme(), address.getAuthority(), null, null, null).toString();
        }
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
                // TODO should throw here?
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
            } else if (!value.equals(Constants.EMPTY_STRING)) {
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
    public static URI stripURIQueryAndFragment(final URI inUri) throws StorageException {
        try {
            return new URI(inUri.getScheme(), inUri.getAuthority(), inUri.getPath(), null, null);
        } catch (final URISyntaxException e) {
            throw Utility.generateNewUnexpectedStorageException(e);
        }
    }

    /**
     * Private Default Ctor.
     */
    private PathUtility() {
        // No op
    }
}
