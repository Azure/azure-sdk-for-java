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
package com.microsoft.windowsazure.storage;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.StringTokenizer;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.microsoft.windowsazure.storage.core.DeserializationHelper;
import com.microsoft.windowsazure.storage.core.SR;

/**
 * RESERVED FOR INTERNAL USE. A class used to serialize ServiceProperties to a byte array.
 */
public final class ServicePropertiesDeserializer {
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
    public static ServiceProperties readServicePropertiesFromStream(final InputStream inStream,
            final OperationContext opContext) throws XMLStreamException, StorageException {
        final ServiceProperties retProps = new ServiceProperties();

        final XMLStreamReader xmlr = DeserializationHelper.createXMLStreamReaderFromStream(inStream);

        // Start document
        int eventType = xmlr.getEventType();
        xmlr.require(XMLStreamConstants.START_DOCUMENT, null, null);

        // Get StorageServiceProperties Header
        eventType = xmlr.next();
        xmlr.require(XMLStreamConstants.START_ELEMENT, null,
                Constants.AnalyticsConstants.STORAGE_SERVICE_PROPERTIES_ELEMENT);

        // Check if there are more events in the input stream
        while (xmlr.hasNext()) {
            eventType = xmlr.next();
            if (eventType == XMLStreamConstants.START_ELEMENT) {
                final String name = xmlr.getName().toString();
                if (name.equals(Constants.AnalyticsConstants.LOGGING_ELEMENT)) {
                    retProps.setLogging(readLoggingProperties(xmlr));
                    xmlr.require(XMLStreamConstants.END_ELEMENT, null, Constants.AnalyticsConstants.LOGGING_ELEMENT);
                }
                else if (name.equals(Constants.AnalyticsConstants.HOUR_METRICS_ELEMENT)) {
                    retProps.setHourMetrics(readMetricsProperties(Constants.AnalyticsConstants.HOUR_METRICS_ELEMENT,
                            xmlr));
                    xmlr.require(XMLStreamConstants.END_ELEMENT, null,
                            Constants.AnalyticsConstants.HOUR_METRICS_ELEMENT);
                }
                else if (name.equals(Constants.AnalyticsConstants.MINUTE_METRICS_ELEMENT)) {
                    retProps.setMinuteMetrics(readMetricsProperties(
                            Constants.AnalyticsConstants.MINUTE_METRICS_ELEMENT, xmlr));
                    xmlr.require(XMLStreamConstants.END_ELEMENT, null,
                            Constants.AnalyticsConstants.MINUTE_METRICS_ELEMENT);
                }
                else if (name.equals(Constants.AnalyticsConstants.CORS_ELEMENT)) {
                    retProps.setCors(readCorsProperties(xmlr));
                    xmlr.require(XMLStreamConstants.END_ELEMENT, null, Constants.AnalyticsConstants.CORS_ELEMENT);
                }
                else if (name.equals(Constants.AnalyticsConstants.DEFAULT_SERVICE_VERSION)) {
                    retProps.setDefaultServiceVersion(DeserializationHelper.readElementFromXMLReader(xmlr,
                            Constants.AnalyticsConstants.DEFAULT_SERVICE_VERSION));
                }
                else {
                    throw new StorageException(StorageErrorCodeStrings.INVALID_XML_DOCUMENT,
                            SR.INVALID_RESPONSE_RECEIVED, Constants.HeaderConstants.HTTP_UNUSED_306, null, null);
                }
            }
            else if (eventType == XMLStreamConstants.END_ELEMENT) {
                if (xmlr.getName().toString().equals(Constants.AnalyticsConstants.STORAGE_SERVICE_PROPERTIES_ELEMENT)) {
                    break;
                }
            }
        }

        return retProps;
    }

