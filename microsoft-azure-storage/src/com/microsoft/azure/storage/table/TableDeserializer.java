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

package com.microsoft.azure.storage.table;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.microsoft.azure.storage.Constants;
import com.microsoft.azure.storage.OperationContext;
import com.microsoft.azure.storage.StorageErrorCodeStrings;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.core.SR;

/**
 * Reserved for internal use. A class used to read Table entities.
 */
final class TableDeserializer {

    /**
     * Used to create Json parsers and generators.
     */
    private static JsonFactory jsonFactory = new JsonFactory();

    /**
     * Reserved for internal use. Parses the operation response as a collection of entities. Reads entity data from the
     * specified input stream using the specified class type and optionally projects each entity result with the
     * specified resolver into an {@link ODataPayload} containing a collection of {@link TableResult} objects. .
     * 
     * @param inStream
     *            The <code>InputStream</code> to read the data to parse from.
     * @param format
     *            The {@link TablePayloadFormat} to use for parsing.
     * @param clazzType
     *            The class type <code>T</code> implementing {@link TableEntity} for the entities returned. Set to
     *            <code>null</code> to ignore the returned entities and copy only response properties into the
     *            {@link TableResult} objects.
     * @param resolver
     *            An {@link EntityResolver} instance to project the entities into instances of type <code>R</code>. Set
     *            to <code>null</code> to return the entities as instances of the class type <code>T</code>.
     * @param opContext
     *            An {@link OperationContext} object used to track the execution of the operation.
     * @return
     *         An {@link ODataPayload} containing a collection of {@link TableResult} objects with the parsed operation
     *         response.
     * @throws InstantiationException
     *             if an error occurs while constructing the result.
     * @throws IllegalAccessException
     *             if an error occurs in reflection while parsing the result.
     * @throws XMLStreamException
     *             if an error occurs while accessing the stream with AtomPub.
     * @throws StorageException
     *             if a storage service error occurs.
     * @throws IOException
     *             if an error occurs while accessing the stream with Json.
     * @throws JsonParseException
     *             if an error occurs while parsing the stream.
     */
    @SuppressWarnings("deprecation")
    static <T extends TableEntity, R> ODataPayload<?> parseQueryResponse(final InputStream inStream,
            final TableRequestOptions options, final Class<T> clazzType, final EntityResolver<R> resolver,
            final OperationContext opContext) throws InstantiationException, IllegalAccessException,
            XMLStreamException, StorageException, JsonParseException, IOException {
        ODataPayload<?> payload;
        if (options.getTablePayloadFormat() == TablePayloadFormat.AtomPub) {
            payload = parseAtomQueryResponse(inStream, clazzType, resolver, opContext);
        }
        else {
            payload = parseJsonQueryResponse(inStream, clazzType, resolver, options, opContext);
        }
        return payload;
    }

