/**
 * Copyright 2011 Microsoft Corporation
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
package com.microsoft.windowsazure.services.core.storage;

import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.microsoft.windowsazure.services.core.storage.utils.Utility;

/**
 * Represents the analytics properties for the service.
 */
public final class ServiceProperties {
    /**
     * Populates the Logging Properties form the xml reader. XmlStreamReader must be at start of Logging element.
     * 
     * @param retProps
     *            the ServiceProperties to populate
     * @param xmlr
     *            the XMLStreamReader to read from
     * @throws XMLStreamException
     *             if the xml is invalid.
     * @throws StorageException
     *             if unexpected xml is found.
     */
    private static void readLoggingProperties(final ServiceProperties retProps, final XMLStreamReader xmlr)
            throws XMLStreamException, StorageException {
        xmlr.require(XMLStreamConstants.START_ELEMENT, null, Constants.AnalyticsConstants.LOGGING_ELEMENT);

        int eventType = xmlr.getEventType();
        String tempParseString = null;

        while (xmlr.hasNext()) {
            eventType = xmlr.next();

            if (eventType == XMLStreamConstants.START_ELEMENT) {
                final String name = xmlr.getName().toString();
                if (name.equals(Constants.AnalyticsConstants.VERSION_ELEMENT)) {
                    retProps.getLogging().setVersion(
                            Utility.readElementFromXMLReader(xmlr, Constants.AnalyticsConstants.VERSION_ELEMENT));
                }
                else if (name.equals(Constants.AnalyticsConstants.DELETE_ELEMENT)) {
                    tempParseString = Utility.readElementFromXMLReader(xmlr,
                            Constants.AnalyticsConstants.DELETE_ELEMENT);
                    if (Boolean.parseBoolean(tempParseString)) {
                        retProps.getLogging().getLogOperationTypes().add(LoggingOperations.DELETE);
                    }
                }
                else if (name.equals(Constants.AnalyticsConstants.READ_ELEMENT)) {
                    tempParseString = Utility.readElementFromXMLReader(xmlr, Constants.AnalyticsConstants.READ_ELEMENT);
                    if (Boolean.parseBoolean(tempParseString)) {
                        retProps.getLogging().getLogOperationTypes().add(LoggingOperations.READ);
                    }
                }
                else if (name.equals(Constants.AnalyticsConstants.WRITE_ELEMENT)) {
                    tempParseString = Utility
                            .readElementFromXMLReader(xmlr, Constants.AnalyticsConstants.WRITE_ELEMENT);
                    if (Boolean.parseBoolean(tempParseString)) {
                        retProps.getLogging().getLogOperationTypes().add(LoggingOperations.WRITE);
                    }
                }
                else if (name.equals(Constants.AnalyticsConstants.RETENTION_POLICY_ELEMENT)) {
                    retProps.getLogging().setRetentionIntervalInDays(readRetentionPolicy(xmlr));
                }
                else {
                    throw new StorageException(StorageErrorCodeStrings.INVALID_XML_DOCUMENT,
                            "The response received is invalid or improperly formatted.",
                            Constants.HeaderConstants.HTTP_UNUSED_306, null, null);
                }
            }
            else {
                xmlr.require(XMLStreamConstants.END_ELEMENT, null, Constants.AnalyticsConstants.LOGGING_ELEMENT);
                break;
            }
        }
    }

