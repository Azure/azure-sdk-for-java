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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;
import java.util.concurrent.TimeoutException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.xml.sax.SAXException;

import com.microsoft.azure.storage.Constants;
import com.microsoft.azure.storage.OperationContext;
import com.microsoft.azure.storage.RequestOptions;
import com.microsoft.azure.storage.ResultContinuation;
import com.microsoft.azure.storage.ResultContinuationType;
import com.microsoft.azure.storage.StorageErrorCode;
import com.microsoft.azure.storage.StorageErrorCodeStrings;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.StorageExtendedErrorInformation;

/**
 * RESERVED FOR INTERNAL USE. A class which provides utility methods.
 */
public final class Utility {
    /**
     * Stores a reference to the GMT time zone.
     */
    public static final TimeZone GMT_ZONE = TimeZone.getTimeZone("GMT");

    /**
     * Stores a reference to the UTC time zone.
     */
    public static final TimeZone UTC_ZONE = TimeZone.getTimeZone("UTC");

    /**
     * Stores a reference to the US locale.
     */
    public static final Locale LOCALE_US = Locale.US;

    /**
     * Stores a reference to the RFC1123 date/time pattern.
     */
    private static final String RFC1123_PATTERN = "EEE, dd MMM yyyy HH:mm:ss z";

    /**
     * Stores a reference to the ISO8601 date/time pattern.
     */
    private static final String ISO8601_PATTERN_NO_SECONDS = "yyyy-MM-dd'T'HH:mm'Z'";

    /**
     * Stores a reference to the ISO8601 date/time pattern.
     */
    private static final String ISO8601_PATTERN = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    /**
     * Stores a reference to the Java version of ISO8601_LONG date/time pattern.  The full version cannot be used
     * because Java Dates have millisecond precision.
     */
    private static final String JAVA_ISO8601_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    
    /**
     * List of ports used for path style addressing.
     */
    private static final List<Integer> pathStylePorts = Arrays.asList(10000, 10001, 10002, 10003, 10004, 10100, 10101,
            10102, 10103, 10104, 11000, 11001, 11002, 11003, 11004, 11100, 11101, 11102, 11103, 11104);

    /**
     * A factory to create SAXParser instances.
     */
    private static final SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();

    /**
     * A factory to create XMLStreamWriter instances.
     */
    private static final XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();

    /**
     * Stores a reference to the date/time pattern with the greatest precision Java.util.Date is capable of expressing.
     */
    private static final String MAX_PRECISION_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS";
    
    /**
     * The length of a datestring that matches the MAX_PRECISION_PATTERN.
     */
    private static final int MAX_PRECISION_DATESTRING_LENGTH = MAX_PRECISION_PATTERN.replaceAll("'", "").length();
    
    /**
     * 
     * Determines the size of an input stream, and optionally calculates the MD5 hash for the stream.
     * 
     * @param sourceStream
     *            A <code>InputStream</code> object that represents the stream to measure.
     * @param writeLength
     *            The number of bytes to read from the stream.
     * @param abandonLength
     *            The number of bytes to read before the analysis is abandoned. Set this value to <code>-1</code> to
     *            force the entire stream to be read. This parameter is provided to support upload thresholds.
     * @param rewindSourceStream
     *            <code>true</code> if the stream should be rewound after it is read; otherwise, <code>false</code>.
     * @param calculateMD5
     *            <code>true</code> if an MD5 hash will be calculated; otherwise, <code>false</code>.
     * 
     * @return A {@link StreamMd5AndLength} object that contains the stream length, and optionally the MD5 hash.
     * 
     * @throws IOException
     *             If an I/O error occurs.
     * @throws StorageException
     *             If a storage service error occurred.
     */
    public static StreamMd5AndLength analyzeStream(final InputStream sourceStream, long writeLength,
            long abandonLength, final boolean rewindSourceStream, final boolean calculateMD5) throws IOException,
            StorageException {
        if (abandonLength < 0) {
            abandonLength = Long.MAX_VALUE;
        }

        if (rewindSourceStream) {
            if (!sourceStream.markSupported()) {
                throw new IllegalArgumentException(SR.INPUT_STREAM_SHOULD_BE_MARKABLE);
            }

            sourceStream.mark(Constants.MAX_MARK_LENGTH);
        }

        MessageDigest digest = null;
        if (calculateMD5) {
            try {
                digest = MessageDigest.getInstance("MD5");
            }
            catch (final NoSuchAlgorithmException e) {
                // This wont happen, throw fatal.
                throw Utility.generateNewUnexpectedStorageException(e);
            }
        }

        if (writeLength < 0) {
            writeLength = Long.MAX_VALUE;
        }

        final StreamMd5AndLength retVal = new StreamMd5AndLength();
        int count = -1;
        final byte[] retrievedBuff = new byte[Constants.BUFFER_COPY_LENGTH];

        int nextCopy = (int) Math.min(retrievedBuff.length, writeLength - retVal.getLength());
        count = sourceStream.read(retrievedBuff, 0, nextCopy);

        while (nextCopy > 0 && count != -1) {
            if (calculateMD5) {
                digest.update(retrievedBuff, 0, count);
            }
            retVal.setLength(retVal.getLength() + count);

            if (retVal.getLength() > abandonLength) {
                // Abandon operation
                retVal.setLength(-1);
                retVal.setMd5(null);
                break;
            }

            nextCopy = (int) Math.min(retrievedBuff.length, writeLength - retVal.getLength());
            count = sourceStream.read(retrievedBuff, 0, nextCopy);
        }

        if (retVal.getLength() != -1 && calculateMD5) {
            retVal.setMd5(Base64.encode(digest.digest()));
        }

        if (retVal.getLength() != -1 && writeLength > 0) {
            retVal.setLength(Math.min(retVal.getLength(), writeLength));
        }

        if (rewindSourceStream) {
            sourceStream.reset();
            sourceStream.mark(Constants.MAX_MARK_LENGTH);
        }

        return retVal;
    }

