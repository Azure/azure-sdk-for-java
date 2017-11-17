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

package com.microsoft.windowsazure.services.media.implementation;

import java.net.URI;
import java.util.Arrays;

import javax.inject.Inject;

import com.microsoft.windowsazure.core.UserAgentFilter;
import com.microsoft.windowsazure.core.pipeline.filter.ServiceRequestFilter;
import com.microsoft.windowsazure.core.pipeline.filter.ServiceResponseFilter;
import com.microsoft.windowsazure.core.pipeline.jersey.ClientConfigSettings;
import com.microsoft.windowsazure.core.pipeline.jersey.ClientFilterAdapter;
import com.microsoft.windowsazure.core.pipeline.jersey.ClientFilterRequestAdapter;
import com.microsoft.windowsazure.core.pipeline.jersey.ClientFilterResponseAdapter;
import com.microsoft.windowsazure.core.pipeline.jersey.ServiceFilter;
import com.microsoft.windowsazure.services.media.MediaContract;
import com.microsoft.windowsazure.services.media.WritableBlobContainerContract;
import com.microsoft.windowsazure.services.media.entityoperations.EntityProxyData;
import com.microsoft.windowsazure.services.media.entityoperations.EntityRestProxy;
import com.microsoft.windowsazure.services.media.models.LocatorInfo;
import com.microsoft.windowsazure.services.media.models.LocatorType;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.ClientFilter;

/**
 * The Class MediaRestProxy.
 */
public class MediaRestProxy extends EntityRestProxy implements MediaContract {
    /** The redirect filter. */
    private RedirectFilter redirectFilter;

    private final ClientConfigSettings clientConfigSettings;

    /**
     * Instantiates a new media rest proxy.
     * 
     * @param channel
     *            the channel
     * @param authFilter
     *            the auth filter
     * @param redirectFilter
     *            the redirect filter
     * @param versionHeadersFilter
     *            the version headers filter
     * @param userAgentFilter
     *            the user agent filter
     * @param clientConfigSettings
     *            Currently configured HTTP client settings
     * 
     */
    @Inject
    public MediaRestProxy(Client channel, OAuthFilter authFilter,
            RedirectFilter redirectFilter,
            VersionHeadersFilter versionHeadersFilter,
            UserAgentFilter userAgentFilter,
            ClientConfigSettings clientConfigSettings) {
        super(channel, new ClientFilter[0]);

        this.clientConfigSettings = clientConfigSettings;
        this.redirectFilter = redirectFilter;
        channel.addFilter(redirectFilter);
        channel.addFilter(authFilter);
        channel.addFilter(versionHeadersFilter);
        channel.addFilter(new ClientFilterRequestAdapter(userAgentFilter));
    }

    /**
     * Instantiates a new media rest proxy.
     * 
     * @param channel
     *            the channel
     * @param filters
     *            the filters
     * @param clientConfigSettings
     *            currently configured HTTP client settings
     */
    private MediaRestProxy(Client channel, ClientFilter[] filters,
            ClientConfigSettings clientConfigSettings) {
        super(channel, filters);
        this.clientConfigSettings = clientConfigSettings;
    }

    @Override
    public MediaContract withFilter(ServiceFilter filter) {
        ClientFilter[] currentFilters = getFilters();
        ClientFilter[] newFilters = Arrays.copyOf(currentFilters,
                currentFilters.length + 1);
        newFilters[currentFilters.length] = new ClientFilterAdapter(filter);
        return new MediaRestProxy(getChannel(), newFilters,
                clientConfigSettings);
    }

    @Override
    public MediaContract withRequestFilterFirst(
            ServiceRequestFilter serviceRequestFilter) {
        ClientFilter[] currentFilters = getFilters();
        ClientFilter[] newFilters = new ClientFilter[currentFilters.length + 1];
        System.arraycopy(currentFilters, 0, newFilters, 1,
                currentFilters.length);
        newFilters[0] = new ClientFilterRequestAdapter(serviceRequestFilter);
        return new MediaRestProxy(getChannel(), newFilters,
                clientConfigSettings);
    }

    @Override
    public MediaContract withRequestFilterLast(
            ServiceRequestFilter serviceRequestFilter) {
        ClientFilter[] currentFilters = getFilters();
        ClientFilter[] newFilters = Arrays.copyOf(currentFilters,
                currentFilters.length + 1);
        newFilters[currentFilters.length] = new ClientFilterRequestAdapter(
                serviceRequestFilter);
        return new MediaRestProxy(getChannel(), newFilters,
                clientConfigSettings);
    }

    @Override
    public MediaContract withResponseFilterFirst(
            ServiceResponseFilter serviceResponseFilter) {
        ClientFilter[] currentFilters = getFilters();
        ClientFilter[] newFilters = new ClientFilter[currentFilters.length + 1];
        System.arraycopy(currentFilters, 0, newFilters, 1,
                currentFilters.length);
        newFilters[0] = new ClientFilterResponseAdapter(serviceResponseFilter);
        return new MediaRestProxy(getChannel(), newFilters,
                clientConfigSettings);
    }

    @Override
    public MediaContract withResponseFilterLast(
            ServiceResponseFilter serviceResponseFilter) {
        ClientFilter[] currentFilters = getFilters();
        ClientFilter[] newFilters = Arrays.copyOf(currentFilters,
                currentFilters.length + 1);
        newFilters[currentFilters.length] = new ClientFilterResponseAdapter(
                serviceResponseFilter);
        return new MediaRestProxy(getChannel(), newFilters,
                clientConfigSettings);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.microsoft.windowsazure.services.media.entityoperations.EntityRestProxy
     * #createProxyData()
     */
    @Override
    protected EntityProxyData createProxyData() {
        return new EntityProxyData() {
            @Override
            public URI getServiceUri() {
                return redirectFilter.getBaseURI();
            }
        };
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.microsoft.windowsazure.services.media.MediaContract#createBlobWriter
     * (com.microsoft.windowsazure.services.media.models.LocatorInfo)
     */
    @Override
    public WritableBlobContainerContract createBlobWriter(LocatorInfo locator) {
        if (locator.getLocatorType() != LocatorType.SAS) {
            throw new IllegalArgumentException("Can only write to SAS locators");
        }

        LocatorParser p = new LocatorParser(locator);

        return new MediaBlobContainerWriter(createUploaderClient(),
                p.getAccountName(), p.getStorageUri(), p.getContainer(),
                p.getSASToken());
    }

    /**
     * Helper class to encapsulate pulling information out of the locator.
     */
    private static class LocatorParser {
        private URI locatorPath;

        public LocatorParser(LocatorInfo locator) {
            locatorPath = URI.create(locator.getPath());
        }

        public String getAccountName() {
            return locatorPath.getHost().split("\\.")[0];
        }

        public String getStorageUri() {
            return locatorPath.getScheme() + "://" + locatorPath.getAuthority();
        }

        public String getContainer() {
            return locatorPath.getPath().substring(1);
        }

        public String getSASToken() {
            return locatorPath.getRawQuery();
        }
    }

    private Client createUploaderClient() {
        ClientConfig clientConfig = new DefaultClientConfig();
        clientConfigSettings.applyConfig(clientConfig);
        Client client = Client.create(clientConfig);
        clientConfigSettings.applyConfig(client);
        return client;
    }

}
