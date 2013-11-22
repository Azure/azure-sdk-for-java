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

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * Class representing a CORS Rule.
 */
public class CorsRule {

    /**
     * The domain names allowed via CORS.
     */
    private List<String> allowedOrigins = new ArrayList<String>();

    /**
     * The response headers that should be exposed to client via CORS.
     */
    private List<String> exposedHeaders = new ArrayList<String>();

    /**
     * The headers allowed to be part of the CORS request.
     */
    private List<String> allowedHeaders = new ArrayList<String>();

    /**
     * The HTTP methods permitted to execute for the allowedOrigins.
     */
    private EnumSet<CorsHttpMethods> allowedMethods = EnumSet.noneOf(CorsHttpMethods.class);;

    /**
     * The length of time in seconds that a preflight response should be cached by browser.
     */
    private int maxAgeInSeconds = 0;

    /**
     * Get allowed origins.
     * 
     * @return the allowedOrigins
     */
    public List<String> getAllowedOrigins() {
        return allowedOrigins;
    }

    /**
     * Set allowed origins.
     * 
     * Limited to 64 origins OR ‘*’ to allow all origins, no more than 256 characters each.
     * 
     * @param allowedOrigins
     */
    public void setAllowedOrigins(List<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    /**
     * Get exposed headers
     * 
     * @return the exposedHeaders
     */
    public List<String> getExposedHeaders() {
        return exposedHeaders;
    }

    /**
     * Set exposed headers
     * 
     * Limited to 64 defined headers and two prefixed headers, no more than 256 characters each.
     * 
     * @param exposedHeaders
     */
    public void setExposedHeaders(List<String> exposedHeaders) {
        this.exposedHeaders = exposedHeaders;
    }

    /**
     * Get allowed headers
     * 
     * @return allowedHeaders
     */
    public List<String> getAllowedHeaders() {
        return allowedHeaders;
    }

    /**
     * Set allowed headers
     * 
     * Limited to 64 defined headers and two prefixed headers, no more than 256 characters each.
     * 
     * @param allowedHeaders
     */
    public void setAllowedHeaders(List<String> allowedHeaders) {
        this.allowedHeaders = allowedHeaders;
    }

    /**
     * Get allowed methods
     * 
     * @return allowedMethods
     */
    public EnumSet<CorsHttpMethods> getAllowedMethods() {
        return allowedMethods;
    }

    /**
     * Set allowed methods
     * 
     * @param allowedMethods
     */
    public void setAllowedMethods(EnumSet<CorsHttpMethods> allowedMethods) {
        this.allowedMethods = allowedMethods;
    }

    /**
     * Get maxAgeInSeconds
     * 
     * @return maxAgeInSeconds
     */
    public int getMaxAgeInSeconds() {
        return maxAgeInSeconds;
    }

    /**
     * Set maxAgeInSeconds
     * 
     * @param maxAgeInSeconds
     */
    public void setMaxAgeInSeconds(int maxAgeInSeconds) {
        this.maxAgeInSeconds = maxAgeInSeconds;
    }

}