    /**
     * Asserts a continuation token is of the specified type.
     * 
     * @param continuationToken
     *            A {@link ResultContinuation} object that represents the continuation token whose type is being
     *            examined.
     * @param continuationType
     *            A {@link ResultContinuationType} value that represents the continuation token type being asserted with
     *            the specified continuation token.
     */
    public static void assertContinuationType(final ResultContinuation continuationToken,
            final ResultContinuationType continuationType) {
        if (continuationToken != null) {
            if (!(continuationToken.getContinuationType() == ResultContinuationType.NONE || continuationToken
                    .getContinuationType() == continuationType)) {
                final String errorMessage = String.format(Utility.LOCALE_US, SR.UNEXPECTED_CONTINUATION_TYPE,
                        continuationToken.getContinuationType(), continuationType);
                throw new IllegalArgumentException(errorMessage);
            }
        }
    }

    /**
     * Asserts that a value is not <code>null</code>.
     * 
     * @param param
     *            A <code>String</code> that represents the name of the parameter, which becomes the exception message
     *            text if the <code>value</code> parameter is <code>null</code>.
     * @param value
     *            An <code>Object</code> object that represents the value of the specified parameter. This is the value
     *            being asserted as not <code>null</code>.
     */
    public static void assertNotNull(final String param, final Object value) {
        if (value == null) {
            throw new IllegalArgumentException(String.format(Utility.LOCALE_US, SR.ARGUMENT_NULL_OR_EMPTY, param));
        }
    }

    /**
     * Asserts that the specified string is not <code>null</code> or empty.
     * 
     * @param param
     *            A <code>String</code> that represents the name of the parameter, which becomes the exception message
     *            text if the <code>value</code> parameter is <code>null</code> or an empty string.
     * @param value
     *            A <code>String</code> that represents the value of the specified parameter. This is the value being
     *            asserted as not <code>null</code> and not an empty string.
     */
    public static void assertNotNullOrEmpty(final String param, final String value) {
        assertNotNull(param, value);

        if (Utility.isNullOrEmpty(value)) {
            throw new IllegalArgumentException(String.format(Utility.LOCALE_US, SR.ARGUMENT_NULL_OR_EMPTY, param));
        }
    }

    /**
     * Asserts that the specified integer is in the valid range.
     * 
     * @param param
     *            A <code>String</code> that represents the name of the parameter, which becomes the exception message
     *            text if the <code>value</code> parameter is out of bounds.
     * @param value
     *            The value of the specified parameter.
     * @param min
     *            The minimum value for the specified parameter.
     * @param max
     *            The maximum value for the specified parameter.
     */
    public static void assertInBounds(final String param, final long value, final long min, final long max) {
        if (value < min || value > max) {
            throw new IllegalArgumentException(String.format(SR.PARAMETER_NOT_IN_RANGE, param, min, max));
        }
    }

    /**
     * Asserts that the specified value is greater than or equal to the min value.
     * 
     * @param param
     *            A <code>String</code> that represents the name of the parameter, which becomes the exception message
     *            text if the <code>value</code> parameter is out of bounds.
     * @param value
     *            The value of the specified parameter.
     * @param min
     *            The minimum value for the specified parameter.
     */
    public static void assertGreaterThanOrEqual(final String param, final long value, final long min) {
        if (value < min) {
            throw new IllegalArgumentException(String.format(SR.PARAMETER_SHOULD_BE_GREATER_OR_EQUAL, param, min));
        }
    }

    /**
     * Returns a value representing whether the maximum execution time would be surpassed.
     * 
     * @param operationExpiryTimeInMs
     *            the time the request expires
     * @return <code>true</code> if the maximum execution time would be surpassed; otherwise, <code>false</code>.
     */
    public static boolean validateMaxExecutionTimeout(Long operationExpiryTimeInMs) {
        return validateMaxExecutionTimeout(operationExpiryTimeInMs, 0);
    }

    /**
     * Returns a value representing whether the maximum execution time would be surpassed.
     * 
     * @param operationExpiryTimeInMs
     *            the time the request expires
     * @param additionalInterval
     *            any additional time required from now
     * @return <code>true</code> if the maximum execution time would be surpassed; otherwise, <code>false</code>.
     */
    public static boolean validateMaxExecutionTimeout(Long operationExpiryTimeInMs, long additionalInterval) {
        if (operationExpiryTimeInMs != null) {
            long currentTime = new Date().getTime();
            return operationExpiryTimeInMs < currentTime + additionalInterval;
        }
        return false;
    }

