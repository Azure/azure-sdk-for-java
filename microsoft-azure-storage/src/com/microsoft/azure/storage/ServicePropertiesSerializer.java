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
package com.microsoft.azure.storage;

import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.microsoft.azure.storage.core.SR;
import com.microsoft.azure.storage.core.Utility;

/**
 * RESERVED FOR INTERNAL USE. A class used to serialize ServiceProperties to a byte array.
 */
final class ServicePropertiesSerializer {

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
    public static byte[] serializeToByteArray(final ServiceProperties properties) throws XMLStreamException,
            StorageException {
        final StringWriter outWriter = new StringWriter();
        final XMLStreamWriter xmlw = Utility.createXMLStreamWriter(outWriter);

        // default is UTF8
        xmlw.writeStartDocument();
        xmlw.writeStartElement(Constants.AnalyticsConstants.STORAGE_SERVICE_PROPERTIES_ELEMENT);

        if (properties.getLogging() != null) {
            // Logging Properties
            writeLoggingProperties(xmlw, properties.getLogging());
        }

        if (properties.getHourMetrics() != null) {
            // Hour Metrics
            writeMetricsProperties(xmlw, properties.getHourMetrics(), Constants.AnalyticsConstants.HOUR_METRICS_ELEMENT);
        }

        if (properties.getMinuteMetrics() != null) {
            // Minute Metrics
            writeMetricsProperties(xmlw, properties.getMinuteMetrics(),
                    Constants.AnalyticsConstants.MINUTE_METRICS_ELEMENT);
        }

        if (properties.getCors() != null) {
            // CORS Properties
            writeCorsProperties(xmlw, properties.getCors());
        }

        // Default Service Version
        if (properties.getDefaultServiceVersion() != null) {
            xmlw.writeStartElement(Constants.AnalyticsConstants.DEFAULT_SERVICE_VERSION);
            xmlw.writeCharacters(properties.getDefaultServiceVersion());
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
}
