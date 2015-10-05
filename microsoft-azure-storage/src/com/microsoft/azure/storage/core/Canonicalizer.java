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
/**
 * 
 */
package com.microsoft.azure.storage.core;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.microsoft.azure.storage.Constants;
import com.microsoft.azure.storage.StorageException;

/**
 * RESERVED FOR INTERNAL USE. This is a Version 2 Canonicalization strategy conforming to the PDC 2009-09-19
 * specification
 */
abstract class Canonicalizer {

    /**
     * The expected length for the canonicalized string when SharedKeyFull is used to sign requests.
     */
    private static final int ExpectedBlobQueueCanonicalizedStringLength = 300;

    /**
     * The expected length for the canonicalized string when SharedKeyFull is used to sign table requests.
     */
    private static final int ExpectedTableCanonicalizedStringLength = 200;

    /**
     * Add x-ms- prefixed headers in a fixed order.
     * 
     * @param conn
     *            the HttpURLConnection for the operation
     * @param canonicalizedString
     *            the canonicalized string to add the canonicalized headerst to.
     */
    private static void addCanonicalizedHeaders(final HttpURLConnection conn, final StringBuilder canonicalizedString) {
        // Look for header names that start with
        // HeaderNames.PrefixForStorageHeader
        // Then sort them in case-insensitive manner.

        final Map<String, List<String>> headers = conn.getRequestProperties();
        final ArrayList<String> httpStorageHeaderNameArray = new ArrayList<String>();

        for (final String key : headers.keySet()) {
            if (key.toLowerCase(Utility.LOCALE_US).startsWith(Constants.PREFIX_FOR_STORAGE_HEADER)) {
                httpStorageHeaderNameArray.add(key.toLowerCase(Utility.LOCALE_US));
            }
        }

        Collections.sort(httpStorageHeaderNameArray);

        // Now go through each header's values in the sorted order and append
        // them to the canonicalized string.
        for (final String key : httpStorageHeaderNameArray) {
            final StringBuilder canonicalizedElement = new StringBuilder(key);
            String delimiter = ":";
            final ArrayList<String> values = getHeaderValues(headers, key);

            boolean appendCanonicalizedElement = false;
            // Go through values, unfold them, and then append them to the
            // canonicalized element string.
            for (final String value : values) {
                if (!Utility.isNullOrEmpty(value)) {
                    appendCanonicalizedElement = true;
                }

                // Unfolding is simply removal of CRLF.
                final String unfoldedValue = value.replace("\r\n", Constants.EMPTY_STRING);

                // Append it to the canonicalized element string.
                canonicalizedElement.append(delimiter);
                canonicalizedElement.append(unfoldedValue);
                delimiter = ",";
            }

            // Now, add this canonicalized element to the canonicalized header
            // string.
            if (appendCanonicalizedElement) {
                appendCanonicalizedElement(canonicalizedString, canonicalizedElement.toString());
            }
        }
    }

    /**
     * Append a string to a string builder with a newline constant
     * 
     * @param builder
     *            the StringBuilder object
     * @param element
     *            the string to append.
     */
    protected static void appendCanonicalizedElement(final StringBuilder builder, final String element) {
        builder.append("\n");
        builder.append(element);
    }