    /**
     * Populates the Metrics Properties form the xml reader. XmlStreamReader must be at start of Metrics element.
     * 
     * @param retProps
     *            the ServiceProperties to populate
     * @param xmlr
     *            the XMLStreamReader to read from
     * @throws XMLStreamException
     *             if the xml is invalid.
     * @throws StorageException
     *             if unexpected xml is found.
     */
    private static void readMetricsProperties(final ServiceProperties retProps, final XMLStreamReader xmlr)
            throws XMLStreamException, StorageException {
        xmlr.require(XMLStreamConstants.START_ELEMENT, null, Constants.AnalyticsConstants.METRICS_ELEMENT);

        int eventType = xmlr.getEventType();
        String tempParseString = null;
        retProps.getMetrics().setMetricsLevel(null);

        while (xmlr.hasNext()) {
            eventType = xmlr.next();

            if (eventType == XMLStreamConstants.START_ELEMENT) {
                final String name = xmlr.getName().toString();
                if (name.equals(Constants.AnalyticsConstants.VERSION_ELEMENT)) {
                    retProps.getMetrics().setVersion(
                            Utility.readElementFromXMLReader(xmlr, Constants.AnalyticsConstants.VERSION_ELEMENT));
                }
                else if (name.equals(Constants.AnalyticsConstants.ENABLED_ELEMENT)) {
                    tempParseString = Utility.readElementFromXMLReader(xmlr,
                            Constants.AnalyticsConstants.ENABLED_ELEMENT);

                    if (Boolean.parseBoolean(tempParseString)) {
                        retProps.getMetrics()
                                .setMetricsLevel(
                                        retProps.getMetrics().getMetricsLevel() != MetricsLevel.SERVICE_AND_API ? MetricsLevel.SERVICE
                                                : MetricsLevel.SERVICE_AND_API);
                    }
                    else {
                        retProps.getMetrics().setMetricsLevel(MetricsLevel.DISABLED);
                    }
                }
                else if (name.equals(Constants.AnalyticsConstants.INCLUDE_APIS_ELEMENT)) {
                    tempParseString = Utility.readElementFromXMLReader(xmlr,
                            Constants.AnalyticsConstants.INCLUDE_APIS_ELEMENT);
                    if (Boolean.parseBoolean(tempParseString)
                            && retProps.getMetrics().getMetricsLevel() != MetricsLevel.DISABLED) {
                        retProps.getMetrics().setMetricsLevel(MetricsLevel.SERVICE_AND_API);
                    }
                }
                else if (name.equals(Constants.AnalyticsConstants.RETENTION_POLICY_ELEMENT)) {
                    retProps.getMetrics().setRetentionIntervalInDays(readRetentionPolicy(xmlr));
                }
                else {
                    throw new StorageException(StorageErrorCodeStrings.INVALID_XML_DOCUMENT,
                            "The response received is invalid or improperly formatted.",
                            Constants.HeaderConstants.HTTP_UNUSED_306, null, null);
                }
            }
            else {
                xmlr.require(XMLStreamConstants.END_ELEMENT, null, Constants.AnalyticsConstants.METRICS_ELEMENT);
                break;
            }
        }
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
                            "The response received is invalid or improperly formatted.",
                            Constants.HeaderConstants.HTTP_UNUSED_306, null, null);
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

        // 1. get StorageServiceProperties Header
        eventType = xmlr.next();
        xmlr.require(XMLStreamConstants.START_ELEMENT, null,
                Constants.AnalyticsConstants.STORAGE_SERVICE_PROPERTIES_ELEMENT);