    /**
     * Reserved for internal use. Parses the operation response as a collection of entities. Reads entity data from the
     * specified input stream using the specified class type and optionally projects each entity result with the
     * specified resolver into an {@link ODataPayload} containing a collection of {@link TableResult} objects. .
     * 
     * @param inStream
     *            The <code>InputStream</code> to read the data to parse from.
     * @param format
     *            The {@link TablePayloadFormat} to use for parsing.
     * @param httpStatusCode
     *            The HTTP status code returned with the operation response.
     * @param clazzType
     *            The class type <code>T</code> implementing {@link TableEntity} for the entities returned. Set to
     *            <code>null</code> to ignore the returned entities and copy only response properties into the
     *            {@link TableResult} objects.
     * @param resolver
     *            An {@link EntityResolver} instance to project the entities into instances of type <code>R</code>. Set
     *            to <code>null</code> to return the entities as instances of the class type <code>T</code>.
     * @param opContext
     *            An {@link OperationContext} object used to track the execution of the operation.
     * @return
     *         A {@link TableResult} object with the parsed operation response.
     * @throws InstantiationException
     *             if an error occurs while constructing the result.
     * @throws IllegalAccessException
     *             if an error occurs in reflection while parsing the result.
     * @throws XMLStreamException
     *             if an error occurs while accessing the stream with AtomPub.
     * @throws StorageException
     *             if a storage service error occurs.
     * @throws IOException
     *             if an error occurs while accessing the stream with Json.
     * @throws JsonParseException
     *             if an error occurs while parsing the stream.
     */
    @SuppressWarnings("deprecation")
    static <T extends TableEntity, R> TableResult parseSingleOpResponse(final InputStream inStream,
            final TableRequestOptions options, final int httpStatusCode, final Class<T> clazzType,
            final EntityResolver<R> resolver, final OperationContext opContext) throws InstantiationException,
            IllegalAccessException, XMLStreamException, StorageException, IOException, JsonParseException {
        TableResult res;
        if (options.getTablePayloadFormat() == TablePayloadFormat.AtomPub) {
            res = parseSingleOpAtomResponse(inStream, httpStatusCode, clazzType, resolver, opContext);
        }
        else {
            res = parseSingleOpJsonResponse(inStream, httpStatusCode, clazzType, resolver, options, opContext);
        }
        return res;
    }