    /**
     * Returns a value representing the remaining time before the operation expires.
     * 
     * @param operationExpiryTimeInMs
     *            the time the request expires
     * @param timeoutIntervalInMs
     *            the server side timeout interval
     * @return the remaining time before the operation expires
     * @throws StorageException
     *             wraps a TimeoutException if there is no more time remaining
     */
    public static int getRemainingTimeout(Long operationExpiryTimeInMs, Integer timeoutIntervalInMs) throws StorageException {
        if (operationExpiryTimeInMs != null) {
            long remainingTime = operationExpiryTimeInMs - new Date().getTime();
            if (remainingTime > Integer.MAX_VALUE) {
                return Integer.MAX_VALUE;
            }
            else if (remainingTime > 0) {
                return (int) remainingTime;
            }
            else {
                TimeoutException timeoutException = new TimeoutException(SR.MAXIMUM_EXECUTION_TIMEOUT_EXCEPTION);
                StorageException translatedException = new StorageException(
                        StorageErrorCodeStrings.OPERATION_TIMED_OUT, SR.MAXIMUM_EXECUTION_TIMEOUT_EXCEPTION,
                        Constants.HeaderConstants.HTTP_UNUSED_306, null, timeoutException);
                throw translatedException;
            }
        }
        else if (timeoutIntervalInMs != null) {
            return timeoutIntervalInMs + Constants.DEFAULT_READ_TIMEOUT;
        }
        else {
            return Constants.DEFAULT_READ_TIMEOUT;
        }
    }

    /**
     * Returns a value that indicates whether a specified URI is a path-style URI.
     * 
     * @param baseURI
     *            A <code>java.net.URI</code> value that represents the URI being checked.
     * @return <code>true</code> if the specified URI is path-style; otherwise, <code>false</code>.
     */
    public static boolean determinePathStyleFromUri(final URI baseURI) {
        String path = baseURI.getPath();
        if (path != null && path.startsWith("/")) {
            path = path.substring(1);
        }

        // if the path is null or empty, this is not path-style
        if (Utility.isNullOrEmpty(path)) {
            return false;
        }

        // if this contains a port or has a host which is not DNS, this is path-style
        return pathStylePorts.contains(baseURI.getPort()) || !isHostDnsName(baseURI);
    }

    /**
     * Returns a boolean indicating whether the host of the specified URI is DNS.
     * 
     * @param uri
     *            The URI whose host to evaluate.
     * @return <code>true</code> if the host is DNS; otherwise, <code>false</code>.
     */
    private static boolean isHostDnsName(URI uri) {
        String host = uri.getHost();
        for (int i = 0; i < host.length(); i++) {
            char hostChar = host.charAt(i);
            if (!Character.isDigit(hostChar) && !(hostChar == '.')) {
                return true;
            }
        }
        return false;
    }

    /**
     * Reads character data for the Etag element from an XML stream reader.
     * 
     * @param xmlr
     *            An <code>XMLStreamReader</code> object that represents the source XML stream reader.
     * 
     * @return A <code>String</code> that represents the character data for the Etag element.
     */
    public static String formatETag(final String etag) {
        if (etag.startsWith("\"") && etag.endsWith("\"")) {
            return etag;
        }
        else {
            return String.format("\"%s\"", etag);
        }
    }

    /**
     * Returns an unexpected storage exception.
     * 
     * @param cause
     *            An <code>Exception</code> object that represents the initial exception that caused the unexpected
     *            error.
     * 
     * @return A {@link StorageException} object that represents the unexpected storage exception being thrown.
     */
    public static StorageException generateNewUnexpectedStorageException(final Exception cause) {
        final StorageException exceptionRef = new StorageException(StorageErrorCode.NONE.toString(),
                "Unexpected internal storage client error.", 306, // unused
                null, null);
        exceptionRef.initCause(cause);
        return exceptionRef;
    }

    /**
     * Returns the current GMT date/time String using the RFC1123 pattern.
     * 
     * @return A <code>String</code> that represents the current GMT date/time using the RFC1123 pattern.
     */
    public static String getGMTTime() {
        return getGMTTime(new Date());
    }

    /**
     * Returns the GTM date/time String for the specified value using the RFC1123 pattern.
     * 
     * @param date
     *            A <code>Date</code> object that represents the date to convert to GMT date/time in the RFC1123
     *            pattern.
     * 
     * @return A <code>String</code> that represents the GMT date/time for the specified value using the RFC1123
     *         pattern.
     */
    public static String getGMTTime(final Date date) {
        final DateFormat formatter = new SimpleDateFormat(RFC1123_PATTERN, LOCALE_US);
        formatter.setTimeZone(GMT_ZONE);
        return formatter.format(date);
    }

    
    /**
     * Returns the UTC date/time String for the specified value using Java's version of the ISO8601 pattern,
     * which is limited to millisecond precision.
     * 
     * @param date
     *            A <code>Date</code> object that represents the date to convert to UTC date/time in Java's version
     *            of the ISO8601 pattern.
     * 
     * @return A <code>String</code> that represents the UTC date/time for the specified value using Java's version
     *            of the ISO8601 pattern.
     */
    public static String getJavaISO8601Time(Date date) {
        final DateFormat formatter = new SimpleDateFormat(JAVA_ISO8601_PATTERN, LOCALE_US);
        formatter.setTimeZone(UTC_ZONE);
        return formatter.format(date);
    }
    
