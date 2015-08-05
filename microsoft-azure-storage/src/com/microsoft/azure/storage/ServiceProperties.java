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
     * Gets the logging properties.
     * 
     * @return A {@link LoggingProperties} object which represents the logging properties. 
     */
    public LoggingProperties getLogging() {
        return this.logging;
    }

    /**
     * Sets the logging properties.
     * 
     * @param logging
     *        A {@link LoggingProperties} object which represents the logging properties.
     */
    public void setLogging(final LoggingProperties logging) {
        this.logging = logging;
    }

    /**
     * Gets the hour metrics properties.
     * 
     * @return A {@link MetricsProperties} object which represents the hour metrics properties.
     */
    public MetricsProperties getHourMetrics() {
        return this.hourMetrics;
    }

    /**
     * Sets the hour metrics properties.
     * 
     * @param metrics
     *        A {@link MetricsProperties} object which represents the hour metrics properties.
     */
    public void setHourMetrics(final MetricsProperties metrics) {
        this.hourMetrics = metrics;
    }

    /**
     * Gets the minute metrics properties.
     * 
     * @return A {@link MetricsProperties} object which represents the minute metrics properties.
     */
    public MetricsProperties getMinuteMetrics() {
        return this.minuteMetrics;
    }

    /**
     * Sets the minute metrics properties.
     * 
     * @param metrics
     *        A {@link MetricsProperties} object which represents the minute metrics properties.
     */
    public void setMinuteMetrics(final MetricsProperties metrics) {
        this.minuteMetrics = metrics;
    }

    /**
     * Gets the Cross-Origin Resource Sharing (CORS) properties.
     * 
     * @return A {@link CorsProperties} object which represents the CORS properties.
     */
    public CorsProperties getCors() {
        return this.cors;
    }

    /**
     * Sets the Cross-Origin Resource Sharing (CORS) properties.
     * 
     * @param cors
     *        A {@link CorsProperties} object which represents the CORS properties.
     */
    public void setCors(final CorsProperties cors) {
        this.cors = cors;
    }

    /**
     * Gets default service version.
     * 
     * @return A <code>String</code> which represents the default service version.
     */
    public String getDefaultServiceVersion() {
        return this.defaultServiceVersion;
    }

    /**
     * Sets default service version.
     * 
     * @param defaultServiceVersion
     *        A <code>String</code> which represents the default service version.
     */
    public void setDefaultServiceVersion(final String defaultServiceVersion) {
        this.defaultServiceVersion = defaultServiceVersion;
    }
}