    /**
     * Returns the a populated LoggingProperties object from the xml reader. XmlStreamReader must be at start of the
     * LoggingProperties element.
     * 
     * @param xmlr
     *            the XMLStreamReader to read from
     * @return the populated LoggingProperties object
     * @throws XMLStreamException
     *             if the xml is invalid.
     * @throws StorageException
     *             if unexpected xml is found.
     */
    private static LoggingProperties readLoggingProperties(final XMLStreamReader xmlr) throws XMLStreamException,
            StorageException {
        xmlr.require(XMLStreamConstants.START_ELEMENT, null, Constants.AnalyticsConstants.LOGGING_ELEMENT);

        int eventType = xmlr.getEventType();
        String tempParseString = null;
        LoggingProperties logging = new LoggingProperties();

        while (xmlr.hasNext()) {
            eventType = xmlr.next();

            if (eventType == XMLStreamConstants.START_ELEMENT) {
                final String name = xmlr.getName().toString();
                if (name.equals(Constants.AnalyticsConstants.VERSION_ELEMENT)) {
                    logging.setVersion(DeserializationHelper.readElementFromXMLReader(xmlr,
                            Constants.AnalyticsConstants.VERSION_ELEMENT));
                }
                else if (name.equals(Constants.AnalyticsConstants.DELETE_ELEMENT)) {
                    tempParseString = DeserializationHelper.readElementFromXMLReader(xmlr,
                            Constants.AnalyticsConstants.DELETE_ELEMENT);
                    if (Boolean.parseBoolean(tempParseString)) {
                        logging.getLogOperationTypes().add(LoggingOperations.DELETE);
                    }
                }
                else if (name.equals(Constants.AnalyticsConstants.READ_ELEMENT)) {
                    tempParseString = DeserializationHelper.readElementFromXMLReader(xmlr,
                            Constants.AnalyticsConstants.READ_ELEMENT);
                    if (Boolean.parseBoolean(tempParseString)) {
                        logging.getLogOperationTypes().add(LoggingOperations.READ);
                    }
                }
                else if (name.equals(Constants.AnalyticsConstants.WRITE_ELEMENT)) {
                    tempParseString = DeserializationHelper.readElementFromXMLReader(xmlr,
                            Constants.AnalyticsConstants.WRITE_ELEMENT);
                    if (Boolean.parseBoolean(tempParseString)) {
                        logging.getLogOperationTypes().add(LoggingOperations.WRITE);
                    }
                }
                else if (name.equals(Constants.AnalyticsConstants.RETENTION_POLICY_ELEMENT)) {
                    logging.setRetentionIntervalInDays(readRetentionPolicy(xmlr));
                }
                else {
                    throw new StorageException(StorageErrorCodeStrings.INVALID_XML_DOCUMENT,
                            SR.INVALID_RESPONSE_RECEIVED, Constants.HeaderConstants.HTTP_UNUSED_306, null, null);
                }
            }
            else {
                xmlr.require(XMLStreamConstants.END_ELEMENT, null, Constants.AnalyticsConstants.LOGGING_ELEMENT);
                break;
            }
        }

        return logging;
    }

    /**
     * Returns the a populated MetricsProperties object from the xml reader. XmlStreamReader must be at start of the
     * MetricsProperties element.
     * 
     * @param xmlr
     *            the XMLStreamReader to read from
     * @param metricsName
     *            the type of metrics being parsed (Hour or Minute)
     * @return the populated MetricsProperties object
     * @throws XMLStreamException
     *             if the xml is invalid.
     * @throws StorageException
     *             if unexpected xml is found.
     */
    private static MetricsProperties readMetricsProperties(String metricsName, final XMLStreamReader xmlr)
            throws XMLStreamException, StorageException {
        xmlr.require(XMLStreamConstants.START_ELEMENT, null, metricsName);

        int eventType = xmlr.getEventType();
        String tempParseString = null;
        MetricsProperties metrics = new MetricsProperties();

        while (xmlr.hasNext()) {
            eventType = xmlr.next();

            if (eventType == XMLStreamConstants.START_ELEMENT) {
                final String name = xmlr.getName().toString();
                if (name.equals(Constants.AnalyticsConstants.VERSION_ELEMENT)) {
                    metrics.setVersion(DeserializationHelper.readElementFromXMLReader(xmlr,
                            Constants.AnalyticsConstants.VERSION_ELEMENT));
                }
                else if (name.equals(Constants.AnalyticsConstants.ENABLED_ELEMENT)) {
                    tempParseString = DeserializationHelper.readElementFromXMLReader(xmlr,
                            Constants.AnalyticsConstants.ENABLED_ELEMENT);

                    if (Boolean.parseBoolean(tempParseString)) {
                        metrics.setMetricsLevel(metrics.getMetricsLevel() != MetricsLevel.SERVICE_AND_API ? MetricsLevel.SERVICE
                                : MetricsLevel.SERVICE_AND_API);
                    }
                    else {
                        metrics.setMetricsLevel(MetricsLevel.DISABLED);
                    }
                }
                else if (name.equals(Constants.AnalyticsConstants.INCLUDE_APIS_ELEMENT)) {
                    tempParseString = DeserializationHelper.readElementFromXMLReader(xmlr,
                            Constants.AnalyticsConstants.INCLUDE_APIS_ELEMENT);
                    if (Boolean.parseBoolean(tempParseString) && metrics.getMetricsLevel() != MetricsLevel.DISABLED) {
                        metrics.setMetricsLevel(MetricsLevel.SERVICE_AND_API);
                    }
                }
                else if (name.equals(Constants.AnalyticsConstants.RETENTION_POLICY_ELEMENT)) {
                    metrics.setRetentionIntervalInDays(readRetentionPolicy(xmlr));
                }
                else {
                    throw new StorageException(StorageErrorCodeStrings.INVALID_XML_DOCUMENT,
                            SR.INVALID_RESPONSE_RECEIVED, Constants.HeaderConstants.HTTP_UNUSED_306, null, null);
                }
            }
            else {
                xmlr.require(XMLStreamConstants.END_ELEMENT, null, metricsName);
                break;
            }
        }

        return metrics;
    }

