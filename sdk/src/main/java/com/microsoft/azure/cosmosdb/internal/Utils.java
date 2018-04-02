/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.microsoft.azure.cosmosdb.internal;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.uuid.EthernetAddress;
import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.TimeBasedGenerator;
import com.microsoft.azure.cosmosdb.ConsistencyLevel;
import com.microsoft.azure.cosmosdb.DocumentCollection;

/**
 * Used internally to provides utility functions for the Azure Cosmos DB database service Java SDK.
 */
public final class Utils {
    private static final ZoneId GMT_ZONE_ID = ZoneId.of("GMT");
    public static final Base64.Encoder Base64Encoder = java.util.Base64.getEncoder();
    public static final Base64.Decoder Base64Decoder = java.util.Base64.getDecoder();

    private static final ObjectMapper simpleObjectMapper = new ObjectMapper();
    private static final TimeBasedGenerator TimeUUIDGegerator = 
            Generators.timeBasedGenerator(EthernetAddress.constructMulticastAddress());

    // NOTE DateTimeFormatter.RFC_1123_DATE_TIME cannot be used.
    // because cosmos db rfc1123 validation requires two digits for day.
    // so Thu, 04 Jan 2018 00:30:37 GMT is accepted by the cosmos db service,
    // but Thu, 4 Jan 2018 00:30:37 GMT is not.
    // Therefore, we need a custom date time formatter.
    private static final DateTimeFormatter RFC_1123_DATE_TIME = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss zzz");