    /**
     * Constructs a canonicalized string from the request's headers that will be used to construct the signature string
     * for signing a Blob or Queue service request under the Shared Key Full authentication scheme.
     * 
     * @param address
     *            the request URI
     * @param accountName
     *            the account name associated with the request
     * @param method
     *            the verb to be used for the HTTP request.
     * @param contentType
     *            the content type of the HTTP request.
     * @param contentLength
     *            the length of the content written to the outputstream in bytes, -1 if unknown
     * @param date
     *            the date/time specification for the HTTP request
     * @param conn
     *            the HttpURLConnection for the operation.
     * @return A canonicalized string.
     * @throws StorageException
     */
    protected static String canonicalizeHttpRequest(final java.net.URL address, final String accountName,
            final String method, final String contentType, final long contentLength, final String date,
            final HttpURLConnection conn) throws StorageException {

        // The first element should be the Method of the request.
        // I.e. GET, POST, PUT, or HEAD.
        final StringBuilder canonicalizedString = new StringBuilder(ExpectedBlobQueueCanonicalizedStringLength);
        canonicalizedString.append(conn.getRequestMethod());

        // The next elements are
        // If any element is missing it may be empty.
        appendCanonicalizedElement(canonicalizedString,
                Utility.getStandardHeaderValue(conn, Constants.HeaderConstants.CONTENT_ENCODING));
        appendCanonicalizedElement(canonicalizedString,
                Utility.getStandardHeaderValue(conn, Constants.HeaderConstants.CONTENT_LANGUAGE));
        appendCanonicalizedElement(canonicalizedString,
                contentLength <= 0 ? Constants.EMPTY_STRING : String.valueOf(contentLength));
        appendCanonicalizedElement(canonicalizedString,
                Utility.getStandardHeaderValue(conn, Constants.HeaderConstants.CONTENT_MD5));
        appendCanonicalizedElement(canonicalizedString, contentType != null ? contentType : Constants.EMPTY_STRING);

        final String dateString = Utility.getStandardHeaderValue(conn, Constants.HeaderConstants.DATE);
        // If x-ms-date header exists, Date should be empty string
        appendCanonicalizedElement(canonicalizedString, dateString.equals(Constants.EMPTY_STRING) ? date
                : Constants.EMPTY_STRING);

        appendCanonicalizedElement(canonicalizedString,
                Utility.getStandardHeaderValue(conn, Constants.HeaderConstants.IF_MODIFIED_SINCE));
        appendCanonicalizedElement(canonicalizedString,
                Utility.getStandardHeaderValue(conn, Constants.HeaderConstants.IF_MATCH));
        appendCanonicalizedElement(canonicalizedString,
                Utility.getStandardHeaderValue(conn, Constants.HeaderConstants.IF_NONE_MATCH));
        appendCanonicalizedElement(canonicalizedString,
                Utility.getStandardHeaderValue(conn, Constants.HeaderConstants.IF_UNMODIFIED_SINCE));
        appendCanonicalizedElement(canonicalizedString,
                Utility.getStandardHeaderValue(conn, Constants.HeaderConstants.RANGE));

        addCanonicalizedHeaders(conn, canonicalizedString);

        appendCanonicalizedElement(canonicalizedString, getCanonicalizedResource(address, accountName));

        return canonicalizedString.toString();
    }

    /**
     * Constructs a canonicalized string that will be used to construct the signature string
     * for signing a Table service request under the Shared Key authentication scheme.
     * 
     * @param address
     *            the request URI
     * @param accountName
     *            the account name associated with the request
     * @param method
     *            the verb to be used for the HTTP request.
     * @param contentType
     *            the content type of the HTTP request.
     * @param contentLength
     *            the length of the content written to the outputstream in bytes, -1 if unknown
     * @param date
     *            the date/time specification for the HTTP request
     * @param conn
     *            the HttpURLConnection for the operation.
     * @return A canonicalized string.
     * @throws StorageException
     */
    protected static String canonicalizeTableHttpRequest(final java.net.URL address, final String accountName,
            final String method, final String contentType, final long contentLength, final String date,
            final HttpURLConnection conn) throws StorageException {
        // The first element should be the Method of the request.
        // I.e. GET, POST, PUT, or HEAD.
        final StringBuilder canonicalizedString = new StringBuilder(ExpectedTableCanonicalizedStringLength);
        canonicalizedString.append(conn.getRequestMethod());

        // The second element should be the MD5 value.
        // This is optional and may be empty.
        final String httpContentMD5Value = Utility.getStandardHeaderValue(conn, Constants.HeaderConstants.CONTENT_MD5);
        appendCanonicalizedElement(canonicalizedString, httpContentMD5Value);

        // The third element should be the content type.
        appendCanonicalizedElement(canonicalizedString, contentType);

        // The fourth element should be the request date.
        // See if there's an storage date header.
        // If there's one, then don't use the date header.

        final String dateString = Utility.getStandardHeaderValue(conn, Constants.HeaderConstants.DATE);
        // If x-ms-date header exists, Date should be that value.
        appendCanonicalizedElement(canonicalizedString, dateString.equals(Constants.EMPTY_STRING) ? date : dateString);

        appendCanonicalizedElement(canonicalizedString, getCanonicalizedResourceLite(address, accountName));

        return canonicalizedString.toString();
    }

