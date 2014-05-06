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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Stack;
import java.util.StringTokenizer;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.microsoft.azure.storage.core.SR;
import com.microsoft.azure.storage.core.Utility;

/**
 * RESERVED FOR INTERNAL USE. A class used to deserialize a service properties.
 */
final class ServicePropertiesHandler extends DefaultHandler {

    private final Stack<String> elementStack = new Stack<String>();
    private StringBuilder bld = new StringBuilder();

    private final ServiceProperties props = new ServiceProperties();

    private CorsRule rule = new CorsRule();
    private boolean retentionPolicyEnabled;
    private int retentionPolicyDays;

    /**
     * Parses the {@link ServiceProperties} from the given XML stream.
     * 
     * @param stream
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    public static ServiceProperties readServicePropertiesFromStream(final InputStream stream) throws SAXException,
            IOException, ParserConfigurationException {
        SAXParser saxParser = Utility.getSAXParser();
        ServicePropertiesHandler handler = new ServicePropertiesHandler();
        saxParser.parse(stream, handler);

        return handler.props;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        this.elementStack.push(localName);

        if (Constants.AnalyticsConstants.CORS_RULE_ELEMENT.equals(localName)) {
            this.rule = new CorsRule();
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        String currentNode = this.elementStack.pop();

        // if the node popped from the stack and the localName don't match, the xml document is improperly formatted
        if (!localName.equals(currentNode)) {
            throw new SAXException(SR.INVALID_RESPONSE_RECEIVED);
        }

        String parentNode = null;
        if (!this.elementStack.isEmpty()) {
            parentNode = this.elementStack.peek();
        }

        String value = this.bld.toString();
        if (value.isEmpty()) {
            value = null;
        }

        if (this.retentionPolicyEnabled && Constants.AnalyticsConstants.RETENTION_POLICY_ELEMENT.equals(currentNode)) {
            if (Constants.AnalyticsConstants.LOGGING_ELEMENT.equals(parentNode)) {
                this.props.getLogging().setRetentionIntervalInDays(this.retentionPolicyDays);
            }
            else if (Constants.AnalyticsConstants.HOUR_METRICS_ELEMENT.equals(parentNode)) {
                this.props.getHourMetrics().setRetentionIntervalInDays(this.retentionPolicyDays);
            }
            else if (Constants.AnalyticsConstants.MINUTE_METRICS_ELEMENT.equals(parentNode)) {
                this.props.getMinuteMetrics().setRetentionIntervalInDays(this.retentionPolicyDays);
            }
        }
        else if (Constants.AnalyticsConstants.CORS_RULE_ELEMENT.equals(currentNode)) {
            this.props.getCors().getCorsRules().add(this.rule);
        }
        else if (Constants.AnalyticsConstants.RETENTION_POLICY_ELEMENT.equals(parentNode)) {
            if (Constants.AnalyticsConstants.DAYS_ELEMENT.equals(currentNode)) {
                this.retentionPolicyDays = Integer.parseInt(value);
            }
            else if (Constants.AnalyticsConstants.ENABLED_ELEMENT.equals(currentNode)) {
                this.retentionPolicyEnabled = Boolean.parseBoolean(value);
            }
        }
        else if (Constants.AnalyticsConstants.LOGGING_ELEMENT.equals(parentNode)) {
            if (Constants.AnalyticsConstants.VERSION_ELEMENT.equals(currentNode)) {
                this.props.getLogging().setVersion(value);
            }
            else if (Constants.AnalyticsConstants.DELETE_ELEMENT.equals(currentNode)) {
                if (Boolean.parseBoolean(value)) {
                    this.props.getLogging().getLogOperationTypes().add(LoggingOperations.DELETE);
                }
            }
            else if (Constants.AnalyticsConstants.READ_ELEMENT.equals(currentNode)) {
                if (Boolean.parseBoolean(value)) {
                    this.props.getLogging().getLogOperationTypes().add(LoggingOperations.READ);
                }
            }
            else if (Constants.AnalyticsConstants.WRITE_ELEMENT.equals(currentNode)) {
                if (Boolean.parseBoolean(value)) {
                    this.props.getLogging().getLogOperationTypes().add(LoggingOperations.WRITE);
                }
            }
        }
        else if (Constants.AnalyticsConstants.HOUR_METRICS_ELEMENT.equals(parentNode)) {
            MetricsProperties metrics = this.props.getHourMetrics();
            if (Constants.AnalyticsConstants.VERSION_ELEMENT.equals(currentNode)) {
                metrics.setVersion(value);
            }
            else if (Constants.AnalyticsConstants.ENABLED_ELEMENT.equals(currentNode)) {
                if (Boolean.parseBoolean(value)) {
                    metrics.setMetricsLevel(metrics.getMetricsLevel() != MetricsLevel.SERVICE_AND_API ? MetricsLevel.SERVICE
                            : MetricsLevel.SERVICE_AND_API);
                }
            }
            else if (Constants.AnalyticsConstants.INCLUDE_APIS_ELEMENT.equals(currentNode)) {
                if (Boolean.parseBoolean(value) && metrics.getMetricsLevel() != MetricsLevel.DISABLED) {
                    metrics.setMetricsLevel(MetricsLevel.SERVICE_AND_API);
                }
            }
        }
        else if (Constants.AnalyticsConstants.MINUTE_METRICS_ELEMENT.equals(parentNode)) {
            MetricsProperties metrics = this.props.getMinuteMetrics();
            if (Constants.AnalyticsConstants.VERSION_ELEMENT.equals(currentNode)) {
                metrics.setVersion(value);
            }
            else if (Constants.AnalyticsConstants.ENABLED_ELEMENT.equals(currentNode)) {
                if (Boolean.parseBoolean(value)) {
                    metrics.setMetricsLevel(metrics.getMetricsLevel() != MetricsLevel.SERVICE_AND_API ? MetricsLevel.SERVICE
                            : MetricsLevel.SERVICE_AND_API);
                }
            }
            else if (Constants.AnalyticsConstants.INCLUDE_APIS_ELEMENT.equals(currentNode)) {
                if (Boolean.parseBoolean(value) && metrics.getMetricsLevel() != MetricsLevel.DISABLED) {
                    metrics.setMetricsLevel(MetricsLevel.SERVICE_AND_API);
                }
            }
        }
        else if (Constants.AnalyticsConstants.DEFAULT_SERVICE_VERSION.equals(currentNode)) {
            this.props.setDefaultServiceVersion(value);
        }
        else if (Constants.AnalyticsConstants.CORS_RULE_ELEMENT.equals(parentNode)) {
            if (Constants.AnalyticsConstants.ALLOWED_ORIGINS_ELEMENT.equals(currentNode)) {
                if (value != null) {
                    this.rule.setAllowedOrigins(splitToList(value, ","));
                }
            }
            else if (Constants.AnalyticsConstants.ALLOWED_METHODS_ELEMENT.equals(currentNode)) {
                if (value != null) {
                    this.rule.setAllowedMethods(splitToEnumSet(value, ","));
                }
            }
            else if (Constants.AnalyticsConstants.EXPOSED_HEADERS_ELEMENT.equals(currentNode)) {
                if (value != null) {
                    this.rule.setExposedHeaders(splitToList(value, ","));
                }
            }
            else if (Constants.AnalyticsConstants.ALLOWED_HEADERS_ELEMENT.equals(currentNode)) {
                if (value != null) {
                    this.rule.setAllowedHeaders(splitToList(value, ","));
                }
            }
            else if (Constants.AnalyticsConstants.MAX_AGE_IN_SECONDS_ELEMENT.equals(currentNode)) {
                this.rule.setMaxAgeInSeconds(Integer.parseInt(value));
            }
        }

        this.bld = new StringBuilder();
    }

    @Override
    public void characters(char ch[], int start, int length) throws SAXException {
        this.bld.append(ch, start, length);
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
    private static EnumSet<CorsHttpMethods> splitToEnumSet(String str, String delimiter) {
        EnumSet<CorsHttpMethods> set = EnumSet.noneOf(CorsHttpMethods.class);
        StringTokenizer st = new StringTokenizer(str, delimiter);
        while (st.hasMoreElements()) {
            set.add(CorsHttpMethods.valueOf(st.nextToken()));
        }
        return set;
    }
}