    /**
     * Parses the retention policy from the xml reader. XmlStreamReader must be at start of retention policy element.
     * 
     * @param xmlr
     *            the XMLStreamReader to read from
     * @return a nullable Integer representing the retention policy
     * @throws XMLStreamException
     *             if the xml is invalid.
     * @throws StorageException
     *             if unexpected xml is found.
     */
    private static Integer readRetentionPolicy(final XMLStreamReader xmlr) throws XMLStreamException, StorageException {
        xmlr.require(XMLStreamConstants.START_ELEMENT, null, Constants.AnalyticsConstants.RETENTION_POLICY_ELEMENT);

        Integer retVal = -1;
        int eventType = xmlr.getEventType();
        String tempParseString = null;

        while (xmlr.hasNext()) {
            eventType = xmlr.next();

            if (eventType == XMLStreamConstants.START_ELEMENT) {
                final String name = xmlr.getName().toString();

                if (name.equals(Constants.AnalyticsConstants.DAYS_ELEMENT)) {
                    tempParseString = DeserializationHelper.readElementFromXMLReader(xmlr,
                            Constants.AnalyticsConstants.DAYS_ELEMENT);

                    // if we have already detected that the retention policy is disabled dont return a value;
                    if (retVal != null) {
                        retVal = Integer.parseInt(tempParseString);
                    }
                }
                else if (name.equals(Constants.AnalyticsConstants.ENABLED_ELEMENT)) {
                    tempParseString = DeserializationHelper.readElementFromXMLReader(xmlr,
                            Constants.AnalyticsConstants.ENABLED_ELEMENT);

                    if (!Boolean.parseBoolean(tempParseString)) {
                        retVal = null;
                    }
                }
                else {
                    throw new StorageException(StorageErrorCodeStrings.INVALID_XML_DOCUMENT,
                            SR.INVALID_RESPONSE_RECEIVED, Constants.HeaderConstants.HTTP_UNUSED_306, null, null);
                }
            }
            else {
                xmlr.require(XMLStreamConstants.END_ELEMENT, null,
                        Constants.AnalyticsConstants.RETENTION_POLICY_ELEMENT);
                break;
            }
        }

        return retVal;
    }

