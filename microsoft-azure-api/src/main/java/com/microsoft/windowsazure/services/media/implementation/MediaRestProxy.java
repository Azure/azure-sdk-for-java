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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.microsoft.windowsazure.services.core.ServiceFilter;
import com.microsoft.windowsazure.services.core.utils.pipeline.ClientFilterAdapter;
import com.microsoft.windowsazure.services.media.MediaContract;
import com.microsoft.windowsazure.services.media.models.AssetInfo;
import com.microsoft.windowsazure.services.media.models.CreateAssetOptions;
import com.microsoft.windowsazure.services.media.models.ListAssetsOptions;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

// TODO: Auto-generated Javadoc
/**
 * The Class MediaRestProxy.
 */
public class MediaRestProxy implements MediaContract {

    /** The channel. */
    private Client channel;

    /** The log. */
    static Log log = LogFactory.getLog(MediaContract.class);

    /** The filters. */
    ServiceFilter[] filters;

    /**
     * Instantiates a new media rest proxy.
     * 
     * @param channel
     *            the channel
     * @param authFilter
     *            the auth filter
     * @param redirectFilter
     *            the redirect filter
     */
    @Inject
    public MediaRestProxy(Client channel, OAuthFilter authFilter, RedirectFilter redirectFilter) {
        this.channel = channel;
        this.filters = new ServiceFilter[0];
        channel.addFilter(redirectFilter);
        channel.addFilter(authFilter);
    }

    /**
     * Instantiates a new media rest proxy.
     * 
     * @param channel
     *            the channel
     * @param filters
     *            the filters
     */
    public MediaRestProxy(Client channel, ServiceFilter[] filters) {
        this.channel = channel;
        this.filters = filters;
    }

    /* (non-Javadoc)
     * @see com.microsoft.windowsazure.services.core.FilterableService#withFilter(com.microsoft.windowsazure.services.core.ServiceFilter)
     */
    @Override
    public MediaContract withFilter(ServiceFilter filter) {
        ServiceFilter[] newFilters = Arrays.copyOf(filters, filters.length + 1);
        newFilters[filters.length] = filter;
        return new MediaRestProxy(channel, newFilters);
    }

    /**
     * Gets the channel.
     * 
     * @return the channel
     */
    public Client getChannel() {
        return channel;
    }

    /**
     * Sets the channel.
     * 
     * @param channel
     *            the new channel
     */
    public void setChannel(Client channel) {
        this.channel = channel;
    }

    /**
     * Gets the resource.
     * 
     * @param entityName
     *            the entity name
     * @return the resource
     */
    private WebResource getResource(String entityName) {
        WebResource resource = getChannel().resource(entityName);
        for (ServiceFilter filter : filters) {
            resource.addFilter(new ClientFilterAdapter(filter));
        }
        return resource;
    }

    /* (non-Javadoc)
     * @see com.microsoft.windowsazure.services.media.MediaContract#createAsset(java.lang.String)
     */
    @Override
    public AssetInfo createAsset(String assetName) {
        CreateAssetOptions createAssetOptions = new CreateAssetOptions();
        return createAsset(assetName, createAssetOptions);
    }

    /* (non-Javadoc)
     * @see com.microsoft.windowsazure.services.media.MediaContract#createAsset(java.lang.String, com.microsoft.windowsazure.services.media.models.CreateAssetOptions)
     */
    @Override
    public AssetInfo createAsset(String assetName, CreateAssetOptions createAssetOptions) {
        return null;
    }

    /* (non-Javadoc)
     * @see com.microsoft.windowsazure.services.media.MediaContract#getAsset(java.lang.String)
     */
    @Override
    public AssetInfo getAsset(String assetId) {
        return null;
    }

    /* (non-Javadoc)
     * @see com.microsoft.windowsazure.services.media.MediaContract#listAssets(com.microsoft.windowsazure.services.media.models.ListAssetsOptions)
     */
    @Override
    public List<AssetInfo> listAssets(ListAssetsOptions listAssetsOptions) {
        List<AssetInfo> listAssetsResult = new ArrayList<AssetInfo>();
        return listAssetsResult;
    }

    /* (non-Javadoc)
     * @see com.microsoft.windowsazure.services.media.MediaContract#listAssets()
     */
    @Override
    public List<AssetInfo> listAssets() {
        ListAssetsOptions listAssetsOptions = new ListAssetsOptions();
        return listAssets(listAssetsOptions);
    }

    /* (non-Javadoc)
     * @see com.microsoft.windowsazure.services.media.MediaContract#updateAsset(com.microsoft.windowsazure.services.media.models.AssetInfo)
     */
    @Override
    public AssetInfo updateAsset(AssetInfo updatedAssetInfo) {
        return null;
    }

    /* (non-Javadoc)
     * @see com.microsoft.windowsazure.services.media.MediaContract#deleteAsset(java.lang.String)
     */
    @Override
    public void deleteAsset(String assetId) {
    }

    /* (non-Javadoc)
     * @see com.microsoft.windowsazure.services.media.MediaContract#deleteAsset(com.microsoft.windowsazure.services.media.models.AssetInfo)
     */
    @Override
    public void deleteAsset(AssetInfo assetInfo) {
        this.deleteAsset(assetInfo.getId());
    }

    /* (non-Javadoc)
     * @see com.microsoft.windowsazure.services.media.MediaContract#createAsset(com.microsoft.windowsazure.services.media.models.AssetInfo)
     */
    @Override
    public AssetInfo createAsset(AssetInfo assetInfo) {
        // TODO Auto-generated method stub
        return null;
    }

}
