package com.microsoft.windowsazure.services.core.storage.utils.implementation;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.stream.XMLStreamException;

import com.microsoft.windowsazure.services.core.storage.Constants;
import com.microsoft.windowsazure.services.core.storage.OperationContext;
import com.microsoft.windowsazure.services.core.storage.ServiceProperties;
import com.microsoft.windowsazure.services.core.storage.StorageException;

/**
 * RESERVED FOR INTERNAL USE. The base response class for the protocol layer
 * 
 * Copyright (c)2011 Microsoft. All rights reserved.
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
    private static
            HashMap<String, String> getValuesByHeaderPrefix(final HttpURLConnection request, final String prefix) {
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
    public static ServiceProperties readServicePropertiesFromStream(
            final InputStream inStream, final OperationContext opContext) throws XMLStreamException, StorageException {
        return ServiceProperties.readServicePropertiesFromStream(inStream, opContext);
    }

    /**
     * Private Default Ctor
     */
    protected BaseResponse() {
        // No op
    }
}
