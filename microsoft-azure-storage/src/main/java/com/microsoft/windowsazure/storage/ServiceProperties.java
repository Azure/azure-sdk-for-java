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
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.microsoft.windowsazure.storage.core.SR;
import com.microsoft.windowsazure.storage.core.Utility;

/**
 * Represents the analytics properties for the service.
 */
public final class ServiceProperties {

    /**
     * The service logging properties. This may not be null.
     */
    private LoggingProperties logging;

    /**
     * The service hour metrics properties.
     */
    private MetricsProperties hourMetrics;

    /**
     * The service minute metrics properties.
     */
    private MetricsProperties minuteMetrics;

    /**
     * The Cross Origin Resource Sharing (CORS) properties.
     */
    private CorsProperties cors;

    /**
     * The default service version for the blob service, or null if no default is specified. This must be null for queue
     * and table service properties.
     */
    private String defaultServiceVersion;

    /**
     * Initializes a new instances of the ServiceProperties class.
     */
    public ServiceProperties() {
        this.setLogging(new LoggingProperties());
        this.setHourMetrics(new MetricsProperties());
        this.setMinuteMetrics(new MetricsProperties());
        this.setCors(new CorsProperties());
    }

    /**
     * Get the logging properties
     * 
     * @return the logging
     */
    public LoggingProperties getLogging() {
        return this.logging;
    }

    /**
     * Set the logging properties
     * 
     * @param logging
     */
    public void setLogging(final LoggingProperties logging) {
        this.logging = logging;
    }

    /**
     * Get the metrics properties
     * 
     * @deprecated use {@link #getHourMetrics()} instead.
     * @return the hour metrics
     */
    @Deprecated
    public MetricsProperties getMetrics() {
        return this.hourMetrics;
    }

    /**
     * Set the metrics properties
     * 
     * @deprecated use {@link #setHourMetrics()} instead.
     * @param metrics
     */
    @Deprecated
    public void setMetrics(final MetricsProperties metrics) {
        this.hourMetrics = metrics;
    }

    /**
     * Get the hour metrics properties
     * 
     * @return the hour metrics
     */
    public MetricsProperties getHourMetrics() {
        return this.hourMetrics;
    }

    /**
     * Set the hour metrics properties
     * 
     * @param metrics
     */
    public void setHourMetrics(final MetricsProperties metrics) {
        this.hourMetrics = metrics;
    }

    /**
     * Get the minute metrics properties
     * 
     * @return the minute metrics
     */
    public MetricsProperties getMinuteMetrics() {
        return this.minuteMetrics;
    }

    /**
     * Set the minute metrics properties
     * 
     * @param metrics
     */
    public void setMinuteMetrics(final MetricsProperties metrics) {
        this.minuteMetrics = metrics;
    }

    /**
     * Get the CORS properties
     * 
     * @return the CORS properties
     */
    public CorsProperties getCors() {
        return this.cors;
    }

    /**
     * Set the CORS properties
     * 
     * @param CORS
     */
    public void setCors(final CorsProperties cors) {
        this.cors = cors;
    }

    /**
     * Get default service version
     * 
     * @return the defaultServiceVersion
     */
    public String getDefaultServiceVersion() {
        return this.defaultServiceVersion;
    }