    /**
     * Reserved for internal use. Parses the operation response as an entity. Parses the result returned in the
     * specified stream in JSON format into a {@link TableResult} containing an entity of the specified class type
     * projected using the specified resolver.
     * 
     * @param parser
     *            The <code>JsonParser</code> to read the data to parse from.
     * @param clazzType
     *            The class type <code>T</code> implementing {@link TableEntity} for the entity returned. Set to
     *            <code>null</code> to ignore the returned entity and copy only response properties into the
     *            {@link TableResult} object.
     * @param resolver
     *            An {@link EntityResolver} instance to project the entity into an instance of type <code>R</code>. Set
     *            to <code>null</code> to return the entity as an instance of the class type <code>T</code>.
     * @param opContext
     *            An {@link OperationContext} object used to track the execution of the operation.
     * @return
     *         A {@link TableResult} containing the parsed entity result of the operation.
     * @throws IOException
     *             if an error occurs while accessing the stream.
     * @throws InstantiationException
     *             if an error occurs while constructing the result.
     * @throws IllegalAccessException
     *             if an error occurs in reflection while parsing the result.
     * @throws StorageException
     *             if a storage service error occurs.
     * @throws IOException
     *             if an error occurs while accessing the stream.
     * @throws JsonParseException
     *             if an error occurs while parsing the stream.
     */
    private static <T extends TableEntity, R> TableResult parseJsonEntity(final JsonParser parser,
            final Class<T> clazzType, HashMap<String, PropertyPair> classProperties, final EntityResolver<R> resolver,
            final TableRequestOptions options, final OperationContext opContext) throws JsonParseException,
            IOException, StorageException, InstantiationException, IllegalAccessException {
        final TableResult res = new TableResult();

        final HashMap<String, EntityProperty> properties = new HashMap<String, EntityProperty>();

        if (!parser.hasCurrentToken()) {
            parser.nextToken();
        }

        ODataUtilities.assertIsStartObjectJsonToken(parser);

        parser.nextToken();

        // get all metadata, if present
        while (parser.getCurrentName().startsWith(ODataConstants.ODATA_PREFIX)) {
            final String name = parser.getCurrentName().substring(ODataConstants.ODATA_PREFIX.length());

            // get the value token
            parser.nextToken();

            if (name.equals(ODataConstants.ETAG)) {
                String etag = parser.getValueAsString();
                res.setEtag(etag);
            }

            // get the key token
            parser.nextToken();
        }

        if (resolver == null && clazzType == null) {
            return res;
        }

        // get object properties
        while (parser.getCurrentToken() != JsonToken.END_OBJECT) {
            String key = Constants.EMPTY_STRING;
            String val = Constants.EMPTY_STRING;
            EdmType edmType = null;

            // checks if this property is preceded by an OData property type annotation
            if (options.getTablePayloadFormat() != TablePayloadFormat.JsonNoMetadata
                    && parser.getCurrentName().endsWith(ODataConstants.ODATA_TYPE_SUFFIX)) {
                parser.nextToken();
                edmType = EdmType.parse(parser.getValueAsString());

                parser.nextValue();
                key = parser.getCurrentName();
                val = parser.getValueAsString();
            }
            else {
                key = parser.getCurrentName();

                parser.nextToken();
                val = parser.getValueAsString();
                edmType = evaluateEdmType(parser.getCurrentToken(), parser.getValueAsString());
            }

            final EntityProperty newProp = new EntityProperty(val, edmType);
            properties.put(key, newProp);

            parser.nextToken();
        }

        String partitionKey = null;
        String rowKey = null;
        Date timestamp = null;
        String etag = null;

        // Remove core properties from map and set individually
        EntityProperty tempProp = properties.remove(TableConstants.PARTITION_KEY);
        if (tempProp != null) {
            partitionKey = tempProp.getValueAsString();
        }

        tempProp = properties.remove(TableConstants.ROW_KEY);
        if (tempProp != null) {
            rowKey = tempProp.getValueAsString();
        }

        tempProp = properties.remove(TableConstants.TIMESTAMP);
        if (tempProp != null) {
            timestamp = tempProp.getValueAsDate();

            if (res.getEtag() == null) {
                etag = getETagFromTimestamp(tempProp.getValueAsString());
                res.setEtag(etag);
            }
        }

        // do further processing for type if JsonNoMetdata by inferring type information via resolver or clazzType
        if (options.getTablePayloadFormat() == TablePayloadFormat.JsonNoMetadata
                && (options.getPropertyResolver() != null || clazzType != null)) {
            if (options.getPropertyResolver() != null) {
                for (final Entry<String, EntityProperty> p : properties.entrySet()) {
                    final String key = p.getKey();
                    final String value = p.getValue().getValueAsString();
                    EdmType edmType;

                    // try to use the property resolver to get the type
                    try {
                        edmType = options.getPropertyResolver().propertyResolver(partitionKey, rowKey, key, value);
                    }
                    catch (Exception e) {
                        throw new StorageException(StorageErrorCodeStrings.INTERNAL_ERROR, SR.CUSTOM_RESOLVER_THREW,
                                Constants.HeaderConstants.HTTP_UNUSED_306, null, e);
                    }

                    // try to create a new entity property using the returned type
                    try {
                        final EntityProperty newProp = new EntityProperty(value, edmType);
                        properties.put(p.getKey(), newProp);
                    }
                    catch (IllegalArgumentException e) {
                        throw new StorageException(StorageErrorCodeStrings.INVALID_TYPE, String.format(
                                SR.FAILED_TO_PARSE_PROPERTY, key, value, edmType),
                                Constants.HeaderConstants.HTTP_UNUSED_306, null, e);
                    }
                }
            }
            else if (clazzType != null) {
                if (classProperties == null) {
                    classProperties = PropertyPair.generatePropertyPairs(clazzType);
                }
                for (final Entry<String, EntityProperty> p : properties.entrySet()) {
                    PropertyPair propPair = classProperties.get(p.getKey());
                    if (propPair != null) {
                        final EntityProperty newProp = new EntityProperty(p.getValue().getValueAsString(),
                                propPair.type);
                        properties.put(p.getKey(), newProp);
                    }
                }
            }
        }

        // set the result properties, now that they are appropriately parsed
        res.setProperties(properties);

        // use resolver if provided, else create entity based on clazz type
        if (resolver != null) {
            res.setResult(resolver.resolve(partitionKey, rowKey, timestamp, res.getProperties(), res.getEtag()));
        }
        else if (clazzType != null) {
            // Generate new entity and return
            final T entity = clazzType.newInstance();
            entity.setEtag(res.getEtag());

            entity.setPartitionKey(partitionKey);
            entity.setRowKey(rowKey);
            entity.setTimestamp(timestamp);

            entity.readEntity(res.getProperties(), opContext);

            res.setResult(entity);
        }

        return res;
    }