    /**
     * Gets the canonicalized resource string for a Blob or Queue service request under the Shared Key Lite
     * authentication scheme.
     * 
     * @param address
     *            the resource URI.
     * @param accountName
     *            the account name for the request.
     * @return the canonicalized resource string.
     * @throws StorageException
     */
    protected static String getCanonicalizedResource(final java.net.URL address, final String accountName)
            throws StorageException {
        // Resource path
        final StringBuilder resourcepath = new StringBuilder("/");
        resourcepath.append(accountName);

        // Note that AbsolutePath starts with a '/'.
        resourcepath.append(address.getPath());
        final StringBuilder canonicalizedResource = new StringBuilder(resourcepath.toString());

        // query parameters
        final Map<String, String[]> queryVariables = PathUtility.parseQueryString(address.getQuery());

        final Map<String, String> lowercasedKeyNameValue = new HashMap<String, String>();

        for (final Entry<String, String[]> entry : queryVariables.entrySet()) {
            // sort the value and organize it as comma separated values
            final List<String> sortedValues = Arrays.asList(entry.getValue());
            Collections.sort(sortedValues);

            final StringBuilder stringValue = new StringBuilder();

            for (final String value : sortedValues) {
                if (stringValue.length() > 0) {
                    stringValue.append(",");
                }

                stringValue.append(value);
            }

            // key turns out to be null for ?a&b&c&d
            lowercasedKeyNameValue.put((entry.getKey()) == null ? null :
                entry.getKey().toLowerCase(Utility.LOCALE_US), stringValue.toString());
        }

        final ArrayList<String> sortedKeys = new ArrayList<String>(lowercasedKeyNameValue.keySet());

        Collections.sort(sortedKeys);

        for (final String key : sortedKeys) {
            final StringBuilder queryParamString = new StringBuilder();

            queryParamString.append(key);
            queryParamString.append(":");
            queryParamString.append(lowercasedKeyNameValue.get(key));

            appendCanonicalizedElement(canonicalizedResource, queryParamString.toString());
        }

        return canonicalizedResource.toString();
    }

    /**
     * Gets the canonicalized resource string for a Blob or Queue service request under the Shared Key Lite
     * authentication scheme.
     * 
     * @param address
     *            the resource URI.
     * @param accountName
     *            the account name for the request.
     * @return the canonicalized resource string.
     * @throws StorageException
     */
    protected static String getCanonicalizedResourceLite(final java.net.URL address, final String accountName)
            throws StorageException {
        // Resource path
        final StringBuilder resourcepath = new StringBuilder("/");
        resourcepath.append(accountName);

        // Note that AbsolutePath starts with a '/'.
        resourcepath.append(address.getPath());
        final StringBuilder canonicalizedResource = new StringBuilder(resourcepath.toString());

        // query parameters
        final Map<String, String[]> queryVariables = PathUtility.parseQueryString(address.getQuery());

        final String[] compVals = queryVariables.get("comp");

        if (compVals != null) {

            final List<String> sortedValues = Arrays.asList(compVals);
            Collections.sort(sortedValues);

            canonicalizedResource.append("?comp=");

            final StringBuilder stringValue = new StringBuilder();
            for (final String value : sortedValues) {
                if (stringValue.length() > 0) {
                    stringValue.append(",");
                }
                stringValue.append(value);
            }

            canonicalizedResource.append(stringValue);
        }

        return canonicalizedResource.toString();
    }

    /**
     * Gets all the values for the given header in the one to many map, performs a trimStart() on each return value
     * 
     * @param headers
     *            a one to many map of key / values representing the header values for the connection.
     * @param headerName
     *            the name of the header to lookup
     * @return an ArrayList<String> of all trimmed values corresponding to the requested headerName. This may be empty
     *         if the header is not found.
     */
    private static ArrayList<String> getHeaderValues(final Map<String, List<String>> headers, final String headerName) {

        final ArrayList<String> arrayOfValues = new ArrayList<String>();
        List<String> values = null;

        for (final Entry<String, List<String>> entry : headers.entrySet()) {
            if (entry.getKey().toLowerCase(Utility.LOCALE_US).equals(headerName)) {
                values = entry.getValue();
                break;
            }
        }
        if (values != null) {
            for (final String value : values) {
                // canonicalization formula requires the string to be left
                // trimmed.
                arrayOfValues.add(Utility.trimStart(value));
            }
        }
        return arrayOfValues;
    }

    /**
     * Constructs a canonicalized string for signing a request.
     * 
     * @param conn
     *            the HttpURLConnection to canonicalize
     * @param accountName
     *            the account name associated with the request
     * @param contentLength
     *            the length of the content written to the outputstream in bytes, -1 if unknown
     * @return a canonicalized string.
     */
    protected abstract String canonicalize(HttpURLConnection conn, String accountName, Long contentLength)
            throws StorageException;
}