    /**
     * Returns a namespace aware <code>SAXParser</code>.
     * 
     * @return A <code>SAXParser</code> instance which is namespace aware
     * 
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    public static SAXParser getSAXParser() throws ParserConfigurationException, SAXException {
        saxParserFactory.setNamespaceAware(true);
        return saxParserFactory.newSAXParser();
    }
    
    /**
     * Returns the standard header value from the specified connection request, or an empty string if no header value
     * has been specified for the request.
     * 
     * @param conn
     *            An <code>HttpURLConnection</code> object that represents the request.
     * @param headerName
     *            A <code>String</code> that represents the name of the header being requested.
     * 
     * @return A <code>String</code> that represents the header value, or <code>null</code> if there is no corresponding
     *         header value for <code>headerName</code>.
     */
    public static String getStandardHeaderValue(final HttpURLConnection conn, final String headerName) {
        final String headerValue = conn.getRequestProperty(headerName);

        // Coalesce null value
        return headerValue == null ? Constants.EMPTY_STRING : headerValue;
    }

    /**
     * Returns the UTC date/time for the specified value using the ISO8601 pattern.
     * 
     * @param value
     *            A <code>Date</code> object that represents the date to convert to UTC date/time in the ISO8601
     *            pattern. If this value is <code>null</code>, this method returns an empty string.
     * 
     * @return A <code>String</code> that represents the UTC date/time for the specified value using the ISO8601
     *         pattern, or an empty string if <code>value</code> is <code>null</code>.
     */
    public static String getUTCTimeOrEmpty(final Date value) {
        if (value == null) {
            return Constants.EMPTY_STRING;
        }

        final DateFormat iso8601Format = new SimpleDateFormat(ISO8601_PATTERN, LOCALE_US);
        iso8601Format.setTimeZone(UTC_ZONE);

        return iso8601Format.format(value);
    }

    /**
     * Returns a <code>XMLStreamWriter</code> with the specified <code>StringWriter</code>.
     * 
     * @param outWriter
     *            The <code>StringWriter</code> to use to create the <code>XMLStreamWriter</code> instance.
     * @return A <code>XMLStreamWriter</code> instance
     * 
     * @throws XMLStreamException
     */
    public static XMLStreamWriter createXMLStreamWriter(StringWriter outWriter) throws XMLStreamException {
        return xmlOutputFactory.createXMLStreamWriter(outWriter);
    }

    /**
     * Creates an instance of the <code>IOException</code> class using the specified exception.
     * 
     * @param ex
     *            An <code>Exception</code> object that represents the exception used to create the IO exception.
     * 
     * @return A <code>java.io.IOException</code> object that represents the created IO exception.
     */
    public static IOException initIOException(final Exception ex) {
        final IOException retEx = new IOException();
        retEx.initCause(ex);
        return retEx;
    }

    /**
     * Returns a value that indicates whether the specified string is <code>null</code> or empty.
     * 
     * @param value
     *            A <code>String</code> being examined for <code>null</code> or empty.
     * 
     * @return <code>true</code> if the specified value is <code>null</code> or empty; otherwise, <code>false</code>
     */
    public static boolean isNullOrEmpty(final String value) {
        return value == null || value.length() == 0;
    }

    /**
     * Returns a value that indicates whether the specified string is <code>null</code>, empty, or whitespace.
     * 
     * @param value
     *            A <code>String</code> being examined for <code>null</code>, empty, or whitespace.
     * 
     * @return <code>true</code> if the specified value is <code>null</code>, empty, or whitespace; otherwise,
     *         <code>false</code>
     */
    public static boolean isNullOrEmptyOrWhitespace(final String value) {
        return value == null || value.trim().length() == 0;
    }

    /**
     * Parses a connection string and returns its values as a hash map of key/value pairs.
     * 
     * @param parseString
     *            A <code>String</code> that represents the connection string to parse.
     * 
     * @return A <code>java.util.HashMap</code> object that represents the hash map of the key / value pairs parsed from
     *         the connection string.
     */
    public static HashMap<String, String> parseAccountString(final String parseString) {

        // 1. split name value pairs by splitting on the ';' character
        final String[] valuePairs = parseString.split(";");
        final HashMap<String, String> retVals = new HashMap<String, String>();

        // 2. for each field value pair parse into appropriate map entries
        for (int m = 0; m < valuePairs.length; m++) {
            if (valuePairs[m].length() == 0) {
                continue;
            }
            final int equalDex = valuePairs[m].indexOf("=");
            if (equalDex < 1) {
                throw new IllegalArgumentException(SR.INVALID_CONNECTION_STRING);
            }

            final String key = valuePairs[m].substring(0, equalDex);
            final String value = valuePairs[m].substring(equalDex + 1);

            // 2.1 add to map
            retVals.put(key, value);
        }

        return retVals;
    }

    /**
     * Returns a GMT date for the specified string in the RFC1123 pattern.
     * 
     * @param value
     *            A <code>String</code> that represents the string to parse.
     * 
     * @return A <code>Date</code> object that represents the GMT date in the RFC1123 pattern.
     * 
     * @throws ParseException
     *             If the specified string is invalid.
     */
    public static Date parseRFC1123DateFromStringInGMT(final String value) throws ParseException {
        final DateFormat format = new SimpleDateFormat(RFC1123_PATTERN, Utility.LOCALE_US);
        format.setTimeZone(GMT_ZONE);
        return format.parse(value);
    }