    /**
     * Reserved for internal use. Parses the operation response as a collection of entities. Reads entity data from the
     * specified input stream using the specified class type and optionally projects each entity result with the
     * specified resolver into an {@link ODataPayload} containing a collection of {@link TableResult} objects.
     * 
     * @param inStream
     *            The <code>InputStream</code> to read the data to parse from.
     * @param clazzType
     *            The class type <code>T</code> implementing {@link TableEntity} for the entities returned. Set to
     *            <code>null</code> to ignore the returned entities and copy only response properties into the
     *            {@link TableResult} objects.
     * @param resolver
     *            An {@link EntityResolver} instance to project the entities into instances of type <code>R</code>. Set
     *            to <code>null</code> to return the entities as instances of the class type <code>T</code>.
     * @param opContext
     *            An {@link OperationContext} object used to track the execution of the operation.
     * @return
     *         An {@link ODataPayload} containing a collection of {@link TableResult} objects with the parsed operation
     *         response.
     * @throws InstantiationException
     *             if an error occurs while constructing the result.
     * @throws IllegalAccessException
     *             if an error occurs in reflection while parsing the result.
     * @throws StorageException
     *             if a storage service error occurs.
     * @throws IOException
     *             if an error occurs while accessing the stream.
     * @throws JsonParseException
     *             if an error occurs while parsing the stream.
     */
    @SuppressWarnings("unchecked")
    private static <T extends TableEntity, R> ODataPayload<?> parseJsonQueryResponse(final InputStream inStream,
            final Class<T> clazzType, final EntityResolver<R> resolver, final TableRequestOptions options,
            final OperationContext opContext) throws InstantiationException, IllegalAccessException, StorageException,
            JsonParseException, IOException {
        ODataPayload<T> corePayload = null;
        ODataPayload<R> resolvedPayload = null;
        ODataPayload<?> commonPayload = null;

        JsonParser parser = createJsonParserFromStream(inStream);

        try {

            if (resolver != null) {
                resolvedPayload = new ODataPayload<R>();
                commonPayload = resolvedPayload;
            }
            else {
                corePayload = new ODataPayload<T>();
                commonPayload = corePayload;
            }

            if (!parser.hasCurrentToken()) {
                parser.nextToken();
            }

            ODataUtilities.assertIsStartObjectJsonToken(parser);

            // move into data  
            parser.nextToken();

            // if there is a clazz type and if JsonNoMetadata, create a classProperties dictionary to use for type inference once 
            // instead of querying the cache many times
            HashMap<String, PropertyPair> classProperties = null;
            if (options.getTablePayloadFormat() == TablePayloadFormat.JsonNoMetadata && clazzType != null) {
                classProperties = PropertyPair.generatePropertyPairs(clazzType);
            }

            while (parser.getCurrentToken() != null) {
                if (parser.getCurrentToken() == JsonToken.FIELD_NAME
                        && parser.getCurrentName().equals(ODataConstants.VALUE)) {
                    // move to start of array
                    parser.nextToken();

                    ODataUtilities.assertIsStartArrayJsonToken(parser);

                    // go to properties
                    parser.nextToken();

                    while (parser.getCurrentToken() == JsonToken.START_OBJECT) {
                        final TableResult res = parseJsonEntity(parser, clazzType, classProperties, resolver, options,
                                opContext);
                        if (corePayload != null) {
                            corePayload.tableResults.add(res);
                        }

                        if (resolver != null) {
                            resolvedPayload.results.add((R) res.getResult());
                        }
                        else {
                            corePayload.results.add((T) res.getResult());
                        }

                        parser.nextToken();
                    }

                    ODataUtilities.assertIsEndArrayJsonToken(parser);
                }

                parser.nextToken();
            }
        }
        finally {
            parser.close();
        }

        return commonPayload;
    }

