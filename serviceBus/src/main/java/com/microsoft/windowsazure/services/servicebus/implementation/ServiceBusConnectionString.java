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

package com.microsoft.windowsazure.services.servicebus.implementation;

import com.microsoft.windowsazure.core.utils.ConnectionStringSyntaxException;
import com.microsoft.windowsazure.core.utils.ParsedConnectionString;

/**
 * Class that parses the fields present in a service bus connection string.
 *
 */
public class ServiceBusConnectionString extends ParsedConnectionString {

    /**
     * Construct the {@link ServiceBusConnectionString} instance with the data
     * from the given connection string
     *
     * @param connectionString
     *            The connection string
     *
     * @throws ConnectionStringSyntaxException
     */
    public ServiceBusConnectionString(String connectionString)
            throws ConnectionStringSyntaxException {
        super(connectionString);
    }

    private String endpoint;
    private String stsEndpoint;
    private String sharedSecretIssuer;
    private String sharedSecretValue;
    private String sharedAccessKeyName;
    private String sharedAccessKey;

    /**
     * Get the endpoint from the connection string
     *
     * @return the endpoint
     */
    public String getEndpoint() {
        return endpoint;
    }

    /**
     * Sets the endpoint value
     *
     * @param endpoint
     */
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    /**
     * Get the StsEndpoint from the connection string
     *
     * @return the sts endpoint
     */
    public String getStsEndpoint() {
        return stsEndpoint;
    }

    /**
     * Sets the StsEndpoint value
     *
     * @param stsEndpoint
     */
    public void setStsEndpoint(String stsEndpoint) {
        this.stsEndpoint = stsEndpoint;
    }

    /**
     * Get the shared secret issuer
     *
     * @return the issuer
     */
    public String getSharedSecretIssuer() {
        return sharedSecretIssuer;
    }

    /**
     * Set the shared secret issuer
     *
     * @param sharedSecretIssuer
     */
    public void setSharedSecretIssuer(String sharedSecretIssuer) {
        this.sharedSecretIssuer = sharedSecretIssuer;
    }

    /**
     * Get the shared secret value
     *
     * @return the shared secret value
     */
    public String getSharedSecretValue() {
        return sharedSecretValue;
    }

    /**
     * Set the shared secret value
     *
     * @param sharedSecretValue
     */
    public void setSharedSecretValue(String sharedSecretValue) {
        this.sharedSecretValue = sharedSecretValue;
    }

    /**
     * @return the sharedAccessKeyName
     */
    public String getSharedAccessKeyName() {
        return sharedAccessKeyName;
    }

    /**
     * @param sharedAccessKeyName the sharedAccessKeyName to set
     */
    public void setSharedAccessKeyName(String sharedAccessKeyName) {
        this.sharedAccessKeyName = sharedAccessKeyName;
    }

    /**
     * @return the sharedAccessKey
     */
    public String getSharedAccessKey() {
        return sharedAccessKey;
    }

    /**
     * @param sharedAccessKey the sharedAccessKey to set
     */
    public void setSharedAccessKey(String sharedAccessKey) {
        this.sharedAccessKey = sharedAccessKey;
    }


}
