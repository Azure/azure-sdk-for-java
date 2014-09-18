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

import com.microsoft.windowsazure.core.pipeline.filter.ServiceRequestFilter;
import com.microsoft.windowsazure.management.configuration.ManagementConfiguration;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * The Class CertificateCloudCredentials.
 */
public class TokenCloudCredentials extends SubscriptionCloudCredentials {
    
    /** The subscription id. */
    private String subscriptionId;

    /** The uri. */
    private URI uri; 
    
    /** The ADAL authorization configuration */
    private AdalAuthConfig adalAuthConfig;

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
     * @param keyStoreCredential the key store credential
     */
    @Inject
    public TokenCloudCredentials(@Named(ManagementConfiguration.URI) URI uri, @Named(ManagementConfiguration.SUBSCRIPTION_ID) String subscriptionId,
            AdalAuthConfig adalAuthConfig) {
        this.uri = uri;
        this.subscriptionId = subscriptionId;
        this.adalAuthConfig = adalAuthConfig;
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
     * Gets the ADAL authentication configuration.
     *
     * @return the ADAL authentication configuration
     */
    public AdalAuthConfig getAdalAuthConfig() {
        return adalAuthConfig;
    }

    /**
     * Sets the ADAL authentication configuration.
     *
     * @param uri the new ADAL authentication configuration
     */
    public void setAdalAuthConfig(AdalAuthConfig adalAuthConfig) {
        this.adalAuthConfig = adalAuthConfig;
    }

    /* (non-Javadoc)
     * @see com.microsoft.windowsazure.credentials.CloudCredentials#applyConfig(java.lang.String, java.util.Map)
     */
    @Override
    public <T> void applyConfig(String profile, Map<String, Object> properties) {
    	ArrayList<ServiceRequestFilter> filters;
    	if (!properties.containsKey("RequestFilters"))
    	{
    		filters = new ArrayList<ServiceRequestFilter>();
    		properties.put("RequestFilters", filters);
    	} else {
    		filters = (ArrayList<ServiceRequestFilter>)properties.get("RequestFilters");
    	}
    	
    	filters.add(new AdalAuthFilter(this.adalAuthConfig));
    }

}