    /**
     * Performs safe decoding of the specified string, taking care to preserve each <code>+</code> character, rather
     * than replacing it with a space character.
     * 
     * @param stringToDecode
     *            A <code>String</code> that represents the string to decode.
     * 
     * @return A <code>String</code> that represents the decoded string.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    public static String safeDecode(final String stringToDecode) throws StorageException {
        if (stringToDecode == null) {
            return null;
        }

        if (stringToDecode.length() == 0) {
            return Constants.EMPTY_STRING;
        }

        try {
            if (stringToDecode.contains("+")) {
                final StringBuilder outBuilder = new StringBuilder();

                int startDex = 0;
                for (int m = 0; m < stringToDecode.length(); m++) {
                    if (stringToDecode.charAt(m) == '+') {
                        if (m > startDex) {
                            outBuilder.append(URLDecoder.decode(stringToDecode.substring(startDex, m),
                                    Constants.UTF8_CHARSET));
                        }

                        outBuilder.append("+");
                        startDex = m + 1;
                    }
                }

                if (startDex != stringToDecode.length()) {
                    outBuilder.append(URLDecoder.decode(stringToDecode.substring(startDex, stringToDecode.length()),
                            Constants.UTF8_CHARSET));
                }

                return outBuilder.toString();
            }
            else {
                return URLDecoder.decode(stringToDecode, Constants.UTF8_CHARSET);
            }
        }
        catch (final UnsupportedEncodingException e) {
            throw Utility.generateNewUnexpectedStorageException(e);
        }
    }

    /**
     * Performs safe encoding of the specified string, taking care to insert <code>%20</code> for each space character,
     * instead of inserting the <code>+</code> character.
     * 
     * @param stringToEncode
     *            A <code>String</code> that represents the string to encode.
     * 
     * @return A <code>String</code> that represents the encoded string.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    public static String safeEncode(final String stringToEncode) throws StorageException {
        if (stringToEncode == null) {
            return null;
        }
        if (stringToEncode.length() == 0) {
            return Constants.EMPTY_STRING;
        }

        try {
            final String tString = URLEncoder.encode(stringToEncode, Constants.UTF8_CHARSET);

            if (stringToEncode.contains(" ")) {
                final StringBuilder outBuilder = new StringBuilder();

                int startDex = 0;
                for (int m = 0; m < stringToEncode.length(); m++) {
                    if (stringToEncode.charAt(m) == ' ') {
                        if (m > startDex) {
                            outBuilder.append(URLEncoder.encode(stringToEncode.substring(startDex, m),
                                    Constants.UTF8_CHARSET));
                        }

                        outBuilder.append("%20");
                        startDex = m + 1;
                    }
                }

                if (startDex != stringToEncode.length()) {
                    outBuilder.append(URLEncoder.encode(stringToEncode.substring(startDex, stringToEncode.length()),
                            Constants.UTF8_CHARSET));
                }

                return outBuilder.toString();
            }
            else {
                return tString;
            }

        }
        catch (final UnsupportedEncodingException e) {
            throw Utility.generateNewUnexpectedStorageException(e);
        }
    }

    /**
     * Determines the relative difference between the two specified URIs.
     * 
     * @param baseURI
     *            A <code>java.net.URI</code> object that represents the base URI for which <code>toUri</code> will be
     *            made relative.
     * @param toUri
     *            A <code>java.net.URI</code> object that represents the URI to make relative to <code>baseURI</code>.
     * 
     * @return A <code>String</code> that either represents the relative URI of <code>toUri</code> to
     *         <code>baseURI</code>, or the URI of <code>toUri</code> itself, depending on whether the hostname and
     *         scheme are identical for <code>toUri</code> and <code>baseURI</code>. If the hostname and scheme of
     *         <code>baseURI</code> and <code>toUri</code> are identical, this method returns an unencoded relative URI
     *         such that if appended to <code>baseURI</code>, it will yield <code>toUri</code>. If the hostname or
     *         scheme of <code>baseURI</code> and <code>toUri</code> are not identical, this method returns an unencoded
     *         full URI specified by <code>toUri</code>.
     * 
     * @throws URISyntaxException
     *             If <code>baseURI</code> or <code>toUri</code> is invalid.
     */
    public static String safeRelativize(final URI baseURI, final URI toUri) throws URISyntaxException {
        // For compatibility followed
        // http://msdn.microsoft.com/en-us/library/system.uri.makerelativeuri.aspx

        // if host and scheme are not identical return from uri
        if (!baseURI.getHost().equals(toUri.getHost()) || !baseURI.getScheme().equals(toUri.getScheme())) {
            return toUri.toString();
        }

        final String basePath = baseURI.getPath();
        String toPath = toUri.getPath();

        int truncatePtr = 1;

        // Seek to first Difference
        // int maxLength = Math.min(basePath.length(), toPath.length());
        int m = 0;
        int ellipsesCount = 0;
        for (; m < basePath.length(); m++) {
            if (m >= toPath.length()) {
                if (basePath.charAt(m) == '/') {
                    ellipsesCount++;
                }
            }
            else {
                if (basePath.charAt(m) != toPath.charAt(m)) {
                    break;
                }
                else if (basePath.charAt(m) == '/') {
                    truncatePtr = m + 1;
                }
            }
        }

        // ../containername and ../containername/{path} should increment the truncatePtr
        // otherwise toPath will incorrectly begin with /containername
        if (m < toPath.length() && toPath.charAt(m) == '/') {
            // this is to handle the empty directory case with the '/' delimiter
            // for example, ../containername/ and ../containername// should not increment the truncatePtr
            if (!(toPath.charAt(m - 1) == '/' && basePath.charAt(m - 1) == '/')) {
                truncatePtr = m + 1;
            }
        }

        if (m == toPath.length()) {
            // No path difference, return query + fragment
            return new URI(null, null, null, toUri.getQuery(), toUri.getFragment()).toString();
        }
        else {
            toPath = toPath.substring(truncatePtr);
            final StringBuilder sb = new StringBuilder();
            while (ellipsesCount > 0) {
                sb.append("../");
                ellipsesCount--;
            }

            if (!Utility.isNullOrEmpty(toPath)) {
                sb.append(toPath);
            }

            if (!Utility.isNullOrEmpty(toUri.getQuery())) {
                sb.append("?");
                sb.append(toUri.getQuery());
            }
            if (!Utility.isNullOrEmpty(toUri.getFragment())) {
                sb.append("#");
                sb.append(toUri.getRawFragment());
            }

            return sb.toString();
        }
    }

