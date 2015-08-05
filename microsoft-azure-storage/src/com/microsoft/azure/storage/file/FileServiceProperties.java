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
        serviceProperties = properties;
        serviceProperties.setHourMetrics(null);
        serviceProperties.setMinuteMetrics(null);
        serviceProperties.setLogging(null);
        serviceProperties.setDefaultServiceVersion(null);
    }

    /**
     * Gets the Cross-Origin Resource Sharing (CORS) properties.
     * 
     * @return A {@link CorsProperties} object which represents the CORS properties.
     */
    public CorsProperties getCors() {
        return serviceProperties.getCors();
    }
    
    /**
     * Sets the Cross-Origin Resource Sharing (CORS) properties.
     * 
     * @param cors
     *        A {@link CorsProperties} object which represents the CORS properties.
     */
    public void setCors(CorsProperties cors) {
        serviceProperties.setCors(cors);
    }
    
    /**
     * Gets the <code>ServiceProperties</code> for use by the service.
     * 
     * @return The <code>ServiceProperties</code>
     */
    ServiceProperties getServiceProperties() {
        return serviceProperties;
    }
}
