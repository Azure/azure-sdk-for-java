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
package com.microsoft.azure.storage.file;

import com.microsoft.azure.storage.CorsProperties;
import com.microsoft.azure.storage.MetricsProperties;
import com.microsoft.azure.storage.ServiceProperties;

/**
 *  Class representing a set of properties pertaining to the Azure File service.
 */
public class FileServiceProperties {
    private ServiceProperties serviceProperties;

    /**
     * Generate a <code>FileServiceProperties</code> from a new <code>ServiceProperties</code>.
     */
    public FileServiceProperties() {
        this(new ServiceProperties());
    }
    
    /**
     * Generate a <code>FileServiceProperties</code> from the given <code>ServiceProperties</code>.
     * 
     * @param properties
     *          The <code>ServiceProperties</code> to use
     */
    FileServiceProperties(ServiceProperties properties) {
        this.serviceProperties = properties;
        this.serviceProperties.setLogging(null);
        this.serviceProperties.setDefaultServiceVersion(null);
    }

    /**
     * Gets the Cross-Origin Resource Sharing (CORS) properties.
     * 
     * @return A {@link CorsProperties} object which represents the CORS properties.
     */
    public CorsProperties getCors() {
        return this.serviceProperties.getCors();
    }
    
    /**
     * Sets the Cross-Origin Resource Sharing (CORS) properties.
     * 
     * @param cors
     *        A {@link CorsProperties} object which represents the CORS properties.
     */
    public void setCors(CorsProperties cors) {
        this.serviceProperties.setCors(cors);
    }
    
    /**
     * Gets the hour metrics properties.
     * 
     * @return A {@link MetricsProperties} object which represents the hour metrics properties.
     */
    public MetricsProperties getHourMetrics() {
        return this.serviceProperties.getHourMetrics();
    }

    /**
     * Sets the hour metrics properties.
     * 
     * @param metrics
     *        A {@link MetricsProperties} object which represents the hour metrics properties.
     */
    public void setHourMetrics(final MetricsProperties metrics) {
        this.serviceProperties.setHourMetrics(metrics);
    }

    /**
     * Gets the minute metrics properties.
     * 
     * @return A {@link MetricsProperties} object which represents the minute metrics properties.
     */
    public MetricsProperties getMinuteMetrics() {
        return this.serviceProperties.getMinuteMetrics();
    }

    /**
     * Sets the minute metrics properties.
     * 
     * @param metrics
     *        A {@link MetricsProperties} object which represents the minute metrics properties.
     */
    public void setMinuteMetrics(final MetricsProperties metrics) {
        this.serviceProperties.setMinuteMetrics(metrics);
    }

    /**
     * Gets the <code>ServiceProperties</code> for use by the service.
     * 
     * @return The <code>ServiceProperties</code>
     */
    ServiceProperties getServiceProperties() {
        return this.serviceProperties;
    }
}