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
package com.microsoft.windowsazure.services.media;

import java.util.List;

import com.microsoft.windowsazure.services.core.FilterableService;
import com.microsoft.windowsazure.services.core.ServiceException;
import com.microsoft.windowsazure.services.media.models.AssetInfo;
import com.microsoft.windowsazure.services.media.models.CreateAssetOptions;
import com.microsoft.windowsazure.services.media.models.ListAssetsOptions;
import com.microsoft.windowsazure.services.media.models.UpdateAssetOptions;

/**
 * Defines the methods available for Windows Azure Media Services.
 */
public interface MediaContract extends FilterableService<MediaContract> {

    /**
     * List assets.
     * 
     * @param listAssetsOptions
     *            the list assets options
     * @return the list
     * @throws ServiceException
     */
    public List<AssetInfo> listAssets(ListAssetsOptions listAssetsOptions) throws ServiceException;

    /**
     * Delete asset.
     * 
     * @param assetId
     *            the asset id
     * @throws ServiceException
     */
    public void deleteAsset(String assetId) throws ServiceException;

    /**
     * Creates the asset.
     * 
     * @param assetName
     *            the asset name
     * @return the asset info
     * @throws ServiceException
     */
    public AssetInfo createAsset(String assetName) throws ServiceException;

    /**
     * Creates the asset.
     * 
     * @param assetName
     *            the asset name
     * @param createAssetOptions
     *            the create asset options
     * @return the asset info
     * @throws ServiceException
     */
    public AssetInfo createAsset(String assetName, CreateAssetOptions createAssetOptions) throws ServiceException;

    /**
     * Gets the asset.
     * 
     * @param assetId
     *            the asset id
     * @return the asset
     * @throws ServiceException
     */
    public AssetInfo getAsset(String assetId) throws ServiceException;

    /**
     * List assets.
     * 
     * @return the list
     * @throws ServiceException
     */
    public List<AssetInfo> listAssets() throws ServiceException;

    /**
     * Delete asset.
     * 
     * @param assetInfo
     *            the asset info
     * @throws ServiceException
     */
    public void deleteAsset(AssetInfo assetInfo) throws ServiceException;

    /**
     * Update asset.
     * 
     * @param assetId
     *            the asset id
     * @param updateAssetOptions
     *            the update asset options
     * @throws ServiceException
     *             the service exception
     */
    public void updateAsset(String assetId, UpdateAssetOptions updateAssetOptions) throws ServiceException;

}