    /**
     * Reserved for internal use. Parses the operation response as an entity. Reads entity data from the specified
     * <code>JsonParser</code> using the specified class type and optionally projects the entity result with the
     * specified resolver into a {@link TableResult} object.
     * 
     * @param parser
     *            The <code>JsonParser</code> to read the data to parse from.
     * @param httpStatusCode
     *            The HTTP status code returned with the operation response.
     * @param clazzType
     *            The class type <code>T</code> implementing {@link TableEntity} for the entity returned. Set to
     *            <code>null</code> to ignore the returned entity and copy only response properties into the
     *            {@link TableResult} object.
     * @param resolver
     *            An {@link EntityResolver} instance to project the entity into an instance of type <code>R</code>. Set
     *            to <code>null</code> to return the entitys as instance of the class type <code>T</code>.
     * @param opContext
     *            An {@link OperationContext} object used to track the execution of the operation.
     * @return
     *         A {@link TableResult} object with the parsed operation response.
     * @throws InstantiationException
     *             if an error occurs while constructing the result.
     * @throws IllegalAccessException
     *             if an error occurs in reflection while parsing the result.
     * @throws StorageException
     *             if a storage service error occurs.
     * @throws IOException
     *             if an error occurs while accessing the stream.
     * @throws JsonParseException
     *             if an error occurs while parsing the stream.
     */
    private static <T extends TableEntity, R> TableResult parseSingleOpJsonResponse(final InputStream inStream,
            final int httpStatusCode, final Class<T> clazzType, final EntityResolver<R> resolver,
            final TableRequestOptions options, final OperationContext opContext) throws InstantiationException,
            IllegalAccessException, StorageException, JsonParseException, IOException {
        JsonParser parser = createJsonParserFromStream(inStream);

        try {
            final TableResult res = parseJsonEntity(parser, clazzType,
                    null /*HashMap<String, PropertyPair> classProperties*/, resolver, options, opContext);
            res.setHttpStatusCode(httpStatusCode);
            return res;
        }
        finally {
            parser.close();
        }
    }