    /**
     * Returns the a populated CorsProperties object from the xml reader. XmlStreamReader must be at start of the
     * CorsProperties element.
     * 
     * @param xmlr
     *            the XMLStreamReader to read from
     * @return the populated CorsProperties object
     * @throws XMLStreamException
     *             if the xml is invalid.
     * @throws StorageException
     *             if unexpected xml is found.
     */
    private static CorsProperties readCorsProperties(final XMLStreamReader xmlr) throws XMLStreamException,
            StorageException {

        xmlr.require(XMLStreamConstants.START_ELEMENT, null, Constants.AnalyticsConstants.CORS_ELEMENT);

        CorsProperties corsProperties = new CorsProperties();

        int eventType = xmlr.getEventType();
        String tempParseString = null;

        while (xmlr.hasNext()) {
            eventType = xmlr.next();

            if (eventType == XMLStreamConstants.START_ELEMENT) {
                if (xmlr.getName().toString().equals(Constants.AnalyticsConstants.CORS_RULE_ELEMENT)) {
                    CorsRule rule = new CorsRule();

                    while (xmlr.hasNext()) {
                        eventType = xmlr.next();

                        if (eventType == XMLStreamConstants.START_ELEMENT) {
                            final String name = xmlr.getName().toString();
                            if (name.equals(Constants.AnalyticsConstants.ALLOWED_ORIGINS_ELEMENT)) {
                                tempParseString = DeserializationHelper.readElementFromXMLReader(xmlr,
                                        Constants.AnalyticsConstants.ALLOWED_ORIGINS_ELEMENT);
                                if (tempParseString != null) {
                                    rule.setAllowedOrigins(splitToList(tempParseString, ","));
                                }
                            }
                            else if (name.equals(Constants.AnalyticsConstants.ALLOWED_METHODS_ELEMENT)) {
                                tempParseString = DeserializationHelper.readElementFromXMLReader(xmlr,
                                        Constants.AnalyticsConstants.ALLOWED_METHODS_ELEMENT);
                                if (tempParseString != null) {
                                    rule.setAllowedMethods(splitToEnumSet(tempParseString, ","));
                                }
                            }
                            else if (name.equals(Constants.AnalyticsConstants.EXPOSED_HEADERS_ELEMENT)) {
                                tempParseString = DeserializationHelper.readElementFromXMLReader(xmlr,
                                        Constants.AnalyticsConstants.EXPOSED_HEADERS_ELEMENT);
                                if (tempParseString != null) {
                                    rule.setExposedHeaders(splitToList(tempParseString, ","));
                                }
                            }
                            else if (name.equals(Constants.AnalyticsConstants.ALLOWED_HEADERS_ELEMENT)) {
                                tempParseString = DeserializationHelper.readElementFromXMLReader(xmlr,
                                        Constants.AnalyticsConstants.ALLOWED_HEADERS_ELEMENT);
                                if (tempParseString != null) {
                                    rule.setAllowedHeaders(splitToList(tempParseString, ","));
                                }
                            }
                            else if (name.equals(Constants.AnalyticsConstants.MAX_AGE_IN_SECONDS_ELEMENT)) {
                                tempParseString = DeserializationHelper.readElementFromXMLReader(xmlr,
                                        Constants.AnalyticsConstants.MAX_AGE_IN_SECONDS_ELEMENT);
                                rule.setMaxAgeInSeconds(Integer.parseInt(tempParseString));
                            }
                            else {
                                throw new StorageException(StorageErrorCodeStrings.INVALID_XML_DOCUMENT,
                                        SR.INVALID_RESPONSE_RECEIVED, Constants.HeaderConstants.HTTP_UNUSED_306, null,
                                        null);
                            }
                        }
                        else {
                            xmlr.require(XMLStreamConstants.END_ELEMENT, null,
                                    Constants.AnalyticsConstants.CORS_RULE_ELEMENT);
                            corsProperties.getCorsRules().add(rule);
                            break;
                        }
                    }
                }
                else {
                    throw new StorageException(StorageErrorCodeStrings.INVALID_XML_DOCUMENT,
                            SR.INVALID_RESPONSE_RECEIVED, Constants.HeaderConstants.HTTP_UNUSED_306, null, null);
                }
            }
            else {
                xmlr.require(XMLStreamConstants.END_ELEMENT, null, Constants.AnalyticsConstants.CORS_ELEMENT);
                break;
            }
        }

        return corsProperties;
    }

    /**
     * Split a delimiter separated string into an ArrayList
     * 
     * @param str
     *            the string to split
     * @param delimiter
     *            the character to split on
     * @return the list of strings
     */
    private static List<String> splitToList(String str, String delimiter) {
        ArrayList<String> list = new ArrayList<String>();
        StringTokenizer st = new StringTokenizer(str, delimiter);
        while (st.hasMoreElements()) {
            list.add(st.nextToken());
        }
        return list;
    }

    /**
     * Split a delimiter separated string into a EnumSet of CorsHttpMethods
     * 
     * @param str
     *            the string to split
     * @param delimiter
     *            the character to split on
     * @return the set of CorsHttpMethods
     */
    private static EnumSet<CorsHttpMethods> splitToEnumSet(String str, String delimiter) throws StorageException {
        EnumSet<CorsHttpMethods> set = EnumSet.noneOf(CorsHttpMethods.class);
        StringTokenizer st = new StringTokenizer(str, delimiter);
        while (st.hasMoreElements()) {
            set.add(CorsHttpMethods.valueOf(st.nextToken()));
        }
        return set;
    }
}