    /**
     * Set default service version
     * 
     * @param defaultServiceVersion
     */
    public void setDefaultServiceVersion(final String defaultServiceVersion) {
        this.defaultServiceVersion = defaultServiceVersion;
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
    public static ServiceProperties readServicePropertiesFromStream(final InputStream inStream,
            final OperationContext opContext) throws XMLStreamException, StorageException {
        final ServiceProperties retProps = new ServiceProperties();

        final XMLStreamReader xmlr = Utility.createXMLStreamReaderFromStream(inStream);

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
                    retProps.setDefaultServiceVersion(Utility.readElementFromXMLReader(xmlr,
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
     * Writes the contents of the ServiceProperties to the stream in xml format.
     * 
     * @param opContext
     *            a tracking object for the request
     * @return a byte array of the content to write to the stream.
     * @throws XMLStreamException
     *             if there is an error writing content to the stream.
     * @throws StorageException
     */
    public byte[] serializeToByteArray(final OperationContext opContext) throws XMLStreamException, StorageException {
        final StringWriter outWriter = new StringWriter();
        final XMLOutputFactory xmlOutFactoryInst = XMLOutputFactory.newInstance();
        final XMLStreamWriter xmlw = xmlOutFactoryInst.createXMLStreamWriter(outWriter);

        // default is UTF8
        xmlw.writeStartDocument();
        xmlw.writeStartElement(Constants.AnalyticsConstants.STORAGE_SERVICE_PROPERTIES_ELEMENT);

        if (this.logging != null) {
            // Logging Properties
            writeLoggingProperties(xmlw, this.logging);
        }

        if (this.hourMetrics != null) {
            // Hour Metrics
            writeMetricsProperties(xmlw, this.hourMetrics, Constants.AnalyticsConstants.HOUR_METRICS_ELEMENT);
        }

        if (this.minuteMetrics != null) {
            // Minute Metrics
            writeMetricsProperties(xmlw, this.minuteMetrics, Constants.AnalyticsConstants.MINUTE_METRICS_ELEMENT);
        }

        if (this.cors != null) {
            // CORS Properties
            writeCorsProperties(xmlw, this.cors);
        }

        // Default Service Version
        if (this.getDefaultServiceVersion() != null) {
            xmlw.writeStartElement(Constants.AnalyticsConstants.DEFAULT_SERVICE_VERSION);
            xmlw.writeCharacters(this.getDefaultServiceVersion());
            xmlw.writeEndElement();
        }

        // end StorageServiceProperties
        xmlw.writeEndElement();

        // end doc
        xmlw.writeEndDocument();

        try {
            return outWriter.toString().getBytes(Constants.UTF8_CHARSET);
        }
        catch (final UnsupportedEncodingException e) {
            throw Utility.generateNewUnexpectedStorageException(e);
        }
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
                    logging.setVersion(Utility.readElementFromXMLReader(xmlr,
                            Constants.AnalyticsConstants.VERSION_ELEMENT));
                }
                else if (name.equals(Constants.AnalyticsConstants.DELETE_ELEMENT)) {
                    tempParseString = Utility.readElementFromXMLReader(xmlr,
                            Constants.AnalyticsConstants.DELETE_ELEMENT);
                    if (Boolean.parseBoolean(tempParseString)) {
                        logging.getLogOperationTypes().add(LoggingOperations.DELETE);
                    }
                }
                else if (name.equals(Constants.AnalyticsConstants.READ_ELEMENT)) {
                    tempParseString = Utility.readElementFromXMLReader(xmlr, Constants.AnalyticsConstants.READ_ELEMENT);
                    if (Boolean.parseBoolean(tempParseString)) {
                        logging.getLogOperationTypes().add(LoggingOperations.READ);
                    }
                }
                else if (name.equals(Constants.AnalyticsConstants.WRITE_ELEMENT)) {
                    tempParseString = Utility
                            .readElementFromXMLReader(xmlr, Constants.AnalyticsConstants.WRITE_ELEMENT);
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
                    metrics.setVersion(Utility.readElementFromXMLReader(xmlr,
                            Constants.AnalyticsConstants.VERSION_ELEMENT));
                }
                else if (name.equals(Constants.AnalyticsConstants.ENABLED_ELEMENT)) {
                    tempParseString = Utility.readElementFromXMLReader(xmlr,
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
                    tempParseString = Utility.readElementFromXMLReader(xmlr,
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
                    tempParseString = Utility.readElementFromXMLReader(xmlr, Constants.AnalyticsConstants.DAYS_ELEMENT);

                    // if we have already detected that the retention policy is disabled dont return a value;
                    if (retVal != null) {
                        retVal = Integer.parseInt(tempParseString);
                    }
                }
                else if (name.equals(Constants.AnalyticsConstants.ENABLED_ELEMENT)) {
                    tempParseString = Utility.readElementFromXMLReader(xmlr,
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
                                tempParseString = Utility.readElementFromXMLReader(xmlr,
                                        Constants.AnalyticsConstants.ALLOWED_ORIGINS_ELEMENT);
                                if (tempParseString != null) {
                                    rule.setAllowedOrigins(splitToList(tempParseString, ","));
                                }
                            }
                            else if (name.equals(Constants.AnalyticsConstants.ALLOWED_METHODS_ELEMENT)) {
                                tempParseString = Utility.readElementFromXMLReader(xmlr,
                                        Constants.AnalyticsConstants.ALLOWED_METHODS_ELEMENT);
                                if (tempParseString != null) {
                                    rule.setAllowedMethods(splitToEnumSet(tempParseString, ","));
                                }
                            }
                            else if (name.equals(Constants.AnalyticsConstants.EXPOSED_HEADERS_ELEMENT)) {
                                tempParseString = Utility.readElementFromXMLReader(xmlr,
                                        Constants.AnalyticsConstants.EXPOSED_HEADERS_ELEMENT);
                                if (tempParseString != null) {
                                    rule.setExposedHeaders(splitToList(tempParseString, ","));
                                }
                            }
                            else if (name.equals(Constants.AnalyticsConstants.ALLOWED_HEADERS_ELEMENT)) {
                                tempParseString = Utility.readElementFromXMLReader(xmlr,
                                        Constants.AnalyticsConstants.ALLOWED_HEADERS_ELEMENT);
                                if (tempParseString != null) {
                                    rule.setAllowedHeaders(splitToList(tempParseString, ","));
                                }
                            }
                            else if (name.equals(Constants.AnalyticsConstants.MAX_AGE_IN_SECONDS_ELEMENT)) {
                                tempParseString = Utility.readElementFromXMLReader(xmlr,
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
     * Writes the retention policy to the XMLStreamWriter.
     * 
     * @param xmlw
     *            the XMLStreamWriter to write to.
     * @param val
     *            the nullable Integer indicating if the retention policy is enabled, and how long
     * @throws XMLStreamException
     */
    private static void writeRetentionPolicy(final XMLStreamWriter xmlw, final Integer val) throws XMLStreamException {
        xmlw.writeStartElement(Constants.AnalyticsConstants.RETENTION_POLICY_ELEMENT);

        // Enabled
        xmlw.writeStartElement(Constants.AnalyticsConstants.ENABLED_ELEMENT);
        xmlw.writeCharacters(val != null ? Constants.TRUE : Constants.FALSE);
        xmlw.writeEndElement();

        if (val != null) {
            // Days
            xmlw.writeStartElement(Constants.AnalyticsConstants.DAYS_ELEMENT);
            xmlw.writeCharacters(val.toString());
            xmlw.writeEndElement();
        }

        // End Retention Policy
        xmlw.writeEndElement();
    }

    /**
     * Writes the given CORS properties to the XMLStreamWriter.
     * 
     * @param xmlw
     *            the XMLStreamWriter to write to.
     * @param cors
     *            the CORS Properties to be written.
     * @throws XMLStreamException
     */
    private static void writeCorsProperties(final XMLStreamWriter xmlw, final CorsProperties cors)
            throws XMLStreamException {
        Utility.assertNotNull("CorsRules", cors.getCorsRules());

        // CORS
        xmlw.writeStartElement(Constants.AnalyticsConstants.CORS_ELEMENT);

        for (CorsRule rule : cors.getCorsRules()) {
            if (rule.getAllowedOrigins().isEmpty() || rule.getAllowedMethods().isEmpty()
                    || rule.getMaxAgeInSeconds() < 0) {
                throw new IllegalArgumentException(SR.INVALID_CORS_RULE);
            }

            xmlw.writeStartElement(Constants.AnalyticsConstants.CORS_RULE_ELEMENT);

            xmlw.writeStartElement(Constants.AnalyticsConstants.ALLOWED_ORIGINS_ELEMENT);
            xmlw.writeCharacters(joinToString(rule.getAllowedOrigins(), ","));
            xmlw.writeEndElement();

            xmlw.writeStartElement(Constants.AnalyticsConstants.ALLOWED_METHODS_ELEMENT);
            xmlw.writeCharacters(joinToString(rule.getAllowedMethods(), ","));
            xmlw.writeEndElement();

            xmlw.writeStartElement(Constants.AnalyticsConstants.EXPOSED_HEADERS_ELEMENT);
            xmlw.writeCharacters(joinToString(rule.getExposedHeaders(), ","));
            xmlw.writeEndElement();

            xmlw.writeStartElement(Constants.AnalyticsConstants.ALLOWED_HEADERS_ELEMENT);
            xmlw.writeCharacters(joinToString(rule.getAllowedHeaders(), ","));
            xmlw.writeEndElement();

            xmlw.writeStartElement(Constants.AnalyticsConstants.MAX_AGE_IN_SECONDS_ELEMENT);
            xmlw.writeCharacters(Integer.toString(rule.getMaxAgeInSeconds()));
            xmlw.writeEndElement();

            xmlw.writeEndElement();
        }

        // end CORS
        xmlw.writeEndElement();
    }

    /**
     * Writes the given metrics properties to the XMLStreamWriter.
     * 
     * @param xmlw
     *            the XMLStreamWriter to write to.
     * @param metrics
     *            the metrics properties to be written.
     * @param metricsName
     *            the type of metrics properties to be written (Hour or Minute)
     * @throws XMLStreamException
     */
    private static void writeMetricsProperties(final XMLStreamWriter xmlw, final MetricsProperties metrics,
            final String metricsName) throws XMLStreamException {
        Utility.assertNotNull("metrics.Configuration", metrics.getMetricsLevel());

        // Metrics
        xmlw.writeStartElement(metricsName);

        // Version
        xmlw.writeStartElement(Constants.AnalyticsConstants.VERSION_ELEMENT);
        xmlw.writeCharacters(metrics.getVersion());
        xmlw.writeEndElement();

        // Enabled
        xmlw.writeStartElement(Constants.AnalyticsConstants.ENABLED_ELEMENT);
        xmlw.writeCharacters(metrics.getMetricsLevel() != MetricsLevel.DISABLED ? Constants.TRUE : Constants.FALSE);
        xmlw.writeEndElement();

        if (metrics.getMetricsLevel() != MetricsLevel.DISABLED) {
            // Include APIs
            xmlw.writeStartElement(Constants.AnalyticsConstants.INCLUDE_APIS_ELEMENT);
            xmlw.writeCharacters(metrics.getMetricsLevel() == MetricsLevel.SERVICE_AND_API ? Constants.TRUE
                    : Constants.FALSE);
            xmlw.writeEndElement();
        }

        // Retention Policy
        writeRetentionPolicy(xmlw, metrics.getRetentionIntervalInDays());

        // end Metrics
        xmlw.writeEndElement();
    }

    /**
     * Writes the given logging properties to the XMLStreamWriter.
     * 
     * @param xmlw
     *            the XMLStreamWriter to write to.
     * @param cors
     *            the logging properties to be written.
     * @throws XMLStreamException
     */
    private static void writeLoggingProperties(final XMLStreamWriter xmlw, final LoggingProperties logging)
            throws XMLStreamException {
        Utility.assertNotNull("logging.LogOperationTypes", logging.getLogOperationTypes());

        // Logging
        xmlw.writeStartElement(Constants.AnalyticsConstants.LOGGING_ELEMENT);

        // Version
        xmlw.writeStartElement(Constants.AnalyticsConstants.VERSION_ELEMENT);
        xmlw.writeCharacters(logging.getVersion());
        xmlw.writeEndElement();

        // Delete
        xmlw.writeStartElement(Constants.AnalyticsConstants.DELETE_ELEMENT);
        xmlw.writeCharacters(logging.getLogOperationTypes().contains(LoggingOperations.DELETE) ? Constants.TRUE
                : Constants.FALSE);
        xmlw.writeEndElement();

        // Read
        xmlw.writeStartElement(Constants.AnalyticsConstants.READ_ELEMENT);
        xmlw.writeCharacters(logging.getLogOperationTypes().contains(LoggingOperations.READ) ? Constants.TRUE
                : Constants.FALSE);
        xmlw.writeEndElement();

        // Write
        xmlw.writeStartElement(Constants.AnalyticsConstants.WRITE_ELEMENT);
        xmlw.writeCharacters(logging.getLogOperationTypes().contains(LoggingOperations.WRITE) ? Constants.TRUE
                : Constants.FALSE);
        xmlw.writeEndElement();

        // Retention Policy
        writeRetentionPolicy(xmlw, logging.getRetentionIntervalInDays());

        // end Logging
        xmlw.writeEndElement();
    }

    /**
     * Concatenate an Iterable<?> set of items with a delimiter between each
     * 
     * @param iterable
     *            the set of items to concatenate
     * @param delimiter
     *            the character to put between each item
     * @return the concatenated string
     */
    private static String joinToString(Iterable<?> iterable, String delimiter) {
        StringBuilder builder = new StringBuilder();
        Iterator<?> iter = iterable.iterator();
        while (iter.hasNext()) {
            builder.append(iter.next());
            if (iter.hasNext()) {
                builder.append(delimiter);
            }
        }
        return builder.toString();
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