        // check if there are more events in the input stream
        while (xmlr.hasNext()) {
            eventType = xmlr.next();
            if (eventType == XMLStreamConstants.START_ELEMENT) {
                final String name = xmlr.getName().toString();
                if (name.equals(Constants.AnalyticsConstants.LOGGING_ELEMENT)) {
                    readLoggingProperties(retProps, xmlr);
                    xmlr.require(XMLStreamConstants.END_ELEMENT, null, Constants.AnalyticsConstants.LOGGING_ELEMENT);
                }
                else if (name.equals(Constants.AnalyticsConstants.METRICS_ELEMENT)) {
                    readMetricsProperties(retProps, xmlr);
                    xmlr.require(XMLStreamConstants.END_ELEMENT, null, Constants.AnalyticsConstants.METRICS_ELEMENT);
                }
                else if (name.equals(Constants.AnalyticsConstants.DEFAULT_SERVICE_VERSION)) {
                    retProps.setDefaultServiceVersion(Utility.readElementFromXMLReader(xmlr,
                            Constants.AnalyticsConstants.DEFAULT_SERVICE_VERSION));
                }
                else {
                    throw new StorageException(StorageErrorCodeStrings.INVALID_XML_DOCUMENT,
                            "The response received is invalid or improperly formatted.",
                            Constants.HeaderConstants.HTTP_UNUSED_306, null, null);
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
     * The service logging properties. This may not be null.
     */
    private LoggingProperties logging;

    /**
     * The service metrics properties.
     */
    private MetricsProperties metrics;

    /**
     * The default service version for the blob service, or null if no default is specified. This must be null for queue
     * and table service properties.
     */
    private String defaultServiceVersion;

    public ServiceProperties() {
        this.setLogging(new LoggingProperties());
        this.setMetrics(new MetricsProperties());
    }

    /**
     * @return the defaultServiceVersion
     */
    public String getDefaultServiceVersion() {
        return this.defaultServiceVersion;
    }

    /**
     * @return the logging
     */
    public LoggingProperties getLogging() {
        return this.logging;
    }

    /**
     * @return the metrics
     */
    public MetricsProperties getMetrics() {
        return this.metrics;
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
        Utility.assertNotNull("Logging", this.getLogging());
        Utility.assertNotNull("Logging.LogOperationTypes", this.getLogging().getLogOperationTypes());
        Utility.assertNotNull("Merics", this.getMetrics());
        Utility.assertNotNull("Merics.Configuration", this.getMetrics().getMetricsLevel());

        final StringWriter outWriter = new StringWriter();
        final XMLOutputFactory xmlOutFactoryInst = XMLOutputFactory.newInstance();
        final XMLStreamWriter xmlw = xmlOutFactoryInst.createXMLStreamWriter(outWriter);

        // default is UTF8
        xmlw.writeStartDocument();
        xmlw.writeStartElement(Constants.AnalyticsConstants.STORAGE_SERVICE_PROPERTIES_ELEMENT);

        // Logging
        xmlw.writeStartElement(Constants.AnalyticsConstants.LOGGING_ELEMENT);

        // Version
        xmlw.writeStartElement(Constants.AnalyticsConstants.VERSION_ELEMENT);
        xmlw.writeCharacters(this.getLogging().getVersion());
        xmlw.writeEndElement();

        // Delete
        xmlw.writeStartElement(Constants.AnalyticsConstants.DELETE_ELEMENT);
        xmlw.writeCharacters(this.getLogging().getLogOperationTypes().contains(LoggingOperations.DELETE) ? Constants.TRUE
                : Constants.FALSE);
        xmlw.writeEndElement();

        // Read
        xmlw.writeStartElement(Constants.AnalyticsConstants.READ_ELEMENT);
        xmlw.writeCharacters(this.getLogging().getLogOperationTypes().contains(LoggingOperations.READ) ? Constants.TRUE
                : Constants.FALSE);
        xmlw.writeEndElement();

        // Write
        xmlw.writeStartElement(Constants.AnalyticsConstants.WRITE_ELEMENT);
        xmlw.writeCharacters(this.getLogging().getLogOperationTypes().contains(LoggingOperations.WRITE) ? Constants.TRUE
                : Constants.FALSE);
        xmlw.writeEndElement();

        // Retention Policy
        writeRetentionPolicy(xmlw, this.getLogging().getRetentionIntervalInDays());

        // end Logging
        xmlw.writeEndElement();

        // Metrics
        xmlw.writeStartElement(Constants.AnalyticsConstants.METRICS_ELEMENT);

        // Version
        xmlw.writeStartElement(Constants.AnalyticsConstants.VERSION_ELEMENT);
        xmlw.writeCharacters(this.getMetrics().getVersion());
        xmlw.writeEndElement();

        // Enabled
        xmlw.writeStartElement(Constants.AnalyticsConstants.ENABLED_ELEMENT);
        xmlw.writeCharacters(this.getMetrics().getMetricsLevel() != MetricsLevel.DISABLED ? Constants.TRUE
                : Constants.FALSE);
        xmlw.writeEndElement();

        if (this.getMetrics().getMetricsLevel() != MetricsLevel.DISABLED) {
            // Include APIs
            xmlw.writeStartElement(Constants.AnalyticsConstants.INCLUDE_APIS_ELEMENT);
            xmlw.writeCharacters(this.getMetrics().getMetricsLevel() == MetricsLevel.SERVICE_AND_API ? Constants.TRUE
                    : Constants.FALSE);
            xmlw.writeEndElement();
        }

        // Retention Policy
        writeRetentionPolicy(xmlw, this.getMetrics().getRetentionIntervalInDays());

        // end Metrics
        xmlw.writeEndElement();

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
            return outWriter.toString().getBytes("UTF8");
        }
        catch (final UnsupportedEncodingException e) {
            throw Utility.generateNewUnexpectedStorageException(e);
        }
    }

    /**
     * @param defaultServiceVersion
     *            the defaultServiceVersion to set
     */
    public void setDefaultServiceVersion(final String defaultServiceVersion) {
        this.defaultServiceVersion = defaultServiceVersion;
    }

    /**
     * @param logging
     *            the logging to set
     */
    public void setLogging(final LoggingProperties logging) {
        this.logging = logging;
    }

    /**
     * @param metrics
     *            the metrics to set
     */
    public void setMetrics(final MetricsProperties metrics) {
        this.metrics = metrics;
    }
}