    static {
        Utils.simpleObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static String encodeBase64String(byte[] binaryData) {
        String encodedString = Base64Encoder.encodeToString(binaryData);

        if (encodedString.endsWith("\r\n")) {
            encodedString = encodedString.substring(0, encodedString.length() - 2);
        }
        return encodedString;
    }

    /**
     * Checks whether the specified link is Name based or not
     *
     * @param link the link to analyze.
     * @return true or false
     */
    public static boolean isNameBased(String link) {
        if (StringUtils.isEmpty(link)) {
            return false;
        }

        // trimming the leading "/"
        if (link.startsWith("/") && link.length() > 1) {
            link = link.substring(1);
        }

        // Splitting the link(separated by "/") into parts
        String[] parts = link.split("/");

        // First part should be "dbs"
        if (parts.length == 0 || StringUtils.isEmpty(parts[0])
                || !parts[0].equalsIgnoreCase(Paths.DATABASES_PATH_SEGMENT)) {
            return false;
        }

        // The second part is the database id(ResourceID or Name) and cannot be
        // empty
        if (parts.length < 2 || StringUtils.isEmpty(parts[1])) {
            return false;
        }

        // Either ResourceID or database name
        String databaseID = parts[1];

        // Length of databaseID(in case of ResourceID) is always 8
        if (databaseID.length() != 8) {
            return true;
        }

        // Decoding the databaseID
        byte[] buffer = ResourceId.fromBase64String(databaseID);

        // Length of decoded buffer(in case of ResourceID) is always 4
        if (buffer.length != 4) {
            return true;
        }

        return false;
    }

    /**
     * Checks whether the specified link is a Database Self Link or a Database
     * ID based link
     *
     * @param link the link to analyze.
     * @return true or false
     */
    public static boolean isDatabaseLink(String link) {
        if (StringUtils.isEmpty(link)) {
            return false;
        }

        // trimming the leading and trailing "/" from the input string
        link = trimBeginingAndEndingSlashes(link);

        // Splitting the link(separated by "/") into parts
        String[] parts = link.split("/");

        if (parts.length != 2) {
            return false;
        }

        // First part should be "dbs"
        if (StringUtils.isEmpty(parts[0]) || !parts[0].equalsIgnoreCase(Paths.DATABASES_PATH_SEGMENT)) {
            return false;
        }

        // The second part is the database id(ResourceID or Name) and cannot be
        // empty
        if (StringUtils.isEmpty(parts[1])) {
            return false;
        }

        return true;
    }

    /**
     * Checks whether the specified path segment is a resource type
     *
     * @param resourcePathSegment the path segment to analyze.
     * @return true or false
     */
    public static boolean IsResourceType(String resourcePathSegment) {
        if (StringUtils.isEmpty(resourcePathSegment)) {
            return false;
        }

        switch (resourcePathSegment.toLowerCase()) {
        case Paths.ATTACHMENTS_PATH_SEGMENT:
        case Paths.COLLECTIONS_PATH_SEGMENT:
        case Paths.DATABASES_PATH_SEGMENT:
        case Paths.PERMISSIONS_PATH_SEGMENT:
        case Paths.USERS_PATH_SEGMENT:
        case Paths.DOCUMENTS_PATH_SEGMENT:
        case Paths.STORED_PROCEDURES_PATH_SEGMENT:
        case Paths.TRIGGERS_PATH_SEGMENT:
        case Paths.USER_DEFINED_FUNCTIONS_PATH_SEGMENT:
        case Paths.CONFLICTS_PATH_SEGMENT:
        case Paths.PARTITION_KEY_RANGE_PATH_SEGMENT:
            return true;

        default:
            return false;
        }
    }

    /**
     * Joins the specified paths by appropriately padding them with '/'
     *
     * @param path1 the first path segment to join.
     * @param path2 the second path segment to join.
     * @return the concatenated path with '/'
     */
    public static String joinPath(String path1, String path2) {
        path1 = trimBeginingAndEndingSlashes(path1);
        String result = "/" + path1 + "/";

        if (!StringUtils.isEmpty(path2)) {
            path2 = trimBeginingAndEndingSlashes(path2);
            result += path2 + "/";
        }

        return result;
    }

    /**
     * Trims the beginning and ending '/' from the given path
     *
     * @param path the path to trim for beginning and ending slashes
     * @return the path without beginning and ending '/'
     */
    public static String trimBeginingAndEndingSlashes(String path) {
        if(path == null) {
            return null;
        }

        if (path.startsWith("/")) {
            path = path.substring(1);
        }

        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        return path;
    }

    public static URL setQuery(String urlString, String query) {

        if (urlString == null)
            throw new IllegalStateException("urlString parameter can't be null.");
        query = Utils.removeLeadingQuestionMark(query);
        try {
            if (query != null && !query.isEmpty()) {
                return new URI(Utils.addTrailingSlash(urlString) + RuntimeConstants.Separators.Query[0] + query)
                        .toURL();
            } else {
                return new URI(Utils.addTrailingSlash(urlString)).toURL();
            }
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Uri is invalid: ", e);
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Uri is invalid: ", e);
        }
    }

    /**
     * Given the full path to a resource, extract the collection path.
     *
     * @param resourceFullName the full path to the resource.
     * @return the path of the collection in which the resource is.
     */
    public static String getCollectionName(String resourceFullName) {
        if (resourceFullName != null) {
            resourceFullName = Utils.trimBeginingAndEndingSlashes(resourceFullName);

            int slashCount = 0;
            for (int i = 0; i < resourceFullName.length(); i++) {
                if (resourceFullName.charAt(i) == '/') {
                    slashCount++;
                    if (slashCount == 4) {
                        return resourceFullName.substring(0, i);
                    }
                }
            }
        }
        return resourceFullName;
    }

    public static Boolean isCollectionPartitioned(DocumentCollection collection) {
        if (collection == null) {
            throw new IllegalArgumentException("collection");
        }

        return collection.getPartitionKey() != null
                && collection.getPartitionKey().getPaths() != null
                && collection.getPartitionKey().getPaths().size() > 0;
    }

    public static boolean isCollectionChild(ResourceType type) {
        return type == ResourceType.Document || type == ResourceType.Attachment || type == ResourceType.Conflict
                || type == ResourceType.StoredProcedure || type == ResourceType.Trigger || type == ResourceType.UserDefinedFunction;
    }

    public static boolean isWriteOperation(OperationType operationType) {
        return operationType == OperationType.Create || operationType == OperationType.Upsert || operationType == OperationType.Delete || operationType == OperationType.Replace
                || operationType == OperationType.ExecuteJavaScript;
    }

    public static boolean isFeedRequest(OperationType requestOperationType) {
        return requestOperationType == OperationType.Create ||
                requestOperationType == OperationType.Upsert ||
                requestOperationType == OperationType.ReadFeed ||
                requestOperationType == OperationType.Query ||
                requestOperationType == OperationType.SqlQuery ||
                requestOperationType == OperationType.HeadFeed;
    }

    private static String addTrailingSlash(String path) {
        if (path == null || path.isEmpty())
            path = new String(RuntimeConstants.Separators.Url);
        else if (path.charAt(path.length() - 1) != RuntimeConstants.Separators.Url[0])
            path = path + RuntimeConstants.Separators.Url[0];

        return path;
    }

    private static String removeLeadingQuestionMark(String path) {
        if (path == null || path.isEmpty())
            return path;

        if (path.charAt(0) == RuntimeConstants.Separators.Query[0])
            return path.substring(1);

        return path;
    }

    public static boolean isValidConsistency(ConsistencyLevel backendConsistency,
            ConsistencyLevel desiredConsistency) {
        switch (backendConsistency) {
        case Strong:
            return desiredConsistency == ConsistencyLevel.Strong ||
            desiredConsistency == ConsistencyLevel.BoundedStaleness ||
            desiredConsistency == ConsistencyLevel.Session ||
            desiredConsistency == ConsistencyLevel.Eventual ||
            desiredConsistency == ConsistencyLevel.ConsistentPrefix;

        case BoundedStaleness:
            return desiredConsistency == ConsistencyLevel.BoundedStaleness ||
            desiredConsistency == ConsistencyLevel.Session ||
            desiredConsistency == ConsistencyLevel.Eventual ||
            desiredConsistency == ConsistencyLevel.ConsistentPrefix;

        case Session:
        case Eventual:
        case ConsistentPrefix:
            return desiredConsistency == ConsistencyLevel.Session ||
            desiredConsistency == ConsistencyLevel.Eventual ||
            desiredConsistency == ConsistencyLevel.ConsistentPrefix;

        default:
            throw new IllegalArgumentException("backendConsistency");
        }
    }

    public static String getUserAgent(String sdkName, String sdkVersion) {
        String osName = System.getProperty("os.name");
        if (osName == null) {
            osName = "Unknown";
        }
        osName = osName.replaceAll("\\s", "");
        String userAgent = String.format("%s/%s JRE/%s %s/%s",
                osName,
                System.getProperty("os.version"),
                System.getProperty("java.version"),
                sdkName,
                sdkVersion);
        return userAgent;
    }

    public static ObjectMapper getSimpleObjectMapper() {
        return Utils.simpleObjectMapper;
    }

    /**
     * Returns Current Time in RFC 1123 format, e.g, 
     * Fri, 01 Dec 2017 19:22:30 GMT.
     * 
     * @return an instance of String
     */
    public static String nowAsRFC1123() {
        ZonedDateTime now = ZonedDateTime.now(GMT_ZONE_ID);
        return Utils.RFC_1123_DATE_TIME.format(now);
    }

    public static UUID randomUUID() {
        return TimeUUIDGegerator.generate();
    }
}