    /**
     * Serializes the parsed StorageException. If an exception is encountered, returns empty string.
     * 
     * @param ex
     *            The StorageException to serialize.
     * @param opContext 
     *            The operation context which provides the logger.
     */
    public static void logHttpError(StorageException ex, OperationContext opContext) {
        if (Logger.shouldLog(opContext)) {
            try {
                StringBuilder bld = new StringBuilder();
                bld.append("Error response received. ");
    
                bld.append("HttpStatusCode= ");
                bld.append(ex.getHttpStatusCode());
                
                bld.append(", HttpStatusMessage= ");
                bld.append(ex.getMessage());
    
                bld.append(", ErrorCode= ");
                bld.append(ex.getErrorCode());
    
                StorageExtendedErrorInformation extendedError = ex.getExtendedErrorInformation();
                if (extendedError != null) {
                    bld.append(", ExtendedErrorInformation= {ErrorMessage= ");
                    bld.append(extendedError.getErrorMessage());
    
                    HashMap<String, String[]> details = extendedError.getAdditionalDetails();
                    if (details != null) {
                        bld.append(", AdditionalDetails= { ");
                        for (Entry<String, String[]> detail : details.entrySet()) {
                            bld.append(detail.getKey());
                            bld.append("= ");
    
                            for (String value : detail.getValue()) {
                                bld.append(value);
                            }
                            bld.append(",");
                        }
                        bld.setCharAt(bld.length() - 1, '}');
                    }
                    bld.append("}");
                }
    
                Logger.debug(opContext, bld.toString());
            } catch (Exception e) {
                // Do nothing
            }
        }
    }

    /**
     * Logs the HttpURLConnection request. If an exception is encountered, logs nothing.
     * 
     * @param conn
     *            The HttpURLConnection to serialize.
     * @param opContext 
     *            The operation context which provides the logger.
     */
    public static void logHttpRequest(HttpURLConnection conn, OperationContext opContext) throws IOException {
        if (Logger.shouldLog(opContext)) {
            try {
                StringBuilder bld = new StringBuilder();
    
                bld.append(conn.getRequestMethod());
                bld.append(" ");
                bld.append(conn.getURL());
                bld.append("\n");
    
                // The Authorization header will not appear due to a security feature in HttpURLConnection
                for (Map.Entry<String, List<String>> header : conn.getRequestProperties().entrySet()) {
                    if (header.getKey() != null) {
                        bld.append(header.getKey());
                        bld.append(": ");
                    }
    
                    for (int i = 0; i < header.getValue().size(); i++) {
                        bld.append(header.getValue().get(i));
                        if (i < header.getValue().size() - 1) {
                            bld.append(",");
                        }
                    }
                    bld.append('\n');
                }
    
                Logger.trace(opContext, bld.toString());
            } catch (Exception e) {
                // Do nothing
            }
        }
    }

    /**
     * Logs the HttpURLConnection response. If an exception is encountered, logs nothing.
     * 
     * @param conn
     *            The HttpURLConnection to serialize.
     * @param opContext 
     *            The operation context which provides the logger.
     */
    public static void logHttpResponse(HttpURLConnection conn, OperationContext opContext) throws IOException {
        if (Logger.shouldLog(opContext)) {
            try {
                StringBuilder bld = new StringBuilder();
    
                // This map's null key will contain the response code and message
                for (Map.Entry<String, List<String>> header : conn.getHeaderFields().entrySet()) {
                    if (header.getKey() != null) {
                        bld.append(header.getKey());
                        bld.append(": ");
                    }
    
                    for (int i = 0; i < header.getValue().size(); i++) {
                        bld.append(header.getValue().get(i));
                        if (i < header.getValue().size() - 1) {
                            bld.append(",");
                        }
                    }
                    bld.append('\n');
                }
    
                Logger.trace(opContext, bld.toString());
            } catch (Exception e) {
                // Do nothing
            }
        }
    }

    /**
     * Trims the specified character from the end of a string.
     * 
     * @param value
     *            A <code>String</code> that represents the string to trim.
     * @param trimChar
     *            The character to trim from the end of the string.
     * 
     * @return The string with the specified character trimmed from the end.
     */
    protected static String trimEnd(final String value, final char trimChar) {
        int stopDex = value.length() - 1;
        while (stopDex > 0 && value.charAt(stopDex) == trimChar) {
            stopDex--;
        }

        return stopDex == value.length() - 1 ? value : value.substring(stopDex);
    }