    /**
     * Reserved for internal use. Parses the operation response as an entity. Parses the result returned in the
     * specified stream in AtomPub format into a {@link TableResult} containing an entity of the specified class type
     * projected using the specified resolver.
     * 
     * @param xmlr
     *            An <code>XMLStreamReader</code> on the input stream.
     * @param clazzType
     *            The class type <code>T</code> implementing {@link TableEntity} for the entity returned. Set to
     *            <code>null</code> to ignore the returned entity and copy only response properties into the
     *            {@link TableResult} object.
     * @param resolver
     *            An {@link EntityResolver} instance to project the entity into an instance of type <code>R</code>. Set
     *            to <code>null</code> to return the entity as an instance of the class type <code>T</code>.
     * @param opContext
     *            An {@link OperationContext} object used to track the execution of the operation.
     * @return
     *         A {@link TableResult} containing the parsed entity result of the operation.
     * 
     * @throws XMLStreamException
     *             if an error occurs while accessing the stream.
     * @throws InstantiationException
     *             if an error occurs while constructing the result.
     * @throws IllegalAccessException
     *             if an error occurs in reflection while parsing the result.
     * @throws StorageException
     *             if a storage service error occurs.
     */
    private static <T extends TableEntity, R> TableResult parseAtomEntity(final XMLStreamReader xmlr,
            final Class<T> clazzType, final EntityResolver<R> resolver, final OperationContext opContext)
            throws XMLStreamException, InstantiationException, IllegalAccessException, StorageException {
        int eventType = xmlr.getEventType();
        final TableResult res = new TableResult();

        xmlr.require(XMLStreamConstants.START_ELEMENT, null, ODataConstants.ENTRY);

        String etag = xmlr.getAttributeValue(ODataConstants.DATA_SERVICES_METADATA_NS, ODataConstants.ETAG);

        res.setEtag(etag);

        while (xmlr.hasNext()) {
            eventType = xmlr.next();
            if (eventType == XMLStreamConstants.CHARACTERS) {
                xmlr.getText();
                continue;
            }

            final String name = xmlr.getName().toString();

            if (eventType == XMLStreamConstants.START_ELEMENT) {
                if (name.equals(ODataConstants.BRACKETED_ATOM_NS + ODataConstants.ID)) {
                    DeserializationHelper.readElementFromXMLReader(xmlr, ODataConstants.ID);
                }
                else if (name.equals(ODataConstants.BRACKETED_DATA_SERVICES_METADATA_NS + ODataConstants.PROPERTIES)) {
                    // Do read properties
                    if (resolver == null && clazzType == null) {
                        return res;
                    }
                    else {
                        res.setProperties(readAtomProperties(xmlr, opContext));
                        break;
                    }
                }
            }
        }

        // Move to end Content
        eventType = xmlr.next();
        if (eventType == XMLStreamConstants.CHARACTERS) {
            eventType = xmlr.next();
        }

        xmlr.require(XMLStreamConstants.END_ELEMENT, null, ODataConstants.CONTENT);

        eventType = xmlr.next();
        if (eventType == XMLStreamConstants.CHARACTERS) {
            eventType = xmlr.next();
        }

        xmlr.require(XMLStreamConstants.END_ELEMENT, null, ODataConstants.ENTRY);

        String rowKey = null;
        String partitionKey = null;
        Date timestamp = null;

        // Remove core properties from map and set individually
        EntityProperty tempProp = res.getProperties().remove(TableConstants.PARTITION_KEY);
        if (tempProp != null) {
            partitionKey = tempProp.getValueAsString();
        }

        tempProp = res.getProperties().remove(TableConstants.ROW_KEY);
        if (tempProp != null) {
            rowKey = tempProp.getValueAsString();
        }

        tempProp = res.getProperties().remove(TableConstants.TIMESTAMP);
        if (tempProp != null) {
            timestamp = tempProp.getValueAsDate();
        }

        if (resolver != null) {
            // Call resolver
            res.setResult(resolver.resolve(partitionKey, rowKey, timestamp, res.getProperties(), res.getEtag()));
        }
        else if (clazzType != null) {
            // Generate new entity and return
            final T entity = clazzType.newInstance();
            entity.setEtag(res.getEtag());

            entity.setPartitionKey(partitionKey);
            entity.setRowKey(rowKey);
            entity.setTimestamp(timestamp);

            entity.readEntity(res.getProperties(), opContext);

            res.setResult(entity);
        }

        return res;
    }

