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

package com.microsoft.windowsazure.credentials;

import com.microsoft.windowsazure.management.configuration.ManagementConfiguration;

import javax.inject.Inject;
import javax.inject.Named;
import java.net.URI;
import java.util.ArrayList;
import java.util.Map;

/**
 * The Class CertificateCloudCredentials.
 */
public class TokenCloudCredentials extends SubscriptionCloudCredentials {
    
    /** The subscription id. */
    private String subscriptionId;

    /** The uri. */
    private URI uri; 
    
    /** The token */
    private String token;

    /**
     * Instantiates a new certificate cloud credentials.
     */
    public TokenCloudCredentials() {
    }

    /**
     * Instantiates a new certificate cloud credentials.
     *
     * @param uri the uri
     * @param subscriptionId the subscription id
     * @param token the ADAL access token
     */
    @Inject
    public TokenCloudCredentials(@Named(ManagementConfiguration.URI) URI uri, @Named(ManagementConfiguration.SUBSCRIPTION_ID) String subscriptionId,
            String token) {
        this.uri = uri;
        this.subscriptionId = subscriptionId;
        this.token = token;
    }

    /* (non-Javadoc)
     * @see com.microsoft.windowsazure.credentials.SubscriptionCloudCredentials#getSubscriptionId()
     */
    public String getSubscriptionId() {
        return subscriptionId;
    }

    /**
     * Sets the subscription id.
     *
     * @param subscriptionId the new subscription id
     */
    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }
    
    /**
     * Gets the URI.
     *
     * @return the URI
     */
    public URI getUri() {
        return uri;
    }

    /**
     * Sets the URI.
     *
     * @param uri the new URI
     */
    public void setUri(URI uri) {
        this.uri = uri;
    }
    
    /**
     * Gets the ADAL authentication token.
     *
     * @return the ADAL authentication token
     */
    public String getToken() {
        return token;
    }

    /**
     * Sets the ADAL authentication configuration.
     *
     * @param token the new ADAL authentication token
     */
    public void setToken(String token) {
        this.token = token;
    }

    /* (non-Javadoc)
     * @see com.microsoft.windowsazure.credentials.CloudCredentials#applyConfig(java.lang.String, java.util.Map)
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> void applyConfig(String profile, Map<String, Object> properties) {
        properties.put("AuthFilter", new AdalAuthFilter(this.token));
    }

}