    /**
     * Trims whitespace from the beginning of a string.
     * 
     * @param value
     *            A <code>String</code> that represents the string to trim.
     * 
     * @return The string with whitespace trimmed from the beginning.
     */
    public static String trimStart(final String value) {
        int spaceDex = 0;
        while (spaceDex < value.length() && value.charAt(spaceDex) == ' ') {
            spaceDex++;
        }

        return value.substring(spaceDex);
    }

    /**
     * Reads data from an input stream and writes it to an output stream, calculates the length of the data written, and
     * optionally calculates the MD5 hash for the data.
     * 
     * @param sourceStream
     *            An <code>InputStream</code> object that represents the input stream to use as the source.
     * @param outStream
     *            An <code>OutputStream</code> object that represents the output stream to use as the destination.
     * @param writeLength
     *            The number of bytes to read from the stream.
     * @param rewindSourceStream
     *            <code>true</code> if the input stream should be rewound <strong>before</strong> it is read; otherwise,
     *            <code>false</code>
     * @param calculateMD5
     *            <code>true</code> if an MD5 hash will be calculated; otherwise, <code>false</code>.
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * @param options
     *            A {@link RequestOptions} object that specifies any additional options for the request. Namely, the
     *            maximum execution time.
     * @return A {@link StreamMd5AndLength} object that contains the output stream length, and optionally the MD5 hash.
     * 
     * @throws IOException
     *             If an I/O error occurs.
     * @throws StorageException
     *             If a storage service error occurred.
     */
    public static StreamMd5AndLength writeToOutputStream(final InputStream sourceStream, final OutputStream outStream,
            long writeLength, final boolean rewindSourceStream, final boolean calculateMD5, OperationContext opContext,
            final RequestOptions options) throws IOException, StorageException {
        return writeToOutputStream(sourceStream, outStream, writeLength, rewindSourceStream, calculateMD5, opContext,
                options, null /*StorageRequest*/);
    }

    /**
     * Reads data from an input stream and writes it to an output stream, calculates the length of the data written, and
     * optionally calculates the MD5 hash for the data.
     * 
     * @param sourceStream
     *            An <code>InputStream</code> object that represents the input stream to use as the source.
     * @param outStream
     *            An <code>OutputStream</code> object that represents the output stream to use as the destination.
     * @param writeLength
     *            The number of bytes to read from the stream.
     * @param rewindSourceStream
     *            <code>true</code> if the input stream should be rewound <strong>before</strong> it is read; otherwise,
     *            <code>false</code>
     * @param calculateMD5
     *            <code>true</code> if an MD5 hash will be calculated; otherwise, <code>false</code>.
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * @param options
     *            A {@link RequestOptions} object that specifies any additional options for the request. Namely, the
     *            maximum execution time.
     * @param request
     *            Used by download resume to set currentRequestByteCount on the request. Otherwise, null is always used.
     * @return A {@link StreamMd5AndLength} object that contains the output stream length, and optionally the MD5 hash.
     * 
     * @throws IOException
     *             If an I/O error occurs.
     * @throws StorageException
     *             If a storage service error occurred.
     */
    public static StreamMd5AndLength writeToOutputStream(final InputStream sourceStream, final OutputStream outStream,
            long writeLength, final boolean rewindSourceStream, final boolean calculateMD5, OperationContext opContext,
            final RequestOptions options, StorageRequest<?, ?, Integer> request) throws IOException, StorageException {
        if (rewindSourceStream && sourceStream.markSupported()) {
            sourceStream.reset();
            sourceStream.mark(Constants.MAX_MARK_LENGTH);
        }

        final StreamMd5AndLength retVal = new StreamMd5AndLength();

        if (calculateMD5) {
            try {
                retVal.setDigest(MessageDigest.getInstance("MD5"));
            }
            catch (final NoSuchAlgorithmException e) {
                // This wont happen, throw fatal.
                throw Utility.generateNewUnexpectedStorageException(e);
            }
        }

        if (writeLength < 0) {
            writeLength = Long.MAX_VALUE;
        }

        final byte[] retrievedBuff = new byte[Constants.BUFFER_COPY_LENGTH];
        int nextCopy = (int) Math.min(retrievedBuff.length, writeLength);
        int count = sourceStream.read(retrievedBuff, 0, nextCopy);

        while (nextCopy > 0 && count != -1) {

            // if maximum execution time would be exceeded
            if (Utility.validateMaxExecutionTimeout(options.getOperationExpiryTimeInMs())) {
                // throw an exception
                TimeoutException timeoutException = new TimeoutException(SR.MAXIMUM_EXECUTION_TIMEOUT_EXCEPTION);
                throw Utility.initIOException(timeoutException);
            }

            if (outStream != null) {
                outStream.write(retrievedBuff, 0, count);
            }

            if (calculateMD5) {
                retVal.getDigest().update(retrievedBuff, 0, count);
            }

            retVal.setLength(retVal.getLength() + count);
            retVal.setCurrentOperationByteCount(retVal.getCurrentOperationByteCount() + count);

            if (request != null) {
                request.setCurrentRequestByteCount(request.getCurrentRequestByteCount() + count);
            }

            nextCopy = (int) Math.min(retrievedBuff.length, writeLength - retVal.getLength());
            count = sourceStream.read(retrievedBuff, 0, nextCopy);
        }

        if (outStream != null) {
            outStream.flush();
        }

        return retVal;
    }

    /**
     * Private Default Constructor.
     */
    private Utility() {
        // No op
    }

