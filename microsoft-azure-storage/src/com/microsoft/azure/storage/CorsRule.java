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

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * Represents a Cross-Origin Resource Sharing (CORS) rule.
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
    private EnumSet<CorsHttpMethods> allowedMethods = EnumSet.noneOf(CorsHttpMethods.class);

    /**
     * The length of time in seconds that a preflight response should be cached by browser.
     */
    private int maxAgeInSeconds = 0;

    /**
     * Gets the allowed origins.
     * 
     * @return A <code>List</code> object which contains the allowed origins.
     */
    public List<String> getAllowedOrigins() {
        return this.allowedOrigins;
    }

    /**
     * Sets the allowed origins.
     * 
     * Limited to 64 origins OR "*" to allow all origins, no more than 256 characters each.
     * 
     * @param allowedOrigins
     *            A <code>List</code> object which contains the allowed origins.
     */
    public void setAllowedOrigins(List<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    /**
     * Gets the exposed headers.
     * 
     * @return A <code>List</code> object which contains the exposed headers.
     */
    public List<String> getExposedHeaders() {
        return this.exposedHeaders;
    }

    /**
     * Sets the exposed headers.
     * 
     * Limited to 64 defined headers and two prefixed headers, no more than 256 characters each.
     * 
     * @param exposedHeaders
     *            A <code>List</code> object which contains the exposed headers.
     */
    public void setExposedHeaders(List<String> exposedHeaders) {
        this.exposedHeaders = exposedHeaders;
    }

    /**
     * Gets the allowed headers.
     * 
     * @return A <code>List</code> object which contains the allowed headers.
     */
    public List<String> getAllowedHeaders() {
        return this.allowedHeaders;
    }

    /**
     * Sets the allowed headers.
     * 
     * Limited to 64 defined headers and two prefixed headers, no more than 256 characters each.
     * 
     * @param allowedHeaders
     *            A <code>List</code> object which contains the allowed headers.
     */
    public void setAllowedHeaders(List<String> allowedHeaders) {
        this.allowedHeaders = allowedHeaders;
    }

    /**
     * Gets the allowed methods.
     * 
     * @return A <code>List</code> object which contains the allowed methods.
     */
    public EnumSet<CorsHttpMethods> getAllowedMethods() {
        return this.allowedMethods;
    }

    /**
     * Sets the allowed methods.
     * 
     * @param allowedMethods
     *            A <code>List</code> object which contains the allowed methods.
     */
    public void setAllowedMethods(EnumSet<CorsHttpMethods> allowedMethods) {
        this.allowedMethods = allowedMethods;
    }

    /**
     * Gets the maximum age in seconds.
     * 
     * @return An <code>int</code> which represents the the maximum age in seconds.
     */
    public int getMaxAgeInSeconds() {
        return this.maxAgeInSeconds;
    }

    /**
     * Sets the maximum age in seconds.
     * 
     * @param maxAgeInSeconds
     *            An <code>int</code> which represents the the maximum age in seconds.
     */
    public void setMaxAgeInSeconds(int maxAgeInSeconds) {
        this.maxAgeInSeconds = maxAgeInSeconds;
    }

}
