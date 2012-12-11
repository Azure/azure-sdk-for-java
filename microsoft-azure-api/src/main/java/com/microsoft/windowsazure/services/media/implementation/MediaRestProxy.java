/**
 * Copyright 2012 Microsoft Corporation
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.microsoft.windowsazure.services.core.ServiceFilter;
import com.microsoft.windowsazure.services.core.utils.pipeline.TimeoutSettings;
import com.microsoft.windowsazure.services.media.MediaContract;
import com.microsoft.windowsazure.services.media.WritableBlobContainerContract;
import com.microsoft.windowsazure.services.media.implementation.entities.EntityRestProxy;
import com.microsoft.windowsazure.services.media.models.LocatorInfo;
import com.microsoft.windowsazure.services.media.models.LocatorType;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

/**
 * The Class MediaRestProxy.
 */
public class MediaRestProxy extends EntityRestProxy implements MediaContract {
    /** The log. */
    static Log log = LogFactory.getLog(MediaContract.class);

    /** The redirect filter. */
    private RedirectFilter redirectFilter;

    private final TimeoutSettings timeoutSettings;

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
     * @param timeoutSettings
     *            Currently configured HTTP client timeouts
     * 
     */
    @Inject
    public MediaRestProxy(Client channel, OAuthFilter authFilter, RedirectFilter redirectFilter,
            VersionHeadersFilter versionHeadersFilter, TimeoutSettings timeoutSettings) {
        super(channel, new ServiceFilter[0]);

        this.timeoutSettings = timeoutSettings;
        this.redirectFilter = redirectFilter;
        channel.addFilter(redirectFilter);
        channel.addFilter(authFilter);
        channel.addFilter(versionHeadersFilter);
    }

    /**
     * Instantiates a new media rest proxy.
     * 
     * @param channel
     *            the channel
     * @param filters
     *            the filters
     * @param timeoutSettings
     *            currently configured HTTP client timeouts
     */
    private MediaRestProxy(Client channel, ServiceFilter[] filters, TimeoutSettings timeoutSettings) {
        super(channel, filters);
        this.timeoutSettings = timeoutSettings;
    }

    /* (non-Javadoc)
     * @see com.microsoft.windowsazure.services.core.FilterableService#withFilter(com.microsoft.windowsazure.services.core.ServiceFilter)
     */
    @Override
    public MediaContract withFilter(ServiceFilter filter) {
        ServiceFilter[] filters = getFilters();
        ServiceFilter[] newFilters = Arrays.copyOf(filters, filters.length + 1);
        newFilters[filters.length] = filter;
        return new MediaRestProxy(getChannel(), newFilters, timeoutSettings);
    }

    /* (non-Javadoc)
     * @see com.microsoft.windowsazure.services.media.MediaContract#getRestServiceUri()
     */
    @Override
    public URI getRestServiceUri() {
        return this.redirectFilter.getBaseURI();
    }

    /* (non-Javadoc)
     * @see com.microsoft.windowsazure.services.media.MediaContract#createBlobWriter(com.microsoft.windowsazure.services.media.models.LocatorInfo)
     */
    @Override
    public WritableBlobContainerContract createBlobWriter(LocatorInfo locator) {
        if (locator.getLocatorType() != LocatorType.SAS) {
            throw new IllegalArgumentException("Can only write to SAS locators");
        }

        LocatorParser p = new LocatorParser(locator);

        return new MediaBlobContainerWriter(createUploaderClient(), p.getAccountName(), p.getStorageUri(),
                p.getContainer(), p.getSASToken());
    }

    /**
     * Helper class to encapsulate pulling information out of the locator.
     */
    private static class LocatorParser {
        URI locatorPath;

        LocatorParser(LocatorInfo locator) {
            locatorPath = URI.create(locator.getPath());
        }

        String getAccountName() {
            return locatorPath.getHost().split("\\.")[0];
        }

        String getStorageUri() {
            return locatorPath.getScheme() + "://" + locatorPath.getAuthority();
        }

        String getContainer() {
            return locatorPath.getPath().substring(1);
        }

        String getSASToken() {
            return locatorPath.getRawQuery();
        }
    }

    private Client createUploaderClient() {
        ClientConfig clientConfig = new DefaultClientConfig();
        timeoutSettings.applyTimeout(clientConfig);
        Client client = Client.create(clientConfig);
        return client;
    }
}