    public static void checkNullaryCtor(Class<?> clazzType) {
        Constructor<?> ctor = null;
        try {
            ctor = clazzType.getDeclaredConstructor((Class<?>[]) null);
        }
        catch (Exception e) {
            throw new IllegalArgumentException(SR.MISSING_NULLARY_CONSTRUCTOR);
        }

        if (ctor == null) {
            throw new IllegalArgumentException(SR.MISSING_NULLARY_CONSTRUCTOR);
        }
    }

    /**
     * Given a String representing a date in a form of the ISO8601 pattern, generates a Date representing it
     * with up to millisecond precision. 
     * 
     * @param dateString
     *              the <code>String</code> to be interpreted as a <code>Date</code>
     *              
     * @return the corresponding <code>Date</code> object
     */
    public static Date parseDate(String dateString) {
        String pattern = MAX_PRECISION_PATTERN;
        switch(dateString.length()) {
            case 28: // "yyyy-MM-dd'T'HH:mm:ss.SSSSSSS'Z'"-> [2012-01-04T23:21:59.1234567Z] length = 28
            case 27: // "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'"-> [2012-01-04T23:21:59.123456Z] length = 27
            case 26: // "yyyy-MM-dd'T'HH:mm:ss.SSSSS'Z'"-> [2012-01-04T23:21:59.12345Z] length = 26
            case 25: // "yyyy-MM-dd'T'HH:mm:ss.SSSS'Z'"-> [2012-01-04T23:21:59.1234Z] length = 25
            case 24: // "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"-> [2012-01-04T23:21:59.123Z] length = 24
                dateString = dateString.substring(0, MAX_PRECISION_DATESTRING_LENGTH);
                break;
            case 23: // "yyyy-MM-dd'T'HH:mm:ss.SS'Z'"-> [2012-01-04T23:21:59.12Z] length = 23
                // SS is assumed to be milliseconds, so a trailing 0 is necessary
                dateString = dateString.replace("Z", "0");
                break;
            case 22: // "yyyy-MM-dd'T'HH:mm:ss.S'Z'"-> [2012-01-04T23:21:59.1Z] length = 22
                // S is assumed to be milliseconds, so trailing 0's are necessary
                dateString = dateString.replace("Z", "00");
                break;
            case 20: // "yyyy-MM-dd'T'HH:mm:ss'Z'"-> [2012-01-04T23:21:59Z] length = 20
                pattern = Utility.ISO8601_PATTERN;
                break;
            case 17: // "yyyy-MM-dd'T'HH:mm'Z'"-> [2012-01-04T23:21Z] length = 17
                pattern = Utility.ISO8601_PATTERN_NO_SECONDS;
                break;
            default:
                throw new IllegalArgumentException(String.format(SR.INVALID_DATE_STRING, dateString));
        }

        final DateFormat format = new SimpleDateFormat(pattern, Utility.LOCALE_US);
        format.setTimeZone(UTC_ZONE);
        try {
            return format.parse(dateString);
        }
        catch (final ParseException e) {
            throw new IllegalArgumentException(String.format(SR.INVALID_DATE_STRING, dateString), e);
        }
    }
    
    /**
     * Given a String representing a date in a form of the ISO8601 pattern, generates a Date representing it
     * with up to millisecond precision. Use {@link #parseDate(String)} instead unless
     * <code>dateBackwardCompatibility</code> is needed.
     * <p>
     * See <a href="http://go.microsoft.com/fwlink/?LinkId=523753">here</a> for more details.
     * 
     * @param dateString
     *              the <code>String</code> to be interpreted as a <code>Date</code>
     * @param dateBackwardCompatibility
     *              <code>true</code> to correct Date values that may have been written
     *              using versions of this library prior to 2.0.0; otherwise, <code>false</code>
     *              
     * @return the corresponding <code>Date</code> object
     */
    public static Date parseDate(String dateString, boolean dateBackwardCompatibility) {
        if (!dateBackwardCompatibility) {
            return parseDate(dateString);
        }

        final int beginMilliIndex = 20;     // Length of "yyyy-MM-ddTHH:mm:ss."
        final int endTenthMilliIndex = 24;  // Length of "yyyy-MM-ddTHH:mm:ss.SSSS"

        // Check whether the millisecond and tenth of a millisecond digits are all 0.
        if (dateString.length() > endTenthMilliIndex &&
                "0000".equals(dateString.substring(beginMilliIndex, endTenthMilliIndex))) {
            // Remove the millisecond and tenth of a millisecond digits.
            // Treat the final three digits (ticks) as milliseconds.
            dateString = dateString.substring(0, beginMilliIndex) + dateString.substring(endTenthMilliIndex);
        }

        return parseDate(dateString);
    }

    /**
     * Determines which location can the listing command target by looking at the
     * continuation token.
     * 
     * @param token
     *            Continuation token
     * @return
     *         Location mode
     */
    public static RequestLocationMode getListingLocationMode(ResultContinuation token) {
        if ((token != null) && token.getTargetLocation() != null) {
            switch (token.getTargetLocation()) {
                case PRIMARY:
                    return RequestLocationMode.PRIMARY_ONLY;

                case SECONDARY:
                    return RequestLocationMode.SECONDARY_ONLY;

                default:
                    throw new IllegalArgumentException(String.format(SR.ARGUMENT_OUT_OF_RANGE_ERROR, "token",
                            token.getTargetLocation()));
            }
        }

        return RequestLocationMode.PRIMARY_OR_SECONDARY;
    }
}