    /**
     * Reserved for internal use. Parses the operation response as a collection of entities. Reads entity data from the
     * specified input stream using the specified class type and optionally projects each entity result with the
     * specified resolver into an {@link ODataPayload} containing a collection of {@link TableResult} objects.
     * 
     * @param inStream
     *            The <code>InputStream</code> to read the data to parse from.
     * @param clazzType
     *            The class type <code>T</code> implementing {@link TableEntity} for the entities returned. Set to
     *            <code>null</code> to ignore the returned entities and copy only response properties into the
     *            {@link TableResult} objects.
     * @param resolver
     *            An {@link EntityResolver} instance to project the entities into instances of type <code>R</code>. Set
     *            to <code>null</code> to return the entities as instances of the class type <code>T</code>.
     * @param opContext
     *            An {@link OperationContext} object used to track the execution of the operation.
     * @return
     *         An {@link ODataPayload} containing a collection of {@link TableResult} objects with the parsed operation
     *         response.
     * 
     * @throws XMLStreamException
     *             if an error occurs while accessing the stream.
     * @throws InstantiationException
     *             if an error occurs while constructing the result.
     * @throws IllegalAccessException
     *             if an error occurs in reflection while parsing the result.
     * @throws StorageException
     *             if a storage service error occurs.
     */
    @SuppressWarnings("unchecked")
    private static <T extends TableEntity, R> ODataPayload<?> parseAtomQueryResponse(final InputStream inStream,
            final Class<T> clazzType, final EntityResolver<R> resolver, final OperationContext opContext)
            throws XMLStreamException, InstantiationException, IllegalAccessException, StorageException {
        ODataPayload<T> corePayload = null;
        ODataPayload<R> resolvedPayload = null;
        ODataPayload<?> commonPayload = null;

        if (resolver != null) {
            resolvedPayload = new ODataPayload<R>();
            commonPayload = resolvedPayload;
        }
        else {
            corePayload = new ODataPayload<T>();
            commonPayload = corePayload;
        }

        final XMLStreamReader xmlr = DeserializationHelper.createXMLStreamReaderFromStream(inStream);
        int eventType = xmlr.getEventType();
        xmlr.require(XMLStreamConstants.START_DOCUMENT, null, null);
        eventType = xmlr.next();

        xmlr.require(XMLStreamConstants.START_ELEMENT, null, ODataConstants.FEED);
        // skip feed chars
        eventType = xmlr.next();

        while (xmlr.hasNext()) {
            eventType = xmlr.next();

            if (eventType == XMLStreamConstants.CHARACTERS) {
                xmlr.getText();
                continue;
            }

            final String name = xmlr.getName().toString();

            if (eventType == XMLStreamConstants.START_ELEMENT) {
                if (name.equals(ODataConstants.BRACKETED_ATOM_NS + ODataConstants.ENTRY)) {
                    final TableResult res = parseAtomEntity(xmlr, clazzType, resolver, opContext);
                    if (corePayload != null) {
                        corePayload.tableResults.add(res);
                    }

                    if (resolver != null) {
                        resolvedPayload.results.add((R) res.getResult());
                    }
                    else {
                        corePayload.results.add((T) res.getResult());
                    }
                }
            }
            else if (eventType == XMLStreamConstants.END_ELEMENT
                    && name.equals(ODataConstants.BRACKETED_ATOM_NS + ODataConstants.FEED)) {
                break;
            }
        }

        xmlr.require(XMLStreamConstants.END_ELEMENT, null, ODataConstants.FEED);
        return commonPayload;
    }

    /**
     * Reserved for internal use. Parses the operation response as an entity. Reads entity data from the specified
     * <code>XMLStreamReader</code> using the specified class type and optionally projects the entity result with the
     * specified resolver into a {@link TableResult} object.
     * 
     * @param xmlr
     *            The <code>XMLStreamReader</code> to read the data to parse from.
     * @param httpStatusCode
     *            The HTTP status code returned with the operation response.
     * @param clazzType
     *            The class type <code>T</code> implementing {@link TableEntity} for the entity returned. Set to
     *            <code>null</code> to ignore the returned entity and copy only response properties into the
     *            {@link TableResult} object.
     * @param resolver
     *            An {@link EntityResolver} instance to project the entity into an instance of type <code>R</code>. Set
     *            to <code>null</code> to return the entitys as instance of the class type <code>T</code>.
     * @param opContext
     *            An {@link OperationContext} object used to track the execution of the operation.
     * @return
     *         A {@link TableResult} object with the parsed operation response.
     * 
     * @throws XMLStreamException
     *             if an error occurs while accessing the stream.
     * @throws InstantiationException
     *             if an error occurs while constructing the result.
     * @throws IllegalAccessException
     *             if an error occurs in reflection while parsing the result.
     * @throws StorageException
     *             if a storage service error occurs.
     */
    private static <T extends TableEntity, R> TableResult parseSingleOpAtomResponse(final InputStream inStream,
            final int httpStatusCode, final Class<T> clazzType, final EntityResolver<R> resolver,
            final OperationContext opContext) throws XMLStreamException, InstantiationException,
            IllegalAccessException, StorageException {
        XMLStreamReader xmlr = DeserializationHelper.createXMLStreamReaderFromStream(inStream);

        try {
            xmlr.require(XMLStreamConstants.START_DOCUMENT, null, null);
            xmlr.next();

            final TableResult res = parseAtomEntity(xmlr, clazzType, resolver, opContext);
            res.setHttpStatusCode(httpStatusCode);
            return res;
        }
        finally {
            xmlr.close();
        }
    }

    /**
     * Reserved for internal use. Reads the properties of an entity from the stream into a map of property names to
     * typed values. Reads the entity data as an AtomPub Entry Resource from the specified {@link XMLStreamReader} into
     * a map of <code>String</code> property names to {@link EntityProperty} data typed values.
     * 
     * @param xmlr
     *            The <code>XMLStreamReader</code> to read the data from.
     * @param opContext
     *            An {@link OperationContext} object used to track the execution of the operation.
     * 
     * @return
     *         A <code>java.util.HashMap</code> containing a map of <code>String</code> property names to
     *         {@link EntityProperty} data typed values found in the entity data.
     * @throws XMLStreamException
     *             if an error occurs accessing the stream.
     */
    private static HashMap<String, EntityProperty> readAtomProperties(final XMLStreamReader xmlr,
            final OperationContext opContext) throws XMLStreamException {
        int eventType = xmlr.getEventType();
        xmlr.require(XMLStreamConstants.START_ELEMENT, null, ODataConstants.PROPERTIES);
        final HashMap<String, EntityProperty> properties = new HashMap<String, EntityProperty>();

        while (xmlr.hasNext()) {
            eventType = xmlr.next();
            if (eventType == XMLStreamConstants.CHARACTERS) {
                xmlr.getText();
                continue;
            }

            if (eventType == XMLStreamConstants.START_ELEMENT
                    && xmlr.getNamespaceURI().equals(ODataConstants.DATA_SERVICES_NS)) {
                final String key = xmlr.getLocalName();
                String val = Constants.EMPTY_STRING;
                String edmType = null;

                if (xmlr.getAttributeCount() > 0) {
                    edmType = xmlr.getAttributeValue(ODataConstants.DATA_SERVICES_METADATA_NS, ODataConstants.TYPE);
                }

                // move to chars
                eventType = xmlr.next();

                if (eventType == XMLStreamConstants.CHARACTERS) {
                    val = xmlr.getText();

                    // end element
                    eventType = xmlr.next();
                }

                xmlr.require(XMLStreamConstants.END_ELEMENT, null, key);

                final EntityProperty newProp = new EntityProperty(val, EdmType.parse(edmType));
                properties.put(key, newProp);
            }
            else if (eventType == XMLStreamConstants.END_ELEMENT
                    && xmlr.getName().toString()
                            .equals(ODataConstants.BRACKETED_DATA_SERVICES_METADATA_NS + ODataConstants.PROPERTIES)) {
                // End read properties
                break;
            }
        }

        xmlr.require(XMLStreamConstants.END_ELEMENT, null, ODataConstants.PROPERTIES);
        return properties;
    }

    private static String getETagFromTimestamp(String timestampString) throws UnsupportedEncodingException {
        timestampString = URLEncoder.encode(timestampString, Constants.UTF8_CHARSET);
        return "W/\"datetime'" + timestampString + "'\"";
    }

    private static EdmType evaluateEdmType(JsonToken token, String value) {
        EdmType edmType = null;

        if (token == JsonToken.VALUE_NULL) {
            edmType = EdmType.NULL;
        }
        else if (token == JsonToken.VALUE_FALSE || token == JsonToken.VALUE_TRUE) {
            edmType = EdmType.BOOLEAN;
        }
        else if (token == JsonToken.VALUE_NUMBER_FLOAT) {
            edmType = EdmType.DOUBLE;
        }
        else if (token == JsonToken.VALUE_NUMBER_INT) {
            edmType = EdmType.INT32;
        }
        else {
            edmType = EdmType.STRING;
        }

        return edmType;
    }

    private static JsonParser createJsonParserFromStream(final InputStream streamRef) throws JsonParseException,
            IOException {
        JsonParser parser = jsonFactory.createParser(streamRef);

        // allows handling of infinity, -infinity, and NaN for Doubles
        return parser.enable(JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS);
    }
}
